package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;

import com.trihydro.odewrapper.model.WydotTimIncidentList;
import io.swagger.annotations.Api;
import com.trihydro.odewrapper.service.WydotTimIncidentService;

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
@Api(description="Incidents")
public class WydotTimIncidentController extends WydotTimBaseController {

    // services   
	private final WydotTimIncidentService wydotTimIncidentService;

    @Autowired
    WydotTimIncidentController(WydotTimIncidentService wydotTimIncidentService) {
        this.wydotTimIncidentService = wydotTimIncidentService;
    }

    @RequestMapping(value="/incident-tim", method = RequestMethod.POST, headers="Accept=application/json")
    public ResponseEntity<String> createIncidentTim(@RequestBody WydotTimIncidentList wydotTimIncidents) { 
       
        System.out.println("Create Incident TIM");

        // build TIM
        wydotTimIncidentService.createIncidentTim(wydotTimIncidents.getTimIncidentList());       

        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value="/incident-tim", method = RequestMethod.PUT, headers="Accept=application/json")
    public ResponseEntity<String> updateIncidentTim(@RequestBody WydotTimIncidentList wydotTimIncidents) { 
       
        System.out.println("Update Incident TIM");

        // build TIM
        wydotTimIncidentService.createIncidentTim(wydotTimIncidents.getTimIncidentList());       

        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }
    
    @RequestMapping(value="/incident-tim/{id}", method = RequestMethod.DELETE, headers="Accept=application/json")
    public ResponseEntity<String> deleteIncidentTim(@PathVariable String clientId) { 
       
        System.out.println("Delete RW TIM");

        // clear TIM
        //wydotTimRwService.allClear(clientId);        

        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }
}
