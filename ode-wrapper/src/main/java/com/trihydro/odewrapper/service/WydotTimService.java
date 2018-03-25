package com.trihydro.odewrapper.service;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

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

import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW;
import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW.TimeToLive;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.OdeGeoRegion;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;

import com.google.gson.Gson;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.ItisCodeService;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.ActiveTimItisCodeService;
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
    WydotRsu[] rsuArr = new WydotRsu[1];    
    DateTimeFormatter utcformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z[UTC]'");            
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
                
        WydotRsu wydotRsu = rsus.stream()
            .filter(x -> x.getRsuId() == rsuId.intValue())
            .findFirst()
            .orElse(null);

        return wydotRsu;
    }

    public static Long sendNewTimToRsu(WydotTravelerInputData timToSend, WydotRsu rsu) {
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

    public static void sendNewTimToSdw(WydotTravelerInputData timToSend){

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
        // create new record id
        String recordId = getNewRecordId();
        sdw.setRecordId(recordId);
                
        // update region name to include record id 
        String regionName = timToSend.getTim().getDataframes()[0].getRegions()[0].getName();
        regionName += "_SAT-" + recordId;
        timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionName);

        // set sdw block in TIM
        timToSend.setSdw(sdw);

        // send to ODE
        String timToSendJson = gson.toJson(timToSend);
        restTemplate.postForObject("https://ode.wyoroad.info:8443/tim", timToSendJson, String.class);
    }


    
    private static String getNewRecordId() {
        String hexChars = "ABCDEF1234567890";
        StringBuilder hexStrB = new StringBuilder();
        Random rnd = new Random();
        while (hexStrB.length() < 9) { // length of the random string.
            int index = (int) (rnd.nextFloat() * hexChars.length());
            hexStrB.append(hexChars.charAt(index));
        }
        String hexStr = hexStrB.toString();
        return hexStr;
    }

    public static void updateTimOnRsu(WydotTravelerInputData timToSend, Long activeTimId){

        String timToSendJson = updateTim(timToSend, activeTimId);

        // send TIM to ODE if not a test
        //if(tim.getIndex() != 0)
        restTemplate.put("https://ode.wyoroad.info:8443/tim", timToSendJson, String.class);        
    } 

    public static void updateTimOnSdw(WydotTravelerInputData timToSend, Long activeTimId){

        String timToSendJson = updateTim(timToSend, activeTimId);

        // send TIM to ODE if not a test
        //if(tim.getIndex() != 0)
        restTemplate.postForObject("https://ode.wyoroad.info:8443/tim", timToSendJson, String.class);        
    } 

    public static String updateTim(WydotTravelerInputData timToSend, Long activeTimId){
        // get existing TIM
        J2735TravelerInformationMessage tim = TimService.getTim(activeTimId);                    
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
        return timToSendJson;
    }
    
    public static void updateActiveTims(ActiveTim activeTim, List<Integer> itisCodeIds, Long timId, String endDateTime){
        // update Active TIM table TIM Id
        ActiveTimService.updateActiveTimTimId(activeTim.getActiveTimId(), timId);

        if(endDateTime != null)
            ActiveTimService.updateActiveTimEndDate(activeTim.getActiveTimId(), endDateTime);        
      
        // remove Active TIM ITIS Codes
        ActiveTimItisCodeService.deleteActiveTimItisCodes(activeTim.getActiveTimId());

        // Add new Active TIM ITIS Codes
        for(int j = 0; j < itisCodeIds.size(); j++)
            ActiveTimItisCodeService.insertActiveTimItisCode(activeTim.getActiveTimId(), itisCodeIds.get(j));       
    }

    public static Long addActiveTim(Long timId, WydotTimBase wydotTim, List<Integer> itisCodeIds, TimType timType, String startDateTime, String endDateTime, String clientId){       

        // Send TIM to Active Tim List        
        Long activeTimId = ActiveTimService.insertActiveTim(timId, wydotTim.getFromRm(), wydotTim.getToRm(), wydotTim.getDirection(), timType.getTimTypeId(), startDateTime, endDateTime, wydotTim.getRoute(), clientId, null);

        // Add ActiveTim ITIS Codes
        for(int j = 0; j < itisCodeIds.size(); j++)
            ActiveTimItisCodeService.insertActiveTimItisCode(activeTimId, itisCodeIds.get(j));  
            
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
        List<TimType> timTypes = TimTypeService.selectAll();

        TimType timType = timTypes.stream()
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