package com.trihydro.cvdatacontroller.controller;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.service.ActiveTimService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@CrossOrigin
@RestController
public class TimController {

    @RequestMapping(value = "/active-tims", method = RequestMethod.GET, headers = "Accept=application/json")
    public List<ActiveTim> selectAllActiveTims() throws Exception {
        List<ActiveTim> activeTims = ActiveTimService.getAllActiveTims();
        return activeTims;
    }

}
