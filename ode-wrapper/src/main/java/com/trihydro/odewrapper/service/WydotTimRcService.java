package com.trihydro.odewrapper.service;

import com.trihydro.odewrapper.model.WydotTimRc;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.TimService;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.ArrayList;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.TimType;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import org.springframework.core.env.Environment;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;
import com.trihydro.odewrapper.model.WydotTravelerInputData;
import com.trihydro.library.service.TimRsuService;
import com.trihydro.library.model.TimRsu;

@Component
public class WydotTimRcService extends WydotTimService
{    
    private CreateBaseTimUtil createBaseTimUtil;    

    @Autowired
    public Environment env;

	@Autowired
	WydotTimRcService(CreateBaseTimUtil createBaseTimUtil) {
        this.createBaseTimUtil = createBaseTimUtil;
    }	

    // creates and updates road condition TIMs based on starting and stopping mileposts
    public void createUpdateRcTim(List<WydotTimRc> timRcList) {
            
        Long timId = null;
        
        // for each tim in wydot's request
        for (WydotTimRc wydotTimRc : timRcList) {
                    
            // FIND ALL RSUS TO SEND TO     
            List<WydotRsu> rsus = getRsusInBuffer(wydotTimRc.getDirection(), Math.min(wydotTimRc.getToRm(), wydotTimRc.getFromRm()), Math.max(wydotTimRc.getToRm(), wydotTimRc.getFromRm()));       

            // build base TIM                
            WydotTravelerInputData timToSend = createBaseTimUtil.buildTim(wydotTimRc);

            // add Road Conditions itis codes
            List<String> items = setItisCodes(wydotTimRc);   
            timToSend.getTim().getDataframes()[0].setItems(items.toArray(new String[items.size()]));
 
            // get tim type            
            TimType timType = getTimType("RC");

            // build region name for active tim logger to use            
            String regionNamePrev = wydotTimRc.getDirection() + "_" + wydotTimRc.getRoute() + "_" + wydotTimRc.getFromRm() + "_" + wydotTimRc.getToRm();   
                                    
            // query for existing active tims for this road segment
            List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
            if(timType != null)
                activeTims = ActiveTimService.getAllActiveTims(wydotTimRc.getFromRm(), wydotTimRc.getToRm(), timType.getTimTypeId(), wydotTimRc.getDirection());   
            
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
                        timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNamePrev + "_RSU-" + rsu.getRsuTarget() + "_RC");                          

                        // update TIM rsu
                        updateTimOnRsu(timToSend, activeTim.getTimId());   
                    }
                    // else active tim is satellite 
                    else{
                        // update satellite tim                         
                        timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNamePrev + "_SAT-" + activeTim.getRecordId() + "_RC");  
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

                    timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNamePrev + "_RSU-" + rsus.get(i).getRsuTarget() + "_RC");    
                                     
                    // send tim to rsu                    
                    sendNewTimToRsu(timToSend, rsus.get(i));  
                }                           
                // send TIM to satellite            
                String recordId = getNewRecordId();         
                timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNamePrev + "_SAT-" + recordId + "_RC");
                sendNewTimToSdw(timToSend, recordId);
            }
        }
	}	

    public boolean allClear(List<WydotTimRc> timRcList){        
        
        for (WydotTimRc wydotTimRc : timRcList) {       

            WydotRsu rsu = null;

            // get tim type            
            TimType timType = getTimType("RC");

            // get all RC active tims
            List<ActiveTim> activeTims = new ArrayList<ActiveTim>();            
            if(timType != null)
                activeTims = ActiveTimService.getAllActiveTims(wydotTimRc.getFromRm(), wydotTimRc.getToRm(), timType.getTimTypeId(), wydotTimRc.getDirection());            

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
                    WydotTravelerInputData timToSend = createBaseTimUtil.buildTim(wydotTimRc);
                    String[] items = new String[1];
                    items[0] = "4846";
                    timToSend.getTim().getDataframes()[0].setItems(items);                    
                    deleteTimFromSdw(timToSend, activeTim.getRecordId(), activeTim.getTimId());                    
                }

                // delete active tim                
                ActiveTimService.deleteActiveTim(activeTim.getActiveTimId());                
            }             
        } 
        return true;
    }


    public List<String> setItisCodes(WydotTimRc wydotTimRc){
        
        List<String> items = new ArrayList<String>();               
        for (Integer item : wydotTimRc.getAdvisory())
            items.add(item.toString());                       
                 
        return items;
    }
}