package com.trihydro.odewrapper.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.trihydro.library.helpers.MilepostReduction;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TimDeleteSummary;
import com.trihydro.library.model.TimQuery;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.OdeService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.library.service.WydotTimService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.helpers.SetItisCodes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;

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

    class RsuClearSuccess {
        String rsuTarget;
        boolean success;
        String errMessage;
    }

    private OdeService odeService;

    @Autowired
    public UtilityController(BasicConfiguration _basicConfiguration, WydotTimService _wydotTimService,
            TimTypeService _timTypeService, SetItisCodes _setItisCodes, ActiveTimService _activeTimService,
            OdeService _odeService, RestTemplateProvider _restTemplateProvider, MilepostReduction _milepostReduction,
            Utility _utility) {
        super(_basicConfiguration, _wydotTimService, _timTypeService, _setItisCodes, _activeTimService,
                _restTemplateProvider, _milepostReduction, _utility);
        this.odeService = _odeService;
    }

    @RequestMapping(value = "/all-rsus-tim-check", method = RequestMethod.GET, headers = "Accept=application/json")
    public ResponseEntity<String> allRsusTimCheck() {

        // String url = configuration.getOdeUrl();
        utility.logWithDate("RSU TIM Check", this.getClass());

        List<RsuCheckResults> rsuCheckResultsList = new ArrayList<RsuCheckResults>();

        // get all RSUs
        for (WydotRsu rsu : wydotTimService.getRsus()) {

            List<Integer> activeTimIndicies = activeTimService.getActiveTimIndicesByRsu(rsu.getRsuTarget());
            Collections.sort(activeTimIndicies);

            RsuCheckResults rsuCheckResults = new RsuCheckResults();
            rsuCheckResults.activeTimIndicesList = activeTimIndicies;
            rsuCheckResults.rsuTarget = rsu.getRsuTarget();

            TimQuery timQuery = odeService.submitTimQuery(rsu, 0);
            if (timQuery == null || timQuery.getIndicies_set() == null) {
                rsuCheckResultsList.add(rsuCheckResults);
                continue;
            }

            Collections.sort(timQuery.getIndicies_set());

            if (!activeTimIndicies.equals(timQuery.getIndicies_set())) {
                rsuCheckResults.queryList = timQuery.getIndicies_set();
                rsuCheckResultsList.add(rsuCheckResults);
            }
        }

        String responseMessage = gson.toJson(rsuCheckResultsList);
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value = "/rsu-tim-check/{address:.+}", method = RequestMethod.GET, headers = "Accept=application/json")
    public ResponseEntity<String> rsuTimCheck(@PathVariable String address) {

        utility.logWithDate("RSU TIM Check", this.getClass());

        List<RsuCheckResults> rsuCheckResultsList = new ArrayList<RsuCheckResults>();

        // get all RSUs

        WydotRsu rsu = wydotTimService.getRsus().stream().filter(x -> x.getRsuTarget().equals(address)).findFirst()
                .orElse(null);

        RsuCheckResults rsuCheckResults = new RsuCheckResults();
        rsuCheckResults.queryList = new ArrayList<Integer>();
        rsuCheckResults.rsuIndexList = new ArrayList<Integer>();
        rsuCheckResults.activeTimIndicesList = new ArrayList<Integer>();

        utility.logWithDate(rsu.getRsuTarget(), this.getClass());
        rsuCheckResults.rsuTarget = rsu.getRsuTarget();

        com.trihydro.library.model.TimQuery timQuery = odeService.submitTimQuery(rsu, 0);

        if (timQuery != null && timQuery.getIndicies_set().size() > 0) {
            for (int index : timQuery.getIndicies_set()) {
                if (index != 0)
                    rsuCheckResults.queryList.add(index);
            }
        }

        rsuCheckResults.activeTimIndicesList = activeTimService.getActiveTimIndicesByRsu(rsu.getRsuTarget());

        if (rsuCheckResults.queryList.size() != 0 || rsuCheckResults.rsuIndexList.size() != 0
                || rsuCheckResults.activeTimIndicesList.size() != 0) {
            rsuCheckResultsList.add(rsuCheckResults);
        }

        String responseMessage = gson.toJson(rsuCheckResultsList);
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value = "/delete-tim", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteTim(@RequestBody ActiveTim activeTim) {

        wydotTimService.deleteTimsFromRsusAndSdx(Stream.of(activeTim).collect(Collectors.toList()));

        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value = "/delete-tims", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<TimDeleteSummary> deleteTims(@RequestBody List<Long> aTimIds) {
        var aTims = activeTimService.getActiveTimsById(aTimIds);
        var summary = wydotTimService.deleteTimsFromRsusAndSdx(aTims);
        return ResponseEntity.status(HttpStatus.OK).body(summary);
    }

    @RequestMapping(value = "/clear-rsu", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> clearRsu(@RequestBody String[] addresses) {
        if (addresses == null || addresses.length == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No addresses supplied");
        }

        List<RsuClearSuccess> deleteResults = new ArrayList<>();

        for (String address : addresses) {
            RsuClearSuccess result = new RsuClearSuccess();
            result.rsuTarget = address;
            WydotRsu rsu = wydotTimService.getRsus().stream().filter(x -> x.getRsuTarget().equals(address)).findFirst()
                    .orElse(null);

            if (rsu != null) {

                // query for used indexes, then send delete for each one
                TimQuery tq = odeService.submitTimQuery(rsu, 1);
                if (tq != null) {

                    for (Integer index : tq.getIndicies_set()) {
                        wydotTimService.deleteTimFromRsu(rsu, index);
                    }
                    result.success = true;
                } else {
                    result.success = false;
                    result.errMessage = "Querying RSU indexes failed";
                }
            } else {
                result.success = false;
                result.errMessage = "RSU not found for provided address";
            }
            deleteResults.add(result);
        }

        return ResponseEntity.status(HttpStatus.OK).body(gson.toJson(deleteResults));
    }
}
