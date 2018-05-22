package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.odewrapper.model.Buffer;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.model.WydotTimList;
import io.swagger.annotations.Api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;

@CrossOrigin
@RestController
@Api(description="Road Construction")
public class WydotTimRwController extends WydotTimBaseController {

    @RequestMapping(value="/rw-tim", method = RequestMethod.POST, headers="Accept=application/json")
    public ResponseEntity<String> createRoadContructionTim(@RequestBody WydotTimList wydotTimList) throws CloneNotSupportedException {                 

        System.out.println("Create/Update RW TIM");

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;

        // build TIM        
        for (WydotTim wydotTim : wydotTimList.getTimRwList()) {
            
            wydotTim.getBuffers().sort(Comparator.comparingDouble(Buffer::getDistance));
            double bufferBefore = 0;

            if(wydotTim.getDirection().equals("both")) {                                

                for (int i = 0; i < wydotTim.getBuffers().size(); i++) {
                
                    // eastbound                    
                    // starts at lower milepost minus the buffer distance
                    double bufferStart = Math.min(wydotTim.getToRm(), wydotTim.getFromRm()) - wydotTim.getBuffers().get(i).getDistance();
                    // ends at lower milepost minus previous buffers
                    double bufferEnd = Math.min(wydotTim.getToRm(), wydotTim.getFromRm()) - bufferBefore;
                                    
                    // update start and stopping mileposts
                    WydotTim wydotTimBuffer = wydotTim.clone();
                    wydotTimBuffer.setFromRm(bufferStart);
                    wydotTimBuffer.setToRm(bufferEnd); 
                    wydotTimBuffer.setClientId(wydotTim.getId() + "-b" + i);                   

                    // send buffer tim
                    wydotTim.setAdvisory(new Integer[] {1025});
                    resultTim = wydotTimService.createUpdateTim("RW", wydotTimBuffer, "eastbound");
                    resultList.add(resultTim);  

                    // westbound
                    // starts at higher milepost plus buffer distance
                    bufferStart = Math.max(wydotTim.getToRm(), wydotTim.getFromRm()) + wydotTim.getBuffers().get(i).getDistance();
                    // ends at higher milepost plus previous buffers
                    bufferEnd = Math.max(wydotTim.getToRm(), wydotTim.getFromRm()) + bufferBefore;
                    
                    // update start and stopping mileposts
                    wydotTimBuffer = wydotTim.clone();
                    wydotTimBuffer.setFromRm(bufferStart);
                    wydotTimBuffer.setToRm(bufferEnd);

                    // send buffer tim
                    wydotTim.setAdvisory(new Integer[] {1025});
                    resultTim = wydotTimService.createUpdateTim("RW", wydotTimBuffer, "westbound");
                    resultList.add(resultTim);  

                    // update running buffer distance
                    bufferBefore = wydotTim.getBuffers().get(i).getDistance();
                }
                // send road construction TIM
                wydotTim.setAdvisory(new Integer[] {1025});
                resultTim = wydotTimService.createUpdateTim("RW", wydotTim, "eastbound");
                resultList.add(resultTim);  
                wydotTim.setAdvisory(new Integer[] {1025});
                resultTim = wydotTimService.createUpdateTim("RW", wydotTim, "westbound");
                resultList.add(resultTim);  
            }
            else{
                if(wydotTim.getDirection().equals("eastbound")){                                
                    for (int i = 0; i < wydotTim.getBuffers().size(); i++) {
                        // eastbound                    
                        // starts at lower milepost minus the buffer distance
                        double bufferStart = Math.min(wydotTim.getToRm(), wydotTim.getFromRm()) - wydotTim.getBuffers().get(i).getDistance();
                        // ends at lower milepost minus previous buffers
                        double bufferEnd = Math.min(wydotTim.getToRm(), wydotTim.getFromRm()) - bufferBefore;
                                        
                        // update start and stopping mileposts
                        WydotTim wydotTimBuffer = null;
						
                        wydotTimBuffer = wydotTim.clone();						
                        wydotTimBuffer.setFromRm(bufferStart);
                        wydotTimBuffer.setToRm(bufferEnd);
                        wydotTimBuffer.setClientId(wydotTim.getClientId() + "-b" + i);

                        // send buffer tim
                        wydotTim.setAdvisory(new Integer[] {1025});
                        resultTim = wydotTimService.createUpdateTim("RW", wydotTimBuffer, "eastbound");
                        resultList.add(resultTim);  
                        // update running buffer distance
                        bufferBefore = wydotTim.getBuffers().get(i).getDistance();
                    }
                    // send road construction TIM
                    wydotTim.setAdvisory(new Integer[] {1025});
                    resultTim = wydotTimService.createUpdateTim("RW", wydotTim, "eastbound");
                    resultList.add(resultTim);  
                }
                else{
                    for (int i = 0; i < wydotTim.getBuffers().size(); i++) {
                        // westbound
                        // starts at higher milepost plus buffer distance
                        double bufferStart = Math.max(wydotTim.getToRm(), wydotTim.getFromRm()) + wydotTim.getBuffers().get(i).getDistance();
                        // ends at higher milepost plus previous buffers
                        double bufferEnd = Math.max(wydotTim.getToRm(), wydotTim.getFromRm()) + bufferBefore;
                                        
                        // update start and stopping mileposts
                        WydotTim wydotTimBuffer = wydotTim.clone();
                        wydotTimBuffer.setFromRm(bufferStart);
                        wydotTimBuffer.setToRm(bufferEnd);
                        wydotTimBuffer.setClientId(wydotTim.getClientId() + "-b" + i);

                        // send buffer tim
                        wydotTim.setAdvisory(new Integer[] {1025});
                        resultTim = wydotTimService.createUpdateTim("RW", wydotTimBuffer, "westbound");
                        resultList.add(resultTim);  
                        // update running buffer distance
                        bufferBefore = wydotTim.getBuffers().get(i).getDistance();
                    }
                    // send road construction TIM
                    wydotTim.setAdvisory(new Integer[] {1025});
                    resultTim = wydotTimService.createUpdateTim("RW", wydotTim, "westbound");
                    resultList.add(resultTim);  
                }
            }
        }

        String responseMessage = gson.toJson(resultList);         
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);    
    }

    @RequestMapping(value="/rw-tim", method = RequestMethod.PUT, headers="Accept=application/json")
    public ResponseEntity<String> updateRoadContructionTim(@RequestBody WydotTimList wydotTimList) { 
        
        // build TIM        
        for (WydotTim wydotTim : wydotTimList.getTimRwList()) {
            if(wydotTim.getDirection().equals("both")) {
                wydotTimService.createUpdateTim("RW", wydotTim, "eastbound");
                wydotTimService.createUpdateTim("RW", wydotTim, "westbound");      
            }
            else
                wydotTimService.createUpdateTim("RW", wydotTim, wydotTim.getDirection());      
        }   

        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }
    
    @RequestMapping(value="/rw-tim/{id}", method = RequestMethod.DELETE, headers="Accept=application/json")
    public ResponseEntity<String> deleteRoadContructionTim(@PathVariable String id) { 

        // clear TIM
        wydotTimService.clearTimsById("RW", id);
        
        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value="/rw-tim/{id}", method = RequestMethod.GET, headers="Accept=application/json")
    public Collection<ActiveTim> getRoadContructionTimById(@PathVariable String id) { 
               
        // get tims              
        List<ActiveTim> activeTims = wydotTimService.selectTimById("RW", id);  

        return activeTims;
    }

    @RequestMapping(value="/rw-tim", method = RequestMethod.GET, headers="Accept=application/json")
    public Collection<ActiveTim> getRoadContructionTim() { 
               
        // get tims           
        List<ActiveTim> activeTims = wydotTimService.selectTimsByType("RW");  

        return activeTims;
    }
}
