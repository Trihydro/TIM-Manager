package com.trihydro.tasks.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.AdvisorySituationDataDeposit;
import com.trihydro.library.model.SemiDialogID;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.SdwService;
import com.trihydro.tasks.config.DataTasksConfiguration;
import com.trihydro.tasks.helpers.EmailFormatter;
import com.trihydro.tasks.models.CActiveTim;
import com.trihydro.tasks.models.CAdvisorySituationDataDeposit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidateSDX implements Runnable {
    private DataTasksConfiguration config;
    private SdwService sdwService;
    private ActiveTimService activeTimService;
    private EmailHelper mailHelper;
    private EmailFormatter emailFormatter;
    private Utility utility;

    @Autowired
    public void InjectDependencies(DataTasksConfiguration _config, SdwService _sdwService,
            ActiveTimService _activeTimService, EmailFormatter _emailFormatter, EmailHelper _mailHelper,
            Utility _utility) {
        config = _config;
        sdwService = _sdwService;
        activeTimService = _activeTimService;
        emailFormatter = _emailFormatter;
        mailHelper = _mailHelper;
        utility = _utility;
    }

    public void run() {
        utility.logWithDate("Running...", this.getClass());

        try {
            validateSdx();
        } catch (Exception ex) {
            utility.logWithDate("Error while validating SDX:", this.getClass());
            ex.printStackTrace();
            // don't rethrow error, or the task won't be reran until the service is
            // restarted.
        }
    }

    private void validateSdx() {
        List<CActiveTim> oracleRecords = new ArrayList<>();
        List<CAdvisorySituationDataDeposit> sdxRecords = new ArrayList<>();

        // Fetch records from Oracle
        for (ActiveTim activeTim : activeTimService.getActiveTimsForSDX()) {
            oracleRecords.add(new CActiveTim(activeTim));
        }

        // Fetch records from SDX
        for (AdvisorySituationDataDeposit asdd : sdwService.getMsgsForOdeUser(SemiDialogID.AdvSitDataDep)) {
            List<Integer> itisCodes = sdwService.getItisCodesFromAdvisoryMessage(asdd.getAdvisoryMessage());
            CAdvisorySituationDataDeposit record = new CAdvisorySituationDataDeposit(asdd, itisCodes);

            sdxRecords.add(record);
        }

        Collections.sort(oracleRecords);
        Collections.sort(sdxRecords);

        // Actions to perform
        List<CActiveTim> toResend = new ArrayList<CActiveTim>();
        List<CAdvisorySituationDataDeposit> deleteFromSdx = new ArrayList<CAdvisorySituationDataDeposit>();

        // Metrics to collect
        List<CActiveTim> invOracleRecords = new ArrayList<CActiveTim>();
        int numSdxOrphanedRecords = 0;
        int numOutdatedSdxRecords = 0;
        int numRecordsNotOnSdx = 0;

        int i = 0;
        int j = 0;

        while (i < oracleRecords.size() || j < sdxRecords.size()) {

            // If either list is at the end, push the remainder of the other list onto their
            // corresponding action
            if (i == oracleRecords.size()) {
                // Any remaining sdx records don't have a corresponding oracle record
                deleteFromSdx.addAll(sdxRecords.subList(j, sdxRecords.size()));
                numSdxOrphanedRecords += sdxRecords.size() - j;
                j = sdxRecords.size();
                continue;
            }
            if (j == sdxRecords.size()) {
                // Any remaining oracle records don't have a corresponding sdx record
                toResend.addAll(oracleRecords.subList(i, oracleRecords.size()));
                numRecordsNotOnSdx += oracleRecords.size() - i;
                i = oracleRecords.size();
                continue;
            }

            CActiveTim oracleRecord = oracleRecords.get(i);
            CAdvisorySituationDataDeposit sdxRecord = sdxRecords.get(j);
            Integer sdxRecordId = sdxRecord.getRecordId();
            Integer oracleRecordId = oracleRecord.getRecordId();

            // If the SAT_RECORD_ID string isn't valid hex, the SDX will reject the record.
            // Push onto invOracleRecords
            if (oracleRecordId == null) {
                invOracleRecords.add(oracleRecord);
                i++;
                continue;
            }

            if (oracleRecordId.equals(sdxRecordId)) {
                // make sure the messages are the same
                if (!sameItisCodes(oracleRecord.getItisCodes(), sdxRecord.getItisCodes())) {
                    numOutdatedSdxRecords++;
                    toResend.add(oracleRecord);
                }
                i++;
                j++;
            } else if (oracleRecordId > sdxRecordId) {
                // The current SDX record doesn't have a corresponding Oracle record...
                numSdxOrphanedRecords++;
                deleteFromSdx.add(sdxRecord);
                j++;
            } else {
                // The current Oracle record doesn't have a corresponding SDX record...
                numRecordsNotOnSdx++;
                toResend.add(oracleRecord);
                i++;
            }
        }

        if (toResend.size() > 0 || deleteFromSdx.size() > 0 || invOracleRecords.size() > 0) {
            String email = emailFormatter.generateSdxSummaryEmail(numSdxOrphanedRecords, numOutdatedSdxRecords,
                    numRecordsNotOnSdx, toResend, deleteFromSdx, invOracleRecords);

            try {
                mailHelper.SendEmail(config.getAlertAddresses(), null, "SDX Validation Results", email,
                        config.getMailPort(), config.getMailHost(), config.getFromEmail());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
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