package com.trihydro.library.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.CountyRoadSegment;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.TriggerRoad;
import com.trihydro.library.model.WydotTim;

@Component
public class CascadeService extends CvDataServiceLibrary {
    private static final String closedItisCode = "770";
    private static final String c2lhpvItisCode = "Closed to light, high profile vehicles";
    private static final String loctItisCode = "2567";
    private static final String nttItisCode = "2568";

    private static final List<String> cascadingItisCodes = new ArrayList<String>() {
        {
            add(closedItisCode);
            add(c2lhpvItisCode);
            add(loctItisCode);
            add(nttItisCode);
        }
    };

    /**
     * This method sends a request to the CV Data Controller to get the trigger road for the given road code.
     * @param roadCode the road code to get the trigger road for
     * @return the trigger road for the given road code (or null if none exists)
     */
    public TriggerRoad getTriggerRoad(String roadCode) {
        String cvRestService = config.getCvRestService();
        String url = String.format("%s/cascade/trigger-road/%s", cvRestService, roadCode);
        RestTemplate restTemplate = restTemplateProvider.GetRestTemplate();
        ResponseEntity<TriggerRoad> response = restTemplate.exchange(url, HttpMethod.GET, null, TriggerRoad.class);
        return response.getBody();
    }

    /**
     * This method sends a request to the CV Data Controller to get the mileposts for the given segment.
     * @param countyRoadSegment the segment to get mileposts for
     * @return a list of mileposts for the given segment
     */
    public List<Milepost> getMilepostsForSegment(CountyRoadSegment countyRoadSegment) {
        return getMilepostsForSegment(countyRoadSegment.getId());
    }

    /**
     * This method sends a request to the CV Data Controller to get the mileposts for the given segment.
     * @param segmentId the id of the segment to get mileposts for
     * @return a list of mileposts for the given segment
     */
    public List<Milepost> getMilepostsForSegment(int segmentId) {
        String cvRestService = config.getCvRestService();
        String url = String.format("%s/cascade/mileposts/%s", cvRestService, segmentId);
        RestTemplate restTemplate = restTemplateProvider.GetRestTemplate();
        ResponseEntity<Milepost[]> response = restTemplate.exchange(url, HttpMethod.GET, null, Milepost[].class);
        return Arrays.asList(response.getBody());
    }

    /**
     * This method checks if the TIM contains any cascading conditions. Returns true if it does, false otherwise.
     */
    public boolean containsCascadingCondition(WydotTim wydotTim) {
        return wydotTim.getItisCodes().stream().anyMatch(cascadingItisCodes::contains);
    }

    /**
     * This method builds a WydotTim for one or more cascading conditions associated with a segment.
     * @param countyRoadSegment The segment that has the condition(s)
     * @param anchor The first milepost in the segment
     * @param lastMilepost The last milepost in the segment
     * @param clientId The client ID of the original TIM
     * @return A WydotTim that represents the cascading condition(s) for the segment
     */
    public WydotTim buildCascadeTim(CountyRoadSegment countyRoadSegment, Milepost anchor, Milepost lastMilepost, String clientId) {
        WydotTim toReturn = new WydotTim();
        // note: direction not set because it's not applicable to cascading conditions
        toReturn.setStartPoint(new Coordinate(anchor.getLatitude(), anchor.getLongitude()));
        toReturn.setEndPoint(new Coordinate(lastMilepost.getLatitude(), lastMilepost.getLongitude()));
        toReturn.setRoute(countyRoadSegment.getCommonName());
        List<String> itisCodes = new ArrayList<String>();
        if (countyRoadSegment.isClosed()) {
            itisCodes.add(closedItisCode);
        }
        if (countyRoadSegment.isC2lhpv()) {
            itisCodes.add(c2lhpvItisCode);
        }
        if (countyRoadSegment.isLoct()) {
            itisCodes.add(loctItisCode);
        }
        if (countyRoadSegment.isNtt()) {
            itisCodes.add(nttItisCode);
        }
        toReturn.setItisCodes(itisCodes);
        toReturn.setClientId(clientId + "_triggered_" + countyRoadSegment.getId());
        return toReturn;
    }
}
