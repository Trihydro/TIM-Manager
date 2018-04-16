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

@CrossOrigin
@RestController
@Api(description="Road Conditions")
public class WydotTimRcController extends WydotTimBaseController {
   
    @RequestMapping(value="/create-update-rc-tim", method = RequestMethod.POST, headers="Accept=application/json")    
    public ResponseEntity<String> createUpdateRoadConditionsTim(@RequestBody WydotTimList wydotTimList) { 
       
        // build TIM        
        for (WydotTim wydotTim : wydotTimList.getTimRcList()) {
            if(wydotTim.getDirection().equals("both")) {
                wydotTimService.createUpdateTim("RC", wydotTim, "eastbound");
                wydotTimService.createUpdateTim("RC", wydotTim, "westbound");      
            }
            else
                wydotTimService.createUpdateTim("RC", wydotTim, wydotTim.getDirection());      
        }

        return ResponseEntity.status(HttpStatus.OK).body(jsonKeyValue("Success", "true"));        
    }

    @RequestMapping(value="/submit-rc-ac", method = RequestMethod.PUT, headers="Accept=application/json")
    public ResponseEntity<String> submitAllClearRoadConditionsTim(@RequestBody WydotTimList wydotTimList) {        

        // clear TIMs
        for (WydotTim wydotTim : wydotTimList.getTimRcList()) {  
            if(wydotTim.getDirection().equals("both")) {
                wydotTimService.clearTimsByRoadSegment("RC", wydotTim, "eastbound");
                wydotTimService.clearTimsByRoadSegment("RC", wydotTim, "westbound");   
            }  
            else
                wydotTimService.clearTimsByRoadSegment("RC", wydotTim, wydotTim.getDirection());   
        }

        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }
}
