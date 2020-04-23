package com.trihydro.tasks.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.tmdd.FullEventUpdate;
import com.trihydro.library.model.tmdd.LinkLocation;
import com.trihydro.library.model.tmdd.PointOnLink;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.TmddService;
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
    private TmddService tmddService;
    private ActiveTimService activeTimService;
    private Utility utility;
    private IdNormalizer idNormalizer;

    @Autowired
    public void InjectDependencies(TmddService tmddService, ActiveTimService activeTimService, Utility utility,
            IdNormalizer idNormalizer) {
        this.tmddService = tmddService;
        this.activeTimService = activeTimService;
        this.utility = utility;
        this.idNormalizer = idNormalizer;
    }

    public void run() {
        utility.logWithDate("Running...", this.getClass());

        try {
            validateTmdd();
        } catch (Exception ex) {
            utility.logWithDate("Error while validating Oracle with TMDD:", this.getClass());
            ex.printStackTrace();
            // don't rethrow error, or the task won't be reran until the service is
            // restarted.
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
            return;
        }

        // Get ActiveTims (for both RSUs and SDX)
        List<ActiveTim> activeTims = null;
        try {
            activeTims = activeTimService.getActiveTimsWithItisCodes();
        } catch (Exception ex) {
            utility.logWithDate("Error fetching Active Tims:", this.getClass());
            ex.printStackTrace();
            return;
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
            // ActiveTimValidationResult inconsistencies = new ActiveTimValidationResult();
            // inconsistencies.setActiveTim(tim);

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

            LinkLocation location = getLocation(feu);
            if (location != null) {
                // Check Start Point
                if (!pointsInRange(location.getPrimaryLocation(), tim.getStartPoint())) {
                    inconsistencies.add(new ActiveTimError("Start Point", formatCoordinate(tim.getStartPoint()),
                            formatPointOnLink(location.getPrimaryLocation())));
                }

                // Check End Point
                if (!pointsInRange(location.getSecondaryLocation(), tim.getEndPoint())) {
                    inconsistencies.add(new ActiveTimError("End Point", formatCoordinate(tim.getEndPoint()),
                            formatPointOnLink(location.getSecondaryLocation())));
                }
            } else {
                // FEU doesn't have a start or end point...
                inconsistencies.add(new ActiveTimError("Start Point", formatCoordinate(tim.getStartPoint()), null));
                inconsistencies.add(new ActiveTimError("End Point", formatCoordinate(tim.getEndPoint()), null));
            }

            // TODO: verify ITIS codes

            if (inconsistencies.size() > 0) {
                ActiveTimValidationResult result = new ActiveTimValidationResult();
                result.setActiveTim(tim);
                result.setErrors(inconsistencies);

                validationResults.add(result);
            }
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

    private boolean pointsInRange(PointOnLink tmddPoint, Coordinate timPoint) {
        if (tmddPoint == null || tmddPoint.getGeoLocation() == null || timPoint == null) {
            return false;
        }

        double tmddLat = tmddPoint.getGeoLocation().getLatitude() / 1000000;
        double tmddLon = tmddPoint.getGeoLocation().getLongitude() / 1000000;

        GlobalCoordinates tmdd = new GlobalCoordinates(tmddLat, tmddLon);
        GlobalCoordinates tim = new GlobalCoordinates(timPoint.getLatitude(), timPoint.getLongitude());

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

        double lat = point.getGeoLocation().getLatitude() / 1000000;
        double lon = point.getGeoLocation().getLongitude() / 1000000;

        return formatPoint(lat, lon);
    }

    private String formatCoordinate(Coordinate point) {
        if (point == null) {
            return null;
        }

        return formatPoint(point.getLatitude(), point.getLongitude());
    }

    private String formatPoint(double lat, double lon) {
        return String.format("{ lat: %.6f, lon: %.6f }", lat, lon);
    }
}