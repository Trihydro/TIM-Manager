package com.trihydro.odewrapper.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.model.Buffer;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.TimRwList;
import com.trihydro.odewrapper.model.WydotTimRw;
import com.trihydro.odewrapper.service.WydotTimService;

import org.apache.commons.lang3.StringUtils;
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
@Api(description = "Road Construction")
public class WydotTimRwController extends WydotTimBaseController {

    private static String type = "RW";
    List<WydotTimRw> timsToSend;

    @Autowired
    public WydotTimRwController(BasicConfiguration _basicConfiguration, WydotTimService _wydotTimService,
            TimTypeService _timTypeService) {
        super(_basicConfiguration, _wydotTimService, _timTypeService);
    }

    @RequestMapping(value = "/rw-tim", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createRoadContructionTim(@RequestBody TimRwList timRwList) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

        System.out.println(dateFormat.format(date) + " - Create/Update RW TIM");
        String post = gson.toJson(timRwList);
        System.out.println(post.toString());

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;
        timsToSend = new ArrayList<WydotTimRw>();

        // build TIM
        for (WydotTimRw wydotTim : timRwList.getTimRwList()) {

            // validate input
            resultTim = validateInputRw(wydotTim);

            // if there are invalidation messages skip to next TIM
            if (resultTim.getResultMessages().size() > 0) {
                resultList.add(resultTim);
                continue;
            }

            // if valid

            // sort buffers by distance
            if (wydotTim.getBuffers() != null)
                wydotTim.getBuffers().sort(Comparator.comparingDouble(Buffer::getDistance));

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
            else if (wydotTim.getDirection().equals("eastbound"))
                makeEastboundTims(wydotTim, timPoint);
            else
                makeWestboundTims(wydotTim, timPoint);

            // compile result messages for user
            resultTim.getResultMessages().add("success");
            resultList.add(resultTim);
        }

        processRequestAsync();

        String responseMessage = gson.toJson(resultList);
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    public void makeEastboundTims(WydotTimRw wydotTim, Double timPoint) {

        // eastbound - add buffer for point TIMs
        WydotTimRw timOneWay = null;

        try {
            timOneWay = wydotTim.clone();
            if (StringUtils.isBlank(timOneWay.getSchedStart())) {
                String startTime = java.time.Clock.systemUTC().instant().toString();
                timOneWay.setSchedStart(startTime);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        if (timPoint != null)
            timOneWay.setFromRm(timPoint - 1);

        timOneWay.setDirection("eastbound");
        timsToSend.add(timOneWay);

        if (timOneWay.getBuffers() != null)
            makeEastboundBufferTim(timOneWay);
    }

    public void makeWestboundTims(WydotTimRw wydotTim, Double timPoint) {

        // westbound - add buffer for point TIMs

        WydotTimRw timOneWay = null;

        try {
            timOneWay = wydotTim.clone();
            if (StringUtils.isBlank(timOneWay.getSchedStart())) {
                String startTime = java.time.Clock.systemUTC().instant().toString();
                timOneWay.setSchedStart(startTime);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        if (timPoint != null)
            timOneWay.setFromRm(timPoint + 1);

        timOneWay.setDirection("westbound");
        timsToSend.add(timOneWay);
        if (timOneWay.getBuffers() != null)
            makeWestboundBufferTim(timOneWay);
    }

    public void makeEastboundBufferTim(WydotTimRw wydotTim) {

        double bufferBefore = 0;

        for (int i = 0; i < wydotTim.getBuffers().size(); i++) {
            // eastbound
            // starts at lower milepost minus the buffer distance
            double bufferStart = Math.min(wydotTim.getToRm(), wydotTim.getFromRm())
                    - wydotTim.getBuffers().get(i).getDistance() - bufferBefore;
            // ends at lower milepost minus previous buffers
            double bufferEnd = Math.min(wydotTim.getToRm(), wydotTim.getFromRm()) - bufferBefore;

            // update start and stopping mileposts
            WydotTimRw wydotTimBuffer = null;

            try {
                wydotTimBuffer = wydotTim.clone();
                if (StringUtils.isBlank(wydotTimBuffer.getSchedStart())) {
                    String startTime = java.time.Clock.systemUTC().instant().toString();
                    wydotTimBuffer.setSchedStart(startTime);
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            wydotTimBuffer.setFromRm(bufferStart);
            wydotTimBuffer.setToRm(bufferEnd);
            wydotTimBuffer.setAction(wydotTim.getBuffers().get(i).getAction());
            wydotTimBuffer.setClientId(wydotTim.getClientId() + "%BUFF" + Integer.toString((int) bufferBefore));

            // send buffer tim
            wydotTimBuffer.setAdvisory(wydotTimService.setBufferItisCodes(wydotTimBuffer.getAction()));
            List<String> tempList = new ArrayList<String>(wydotTimBuffer.getAdvisory().length);
            for (Integer code : wydotTimBuffer.getAdvisory()) {
                tempList.add(code.toString());
            }
            wydotTimBuffer.setItisCodes(tempList);
            timsToSend.add(wydotTimBuffer);

            // update running buffer distance
            bufferBefore = wydotTim.getBuffers().get(i).getDistance();
        }
    }

    public void processRequestAsync() {
        // An Async task always executes in new thread
        new Thread(new Runnable() {
            public void run() {
                for (WydotTimRw tim : timsToSend) {
                    processRequest(tim, getTimType(type), tim.getSchedStart(), tim.getSchedEnd(), null);
                }
            }
        }).start();
    }

    public void makeWestboundBufferTim(WydotTimRw wydotTim) {

        double bufferBefore = 0;

        for (int i = 0; i < wydotTim.getBuffers().size(); i++) {
            // westbound
            // starts at higher milepost plus buffer distance
            double bufferStart = Math.max(wydotTim.getToRm(), wydotTim.getFromRm())
                    + wydotTim.getBuffers().get(i).getDistance() + bufferBefore;
            // ends at higher milepost plus previous buffers
            double bufferEnd = Math.max(wydotTim.getToRm(), wydotTim.getFromRm()) + bufferBefore;

            // update start and stopping mileposts
            WydotTimRw wydotTimBuffer = null;
            try {
                wydotTimBuffer = wydotTim.clone();
                if (StringUtils.isBlank(wydotTimBuffer.getSchedStart())) {
                    String startTime = java.time.Clock.systemUTC().instant().toString();
                    wydotTimBuffer.setSchedStart(startTime);
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            wydotTimBuffer.setFromRm(bufferStart);
            wydotTimBuffer.setToRm(bufferEnd);
            wydotTimBuffer.setAction(wydotTim.getBuffers().get(i).getAction());
            wydotTimBuffer.setClientId(wydotTim.getClientId() + "%BUFF" + Integer.toString((int) bufferBefore));

            // send buffer tim
            wydotTimBuffer.setAdvisory(wydotTimService.setBufferItisCodes(wydotTimBuffer.getAction()));
            List<String> tempList = new ArrayList<String>(wydotTimBuffer.getAdvisory().length);
            for (Integer code : wydotTimBuffer.getAdvisory()) {
                tempList.add(code.toString());
            }
            wydotTimBuffer.setItisCodes(tempList);
            timsToSend.add(wydotTimBuffer);

            // update running buffer distance
            bufferBefore = wydotTim.getBuffers().get(i).getDistance();
        }
    }

    @RequestMapping(value = "/rw-tim/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteRoadContructionTim(@PathVariable String id) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

        System.out.println(dateFormat.format(date) + " - Delete RW TIM");
        // clear TIM
        wydotTimService.clearTimsById(type, id, null);

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
    public Collection<ActiveTim> getRoadConstructionTim() {

        // get tims
        List<ActiveTim> activeTims = wydotTimService.selectTimsByType(type);

        return activeTims;
    }

}
