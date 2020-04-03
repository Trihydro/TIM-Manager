package com.trihydro.tasks.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.RsuIndexInfo;
import com.trihydro.tasks.models.ActiveTimMapping;
import com.trihydro.tasks.models.CActiveTim;
import com.trihydro.tasks.models.CAdvisorySituationDataDeposit;
import com.trihydro.tasks.models.Collision;
import com.trihydro.tasks.models.EnvActiveTim;
import com.trihydro.tasks.models.Environment;
import com.trihydro.tasks.models.RsuValidationResult;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class EmailFormatter {
    private String formatSdxMain;
    private String formatSection;
    private String formatRsuMain;
    private String formatRsuResults;

    public EmailFormatter() throws IOException {
        formatSdxMain = readFile("email-templates/sdx-main.html");
        formatSection = readFile("email-templates/section.html");
        formatRsuMain = readFile("email-templates/rsu-main.html");
        formatRsuResults = readFile("email-templates/rsu-results.html");
    }

    private String readFile(String path) throws IOException {
        File tmp = new ClassPathResource(path).getFile();
        return new String(Files.readAllBytes(tmp.toPath()));
    }

    public String generateSdxSummaryEmail(int numSdxOrphaned, int numOutdatedSdx, int numNotOnSdx,
            List<CActiveTim> toResend, List<CAdvisorySituationDataDeposit> deleteFromSdx,
            List<CActiveTim> invOracleRecords) {
        // Create summary
        String body = formatSdxMain.replaceAll("\\{num-stale\\}", Integer.toString(numOutdatedSdx));
        body = body.replaceAll("\\{num-sdx-orphaned\\}", Integer.toString(numSdxOrphaned));
        body = body.replaceAll("\\{num-not-present-sdx\\}", Integer.toString(numNotOnSdx));
        body = body.replaceAll("\\{num-inv-oracle\\}", Integer.toString(invOracleRecords.size()));

        // Add tables w/ detailed info, if available
        String content = "";

        // Invalid Oracle records
        if (invOracleRecords.size() > 0) {
            String section = formatSection.replaceAll("\\{title\\}", "Invalid Oracle records");
            section = section.replaceAll("\\{headers\\}", getHeader("ACTIVE_TIM_ID", "SAT_RECORD_ID"));

            String rows = "";
            for (CActiveTim record : invOracleRecords) {
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

        // Remove unnecessary whitespace
        body = body.replaceAll("\\s*\n\\s*", "");

        return body;
    }

    public String generateRsuSummaryEmail(List<String> unresponsiveRsus, List<RsuValidationResult> rsusWithErrors,
            List<String> unexpectedErrors) {
        String body = formatRsuMain;

        // List RSUs that couldn't be verified
        String failedIpAddresses = String.join(", ", unresponsiveRsus);
        body = body.replaceAll("\\{failedIpAddresses\\}", failedIpAddresses);

        // Populate RSUs With Errors section (if applicable)
        String rsuResults = "";
        for (RsuValidationResult rsuResult : rsusWithErrors) {
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

    private String getRsuResult(RsuValidationResult result) {
        String section = formatRsuResults.replaceAll("\\{ipv4Address\\}", result.getRsu());
        String subSection = "";

        // List unaccounted for indexes
        subSection = "";
        for (Integer index : result.getUnaccountedForIndices()) {
            if (!subSection.equals("")) {
                subSection += ", ";
            }
            subSection += index.toString();
        }
        section = section.replaceAll("\\{unaccountedIndexes\\}", subSection);

        // List Active TIMs missing from RSU
        subSection = "";
        for (EnvActiveTim record : result.getMissingFromRsu()) {
            ActiveTim tim = record.getActiveTim();
            subSection += getRow(record.getEnvironment().toString(), tim.getActiveTimId().toString(),
                    tim.getRsuIndex().toString());
        }
        section = section.replaceAll("\\{rowsMissingTims\\}", subSection);

        // List Stale TIMs on RSU
        subSection = "";
        for (ActiveTimMapping staleTim : result.getStaleIndexes()) {
            Environment env = staleTim.getEnvTim().getEnvironment();
            ActiveTim tim = staleTim.getEnvTim().getActiveTim();
            RsuIndexInfo rsuIndex = staleTim.getRsuIndexInfo();
            subSection += getRow(env.toString(), tim.getActiveTimId().toString(), rsuIndex.getIndex().toString(),
                    tim.getStartDateTime(), rsuIndex.getDeliveryStartTime());
        }
        section = section.replaceAll("\\{rowsStaleTims\\}", subSection);

        // List Active TIMs claiming same index
        subSection = "";
        for (Collision collision : result.getCollisions()) {
            String activeTimIds = "";
            for (EnvActiveTim record : collision.getTims()) {
                if (!activeTimIds.equals("")) {
                    activeTimIds += ", ";
                }
                activeTimIds += record.getActiveTim().getActiveTimId().toString();
                activeTimIds += " (" + record.getEnvironment().toString() + ")";
            }

            subSection += getRow(collision.getIndex().toString(), activeTimIds);
        }
        section = section.replaceAll("\\{rowsCollisions\\}", subSection);

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