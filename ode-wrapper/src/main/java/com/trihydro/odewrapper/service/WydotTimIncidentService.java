package com.trihydro.odewrapper.service;

import com.trihydro.odewrapper.model.WydotTimIncident;
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
public class WydotTimIncidentService extends WydotTimService
{    
    private CreateBaseTimUtil createBaseTimUtil;    

    @Autowired
    public Environment env;

	@Autowired
	WydotTimIncidentService(CreateBaseTimUtil createBaseTimUtil) {
        this.createBaseTimUtil = createBaseTimUtil;
    }	

    // creates and updates road condition TIMs based on starting and stopping mileposts
    public void createIncidentTim(List<WydotTimIncident> timIncidentList) {
            
        Long timId = null;
        
        // for each tim in wydot's request
        for (WydotTimIncident wydotTimIncident : timIncidentList) {
                    
            // FIND ALL RSUS TO SEND TO     
            List<WydotRsu> rsus = getRsusInBuffer(wydotTimIncident.getDirection(), Math.min(wydotTimIncident.getToRm(), wydotTimIncident.getFromRm()), Math.max(wydotTimIncident.getToRm(), wydotTimIncident.getFromRm()));       

            // build base TIM                
            WydotTravelerInputData timToSend = createBaseTimUtil.buildTim(wydotTimIncident);

            // set duration for two hours
            timToSend.getTim().getDataframes()[0].setDurationTime(120);            

            // add Road Conditions itis codes
            List<String> items = setItisCodes(wydotTimIncident);   
            timToSend.getTim().getDataframes()[0].setItems(items.toArray(new String[items.size()]));
 
            // get tim type            
            TimType timType = getTimType("I");

            // build region name for active tim logger to use
            String regionName = wydotTimIncident.getDirection() + "_" + wydotTimIncident.getHighway() + "_" + wydotTimIncident.getFromRm() + "_" + wydotTimIncident.getToRm();           
                        
            // query for existing active tims for this road segment
            List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
            // if(timType != null)
            //     activeTims = ActiveTimService.getActiveTims(wydotTimRc.getFromRm(), wydotTimRc.getToRm(), timType.getTimTypeId(), wydotTimRc.getDirection());   
            
            // if there are active tims for this area
            if(activeTims.size() > 0) {                    
                
                // query database for rsus that active tims could be on
                List<TimRsu> timRsus = TimRsuService.selectAll();

                // for each active tim
                for (ActiveTim activeTim : activeTims) {                                
                    
                    // look to see if tim is on RSU
                    TimRsu activeTimRsu = timRsus.stream()
                        .filter(x -> x.getTimId() == activeTim.getTimId())
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
                        regionName += "_RSU-" + rsu.getRsuTarget();   
                        regionName += "_I";                               
                        timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionName);  

                        // update TIM rsu
                        updateTimOnRsu(timToSend, activeTim.getTimId());   
                    }
                    // else active tim is satellite 
                    else{
                        // TODO - Send to Sat

                        // TODO - Update Active Sat TIM
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

                    regionName += "_RSU-" + rsus.get(i).getRsuTarget();   
                    regionName += "_I";                               
                    timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionName);  
                                     
                    // send tim to rsu
                    timId = sendNewTimToRsu(timToSend, rsus.get(i));  
                }            
            }

            // TODO: Send to SAT
        }
	}	

    public boolean allClear(List<WydotTimIncident> timIncidentList){        
        
        for (WydotTimIncident WydotTimIncident : timIncidentList) {

            if(!WydotTimIncident.getHighway().equals("I80"))
                return false;            

            // get tim type            
            TimType timType = getTimType("I");

            // get all RC active tims
            List<ActiveTim> activeTims = new ArrayList<ActiveTim>();            
            // if(timType != null)
            //     activeTims = ActiveTimService.getActiveTims(wydotTimRc.getFromRm(), wydotTimRc.getToRm(), timType.getTimTypeId(), wydotTimRc.getDirection());            

            // for each active RC TIM in this area
            for (ActiveTim activeTim : activeTims) {
                
                // get the TIM 
                J2735TravelerInformationMessage tim = TimService.getTim(activeTim.getActiveTimId());          

                // get RSU TIM is on
                List<TimRsu> timRsus = TimRsuService.getTimRsusByTimId(activeTim.getTimId());
                
                // get full RSU               
                WydotRsu rsu = getRsu(timRsus.get(0).getRsuId());

                // delete tim off rsu           
                deleteTimFromRsu(rsu, tim.getIndex()); 
                
                // delete active tim                
                ActiveTimService.deleteActiveTim(activeTim.getActiveTimId());                
            }  
            // TODO: SATELITTE TIMS
        } 
        return true;
    }


    public List<String> setItisCodes(WydotTimIncident wydotTimIncident){
        
        List<String> items = new ArrayList<String>();               
        // for (Integer item : wydotTimIncident.getAdvisory())
        //     items.add(item.toString());                       
                 
        return items;
    }
}