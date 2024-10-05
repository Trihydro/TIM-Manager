package com.trihydro.library.helpers;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;
import com.trihydro.library.model.ActiveTimError;
import com.trihydro.library.model.ActiveTimHolding;
import com.trihydro.library.model.ActiveTimValidationResult;
import com.trihydro.library.model.ContentEnum;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.MilepostBuffer;
import com.trihydro.library.model.ResubmitTimException;
import com.trihydro.library.model.TimQuery;
import com.trihydro.library.model.TimUpdateModel;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.WydotRsuTim;
import com.trihydro.library.model.WydotTim;
import com.trihydro.library.model.WydotTravelerInputData;
import com.trihydro.library.service.ActiveTimHoldingService;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.CascadeService;
import com.trihydro.library.service.DataFrameService;
import com.trihydro.library.service.MilepostService;
import com.trihydro.library.service.OdeService;
import com.trihydro.library.service.PathNodeLLService;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.SdwService;
import com.trihydro.library.service.TimGenerationProps;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.plugin.RoadSideUnit.RSU;
import us.dot.its.jpo.ode.plugin.SNMP;
import us.dot.its.jpo.ode.plugin.ServiceRequest;
import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW;
import us.dot.its.jpo.ode.plugin.SnmpProtocol;
import us.dot.its.jpo.ode.plugin.j2735.OdeGeoRegion;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.MsgId;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region.Path;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.RoadSignID;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.NodeXY;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.FrameType.TravelerInfoType;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.MutcdCode.MutcdCodeEnum;

@Component
public class TimGenerationHelper {
    private Utility utility;
    private DataFrameService dataFrameService;
    private PathNodeLLService pathNodeLLService;
    private ActiveTimService activeTimService;
    private MilepostService milepostService;
    private CascadeService cascadeService;
    private Gson gson;
    private MilepostReduction milepostReduction;
    private RsuService rsuService;
    private TimGenerationProps config;
    private OdeService odeService;
    private ActiveTimHoldingService activeTimHoldingService;
    private SdwService sdwService;
    private SnmpHelper snmpHelper;
    private RegionNameTrimmer regionNameTrimmer;
    private CreateBaseTimUtil createBaseTimUtil;

    @Autowired
    public TimGenerationHelper(Utility _utility, DataFrameService _dataFrameService,
            PathNodeLLService _pathNodeLLService, ActiveTimService _activeTimService, MilepostService _milepostService,
            MilepostReduction _milepostReduction, TimGenerationProps _config, RsuService _rsuService,
            OdeService _odeService, ActiveTimHoldingService _activeTimHoldingService, SdwService _sdwService,
            SnmpHelper _snmpHelper, CascadeService _cascadeService, RegionNameTrimmer _regionNameTrimmer,
            CreateBaseTimUtil _createBaseTimUtil) {
        gson = new Gson();
        utility = _utility;
        dataFrameService = _dataFrameService;
        pathNodeLLService = _pathNodeLLService;
        activeTimService = _activeTimService;
        milepostService = _milepostService;
        milepostReduction = _milepostReduction;
        config = _config;
        rsuService = _rsuService;
        odeService = _odeService;
        activeTimHoldingService = _activeTimHoldingService;
        sdwService = _sdwService;
        snmpHelper = _snmpHelper;
        cascadeService = _cascadeService;
        regionNameTrimmer = _regionNameTrimmer;
        createBaseTimUtil = _createBaseTimUtil;
    }

    private String getIsoDateTimeString(ZonedDateTime date) {
        if (date == null) {
            return null;
        }

        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        var utcDate = date.withZoneSameInstant(ZoneOffset.UTC);
        return utcDate.format(formatter);
    }

    private String getIsoDateTimeString(Timestamp time) {
        if (time == null) {
            return null;
        }

        var formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return formatter.format(time);
    }

    public List<ResubmitTimException> updateAndResubmitToOde(List<ActiveTimValidationResult> validationResults) {
        List<ResubmitTimException> exceptions = new ArrayList<>();
        if (validationResults == null || validationResults.size() == 0) {
            utility.logWithDate("No validation results found to update and resubmit to ODE.",
                    TimGenerationHelper.class);
            return exceptions;
        }
        // iterate over tims, fetch, and push out
        for (ActiveTimValidationResult validationResult : validationResults) {
            try {
                var activeTimId = validationResult.getActiveTim().getActiveTimId();
                var tum = activeTimService.getUpdateModelFromActiveTimId(activeTimId);

                if (tum == null) {
                    exceptions.add(new ResubmitTimException(activeTimId, "Failed to get Update Model from active tim"));
                    continue;
                }
                if (!isValidTim(tum)) {
                    exceptions.add(new ResubmitTimException(activeTimId,
                            "Failed to generate valid Update Model from active tim"));
                    continue;
                }

                if (tum.getLaneWidth() == null) {
                    tum.setLaneWidth(config.getDefaultLaneWidth());
                } else {
                    // Database has lane width as cm, but ODE takes m
                    tum.setLaneWidth(tum.getLaneWidth().divide(BigDecimal.valueOf(100)));
                }

                WydotTim wydotTim = getWydotTimFromTum(tum);
                List<Milepost> mps = new ArrayList<>();
                List<Milepost> allMps = getAllMps(wydotTim);
                if (allMps.size() < 2) {
                    String exMsg = String.format(
                            "Unable to resubmit TIM, less than 2 mileposts found to determine service area for Active_Tim %d",
                            tum.getActiveTimId());
                    utility.logWithDate(exMsg);
                    exceptions.add(new ResubmitTimException(activeTimId, exMsg));
                    continue;
                }
                Milepost firstPoint = allMps.get(0);
                Milepost secondPoint = allMps.get(1);

                // reduce the mileposts by removing straight away posts
                var anchorMp = getAnchorPoint(firstPoint, secondPoint);
                mps = milepostReduction.applyMilepostReductionAlgorithm(allMps, config.getPathDistanceLimit());
                OdeTravelerInformationMessage tim = getTim(tum, mps, allMps, anchorMp, false);
                if (tim == null) {
                    String exMsg = String.format("Failed to instantiate TIM for active_tim_id %d",
                            tum.getActiveTimId());
                    utility.logWithDate(exMsg);
                    exceptions.add(new ResubmitTimException(activeTimId, exMsg));
                    continue;
                }

                // update the TIM based on validationResult
                tim = updateTim(tum, tim, validationResult, exceptions);

                // check for any exceptions while updating TIM
                if (tim == null || exceptions.size() > 0) {
                    utility.logWithDate("Unable to update TIM (active_tim_id " + tum.getActiveTimId()
                            + ") with validationResult: " + gson.toJson(validationResult));
                    continue;
                }
                WydotTravelerInputData timToSend = new WydotTravelerInputData();
                timToSend.setTim(tim);
                var extraEx = sendTim(timToSend, tum, activeTimId, mps);
                if (extraEx.size() > 0) {
                    exceptions.addAll(extraEx);
                }
            } catch (Exception ex) {
                utility.logWithDate("Failed attempting to update TIM (active_tim_id "
                        + validationResult.getActiveTim().getActiveTimId() + ") with ActiveTimValidationResult: "
                        + gson.toJson(validationResult));
                ex.printStackTrace();
            }
        }
        if (exceptions.size() > 0) {
            utility.logWithDate("Errors occurred while resubmitting TIMs: " + gson.toJson(exceptions),
                    TimGenerationHelper.class);
        }
        return exceptions;
    }

    private OdeTravelerInformationMessage updateTim(TimUpdateModel tum, OdeTravelerInformationMessage tim,
            ActiveTimValidationResult validationResult, List<ResubmitTimException> exceptions) {

        DateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yy HH.MM.SS");
        var rebuildTim = false;
        for (ActiveTimError ate : validationResult.getErrors()) {
            switch (ate.getName()) {
                case endPoint:
                    // FEU is Coordinate
                    var endPt = gson.fromJson(ate.getTmddValue(), Coordinate.class);
                    tum.setEndPoint(endPt);
                    rebuildTim = true;
                    break;

                case endTime:
                    // change the end time on our TIM
                    // note that we are only interested in changing the DataFrame.durationTime here,
                    // as the endDateTime is calculated later based on startDateTime and this
                    // duration
                    // FEU is text formatted as 2020-12-08 09:31:00
                    try {
                        Date endDateTime = dFormat.parse(ate.getTmddValue());
                        int durationTime = utility.getMinutesDurationBetweenTwoDates(tum.getStartDateTime(),
                                simpleDateFormat.format(endDateTime));
                        // J2735 has duration time of 0-32000
                        // the ODE fails if we have greater than 32000
                        if (durationTime > 32000) {
                            durationTime = 32000;
                        }
                        tim.getDataframes()[0].setDurationTime(durationTime);
                    } catch (ParseException e) {
                        String exMsg = String.format("Failed to parse associated FEU date: %s", ate.getTmddValue());
                        utility.logWithDate(exMsg);
                        exceptions.add(new ResubmitTimException(tum.getActiveTimId(), exMsg));
                    }
                    break;

                case itisCodes:
                    // FEU is { ### }
                    // since we will set codes to those in FEU all we need to do is deserialize them
                    // and set
                    var codes = ate.getTmddValue();
                    // remove end brackets
                    codes = codes.replace("{", "");
                    codes = codes.replace("}", "");

                    // codes now looks like: 1234,5678,0987
                    String[] itisCodes = codes.split(",");
                    DataFrame df = tim.getDataframes()[0];
                    df.setItems(itisCodes);
                    break;

                case startPoint:
                    var startPt = gson.fromJson(ate.getTmddValue(), Coordinate.class);
                    tum.setStartPoint(startPt);
                    rebuildTim = true;
                    break;

                case other:
                default:
                    break;
            }
        }

        if (rebuildTim) {
            // rebuild tim...this should maybe be extracted to another function and called
            // from multiple locations
            WydotTim wydotTim = getWydotTimFromTum(tum);
            List<Milepost> mps = new ArrayList<>();
            List<Milepost> allMps = getAllMps(wydotTim);
            if (allMps.size() < 2) {
                String exMsg = String.format(
                        "Unable to resubmit TIM, less than 2 mileposts found to determine service area for Active_Tim %d",
                        tum.getActiveTimId());
                utility.logWithDate(exMsg);
                exceptions.add(new ResubmitTimException(tum.getActiveTimId(), exMsg));
                return null;
            }
            Milepost firstPoint = allMps.get(0);
            Milepost secondPoint = allMps.get(1);

            // reduce the mileposts by removing straight away posts
            var anchorMp = getAnchorPoint(firstPoint, secondPoint);
            mps = milepostReduction.applyMilepostReductionAlgorithm(allMps, config.getPathDistanceLimit());
            tim = getTim(tum, mps, allMps, anchorMp, false);
            if (tim == null) {
                String exMsg = String.format("Failed to instantiate TIM for active_tim_id %d", tum.getActiveTimId());
                utility.logWithDate(exMsg);
                exceptions.add(new ResubmitTimException(tum.getActiveTimId(), exMsg));
                return null;
            }
        }
        return tim;
    }

    private WydotTim getWydotTimFromTum(TimUpdateModel tum) {
        WydotTim wydotTim = new WydotTim();
        wydotTim.setRoute(tum.getRoute());
        wydotTim.setDirection(tum.getDirection());
        wydotTim.setStartPoint(tum.getStartPoint());
        wydotTim.setEndPoint(tum.getEndPoint());
        wydotTim.setClientId(tum.getClientId());
        return wydotTim;
    }

    private List<Milepost> getAllMps(WydotTim wydotTim) {
        List<Milepost> allMps = new ArrayList<>();

        if (!CascadeService.isCascadeTim(wydotTim)) {
            utility.logWithDate("Fetching mileposts for regular TIM with client id: " + wydotTim.getClientId());
            if (wydotTim.getEndPoint() != null) {
                allMps = milepostService.getMilepostsByStartEndPointDirection(wydotTim);
                utility.logWithDate(String.format("Found %d mileposts between %s and %s", allMps.size(),
                        gson.toJson(wydotTim.getStartPoint()), gson.toJson(wydotTim.getEndPoint())));
            } else {
                // point incident
                MilepostBuffer mpb = new MilepostBuffer();
                mpb.setBufferMiles(config.getPointIncidentBufferMiles());
                mpb.setCommonName(wydotTim.getRoute());
                mpb.setDirection(wydotTim.getDirection());
                mpb.setPoint(wydotTim.getStartPoint());
                allMps = milepostService.getMilepostsByPointWithBuffer(mpb);
                utility.logWithDate(String.format("Found %d mileposts for point %s", allMps.size(),
                        gson.toJson(wydotTim.getStartPoint())));
            }
        } else {
            utility.logWithDate("Fetching mileposts for cascade TIM with client id: " + wydotTim.getClientId());
            try {
                allMps = cascadeService.getAllMilepostsFromCascadeTim(wydotTim);
                utility.logWithDate(
                        String.format("Found %d mileposts for cascade TIM with client id: %s", allMps.size(),
                                wydotTim.getClientId()));
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
                utility.logWithDate("Failed to get mileposts from cascade tim: " + ex.getMessage());
            }
        }

        return allMps;
    }

    /**
     * Rebuilds and resubmits TIMs to the ODE
     * 
     * @param activeTimIds TIMs to resubmit
     * @return Errors that occurred while processing the request
     */
    public List<ResubmitTimException> resubmitToOde(List<Long> activeTimIds) {
        List<ResubmitTimException> exceptions = new ArrayList<>();
        if (activeTimIds == null) {
            utility.logWithDate("No active TIMs found to resubmit to ODE. Returning...", TimGenerationHelper.class);
            return exceptions;
        }
        // iterate over tims, fetch, and push out
        for (Long activeTimId : activeTimIds) {
            try {
                var tum = activeTimService.getUpdateModelFromActiveTimId(activeTimId);

                if (tum == null) {
                    exceptions.add(new ResubmitTimException(activeTimId, "Failed to get Update Model from active tim"));
                    continue;
                }
                if (!isValidTim(tum)) {
                    exceptions.add(new ResubmitTimException(activeTimId,
                            "Failed to generate valid Update Model from active tim"));
                    continue;
                }

                if (tum.getLaneWidth() == null) {
                    tum.setLaneWidth(config.getDefaultLaneWidth());
                } else {
                    // Database has lane width as cm, but ODE takes m
                    tum.setLaneWidth(tum.getLaneWidth().divide(BigDecimal.valueOf(100)));
                }

                WydotTim wydotTim = getWydotTimFromTum(tum);
                List<Milepost> reduced_mps = new ArrayList<>();
                List<Milepost> allMps = getAllMps(wydotTim);
                if (allMps.size() < 2) {
                    String exMsg = String.format(
                            "Unable to resubmit TIM, less than 2 mileposts found for Active_Tim %d",
                            tum.getActiveTimId());
                    utility.logWithDate(exMsg);
                    exceptions.add(new ResubmitTimException(activeTimId, exMsg));
                    continue;
                }
                Milepost firstPoint = allMps.get(0);
                Milepost secondPoint = allMps.get(1);

                // reduce the mileposts by removing straight away posts
                var anchorMp = getAnchorPoint(firstPoint, secondPoint);
                reduced_mps = milepostReduction.applyMilepostReductionAlgorithm(allMps, config.getPathDistanceLimit());
                OdeTravelerInformationMessage tim = getTim(tum, reduced_mps, allMps, anchorMp, false, false);
                if (tim == null) {
                    String exMsg = String.format("Failed to instantiate TIM for active_tim_id %d",
                            tum.getActiveTimId());
                    utility.logWithDate(exMsg);
                    exceptions.add(new ResubmitTimException(activeTimId, exMsg));
                    continue;
                }
                WydotTravelerInputData timToSend = new WydotTravelerInputData();
                timToSend.setTim(tim);
                var extraEx = sendTim(timToSend, tum, activeTimId, reduced_mps);
                if (extraEx.size() > 0) {
                    exceptions.addAll(extraEx);
                }
            } catch (Exception ex) {
                exceptions.add(new ResubmitTimException(activeTimId, ex.getMessage()));
            }
        }
        if (exceptions.size() > 0) {
            utility.logWithDate("Errors occurred while resubmitting TIMs: " + gson.toJson(exceptions),
                    TimGenerationHelper.class);
        }
        return exceptions;
    }

    /**
     * Resets TIMs starting time and resubmits TIMs to the ODE
     * 
     * @param activeTimIds TIMs to resubmit
     * @return Errors that occurred while processing the request
     */
    public List<ResubmitTimException> resetTimStartTimeAndResubmitToOde(List<Long> activeTimIds) {
        List<ResubmitTimException> exceptions = new ArrayList<>();
        if (activeTimIds == null) {
            utility.logWithDate("No active TIMs found to resubmit to ODE. Returning...", TimGenerationHelper.class);
            return exceptions;
        }
        // iterate over tims, fetch, and push out
        for (Long activeTimId : activeTimIds) {
            try {
                var tum = activeTimService.getUpdateModelFromActiveTimId(activeTimId);

                if (tum == null) {
                    exceptions.add(new ResubmitTimException(activeTimId, "Failed to get Update Model from active tim"));
                    continue;
                }
                if (!isValidTim(tum)) {
                    exceptions.add(new ResubmitTimException(activeTimId,
                            "Failed to generate valid Update Model from active tim"));
                    continue;
                }

                if (tum.getLaneWidth() == null) {
                    tum.setLaneWidth(config.getDefaultLaneWidth());
                } else {
                    // Database has lane width as cm, but ODE takes m
                    tum.setLaneWidth(tum.getLaneWidth().divide(BigDecimal.valueOf(100)));
                }

                WydotTim wydotTim = getWydotTimFromTum(tum);
                List<Milepost> reduced_mps = new ArrayList<>();
                List<Milepost> allMps = getAllMps(wydotTim);
                if (allMps.size() < 2) {
                    String exMsg = String.format(
                            "Unable to resubmit TIM, less than 2 mileposts found for Active_Tim %d",
                            tum.getActiveTimId());
                    utility.logWithDate(exMsg);
                    exceptions.add(new ResubmitTimException(activeTimId, exMsg));
                    continue;
                }
                Milepost firstPoint = allMps.get(0);
                Milepost secondPoint = allMps.get(1);

                // reduce the mileposts by removing straight away posts
                var anchorMp = getAnchorPoint(firstPoint, secondPoint);
                reduced_mps = milepostReduction.applyMilepostReductionAlgorithm(allMps, config.getPathDistanceLimit());
                OdeTravelerInformationMessage tim = getTim(tum, reduced_mps, allMps, anchorMp, true, false);
                if (tim == null) {
                    String exMsg = String.format("Failed to instantiate TIM for active_tim_id %d",
                            tum.getActiveTimId());
                    utility.logWithDate(exMsg);
                    exceptions.add(new ResubmitTimException(activeTimId, exMsg));
                    continue;
                }
                WydotTravelerInputData timToSend = new WydotTravelerInputData();
                timToSend.setTim(tim);
                var extraEx = sendTim(timToSend, tum, activeTimId, reduced_mps);
                if (extraEx.size() > 0) {
                    exceptions.addAll(extraEx);
                }
            } catch (Exception ex) {
                exceptions.add(new ResubmitTimException(activeTimId, ex.getMessage()));
            }
        }
        if (exceptions.size() > 0) {
            utility.logWithDate("Errors occurred while resubmitting TIMs: " + gson.toJson(exceptions),
                    TimGenerationHelper.class);
        }
        return exceptions;
    }

    /**
     * Expires existing TIMs and resubmits TIMs to the ODE
     * 
     * @param activeTimIds TIMs to resubmit
     * @return Errors that occurred while processing the request
     */
    public List<ResubmitTimException> expireTimAndResubmitToOde(List<Long> activeTimIds) {
        List<ResubmitTimException> exceptions = new ArrayList<>();
        if (activeTimIds == null) {
            utility.logWithDate("No active TIMs found to resubmit to ODE. Returning...", TimGenerationHelper.class);
            return exceptions;
        }
        // iterate over tims, fetch, and push out
        for (Long activeTimId : activeTimIds) {
            try {
                var tum = activeTimService.getUpdateModelFromActiveTimId(activeTimId);

                if (tum == null) {
                    exceptions.add(new ResubmitTimException(activeTimId, "Failed to get Update Model from active tim"));
                    continue;
                }
                if (!isValidTim(tum)) {
                    exceptions.add(new ResubmitTimException(activeTimId,
                            "Failed to generate valid Update Model from active tim"));
                    continue;
                }

                if (tum.getLaneWidth() == null) {
                    tum.setLaneWidth(config.getDefaultLaneWidth());
                } else {
                    // Database has lane width as cm, but ODE takes m
                    tum.setLaneWidth(tum.getLaneWidth().divide(BigDecimal.valueOf(100)));
                }

                WydotTim wydotTim = getWydotTimFromTum(tum);
                List<Milepost> reduced_mps = new ArrayList<>();
                List<Milepost> allMps = getAllMps(wydotTim);
                if (allMps.size() < 2) {
                    String exMsg = String.format(
                            "Unable to resubmit TIM, less than 2 mileposts found for Active_Tim %d",
                            tum.getActiveTimId());
                    utility.logWithDate(exMsg);
                    exceptions.add(new ResubmitTimException(activeTimId, exMsg));
                    continue;
                }
                Milepost firstPoint = allMps.get(0);
                Milepost secondPoint = allMps.get(1);

                // reduce the mileposts by removing straight away posts
                var anchorMp = getAnchorPoint(firstPoint, secondPoint);
                reduced_mps = milepostReduction.applyMilepostReductionAlgorithm(allMps, config.getPathDistanceLimit());

                OdeTravelerInformationMessage tim = getTim(tum, reduced_mps, allMps, anchorMp, false, true);
                if (tim == null) {
                    String exMsg = String.format("Failed to instantiate TIM for active_tim_id %d",
                            tum.getActiveTimId());
                    utility.logWithDate(exMsg);
                    exceptions.add(new ResubmitTimException(activeTimId, exMsg));
                    continue;
                }
                WydotTravelerInputData timToSend = new WydotTravelerInputData();
                timToSend.setTim(tim);
                var extraEx = sendTim(timToSend, tum, activeTimId, reduced_mps);
                activeTimService.markForDeletion(activeTimId);
                if (extraEx.size() > 0) {
                    exceptions.addAll(extraEx);
                }
            } catch (Exception ex) {
                exceptions.add(new ResubmitTimException(activeTimId, ex.getMessage()));
            }
        }
        if (exceptions.size() > 0) {
            utility.logWithDate("Errors occurred while resubmitting TIMs: " + gson.toJson(exceptions),
                    TimGenerationHelper.class);
        }
        return exceptions;
    }

    public boolean isValidTim(TimUpdateModel tum) {

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

    private OdeTravelerInformationMessage getTim(TimUpdateModel aTim, List<Milepost> mps, List<Milepost> allMps,
            Milepost anchor, boolean resetStartTimes) {
        return getTim(aTim, mps, allMps, anchor, resetStartTimes, false);
    }

    private OdeTravelerInformationMessage getTim(TimUpdateModel aTim, List<Milepost> mps, List<Milepost> allMps,
            Milepost anchor, boolean resetStartTimes, boolean resetExpirationTime) {
        String nowAsISO = Instant.now().toString();
        DataFrame dataFrame = getDataFrame(aTim, anchor, resetStartTimes, resetExpirationTime);
        // check to see if we have any itis codes
        // if not, just continue on
        if (dataFrame.getItems() == null || dataFrame.getItems().length == 0) {
            utility.logWithDate("No itis codes found for data_frame " + aTim.getDataFrameId() + ". Skipping...");
            return null;
        }
        List<OdeTravelerInformationMessage.DataFrame.Region> regions = buildRegions(aTim, mps, allMps, anchor);
        dataFrame.setRegions(regions.toArray(new OdeTravelerInformationMessage.DataFrame.Region[regions.size()]));

        DataFrame[] dataframes = new DataFrame[1];
        dataframes[0] = dataFrame;

        OdeTravelerInformationMessage tim = new OdeTravelerInformationMessage();
        tim.setDataframes(dataframes);
        tim.setMsgCnt(getMsgCnt(aTim.getMsgCnt()));
        tim.setPacketID(aTim.getPacketId());

        tim.setTimeStamp(nowAsISO);

        tim.setUrlB(aTim.getUrlB());
        return tim;
    }

    /**
     * Builds a list of regions based on the given TimUpdateModel, reduced
     * mileposts, all mileposts, and anchor milepost.
     * If the number of reduced mileposts is less than or equal to 63, a single
     * region is built from the update model.
     * If the number of reduced mileposts is greater than 63, multiple regions are
     * built from the update model.
     *
     * @param aTim             The TimUpdateModel object containing the update
     *                         information.
     * @param reducedMileposts The list of reduced mileposts.
     * @param allMps           The list of all mileposts.
     * @param anchor           The anchor milepost.
     * @return The list of regions built from the update model.
     */
    private List<OdeTravelerInformationMessage.DataFrame.Region> buildRegions(TimUpdateModel aTim,
            List<Milepost> reducedMileposts, List<Milepost> allMps, Milepost anchor) {
        if (reducedMileposts.size() <= 63) {
            utility.logWithDate("Less than 63 mileposts, building a single region from update model.",
                    TimGenerationHelper.class);
            List<OdeTravelerInformationMessage.DataFrame.Region> regions = new ArrayList<OdeTravelerInformationMessage.DataFrame.Region>();
            OdeTravelerInformationMessage.DataFrame.Region singleRegion = buildSingleRegionFromUpdateModel(aTim,
                    reducedMileposts, allMps, anchor);
            regions.add(singleRegion);
            return regions;
        } else {
            utility.logWithDate("More than 63 mileposts, building multiple regions from update model.",
                    TimGenerationHelper.class);
            return buildMultipleRegionsFromUpdateModel(aTim, reducedMileposts, allMps, anchor);
        }
    }

    /**
     * Builds multiple regions from the given update model.
     *
     * @param aTim             The TimUpdateModel object.
     * @param reducedMileposts The list of reduced mileposts.
     * @param allMps           The list of all mileposts.
     * @param anchor           The anchor milepost.
     * @return The list of OdeTravelerInformationMessage.DataFrame.Region objects.
     */
    private List<OdeTravelerInformationMessage.DataFrame.Region> buildMultipleRegionsFromUpdateModel(
            TimUpdateModel aTim, List<Milepost> reducedMileposts, List<Milepost> allMps, Milepost anchor) {
        List<OdeTravelerInformationMessage.DataFrame.Region> regions = new ArrayList<OdeTravelerInformationMessage.DataFrame.Region>();

        int maxMilepostsPerRegion = 63;

        List<Milepost> milepostsForNextRegion = new ArrayList<Milepost>();
        Milepost nextAnchor = new Milepost(anchor);

        // iterate over the reduced mileposts and build multiple regions
        for (int i = 0; i < reducedMileposts.size(); i++) {
            milepostsForNextRegion.add(reducedMileposts.get(i));
            // if we have reached the max number of mileposts per region, or if we are at
            // the end of the list
            if (milepostsForNextRegion.size() == maxMilepostsPerRegion || i == reducedMileposts.size() - 1) {
                OdeTravelerInformationMessage.DataFrame.Region region = buildSingleRegionFromUpdateModel(aTim,
                        milepostsForNextRegion, allMps, nextAnchor);
                regions.add(region);
                milepostsForNextRegion.clear();
                nextAnchor = reducedMileposts.get(i);
            }
        }

        utility.logWithDate("Built " + regions.size() + " regions from update model.", TimGenerationHelper.class);
        return regions;
    }

    private List<ResubmitTimException> sendTim(WydotTravelerInputData timToSend, TimUpdateModel tum, Long activeTimId,
            List<Milepost> reduced_mps) {
        List<ResubmitTimException> exceptions = new ArrayList<>();
        // try to send to RSU if not a sat TIM and along route with RSUs
        if (StringUtils.isBlank(tum.getSatRecordId())
                && Arrays.asList(config.getRsuRoutes()).contains(tum.getRoute())) {
            var exMsg = updateAndSendRSU(timToSend, tum);
            if (StringUtils.isNotBlank(exMsg)) {
                exceptions.add(new ResubmitTimException(activeTimId, exMsg));
            }
        }

        // only send to SDX if the sat record id exists
        if (!StringUtils.isBlank(tum.getSatRecordId())) {
            var exMsg = updateAndSendSDX(timToSend, tum, reduced_mps);
            if (StringUtils.isNotBlank(exMsg)) {
                exceptions.add(new ResubmitTimException(activeTimId, exMsg));
            }
        } else {
            String exMsg = "active_tim_id " + tum.getActiveTimId()
                    + " not sent to SDX (no SAT_RECORD_ID found in database)";
            utility.logWithDate(exMsg);
        }
        if (exceptions.size() > 0) {
            utility.logWithDate("Errors occurred while resubmitting TIMs: " + gson.toJson(exceptions),
                    TimGenerationHelper.class);
        }
        return exceptions;
    }

    private int getMsgCnt(int currentCnt) {
        if (currentCnt == 127)
            return 1;
        // else increment msgCnt
        else
            return currentCnt++;
    }

    private DataFrame getDataFrame(TimUpdateModel aTim, Milepost anchor, boolean resetStartTimes,
            boolean resetExpirationTime) {
        // RoadSignID
        RoadSignID rsid = new RoadSignID();
        rsid.setPosition(getAnchorPosition(anchor));
        rsid.setViewAngle("1111111111111111");

        // if we are coming in with content=speedLimit and frameType=roadSignage,
        // we need to set the mutcdCode to regulatory to display the regulatory signage
        if (aTim.getDfContent() == ContentEnum.speedLimit && aTim.getFrameType() == TravelerInfoType.roadSignage) {
            rsid.setMutcdCode(MutcdCodeEnum.regulatory);
        } else {
            rsid.setMutcdCode(MutcdCodeEnum.warning);
        }

        // MsgId
        MsgId msgId = new MsgId();
        msgId.setRoadSignID(rsid);

        // DataFrame
        DataFrame df = new DataFrame();
        df.setNotUsed(aTim.getNotUsed());
        df.setFrameType(aTim.getFrameType());
        df.setMsgId(msgId);
        df.setPriority(5);// 0-7, 0 being least important, 7 being most
        df.setNotUsed1(aTim.getNotUsed1());
        df.setNotUsed3(aTim.getNotUsed3());
        df.setNotUsed2(aTim.getNotUsed2());
        if (aTim.getDfContent() != null)
            df.setContent(aTim.getDfContent().getStringValue());
        df.setUrl(aTim.getUrl());

        // set startTime
        // ODE wants a different format: "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        // but we have it stored as "2020-05-15 16:09:00"
        if (resetStartTimes && aTim.getDurationTime() == 32000) {
            var newStart = ZonedDateTime.now();
            df.setStartDateTime(getIsoDateTimeString(newStart));
        } else {
            df.setStartDateTime(getIsoDateTimeString(aTim.getStartDate_Timestamp()));
        }

        // set durationTime
        if (resetExpirationTime) {
            df.setDurationTime(5); // set to non-zero so RSUs send to OBUs
        } else if (aTim.getEndDateTime() != null) {
            int durationTime = utility.getMinutesDurationBetweenTwoDates(df.getStartDateTime(), aTim.getEndDateTime());
            // J2735 has duration time of 0-32000
            // the ODE fails if we have greater than 32000
            if (durationTime > 32000) {
                durationTime = 32000;
            }
            df.setDurationTime(durationTime);
        } else {
            // we don't have an endDate so set duration to max time (never expire)
            df.setDurationTime(32000);
        }

        df.setItems(dataFrameService.getItisCodesForDataFrameId(aTim.getDataFrameId()));
        return df;
    }

    /**
     * Retrieves the anchor position based on the provided TimUpdateModel and
     * Milepost.
     * If the anchor latitude and longitude are not null in the TimUpdateModel, the
     * anchor position is set using those values.
     * If the anchor is not null, the anchor position is set using the latitude and
     * longitude of the anchor.
     * If both the TimUpdateModel and the anchor are null, the anchor position is
     * set to latitude 0, longitude 0, and elevation 0.
     * 
     * @param aTim   The TimUpdateModel containing the anchor latitude and
     *               longitude.
     * @param anchor The Milepost representing the anchor.
     * @return The OdePosition3D object representing the anchor position.
     */
    private OdePosition3D getAnchorPosition(Milepost anchor) {
        OdePosition3D anchorPosition = new OdePosition3D();
        if (anchor != null) {
            anchorPosition.setLatitude(anchor.getLatitude());
            anchorPosition.setLongitude(anchor.getLongitude());
        } else {
            anchorPosition.setLatitude(BigDecimal.valueOf(0));
            anchorPosition.setLongitude(BigDecimal.valueOf(0));
            anchorPosition.setElevation(BigDecimal.valueOf(0));
        }

        return anchorPosition;
    }

    /**
     * Builds a single Region object from the provided TimUpdateModel,
     * reducedMileposts, allMps, and anchor.
     *
     * @param aTim             The TimUpdateModel object containing the data for the
     *                         region.
     * @param reducedMileposts The list of reduced mileposts.
     * @param allMps           The list of all mileposts.
     * @param anchor           The anchor milepost.
     * @return The built Region object.
     */
    private Region buildSingleRegionFromUpdateModel(TimUpdateModel aTim, List<Milepost> reducedMileposts,
            List<Milepost> allMps, Milepost anchor) {
        Region region = new Region();
        // TODO: set name?
        // TODO: set regulator id?

        // set lane width
        region.setLaneWidth(aTim.getLaneWidth());

        // set directionality, default to 3
        String regionDirectionality = aTim.getDirectionality();
        if (regionDirectionality == null || regionDirectionality.isEmpty()) {
            regionDirectionality = "3";
        }
        region.setDirectionality(regionDirectionality);

        // set closed path
        region.setClosedPath(aTim.getClosedPath());

        // set anchor position
        region.setAnchorPosition(getAnchorPosition(anchor));

        // set description
        String regionDescription = aTim.getRegionDescription(); // J2735 - one of path, geometry, oldRegion
        if (regionDescription == null || regionDescription.isEmpty()) {
            regionDescription = "path"; // if null, set it to path...we only support path anyway, and only have tables
                                        // supporting path
        }
        region.setDescription(regionDescription);

        // set direction
        String regionDirection = aTim.getRegionDirection();
        if (regionDirection == null || regionDirection.isEmpty()) {
            boolean isCascadeTim = aTim.getClientId().contains(CascadeService.CASCADE_TIM_ID_DELIMITER);
            regionDirection = createBaseTimUtil.buildHeadingSliceFromMileposts(isCascadeTim, allMps,
                    region.getAnchorPosition());
        }
        region.setDirection(regionDirection); // region direction is a heading slice ie 0001100000000000

        // set path nodes
        if (aTim.getPathId() != null) {
            NodeXY[] nodes = createBaseTimUtil.buildNodePathFromMileposts(reducedMileposts, anchor);
            Path path = new Path();
            path.setScale(0);
            path.setType("ll");// offset path is now standard
            path.setNodes(nodes);
            region.setPath(path);
        }

        return region;
    }

    private String getRsuRegionName(TimUpdateModel aTim, RSU rsu) {
        return getBaseRegionName(aTim, "_RSU-" + rsu.getRsuTarget());
    }

    private String getSATRegionName(TimUpdateModel aTim, String recordId) {
        return getBaseRegionName(aTim, "_SAT-" + recordId);
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

    private String updateAndSendRSU(WydotTravelerInputData timToSend, TimUpdateModel aTim) {
        String exMsg = "";
        List<WydotRsuTim> wydotRsus = rsuService.getFullRsusTimIsOn(aTim.getTimId());
        List<WydotRsu> dbRsus = new ArrayList<WydotRsu>();
        if (wydotRsus == null || wydotRsus.size() <= 0) {
            utility.logWithDate("RSUs not found to update db for active_tim_id " + aTim.getActiveTimId());

            dbRsus = rsuService.getRsusByLatLong(aTim.getDirection(), aTim.getStartPoint(), aTim.getEndPoint(),
                    aTim.getRoute());

            // if no RSUs found
            if (dbRsus.size() == 0) {
                exMsg = "No possible RSUs found for active_tim_id " + aTim.getActiveTimId();
                utility.logWithDate(exMsg);
                return exMsg;
            }
        }
        // set SNMP command
        String startTimeString = aTim.getStartDate_Timestamp() != null
                ? aTim.getStartDate_Timestamp().toInstant().toString()
                : "";
        String endTimeString = aTim.getEndDate_Timestamp() != null ? aTim.getEndDate_Timestamp().toInstant().toString()
                : "";
        SNMP snmp = snmpHelper.getSnmp(startTimeString, endTimeString, timToSend);
        timToSend.setRequest(new ServiceRequest());
        timToSend.getRequest().setSnmp(snmp);

        RSU[] rsus = new RSU[1];
        if (wydotRsus.size() > 0) {
            RSU rsu = null;
            for (int i = 0; i < wydotRsus.size(); i++) {
                var wydotRsu = wydotRsus.get(i);
                // set RSUS
                rsu = new RSU();
                rsu.setSnmpProtocol(SnmpProtocol.NTCIP1218);
                rsu.setRsuIndex(wydotRsu.getIndex());
                rsu.setRsuTarget(wydotRsu.getRsuTarget());
                // rsuUsername, rsuPassword will take ODE defaults.
                rsu.setRsuRetries(3);
                rsu.setRsuTimeout(5000);
                rsus[0] = rsu;
                timToSend.getRequest().setRsus(rsus);

                String regionName = getRsuRegionName(aTim, wydotRsu);
                try {
                    regionName = regionNameTrimmer.trimRegionNameIfTooLong(regionName);
                } catch (IllegalArgumentException e) {
                    utility.logWithDate("Failed to trim region name: " + e.getMessage());
                }
                timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionName);
                utility.logWithDate("Sending TIM to RSU for refresh: " + gson.toJson(timToSend));

                var rsuClearExMsg = odeService.deleteTimFromRsu(rsu, Integer.valueOf(wydotRsu.getIndex()));
                var rsuExMsg = odeService.sendNewTimToRsu(timToSend);

                if (!StringUtils.isEmpty(rsuClearExMsg)) {
                    exMsg += rsuClearExMsg + "\n";
                }

                if (!StringUtils.isEmpty(rsuExMsg)) {
                    exMsg += rsuExMsg + "\n";
                }
            }
        } else {
            // we don't have any existing RSUs, but some fall within the boundary so send
            // new ones there. We need to update requestName in this case
            for (int i = 0; i < dbRsus.size(); i++) {
                String regionName = getRsuRegionName(aTim, dbRsus.get(i));
                try {
                    regionName = regionNameTrimmer.trimRegionNameIfTooLong(regionName);
                } catch (IllegalArgumentException e) {
                    utility.logWithDate("Failed to trim region name: " + e.getMessage());
                }
                timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionName);
                rsus[0] = dbRsus.get(i);
                timToSend.getRequest().setRsus(rsus);

                // get next index
                TimQuery timQuery = odeService.submitTimQuery(dbRsus.get(i), 0);

                // query failed, don't send TIM
                // log the error and continue
                if (timQuery == null) {
                    WydotRsu wydotRsu = (WydotRsu) timToSend.getRequest().getRsus()[0];
                    var tmpErrMsg = "Returning without sending TIM to RSU. submitTimQuery failed for RSU "
                            + gson.toJson(wydotRsu);
                    exMsg += tmpErrMsg + "\n";
                    utility.logWithDate(tmpErrMsg);
                    continue;
                }
                // Fetch existing active_tim_holding records. If other TIMs are en route to this
                // RSU, make sure we don't overwrite their claimed indexes
                List<ActiveTimHolding> existingHoldingRecords = activeTimHoldingService
                        .getActiveTimHoldingForRsu(dbRsus.get(i).getRsuTarget());
                existingHoldingRecords.forEach(x -> timQuery.appendIndex(x.getRsuIndex()));

                // Finally, fetch all active_tims that are supposed to be on this RSU. Some may
                // not be there, due to network or RSU issues. Make sure we don't claim an index
                // that's already been claimed.
                List<Integer> claimedIndexes = rsuService.getActiveRsuTimIndexes(dbRsus.get(i).getRsuId());
                claimedIndexes.forEach(x -> timQuery.appendIndex(x));

                Integer nextRsuIndex = odeService.findFirstAvailableIndexWithRsuIndex(timQuery.getIndicies_set());

                // unable to find next available index
                // log error and continue
                if (nextRsuIndex == null) {
                    WydotRsu wydotRsu = (WydotRsu) timToSend.getRequest().getRsus()[0];
                    var tmpErrMsg = "Unable to find an available index for RSU " + gson.toJson(wydotRsu);
                    exMsg += tmpErrMsg + "\n";
                    utility.logWithDate(tmpErrMsg);
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
                activeTimHolding.setPacketId(timToSend.getTim().getPacketID());
                activeTimHoldingService.insertActiveTimHolding(activeTimHolding);

                // set msgCnt to 1 and create new packetId
                timToSend.getTim().setMsgCnt(1);
                timToSend.getRequest().getRsus()[0].setRsuIndex(nextRsuIndex);

                var newRsuEx = odeService.sendNewTimToRsu(timToSend);
                if (!StringUtils.isEmpty(newRsuEx)) {
                    exMsg += newRsuEx + "\n";
                }
                rsus[0] = dbRsus.get(i);
                timToSend.getRequest().setRsus(rsus);
            }
        }
        return exMsg;
    }

    private String updateAndSendSDX(WydotTravelerInputData timToSend, TimUpdateModel aTim, List<Milepost> reduced_mps) {
        // Ensure request is empty
        timToSend.setRequest(new ServiceRequest());

        SDW sdw = new SDW();
        sdw.setTtl(config.getSdwTtl());
        sdw.setRecordId(aTim.getSatRecordId());

        // fetch all mileposts, get service region by bounding box
        OdeGeoRegion serviceRegion = getServiceRegion(reduced_mps);
        sdw.setServiceRegion(serviceRegion);

        String regionName = getSATRegionName(aTim, aTim.getSatRecordId());
        try {
            regionName = regionNameTrimmer.trimRegionNameIfTooLong(regionName);
        } catch (IllegalArgumentException e) {
            utility.logWithDate("Failed to trim region name: " + e.getMessage());
        }
        timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionName);

        // set sdw block in TIM
        timToSend.getRequest().setSdw(sdw);

        utility.logWithDate("Sending TIM to SDW for refresh: " + gson.toJson(timToSend));
        return odeService.updateTimOnSdw(timToSend);
    }

    private OdeGeoRegion getServiceRegion(List<Milepost> mileposts) {

        Comparator<Milepost> compLat = (l1, l2) -> l1.getLatitude().compareTo(l2.getLatitude());
        Comparator<Milepost> compLong = (l1, l2) -> l1.getLongitude().compareTo(l2.getLongitude());
        OdeGeoRegion serviceRegion = new OdeGeoRegion();

        if (mileposts.size() > 0) {

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

            serviceRegion.setNwCorner(nwCorner);
            serviceRegion.setSeCorner(seCorner);
        } else {
            System.out.println("getServiceRegion fails due to no mileposts");
        }
        return serviceRegion;
    }

    /**
     * This method returns the anchor point for the given mileposts.
     * 
     * @param firstPoint  The first milepost.
     * @param secondPoint The second milepost.
     * @return The anchor point as a Milepost.
     */
    private Milepost getAnchorPoint(Milepost firstPoint, Milepost secondPoint) {
        Coordinate anchorCoordinate = utility.calculateAnchorCoordinate(firstPoint, secondPoint);

        Milepost anchor = new Milepost();
        anchor.setLatitude(anchorCoordinate.getLatitude());
        anchor.setLongitude(anchorCoordinate.getLongitude());
        anchor.setMilepost(firstPoint.getMilepost());
        anchor.setDirection(firstPoint.getDirection());
        return anchor;
    }
}
