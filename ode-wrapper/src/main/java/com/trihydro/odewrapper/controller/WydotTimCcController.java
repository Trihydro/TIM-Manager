package com.trihydro.odewrapper.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.trihydro.library.model.ContentEnum;
import com.trihydro.library.model.WydotTim;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.library.service.WydotTimService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.helpers.SetItisCodes;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.TimRcList;
import com.trihydro.odewrapper.model.WydotTimRc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.FrameType.TravelerInfoType;

@CrossOrigin
@RestController
@Api(description = "Chain Controls")
public class WydotTimCcController extends WydotTimBaseController {

    private final String type = "CC";

    @Autowired
    public WydotTimCcController(BasicConfiguration _basicConfiguration, WydotTimService _wydotTimService,
            TimTypeService _timTypeService, SetItisCodes _setItisCodes, ActiveTimService _activeTimService,
            RestTemplateProvider _restTemplateProvider) {
        super(_basicConfiguration, _wydotTimService, _timTypeService, _setItisCodes, _activeTimService,
                _restTemplateProvider);
    }

    @RequestMapping(value = "/cc-tim", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createChainControlTim(@RequestBody TimRcList timRcList) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

        System.out.println(dateFormat.format(date) + " - CHAIN CONTROL TIM");

        String post = gson.toJson(timRcList);
        System.out.println(post.toString());

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;
        List<WydotTimRc> timsToSend = new ArrayList<WydotTimRc>();

        for (WydotTimRc wydotTim : timRcList.getTimRcList()) {
            validateInputCc(wydotTim);
        }
        wydotTimService.deleteWydotTimsByType(timRcList.getTimRcList(), "CC");

        // build TIM
        for (WydotTimRc wydotTim : timRcList.getTimRcList()) {

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

        processRequestAsync(timsToSend);

        String responseMessage = gson.toJson(resultList);
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    public void processRequestAsync(List<WydotTimRc> wydotTims) {
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
