package com.trihydro.cvlogger.app;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.trihydro.cvlogger.app.services.TracManager;
import com.trihydro.cvlogger.config.DataLoggerConfiguration;
import com.trihydro.library.helpers.JavaMailSenderImplProvider;
import com.trihydro.library.helpers.JsonToJavaConverter;
import com.trihydro.library.model.TracMessageSent;
import com.trihydro.library.model.TracMessageType;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.library.service.TracMessageSentService;
import com.trihydro.library.service.TracMessageTypeService;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class TracManagerTest {

        @Mock
        private RestTemplate restTemplate;

        @Mock
        private JavaMailSenderImpl jmsi;
        @Mock
        private TracMessageTypeService mockTrackMessageTypeService;
        @Mock
        private TracMessageSentService mockTracMessageSentService;
        @Mock
        private JavaMailSenderImplProvider mockJavaMailSenderImplProvider;
        @Mock
        private RestTemplateProvider mockRestTemplateProvider;

        @Spy
        JsonToJavaConverter jsonToJava = new JsonToJavaConverter();

        @InjectMocks
        TracManager uut;

        @BeforeEach
        public void setup() {
                List<TracMessageType> tmts = new ArrayList<TracMessageType>();
                TracMessageType tmt = new TracMessageType();
                tmt.setTracMessageType("DN");
                tmt.setTracMessageTypeId(-1);
                tmts.add(tmt);
                when(mockTrackMessageTypeService.selectAll()).thenReturn(tmts);
                when(mockRestTemplateProvider.GetRestTemplate()).thenReturn(restTemplate);
        }

        private void setupMailSender(){
                when(mockJavaMailSenderImplProvider.getJSenderImpl(anyString(), anyInt())).thenReturn(jmsi);
        }

        @Test
        public void TestSubmitDNMsgToTrac_SuccessFirstRound() throws IOException, URISyntaxException {
                // setup
                when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), Mockito.<HttpEntity<?>>any(),
                                Mockito.<Class<String>>any()))
                                                .thenReturn(new ResponseEntity<String>("ok", HttpStatus.OK));

                String value = new String(Files.readAllBytes(
                                Paths.get(getClass().getResource("/distressNotification_OdeOutput.json").toURI())));
                DataLoggerConfiguration config = new DataLoggerConfiguration();
                config.setTracUrl("");

                // call function to test
                uut.submitDNMsgToTrac(value, config);

                // assert TracMessageSentService.selectPacketIds called once
                verify(mockTracMessageSentService).selectPacketIds();

                // assert exchange called once
                verify(restTemplate).exchange(any(URI.class), any(HttpMethod.class), Mockito.<HttpEntity<String>>any(),
                                Mockito.<Class<String>>any());

                ArgumentCaptor<TracMessageSent> argument = ArgumentCaptor.forClass(TracMessageSent.class);
                verify(mockTracMessageSentService).insertTracMessageSent(argument.capture());
                Assertions.assertEquals("EC9C236B0000000000", argument.getValue().getPacketId());
                Assertions.assertEquals(Integer.valueOf(200), argument.getValue().getRestResponseCode());
                Assertions.assertEquals(true, argument.getValue().isMessageSent());
                Assertions.assertEquals(false, argument.getValue().isEmailSent());
        }

        @Test
        public void TestSubmitDNMsgToTrac_SuccessSecondRound() throws IOException, URISyntaxException {
                // setup
                when(restTemplate.exchange(any(URI.class), Mockito.any(HttpMethod.class), Mockito.<HttpEntity<?>>any(),
                                Mockito.<Class<String>>any())).thenThrow(new RestClientException("error"))
                                                .thenReturn(new ResponseEntity<String>("ok", HttpStatus.OK));

                String value = new String(Files.readAllBytes(
                                Paths.get(getClass().getResource("/distressNotification_OdeOutput.json").toURI())));
                DataLoggerConfiguration config = new DataLoggerConfiguration();
                config.setTracUrl("");

                // call function to test
                uut.submitDNMsgToTrac(value, config);

                // assert TracMessageSentService.selectPacketIds called once
                verify(mockTracMessageSentService).selectPacketIds();
                // assert exchange called once
                verify(restTemplate, Mockito.times(2)).exchange(any(URI.class), any(HttpMethod.class),
                                Mockito.<HttpEntity<String>>any(), Mockito.<Class<String>>any());
                verify(mockTracMessageSentService).insertTracMessageSent(any(TracMessageSent.class));
        }

        @Test
        public void TestSubmitDNMsgToTrac_ErrorSendEmail() throws IOException, URISyntaxException {
                // setup
                setupMailSender();
                when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), Mockito.<HttpEntity<?>>any(),
                                Mockito.<Class<String>>any())).thenThrow(new RestClientException("error"));

                String value = new String(Files.readAllBytes(
                                Paths.get(getClass().getResource("/distressNotification_OdeOutput.json").toURI())));
                DataLoggerConfiguration config = new DataLoggerConfiguration();
                config.setTracUrl("http://test.com");
                config.setMailHost("mailHost");
                config.setMailPort(22);
                String[] addresses = new String[1];
                addresses[0] = "email@test.com";
                config.setAlertAddresses(addresses);

                // call function to test
                uut.submitDNMsgToTrac(value, config);

                verify(mockTracMessageSentService).selectPacketIds();

                // assert exchange called once
                verify(restTemplate, Mockito.times(2)).exchange(any(URI.class), any(HttpMethod.class),
                                Mockito.<HttpEntity<String>>any(), Mockito.<Class<String>>any());

                ArgumentCaptor<TracMessageSent> argument = ArgumentCaptor.forClass(TracMessageSent.class);
                verify(mockTracMessageSentService).insertTracMessageSent(argument.capture());
                Assertions.assertEquals("EC9C236B0000000000", argument.getValue().getPacketId());
                Assertions.assertEquals(Integer.valueOf(-1), argument.getValue().getRestResponseCode());
                Assertions.assertEquals(false, argument.getValue().isMessageSent());
                Assertions.assertEquals(true, argument.getValue().isEmailSent());

                verify(jmsi).send(any(SimpleMailMessage.class));
        }

        @Test
        public void TestSubmitDNMsgToTrac_ServerErrorSendEmail() throws IOException, URISyntaxException {
                // setup
                setupMailSender();
                when(restTemplate.exchange(any(URI.class), Mockito.any(HttpMethod.class), Mockito.<HttpEntity<?>>any(),
                                Mockito.<Class<String>>any())).thenReturn(
                                                new ResponseEntity<String>("Error", HttpStatus.INTERNAL_SERVER_ERROR));

                String value = new String(Files.readAllBytes(
                                Paths.get(getClass().getResource("/distressNotification_OdeOutput.json").toURI())));
                DataLoggerConfiguration config = new DataLoggerConfiguration();
                config.setTracUrl("http://test.com");
                config.setMailHost("mailHost");
                config.setMailPort(22);
                String[] addresses = new String[1];
                addresses[0] = "email@test.com";
                config.setAlertAddresses(addresses);

                // call function to test
                uut.submitDNMsgToTrac(value, config);

                verify(mockTracMessageSentService).selectPacketIds();

                // assert exchange called once
                verify(restTemplate, Mockito.times(1)).exchange(any(URI.class), any(HttpMethod.class),
                                Mockito.<HttpEntity<String>>any(), Mockito.<Class<String>>any());

                ArgumentCaptor<TracMessageSent> argument = ArgumentCaptor.forClass(TracMessageSent.class);
                verify(mockTracMessageSentService).insertTracMessageSent(argument.capture());
                Assertions.assertEquals("EC9C236B0000000000", argument.getValue().getPacketId());
                Assertions.assertEquals(Integer.valueOf(500), argument.getValue().getRestResponseCode());
                Assertions.assertEquals(false, argument.getValue().isMessageSent());
                Assertions.assertEquals(true, argument.getValue().isEmailSent());

                verify(jmsi).send(any(SimpleMailMessage.class));
        }

        @Test
        public void TestSubmitDNMsgToTrac_ServerErrorSendEmailFail() throws IOException, URISyntaxException {
                // setup
                setupMailSender();
                when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), Mockito.<HttpEntity<?>>any(),
                                Mockito.<Class<String>>any())).thenReturn(
                                                new ResponseEntity<String>("Error", HttpStatus.INTERNAL_SERVER_ERROR));

                doThrow(new MailSendException("Exception")).when(jmsi).send(any(SimpleMailMessage.class));

                String value = new String(Files.readAllBytes(
                                Paths.get(getClass().getResource("/distressNotification_OdeOutput.json").toURI())));
                DataLoggerConfiguration config = new DataLoggerConfiguration();
                config.setTracUrl("http://test.com");
                config.setMailHost("mailHost");
                config.setMailPort(22);
                String[] addresses = new String[1];
                addresses[0] = "email@test.com";
                config.setAlertAddresses(addresses);

                // call function to test
                uut.submitDNMsgToTrac(value, config);

                verify(mockTracMessageSentService).selectPacketIds();

                // assert exchange called once
                verify(restTemplate, Mockito.times(1)).exchange(any(URI.class), any(HttpMethod.class),
                                Mockito.<HttpEntity<String>>any(), Mockito.<Class<String>>any());

                ArgumentCaptor<TracMessageSent> argument = ArgumentCaptor.forClass(TracMessageSent.class);
                verify(mockTracMessageSentService).insertTracMessageSent(argument.capture());
                Assertions.assertEquals("EC9C236B0000000000", argument.getValue().getPacketId());
                Assertions.assertEquals(Integer.valueOf(500), argument.getValue().getRestResponseCode());
                Assertions.assertEquals(false, argument.getValue().isMessageSent());
                Assertions.assertEquals(false, argument.getValue().isEmailSent());

                verify(jmsi).send(any(SimpleMailMessage.class));
        }

        @Test
        public void TestSubmitDNMsgToTrac_ServerErrorCheckEmail() throws IOException, URISyntaxException {
                // setup
                setupMailSender();
                when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), Mockito.<HttpEntity<?>>any(),
                                Mockito.<Class<String>>any())).thenReturn(
                                                new ResponseEntity<String>("Error", HttpStatus.INTERNAL_SERVER_ERROR));

                doThrow(new MailSendException("Exception")).when(jmsi).send(any(SimpleMailMessage.class));

                String value = new String(Files.readAllBytes(
                                Paths.get(getClass().getResource("/distressNotification_OdeOutput.json").toURI())));
                DataLoggerConfiguration config = new DataLoggerConfiguration();
                config.setTracUrl("");
                config.setMailHost("mailHost");
                config.setMailPort(22);
                String[] addresses = new String[3];
                addresses[0] = "email@test.com";
                addresses[1] = "email2@test.com";
                addresses[2] = "email@trihydro.com";
                config.setAlertAddresses(addresses);

                // call function to test
                uut.submitDNMsgToTrac(value, config);

                // verify to field of email
                ArgumentCaptor<SimpleMailMessage> smmArgument = ArgumentCaptor.forClass(SimpleMailMessage.class);
                verify(jmsi).send(smmArgument.capture());
                String[] tos = smmArgument.getValue().getTo();
                Assertions.assertEquals(2, tos.length);
                List<String> tosList = Arrays.asList(tos);
                Assertions.assertTrue(tosList.contains("email@test.com"));
                Assertions.assertTrue(tosList.contains("email2@test.com"));

                // verify bcc field
                String[] bcc = smmArgument.getValue().getBcc();
                Assertions.assertEquals(1, bcc.length);
                Assertions.assertEquals("email@trihydro.com", bcc[0]);
        }
}