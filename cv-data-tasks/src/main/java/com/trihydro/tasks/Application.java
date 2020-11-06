package com.trihydro.tasks;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.GsonFactory;
import com.trihydro.library.helpers.JavaMailSenderImplProvider;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.BsmService;
import com.trihydro.library.service.DriverAlertService;
import com.trihydro.library.service.HmiLogService;
import com.trihydro.library.service.ItisCodeService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.library.service.RsuDataService;
import com.trihydro.library.service.SdwService;
import com.trihydro.library.service.StatusLogService;
import com.trihydro.library.service.TimService;
import com.trihydro.library.service.TmddService;
import com.trihydro.library.service.UtilityService;
import com.trihydro.tasks.actions.CleanupActiveTims;
import com.trihydro.tasks.actions.CleanupBsms;
import com.trihydro.tasks.actions.RemoveExpiredActiveTims;
import com.trihydro.tasks.actions.RetentionPolicyEnforcement;
import com.trihydro.tasks.actions.ValidateRsus;
import com.trihydro.tasks.actions.ValidateSdx;
import com.trihydro.tasks.actions.ValidateTmdd;
import com.trihydro.tasks.actions.VerifyHSMFunctional;
import com.trihydro.tasks.config.DataTasksConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ SdwService.class, Utility.class, EmailHelper.class, JavaMailSenderImplProvider.class, ActiveTimService.class,
                BsmService.class, ItisCodeService.class, RsuDataService.class, RestTemplateProvider.class,
                TmddService.class, GsonFactory.class, DriverAlertService.class, HmiLogService.class,
                StatusLogService.class, TimService.class, UtilityService.class })

public class Application {
        protected static DataTasksConfiguration config;

        private RemoveExpiredActiveTims removeExpiredActiveTims;
        private CleanupActiveTims cleanupActiveTims;
        private CleanupBsms cleanupBsms;
        private ValidateSdx sdxValidator;
        private ValidateRsus rsuValidator;
        private ValidateTmdd tmddValidator;
        private VerifyHSMFunctional hsmFunctional;
        RetentionPolicyEnforcement retentionEnforcement;

        @Autowired
        public void InjectDependencies(DataTasksConfiguration _config, RemoveExpiredActiveTims _removeExpiredActiveTims,
                        CleanupActiveTims _cleanupActiveTims, CleanupBsms _cleanupBsms, ValidateSdx _sdxValidator,
                        ValidateRsus _rsuValidator, ValidateTmdd _tmddValidator,
                        RetentionPolicyEnforcement _retentionEnforcement, VerifyHSMFunctional _hsmFunctional) {
                config = _config;
                removeExpiredActiveTims = _removeExpiredActiveTims;
                cleanupActiveTims = _cleanupActiveTims;
                cleanupBsms = _cleanupBsms;
                sdxValidator = _sdxValidator;
                rsuValidator = _rsuValidator;
                tmddValidator = _tmddValidator;
                retentionEnforcement = _retentionEnforcement;
                hsmFunctional = _hsmFunctional;
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

                // Cleanup BSMs
                scheduledExecutorService.scheduleAtFixedRate(cleanupBsms, 10, config.getBsmCleanupPeriodMinutes(),
                                TimeUnit.MINUTES);

                // SDX Validator
                scheduledExecutorService.scheduleAtFixedRate(sdxValidator, 15, config.getSdxValidationPeriodMinutes(),
                                TimeUnit.MINUTES);

                // HSM Check
                if (config.getRunHSMCheck()) {
                        scheduledExecutorService.scheduleAtFixedRate(hsmFunctional, 0,
                                        config.getHsmFunctionalityMinutes(), TimeUnit.MINUTES);
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