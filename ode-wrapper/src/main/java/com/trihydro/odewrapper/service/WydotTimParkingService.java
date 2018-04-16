package com.trihydro.odewrapper.service;

import com.trihydro.library.model.ActiveTim;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;

@Component
public class WydotTimParkingService extends WydotTimService
{    
    private CreateBaseTimUtil createBaseTimUtil;    

	@Autowired
	WydotTimParkingService(CreateBaseTimUtil createBaseTimUtil) {
        this.createBaseTimUtil = createBaseTimUtil;
    }	

    // // creates and updates road condition TIMs based on starting and stopping mileposts
    // public void createParkingTim(List<WydotTimRc> timParkingList) {

    //     Long timId = null;
        
    //     // for each tim in wydot's request
    //     for (WydotTimRc wydotTimPark : timParkingList) {
                 
    //         System.out.println("Parking TIM");
    //         System.out.println("direction:" + wydotTimPark.getDirection());
    //         System.out.println("route:" + wydotTimPark.getRoute());
    //         System.out.println("fromRm:" + wydotTimPark.getFromRm());
    //         System.out.println("toRm:" + wydotTimPark.getToRm());
            
    //         // FIND ALL RSUS TO SEND TO     
    //         List<WydotRsu> rsus = getRsusInBuffer(wydotTimPark.getDirection(), Math.min(wydotTimPark.getToRm(), wydotTimPark.getFromRm()), Math.max(wydotTimPark.getToRm(), wydotTimPark.getFromRm()));       

    //         // build base TIM                
    //         WydotTravelerInputData timToSend = createBaseTimUtil.buildTim(wydotTimPark);

    //         // set duration for two hours
    //         timToSend.getTim().getDataframes()[0].setDurationTime(120);            

    //         // add Road Conditions itis codes
    //         List<String> items = setItisCodes(wydotTimPark);   
    //         timToSend.getTim().getDataframes()[0].setItems(items.toArray(new String[items.size()]));
 
    //         // get tim type            
    //         TimType timType = getTimType(timTypeString);

    //         // build region name for active tim logger to use
    //         String regionName = wydotTimPark.getDirection() + "_" + wydotTimPark.getRoute() + "_" + wydotTimPark.getFromRm() + "_" + wydotTimPark.getToRm();           
                        
    //         // query for existing active tims for this road segment
    //         List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
    //         // if(timType != null)
    //         //     activeTims = ActiveTimService.getActiveTims(wydotTimRc.getFromRm(), wydotTimRc.getToRm(), timType.getTimTypeId(), wydotTimRc.getDirection());   
            
    //         // if there are active tims for this area
    //         if(activeTims.size() > 0) {                    
                
    //             // query database for rsus that active tims could be on
    //             List<TimRsu> timRsus = TimRsuService.selectAll();

    //             // for each active tim
    //             for (ActiveTim activeTim : activeTims) {                                
                    
    //                 // look to see if tim is on RSU
    //                 TimRsu activeTimRsu = timRsus.stream()
    //                     .filter(x -> x.getTimId() == activeTim.getTimId())
    //                     .findFirst()
    //                     .orElse(null);
                                                                
    //                 // if so, update RSU
    //                 if(activeTimRsu != null){

    //                     // get rsu
    //                     WydotRsu rsu = rsus.stream()
    //                     .filter(x -> x.getRsuId().equals(Integer.valueOf(activeTimRsu.getRsuId().toString())))
    //                     .findFirst()
    //                     .orElse(null);

    //                     // add rsu to tim
    //                     rsuArr[0] = rsu;
    //                     timToSend.setRsus(rsuArr);            
                        
    //                     // update region name for active tim logger
    //                     regionName += "_RSU-" + rsu.getRsuTarget();   
    //                     regionName += "_" + timTypeString;                               
    //                     timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionName);  

    //                     // update TIM rsu
    //                     updateTimOnRsu(timToSend, activeTim.getTimId());   
    //                 }
    //                 // else active tim is satellite 
    //                 else{
    //                     // TODO - Send to Sat

    //                     // TODO - Update Active Sat TIM
    //                 }    
    //             }                
    //         }     
    //         // else add new active tim
    //         else {
    //             // for each rsu 
    //             for(int i = 0; i < rsus.size(); i++) {
    //                 // add rsu to tim
    //                 rsuArr[0] = rsus.get(i);
    //                 timToSend.setRsus(rsuArr);

    //                 regionName += "_RSU-" + rsus.get(i).getRsuTarget();   
    //                 regionName += "_" + timTypeString;                               
    //                 timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionName);  
                                     
    //                 // send tim to rsu
    //                 timId = sendNewTimToRsu(timToSend, rsus.get(i));  
    //             }            
    //         }

    //         // TODO: Send to SAT
    //     }
	// }	

    // public List<String> setItisCodes(WydotTimRc wydotTimRc){
        
    //     List<String> items = new ArrayList<String>();               
    //     for (Integer item : wydotTimRc.getAdvisory())
    //         items.add(item.toString());                       
                 
    //     return items;
    // }
}