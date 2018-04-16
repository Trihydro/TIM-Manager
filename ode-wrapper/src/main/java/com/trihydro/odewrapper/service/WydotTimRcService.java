package com.trihydro.odewrapper.service;

import com.trihydro.odewrapper.model.WydotTim;
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
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;
import com.trihydro.odewrapper.model.WydotTravelerInputData;
import com.trihydro.library.service.TimRsuService;
import com.trihydro.library.model.TimRsu;

@Component
public class WydotTimRcService extends WydotTimService
{    
    // private CreateBaseTimUtil createBaseTimUtil;    

	// @Autowired
	// WydotTimRcService(CreateBaseTimUtil createBaseTimUtil) {
    //     this.createBaseTimUtil = createBaseTimUtil;
    //     timTypeString = "RC";
    // }	

    // // creates and updates road condition TIMs based on starting and stopping mileposts
    // public void createUpdateRcTim(List<WydotTim> timRcList) {
                    
    //     // for each tim in wydot's request
    //     for (WydotTim wydotTimRc : timRcList) {
            
    //         System.out.println("RC TIM");
    //         System.out.println("direction:" + wydotTimRc.getDirection());
    //         System.out.println("route:" + wydotTimRc.getRoute());
    //         System.out.println("fromRm:" + wydotTimRc.getFromRm());
    //         System.out.println("toRm:" + wydotTimRc.getToRm());

    //         // FIND ALL RSUS TO SEND TO     
    //         List<WydotRsu> rsus = getRsusInBuffer(wydotTimRc.getDirection(), Math.min(wydotTimRc.getToRm(), wydotTimRc.getFromRm()), Math.max(wydotTimRc.getToRm(), wydotTimRc.getFromRm()));       

    //         // build base TIM                
    //         WydotTravelerInputData timToSend = createBaseTimUtil.buildTim(wydotTimRc);

    //         // add Road Conditions itis codes
    //         List<String> items = setItisCodes(wydotTimRc);   
    //         timToSend.getTim().getDataframes()[0].setItems(items.toArray(new String[items.size()]));
 
    //         // get tim type            
    //         TimType timType = getTimType(timTypeString);

    //         // build region name for active tim logger to use            
    //         String regionNamePrev = wydotTimRc.getDirection() + "_" + wydotTimRc.getRoute() + "_" + wydotTimRc.getFromRm() + "_" + wydotTimRc.getToRm();   
                                    
    //         // query database for rsus that active tims could be on
    //         List<ActiveTim> activeTims = null;
    //         // for each rsu in range
    //         for (WydotRsu rsu : rsus) {

    //             // add rsu to tim
    //             rsuArr[0] = rsu;
    //             timToSend.setRsus(rsuArr);            
                
    //             // update region name for active tim logger
    //             timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNamePrev + "_RSU-" + rsu.getRsuTarget() + "_" + timTypeString);
              
    //             activeTims = ActiveTimService.getActiveRCTimsOnRsu(rsu.getRsuTarget(), wydotTimRc.getFromRm(), wydotTimRc.getToRm(), wydotTimRc.getDirection());

    //             // update tim                       
    //             if(activeTims != null && activeTims.size() > 0){                                            
    //                 // update TIM rsu
    //                 updateTimOnRsu(timToSend, activeTims.get(0).getTimId());                                
    //             }              
    //             else{     
    //                 // send new tim to rsu                    
    //                 sendNewTimToRsu(timToSend, rsu);  
    //             }
    //         }

    //         // satellite
    //         List<ActiveTim> activeSatTims = ActiveTimService.getActiveSatTims(wydotTimRc.getFromRm(), wydotTimRc.getToRm(), timType.getTimTypeId(), wydotTimRc.getDirection());
        
    //         if(activeSatTims != null && activeSatTims.size() > 0){
    //             timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNamePrev + "_SAT-" + activeSatTims.get(0).getRecordId() + "_" + timTypeString);  
    //             updateTimOnSdw(timToSend, activeSatTims.get(0).getTimId(), activeSatTims.get(0).getRecordId());
    //         }
    //         else{
    //             String recordId = getNewRecordId();    
    //             timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNamePrev + "_SAT-" + recordId + "_" + timTypeString);
    //             sendNewTimToSdw(timToSend, recordId);
    //         }
    //     }
	// }	

    // public boolean allClear(List<WydotTimRc> timRcList){        
        
    //     for (WydotTimRc wydotTimRc : timRcList) {       

    //         WydotRsu rsu = null;

    //         // get tim type            
    //         TimType timType = getTimType(timTypeString);

    //         // get all RC active tims
    //         List<ActiveTim> activeTims = new ArrayList<ActiveTim>();            
    //         if(timType != null)
    //             activeTims = ActiveTimService.getAllActiveTims(wydotTimRc.getFromRm(), wydotTimRc.getToRm(), timType.getTimTypeId(), wydotTimRc.getDirection());            

    //         // for each active RC TIM in this area
    //         for (ActiveTim activeTim : activeTims) {
                
    //             // get the TIM 
    //             J2735TravelerInformationMessage tim = TimService.getTim(activeTim.getTimId());          

    //             // get RSU TIM is on
    //             List<TimRsu> timRsus = TimRsuService.getTimRsusByTimId(activeTim.getTimId());
                
    //             // get full RSU               
    //             if(timRsus.size() == 1){
    //                 rsu = getRsu(timRsus.get(0).getRsuId());
    //                 // delete tim off rsu           
    //                 deleteTimFromRsu(rsu, tim.getIndex());                 
    //             }
    //             else{
    //                 // is satellite tim
    //                 WydotTravelerInputData timToSend = createBaseTimUtil.buildTim(wydotTimRc);
    //                 String[] items = new String[1];
    //                 items[0] = "4846";
    //                 timToSend.getTim().getDataframes()[0].setItems(items);                    
    //                 deleteTimFromSdw(timToSend, activeTim.getRecordId(), activeTim.getTimId());                    
    //             }

    //             // delete active tim                
    //             ActiveTimService.deleteActiveTim(activeTim.getActiveTimId());                
    //         }             
    //     } 
    //     return true;
    // }


    // public List<String> setItisCodes(WydotTimRc wydotTimRc){
        
    //     List<String> items = new ArrayList<String>();               
    //     for (Integer item : wydotTimRc.getAdvisory()){
    //         System.out.println("RC Item: " + item.toString());
    //         items.add(item.toString());                       
    //     }
            
                 
    //     return items;
    // }
}