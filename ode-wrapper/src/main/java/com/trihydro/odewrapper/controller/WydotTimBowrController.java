package com.trihydro.odewrapper.controller;

import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.MilepostReduction;
import com.trihydro.library.helpers.TimGenerationHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ContentEnum;
import com.trihydro.library.model.WydotTim;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.CascadeService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.library.service.WydotTimService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.helpers.SetItisCodes;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.TimBowrList;
import com.trihydro.odewrapper.model.WydotTimBowr;

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
        utility.logWithDate("Create Or Update Blow Over Weight Restriction TIM", this.getClass());

        List<ControllerResult> results = new ArrayList<ControllerResult>();
        List<ControllerResult> errors = new ArrayList<ControllerResult>();
        ControllerResult timResult = null;
        List<WydotTim> timsToSend = new ArrayList<WydotTim>();

        for (WydotTimBowr wydotTimBowr : timBowrList.getTimBowrList()) {
            
            timResult = validateInputBowr(wydotTimBowr);

            if (timResult.getResultMessages().size() > 0) {
                results.add(timResult);
                errors.add(timResult);
                continue;
            }

            timsToSend.add(wydotTimBowr);

            timResult.getResultMessages().add("success");
            results.add(timResult);
        }

        processRequestAsync(timsToSend);
        String responseMessage = gson.toJson(results);
        if (errors.size() > 0) {
            utility.logWithDate("Failed to send TIMs: " + gson.toJson(errors), this.getClass());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMessage);
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value = "/submit-bowr-clear/{clientId}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> submitBowrClear(@PathVariable String clientId) {
        utility.logWithDate("Submit Blow Over Weight Restriction Clear", this.getClass());

        List<Long> existingTimIds = new ArrayList<Long>();

        // validate client id
        if (clientId == null || clientId.length() == 0) {
            String responseMessage = "Null or empty value for client id";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMessage);
        }

        // get existing active TIMs
        var timType = getTimType(type);
        Long timTypeId = timType != null ? timType.getTimTypeId() : null;
        List<ActiveTim> existingActiveTims = activeTimService.getActiveTimsByClientIdDirection(clientId, timTypeId, null);
        if (existingActiveTims.size() == 0) {
            utility.logWithDate("No active TIMs found for client id: " + clientId, this.getClass());
            String responseMessage = "No active TIMs found for client id: " + clientId;
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMessage);
        }

        // get ids from existingActiveTims
        for (ActiveTim existingActiveTim : existingActiveTims) {
            existingTimIds.add(existingActiveTim.getActiveTimId());
        }

        // expire existing tims
        if (existingTimIds.size() > 0) {
            timGenerationHelper.expireTimAndResubmitToOde(existingTimIds);
        }

        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    public void processRequestAsync(List<WydotTim> wydotTims) {
        // An Async task always executes in new thread
        new Thread(new Runnable() {
            public void run() {
                for (WydotTim tim : wydotTims) {
                    WydotTimBowr wydotTimBowr = (WydotTimBowr) tim;

                    // get start time
                    String startTime = wydotTimBowr.getStartDateTime();
                    if (startTime == null) {
                        startTime = getStartTime();
                    }
                    
                    // get end time
                    String endTime = wydotTimBowr.getEndDateTime();
                    
                    processRequest(tim, getTimType(type), startTime, endTime, null, ContentEnum.advisory,
                            TravelerInfoType.advisory);
                }
            }
        }).start();
    }
}
