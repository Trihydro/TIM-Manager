package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;

import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.model.WydotTimList;
import io.swagger.annotations.Api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;

@CrossOrigin
@RestController
@Api(description="Road Construction")
public class WydotTimRwController extends WydotTimBaseController {

    @RequestMapping(value="/rw-tim", method = RequestMethod.POST, headers="Accept=application/json")
    public ResponseEntity<String> createRoadContructionTim(@RequestBody WydotTimList wydotTimList) {                 

        // build TIM        
        for (WydotTim wydotTim : wydotTimList.getTimRwList()) {
            if(wydotTim.getDirection().equals("both")) {
                wydotTimService.createUpdateTim("RW", wydotTim, "eastbound");
                wydotTimService.createUpdateTim("RW", wydotTim, "westbound");      
            }
            else
                wydotTimService.createUpdateTim("RW", wydotTim, wydotTim.getDirection());      
        }

        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value="/rw-tim", method = RequestMethod.PUT, headers="Accept=application/json")
    public ResponseEntity<String> updateRoadContructionTim(@RequestBody WydotTimList wydotTimList) { 
        
        // build TIM        
        for (WydotTim wydotTim : wydotTimList.getTimRwList()) {
            if(wydotTim.getDirection().equals("both")) {
                wydotTimService.createUpdateTim("RW", wydotTim, "eastbound");
                wydotTimService.createUpdateTim("RW", wydotTim, "westbound");      
            }
            else
                wydotTimService.createUpdateTim("RW", wydotTim, wydotTim.getDirection());      
        }   

        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }
    
    @RequestMapping(value="/rw-tim/{id}", method = RequestMethod.DELETE, headers="Accept=application/json")
    public ResponseEntity<String> deleteRoadContructionTim(@PathVariable String id) { 

        // clear TIM
        wydotTimService.clearTimsById("RW", id);
        
        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value="/rw-tim/{id}", method = RequestMethod.GET, headers="Accept=application/json")
    public ResponseEntity<String> getRoadContructionTim(@PathVariable String id) { 
               
        // get tim              
        wydotTimService.selectTimById("RW", id);  

        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

}
