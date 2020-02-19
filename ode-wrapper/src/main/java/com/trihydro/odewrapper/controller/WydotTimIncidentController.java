package com.trihydro.odewrapper.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TimType;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.TimIncidentList;
import com.trihydro.odewrapper.model.WydotTimIncident;
import com.trihydro.odewrapper.service.WydotTimService;

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

@CrossOrigin
@RestController
@Api(description = "Incidents")
public class WydotTimIncidentController extends WydotTimBaseController {

    private static String type = "I";
    // get tim type
    TimType timType = getTimType(type);

    @Autowired
    public WydotTimIncidentController(BasicConfiguration _basicConfiguration, WydotTimService _wydotTimService,
            TimTypeService _timTypeService) {
        super(_basicConfiguration, _wydotTimService, _timTypeService);
    }

    @RequestMapping(value = "/incident-tim", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createIncidentTim(@RequestBody TimIncidentList timIncidentList) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

        System.out.println(dateFormat.format(date) + " - Create Incident TIM");
        String post = gson.toJson(timIncidentList);
        System.out.println(post.toString());

        List<WydotTimIncident> timsToSend = new ArrayList<WydotTimIncident>();

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;

        // build TIM
        for (WydotTimIncident wydotTim : timIncidentList.getTimIncidentList()) {

            resultTim = validateInputIncident(wydotTim);

            if (resultTim.getResultMessages().size() > 0) {
                resultList.add(resultTim);
                continue;
            }

            // make tims
            timsToSend.add(wydotTim);

            resultTim.getResultMessages().add("success");
            resultList.add(resultTim);
        }

        makeTims(timsToSend);

        String responseMessage = gson.toJson(resultList);
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value = "/incident-tim", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateIncidentTim(@RequestBody TimIncidentList timIncidentList) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

        System.out.println(dateFormat.format(date) + " - Update Incident TIM");
        String post = gson.toJson(timIncidentList);
        System.out.println(post.toString());

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;
        List<WydotTimIncident> timsToSend = new ArrayList<WydotTimIncident>();

        // delete TIMs
        for (WydotTimIncident wydotTim : timIncidentList.getTimIncidentList()) {

            resultTim = validateInputIncident(wydotTim);

            if (resultTim.getResultMessages().size() > 0) {
                resultList.add(resultTim);
                continue;
            }
            
            // make tims
            timsToSend.add(wydotTim);

            resultTim.getResultMessages().add("success");
            resultList.add(resultTim);
        }
        wydotTimService.deleteWydotTimsByType(timIncidentList.getTimIncidentList(), type);

        // make tims and send them
        makeTims(timsToSend);

        String responseMessage = gson.toJson(resultList);
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    public void makeTims(List<WydotTimIncident> wydotTims) {

        new Thread(new Runnable() {
            public void run() {
                String startTime = java.time.Clock.systemUTC().instant().toString();
                for (WydotTimIncident wydotTim : wydotTims) {

                    Double timPoint = null;

                    // set route
                    wydotTim.setRoute(wydotTim.getHighway());

                    // check if this is a point TIM
                    if (wydotTim.getFromRm().equals(wydotTim.getToRm()) || wydotTim.getToRm() == null) {
                        timPoint = wydotTim.getFromRm();
                    }

                    if (wydotTim.getDirection().equals("both")) {

                        // first TIM - eastbound - add buffer for point TIMs
                        if (timPoint != null)
                            wydotTim.setToRm(timPoint - 1);

                        createSendTims(wydotTim, "eastbound", timType, startTime, null, wydotTim.getPk());

                        // second TIM - westbound - add buffer for point TIMs
                        if (timPoint != null)
                            wydotTim.setToRm(timPoint + 1);

                        createSendTims(wydotTim, "westbound", timType, startTime, null, wydotTim.getPk());
                    } else {
                        // single direction TIM

                        // eastbound - add buffer for point TIMs
                        if (wydotTim.getDirection().equals("eastbound") && timPoint != null)
                            wydotTim.setToRm(timPoint - 1);

                        // westbound - add buffer for point TIMs
                        if (wydotTim.getDirection().equals("westbound") && timPoint != null)
                            wydotTim.setToRm(timPoint + 1);

                        createSendTims(wydotTim, wydotTim.getDirection(), timType, startTime, null, wydotTim.getPk());
                    }
                }
            }
        }).start();
    }

    @RequestMapping(value = "/incident-tim/{incidentId}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteIncidentTim(@PathVariable String incidentId) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

        System.out.println(dateFormat.format(date) + " - Delete Incident TIM");

        // clear TIM
        wydotTimService.clearTimsById("I", incidentId, null);

        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value = "/incident-tim", method = RequestMethod.GET, headers = "Accept=application/json")
    public Collection<ActiveTim> getIncidentTims() {

        // get active TIMs
        List<ActiveTim> activeTims = wydotTimService.selectTimsByType("I");

        return activeTims;
    }

    @RequestMapping(value = "/incident-tim/{incidentId}", method = RequestMethod.GET, headers = "Accept=application/json")
    public Collection<ActiveTim> getIncidentTimById(@PathVariable String incidentId) {

        // get active TIMs
        List<ActiveTim> activeTims = wydotTimService.selectTimByClientId("I", incidentId);

        // // add ITIS codes to TIMs
        // for (ActiveTim activeTim : activeTims) {
        // ActiveTimService.addItisCodesToActiveTim(activeTim);
        // }

        return activeTims;
    }

    // asynchronous TIM creation
    public void processRequest(List<WydotTimIncident> wydotTims) {
        // An Async task always executes in new thread
        new Thread(new Runnable() {
            public void run() {

                // get tim type
                TimType timType = getTimType(type);
                String startTime = java.time.Clock.systemUTC().instant().toString();
                for (WydotTimIncident wydotTim : wydotTims) {

                    Double timPoint = null;

                    // set route
                    wydotTim.setRoute(wydotTim.getHighway());

                    // check if this is a point TIM
                    if (wydotTim.getFromRm().equals(wydotTim.getToRm()) || wydotTim.getToRm() == null) {
                        timPoint = wydotTim.getFromRm();
                    }

                    if (wydotTim.getDirection().equals("both")) {

                        // first TIM - eastbound - add buffer for point TIMs
                        if (timPoint != null)
                            wydotTim.setFromRm(timPoint - 1);

                        createSendTims(wydotTim, "eastbound", timType, startTime, null, wydotTim.getPk());

                        // second TIM - westbound - add buffer for point TIMs
                        if (timPoint != null)
                            wydotTim.setFromRm(timPoint + 1);

                        createSendTims(wydotTim, "westbound", timType, startTime, null, wydotTim.getPk());
                    } else {
                        // single direction TIM

                        // eastbound - add buffer for point TIMs
                        if (wydotTim.getDirection().equals("eastbound") && timPoint != null)
                            wydotTim.setFromRm(timPoint - 1);

                        // westbound - add buffer for point TIMs
                        if (wydotTim.getDirection().equals("westbound") && timPoint != null)
                            wydotTim.setFromRm(timPoint + 1);

                        createSendTims(wydotTim, wydotTim.getDirection(), timType, startTime, null, wydotTim.getPk());
                    }
                }
            }
        }).start();
    }
}
