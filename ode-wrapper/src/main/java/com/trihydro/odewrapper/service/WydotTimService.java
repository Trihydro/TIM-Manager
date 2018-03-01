package com.trihydro.odewrapper.service;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Random;
import java.util.Arrays;
import java.util.Collections;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.odewrapper.model.WydotTravelerInputData;
import com.trihydro.odewrapper.model.TimQuery;
import com.trihydro.library.model.TimType;
import com.trihydro.odewrapper.model.WydotTimBase;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import com.google.gson.Gson;
import com.trihydro.library.service.tim.ActiveTimLogger;
import com.trihydro.library.service.tim.TimRsuLogger;
import com.trihydro.library.service.tim.ActiveTimItisCodeLogger;
import com.trihydro.library.service.tim.TimService;
import com.trihydro.library.service.timtype.TimTypeService;
import com.trihydro.odewrapper.helpers.DBUtility;
// import com.trihydro.odewrapper.service.J2735TravelerInformationMessageService;
import com.trihydro.library.model.ActiveTim;
import org.springframework.http.MediaType;

@Component
public class WydotTimService
{    
    @Autowired
    public static Environment env;
    public static DBUtility dbUtility;
    // private static J2735TravelerInformationMessageService j2735TravelerInformationMessageService;
    public static RestTemplate restTemplate = new RestTemplate();         
    public static Gson gson = new Gson();

    @Autowired
    public void setDBUtility(DBUtility dbUtilityRh) {
        dbUtility = dbUtilityRh;
    }

    public DBUtility getDBUtility() {
        return dbUtility;
    }

    // @Autowired
    // public void setJ2735TravelerInformationMessageService(J2735TravelerInformationMessageService j2735TravelerInformationMessageServiceRh) {
    //     j2735TravelerInformationMessageService = j2735TravelerInformationMessageServiceRh;
    // }

    // public J2735TravelerInformationMessageService getJ2735TravelerInformationMessageService() {
    //     return j2735TravelerInformationMessageService;
    // }

    public static Long sendNewTimToRsu(WydotTravelerInputData timToSend, WydotRsu rsu) {
        // set msgCnt to 1 and create new packetId
        timToSend.getTim().setMsgCnt(1);

        Random rand = new Random();            
        int randomNum = rand.nextInt(100000) + 10000;            
        String packetIdHexString = Integer.toHexString(randomNum);
        packetIdHexString = packetIdHexString + String.join("", Collections.nCopies(18 - packetIdHexString.length(), "0"));
        timToSend.getTim().setPacketID(packetIdHexString);

        // set RSU index and send TIM if query is successful 
        TimQuery timQuery = submitTimQuery(rsu, 0);
        if(timQuery != null){
            timToSend.getTim().setIndex(findFirstAvailableIndex(timQuery.getIndicies_set()));
            String timToSendJson = gson.toJson(timToSend); 
            // send TIM if not a test
            restTemplate.postForObject("https://ode.wyoroad.info:8443/tim", timToSendJson, String.class);
        }
        else
            timToSend.getTim().setIndex(0);
                                              
        // save TIM to DB 
        
        // to do 
        //Long timId = sendTimToDB(timToSend, j2735TravelerInformationMessageService);

        //TimRsuLogger.insertTimRsu(timId, rsu.getRsuId(), dbUtility.getConnection());

        Long timId = new Long(0);
        return timId;
    }

    public static Long updateTimOnRsu(WydotTravelerInputData timToSend, Long activeTimId, Integer rsuId){

        // get existing TIM
        J2735TravelerInformationMessage tim = TimService.getTim(activeTimId, dbUtility.getConnection());                    
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

        String timToSendJson = gson.toJson(timToSend); 

        // send TIM to ODE if not a test
        if(tim.getIndex() != 0)
            restTemplate.put("https://ode.wyoroad.info:8443/tim", timToSendJson, String.class);   

        // save TIM to DB 
        // TODO

        //Long timId = sendTimToDB(timToSend, j2735TravelerInformationMessageService);  

        // Update TIM_RSU table
      //  TimRsuLogger.insertTimRsu(timId, rsuId, dbUtility.getConnection());
        
        Long timId = new Long(0);
        return timId;
    }  
    
    public static void updateActiveTims(ActiveTim activeTim, List<Integer> itisCodeIds, Long timId, String endDateTime){
        // update Active TIM table TIM Id
        ActiveTimLogger.updateActiveTimTimId(activeTim.getActiveTimId(), timId, dbUtility.getConnection());

        if(endDateTime != null)
            ActiveTimLogger.updateActiveTimEndDate(activeTim.getActiveTimId(), endDateTime, dbUtility.getConnection());        
      
        // remove Active TIM ITIS Codes
        ActiveTimItisCodeLogger.deleteActiveTimItisCodes(activeTim.getActiveTimId(), dbUtility.getConnection());

        // Add new Active TIM ITIS Codes
        for(int j = 0; j < itisCodeIds.size(); j++)
            ActiveTimItisCodeLogger.insertActiveTimItisCode(activeTim.getActiveTimId(), itisCodeIds.get(j), dbUtility.getConnection());       
    }

    public static Long addActiveTim(Long timId, WydotTimBase wydotTim, List<Integer> itisCodeIds, TimType timType, String startDateTime, String endDateTime, String clientId){       

        // Send TIM to Active Tim List        
        Long activeTimId = ActiveTimLogger.insertActiveTim(timId, wydotTim.getFromRm(), wydotTim.getToRm(), wydotTim.getDirection(), timType.getTimTypeId(), startDateTime, endDateTime, wydotTim.getRoute(), clientId, dbUtility.getConnection());

        // Add ActiveTim ITIS Codes
        for(int j = 0; j < itisCodeIds.size(); j++)
            ActiveTimItisCodeLogger.insertActiveTimItisCode(activeTimId, itisCodeIds.get(j), dbUtility.getConnection());  
            
        return activeTimId;
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
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);                
        HttpEntity<String> entity = new HttpEntity<String>(rsuJson, headers);

        try{
            restTemplate.delete("https://ode.wyoroad.info:8443/tim?index=" + index.toString(), entity, String.class);   
        }        
        catch(HttpClientErrorException e ){
            System.out.println(e.getMessage());
        }
    }
        
            
    public static void removeActiveTim(){
    
        // remove Active TIM ITIS Codes
       // ActiveTimItisCodeLogger.deleteActiveTimItisCodes(activeTim.getActiveTimId(), dbUtility.getConnection());

        // delete tim from ActiveTim table

        
    }

    public static TimType getTimType(String timTypeName){
        // get tim type
        List<TimType> timTypes = TimTypeService.selectAll(dbUtility.getConnection());

        TimType timType = timTypes.stream()
        .filter(x -> x.getType().equals(timTypeName))
        .findFirst()
        .orElse(null);

        return timType;
    } 
    
    public static Long sendTimToDB(WydotTravelerInputData travelerInputData) { 
        // insert tim	
        // Long timId = j2735TravelerInformationMessageService.insertTIM(travelerInputData.getTim());

        //Long timId = TimLogger.insertTim(travelerInputData.getTim(), J2735TravelerInformationMessage j2735TravelerInformationMessage, Connection connection) { 
        Long timId = new Long(0);
        return timId;
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