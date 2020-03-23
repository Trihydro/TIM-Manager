package com.trihydro.rsudatacontroller.controller;

import java.util.List;

import com.trihydro.rsudatacontroller.model.RsuTim;
import com.trihydro.rsudatacontroller.service.RsuService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("rsu")
public class RsuTimController {
    private RsuService rsuService;

    @Autowired
    public void InjectDependencies(RsuService rsuService) {
        this.rsuService = rsuService;
    }

    /**
     * Fetches the delivery start times for each TIM at each index on the RSU
     * 
     * @param ipv4Address Address of RSU
     * @return HTTP 200 - Success, HTTP 400 - Bad Request, HTTP 422 - Unprocessable
     *         Entity (timeout when communicating with RSU), HTTP 500 - (unable to
     *         invoke SNMP due to poorly formatted command)
     */
    @RequestMapping(value = "/{rsuAddress}/tims/delivery-start", method = RequestMethod.GET)
    public ResponseEntity<List<RsuTim>> GetRsuTimsDeliveryStart(@PathVariable("rsuAddress") String ipv4Address) {
        List<RsuTim> results = null;

        if (!ipv4Address.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")) {
            return ResponseEntity.badRequest().body(null);
        }

        try {
            results = rsuService.getAllDeliveryStartTimes(ipv4Address);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(null);
        }

        if (results == null) {
            return ResponseEntity.unprocessableEntity().body(null);
        }

        return ResponseEntity.ok(results);
    }
}