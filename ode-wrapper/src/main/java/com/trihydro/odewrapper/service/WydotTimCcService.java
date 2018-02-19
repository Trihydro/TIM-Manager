package com.trihydro.odewrapper.service;

import com.trihydro.odewrapper.model.WydotTimRc;
import com.trihydro.service.rsu.RsuService;
import com.trihydro.service.model.ActiveTim;
import com.trihydro.service.tim.ActiveTimLogger;
import com.trihydro.service.tim.TimLogger;
import com.trihydro.service.itiscode.ItisCodeService;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.ArrayList;
import com.trihydro.service.model.WydotRsu;
import com.trihydro.service.model.ItisCode;
import com.trihydro.service.model.TimType;
import org.springframework.web.client.RestTemplate;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import org.springframework.core.env.Environment;
import com.trihydro.odewrapper.helpers.DBUtility;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;
import com.trihydro.odewrapper.model.WydotTravelerInputData;
import com.trihydro.service.tim.TimRsuLogger;
import com.trihydro.service.model.TimRsu;

@Component
public class WydotTimCcService extends WydotTimService
{   
    

}