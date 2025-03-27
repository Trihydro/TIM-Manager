package com.trihydro.odewrapper.controller;

import com.trihydro.library.model.TimUpdateModel;
import java.text.ParseException;
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
import java.util.Objects;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.trihydro.library.helpers.MilepostReduction;
import com.trihydro.library.helpers.TimGenerationHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
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
import com.trihydro.odewrapper.helpers.SetItisCodes.WeightNotSupportedException;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.WydotTimBowr;
import com.trihydro.odewrapper.model.WydotTimIncident;
import com.trihydro.odewrapper.model.WydotTimParking;
import com.trihydro.odewrapper.model.WydotTimRc;
import com.trihydro.odewrapper.model.WydotTimVsl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.plugin.j2735.timstorage.FrameType.TravelerInfoType;

@Component
@Slf4j
public abstract class WydotTimBaseController {

    protected static BasicConfiguration configuration;
    protected WydotTimService wydotTimService;
    protected TimTypeService timTypeService;
    private TimType timType = null;
    private final SetItisCodes setItisCodes;
    protected ActiveTimService activeTimService;
    protected RestTemplateProvider restTemplateProvider;
    MilepostReduction milepostReduction;
    protected Utility utility;
    protected TimGenerationHelper timGenerationHelper;

    protected static Gson gson = new Gson();
    private List<TimType> timTypes;

    public WydotTimBaseController(BasicConfiguration _basicConfiguration,
                                  WydotTimService _wydotTimService, TimTypeService _timTypeService,
                                  SetItisCodes _setItisCodes, ActiveTimService _activeTimService,
                                  RestTemplateProvider _restTemplateProvider,
                                  MilepostReduction _milepostReduction, Utility _utility,
                                  TimGenerationHelper _timGenerationHelper) {
        configuration = _basicConfiguration;
        wydotTimService = _wydotTimService;
        timTypeService = _timTypeService;
        setItisCodes = _setItisCodes;
        activeTimService = _activeTimService;
        restTemplateProvider = _restTemplateProvider;
        milepostReduction = _milepostReduction;
        utility = _utility;
        timGenerationHelper = _timGenerationHelper;
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
        if (tim.getDirection() != null) {
            result.setDirection(tim.getDirection());
        }

        result.setClientId(tim.getClientId());

        if (tim.getRoute() == null || !routeSupported(tim.getRoute())) {
            resultMessages.add("route not supported");
        } else {
            result.setRoute(tim.getRoute());
        }
        // if direction is not i/d/b fail
        if (!tim.getDirection().equalsIgnoreCase("i") &&
            !tim.getDirection().equalsIgnoreCase("d") &&
            !tim.getDirection().equalsIgnoreCase("b")) {
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
        if (itisCodes.isEmpty()) {
            resultMessages.add("No ITIS codes found");
        }
        result.setItisCodes(itisCodes);
        tim.setItisCodes(itisCodes);

        result.setResultMessages(resultMessages);
        return result;
    }

    public ControllerResult validateInputIncident(WydotTimIncident tim) {

        ControllerResult result = new ControllerResult();
        List<String> resultMessages = new ArrayList<String>();

        // get route number
        if (tim.getDirection() != null) {
            result.setDirection(tim.getDirection());
        }
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
        if (!tim.getDirection().equalsIgnoreCase("i") &&
            !tim.getDirection().equalsIgnoreCase("d") &&
            !tim.getDirection().equalsIgnoreCase("b")) {
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
        if (itisCodes.isEmpty()) {
            resultMessages.add("No ITIS codes found");
        }
        result.setItisCodes(itisCodes);
        tim.setItisCodes(itisCodes);

        result.setResultMessages(resultMessages);
        return result;
    }

    protected ControllerResult validateInputRw(WydotTimRw tim) {

        ControllerResult result = new ControllerResult();
        List<String> resultMessages = new ArrayList<String>();

        // get route number
        if (tim.getDirection() != null) {
            result.setDirection(tim.getDirection());
        }
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
        if (!tim.getDirection().equalsIgnoreCase("i") &&
            !tim.getDirection().equalsIgnoreCase("d") &&
            !tim.getDirection().equalsIgnoreCase("b")) {
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
                var convertedDate =
                    LocalDate.parse(tim.getSchedStart(), DateTimeFormatter.ISO_LOCAL_DATE);
                var startOfDay = convertedDate.atStartOfDay(ZoneId.systemDefault());
                tim.setSchedStart(getIsoDateTimeString(startOfDay));
            } catch (DateTimeParseException e) {
                resultMessages.add(
                    "Bad value supplied for schedStart. Should follow the format: yyyy-MM-dd");
                e.printStackTrace();
            }
        }
        if (tim.getSchedEnd() != null) {
            try {
                var convertedDate =
                    LocalDate.parse(tim.getSchedEnd(), DateTimeFormatter.ISO_LOCAL_DATE);
                // LocalTime.MAX sets the time to 11:59 PM. This is especially important when
                // construction is only scheduled for a day (ex. 5-12 to 5-12)
                var endOfDay = ZonedDateTime.of(LocalDateTime.of(convertedDate, LocalTime.MAX),
                    ZoneId.systemDefault());
                tim.setSchedEnd(getIsoDateTimeString(endOfDay));
            } catch (DateTimeParseException e) {
                resultMessages.add(
                    "Bad value supplied for schedEnd. Should follow the format: yyyy-MM-dd");
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
        if (itisCodes.isEmpty()) {
            resultMessages.add("No ITIS codes found");
        }
        result.setItisCodes(itisCodes);
        tim.setItisCodes(itisCodes);

        result.setResultMessages(resultMessages);
        return result;
    }

    public boolean routeSupported(String route) {
        // call out to REST service to get all routes once, then use that
        // if (routes.size() == 0) {
        //     String url = String.format("%s/routes", configuration.getCvRestService());
        //     ResponseEntity<String[]> response = restTemplateProvider.GetRestTemplate().getForEntity(url,
        //             String[].class);
        //     routes = Arrays.asList(response.getBody());
        // }
        // return routes.contains(route);

        // Since routes are not loaded, assume all route are supported.
        return !route.isEmpty();
    }

    protected ControllerResult validateInputRc(WydotTimRc tim) {

        ControllerResult result = new ControllerResult();
        List<String> resultMessages = new ArrayList<>();

        // get route number
        if (tim.getDirection() != null) {
            result.setDirection(tim.getDirection());
        }

        if (tim.getRoute() == null || !routeSupported(tim.getRoute())) {
            resultMessages.add("route not supported");
        } else {
            result.setRoute(tim.getRoute());
        }
        // if direction is not i/d/b fail
        if (tim.getDirection() != null && !tim.getDirection().equalsIgnoreCase("i") &&
            !tim.getDirection().equalsIgnoreCase("d") &&
            !tim.getDirection().equalsIgnoreCase("b")) {
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
        if (itisCodes.isEmpty()) {
            resultMessages.add("No ITIS codes found");
        }
        result.setItisCodes(itisCodes);
        tim.setItisCodes(itisCodes);

        result.setResultMessages(resultMessages);
        return result;
    }

    protected ControllerResult validateRcAc(WydotTimRc allClear) {
        ControllerResult result = new ControllerResult();
        List<String> resultMessages = new ArrayList<>();

        // All Clear must have a valid CLIENT_ID and DIRECTION
        if (StringUtils.isBlank(allClear.getRoadCode())) {
            resultMessages.add("Road Code must be supplied");
        } else {
            result.setClientId(allClear.getRoadCode());
            allClear.setClientId(allClear.getRoadCode());
        }

        if (allClear.getDirection() == null) {
            resultMessages.add("Null value for direction");
        } else if (!allClear.getDirection().equalsIgnoreCase("i") &&
            !allClear.getDirection().equalsIgnoreCase("d") &&
            !allClear.getDirection().equalsIgnoreCase("b")) {
            resultMessages.add("direction not supported");
        } else {
            result.setDirection(allClear.getDirection());
        }

        result.setResultMessages(resultMessages);
        return result;
    }

    protected ControllerResult validateInputVsl(WydotTimVsl tim) {

        ControllerResult result = new ControllerResult();
        List<String> resultMessages = new ArrayList<>();

        // get route number
        if (tim.getDirection() != null) {
            result.setDirection(tim.getDirection());
        }

        if (tim.getRoute() == null || !routeSupported(tim.getRoute())) {
            resultMessages.add("route not supported");
        } else {
            result.setRoute(tim.getRoute());
        }

        // if direction is not i/d/b fail
        if (!tim.getDirection().equalsIgnoreCase("i") &&
            !tim.getDirection().equalsIgnoreCase("d") &&
            !tim.getDirection().equalsIgnoreCase("b")) {
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
        if (itisCodes.isEmpty()) {
            resultMessages.add("No ITIS codes found");
        }
        result.setItisCodes(itisCodes);
        tim.setItisCodes(itisCodes);

        result.setResultMessages(resultMessages);
        return result;
    }

    protected ControllerResult validateInputCc(WydotTimRc tim) {

        ControllerResult result = new ControllerResult();
        List<String> resultMessages = new ArrayList<>();

        // get route number
        if (tim.getDirection() != null) {
            result.setDirection(tim.getDirection());
        }

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
        if (!tim.getDirection().equalsIgnoreCase("i") &&
            !tim.getDirection().equalsIgnoreCase("d") &&
            !tim.getDirection().equalsIgnoreCase("b")) {
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
        if (itisCodes.isEmpty()) {
            resultMessages.add("No ITIS codes found");
        }
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
            if (actionSplit.length < 2) {
                return false;
            }
            if (!actionSplit[0].equals("delay")) {
                return false;
            }
            if (!StringUtils.isNumeric(actionSplit[1])) {
                return false;
            }
            if (Float.parseFloat(actionSplit[1]) < 0) {
                return false;
            }
            return true;
        } else if (action.equals("prepareStop")) {
            return true;
        } else if (action.contains("reduceSpeed_")) {
            String[] actionSplit = action.split("_");
            if (actionSplit.length < 2) {
                return false;
            }
            if (!actionSplit[0].equals("reduceSpeed")) {
                return false;
            }
            if (!StringUtils.isNumeric(actionSplit[1])) {
                return false;
            }
            if (Float.parseFloat(actionSplit[1]) < 0) {
                return false;
            }
            return true;
        }
        return false;
    }

    protected ControllerResult validateInputBowr(WydotTimBowr tim) {
        ControllerResult toReturn = new ControllerResult();
        List<String> resultMessages = new ArrayList<>();

        // check direction
        if (tim.getDirection() != null) {
            toReturn.setDirection(tim.getDirection());
        }
        if (tim.getDirection() != null && !tim.getDirection().equalsIgnoreCase("i") &&
            !tim.getDirection().equalsIgnoreCase("d") &&
            !tim.getDirection().equalsIgnoreCase("b")) {
            resultMessages.add("direction not supported");
        }

        // check route
        if (tim.getRoute() == null) {
            resultMessages.add("Null value for route");
        } else {
            toReturn.setRoute(tim.getRoute());
        }

        // check points
        if (tim.getStartPoint() == null || !tim.getStartPoint().isValid()) {
            resultMessages.add("Invalid startPoint");
        }
        if (tim.getEndPoint() == null || !tim.getEndPoint().isValid()) {
            resultMessages.add("Invalid endPoint");
        }

        if (tim.getClientId() == null) {
            resultMessages.add("Null value for client id");
        } else {
            tim.setClientId(tim.getClientId());
        }

        // set itis codes
        List<String> itisCodes = new ArrayList<>();
        try {
            itisCodes = setItisCodes.setItisCodesBowr(tim);
        } catch (WeightNotSupportedException exception) {
            resultMessages.add("Weight not supported");
        }
        toReturn.setItisCodes(itisCodes);
        tim.setItisCodes(itisCodes);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date parsedStartDate = null;
        Date parsedEndDate = null;
        // check start/end datetimes, which are optional
        if (tim.getStartDateTime() != null) {
            // ensure this is specified in ISO8601-2019
            try {
                parsedStartDate = dateFormat.parse(tim.getStartDateTime());
            } catch (ParseException e) {
                resultMessages.add("Invalid startDateTime. Must be in ISO8601-2019 format.");
            }
        }
        if (tim.getEndDateTime() != null) {
            // ensure this is specified in ISO8601-2019
            try {
                parsedEndDate = dateFormat.parse(tim.getEndDateTime());
            } catch (ParseException e) {
                resultMessages.add("Invalid endDateTime. Must be in ISO8601-2019 format.");
            }
        }

        // check that start time is before end time
        if (parsedStartDate != null && parsedEndDate != null) {
            if (parsedStartDate.after(parsedEndDate)) {
                resultMessages.add("Invalid startDateTime. Start time must be before end time.");
            }
        }

        // return
        toReturn.setResultMessages(resultMessages);
        return toReturn;
    }

    public void processRequest(WydotTim wydotTim, TimType timType, String startDateTime,
                               String endDateTime, Integer pk, ContentEnum content,
                               TravelerInfoType frameType) {

        if (wydotTim.getDirection().equalsIgnoreCase("b")) {
            var iTim = wydotTim.copy();
            var dTim = wydotTim.copy();
            iTim.setDirection("I");
            dTim.setDirection("D");
            // I
            expireReduceCreateSendTims(iTim, timType, startDateTime, endDateTime, pk, content,
                frameType);
            // D
            expireReduceCreateSendTims(dTim, timType, startDateTime, endDateTime, pk, content,
                frameType);
        } else {
            expireReduceCreateSendTims(wydotTim, timType, startDateTime, endDateTime, pk, content,
                frameType);
        }
    }

    public TimType getTimType(String timTypeName) {

        if (timType != null && Objects.equals(timType.getType(), timTypeName)) {
            return timType;
        } else {
            // get tim type
            timType =
                getTimTypes().stream().filter(x -> x.getType().equals(timTypeName)).findFirst()
                    .orElse(null);

            return timType;
        }
    }

    public List<TimType> getTimTypes() {
        if (timTypes != null) {
            return timTypes;
        } else {
            timTypes = timTypeService.selectAll();
            return timTypes;
        }
    }

    protected void expireReduceCreateSendTims(WydotTim wydotTim, TimType timType,
                                              String startDateTime, String endDateTime, Integer pk,
                                              ContentEnum content, TravelerInfoType frameType) {
        // Clear any existing TIMs with the same client id
        Long timTypeId = timType != null ? timType.getTimTypeId() : null;
        var existingTims =
            activeTimService.getActiveTimsByClientIdDirection(wydotTim.getClientId(), timTypeId,
                wydotTim.getDirection());

        // Expire existing tims
        List<Long> existingTimIds = new ArrayList<>();
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

        Milepost anchor;
        try {
            anchor = getAnchorPoint(firstPoint, secondPoint);
        } catch (Utility.IdenticalPointsException e) {
            anchor = recoverFromIdenticalPointsException(milepostsAll);
            if (anchor == null) {
                log.warn("Unable to recover from identical points exception for active TIM.");
                return;
            }

        }
        var reducedMileposts = milepostReduction.applyMilepostReductionAlgorithm(milepostsAll,
            configuration.getPathDistanceLimit());

        createSendTims(wydotTim, timType, startDateTime, endDateTime, pk, content, frameType,
            milepostsAll, reducedMileposts, anchor);
    }

    // creates a TIM and sends it to RSUs and Satellite
    protected void createSendTims(WydotTim wydotTim, TimType timType, String startDateTime,
                                  String endDateTime, Integer pk, ContentEnum content,
                                  TravelerInfoType frameType, List<Milepost> allMileposts,
                                  List<Milepost> reducedMileposts, Milepost anchor) {

        // create TIM
        WydotTravelerInputData timToSend =
            wydotTimService.createTim(wydotTim, timType.getType(), startDateTime, endDateTime,
                content, frameType, allMileposts, reducedMileposts, anchor);

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
            wydotTimService.sendTimToRsus(wydotTim, timToSend, regionNamePrev, timType, pk,
                endDateTime, endPoint);
        }
        // send TIM to SDW
        // remove rsus from TIM
        timToSend.getRequest().setRsus(null);
        wydotTimService.sendTimToSDW(wydotTim, timToSend, regionNamePrev, timType, pk, endPoint,
            reducedMileposts);
    }

    /**
     * This method returns the anchor point for the given mileposts.
     *
     * @param firstPoint  The first milepost.
     * @param secondPoint The second milepost.
     * @return The anchor point as a Milepost.
     */
    private Milepost getAnchorPoint(Milepost firstPoint, Milepost secondPoint)
        throws Utility.IdenticalPointsException {
        Coordinate anchorCoordinate = utility.calculateAnchorCoordinate(firstPoint, secondPoint);

        Milepost anchor = new Milepost();
        anchor.setLatitude(anchorCoordinate.getLatitude());
        anchor.setLongitude(anchorCoordinate.getLongitude());
        anchor.setMilepost(firstPoint.getMilepost());
        anchor.setDirection(firstPoint.getDirection());
        return anchor;
    }

    /**
     * Attempts to recover from an identical points exception by removing the first milepost
     * and re-evaluating the remaining mileposts. If recovery is not possible due to insufficient
     * mileposts or repeated identical points, returns null.
     *
     * @param allMps   The list of Mileposts to process. The list must contain at least three mileposts
     *                 to attempt recovery.
     * @return The anchor point Milepost if recovery is successful, or null if recovery fails.
     */
    private Milepost recoverFromIdenticalPointsException(List<Milepost> allMps) {
        log.info("Attempting to recover from identical points exception");
        if (allMps.size() < 3) {
            // if we only have 2 mileposts, we can't recover
            log.warn(
                "Unable to recover from identical points exception for active TIM, less than 3 mileposts found.");
            return null;
        }
        // if we have more than 2 mileposts, we can remove the first milepost and try again
        allMps.remove(0);
        Milepost firstPoint = allMps.get(0);
        Milepost secondPoint = allMps.get(1);
        try {
            return getAnchorPoint(firstPoint, secondPoint);
        } catch (Utility.IdenticalPointsException e2) {
            log.warn("Unable to recover from identical points exception for active TIM, first three mileposts are identical.");
            return null;
        }
    }
}