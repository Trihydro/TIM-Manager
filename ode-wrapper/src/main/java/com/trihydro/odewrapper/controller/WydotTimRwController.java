package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;

import com.trihydro.odewrapper.model.WydotTimRwList;
import io.swagger.annotations.Api;
import java.util.ArrayList;
import java.util.Arrays;
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

    @RequestMapping(value="/create-rw-tim", method = RequestMethod.POST, headers="Accept=application/json")
    public ResponseEntity<String> createRwTim(@RequestBody WydotTimRwList wydotTimRws) { 
       
        System.out.println("Create RW TIM");

        // build TIM
        ArrayList<Long> activeTimIds = wydotTimRwService.createRwTim(wydotTimRws.getTimRwList());       
        
        Long[] activeTimIdsArr = new Long[activeTimIds.size()];
        activeTimIds.toArray(activeTimIdsArr);

        return ResponseEntity.status(HttpStatus.OK).body(jsonKeyValue("active_tims", Arrays.toString(activeTimIdsArr)));    
    }

    @RequestMapping(value="/update-rw-tim", method = RequestMethod.PUT, headers="Accept=application/json")
    public ResponseEntity<String> updateRwTim(@RequestBody WydotTimRwList wydotTimRws) { 
       
        System.out.println("Update RW TIM");

        // build TIM
        ArrayList<Long> activeTimIds = wydotTimRwService.updateRwTim(wydotTimRws.getTimRwList());       
        
        Long[] activeTimIdsArr = new Long[activeTimIds.size()];
        activeTimIds.toArray(activeTimIdsArr);

        return ResponseEntity.status(HttpStatus.OK).body(jsonKeyValue("active_tims", Arrays.toString(activeTimIdsArr)));   
    }
    
    @RequestMapping(value="/delete-rw-tim/{id}", method = RequestMethod.DELETE, headers="Accept=application/json")
    public ResponseEntity<String> deleteRwTim(@PathVariable String clientId) { 
       
        System.out.println("Delete RW TIM");

        // clear TIM
        //wydotTimRwService.allClear(clientId);        

        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }
}
