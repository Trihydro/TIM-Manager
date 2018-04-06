package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;

import com.trihydro.library.service.ActiveTimService;
import com.trihydro.odewrapper.model.WydotTimRwList;
import io.swagger.annotations.Api;
import com.trihydro.odewrapper.service.WydotTimRwService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@CrossOrigin
@RestController
@Api(description="Road Construction")
public class WydotTimRwController extends WydotTimBaseController {

    // services   
	private final WydotTimRwService wydotTimRwService;

    @Autowired
    WydotTimRwController(WydotTimRwService wydotTimRwService) {
        this.wydotTimRwService = wydotTimRwService;
    }

    @RequestMapping(value="/rw-tim", method = RequestMethod.POST, headers="Accept=application/json")
    public ResponseEntity<String> createRoadContructionTim(@RequestBody WydotTimRwList wydotTimRws) { 
       
        System.out.println("Create RW TIM");

        // build TIM
        wydotTimRwService.createRwTim(wydotTimRws.getTimRwList());       

        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value="/rw-tim", method = RequestMethod.PUT, headers="Accept=application/json")
    public ResponseEntity<String> updateRoadContructionTim(@RequestBody WydotTimRwList wydotTimRws) { 
       
        System.out.println("Update RW TIM");

        // build TIM
        wydotTimRwService.updateRwTim(wydotTimRws.getTimRwList());       

        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }
    
    @RequestMapping(value="/rw-tim/{id}", method = RequestMethod.DELETE, headers="Accept=application/json")
    public ResponseEntity<String> deleteRoadContructionTim(@PathVariable String id) { 
       
        System.out.println("Delete RW TIM");

        // clear TIM
        //wydotTimRwService.allClear(clientId);        

        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value="/rw-tim/{id}", method = RequestMethod.GET, headers="Accept=application/json")
    public ResponseEntity<String> getRoadContructionTim(@PathVariable String id) { 
       
        System.out.println("GET RW TIM");

        // get tim              
        wydotTimRwService.getRwTim(id);  

        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

}
