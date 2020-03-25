package com.trihydro.tasks.actions;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.AdvisorySituationDataDeposit;
import com.trihydro.tasks.config.EmailConfiguration;
import com.trihydro.tasks.models.CActiveTim;
import com.trihydro.tasks.models.CAdvisorySituationDataDeposit;
import com.trihydro.tasks.models.Collision;
import com.trihydro.tasks.models.EnvActiveTim;
import com.trihydro.tasks.models.Environment;
import com.trihydro.tasks.models.RsuValidationResult;

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
                assertTrue("Number of stale records on SDX (different ITIS codes than ActiveTim): 2", emailBody.matches(
                                "[\\s\\S]*Number of stale records on SDX \\(different ITIS codes than ActiveTim\\):</td>\\s*<td>2[\\s\\S]*"));
                assertTrue("Number of messages on SDX without corresponding Oracle record: 1", emailBody.matches(
                                "[\\s\\S]*Number of messages on SDX without corresponding Oracle record:</td>\\s*<td>1[\\s\\S]*"));
                assertTrue("Number of Oracle records without corresponding message in SDX: 3", emailBody.matches(
                                "[\\s\\S]*Number of Oracle records without corresponding message in SDX:</td>\\s*<td>3[\\s\\S]*"));
                assertTrue("Number of invalid records in Oracle: 0", emailBody
                                .matches("[\\s\\S]*Number of invalid records in Oracle:</td>\\s*<td>0[\\s\\S]*"));
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
                assertTrue("<tr><td>-200</td></tr>", emailBody.matches("[\\s\\S]*<tr><td>-200</td></tr>[\\s\\S]*"));
        }

        @Test
        public void generateRsuSummaryEmail_success() throws IOException {
                // Arrange
                EmailConfiguration uut = new EmailConfiguration();

                List<String> unresponsiveRsus = new ArrayList<>();
                List<String> unexpectedErrors = new ArrayList<>();
                List<RsuValidationResult> rsusWithErrors = new ArrayList<>();

                unresponsiveRsus.add("10.145.0.0");

                // Act
                String emailBody = uut.generateRsuSummaryEmail(unresponsiveRsus, rsusWithErrors, unexpectedErrors);

                // Assert
                assertTrue("Unable to verify the following RSUs 10.145.0.0",
                                emailBody.matches("[\\s\\S]*<div class=\"indent\"><p>10.145.0.0</p></div>[\\s\\S]*"));
        }

        @Test
        public void generateRsuSummaryEmail_invalidRsu() throws IOException {
                // Arrange
                EmailConfiguration uut = new EmailConfiguration();

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
                }, Environment.DEV);
                Collision c = new Collision(2, Arrays.asList(coll1, coll2));
                invalidRsu.setCollisions(Arrays.asList(c));

                // Unaccounted for RSU index
                invalidRsu.setUnaccountedForIndices(Arrays.asList(3));

                rsusWithErrors.add(invalidRsu);

                // Act
                String emailBody = uut.generateRsuSummaryEmail(unresponsiveRsus, rsusWithErrors, unexpectedErrors);

                // Assert
                assertTrue("<h3>RSUs with Errors</h3><h4>10.145.0.0</h4>", emailBody
                                .matches("[\\s\\S]*<h3>RSUs with Errors</h3>\\s*<h4>10.145.0.0</h4>[\\s\\S]*"));
                assertTrue("... Populated Indexes w/o ActiveTim record ... 3 ...", emailBody
                                .matches("[\\s\\S]*Populated Indexes w/o ActiveTim record[\\s\\S]*3[\\s\\S]*"));
                assertTrue("... Active TIMs missing from RSU ... <tr><td>1</td><td>1</td></tr> ...", emailBody.matches(
                                "[\\s\\S]*Active TIMs missing from RSU[\\s\\S]*<tr><td>1</td><td>1</td></tr>[\\s\\S]*"));
                assertTrue("... Active TIMs claiming same index ... <tr><td>2</td><td>2, 3</td></tr> ...", emailBody
                                .matches("[\\s\\S]*Active TIMs claiming same index[\\s\\S]*<tr><td>2</td><td>2, 3</td></tr>[\\s\\S]*"));
        }

        @Test
        public void generateRsuSummaryEmail_unexpectedError() throws IOException {
                // Arrange
                EmailConfiguration uut = new EmailConfiguration();

                List<String> unresponsiveRsus = new ArrayList<>();
                List<String> unexpectedErrors = new ArrayList<>();
                List<RsuValidationResult> rsusWithErrors = new ArrayList<>();

                unexpectedErrors.add("10.145.0.0: InterruptedException");

                // Act
                String emailBody = uut.generateRsuSummaryEmail(unresponsiveRsus, rsusWithErrors, unexpectedErrors);

                // Assert
                assertTrue("... <h3>Unexpected Errors Processing RSUs</h3> ... <li>10.145.0.0: InterruptedException</li> ...",
                                emailBody.matches(
                                                "[\\s\\S]*<h3>Unexpected Errors Processing RSUs</h3>\\s*<ul>\\s*<li>10.145.0.0: InterruptedException</li>\\s*</ul>[\\s\\S]*"));
        }
}