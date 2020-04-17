package com.trihydro.odewrapper.controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.trihydro.library.model.TimType;
import com.trihydro.library.model.WydotTim;
import com.trihydro.library.model.WydotTravelerInputData;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.helpers.SetItisCodes;
import com.trihydro.odewrapper.model.Buffer;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.WydotTimIncident;
import com.trihydro.odewrapper.model.WydotTimParking;
import com.trihydro.odewrapper.model.WydotTimRc;
import com.trihydro.odewrapper.model.WydotTimRw;
import com.trihydro.odewrapper.model.WydotTimVsl;
import com.trihydro.odewrapper.service.WydotTimService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public abstract class WydotTimBaseController {

    protected static BasicConfiguration configuration;
    protected WydotTimService wydotTimService;
    protected TimTypeService timTypeService;
    private TimType timType = null;
    private SetItisCodes setItisCodes;
    protected ActiveTimService activeTimService;
    protected RestTemplateProvider restTemplateProvider;

    private List<String> routes = new ArrayList<>();

    public WydotTimBaseController(BasicConfiguration _basicConfiguration, WydotTimService _wydotTimService,
            TimTypeService _timTypeService, SetItisCodes _setItisCodes, ActiveTimService _activeTimService,
            RestTemplateProvider _restTemplateProvider) {
        configuration = _basicConfiguration;
        wydotTimService = _wydotTimService;
        timTypeService = _timTypeService;
        setItisCodes = _setItisCodes;
        activeTimService = _activeTimService;
        restTemplateProvider = _restTemplateProvider;
    }

    protected static Gson gson = new Gson();
    private List<TimType> timTypes;

    protected ControllerResult validateInputParking(WydotTimParking tim) {

        ControllerResult result = new ControllerResult();
        List<String> resultMessages = new ArrayList<String>();

        // get route number
        if (tim.getDirection() != null)
            result.setDirection(tim.getDirection());

        result.setClientId(tim.getClientId());

        if (tim.getRoute() == null || !routeSupported(tim.getRoute())) {
            resultMessages.add("route not supported");
        } else {
            result.setRoute(tim.getRoute());
        }
        // if direction is not i/d/b fail
        if (!tim.getDirection().toLowerCase().equals("i") && !tim.getDirection().toLowerCase().equals("d")
                && !tim.getDirection().toLowerCase().equals("b")) {
            resultMessages.add("direction not supported");
        }
        if (tim.getMileMarker() != null && tim.getMileMarker() < 0) {
            resultMessages.add("Invalid milemarker");
        }
        if (tim.getMileMarker() == null) {
            resultMessages.add("Null value for mileMarker");
        }
        if (tim.getClientId() == null) {
            resultMessages.add("Null value for clientId");
        }

        // set itis codes
        List<String> itisCodes = setItisCodes.setItisCodesParking(tim);
        if (itisCodes.size() == 0)
            resultMessages.add("No ITIS codes found");
        result.setItisCodes(itisCodes);
        tim.setItisCodes(itisCodes);

        result.setResultMessages(resultMessages);
        return result;
    }

    public ControllerResult validateInputIncident(WydotTimIncident tim) {

        ControllerResult result = new ControllerResult();
        List<String> resultMessages = new ArrayList<String>();

        // get route number
        if (tim.getDirection() != null)
            result.setDirection(tim.getDirection());
        if (tim.getIncidentId() != null) {
            result.setClientId(tim.getIncidentId());
            tim.setClientId(tim.getIncidentId());
        }

        if (tim.getHighway() == null || !routeSupported(tim.getHighway())) {
            resultMessages.add("route not supported");
        } else {
            tim.setRoute(tim.getHighway());
            result.setRoute(tim.getHighway());
        }

        // if direction is not i/d/b fail
        if (!tim.getDirection().toLowerCase().equals("i") && !tim.getDirection().toLowerCase().equals("d")
                && !tim.getDirection().toLowerCase().equals("b")) {
            resultMessages.add("direction not supported");
        }
        if (tim.getIncidentId() == null) {
            resultMessages.add("Null value for incidentId");
        }
        if (tim.getStartPoint() == null || !tim.getStartPoint().isValid()) {
            resultMessages.add("Invalid startPoint");
        }
        if (tim.getEndPoint() == null || !tim.getEndPoint().isValid()) {
            resultMessages.add("Invalid endPoint");
        }

        // set itis codes
        List<String> itisCodes = setItisCodes.setItisCodesIncident(tim);
        if (itisCodes.size() == 0)
            resultMessages.add("No ITIS codes found");
        result.setItisCodes(itisCodes);
        tim.setItisCodes(itisCodes);

        result.setResultMessages(resultMessages);
        return result;
    }

    protected ControllerResult validateInputRw(WydotTimRw tim) {

        ControllerResult result = new ControllerResult();
        List<String> resultMessages = new ArrayList<String>();

        // get route number
        if (tim.getDirection() != null)
            result.setDirection(tim.getDirection());
        if (tim.getId() != null) {
            result.setClientId(tim.getId());
            tim.setClientId(tim.getId());
        }

        if (tim.getHighway() == null || !routeSupported(tim.getHighway())) {
            resultMessages.add("route not supported");
        } else {
            tim.setRoute(tim.getHighway());
            result.setRoute(tim.getHighway());
        }

        // if direction is not i/d/b fail
        if (!tim.getDirection().toLowerCase().equals("i") && !tim.getDirection().toLowerCase().equals("d")
                && !tim.getDirection().toLowerCase().equals("b")) {
            resultMessages.add("direction not supported");
        }
        if (tim.getStartPoint() == null || !tim.getStartPoint().isValid()) {
            resultMessages.add("Invalid startPoint");
        }
        if (tim.getEndPoint() == null || !tim.getEndPoint().isValid()) {
            resultMessages.add("Invalid endPoint");
        }
        if (tim.getHighway() == null) {
            resultMessages.add("Null value for highway");
        }
        if (tim.getId() == null) {
            resultMessages.add("Null value for id");
        }
        if (tim.getDirection() == null) {
            resultMessages.add("Null value for direction");
        }
        if (tim.getSchedStart() == null) {
            resultMessages.add("Null value for schedStart");
        } else {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date convertedDate = dateFormat.parse(tim.getSchedStart());
                tim.setSchedStart(convertedDate.toInstant().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (tim.getSchedEnd() != null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date convertedDate = dateFormat.parse(tim.getSchedEnd());
                tim.setSchedEnd(convertedDate.toInstant().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (tim.getBuffers() != null) {
            for (Buffer buffer : tim.getBuffers()) {
                if (buffer.getDistance() == null) {
                    resultMessages.add("Null value for buffer distance");
                }
                if (buffer.getDistance() != null && buffer.getDistance() < 0) {
                    resultMessages.add("Invalid value for buffer distance");
                }
                if (buffer.getAction() == null) {
                    resultMessages.add("Null value for buffer action");
                }
                if (buffer.getAction() != null && !isValidAction(buffer.getAction())) {
                    resultMessages.add("Unsupport value for buffer action");
                }
            }
        }

        // ensure projectKey was supplied
        if (tim.getProjectKey() == null) {
            resultMessages.add("Project Key must be supplied");
        }

        // set itis codes
        List<String> itisCodes = setItisCodes.setItisCodesRw(tim);
        if (itisCodes.size() == 0)
            resultMessages.add("No ITIS codes found");
        result.setItisCodes(itisCodes);
        tim.setItisCodes(itisCodes);

        result.setResultMessages(resultMessages);
        return result;
    }

    public boolean routeSupported(String route) {
        // call out to REST service to get all routes once, then use that
        if (routes.size() == 0) {
            String url = String.format("%s/routes", configuration.getCvRestService());
            ResponseEntity<String[]> response = restTemplateProvider.GetRestTemplate().getForEntity(url,
                    String[].class);
            routes = Arrays.asList(response.getBody());
        }
        return routes.contains(route);
    }

    protected ControllerResult validateInputRc(WydotTimRc tim) {

        ControllerResult result = new ControllerResult();
        List<String> resultMessages = new ArrayList<String>();

        // get route number
        if (tim.getDirection() != null)
            result.setDirection(tim.getDirection());

        if (tim.getRoute() == null || !routeSupported(tim.getRoute())) {
            resultMessages.add("route not supported");
        } else {
            result.setRoute(tim.getRoute());
        }
        // if direction is not i/d/b fail
        if (!tim.getDirection().toLowerCase().equals("i") && !tim.getDirection().toLowerCase().equals("d")
                && !tim.getDirection().toLowerCase().equals("b")) {
            resultMessages.add("direction not supported");
        }
        if (tim.getStartPoint() == null || !tim.getStartPoint().isValid()) {
            resultMessages.add("Invalid startPoint");
        }
        if (tim.getEndPoint() == null || !tim.getEndPoint().isValid()) {
            resultMessages.add("Invalid endPoint");
        }
        if (tim.getRoute() == null) {
            resultMessages.add("Null value for route");
        }
        if (tim.getDirection() == null) {
            resultMessages.add("Null value for direction");
        }
        if (tim.getRoadCode() == null) {
            resultMessages.add("Null value for roadCode");
        } else {
            tim.setClientId(tim.getRoadCode());
        }

        // set itis codes
        List<String> itisCodes = setItisCodes.setItisCodesRc(tim);
        if (itisCodes.size() == 0)
            resultMessages.add("No ITIS codes found");
        result.setItisCodes(itisCodes);
        tim.setItisCodes(itisCodes);

        result.setResultMessages(resultMessages);
        return result;
    }

    protected ControllerResult validateInputVsl(WydotTimVsl tim) {

        ControllerResult result = new ControllerResult();
        List<String> resultMessages = new ArrayList<String>();

        // get route number
        if (tim.getDirection() != null)
            result.setDirection(tim.getDirection());

        if (tim.getRoute() == null || !routeSupported(tim.getRoute())) {
            resultMessages.add("route not supported");
        } else {
            result.setRoute(tim.getRoute());
        }

        // if direction is not i/d/b fail
        if (!tim.getDirection().toLowerCase().equals("i") && !tim.getDirection().toLowerCase().equals("d")
                && !tim.getDirection().toLowerCase().equals("b")) {
            resultMessages.add("direction not supported");
        }
        if (tim.getStartPoint() == null || !tim.getStartPoint().isValid()) {
            resultMessages.add("Invalid startPoint");
        }
        if (tim.getEndPoint() == null || !tim.getEndPoint().isValid()) {
            resultMessages.add("Invalid endPoint");
        }
        if (tim.getRoute() == null) {
            resultMessages.add("Null value for route");
        }
        if (tim.getDirection() == null) {
            resultMessages.add("Null value for direction");
        }
        if (tim.getDeviceId() == null) {
            resultMessages.add("Null value for deviceId");
        } else {
            tim.setClientId(tim.getDeviceId());
        }

        // set itis codes
        List<String> itisCodes = setItisCodes.setItisCodesVsl(tim);
        if (itisCodes.size() == 0)
            resultMessages.add("No ITIS codes found");
        result.setItisCodes(itisCodes);
        tim.setItisCodes(itisCodes);

        result.setResultMessages(resultMessages);
        return result;
    }

    protected ControllerResult validateInputCc(WydotTimRc tim) {

        ControllerResult result = new ControllerResult();
        List<String> resultMessages = new ArrayList<String>();

        // get route number
        if (tim.getDirection() != null)
            result.setDirection(tim.getDirection());

        if (tim.getSegment() != null) {
            result.setClientId(tim.getSegment());
            tim.setClientId(tim.getSegment());
        } else {
            resultMessages.add("Null value for segment");
        }

        if (tim.getRoute() == null || !routeSupported(tim.getRoute())) {
            resultMessages.add("route not supported");
        } else {
            result.setRoute(tim.getRoute());
        }

        // if direction is not i/d/b fail
        if (!tim.getDirection().toLowerCase().equals("i") && !tim.getDirection().toLowerCase().equals("d")
                && !tim.getDirection().toLowerCase().equals("b")) {
            resultMessages.add("direction not supported");
        }
        if (tim.getStartPoint() == null || !tim.getStartPoint().isValid()) {
            resultMessages.add("Invalid startPoint");
        }
        if (tim.getEndPoint() == null || !tim.getEndPoint().isValid()) {
            resultMessages.add("Invalid endPoint");
        }
        if (tim.getRoute() == null) {
            resultMessages.add("Null value for route");
        }
        if (tim.getSegment() == null) {
            resultMessages.add("Null value for segment");
        }
        if (tim.getDirection() == null) {
            resultMessages.add("Null value for direction");
        }

        // set itis codes
        List<String> itisCodes = setItisCodes.setItisCodesFromAdvisoryArray(tim);
        if (itisCodes.size() == 0)
            resultMessages.add("No ITIS codes found");
        result.setItisCodes(itisCodes);
        tim.setItisCodes(itisCodes);

        result.setResultMessages(resultMessages);
        return result;
    }

    public boolean isValidAction(String action) {

        if (action.equals("leftClosed")) {
            return true;
        } else if (action.equals("rightClosed")) {
            return true;
        } else if (action.equals("workers")) {
            return true;
        } else if (action.equals("surfaceGravel")) {
            return true;
        } else if (action.equals("surfaceMilled")) {
            return true;
        } else if (action.equals("surfaceDirt")) {
            return true;
        } else if (action.contains("delay_")) {
            String[] actionSplit = action.split("_");
            if (actionSplit.length < 2)
                return false;
            if (!actionSplit[0].equals("delay"))
                return false;
            if (!StringUtils.isNumeric(actionSplit[1]))
                return false;
            if (Float.parseFloat(actionSplit[1]) < 0)
                return false;
            return true;
        } else if (action.equals("prepareStop")) {
            return true;
        } else if (action.contains("reduceSpeed_")) {
            String[] actionSplit = action.split("_");
            if (actionSplit.length < 2)
                return false;
            if (!actionSplit[0].equals("reduceSpeed"))
                return false;
            if (!StringUtils.isNumeric(actionSplit[1]))
                return false;
            if (Float.parseFloat(actionSplit[1]) < 0)
                return false;
            return true;
        }
        return false;
    }

    public void processRequest(WydotTim wydotTim, TimType timType, String startDateTime, String endDateTime,
            Integer pk) {

        if (wydotTim.getDirection().equalsIgnoreCase("b")) {
            // i
            createSendTims(wydotTim, "I", timType, startDateTime, endDateTime, pk);
            // d
            createSendTims(wydotTim, "D", timType, startDateTime, endDateTime, pk);
        } else {
            createSendTims(wydotTim, wydotTim.getDirection(), timType, startDateTime, endDateTime, pk);
        }
    }

    public TimType getTimType(String timTypeName) {

        if (timType != null && timType.getType() == timTypeName) {
            return timType;
        } else {
            // get tim type
            timType = getTimTypes().stream().filter(x -> x.getType().equals(timTypeName)).findFirst().orElse(null);

            return timType;
        }
    }

    public List<TimType> getTimTypes() {
        if (timTypes != null)
            return timTypes;
        else {
            timTypes = timTypeService.selectAll();
            return timTypes;
        }
    }

    // creates a TIM and sends it to RSUs and Satellite
    protected void createSendTims(WydotTim wydotTim, String direction, TimType timType, String startDateTime,
            String endDateTime, Integer pk) {
        // create TIM
        WydotTravelerInputData timToSend = wydotTimService.createTim(wydotTim, direction, timType.getType(),
                startDateTime, endDateTime);
        String regionNamePrev = direction + "_" + wydotTim.getRoute();

        if (Arrays.asList(configuration.getRsuRoutes()).contains(wydotTim.getRoute())) {
            // send TIM to RSUs
            wydotTimService.sendTimToRsus(wydotTim, timToSend, regionNamePrev, direction, timType, pk, endDateTime);
        }
        // send TIM to SDW
        // remove rsus from TIM
        timToSend.getRequest().setRsus(null);
        wydotTimService.sendTimToSDW(wydotTim, timToSend, regionNamePrev, direction, timType, pk);
    }
}