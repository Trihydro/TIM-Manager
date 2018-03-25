package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;
import com.trihydro.odewrapper.model.WydotTimRcList;
import io.swagger.annotations.Api;
import com.trihydro.odewrapper.service.WydotTimParkingService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@CrossOrigin
@RestController
@Api(description="Parking")
public class WydotTimParkingController extends WydotTimBaseController {

    // services   
    private final WydotTimParkingService wydotTimParkingService;

    @Autowired
    WydotTimParkingController(WydotTimParkingService wydotTimParkingService) {
        this.wydotTimParkingService = wydotTimParkingService;
    }
    
    @RequestMapping(value="/parking-tim", method = RequestMethod.POST, headers="Accept=application/json")
    public ResponseEntity<String> createCcTim(@RequestBody WydotTimRcList wydotTimRcs) {        
        
        // build TIM
        wydotTimParkingService.createParkingTim(wydotTimRcs.getTimRcList());     

        return ResponseEntity.status(HttpStatus.OK).body(jsonKeyValue("Success", "true"));        
    }
}
