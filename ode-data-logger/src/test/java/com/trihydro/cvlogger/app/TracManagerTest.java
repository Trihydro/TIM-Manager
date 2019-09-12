package com.trihydro.cvlogger.app;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.trihydro.cvlogger.app.services.TracManager;
import com.trihydro.library.model.ConfigProperties;
import com.trihydro.library.model.TracMessageSent;
import com.trihydro.library.service.TracMessageSentService;
import com.trihydro.library.service.TracMessageTypeService;
import com.trihydro.library.model.TracMessageType;

/**
 * Unit tests for TimRefreshController
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ TracMessageSentService.class, TracMessageTypeService.class })
public class TracManagerTest {

        @Mock
        private RestTemplate restTemplate;

        @Mock
        private JavaMailSender jms;

        @InjectMocks
        TracManager uut;

        @Before
        public void setup() {
                PowerMockito.mockStatic(TracMessageSentService.class);
                PowerMockito.mockStatic(TracMessageTypeService.class);

                List<TracMessageType> tmts = new ArrayList<TracMessageType>();
                TracMessageType tmt = new TracMessageType();
                tmt.setTracMessageType("DN");
                tmt.setTracMessageTypeId(-1);
                tmts.add(tmt);
                when(TracMessageTypeService.selectAll()).thenReturn(tmts);
        }

        @Test
        public void TestSubmitDNMsgToTrac_SuccessFirstRound() throws IOException, URISyntaxException {
                // setup
                when(restTemplate.exchange(any(URI.class), Matchers.any(HttpMethod.class),
                                Matchers.<HttpEntity<?>>any(), Matchers.<Class<String>>any()))
                                                .thenReturn(new ResponseEntity<String>("ok", HttpStatus.OK));

                String value = new String(Files.readAllBytes(
                                Paths.get(getClass().getResource("/distressNotification_OdeOutput.json").toURI())));
                ConfigProperties config = new ConfigProperties();
                config.setTracUrl("");

                // call function to test
                uut.submitDNMsgToTrac(value, config);

                // verify static functions, called once
                PowerMockito.verifyStatic(VerificationModeFactory.times(1));
                // assert TracMessageSentService.selectAll called once
                TracMessageSentService.selectAll();

                // assert exchange called once
                verify(restTemplate).exchange(any(URI.class), any(HttpMethod.class), Matchers.<HttpEntity<String>>any(),
                                Matchers.<Class<String>>any());

                ArgumentCaptor<TracMessageSent> argument = ArgumentCaptor.forClass(TracMessageSent.class);
                PowerMockito.verifyStatic(VerificationModeFactory.times(1));
                TracMessageSentService.insertTracMessageSent(argument.capture());
                assertEquals("EC9C236B0000000000", argument.getValue().getPacketId());
                assertEquals(new Integer(200), argument.getValue().getRestResponseCode());
                assertEquals(true, argument.getValue().isMessageSent());
                assertEquals(false, argument.getValue().isEmailSent());
        }

        @Test
        public void TestSubmitDNMsgToTrac_SuccessSecondRound() throws IOException, URISyntaxException {
                // setup
                when(restTemplate.exchange(any(URI.class), Matchers.any(HttpMethod.class),
                                Matchers.<HttpEntity<?>>any(), Matchers.<Class<String>>any()))
                                                .thenThrow(new RestClientException("error"))
                                                .thenReturn(new ResponseEntity<String>("ok", HttpStatus.OK));

                String value = new String(Files.readAllBytes(
                                Paths.get(getClass().getResource("/distressNotification_OdeOutput.json").toURI())));
                ConfigProperties config = new ConfigProperties();
                config.setTracUrl("");

                // call function to test
                uut.submitDNMsgToTrac(value, config);

                // verify static functions, called once
                PowerMockito.verifyStatic(VerificationModeFactory.times(1));
                // assert TracMessageSentService.selectAll called once
                TracMessageSentService.selectAll();

                // assert exchange called once
                verify(restTemplate, Mockito.times(2)).exchange(any(URI.class), any(HttpMethod.class),
                                Matchers.<HttpEntity<String>>any(), Matchers.<Class<String>>any());

                PowerMockito.verifyStatic(VerificationModeFactory.times(1));
                // assert TracMessageSentService.insertTracMessageSent called once
                TracMessageSentService.insertTracMessageSent(any(TracMessageSent.class));
        }

        @Test
        public void TestSubmitDNMsgToTrac_ErrorSendEmail() throws IOException, URISyntaxException {
                // setup
                when(restTemplate.exchange(any(URI.class), Matchers.any(HttpMethod.class),
                                Matchers.<HttpEntity<?>>any(), Matchers.<Class<String>>any()))
                                                .thenThrow(new RestClientException("error"));

                String value = new String(Files.readAllBytes(
                                Paths.get(getClass().getResource("/distressNotification_OdeOutput.json").toURI())));
                ConfigProperties config = new ConfigProperties();
                config.setTracUrl("");
                config.setAlertAddresses("email@test.com");

                // call function to test
                uut.submitDNMsgToTrac(value, config);

                // verify static functions, called once
                PowerMockito.verifyStatic(VerificationModeFactory.times(1));
                // assert TracMessageSentService.selectAll called once
                TracMessageSentService.selectAll();

                // assert exchange called once
                verify(restTemplate, Mockito.times(2)).exchange(any(URI.class), any(HttpMethod.class),
                                Matchers.<HttpEntity<String>>any(), Matchers.<Class<String>>any());

                ArgumentCaptor<TracMessageSent> argument = ArgumentCaptor.forClass(TracMessageSent.class);
                PowerMockito.verifyStatic(VerificationModeFactory.times(1));
                TracMessageSentService.insertTracMessageSent(argument.capture());
                assertEquals("EC9C236B0000000000", argument.getValue().getPacketId());
                assertEquals(new Integer(-1), argument.getValue().getRestResponseCode());
                assertEquals(false, argument.getValue().isMessageSent());
                assertEquals(true, argument.getValue().isEmailSent());

                verify(jms).send(any(SimpleMailMessage.class));
        }

        @Test
        public void TestSubmitDNMsgToTrac_ServerErrorSendEmail() throws IOException, URISyntaxException {
                // setup
                when(restTemplate.exchange(any(URI.class), Matchers.any(HttpMethod.class),
                                Matchers.<HttpEntity<?>>any(), Matchers.<Class<String>>any())).thenReturn(
                                                new ResponseEntity<String>("Error", HttpStatus.INTERNAL_SERVER_ERROR));

                String value = new String(Files.readAllBytes(
                                Paths.get(getClass().getResource("/distressNotification_OdeOutput.json").toURI())));
                ConfigProperties config = new ConfigProperties();
                config.setTracUrl("");
                config.setAlertAddresses("email@test.com");

                // call function to test
                uut.submitDNMsgToTrac(value, config);

                // verify static functions, called once
                PowerMockito.verifyStatic(VerificationModeFactory.times(1));
                // assert TracMessageSentService.selectAll called once
                TracMessageSentService.selectAll();

                // assert exchange called once
                verify(restTemplate, Mockito.times(1)).exchange(any(URI.class), any(HttpMethod.class),
                                Matchers.<HttpEntity<String>>any(), Matchers.<Class<String>>any());

                ArgumentCaptor<TracMessageSent> argument = ArgumentCaptor.forClass(TracMessageSent.class);
                PowerMockito.verifyStatic(VerificationModeFactory.times(1));
                TracMessageSentService.insertTracMessageSent(argument.capture());
                assertEquals("EC9C236B0000000000", argument.getValue().getPacketId());
                assertEquals(new Integer(500), argument.getValue().getRestResponseCode());
                assertEquals(false, argument.getValue().isMessageSent());
                assertEquals(true, argument.getValue().isEmailSent());

                verify(jms).send(any(SimpleMailMessage.class));
        }

        @Test
        public void TestSubmitDNMsgToTrac_ServerErrorSendEmailFail() throws IOException, URISyntaxException {
                // setup
                when(restTemplate.exchange(any(URI.class), Matchers.any(HttpMethod.class),
                                Matchers.<HttpEntity<?>>any(), Matchers.<Class<String>>any())).thenReturn(
                                                new ResponseEntity<String>("Error", HttpStatus.INTERNAL_SERVER_ERROR));

                doThrow(new MailSendException("Exception")).when(jms).send(any(SimpleMailMessage.class));

                String value = new String(Files.readAllBytes(
                                Paths.get(getClass().getResource("/distressNotification_OdeOutput.json").toURI())));
                ConfigProperties config = new ConfigProperties();
                config.setTracUrl("");
                config.setAlertAddresses("email@test.com");

                // call function to test
                uut.submitDNMsgToTrac(value, config);

                // verify static functions, called once
                PowerMockito.verifyStatic(VerificationModeFactory.times(1));
                // assert TracMessageSentService.selectAll called once
                TracMessageSentService.selectAll();

                // assert exchange called once
                verify(restTemplate, Mockito.times(1)).exchange(any(URI.class), any(HttpMethod.class),
                                Matchers.<HttpEntity<String>>any(), Matchers.<Class<String>>any());

                ArgumentCaptor<TracMessageSent> argument = ArgumentCaptor.forClass(TracMessageSent.class);
                PowerMockito.verifyStatic(VerificationModeFactory.times(1));
                TracMessageSentService.insertTracMessageSent(argument.capture());
                assertEquals("EC9C236B0000000000", argument.getValue().getPacketId());
                assertEquals(new Integer(500), argument.getValue().getRestResponseCode());
                assertEquals(false, argument.getValue().isMessageSent());
                assertEquals(false, argument.getValue().isEmailSent());

                verify(jms).send(any(SimpleMailMessage.class));
        }

        @Test
        public void TestSubmitDNMsgToTrac_ServerErrorCheckEmail() throws IOException, URISyntaxException {
                // setup
                when(restTemplate.exchange(any(URI.class), Matchers.any(HttpMethod.class),
                                Matchers.<HttpEntity<?>>any(), Matchers.<Class<String>>any())).thenReturn(
                                                new ResponseEntity<String>("Error", HttpStatus.INTERNAL_SERVER_ERROR));

                doThrow(new MailSendException("Exception")).when(jms).send(any(SimpleMailMessage.class));

                String value = new String(Files.readAllBytes(
                                Paths.get(getClass().getResource("/distressNotification_OdeOutput.json").toURI())));
                ConfigProperties config = new ConfigProperties();
                config.setTracUrl("");
                config.setAlertAddresses("email@test.com,email2@test.com,email@trihydro.com");

                // call function to test
                uut.submitDNMsgToTrac(value, config);

                // verify to field of email
                ArgumentCaptor<SimpleMailMessage> smmArgument = ArgumentCaptor.forClass(SimpleMailMessage.class);
                verify(jms).send(smmArgument.capture());
                String[] tos = smmArgument.getValue().getTo();
                assertEquals(2, tos.length);
                List<String> tosList = Arrays.asList(tos);
                assertTrue(tosList.contains("email@test.com"));
                assertTrue(tosList.contains("email2@test.com"));

                // verify bcc field
                String[] bcc = smmArgument.getValue().getBcc();
                assertEquals(1, bcc.length);
                assertEquals("email@trihydro.com", bcc[0]);
        }
}