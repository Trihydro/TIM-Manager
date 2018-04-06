package com.trihydro.odewrapper.service;

import com.trihydro.odewrapper.model.WydotTimVsl;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.service.ActiveTimService;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.ArrayList;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.odewrapper.model.WydotTravelerInputData;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.model.TimType;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;

@Component
public class WydotTimVslService extends WydotTimService
{    
    private CreateBaseTimUtil createBaseTimUtil;

	@Autowired
	WydotTimVslService(CreateBaseTimUtil createBaseTimUtil ) {
        super();
        this.createBaseTimUtil = createBaseTimUtil;
        timTypeString = "VSL";
    }	

    // creates and updates VSL TIMs based on starting and stopping mileposts
    public void createUpdateVslTim(List<WydotTimVsl> timVslList) {
        
        // for each tim in wydot's request
        for (WydotTimVsl wydotTimVsl : timVslList) {
            
            System.out.println("VSL TIM");
            System.out.println("direction:" + wydotTimVsl.getDirection());
            System.out.println("route:" + wydotTimVsl.getRoute());
            System.out.println("fromRm:" + wydotTimVsl.getFromRm());
            System.out.println("toRm:" + wydotTimVsl.getToRm());
            System.out.println("speed:" + wydotTimVsl.getSpeed());

            // FIND ALL RSUS TO SEND TO     
            List<WydotRsu> rsus = getRsusInBuffer(wydotTimVsl.getDirection(), Math.min(wydotTimVsl.getToRm(), wydotTimVsl.getFromRm()), Math.max(wydotTimVsl.getToRm(), wydotTimVsl.getFromRm()));       

            // build base TIM                
            WydotTravelerInputData timToSend = createBaseTimUtil.buildTim(wydotTimVsl);

            // add VSL itis codes
            List<String> items = setItisCodes(wydotTimVsl);   
            timToSend.getTim().getDataframes()[0].setItems(items.toArray(new String[items.size()]));
 
            // get tim type            
            TimType timType = getTimType(timTypeString);

            // build region name for active tim logger to use       
            String regionNamePrev = wydotTimVsl.getDirection() + "_" + wydotTimVsl.getRoute() + "_" + wydotTimVsl.getFromRm() + "_" + wydotTimVsl.getToRm();   
              
            // query database for rsus that active tims could be on
            List<ActiveTim> activeTims = null;
            // for each rsu in range
            for (WydotRsu rsu : rsus) {

                // add rsu to tim
                rsuArr[0] = rsu;
                timToSend.setRsus(rsuArr);            
                
                // update region name for active tim logger
                timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNamePrev + "_RSU-" + rsu.getRsuTarget() + "_" + timTypeString);
                
                activeTims = ActiveTimService.getActiveVSLTimsOnRsu(rsu.getRsuTarget(), wydotTimVsl.getFromRm(), wydotTimVsl.getToRm(), wydotTimVsl.getDirection());

                // if active TIMs exist                      
                if(activeTims != null && activeTims.size() > 0){                                            
                    // update TIM rsu
                    updateTimOnRsu(timToSend, activeTims.get(0).getTimId());                                
                }              
                else{     
                    // send new tim to rsu                    
                    sendNewTimToRsu(timToSend, rsu);  
                }
            }

            // satellite
            List<ActiveTim> activeSatTims = ActiveTimService.getActiveSatTims(wydotTimVsl.getFromRm(), wydotTimVsl.getToRm(), timType.getTimTypeId(), wydotTimVsl.getDirection());
        
            if(activeSatTims != null && activeSatTims.size() > 0){
                timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNamePrev + "_SAT-" + activeSatTims.get(0).getRecordId() + "_" + timTypeString);  
                updateTimOnSdw(timToSend, activeSatTims.get(0).getTimId(), activeSatTims.get(0).getRecordId());
            }
            else{
                String recordId = getNewRecordId();    
                timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNamePrev + "_SAT-" + recordId + "_" + timTypeString);
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