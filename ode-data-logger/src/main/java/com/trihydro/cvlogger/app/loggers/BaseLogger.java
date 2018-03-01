package com.trihydro.cvlogger.app.loggers;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeLogMetadataReceived;
import us.dot.its.jpo.ode.model.OdeTimPayload;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import com.trihydro.cvlogger.app.converters.JsonToJavaConverter;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.model.TimType;
import com.trihydro.library.service.itiscode.ItisCodeService;
import com.trihydro.library.service.tim.ActiveTimItisCodeLogger;
import com.trihydro.library.service.tim.ActiveTimLogger;
import com.trihydro.library.service.tim.DataFrameItisCodeService;
import com.trihydro.library.service.tim.DataFrameService;
import com.trihydro.library.service.tim.NodeXYLogger;
import com.trihydro.library.service.tim.PathLogger;
import com.trihydro.library.service.tim.PathNodeXYLogger;
import com.trihydro.library.service.tim.RegionLogger;
import com.trihydro.library.service.tim.TimService;
import com.trihydro.library.service.timtype.TimTypeService;

public class BaseLogger {
    
    protected List<ItisCode> itisCodes;
    protected Connection connection;
    protected List<TimType> timTypes;

    public BaseLogger(Connection connection){
        this.itisCodes = ItisCodeService.selectAll(connection); 
        this.connection = connection;
        this.timTypes = TimTypeService.selectAll(connection);
    }    
	
}