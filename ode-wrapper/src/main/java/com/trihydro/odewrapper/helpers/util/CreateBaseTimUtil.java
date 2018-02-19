package com.trihydro.odewrapper.helpers.util;

import com.trihydro.odewrapper.model.WydotTimBase;
import com.google.gson.Gson;
import com.trihydro.service.milepost.MilepostService;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import com.trihydro.odewrapper.model.TimQuery;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage.DataFrame.MsgId;

import com.trihydro.odewrapper.model.Milepost;
import com.trihydro.service.model.WydotRsu;
import com.trihydro.odewrapper.model.WydotTravelerInputData;
import java.math.BigDecimal;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.TravelerInputData;
import us.dot.its.jpo.ode.plugin.RoadSideUnit.RSU;
import us.dot.its.jpo.ode.plugin.SNMP;
import us.dot.its.jpo.ode.plugin.j2735.OdeGeoRegion;
import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW.TimeToLive;
import com.trihydro.odewrapper.helpers.DBUtility;

@Component
public class CreateBaseTimUtil
{    
    private MilepostService milepostService;
    public static DBUtility dbUtility;
    
    @Autowired
    public Environment env;

	@Autowired
	CreateBaseTimUtil(MilepostService milepostService) {
        this.milepostService = milepostService;	
    }	    
    
    @Autowired
    public void setDBUtility(DBUtility dbUtilityRh) {
        dbUtility = dbUtilityRh;
    }

    public DBUtility getDBUtility() {
        return dbUtility;
    }

	public WydotTravelerInputData buildTim(WydotTimBase timBase) {                

        // build TIM object with data
        WydotTravelerInputData timToSend = new WydotTravelerInputData();
        J2735TravelerInformationMessage tim = new J2735TravelerInformationMessage();     
        tim.setUrlB("null");                                              

        //String[] items = timQuery.getIndicies_set().replaceAll("\\[", "").replaceAll(" ", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");

        
        // int[] results = new int[items.length];
        
        // for (int i = 0; i < items.length; i++) {
        //     try {
        //         results[i] = Integer.parseInt(items[i]);
        //     } catch (NumberFormatException nfe) {
        //         //NOTE: write something here if you need to recover from formatting errors
        //     };
        // }


        
        

       // tim.setIndex(findFirstAvailableIndex(results));

        LocalDateTime ldt = LocalDateTime.now();
       
        ZoneId mstZoneId = ZoneId.of("America/Denver");
        System.out.println("TimeZone : " + mstZoneId);
       
               //LocalDateTime + ZoneId = ZonedDateTime
        ZonedDateTime mstZonedDateTime = ldt.atZone(mstZoneId);      
        String startDateTime = mstZonedDateTime.toLocalDateTime().toString() + "-07:00";
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS-07:00"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());
        System.out.println(nowAsISO);

        // current time - UTC?
        tim.setTimeStamp(nowAsISO);
        
        J2735TravelerInformationMessage.DataFrame dataFrame = new J2735TravelerInformationMessage.DataFrame();
        dataFrame.setsspTimRights((short)1);
        dataFrame.setsspLocationRights((short)1);
        dataFrame.setsspMsgContent((short)1);
        MsgId msgId = new MsgId();
        msgId.setFurtherInfoID("CDEF");
        dataFrame.setMsgId(msgId);
        dataFrame.setStartDateTime(startDateTime);
        // duration time set to 22 days worth of minutes
        dataFrame.setDurationTime(32000);
        

        dataFrame.setPriority(5);
        dataFrame.setContent("Advisory");
        dataFrame.setFrameType(us.dot.its.jpo.ode.plugin.j2735.timstorage.FrameType.TravelerInfoType.advisory);
        dataFrame.setUrl("null");

        List<J2735TravelerInformationMessage.DataFrame.Region> regions = new ArrayList<J2735TravelerInformationMessage.DataFrame.Region>();
        J2735TravelerInformationMessage.DataFrame.Region region = new J2735TravelerInformationMessage.DataFrame.Region();
        region.setName("Testing TIM");
        region.setRegulatorID(0);
        //region.setSegmentID(timBase.getDistrict());

        region.setLaneWidth(new BigDecimal(327));
        region.setDirectionality(new Long(3));
        region.setClosedPath(false);
       
        // path
        region.setDescription("path");
        J2735TravelerInformationMessage.DataFrame.Region.Path path = new J2735TravelerInformationMessage.DataFrame.Region.Path();
        path.setScale(0);
        path.setType("xy");

        List<Milepost> mileposts;
        mileposts = MilepostService.selectMilepostRange(timBase.getDirection(), Math.min(timBase.getToRm(), timBase.getFromRm()), Math.max(timBase.getToRm(), timBase.getFromRm()), dbUtility.getConnection());
        
        OdePosition3D anchorPosition = new OdePosition3D();
        if(mileposts.size() > 0){
            anchorPosition.setLatitude(new BigDecimal(mileposts.get(0).getLatitude()));
            anchorPosition.setLongitude(new BigDecimal(mileposts.get(0).getLongitude()));
            anchorPosition.setElevation(new BigDecimal(mileposts.get(0).getElevation() * 0.3048));
        }
        else{
            anchorPosition.setLatitude(new BigDecimal(0));
            anchorPosition.setLongitude(new BigDecimal(0));
            anchorPosition.setElevation(new BigDecimal(0));
        }
        region.setAnchorPosition(anchorPosition);

        ArrayList<J2735TravelerInformationMessage.NodeXY> nodes = new ArrayList<J2735TravelerInformationMessage.NodeXY>();
        
        // add circle later for paths over 12 miles

        // path list - change later
        for(int j = 1; j < mileposts.size(); j++) {
            J2735TravelerInformationMessage.NodeXY node = new J2735TravelerInformationMessage.NodeXY();            
            node.setNodeLat(new BigDecimal(mileposts.get(j).getLatitude()));
            node.setNodeLong(new BigDecimal(mileposts.get(j).getLongitude()));            
            node.setDelta("node-LatLon");   
            nodes.add(node);         
        }

        path.setNodes(nodes.toArray(new J2735TravelerInformationMessage.NodeXY[nodes.size()]));
        region.setPath(path);
        
        // direction - change later
        region.setDirection("1111111111111111");

        regions.add(region);
        dataFrame.setRegions(regions.toArray(new J2735TravelerInformationMessage.DataFrame.Region[regions.size()]));

        J2735TravelerInformationMessage.DataFrame[] dataFrames = new J2735TravelerInformationMessage.DataFrame[1];
        dataFrames[0] = dataFrame;
        tim.setDataframes(dataFrames);      

        SNMP snmp = new SNMP();
        snmp.setChannel(178);
        snmp.setRsuid("00000083");
        snmp.setMsgid(31);
        snmp.setMode(1);
        snmp.setChannel(178);
        snmp.setInterval(2);
        snmp.setDeliverystart("2018-01-01T17:47:11-05:00");
        snmp.setDeliverystop("2019-01-01T17:47:11-05:15");
        snmp.setEnable(1);
        snmp.setStatus(4);
        timToSend.setSnmp(snmp);

        // SDW sdw = new SDW();
               
        // sdw.setServiceRegion(getServiceRegion(mileposts));
        // sdw.setTtl(TimeToLive.oneday);
        
        // timToSend.setSdw(sdw);

        timToSend.setTim(tim);        

        return timToSend;
    }

    protected OdeGeoRegion getServiceRegion(List<Milepost> mileposts){
        
        Comparator<Milepost> compLat = (l1, l2) -> Double.compare( l1.getLatitude(), l2.getLatitude());
        Comparator<Milepost> compLong = (l1, l2) -> Double.compare( l1.getLongitude(), l2.getLongitude());

        Milepost maxLat = mileposts.stream()
            .max(compLat)
            .get();

        Milepost minLat = mileposts.stream()
            .min(compLat)
            .get();
        
        Milepost maxLong = mileposts.stream()
            .max(compLong)
            .get();

        Milepost minLong = mileposts.stream()
            .min(compLong)
            .get();

        OdePosition3D nwCorner = new OdePosition3D();
        nwCorner.setLatitude(new BigDecimal(maxLat.getLatitude()));
        nwCorner.setLongitude(new BigDecimal(minLong.getLongitude()));
        
        OdePosition3D seCorner = new OdePosition3D();
        seCorner.setLatitude(new BigDecimal(minLat.getLatitude()));
        seCorner.setLongitude(new BigDecimal(maxLong.getLongitude()));

        OdeGeoRegion serviceRegion = new OdeGeoRegion();
        serviceRegion.setNwCorner(nwCorner);
        serviceRegion.setSeCorner(seCorner);
        System.out.println("nwCorner: " + nwCorner.getLatitude() + ", " + nwCorner.getLongitude());
        System.out.println("seCorner: " + seCorner.getLatitude() + ", " + seCorner.getLongitude());
        return serviceRegion;
    }

    

    protected String getDelta(Double distance) {
        if(distance >= -.0002048 && distance < .0002048)
            return "node-LL1";
        else if(distance >= -.0008192 && distance < .0008192)
            return "node-LL2";
        else if(distance >= -.0032768 && distance < .0032768)
            return "node-LL3";
        else if(distance >= -.0131072 && distance < .0131072)
            return "node-LL4";
        else if(distance >= -.2097152 && distance < .2097152)
            return "node-LL5";
        else
            return "node-LL6";
    }

    public static boolean contains(final int[] array, final int v) {
        boolean result = false;
        for(int i : array){
            if(i == v){
                result = true;
                break;
            }
        }
        return result;
    }

}