package com.trihydro.timrefresh;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.MilepostReduction;
import com.trihydro.library.helpers.TimGenerationHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTimHolding;
import com.trihydro.library.model.AdvisorySituationDataDeposit;
import com.trihydro.library.model.Logging_TimUpdateModel;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.MilepostBuffer;
import com.trihydro.library.model.TimQuery;
import com.trihydro.library.model.TimUpdateModel;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.WydotRsuTim;
import com.trihydro.library.model.WydotTim;
import com.trihydro.library.model.WydotTravelerInputData;
import com.trihydro.library.service.ActiveTimHoldingService;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.MilepostService;
import com.trihydro.library.service.OdeService;
import com.trihydro.library.service.RegionService;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.SdwService;
import com.trihydro.timrefresh.config.TimRefreshConfiguration;
import com.trihydro.timrefresh.service.WydotTimService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.plugin.RoadSideUnit.RSU;
import us.dot.its.jpo.ode.plugin.SNMP;
import us.dot.its.jpo.ode.plugin.ServiceRequest;
import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW;
import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW.TimeToLive;
import us.dot.its.jpo.ode.plugin.j2735.OdeGeoRegion;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;

@Component
public class TimRefreshController {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    public Gson gson = new Gson();
    protected TimRefreshConfiguration configuration;
    private SdwService sdwService;
    private Utility utility;
    private EmailHelper emailHelper;
    private OdeService odeService;
    private MilepostService milepostService;
    private ActiveTimHoldingService activeTimHoldingService;
    private ActiveTimService activeTimService;
    private RegionService regionService;
    private RsuService rsuService;
    private WydotTimService wydotTimService;
    private MilepostReduction milepostReduction;
    private TimGenerationHelper timGenerationHelper;

    @Autowired
    public TimRefreshController(TimRefreshConfiguration configurationRhs, SdwService _sdwService, Utility _utility,
            OdeService _odeService, MilepostService _milepostService, ActiveTimHoldingService _activeTimHoldingService,
            ActiveTimService _activeTimService, RegionService _regionService, RsuService _rsuService,
            WydotTimService _WydotTimService, MilepostReduction _milepostReduction, EmailHelper _emailHelper,
            TimGenerationHelper _timGenerationHelper) {
        configuration = configurationRhs;
        sdwService = _sdwService;
        utility = _utility;
        odeService = _odeService;
        milepostService = _milepostService;
        activeTimHoldingService = _activeTimHoldingService;
        activeTimService = _activeTimService;
        regionService = _regionService;
        rsuService = _rsuService;
        wydotTimService = _WydotTimService;
        milepostReduction = _milepostReduction;
        emailHelper = _emailHelper;
        timGenerationHelper = _timGenerationHelper;
    }

    @Scheduled(cron = "${cron.expression}") // run at 1:00am every day
    public void performTaskUsingCron() {
        System.out.println("Regular task performed using Cron at " + dateFormat.format(new Date()));

        // fetch Active_TIM that are expiring within 24 hrs
        List<TimUpdateModel> expiringTims = activeTimService.getExpiringActiveTims();

        System.out.println(expiringTims.size() + " expiring TIMs found");
        List<Logging_TimUpdateModel> invalidTims = new ArrayList<Logging_TimUpdateModel>();

        // loop through and issue new TIM to ODE
        for (TimUpdateModel aTim : expiringTims) {
            System.out.println("------ Processing active_tim with id: " + aTim.getActiveTimId());

            if (aTim.getLaneWidth() == null) {
                aTim.setLaneWidth(configuration.getDefaultLaneWidth());
            }

            // Validation

            if (!isValidTim(aTim)) {
                invalidTims.add(new Logging_TimUpdateModel(aTim));
                continue;
            }

            // Mileposts
            WydotTim wydotTim = new WydotTim();
            wydotTim.setRoute(aTim.getRoute());
            wydotTim.setDirection(aTim.getDirection());
            wydotTim.setStartPoint(aTim.getStartPoint());
            wydotTim.setEndPoint(aTim.getEndPoint());

            List<Milepost> mps = new ArrayList<>();
            List<Milepost> allMps = new ArrayList<>();
            if (wydotTim.getEndPoint() != null) {
                mps = milepostService.getMilepostsByStartEndPointDirection(wydotTim);
                utility.logWithDate(String.format("Found %d mileposts between %s and %s", mps.size(),
                        gson.toJson(wydotTim.getStartPoint()), gson.toJson(wydotTim.getEndPoint())));
            } else {
                // point incident
                MilepostBuffer mpb = new MilepostBuffer();
                mpb.setBufferMiles(configuration.getPointIncidentBufferMiles());
                mpb.setCommonName(wydotTim.getRoute());
                mpb.setDirection(wydotTim.getDirection());
                mpb.setPoint(wydotTim.getStartPoint());
                allMps = milepostService.getMilepostsByPointWithBuffer(mpb);
                utility.logWithDate(String.format("Found %d mileposts for point %s", mps.size(),
                        gson.toJson(wydotTim.getStartPoint())));
            }
            // reduce the mileposts by removing straight away posts
            mps = milepostReduction.applyMilepostReductionAlorithm(allMps, configuration.getPathDistanceLimit());

            if (mps.size() == 0) {
                utility.logWithDate(String.format(
                        "Unable to send TIM to SDX, no mileposts found to determine service area for Active_Tim %s",
                        aTim.getActiveTimId()));
                continue;
            }

            OdeTravelerInformationMessage tim = timGenerationHelper.getTim(aTim, mps, allMps);
            if (tim == null) {
                utility.logWithDate(
                        String.format("Failed to instantiate TIM for active_tim_id %s", aTim.getActiveTimId()));
                continue;
            }
            WydotTravelerInputData timToSend = new WydotTravelerInputData();
            timToSend.setRequest(new ServiceRequest());
            timToSend.setTim(tim);

            // try to send to RSU if along route with RSUs
            if (Arrays.asList(configuration.getRsuRoutes()).contains(aTim.getRoute())) {
                updateAndSendRSU(timToSend, aTim);
            }

            // only send to SDX if the sat record id exists
            if (!StringUtils.isEmpty(aTim.getSatRecordId()) && !StringUtils.isBlank(aTim.getSatRecordId())) {
                updateAndSendSDX(timToSend, aTim, mps);
            } else {
                utility.logWithDate("active_tim_id " + aTim.getActiveTimId()
                        + " not sent to SDX (no SAT_RECORD_ID found in database)");
            }
        }

        if (invalidTims.size() > 0) {
            String body = "The Tim Refresh application found invalid TIM(s) while attempting to refresh.";
            body += "<br/>";
            body += "The associated ActiveTim records are: <br/>";
            for (Logging_TimUpdateModel timUpdateModel : invalidTims) {
                body += gson.toJson(timUpdateModel);
                body += "<br/><br/>";
            }

            try {
                utility.logWithDate(
                        "Sending error email. The following TIM exceptions were found: " + gson.toJson(body));
                emailHelper.SendEmail(configuration.getAlertAddresses(), null, "TIM Refresh Invalid TIM", body,
                        configuration.getMailPort(), configuration.getMailHost(), configuration.getFromEmail());
            } catch (Exception e) {
                utility.logWithDate("Exception attempting to send email for invalid TIM:");
                e.printStackTrace();
            }
        }
    }

    private boolean isValidTim(TimUpdateModel tum) {

        // start point
        var stPt = tum.getStartPoint();
        if (stPt == null || stPt.getLatitude() == null || stPt.getLongitude() == null)
            return false;

        // direction
        if (tum.getDirection() == null || tum.getDirection().isEmpty())
            return false;

        // route
        if (tum.getRoute() == null || tum.getRoute().isEmpty())
            return false;

        return true;
    }

    private void updateAndSendRSU(WydotTravelerInputData timToSend, TimUpdateModel aTim) {
        List<WydotRsuTim> wydotRsus = rsuService.getFullRsusTimIsOn(aTim.getTimId());
        List<WydotRsu> dbRsus = new ArrayList<WydotRsu>();
        if (wydotRsus == null || wydotRsus.size() <= 0) {
            utility.logWithDate("RSUs not found to update db for active_tim_id " + aTim.getActiveTimId());

            dbRsus = rsuService.getRsusByLatLong(aTim.getDirection(), aTim.getStartPoint(), aTim.getEndPoint(),
                    aTim.getRoute());

            // if no RSUs found
            if (dbRsus.size() == 0) {
                utility.logWithDate("No possible RSUs found for active_tim_id " + aTim.getActiveTimId());
                return;
            }
        }
        // set SNMP command
        String startTimeString = aTim.getStartDate_Timestamp() != null
                ? aTim.getStartDate_Timestamp().toInstant().toString()
                : "";
        String endTimeString = aTim.getEndDate_Timestamp() != null ? aTim.getEndDate_Timestamp().toInstant().toString()
                : "";
        SNMP snmp = odeService.getSnmp(startTimeString, endTimeString, timToSend);
        timToSend.getRequest().setSnmp(snmp);

        RSU[] rsus = new RSU[1];
        if (wydotRsus.size() > 0) {
            rsus = new RSU[wydotRsus.size()];
            RSU rsu = null;
            for (int i = 0; i < wydotRsus.size(); i++) {
                // set RSUS
                rsu = new RSU();
                rsu.setRsuIndex(wydotRsus.get(i).getRsuIndex());
                rsu.setRsuTarget(wydotRsus.get(i).getRsuTarget());
                rsu.setRsuUsername(wydotRsus.get(i).getRsuUsername());
                rsu.setRsuPassword(wydotRsus.get(i).getRsuPassword());
                rsu.setRsuRetries(2);
                rsu.setRsuTimeout(5000);
                rsus[0] = rsu;
                timToSend.getRequest().setRsus(rsus);

                timToSend.getTim().getDataframes()[0].getRegions()[0].setName(getRsuRegionName(aTim, rsu));
                System.out.println("Sending TIM to RSU for refresh: " + gson.toJson(timToSend));
                wydotTimService.updateTimOnRsu(timToSend);
            }
        } else {
            // we don't have any existing RSUs, but some fall within the boundary so send
            // new ones there. We need to update requestName in this case
            for (int i = 0; i < dbRsus.size(); i++) {
                timToSend.getTim().getDataframes()[0].getRegions()[0].setName(getRsuRegionName(aTim, dbRsus.get(i)));
                rsus[0] = dbRsus.get(i);
                timToSend.getRequest().setRsus(rsus);

                // get next index
                // first fetch existing active_tim_holding records
                List<ActiveTimHolding> existingHoldingRecords = activeTimHoldingService
                        .getActiveTimHoldingForRsu(dbRsus.get(i).getRsuTarget());
                TimQuery timQuery = odeService.submitTimQuery(dbRsus.get(i), 0);

                // query failed, don't send TIM
                // log the error and continue
                if (timQuery == null) {
                    WydotRsu wydotRsu = (WydotRsu) timToSend.getRequest().getRsus()[0];
                    utility.logWithDate("Returning without sending TIM to RSU. submitTimQuery failed for RSU "
                            + gson.toJson(wydotRsu));
                    continue;
                }

                existingHoldingRecords.forEach(x -> timQuery.appendIndex(x.getRsuIndex()));
                Integer nextRsuIndex = odeService.findFirstAvailableIndexWithRsuIndex(timQuery.getIndicies_set());

                // unable to find next available index
                // log error and continue
                if (nextRsuIndex == null) {
                    WydotRsu wydotRsu = (WydotRsu) timToSend.getRequest().getRsus()[0];
                    utility.logWithDate("Unable to find an available index for RSU " + gson.toJson(wydotRsu));
                    continue;
                }

                // create new active_tim_holding record, to account for any index changes
                WydotTim wydotTim = new WydotTim();
                wydotTim.setClientId(aTim.getClientId());
                wydotTim.setDirection(aTim.getDirection());
                wydotTim.setStartPoint(aTim.getStartPoint());
                wydotTim.setEndPoint(aTim.getEndPoint());
                ActiveTimHolding activeTimHolding = new ActiveTimHolding(wydotTim, dbRsus.get(i).getRsuTarget(), null,
                        aTim.getEndPoint());
                activeTimHolding.setRsuIndex(nextRsuIndex);
                activeTimHoldingService.insertActiveTimHolding(activeTimHolding);

                odeService.sendNewTimToRsu(timToSend, aTim.getEndDateTime(), nextRsuIndex);
                rsus[0] = dbRsus.get(i);
                timToSend.getRequest().setRsus(rsus);
            }
        }
    }

    private void updateAndSendSDX(WydotTravelerInputData timToSend, TimUpdateModel aTim, List<Milepost> mps) {
        // remove rsus from TIM
        timToSend.getRequest().setRsus(null);
        SDW sdw = new SDW();
        AdvisorySituationDataDeposit asdd = sdwService.getSdwDataByRecordId(aTim.getSatRecordId());
        if (asdd == null) {
            System.out.println("SAT record not found for id " + aTim.getSatRecordId());
            updateAndSendNewSDX(timToSend, aTim, mps);
            return;
        }

        // fetch all mileposts, get service region by bounding box
        OdeGeoRegion serviceRegion = wydotTimService.getServiceRegion(mps);

        // we are saving our ttl unencoded at the root level of the object as an int
        // representing the enum
        // the DOT sdw ttl goes by string, so we need to do a bit of translation here
        TimeToLive ttl = TimeToLive.valueOf(asdd.getTimeToLive().getStringValue());
        sdw.setTtl(ttl);
        sdw.setRecordId(aTim.getSatRecordId());
        sdw.setServiceRegion(serviceRegion);

        // set sdw block in TIM
        utility.logWithDate("Sending TIM to SDW for refresh: " + gson.toJson(timToSend));
        timToSend.getRequest().setSdw(sdw);
        wydotTimService.updateTimOnSdw(timToSend);
    }

    private void updateAndSendNewSDX(WydotTravelerInputData timToSend, TimUpdateModel aTim, List<Milepost> mps) {
        String recordId = sdwService.getNewRecordId();
        System.out.println("Generating new SAT id and TIM: " + recordId);
        String regionName = getSATRegionName(aTim, recordId);

        // Update region.name in database
        regionService.updateRegionName(Long.valueOf(aTim.getRegionId()), regionName);
        // Update active_tim.
        activeTimService.updateActiveTim_SatRecordId(aTim.getActiveTimId(), recordId);
        timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionName);
        wydotTimService.sendNewTimToSdw(timToSend, recordId, mps);
    }

    private String getRsuRegionName(TimUpdateModel aTim, RSU rsu) {
        return getBaseRegionName(aTim, "_RSU-" + rsu.getRsuTarget());
    }

    private String getBaseRegionName(TimUpdateModel aTim, String middle) {
        String regionName = aTim.getDirection();
        regionName += "_" + aTim.getRoute();
        regionName += middle;// SAT_xxx or RSU_xxx

        String timType = aTim.getTimTypeName();
        if (timType == null || timType.isEmpty()) {
            timType = "RC";// defaulting to Road Condition
        }
        // the rest depend on each other to be there for indexing
        // note that if we don't have a type, our logger inserts a new active_tim rather
        // than updating
        regionName += "_" + timType;

        if (aTim.getClientId() != null) {
            regionName += "_" + aTim.getClientId();

            if (aTim.getPk() != null) {
                regionName += "_" + aTim.getPk();
            }
        }
        return regionName;
    }

    private String getSATRegionName(TimUpdateModel aTim, String recordId) {

        // name is direction_route_startMP_endMP_SAT-satRecordId_TIMType_ClientId_pk
        String oldName = aTim.getRegionName();
        if (oldName != null && oldName.length() > 0) {
            // just replace existing satRecordId with new
            return oldName.replace(aTim.getSatRecordId(), recordId);
        } else {
            // generating from scratch...
            return getBaseRegionName(aTim, "_SAT-" + recordId);
        }
    }
}