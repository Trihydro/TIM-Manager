package com.trihydro.tasks;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.trihydro.library.helpers.CreateBaseTimUtil;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.GsonFactory;
import com.trihydro.library.helpers.JavaMailSenderImplProvider;
import com.trihydro.library.helpers.MilepostReduction;
import com.trihydro.library.helpers.RegionNameTrimmer;
import com.trihydro.library.helpers.SnmpHelper;
import com.trihydro.library.helpers.TimGenerationHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.ActiveTimHoldingService;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.DataFrameService;
import com.trihydro.library.service.ItisCodeService;
import com.trihydro.library.service.MilepostService;
import com.trihydro.library.service.OdeService;
import com.trihydro.library.service.PathNodeLLService;
import com.trihydro.library.service.RegionService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.library.service.RsuDataService;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.SdwService;
import com.trihydro.library.service.StatusLogService;
import com.trihydro.library.service.TimRsuService;
import com.trihydro.library.service.TimService;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.library.service.TmddService;
import com.trihydro.library.service.WydotTimService;
import com.trihydro.tasks.actions.CleanupActiveTims;
import com.trihydro.tasks.actions.RemoveExpiredActiveTims;
import com.trihydro.tasks.actions.RetentionPolicyEnforcement;
import com.trihydro.tasks.actions.ValidateRsus;
import com.trihydro.tasks.actions.ValidateSdx;
import com.trihydro.tasks.actions.ValidateTmdd;
import com.trihydro.tasks.actions.VerifyHSMFunctional;
import com.trihydro.tasks.config.DataTasksConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ SdwService.class, Utility.class, EmailHelper.class, JavaMailSenderImplProvider.class, ActiveTimService.class,
                ItisCodeService.class, RsuDataService.class, RestTemplateProvider.class,
                TmddService.class, GsonFactory.class,
                StatusLogService.class, TimService.class, DataFrameService.class,
                TimGenerationHelper.class, PathNodeLLService.class, MilepostService.class, MilepostReduction.class,
                RegionService.class, RsuService.class, OdeService.class, ActiveTimHoldingService.class,
                WydotTimService.class, TimTypeService.class, CreateBaseTimUtil.class, TimRsuService.class,
                SnmpHelper.class, RegionNameTrimmer.class })

public class Application {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    protected static DataTasksConfiguration config;

        private RemoveExpiredActiveTims removeExpiredActiveTims;
        private CleanupActiveTims cleanupActiveTims;
        private ValidateSdx sdxValidator;
        private ValidateRsus rsuValidator;
        private ValidateTmdd tmddValidator;
        private VerifyHSMFunctional hsmFunctional;
        RetentionPolicyEnforcement retentionEnforcement;
        private Utility utility;

        @Autowired
        public void InjectDependencies(DataTasksConfiguration _config, RemoveExpiredActiveTims _removeExpiredActiveTims,
                        CleanupActiveTims _cleanupActiveTims, ValidateSdx _sdxValidator,
                        ValidateRsus _rsuValidator, ValidateTmdd _tmddValidator,
                        RetentionPolicyEnforcement _retentionEnforcement, VerifyHSMFunctional _hsmFunctional,
                        Utility _utility) {
                config = _config;
                removeExpiredActiveTims = _removeExpiredActiveTims;
                cleanupActiveTims = _cleanupActiveTims;
                sdxValidator = _sdxValidator;
                rsuValidator = _rsuValidator;
                tmddValidator = _tmddValidator;
                retentionEnforcement = _retentionEnforcement;
                hsmFunctional = _hsmFunctional;
                utility = _utility;
        }

        public static void main(String[] args) {
                SpringApplication.run(Application.class, args);
        }

        @PostConstruct
        public void run() throws IOException {
                ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);

                // Remove Expired Active Tims
                scheduledExecutorService.scheduleAtFixedRate(removeExpiredActiveTims, 0,
                                config.getRemoveExpiredPeriodMinutes(), TimeUnit.MINUTES);

                // Cleanup Active Tims
                scheduledExecutorService.scheduleAtFixedRate(cleanupActiveTims, 5, config.getCleanupPeriodMinutes(),
                                TimeUnit.MINUTES);

                // SDX Validator
                scheduledExecutorService.scheduleAtFixedRate(sdxValidator, 15, config.getSdxValidationPeriodMinutes(),
                                TimeUnit.MINUTES);

                // HSM Check
                if (config.getRunHsmCheck()) {
                    LOG.info("HSM check configured, scheduling...");
                    scheduledExecutorService.scheduleAtFixedRate(hsmFunctional, 0,
                                        config.getHsmFunctionalityMinutes(), TimeUnit.MINUTES);
                } else {
                    LOG.info("HSM check not configured, skipping...");
                }

                // RSU Validator
                // Since we're validating Active Tims from both environments in the same task,
                // we only want this running in 1 environment, or else we'll receive duplicate
                // emails
                if (config.getRunRsuValidation()) {
                        scheduledExecutorService.scheduleAtFixedRate(rsuValidator, 20,
                                        config.getRsuValidationPeriodMinutes(), TimeUnit.MINUTES);
                }

                // TMDD Validator
                // Since dev has many Active TIMs that aren't present on the TMDD,
                // we should only be running the TMDD validation in prod.
                if (config.getRunTmddValidation()) {
                        scheduledExecutorService.scheduleAtFixedRate(tmddValidator, 25,
                                        config.getTmddValidationPeriodMinutes(), TimeUnit.MINUTES);
                }

                // Retention Policy Enforcement
                scheduledExecutorService.scheduleAtFixedRate(retentionEnforcement, 30,
                                config.getRetentionEnforcementPeriodMinutes(), TimeUnit.MINUTES);
        }
}