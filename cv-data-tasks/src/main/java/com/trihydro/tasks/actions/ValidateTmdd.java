package com.trihydro.tasks.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.TmddItisCode;
import com.trihydro.library.model.tmdd.EventDescription;
import com.trihydro.library.model.tmdd.FullEventUpdate;
import com.trihydro.library.model.tmdd.LinkLocation;
import com.trihydro.library.model.tmdd.PointOnLink;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.ItisCodeService;
import com.trihydro.library.service.TmddService;
import com.trihydro.tasks.config.DataTasksConfiguration;
import com.trihydro.tasks.helpers.EmailFormatter;
import com.trihydro.tasks.helpers.IdNormalizer;
import com.trihydro.tasks.models.ActiveTimError;
import com.trihydro.tasks.models.ActiveTimValidationResult;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
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

    private Map<String, Integer> tmddItisCodes;

    private List<String> errors;

    @Autowired
    public void InjectDependencies(DataTasksConfiguration config, TmddService tmddService,
            ActiveTimService activeTimService, ItisCodeService itisCodeService, IdNormalizer idNormalizer,
            EmailFormatter emailFormatter, EmailHelper mailHelper, Utility utility) {
        this.config = config;
        this.tmddService = tmddService;
        this.activeTimService = activeTimService;
        this.itisCodeService = itisCodeService;
        this.idNormalizer = idNormalizer;
        this.emailFormatter = emailFormatter;
        this.mailHelper = mailHelper;
        this.utility = utility;
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

                mailHelper.SendEmail(config.getAlertAddresses(), null, "TMDD Validation Error(s)", email,
                        config.getMailPort(), config.getMailHost(), config.getFromEmail());
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

            // Check Start Time
            String feuStartTime = getStartTime(feu);
            if (feuStartTime == null || !feuStartTime.equals(tim.getStartDateTime())) {
                inconsistencies.add(new ActiveTimError("Start Time", tim.getStartDateTime(), feuStartTime));
            }

            // Check End Time (could be null)
            String feuEndTime = getEndTime(feu);
            if (feuEndTime == null) {
                // If they aren't both null...
                if (tim.getEndDateTime() != null) {
                    inconsistencies.add(new ActiveTimError("End Time", tim.getEndDateTime(), feuEndTime));
                }
            } else {
                // feuEndTime isn't null, check if equal to tim's endDateTime
                if (!feuEndTime.equals(tim.getEndDateTime())) {
                    inconsistencies.add(new ActiveTimError("End Time", tim.getEndDateTime(), feuEndTime));
                }
            }

            LinkLocation feuLocation = getLocation(feu);
            if (feuLocation != null) {
                // Check Start Point
                if (!pointsInRange(feuLocation.getPrimaryLocation(), tim.getStartPoint())) {
                    inconsistencies.add(new ActiveTimError("Start Point", formatCoordinate(tim.getStartPoint()),
                            formatPointOnLink(feuLocation.getPrimaryLocation())));
                }

                // Check End Point
                if (!pointsInRange(feuLocation.getSecondaryLocation(), tim.getEndPoint())) {
                    inconsistencies.add(new ActiveTimError("End Point", formatCoordinate(tim.getEndPoint()),
                            formatPointOnLink(feuLocation.getSecondaryLocation())));
                }
            } else {
                // FEU doesn't have a start or end point...
                inconsistencies.add(new ActiveTimError("Start Point", formatCoordinate(tim.getStartPoint()), null));
                inconsistencies.add(new ActiveTimError("End Point", formatCoordinate(tim.getEndPoint()), null));
            }

            // Check ITIS Codes
            List<EventDescription> feuEds = getEventDescriptions(feu);
            List<Integer> feuItisCodes = getNumericItisCodes(feuEds);
            if (!correctItisCodes(tim.getItisCodes(), feuItisCodes)) {
                inconsistencies.add(new ActiveTimError("ITIS Codes", formatItisCodes(tim.getItisCodes()),
                        formatItisCodes(feuItisCodes)));
            }

            if (inconsistencies.size() > 0) {
                ActiveTimValidationResult result = new ActiveTimValidationResult();
                result.setActiveTim(tim);
                result.setErrors(inconsistencies);

                validationResults.add(result);
            }
        }

        if (unableToVerify.size() > 0 || validationResults.size() > 0) {
            var exceptions = cleanupData(unableToVerify);
            String email = emailFormatter.generateTmddSummaryEmail(unableToVerify, validationResults);

            try {
                mailHelper.SendEmail(config.getAlertAddresses(), null, "TMDD Validation Results", email,
                        config.getMailPort(), config.getMailHost(), config.getFromEmail());
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

    private String getStartTime(FullEventUpdate feu) {
        String sTime = null;

        if (feu != null && feu.getMessageHeader() != null && feu.getMessageHeader().getMessageTimeStamp() != null) {
            sTime = feu.getMessageHeader().getMessageTimeStamp().asDateTimeString();
        }

        return sTime;
    }

    private String getEndTime(FullEventUpdate feu) {
        String sTime = null;

        // MessageExpiryTime is optional
        if (feu != null && feu.getMessageHeader() != null && feu.getMessageHeader().getMessageExpiryTime() != null) {
            sTime = feu.getMessageHeader().getMessageExpiryTime().asDateTimeString();
        }

        return sTime;
    }

    private LinkLocation getLocation(FullEventUpdate feu) {
        LinkLocation location = null;

        if (feu != null && feu.getEventElementDetails() != null && feu.getEventElementDetails().size() > 0
                && feu.getEventElementDetails().get(0) != null
                && feu.getEventElementDetails().get(0).getEventLocations() != null
                && feu.getEventElementDetails().get(0).getEventLocations().size() > 0
                && feu.getEventElementDetails().get(0).getEventLocations().get(0) != null) {

            location = feu.getEventElementDetails().get(0).getEventLocations().get(0).getLocationOnLink();
        }

        return location;
    }

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

    private boolean pointsInRange(PointOnLink tmddPoint, Coordinate timPoint) {
        if (tmddPoint == null || tmddPoint.getGeoLocation() == null || timPoint == null) {
            return false;
        }

        double tmddLat = tmddPoint.getGeoLocation().getLatitude() / 1000000.0;
        double tmddLon = tmddPoint.getGeoLocation().getLongitude() / 1000000.0;

        GlobalCoordinates tmdd = new GlobalCoordinates(tmddLat, tmddLon);
        GlobalCoordinates tim = new GlobalCoordinates(timPoint.getLatitude().doubleValue(),
                timPoint.getLongitude().doubleValue());

        GeodeticCalculator geoCalc = new GeodeticCalculator();
        GeodeticCurve curve = geoCalc.calculateGeodeticCurve(Ellipsoid.WGS84, tmdd, tim);
        double miles = 0.000621371 * curve.getEllipsoidalDistance();

        // Are they within 1/5th of a mile?
        return miles < 0.2;
    }

    private String formatPointOnLink(PointOnLink point) {
        if (point == null || point.getGeoLocation() == null) {
            return null;
        }

        double lat = point.getGeoLocation().getLatitude() / 1000000.0;
        double lon = point.getGeoLocation().getLongitude() / 1000000.0;

        return formatPoint(lat, lon);
    }

    private String formatCoordinate(Coordinate point) {
        if (point == null) {
            return null;
        }

        return formatPoint(point.getLatitude().doubleValue(), point.getLongitude().doubleValue());
    }

    private String formatPoint(double lat, double lon) {
        return String.format("{ lat: %.6f, lon: %.6f }", lat, lon);
    }

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

    private String cleanupData(List<ActiveTim> unableToVerify) {
        // TODO: delete unableToVerify, update and resend others
        String deleteError = "";
        if (unableToVerify.size() > 0) {
            deleteError = deleteActiveTims(unableToVerify);
        }
        return deleteError;
    }

    private String deleteActiveTims(List<ActiveTim> unableToVerify) {
        return null;
        // wydotTimService.deleteTimsFromRsusAndSdx(unableToVerify);
    }

}