package com.trihydro.tasks.actions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Calendar;
import java.util.Date;

import javax.mail.MessagingException;

import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.tasks.config.DataTasksConfiguration;
import com.trihydro.tasks.models.SignTimModel;
import com.trihydro.tasks.models.hsmresponse.HsmResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class VerifyHSMFunctionalTest {
        @Mock
        private DataTasksConfiguration mockConfig;
        @Mock
        private Utility mockUtility;
        @Mock
        private RestTemplateProvider mockRestTemplateProvider;
        @Mock
        private RestTemplate mockRestTemplate;
        @Mock
        private EmailHelper mockEmailHelper;
        @Mock
        private ResponseEntity<HsmResponse> mockResponseEntity;

        @InjectMocks
        private VerifyHSMFunctional uut;

        @BeforeEach
        public void setup() {
                doReturn(mockRestTemplate).when(mockRestTemplateProvider).GetRestTemplate();
                doReturn(mockResponseEntity).when(mockRestTemplate).exchange(any(String.class), any(HttpMethod.class),
                                Mockito.<HttpEntity<String>>any(), Mockito.<Class<HsmResponse>>any());
        }

        @Test
        public void cleanupActiveTims_runTest_OK() {
                // Arrange
                doReturn(HttpStatus.OK).when(mockResponseEntity).getStatusCode();

                // Act
                uut.run();

                // assert exchange called
                verify(mockRestTemplate).exchange(any(String.class), any(HttpMethod.class),
                                Mockito.<HttpEntity<SignTimModel>>any(), Mockito.<Class<HsmResponse>>any());
                verifyNoInteractions(mockEmailHelper);
        }

        @Test
        public void cleanupActiveTims_runTest_OK_PrevErr() throws MailException, MessagingException {
                // Arrange
                doReturn(HttpStatus.OK).when(mockResponseEntity).getStatusCode();
                uut.errorLastSent = new Date();

                // Act
                uut.run();

                // assert exchange called
                verify(mockRestTemplate).exchange(any(String.class), any(HttpMethod.class),
                                Mockito.<HttpEntity<SignTimModel>>any(), Mockito.<Class<HsmResponse>>any());
                String email = "HSM Functional Tester was successful in attempting to sign a TIM";
                verify(mockEmailHelper).SendEmail(mockConfig.getAlertAddresses(), null, "HSM Back Up", email,
                                mockConfig.getMailPort(), mockConfig.getMailHost(), mockConfig.getFromEmail());
        }

        @Test
        public void cleanupActiveTims_runTest_Err_NullSendDate() throws MailException, MessagingException {
                // Arrange
                doReturn(HttpStatus.I_AM_A_TEAPOT).when(mockResponseEntity).getStatusCode();
                doReturn(HttpStatus.I_AM_A_TEAPOT.value()).when(mockResponseEntity).getStatusCodeValue();

                // Act
                uut.run();

                // Assert
                verify(mockRestTemplate).exchange(any(String.class), any(HttpMethod.class),
                                Mockito.<HttpEntity<SignTimModel>>any(), Mockito.<Class<HsmResponse>>any());
                String email = "HSM Functional Tester encountered an error while attempting to sign a TIM. The response from the HSM at ";
                email += mockConfig.getHsmUrl() + "/signtim/";
                email += " was Http Status " + HttpStatus.I_AM_A_TEAPOT.value();
                email += ". The body of the response is as follows: ";
                email += "<br/><br/>";
                email += mockResponseEntity.getBody();

                verify(mockEmailHelper).SendEmail(mockConfig.getAlertAddresses(), null, "HSM Error", email,
                                mockConfig.getMailPort(), mockConfig.getMailHost(), mockConfig.getFromEmail());
        }

        @Test
        public void cleanupActiveTims_runTest_Err_SendDateWithinPeriod() throws MailException, MessagingException {
                // Arrange
                doReturn(HttpStatus.I_AM_A_TEAPOT).when(mockResponseEntity).getStatusCode();
                doReturn(10).when(mockConfig).getHsmErrorEmailFrequencyMinutes();// only send email within 10 mins
                // set date to 5 minutes ago
                var lastSent = Calendar.getInstance();
                lastSent.setTime(new Date());
                lastSent.add(Calendar.MINUTE, -5);
                uut.errorLastSent = lastSent.getTime();

                // Act
                uut.run();

                // Assert
                verify(mockRestTemplate).exchange(any(String.class), any(HttpMethod.class),
                                Mockito.<HttpEntity<SignTimModel>>any(), Mockito.<Class<HsmResponse>>any());
                verifyNoInteractions(mockEmailHelper);
        }

        @Test
        public void cleanupActiveTims_runTest_Err_SendDateOutsidePeriod() throws MailException, MessagingException {
                // Arrange
                doReturn(HttpStatus.I_AM_A_TEAPOT).when(mockResponseEntity).getStatusCode();
                doReturn(HttpStatus.I_AM_A_TEAPOT.value()).when(mockResponseEntity).getStatusCodeValue();
                doReturn(10).when(mockConfig).getHsmErrorEmailFrequencyMinutes();// only send email within 10 mins
                // set date to 5 minutes ago
                var lastSent = Calendar.getInstance();
                lastSent.setTime(new Date());
                lastSent.add(Calendar.MINUTE, -15);// set 15 minutes ago so it fires off
                uut.errorLastSent = lastSent.getTime();

                // Act
                uut.run();

                // Assert
                verify(mockRestTemplate).exchange(any(String.class), any(HttpMethod.class),
                                Mockito.<HttpEntity<SignTimModel>>any(), Mockito.<Class<HsmResponse>>any());
                String email = "HSM Functional Tester encountered an error while attempting to sign a TIM. The response from the HSM at ";
                email += mockConfig.getHsmUrl() + "/signtim/";
                email += " was Http Status " + HttpStatus.I_AM_A_TEAPOT.value();
                email += ". The body of the response is as follows: ";
                email += "<br/><br/>";
                email += mockResponseEntity.getBody();

                verify(mockEmailHelper).SendEmail(mockConfig.getAlertAddresses(), null, "HSM Error", email,
                                mockConfig.getMailPort(), mockConfig.getMailHost(), mockConfig.getFromEmail());
        }
}
