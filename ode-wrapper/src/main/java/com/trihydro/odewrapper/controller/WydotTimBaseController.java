package com.trihydro.odewrapper.controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.trihydro.library.helpers.MilepostReduction;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.Buffer;
import com.trihydro.library.model.ContentEnum;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.TimType;
import com.trihydro.library.model.WydotTim;
import com.trihydro.library.model.WydotTimRw;
import com.trihydro.library.model.WydotTravelerInputData;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.library.service.WydotTimService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.helpers.SetItisCodes;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.IdGenerator;
import com.trihydro.odewrapper.model.WydotTimIncident;
import com.trihydro.odewrapper.model.WydotTimParking;
import com.trihydro.odewrapper.model.WydotTimRc;
import com.trihydro.odewrapper.model.WydotTimVsl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.plugin.j2735.timstorage.FrameType.TravelerInfoType;

@Component
public abstract class WydotTimBaseController {

    protected static BasicConfiguration configuration;
    protected WydotTimService wydotTimService;
    protected TimTypeService timTypeService;
    private TimType timType = null;
    private SetItisCodes setItisCodes;
    protected ActiveTimService activeTimService;
    protected RestTemplateProvider restTemplateProvider;
    MilepostReduction milepostReduction;
    protected Utility utility;

    private List<String> routes = new ArrayList<>();
    protected static Gson gson = new Gson();
    private List<TimType> timTypes;

    public WydotTimBaseController(BasicConfiguration _basicConfiguration, WydotTimService _wydotTimService,
            TimTypeService _timTypeService, SetItisCodes _setItisCodes, ActiveTimService _activeTimService,
            RestTemplateProvider _restTemplateProvider, MilepostReduction _milepostReduction, Utility _utility) {
        configuration = _basicConfiguration;
        wydotTimService = _wydotTimService;
        timTypeService = _timTypeService;
        setItisCodes = _setItisCodes;
        activeTimService = _activeTimService;
        restTemplateProvider = _restTemplateProvider;
        milepostReduction = _milepostReduction;
        utility = _utility;
    }

    protected String getStartTime() {
        Date date = new Date();
        return getIsoDateTimeString(date);
    }

    protected String getIsoDateTimeString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

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
        if (!tim.getDirection().equalsIgnoreCase("i") && !tim.getDirection().equalsIgnoreCase("d")
                && !tim.getDirection().equalsIgnoreCase("b")) {
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
        if (!tim.getDirection().equalsIgnoreCase("i") && !tim.getDirection().equalsIgnoreCase("d")
                && !tim.getDirection().equalsIgnoreCase("b")) {
            resultMessages.add("direction not supported");
        }
        if (tim.getIncidentId() == null) {
            resultMessages.add("Null value for incidentId");
        }
        if (tim.getStartPoint() == null || !tim.getStartPoint().isValid()) {
            resultMessages.add("Invalid startPoint");
        }
        // endPoint may be null here, so check if not null that it is valid
        if (tim.getEndPoint() != null && !tim.getEndPoint().isValid()) {
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
        if (!tim.getDirection().equalsIgnoreCase("i") && !tim.getDirection().equalsIgnoreCase("d")
                && !tim.getDirection().equalsIgnoreCase("b")) {
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
                tim.setSchedStart(getIsoDateTimeString(convertedDate));
            } catch (ParseException e) {
                resultMessages.add("Bad value supplied for schedStart. Should follow the format: yyyy-MM-dd");
                e.printStackTrace();
            }
        }
        if (tim.getSchedEnd() != null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date convertedDate = dateFormat.parse(tim.getSchedEnd());
                tim.setSchedEnd(getIsoDateTimeString(convertedDate));
            } catch (ParseException e) {
                resultMessages.add("Bad value supplied for schedEnd. Should follow the format: yyyy-MM-dd");
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
        if (tim.getDirection() != null && !tim.getDirection().equalsIgnoreCase("i")
                && !tim.getDirection().equalsIgnoreCase("d") && !tim.getDirection().equalsIgnoreCase("b")) {
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
        if (!tim.getDirection().equalsIgnoreCase("i") && !tim.getDirection().equalsIgnoreCase("d")
                && !tim.getDirection().equalsIgnoreCase("b")) {
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
        if (!tim.getDirection().equalsIgnoreCase("i") && !tim.getDirection().equalsIgnoreCase("d")
                && !tim.getDirection().equalsIgnoreCase("b")) {
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

    public void processRequest(WydotTim wydotTim, TimType timType, String startDateTime, String endDateTime, Integer pk,
            ContentEnum content, TravelerInfoType frameType) {

        if (wydotTim.getDirection().equalsIgnoreCase("b")) {
            var iTim = wydotTim.copy();
            var dTim = wydotTim.copy();
            iTim.setDirection("I");
            dTim.setDirection("D");
            // I
            createSendTims(iTim, timType, startDateTime, endDateTime, pk, content, frameType);
            // D
            createSendTims(dTim, timType, startDateTime, endDateTime, pk, content, frameType);
        } else {
            createSendTims(wydotTim, timType, startDateTime, endDateTime, pk, content, frameType);
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

    protected void createSendTims(WydotTim wydotTim, TimType timType, String startDateTime, String endDateTime,
            Integer pk, ContentEnum content, TravelerInfoType frameType) {
        // Clear any existing TIMs with the same client id
        Long timTypeId = timType != null ? timType.getTimTypeId() : null;
        var existingTims = activeTimService.getActiveTimsByClientIdDirection(wydotTim.getClientId(), timTypeId,
                wydotTim.getDirection());
        wydotTimService.deleteTimsFromRsusAndSdx(existingTims);

        // Get mileposts that will define the TIM's region
        var milepostsAll = wydotTimService.getAllMilepostsForTim(wydotTim);
        var reducedMileposts = milepostReduction.applyMilepostReductionAlorithm(milepostsAll,
                configuration.getPathDistanceLimit());

        // don't continue if we have no mileposts
        if (milepostsAll.size() == 0) {
            utility.logWithDate("Found 0 mileposts, unable to generate TIM");
            return;
        }

        var anchor = milepostsAll.remove(0);

        createSendTims(wydotTim, timType, startDateTime, endDateTime, pk, content, frameType, milepostsAll,
                reducedMileposts, anchor, new IdGenerator());
    }

    // creates a TIM and sends it to RSUs and Satellite
    protected void createSendTims(WydotTim wydotTim, TimType timType, String startDateTime, String endDateTime,
            Integer pk, ContentEnum content, TravelerInfoType frameType, List<Milepost> allMileposts,
            List<Milepost> reducedMileposts, Milepost anchor, IdGenerator idGenerator) {

        if (reducedMileposts.size() > 63) {
            // Even after reducing the mileposts, this TIM requires more nodes than J2735
            // allows. Split this TIM into half. The first TIM will cover the first half of
            // reduced nodes, the second TIM will cover the second.
            var firstTim = wydotTim.copy();
            var secondTim = wydotTim.copy();

            // Note that the last milepost for firstTim is the same as the first milepost
            // for secondTim. This ensures the first ends where the second begins, without
            // any gap in coverage.
            var firstStartMp = reducedMileposts.get(0);
            var firstEndMp = reducedMileposts.get((reducedMileposts.size() / 2));
            var secondStartMp = reducedMileposts.get(reducedMileposts.size() / 2);
            var secondEndMp = reducedMileposts.get(reducedMileposts.size() - 1);

            firstTim.setStartPoint(new Coordinate(firstStartMp.getLatitude(), firstStartMp.getLongitude()));
            firstTim.setEndPoint(new Coordinate(firstEndMp.getLatitude(), firstEndMp.getLongitude()));
            secondTim.setStartPoint(new Coordinate(secondStartMp.getLatitude(), secondStartMp.getLongitude()));
            secondTim.setEndPoint(new Coordinate(secondEndMp.getLatitude(), secondEndMp.getLongitude()));

            // The anchor point for the second TIM should be the milepost immediately before
            // the start point. If we were to pull the anchor point from the reducedMilepost
            // set, it may be much further down the road, which isn't what we want.
            var secondStartIndex = allMileposts.indexOf(secondStartMp);
            var secondAnchor = allMileposts.get(secondStartIndex - 1);

            createSendTims(firstTim, timType, startDateTime, endDateTime, pk, content, frameType, allMileposts,
                    reducedMileposts.subList(0, (reducedMileposts.size() / 2) + 1), anchor, idGenerator);
            createSendTims(secondTim, timType, startDateTime, endDateTime, pk, content, frameType, allMileposts,
                    reducedMileposts.subList(reducedMileposts.size() / 2, reducedMileposts.size()), secondAnchor,
                    idGenerator);

            return;
        }

        wydotTim.setClientId(wydotTim.getClientId() + "-" + idGenerator.getNextId());

        // create TIM
        WydotTravelerInputData timToSend = wydotTimService.createTim(wydotTim, timType.getType(), startDateTime,
                endDateTime, content, frameType, allMileposts, reducedMileposts, anchor);

        if (timToSend == null) {
            return;
        }

        String regionNamePrev = wydotTim.getDirection() + "_" + wydotTim.getRoute();

        var endPoint = new Coordinate();
        if (wydotTim.getEndPoint() != null) {
            endPoint = wydotTim.getEndPoint();
        } else {
            var endMp = timToSend.getMileposts().get(timToSend.getMileposts().size() - 1);
            endPoint = new Coordinate(endMp.getLatitude(), endMp.getLongitude());
        }

        if (Arrays.asList(configuration.getRsuRoutes()).contains(wydotTim.getRoute())) {
            // send TIM to RSUs
            wydotTimService.sendTimToRsus(wydotTim, timToSend, regionNamePrev, timType, pk, endDateTime, endPoint);
        }
        // send TIM to SDW
        // remove rsus from TIM
        timToSend.getRequest().setRsus(null);
        wydotTimService.sendTimToSDW(wydotTim, timToSend, regionNamePrev, timType, pk, endPoint);
    }
}