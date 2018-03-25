package com.trihydro.cvlogger.app.loggers;

import java.util.List;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.model.TimType;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.service.ItisCodeService;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.TimTypeService;

public class BaseLogger {
    
    protected static List<ItisCode> itisCodes;
    protected static List<TimType> timTypes;
    protected static List<WydotRsu> rsus;

    static{
        itisCodes = ItisCodeService.selectAll(); 
        timTypes = TimTypeService.selectAll();
        rsus = RsuService.selectAll(); 
    }    
	
}