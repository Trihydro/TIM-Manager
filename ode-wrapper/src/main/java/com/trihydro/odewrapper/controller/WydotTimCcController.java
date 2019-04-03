package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;

import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.model.WydotTimList;
import com.trihydro.odewrapper.model.WydotTimRc;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import java.util.List;

import com.trihydro.library.model.TimType;
import com.trihydro.odewrapper.model.ControllerResult;
import java.util.ArrayList;

@CrossOrigin
@RestController
@Api(description = "Chain Controls")
public class WydotTimCcController extends WydotTimBaseController {

    private static String type = "CC";
    // get tim type
    TimType timType = getTimType(type);

    @RequestMapping(value = "/cc-tim", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createChainControlTim(@RequestBody WydotTimList wydotTimList) {

        System.out.println("CHAIN CONTROL TIM");

        String post = gson.toJson(wydotTimList);
        System.out.println(post.toString());

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;
        List<WydotTimRc> timsToSend = new ArrayList<WydotTimRc>();

        for (WydotTimRc wydotTim : wydotTimList.getTimRcList()) {
            validateInputCc(wydotTim);
            wydotTimService.clearTimsById(type, wydotTim.getClientId(), null);
        }

        // build TIM
        for (WydotTimRc wydotTim : wydotTimList.getTimRcList()) {

            resultTim = validateInputCc(wydotTim);

            if (resultTim.getResultMessages().size() > 0) {
                resultList.add(resultTim);
                continue;
            }        

            // add TIM to list for processing later
            timsToSend.add(wydotTim);          

            resultTim.getResultMessages().add("success");
            resultList.add(resultTim);
        }

        processRequestTest(timsToSend);

        String responseMessage = gson.toJson(resultList);
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    public void processRequestTest(List<WydotTimRc> wydotTims) {
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
