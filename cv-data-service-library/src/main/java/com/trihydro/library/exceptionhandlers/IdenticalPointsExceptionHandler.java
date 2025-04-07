package com.trihydro.library.exceptionhandlers;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.Milepost;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IdenticalPointsExceptionHandler {
    private final Utility utility;

    public IdenticalPointsExceptionHandler(Utility utility) {
        this.utility = utility;
    }

    /**
     * Attempts to recover from an identical points exception by removing the first milepost
     * and re-evaluating the remaining mileposts. If recovery is not possible due to insufficient
     * mileposts or repeated identical points, returns null.
     *
     * @param allMps The list of Mileposts to process. The list must contain at least three mileposts
     *               to attempt recovery.
     * @return The anchor point Milepost if recovery is successful, or null if recovery fails.
     */
    public Milepost recover(List<Milepost> allMps) {
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
            Coordinate anchorCoordinate = utility.calculateAnchorCoordinate(firstPoint, secondPoint);
            return new Milepost(null, firstPoint.getMilepost(), firstPoint.getDirection(),
                anchorCoordinate.getLatitude(), anchorCoordinate.getLongitude());
        } catch (Utility.IdenticalPointsException e2) {
            log.warn("Unable to recover from identical points exception for active TIM, first three mileposts are identical.");
            return null;
        }
    }
}
