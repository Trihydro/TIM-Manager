package com.trihydro.odewrapper.controller;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.model.WydotTravelerInputData;
import com.trihydro.odewrapper.service.WydotTimService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import com.trihydro.library.model.TimType;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.odewrapper.helpers.SetItisCodes;
import com.trihydro.odewrapper.model.Buffer;
import com.trihydro.odewrapper.model.ControllerResult;

@RestController
@ApiIgnore
public abstract class WydotTimBaseController {

    // services
    protected final WydotTimService wydotTimService;
    protected static Gson gson = new Gson();
    private List<TimType> timTypes;

    WydotTimBaseController() {
        this.wydotTimService = new WydotTimService();
    }

    protected ControllerResult validateInputParking(WydotTim tim) {

        ControllerResult result = new ControllerResult();
        List<String> resultMessages = new ArrayList<String>();

        // get route number
        if (tim.getDirection() != null)
            result.setDirection(tim.getDirection());

        result.setClientId(tim.getClientId());

        String route = null;
        if (tim.getRoute() != null) {
            route = tim.getRoute().replaceAll("\\D+", "");
            result.setRoute(tim.getRoute());
        } else {
            resultMessages.add("route not supported");
        }
        // if route is not 80 fail
        if (!route.equals("80")) {
            resultMessages.add("route not supported");
        }
        // if direction is not eastbound/westbound/both fail
        if (!tim.getDirection().toLowerCase().equals("eastbound")
                && !tim.getDirection().toLowerCase().equals("westbound")
                && !tim.getDirection().toLowerCase().equals("both")) {
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
        List<String> itisCodes = SetItisCodes.setItisCodesParking(tim);
        if (itisCodes.size() == 0)
            resultMessages.add("No ITIS codes found");
        result.setItisCodes(itisCodes);
        tim.setItisCodes(itisCodes);

        result.setResultMessages(resultMessages);
        return result;
    }

    protected ControllerResult validateInputIncident(WydotTim tim) {

        ControllerResult result = new ControllerResult();
        List<String> resultMessages = new ArrayList<String>();

        // get route number
        if (tim.getDirection() != null)
            result.setDirection(tim.getDirection());
        if (tim.getIncidentId() != null)
            result.setClientId(tim.getIncidentId());

        String route = null;
        if (tim.getHighway() != null) {
            route = tim.getHighway().replaceAll("\\D+", "");
            result.setRoute(tim.getHighway());
        } else {
            resultMessages.add("route not supported");
        }
        // if route is not 80 fail
        if (!route.equals("80")) {
            resultMessages.add("route not supported");
        }
        // if direction is not eastbound/westbound/both fail
        if (!tim.getDirection().toLowerCase().equals("eastbound")
                && !tim.getDirection().toLowerCase().equals("westbound")
                && !tim.getDirection().toLowerCase().equals("both")) {
            resultMessages.add("direction not supported");
        }
        if (tim.getIncidentId() == null) {
            resultMessages.add("Null value for incidentId");
        }
        if (tim.getToRm() != null && tim.getToRm() < 0) {
            resultMessages.add("Invalid toRm");
        }
        if (tim.getFromRm() < 0) {
            resultMessages.add("Invalid fromRm");
        }
        if (tim.getFromRm() == null) {
            resultMessages.add("Null value for fromRm");
        }

        // set itis codes
        List<String> itisCodes = SetItisCodes.setItisCodesIncident(tim);
        if (itisCodes.size() == 0)
            resultMessages.add("No ITIS codes found");
        result.setItisCodes(itisCodes);
        tim.setItisCodes(itisCodes);

        result.setResultMessages(resultMessages);
        return result;
    }

    protected ControllerResult validateInputRw(WydotTim tim) {

        ControllerResult result = new ControllerResult();
        List<String> resultMessages = new ArrayList<String>();

        // get route number
        if (tim.getDirection() != null)
            result.setDirection(tim.getDirection());
        if (tim.getId() != null)
            result.setClientId(tim.getId());

        String route = null;
        if (tim.getHighway() != null) {
            route = tim.getHighway().replaceAll("\\D+", "");
            result.setRoute(tim.getHighway());
        } else {
            resultMessages.add("route not supported");
        }

        // if route is not 80 fail
        if (!route.equals("80")) {
            resultMessages.add("route not supported");
        }
        // if direction is not eastbound/westbound/both fail
        if (!tim.getDirection().toLowerCase().equals("eastbound")
                && !tim.getDirection().toLowerCase().equals("westbound")
                && !tim.getDirection().toLowerCase().equals("both")) {
            resultMessages.add("direction not supported");
        }
        if (tim.getToRm() != null && tim.getToRm() < 0) {
            resultMessages.add("Invalid toRm");
        }
        if (tim.getFromRm() < 0) {
            resultMessages.add("Invalid fromRm");
        }
        if (tim.getFromRm() == null) {
            resultMessages.add("Null value for fromRm");
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
        if (tim.getId() == null) {
            resultMessages.add("Null value for id");
        }
        if (tim.getStartTs() == null) {
            resultMessages.add("Null value for startTs");
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

        // set itis codes
        List<String> itisCodes = SetItisCodes.setItisCodesRw(tim);
        if (itisCodes.size() == 0)
            resultMessages.add("No ITIS codes found");
        result.setItisCodes(itisCodes);
        tim.setItisCodes(itisCodes);

        result.setResultMessages(resultMessages);
        return result;
    }

    protected ControllerResult validateInputRc(WydotTim tim) {

        ControllerResult result = new ControllerResult();
        List<String> resultMessages = new ArrayList<String>();

        // get route number
        if (tim.getDirection() != null)
            result.setDirection(tim.getDirection());

        String route = null;
        if (tim.getRoute() != null) {
            route = tim.getRoute().replaceAll("\\D+", "");
            result.setRoute(tim.getRoute());
        } else {
            resultMessages.add("route not supported");
        }

        // if route is not 80 fail
        if (!route.equals("80")) {
            resultMessages.add("route not supported");
        }
        // if direction is not eastbound/westbound/both fail
        if (!tim.getDirection().toLowerCase().equals("eastbound")
                && !tim.getDirection().toLowerCase().equals("westbound")
                && !tim.getDirection().toLowerCase().equals("both")) {
            resultMessages.add("direction not supported");
        }
        if (tim.getToRm() != null && tim.getToRm() < 0) {
            resultMessages.add("Invalid toRm");
        }
        if (tim.getFromRm() < 0) {
            resultMessages.add("Invalid fromRm");
        }
        if (tim.getFromRm() == null) {
            resultMessages.add("Null value for fromRm");
        }
        if (tim.getToRm() == null) {
            resultMessages.add("Null value for toRm");
        }
        if (tim.getRoute() == null) {
            resultMessages.add("Null value for route");
        }
        if (tim.getDirection() == null) {
            resultMessages.add("Null value for direction");
        }

        // set itis codes
        List<String> itisCodes = SetItisCodes.setItisCodesRc(tim);
        if (itisCodes.size() == 0)
            resultMessages.add("No ITIS codes found");
        result.setItisCodes(itisCodes);
        tim.setItisCodes(itisCodes);

        result.setResultMessages(resultMessages);
        return result;
    }

    protected ControllerResult validateInputVsl(WydotTim tim) {

        ControllerResult result = new ControllerResult();
        List<String> resultMessages = new ArrayList<String>();

        // get route number
        if (tim.getDirection() != null)
            result.setDirection(tim.getDirection());

        String route = null;
        if (tim.getRoute() != null) {
            route = tim.getRoute().replaceAll("\\D+", "");
            result.setRoute(tim.getRoute());
        } else {
            resultMessages.add("route not supported");
        }

        // if route is not 80 fail
        if (!route.equals("80")) {
            resultMessages.add("route not supported");
        }
        // if direction is not eastbound/westbound/both fail
        if (!tim.getDirection().toLowerCase().equals("eastbound")
                && !tim.getDirection().toLowerCase().equals("westbound")
                && !tim.getDirection().toLowerCase().equals("both")) {
            resultMessages.add("direction not supported");
        }
        if (tim.getToRm() != null && tim.getToRm() < 0) {
            resultMessages.add("Invalid toRm");
        }
        if (tim.getFromRm() < 0) {
            resultMessages.add("Invalid fromRm");
        }
        if (tim.getFromRm() == null) {
            resultMessages.add("Null value for fromRm");
        }
        if (tim.getToRm() == null) {
            resultMessages.add("Null value for toRm");
        }
        if (tim.getRoute() == null) {
            resultMessages.add("Null value for route");
        }
        if (tim.getDirection() == null) {
            resultMessages.add("Null value for direction");
        }

        // set itis codes
        List<String> itisCodes = SetItisCodes.setItisCodesVsl(tim);
        if (itisCodes.size() == 0)
            resultMessages.add("No ITIS codes found");
        result.setItisCodes(itisCodes);
        tim.setItisCodes(itisCodes);

        result.setResultMessages(resultMessages);
        return result;
    }

    protected ControllerResult validateInputCc(WydotTim tim) {

        ControllerResult result = new ControllerResult();
        List<String> resultMessages = new ArrayList<String>();

        // get route number
        if (tim.getDirection() != null)
            result.setDirection(tim.getDirection());

        if (tim.getSegment() != null)
            result.setClientId(tim.getSegment());

        String route = null;
        if (tim.getRoute() != null) {
            route = tim.getRoute().replaceAll("\\D+", "");
            result.setRoute(tim.getRoute());
        } else {
            resultMessages.add("route not supported");
        }

        // if route is not 80 fail
        if (!route.equals("80")) {
            resultMessages.add("route not supported");
        }
        // if direction is not eastbound/westbound/both fail
        if (!tim.getDirection().toLowerCase().equals("eastbound")
                && !tim.getDirection().toLowerCase().equals("westbound")
                && !tim.getDirection().toLowerCase().equals("both")) {
            resultMessages.add("direction not supported");
        }
        if (tim.getToRm() != null && tim.getToRm() < 0) {
            resultMessages.add("Invalid toRm");
        }
        if (tim.getFromRm() < 0) {
            resultMessages.add("Invalid fromRm");
        }
        if (tim.getFromRm() == null) {
            resultMessages.add("Null value for fromRm");
        }
        if (tim.getToRm() == null) {
            resultMessages.add("Null value for toRm");
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
        List<String> itisCodes = SetItisCodes.setItisCodesFromAdvisoryArray(tim);
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

    public abstract void processRequest(List<WydotTim> wydotTims);

    public TimType getTimType(String timTypeName) {

        // get tim type
        TimType timType = getTimTypes().stream().filter(x -> x.getType().equals(timTypeName)).findFirst().orElse(null);

        return timType;
    }

    public List<TimType> getTimTypes() {
        if (timTypes != null)
            return timTypes;
        else {
            timTypes = TimTypeService.selectAll();
            return timTypes;
        }
    }

    // creates a TIM and sends it to RSUs and Satellite
    protected void createSendTims(WydotTim wydotTim, String direction, TimType timType) {
        // build region name for active tim logger to use
        String regionNamePrev = direction + "_" + wydotTim.getRoute() + "_" + wydotTim.getFromRm() + "_"
                + wydotTim.getToRm();
        // create TIM
        WydotTravelerInputData timToSend = wydotTimService.createTim(wydotTim, direction, timType.getType());
        // send TIM to RSUs
        wydotTimService.sendTimToRsus(wydotTim, timToSend, regionNamePrev, wydotTim.getDirection(), timType);
        // send TIM to SDW
        wydotTimService.sendTimToSDW(wydotTim, timToSend, regionNamePrev, wydotTim.getDirection(), timType);
    }

}