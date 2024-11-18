package com.trihydro.tasks.helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ActiveTimError;
import com.trihydro.library.model.ActiveTimErrorType;
import com.trihydro.library.model.ActiveTimValidationResult;
import com.trihydro.library.model.AdvisorySituationDataDeposit;
import com.trihydro.library.model.RsuIndexInfo;
import com.trihydro.tasks.TestHelper;
import com.trihydro.tasks.models.ActiveTimMapping;
import com.trihydro.tasks.models.CActiveTim;
import com.trihydro.tasks.models.CAdvisorySituationDataDeposit;
import com.trihydro.tasks.models.Collision;
import com.trihydro.tasks.models.RsuInformation;
import com.trihydro.tasks.models.RsuValidationRecord;
import com.trihydro.tasks.models.RsuValidationResult;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EmailFormatterTest {
    private String exText = "This is a great exception summary";
    private RsuInformation testRsuInfo = new RsuInformation("0.0.0.0");

    @Test
    public void generateSdxSummaryEmail_success() throws IOException {
        // Arrange
        EmailFormatter uut = new EmailFormatter();

        List<CActiveTim> toResend = new ArrayList<>();
        List<CAdvisorySituationDataDeposit> deleteFromSdx = new ArrayList<>();
        List<CActiveTim> invDbRecords = new ArrayList<>();

        // Act
        String emailBody = uut.generateSdxSummaryEmail(1, 2, 3, toResend, deleteFromSdx, invDbRecords, exText);

        // Assert
        Assertions.assertTrue(
                emailBody.matches(
                        ".*Number of stale records on SDX \\(different ITIS codes than ActiveTim\\):</td>\\s*<td>2.*"),
                "Number of stale records on SDX (different ITIS codes than ActiveTim): 2");
        Assertions.assertTrue(
                emailBody.matches(".*Number of messages on SDX without corresponding Database record:</td>\\s*<td>1.*"),
                "Number of messages on SDX without corresponding Database record: 1");
        Assertions.assertTrue(
                emailBody.matches(".*Number of Database records without corresponding message in SDX:</td>\\s*<td>3.*"),
                "Number of Database records without corresponding message in SDX: 3");
        Assertions.assertTrue(emailBody.matches(".*Number of invalid records in Database:</td>\\s*<td>0.*"),
                "Number of invalid records in Database: 0");
        Assertions.assertTrue(emailBody.matches(".*Exceptions while attempting automatic cleanup.*"));
        Assertions.assertTrue(emailBody.matches(".*" + exText + ".*"));
    }

    @Test
    public void generateSdxSummaryEmail_withInvDbRecordsSection() throws IOException {
        // Arrange
        EmailFormatter uut = new EmailFormatter();

        List<CActiveTim> toResend = new ArrayList<>();
        List<CAdvisorySituationDataDeposit> deleteFromSdx = new ArrayList<>();
        List<CActiveTim> invDbRecords = new ArrayList<>();

        ActiveTim invRecord = new ActiveTim();
        invRecord.setActiveTimId(100l);
        invRecord.setSatRecordId("invHex");

        invDbRecords.add(new CActiveTim(invRecord));

        // Act
        String emailBody = uut.generateSdxSummaryEmail(0, 0, 0, toResend, deleteFromSdx, invDbRecords, "");

        // Assert
        Assertions.assertTrue(emailBody.matches(".*<h3>Invalid Database records</h3>.*"),
                "Contains section: Invalid Database records");
        Assertions.assertTrue(emailBody.matches(".*<tr><td>100</td><td>invHex</td></tr>.*"),
                "<tr><td>100</td><td>invHex</td></tr>");
    }

    @Test
    public void generateSdxSummaryEmail_withToResendSection() throws IOException {
        // Arrange
        EmailFormatter uut = new EmailFormatter();

        List<CActiveTim> toResend = new ArrayList<>();
        List<CAdvisorySituationDataDeposit> deleteFromSdx = new ArrayList<>();
        List<CActiveTim> invDbRecords = new ArrayList<>();

        ActiveTim resend = new ActiveTim();
        resend.setActiveTimId(200l);
        resend.setSatRecordId("AA1234");

        toResend.add(new CActiveTim(resend));

        // Act
        String emailBody = uut.generateSdxSummaryEmail(0, 1, 0, toResend, deleteFromSdx, invDbRecords, "");

        // Assert
        Assertions.assertTrue(emailBody.matches(".*<h3>ActiveTims to resend to SDX</h3>.*"),
                "Contains section: ActiveTims to resend to SDX");
        Assertions.assertTrue(emailBody.matches(".*<tr><td>200</td><td>AA1234</td></tr>.*"),
                "<tr><td>200</td><td>AA1234</td></tr>");
    }

    @Test
    public void generateSdxSummaryEmail_withStaleSdxSection() throws IOException {
        // Arrange
        EmailFormatter uut = new EmailFormatter();

        List<CActiveTim> toResend = new ArrayList<>();
        List<CAdvisorySituationDataDeposit> deleteFromSdx = new ArrayList<>();
        List<CActiveTim> invDbRecords = new ArrayList<>();

        AdvisorySituationDataDeposit invAsdd = new AdvisorySituationDataDeposit();
        invAsdd.setRecordId(-200);
        deleteFromSdx.add(new CAdvisorySituationDataDeposit(invAsdd, null));

        // Act
        String emailBody = uut.generateSdxSummaryEmail(1, 0, 0, toResend, deleteFromSdx, invDbRecords, "");

        // Assert
        Assertions.assertTrue(emailBody.matches(".*<h3>Orphaned records to delete from SDX</h3>.*"),
                "Contains section: Orphaned records to delete from SDX");
        Assertions.assertTrue(emailBody.matches(".*<tr><td>-200</td></tr>.*"), "<tr><td>-200</td></tr>");
    }

    @Test
    public void generateRsuSummaryEmail_success() throws IOException {
        // Arrange
        String expectedEmail = TestHelper.readFile("/email-snapshots/rsuSummary_success.html", getClass());
        EmailFormatter uut = new EmailFormatter();

        List<RsuValidationRecord> valRecords = new ArrayList<>();
        valRecords.add(new RsuValidationRecord(testRsuInfo));
        List<String> unexpectedErrors = new ArrayList<>();

        // Act
        String emailBody = uut.generateRsuSummaryEmail(valRecords, unexpectedErrors);

        // Assert
        Assertions.assertEquals(expectedEmail, emailBody);
    }

    @Test
    public void generateRsuSummaryEmail_unresponsiveRsu() throws IOException {
        // Arrange
        String expectedEmail = TestHelper.readFile("/email-snapshots/rsuSummary_unresponsiveRsu.html", getClass());
        EmailFormatter uut = new EmailFormatter();

        List<RsuValidationRecord> valRecords = new ArrayList<>();
        List<String> unexpectedErrors = new ArrayList<>();

        RsuValidationResult valResult = new RsuValidationResult();
        valResult.setRsuUnresponsive(true);

        RsuValidationRecord record = new RsuValidationRecord(testRsuInfo);
        record.addValidationResult(valResult);
        valRecords.add(record);

        // Act
        String emailBody = uut.generateRsuSummaryEmail(valRecords, unexpectedErrors);

        // Assert
        Assertions.assertEquals(expectedEmail, emailBody);
    }

    @Test
    public void generateRsuSummaryEmail_invalidRsu() throws IOException {
        // Arrange
        String expectedEmail = TestHelper.readFile("/email-snapshots/rsuSummary_invalidRsu.html", getClass());
        EmailFormatter uut = new EmailFormatter();

        List<RsuValidationRecord> valRecords = new ArrayList<>();
        List<String> unexpectedErrors = new ArrayList<>();

        RsuValidationRecord record = new RsuValidationRecord(testRsuInfo);
        RsuValidationResult firstPass = resultWithInconsistencies(true);
        record.addValidationResult(firstPass);
        RsuValidationResult secondPass = resultWithInconsistencies(false);
        record.addValidationResult(secondPass);

        valRecords.add(record);

        // Act
        String emailBody = uut.generateRsuSummaryEmail(valRecords, unexpectedErrors);

        // Assert
        Assertions.assertEquals(expectedEmail, emailBody);
    }

    @Test
    public void generateRsuSummaryEmail_unexpectedError() throws IOException {
        // Arrange
        String expectedEmail = TestHelper.readFile("/email-snapshots/rsuSummary_unexpectedError.html", getClass());
        EmailFormatter uut = new EmailFormatter();

        List<RsuValidationRecord> valRecords = new ArrayList<>();
        List<String> unexpectedErrors = new ArrayList<>();

        unexpectedErrors.add("Error occurred while fetching all RSUs - "
                + "unable to validate any RSUs that don't have an existing, active TIM.");

        // Act
        String emailBody = uut.generateRsuSummaryEmail(valRecords, unexpectedErrors);

        // Assert
        Assertions.assertEquals(expectedEmail, emailBody);
    }

    @Test
    public void generateRsuSummaryEmail_error2ndPass() throws IOException {
        // Arrange
        String expectedEmail = TestHelper.readFile("/email-snapshots/rsuSummary_error2ndPass.html", getClass());
        EmailFormatter uut = new EmailFormatter();

        List<RsuValidationRecord> valRecords = new ArrayList<>();
        List<String> unexpectedErrors = new ArrayList<>();

        RsuValidationRecord record = new RsuValidationRecord(testRsuInfo);
        RsuValidationResult firstPass = resultWithInconsistencies(true);
        record.addValidationResult(firstPass);

        // An error occurred during the second call to validateRsu.
        record.setError("Error while validating RSU");

        valRecords.add(record);

        // Act
        String emailBody = uut.generateRsuSummaryEmail(valRecords, unexpectedErrors);

        // Assert
        Assertions.assertEquals(expectedEmail, emailBody);
    }

    @Test
    public void generateTmddSummaryEmail_success() throws IOException {
        // Arrange
        EmailFormatter uut = new EmailFormatter();

        List<ActiveTim> unableToVerify = new ArrayList<>();
        unableToVerify.add(new ActiveTim() {
            {
                setActiveTimId(1234l);
                setClientId("AA1234");
            }
        });
        List<ActiveTimValidationResult> validationResults = new ArrayList<>();

        String exText = "A great exception";

        // Act
        String result = uut.generateTmddSummaryEmail(unableToVerify, validationResults, exText);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.matches(".*<p>1234 \\(AA1234\\)</p>.*"));
        Assertions.assertTrue(result.matches(".*Exceptions while attempting automatic cleanup.*"));
        Assertions.assertTrue(result.matches(".*" + exText + ".*"));
    }

    @Test
    public void generateTmddSummaryEmail_inconsistencies() throws IOException {
        // Arrange
        EmailFormatter uut = new EmailFormatter();

        List<ActiveTim> unableToVerify = new ArrayList<>();
        List<ActiveTimValidationResult> validationResults = new ArrayList<>();

        ActiveTim tim = new ActiveTim();
        tim.setActiveTimId(1234l);
        tim.setClientId("AA1234");
        ActiveTimError error = new ActiveTimError(ActiveTimErrorType.endPoint, "timValue", "tmddValue");

        ActiveTimValidationResult valResult = new ActiveTimValidationResult();
        valResult.setActiveTim(tim);
        valResult.getErrors().add(error);
        validationResults.add(valResult);

        // Act
        String result = uut.generateTmddSummaryEmail(unableToVerify, validationResults, "");

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.contains("<h4>1234 (AA1234)</h4>"));
        Assertions.assertTrue(result.contains("<td>End Point</td><td>timValue</td><td>tmddValue</td>"));
    }

    @Test
    public void generateTmddSummaryEmail_inconsistenciesWithNull() throws IOException {
        // Arrange
        EmailFormatter uut = new EmailFormatter();

        List<ActiveTim> unableToVerify = new ArrayList<>();
        List<ActiveTimValidationResult> validationResults = new ArrayList<>();

        ActiveTim tim = new ActiveTim();
        tim.setActiveTimId(1234l);
        tim.setClientId("AA1234");
        ActiveTimError error = new ActiveTimError(ActiveTimErrorType.endPoint, null, "tmddValue");

        ActiveTimValidationResult valResult = new ActiveTimValidationResult();
        valResult.setActiveTim(tim);
        valResult.getErrors().add(error);
        validationResults.add(valResult);

        // Act
        String result = uut.generateTmddSummaryEmail(unableToVerify, validationResults, "");

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.contains("<h4>1234 (AA1234)</h4>"));
        Assertions.assertTrue(result.contains("<td>End Point</td><td>null</td><td>tmddValue</td>"));
    }

    private RsuValidationResult resultWithInconsistencies(boolean beforeAutocorrect) {
        RsuValidationResult result = new RsuValidationResult();

        // 2 ActiveTims, collided at index 2 on RSU
        ActiveTim coll1 = new ActiveTim() {
            {
                setActiveTimId(2l);
            }
        };

        ActiveTim coll2 = new ActiveTim() {
            {
                setActiveTimId(3l);
            }
        };
        Collision c = new Collision(2, Arrays.asList(coll1, coll2));
        result.setCollisions(Arrays.asList(c));

        if (beforeAutocorrect) {
            // ActiveTim missing from RSU
            ActiveTim missing = new ActiveTim() {
                {
                    setActiveTimId(1l);
                    setRsuIndex(1);
                }
            };
            result.setMissingFromRsu(Arrays.asList(missing));

            // ActiveTim stale on index 3
            ActiveTim staleTim = new ActiveTim() {
                {
                    setActiveTimId(4l);
                    setStartDateTime("2020-01-01");
                    setRsuIndex(3);
                }
            };
            RsuIndexInfo indexInfo = new RsuIndexInfo(3, "2020-02-02");

            ActiveTimMapping staleMapping = new ActiveTimMapping(staleTim, indexInfo);
            result.getStaleIndexes().add(staleMapping);

            // Unaccounted for RSU index
            result.setUnaccountedForIndices(Arrays.asList(3));
        }

        return result;
    }
}
