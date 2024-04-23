package com.trihydro.odewrapper.controller;

import java.util.List;

import com.trihydro.library.helpers.MilepostReduction;
import com.trihydro.library.helpers.TimGenerationHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ContentEnum;
import com.trihydro.library.model.WydotTim;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.CascadeService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.library.service.WydotTimService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.helpers.SetItisCodes;
import com.trihydro.odewrapper.model.TimBowrList;
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
import us.dot.its.jpo.ode.plugin.j2735.timstorage.FrameType.TravelerInfoType;

@CrossOrigin
@RestController
@Api(description = "Blow Over Weight Restrictions")
public class WydotTimBowrController extends WydotTimBaseController {

    private final String type = "BOWR";

    @Autowired
    public WydotTimBowrController(BasicConfiguration _basicConfiguration, WydotTimService _wydotTimService,
            TimTypeService _timTypeService, SetItisCodes _setItisCodes, ActiveTimService _activeTimService,
            RestTemplateProvider _restTemplateProvider, MilepostReduction _milepostReduction, Utility _utility,
            TimGenerationHelper _timGenerationHelper, CascadeService _cascadeService) {
        super(_basicConfiguration, _wydotTimService, _timTypeService, _setItisCodes, _activeTimService,
                _restTemplateProvider, _milepostReduction, _utility, _timGenerationHelper, _cascadeService);
    }

    @RequestMapping(value = "/create-or-update-bowr-tim", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createOrUpdateBowrTim(@RequestBody TimBowrList timBowrList) {
        utility.logWithDate("Create Or Update Blow Over Weight Restriction TIM");

        // TODO: implement

        String responseMessage = "Test";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value = "/submit-bowr-clear/{clientId}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> submitBowrClear(@PathVariable String clientId) {
        utility.logWithDate("Submit Blow Over Weight Restriction Clear");

        // TODO: implement

        String responseMessage = "Test";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    public void processRequestAsync(List<WydotTim> wydotTims) {
        // An Async task always executes in new thread
        new Thread(new Runnable() {
            public void run() {
                var startTime = getStartTime();
                for (WydotTim tim : wydotTims) {
                    processRequest(tim, getTimType(type), startTime, null, null, ContentEnum.advisory,
                            TravelerInfoType.advisory);
                }
            }
        }).start();
    }
}
