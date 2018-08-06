package com.trihydro.odewrapper.helpers;

import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.library.model.IncidentChoice;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.service.IncidentChoicesService;
import com.trihydro.library.service.ItisCodeService;
import com.trihydro.library.service.MilepostService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;
import java.util.List;
import java.util.ArrayList;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage.DataFrame.MsgId;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage.DataFrame.RoadSignID;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.MutcdCode.MutcdCodeEnum;

import com.trihydro.odewrapper.model.WydotTravelerInputData;
import java.math.BigDecimal;

@Component
public class SetItisCodes
{    
    private static List<IncidentChoice> incidentProblems;
    private static List<IncidentChoice> incidentEffects;
    private static List<IncidentChoice> incidentActions;

    private static List<ItisCode> itisCodes;

    public static List<ItisCode> getItisCodes() {
        if(itisCodes != null)
            return itisCodes;
        else{
            itisCodes = ItisCodeService.selectAll(); 
            return itisCodes;
        }
    }

    public static List<String> setItisCodesFromAdvisoryArray(WydotTim wydotTim) {    
        
        // check to see if code exists
        
        List<String> items = new ArrayList<String>();               
        for (Integer item : wydotTim.getAdvisory()){

            ItisCode code = getItisCodes().stream()
            .filter(x -> x.getItisCode().equals(item))
            .findFirst()
            .orElse(null);

            if(code != null)
                items.add(item.toString());                       
        }                            
        return items;
    }

    public static List<String> setItisCodesRc(WydotTim wydotTim) {
        
        List<String> items = new ArrayList<String>();   

        ItisCode code = null;

        for (Integer item : wydotTim.getAdvisory()){

            // map "closed" itis code
            if(item == 769){
                code = getItisCodes().stream()
                .filter(x -> x.getItisCode().equals(770))
                .findFirst()
                .orElse(null);
            }
            else{
                code = getItisCodes().stream()
                .filter(x -> x.getItisCode().equals(item))
                .findFirst()
                .orElse(null);                        
            }

            if(code != null)
                items.add(code.getItisCode().toString());    
        }                   

        return items;
    }

    public static List<String> setItisCodesVsl(WydotTim wydotTim) {
        
        List<String> items = new ArrayList<String>();        
        
        ItisCode speed = getItisCodes().stream()
            .filter(x -> x.getDescription().equals(wydotTim.getSpeed().toString()))
            .findFirst()
            .orElse(null);
        if(speed != null) {
            items.add(speed.getItisCode().toString());   
        }
        else
            return items;

        ItisCode speedLimit = getItisCodes().stream()
            .filter(x -> x.getDescription().equals("speed limit"))
            .findFirst()
            .orElse(null);
        if(speedLimit != null) {
            items.add(speedLimit.getItisCode().toString());           
        }

        ItisCode mph = getItisCodes().stream()
            .filter(x -> x.getDescription().equals("mph"))
            .findFirst()
            .orElse(null);
        if(mph != null){
            items.add(mph.getItisCode().toString());  
        }

        return items;
    }

    public static List<String> setItisCodesParking(WydotTim wydotTim) {
        
        // check to see if code exists        
        List<String> items = new ArrayList<String>();               
    
        ItisCode code = getItisCodes().stream()
        .filter(x -> x.getItisCode().equals(wydotTim.getAvailability()))
        .findFirst()
        .orElse(null);        

        if(code != null)
            items.add(wydotTim.getAvailability().toString());                       

        if(wydotTim.getExit() != null){
            items.add("11794");
            List<String> list = splitExitNumberFromLetter(wydotTim.getExit());
            int exitItisCodeNumber = convertNumberToItisCode(Integer.parseInt(list.get(0)));
            items.add(String.valueOf(exitItisCodeNumber));
            if(list.size() > 1)
                items.add(list.get(1));
        }
        else{
            items.add("7986");
        }

        return items;
    }

    public static List<String> splitExitNumberFromLetter(String exit){
        
        List<String> list = new ArrayList<String>();
        String exitNumber = ""; 
        String exitLetter = "";
        for (int i = 0; i < exit.length(); i++) {
            if(StringUtils.isNumeric(String.valueOf(exit.charAt(i)))){
                exitNumber += exit.charAt(i);
            }
            else {
                exitLetter += exit.charAt(i);
            }
        }

        list.add(exitNumber);
        if(exitLetter.length() > 0)
            list.add(exitLetter);

        return list;
    }

    public static List<String> setItisCodesFromAvailability(WydotTim wydotTim) {    
        
        // check to see if code exists        
        List<String> items = new ArrayList<String>();               
        
        ItisCode code = getItisCodes().stream()
        .filter(x -> x.getItisCode().equals(wydotTim.getAvailability()))
        .findFirst()
        .orElse(null);        

        if(code != null)
            items.add(wydotTim.getAvailability().toString());                       

        if(wydotTim.getExit() != null){
            items.add("11794");
            items.add(wydotTim.getExit());
        }
        else{
            items.add("7986");
        }

        return items;
    }

    private static int convertNumberToItisCode(int number){
        int itisCode = 1 + 12544;
        return itisCode;
    }

    public static List<String> setItisCodesIncident(WydotTim wydotTim) {        
        List<String> items = new ArrayList<String>(); 

        // action
        IncidentChoice incidentAction = getIncidentActions().stream()
            .filter(x -> x.getCode().equals(wydotTim.getAction()))
            .findFirst()
            .orElse(null);
        
        // if action is not null and action itis code exists
        if(incidentAction != null && incidentAction.getItisCodeId() != null){
            ItisCode actionItisCode = getItisCodes().stream()
                .filter(x -> x.getItisCodeId().equals(incidentAction.getItisCodeId()))
                .findFirst()
                .orElse(null);
            if(actionItisCode != null){
                items.add(actionItisCode.getItisCode().toString());  
            }
        }

        // effect
        IncidentChoice incidentEffect = getIncidentEffects().stream()
            .filter(x -> x.getCode().equals(wydotTim.getEffect()))
            .findFirst()
            .orElse(null);
        
        // if effect is not null and effect itis code exists
        if(incidentEffect != null && incidentEffect.getItisCodeId() != null){
            ItisCode effectItisCode = getItisCodes().stream()
                .filter(x -> x.getItisCodeId().equals(incidentEffect.getItisCodeId()))
                .findFirst()
                .orElse(null);
            if(effectItisCode != null){
                items.add(effectItisCode.getItisCode().toString());  
            }
        }

        // problem
        IncidentChoice incidentProblem = getIncidentProblems().stream()
            .filter(x -> x.getCode().equals(wydotTim.getProblem()))
            .findFirst()
            .orElse(null);
        
        // if problem is not null and problem itis code exists
        if(incidentProblem != null && incidentProblem.getItisCodeId() != null){
            ItisCode problemItisCode = getItisCodes().stream()
                .filter(x -> x.getItisCodeId().equals(incidentProblem.getItisCodeId()))
                .findFirst()
                .orElse(null);
            if(problemItisCode != null){
                items.add(problemItisCode.getItisCode().toString());  
            }
        }

        if(items.size() == 0)
            items.add("531");

        return items;
    }

    public static List<IncidentChoice> getIncidentProblems(){
        if(incidentProblems != null)
            return incidentProblems;
        else{
            incidentProblems = IncidentChoicesService.selectAllIncidentProblems(); 
            return incidentProblems;
        }
    }

    public static List<IncidentChoice> getIncidentEffects(){
        if(incidentEffects != null)
            return incidentEffects;
        else{
            incidentEffects = IncidentChoicesService.selectAllIncidentEffects(); 
            return incidentEffects;
        }
    }

    public static List<IncidentChoice> getIncidentActions(){
        if(incidentActions != null)
            return incidentActions;
        else{
            incidentActions = IncidentChoicesService.selectAllIncidentActions(); 
            return incidentActions;
        }
    }

    public static List<String> setItisCodesRw(WydotTim wydotTim){

        List<String> items = new ArrayList<String>();      
        
        items.add("1025");           
       
        return items;
    }
	
}