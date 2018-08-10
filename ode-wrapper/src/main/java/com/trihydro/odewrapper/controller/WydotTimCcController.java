package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;

import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.model.WydotTimList;
import com.trihydro.odewrapper.model.WydotTravelerInputData;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import java.util.List;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TimType;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.odewrapper.model.ControllerResult;
import java.util.ArrayList;

@CrossOrigin
@RestController
@Api(description="Chain Controls")
public class WydotTimCcController extends WydotTimBaseController {
    
    private static String type = "CC";

    @RequestMapping(value="/cc-tim", method = RequestMethod.POST, headers="Accept=application/json")
    public ResponseEntity<String> createChainControlTim(@RequestBody WydotTimList wydotTimList) {        
        
        System.out.println("CHAIN CONTROL TIM");
        List<ControllerResult> resultList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;

        // build TIM        
        for (WydotTim wydotTim : wydotTimList.getTimRcList()) {
             
            resultTim = validateInputIncident(wydotTim);

            if(resultTim.getResultMessages().size() > 0){
                resultList.add(resultTim);
                continue;
            }          
            
            processRequest(wydotTim, resultTim.getItisCodes());   
            resultTim.getResultMessages().add("success");
            resultList.add(resultTim); 
        }
        
        String responseMessage = gson.toJson(resultList);         
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }   

    // asynchronous TIM creation
    public void processRequest(WydotTim wydotTim, List<String> itisCodes) 
    {
        // An Async task always executes in new thread
        new Thread(new Runnable() {
            public void run() {
                // if direction == both
                if(wydotTim.getDirection().equals("both")){
                    // eastbound
                    handleTims(wydotTim, itisCodes, "eastbound");              
                                        
                    // westbound
                    handleTims(wydotTim, itisCodes, "westbound");
                }
                    
                // else handle one direction
                handleTims(wydotTim, itisCodes, wydotTim.getDirection());         
            }
        }).start();
    }

    private void removeRsuTims(List<ActiveTim> activeTims){
        if(activeTims.size() > 0){                                
            for (ActiveTim activeTim : activeTims) {
                ActiveTimService.deleteActiveTim(activeTim.getActiveTimId());
            }              
        }    
    }

    private void handleTims(WydotTim wydotTim, List<String> itisCodes, String direction){
        // get tim type            
        TimType timType = getTimType(type);
        List<ActiveTim> activeTims = null;
        // else handle one direction
        // check if update
        activeTims = ActiveTimService.getActiveRsuTimsByClientIdDirection(wydotTim.getSegment(), timType.getTimTypeId(), direction);
        // if update delete old tims
        removeRsuTims(activeTims);                                                      
        // then create new tims
        createSendTims(wydotTim, itisCodes, direction, timType);
    }

    private void createSendTims(WydotTim wydotTim, List<String> itisCodes, String direction, TimType timType){
        // build region name for active tim logger to use            
        String regionNamePrev = direction + "_" + wydotTim.getRoute() + "_" + wydotTim.getFromRm() + "_" + wydotTim.getToRm();  
        // create TIM
        WydotTravelerInputData timToSendWB = wydotTimService.createTim(wydotTim, direction, type, itisCodes);
        // send TIM to RSUs
        wydotTimService.sendTimToRsus(wydotTim, timToSendWB, regionNamePrev, wydotTim.getDirection(), timType);
         // update or send new SDW TIM
         wydotTimService.sendTimToSDW(wydotTim, timToSendWB, regionNamePrev, wydotTim.getDirection(), timType);
    }
}
