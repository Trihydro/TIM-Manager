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
@Api(description="Chain Controls")
public class WydotTimCcController extends WydotTimBaseController {
    
    @RequestMapping(value="/cc-tim", method = RequestMethod.POST, headers="Accept=application/json")
    public ResponseEntity<String> createChainControlTim(@RequestBody WydotTimList wydotTimList) {        
        
        System.out.println("CHAIN CONTROL TIM");

        // build TIM        
        for (WydotTim wydotTim : wydotTimList.getTimRcList()) {

            // set client id
            wydotTim.setClientId(wydotTim.getSegment());

            if(wydotTim.getDirection().equals("both")) {                
                wydotTimService.createUpdateTim("CC", wydotTim, "eastbound");
                wydotTimService.createUpdateTim("CC", wydotTim, "westbound");      
            }
            else
            wydotTimService.createUpdateTim("CC", wydotTim, wydotTim.getDirection());      
        }

        return ResponseEntity.status(HttpStatus.OK).body(jsonKeyValue("Success", "true"));        
    }
}
