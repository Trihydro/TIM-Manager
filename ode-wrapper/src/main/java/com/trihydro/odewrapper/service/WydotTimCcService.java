package com.trihydro.odewrapper.service;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TimType;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.trihydro.odewrapper.model.WydotTravelerInputData;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.odewrapper.model.WydotTim;

@Component
public class WydotTimCcService extends WydotTimService
{   
    private CreateBaseTimUtil createBaseTimUtil;    

    @Autowired
	WydotTimCcService(CreateBaseTimUtil createBaseTimUtil ) {
        super();
        // this.createBaseTimUtil = createBaseTimUtil;
        // timTypeString = "CC";
    }	

    public void createCcTim(List<WydotTim> timCcList) {        
        
    //     // for each tim in wydot's request
    //     for (WydotTimCc wydotTimCc : timCcList) {
            
    //         System.out.println("CC TIM");
    //         System.out.println("direction:" + wydotTimCc.getDirection());
    //         System.out.println("route:" + wydotTimCc.getRoute());
    //         System.out.println("fromRm:" + wydotTimCc.getFromRm());
    //         System.out.println("toRm:" + wydotTimCc.getToRm());

    //         // FIND ALL RSUS TO SEND TO     
    //         List<WydotRsu> rsus = getRsusInBuffer(wydotTimCc.getDirection(), Math.min(wydotTimCc.getToRm(), wydotTimCc.getFromRm()), Math.max(wydotTimCc.getToRm(), wydotTimCc.getFromRm()));       

    //         // build base TIM                
    //         WydotTravelerInputData timToSend = createBaseTimUtil.buildTim(wydotTimCc);

    //         // add Road Conditions itis codes
    //         List<String> items = setItisCodes(wydotTimCc);   
    //         timToSend.getTim().getDataframes()[0].setItems(items.toArray(new String[items.size()]));
 
    //         // get tim type            
    //         TimType timType = getTimType(timTypeString);

    //         // build region name for active tim logger to use
    //         String regionNamePrev = wydotTimCc.getDirection() + "_" + wydotTimCc.getRoute() + "_" + wydotTimCc.getFromRm() + "_" + wydotTimCc.getToRm();       
                        
    //         // query database for rsus that active tims could be on
    //         List<ActiveTim> activeTims = null;
    //         // for each rsu in range
    //         for (WydotRsu rsu : rsus) {
    //             // add rsu to tim
    //             rsuArr[0] = rsu;
    //             timToSend.setRsus(rsuArr);      

    //              // update region name for active tim logger
    //              timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNamePrev + "_RSU-" + rsu.getRsuTarget() + "_" + timTypeString);
              
    //              activeTims = ActiveTimService.getActiveCCTimsOnRsu(rsu.getRsuTarget(), wydotTimCc.getId());
 
    //              // update tim                       
    //              if(activeTims != null && activeTims.size() > 0){                                            
    //                  // update TIM rsu
    //                  updateTimOnRsu(timToSend, activeTims.get(0).getTimId());                                
    //              }              
    //              else{     
    //                  // send new tim to rsu                    
    //                  sendNewTimToRsu(timToSend, rsu);  
    //              }
    //         }

    //         // // query for existing active tims for this road segment
    //         // List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
    //         // if(timType != null)
    //         //     activeTims = ActiveTimService.getActiveTimsByClientId(wydotTimCc.get());      
    //         //     getActiveCcTimsOnRsu         
            
    //         // // if there are active tims for this area
    //         // if(activeTims.size() > 0) {                    
                
    //         //     // query database for rsus that active tims could be on
    //         //     List<TimRsu> timRsus = TimRsuService.selectAll();

    //         //     // for each active tim
    //         //     for (ActiveTim activeTim : activeTims) {                                
                    
    //         //         // look to see if tim is on RSU
    //         //         TimRsu activeTimRsu = timRsus.stream()
    //         //             .filter(x -> x.getTimId() == activeTim.getTimId())
    //         //             .findFirst()
    //         //             .orElse(null);
                                                                
    //         //         // if so, update RSU
    //         //         if(activeTimRsu != null){

    //         //             // get rsu
    //         //             WydotRsu rsu = rsus.stream()
    //         //             .filter(x -> x.getRsuId().equals(Integer.valueOf(activeTimRsu.getRsuId().toString())))
    //         //             .findFirst()
    //         //             .orElse(null);

    //         //             // add rsu to tim
    //         //             rsuArr[0] = rsu;
    //         //             timToSend.setRsus(rsuArr);            
                        
    //         //             // update region name for active tim logger
    //         //             regionName += "_RSU-" + rsu.getRsuTarget();   
    //         //             regionName += "_" + timTypeString;                               
    //         //             timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionName);  

    //         //             // update TIM rsu
    //         //             updateTimOnRsu(timToSend, activeTim.getTimId());   
    //         //         }
    //         //         // else active tim is satellite 
    //         //         else{
    //         //             // TODO - Send to Sat

    //         //             // TODO - Update Active Sat TIM
    //         //         }    
    //         //     }                
    //         // }     
    //         // // else add new active tim
    //         // else {
    //         //     // for each rsu 
    //         //     for(int i = 0; i < rsus.size(); i++) {
    //         //         // add rsu to tim
    //         //         rsuArr[0] = rsus.get(i);
    //         //         timToSend.setRsus(rsuArr);

    //         //         regionName += "_RSU-" + rsus.get(i).getRsuTarget();   
    //         //         regionName += "_" + timTypeString;                               
    //         //         timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionName);  
                                     
    //         //         // send tim to rsu
    //         //         timId = sendNewTimToRsu(timToSend, rsus.get(i));  
    //         //     }            
    //         // }

    //         // // TODO: Send to SAT
    //     }
    // }	

    // public List<String> setItisCodes(WydotTimCc wydotTimCc){
        
    //     List<String> items = new ArrayList<String>();               
    //     for (Integer item : wydotTimCc.getAdvisory())
    //         items.add(item.toString());                       
                 
    //     return items;
    // }
    }
}