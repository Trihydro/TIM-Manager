package com.trihydro.odewrapper.service;

import com.trihydro.odewrapper.model.WydotTimRw;
import com.trihydro.library.service.rsu.RsuService;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.service.tim.ActiveTimLogger;
import com.trihydro.library.service.tim.TimService;
import com.trihydro.library.service.itiscode.ItisCodeService;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.ArrayList;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.model.TimType;
import org.springframework.web.client.RestTemplate;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import org.springframework.core.env.Environment;
import com.trihydro.odewrapper.helpers.DBUtility;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;
import com.trihydro.odewrapper.model.WydotTravelerInputData;
import com.trihydro.library.service.tim.TimRsuLogger;
import com.trihydro.library.model.TimRsu;

@Component
public class WydotTimRwService extends WydotTimService
{    
    private ItisCodeService itisCodeService;
    private CreateBaseTimUtil createBaseTimUtil;    

    @Autowired
    public Environment env;

    @Autowired
    public void setDBUtility(DBUtility dbUtilityRh) {
        dbUtility = dbUtilityRh;
    }

	@Autowired
	WydotTimRwService(CreateBaseTimUtil createBaseTimUtil) {      
        this.createBaseTimUtil = createBaseTimUtil;
    }	

    public ArrayList<Long> createRwTim(List<WydotTimRw> timRwList) {
            
        Long timId = null;
        ArrayList<Long> activeTimIds = new ArrayList<Long>();

        // discard non-I-80 requests 
        for (WydotTimRw wydotTimRw : timRwList) {

            System.out.println("From: " + wydotTimRw.getFromRm().toString() + " To: " + wydotTimRw.getToRm().toString());
            System.out.println("Route: " + wydotTimRw.getRoute().toString());
                    
            if(!wydotTimRw.getRoute().equals("I80")){
                return null;
            }

            // FIND ALL RSUS TO SEND TO     
            List<WydotRsu> rsus = RsuService.selectRsusInBuffer(wydotTimRw.getDirection(), Math.min(wydotTimRw.getToRm(), wydotTimRw.getFromRm()), Math.max(wydotTimRw.getToRm(), wydotTimRw.getFromRm()), dbUtility.getConnection());       
            List<ItisCode> itisCodes = itisCodeService.selectAll(dbUtility.getConnection());

            // build base TIM                
            WydotTravelerInputData timToSend = createBaseTimUtil.buildTim(wydotTimRw);

            // add Road Conditions Codes 
            // items array (ITIS Codes)
            List<String> items = new ArrayList<String>();
            List<Integer> itisCodeIds = new ArrayList<Integer>();

            for (Integer item : wydotTimRw.getAdvisory()) {
                
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
            TimType timType = getTimType("RW");
            
            // for each rsu
            for(int i = 0; i < rsus.size(); i++) {

                // query RSU for indices
                rsus.get(i).setRsuTimeout(Integer.parseInt(env.getProperty("rsuTimeout")));
                rsus.get(i).setRsuRetries(Integer.parseInt(env.getProperty("rsuRetries")));     
                WydotRsu[] timRsus = new WydotRsu[1];
                timRsus[0] = rsus.get(i);
                timToSend.setRsus(timRsus);
                                                                            
                timId = sendNewTimToRsu(timToSend, rsus.get(i));
                Long activeTimId = addActiveTim(timId, wydotTimRw, itisCodeIds, timType, wydotTimRw.getStartDateTime(), wydotTimRw.getEndDateTime(), wydotTimRw.getClientId());               
                activeTimIds.add(activeTimId);                                                                              
            }            
          
            // TODO: Send to SAT
        }
        return activeTimIds;
    }	  

    public ArrayList<Long> updateRwTim(List<WydotTimRw> timRwList) {
        
        Long timId = null;
        ArrayList<Long> activeTimIds = new ArrayList<Long>();        

        // discard non-I-80 requests 
        for (WydotTimRw wydotTimRw : timRwList) {

            System.out.println("From: " + wydotTimRw.getFromRm().toString() + " To: " + wydotTimRw.getToRm().toString());
            System.out.println("Route: " + wydotTimRw.getRoute().toString());
                    
            if(!wydotTimRw.getRoute().equals("I80")){
                return null;
            }

            // FIND ALL RSUS TO SEND TO     
            List<WydotRsu> rsus = RsuService.selectRsusInBuffer(wydotTimRw.getDirection(), Math.min(wydotTimRw.getToRm(), wydotTimRw.getFromRm()), Math.max(wydotTimRw.getToRm(), wydotTimRw.getFromRm()), dbUtility.getConnection());       
            List<ItisCode> itisCodes = itisCodeService.selectAll(dbUtility.getConnection());

            // build base TIM                
            WydotTravelerInputData timToSend = createBaseTimUtil.buildTim(wydotTimRw);

            // add Road Conditions Codes 
            // items array (ITIS Codes)
            List<String> items = new ArrayList<String>();
            List<Integer> itisCodeIds = new ArrayList<Integer>();

            for (Integer item : wydotTimRw.getAdvisory()) {
                
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
            TimType timType = getTimType("RW");
            List<ActiveTim> activeTims = new ArrayList<ActiveTim>();

            if(timType != null)
                activeTims = ActiveTimLogger.getActiveTimsByClientId(wydotTimRw.getClientId(), dbUtility.getConnection());   
            
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
                        updateActiveTims(activeTim, itisCodeIds, timId, wydotTimRw.getEndDateTime()); 
                        activeTimIds.add(activeTim.getActiveTimId());                           
                    }
                    // else active tim is satellite 
                    else{
                        // TODO - Send to Sat

                        // TODO - Update Active Sat TIM
                    }    
                }                
            }          

            // TODO: Send to SAT
        }
        return activeTimIds;
    }

    public boolean deleteRwTim(String clientId){        
        
        // get tim type            
        TimType timType = getTimType("RW");
        List<ActiveTim> activeTims = new ArrayList<ActiveTim>();

        if(timType != null)
            activeTims = ActiveTimLogger.getActiveTimsByClientId(clientId, dbUtility.getConnection());   
        
        for (ActiveTim activeTim : activeTims) {
            // get all tims
            J2735TravelerInformationMessage tim = TimService.getTim(activeTim.getActiveTimId(), dbUtility.getConnection());                    
            // get RSU TIM is on
            List<TimRsu> timRsus = TimRsuLogger.getTimRsusByTimId(activeTim.getTimId(), dbUtility.getConnection());
            // get full RSU
            for (TimRsu timRsu : timRsus) {
                WydotRsu rsu = RsuService.getRsu(timRsu.getRsuId(), dbUtility.getConnection());
                // delete tim off rsu
                rsu.setRsuRetries(3);
                rsu.setRsuTimeout(2000);
                deleteTimFromRsu(rsu, tim.getIndex()); 
            }
            // delete active tims                
            ActiveTimLogger.deleteActiveTim(activeTim.getActiveTimId(), dbUtility.getConnection());                
        }  
    
        return true;
    }

}