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

    @RequestMapping(value = "/{rsuAddress}/tims/delivery-start", method = RequestMethod.GET)
    public ResponseEntity<List<RsuTim>> GetRsuTimsDeliveryStart(@PathVariable("rsuAddress") String ipv4Address) {
        List<RsuTim> results = null;

        try {
            results = rsuService.getAllDeliveryStartTimes(ipv4Address);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(null);
        }

        return ResponseEntity.ok(results);
    }
}