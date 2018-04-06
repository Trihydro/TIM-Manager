package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;
import com.trihydro.odewrapper.model.WydotTimVslList;
import io.swagger.annotations.Api;
import com.trihydro.odewrapper.service.WydotTimVslService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.ArrayList;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@CrossOrigin
@RestController
@Api(description="Variable Speed Limits")
public class WydotTimVslController extends WydotTimBaseController {

    // services   
    private final WydotTimVslService wydotTimVslService;

    @Autowired
    WydotTimVslController(WydotTimVslService wydotTimVslService) {
        this.wydotTimVslService = wydotTimVslService;
    }
    
    @RequestMapping(value="/vsl-tim", method = RequestMethod.POST, headers="Accept=application/json")
    public ResponseEntity<String> createUpdateVslTim(@RequestBody WydotTimVslList wydotTimVsls) {        
        
        // build TIM
        wydotTimVslService.createUpdateVslTim(wydotTimVsls.getTimVslList());    
        
        // return success
        return ResponseEntity.status(HttpStatus.OK).body(jsonKeyValue("Success", "true"));        
    }
}
