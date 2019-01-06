package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TimType;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.model.WydotTimList;
import com.trihydro.odewrapper.model.WydotTimVsl;

import org.springframework.http.HttpStatus;

@CrossOrigin
@RestController
@Api(description = "Variable Speed Limits")
public class WydotTimVslController extends WydotTimBaseController {

    private static String type = "VSL";
    // get tim type
    TimType timType = getTimType(type);

    @RequestMapping(value = "/vsl-tim", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createUpdateVslTim(@RequestBody WydotTimList wydotTimList) {

        System.out.println("Create/Update VSL TIM");
        String post = gson.toJson(wydotTimList);
        System.out.println(post.toString());

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;
        List<WydotTim> timsToSend = new ArrayList<WydotTim>();

        // build TIM
        for (WydotTimVsl wydotTim : wydotTimList.getTimVslList()) {
            resultTim = validateInputVsl(wydotTim);

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

    @RequestMapping(value = "/vsl-tim", method = RequestMethod.GET, headers = "Accept=application/json")
    public Collection<ActiveTim> getVslTims() {

        // get active TIMs
        List<ActiveTim> activeTims = wydotTimService.selectTimsByType(type);

        // add ITIS codes to TIMs
        for (ActiveTim activeTim : activeTims) {
            ActiveTimService.addItisCodesToActiveTim(activeTim);
        }

        return activeTims;
    }

}
