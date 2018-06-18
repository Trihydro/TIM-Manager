package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import oracle.net.aso.l;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.odewrapper.model.ControllerResult;
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
       
        System.out.println("Create Incident TIM");

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;

        // build TIM        
        for (WydotTim wydotTim : wydotTimList.getTimIncidentList()) {
            
            // set client ID
            wydotTim.setClientId(wydotTim.getIncidentId());
            // set start time
            wydotTim.setStartDateTime(wydotTim.getTs());

            if(wydotTim.getDirection().equals("both")) {
                
                // first TIM - eastbound
                resultTim = wydotTimService.createUpdateTim("I", wydotTim, "eastbound");
                resultList.add(resultTim);  

                // second TIM - westbound
                resultTim = wydotTimService.createUpdateTim("I", wydotTim, "westbound");                  
                resultList.add(resultTim);    
            }
            else {
                // single direction TIM
                resultTim = wydotTimService.createUpdateTim("I", wydotTim, wydotTim.getDirection());   
                resultList.add(resultTim);  
            }
        }                

        String responseMessage = gson.toJson(resultList);         
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value="/incident-tim", method = RequestMethod.PUT, headers="Accept=application/json")
    public ResponseEntity<String> updateIncidentTim(@RequestBody WydotTimList wydotTimList) { 
       
        System.out.println("Update Incident TIM");

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;
       
        // build TIM        
        for (WydotTim wydotTim : wydotTimList.getTimIncidentList()) {
            
            // set client ID
            wydotTim.setClientId(wydotTim.getIncidentId());
            // set start time
            wydotTim.setStartDateTime(wydotTim.getTs());
 
            if(wydotTim.getDirection().equals("both")) {
                
                resultTim = wydotTimService.createUpdateTim("I", wydotTim, "eastbound");
                resultList.add(resultTim);

                resultTim = wydotTimService.createUpdateTim("I", wydotTim, "westbound");  
                resultList.add(resultTim);    
            }
            else {
                resultTim = wydotTimService.createUpdateTim("I", wydotTim, wydotTim.getDirection());  
                resultList.add(resultTim);   
            }
        }

        String responseMessage = gson.toJson(resultList);         
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }
    
    @RequestMapping(value="/incident-tim/{incidentId}", method = RequestMethod.DELETE, headers="Accept=application/json")
    public ResponseEntity<String> deleteIncidentTim(@PathVariable String incidentId) { 
       
        // clear TIM
        wydotTimService.clearTimsById("I", incidentId);        

        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value="/incident-tim", method = RequestMethod.GET, headers="Accept=application/json")
    public Collection<ActiveTim> getIncidentTims() { 
       
        // get active TIMs
        List<ActiveTim> activeTims = wydotTimService.selectTimsByType("I");   
        
        // add ITIS codes to TIMs
        for (ActiveTim activeTim : activeTims) {
            ActiveTimService.addItisCodesToActiveTim(activeTim);
        }

        return activeTims;
    }

    @RequestMapping(value="/incident-tim/{incidentId}", method = RequestMethod.GET, headers="Accept=application/json")
    public Collection<ActiveTim> getIncidentTimById(@PathVariable String incidentId) { 
       
        // get active TIMs
        List<ActiveTim> activeTims = wydotTimService.selectTimByClientId("I", incidentId);    

        // add ITIS codes to TIMs
        for (ActiveTim activeTim : activeTims) {
            ActiveTimService.addItisCodesToActiveTim(activeTim);
        }

        return activeTims;
    }
}
