package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TimType;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.odewrapper.model.TimQuery;
import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.model.WydotTimList;
import com.trihydro.odewrapper.model.WydotTravelerInputData;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@CrossOrigin
@RestController
@Api(description = "Utilities")
public class UtilityController extends WydotTimBaseController {

    class RsuCheckResults {
        List<Integer> queryList;
        List<Integer> rsuIndexList;
        List<Integer> activeTimIndicesList;
        String rsuTarget;
    }

    private static String type = "TEST";
    // get tim type
    TimType timType = getTimType(type);

    @RequestMapping(value = "/create-sat-tim", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createSatTim(@RequestBody WydotTimList wydotTimList) {

        // build TIM
        for (WydotTim wydotTim : wydotTimList.getTimList()) {

            // send TIM

            String regionNamePrev = wydotTim.getDirection() + "_" + wydotTim.getRoute() + "_" + wydotTim.getFromRm()
                    + "_" + wydotTim.getToRm();

            WydotTravelerInputData timToSend = wydotTimService.createTim(wydotTim, wydotTim.getDirection(), null, null,
                    null);

            wydotTimService.sendTimToSDW(wydotTim, timToSend, regionNamePrev, wydotTim.getDirection(), timType, null);

        }
        return ResponseEntity.status(HttpStatus.OK).body("ok");
    }

    @RequestMapping(value = "/all-rsus-tim-check", method = RequestMethod.GET, headers = "Accept=application/json")
    public ResponseEntity<String> allRsusTimCheck() {

        // String url = configuration.getOdeUrl();
        System.out.println("RSU TIM Check");

        List<RsuCheckResults> rsuCheckResultsList = new ArrayList<RsuCheckResults>();

        // get all RSUs
        for (WydotRsu rsu : wydotTimService.getRsus()) {
            
            List<Integer> activeTimIndicies = ActiveTimService.getActiveTimIndicesByRsu(rsu.getRsuTarget());
            Collections.sort(activeTimIndicies);

            RsuCheckResults rsuCheckResults = new RsuCheckResults();
            rsuCheckResults.activeTimIndicesList = activeTimIndicies;
            rsuCheckResults.rsuTarget = rsu.getRsuTarget();
            
            TimQuery timQuery = submitTimQuery(rsu, 0);
            if(timQuery == null || timQuery.getIndicies_set() == null){
                rsuCheckResultsList.add(rsuCheckResults);
                continue;
            }
                
            Collections.sort(timQuery.getIndicies_set());           

            if(!activeTimIndicies.equals(timQuery.getIndicies_set())){                
                rsuCheckResults.queryList = timQuery.getIndicies_set();            
                rsuCheckResultsList.add(rsuCheckResults);
            }           
        }

        String responseMessage = gson.toJson(rsuCheckResultsList);
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value = "/rsu-tim-check/{address:.+}", method = RequestMethod.GET, headers = "Accept=application/json")
    public ResponseEntity<String> rsuTimCheck(@PathVariable String address) {

        System.out.println("RSU TIM Check");

        List<RsuCheckResults> rsuCheckResultsList = new ArrayList<RsuCheckResults>();

        // get all RSUs

        WydotRsu rsu = wydotTimService.getRsus().stream().filter(x -> x.getRsuTarget().equals(address)).findFirst()
                .orElse(null);

        RsuCheckResults rsuCheckResults = new RsuCheckResults();
        rsuCheckResults.queryList = new ArrayList<Integer>();
        rsuCheckResults.rsuIndexList = new ArrayList<Integer>();
        rsuCheckResults.activeTimIndicesList = new ArrayList<Integer>();

        System.out.println(rsu.getRsuTarget());
        rsuCheckResults.rsuTarget = rsu.getRsuTarget();

        TimQuery timQuery = submitTimQuery(rsu, 0);

        if (timQuery != null && timQuery.getIndicies_set().size() > 0) {
            for (int index : timQuery.getIndicies_set()) {
                if (index != 0)
                    rsuCheckResults.queryList.add(index);
            }
        }

        rsuCheckResults.activeTimIndicesList = ActiveTimService.getActiveTimIndicesByRsu(rsu.getRsuTarget());

        if (rsuCheckResults.queryList.size() != 0 || rsuCheckResults.rsuIndexList.size() != 0
                || rsuCheckResults.activeTimIndicesList.size() != 0) {
            rsuCheckResultsList.add(rsuCheckResults);
        }

        String responseMessage = gson.toJson(rsuCheckResultsList);
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value = "/delete-tim", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteTim(@RequestBody ActiveTim activeTim) {

        wydotTimService.deleteTimsFromRsusAndSdw(Stream.of(activeTim).collect(Collectors.toList()));

        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }
}
