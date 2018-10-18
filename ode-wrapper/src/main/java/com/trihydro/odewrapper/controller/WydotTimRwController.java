package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TimType;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.odewrapper.model.Buffer;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.model.WydotTimList;
import com.trihydro.odewrapper.model.WydotTravelerInputData;

import io.swagger.annotations.Api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;

@CrossOrigin
@RestController
@Api(description = "Road Construction")
public class WydotTimRwController extends WydotTimBaseController {

    private static String type = "RW";

    List<WydotTim> validTims;

    @RequestMapping(value = "/rw-tim", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createRoadContructionTim(@RequestBody WydotTimList wydotTimList) {

        System.out.println("Create/Update RW TIM");

        validTims = new ArrayList<WydotTim>();

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;

        // build TIM
        for (WydotTim wydotTim : wydotTimList.getTimRwList()) {

            // validate input
            resultTim = validateInputRw(wydotTim);

            // if there are invalidation messages skip to next TIM
            if (resultTim.getResultMessages().size() > 0) {
                resultList.add(resultTim);
                continue;
            }

            // if valid

            // sort buffers by distance
            wydotTim.getBuffers().sort(Comparator.comparingDouble(Buffer::getDistance));

            // adjust input
            wydotTim.setClientId(wydotTim.getId());
            wydotTim.setRoute(wydotTim.getHighway());
            wydotTim.setStartDateTime(wydotTim.getStartTs());

            Double timPoint = null;

            // check if its a point TIM
            if (wydotTim.getFromRm().equals(wydotTim.getToRm()) || wydotTim.getToRm() == null) {
                timPoint = wydotTim.getFromRm();
            }

            // if bi-directional
            if (wydotTim.getDirection().equals("both")) {
                // make eastbound TIMs
                makeEastboundTims(wydotTim, timPoint);
                // make westbound TIMs
                makeWestboundTims(wydotTim, timPoint);
            }
            // else make one direction TIMs
            if (wydotTim.getDirection().equals("eastbound"))
                makeEastboundTims(wydotTim, timPoint);
            else
                makeWestboundTims(wydotTim, timPoint);

            // compile result messages for user
            resultTim.getResultMessages().add("success");
            resultList.add(resultTim);
        }

        // send TIMs
        processRequest(validTims);

        String responseMessage = gson.toJson(resultList);
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    // asynchronous TIM creation
    public void processRequest(List<WydotTim> wydotTims) {
        // An Async task always executes in new thread
        new Thread(new Runnable() {
            public void run() {
                // get tim type
                TimType timType = getTimType(type);

                for (WydotTim wydotTim : wydotTims) {
                    if (wydotTim.getDirection().equals("both")) {
                        // eastbound
                        createSendTims(wydotTim, "eastbound", timType);

                        // westbound
                        createSendTims(wydotTim, "westbound", timType);
                    } else {
                        createSendTims(wydotTim, wydotTim.getDirection(), timType);
                    }
                }
            }
        }).start();
    }

    public void makeEastboundTims(WydotTim wydotTim, Double timPoint) {

        // eastbound - add buffer for point TIMs
        if (timPoint != null)
            wydotTim.setFromRm(timPoint - 1);

        validTims.add(wydotTim);
        makeEastboundBufferTim(wydotTim);
    }

    public void makeWestboundTims(WydotTim wydotTim, Double timPoint) {

        // westbound - add buffer for point TIMs
        if (timPoint != null)
            wydotTim.setFromRm(timPoint + 1);

        validTims.add(wydotTim);

        makeWestboundBufferTim(wydotTim);
    }

    // asynchronous TIM creation
    public void processRequestOld(WydotTim wydotTim) {
        // An Async task always executes in new thread
        new Thread(new Runnable() {
            public void run() {
                Double timPoint = null;
                // get tim type
                TimType timType = getTimType(type);

                // perform any operation
                System.out.println("Performing operation in Asynchronous Task");

                wydotTim.getBuffers().sort(Comparator.comparingDouble(Buffer::getDistance));
                double bufferBefore = 0;
                wydotTim.setClientId(wydotTim.getId());
                // set route
                wydotTim.setRoute(wydotTim.getHighway());
                wydotTim.setStartDateTime(wydotTim.getStartTs());

                // if its a point TIM
                if (wydotTim.getFromRm().equals(wydotTim.getToRm()) || wydotTim.getToRm() == null) {
                    timPoint = wydotTim.getFromRm();
                }

                if (wydotTim.getDirection().equals("both")) {

                    for (int i = 0; i < wydotTim.getBuffers().size(); i++) {

                        // eastbound - add buffer for point TIMs
                        if (timPoint != null)
                            wydotTim.setFromRm(timPoint - 1);

                        // starts at lower milepost minus the buffer distance
                        double bufferStart = Math.min(wydotTim.getToRm(), wydotTim.getFromRm())
                                - wydotTim.getBuffers().get(i).getDistance();
                        // ends at lower milepost minus previous buffers
                        double bufferEnd = Math.min(wydotTim.getToRm(), wydotTim.getFromRm()) - bufferBefore;

                        // update start and stopping mileposts
                        WydotTim wydotTimBuffer = null;
                        try {
                            wydotTimBuffer = wydotTim.clone();
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                        wydotTimBuffer.setFromRm(bufferStart);
                        wydotTimBuffer.setAction(wydotTim.getBuffers().get(i).getAction());
                        wydotTimBuffer.setToRm(bufferEnd);
                        wydotTimBuffer.setClientId(wydotTim.getClientId());

                        // send buffer tim
                        wydotTimBuffer.setAdvisory(wydotTimService.setBufferItisCodes(wydotTimBuffer.getAction()));
                        // wydotTimService.createTim(wydotTimBuffer, "eastbound", type, itisCodes);
                        createSendTims(wydotTimBuffer, "eastbound", timType);

                        // westbound - add buffer for point TIMs
                        if (timPoint != null)
                            wydotTim.setFromRm(timPoint + 1);

                        // starts at higher milepost plus buffer distance
                        bufferStart = Math.max(wydotTim.getToRm(), wydotTim.getFromRm())
                                + wydotTim.getBuffers().get(i).getDistance();
                        // ends at higher milepost plus previous buffers
                        bufferEnd = Math.max(wydotTim.getToRm(), wydotTim.getFromRm()) + bufferBefore;

                        // update start and stopping mileposts
                        try {
                            wydotTimBuffer = wydotTim.clone();
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                        wydotTimBuffer.setFromRm(bufferStart);
                        wydotTimBuffer.setAction(wydotTim.getBuffers().get(i).getAction());
                        wydotTimBuffer.setToRm(bufferEnd);
                        wydotTimBuffer.setClientId(wydotTim.getClientId());
                        // send buffer tim
                        wydotTimBuffer.setAdvisory(wydotTimService.setBufferItisCodes(wydotTimBuffer.getAction()));
                        // wydotTimService.createTim(wydotTimBuffer, "westbound", type, itisCodes);
                        createSendTims(wydotTimBuffer, "westbound", timType);

                        // update running buffer distance
                        bufferBefore = wydotTim.getBuffers().get(i).getDistance();
                    }
                    // send road construction TIM
                    // wydotTimService.createTim(wydotTim, "eastbound", type, itisCodes);
                    createSendTims(wydotTim, "eastbound", timType);
                    // wydotTimService.createTim(wydotTim, "westbound", type, itisCodes);
                    createSendTims(wydotTim, "westbound", timType);
                } else {
                    if (wydotTim.getDirection().equals("eastbound")) {

                        // eastbound - add buffer for point TIMs
                        if (timPoint != null)
                            wydotTim.setFromRm(timPoint - 1);

                        for (int i = 0; i < wydotTim.getBuffers().size(); i++) {
                            // eastbound
                            // starts at lower milepost minus the buffer distance
                            double bufferStart = Math.min(wydotTim.getToRm(), wydotTim.getFromRm())
                                    - wydotTim.getBuffers().get(i).getDistance();
                            // ends at lower milepost minus previous buffers
                            double bufferEnd = Math.min(wydotTim.getToRm(), wydotTim.getFromRm()) - bufferBefore;

                            // update start and stopping mileposts
                            WydotTim wydotTimBuffer = null;

                            try {
                                wydotTimBuffer = wydotTim.clone();
                            } catch (CloneNotSupportedException e) {
                                e.printStackTrace();
                            }
                            wydotTimBuffer.setFromRm(bufferStart);
                            wydotTimBuffer.setToRm(bufferEnd);
                            wydotTimBuffer.setAction(wydotTim.getBuffers().get(i).getAction());
                            wydotTimBuffer.setClientId(wydotTim.getClientId());

                            // send buffer tim
                            wydotTimBuffer.setAdvisory(wydotTimService.setBufferItisCodes(wydotTimBuffer.getAction()));
                            // wydotTimService.createTim(wydotTimBuffer, "eastbound", type, itisCodes);
                            createSendTims(wydotTimBuffer, "eastbound", timType);

                            // update running buffer distance
                            bufferBefore = wydotTim.getBuffers().get(i).getDistance();
                        }
                        // send road construction TIM
                        // wydotTimService.createTim(wydotTim, "eastbound", type, itisCodes);
                        createSendTims(wydotTim, "eastbound", timType);
                    } else {

                        // westbound - add buffer for point TIMs
                        if (timPoint != null)
                            wydotTim.setFromRm(timPoint + 1);

                        for (int i = 0; i < wydotTim.getBuffers().size(); i++) {
                            // westbound
                            // starts at higher milepost plus buffer distance
                            double bufferStart = Math.max(wydotTim.getToRm(), wydotTim.getFromRm())
                                    + wydotTim.getBuffers().get(i).getDistance();
                            // ends at higher milepost plus previous buffers
                            double bufferEnd = Math.max(wydotTim.getToRm(), wydotTim.getFromRm()) + bufferBefore;

                            // update start and stopping mileposts
                            WydotTim wydotTimBuffer = null;
                            try {
                                wydotTimBuffer = wydotTim.clone();
                            } catch (CloneNotSupportedException e) {
                                e.printStackTrace();
                            }
                            wydotTimBuffer.setFromRm(bufferStart);
                            wydotTimBuffer.setToRm(bufferEnd);
                            wydotTimBuffer.setAction(wydotTim.getBuffers().get(i).getAction());
                            wydotTimBuffer.setClientId(wydotTim.getClientId());

                            // send buffer tim
                            wydotTimBuffer.setAdvisory(wydotTimService.setBufferItisCodes(wydotTimBuffer.getAction()));
                            // wydotTimService.createTim(wydotTimBuffer, "westbound", type, itisCodes);
                            createSendTims(wydotTimBuffer, "westbound", timType);

                            // update running buffer distance
                            bufferBefore = wydotTim.getBuffers().get(i).getDistance();
                        }
                        // send road construction TIM
                        // wydotTimService.createTim(wydotTim, "westbound", type, itisCodes);
                        createSendTims(wydotTim, "westbound", timType);
                    }
                }
            }
        }).start();
    }

    public void makeEastboundBufferTim(WydotTim wydotTim) {

        double bufferBefore = 0;

        for (int i = 0; i < wydotTim.getBuffers().size(); i++) {
            // eastbound
            // starts at lower milepost minus the buffer distance
            double bufferStart = Math.min(wydotTim.getToRm(), wydotTim.getFromRm())
                    - wydotTim.getBuffers().get(i).getDistance();
            // ends at lower milepost minus previous buffers
            double bufferEnd = Math.min(wydotTim.getToRm(), wydotTim.getFromRm()) - bufferBefore;

            // update start and stopping mileposts
            WydotTim wydotTimBuffer = null;

            try {
                wydotTimBuffer = wydotTim.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            wydotTimBuffer.setFromRm(bufferStart);
            wydotTimBuffer.setToRm(bufferEnd);
            wydotTimBuffer.setAction(wydotTim.getBuffers().get(i).getAction());
            wydotTimBuffer.setClientId(wydotTim.getClientId());

            // send buffer tim
            wydotTimBuffer.setAdvisory(wydotTimService.setBufferItisCodes(wydotTimBuffer.getAction()));
            // wydotTimService.createTim(wydotTimBuffer, "eastbound", type, itisCodes);
            // createSendTims(wydotTimBuffer, itisCodes, "eastbound", timType);
            validTims.add(wydotTimBuffer);

            // update running buffer distance
            bufferBefore = wydotTim.getBuffers().get(i).getDistance();
        }
    }

    public void makeWestboundBufferTim(WydotTim wydotTim) {

        double bufferBefore = 0;

        for (int i = 0; i < wydotTim.getBuffers().size(); i++) {
            // westbound
            // starts at higher milepost plus buffer distance
            double bufferStart = Math.max(wydotTim.getToRm(), wydotTim.getFromRm())
                    + wydotTim.getBuffers().get(i).getDistance();
            // ends at higher milepost plus previous buffers
            double bufferEnd = Math.max(wydotTim.getToRm(), wydotTim.getFromRm()) + bufferBefore;

            // update start and stopping mileposts
            WydotTim wydotTimBuffer = null;
            try {
                wydotTimBuffer = wydotTim.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            wydotTimBuffer.setFromRm(bufferStart);
            wydotTimBuffer.setToRm(bufferEnd);
            wydotTimBuffer.setAction(wydotTim.getBuffers().get(i).getAction());
            wydotTimBuffer.setClientId(wydotTim.getClientId());

            // send buffer tim
            wydotTimBuffer.setAdvisory(wydotTimService.setBufferItisCodes(wydotTimBuffer.getAction()));
            // createSendTims(wydotTimBuffer, itisCodes, "westbound", timType);
            validTims.add(wydotTimBuffer);
            // update running buffer distance
            bufferBefore = wydotTim.getBuffers().get(i).getDistance();
        }
    }

    @RequestMapping(value = "/rw-tim/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteRoadContructionTim(@PathVariable String id) {

        // clear TIM
        wydotTimService.clearTimsById(type, id);

        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value = "/rw-tim/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    public Collection<ActiveTim> getRoadContructionTimById(@PathVariable String id) {

        // get tims
        List<ActiveTim> activeTims = wydotTimService.selectTimByClientId(type, id);
        return activeTims;
    }

    @RequestMapping(value = "/rw-tim/itis-codes/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    public Collection<ActiveTim> getRoadContructionTimByIdWithItisCodes(@PathVariable String id) {

        // get tims
        List<ActiveTim> activeTims = wydotTimService.selectTimByClientId(type, id);

        // add ITIS codes to TIMs
        for (ActiveTim activeTim : activeTims) {
            ActiveTimService.addItisCodesToActiveTim(activeTim);
        }

        return activeTims;
    }

    @RequestMapping(value = "/rw-tim", method = RequestMethod.GET, headers = "Accept=application/json")
    public Collection<ActiveTim> getRoadContructionTim() {

        // get tims
        List<ActiveTim> activeTims = wydotTimService.selectTimsByType(type);

        return activeTims;
    }

}
