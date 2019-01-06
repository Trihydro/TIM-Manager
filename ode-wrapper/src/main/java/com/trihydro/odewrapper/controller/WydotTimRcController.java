package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;

import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.TimType;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.model.WydotTimList;
import com.trihydro.odewrapper.model.WydotTimRc;

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
    // get tim type
    TimType timType = getTimType(type);

    @RequestMapping(value = "/create-update-rc-tim", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createUpdateRoadConditionsTim(@RequestBody WydotTimList wydotTimList) {

        System.out.println("Create Update RC TIM");
        String post = gson.toJson(wydotTimList);
        System.out.println(post.toString());

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;
        List<WydotTim> timsToSend = new ArrayList<WydotTim>();

        // build TIM
        for (WydotTimRc wydotTim : wydotTimList.getTimRcList()) {

            resultTim = validateInputRc(wydotTim);

            if (resultTim.getResultMessages().size() > 0) {
                resultList.add(resultTim);
                continue;
            }

            // send TIM
            processRequest(wydotTim, timType, null, null, null);

             // add TIM to list for processing later
             timsToSend.add(wydotTim);        

            resultTim.getResultMessages().add("success");
            resultList.add(resultTim);
        }

        processRequestTest(timsToSend);
        String responseMessage = gson.toJson(resultList);
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value = "/submit-rc-ac", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> submitAllClearRoadConditionsTim(@RequestBody WydotTimList wydotTimList) {

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();

        System.out.println("All Clear");
        String post = gson.toJson(wydotTimList);
        System.out.println(post.toString());

        for (WydotTimRc wydotTim : wydotTimList.getTimRcList()) {
            validateInputRc(wydotTim);
            wydotTimService.clearTimsById(timType.getType(), wydotTim.getClientId(), wydotTim.getDirection());
        }

        String responseMessage = gson.toJson(resultList);
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    public void processRequestTest(List<WydotTim> wydotTims) {
        // An Async task always executes in new thread
        new Thread(new Runnable() {
            public void run() {
                for (WydotTim tim : wydotTims) {
                    processRequest(tim, timType, null, null, null);
                }
            }
        }).start();
    }
}
