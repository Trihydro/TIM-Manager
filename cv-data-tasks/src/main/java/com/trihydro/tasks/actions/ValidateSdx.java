package com.trihydro.tasks.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.TimGenerationHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.AdvisorySituationDataDeposit;
import com.trihydro.library.model.ResubmitTimException;
import com.trihydro.library.model.SemiDialogID;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.SdwService;
import com.trihydro.tasks.config.DataTasksConfiguration;
import com.trihydro.tasks.helpers.EmailFormatter;
import com.trihydro.tasks.models.CActiveTim;
import com.trihydro.tasks.models.CAdvisorySituationDataDeposit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidateSdx implements Runnable {
    private DataTasksConfiguration config;
    private SdwService sdwService;
    private ActiveTimService activeTimService;
    private EmailHelper mailHelper;
    private EmailFormatter emailFormatter;
    private Utility utility;
    private TimGenerationHelper timGenerationHelper;

    @Autowired
    public void InjectDependencies(DataTasksConfiguration _config, SdwService _sdwService,
            ActiveTimService _activeTimService, EmailFormatter _emailFormatter, EmailHelper _mailHelper,
            Utility _utility, TimGenerationHelper _timGenerationHelper) {
        config = _config;
        sdwService = _sdwService;
        activeTimService = _activeTimService;
        emailFormatter = _emailFormatter;
        mailHelper = _mailHelper;
        utility = _utility;
        timGenerationHelper = _timGenerationHelper;
    }

    public void run() {
        System.out.println("Running...");

        try {
            validateSdx();
        } catch (Exception ex) {
            System.out.println("Error while validating SDX:");
            ex.printStackTrace();
            // don't rethrow error, or the task won't be reran until the service is
            // restarted.
        }
    }

    private void validateSdx() {
        List<CActiveTim> dbRecords = new ArrayList<>();
        List<CAdvisorySituationDataDeposit> sdxRecords = new ArrayList<>();

        // Fetch records from the Database
        for (ActiveTim activeTim : activeTimService.getActiveTimsForSDX()) {
            dbRecords.add(new CActiveTim(activeTim));
        }

        // Fetch records from SDX
        for (AdvisorySituationDataDeposit asdd : sdwService.getMsgsForOdeUser(SemiDialogID.AdvSitDataDep)) {
            List<Integer> itisCodes = sdwService.getItisCodesFromAdvisoryMessage(asdd.getAdvisoryMessage());
            CAdvisorySituationDataDeposit record = new CAdvisorySituationDataDeposit(asdd, itisCodes);

            sdxRecords.add(record);
        }

        Collections.sort(dbRecords);
        Collections.sort(sdxRecords);

        // Actions to perform
        List<CActiveTim> toResend = new ArrayList<CActiveTim>();
        List<CAdvisorySituationDataDeposit> deleteFromSdx = new ArrayList<CAdvisorySituationDataDeposit>();

        // Metrics to collect
        List<CActiveTim> invDbRecords = new ArrayList<CActiveTim>();
        int numSdxOrphanedRecords = 0;
        int numOutdatedSdxRecords = 0;
        int numRecordsNotOnSdx = 0;

        int i = 0;
        int j = 0;

        while (i < dbRecords.size() || j < sdxRecords.size()) {

            // If either list is at the end, push the remainder of the other list onto their
            // corresponding action
            if (i == dbRecords.size()) {
                // Any remaining sdx records don't have a corresponding database record
                deleteFromSdx.addAll(sdxRecords.subList(j, sdxRecords.size()));
                numSdxOrphanedRecords += sdxRecords.size() - j;
                j = sdxRecords.size();
                continue;
            }
            if (j == sdxRecords.size()) {
                // Any remaining database records don't have a corresponding sdx record
                toResend.addAll(dbRecords.subList(i, dbRecords.size()));
                numRecordsNotOnSdx += dbRecords.size() - i;
                i = dbRecords.size();
                continue;
            }

            CActiveTim dbRecord = dbRecords.get(i);
            CAdvisorySituationDataDeposit sdxRecord = sdxRecords.get(j);
            Integer sdxRecordId = sdxRecord.getRecordId();
            Integer dbRecordId = dbRecord.getRecordId();

            // If the SAT_RECORD_ID string isn't valid hex, the SDX will reject the record.
            // Push onto invDbRecords
            if (dbRecordId == null) {
                invDbRecords.add(dbRecord);
                i++;
                continue;
            }

            if (dbRecordId.equals(sdxRecordId)) {
                // Make sure the numeric ITIS Codes are the same.
                // TODO: we should also check the TIM's ITISText values, if any are present
                if (!sameItisCodes(dbRecord.getItisCodes(), sdxRecord.getItisCodes())) {
                    numOutdatedSdxRecords++;
                    toResend.add(dbRecord);
                }
                i++;
                j++;
            } else if (dbRecordId > sdxRecordId) {
                // The current SDX record doesn't have a corresponding Database record...
                numSdxOrphanedRecords++;
                deleteFromSdx.add(sdxRecord);
                j++;
            } else {
                // The current Database record doesn't have a corresponding SDX record...
                numRecordsNotOnSdx++;
                toResend.add(dbRecord);
                i++;
            }
        }

        if (toResend.size() > 0 || deleteFromSdx.size() > 0 || invDbRecords.size() > 0) {
            // For now, we'll just report on the invDbRecords
            String exceptionText = cleanupData(toResend, deleteFromSdx);
            String email = emailFormatter.generateSdxSummaryEmail(numSdxOrphanedRecords, numOutdatedSdxRecords,
                    numRecordsNotOnSdx, toResend, deleteFromSdx, invDbRecords, exceptionText);

            try {
                mailHelper.SendEmail(config.getAlertAddresses(), "SDX Validation Results", email);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Call out to appropriate endpoints to remove, or refresh data as appropriate
     * 
     * @param toResend      List of records to resubmit to the ODE for processing
     * @param deleteFromSdx List of records to delete from the SDX
     * @return A String representing exceptions to include in the summary email
     */
    private String cleanupData(List<CActiveTim> toResend, List<CAdvisorySituationDataDeposit> deleteFromSdx) {
        String deleteError = "";
        List<ResubmitTimException> resendExceptions = new ArrayList<>();
        if (toResend.size() > 0) {
            resendExceptions = resendTims(toResend);
        }
        // delete from SDX the given records
        if (deleteFromSdx.size() > 0) {
            deleteError = removeFromSdx(deleteFromSdx);
        }

        String exceptionText = "";
        if (StringUtils.isNotBlank(deleteError) || resendExceptions.size() > 0) {
            if (StringUtils.isNotBlank(deleteError)) {
                exceptionText += "The following recordIds failed to delete from the SDX: " + deleteError;
                exceptionText += "<br>";
            }

            if (resendExceptions.size() > 0) {
                Gson gson = new Gson();
                exceptionText += "The following exceptions were found while attempting to resubmit TIMs: ";
                exceptionText += "<br/>";
                for (ResubmitTimException rte : resendExceptions) {
                    exceptionText += gson.toJson(rte);
                    exceptionText += "<br/>";
                }
            }
        }
        return exceptionText;
    }

    private List<ResubmitTimException> resendTims(List<CActiveTim> toResend) {
        var activeTimIds = toResend.stream().map(x -> x.getActiveTim().getActiveTimId()).collect(Collectors.toList());
        return timGenerationHelper.resubmitToOde(activeTimIds);
    }

    private String removeFromSdx(List<CAdvisorySituationDataDeposit> deleteFromSdx) {
        // Issue one delete call to the REST service, encompassing all sat_record_ids
        var satRecordIds = deleteFromSdx.stream().map(x -> x.getAsdd().getRecordId()).collect(Collectors.toList());
        HashMap<Integer, Boolean> sdxDelResults = sdwService.deleteSdxDataByRecordIdIntegers(satRecordIds);
        String failedResultsText = "";

        // Determine if anything failed
        Boolean errorsOccurred = sdxDelResults.entrySet().stream()
                .anyMatch(x -> x.getValue() != null && x.getValue() == false);
        if (errorsOccurred) {
            failedResultsText = sdxDelResults.entrySet().stream().filter(x -> x.getValue() == false)
                    .map(x -> x.getKey().toString()).collect(Collectors.joining(","));
        }
        return failedResultsText;
    }

    private boolean sameItisCodes(List<Integer> o1, List<Integer> o2) {
        boolean result = true;

        if (o1 == null || o2 == null || o1.size() != o2.size()) {
            result = false;
        } else {
            for (int i = 0; i < o1.size(); i++) {
                boolean inBoth = false;

                for (int j = 0; j < o2.size(); j++) {
                    if (o1.get(i) != null && o1.get(i).equals(o2.get(j))) {
                        inBoth = true;
                        break;
                    }
                }

                if (!inBoth) {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }
}