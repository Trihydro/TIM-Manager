package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;

import com.trihydro.odewrapper.model.WydotTimCcList;
import io.swagger.annotations.Api;
import com.trihydro.odewrapper.service.WydotTimCcService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@CrossOrigin
@RestController
@Api(description="Chain Controls")
public class WydotTimCcController {

    // services   
    private final WydotTimCcService wydotTimCcService;

    @Autowired
    WydotTimCcController(WydotTimCcService wydotTimCcService) {
        this.wydotTimCcService = wydotTimCcService;
    }

    @RequestMapping(value="/cc-tim", method = RequestMethod.POST, headers="Accept=application/json")
    public ResponseEntity<String> createCcTim(@RequestBody WydotTimCcList wydotTimRcs) { 
       
        System.out.println("Create RC TIM");

        // build TIM
        //wydotTimCcService.createRcTim(wydotTimRcs.getTimRcList());        

        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    // @RequestMapping(value="/update-cc-tim", method = RequestMethod.PUT, headers="Accept=application/json")
    // public ResponseEntity<String> updateCcTim(@RequestBody WydotTimCcList wydotTimRcs) { 
       
    //     System.out.println("Create RC TIM");

    //     // build TIM
    //     //wydotTimCcService.createRcTim(wydotTimRcs.getTimRcList());        

    //     String responseMessage = "success";
    //     return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    // }

    // @RequestMapping(value="/submit-cc-ac", method = RequestMethod.DELETE, headers="Accept=application/json")
    // public ResponseEntity<String> submitCcRcTim(@RequestBody WydotTimCcList wydotTimRcs) { 
       
    //     System.out.println("All clear");

    //     // clear TIM
    //     //wydotTimCcService.allClear(wydotTimRcs.getTimRcList());        

    //     String responseMessage = "success";
    //     return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    // }
}
