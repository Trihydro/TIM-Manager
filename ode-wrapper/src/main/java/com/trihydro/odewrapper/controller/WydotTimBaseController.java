package com.trihydro.odewrapper.controller;

import com.google.gson.Gson;
import com.trihydro.odewrapper.service.WydotTimService;

import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@ApiIgnore
public class WydotTimBaseController {
    
     // services   
     protected final WydotTimService wydotTimService;
     protected static Gson gson = new Gson();
    
    WydotTimBaseController() {
         this.wydotTimService = new WydotTimService();
    }

    
    
    public String jsonKeyValue(String key, String value) {
        return "{\"" + key + "\":" + value + "}";
    }


}
