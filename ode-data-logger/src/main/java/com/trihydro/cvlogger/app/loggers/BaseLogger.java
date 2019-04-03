package com.trihydro.cvlogger.app.loggers;

import java.util.List;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.model.TimType;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.service.ItisCodeService;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.TimTypeService;

public class BaseLogger {

    private static List<ItisCode> itisCodes;
    private static List<TimType> timTypes;
    private static List<WydotRsu> rsus;

    public static List<ItisCode> getItisCodes() {
        if (itisCodes == null) {
            itisCodes = ItisCodeService.selectAll();
        }
        return itisCodes;
    }

    public static List<TimType> getTimTypes() {
        if (timTypes == null) {
            timTypes = TimTypeService.selectAll();
        }
        return timTypes;
    }

    public static List<WydotRsu> getRsus() {
        if (rsus == null) {
            rsus = RsuService.selectAll();
        }
        return rsus;
    }

}