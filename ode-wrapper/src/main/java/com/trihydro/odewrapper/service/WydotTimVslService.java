package com.trihydro.odewrapper.service;

import com.trihydro.odewrapper.model.WydotTimVsl;
import com.trihydro.service.rsu.RsuService;
import com.trihydro.service.model.ActiveTim;
import com.trihydro.service.tim.ActiveTimLogger;
import com.trihydro.service.itiscode.ItisCodeService;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.ArrayList;
import com.trihydro.service.model.WydotRsu;
import com.trihydro.odewrapper.model.WydotTravelerInputData;
import com.trihydro.service.model.ItisCode;
import com.trihydro.service.model.TimType;
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

    public ArrayList<Long> createVslTim(List<WydotTimVsl> timVslList) {

        Long timId = null;
        ArrayList<Long> activeTimIds = new ArrayList<Long>();
        
        // discard non-I-80 requests 
        for (WydotTimVsl wydotTimVsl : timVslList) {

            System.out.println("From: " + wydotTimVsl.getFromRm().toString() + " To: " + wydotTimVsl.getToRm().toString());
                    
            if(!wydotTimVsl.getRoute().equals("I80")){
                return null;
            }

            // FIND ALL RSUS TO SEND TO     
            List<WydotRsu> rsus = RsuService.selectRsusInBuffer(wydotTimVsl.getDirection(), Math.min(wydotTimVsl.getToRm(), wydotTimVsl.getFromRm()), Math.max(wydotTimVsl.getToRm(), wydotTimVsl.getFromRm()), dbUtility.getConnection());       
            List<ItisCode> itisCodes = ItisCodeService.selectAll(dbUtility.getConnection());                              
            
            System.out.println("RSUS: " + rsus.size());
            System.out.println("itisCodes: " + itisCodes.size());
            
            // build base TIM                
            WydotTravelerInputData timToSend = createBaseTimUtil.buildTim(wydotTimVsl);
            
            // add VSL fields
            // items array (ITIS Codes)
            List<String> items = new ArrayList<String>();
            List<Integer> itisCodeIds = new ArrayList<Integer>();

            ItisCode speedLimit = itisCodes.stream()
                            .filter(x -> x.getDescription().equals("Speed Limit"))
                            .findFirst()
                            .orElse(null);
            if(speedLimit != null) {
                items.add(speedLimit.getItisCode().toString());
                itisCodeIds.add(speedLimit.getItisCodeId());
            }

            ItisCode speed = itisCodes.stream()
            .filter(x -> x.getDescription().equals(wydotTimVsl.getSpeed().toString()))
            .findFirst()
            .orElse(null);
            if(speed != null) {
                items.add(speed.getItisCode().toString());   
                itisCodeIds.add(speed.getItisCodeId());    
            }
                       
            ItisCode mph = itisCodes.stream()
            .filter(x -> x.getDescription().equals("mph"))
            .findFirst()
            .orElse(null);
            if(mph != null){
                items.add(mph.getItisCode().toString());  
                itisCodeIds.add(mph.getItisCodeId());     
            }
                   
            timToSend.getTim().getDataframes()[0].setItems(items.toArray(new String[items.size()]));
            
            // get tim type            
            TimType timType = getTimType("VSL");
            List<ActiveTim> activeTims = new ArrayList<ActiveTim>(); 

            if(timType != null)             
                activeTims = ActiveTimLogger.getActiveTims(wydotTimVsl.getFromRm(), wydotTimVsl.getToRm(), timType.getTimTypeId(), wydotTimVsl.getDirection(), dbUtility.getConnection());            
                
            // for each rsu
            for(int i = 0; i < rsus.size(); i++) {
                // query RSU for indices
                rsus.get(i).setRsuTimeout(Integer.parseInt(env.getProperty("rsuTimeout")));
                rsus.get(i).setRsuRetries(Integer.parseInt(env.getProperty("rsuRetries")));     
                WydotRsu[] timRsus = new WydotRsu[1];
                timRsus[0] = rsus.get(i);
                timToSend.setRsus(timRsus);
                            
                System.out.println("active tim size: " + activeTims.size());
                
                // send tim to rsu 
                if(activeTims.size() > 0) {
                    for (ActiveTim activeTim : activeTims) {
                        timId = updateTimOnRsu(timToSend, activeTim.getTimId(), rsus.get(i).getRsuId());
                        updateActiveTims(activeTim, itisCodeIds, timId, null);    
                        activeTimIds.add(activeTim.getActiveTimId());
                    }                                  
                }     
                else {
                    timId = sendNewTimToRsu(timToSend, rsus.get(i)); 
                    Long activeTimId = addActiveTim(timId, wydotTimVsl, itisCodeIds, timType, timToSend.getTim().getDataframes()[0].getStartDateTime(), null, null);
                    activeTimIds.add(activeTimId);
                }               
            }

            // TODO: Send to SAT                                          
            
        }
        return activeTimIds;
    }	
}