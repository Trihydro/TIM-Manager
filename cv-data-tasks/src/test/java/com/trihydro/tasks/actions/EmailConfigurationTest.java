package com.trihydro.tasks.actions;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.AdvisorySituationDataDeposit;
import com.trihydro.tasks.config.EmailConfiguration;
import com.trihydro.tasks.models.CActiveTim;
import com.trihydro.tasks.models.CAdvisorySituationDataDeposit;

import org.junit.Test;

public class EmailConfigurationTest {
    @Test
    public void generateSdxSummaryEmail_success() throws IOException {
        // Arrange
        EmailConfiguration uut = new EmailConfiguration();

        List<CActiveTim> toResend = new ArrayList<>();
        List<CAdvisorySituationDataDeposit> deleteFromSdx = new ArrayList<>();
        List<CActiveTim> invOracleRecords = new ArrayList<>();

        // Act
        String emailBody = uut.generateSdxSummaryEmail(1, 2, 3, toResend, deleteFromSdx, invOracleRecords);

        // Assert
        assertTrue("Number of stale records on SDX (different ITIS codes than ActiveTim): 2",
                emailBody.matches("[\\s\\S]*Number of stale records on SDX \\(different ITIS codes than ActiveTim\\):</td>\\s*<td>2[\\s\\S]*"));
        assertTrue("Number of messages on SDX without corresponding Oracle record: 1", emailBody.matches(
                "[\\s\\S]*Number of messages on SDX without corresponding Oracle record:</td>\\s*<td>1[\\s\\S]*"));
        assertTrue("Number of Oracle records without corresponding message in SDX: 3", emailBody.matches(
                "[\\s\\S]*Number of Oracle records without corresponding message in SDX:</td>\\s*<td>3[\\s\\S]*"));
        assertTrue("Number of invalid records in Oracle: 0",
                emailBody.matches("[\\s\\S]*Number of invalid records in Oracle:</td>\\s*<td>0[\\s\\S]*"));
    }

    @Test
    public void generateSdxSummaryEmail_withInvOracleRecordsSection() throws IOException {
        // Arrange
        EmailConfiguration uut = new EmailConfiguration();

        List<CActiveTim> toResend = new ArrayList<>();
        List<CAdvisorySituationDataDeposit> deleteFromSdx = new ArrayList<>();
        List<CActiveTim> invOracleRecords = new ArrayList<>();

        ActiveTim invRecord = new ActiveTim();
        invRecord.setActiveTimId(100l);
        invRecord.setSatRecordId("invHex");

        invOracleRecords.add(new CActiveTim(invRecord));

        // Act
        String emailBody = uut.generateSdxSummaryEmail(0, 0, 0, toResend, deleteFromSdx, invOracleRecords);

        // Assert
        assertTrue("<h3>Invalid Oracle records</h3>",
                emailBody.matches("[\\s\\S]*<h3>Invalid Oracle records</h3>[\\s\\S]*"));
        assertTrue("<tr><td>100</td><td>invHex</td></tr>",
                emailBody.matches("[\\s\\S]*<tr><td>100</td><td>invHex</td></tr>[\\s\\S]*"));
    }

    @Test
    public void generateSdxSummaryEmail_withToResendSection() throws IOException {
        // Arrange
        EmailConfiguration uut = new EmailConfiguration();

        List<CActiveTim> toResend = new ArrayList<>();
        List<CAdvisorySituationDataDeposit> deleteFromSdx = new ArrayList<>();
        List<CActiveTim> invOracleRecords = new ArrayList<>();

        ActiveTim resend = new ActiveTim();
        resend.setActiveTimId(200l);
        resend.setSatRecordId("AA1234");

        toResend.add(new CActiveTim(resend));

        // Act
        String emailBody = uut.generateSdxSummaryEmail(0, 1, 0, toResend, deleteFromSdx, invOracleRecords);

        // Assert
        assertTrue("<h3>ActiveTims to resend to SDX/h3>",
                emailBody.matches("[\\s\\S]*<h3>ActiveTims to resend to SDX</h3>[\\s\\S]*"));
        assertTrue("<tr><td>200</td><td>AA1234</td></tr>",
                emailBody.matches("[\\s\\S]*<tr><td>200</td><td>AA1234</td></tr>[\\s\\S]*"));
    }

    @Test
    public void generateSdxSummaryEmail_withStaleSdxSection() throws IOException {
        // Arrange
        EmailConfiguration uut = new EmailConfiguration();

        List<CActiveTim> toResend = new ArrayList<>();
        List<CAdvisorySituationDataDeposit> deleteFromSdx = new ArrayList<>();
        List<CActiveTim> invOracleRecords = new ArrayList<>();

        AdvisorySituationDataDeposit invAsdd = new AdvisorySituationDataDeposit();
        invAsdd.setRecordId(-200);
        deleteFromSdx.add(new CAdvisorySituationDataDeposit(invAsdd));

        // Act
        String emailBody = uut.generateSdxSummaryEmail(1, 0, 0, toResend, deleteFromSdx, invOracleRecords);

        // Assert
        assertTrue("<h3>Orphaned records to delete from SDX</h3>",
                emailBody.matches("[\\s\\S]*<h3>Orphaned records to delete from SDX</h3>[\\s\\S]*"));
        assertTrue("<tr><td>-200</td></tr>",
                emailBody.matches("[\\s\\S]*<tr><td>-200</td></tr>[\\s\\S]*"));
    }
}