package com.trihydro.loggerkafkaconsumer.app.dataConverters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.trihydro.library.helpers.JsonToJavaConverter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;

import us.dot.its.jpo.ode.model.OdeBsmMetadata;
import us.dot.its.jpo.ode.model.OdeBsmPayload;
import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeLogMetadata.SecurityResultCode;
import us.dot.its.jpo.ode.model.OdeMsgMetadata.GeneratedBy;
import us.dot.its.jpo.ode.model.SerialId;
import us.dot.its.jpo.ode.plugin.j2735.J2735TransmissionState;

@RunWith(StrictStubs.class)
public class BsmDataConverterTest {

    @Spy
    private JsonToJavaConverter jsonToJava = new JsonToJavaConverter();

    @InjectMocks
    private BsmDataConverter uut;

    @Test
    public void processBsmJson() throws IOException {
        // Arrange
        // create test objects
        SerialId serialId;

        OdeBsmMetadata odeBsmMetadata = new OdeBsmMetadata();
        odeBsmMetadata.setRecordGeneratedBy(GeneratedBy.OBU);

        odeBsmMetadata.setSchemaVersion(3);
        odeBsmMetadata.setSecurityResultCode(SecurityResultCode.unknown);
        odeBsmMetadata.setPayloadType("us.dot.its.jpo.ode.model.OdeBsmPayload");

        serialId = new SerialId("c8babc6e-ec8a-4232-a151-1dcd27c323ef", 1, 4070, 2, 0);
        odeBsmMetadata.setSerialId(serialId);

        odeBsmMetadata.setSanitized(false);
        odeBsmMetadata.setRecordGeneratedAt("2017-09-08T14:51:19.294Z[UTC]");

        odeBsmMetadata.setRecordType(RecordType.bsmLogDuringEvent);
        odeBsmMetadata.setLogFileName("bsmLogDuringEvent_OdeOutput.json");

        odeBsmMetadata.setOdeReceivedAt("2017-11-22T18:37:29.31Z[UTC]");

        String value = new String(Files.readAllBytes(Paths.get("src/test/resources/bsmLogDuringEvent_OdeOutput.json")));

        // Act
        OdeData odeDataTest = uut.processBsmJson(value);

        OdeBsmMetadata odeBsmMetadataTest = (OdeBsmMetadata) odeDataTest.getMetadata();
        OdeBsmPayload odeBsmPayloadTest = (OdeBsmPayload) odeDataTest.getPayload();

        // Assert
        assertNotNull(odeBsmMetadataTest);
        assertEquals(odeBsmMetadata, odeBsmMetadataTest);

        assertNotNull(odeBsmPayloadTest);
        assertEquals(Integer.valueOf(11), odeBsmPayloadTest.getBsm().getCoreData().getMsgCnt());
        assertEquals("738B0000", odeBsmPayloadTest.getBsm().getCoreData().getId());
        assertEquals(Integer.valueOf(19400), odeBsmPayloadTest.getBsm().getCoreData().getSecMark());
        assertEquals("40.4740003", odeBsmPayloadTest.getBsm().getCoreData().getPosition().getLatitude().toString());
        assertEquals("-104.9691846", odeBsmPayloadTest.getBsm().getCoreData().getPosition().getLongitude().toString());
        assertEquals(new BigDecimal(1489), odeBsmPayloadTest.getBsm().getCoreData().getPosition().getElevation());
        assertEquals(new BigDecimal(0), odeBsmPayloadTest.getBsm().getCoreData().getAccelSet().getAccelYaw());
        assertEquals("12.7", odeBsmPayloadTest.getBsm().getCoreData().getAccuracy().getSemiMajor().toString());
        assertEquals("12.7", odeBsmPayloadTest.getBsm().getCoreData().getAccuracy().getSemiMinor().toString());
        assertEquals(J2735TransmissionState.NEUTRAL, odeBsmPayloadTest.getBsm().getCoreData().getTransmission());
        assertEquals("0.1", odeBsmPayloadTest.getBsm().getCoreData().getSpeed().toString());
        assertEquals("19.9125", odeBsmPayloadTest.getBsm().getCoreData().getHeading().toString());
        assertEquals(false, odeBsmPayloadTest.getBsm().getCoreData().getBrakes().getWheelBrakes().get("leftFront"));
        assertEquals(false, odeBsmPayloadTest.getBsm().getCoreData().getBrakes().getWheelBrakes().get("rightFront"));
        assertEquals(true, odeBsmPayloadTest.getBsm().getCoreData().getBrakes().getWheelBrakes().get("unavailable"));
        assertEquals(false, odeBsmPayloadTest.getBsm().getCoreData().getBrakes().getWheelBrakes().get("leftRear"));
        assertEquals(false, odeBsmPayloadTest.getBsm().getCoreData().getBrakes().getWheelBrakes().get("rightRear"));
        assertEquals("unavailable", odeBsmPayloadTest.getBsm().getCoreData().getBrakes().getTraction());
        assertEquals("unavailable", odeBsmPayloadTest.getBsm().getCoreData().getBrakes().getAbs());
        assertEquals("unavailable", odeBsmPayloadTest.getBsm().getCoreData().getBrakes().getScs());
        assertEquals("unavailable", odeBsmPayloadTest.getBsm().getCoreData().getBrakes().getBrakeBoost());
        assertEquals("unavailable", odeBsmPayloadTest.getBsm().getCoreData().getBrakes().getAuxBrakes());
        assertEquals(2, odeBsmPayloadTest.getBsm().getPartII().size());
    }
}