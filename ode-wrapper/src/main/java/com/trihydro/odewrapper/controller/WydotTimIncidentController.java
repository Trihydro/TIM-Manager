package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import oracle.net.aso.l;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.model.WydotTimList;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;

@CrossOrigin
@RestController
@Api(description="Incidents")
public class WydotTimIncidentController extends WydotTimBaseController {

    @RequestMapping(value="/incident-tim", method = RequestMethod.POST, headers="Accept=application/json")
    public ResponseEntity<String> createIncidentTim(@RequestBody WydotTimList wydotTimList) { 
       
        String result;

        List<WydotTim> resultList = new ArrayList<WydotTim>();
        WydotTim resultTim = null;

        // build TIM        
        for (WydotTim wydotTim : wydotTimList.getTimIncidentList()) {
            if(wydotTim.getDirection().equals("both")) {
                result = wydotTimService.createUpdateTim("I", wydotTim, "eastbound");
                resultTim = new WydotTim();
                resultTim.setDirection("eastbound");
                resultTim.setResultMessage(result);
                resultList.add(resultTim);
                result = wydotTimService.createUpdateTim("I", wydotTim, "westbound");  
                resultTim = new WydotTim();
                resultTim.setDirection("westbound");
                resultTim.setResultMessage(result);
                resultList.add(resultTim);    
            }
            else {
                result = wydotTimService.createUpdateTim("I", wydotTim, wydotTim.getDirection());   
                resultTim = new WydotTim();
                resultTim.setDirection(wydotTim.getDirection());
                resultTim.setResultMessage(result);
                resultList.add(resultTim);  
            }
        }                

        String responseMessage = gson.toJson(resultList); 
        
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value="/incident-tim", method = RequestMethod.PUT, headers="Accept=application/json")
    public ResponseEntity<String> updateIncidentTim(@RequestBody WydotTimList wydotTimList) { 
       
        System.out.println("Update Incident TIM");

        // build TIM        
        for (WydotTim wydotTim : wydotTimList.getTimIncidentList()) {
            if(wydotTim.getDirection().equals("both")) {
                wydotTimService.createUpdateTim("I", wydotTim, "eastbound");
                wydotTimService.createUpdateTim("I", wydotTim, "westbound");      
            }
            else
                wydotTimService.createUpdateTim("I", wydotTim, wydotTim.getDirection());      
        }

        String responseMessage = "{\"message\": \"success\"}";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }
    
    @RequestMapping(value="/incident-tim/{incidentId}", method = RequestMethod.DELETE, headers="Accept=application/json")
    public ResponseEntity<String> deleteIncidentTim(@PathVariable String incidentId) { 
       
        // clear TIM
        wydotTimService.clearTimsById("I", incidentId);        

        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value="/incident-tim/{incidentId}", method = RequestMethod.GET, headers="Accept=application/json")
    public Collection<ActiveTim> getIncidentTim(@PathVariable String incidentId) { 
       
        // clear TIM
        List<ActiveTim> activeTims = wydotTimService.selectTimById("I", incidentId);        

        return activeTims;

        // String responseMessage = "success";
        // return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }
}
