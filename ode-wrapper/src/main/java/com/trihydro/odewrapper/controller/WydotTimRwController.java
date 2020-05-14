package com.trihydro.odewrapper.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ContentEnum;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.helpers.SetItisCodes;
import com.trihydro.odewrapper.model.Buffer;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.TimRwList;
import com.trihydro.odewrapper.model.WydotTimRw;
import com.trihydro.odewrapper.service.WydotTimService;

import org.apache.commons.lang3.StringUtils;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalCoordinates;
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

    private final String type = "RW";
    List<WydotTimRw> timsToSend;

    @Autowired
    public WydotTimRwController(BasicConfiguration _basicConfiguration, WydotTimService _wydotTimService,
            TimTypeService _timTypeService, SetItisCodes _setItisCodes, ActiveTimService _activeTimService,
            RestTemplateProvider _restTemplateProvider) {
        super(_basicConfiguration, _wydotTimService, _timTypeService, _setItisCodes, _activeTimService,
                _restTemplateProvider);
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

            // if bi-directional
            if (wydotTim.getDirection().equals("b")) {
                // make i TIMs
                makeIncreasingTims(wydotTim);
                // make d TIMs
                makeDecreasingTims(wydotTim);
            }
            // else make one direction TIMs
            else if (wydotTim.getDirection().equals("i"))
                makeIncreasingTims(wydotTim);
            else
                makeDecreasingTims(wydotTim);

            // compile result messages for user
            resultTim.getResultMessages().add("success");
            resultList.add(resultTim);
        }

        processRequestAsync();

        String responseMessage = gson.toJson(resultList);
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    public void makeIncreasingTims(WydotTimRw wydotTim) {

        // i - add buffer for point TIMs
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

        timOneWay.setDirection("i");
        timsToSend.add(timOneWay);

        if (timOneWay.getBuffers() != null)
            makeIncreasingBufferTim(timOneWay);
    }

    public void makeDecreasingTims(WydotTimRw wydotTim) {

        // d - add buffer for point TIMs

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

        timOneWay.setDirection("d");
        timsToSend.add(timOneWay);
        if (timOneWay.getBuffers() != null)
            makeDecreasingBufferTim(timOneWay);
    }

    private double getIBearingForRoute(String route) {
        // TODO: this needs to call out to the DIRECTION_EXCEPTION view and compare
        // mileage to determine direction
        Integer numericRoute = Integer.parseInt(route.replaceAll("\\D+", ""));
        if (numericRoute % 2 == 0) {
            return 270;
        }
        return 180;
    }

    private double getDBearingForRoute(String route) {
        // TODO: this needs to call out to the DIRECTION_EXCEPTION view and compare
        // mileage to determine direction
        Integer numericRoute = Integer.parseInt(route.replaceAll("\\D+", ""));
        if (numericRoute % 2 == 0) {
            return 90;
        }
        return 0;
    }

    public void makeIncreasingBufferTim(WydotTimRw wydotTim) {

        double bufferBefore = 0.000;

        Ellipsoid reference = Ellipsoid.WGS84;
        GlobalCoordinates startCoordinates = new GlobalCoordinates(wydotTim.getStartPoint().getLatitude(),
                wydotTim.getStartPoint().getLongitude());
        GlobalCoordinates nextCoordinates = null;
        double bearing = getIBearingForRoute(wydotTim.getRoute());
        GeodeticCalculator calculator = new GeodeticCalculator();

        for (int i = 0; i < wydotTim.getBuffers().size(); i++) {
            // i
            // starts at lower milepost minus the buffer distance
            nextCoordinates = calculator.calculateEndingGlobalCoordinates(reference, startCoordinates, bearing,
                    wydotTim.getBuffers().get(i).getDistanceMeters());

            // update start and stopping points
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
            wydotTimBuffer.setStartPoint(new Coordinate(nextCoordinates.getLatitude(), nextCoordinates.getLongitude()));
            wydotTimBuffer.setEndPoint(new Coordinate(startCoordinates.getLatitude(), startCoordinates.getLongitude()));
            wydotTimBuffer.setAction(wydotTim.getBuffers().get(i).getAction());
            wydotTimBuffer.setClientId(wydotTim.getClientId() + "%BUFF" + Integer.toString((int) bufferBefore));

            // send buffer tim
            wydotTimBuffer.setAdvisory(wydotTimService.setBufferItisCodes(wydotTimBuffer.getAction()));
            if (wydotTimBuffer.getAdvisory() != null) {
                List<String> tempList = new ArrayList<String>(wydotTimBuffer.getAdvisory().length);
                for (Integer code : wydotTimBuffer.getAdvisory()) {
                    tempList.add(code.toString());
                }
                wydotTimBuffer.setItisCodes(tempList);
            }
            timsToSend.add(wydotTimBuffer);

            // update running buffer distance
            bufferBefore = wydotTim.getBuffers().get(i).getDistance();
            startCoordinates = nextCoordinates;
        }
    }

    public void makeDecreasingBufferTim(WydotTimRw wydotTim) {

        double bufferBefore = 0;

        Ellipsoid reference = Ellipsoid.WGS84;
        GlobalCoordinates startCoordinates = new GlobalCoordinates(wydotTim.getEndPoint().getLatitude(),
                wydotTim.getEndPoint().getLongitude());
        GlobalCoordinates nextCoordinates = null;
        double bearing = getDBearingForRoute(wydotTim.getRoute());
        GeodeticCalculator calculator = new GeodeticCalculator();

        for (int i = 0; i < wydotTim.getBuffers().size(); i++) {
            // d
            // starts at higher milepost plus buffer distance
            nextCoordinates = calculator.calculateEndingGlobalCoordinates(reference, startCoordinates, bearing,
                    wydotTim.getBuffers().get(i).getDistanceMeters());

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
            wydotTimBuffer
                    .setStartPoint(new Coordinate(startCoordinates.getLatitude(), startCoordinates.getLongitude()));
            wydotTimBuffer.setEndPoint(new Coordinate(nextCoordinates.getLatitude(), nextCoordinates.getLongitude()));
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
            startCoordinates = nextCoordinates;
        }
    }

    public void processRequestAsync() {
        // An Async task always executes in new thread
        new Thread(new Runnable() {
            public void run() {
                for (WydotTimRw tim : timsToSend) {
                    // check for speed limit, itis code 268
                    if (tim.getItisCodes() != null && tim.getItisCodes().size() == 3
                            && tim.getItisCodes().get(0) == "268") {
                        processRequest(tim, getTimType(type), tim.getSchedStart(), tim.getSchedEnd(), null,
                                ContentEnum.speedLimit);
                    } else {
                        processRequest(tim, getTimType(type), tim.getSchedStart(), tim.getSchedEnd(), null,
                                ContentEnum.advisory);
                    }
                }
            }
        }).start();

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
            activeTimService.addItisCodesToActiveTim(activeTim);
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
