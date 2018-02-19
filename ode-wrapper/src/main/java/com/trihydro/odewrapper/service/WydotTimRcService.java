package com.trihydro.odewrapper.service;

import com.trihydro.odewrapper.model.WydotTimRc;
import com.trihydro.service.rsu.RsuService;
import com.trihydro.service.model.ActiveTim;
import com.trihydro.service.tim.ActiveTimLogger;
import com.trihydro.service.tim.TimLogger;
import com.trihydro.service.itiscode.ItisCodeService;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.ArrayList;
import com.trihydro.service.model.WydotRsu;
import com.trihydro.service.model.ItisCode;
import com.trihydro.service.model.TimType;
import org.springframework.web.client.RestTemplate;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import org.springframework.core.env.Environment;
import com.trihydro.odewrapper.helpers.DBUtility;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;
import com.trihydro.odewrapper.model.WydotTravelerInputData;
import com.trihydro.service.tim.TimRsuLogger;
import com.trihydro.service.model.TimRsu;

@Component
public class WydotTimRcService extends WydotTimService
{    
    private CreateBaseTimUtil createBaseTimUtil;    

    @Autowired
    public Environment env;

    @Autowired
    public void setDBUtility(DBUtility dbUtilityRh) {
        dbUtility = dbUtilityRh;
    }

	@Autowired
	WydotTimRcService(CreateBaseTimUtil createBaseTimUtil) {
        this.createBaseTimUtil = createBaseTimUtil;
    }	

    public ArrayList<Long> createRcTim(List<WydotTimRc> timRcList) {
            
        Long timId = null;
        ArrayList<Long> activeTimIds = new ArrayList<Long>();
        
        // discard non-I-80 requests 
        for (WydotTimRc wydotTimRc : timRcList) {

            System.out.println("From: " + wydotTimRc.getFromRm().toString() + " To: " + wydotTimRc.getToRm().toString());
            System.out.println("Route: " + wydotTimRc.getRoute().toString());
                    
            if(!wydotTimRc.getRoute().equals("I80")){
                return null;
            }

            // FIND ALL RSUS TO SEND TO     
            List<WydotRsu> rsus = RsuService.selectRsusInBuffer(wydotTimRc.getDirection(), Math.min(wydotTimRc.getToRm(), wydotTimRc.getFromRm()), Math.max(wydotTimRc.getToRm(), wydotTimRc.getFromRm()), dbUtility.getConnection());       
            List<ItisCode> itisCodes = ItisCodeService.selectAll(dbUtility.getConnection());

            // build base TIM                
            WydotTravelerInputData timToSend = createBaseTimUtil.buildTim(wydotTimRc);

            // add Road Conditions Codes 
            // items array (ITIS Codes)
            List<String> items = new ArrayList<String>();
            List<Integer> itisCodeIds = new ArrayList<Integer>();

            for (Integer item : wydotTimRc.getAdvisory()) {
                
                items.add(item.toString());
                System.out.println("Code: " + item.toString());
                ItisCode itisCode = itisCodes.stream()
                    .filter(x -> x.getItisCode().equals(item))
                    .findFirst()
                    .orElse(null);
                if(itisCode != null)
                    itisCodeIds.add(itisCode.getItisCodeId());                 
            }               

            timToSend.getTim().getDataframes()[0].setItems(items.toArray(new String[items.size()]));
 
            // get tim type            
            TimType timType = getTimType("RC");
            List<ActiveTim> activeTims = new ArrayList<ActiveTim>();

            if(timType != null)
                ActiveTimLogger.getActiveTims(wydotTimRc.getFromRm(), wydotTimRc.getToRm(), timType.getTimTypeId(), wydotTimRc.getDirection(), dbUtility.getConnection());   
            
            // if there are active tims for this area
            // send tim to rsu 
            if(activeTims.size() > 0) {                    
                for (ActiveTim activeTim : activeTims) {                                
                    // if active tim is RSU tim
                    List<TimRsu> timRsus =  TimRsuLogger.getTimRsusByTimId(activeTim.getTimId(), dbUtility.getConnection());     
                    
                    // if RSU tim
                    if(timRsus.size() == 1){

                        WydotRsu rsu = rsus.stream()
                        .filter(x -> x.getRsuId().equals(Integer.valueOf(timRsus.get(0).getRsuId().toString())))
                        .findFirst()
                        .orElse(null);

                        // set tim RSU
                        rsu.setRsuTimeout(5000);
                        rsu.setRsuRetries(3);     
                        WydotRsu[] rsuArr = new WydotRsu[1];
                        rsuArr[0] = rsu;
                        timToSend.setRsus(rsuArr);

                        // update TIM rsu
                        timId = updateTimOnRsu(timToSend, activeTim.getTimId(), rsu.getRsuId());
                        // update Active TIM
                        updateActiveTims(activeTim, itisCodeIds, timId, null);         
                        activeTimIds.add(activeTim.getActiveTimId());                   
                    }
                    // else active tim is satellite 
                    else{
                        // TODO - Send to Sat

                        // TODO - Update Active Sat TIM
                    }    
                }                
            }     
            // else new active tims
            else {
                // for each rsu
                for(int i = 0; i < rsus.size(); i++) {

                    // query RSU for indices
                    rsus.get(i).setRsuTimeout(5000);
                    rsus.get(i).setRsuRetries(3);     
                    WydotRsu[] timRsus = new WydotRsu[1];
                    timRsus[0] = rsus.get(i);
                    timToSend.setRsus(timRsus);
                                                                              
                    timId = sendNewTimToRsu(timToSend, rsus.get(i));
                    Long activeTimId = addActiveTim(timId, wydotTimRc, itisCodeIds, timType, timToSend.getTim().getDataframes()[0].getStartDateTime(), null, null);       
                    activeTimIds.add(activeTimId);                    
                }            
            }

            // TODO: Send to SAT
        }
        return activeTimIds;
	}	

    public boolean allClear(List<WydotTimRc> timRcList){        
        
        // discard non-I-80 requests 
        for (WydotTimRc wydotTimRc : timRcList) {

            System.out.println("From: " + wydotTimRc.getFromRm().toString() + " To: " + wydotTimRc.getToRm().toString());            
                    
            if(!wydotTimRc.getRoute().equals("I80")){
                return false;
            }

            // get tim type            
            TimType timType = getTimType("RC");

            // FIND ALL RSUS TO SEND TO     
            List<WydotRsu> rsus = RsuService.selectRsusInBuffer(wydotTimRc.getDirection(), Math.min(wydotTimRc.getToRm(), wydotTimRc.getFromRm()), Math.max(wydotTimRc.getToRm(), wydotTimRc.getFromRm()), dbUtility.getConnection());           

            // get all RC active tims
            List<ActiveTim> activeTims = new ArrayList<ActiveTim>();

            if(timType != null)
                activeTims = ActiveTimLogger.getActiveTims(wydotTimRc.getFromRm(), wydotTimRc.getToRm(), timType.getTimTypeId(), wydotTimRc.getDirection(), dbUtility.getConnection());            

            for (ActiveTim activeTim : activeTims) {
                // get all tims
                J2735TravelerInformationMessage tim = TimLogger.getTim(activeTim.getActiveTimId(), dbUtility.getConnection());                    
                // get RSU TIM is on
                List<TimRsu> timRsus = TimRsuLogger.getTimRsusByTimId(activeTim.getTimId(), dbUtility.getConnection());
                // get full RSU
                for (TimRsu timRsu : timRsus) {
                    WydotRsu rsu = RsuService.getRsu(timRsu.getRsuId(), dbUtility.getConnection());
                    // delete tim off rsu
                    rsu.setRsuTimeout(Integer.parseInt(env.getProperty("rsuTimeout")));
                    rsu.setRsuRetries(Integer.parseInt(env.getProperty("rsuRetries")));     
                    deleteTimFromRsu(rsu, tim.getIndex()); 
                }
                // delete active tims                
                ActiveTimLogger.deleteActiveTim(activeTim.getActiveTimId(), dbUtility.getConnection());                
            }  
        } 
        return true;
    }

}