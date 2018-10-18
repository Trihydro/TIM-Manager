package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;

import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.TimType;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.model.WydotTimList;
import com.trihydro.odewrapper.model.WydotTravelerInputData;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;

@CrossOrigin
@RestController
@Api(description = "Road Conditions")
public class WydotTimRcController extends WydotTimBaseController {

    private static String type = "RC";

    @RequestMapping(value = "/create-update-rc-tim", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createUpdateRoadConditionsTim(@RequestBody WydotTimList wydotTimList) {

        System.out.println("Create Update RC TIM");

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;
        List<WydotTim> validTims = new ArrayList<WydotTim>();

        // build TIM
        for (WydotTim wydotTim : wydotTimList.getTimRcList()) {

            resultTim = validateInputRc(wydotTim);

            if (resultTim.getResultMessages().size() > 0) {
                resultList.add(resultTim);
                continue;
            }

            // add valid TIM to list to be sent
            validTims.add(wydotTim);

            resultTim.getResultMessages().add("success");
            resultList.add(resultTim);
        }

        processRequest(validTims);

        String responseMessage = gson.toJson(resultList);
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value = "/submit-rc-ac", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> submitAllClearRoadConditionsTim(@RequestBody WydotTimList wydotTimList) {

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;

        // clear TIMs
        for (WydotTim wydotTim : wydotTimList.getTimRcList()) {
            if (wydotTim.getDirection().equals("both")) {
                resultTim = wydotTimService.clearTimsByRoadSegment("RC", wydotTim, "eastbound");
                resultList.add(resultTim);
                resultTim = wydotTimService.clearTimsByRoadSegment("RC", wydotTim, "westbound");
                resultList.add(resultTim);
            } else
                resultTim = wydotTimService.clearTimsByRoadSegment("RC", wydotTim, wydotTim.getDirection());
            resultList.add(resultTim);
        }

        String responseMessage = gson.toJson(resultList);
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    // asynchronous TIM creation
    public void processRequest(List<WydotTim> wydotTims) {
        // An Async task always executes in new thread
        new Thread(new Runnable() {
            public void run() {
                // get tim type
                TimType timType = getTimType(type);
                for (WydotTim wydotTim : wydotTims) {
                    if (wydotTim.getDirection().equals("both")) {
                        // eastbound
                        createSendTims(wydotTim, "eastbound", timType);

                        // westbound
                        createSendTims(wydotTim, "westbound", timType);
                    } else {
                        createSendTims(wydotTim, wydotTim.getDirection(), timType);
                    }
                }
            }
        }).start();
    }
}
