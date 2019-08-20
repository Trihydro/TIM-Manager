package com.trihydro.timrefresh;

import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.DataFrameService;
import com.trihydro.library.service.MilepostService;
import com.trihydro.library.service.PathNodeXYService;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.SdwService;
import com.trihydro.library.model.AdvisorySituationDataDeposit;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.TimUpdateModel;
import com.trihydro.library.model.WydotRsuTim;
import com.trihydro.library.model.WydotTravelerInputData;
import com.trihydro.timrefresh.service.WydotTimService;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import oracle.sql.TIMESTAMP;
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

    @Scheduled(cron = "*/5 * * * * *")
    public void performTaskUsingCron() {
        System.out.println("Regular task performed using Cron at " + dateFormat.format(new Date()));

        // fetch Active_TIM that are expiring within 24 hrs
        List<TimUpdateModel> expiringTims = ActiveTimService.getExpiringActiveTims();

        // log ones to be updated

        // loop through and issue new TIM to ODE
        for (TimUpdateModel aTim : expiringTims) {
            WydotTravelerInputData timToSend = new WydotTravelerInputData();

            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat dteFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
            dteFormat.setTimeZone(tz);
            String nowAsISO = dteFormat.format(new Date());

            // RoadSignID
            RoadSignID rsid = new RoadSignID();
            rsid.setMutcdCode(MutcdCodeEnum.warning);
            rsid.setViewAngle("1111111111111111");
            rsid.setPosition(new OdePosition3D(aTim.getAnchorLat(), aTim.getAnchorLong(), null));

            MsgId msgId = new MsgId();
            msgId.setRoadSignID(rsid);

            // DataFrame
            DataFrame df = new DataFrame();
            df.setStartDateTime(nowAsISO);
            df.setSspTimRights(aTim.getSspTimRights());
            df.setFrameType(TravelerInfoType.advisory);// TODO: convert this from our db value
            df.setMsgId(msgId);
            df.setPriority(5);// 0-7, 0 being least important, 7 being most
            df.setSspLocationRights(aTim.getSspLocationRights());
            df.setSspMsgTypes(aTim.getSspMsgTypes());
            df.setSspMsgContent(aTim.getSspMsgContent());
            df.setContent(aTim.getDfContent());
            df.setUrl(aTim.getUrl());

            df.setItems(DataFrameService.getItisCodesForDataFrameId(aTim.getDataFrameId()));

            // Set region information
            Region region = new Region();
            region.setName(aTim.getRegionName());
            region.setAnchorPosition(new OdePosition3D(aTim.getAnchorLat(), aTim.getAnchorLong(), null));
            region.setLaneWidth(aTim.getLaneWidth());
            region.setDirection(aTim.getDirection());
            region.setDirectionality(aTim.getDirectionality());
            region.setClosedPath(aTim.getClosedPath());

            if (aTim.getPathId() != null) {
                NodeXY[] nodes = PathNodeXYService.GetNodeXYForPath(aTim.getPathId());
                Path path = new Path();
                path.setType("xy");
                path.setNodes(nodes);
                region.setPath(path);
            }

            Region[] regions = new Region[1];
            regions[0] = region;
            df.setRegions(regions);

            DataFrame[] dataframes = new DataFrame[1];
            dataframes[0] = df;

            OdeTravelerInformationMessage tim = new OdeTravelerInformationMessage();
            tim.setDataframes(dataframes);
            tim.setMsgCnt(aTim.getMsgCnt());
            tim.setTimeStamp(LocalDateTime.now().toString());

            // tim.setPacketID();
            tim.setUrlB(aTim.getUrlB());

            timToSend.setRequest(new ServiceRequest());
            timToSend.setTim(tim);

            if (!StringUtils.isEmpty(aTim.getRsuTarget()) && !StringUtils.isBlank(aTim.getRsuTarget())) {
                UpdateAndSendRSU(timToSend, aTim);
            }

            if (!StringUtils.isEmpty(aTim.getSatRecordId()) && !StringUtils.isBlank(aTim.getSatRecordId())) {
                UpdateAndSendSDW(timToSend, aTim);
            }
        }

    }

    private void UpdateAndSendRSU(WydotTravelerInputData timToSend, TimUpdateModel aTim) {
        List<WydotRsuTim> wydotRsus = RsuService.getFullRsusTimIsOn(aTim.getTimId());
        if (wydotRsus.size() <= 0) {
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

        WydotTimService.updateTimOnRsu(timToSend);
    }

    private void UpdateAndSendSDW(WydotTravelerInputData timToSend, TimUpdateModel aTim) {
        SDW sdw = new SDW();
        AdvisorySituationDataDeposit asdd = SdwService.getSdwDataByRecordId(aTim.getSatRecordId());

        // fetch all mileposts, get service region by bounding box
        List<Milepost> mps = MilepostService.selectMilepostRange(aTim.getDirection(), aTim.getRoute(),
                aTim.getMilepostStart(), aTim.getMilepostStop());
        OdeGeoRegion serviceRegion = WydotTimService.getServiceRegion(mps);

        // we are saving our ttl unencoded at the root level of the object as an int
        // representing the enum
        // the DOT sdw ttl goes by string, so we need to do a bit of translation here
        TimeToLive ttl = TimeToLive.valueOf(asdd.getTimeToLive().getStringValue());
        sdw.setTtl(ttl);
        sdw.setRecordId(aTim.getSatRecordId());
        sdw.setServiceRegion(serviceRegion);

        // set sdw block in TIM
        timToSend.getRequest().setSdw(sdw);
        WydotTimService.updateTimOnSdw(timToSend);
    }
}