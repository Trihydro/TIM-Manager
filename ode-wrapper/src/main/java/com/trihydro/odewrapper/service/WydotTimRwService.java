package com.trihydro.odewrapper.service;

import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.TimService;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.TimType;

import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;
import com.trihydro.odewrapper.model.WydotTravelerInputData;
import com.trihydro.library.service.TimRsuService;
import com.trihydro.library.model.TimRsu;
import static java.lang.Math.toIntExact;


@Component
public class WydotTimRwService extends WydotTimService
{    
    private CreateBaseTimUtil createBaseTimUtil;    
    private TimType timType;

	@Autowired
	WydotTimRwService(CreateBaseTimUtil createBaseTimUtil) {      
        this.createBaseTimUtil = createBaseTimUtil;
    }	

    // public void createRwTim(List<WydotTim> timRwList) {
      

    //     // discard non-I-80 requests 
    //     for (WydotTimRw wydotTimRw : timRwList) { 
            
    //         System.out.println("RW TIM");
    //         System.out.println("direction:" + wydotTimRw.getDirection());
    //         System.out.println("route:" + wydotTimRw.getRoute());
    //         System.out.println("fromRm:" + wydotTimRw.getFromRm());
    //         System.out.println("toRm:" + wydotTimRw.getToRm());

    //         // FIND ALL RSUS TO SEND TO     
    //         List<WydotRsu> rsus = getRsusInBuffer(wydotTimRw.getDirection(), Math.min(wydotTimRw.getToRm(), wydotTimRw.getFromRm()), Math.max(wydotTimRw.getToRm(), wydotTimRw.getFromRm()));       

    //         // build base TIM                
    //         WydotTravelerInputData timToSend = createBaseTimUtil.buildTim(wydotTimRw);

    //         // add Road Contruction ITIS Codes            
    //         List<String> items = setItisCodes(wydotTimRw);            
    //         timToSend.getTim().getDataframes()[0].setItems(items.toArray(new String[items.size()]));

    //         // Convert start date time coming from REST call to a local format
    //         //String startDateTimeLocal = convertUtcDateTimeToLocal(wydotTimRw.getStartDateTime());

    //         // convert to local time
    //         String startDateTimeLocal = convertUtcDateTimeToLocal(wydotTimRw.getStartDateTime());
    //         timToSend.getTim().getDataframes()[0].setStartDateTime(startDateTimeLocal);

    //         // duration time
    //         if(wydotTimRw.getEndDateTime() != null){               
    //             long durationTime = getMinutesDurationBetweenTwoDates(wydotTimRw.getStartDateTime(), wydotTimRw.getEndDateTime());
    //             timToSend.getTim().getDataframes()[0].setDurationTime(toIntExact(durationTime));
    //         }            
            
    //         String regionNamePrev = wydotTimRw.getDirection() + "_" + wydotTimRw.getRoute() + "_" + wydotTimRw.getFromRm() + "_" + wydotTimRw.getToRm();           
            
    //         // query database for rsus that active tims could be on
    //         List<ActiveTim> activeTims = null;
    //         // for each rsu in range
    //         for (WydotRsu rsu : rsus) {

    //             // add rsu to tim
    //             rsuArr[0] = rsu;
    //             timToSend.setRsus(rsuArr);            
                
    //             // update region name for active tim logger
    //             timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNamePrev + "_RSU-" + rsu.getRsuTarget() + "_" + timTypeString);
              
    //             //activeTims = ActiveTimService.getActiveRSUTimsByClientId(wydotTimRw.getClientId());

    //             // update tim                       
    //             if(activeTims != null && activeTims.size() > 0){                                            
    //                 // return error                     
    //             }              
    //             else{     
    //                 // send new tim to rsu                    
    //                 sendNewTimToRsu(timToSend, rsu);  
    //             }
    //         }

    //         // satellite
    //         List<ActiveTim> activeSatTims = ActiveTimService.getActiveSatTims(wydotTimRw.getFromRm(), wydotTimRw.getToRm(), timType.getTimTypeId(), wydotTimRw.getDirection());
        
    //         if(activeSatTims != null && activeSatTims.size() > 0){
    //             // return error
    //         }
    //         else{
    //             String recordId = getNewRecordId();    
    //             timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNamePrev + "_SAT-" + recordId + "_" + timTypeString);
    //             sendNewTimToSdw(timToSend, recordId);
    //         }                                   
    //     }
    // }	  

    // public ActiveTim getRwTim(String clientId){

    //     ActiveTim activeTim = ActiveTimService.getActiveTimByClientId(clientId, timType.getTimTypeId());

    //     return activeTim;
    // }

    // public void updateRwTim(List<WydotTimRw> timRwList) {
        
    //     Long timId = null;

    //     // discard non-I-80 requests 
    //     for (WydotTimRw wydotTimRw : timRwList) {                         

    //         // FIND ALL RSUS TO SEND TO     
    //         List<WydotRsu> rsus = getRsusInBuffer(wydotTimRw.getDirection(), Math.min(wydotTimRw.getToRm(), wydotTimRw.getFromRm()), Math.max(wydotTimRw.getToRm(), wydotTimRw.getFromRm()));       

    //         // build base TIM                
    //         WydotTravelerInputData timToSend = createBaseTimUtil.buildTim(wydotTimRw);

    //         // add Road Work ITIS Codes            
    //         List<String> items = setItisCodes(wydotTimRw);
    //         timToSend.getTim().getDataframes()[0].setItems(items.toArray(new String[items.size()]));

    //         // Convert start date time coming from REST call to a local format
    //         String startDateTimeLocal = convertUtcDateTimeToLocal(wydotTimRw.getStartDateTime());
    //         timToSend.getTim().getDataframes()[0].setStartDateTime(startDateTimeLocal);

    //         // duration time
    //         if(wydotTimRw.getEndDateTime() != null){               
    //             long durationTime = getMinutesDurationBetweenTwoDates(wydotTimRw.getStartDateTime(), wydotTimRw.getEndDateTime());
    //             timToSend.getTim().getDataframes()[0].setDurationTime(toIntExact(durationTime));
    //         }    

    //         // set region name
    //         String regionNamePrev = wydotTimRw.getDirection() + "_" + wydotTimRw.getRoute() + "_" + wydotTimRw.getFromRm() + "_" + wydotTimRw.getToRm();           
            
    //         // query database for rsus that active tims could be on
    //         List<ActiveTim> activeTims = null;
    //         // for each rsu in range
    //         for (WydotRsu rsu : rsus) {

    //             // add rsu to tim
    //             rsuArr[0] = rsu;
    //             timToSend.setRsus(rsuArr);            
                
    //             // update region name for active tim logger
    //             timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNamePrev + "_RSU-" + rsu.getRsuTarget() + "_" + timTypeString);
              
    //            // activeTims = ActiveTimService.getActiveRSUTimsByClientId(wydotTimRw.getClientId());

    //             // update tim                       
    //             if(activeTims != null && activeTims.size() > 0){                                            
    //                 // return error                     
    //             }              
    //             else{     
    //                 // send new tim to rsu                    
    //                 sendNewTimToRsu(timToSend, rsu);  
    //             }
    //         }

    //         // satellite
    //         List<ActiveTim> activeSatTims = ActiveTimService.getActiveSATTimsByClientId(wydotTimRw.getClientId());
        
    //         if(activeSatTims != null && activeSatTims.size() > 0){
    //             // return error
    //         }
    //         else{
    //             String recordId = getNewRecordId();    
    //             timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNamePrev + "_SAT-" + recordId + "_" + timTypeString);
    //             sendNewTimToSdw(timToSend, recordId);
    //         }                
    //     }
    // }

    // public boolean deleteRwTim(String clientId){        
        
    //     // get tim type            
    //     List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
    //     activeTims = ActiveTimService.getActiveTimsByClientId(clientId);   
        
    //     for (ActiveTim activeTim : activeTims) {
    //         // get all tims
    //         J2735TravelerInformationMessage tim = TimService.getTim(activeTim.getActiveTimId());                    
    //         // get RSU TIM is on
    //         List<TimRsu> timRsus = TimRsuService.getTimRsusByTimId(activeTim.getTimId());
    //         // get full RSU
    //         for (TimRsu timRsu : timRsus) {
    //             // get rsu
    //             WydotRsu rsu = getRsu(timRsu.getRsuId());
    //             // delete tim off rsu
    //             deleteTimFromRsu(rsu, tim.getIndex()); 
    //         }
    //         // delete active tims                
    //         ActiveTimService.deleteActiveTim(activeTim.getActiveTimId());                
    //     }  
    
    //     return true;
    // }

    // public List<String> setItisCodes(WydotTimRw wydotTimRw){
        
    //     List<String> items = new ArrayList<String>();               
    //     for (Integer item : wydotTimRw.getAdvisory())
    //         items.add(item.toString());                       
                 
    //     return items;
    // }

    // public String convertUtcDateTimeToLocal(String utcDateTime){  

    //     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");            
    //     LocalDateTime startDate = LocalDateTime.parse(utcDateTime, formatter);        
    //     ZoneId mstZoneId = ZoneId.of("America/Denver");              
    //     ZonedDateTime mstZonedDateTime = startDate.atZone(mstZoneId);      
    //     String startDateTime = mstZonedDateTime.toLocalDateTime().toString() + "-06:00";
      
    //     return startDateTime;
    // }

    // public long getMinutesDurationBetweenTwoDates(String startDateTime, String endDateTime){

    //     LocalDateTime endDate = LocalDateTime.parse(endDateTime, utcformatter);
    //     LocalDateTime startDateTimeLocal = LocalDateTime.parse(startDateTime, utcformatter);                
    //     java.time.Duration dateDuration = java.time.Duration.between(startDateTimeLocal, endDate);
    //     Math.abs(dateDuration.toMinutes());
    //     long durationTime = Math.abs(dateDuration.toMinutes());
    //     return durationTime;
    // }

}