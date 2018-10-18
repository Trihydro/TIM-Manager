package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TimType;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.model.WydotTimList;
import com.trihydro.odewrapper.model.WydotTravelerInputData;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;

@CrossOrigin
@RestController
@Api(description = "Incidents")
public class WydotTimIncidentController extends WydotTimBaseController {

    private static String type = "I";

    @RequestMapping(value = "/incident-tim", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createIncidentTim(@RequestBody WydotTimList wydotTimList) {

        System.out.println("Create Incident TIM");

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;
        List<WydotTim> validTims = new ArrayList<WydotTim>();

        // build TIM
        for (WydotTim wydotTim : wydotTimList.getTimIncidentList()) {

            resultTim = validateInputIncident(wydotTim);

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

    @RequestMapping(value = "/incident-tim", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateIncidentTim(@RequestBody WydotTimList wydotTimList) {

        System.out.println("Update Incident TIM");

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;
        List<WydotTim> validTims = new ArrayList<WydotTim>();

        // build TIM
        for (WydotTim wydotTim : wydotTimList.getTimIncidentList()) {

            resultTim = validateInputIncident(wydotTim);

            if (resultTim.getResultMessages().size() > 0) {
                resultList.add(resultTim);
                continue;
            }

            // add valid TIM to list to be sent
            validTims.add(wydotTim);

            // find tims to update
            List<ActiveTim> activeTims = ActiveTimService.getRsusWithActiveTim(wydotTim.getIncidentId(),
                    wydotTim.getDirection(), "I");

            // for each TIM, delete off RSUs

            // delete off active tim table

            // recreate RSU TIMs

            // update SAT TIM

            resultTim.getResultMessages().add("success");
            resultList.add(resultTim);
        }

        processRequest(validTims);

        String responseMessage = gson.toJson(resultList);
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value = "/incident-tim/{incidentId}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteIncidentTim(@PathVariable String incidentId) {

        // clear TIM
        wydotTimService.clearTimsById("I", incidentId);

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
    public void processRequest(List<WydotTim> wydotTims) {
        // An Async task always executes in new thread
        new Thread(new Runnable() {
            public void run() {

                // get tim type
                TimType timType = getTimType(type);

                for (WydotTim wydotTim : wydotTims) {

                    Double timPoint = null;
                    // set client ID
                    wydotTim.setClientId(wydotTim.getIncidentId());
                    // set start time
                    wydotTim.setStartDateTime(wydotTim.getTs());
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

                        // wydotTimService.createUpdateTim("I", wydotTim, "eastbound", itisCodes);
                        createSendTims(wydotTim, "eastbound", timType);

                        // second TIM - westbound - add buffer for point TIMs
                        if (timPoint != null)
                            wydotTim.setFromRm(timPoint + 1);

                        // wydotTimService.createUpdateTim("I", wydotTim, "westbound", itisCodes);
                        createSendTims(wydotTim, "westbound", timType);
                    } else {
                        // single direction TIM

                        // eastbound - add buffer for point TIMs
                        if (wydotTim.getDirection().equals("eastbound") && timPoint != null)
                            wydotTim.setFromRm(timPoint - 1);

                        // westbound - add buffer for point TIMs
                        if (wydotTim.getDirection().equals("westbound") && timPoint != null)
                            wydotTim.setFromRm(timPoint + 1);

                        // wydotTimService.createUpdateTim("I", wydotTim, wydotTim.getDirection(),
                        // itisCodes);
                        createSendTims(wydotTim, wydotTim.getDirection(), timType);
                    }
                }
            }
        }).start();
    }
}
