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

    @Autowired
    public TimGenerationHelper(Utility _utility, DataFrameService _dataFrameService,
            PathNodeXYService _pathNodeXYService) {
        utility = _utility;
        dataFrameService = _dataFrameService;
        pathNodeXYService = _pathNodeXYService;
    }

    public OdeTravelerInformationMessage getTim(TimUpdateModel aTim, List<Milepost> mps, List<Milepost> allMps) {
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

}
