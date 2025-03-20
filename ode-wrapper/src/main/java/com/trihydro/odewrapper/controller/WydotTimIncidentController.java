package com.trihydro.odewrapper.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import com.trihydro.library.helpers.MilepostReduction;
import com.trihydro.library.helpers.TimGenerationHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ContentEnum;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.library.service.WydotTimService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.helpers.SetItisCodes;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.TimIncidentList;
import com.trihydro.odewrapper.model.WydotTimIncident;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.FrameType.TravelerInfoType;

@CrossOrigin
@RestController
@Api(description = "Incidents")
@Slf4j
public class WydotTimIncidentController extends WydotTimBaseController {

  private final String type = "I";

  @Autowired
  public WydotTimIncidentController(BasicConfiguration _basicConfiguration, WydotTimService _wydotTimService, TimTypeService _timTypeService,
                                    SetItisCodes _setItisCodes, ActiveTimService _activeTimService, RestTemplateProvider _restTemplateProvider,
                                    MilepostReduction _milepostReduction, Utility _utility, TimGenerationHelper _timGenerationHelper) {
    super(_basicConfiguration, _wydotTimService, _timTypeService, _setItisCodes, _activeTimService, _restTemplateProvider, _milepostReduction,
        _utility, _timGenerationHelper);
  }

  @RequestMapping(value = "/incident-tim", method = RequestMethod.POST, headers = "Accept=application/json")
  public ResponseEntity<String> createIncidentTim(@RequestBody TimIncidentList timIncidentList) {
    log.info("Create Incident TIM");
    String post = gson.toJson(timIncidentList);
    log.info(post);

    List<WydotTimIncident> timsToSend = new ArrayList<>();

    List<ControllerResult> resultList = new ArrayList<>();
    ControllerResult resultTim;

    // build TIM
    for (WydotTimIncident wydotTim : timIncidentList.getTimIncidentList()) {

      resultTim = validateInputIncident(wydotTim);

      if (!resultTim.getResultMessages().isEmpty()) {
        resultList.add(resultTim);
        continue;
      }

      // make tims
      timsToSend.add(wydotTim);

      resultTim.getResultMessages().add("success");
      resultList.add(resultTim);
    }

    makeTimsAsync(timsToSend);

    String responseMessage = gson.toJson(resultList);
    return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
  }

  @RequestMapping(value = "/incident-tim", method = RequestMethod.PUT, headers = "Accept=application/json")
  public ResponseEntity<String> updateIncidentTim(@RequestBody TimIncidentList timIncidentList) {
    log.info("Update Incident TIM");
    String post = gson.toJson(timIncidentList);
    log.info(post);

    List<ControllerResult> resultList = new ArrayList<>();
    ControllerResult resultTim;
    List<WydotTimIncident> timsToSend = new ArrayList<>();

    // delete TIMs
    for (WydotTimIncident wydotTim : timIncidentList.getTimIncidentList()) {
      resultTim = validateInputIncident(wydotTim);
      if (!resultTim.getResultMessages().isEmpty()) {
        resultList.add(resultTim);
        continue;
      }
      // make tims
      timsToSend.add(wydotTim);
      resultTim.getResultMessages().add("success");
      resultList.add(resultTim);
    }

    if (!timsToSend.isEmpty()) {
      // make tims, expire existing ones, and send them
      makeTimsAsync(timsToSend);
    }
    String responseMessage = gson.toJson(resultList);
    return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
  }

  public void makeTimsAsync(List<WydotTimIncident> wydotTims) {
    new Thread(() -> {
      var startTime = getStartTime();
      for (WydotTimIncident wydotTim : wydotTims) {
        // set route
        wydotTim.setRoute(wydotTim.getHighway());
        processRequest(wydotTim, getTimType(type), startTime, null, wydotTim.getPk(), ContentEnum.advisory, TravelerInfoType.advisory);
      }
    }).start();
  }

  @RequestMapping(value = "/incident-tim/{incidentId}", method = RequestMethod.DELETE, headers = "Accept=application/json")
  public ResponseEntity<String> deleteIncidentTim(@PathVariable String incidentId) {
    log.info("Delete Incident TIM");

    // expire and clear TIM
    wydotTimService.clearTimsById("I", incidentId, null);
    String responseMessage = "success";
    return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
  }

  @RequestMapping(value = "/incident-tim", method = RequestMethod.GET, headers = "Accept=application/json")
  public Collection<ActiveTim> getIncidentTims() {
    // get active TIMs
    return wydotTimService.selectTimsByType("I");
  }

  @RequestMapping(value = "/incident-tim/{incidentId}", method = RequestMethod.GET, headers = "Accept=application/json")
  public Collection<ActiveTim> getIncidentTimById(@PathVariable String incidentId) {
    // get active TIMs
    return wydotTimService.selectTimByClientId("I", incidentId);
  }
}
