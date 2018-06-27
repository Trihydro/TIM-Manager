package com.trihydro.odewrapper.helpers.util;

import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.service.MilepostService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;
import java.util.List;
import java.util.ArrayList;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage.DataFrame.MsgId;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage.DataFrame.RoadSignID;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.MutcdCode.MutcdCodeEnum;

import com.trihydro.odewrapper.model.WydotTravelerInputData;
import java.math.BigDecimal;

@Component
public class CreateBaseTimUtil
{    
 
	public static WydotTravelerInputData buildTim(WydotTim wydotTim, String direction, String route) {                

        // build TIM object with data
        WydotTravelerInputData timToSend = new WydotTravelerInputData();
        J2735TravelerInformationMessage tim = new J2735TravelerInformationMessage();     
        tim.setUrlB("null");                                              

        // set TIM Properties
        J2735TravelerInformationMessage.DataFrame dataFrame = new J2735TravelerInformationMessage.DataFrame();
        dataFrame.setSspTimRights((short)1);
        dataFrame.setSspLocationRights((short)1);
        dataFrame.setSspMsgContent((short)1);
        dataFrame.setSspMsgTypes((short)1);

        // set TIM TimeStamp and StartDateTime to current time in UTC
        String nowAsISO = Instant.now().toString();
        tim.setTimeStamp(nowAsISO);
        dataFrame.setStartDateTime(nowAsISO);

        // duration time set to 22 days worth of minutes
        dataFrame.setDurationTime(32000);
        
        dataFrame.setPriority(5);
        dataFrame.setContent("Advisory");
        dataFrame.setFrameType(us.dot.its.jpo.ode.plugin.j2735.timstorage.FrameType.TravelerInfoType.advisory);
        dataFrame.setUrl("null");

        List<J2735TravelerInformationMessage.DataFrame.Region> regions = new ArrayList<J2735TravelerInformationMessage.DataFrame.Region>();
        J2735TravelerInformationMessage.DataFrame.Region region = new J2735TravelerInformationMessage.DataFrame.Region();
        region.setName("Temp");
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
      
        timToSend.setMileposts(MilepostService.selectMilepostRange(direction, route, wydotTim.getFromRm(), wydotTim.getToRm()));
        
        List<Milepost> sizeRestrictedMilepostList = timToSend.getMileposts();

        int mod = 4;
        while(sizeRestrictedMilepostList.size() > 60){
            
            List<Milepost> tempList = new ArrayList<Milepost>(); 

            for (Milepost milepost : sizeRestrictedMilepostList) {          
                                
                if(Math.round(milepost.getMilepost() * 10 % mod) == 0){
                    tempList.add(milepost);
                }                                 
            }

            sizeRestrictedMilepostList = tempList;
            mod += 2;
        }

        timToSend.setMileposts(sizeRestrictedMilepostList);

        OdePosition3D anchorPosition = new OdePosition3D();
        if(timToSend.getMileposts().size() > 0){
            anchorPosition.setLatitude(new BigDecimal(timToSend.getMileposts().get(0).getLatitude()));
            anchorPosition.setLongitude(new BigDecimal(timToSend.getMileposts().get(0).getLongitude()));
            //anchorPosition.setElevation(new BigDecimal(timToSend.getMileposts().get(0).getElevation() * 0.3048));
        }
        else{
            anchorPosition.setLatitude(new BigDecimal(0));
            anchorPosition.setLongitude(new BigDecimal(0));
            anchorPosition.setElevation(new BigDecimal(0));
        }

        MsgId msgId = new MsgId();
        RoadSignID roadSignID = new RoadSignID();
        OdePosition3D position = new OdePosition3D();
        position.setLatitude(anchorPosition.getLatitude());
        position.setLongitude(anchorPosition.getLongitude());
        roadSignID.setPosition(position);
        roadSignID.setMutcdCode(MutcdCodeEnum.warning);
        roadSignID.setViewAngle("1111111111111111");
        msgId.setRoadSignID(roadSignID);
        dataFrame.setMsgId(msgId);

        region.setAnchorPosition(anchorPosition);

        ArrayList<J2735TravelerInformationMessage.NodeXY> nodes = new ArrayList<J2735TravelerInformationMessage.NodeXY>();        

        int timDirection = 0;
        // path list - change later
        for(int j = 1; j < timToSend.getMileposts().size(); j++) {
            J2735TravelerInformationMessage.NodeXY node = new J2735TravelerInformationMessage.NodeXY();            
            node.setNodeLat(new BigDecimal(timToSend.getMileposts().get(j).getLatitude()));
            node.setNodeLong(new BigDecimal(timToSend.getMileposts().get(j).getLongitude()));            
            node.setDelta("node-LatLon");   
            nodes.add(node);         
            timDirection |= getDirection(timToSend.getMileposts().get(j).getBearing());
        }

        // set direction based on bearings
        String dirTest = Integer.toBinaryString(timDirection);
        dirTest = StringUtils.repeat("0", 16 - dirTest.length()) + dirTest;
        dirTest = StringUtils.reverse(dirTest);
        region.setDirection(dirTest); // heading slice	

        // set path nodes
        path.setNodes(nodes.toArray(new J2735TravelerInformationMessage.NodeXY[nodes.size()]));
        region.setPath(path);
    
        regions.add(region);
        dataFrame.setRegions(regions.toArray(new J2735TravelerInformationMessage.DataFrame.Region[regions.size()]));

        J2735TravelerInformationMessage.DataFrame[] dataFrames = new J2735TravelerInformationMessage.DataFrame[1];
        dataFrames[0] = dataFrame;
        tim.setDataframes(dataFrames);           

        timToSend.setTim(tim);        

        return timToSend;
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

    protected static int getDirection(Double bearing){

		int direction = 0;

		if(bearing >= 0 && bearing <= 22.5)
			direction = 1;
		else if(bearing > 22.5 && bearing <= 45)
			direction = 2;
		else if(bearing > 45 && bearing <= 67.5)
			direction = 4;
		else if(bearing > 67.5 && bearing <= 90)
			direction = 8;
		else if(bearing > 90 && bearing <= 112.5)
			direction = 16;
		else if(bearing > 112.5 && bearing <= 135)
			direction = 32;
		else if(bearing > 135 && bearing <= 157.5)
			direction = 64;
		else if(bearing > 157.5 && bearing <= 180)
			direction = 128;
		else if(bearing > 180 && bearing <= 202.5)
			direction = 256;		
		else if(bearing > 202.5 && bearing <= 225)
			direction = 512;
		else if(bearing > 225 && bearing <= 247.5)
			direction = 1024;
		else if(bearing > 247.5 && bearing <= 270)
			direction = 2048;
		else if(bearing > 270 && bearing <= 292.5)
			direction = 4096;
		else if(bearing > 292.5 && bearing <= 315)
			direction = 8192;
		else if(bearing > 315 && bearing <= 337.5)
			direction = 16384;
		else if(bearing > 337.5 && bearing <= 360)
			direction = 32768;

		return direction;
	}
}