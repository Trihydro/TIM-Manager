package com.trihydro.odewrapper.controller;

import com.trihydro.library.helpers.MilepostReduction;
import com.trihydro.library.helpers.TimGenerationHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ContentEnum;
import com.trihydro.library.model.CountyRoadSegment;
import com.trihydro.library.model.TimType;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.CascadeService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.library.service.WydotTimService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.helpers.SetItisCodes;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.FrameType.TravelerInfoType;

@CrossOrigin
@RestController
@Api(description = "County Roads")
public class WydotCountyRoadsController extends WydotTimBaseController {

    @Autowired
    public WydotCountyRoadsController(BasicConfiguration _basicConfiguration, WydotTimService _wydotTimService,
            TimTypeService _timTypeService, SetItisCodes _setItisCodes, ActiveTimService _activeTimService,
            RestTemplateProvider _restTemplateProvider, MilepostReduction _milepostReduction, Utility _utility,
            TimGenerationHelper _timGenerationHelper, CascadeService _cascadeService) {
        super(_basicConfiguration, _wydotTimService, _timTypeService, _setItisCodes, _activeTimService,
                _restTemplateProvider, _milepostReduction, _utility, _timGenerationHelper, _cascadeService);
    }

    @RequestMapping(value = "/cascade-conditions-for-segment/{segmentId}", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> cascadeConditions(@PathVariable int segmentId) {
        utility.logWithDate("Cascade Conditions for Segment: " + segmentId);

        CountyRoadSegment countyRoadSegment = cascadeService.retrieveCountyRoadSegment(segmentId);
        if (countyRoadSegment == null) {
            String responseMessage = "County Road Segment not found";
            return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
        }

        // need CountyRoadSegment countyRoadSegment, TimType timType, String startDateTime, String endDateTime, Integer pk, ContentEnum content, TravelerInfoType frameType, String clientId
        TimType timType = getTimType("RC");
        String startDateTime = getStartTime();
        String endDateTime = null;
        Integer pk = 0;
        ContentEnum content = ContentEnum.advisory;
        TravelerInfoType frameType = TravelerInfoType.advisory;
        String clientId = "adhoc-" + segmentId;

        // handle cascading conditions
        cascadeConditionsForSegmentAsync(countyRoadSegment, timType, startDateTime, endDateTime, pk, content, frameType, clientId);

        String responseMessage = "success";
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @RequestMapping(value = "/clear-conditions-for-segment/{segmentId}", method = RequestMethod.PUT, headers = "Accept=application/json")
    public void clearConditionsAssociatedWithCountyRoadSegment(@PathVariable int segmentId) {
        List<ActiveTim> allActiveTimsWithItisCodesAssociatedWithSegment = cascadeService.getActiveTimsWithItisCodesAssociatedWithSegment(segmentId);
        List<String> clientIdsAssociatedWithSegment = allActiveTimsWithItisCodesAssociatedWithSegment.stream().map(ActiveTim::getClientId).collect(Collectors.toList());
        TimType timType = getTimType("RC");
        for (String clientIdToClear : clientIdsAssociatedWithSegment) {
            // clear exiting conditions
            wydotTimService.clearTimsById(timType.getType(), clientIdToClear, null);
        }
    }

    private void cascadeConditionsForSegmentAsync(CountyRoadSegment countyRoadSegment, TimType timType, String startDateTime, String endDateTime, Integer pk, ContentEnum content, TravelerInfoType frameType, String clientId) {
        // An Async task always executes in new thread
        new Thread(new Runnable() {
            public void run() {
                utility.logWithDate("=================== ADHOC CRC Start ===================");
                cascadeConditionsForSegment(countyRoadSegment, timType, startDateTime, endDateTime, pk, content, frameType, clientId);
                utility.logWithDate("=================== ADHOC CRC End ===================");
            }
        }).start();
    }
}
