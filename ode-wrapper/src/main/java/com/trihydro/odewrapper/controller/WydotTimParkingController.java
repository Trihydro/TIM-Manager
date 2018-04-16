package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;

import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.model.WydotTimList;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;

@CrossOrigin
@RestController
@Api(description="Parking")
public class WydotTimParkingController extends WydotTimBaseController {
    
    @RequestMapping(value="/parking-tim", method = RequestMethod.POST, headers="Accept=application/json")
    public ResponseEntity<String> createParkingTim(@RequestBody WydotTimList wydotTimList) {        
                
        // build TIM        
          for (WydotTim wydotTim : wydotTimList.getTimParkingList()) {
            if(wydotTim.getDirection().equals("both")) {
                wydotTimService.createUpdateTim("P", wydotTim, "eastbound");
                wydotTimService.createUpdateTim("P", wydotTim, "westbound");      
            }
            else
                wydotTimService.createUpdateTim("P", wydotTim, wydotTim.getDirection());      
        }
        
        return ResponseEntity.status(HttpStatus.OK).body(jsonKeyValue("Success", "true"));        
    }
}
