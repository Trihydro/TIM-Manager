package com.trihydro.odewrapper.service;

import com.trihydro.odewrapper.model.WydotTimRw;
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
import org.springframework.core.env.Environment;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;
import com.trihydro.odewrapper.model.WydotTravelerInputData;
import com.trihydro.library.service.TimRsuService;
import com.trihydro.library.model.TimRsu;
import static java.lang.Math.toIntExact;


@Component
public class WydotTimRwService extends WydotTimService
{    
    private CreateBaseTimUtil createBaseTimUtil;    

    @Autowired
    public Environment env;

	@Autowired
	WydotTimRwService(CreateBaseTimUtil createBaseTimUtil) {      
        this.createBaseTimUtil = createBaseTimUtil;
    }	

    public void createRwTim(List<WydotTimRw> timRwList) {
            
        Long timId = null;

        // discard non-I-80 requests 
        for (WydotTimRw wydotTimRw : timRwList) {    

            // FIND ALL RSUS TO SEND TO     
            List<WydotRsu> rsus = getRsusInBuffer(wydotTimRw.getDirection(), Math.min(wydotTimRw.getToRm(), wydotTimRw.getFromRm()), Math.max(wydotTimRw.getToRm(), wydotTimRw.getFromRm()));       

            // build base TIM                
            WydotTravelerInputData timToSend = createBaseTimUtil.buildTim(wydotTimRw);

            // add Road Contruction ITIS Codes            
            List<String> items = setItisCodes(wydotTimRw);            
            timToSend.getTim().getDataframes()[0].setItems(items.toArray(new String[items.size()]));

            // Convert start date time coming from REST call to a local format
            //String startDateTimeLocal = convertUtcDateTimeToLocal(wydotTimRw.getStartDateTime());

            // convert to local time
            String startDateTimeLocal = convertUtcDateTimeToLocal(wydotTimRw.getStartDateTime());
            timToSend.getTim().getDataframes()[0].setStartDateTime(startDateTimeLocal);

            // duration time
            if(wydotTimRw.getEndDateTime() != null){               
                long durationTime = getMinutesDurationBetweenTwoDates(wydotTimRw.getStartDateTime(), wydotTimRw.getEndDateTime());
                timToSend.getTim().getDataframes()[0].setDurationTime(toIntExact(durationTime));
            }            
                
            // get tim type            
            TimType timType = getTimType("RW");
            
            String regionName = wydotTimRw.getDirection() + "_" + wydotTimRw.getRoute() + "_" + wydotTimRw.getFromRm() + "_" + wydotTimRw.getToRm();           
            
            // for each rsu
            for(int i = 0; i < rsus.size(); i++) {

                // query RSU for indices
                rsuArr[0] = rsus.get(i);
                timToSend.setRsus(rsuArr);                 
                
                regionName += "_RSU-" + rsus.get(i).getRsuTarget();
                regionName += "_RW_" + wydotTimRw.getClientId();                   
                timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionName);  
                
                timId = sendNewTimToRsu(timToSend, rsus.get(i));                                                                      
            }                                           

            // TODO: Send to SAT
        }
    }	  

    public void updateRwTim(List<WydotTimRw> timRwList) {
        
        Long timId = null;

        // discard non-I-80 requests 
        for (WydotTimRw wydotTimRw : timRwList) {                         

            // FIND ALL RSUS TO SEND TO     
            List<WydotRsu> rsus = getRsusInBuffer(wydotTimRw.getDirection(), Math.min(wydotTimRw.getToRm(), wydotTimRw.getFromRm()), Math.max(wydotTimRw.getToRm(), wydotTimRw.getFromRm()));       

            // build base TIM                
            WydotTravelerInputData timToSend = createBaseTimUtil.buildTim(wydotTimRw);

            // add Road Work ITIS Codes            
            List<String> items = setItisCodes(wydotTimRw);
            timToSend.getTim().getDataframes()[0].setItems(items.toArray(new String[items.size()]));

            // Convert start date time coming from REST call to a local format
            String startDateTimeLocal = convertUtcDateTimeToLocal(wydotTimRw.getStartDateTime());
            timToSend.getTim().getDataframes()[0].setStartDateTime(startDateTimeLocal);

            // duration time
            if(wydotTimRw.getEndDateTime() != null){               
                long durationTime = getMinutesDurationBetweenTwoDates(wydotTimRw.getStartDateTime(), wydotTimRw.getEndDateTime());
                timToSend.getTim().getDataframes()[0].setDurationTime(toIntExact(durationTime));
            }    

            // get tim type            
            TimType timType = getTimType("RW");

            // set region name
            String regionName = wydotTimRw.getDirection() + "_" + wydotTimRw.getRoute() + "_" + wydotTimRw.getFromRm() + "_" + wydotTimRw.getToRm();           
            
            List<ActiveTim> activeTims = new ArrayList<ActiveTim>();

            // get active TIMs be client ID
            if(timType != null)
                activeTims = ActiveTimService.getActiveTimsByClientId(wydotTimRw.getClientId());   
            
            // if there are active tims for this area
            if(activeTims.size() > 0) {                    
                for (ActiveTim activeTim : activeTims) {                                
                    // if active tim is RSU tim
                    List<TimRsu> timRsus =  TimRsuService.getTimRsusByTimId(activeTim.getTimId());     
                    
                    // if RSU tim
                    if(timRsus.size() == 1){

                        WydotRsu rsu = rsus.stream()
                        .filter(x -> x.getRsuId().equals(Integer.valueOf(timRsus.get(0).getRsuId().toString())))
                        .findFirst()
                        .orElse(null);

                        // set tim RSU
                        rsuArr[0] = rsu;
                        timToSend.setRsus(rsuArr);

                        regionName += "_RSU-" + rsu.getRsuTarget();
                        regionName += "_RW_" + wydotTimRw.getClientId();                   
                        timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionName);  

                        // update TIM rsu
                        updateTimOnRsu(timToSend, activeTim.getTimId());                                               
                    }
                    // else active tim is satellite 
                    else {
                        // TODO - Send to Sat
                   

                        // TODO - Update Active Sat TIM
                    }    
                }                
            }          

            // TODO: Send to SAT
        }
    }

    public boolean deleteRwTim(String clientId){        
        
        // get tim type            
        List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
        activeTims = ActiveTimService.getActiveTimsByClientId(clientId);   
        
        for (ActiveTim activeTim : activeTims) {
            // get all tims
            J2735TravelerInformationMessage tim = TimService.getTim(activeTim.getActiveTimId());                    
            // get RSU TIM is on
            List<TimRsu> timRsus = TimRsuService.getTimRsusByTimId(activeTim.getTimId());
            // get full RSU
            for (TimRsu timRsu : timRsus) {
                // get rsu
                WydotRsu rsu = getRsu(timRsu.getRsuId());
                // delete tim off rsu
                deleteTimFromRsu(rsu, tim.getIndex()); 
            }
            // delete active tims                
            ActiveTimService.deleteActiveTim(activeTim.getActiveTimId());                
        }  
    
        return true;
    }

    public List<String> setItisCodes(WydotTimRw wydotTimRw){
        
        List<String> items = new ArrayList<String>();               
        for (Integer item : wydotTimRw.getAdvisory())
            items.add(item.toString());                       
                 
        return items;
    }

    public String convertUtcDateTimeToLocal(String utcDateTime){  

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");            
        LocalDateTime startDate = LocalDateTime.parse(utcDateTime, formatter);        
        ZoneId mstZoneId = ZoneId.of("America/Denver");              
        ZonedDateTime mstZonedDateTime = startDate.atZone(mstZoneId);      
        String startDateTime = mstZonedDateTime.toLocalDateTime().toString() + "-06:00";
      
        return startDateTime;
    }

    public long getMinutesDurationBetweenTwoDates(String startDateTime, String endDateTime){

        LocalDateTime endDate = LocalDateTime.parse(endDateTime, utcformatter);
        LocalDateTime startDateTimeLocal = LocalDateTime.parse(startDateTime, utcformatter);                
        java.time.Duration dateDuration = java.time.Duration.between(startDateTimeLocal, endDate);
        Math.abs(dateDuration.toMinutes());
        long durationTime = Math.abs(dateDuration.toMinutes());
        return durationTime;
    }

}