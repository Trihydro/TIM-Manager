package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trihydro.library.model.RsuIndex;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.RsuIndexService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.model.TimQuery;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
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

    @RequestMapping(value = "/all-rsus-tim-check", method = RequestMethod.GET, headers = "Accept=application/json")
    public ResponseEntity<String> allRsusTimCheck() {

        // String url = configuration.getOdeUrl();
        System.out.println("RSU TIM Check");

        List<RsuCheckResults> rsuCheckResultsList = new ArrayList<RsuCheckResults>();

        // get all RSUs
        for (WydotRsu rsu : wydotTimService.getRsus()) {

            RsuCheckResults rsuCheckResults = new RsuCheckResults();
            rsuCheckResults.queryList = new ArrayList<Integer>();
            rsuCheckResults.rsuIndexList = new ArrayList<Integer>();
            rsuCheckResults.activeTimIndicesList = new ArrayList<Integer>();

            System.out.println(rsu.getRsuTarget());
            rsuCheckResults.rsuTarget = rsu.getRsuTarget();

            TimQuery timQuery = submitTimQuery(rsu, 0);

            if (timQuery != null && timQuery.getIndicies_set().length > 0) {
                for (int index : timQuery.getIndicies_set()) {
                    if (index != 0)
                        rsuCheckResults.queryList.add(index);
                }
            }

            List<RsuIndex> rsuIndicies = RsuIndexService.selectByRsuId(rsu.getRsuId());

            for (RsuIndex rsuIndex : rsuIndicies) {
                rsuCheckResults.rsuIndexList.add(rsuIndex.getRsuIndex());
            }

            rsuCheckResults.activeTimIndicesList = ActiveTimService.getActiveTimIndicesByRsu(rsu.getRsuTarget());

            if (rsuCheckResults.queryList.size() != 0 || rsuCheckResults.rsuIndexList.size() != 0
                    || rsuCheckResults.activeTimIndicesList.size() != 0) {
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

        if (timQuery != null && timQuery.getIndicies_set().length > 0) {
            for (int index : timQuery.getIndicies_set()) {
                if (index != 0)
                    rsuCheckResults.queryList.add(index);
            }
        }

        List<RsuIndex> rsuIndicies = RsuIndexService.selectByRsuId(rsu.getRsuId());

        for (RsuIndex rsuIndex : rsuIndicies) {
            rsuCheckResults.rsuIndexList.add(rsuIndex.getRsuIndex());
        }

        rsuCheckResults.activeTimIndicesList = ActiveTimService.getActiveTimIndicesByRsu(rsu.getRsuTarget());

        if (rsuCheckResults.queryList.size() != 0 || rsuCheckResults.rsuIndexList.size() != 0
                || rsuCheckResults.activeTimIndicesList.size() != 0) {
            rsuCheckResultsList.add(rsuCheckResults);
        }

        String responseMessage = gson.toJson(rsuCheckResultsList);
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }
}
