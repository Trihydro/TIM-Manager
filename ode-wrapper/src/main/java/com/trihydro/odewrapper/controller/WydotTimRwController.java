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
import com.trihydro.odewrapper.model.WydotTimRw;

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
    // get tim type
    TimType timType = getTimType(type);

    @RequestMapping(value = "/rw-tim", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createRoadContructionTim(@RequestBody WydotTimList wydotTimList) {

        System.out.println("Create/Update RW TIM");
        String post = gson.toJson(wydotTimList);
        System.out.println(post.toString());

        List<ControllerResult> resultList = new ArrayList<ControllerResult>();
        ControllerResult resultTim = null;

        // build TIM
        for (WydotTimRw wydotTim : wydotTimList.getTimRwList()) {

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

            // adjust input
            wydotTim.setRoute(wydotTim.getHighway());
            System.out.println("SchedStart: " + wydotTim.getSchedStart());
            System.out.println("SchedEnd: " + wydotTim.getSchedEnd());

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

        String responseMessage = gson.toJson(resultList);
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    public void makeEastboundTims(WydotTimRw wydotTim, Double timPoint) {

        // eastbound - add buffer for point TIMs
        if (timPoint != null)
            wydotTim.setFromRm(timPoint - 1);

        // validTims.add(wydotTim);
        processRequest(wydotTim, timType, wydotTim.getSchedStart(), wydotTim.getSchedEnd(), null);
        if (wydotTim.getBuffers() != null)
            makeEastboundBufferTim(wydotTim);
    }

    public void makeWestboundTims(WydotTimRw wydotTim, Double timPoint) {

        // westbound - add buffer for point TIMs
        if (timPoint != null)
            wydotTim.setFromRm(timPoint + 1);

        // validTims.add(wydotTim);
        processRequest(wydotTim, timType, wydotTim.getSchedStart(), wydotTim.getSchedEnd(), null);
        if (wydotTim.getBuffers() != null)
            makeWestboundBufferTim(wydotTim);
    }

    public void makeEastboundBufferTim(WydotTimRw wydotTim) {

        double bufferBefore = 0;

        for (int i = 0; i < wydotTim.getBuffers().size(); i++) {
            // eastbound
            // starts at lower milepost minus the buffer distance
            double bufferStart = Math.min(wydotTim.getToRm(), wydotTim.getFromRm())
                    - wydotTim.getBuffers().get(i).getDistance();
            // ends at lower milepost minus previous buffers
            double bufferEnd = Math.min(wydotTim.getToRm(), wydotTim.getFromRm()) - bufferBefore;

            // update start and stopping mileposts
            WydotTimRw wydotTimBuffer = null;

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
            List<String> tempList = new ArrayList<String>(wydotTimBuffer.getAdvisory().length);
            for (Integer code : wydotTimBuffer.getAdvisory()) {
                tempList.add(code.toString());
            }
            wydotTimBuffer.setItisCodes(tempList);
            // validTims.add(wydotTimBuffer);
            processRequest(wydotTimBuffer, timType, wydotTimBuffer.getSchedStart(), wydotTimBuffer.getSchedEnd(), null);

            // update running buffer distance
            bufferBefore = wydotTim.getBuffers().get(i).getDistance();
        }
    }

    public void makeWestboundBufferTim(WydotTimRw wydotTim) {

        double bufferBefore = 0;

        for (int i = 0; i < wydotTim.getBuffers().size(); i++) {
            // westbound
            // starts at higher milepost plus buffer distance
            double bufferStart = Math.max(wydotTim.getToRm(), wydotTim.getFromRm())
                    + wydotTim.getBuffers().get(i).getDistance();
            // ends at higher milepost plus previous buffers
            double bufferEnd = Math.max(wydotTim.getToRm(), wydotTim.getFromRm()) + bufferBefore;

            // update start and stopping mileposts
            WydotTimRw wydotTimBuffer = null;
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
            List<String> tempList = new ArrayList<String>(wydotTimBuffer.getAdvisory().length);
            for (Integer code : wydotTimBuffer.getAdvisory()) {
                tempList.add(code.toString());
            }
            wydotTimBuffer.setItisCodes(tempList);
            // validTims.add(wydotTimBuffer);
            processRequest(wydotTimBuffer, timType, wydotTimBuffer.getSchedStart(), wydotTimBuffer.getSchedEnd(), null);

            // update running buffer distance
            bufferBefore = wydotTim.getBuffers().get(i).getDistance();
        }
    }

    @RequestMapping(value = "/rw-tim/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteRoadContructionTim(@PathVariable String id) {

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
