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
    /**
     * This is the delimiter used to separate the original client ID
     * from the segment ID in the client ID of a cascading TIM.
     */
    public static final String CASCADE_TIM_ID_DELIMITER = "_trgd_";

    private static final String closedItisCode = "769";
    private static final String c2lhpvItisCode = "2569";
    private static final String loctItisCode = "2567";
    private static final String nttItisCode = "2568";

    /**
     * This method sends a request to the CV Data Controller to get the trigger road for the given road code.
     * @param roadCode the road code to get the trigger road for
     * @return the trigger road for the given road code (or null if none exists)
     */
    public TriggerRoad getTriggerRoad(String roadCode) {
        String cvRestService = config.getCvRestService();
        String url = String.format("%s/cascade/trigger-road/%s", cvRestService, roadCode);
        RestTemplate restTemplate = restTemplateProvider.GetRestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        TriggerRoad toReturn = TriggerRoad.fromJson(response.getBody());
        return toReturn;
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
     * This method builds a WydotTim for one or more cascading conditions associated with a segment.
     * @param countyRoadSegment The segment that has the condition(s)
     * @param firstMilepost The first milepost in the segment
     * @param lastMilepost The last milepost in the segment
     * @param clientId The client ID of the original TIM
     * @return A WydotTim that represents the cascading condition(s) for the segment
     */
    public WydotTim buildCascadeTim(CountyRoadSegment countyRoadSegment, Milepost firstMilepost, Milepost lastMilepost, String clientId) {
        WydotTim toReturn = new WydotTim();
        toReturn.setDirection("1111111111111111"); // direction not applicable but must be set in order to insert into active tim holding table
        toReturn.setStartPoint(new Coordinate(firstMilepost.getLatitude(), firstMilepost.getLongitude()));
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
        toReturn.setClientId(clientId + CASCADE_TIM_ID_DELIMITER + countyRoadSegment.getId());
        return toReturn;
    }

    /**
     * This method checks if the given TIM is a cascade TIM.
     * @param wydotTim The TIM to check
     * @return true if the TIM is a cascade TIM, false otherwise
     */
    public static boolean isCascadeTim(WydotTim wydotTim) {
        String clientId = wydotTim.getClientId();
        if (clientId == null) {
            return false;
        }
        return clientId.contains(CASCADE_TIM_ID_DELIMITER);
    }

    /**
     * This method gets all mileposts for a cascade TIM.
     * @param cascadeTim The cascade TIM to get mileposts for
     * @return A list of mileposts for the cascade TIM
     * @throws ArrayIndexOutOfBoundsException if the client ID of the cascade TIM is not in the expected format
     * @throws NumberFormatException if the segment ID in the client ID of the cascade TIM is not an integer
     */
    public List<Milepost> getAllMilepostsFromCascadeTim(WydotTim cascadeTim) throws ArrayIndexOutOfBoundsException, NumberFormatException {
        List<Milepost> allMps = new ArrayList<>();
        String countyRoadIdString = cascadeTim.getClientId().split(CASCADE_TIM_ID_DELIMITER)[1];
        if (countyRoadIdString == null) {
            return allMps;
        }
        int countyRoadId = Integer.parseInt(countyRoadIdString);
        allMps = getMilepostsForSegment(countyRoadId);
        return allMps;
    }
}
