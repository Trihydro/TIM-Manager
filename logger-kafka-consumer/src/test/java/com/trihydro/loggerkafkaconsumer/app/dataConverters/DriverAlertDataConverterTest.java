package com.trihydro.loggerkafkaconsumer.app.dataConverters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.trihydro.library.helpers.JsonToJavaConverter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeDriverAlertPayload;
import us.dot.its.jpo.ode.model.OdeLogMetadata;
import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeLogMetadata.SecurityResultCode;
import us.dot.its.jpo.ode.model.OdeLogMsgMetadataLocation;
import us.dot.its.jpo.ode.model.OdeMsgMetadata.GeneratedBy;
import us.dot.its.jpo.ode.model.ReceivedMessageDetails;
import us.dot.its.jpo.ode.model.SerialId;

@ExtendWith(MockitoExtension.class)
public class DriverAlertDataConverterTest {

    @Spy
    private JsonToJavaConverter jsonToJava = new JsonToJavaConverter();

    @InjectMocks
    private DriverAlertDataConverter uut;

    @Test
    public void processDriverAlertJson() throws IOException {

        // Arrange
        ReceivedMessageDetails receivedMessageDetails = new ReceivedMessageDetails();
        OdeLogMsgMetadataLocation locationData = new OdeLogMsgMetadataLocation();
        SerialId serialId;

        OdeLogMetadata odeDriverAlertMetadata = new OdeLogMetadata();
        odeDriverAlertMetadata.setRecordGeneratedBy(GeneratedBy.OBU);

        locationData.setElevation("1486.0");
        locationData.setHeading("331.9000");
        locationData.setLatitude("40.4739771");
        locationData.setLongitude("-104.9691666");
        locationData.setSpeed("1.04");

        receivedMessageDetails.setLocationData(locationData);
        odeDriverAlertMetadata.setReceivedMessageDetails(receivedMessageDetails);

        odeDriverAlertMetadata.setSchemaVersion(3);
        odeDriverAlertMetadata.setSecurityResultCode(SecurityResultCode.unknown);
        odeDriverAlertMetadata.setPayloadType("us.dot.its.jpo.ode.model.OdeDriverAlertPayload");

        serialId = new SerialId("408f1086-d028-4919-afc7-50ec097ddba9", 1, 12, 2, 0);
        odeDriverAlertMetadata.setSerialId(serialId);

        odeDriverAlertMetadata.setSanitized(false);
        odeDriverAlertMetadata.setRecordGeneratedAt("2017-10-04T21:03:56.614Z[UTC]");

        odeDriverAlertMetadata.setRecordType(RecordType.driverAlert);
        odeDriverAlertMetadata.setRecordGeneratedBy(GeneratedBy.OBU);
        odeDriverAlertMetadata.setLogFileName("driverAlert_OdeOutput.csv");

        odeDriverAlertMetadata.setOdeReceivedAt("2017-11-30T21:37:24.266Z[UTC]");

        String value = new String(Files.readAllBytes(Paths.get("src/test/resources/driverAlert_OdeOutput.json")));

        // Act
        OdeData odeDataTest = uut.processDriverAlertJson(value);

        OdeLogMetadata odeDriverAlertMetadataTest = (OdeLogMetadata) odeDataTest.getMetadata();
        OdeDriverAlertPayload odeDriverAlertPayloadTest = (OdeDriverAlertPayload) odeDataTest.getPayload();

        // Assert
        Assertions.assertNotNull(odeDriverAlertMetadataTest);
        Assertions.assertEquals(odeDriverAlertMetadata, odeDriverAlertMetadataTest);

        Assertions.assertNotNull(odeDriverAlertPayloadTest);
        Assertions.assertEquals("ICW", odeDriverAlertPayloadTest.getAlert());
    }

    @Test
    public void processDriverAlertJson_FAIL_Metadata() throws IOException {

        // Arrange
        String value = new String(Files.readAllBytes(Paths.get("src/test/resources/driverAlert_OdeOutput_NullMetadata.json")));

        // Act
        OdeData odeDataTest = uut.processDriverAlertJson(value);

        // Assert
        Assertions.assertNull(odeDataTest);
    }

    @Test
    public void processDriverAlertJson_FAIL_Payload() throws IOException {

        // Arrange
        String value = new String(Files.readAllBytes(Paths.get("src/test/resources/driverAlert_OdeOutput_NullPayload.json")));

        // Act
        OdeData odeDataTest = uut.processDriverAlertJson(value);

        // Assert
        Assertions.assertNull(odeDataTest);
    }
}