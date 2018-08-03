package com.trihydro.odewrapper.controller;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.service.WydotTimService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import com.trihydro.odewrapper.model.Buffer;
import com.trihydro.odewrapper.model.ControllerResult;

@RestController
@ApiIgnore
public class WydotTimBaseController {
    
    // services   
    protected final WydotTimService wydotTimService;
    protected static Gson gson = new Gson();
  
    
    WydotTimBaseController() {
         this.wydotTimService = new WydotTimService();
    }

    protected ControllerResult validateInputParking(WydotTim tim){
        
        ControllerResult result = new ControllerResult();
        List<String> resultMessages = new ArrayList<String>();

        // get route number
        if(tim.getDirection() != null)
            result.setDirection(tim.getDirection());
        if(tim.getClientId() != null)
            result.setClientId(tim.getId());

        String route = null;
        if(tim.getRoute() != null){
            route = tim.getRoute().replaceAll("\\D+","");    
            result.setRoute(tim.getRoute());
        }
        else{            
            resultMessages.add("route not supported");   
        }
        // if route is not 80 fail
        if(!route.equals("80")){
            resultMessages.add("route not supported");            
        }
        // if direction is not eastbound/westbound/both fail
        if(!tim.getDirection().toLowerCase().equals("eastbound") && !tim.getDirection().toLowerCase().equals("westbound") && !tim.getDirection().toLowerCase().equals("both")){
            resultMessages.add("direction not supported");            
        }
        if(tim.getMileMarker() != null && tim.getMileMarker() < 0){
            resultMessages.add("Invalid milemarker");       
        }
        if(tim.getMileMarker() == null){
            resultMessages.add("Null value for mileMarker");           
        }   
        if(tim.getClientId() == null){
            resultMessages.add("Null value for clientId");           
        }    

        result.setResultMessages(resultMessages);
        return result;
    }

    protected ControllerResult validateInputRW(WydotTim tim){
        
        ControllerResult result = new ControllerResult();
        List<String> resultMessages = new ArrayList<String>();

        // get route number
        if(tim.getDirection() != null)
            result.setDirection(tim.getDirection());
        if(tim.getId() != null)
            result.setClientId(tim.getId());

        String route = null;
        if(tim.getHighway() != null){
            route = tim.getHighway().replaceAll("\\D+","");    
            result.setRoute(tim.getHighway());
        }
        else{            
            resultMessages.add("route not supported");   
        }
        
        // if route is not 80 fail
        if(!route.equals("80")){
            resultMessages.add("route not supported");            
        }
        // if direction is not eastbound/westbound/both fail
        if(!tim.getDirection().toLowerCase().equals("eastbound") && !tim.getDirection().toLowerCase().equals("westbound") && !tim.getDirection().toLowerCase().equals("both")){
            resultMessages.add("direction not supported");            
        }
        if(tim.getToRm() != null && tim.getToRm() < 0){
            resultMessages.add("Invalid toRm");       
        }
        if(tim.getFromRm() < 0){
            resultMessages.add("Invalid fromRm");           
        }              
        if(tim.getFromRm() == null){
            resultMessages.add("Null value for fromRm");
        }                           
        if(tim.getHighway() == null){
            resultMessages.add("Null value for highway");
        }
        if(tim.getId() == null){
            resultMessages.add("Null value for id");
        }           
        if(tim.getDirection() == null){
            resultMessages.add("Null value for direction");
        }            
        if(tim.getId() == null){
            resultMessages.add("Null value for id");
        } 
        if(tim.getStartTs() == null){
            resultMessages.add("Null value for startTs");
        }
        if(tim.getBuffers() != null){
            for (Buffer buffer : tim.getBuffers()) {
                if(buffer.getDistance() == null){
                    resultMessages.add("Null value for buffer distance");                
                }
                if(buffer.getDistance() != null && buffer.getDistance() < 0){
                    resultMessages.add("Invalid value for buffer distance");           
                }    
                if(buffer.getAction() == null){
                    resultMessages.add("Null value for buffer action");          
                }               
                if(buffer.getAction() != null && !isValidAction(buffer.getAction())){
                    resultMessages.add("Unsupport value for buffer action");                  
                }                    
            }            
        }      
        
        result.setResultMessages(resultMessages);
        return result;
    }

    public boolean isValidAction(String action){

        if(action.equals("leftClosed")){
            return true;
        }                       
        else if(action.equals("rightClosed")){
            return true;
        }
        else if(action.equals("workers")){
            return true;
        }
        else if(action.equals("surfaceGravel")){
            return true;
        }
        else if(action.equals("surfaceMilled")){
            return true;
        }
        else if(action.equals("surfaceDirt")){
            return true;
        }
        else if(action.contains("delay_")){
            String[] actionSplit = action.split("_");
            if(actionSplit.length < 2)
                return false;
            if(!actionSplit[0].equals("delay"))
                return false;
            if(!StringUtils.isNumeric(actionSplit[1]))
                return false;
            if(Float.parseFloat(actionSplit[1]) < 0)
                return false;
            return true;
        }
        else if(action.equals("prepareStop")){
            return true;
        }
        else if(action.contains("reduceSpeed_")){
            String[] actionSplit = action.split("_");
            if(actionSplit.length < 2)
                return false;
            if(!actionSplit[0].equals("reduceSpeed"))
                return false;
            if(!StringUtils.isNumeric(actionSplit[1]))
                return false;
            if(Float.parseFloat(actionSplit[1]) < 0)
                return false;
            return true;
        }
        return false;
    }
    
    public String jsonKeyValue(String key, String value) {
        return "{\"" + key + "\":" + value + "}";
    }
}
