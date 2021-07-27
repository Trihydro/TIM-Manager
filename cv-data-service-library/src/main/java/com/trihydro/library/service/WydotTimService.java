package com.trihydro.library.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.trihydro.library.helpers.CreateBaseTimUtil;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.SnmpHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveRsuTimQueryModel;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ActiveTimHolding;
import com.trihydro.library.model.ContentEnum;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.EmailProps;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.MilepostBuffer;
import com.trihydro.library.model.OdeProps;
import com.trihydro.library.model.TimDeleteSummary;
import com.trihydro.library.model.TimQuery;
import com.trihydro.library.model.TimRsu;
import com.trihydro.library.model.TimType;
import com.trihydro.library.model.WydotOdeTravelerInformationMessage;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.WydotTim;
import com.trihydro.library.model.WydotTimRw;
import com.trihydro.library.model.WydotTravelerInputData;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW;
import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW.TimeToLive;
import us.dot.its.jpo.ode.plugin.j2735.OdeGeoRegion;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.FrameType.TravelerInfoType;

@Component
public class WydotTimService {

    protected EmailProps emailProps;
    private OdeProps odeProps;
    private TimGenerationProps genProps;
    protected EmailHelper emailHelper;
    protected TimTypeService timTypeService;
    private SdwService sdwService;
    private Utility utility;
    private OdeService odeService;
    private CreateBaseTimUtil createBaseTimUtil;
    private ActiveTimHoldingService activeTimHoldingService;
    private ActiveTimService activeTimService;
    private TimRsuService timRsuService;
    private RsuService rsuService;
    private TimService timService;
    private SnmpHelper snmpHelper;
    private MilepostService milepostService;

    @Autowired
    public void InjectDependencies(EmailProps _emailProps, OdeProps _odeProps, TimGenerationProps _genProps,
            EmailHelper _emailHelper, TimTypeService _timTypeService, SdwService _sdwService, Utility _utility,
            OdeService _odeService, CreateBaseTimUtil _createBaseTimUtil,
            ActiveTimHoldingService _activeTimHoldingService, ActiveTimService _activeTimService,
            TimRsuService _timRsuService, RestTemplateProvider _restTemplateProvider, RsuService _rsuService,
            TimService _timService, SnmpHelper _snmpHelper, MilepostService _milepostService) {
        emailProps = _emailProps;
        odeProps = _odeProps;
        genProps = _genProps;
        emailHelper = _emailHelper;
        timTypeService = _timTypeService;
        sdwService = _sdwService;
        utility = _utility;
        odeService = _odeService;
        createBaseTimUtil = _createBaseTimUtil;
        activeTimHoldingService = _activeTimHoldingService;
        activeTimService = _activeTimService;
        timRsuService = _timRsuService;
        restTemplateProvider = _restTemplateProvider;
        rsuService = _rsuService;
        timService = _timService;
        snmpHelper = _snmpHelper;
        milepostService = _milepostService;
    }

    private RestTemplateProvider restTemplateProvider;
    private Gson gson = new Gson();
    private List<WydotRsu> rsus;
    private List<TimType> timTypes;
    WydotRsu[] rsuArr = new WydotRsu[1];
    DateTimeFormatter utcformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public WydotTravelerInputData createTim(WydotTim wydotTim, String timTypeStr, String startDateTime,
            String endDateTime, ContentEnum content, TravelerInfoType frameType, List<Milepost> allMileposts,
            List<Milepost> reducedMileposts, Milepost anchor) {

        // build base TIM
        WydotTravelerInputData timToSend = createBaseTimUtil.buildTim(wydotTim, genProps, content, frameType,
                allMileposts, reducedMileposts, anchor);

        if (timToSend == null) {
            return null;
        }

        // overwrite start date/time if one is provided (start date/time has been set to
        // the current time in base tim creation)
        if (startDateTime != null) {
            timToSend.getTim().getDataframes()[0].setStartDateTime(startDateTime);
        }

        // set the duration if there is an enddate
        if (endDateTime != null) {
            int durationTime = utility.getMinutesDurationBetweenTwoDates(startDateTime, endDateTime);
            // J2735 has duration time of 0-32000
            // the ODE fails if we have greater than 32000
            if (durationTime > 32000) {
                durationTime = 32000;
            }
            timToSend.getTim().getDataframes()[0].setDurationTime(durationTime);
        }

        // if parking TIM
        if (timTypeStr.equals("P")) {
            // set duration for two hours
            timToSend.getTim().getDataframes()[0].setDurationTime(120);
        }

        // set PacketId to a random 18 character hex value
        Random rand = new Random();
        StringBuffer sb = new StringBuffer();
        while (sb.length() < 18) {
            sb.append(Integer.toHexString(rand.nextInt()));
        }
        timToSend.getTim().setPacketID(sb.toString().substring(0, 18).toUpperCase());

        return timToSend;
    }

    public List<Milepost> getAllMilepostsForTim(WydotTim wydotTim) {
        List<Milepost> milepostsAll = new ArrayList<>();

        if (wydotTim.getEndPoint() != null && wydotTim.getEndPoint().getLatitude() != null
                && wydotTim.getEndPoint().getLongitude() != null) {
            milepostsAll = milepostService.getMilepostsByStartEndPointDirection(wydotTim);
        } else {
            // point incident
            MilepostBuffer mpb = new MilepostBuffer();
            mpb.setBufferMiles(genProps.getPointIncidentBufferMiles());
            mpb.setCommonName(wydotTim.getRoute());
            mpb.setDirection(wydotTim.getDirection());
            mpb.setPoint(wydotTim.getStartPoint());
            milepostsAll = milepostService.getMilepostsByPointWithBuffer(mpb);
        }

        return milepostsAll;
    }

    public void sendTimToSDW(WydotTim wydotTim, WydotTravelerInputData timToSend, String regionNamePrev,
            TimType timType, Integer pk, Coordinate endPoint) {

        List<ActiveTim> activeSatTims = null;

        // find active TIMs by client Id and direction
        activeSatTims = activeTimService.getActiveTimsByClientIdDirection(wydotTim.getClientId(),
                timType.getTimTypeId(), wydotTim.getDirection());

        // filter by SAT TIMs
        activeSatTims = activeSatTims.stream().filter(x -> x.getSatRecordId() != null).collect(Collectors.toList());

        String recordId = activeSatTims != null && activeSatTims.size() > 0 ? activeSatTims.get(0).getSatRecordId()
                : sdwService.getNewRecordId();

        // save new active_tim_holding record
        ActiveTimHolding activeTimHolding = new ActiveTimHolding(wydotTim, null, recordId, endPoint);
        activeTimHolding.setPacketId(timToSend.getTim().getPacketID());

        // Set projectKey, if this is a RW TIM
        if (wydotTim instanceof WydotTimRw) {
            activeTimHolding.setProjectKey(((WydotTimRw) wydotTim).getProjectKey());
        }

        activeTimHoldingService.insertActiveTimHolding(activeTimHolding);

        // If there is a corresponding Active TIM, reset the expiration date
        if (activeSatTims.size() > 0) {
            var activeTimId = activeSatTims.get(0).getActiveTimId();
            activeTimService.resetActiveTimsExpirationDate(Arrays.asList(activeTimId));
        }

        String regionNameTemp = regionNamePrev + "_SAT-" + recordId + "_" + timType.getType();
        if (wydotTim.getClientId() != null)
            regionNameTemp += "_" + wydotTim.getClientId();

        // add on wydot primary key if it exists
        if (pk != null)
            regionNameTemp += "_" + pk;

        timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNameTemp);

        if (activeSatTims != null && activeSatTims.size() > 0) {

            WydotOdeTravelerInformationMessage tim = timService.getTim(activeSatTims.get(0).getTimId());
            updateTimOnSdw(timToSend, activeSatTims.get(0).getTimId(), activeSatTims.get(0).getSatRecordId(), tim);
        } else {
            sendNewTimToSdw(timToSend, recordId);
        }
    }

    public void sendTimToRsus(WydotTim wydotTim, WydotTravelerInputData timToSend, String regionNamePrev,
            TimType timType, Integer pk, String endDateTime, Coordinate endPoint) {
        // FIND ALL RSUS TO SEND TO
        // TODO: should this query a graph db instead to follow with milepost?
        List<WydotRsu> rsus = rsuService.getRsusByLatLong(wydotTim.getDirection(), wydotTim.getStartPoint(), endPoint,
                wydotTim.getRoute());

        // if no RSUs found
        if (rsus.size() == 0) {
            utility.logWithDate("No RSUs found to place TIM on, returning");
            return;
        }

        // for each rsu in range
        for (WydotRsu rsu : rsus) {

            // update region name for active tim logger
            String regionNameTemp = regionNamePrev + "_RSU-" + rsu.getRsuTarget() + "_" + timType.getType();

            // add clientId to region name
            if (wydotTim.getClientId() != null)
                regionNameTemp += "_" + wydotTim.getClientId();

            // add on wydot primary key to region name if it exists
            if (pk != null)
                regionNameTemp += "_" + pk;

            // set region name -- used for active tim logging
            timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNameTemp);

            // look for active tim on this rsu
            ActiveRsuTimQueryModel artqm = new ActiveRsuTimQueryModel(wydotTim.getDirection(), wydotTim.getClientId(),
                    rsu.getRsuTarget());
            ActiveTim activeTim = activeTimService.getActiveRsuTim(artqm);

            // create new active_tim_holding record
            ActiveTimHolding activeTimHolding = new ActiveTimHolding(wydotTim, rsu.getRsuTarget(), null, endPoint);
            activeTimHolding.setPacketId(timToSend.getTim().getPacketID());

            // Set projectKey, if this is a RW TIM
            if (wydotTim instanceof WydotTimRw) {
                activeTimHolding.setProjectKey(((WydotTimRw) wydotTim).getProjectKey());
            }

            // if active tims exist, update tim
            if (activeTim != null) {
                activeTimHoldingService.insertActiveTimHolding(activeTimHolding);
                WydotOdeTravelerInformationMessage tim = timService.getTim(activeTim.getTimId());

                // Reset the expiration date
                activeTimService.resetActiveTimsExpirationDate(Arrays.asList(activeTim.getActiveTimId()));

                // update TIM rsu
                // add rsu to tim
                rsuArr[0] = rsu;
                timToSend.getRequest().setRsus(rsuArr);
                updateTimOnRsu(timToSend, activeTim.getTimId(), tim, rsu.getRsuId(), endDateTime);
            } else {
                // send new tim to rsu
                // first, determine which indices are currently populated on the RSU
                TimQuery timQuery = odeService.submitTimQuery(rsu, 0);

                // if query failed, don't send TIM,
                // log the error and continue
                if (timQuery == null) {
                    utility.logWithDate(
                            "Returning without sending TIM to RSU. submitTimQuery failed for RSU " + gson.toJson(rsu));
                    continue;
                }

                // Fetch existing active_tim_holding records. If other TIMs are en route to this
                // RSU, make sure we don't overwrite their claimed indexes
                List<ActiveTimHolding> existingHoldingRecords = activeTimHoldingService
                        .getActiveTimHoldingForRsu(rsu.getRsuTarget());
                existingHoldingRecords.forEach(x -> timQuery.appendIndex(x.getRsuIndex()));

                // Finally, fetch all active_tims that are supposed to be on this RSU. Some may
                // not be there, due to network or RSU issues. Make sure we don't claim an index
                // that's already been claimed.
                List<Integer> claimedIndexes = rsuService.getActiveRsuTimIndexes(rsu.getRsuId());
                claimedIndexes.forEach(x -> timQuery.appendIndex(x));

                Integer nextRsuIndex = odeService.findFirstAvailableIndexWithRsuIndex(timQuery.getIndicies_set());

                // if unable to find next available index,
                // log error and continue
                if (nextRsuIndex == null) {
                    utility.logWithDate("Unable to find an available index for RSU " + gson.toJson(rsu));
                    continue;
                }

                activeTimHolding.setRsuIndex(nextRsuIndex);
                activeTimHoldingService.insertActiveTimHolding(activeTimHolding);

                // add rsu to tim
                rsu.setRsuIndex(nextRsuIndex);
                rsuArr[0] = rsu;
                timToSend.getRequest().setRsus(rsuArr);
                var df = timToSend.getTim().getDataframes()[0];
                timToSend.getRequest().setSnmp(snmpHelper.getSnmp(df.getStartDateTime(), endDateTime, timToSend));

                // set msgCnt to 1
                timToSend.getTim().setMsgCnt(1);
                odeService.sendNewTimToRsu(timToSend);
            }
        }
    }

    public TimDeleteSummary deleteTimsFromRsusAndSdx(List<ActiveTim> activeTims) {

        var returnValue = new TimDeleteSummary();
        if (activeTims == null || activeTims.isEmpty()) {
            return returnValue;
        }
        WydotRsu rsu = null;

        // split activeTims into sat and rsu for processing
        List<ActiveTim> satTims = activeTims.stream().filter(x -> StringUtils.isNotBlank(x.getSatRecordId()))
                .collect(Collectors.toList());
        List<ActiveTim> rsuTims = activeTims.stream().filter(x -> StringUtils.isBlank(x.getSatRecordId()))
                .collect(Collectors.toList());

        for (ActiveTim activeTim : rsuTims) {
            // get RSU TIM is on
            List<TimRsu> timRsus = timRsuService.getTimRsusByTimId(activeTim.getTimId());
            // get full RSU

            if (timRsus.size() > 0) {
                for (TimRsu timRsu : timRsus) {
                    rsu = getRsu(timRsu.getRsuId());
                    // delete tim off rsu
                    utility.logWithDate("Deleting TIM from RSU. Corresponding tim_id: " + activeTim.getTimId());
                    if (!deleteTimFromRsu(rsu, timRsu.getRsuIndex())) {
                        returnValue.addfailedRsuTimJson(gson.toJson(timRsu));
                    }
                }
            }
            // delete active tim
            if (activeTimService.deleteActiveTim(activeTim.getActiveTimId())) {
                returnValue.addSuccessfulRsuDeletions(activeTim.getActiveTimId());
            } else {
                returnValue.addFailedActiveTimDeletions(activeTim.getActiveTimId());
            }
        }

        if (satTims != null && satTims.size() > 0) {
            // Get the sat_record_id values and active_tim_id values
            List<String> satRecordIds = satTims.stream().map(ActiveTim::getSatRecordId).collect(Collectors.toList());
            List<Long> activeSatTimIds = satTims.stream().map(ActiveTim::getActiveTimId).collect(Collectors.toList());

            // Issue one delete call to the REST service, encompassing all sat_record_ids
            HashMap<Integer, Boolean> sdxDelResults = sdwService.deleteSdxDataBySatRecordId(satRecordIds);

            // Determine if anything failed
            Boolean errorsOccurred = sdxDelResults.entrySet().stream()
                    .anyMatch(x -> x.getValue() != null && x.getValue() == false);
            if (errorsOccurred) {
                // pull out failed deletions for corresponding active_tim records so we don't
                // orphan them
                Stream<Entry<Integer, Boolean>> failedStream = sdxDelResults.entrySet().stream()
                        .filter(x -> x.getValue() == false);
                List<Integer> failedSatRecords = failedStream.map(x -> x.getKey()).collect(Collectors.toList());

                activeSatTimIds = satTims.stream()
                        .filter(x -> !failedSatRecords.contains(Integer.parseUnsignedInt(x.getSatRecordId(), 16)))
                        .map(ActiveTim::getActiveTimId).collect(Collectors.toList());
                String failedResultsText = sdxDelResults.entrySet().stream().filter(x -> x.getValue() == false)
                        .map(x -> x.getKey().toString()).collect(Collectors.joining(","));
                if (StringUtils.isNotBlank(failedResultsText)) {
                    String body = "The following recordIds failed to delete from the SDX: " + failedResultsText;
                    returnValue.setSatelliteErrorSummary(body);
                    try {
                        emailHelper.SendEmail(emailProps.getAlertAddresses(), "SDX Delete Fail", body);
                    } catch (Exception ex) {
                        utility.logWithDate(body + ", and the email failed to send to support");
                        ex.printStackTrace();
                    }
                }
            }

            if (activeTimService.deleteActiveTimsById(activeSatTimIds)) {
                returnValue.setSuccessfulSatelliteDeletions(activeSatTimIds);
            }
        }
        return returnValue;
    }

    public boolean clearTimsById(String timTypeStr, String clientId, String direction) {
        return clearTimsById(timTypeStr, clientId, direction, false);
    }

    public boolean clearTimsById(String timTypeStr, String clientId, String direction, boolean hasBuffers) {

        List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
        TimType timType = getTimType(timTypeStr);
        activeTims
                .addAll(activeTimService.getActiveTimsByClientIdDirection(clientId, timType.getTimTypeId(), direction));

        if (hasBuffers) {
            activeTims.addAll(activeTimService.getBufferTimsByClientId(clientId));
        }

        utility.logWithDate(activeTims.size() + " active_tim found for deletion");

        deleteTimsFromRsusAndSdx(activeTims);

        return true;
    }

    public boolean deleteWydotTimsByType(List<? extends WydotTim> wydotTims, String timTypeStr) {
        List<ActiveTim> aTims = new ArrayList<ActiveTim>();
        TimType timType = getTimType(timTypeStr);
        aTims = activeTimService.getActiveTimsByWydotTim(wydotTims, timType.getTimTypeId());
        deleteTimsFromRsusAndSdx(aTims);

        return true;
    }

    public List<ActiveTim> selectTimByClientId(String timTypeStr, String clientId) {

        TimType timType = getTimType(timTypeStr);

        List<ActiveTim> activeTims = activeTimService.getActiveTimsByClientIdDirection(clientId, timType.getTimTypeId(),
                null);

        return activeTims;
    }

    public List<ActiveTim> selectTimsByType(String timTypeStr) {

        TimType timType = getTimType(timTypeStr);

        List<ActiveTim> activeTims = activeTimService.getActivesTimByType(timType.getTimTypeId());

        return activeTims;
    }

    public List<WydotRsu> getRsus() {
        if (rsus != null)
            return rsus;
        else {
            rsus = rsuService.selectAll();
            for (WydotRsu rsu : rsus) {
                rsu.setRsuRetries(2);
                rsu.setRsuTimeout(2000);
            }
            return rsus;
        }
    }

    public List<TimType> getTimTypes() {
        if (timTypes != null)
            return timTypes;
        else {
            timTypes = timTypeService.selectAll();
            return timTypes;
        }
    }

    public WydotRsu getRsu(Long rsuId) {

        WydotRsu wydotRsu = null;
        try {
            wydotRsu = getRsus().stream().filter(x -> x.getRsuId() == rsuId.intValue()).findFirst().orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return wydotRsu;
    }

    public void sendNewTimToSdw(WydotTravelerInputData timToSend, String recordId) {

        // set msgCnt to 1 and create new packetId
        timToSend.getTim().setMsgCnt(1);

        SDW sdw = new SDW();

        // calculate service region
        sdw.setServiceRegion(getServiceRegion(timToSend.getMileposts()));

        // set time to live
        sdw.setTtl(getTimeToLive(timToSend.getTim().getDataframes()[0].getDurationTime()));

        // set new record id
        sdw.setRecordId(recordId);

        // set sdw block in TIM
        timToSend.getRequest().setSdw(sdw);

        // send to ODE
        String timToSendJson = gson.toJson(timToSend);

        try {
            utility.logWithDate("Sending new TIM to SDW. sat_record_id: " + recordId);
            restTemplateProvider.GetRestTemplate().postForObject(odeProps.getOdeUrl() + "/tim", timToSendJson,
                    String.class);
        } catch (RuntimeException targetException) {
            System.out.println("Failed to send new TIM to SDW");
            targetException.printStackTrace();
        }
    }

    public void updateTimOnRsu(WydotTravelerInputData timToSend, Long timId, WydotOdeTravelerInformationMessage tim,
            Integer rsuId, String endDateTime) {

        WydotTravelerInputData updatedTim = updateTim(timToSend, timId, tim);

        // set rsu index here
        DataFrame df = updatedTim.getTim().getDataframes()[0];
        TimRsu timRsu = timRsuService.getTimRsu(timId, rsuId);
        updatedTim.getRequest().getRsus()[0].setRsuIndex(timRsu.getRsuIndex());
        updatedTim.getRequest().setSnmp(snmpHelper.getSnmp(df.getStartDateTime(), endDateTime, timToSend));

        try {
            var rsu = getRsu(timRsu.getRsuId());
            utility.logWithDate("Preparing to submit updated TIM. Clearing index " + timRsu.getRsuIndex() + "on RSU "
                    + timRsu.getRsuId());
            // The ODE response code is misleading. If there is a failure in this step or
            // the next, the issue should get addressed when the RSU Validation task is
            // ran.
            deleteTimFromRsu(rsu, timRsu.getRsuIndex());
            odeService.sendNewTimToRsu(updatedTim);
        } catch (Exception ex) {
            utility.logWithDate("Failed to send update to RSU.");
        }
    }

    public void updateTimOnSdw(WydotTravelerInputData timToSend, Long timId, String recordId,
            WydotOdeTravelerInformationMessage tim) {

        WydotTravelerInputData updatedTim = updateTim(timToSend, timId, tim);

        SDW sdw = new SDW();

        // calculate service region
        sdw.setServiceRegion(getServiceRegion(timToSend.getMileposts()));

        // set time to live
        sdw.setTtl(getTimeToLive(timToSend.getTim().getDataframes()[0].getDurationTime()));

        // set new record id
        sdw.setRecordId(recordId);

        // set sdw block in TIM
        updatedTim.getRequest().setSdw(sdw);

        String timToSendJson = gson.toJson(updatedTim);

        // send TIM
        try {
            utility.logWithDate("Updating TIM on SDW. tim_id: " + timId + ", sat_record_id: " + recordId);
            restTemplateProvider.GetRestTemplate().postForObject(odeProps.getOdeUrl() + "/tim", timToSendJson,
                    String.class);
        } catch (RuntimeException targetException) {
            utility.logWithDate("exception updating tim on SDW");
            targetException.printStackTrace();
        }
    }

    public WydotTravelerInputData updateTim(WydotTravelerInputData timToSend, Long timId,
            WydotOdeTravelerInformationMessage tim) {

        // set TIM packetId
        timToSend.getTim().setPacketID(tim.getPacketID());

        // roll msgCnt over to 1 if at 127
        if (tim.getMsgCnt() == 127)
            timToSend.getTim().setMsgCnt(1);
        // else increment msgCnt
        else
            timToSend.getTim().setMsgCnt(tim.getMsgCnt() + 1);

        return timToSend;
    }

    public Boolean deleteTimFromRsu(WydotRsu rsu, Integer index) {

        String rsuJson = gson.toJson(rsu);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(rsuJson, headers);

        utility.logWithDate("Deleting TIM on index " + index.toString() + " from rsu " + rsu.getRsuTarget());
        var response = restTemplateProvider.GetRestTemplate_NoErrors().exchange(
                odeProps.getOdeUrl() + "/tim?index=" + index.toString(), HttpMethod.DELETE, entity, String.class);

        return response.getStatusCode().is2xxSuccessful();
    }

    public TimType getTimType(String timTypeName) {

        // get tim type
        TimType timType = getTimTypes().stream().filter(x -> x.getType().equals(timTypeName)).findFirst().orElse(null);

        return timType;
    }

    protected OdeGeoRegion getServiceRegion(List<Milepost> mileposts) {

        Comparator<Milepost> compLat = (l1, l2) -> l1.getLatitude().compareTo(l2.getLatitude());
        Comparator<Milepost> compLong = (l1, l2) -> l1.getLongitude().compareTo(l2.getLongitude());

        Milepost maxLat = mileposts.stream().max(compLat).get();

        Milepost minLat = mileposts.stream().min(compLat).get();

        Milepost maxLong = mileposts.stream().max(compLong).get();

        Milepost minLong = mileposts.stream().min(compLong).get();

        OdePosition3D nwCorner = new OdePosition3D();
        nwCorner.setLatitude(maxLat.getLatitude());
        nwCorner.setLongitude(minLong.getLongitude());

        OdePosition3D seCorner = new OdePosition3D();
        seCorner.setLatitude(minLat.getLatitude());
        seCorner.setLongitude(maxLong.getLongitude());

        OdeGeoRegion serviceRegion = new OdeGeoRegion();
        serviceRegion.setNwCorner(nwCorner);
        serviceRegion.setSeCorner(seCorner);
        return serviceRegion;
    }

    public Integer[] setBufferItisCodes(String action) {

        Integer[] codes = null;

        if (action.equals("leftClosed")) {
            codes = new Integer[2];
            codes[0] = 777;
            codes[1] = 13580;
        } else if (action.equals("rightClosed")) { // Right lane closed
            codes = new Integer[2];
            codes[0] = 777;
            codes[1] = 13579;
        } else if (action.equals("workers")) {
            codes = new Integer[1];
            codes[0] = 6952;
        } else if (action.equals("surfaceGravel")) { // Gravel
            codes = new Integer[1];
            codes[0] = 5933;
        } else if (action.equals("surfaceMilled")) {
            codes = new Integer[1];
            codes[0] = 6017;
        } else if (action.equals("surfaceDirt")) {
            codes = new Integer[1];
            codes[0] = 6016;
        } else if (action.contains("delay_")) { // XX Minute delays
            codes = new Integer[3];

            // Delay ITIS code
            codes[0] = 1537;

            // number (minutes) ITIS code
            String[] result = action.split("_");
            String number = result[1];
            codes[1] = Integer.parseInt(number) + 12544;

            // mintues ITIS code
            codes[2] = 8728;
        } else if (action.equals("prepareStop")) {
            // content=advisory
            codes = new Integer[1];
            codes[0] = 7186;
        } else if (action.contains("reduceSpeed_")) { // Construction zone speed limit XX from delay on Con Admin
            // content=speedLimit
            codes = new Integer[3];
            // Reduced speed ITIS code
            codes[0] = 7443;

            // number ITIS code
            String[] result = action.split("_");
            String number = result[1];
            codes[1] = Integer.parseInt(number) + 12544;

            // MPH ITIS code
            codes[2] = 8720;
        }

        return codes;
    }

    /**
     * Get's appropriate SDX TTL based on duration
     * 
     * @param duration DataFrame duration (minutes), up to 32000 per J2735
     */
    private TimeToLive getTimeToLive(int duration) {
        if (duration == 32000) {
            // ODE defaults to thirtyminutes, if TTL is null. To get around this,
            // we'll just pass the largest TTL value: oneyear.
            return TimeToLive.oneyear;
        } else if (duration <= 1) {
            return TimeToLive.oneminute;
        } else if (duration <= 30) {
            return TimeToLive.thirtyminutes;
        } else if (duration <= 60 * 24) {
            return TimeToLive.oneday;
        } else if (duration <= 60 * 24 * 7) {
            return TimeToLive.oneweek;
        } else if (duration <= 60 * 24 * 31) {
            return TimeToLive.onemonth;
        } else {
            // duration isn't indefinite, but it also doesn't fit in one of the smaller
            // TTL buckets. Return the largest, finite TTL value.
            return TimeToLive.oneyear;
        }
    }
}