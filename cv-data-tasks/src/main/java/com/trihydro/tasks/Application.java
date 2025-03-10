package com.trihydro.tasks;

import com.trihydro.tasks.actions.CleanupStaleActiveTimHoldingRecords;
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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({SdwService.class, Utility.class, EmailHelper.class, JavaMailSenderImplProvider.class,
    ActiveTimService.class, ItisCodeService.class, RsuDataService.class, RestTemplateProvider.class,
    TmddService.class, GsonFactory.class, StatusLogService.class, TimService.class,
    DataFrameService.class, TimGenerationHelper.class, PathNodeLLService.class,
    MilepostService.class, MilepostReduction.class, RegionService.class, RsuService.class,
    OdeService.class, ActiveTimHoldingService.class, WydotTimService.class, TimTypeService.class,
    CreateBaseTimUtil.class, TimRsuService.class, SnmpHelper.class, RegionNameTrimmer.class})

@Slf4j
public class Application {
    protected static DataTasksConfiguration config;

    private final RemoveExpiredActiveTims removeExpiredActiveTims;
    private final CleanupActiveTims cleanupActiveTims;
    private final ValidateSdx sdxValidator;
    private final ValidateRsus rsuValidator;
    private final ValidateTmdd tmddValidator;
    private final VerifyHSMFunctional hsmFunctional;
    private final RetentionPolicyEnforcement retentionEnforcement;
    private final CleanupStaleActiveTimHoldingRecords cleanupStaleActiveTimHoldingRecords;

    @Autowired
    public Application(DataTasksConfiguration config,
                       RemoveExpiredActiveTims removeExpiredActiveTims,
                       CleanupActiveTims cleanupActiveTims, ValidateSdx sdxValidator,
                       ValidateRsus rsuValidator, ValidateTmdd tmddValidator,
                       RetentionPolicyEnforcement retentionEnforcement,
                       VerifyHSMFunctional hsmFunctional,
                       CleanupStaleActiveTimHoldingRecords cleanupStaleActiveTimHoldingRecords) {
        Application.config = config;
        this.removeExpiredActiveTims = removeExpiredActiveTims;
        this.cleanupActiveTims = cleanupActiveTims;
        this.sdxValidator = sdxValidator;
        this.rsuValidator = rsuValidator;
        this.tmddValidator = tmddValidator;
        this.retentionEnforcement = retentionEnforcement;
        this.hsmFunctional = hsmFunctional;
        this.cleanupStaleActiveTimHoldingRecords = cleanupStaleActiveTimHoldingRecords;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void run() throws IOException {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);

        // Remove Expired Active Tims
        log.info("Scheduling Remove Expired Active Tims...");
        scheduledExecutorService.scheduleAtFixedRate(removeExpiredActiveTims, 0,
            config.getRemoveExpiredPeriodMinutes(), TimeUnit.MINUTES);

        // Cleanup Active Tims
        log.info("Scheduling Cleanup Active Tims...");
        scheduledExecutorService.scheduleAtFixedRate(cleanupActiveTims, 5,
            config.getCleanupPeriodMinutes(), TimeUnit.MINUTES);

        // SDX Validator
        log.info("Scheduling SDX Validator...");
        scheduledExecutorService.scheduleAtFixedRate(sdxValidator, 15,
            config.getSdxValidationPeriodMinutes(), TimeUnit.MINUTES);

        // HSM Check
        if (config.getRunHsmCheck()) {
            log.info("HSM check configured, scheduling...");
            scheduledExecutorService.scheduleAtFixedRate(hsmFunctional, 0,
                config.getHsmFunctionalityMinutes(), TimeUnit.MINUTES);
        } else {
            log.info("HSM check not configured, skipping...");
        }

        // RSU Validator
        // Since we're validating Active Tims from both environments in the same task,
        // we only want this running in 1 environment, or else we'll receive duplicate
        // emails
        if (config.getRunRsuValidation()) {
            log.info("Scheduling RSU Validator...");
            scheduledExecutorService.scheduleAtFixedRate(rsuValidator, 20,
                config.getRsuValidationPeriodMinutes(), TimeUnit.MINUTES);
        } else {
            log.info("RSU Validation not configured, skipping...");
        }

        // TMDD Validator
        // Since dev has many Active TIMs that aren't present on the TMDD,
        // we should only be running the TMDD validation in prod.
        if (config.getRunTmddValidation()) {
            log.info("Scheduling TMDD Validator...");
            scheduledExecutorService.scheduleAtFixedRate(tmddValidator, 25,
                config.getTmddValidationPeriodMinutes(), TimeUnit.MINUTES);
        } else {
            log.info("TMDD Validation not configured, skipping...");
        }

        // Retention Policy Enforcement
        log.info("Scheduling Retention Policy Enforcement...");
        scheduledExecutorService.scheduleAtFixedRate(retentionEnforcement, 30,
            config.getRetentionEnforcementPeriodMinutes(), TimeUnit.MINUTES);

        // Cleanup Stale Active Tim Holding Records
        log.info("Scheduling Cleanup Stale Active Tim Holding Records...");
        scheduledExecutorService.scheduleAtFixedRate(cleanupStaleActiveTimHoldingRecords, 0, 1, // TODO: make period configurable
            TimeUnit.HOURS);
    }
}