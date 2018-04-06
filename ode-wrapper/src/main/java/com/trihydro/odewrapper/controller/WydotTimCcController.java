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
public class WydotTimCcController extends WydotTimBaseController {

    // services   
    private final WydotTimCcService wydotTimCcService;

    @Autowired
    WydotTimCcController(WydotTimCcService wydotTimCcService) {
        this.wydotTimCcService = wydotTimCcService;
    }
    
    @RequestMapping(value="/cc-tim", method = RequestMethod.POST, headers="Accept=application/json")
    public ResponseEntity<String> createChainControlTim(@RequestBody WydotTimCcList wydotTimCcs) {        
        
        System.out.println("Chain control called");

        // build TIM
        wydotTimCcService.createCcTim(wydotTimCcs.getTimCcList());     

        return ResponseEntity.status(HttpStatus.OK).body(jsonKeyValue("Success", "true"));        
    }
}
