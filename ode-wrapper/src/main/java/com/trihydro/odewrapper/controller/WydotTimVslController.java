package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TimType;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.model.WydotTimList;
import com.trihydro.odewrapper.model.WydotTravelerInputData;

import org.springframework.http.HttpStatus;

@CrossOrigin
@RestController
@Api(description="Variable Speed Limits")
public class WydotTimVslController extends WydotTimBaseController {

    private static String type = "VSL";
    
    @RequestMapping(value="/vsl-tim", method = RequestMethod.POST, headers="Accept=application/json")
    public ResponseEntity<String> createUpdateVslTim(@RequestBody WydotTimList wydotTimList) {        
        
        System.out.println("Create/Update VSL TIM");

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;

        // build TIM        
        for (WydotTim wydotTim : wydotTimList.getTimVslList()) {
            resultTim = validateInputVsl(wydotTim);

            if(resultTim.getResultMessages().size() > 0){
                resultList.add(resultTim);
                continue;
            }
                
            createTims(wydotTim, resultTim.getItisCodes());
            
            resultTim.getResultMessages().add("success");
            resultList.add(resultTim);     
        }
        
        String responseMessage = gson.toJson(resultList);         
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);    
    }
    
    @RequestMapping(value="/vsl-tim", method = RequestMethod.GET, headers="Accept=application/json")
    public Collection<ActiveTim> getVslTims() { 
       
        // get active TIMs
        List<ActiveTim> activeTims = wydotTimService.selectTimsByType("VSL");   
        
        // add ITIS codes to TIMs
        for (ActiveTim activeTim : activeTims) {
            ActiveTimService.addItisCodesToActiveTim(activeTim);
        }
          
        return activeTims;
    }

    // asynchronous TIM creation
    public void createTims(WydotTim wydotTim, List<String> itisCodes) 
    {
        // An Async task always executes in new thread
        new Thread(new Runnable() {
            public void run() {

                // get tim type            
                TimType timType = getTimType(type);
                
                if(wydotTim.getDirection().equals("both")) {
                    // eastbound
                    createSendTims(wydotTim, itisCodes, "eastbound", timType);                                         

                    // westbound
                    createSendTims(wydotTim, itisCodes, "westbound", timType);
                }
                else{
                    createSendTims(wydotTim, itisCodes, wydotTim.getDirection(), timType);
                }                         
            }
        }).start();
    }

    private void createSendTims(WydotTim wydotTim, List<String> itisCodes, String direction, TimType timType){
        // build region name for active tim logger to use            
        String regionNamePrevWB = direction + "_" + wydotTim.getRoute() + "_" + wydotTim.getFromRm() + "_" + wydotTim.getToRm();  
        // create TIM
        WydotTravelerInputData timToSendWB = wydotTimService.createTim(wydotTim, direction, type, itisCodes);
        // send TIM to RSUs
        wydotTimService.sendTimToRsus(wydotTim, timToSendWB, regionNamePrevWB, wydotTim.getDirection(), timType);
        // send TIM to SDW
        wydotTimService.sendTimToSDW(wydotTim, timToSendWB, regionNamePrevWB, wydotTim.getDirection(), timType);
    }
}
