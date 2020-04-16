package com.trihydro.odewrapper.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.WydotTim;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.helpers.ContentEnum;
import com.trihydro.odewrapper.helpers.SetItisCodes;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.TimVslList;
import com.trihydro.odewrapper.model.WydotTimVsl;
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
@Api(description = "Variable Speed Limits")
public class WydotTimVslController extends WydotTimBaseController {

    private final String type = "VSL";

    @Autowired
    public WydotTimVslController(BasicConfiguration _basicConfiguration, WydotTimService _wydotTimService,
            TimTypeService _timTypeService, SetItisCodes _setItisCodes, ActiveTimService _activeTimService,
            RestTemplateProvider _restTemplateProvider) {
        super(_basicConfiguration, _wydotTimService, _timTypeService, _setItisCodes, _activeTimService,
                _restTemplateProvider);
    }

    @RequestMapping(value = "/vsl-tim", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createUpdateVslTim(@RequestBody TimVslList timVslList) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

        System.out.println(dateFormat.format(date) + " - Create/Update VSL TIM");
        String post = gson.toJson(timVslList);
        System.out.println(post.toString());

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;
        List<WydotTim> timsToSend = new ArrayList<WydotTim>();

        // build TIM
        for (WydotTimVsl wydotTim : timVslList.getTimVslList()) {
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

        processRequestAsync(timsToSend);
        String responseMessage = gson.toJson(resultList);
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    public void processRequestAsync(List<WydotTim> wydotTims) {
        // An Async task always executes in new thread
        new Thread(new Runnable() {
            public void run() {
                String startTime = java.time.Clock.systemUTC().instant().toString();
                for (WydotTim tim : wydotTims) {
                    processRequest(tim, getTimType(type), startTime, null, null, ContentEnum.speedLimit);
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
            activeTimService.addItisCodesToActiveTim(activeTim);
        }

        return activeTims;
    }

}
