package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.trihydro.library.model.TimType;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.TimRcList;
import com.trihydro.odewrapper.model.WydotTim;
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
    public ResponseEntity<String> createUpdateRoadConditionsTim(@RequestBody TimRcList timRcList) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();       

        System.out.println(dateFormat.format(date) + " - Create Update RC TIM");
        String post = gson.toJson(timRcList);
        System.out.println(post.toString());

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();
        List<ControllerResult> errList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;
        List<WydotTim> timsToSend = new ArrayList<WydotTim>();

        // build TIM
        for (WydotTimRc wydotTim : timRcList.getTimRcList()) {

            resultTim = validateInputRc(wydotTim);

            if (resultTim.getResultMessages().size() > 0) {
                resultList.add(resultTim);
                errList.add(resultTim);
                continue;
            }

            // add TIM to list for processing later
            timsToSend.add(wydotTim);

            resultTim.getResultMessages().add("success");
            resultList.add(resultTim);
        }

        processRequestTest(timsToSend);
        String responseMessage = gson.toJson(resultList);
        System.out.println("Failed to send TIMs: " + gson.toJson(errList));
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value = "/submit-rc-ac", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> submitAllClearRoadConditionsTim(@RequestBody TimRcList timRcList) {

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();       

        System.out.println(dateFormat.format(date) + " - All Clear");
        String post = gson.toJson(timRcList);
        System.out.println(post.toString());

        for (WydotTimRc wydotTim : timRcList.getTimRcList()) {
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
