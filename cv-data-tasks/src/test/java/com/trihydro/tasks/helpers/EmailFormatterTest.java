package com.trihydro.tasks.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.AdvisorySituationDataDeposit;
import com.trihydro.library.model.RsuIndexInfo;
import com.trihydro.tasks.models.ActiveTimMapping;
import com.trihydro.tasks.models.CActiveTim;
import com.trihydro.tasks.models.CAdvisorySituationDataDeposit;
import com.trihydro.tasks.models.Collision;
import com.trihydro.tasks.models.EnvActiveTim;
import com.trihydro.tasks.models.Environment;
import com.trihydro.tasks.models.RsuValidationResult;

import org.junit.Test;

public class EmailFormatterTest {
    @Test
    public void generateSdxSummaryEmail_success() throws IOException {
        // Arrange
        EmailFormatter uut = new EmailFormatter();

        List<CActiveTim> toResend = new ArrayList<>();
        List<CAdvisorySituationDataDeposit> deleteFromSdx = new ArrayList<>();
        List<CActiveTim> invOracleRecords = new ArrayList<>();

        // Act
        String emailBody = uut.generateSdxSummaryEmail(1, 2, 3, toResend, deleteFromSdx, invOracleRecords);

        // Assert
        assertTrue("Number of stale records on SDX (different ITIS codes than ActiveTim): 2", emailBody.matches(
                ".*Number of stale records on SDX \\(different ITIS codes than ActiveTim\\):</td>\\s*<td>2.*"));
        assertTrue("Number of messages on SDX without corresponding Oracle record: 1",
                emailBody.matches(".*Number of messages on SDX without corresponding Oracle record:</td>\\s*<td>1.*"));
        assertTrue("Number of Oracle records without corresponding message in SDX: 3",
                emailBody.matches(".*Number of Oracle records without corresponding message in SDX:</td>\\s*<td>3.*"));
        assertTrue("Number of invalid records in Oracle: 0",
                emailBody.matches(".*Number of invalid records in Oracle:</td>\\s*<td>0.*"));
    }

    @Test
    public void generateSdxSummaryEmail_withInvOracleRecordsSection() throws IOException {
        // Arrange
        EmailFormatter uut = new EmailFormatter();

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
        assertTrue("Contains section: Invalid Oracle records",
                emailBody.matches(".*<h3>Invalid Oracle records</h3>.*"));
        assertTrue("<tr><td>100</td><td>invHex</td></tr>",
                emailBody.matches(".*<tr><td>100</td><td>invHex</td></tr>.*"));
    }

    @Test
    public void generateSdxSummaryEmail_withToResendSection() throws IOException {
        // Arrange
        EmailFormatter uut = new EmailFormatter();

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
        assertTrue("Contains section: ActiveTims to resend to SDX",
                emailBody.matches(".*<h3>ActiveTims to resend to SDX</h3>.*"));
        assertTrue("<tr><td>200</td><td>AA1234</td></tr>",
                emailBody.matches(".*<tr><td>200</td><td>AA1234</td></tr>.*"));
    }

    @Test
    public void generateSdxSummaryEmail_withStaleSdxSection() throws IOException {
        // Arrange
        EmailFormatter uut = new EmailFormatter();

        List<CActiveTim> toResend = new ArrayList<>();
        List<CAdvisorySituationDataDeposit> deleteFromSdx = new ArrayList<>();
        List<CActiveTim> invOracleRecords = new ArrayList<>();

        AdvisorySituationDataDeposit invAsdd = new AdvisorySituationDataDeposit();
        invAsdd.setRecordId(-200);
        deleteFromSdx.add(new CAdvisorySituationDataDeposit(invAsdd, null));

        // Act
        String emailBody = uut.generateSdxSummaryEmail(1, 0, 0, toResend, deleteFromSdx, invOracleRecords);

        // Assert
        assertTrue("Contains section: Orphaned records to delete from SDX",
                emailBody.matches(".*<h3>Orphaned records to delete from SDX</h3>.*"));
        assertTrue("<tr><td>-200</td></tr>", emailBody.matches(".*<tr><td>-200</td></tr>.*"));
    }

    @Test
    public void generateRsuSummaryEmail_success() throws IOException {
        // Arrange
        EmailFormatter uut = new EmailFormatter();

        List<String> unresponsiveRsus = new ArrayList<>();
        List<String> unexpectedErrors = new ArrayList<>();
        List<RsuValidationResult> rsusWithErrors = new ArrayList<>();

        unresponsiveRsus.add("10.145.0.0");

        // Act
        String emailBody = uut.generateRsuSummaryEmail(unresponsiveRsus, rsusWithErrors, unexpectedErrors);

        // Assert
        assertTrue("Unable to verify the following RSUs 10.145.0.0",
                emailBody.matches(".*<div class=\"indent\"><p>10.145.0.0</p></div>.*"));
    }

    @Test
    public void generateRsuSummaryEmail_invalidRsu() throws IOException {
        // Arrange
        EmailFormatter uut = new EmailFormatter();

        List<String> unresponsiveRsus = new ArrayList<>();
        List<String> unexpectedErrors = new ArrayList<>();
        List<RsuValidationResult> rsusWithErrors = new ArrayList<>();

        RsuValidationResult invalidRsu = new RsuValidationResult("10.145.0.0");

        // ActiveTim missing from RSU
        EnvActiveTim missing = new EnvActiveTim(new ActiveTim() {
            {
                setActiveTimId(1l);
                setRsuIndex(1);
            }
        }, Environment.DEV);
        invalidRsu.setMissingFromRsu(Arrays.asList(missing));

        // 2 ActiveTims, collided at index 2 on RSU
        EnvActiveTim coll1 = new EnvActiveTim(new ActiveTim() {
            {
                setActiveTimId(2l);
            }
        }, Environment.DEV);

        EnvActiveTim coll2 = new EnvActiveTim(new ActiveTim() {
            {
                setActiveTimId(3l);
            }
        }, Environment.PROD);
        Collision c = new Collision(2, Arrays.asList(coll1, coll2));
        invalidRsu.setCollisions(Arrays.asList(c));

        // ActiveTim stale on index 3
        EnvActiveTim staleTim = new EnvActiveTim(new ActiveTim() {
            {
                setActiveTimId(4l);
                setStartDateTime("2020-01-01");
                setRsuIndex(3);
            }
        }, Environment.DEV);
        RsuIndexInfo indexInfo = new RsuIndexInfo(3, "2020-02-02");

        ActiveTimMapping staleMapping = new ActiveTimMapping(staleTim, indexInfo);
        invalidRsu.getStaleIndexes().add(staleMapping);

        // Unaccounted for RSU index
        invalidRsu.setUnaccountedForIndices(Arrays.asList(3));

        rsusWithErrors.add(invalidRsu);

        // Act
        String emailBody = uut.generateRsuSummaryEmail(unresponsiveRsus, rsusWithErrors, unexpectedErrors);

        // Assert
        assertTrue("<h3>RSUs with Errors</h3><h4>10.145.0.0</h4>",
                emailBody.matches(".*<h3>RSUs with Errors</h3><h4>10.145.0.0</h4>.*"));
        assertTrue("... Populated indexes without record ... 3 ...",
                emailBody.matches(".*Populated indexes without record.*3.*"));

        String tbody = getRowsForListItem("Active TIMs missing from RSU", emailBody);
        assertEquals("<tr><td>DEV</td><td>1</td><td>1</td></tr>", tbody);

        tbody = getRowsForListItem("Stale TIMs on RSU", emailBody);
        assertEquals("<tr><td>DEV</td><td>4</td><td>3</td><td>2020-01-01</td><td>2020-02-02</td></tr>", tbody);

        tbody = getRowsForListItem("Active TIM index collisions", emailBody);
        assertEquals("<tr><td>2</td><td>2 (DEV), 3 (PROD)</td></tr>", tbody);
    }

    @Test
    public void generateRsuSummaryEmail_unexpectedError() throws IOException {
        // Arrange
        EmailFormatter uut = new EmailFormatter();

        List<String> unresponsiveRsus = new ArrayList<>();
        List<String> unexpectedErrors = new ArrayList<>();
        List<RsuValidationResult> rsusWithErrors = new ArrayList<>();

        unexpectedErrors.add("10.145.0.0: InterruptedException");

        // Act
        String emailBody = uut.generateRsuSummaryEmail(unresponsiveRsus, rsusWithErrors, unexpectedErrors);

        // Assert
        assertTrue("... <h3>Unexpected Errors Processing RSUs</h3> ... <li>10.145.0.0: InterruptedException</li> ...",
                emailBody.matches(
                        ".*<h3>Unexpected Errors Processing RSUs</h3><ul><li>10.145.0.0: InterruptedException</li></ul>.*"));
    }

    // RSU Validation Email helper method
    public String getRowsForListItem(String listItemHeader, String emailBody) {
        String row = "";

        Pattern p = Pattern.compile("<li>" + listItemHeader + ".*?<tbody>(.*?)<\\/tbody>.*?<\\/li>");
        Matcher m = p.matcher(emailBody);
        if (m.find()) {
            row = m.group(1);
        }

        return row;
    }
}