package com.trihydro.library.helpers;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.grum.geocalc.Coordinate;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.MilepostReduction;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTimHolding;
import com.trihydro.library.model.AdvisorySituationDataDeposit;
import com.trihydro.library.model.Logging_TimUpdateModel;
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
import com.trihydro.library.service.DataFrameService;
import com.trihydro.library.service.MilepostService;
import com.trihydro.library.service.OdeService;
import com.trihydro.library.service.PathNodeXYService;
import com.trihydro.library.service.RegionService;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.SdwService;
import com.trihydro.library.service.TimGenerationProps;

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
    private PathNodeXYService pathNodeXYService;
    private ActiveTimService activeTimService;
    private MilepostService milepostService;
    private Gson gson;
    private MilepostReduction milepostReduction;
    private RegionService regionService;
    private RsuService rsuService;
    private TimGenerationProps config;
    private OdeService odeService;
    private ActiveTimHoldingService activeTimHoldingService;
    private SdwService sdwService;

    @Autowired
    public TimGenerationHelper(Utility _utility, DataFrameService _dataFrameService,
            PathNodeXYService _pathNodeXYService, ActiveTimService _activeTimService, MilepostService _milepostService,
            MilepostReduction _milepostReduction, TimGenerationProps _config, RegionService _regionService,
            RsuService _rsuService, OdeService _odeService, ActiveTimHoldingService _activeTimHoldingService,
            SdwService _sdwService) {
        gson = new Gson();
        utility = _utility;
        dataFrameService = _dataFrameService;
        pathNodeXYService = _pathNodeXYService;
        activeTimService = _activeTimService;
        milepostService = _milepostService;
        milepostReduction = _milepostReduction;
        config = _config;
        regionService = _regionService;
        rsuService = _rsuService;
        odeService = _odeService;
        activeTimHoldingService = _activeTimHoldingService;
        sdwService = _sdwService;
    }

    public List<ResubmitTimException> resubmitToOde(List<Long> activeTimIds) {
        List<ResubmitTimException> exceptions = new ArrayList<>();
        // iterate over tims, fetch, and push out
        for (Long activeTimId : activeTimIds) {
            var tum = activeTimService.getUpdateModelFromActiveTimId(activeTimId);

            if (tum.getLaneWidth() == null) {
                tum.setLaneWidth(config.getDefaultLaneWidth());
            }

            // Mileposts
            WydotTim wydotTim = new WydotTim();
            wydotTim.setRoute(tum.getRoute());
            wydotTim.setDirection(tum.getDirection());
            wydotTim.setStartPoint(tum.getStartPoint());
            wydotTim.setEndPoint(tum.getEndPoint());

            List<Milepost> mps = new ArrayList<>();
            List<Milepost> allMps = new ArrayList<>();
            if (wydotTim.getEndPoint() != null) {
                mps = milepostService.getMilepostsByStartEndPointDirection(wydotTim);
                utility.logWithDate(String.format("Found %d mileposts between %s and %s", mps.size(),
                        gson.toJson(wydotTim.getStartPoint()), gson.toJson(wydotTim.getEndPoint())));
            } else {
                // point incident
                MilepostBuffer mpb = new MilepostBuffer();
                mpb.setBufferMiles(config.getPointIncidentBufferMiles());
                mpb.setCommonName(wydotTim.getRoute());
                mpb.setDirection(wydotTim.getDirection());
                mpb.setPoint(wydotTim.getStartPoint());
                allMps = milepostService.getMilepostsByPointWithBuffer(mpb);
                utility.logWithDate(String.format("Found %d mileposts for point %s", mps.size(),
                        gson.toJson(wydotTim.getStartPoint())));
            }
            // reduce the mileposts by removing straight away posts
            mps = milepostReduction.applyMilepostReductionAlorithm(allMps, config.getPathDistanceLimit());

            if (mps.size() == 0) {
                String exMsg = String.format(
                        "Unable to send TIM to SDX, no mileposts found to determine service area for Active_Tim %s",
                        tum.getActiveTimId());
                utility.logWithDate(exMsg);
                exceptions.add(new ResubmitTimException(activeTimId, exMsg));
                continue;
            }

            OdeTravelerInformationMessage tim = getTim(tum, mps, allMps);
            if (tim == null) {
                String exMsg = String.format("Failed to instantiate TIM for active_tim_id %s", tum.getActiveTimId());
                utility.logWithDate(exMsg);
                exceptions.add(new ResubmitTimException(activeTimId, exMsg));
                continue;
            }
            WydotTravelerInputData timToSend = new WydotTravelerInputData();
            timToSend.setRequest(new ServiceRequest());
            timToSend.setTim(tim);

            // try to send to RSU if along route with RSUs
            if (Arrays.asList(config.getRsuRoutes()).contains(tum.getRoute())) {
                var exMsg = updateAndSendRSU(timToSend, tum);
                if (StringUtils.isNotBlank(exMsg)) {
                    exceptions.add(new ResubmitTimException(activeTimId, exMsg));
                }
            }

            // only send to SDX if the sat record id exists
            if (!StringUtils.isEmpty(tum.getSatRecordId()) && !StringUtils.isBlank(tum.getSatRecordId())) {
                var exMsg = updateAndSendSDX(timToSend, tum, mps);
                if (StringUtils.isNotBlank(exMsg)) {
                    exceptions.add(new ResubmitTimException(activeTimId, exMsg));
                }
            } else {
                String exMsg = "active_tim_id " + tum.getActiveTimId()
                        + " not sent to SDX (no SAT_RECORD_ID found in database)";
                utility.logWithDate(exMsg);
                exceptions.add(new ResubmitTimException(activeTimId, exMsg));
            }
        }
        return exceptions;
    }

    private OdeTravelerInformationMessage getTim(TimUpdateModel aTim, List<Milepost> mps, List<Milepost> allMps) {
        String nowAsISO = Instant.now().toString();
        DataFrame df = getDataFrame(aTim, nowAsISO, mps);
        // check to see if we have any itis codes
        // if not, just continue on
        if (df.getItems() == null || df.getItems().length == 0) {
            utility.logWithDate("No itis codes found for data_frame " + aTim.getDataFrameId() + ". Skipping...");
            return null;
        }
        Region region = getRegion(aTim, mps, allMps);
        Region[] regions = new Region[1];
        regions[0] = region;
        df.setRegions(regions);

        DataFrame[] dataframes = new DataFrame[1];
        dataframes[0] = df;

        OdeTravelerInformationMessage tim = new OdeTravelerInformationMessage();
        tim.setDataframes(dataframes);
        tim.setMsgCnt(getMsgCnt(aTim.getMsgCnt()));
        tim.setPacketID(aTim.getPacketId());

        tim.setTimeStamp(nowAsISO);

        tim.setUrlB(aTim.getUrlB());
        return tim;
    }

    private int getMsgCnt(int currentCnt) {
        if (currentCnt == 127)
            return 1;
        // else increment msgCnt
        else
            return currentCnt++;
    }

    private NodeXY[] buildNodePathFromMileposts(List<Milepost> mps) {
        NodeXY[] nodes = new NodeXY[mps.size()];
        for (int i = 0; i < mps.size(); i++) {
            NodeXY node = new OdeTravelerInformationMessage.NodeXY();
            node.setNodeLat(mps.get(i).getLatitude());
            node.setNodeLong(mps.get(i).getLongitude());
            node.setDelta("node-LatLon");
            nodes[i] = node;
        }
        return nodes;
    }

    private String getHeadingSliceFromMileposts(List<Milepost> mps, OdePosition3D anchor) {
        int timDirection = 0;
        // path list - change later
        if (mps != null && mps.size() > 0) {
            double startLat = anchor.getLatitude().doubleValue();
            double startLon = anchor.getLongitude().doubleValue();
            for (int j = 0; j < mps.size(); j++) {
                double lat = mps.get(j).getLatitude().doubleValue();
                double lon = mps.get(j).getLongitude().doubleValue();

                Point standPoint = Point.at(Coordinate.fromDegrees(startLat), Coordinate.fromDegrees(startLon));
                Point forePoint = Point.at(Coordinate.fromDegrees(lat), Coordinate.fromDegrees(lon));

                timDirection |= utility.getDirection(EarthCalc.bearing(standPoint, forePoint));
                // reset for next round
                startLat = lat;
                startLon = lon;
            }
        }

        // set direction based on bearings
        String dirTest = Integer.toBinaryString(timDirection);
        dirTest = StringUtils.repeat("0", 16 - dirTest.length()) + dirTest;
        dirTest = StringUtils.reverse(dirTest);
        return dirTest; // heading slice
    }

    private DataFrame getDataFrame(TimUpdateModel aTim, String nowAsISO, List<Milepost> mps) {
        // RoadSignID
        RoadSignID rsid = new RoadSignID();
        rsid.setMutcdCode(MutcdCodeEnum.warning);
        rsid.setViewAngle("1111111111111111");
        rsid.setPosition(getAnchorPosition(aTim, mps));

        // MsgId
        MsgId msgId = new MsgId();
        msgId.setRoadSignID(rsid);

        // DataFrame
        DataFrame df = new DataFrame();
        df.setStartDateTime(nowAsISO);
        df.setSspTimRights(aTim.getSspTimRights());
        df.setFrameType(TravelerInfoType.advisory);
        df.setMsgId(msgId);
        df.setPriority(5);// 0-7, 0 being least important, 7 being most
        df.setSspLocationRights(aTim.getSspLocationRights());
        df.setSspMsgTypes(aTim.getSspMsgTypes());
        df.setSspMsgContent(aTim.getSspMsgContent());
        df.setContent(aTim.getDfContent());
        df.setUrl(aTim.getUrl());

        // set durationTime
        if (aTim.getEndDateTime() != null) {
            int durationTime = utility.getMinutesDurationBetweenTwoDates(aTim.getStartDateTime(),
                    aTim.getEndDateTime());
            df.setDurationTime(durationTime);
        } else {
            // we don't have an endDate so set duration time to 22 days worth of minutes
            // (max time)
            df.setDurationTime(32000);
        }

        df.setItems(dataFrameService.getItisCodesForDataFrameId(aTim.getDataFrameId()));
        return df;
    }

    private OdePosition3D getAnchorPosition(TimUpdateModel aTim, List<Milepost> mps) {
        OdePosition3D anchorPosition = new OdePosition3D();
        if (aTim.getAnchorLat() != null && aTim.getAnchorLong() != null) {
            anchorPosition.setLatitude(aTim.getAnchorLat());
            anchorPosition.setLongitude(aTim.getAnchorLong());
        } else {
            if (mps.size() > 0) {
                anchorPosition.setLatitude(mps.get(0).getLatitude());
                anchorPosition.setLongitude(mps.get(0).getLongitude());
            } else {
                anchorPosition.setLatitude(BigDecimal.valueOf(0));
                anchorPosition.setLongitude(BigDecimal.valueOf(0));
                anchorPosition.setElevation(BigDecimal.valueOf(0));
            }
        }
        return anchorPosition;
    }

    private Region getRegion(TimUpdateModel aTim, List<Milepost> mps, List<Milepost> allMps) {
        // Set region information
        Region region = new Region();
        region.setName(aTim.getRegionName());
        region.setAnchorPosition(getAnchorPosition(aTim, mps));
        region.setLaneWidth(aTim.getLaneWidth());
        String regionDirection = aTim.getRegionDirection();
        if (regionDirection == null || regionDirection.isEmpty()) {
            // we need to calculate the heading slice from all mileposts and not the subset
            regionDirection = getHeadingSliceFromMileposts(allMps, region.getAnchorPosition());
        }
        region.setDirection(regionDirection);// region direction is a heading slice ie 0001100000000000

        // set directionality, default to 3
        String directionality = aTim.getDirectionality();
        if (directionality == null || directionality.isEmpty()) {
            directionality = "3";
        }
        region.setDirectionality(directionality);
        region.setClosedPath(aTim.getClosedPath());

        String regionDescrip = aTim.getRegionDescription();// J2736 - one of path, geometry, oldRegion
        if (regionDescrip == null || regionDescrip.isEmpty()) {
            regionDescrip = "path";// if null, set it to path...we only support path anyway, and only have tables
                                   // supporting path
        }
        region.setDescription(regionDescrip);

        if (aTim.getPathId() != null) {
            NodeXY[] nodes = pathNodeXYService.getNodeXYForPath(aTim.getPathId());
            if (nodes == null || nodes.length == 0) {
                nodes = buildNodePathFromMileposts(mps);
            }
            Path path = new Path();
            path.setScale(0);
            path.setType("xy");
            path.setNodes(nodes);
            region.setPath(path);
        }

        return region;
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
                var rsuExMsg = odeService.updateTimOnRsu(timToSend);
                if (!StringUtils.isEmpty(rsuExMsg)) {
                    exMsg += rsuExMsg + "\n";
                }
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
                    var tmpErrMsg = "Returning without sending TIM to RSU. submitTimQuery failed for RSU "
                            + gson.toJson(wydotRsu);
                    exMsg += tmpErrMsg + "\n";
                    utility.logWithDate(tmpErrMsg);
                    continue;
                }

                existingHoldingRecords.forEach(x -> timQuery.appendIndex(x.getRsuIndex()));
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
                activeTimHoldingService.insertActiveTimHolding(activeTimHolding);

                var newRsuEx = odeService.sendNewTimToRsu(timToSend, aTim.getEndDateTime(), nextRsuIndex);
                if (!StringUtils.isEmpty(newRsuEx)) {
                    exMsg += newRsuEx + "\n";
                }
                rsus[0] = dbRsus.get(i);
                timToSend.getRequest().setRsus(rsus);
            }
        }
        return exMsg;
    }

    private String updateAndSendSDX(WydotTravelerInputData timToSend, TimUpdateModel aTim, List<Milepost> mps) {
        // remove rsus from TIM
        timToSend.getRequest().setRsus(null);
        SDW sdw = new SDW();
        AdvisorySituationDataDeposit asdd = sdwService.getSdwDataByRecordId(aTim.getSatRecordId());
        if (asdd == null) {
            System.out.println("SAT record not found for id " + aTim.getSatRecordId());
            return updateAndSendNewSDX(timToSend, aTim, mps);
        }

        // fetch all mileposts, get service region by bounding box
        OdeGeoRegion serviceRegion = odeService.getServiceRegion(mps);

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
        return odeService.updateTimOnSdw(timToSend);
    }

    private String updateAndSendNewSDX(WydotTravelerInputData timToSend, TimUpdateModel aTim, List<Milepost> mps) {
        String recordId = sdwService.getNewRecordId();
        System.out.println("Generating new SAT id and TIM: " + recordId);
        String regionName = getSATRegionName(aTim, recordId);

        // Update region.name in database
        regionService.updateRegionName(Long.valueOf(aTim.getRegionId()), regionName);
        // Update active_tim.
        activeTimService.updateActiveTim_SatRecordId(aTim.getActiveTimId(), recordId);
        timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionName);
        return odeService.sendNewTimToSdw(timToSend, recordId, mps, config.getSdwTtl());
    }

}
