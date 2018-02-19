package com.trihydro.odewrapper.controller;

import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@ApiIgnore
public class WydotTimBaseController {
    public String jsonKeyValue(String key, String value) {
        return "{\"" + key + "\":" + value + "}";
    }
}
