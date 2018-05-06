package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.model.WydotTimList;

import org.springframework.http.HttpStatus;

@CrossOrigin
@RestController
@Api(description="Variable Speed Limits")
public class WydotTimVslController extends WydotTimBaseController {
    
    @RequestMapping(value="/vsl-tim", method = RequestMethod.POST, headers="Accept=application/json")
    public ResponseEntity<String> createUpdateVslTim(@RequestBody WydotTimList wydotTimList) {        
        
        System.out.println("Create/Update VSL TIM");

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;

        // build TIM        
        for (WydotTim wydotTim : wydotTimList.getTimVslList()) {
            if(wydotTim.getDirection().equals("both")) {
                resultTim = wydotTimService.createUpdateTim("VSL", wydotTim, "eastbound");
                resultList.add(resultTim);  

                resultTim = wydotTimService.createUpdateTim("VSL", wydotTim, "westbound");      
                resultList.add(resultTim);  
            }
            else
                resultTim = wydotTimService.createUpdateTim("VSL", wydotTim, wydotTim.getDirection());  
                resultList.add(resultTim);      
        }
        
        String responseMessage = gson.toJson(resultList);         
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);    
    }
}
