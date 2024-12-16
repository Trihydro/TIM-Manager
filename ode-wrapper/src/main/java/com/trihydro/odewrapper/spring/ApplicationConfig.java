package com.trihydro.odewrapper.spring;

import com.trihydro.library.helpers.CreateBaseTimUtil;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.JavaMailSenderImplProvider;
import com.trihydro.library.helpers.MilepostReduction;
import com.trihydro.library.helpers.RegionNameTrimmer;
import com.trihydro.library.helpers.SnmpHelper;
import com.trihydro.library.helpers.TimGenerationHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.ActiveTimHoldingService;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.DataFrameService;
import com.trihydro.library.service.IncidentChoicesService;
import com.trihydro.library.service.ItisCodeService;
import com.trihydro.library.service.LoggingService;
import com.trihydro.library.service.MilepostService;
import com.trihydro.library.service.OdeService;
import com.trihydro.library.service.PathNodeLLService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.SdwService;
import com.trihydro.library.service.TimRsuService;
import com.trihydro.library.service.TimService;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.library.service.WydotTimService;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Import({ ActiveTimService.class,
        ActiveTimHoldingService.class,
        CreateBaseTimUtil.class,
        DataFrameService.class,
        EmailHelper.class,
        ItisCodeService.class,
        IncidentChoicesService.class,
        JavaMailSenderImplProvider.class,
        LoggingService.class,
        MilepostReduction.class,
        MilepostService.class,
        OdeService.class,
        PathNodeLLService.class,
        RestTemplateProvider.class,
        RsuService.class,
        SdwService.class,
        SnmpHelper.class,
        TimRsuService.class,
        TimService.class,
        TimTypeService.class,
        TimGenerationHelper.class,
        Utility.class,
        WydotTimService.class,
        RegionNameTrimmer.class })
public class ApplicationConfig implements WebMvcConfigurer {
    public ApplicationConfig() {
        super();
    }
}