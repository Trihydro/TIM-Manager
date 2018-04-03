package com.trihydro.odewrapper.service;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

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
import org.springframework.http.HttpMethod;

import us.dot.its.jpo.ode.plugin.SNMP;
import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW;
import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW.TimeToLive;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.OdeGeoRegion;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;

import com.google.gson.Gson;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.ItisCodeService;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.TimService;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.model.Milepost;
import org.springframework.http.MediaType;

@Component
public class WydotTimService
{    
    @Autowired
    public static Environment env;
    public static RestTemplate restTemplate = new RestTemplate();         
    public static Gson gson = new Gson();
    private List<ItisCode> itisCodes;
    private List<WydotRsu> rsus;    
    private List<TimType> timTypes;    
    WydotRsu[] rsuArr = new WydotRsu[1];    
    DateTimeFormatter utcformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");            
    DateTimeFormatter mdtformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss-06:00");            

    public List<ItisCode> getItisCodes(){
        if(itisCodes != null)
            return itisCodes;
        else{
            itisCodes = ItisCodeService.selectAll(); 
            return itisCodes;
        }
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

    public List<WydotRsu> getRsusInBuffer(String direction, Double lowerMilepost, Double higherMilepost){
        
        int buffer = 5;

        List<WydotRsu> rsus = null;

        if(direction.toLowerCase().equals("eastbound")) {		
            Double startBuffer = lowerMilepost - buffer;	

            rsus = getRsus().stream()
            .filter(x -> x.getMilepost() >= startBuffer)
            .filter(x -> x.getMilepost() <= higherMilepost)
            .filter(x -> x.getRoute().matches(".*80.*"))
            .collect(Collectors.toList());
           
        }
        else{
            Double startBuffer = higherMilepost + buffer;		

            rsus = getRsus().stream()
            .filter(x -> x.getMilepost() >= lowerMilepost)
            .filter(x -> x.getMilepost() <= startBuffer)
            .filter(x -> x.getRoute().matches(".*80.*"))
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
        sdw.setTtl(TimeToLive.oneyear);
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
        sdw.setTtl(TimeToLive.oneyear);
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

    public static Long addActiveTim(Long timId, WydotTimBase wydotTim, List<Integer> itisCodeIds, TimType timType, String startDateTime, String endDateTime, String clientId){       

        // Send TIM to Active Tim List        
        Long activeTimId = ActiveTimService.insertActiveTim(timId, wydotTim.getFromRm(), wydotTim.getToRm(), wydotTim.getDirection(), timType.getTimTypeId(), startDateTime, endDateTime, wydotTim.getRoute(), clientId, null);
            
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