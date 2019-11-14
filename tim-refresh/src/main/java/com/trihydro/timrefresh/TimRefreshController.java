package com.trihydro.timrefresh;

import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.DataFrameService;
import com.trihydro.library.service.MilepostService;
import com.trihydro.library.service.PathNodeXYService;
import com.trihydro.library.service.RegionService;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.SdwService;
import com.google.gson.Gson;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.AdvisorySituationDataDeposit;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.TimUpdateModel;
import com.trihydro.library.model.WydotRsuTim;
import com.trihydro.library.model.WydotTravelerInputData;
import com.trihydro.timrefresh.service.WydotTimService;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.plugin.SNMP;
import us.dot.its.jpo.ode.plugin.ServiceRequest;
import us.dot.its.jpo.ode.plugin.RoadSideUnit.RSU;
import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW;
import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW.TimeToLive;
import us.dot.its.jpo.ode.plugin.j2735.OdeGeoRegion;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.NodeXY;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.MsgId;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.RoadSignID;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region.Path;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.FrameType.TravelerInfoType;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.MutcdCode.MutcdCodeEnum;

@Component
public class TimRefreshController {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    public static Gson gson = new Gson();

    @Scheduled(cron = "${cron.expression}") // run at 1:00am every day
    public void performTaskUsingCron() {
        System.out.println("Regular task performed using Cron at " + dateFormat.format(new Date()));

        // fetch Active_TIM that are expiring within 24 hrs
        List<TimUpdateModel> expiringTims = ActiveTimService.getExpiringActiveTims();

        System.out.println(expiringTims.size() + " expiring TIMs found");

        // loop through and issue new TIM to ODE
        for (TimUpdateModel aTim : expiringTims) {
            System.out.println("------ Processing active_tim with id: " + aTim.getActiveTimId());

            // Mileposts
            String route = aTim.getRoute().replaceAll("\\D+", "");// get just the numeric value for the 'like' statement
                                                                  // to avoid issues with differing formats
            List<Milepost> mps = MilepostService.selectMilepostRange(aTim.getDirection(), route,
                    aTim.getMilepostStart(), aTim.getMilepostStop());
            if (mps.size() == 0) {
                System.out.println("Unable to send TIM to SDW, no mileposts found to determine service area");
                continue;
            }

            OdeTravelerInformationMessage tim = getTim(aTim, mps);
            if (tim == null) {
                continue;
            }
            WydotTravelerInputData timToSend = new WydotTravelerInputData();
            timToSend.setRequest(new ServiceRequest());
            timToSend.setTim(tim);

            if (!StringUtils.isEmpty(aTim.getRsuTarget()) && !StringUtils.isBlank(aTim.getRsuTarget())) {
                updateAndSendRSU(timToSend, aTim);
            }

            if (!StringUtils.isEmpty(aTim.getSatRecordId()) && !StringUtils.isBlank(aTim.getSatRecordId())) {
                updateAndSendSDW(timToSend, aTim, mps);
            }
        }
    }

    private OdeTravelerInformationMessage getTim(TimUpdateModel aTim, List<Milepost> mps) {
        String nowAsISO = Instant.now().toString();
        DataFrame df = getDataFrame(aTim, nowAsISO, mps);
        // check to see if we have any itis codes
        // if not, just continue on
        if (df.getItems() == null || df.getItems().length == 0) {
            System.out.println("No itis codes found for data_frame " + aTim.getDataFrameId() + ". Skipping...");
            return null;
        }
        Region region = getRegion(aTim, mps);
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
            node.setNodeLat(new BigDecimal(mps.get(i).getLatitude()));
            node.setNodeLong(new BigDecimal(mps.get(i).getLongitude()));
            node.setDelta("node-LatLon");
            nodes[i] = node;
        }
        return nodes;
    }

    private String getHeadingSliceFromMileposts(List<Milepost> mps) {
        int timDirection = 0;
        for (int i = 0; i < mps.size(); i++) {
            timDirection |= Utility.getDirection(mps.get(i).getBearing());
        }

        // set direction based on bearings
        String dirTest = Integer.toBinaryString(timDirection);
        dirTest = StringUtils.repeat("0", 16 - dirTest.length()) + dirTest;
        dirTest = StringUtils.reverse(dirTest);
        return dirTest; // heading slice
    }

    private void updateAndSendRSU(WydotTravelerInputData timToSend, TimUpdateModel aTim) {
        List<WydotRsuTim> wydotRsus = RsuService.getFullRsusTimIsOn(aTim.getTimId());
        if (wydotRsus.size() <= 0) {
            System.out.println("RSUs not found for tim_id " + aTim.getTimId());
            return;
        }

        RSU[] rsus = new RSU[wydotRsus.size()];
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
            rsus[i] = rsu;
        }

        timToSend.getRequest().setRsus(rsus);

        // set SNMP command
        SNMP snmp = new SNMP();
        snmp.setRsuid("00000083");
        snmp.setMsgid(31);
        snmp.setMode(1);
        snmp.setChannel(178);
        snmp.setInterval(2);

        // getStartDateTime returns oracle timestamp format of
        // 08-JUL-19 03.50.00.000000000 AM (this is UTC)
        // but we need 2019-08-16T19:54:00.000Z format
        snmp.setDeliverystart(aTim.getStartDate_Timestamp().toInstant().toString());
        snmp.setDeliverystop(aTim.getEndDate_Timestamp().toInstant().toString());
        snmp.setEnable(1);
        snmp.setStatus(4);

        timToSend.getRequest().setSnmp(snmp);

        System.out.println("Sending TIM to RSU for refresh: " + gson.toJson(timToSend));
        WydotTimService.updateTimOnRsu(timToSend);
    }

    private void updateAndSendSDW(WydotTravelerInputData timToSend, TimUpdateModel aTim, List<Milepost> mps) {
        SDW sdw = new SDW();
        AdvisorySituationDataDeposit asdd = SdwService.getSdwDataByRecordId(aTim.getSatRecordId());
        if (asdd == null) {
            System.out.println("SAT record not found for id " + aTim.getSatRecordId());
            updateAndSendNewSDW(timToSend, aTim, mps);
            return;
        }

        // fetch all mileposts, get service region by bounding box
        OdeGeoRegion serviceRegion = WydotTimService.getServiceRegion(mps);

        // we are saving our ttl unencoded at the root level of the object as an int
        // representing the enum
        // the DOT sdw ttl goes by string, so we need to do a bit of translation here
        TimeToLive ttl = TimeToLive.valueOf(asdd.getTimeToLive().getStringValue());
        sdw.setTtl(ttl);
        sdw.setRecordId(aTim.getSatRecordId());
        sdw.setServiceRegion(serviceRegion);

        // set sdw block in TIM
        System.out.println("Sending TIM to SDW for refresh: " + gson.toJson(timToSend));
        timToSend.getRequest().setSdw(sdw);
        WydotTimService.updateTimOnSdw(timToSend);
    }

    private void updateAndSendNewSDW(WydotTravelerInputData timToSend, TimUpdateModel aTim, List<Milepost> mps) {
        String recordId = SdwService.getNewRecordId();
        System.out.println("Generating new SAT id and TIM: " + recordId);
        String regionName = getSATRegionName(aTim, recordId);

        // Update region.name in database
        RegionService.updateRegionName(new Long(aTim.getRegionId()), regionName);
        // Update active_tim.
        ActiveTimService.updateActiveTim_SatRecordId(aTim.getActiveTimId(), recordId);
        timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionName);
        WydotTimService.sendNewTimToSdw(timToSend, recordId, mps);
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
            int durationTime = Utility.getMinutesDurationBetweenTwoDates(aTim.getStartDateTime(),
                    aTim.getEndDateTime());
            df.setDurationTime(durationTime);
        } else {
            // we don't have an endDate so set duration time to 22 days worth of minutes
            // (max time)
            df.setDurationTime(32000);
        }

        df.setItems(DataFrameService.getItisCodesForDataFrameId(aTim.getDataFrameId()));
        return df;
    }

    private OdePosition3D getAnchorPosition(TimUpdateModel aTim, List<Milepost> mps) {
        OdePosition3D anchorPosition = new OdePosition3D();
        if (aTim.getAnchorLat() != null && aTim.getAnchorLong() != null) {
            anchorPosition.setLatitude(aTim.getAnchorLat());
            anchorPosition.setLongitude(aTim.getAnchorLong());
        } else {
            if (mps.size() > 0) {
                anchorPosition.setLatitude(new BigDecimal(mps.get(0).getLatitude()));
                anchorPosition.setLongitude(new BigDecimal(mps.get(0).getLongitude()));
            } else {
                anchorPosition.setLatitude(new BigDecimal(0));
                anchorPosition.setLongitude(new BigDecimal(0));
                anchorPosition.setElevation(new BigDecimal(0));
            }
        }
        return anchorPosition;
    }

    private Region getRegion(TimUpdateModel aTim, List<Milepost> mps) {
        // Set region information
        Region region = new Region();
        region.setName(aTim.getRegionName());
        region.setAnchorPosition(getAnchorPosition(aTim, mps));
        region.setLaneWidth(aTim.getLaneWidth());
        String regionDirection = aTim.getRegionDirection();
        if (regionDirection == null || regionDirection == "") {
            regionDirection = getHeadingSliceFromMileposts(mps);
        }
        region.setDirection(regionDirection);// region direction is a heading slice ie 0001100000000000

        // set directionality, default to 3
        String directionality = aTim.getDirectionality();
        if (directionality == null || directionality == "") {
            directionality = "3";
        }
        region.setDirectionality(directionality);
        region.setClosedPath(aTim.getClosedPath());

        String regionDescrip = aTim.getRegionDescription();// J2736 - one of path, geometry, oldRegion
        if (regionDescrip == null || regionDescrip == "") {
            regionDescrip = "path";// if null, set it to path...we only support path anyway, and only have tables
                                   // supporting path
        }
        region.setDescription(regionDescrip);

        if (aTim.getPathId() != null) {
            NodeXY[] nodes = PathNodeXYService.GetNodeXYForPath(aTim.getPathId());
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

    private String getSATRegionName(TimUpdateModel aTim, String recordId) {

        // name is direction_route_startMP_endMP_SAT-satRecordId_TIMType_ClientId_pk
        String oldName = aTim.getRegionName();
        if (oldName != null && oldName.length() > 0) {
            // just replace existing satRecordId with new
            return oldName.replace(aTim.getSatRecordId(), recordId);
        } else {
            // generating from scratch...
            String regionNamePrev = aTim.getDirection() + "_" + aTim.getRoute() + "_" + aTim.getMilepostStart() + "_"
                    + aTim.getMilepostStop();

            String regionNameTemp = regionNamePrev + "_SAT-" + recordId;

            String timType = aTim.getTimTypeName();
            if (timType == null || timType == "") {
                timType = "RC";// defaulting to Road Condition
            }
            // the rest depend on each other to be there for indexing
            // note that if we don't have a type, our logger inserts a new active_tim rather
            // than updating
            regionNameTemp += "_" + timType;

            if (aTim.getClientId() != null) {
                regionNameTemp += "_" + aTim.getClientId();

                if (aTim.getPk() != null) {
                    regionNameTemp += "_" + aTim.getPk();
                }
            }
            return regionNameTemp;
        }
    }
}