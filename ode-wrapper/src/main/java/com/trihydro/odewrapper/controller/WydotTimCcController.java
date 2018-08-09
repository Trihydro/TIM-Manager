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

            // check to see if its an update

            // get Active RSU TIMs based on route/clientId/direction
            // ActiveTimService.getActiveRsuTims(milepostStart, milepostStop, timTypeId, direction)
            // List<ActiveTim> activeTims = ActiveTimService.getRsusWithActiveTim(wydotTim.getClientId(), );                

            if(resultTim.getResultMessages().size() > 0){
                resultList.add(resultTim);
                continue;
            }          
            
            createTims(wydotTim, resultTim.getItisCodes());   
            resultTim.getResultMessages().add("success");
            resultList.add(resultTim); 
        }
        
        String responseMessage = gson.toJson(resultList);         
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    // asynchronous TIM creation
    public void createTims(WydotTim wydotTim, List<String> itisCodes) 
    {
        // An Async task always executes in new thread
        new Thread(new Runnable() {
            public void run() {
                // set client id
                wydotTim.setClientId(wydotTim.getSegment());

                if(wydotTim.getDirection().equals("both")) {                
                    wydotTimService.createUpdateTim("CC", wydotTim, "eastbound", itisCodes);
                    wydotTimService.createUpdateTim("CC", wydotTim, "westbound", itisCodes);      
                }
                else
                    wydotTimService.createUpdateTim("CC", wydotTim, wydotTim.getDirection(), itisCodes);                     
            }
        }).start();
    }

    private void createSendTims(WydotTim wydotTim, List<String> itisCodes, String direction, TimType timType){
        // build region name for active tim logger to use            
        String regionNamePrevWB = direction + "_" + wydotTim.getRoute() + "_" + wydotTim.getFromRm() + "_" + wydotTim.getToRm();  
        // create TIM
        WydotTravelerInputData timToSendWB = wydotTimService.createTim(wydotTim, direction, type, itisCodes);
        // send TIM to RSUs
        wydotTimService.sendTimToRsus(wydotTim, timToSendWB, regionNamePrevWB, wydotTim.getDirection(), timType);
        // send TIM to SDW
        wydotTimService.sendTimToSDW(wydotTim, timToSendWB, regionNamePrevWB, wydotTim.getDirection(), timType);
    }
}
