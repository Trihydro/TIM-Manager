package com.trihydro.tasks.actions;

//import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.TimGenerationHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ActiveTimError;
import com.trihydro.library.model.ActiveTimErrorType;
import com.trihydro.library.model.ActiveTimValidationResult;
//import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.ResubmitTimException;
import com.trihydro.library.model.TimDeleteSummary;
import com.trihydro.library.model.TmddItisCode;
import com.trihydro.library.model.tmdd.EventDescription;
import com.trihydro.library.model.tmdd.FullEventUpdate;
//import com.trihydro.library.model.tmdd.LinkLocation;
//import com.trihydro.library.model.tmdd.PointOnLink;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.ItisCodeService;
import com.trihydro.library.service.TmddService;
import com.trihydro.library.service.WydotTimService;
import com.trihydro.tasks.config.DataTasksConfiguration;
import com.trihydro.tasks.helpers.EmailFormatter;
import com.trihydro.tasks.helpers.IdNormalizer;

import org.apache.commons.lang3.StringUtils;
//import org.gavaghan.geodesy.Ellipsoid;
//import org.gavaghan.geodesy.GeodeticCalculator;
//import org.gavaghan.geodesy.GeodeticCurve;
//import org.gavaghan.geodesy.GlobalCoordinates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidateTmdd implements Runnable {
    private DataTasksConfiguration config;
    private TmddService tmddService;
    private ActiveTimService activeTimService;
    private ItisCodeService itisCodeService;
    private IdNormalizer idNormalizer;
    private EmailFormatter emailFormatter;
    private EmailHelper mailHelper;
    private Utility utility;
    private WydotTimService wydotTimService;
    private TimGenerationHelper timGenerationHelper;
    private Map<String, Integer> tmddItisCodes;
    private List<String> errors;
    private Gson gson = new Gson();

    @Autowired
    public void InjectDependencies(DataTasksConfiguration config, TmddService tmddService,
            ActiveTimService activeTimService, ItisCodeService itisCodeService, IdNormalizer idNormalizer,
            EmailFormatter emailFormatter, EmailHelper mailHelper, Utility utility, WydotTimService _wydotTimService,
            TimGenerationHelper _timGenerationHelper) {
        this.config = config;
        this.tmddService = tmddService;
        this.activeTimService = activeTimService;
        this.itisCodeService = itisCodeService;
        this.idNormalizer = idNormalizer;
        this.emailFormatter = emailFormatter;
        this.mailHelper = mailHelper;
        this.utility = utility;
        wydotTimService = _wydotTimService;
        timGenerationHelper = _timGenerationHelper;
    }

    public void run() {
        utility.logWithDate("Running...", this.getClass());
        errors = new ArrayList<>();

        try {
            validateTmdd();
        } catch (Exception ex) {
            utility.logWithDate("Error while validating Oracle with TMDD:", this.getClass());
            ex.printStackTrace();
            errors.add(ex.getMessage());

            // don't rethrow error, or the task won't be reran until the service is
            // restarted.
        }

        if (errors.size() > 0) {
            try {
                String email = "Error(s) occurred durring Oracle-TMDD message validation:<br>"
                        + String.join("<br><br>", errors);

                mailHelper.SendEmail(config.getAlertAddresses(), "TMDD Validation Error(s)", email);
            } catch (Exception ex) {
                utility.logWithDate("Failed to send error summary email:", this.getClass());
                ex.printStackTrace();
            }
        }
    }

    private void validateTmdd() {
        // Get FEUs from TMDD
        List<FullEventUpdate> feus = null;
        try {
            feus = tmddService.getTmddEvents();
        } catch (Exception ex) {
            utility.logWithDate("Error fetching FEUs from TMDD:", this.getClass());
            ex.printStackTrace();
            errors.add("Error fetching FEUs from TMDD: " + ex.getMessage());

            return;
        }

        // Get ActiveTims (for both RSUs and SDX)
        List<ActiveTim> activeTims = null;
        try {
            activeTims = activeTimService.getActiveTimsWithItisCodes(true);
        } catch (Exception ex) {
            utility.logWithDate("Error fetching Active Tims:", this.getClass());
            ex.printStackTrace();
            errors.add("Error fetching Active Tims: " + ex.getMessage());

            return;
        }

        // If the TMDD ITIS Code cache isn't setup, initialize it.
        if (tmddItisCodes == null) {
            try {
                initializeTmddItisCodes();
            } catch (Exception ex) {
                utility.logWithDate("Unable to initialize TMDD ITIS Code cache:", this.getClass());
                ex.printStackTrace();
                errors.add("Unable to initialize TMDD ITIS Code cache: " + ex.getMessage());

                return;
            }
        }

        // Initialize FEU map with initial capacity of 1024
        // This allows us to store 0.75*1024 elements before the map resizes (which
        // requires a rehashing of all elements). Currently, the TMDD is reporting 560
        // FEUs.
        Map<String, FullEventUpdate> feuMap = new HashMap<>(1024);
        for (FullEventUpdate feu : feus) {
            String id = idNormalizer.fromFeu(feu);

            if (id != null) {
                feuMap.put(id, feu);
            }
        }

        List<ActiveTim> unableToVerify = new ArrayList<>();
        List<ActiveTimValidationResult> validationResults = new ArrayList<>();

        for (ActiveTim tim : activeTims) {
            String id = idNormalizer.fromActiveTim(tim);
            if (id == null) {
                unableToVerify.add(tim);
                continue;
            }

            FullEventUpdate feu = feuMap.get(id);
            if (feu == null) {
                unableToVerify.add(tim);
                continue;
            }

            List<ActiveTimError> inconsistencies = new ArrayList<>();

            // Check End Time (could be null)
            String feuEndTime = getEndTime(feu);
            if (feuEndTime == null) {
                // If they aren't both null...
                if (tim.getEndDateTime() != null) {
                    inconsistencies
                            .add(new ActiveTimError(ActiveTimErrorType.endTime, tim.getEndDateTime(), feuEndTime));
                }
            } else {
                // feuEndTime isn't null, check if equal to tim's endDateTime
                if (!feuEndTime.equals(tim.getEndDateTime())) {
                    inconsistencies
                            .add(new ActiveTimError(ActiveTimErrorType.endTime, tim.getEndDateTime(), feuEndTime));
                }
            }

            // Turning off start/endpoint validation for now, to compensate for TIM splitting in the ode-wrapper when TIMs have
            // >63 nodes
            // LinkLocation feuLocation = getLocation(feu);
            // if (feuLocation != null) {
            //     // Check Start Point
            //     if (!pointsInRange(feuLocation.getPrimaryLocation(), tim.getStartPoint())) {
            //         inconsistencies.add(
            //                 new ActiveTimError(ActiveTimErrorType.startPoint, formatCoordinate(tim.getStartPoint()),
            //                         formatPointOnLink(feuLocation.getPrimaryLocation())));
            //     }

            //     // Check End Point
            //     if (!pointsInRange(feuLocation.getSecondaryLocation(), tim.getEndPoint())) {
            //         inconsistencies
            //                 .add(new ActiveTimError(ActiveTimErrorType.endPoint, formatCoordinate(tim.getEndPoint()),
            //                         formatPointOnLink(feuLocation.getSecondaryLocation())));
            //     }
            // } else {
            //     // FEU doesn't have a start or end point...
            //     inconsistencies.add(
            //             new ActiveTimError(ActiveTimErrorType.startPoint, formatCoordinate(tim.getStartPoint()), null));
            //     inconsistencies.add(
            //             new ActiveTimError(ActiveTimErrorType.endPoint, formatCoordinate(tim.getEndPoint()), null));
            // }

            // Check ITIS Codes
            List<EventDescription> feuEds = getEventDescriptions(feu);
            List<Integer> feuItisCodes = getNumericItisCodes(feuEds);
            if (!correctItisCodes(tim.getItisCodes(), feuItisCodes)) {
                inconsistencies.add(new ActiveTimError(ActiveTimErrorType.itisCodes,
                        formatItisCodes(tim.getItisCodes()), formatItisCodes(feuItisCodes)));
            }

            if (inconsistencies.size() > 0) {
                ActiveTimValidationResult result = new ActiveTimValidationResult();
                result.setActiveTim(tim);
                result.setErrors(inconsistencies);

                validationResults.add(result);
            }
        }

        if (unableToVerify.size() > 0 || validationResults.size() > 0) {
            var exceptions = cleanupData(unableToVerify, validationResults);
            String email = emailFormatter.generateTmddSummaryEmail(unableToVerify, validationResults, exceptions);

            try {
                mailHelper.SendEmail(config.getAlertAddresses(), "TMDD Validation Results", email);
            } catch (Exception ex) {
                utility.logWithDate("Error sending summary email:", this.getClass());
                ex.printStackTrace();
                errors.add("Error sending summary email: " + ex.getMessage());
            }
        }
    }

    private void initializeTmddItisCodes() {
        List<TmddItisCode> itisCodes = itisCodeService.selectAllTmddItisCodes();

        // Initial capacity for 150 ITIS Codes (currently 145) before resizing
        tmddItisCodes = new HashMap<>(200);

        for (TmddItisCode code : itisCodes) {
            tmddItisCodes.put(code.normalized(), code.getItisCode());
        }
    }

    private String getEndTime(FullEventUpdate feu) {
        String sTime = null;

        // MessageExpiryTime is optional
        if (feu != null && feu.getMessageHeader() != null && feu.getMessageHeader().getMessageExpiryTime() != null) {
            sTime = feu.getMessageHeader().getMessageExpiryTime().asDateTimeString();
        }

        return sTime;
    }

    // private LinkLocation getLocation(FullEventUpdate feu) {
    //     LinkLocation location = null;

    //     if (feu != null && feu.getEventElementDetails() != null && feu.getEventElementDetails().size() > 0
    //             && feu.getEventElementDetails().get(0) != null
    //             && feu.getEventElementDetails().get(0).getEventLocations() != null
    //             && feu.getEventElementDetails().get(0).getEventLocations().size() > 0
    //             && feu.getEventElementDetails().get(0).getEventLocations().get(0) != null) {

    //         location = feu.getEventElementDetails().get(0).getEventLocations().get(0).getLocationOnLink();
    //     }

    //     return location;
    // }

    private List<EventDescription> getEventDescriptions(FullEventUpdate feu) {
        List<EventDescription> eventDescriptions = null;

        if (feu != null && feu.getEventElementDetails() != null && feu.getEventElementDetails().size() > 0
                && feu.getEventElementDetails().get(0) != null) {
            eventDescriptions = feu.getEventElementDetails().get(0).getEventDescriptions();
        }

        return eventDescriptions;
    }

    private List<Integer> getNumericItisCodes(List<EventDescription> eventDescriptions) {
        if (eventDescriptions == null) {
            return null;
        }

        List<Integer> itisCodes = new ArrayList<Integer>();

        for (EventDescription description : eventDescriptions) {
            if (description == null || description.getPhrase() == null) {
                continue;
            }

            Integer itisCode = tmddItisCodes.get(description.getPhrase().normalized());

            if (itisCode != null) {
                itisCodes.add(itisCode);
            }
        }

        return itisCodes;
    }

    // Check that all the ITIS Codes present in an Active TIM are present in the
    // corresponding FEU. Note that FEUs may have more ITIS codes than an Active
    // TIM, as the ITIS Codes reported in TIMs are a subset of those reported by the
    // TMDD
    private boolean correctItisCodes(List<Integer> activeTimItisCodes, List<Integer> feuItisCodes) {
        if (activeTimItisCodes == null || feuItisCodes == null) {
            if (activeTimItisCodes == null && feuItisCodes == null) {
                return true;
            }

            return false;
        }

        var result = true;

        // Iterate over activeTimItisCodes and ensure each is present in feuItisCodes
        for (var i = 0; i < activeTimItisCodes.size(); i++) {
            var inBoth = false;
            for (var j = 0; j < feuItisCodes.size(); j++) {
                if (activeTimItisCodes.get(i) != null && activeTimItisCodes.get(i).equals(feuItisCodes.get(j))) {
                    inBoth = true;
                    break;
                }
            }

            if (!inBoth) {
                result = false;
                break;
            }
        }

        return result;
    }

    // private boolean pointsInRange(PointOnLink tmddPoint, Coordinate timPoint) {
    //     if (tmddPoint == null || tmddPoint.getGeoLocation() == null || timPoint == null) {
    //         return false;
    //     }

    //     double tmddLat = tmddPoint.getGeoLocation().getLatitude() / 1000000.0;
    //     double tmddLon = tmddPoint.getGeoLocation().getLongitude() / 1000000.0;

    //     GlobalCoordinates tmdd = new GlobalCoordinates(tmddLat, tmddLon);
    //     GlobalCoordinates tim = new GlobalCoordinates(timPoint.getLatitude().doubleValue(),
    //             timPoint.getLongitude().doubleValue());

    //     GeodeticCalculator geoCalc = new GeodeticCalculator();
    //     GeodeticCurve curve = geoCalc.calculateGeodeticCurve(Ellipsoid.WGS84, tmdd, tim);
    //     double miles = 0.000621371 * curve.getEllipsoidalDistance();

    //     // Are they within 1/5th of a mile?
    //     return miles < 0.2;
    // }

    // private String formatPointOnLink(PointOnLink point) {
    //     if (point == null || point.getGeoLocation() == null) {
    //         return null;
    //     }

    //     double lat = point.getGeoLocation().getLatitude() / 1000000.0;
    //     double lon = point.getGeoLocation().getLongitude() / 1000000.0;

    //     var coord = new Coordinate(BigDecimal.valueOf(lat), BigDecimal.valueOf(lon));
    //     return gson.toJson(coord);
    // }

    // private String formatCoordinate(Coordinate point) {
    //     if (point == null) {
    //         return null;
    //     }

    //     return gson.toJson(point);
    // }

    private String formatItisCodes(List<Integer> itisCodes) {
        // { }
        // { 1 }
        // { 1, 2 }
        String result = "{ ";

        if (itisCodes != null) {
            for (Integer itisCode : itisCodes) {
                result += String.format("%d, ", itisCode);
            }
        }

        result = result.replaceAll(", $", " ");
        result += "}";

        return result;
    }

    private String cleanupData(List<ActiveTim> unableToVerify, List<ActiveTimValidationResult> validationResults) {
        String cleanupError = "";
        if (unableToVerify.size() > 0) {
            cleanupError = deleteActiveTims(unableToVerify);
        }
        if (validationResults.size() > 0) {
            cleanupError += updateAndResend(validationResults);
        }
        return cleanupError;
    }

    private String updateAndResend(List<ActiveTimValidationResult> validationResults) {
        List<ActiveTim> toClear = new ArrayList<>();
        List<ActiveTimValidationResult> toResend = new ArrayList<>();

        for (var result : validationResults) {
            var itisCodeError = result.getErrors().stream().filter(err -> err.getName() == ActiveTimErrorType.itisCodes)
                    .findAny();
            if (itisCodeError.isPresent()) {

                if (itisCodeError.get().getTmddValue().replaceAll("\\{|\\s|\\}", "").equals("6011")) {
                    // If it should be dry roads, submit an All Clear. This will delete the active
                    // tim, rendering any other errors, if present, irrelevant.
                    toClear.add(result.getActiveTim());

                    // All Clear queued, no need to proceed with re-submitting the TIM (it will just
                    // get cleared). So we won't add it to toResend.
                } else {
                    toResend.add(result);
                }
            } else {
                toResend.add(result);
            }
        }

        String exMsg = "";

        if (toResend.size() > 0) {
            var exceptions = timGenerationHelper.updateAndResubmitToOde(toResend);

            if (exceptions.size() > 0 ) {
                exMsg += "The Validate TMDD application ran into exceptions while attempting to resubmit TIMs. The following exceptions were found: ";
                exMsg += "<br/>";
                for (ResubmitTimException rte : exceptions) {
                    exMsg += gson.toJson(rte);
                    exMsg += "<br/>";
                }
            }
        }

        if (toClear.size() > 0) {
            exMsg += deleteActiveTims(toClear);
        }

        return exMsg;
    }

    private String deleteActiveTims(List<ActiveTim> unableToVerify) {
        String errSummary = "";
        TimDeleteSummary tds = wydotTimService.deleteTimsFromRsusAndSdx(unableToVerify);

        // Check for exceptions and add to errSummary
        if (StringUtils.isNotBlank(tds.getSatelliteErrorSummary())) {
            errSummary += tds.getSatelliteErrorSummary();
            errSummary += "<br/>";
        }
        if (tds.getFailedActiveTimDeletions().size() > 0) {
            errSummary += "The following active tim record failed to delete: <br/>";
        }
        for (Long aTimId : tds.getFailedActiveTimDeletions()) {
            errSummary += aTimId;
            errSummary += "<br/>";
        }
        if (tds.getFailedRsuTimJson().size() > 0) {
            errSummary += "<br/><br/>";
            errSummary += "The following RsuTim records failed to remove values from associated RSUs: <br/>";
            errSummary += tds.getRsuErrorSummary();
        }

        return errSummary;
    }
}