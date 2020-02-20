package com.trihydro.odewrapper.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.trihydro.library.model.WydotTim;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.helpers.SetItisCodes;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.TimRcList;
import com.trihydro.odewrapper.model.WydotTimRc;
import com.trihydro.odewrapper.service.WydotTimService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;

@CrossOrigin
@RestController
@Api(description = "Road Conditions")
public class WydotTimRcController extends WydotTimBaseController {

    private static String type = "RC";

    @Autowired
    public WydotTimRcController(BasicConfiguration _basicConfiguration, WydotTimService _wydotTimService,
            TimTypeService _timTypeService, SetItisCodes _setItisCodes) {
        super(_basicConfiguration, _wydotTimService, _timTypeService, _setItisCodes);
    }

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

        processRequestAsync(timsToSend);
        String responseMessage = gson.toJson(resultList);
        if (errList.size() > 0) {
            System.out.println("Failed to send TIMs: " + gson.toJson(errList));
        }
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

        List<ControllerResult> errList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;
        List<WydotTim> timsToDelete = new ArrayList<WydotTim>();

        for (WydotTimRc wydotTim : timRcList.getTimRcList()) {
            resultTim = validateInputRc(wydotTim);
            if (resultTim.getResultMessages().size() > 0) {
                resultList.add(resultTim);
                errList.add(resultTim);
                continue;
            }

            timsToDelete.add(wydotTim);
            resultTim.getResultMessages().add("success");
            resultList.add(resultTim);
        }

        if (timsToDelete.size() > 0) {
            wydotTimService.deleteWydotTimsByType(timsToDelete, type);
        }

        String responseMessage = gson.toJson(resultList);
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    public void processRequestAsync(List<WydotTim> wydotTims) {
        // An Async task always executes in new thread
        new Thread(new Runnable() {
            public void run() {
                String startTime = java.time.Clock.systemUTC().instant().toString();
                for (WydotTim tim : wydotTims) {
                    processRequest(tim, getTimType(type), startTime, null, null);
                }
            }
        }).start();
    }
}
