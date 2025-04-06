package com.trihydro.odewrapper.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.trihydro.library.helpers.MilepostReduction;
import com.trihydro.library.helpers.TimGenerationHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ContentEnum;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.library.service.WydotTimService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.helpers.SetItisCodes;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.TimParkingList;
import com.trihydro.odewrapper.model.WydotTimParking;

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
@Api(description = "Parking")
public class WydotTimParkingController extends WydotTimBaseController {

    private final String type = "P";

    @Autowired
    public WydotTimParkingController(BasicConfiguration _basicConfiguration, WydotTimService _wydotTimService,
            TimTypeService _timTypeService, SetItisCodes _setItisCodes, ActiveTimService _activeTimService,
            RestTemplateProvider _restTemplateProvider, MilepostReduction _milepostReduction, Utility _utility,
            TimGenerationHelper _timGenerationHelper) {
        super(_basicConfiguration, _wydotTimService, _timTypeService, _setItisCodes, _activeTimService,
                _restTemplateProvider, _milepostReduction, _utility, _timGenerationHelper);
    }

    @RequestMapping(value = "/parking-tim", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createParkingTim(@RequestBody TimParkingList timParkingList) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

        String msg = dateFormat.format(date) + " - Create Parking TIM";
        System.out.println(msg);
        String post = gson.toJson(timParkingList);
        System.out.println(post.toString());

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;
        List<WydotTimParking> validTims = new ArrayList<WydotTimParking>();

        // build TIM
        for (WydotTimParking wydotTim : timParkingList.getTimParkingList()) {

            resultTim = validateInputParking(wydotTim);

            if (resultTim.getResultMessages().size() > 0) {
                resultList.add(resultTim);
                continue;
            }

            // add valid TIM to list to be sent
            validTims.add(wydotTim);

            resultTim.getResultMessages().add("success");
            resultList.add(resultTim);
        }

        processRequestAsync(validTims);

        String responseMessage = gson.toJson(resultList);
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value = "/parking-tim", method = RequestMethod.GET, headers = "Accept=application/json")
    public Collection<ActiveTim> getParkingTims() {

        // clear TIM
        List<ActiveTim> activeTims = wydotTimService.selectTimsByType("P");

        return activeTims;
    }

    @RequestMapping(value = "/parking-tim/{clientId}", method = RequestMethod.GET, headers = "Accept=application/json")
    public Collection<ActiveTim> getParkingTimById(@PathVariable String clientId) {

        // clear TIM
        List<ActiveTim> activeTims = wydotTimService.selectTimByClientId("P", clientId);

        return activeTims;
    }

    @RequestMapping(value = "/parking-tim/itis-codes/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    public Collection<ActiveTim> getParkingTimByIdWithItisCodes(@PathVariable String id) {

        // get tims
        List<ActiveTim> activeTims = wydotTimService.selectTimByClientId("P", id);

        // add ITIS codes to TIMs
        for (ActiveTim activeTim : activeTims) {
            activeTimService.addItisCodesToActiveTim(activeTim);
        }

        return activeTims;
    }

    @RequestMapping(value = "/parking-tim/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteParkingTim(@PathVariable String id) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

        String msg = dateFormat.format(date) + " - Delete Parking TIM";
        System.out.println(msg);
        // expire and clear TIM
        wydotTimService.clearTimsById("P", id, null);

        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    public void processRequestAsync(List<WydotTimParking> wydotTims) {
        new Thread(new Runnable() {
            public void run() {
                var startTime = getStartTime();
                for (WydotTimParking wydotTim : wydotTims) {
                    processRequest(wydotTim, getTimType(type), startTime, null, null, ContentEnum.exitService,
                            TravelerInfoType.advisory);
                }
            }
        }).start();
    }
}