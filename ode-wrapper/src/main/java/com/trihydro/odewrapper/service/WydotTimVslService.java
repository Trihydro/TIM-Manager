package com.trihydro.odewrapper.service;

import com.trihydro.odewrapper.model.WydotTimVsl;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.TimRsuService;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.ArrayList;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.odewrapper.model.WydotTravelerInputData;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.model.TimRsu;
import com.trihydro.library.model.TimType;
import org.springframework.core.env.Environment;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;

@Component
public class WydotTimVslService extends WydotTimService
{    
    private CreateBaseTimUtil createBaseTimUtil;

    @Autowired
    public Environment env;

	@Autowired
	WydotTimVslService(CreateBaseTimUtil createBaseTimUtil ) {
        super();
        this.createBaseTimUtil = createBaseTimUtil;
    }	

    // creates and updates VSL TIMs based on starting and stopping mileposts
    public void createUpdateVslTim(List<WydotTimVsl> timVslList) {

        System.out.println("creating VSL tim...");

        Long timId = null;
        
        // for each tim in wydot's request
        for (WydotTimVsl wydotTimVsl : timVslList) {
                    
            // set up the rsu array
            WydotRsu[] rsuArr = new WydotRsu[1];         

            // FIND ALL RSUS TO SEND TO     
            List<WydotRsu> rsus = getRsusInBuffer(wydotTimVsl.getDirection(), Math.min(wydotTimVsl.getToRm(), wydotTimVsl.getFromRm()), Math.max(wydotTimVsl.getToRm(), wydotTimVsl.getFromRm()));       

            System.out.println("rsu list: " + rsus.size());

            // build base TIM                
            WydotTravelerInputData timToSend = createBaseTimUtil.buildTim(wydotTimVsl);

            // add Road Conditions itis codes
            List<String> items = setItisCodes(wydotTimVsl);   
            timToSend.getTim().getDataframes()[0].setItems(items.toArray(new String[items.size()]));
 
            // get tim type            
            TimType timType = getTimType("VSL");

            // build region name for active tim logger to use       
            String regionNamePrev = wydotTimVsl.getDirection() + "_" + wydotTimVsl.getRoute() + "_" + wydotTimVsl.getFromRm() + "_" + wydotTimVsl.getToRm();   
                        
            // query for existing active tims for this road segment
            List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
            if(timType != null)
                activeTims = ActiveTimService.getAllActiveTims(wydotTimVsl.getFromRm(), wydotTimVsl.getToRm(), timType.getTimTypeId(), wydotTimVsl.getDirection());   
            
            // if there are active tims for this area
            if(activeTims.size() > 0) {                    
                
                // query database for rsus that active tims could be on
                List<TimRsu> timRsus = TimRsuService.selectAll();

                // for each active tim
                for (ActiveTim activeTim : activeTims) {                                
                    
                    // look to see if tim is on RSU
                    TimRsu activeTimRsu = timRsus.stream()
                        .filter(x -> x.getTimId().equals(activeTim.getTimId()))
                        .findFirst()
                        .orElse(null);
                                                                
                    // if so, update RSU
                    if(activeTimRsu != null){

                        // get rsu
                        WydotRsu rsu = rsus.stream()
                        .filter(x -> x.getRsuId().equals(Integer.valueOf(activeTimRsu.getRsuId().toString())))
                        .findFirst()
                        .orElse(null);

                        // add rsu to tim
                        rsuArr[0] = rsu;
                        timToSend.setRsus(rsuArr);            
                        
                        // update region name for active tim logger
                        timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNamePrev + "_RSU-" + rsu.getRsuTarget() + "_VSL");  

                        // update TIM rsu
                        updateTimOnRsu(timToSend, activeTim.getTimId());   
                    }
                    // else active tim is satellite 
                    else{
                        // update satellite tim                         
                        timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNamePrev + "_SAT-" + activeTim.getRecordId() + "_VSL");  
                        updateTimOnSdw(timToSend, activeTim.getTimId(), activeTim.getRecordId());
                    }    
                }                
            }     
            // else add new active tim
            else {
                // for each rsu                
                for(int i = 0; i < rsus.size(); i++) {
                    // add rsu to tim
                    rsuArr[0] = rsus.get(i);
                    timToSend.setRsus(rsuArr); 
                                     
                    // send tim to rsu
                    timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNamePrev + "_RSU-" + rsus.get(i).getRsuTarget() + "_VSL");  
                    sendNewTimToRsu(timToSend, rsus.get(i)); 
                    
                }            
                // send TIM to satellite            
                String recordId = getNewRecordId();         
                timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNamePrev + "_SAT-" + recordId + "_VSL");
                sendNewTimToSdw(timToSend, recordId);
            }     
        }
    }	

    public List<String> setItisCodes(WydotTimVsl wydotTimVsl){
        
        List<String> items = new ArrayList<String>();        
        
        ItisCode speedLimit = getItisCodes().stream()
            .filter(x -> x.getDescription().equals("Speed Limit"))
            .findFirst()
            .orElse(null);
            if(speedLimit != null) {
            items.add(speedLimit.getItisCode().toString());           
        }

        ItisCode speed = getItisCodes().stream()
            .filter(x -> x.getDescription().equals(wydotTimVsl.getSpeed().toString()))
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
}