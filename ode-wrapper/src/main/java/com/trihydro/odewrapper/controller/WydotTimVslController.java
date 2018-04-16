package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.model.WydotTimList;

import org.springframework.http.HttpStatus;

@CrossOrigin
@RestController
@Api(description="Variable Speed Limits")
public class WydotTimVslController extends WydotTimBaseController {
    
    @RequestMapping(value="/vsl-tim", method = RequestMethod.POST, headers="Accept=application/json")
    public ResponseEntity<String> createUpdateVslTim(@RequestBody WydotTimList wydotTimList) {        
                
        // build TIM        
        for (WydotTim wydotTim : wydotTimList.getTimVslList()) {
            if(wydotTim.getDirection().equals("both")) {
                wydotTimService.createUpdateTim("VSL", wydotTim, "eastbound");
                wydotTimService.createUpdateTim("VSL", wydotTim, "westbound");      
            }
            else
                wydotTimService.createUpdateTim("VSL", wydotTim, wydotTim.getDirection());      
        }
        
        // return success
        return ResponseEntity.status(HttpStatus.OK).body(jsonKeyValue("Success", "true"));        
    }
}
