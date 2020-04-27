package com.trihydro.tasks;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.JavaMailSenderImplProvider;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.library.service.RsuDataService;
import com.trihydro.library.service.SdwService;
import com.trihydro.tasks.actions.CleanupActiveTims;
import com.trihydro.tasks.actions.RemoveExpiredActiveTims;
import com.trihydro.tasks.actions.RetentionPolicyEnforcement;
import com.trihydro.tasks.actions.ValidateRsus;
import com.trihydro.tasks.actions.ValidateSDX;
import com.trihydro.tasks.config.DataTasksConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ SdwService.class, Utility.class, EmailHelper.class, JavaMailSenderImplProvider.class, ActiveTimService.class,
                RsuDataService.class, RestTemplateProvider.class })
public class Application {
        protected static DataTasksConfiguration config;

        private RemoveExpiredActiveTims removeExpiredActiveTims;
        private CleanupActiveTims cleanupActiveTims;
        private RetentionPolicyEnforcement retentionEnforcement;
        private ValidateSDX sdxValidator;
        private ValidateRsus rsuValidator;

        @Autowired
        public void InjectDependencies(DataTasksConfiguration _config, RemoveExpiredActiveTims _removeExpiredActiveTims,
                        CleanupActiveTims _cleanupActiveTims, ValidateSDX _sdxValidator, ValidateRsus _rsuValidator,
                        RetentionPolicyEnforcement _retentionEnforcement) {
                config = _config;
                removeExpiredActiveTims = _removeExpiredActiveTims;
                cleanupActiveTims = _cleanupActiveTims;
                sdxValidator = _sdxValidator;
                rsuValidator = _rsuValidator;
                retentionEnforcement = _retentionEnforcement;
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
                scheduledExecutorService.scheduleAtFixedRate(sdxValidator, 10, config.getSdxValidationPeriodMinutes(),
                                TimeUnit.MINUTES);

                // RSU Validator
                // Since we're validating Active Tims from both environments in the same task,
                // we only want this running in 1 environment, or else we'll receive duplicate
                // emails
                if (config.getRunRsuValidation()) {
                        scheduledExecutorService.scheduleAtFixedRate(rsuValidator, 15,
                                        config.getRsuValidationPeriodMinutes(), TimeUnit.MINUTES);
                }

                // Retention Policy Enforcement
                scheduledExecutorService.scheduleAtFixedRate(retentionEnforcement, 20,
                                config.getRetentionEnforcementPeriodMinutes(), TimeUnit.MINUTES);
        }
}