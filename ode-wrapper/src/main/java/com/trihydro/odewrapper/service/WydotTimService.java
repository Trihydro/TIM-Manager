package com.trihydro.odewrapper.service;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import com.trihydro.library.model.WydotRsu;
import com.trihydro.odewrapper.model.WydotTravelerInputData;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;
import com.trihydro.library.model.IncidentChoice;
import com.trihydro.odewrapper.model.TimQuery;
import com.trihydro.library.model.TimType;
import com.trihydro.odewrapper.model.WydotTim;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import us.dot.its.jpo.ode.plugin.SNMP;
import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW;
import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW.TimeToLive;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.OdeGeoRegion;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;

import com.google.gson.Gson;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.IncidentChoicesService;
import com.trihydro.library.service.ItisCodeService;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.TimRsuService;
import com.trihydro.library.service.TimService;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.TimRsu;

import org.springframework.http.MediaType;
import static java.lang.Math.toIntExact;

@Component
public class WydotTimService
{    
    @Autowired
    public static Environment env;
    public static RestTemplate restTemplate = new RestTemplate();         
    public static Gson gson = new Gson();
    private List<ItisCode> itisCodes;
    private List<IncidentChoice> incidentProblems;
    private List<IncidentChoice> incidentEffects;
    private List<IncidentChoice> incidentActions;
    private List<WydotRsu> rsus;    
    private List<TimType> timTypes;    
    WydotRsu[] rsuArr = new WydotRsu[1];    
    DateTimeFormatter utcformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");            
    DateTimeFormatter mdtformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss-06:00");        

    public String createUpdateTim(String timTypeStr, WydotTim wydotTim, String direction){
                 
        // for each tim in wydot's request      
        String returnMessage = "";      
        System.out.println(timTypeStr + " TIM");
        System.out.println("direction: " + wydotTim.getDirection());
        String route = (wydotTim.getRoute() != null ? wydotTim.getRoute() : wydotTim.getHighway());
        route = route.replaceAll("\\D+","");
        System.out.println("route: " + route);
        System.out.println("fromRm: " + wydotTim.getFromRm());
        System.out.println("toRm: " + wydotTim.getToRm());

        // FIND ALL RSUS TO SEND TO     
        List<WydotRsu> rsus = getRsusInBuffer(direction, Math.min(wydotTim.getToRm(), wydotTim.getFromRm()), Math.max(wydotTim.getToRm(), wydotTim.getFromRm()), route);       

        // build base TIM                
        WydotTravelerInputData timToSend = CreateBaseTimUtil.buildTim(wydotTim, direction, route);

        if(timToSend.getMileposts().size() == 0){
            return "No mileposts found";
        }

        List<String> items = null;
        // add Road Conditions itis codes
        if(timTypeStr.equals("RC") || timTypeStr.equals("RW") || timTypeStr.equals("CC") || timTypeStr.equals("P") )
            items = setItisCodesFromArray(wydotTim);   
        else if(timTypeStr.equals("VSL"))
            items = setItisCodesVsl(wydotTim);   
        else if(timTypeStr.equals("I"))
            items = setItisCodesIncident(wydotTim);   
        
        if(items.size() == 0){
            return "No ITIS codes found, TIM not sent";
        }
        timToSend.getTim().getDataframes()[0].setItems(items.toArray(new String[items.size()]));

        // get tim type            
        TimType timType = getTimType(timTypeStr);


        if(wydotTim.getStartDateTime() != null){
            String startDateTimeLocal = convertUtcDateTimeToLocal(wydotTim.getStartDateTime());
            timToSend.getTim().getDataframes()[0].setStartDateTime(startDateTimeLocal);
        }
        else if(wydotTim.getTs() != null){
            String startDateTimeLocal = convertUtcDateTimeToLocal(wydotTim.getTs());
            timToSend.getTim().getDataframes()[0].setStartDateTime(startDateTimeLocal);
        }

        // duration time if there is an enddate
        if(wydotTim.getEndDateTime() != null){               
            long durationTime = getMinutesDurationBetweenTwoDates(wydotTim.getStartDateTime(), wydotTim.getEndDateTime());
            timToSend.getTim().getDataframes()[0].setDurationTime(toIntExact(durationTime));
        }    

        // if parking TIM
        if(timTypeStr.equals("P")){
            // set duration for two hours
            timToSend.getTim().getDataframes()[0].setDurationTime(120);      
        }

        // build region name for active tim logger to use            
        String regionNamePrev = direction + "_" + route + "_" + wydotTim.getFromRm() + "_" + wydotTim.getToRm();   
                                
        // query database for rsus that active tims could be on
        List<ActiveTim> activeTims = null;
        // for each rsu in range
        for (WydotRsu rsu : rsus) {

            // add rsu to tim
            rsuArr[0] = rsu;
            timToSend.setRsus(rsuArr);            
            
            // update region name for active tim logger
            String regionNameTemp = regionNamePrev + "_RSU-" + rsu.getRsuTarget() + "_" + timTypeStr;
            if(wydotTim.getClientId() != null)
                regionNameTemp += "_" + wydotTim.getClientId();
            else if(wydotTim.getIncidentId() != null)
                regionNameTemp += "_" + wydotTim.getIncidentId() + "_" + wydotTim.getPk();
            
            timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNameTemp);
            
            if(timTypeStr.equals("RC") || timTypeStr.equals("VSL"))
                activeTims = ActiveTimService.getActiveTimsOnRsuByRoadSegment(rsu.getRsuTarget(), timType.getTimTypeId(), wydotTim.getFromRm(), wydotTim.getToRm(), direction);       
            else if(timTypeStr.equals("RW") || timTypeStr.equals("CC") || timTypeStr.equals("P"))                
                activeTims = ActiveTimService.getActiveRSUTimsByClientId(timType.getTimTypeId(), wydotTim.getClientId());
            else if(timTypeStr.equals("I"))
                activeTims = ActiveTimService.getActiveRSUTimsByClientId(timType.getTimTypeId(), wydotTim.getIncidentId());

            // update tim                       
            if(activeTims != null && activeTims.size() > 0){                                            
                // update TIM rsu
                updateTimOnRsu(timToSend, activeTims.get(0).getTimId());                                
                returnMessage += "success";
            }              
            else{     
                // send new tim to rsu                    
                sendNewTimToRsu(timToSend, rsu);  
                returnMessage += "success";
            }
        }

        // satellite
        List<ActiveTim> activeSatTims = ActiveTimService.getActiveSatTims(wydotTim.getFromRm(), wydotTim.getToRm(), timType.getTimTypeId(), direction);                

        if(activeSatTims != null && activeSatTims.size() > 0){
            String regionNameTemp = regionNamePrev + "_SAT-" + activeSatTims.get(0).getSatRecordId() + "_" + timTypeStr;
            if(wydotTim.getClientId() != null)
                regionNameTemp += "_" + wydotTim.getClientId();
            else if(wydotTim.getIncidentId() != null)
                regionNameTemp += "_" + wydotTim.getIncidentId() + "_" + wydotTim.getPk();
            timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNameTemp);  
            updateTimOnSdw(timToSend, activeSatTims.get(0).getTimId(), activeSatTims.get(0).getSatRecordId());
            returnMessage += "success";
        }
        else{
            String recordId = getNewRecordId();    
            String regionNameTemp = regionNamePrev + "_SAT-" + recordId + "_" + timTypeStr;
            if(wydotTim.getClientId() != null)
                regionNameTemp += "_" + wydotTim.getClientId();
            else if(wydotTim.getIncidentId() != null)
                regionNameTemp += "_" + wydotTim.getIncidentId() + "_" + wydotTim.getPk();
            timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNameTemp);
            sendNewTimToSdw(timToSend, recordId);
            returnMessage += "success";
        }        

        return returnMessage;   
    }

    public boolean clearTimsByRoadSegment(String timTypeStr, WydotTim wydotTim, String direction){        
        
        WydotRsu rsu = null;

        String route = (wydotTim.getRoute() != null ? wydotTim.getRoute() : wydotTim.getHighway());
        route = route.replaceAll("\\D+","");

        // get tim type            
        TimType timType = getTimType(timTypeStr);

        // get all RC active tims
        List<ActiveTim> activeTims = new ArrayList<ActiveTim>();            
        if(timType != null)
            activeTims = ActiveTimService.getAllActiveTims(wydotTim.getFromRm(), wydotTim.getToRm(), timType.getTimTypeId(), direction);            

        // for each active RC TIM in this area
        for (ActiveTim activeTim : activeTims) {
            
            // get the TIM 
            J2735TravelerInformationMessage tim = TimService.getTim(activeTim.getTimId());          

            // get RSU TIM is on
            List<TimRsu> timRsus = TimRsuService.getTimRsusByTimId(activeTim.getTimId());
            
            // get full RSU               
            if(timRsus.size() == 1){
                rsu = getRsu(timRsus.get(0).getRsuId());
                // delete tim off rsu           
                deleteTimFromRsu(rsu, tim.getIndex());                 
            }
            else{
                // is satellite tim
                WydotTravelerInputData timToSend = CreateBaseTimUtil.buildTim(wydotTim, direction, route);
                String[] items = new String[1];
                items[0] = "4868";
                timToSend.getTim().getDataframes()[0].setItems(items);                    
                deleteTimFromSdw(timToSend, activeTim.getSatRecordId(), activeTim.getTimId());                    
            }

            // delete active tim                
            ActiveTimService.deleteActiveTim(activeTim.getActiveTimId());                
        }             
        
        return true;
    }

    public boolean clearTimsById(String timTypeStr, String clientId){                
     
        WydotTim wydotTim = new WydotTim();
     
        List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
        WydotRsu rsu = null;
        activeTims = ActiveTimService.getActiveTimsByClientId(clientId);   
        
        for (ActiveTim activeTim : activeTims) {

            wydotTim.setFromRm(activeTim.getMilepostStart());
            wydotTim.setToRm(activeTim.getMilepostStop());

            // get all tims
            J2735TravelerInformationMessage tim = TimService.getTim(activeTim.getActiveTimId());                    
            // get RSU TIM is on
            List<TimRsu> timRsus = TimRsuService.getTimRsusByTimId(activeTim.getTimId());
                // get full RSU
                       
                if(timRsus.size() == 1){
                rsu = getRsu(timRsus.get(0).getRsuId());
                // delete tim off rsu           
                deleteTimFromRsu(rsu, tim.getIndex());                 
            }
            else{
                // is satellite tim
                WydotTravelerInputData timToSend = CreateBaseTimUtil.buildTim(wydotTim, activeTim.getDirection(), activeTim.getRoute());
                String[] items = new String[1];
                items[0] = "4868";
                timToSend.getTim().getDataframes()[0].setItems(items);                    
                deleteTimFromSdw(timToSend, activeTim.getSatRecordId(), activeTim.getTimId());                    
            }

            // delete active tim                
            ActiveTimService.deleteActiveTim(activeTim.getActiveTimId());           
        }  
    
        return true;
    }

    public List<ActiveTim> selectTimById(String timTypeStr, String clientId){

        TimType timType = getTimType(timTypeStr);
        
        List<ActiveTim> activeTims = ActiveTimService.getActivesTimByClientId(clientId, timType.getTimTypeId());

        return activeTims;
    }

    public List<ActiveTim> selectTimsByType(String timTypeStr){

        TimType timType = getTimType(timTypeStr);
        
        List<ActiveTim> activeTims = ActiveTimService.getActivesTimByType(timType.getTimTypeId());

        return activeTims;
    }

    public List<String> setItisCodesFromArray(WydotTim wydotTim){        
        List<String> items = new ArrayList<String>();               
        for (Integer item : wydotTim.getAdvisory()){
            items.add(item.toString());                       
        }                            
        return items;
    }

    public List<ItisCode> getItisCodes(){
        if(itisCodes != null)
            return itisCodes;
        else{
            itisCodes = ItisCodeService.selectAll(); 
            return itisCodes;
        }
    }

    public List<IncidentChoice> getIncidentProblems(){
        if(incidentProblems != null)
            return incidentProblems;
        else{
            incidentProblems = IncidentChoicesService.selectAllIncidentProblems(); 
            return incidentProblems;
        }
    }

    public List<IncidentChoice> getIncidentEffects(){
        if(incidentEffects != null)
            return incidentEffects;
        else{
            incidentEffects = IncidentChoicesService.selectAllIncidentEffects(); 
            return incidentEffects;
        }
    }

    public List<IncidentChoice> getIncidentActions(){
        if(incidentActions != null)
            return incidentActions;
        else{
            incidentActions = IncidentChoicesService.selectAllIncidentActions(); 
            return incidentActions;
        }
    }

    public List<String> setItisCodesVsl(WydotTim wydotTim){
        
        List<String> items = new ArrayList<String>();        
        
        ItisCode speedLimit = getItisCodes().stream()
            .filter(x -> x.getDescription().equals("Speed Limit"))
            .findFirst()
            .orElse(null);
        if(speedLimit != null) {
            items.add(speedLimit.getItisCode().toString());           
        }

        ItisCode speed = getItisCodes().stream()
            .filter(x -> x.getDescription().equals(wydotTim.getSpeed().toString()))
            .findFirst()
            .orElse(null);
        if(speed != null) {
            items.add(speed.getItisCode().toString());   
        }

        ItisCode mph = getItisCodes().stream()
            .filter(x -> x.getDescription().equals("mph"))
            .findFirst()
            .orElse(null);
        if(mph != null){
            items.add(mph.getItisCode().toString());  
        }

        return items;
    }

    public List<String> setItisCodesIncident(WydotTim wydotTim) {        
        List<String> items = new ArrayList<String>(); 

        // action
        IncidentChoice incidentAction = getIncidentActions().stream()
            .filter(x -> x.getCode().equals(wydotTim.getAction()))
            .findFirst()
            .orElse(null);
        
        // if action is not null and action itis code exists
        if(incidentAction != null && incidentAction.getItisCodeId() != null){
            ItisCode actionItisCode = getItisCodes().stream()
                .filter(x -> x.getItisCodeId().equals(incidentAction.getItisCodeId()))
                .findFirst()
                .orElse(null);
            if(actionItisCode != null){
                items.add(actionItisCode.getItisCode().toString());  
            }
        }

        // effect
        IncidentChoice incidentEffect = getIncidentEffects().stream()
            .filter(x -> x.getCode().equals(wydotTim.getEffect()))
            .findFirst()
            .orElse(null);
        
        // if effect is not null and effect itis code exists
        if(incidentEffect != null && incidentEffect.getItisCodeId() != null){
            ItisCode effectItisCode = getItisCodes().stream()
                .filter(x -> x.getItisCodeId().equals(incidentEffect.getItisCodeId()))
                .findFirst()
                .orElse(null);
            if(effectItisCode != null){
                items.add(effectItisCode.getItisCode().toString());  
            }
        }

        // problem
        IncidentChoice incidentProblem = getIncidentProblems().stream()
            .filter(x -> x.getCode().equals(wydotTim.getProblem()))
            .findFirst()
            .orElse(null);
        
        // if problem is not null and problem itis code exists
        if(incidentProblem != null && incidentProblem.getItisCodeId() != null){
            ItisCode problemItisCode = getItisCodes().stream()
                .filter(x -> x.getItisCodeId().equals(incidentProblem.getItisCodeId()))
                .findFirst()
                .orElse(null);
            if(problemItisCode != null){
                items.add(problemItisCode.getItisCode().toString());  
            }
        }

        return items;
    }

    public long getMinutesDurationBetweenTwoDates(String startDateTime, String endDateTime){

        LocalDateTime endDate = LocalDateTime.parse(endDateTime, utcformatter);
        LocalDateTime startDateTimeLocal = LocalDateTime.parse(startDateTime, utcformatter);                
        java.time.Duration dateDuration = java.time.Duration.between(startDateTimeLocal, endDate);
        Math.abs(dateDuration.toMinutes());
        long durationTime = Math.abs(dateDuration.toMinutes());
        return durationTime;
    }

    public List<WydotRsu> getRsus(){
        if(rsus != null)
            return rsus;
        else{
            rsus = RsuService.selectAll(); 
            for (WydotRsu rsu : rsus) {
                rsu.setRsuRetries(3);
                rsu.setRsuTimeout(5000);
            }
            return rsus;
        }
    }    

    public List<TimType> getTimTypes(){
        if(timTypes != null)
            return timTypes;
        else{
            timTypes = TimTypeService.selectAll();            
            return timTypes;
        }
    }    

    public List<WydotRsu> getRsusInBuffer(String direction, Double lowerMilepost, Double higherMilepost, String route){
        
        int buffer = 5;

        List<WydotRsu> rsus = null;

        if(direction.toLowerCase().equals("eastbound")) {		
            Double startBuffer = lowerMilepost - buffer;	

            rsus = getRsus().stream()
            .filter(x -> x.getMilepost() >= startBuffer)
            .filter(x -> x.getMilepost() <= higherMilepost)
            .filter(x -> x.getRoute().matches(".*" + route + ".*"))
            .collect(Collectors.toList());
           
        }
        else{
            Double startBuffer = higherMilepost + buffer;		

            rsus = getRsus().stream()
            .filter(x -> x.getMilepost() >= lowerMilepost)
            .filter(x -> x.getMilepost() <= startBuffer)
            .filter(x -> x.getRoute().matches(".*" + route + ".*"))
            .collect(Collectors.toList());
        }

        return rsus;
    }

    public WydotRsu getRsu(Long rsuId){
                
        WydotRsu wydotRsu = getRsus().stream()
            .filter(x -> x.getRsuId() == rsuId.intValue())
            .findFirst()
            .orElse(null);

        return wydotRsu;
    }

    public static Long sendNewTimToRsu(WydotTravelerInputData timToSend, WydotRsu rsu) {
        
        // add snmp
        SNMP snmp = new SNMP();
        snmp.setChannel(178);
        snmp.setRsuid("00000083");
        snmp.setMsgid(31);
        snmp.setMode(1);
        snmp.setChannel(178);
        snmp.setInterval(2);
        snmp.setDeliverystart("2018-01-01T00:00:00-06:00");
        snmp.setDeliverystop("2019-01-01T00:00:00-06:00");
        snmp.setEnable(1);
        snmp.setStatus(4);
        timToSend.setSnmp(snmp);

        // set msgCnt to 1 and create new packetId
        timToSend.getTim().setMsgCnt(1);

        Random rand = new Random();            
        int randomNum = rand.nextInt(1000000) + 100000;            
        String packetIdHexString = Integer.toHexString(randomNum);
        packetIdHexString = String.join("", Collections.nCopies(18 - packetIdHexString.length(), "0")) + packetIdHexString;
        timToSend.getTim().setPacketID(packetIdHexString);

        // set RSU index and send TIM if query is successful 
        TimQuery timQuery = submitTimQuery(rsu, 0);
        if(timQuery != null){
            timToSend.getTim().setIndex(findFirstAvailableIndex(timQuery.getIndicies_set()));
            String timToSendJson = gson.toJson(timToSend); 
            System.out.println(timToSendJson);
            // send TIM if not a test
            restTemplate.postForObject("https://ode.wyoroad.info:8443/tim", timToSendJson, String.class);
        }
        else
            timToSend.getTim().setIndex(0);
                                              
        Long timId = new Long(0);
        return timId;
    }

    public static void sendNewTimToSdw(WydotTravelerInputData timToSend, String recordId){

        // set msgCnt to 1 and create new packetId
        timToSend.getTim().setMsgCnt(1);
        
        Random rand = new Random();            
        int randomNum = rand.nextInt(1000000) + 100000;            
        String packetIdHexString = Integer.toHexString(randomNum);
        packetIdHexString = String.join("", Collections.nCopies(18 - packetIdHexString.length(), "0")) + packetIdHexString;
        timToSend.getTim().setPacketID(packetIdHexString);

        SDW sdw = new SDW();

        // calculate service region
        sdw.setServiceRegion(getServiceRegion(timToSend.getMileposts()));

        // set time to live
        sdw.setTtl(TimeToLive.thirtyminutes);
        // set new record id
        sdw.setRecordId(recordId);

        // set sdw block in TIM
        timToSend.setSdw(sdw);

        // send to ODE
        String timToSendJson = gson.toJson(timToSend);
        restTemplate.postForObject("https://ode.wyoroad.info:8443/tim", timToSendJson, String.class);
    }
    
    protected static String getNewRecordId() {
        String hexChars = "ABCDEF1234567890";
        StringBuilder hexStrB = new StringBuilder();
        Random rnd = new Random();
        while (hexStrB.length() < 8) { // length of the random string.
            int index = (int) (rnd.nextFloat() * hexChars.length());
            hexStrB.append(hexChars.charAt(index));
        }
        String hexStr = hexStrB.toString();
        return hexStr;
    }

    public String convertUtcDateTimeToLocal(String utcDateTime){  

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");            
        LocalDateTime startDate = LocalDateTime.parse(utcDateTime, formatter);        
        ZoneId mstZoneId = ZoneId.of("America/Denver");              
        ZonedDateTime mstZonedDateTime = startDate.atZone(mstZoneId);      
        String startDateTime = mstZonedDateTime.toLocalDateTime().toString() + "-06:00";
      
        return startDateTime;
    }

    public static void updateTimOnRsu(WydotTravelerInputData timToSend, Long timId){

        WydotTravelerInputData updatedTim = updateTim(timToSend, timId);

        SNMP snmp = new SNMP();
        snmp.setChannel(178);
        snmp.setRsuid("00000083");
        snmp.setMsgid(31);
        snmp.setMode(1);
        snmp.setChannel(178);
        snmp.setInterval(2);
        snmp.setDeliverystart("2018-01-01T00:00:00-06:00");
        snmp.setDeliverystop("2019-01-01T00:00:00-06:00");
        snmp.setEnable(1);
        snmp.setStatus(4);
        timToSend.setSnmp(snmp);

        String timToSendJson = gson.toJson(updatedTim); 

        // send TIM to ODE if not a test
        //if(tim.getIndex() != 0)
        restTemplate.put("https://ode.wyoroad.info:8443/tim", timToSendJson, String.class);        
    } 

    public static void updateTimOnSdw(WydotTravelerInputData timToSend, Long timId, String recordId){

        WydotTravelerInputData updatedTim = updateTim(timToSend, timId);

        SDW sdw = new SDW();
        
        // calculate service region
        sdw.setServiceRegion(getServiceRegion(timToSend.getMileposts()));

        // set time to live
        sdw.setTtl(TimeToLive.thirtyminutes);
        // set new record id
        sdw.setRecordId(recordId);

        // set sdw block in TIM
        updatedTim.setSdw(sdw);

        String timToSendJson = gson.toJson(updatedTim); 

        // send TIM to ODE if not a test
        //if(tim.getIndex() != 0)
        restTemplate.postForObject("https://ode.wyoroad.info:8443/tim", timToSendJson, String.class);        
    } 

    public static WydotTravelerInputData updateTim(WydotTravelerInputData timToSend, Long timId){
        // get existing TIM
        J2735TravelerInformationMessage tim = TimService.getTim(timId);                    
        // set TIM packetId 
        timToSend.getTim().setPacketID(tim.getPacketID());
        // get RSU index
        timToSend.getTim().setIndex(tim.getIndex());

        // roll msgCnt over to 0 if at 127
        if(tim.getMsgCnt() == 127)
            timToSend.getTim().setMsgCnt(0);
        // else increment msgCnt
        else
            timToSend.getTim().setMsgCnt(tim.getMsgCnt() + 1);        
               
        return timToSend;
    }
    
    public static void updateActiveTims(ActiveTim activeTim, List<Integer> itisCodeIds, Long timId, String endDateTime){
        // update Active TIM table TIM Id
        ActiveTimService.updateActiveTimTimId(activeTim.getActiveTimId(), timId);

        if(endDateTime != null)
            ActiveTimService.updateActiveTimEndDate(activeTim.getActiveTimId(), endDateTime);        
    }    

    protected static TimQuery submitTimQuery(WydotRsu rsu, int counter){
        
        // stop if this fails five times
        if(counter == 2)
            return null;

        // tim query to ODE      
        String rsuJson = gson.toJson(rsu);
           
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);                
        HttpEntity<String> entity = new HttpEntity<String>(rsuJson, headers);
        
        String responseStr = null;

        try{           
            responseStr = restTemplate.postForObject("https://ode.wyoroad.info:8443/tim/query", entity, String.class);          
        }
        catch(RestClientException e){
            return submitTimQuery(rsu, counter + 1);
        }

        String[] items = responseStr.replaceAll("\\\"", "").replaceAll("\\:", "").replaceAll("indicies_set", "").replaceAll("\\{", "").replaceAll("\\}", "").replaceAll("\\[", "").replaceAll(" ", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
        
        int[] results = new int[items.length];
        
        for (int i = 0; i < items.length; i++) {
            try {
                results[i] = Integer.parseInt(items[i]);
            } catch (NumberFormatException nfe) {
                //NOTE: write something here if you need to recover from formatting errors
            };
        }

        Arrays.sort(results);

        TimQuery timQuery = new TimQuery();
        timQuery.setIndicies_set(results);
      //  TimQuery timQuery = gson.fromJson(responseStr, TimQuery.class);
        
        return timQuery;
    }

    public static void deleteTimFromRsu(WydotRsu rsu, Integer index){

        String rsuJson = gson.toJson(rsu);
        
        System.out.println(rsuJson);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);                
        HttpEntity<String> entity = new HttpEntity<String>(rsuJson, headers);

        try{
            restTemplate.exchange("https://ode.wyoroad.info:8443/tim?index=" + index.toString(), HttpMethod.DELETE, entity, String.class);              
        }        
        catch(HttpClientErrorException e ){
            System.out.println(e.getMessage());
        }
    }

    public static void deleteTimFromSdw(WydotTravelerInputData timToSend, String recordId, Long timId){
        
        WydotTravelerInputData updatedTim = updateTim(timToSend, timId);         

        SDW sdw = new SDW();
        
        // calculate service region
        sdw.setServiceRegion(getServiceRegion(timToSend.getMileposts()));

        // set time to live
        sdw.setTtl(TimeToLive.oneminute);
        // set new record id
        sdw.setRecordId(recordId);

        // set sdw block in TIM
        updatedTim.setSdw(sdw);

        String timToSendJson = gson.toJson(updatedTim); 
       
        restTemplate.postForObject("https://ode.wyoroad.info:8443/tim", timToSendJson, String.class);        
       
    }

    public TimType getTimType(String timTypeName){
        
        // get tim type       
        TimType timType = getTimTypes().stream()
        .filter(x -> x.getType().equals(timTypeName))
        .findFirst()
        .orElse(null);

        return timType;
    } 
    
    protected static OdeGeoRegion getServiceRegion(List<Milepost> mileposts){
        
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

    protected static int findFirstAvailableIndex(int[] indicies){
        for (int i = 1; i < 100; i++) {
            if(!contains(indicies, i)) {
                return i;
            }
        }
        return 0;
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