package com.trihydro.odewrapper.controller;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.trihydro.library.helpers.MilepostReduction;
import com.trihydro.library.helpers.TimGenerationHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.Buffer;
import com.trihydro.library.model.ContentEnum;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.CountyRoadSegment;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.TimType;
import com.trihydro.library.model.TriggerRoad;
import com.trihydro.library.model.WydotTim;
import com.trihydro.library.model.WydotTimRw;
import com.trihydro.library.model.WydotTravelerInputData;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.CascadeService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.library.service.WydotTimService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.helpers.SetItisCodes;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.IdGenerator;
import com.trihydro.odewrapper.model.WydotTimCc;
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
    protected TimGenerationHelper timGenerationHelper;
    protected CascadeService cascadeService;

    private List<String> routes = new ArrayList<>();
    protected static Gson gson = new Gson();
    private List<TimType> timTypes;

    public WydotTimBaseController(BasicConfiguration _basicConfiguration, WydotTimService _wydotTimService,
            TimTypeService _timTypeService, SetItisCodes _setItisCodes, ActiveTimService _activeTimService,
            RestTemplateProvider _restTemplateProvider, MilepostReduction _milepostReduction, Utility _utility, 
            TimGenerationHelper _timGenerationHelper, CascadeService _cascadeService) {
        configuration = _basicConfiguration;
        wydotTimService = _wydotTimService;
        timTypeService = _timTypeService;
        setItisCodes = _setItisCodes;
        activeTimService = _activeTimService;
        restTemplateProvider = _restTemplateProvider;
        milepostReduction = _milepostReduction;
        utility = _utility;
        timGenerationHelper = _timGenerationHelper;
        cascadeService = _cascadeService;
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

    protected String getIsoDateTimeString(ZonedDateTime date) {
        if (date == null) {
            return null;
        }

        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        var utcDate = date.withZoneSameInstant(ZoneOffset.UTC);
        return utcDate.format(formatter);
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
            try {
                var convertedDate = LocalDate.parse(tim.getSchedStart(), DateTimeFormatter.ISO_LOCAL_DATE);
                var startOfDay = convertedDate.atStartOfDay(ZoneId.systemDefault());
                tim.setSchedStart(getIsoDateTimeString(startOfDay));
            } catch (DateTimeParseException e) {
                resultMessages.add("Bad value supplied for schedStart. Should follow the format: yyyy-MM-dd");
                e.printStackTrace();
            }
        }
        if (tim.getSchedEnd() != null) {
            try {
                var convertedDate = LocalDate.parse(tim.getSchedEnd(), DateTimeFormatter.ISO_LOCAL_DATE);
                // LocalTime.MAX sets the time to 11:59 PM. This is especially important when
                // construction is only scheduled for a day (ex. 5-12 to 5-12)
                var endOfDay = ZonedDateTime.of(LocalDateTime.of(convertedDate, LocalTime.MAX), ZoneId.systemDefault());
                tim.setSchedEnd(getIsoDateTimeString(endOfDay));
            } catch (DateTimeParseException e) {
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

    protected ControllerResult validateRcAc(WydotTimRc allClear) {
        ControllerResult result = new ControllerResult();
        List<String> resultMessages = new ArrayList<String>();

        // All Clear must have a valid CLIENT_ID and DIRECTION
        if (StringUtils.isBlank(allClear.getRoadCode())) {
            resultMessages.add("Road Code must be supplied");
        } else {
            result.setClientId(allClear.getRoadCode());
            allClear.setClientId(allClear.getRoadCode());
        }

        if (allClear.getDirection() == null) {
            resultMessages.add("Null value for direction");
        } else if (!allClear.getDirection().equalsIgnoreCase("i") && !allClear.getDirection().equalsIgnoreCase("d")
                && !allClear.getDirection().equalsIgnoreCase("b")) {
            resultMessages.add("direction not supported");
        } else {
            result.setDirection(allClear.getDirection());
        }

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
            expireReduceCreateSendTims(iTim, timType, startDateTime, endDateTime, pk, content, frameType);
            // D
            expireReduceCreateSendTims(dTim, timType, startDateTime, endDateTime, pk, content, frameType);
        } else {
            expireReduceCreateSendTims(wydotTim, timType, startDateTime, endDateTime, pk, content, frameType);
        }

        // if tim is associated with a trigger road, cascade conditions
        TriggerRoad triggerRoad = getTriggerRoad(wydotTim);
        if (triggerRoad != null) {
            handleCascadingConditions(wydotTim, timType, startDateTime, endDateTime, pk, content, frameType, triggerRoad);
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

    protected void expireReduceCreateSendTims(WydotTim wydotTim, TimType timType, String startDateTime, String endDateTime,
            Integer pk, ContentEnum content, TravelerInfoType frameType) {
        // Clear any existing TIMs with the same client id
        Long timTypeId = timType != null ? timType.getTimTypeId() : null;
        var existingTims = activeTimService.getActiveTimsByClientIdDirection(wydotTim.getClientId(), timTypeId,
                wydotTim.getDirection());

        // Expire existing tims
        List<Long> existingTimIds = new ArrayList<Long>();
        for (ActiveTim existingTim : existingTims) {
            existingTimIds.add(existingTim.getActiveTimId());
        }
        timGenerationHelper.expireTimAndResubmitToOde(existingTimIds);
        
        // Get mileposts that will define the TIM's region
        var milepostsAll = wydotTimService.getAllMilepostsForTim(wydotTim);

        // Per J2735, NodeSetLL's must contain at least 2 nodes. ODE will fail to
        // PER-encode TIM if we supply less than 2.
        if (milepostsAll.size() < 2) {
            utility.logWithDate("Found less than 2 mileposts, unable to generate TIM.");
            return;
        }
        Milepost firstPoint = milepostsAll.get(0);
        Milepost secondPoint = milepostsAll.get(1);

        var anchor = getAnchorPoint(firstPoint, secondPoint);
        var reducedMileposts = milepostReduction.applyMilepostReductionAlgorithm(milepostsAll,
                configuration.getPathDistanceLimit());

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
            var firstEndMp = reducedMileposts.get(reducedMileposts.size() / 2);
            var secondStartMp = reducedMileposts.get(reducedMileposts.size() / 2);
            var secondEndMp = reducedMileposts.get(reducedMileposts.size() - 1);

            firstTim.setStartPoint(new Coordinate(firstStartMp.getLatitude(), firstStartMp.getLongitude()));
            firstTim.setEndPoint(new Coordinate(firstEndMp.getLatitude(), firstEndMp.getLongitude()));
            secondTim.setStartPoint(new Coordinate(secondStartMp.getLatitude(), secondStartMp.getLongitude()));
            secondTim.setEndPoint(new Coordinate(secondEndMp.getLatitude(), secondEndMp.getLongitude()));

            // The anchor point for the second TIM should be the milepost immediately before
            // the start point. If we were to pull the anchor point from the reducedMilepost
            // set, it may be much further down the road, which isn't what we want.
            var firstEndIndex = allMileposts.indexOf(firstEndMp);
            var secondStartIndex = allMileposts.indexOf(secondStartMp);

            // Get the subset of "all mileposts" that pertains to our new TIM
            // If we use the full set of mileposts we wouldn't get an accurate
            // direction/heading slice.
            // Note that .subList is inclusive of fromIndex, but exclusive of toIndex
            var firstTimAllMileposts = allMileposts.subList(0, firstEndIndex + 1);
            var secondTimAllMileposts = new ArrayList<Milepost>(
                    allMileposts.subList(secondStartIndex, allMileposts.size()));
            //.remove is removing this from ALL lists, which is inaccurate
            // var secondAnchor = secondTimAllMileposts.remove(0);
            var secondAnchor = allMileposts.get(secondStartIndex - 1);

            createSendTims(firstTim, timType, startDateTime, endDateTime, pk, content, frameType, firstTimAllMileposts,
                    reducedMileposts.subList(0, (reducedMileposts.size() / 2) + 1), anchor, idGenerator);
            createSendTims(secondTim, timType, startDateTime, endDateTime, pk, content, frameType,
                    secondTimAllMileposts,
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
            var endMp = reducedMileposts.get(reducedMileposts.size() - 1);
            endPoint = new Coordinate(endMp.getLatitude(), endMp.getLongitude());
        }

        if (Arrays.asList(configuration.getRsuRoutes()).contains(wydotTim.getRoute())) {
            // send TIM to RSUs
            wydotTimService.sendTimToRsus(wydotTim, timToSend, regionNamePrev, timType, pk, endDateTime, endPoint);
        }
        // send TIM to SDW
        // remove rsus from TIM
        timToSend.getRequest().setRsus(null);
        wydotTimService.sendTimToSDW(wydotTim, timToSend, regionNamePrev, timType, pk, endPoint, reducedMileposts);
    }

    /**
     * This method retrieves the trigger road for the given WydotTim.
     * @param wydotTim The WydotTim to retrieve the trigger road for.
     * @return The trigger road for the given WydotTim if it exists, otherwise null.
     */
    private TriggerRoad getTriggerRoad(WydotTim wydotTim) {
        // check for road_code by casting WydotTim and only attempt to retrieve TriggerRoad if road_code is present in the class
        String roadCode = null;
        if (wydotTim instanceof WydotTimCc) {
            // Model `WydotTimCc` does not have a road_code field, return
            return null;
        }
        else if (wydotTim instanceof WydotTimIncident) {
            // Model `WydotTimIncident` does not have a road_code field, return
            return null;
        }
        else if (wydotTim instanceof WydotTimParking) {
            // Model `WydotTimParking` does not have a road_code field, return
            return null;
        }
        else if (wydotTim instanceof WydotTimRc) {
            // retrieve road_code from WydotTimRc
            roadCode = ((WydotTimRc) wydotTim).getRoadCode();
        }
        else if (wydotTim instanceof WydotTimRw) {
            // Model `WydotTimRw` does not have a road_code field, return
            return null;
        }
        else if (wydotTim instanceof WydotTimVsl) {
            // Model `WydotTimVsl` does not have a road_code field, return
            return null;
        }
        else {
            utility.logWithDate("Unrecognized model type, unable to generate cascading conditions.");
            return null;
        }
        
        // retrieve trigger road
        return cascadeService.getTriggerRoad(roadCode);
    }

    /**
     * This method cascades conditions for each associated segment of the given trigger road.
     */
    protected void handleCascadingConditions(WydotTim wydotTim, TimType timType, String startDateTime, String endDateTime, Integer pk, ContentEnum content, TravelerInfoType frameType, TriggerRoad triggerRoad) {
        utility.logWithDate("=================== CRC Start (" + triggerRoad.getRoadCode() + ") ===================");
        utility.logWithDate("Handling cascading conditions for trigger road: " + triggerRoad.getRoadCode());
        List<CountyRoadSegment> countyRoadSegments = triggerRoad.getCountyRoadSegments();
        utility.logWithDate("Trigger road " + triggerRoad.getRoadCode() + " has " + countyRoadSegments.size() + " segments associated with it.");
        for (CountyRoadSegment countyRoadSegment : countyRoadSegments) {
            cascadeConditionsForSegment(countyRoadSegment, timType, startDateTime, endDateTime, pk, content, frameType, wydotTim.getClientId());
        }
        utility.logWithDate("=================== CRC End (" + triggerRoad.getRoadCode() + ") ===================");
    }

    /**
     * This method creates a new WydotTim for the given segment and sends it to RSUs and Satellite.
     */
    private void cascadeConditionsForSegment(CountyRoadSegment countyRoadSegment, TimType timType, String startDateTime, String endDateTime, Integer pk, ContentEnum content, TravelerInfoType frameType, String clientId) {
        // if existing condition is identical, return
        boolean identicalConditionExistsForSegment = performExistenceChecks(countyRoadSegment, timType, endDateTime);
        if (identicalConditionExistsForSegment) {
            utility.logWithDate("Identical condition already exists, skipping TIM generation");
            return;
        }
        
        if (!countyRoadSegment.hasOneOrMoreCondition()) {
            // no conditions associated with the segment, no need to generate any TIMs
            utility.logWithDate("No conditions associated with segment " + countyRoadSegment.getId() + ", skipping TIM generation.");
            return;
        }

        List<Milepost> cascadeMileposts = cascadeService.getMilepostsForSegment(countyRoadSegment);
        if (cascadeMileposts.size() < 2) { // Per J2735, NodeSetLL's must contain at least 2 nodes. ODE will fail to PER-encode TIM if we supply less than 2.
            utility.logWithDate("Found less than 2 mileposts while attempting to cascade condition, unable to generate TIM.");
            return;
        }
        Milepost firstPoint = cascadeMileposts.get(0);
        Milepost secondPoint = cascadeMileposts.get(1);
        var anchor = getAnchorPoint(firstPoint, secondPoint);
        var reducedMileposts = milepostReduction.applyMilepostReductionAlgorithm(cascadeMileposts, configuration.getPathDistanceLimit());
        WydotTim cascadeTim = cascadeService.buildCascadeTim(countyRoadSegment, reducedMileposts.get(0), reducedMileposts.get(reducedMileposts.size() - 1), clientId);
        utility.logWithDate("Generating TIM for segment " + countyRoadSegment.getId() + " with ITIS codes: " + countyRoadSegment.toITISCodes().toString());
        createSendTims(cascadeTim, timType, startDateTime, endDateTime, pk, content, frameType, cascadeMileposts, reducedMileposts, anchor, new IdGenerator());
    }

    /**
     * This method performs several existence checks.
     * - If multiple client ids are associated with the segment, all existing conditions are cleared and the method returns false.
     * - If an identical condition exists for the segment, the method returns true.
     * - If an existing condition is not identical to the requested condition, the existing condition is cleared and the method returns false.
     * 
     * Notes: 
     * - A condition in this context is defined as a collection of ITIS codes.
     * - An identical condition is defined as a condition with the same ITIS codes and end_date.
     * 
     * @param countyRoadSegment The CountyRoadSegment to perform existence checks for.
     * @return True if a single condition exists for the given segment and that condition is identical to the requested condition, otherwise false.
     */
    private boolean performExistenceChecks(CountyRoadSegment countyRoadSegment, TimType timType, String endDateTime) {
        int segmentId = countyRoadSegment.getId();
        List<ActiveTim> allActiveTimsWithItisCodesAssociatedWithSegment = cascadeService.getActiveTimsWithItisCodesAssociatedWithSegment(segmentId);
        List<String> clientIdsAssociatedWithSegment = allActiveTimsWithItisCodesAssociatedWithSegment.stream().map(ActiveTim::getClientId).collect(Collectors.toList());
        int numExistingConditions = allActiveTimsWithItisCodesAssociatedWithSegment.size();
        if (numExistingConditions == 0) {
            return false;
        }
        else if (numExistingConditions > 1) {
            utility.logWithDate("Multiple client ids detected for segment " + segmentId + "("+ clientIdsAssociatedWithSegment.toString() + "), clearing existing conditions for all client ids.");
            clearAllExistingConditionsForSegment(clientIdsAssociatedWithSegment);
            return false; // no identical condition exists at this point, return false
        }
        else if (numExistingConditions == 1) {
            ActiveTim existingCondition = allActiveTimsWithItisCodesAssociatedWithSegment.get(0);
            utility.logWithDate("Single existing condition found for segment " + segmentId + " with client id: " + existingCondition.getClientId());

            // check if existing condition is identical to requested condition
            boolean identicalITISCodes = false;
            boolean identicalEndDate = false;
            List<Integer> existingITISCodes = existingCondition.getItisCodes();
            if (existingITISCodes != null) {
                if (existingITISCodes.equals(countyRoadSegment.toITISCodes())) {
                    identicalITISCodes = true;
                }
            }
            else {
                utility.logWithDate("Warning: Null value found for existing ITIS codes.");
            }

            // check if end_date is identical
            if (existingCondition.getEndDateTime() != null) {
                // existing condition has an end date, check if it is identical
                if (existingCondition.getEndDateTime().equals(endDateTime)) {
                    identicalEndDate = true;
                }
            }
            else {
                // existing condition has no end date, check if requested condition has no end date
                if (endDateTime == null) {
                    identicalEndDate = true;
                }
            }

            if (identicalITISCodes && identicalEndDate) {
                return true; // identical condition exists, return true
            }

            wydotTimService.clearTimsById(timType.getType(), trimClientIdForQuery(existingCondition.getClientId()), null);
            return false; // no identical condition exists at this point, return false
        }
        else {
            utility.logWithDate("Warning: Expected positive number of client ids for segment " + segmentId + ", found " + numExistingConditions + ". Treating as zero.");
            return false;
        }
    }

    /**
     * This method clears any existing conditions that were previously cascaded for the given segment to ensure outdated conditions do not get left behind
     */
    private void clearAllExistingConditionsForSegment(List<String> clientIdsAssociatedWithSegment) {
        for (String clientIdToClear : clientIdsAssociatedWithSegment) {
            // clear exiting conditions
            wydotTimService.clearTimsById(timType.getType(), trimClientIdForQuery(clientIdToClear), null);
        }
    }

    /**
     * This method trims the clientId to account for the /client-id-direction query, which adds -0 to the end of the clientId.
     * Example input: myclientid-0
     * Example output: myclientid
     */
    private String trimClientIdForQuery(String clientIdToTrim) {
        if (clientIdToTrim.lastIndexOf("-") != -1) {
            clientIdToTrim = clientIdToTrim.substring(0, clientIdToTrim.lastIndexOf("-"));
        }
        return clientIdToTrim;
    }

    /**
     * This method returns the anchor point for the given mileposts.
     * @param firstPoint The first milepost.
     * @param secondPoint The second milepost.
     * @return The anchor point as a Milepost.
     */
    private Milepost getAnchorPoint(Milepost firstPoint, Milepost secondPoint) {
        Coordinate anchorCoordinate = utility.calculateAnchorCoordinate(firstPoint, secondPoint);

        Milepost anchor = new Milepost();
        anchor.setLatitude(anchorCoordinate.getLatitude());
        anchor.setLongitude(anchorCoordinate.getLongitude());
        anchor.setMilepost(firstPoint.getMilepost());
        anchor.setDirection(firstPoint.getDirection());
        return anchor;
    }
}