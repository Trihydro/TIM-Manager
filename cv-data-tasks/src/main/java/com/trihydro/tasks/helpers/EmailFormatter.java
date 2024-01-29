package com.trihydro.tasks.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ActiveTimError;
import com.trihydro.library.model.ActiveTimValidationResult;
import com.trihydro.library.model.RsuIndexInfo;
import com.trihydro.tasks.models.ActiveTimMapping;
import com.trihydro.tasks.models.CActiveTim;
import com.trihydro.tasks.models.CAdvisorySituationDataDeposit;
import com.trihydro.tasks.models.Collision;
import com.trihydro.tasks.models.RsuValidationRecord;
import com.trihydro.tasks.models.RsuValidationResult;

import org.springframework.stereotype.Component;

@Component
public class EmailFormatter {
    private String formatSdxMain;
    private String formatSection;
    private String formatRsuMain;
    private String formatRsuResults;
    private String formatRsuValSummary;
    private String formatTmddMain;
    private String formatTmddResults;

    public EmailFormatter() throws IOException {
        formatSdxMain = readFile("/email-templates/sdx-main.html");
        formatSection = readFile("/email-templates/section.html");
        formatRsuMain = readFile("/email-templates/rsu-main.html");
        formatRsuResults = readFile("/email-templates/rsu-results.html");
        formatRsuValSummary = readFile("/email-templates/rsu-val-summary.html");
        formatTmddMain = readFile("/email-templates/tmdd-main.html");
        formatTmddResults = readFile("/email-templates/tmdd-results.html");
    }

    private String readFile(String path) throws IOException {
        String file = null;
        try (InputStream inputStream = getClass().getResourceAsStream(path);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            file = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        return file;
    }

    public String generateSdxSummaryEmail(int numSdxOrphaned, int numOutdatedSdx, int numNotOnSdx,
            List<CActiveTim> toResend, List<CAdvisorySituationDataDeposit> deleteFromSdx,
            List<CActiveTim> invDbRecords, String exceptions) {
        // Create summary
        String body = formatSdxMain.replaceAll("\\{num-stale\\}", Integer.toString(numOutdatedSdx));
        body = body.replaceAll("\\{num-sdx-orphaned\\}", Integer.toString(numSdxOrphaned));
        body = body.replaceAll("\\{num-not-present-sdx\\}", Integer.toString(numNotOnSdx));
        body = body.replaceAll("\\{num-inv-database\\}", Integer.toString(invDbRecords.size()));

        // Add tables w/ detailed info, if available
        String content = "";

        // Invalid Database records
        if (invDbRecords.size() > 0) {
            String section = formatSection.replaceAll("\\{title\\}", "Invalid Database records");
            section = section.replaceAll("\\{headers\\}", getHeader("ACTIVE_TIM_ID", "SAT_RECORD_ID"));

            String rows = "";
            for (CActiveTim record : invDbRecords) {
                rows += getRow(Long.toString(record.getActiveTim().getActiveTimId()),
                        record.getActiveTim().getSatRecordId());
            }
            section = section.replaceAll("\\{rows\\}", rows);

            content += section;
        }

        // ActiveTims to resend to SDX
        if (toResend.size() > 0) {
            String section = formatSection.replaceAll("\\{title\\}", "ActiveTims to resend to SDX");
            section = section.replaceAll("\\{headers\\}", getHeader("ACTIVE_TIM_ID", "SAT_RECORD_ID"));

            String rows = "";
            for (CActiveTim record : toResend) {
                rows += getRow(Long.toString(record.getActiveTim().getActiveTimId()),
                        record.getActiveTim().getSatRecordId());
            }
            section = section.replaceAll("\\{rows\\}", rows);

            content += section;
        }

        // Orphaned records to delete from SDX
        if (deleteFromSdx.size() > 0) {
            String section = formatSection.replaceAll("\\{title\\}", "Orphaned records to delete from SDX");
            section = section.replaceAll("\\{headers\\}", getHeader("recordId"));

            String rows = "";
            for (CAdvisorySituationDataDeposit record : deleteFromSdx) {
                rows += getRow(Integer.toString(record.getRecordId()));
            }
            section = section.replaceAll("\\{rows\\}", rows);

            content += section;
        }

        body = body.replaceAll("\\{summary-tables\\}", content);

        body = body.replaceAll("\\{exception-summary\\}", exceptions);

        // Remove unnecessary whitespace
        body = body.replaceAll("\\s*\n\\s*", "");

        return body;
    }

    public String generateRsuSummaryEmail(List<RsuValidationRecord> rsuValidationRecords,
            List<String> unexpectedErrors) {
        String body = formatRsuMain;

        // List RSUs that couldn't be verified
        var unresponsiveRsus = rsuValidationRecords.stream().filter(
                rsu -> rsu.getValidationResults().size() > 0 && rsu.getValidationResults().get(0).getRsuUnresponsive())
                .map(rsu -> rsu.getRsuInformation().getIpv4Address()).collect(Collectors.toList());

        String failedIpAddresses = String.join(", ", unresponsiveRsus);
        body = body.replaceAll("\\{failedIpAddresses\\}", failedIpAddresses);

        // Populate RSUs With Errors section (if applicable)
        String rsuResults = "";
        for (var rsuResult : rsuValidationRecords) {
            rsuResults += getRsuResult(rsuResult);
        }
        body = body.replaceAll("\\{rsusWithErrors\\}", rsuResults);

        // List unexpected errors
        String errorList = "";
        for (String error : unexpectedErrors) {
            errorList += "<li>" + error + "</li>";
        }
        body = body.replaceAll("\\{errorsList\\}", errorList);

        // Remove unnecessary whitespace
        body = body.replaceAll("\\s*\n\\s*", "");

        return body;
    }

    public String generateTmddSummaryEmail(List<ActiveTim> unableToVerify,
            List<ActiveTimValidationResult> validationResults, String exceptions) {
        String body = formatTmddMain;

        // List Active TIMs that couldn't be verified
        String notVerified = "";
        for (ActiveTim tim : unableToVerify) {
            notVerified += tim.getActiveTimId() + " (" + tim.getClientId() + "), ";
        }
        notVerified = notVerified.replaceAll(", $", "");
        body = body.replaceAll("\\{notVerified\\}", notVerified);

        // Populate Inconsistencies section
        String inconsistencies = "";
        for (ActiveTimValidationResult result : validationResults) {
            inconsistencies += getTmddResult(result);
        }
        body = body.replaceAll("\\{content\\}", inconsistencies);

        // Add in any exceptions
        body = body.replaceAll("\\{exception-summary\\}", exceptions);

        // Remove unnecessary whitespace
        body = body.replaceAll("\\s*\n\\s*", "");

        return body;
    }

    private String getRsuResult(RsuValidationRecord rsuValRecord) {
        // This RSU has reportable errors if there are 2 validation results (we found
        // inconsistencies
        // then attempted to correct them), there are index collisions, or there's an
        // error
        if (!(rsuValRecord.getValidationResults().size() == 2
                || (rsuValRecord.getValidationResults().size() > 0
                        && rsuValRecord.getValidationResults().get(0).getCollisions().size() > 0)
                || rsuValRecord.getError() != null)) {
            return "";
        }

        String section = formatRsuResults.replaceAll("\\{ipv4Address\\}",
                rsuValRecord.getRsuInformation().getIpv4Address());

        // If we encountered an error before being able to perform the initial
        // validation, note it.
        if (rsuValRecord.getValidationResults().size() == 0) {
            if (rsuValRecord.getError() != null) {
                section = section.replaceAll("\\{beforeCorrections\\}", rsuValRecord.getError());
            } else {
                section = section.replaceAll("\\{beforeCorrections\\}",
                        "Unknown error occurred during initial validation attempt.");
            }

            section = section.replaceAll("\\{afterCorrections\\}",
                    "Second validation attempt wasn't completed due to error in first attempt.");

            return section;
        }

        section = section.replaceAll("\\{beforeCorrections\\}",
                getRsuValSummary(rsuValRecord.getValidationResults().get(0)));

        if(rsuValRecord.getValidationResults().size() == 1) {
            if (rsuValRecord.getError() != null) {
                section = section.replaceAll("\\{afterCorrections\\}", rsuValRecord.getError());
            } else {
                section = section.replaceAll("\\{afterCorrections\\}",
                        "Second validation attempt wasn't performed." + 
                        " Errors found may not be automatically correctable (e.g. index collisions)");
            }

            return section;
        }
        
        section = section.replaceAll("\\{afterCorrections\\}",
                getRsuValSummary(rsuValRecord.getValidationResults().get(1)));

        return section;
    }

    private String getRsuValSummary(RsuValidationResult valResult) {
        if(valResult.getRsuUnresponsive()) {
            return "RSU was unresponsive.";
        }
        
        String section = formatRsuValSummary;

        // List unaccounted for indexes
        String subSection = "";
        for (Integer index : valResult.getUnaccountedForIndices()) {
            if (!subSection.equals("")) {
                subSection += ", ";
            }
            subSection += index.toString();
        }
        section = section.replaceAll("\\{unaccountedIndexes\\}", subSection);

        // List Active TIMs missing from RSU
        subSection = "";
        for (ActiveTim tim : valResult.getMissingFromRsu()) {
            subSection += getRow(tim.getActiveTimId().toString(),
                    tim.getRsuIndex().toString());
        }
        section = section.replaceAll("\\{rowsMissingTims\\}", subSection);

        // List Stale TIMs on RSU
        subSection = "";
        for (ActiveTimMapping staleTim : valResult.getStaleIndexes()) {
            ActiveTim tim = staleTim.getActiveTim();
            RsuIndexInfo rsuIndex = staleTim.getRsuIndexInfo();
            subSection += getRow(tim.getActiveTimId().toString(), rsuIndex.getIndex().toString(),
                    tim.getStartDateTime(), rsuIndex.getDeliveryStartTime());
        }
        section = section.replaceAll("\\{rowsStaleTims\\}", subSection);

        // List Active TIMs claiming same index
        subSection = "";
        for (Collision collision : valResult.getCollisions()) {
            String activeTimIds = "";
            for (ActiveTim tim : collision.getTims()) {
                if (!activeTimIds.equals("")) {
                    activeTimIds += ", ";
                }
                activeTimIds += tim.getActiveTimId().toString();
            }

            subSection += getRow(collision.getIndex().toString(), activeTimIds);
        }
        section = section.replaceAll("\\{rowsCollisions\\}", subSection);

        return section;
    }

    private String getTmddResult(ActiveTimValidationResult result) {
        String header = result.getActiveTim().getActiveTimId() + " (" + result.getActiveTim().getClientId() + ")";
        String section = formatTmddResults.replaceAll("\\{header\\}", header);

        String rows = "";
        for (ActiveTimError error : result.getErrors()) {
            rows += getRow(error.getName().getStringValue(), error.getTimValue(), error.getTmddValue());
        }
        section = section.replaceAll("\\{rows\\}", rows);

        return section;
    }

    private String getHeader(String... values) {
        String headers = "";
        for (String value : values) {
            headers += "<th>" + value + "</th>\n";
        }

        return headers;
    }

    private String getRow(String... values) {
        String row = "<tr>";
        for (String value : values) {
            row += "<td>" + value + "</td>";
        }

        row += "</tr>\n";
        return row;
    }
}