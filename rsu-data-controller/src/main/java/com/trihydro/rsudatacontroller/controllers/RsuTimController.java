package com.trihydro.rsudatacontroller.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("rsu")
public class RsuTimController {

    @Autowired
    public void InjectDependencies() {

    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public ResponseEntity<String> GetRsuTimsDeliveryStart() {
        return ResponseEntity.ok("hello world");
    }
}