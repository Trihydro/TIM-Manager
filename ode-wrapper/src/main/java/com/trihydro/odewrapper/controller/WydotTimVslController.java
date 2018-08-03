package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import us.dot.its.jpo.ode.util.DateTimeUtils;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.model.WydotTimList;

import org.springframework.http.HttpStatus;

@CrossOrigin
@RestController
@Api(description="Variable Speed Limits")
public class WydotTimVslController extends WydotTimBaseController {
    
    @RequestMapping(value="/test-tim", method = RequestMethod.POST, headers="Accept=application/json")
    public ResponseEntity<String> ctestTim(@RequestBody WydotTimList wydotTimList) {        
        
        String isoTime = "2018-06-11T10:00-06:00";

        String time = Instant.now().toString();
        System.out.println(time);

        TimeZone tz = TimeZone.getTimeZone("UTC");

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());

        System.out.println(nowAsISO);

        //String isoTime = "2018-06-11T16:00:00.000Z";

        int startYear = 0;
        int startMinute = 527040;
        try {
           ZonedDateTime zDateTime = DateTimeUtils.isoDateTime(isoTime);
           System.out.println(zDateTime);
           startYear = zDateTime.getYear();           
           ZonedDateTime beginningOfYear = ZonedDateTime.of(startYear, 1, 1, 0, 0, 0, 0, zDateTime.getZone());
           System.out.println(beginningOfYear);
           startMinute = (int)ChronoUnit.MINUTES.between(beginningOfYear, zDateTime);
        } catch (ParseException e) {
           // failed to parse datetime, default back to unknown values
        }
        
        System.out.println(startMinute);
        
        return ResponseEntity.status(HttpStatus.OK).body("test");    
    }

    @RequestMapping(value="/vsl-tim", method = RequestMethod.POST, headers="Accept=application/json")
    public ResponseEntity<String> createUpdateVslTim(@RequestBody WydotTimList wydotTimList) {        
        
        System.out.println("Create/Update VSL TIM");

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;

        // build TIM        
        for (WydotTim wydotTim : wydotTimList.getTimVslList()) {
            if(wydotTim.getDirection().equals("both")) {
                wydotTimService.createUpdateTim("VSL", wydotTim, "eastbound");            
                wydotTimService.createUpdateTim("VSL", wydotTim, "westbound");
            }
            else
                wydotTimService.createUpdateTim("VSL", wydotTim, wydotTim.getDirection());                 
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

}
