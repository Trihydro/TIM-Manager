package com.trihydro.tasks.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import com.trihydro.tasks.models.CActiveTim;
import com.trihydro.tasks.models.CAdvisorySituationDataDeposit;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class EmailConfiguration {
    private String formatMain;
    private String formatSection;

    public EmailConfiguration() throws IOException {
        formatMain = readFile("email-templates/main.html");
        formatSection = readFile("email-templates/section.html");
    }

    private String readFile(String path) throws IOException {
        File tmp = new ClassPathResource(path).getFile();
        return new String(Files.readAllBytes(tmp.toPath()));
    }

    public String generateSdxSummaryEmail(int numSdxOrphaned, int numOutdatedSdx, int numNotOnSdx,
            List<CActiveTim> toResend, List<CAdvisorySituationDataDeposit> deleteFromSdx,
            List<CActiveTim> invOracleRecords) {
                String body = formatMain.replaceAll("\\{num-stale\\}", Integer.toString(numOutdatedSdx));
                body = body.replaceAll("\\{num-sdx-orphaned\\}", Integer.toString(numSdxOrphaned));
                body = body.replaceAll("\\{num-not-present-sdx\\}", Integer.toString(numNotOnSdx));
                body = body.replaceAll("\\{num-inv-oracle\\}", Integer.toString(invOracleRecords.size()));

                return body;
            }
    }