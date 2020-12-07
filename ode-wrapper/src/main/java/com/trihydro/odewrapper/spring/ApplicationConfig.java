package com.trihydro.odewrapper.spring;

import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.JavaMailSenderImplProvider;
import com.trihydro.library.helpers.MilepostReduction;
import com.trihydro.library.helpers.TimGenerationHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.ActiveTimHoldingService;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.IncidentChoicesService;
import com.trihydro.library.service.ItisCodeService;
import com.trihydro.library.service.LoggingService;
import com.trihydro.library.service.MilepostService;
import com.trihydro.library.service.OdeService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.SdwService;
import com.trihydro.library.service.TimRsuService;
import com.trihydro.library.service.TimService;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.odewrapper.service.WydotTimService;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Import({ ActiveTimService.class, ActiveTimHoldingService.class, OdeService.class, SdwService.class,
        TimTypeService.class, MilepostService.class, TimRsuService.class, WydotTimService.class, RsuService.class,
        TimService.class, ItisCodeService.class, IncidentChoicesService.class, MilepostReduction.class,
        RestTemplateProvider.class, LoggingService.class, Utility.class, EmailHelper.class,
        JavaMailSenderImplProvider.class, TimGenerationHelper.class })
public class ApplicationConfig implements WebMvcConfigurer {
    public ApplicationConfig() {
        super();
    }
}