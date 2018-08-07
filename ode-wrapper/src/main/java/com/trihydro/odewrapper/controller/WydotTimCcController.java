package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;

import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.model.WydotTimList;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import java.util.List;
import com.trihydro.odewrapper.model.ControllerResult;
import java.util.ArrayList;

@CrossOrigin
@RestController
@Api(description="Chain Controls")
public class WydotTimCcController extends WydotTimBaseController {
    
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
            
            createTims(wydotTim);   
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
}
