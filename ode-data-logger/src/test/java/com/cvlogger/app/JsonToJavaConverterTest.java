package com.cvlogger.app;

import com.cvlogger.app.converters.JsonToJavaConverter;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import us.dot.its.jpo.ode.model.OdeBsmMetadata;
import us.dot.its.jpo.ode.model.OdeBsmPayload;
import us.dot.its.jpo.ode.model.OdeDriverAlertMetadata;
import us.dot.its.jpo.ode.model.OdeDriverAlertPayload;
import us.dot.its.jpo.ode.model.OdeLogMsgMetadataLocation;
import us.dot.its.jpo.ode.model.OdeTimMetadata;
import us.dot.its.jpo.ode.model.OdeTimPayload;
import us.dot.its.jpo.ode.model.ReceivedMessageDetails;
import us.dot.its.jpo.ode.model.RxSource;
import us.dot.its.jpo.ode.model.SerialId;
import us.dot.its.jpo.ode.plugin.j2735.J2735SupplementalVehicleExtensions;
import us.dot.its.jpo.ode.plugin.j2735.J2735TransmissionState;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.J2735VehicleSafetyExtensions;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.model.OdeMsgMetadata.GeneratedBy;
import us.dot.its.jpo.ode.model.OdeLogMetadata.SecurityResultCode;
import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeBsmMetadata.BsmSource;

/**
 * Unit tests for JSON to Java Object Converters.
 */
public class JsonToJavaConverterTest {	

	@Test 
	public void TestConvertTimMetadataJsonToJava() throws IOException {
        
        // create test objects
        ReceivedMessageDetails receivedMessageDetails = new ReceivedMessageDetails();
        OdeLogMsgMetadataLocation locationData = new OdeLogMsgMetadataLocation();     
        SerialId serialId; 

        OdeTimMetadata odeTimMetadata = new OdeTimMetadata();
        odeTimMetadata.setRecordGeneratedBy(GeneratedBy.OBU);

        locationData.setElevation("1515");
        locationData.setHeading("0.0000");
        locationData.setLatitude("40.4739533");
        locationData.setLongitude("-104.9689995");
        locationData.setSpeed("0.14");

        receivedMessageDetails.setLocationData(locationData);
        receivedMessageDetails.setRxSource(RxSource.SAT);

        odeTimMetadata.setReceivedMessageDetails(receivedMessageDetails);
        odeTimMetadata.setSchemaVersion(3);
        odeTimMetadata.setSecurityResultCode(SecurityResultCode.unknown);
        odeTimMetadata.setPayloadType("us.dot.its.jpo.ode.model.OdeTimPayload");

        serialId = new SerialId("f212c298-4021-412a-b7c6-1fdb64a6a227", 1, 4, 2, 0);  
        odeTimMetadata.setSerialId(serialId);

        odeTimMetadata.setSanitized(false);
        odeTimMetadata.setRecordGeneratedAt("2017-09-05T20:23:39.194Z[UTC]");

        odeTimMetadata.setRecordType(RecordType.rxMsg);
        odeTimMetadata.setLogFileName("rxMsg_TIM.bin");

        odeTimMetadata.setOdeReceivedAt("2017-11-09T13:33:34.039Z[UTC]");


        String value = new String(Files.readAllBytes(Paths.get("src/test/resources/rxMsg_TIM_OdeOutput.json")));    
        
        OdeTimMetadata odeTimMetadataTest = JsonToJavaConverter.convertTimMetadataJsonToJava(value);

        assertNotNull(odeTimMetadataTest);
        assertEquals(odeTimMetadata, odeTimMetadataTest); 
        assertEquals(odeTimMetadata.getSecurityResultCode(), odeTimMetadataTest.getSecurityResultCode());  
    }
    
    @Test 
	public void TestConvertTimMetadataNullException() throws IOException {        
        OdeTimMetadata odeTimMetadataTest = JsonToJavaConverter.convertTimMetadataJsonToJava("");
        assertNull(odeTimMetadataTest);    
    }
    
    @Test @Ignore
	public void TestConvertTimPayloadJsonToJava() throws IOException {
        
        // create test objects
        J2735TravelerInformationMessage tim = new J2735TravelerInformationMessage();

        OdeTimPayload odeTimPayload = new OdeTimPayload();

        J2735TravelerInformationMessage.DataFrame[] dataFrames = new J2735TravelerInformationMessage.DataFrame[1];
        J2735TravelerInformationMessage.DataFrame dataFrame = new J2735TravelerInformationMessage.DataFrame();
        J2735TravelerInformationMessage.DataFrame.Region[] regions = new J2735TravelerInformationMessage.DataFrame.Region[1];
        J2735TravelerInformationMessage.DataFrame.Region region = new J2735TravelerInformationMessage.DataFrame.Region();
        J2735TravelerInformationMessage.DataFrame.Region.Path path = new J2735TravelerInformationMessage.DataFrame.Region.Path();

        tim.setMsgCnt(0);
        //tim.setPacketID(4364682555337299984384);
        tim.setTimeStamp("2017-10-11T21:32");

        OdePosition3D anchorPosition = new OdePosition3D();
        anchorPosition.setLatitude((new BigDecimal(263056840)).multiply(new BigDecimal(.0000001)));
        anchorPosition.setLongitude((new BigDecimal(-801481510)).multiply(new BigDecimal(.0000001)));
        //anchorPosition.setElevation(new BigDecimal(20));

        region.setAnchorPosition(anchorPosition);

        J2735TravelerInformationMessage.NodeXY nodeXY0 = new J2735TravelerInformationMessage.NodeXY();		
        nodeXY0.setNodeLat((new BigDecimal(405744807)).multiply(new BigDecimal(.0000001)));
        nodeXY0.setNodeLong((new BigDecimal(-1050524251)).multiply(new BigDecimal(.0000001)));
        nodeXY0.setDelta("node-LatLon");

        J2735TravelerInformationMessage.NodeXY[] nodeXYArr = new  J2735TravelerInformationMessage.NodeXY[2];
        nodeXYArr[0] = nodeXY0;

        J2735TravelerInformationMessage.NodeXY nodeXY1 = new J2735TravelerInformationMessage.NodeXY();		
        nodeXY1.setNodeLat((new BigDecimal(405735393)).multiply(new BigDecimal(.0000001)));
        nodeXY1.setNodeLong((new BigDecimal(-1050500237)).multiply(new BigDecimal(.0000001)));
        nodeXY1.setDelta("node-LatLon");
        nodeXYArr[1] = nodeXY1;

        path.setNodes(nodeXYArr);
        region.setPath(path);
        regions[0] = region;
        dataFrame.setRegions(regions);
        dataFrames[0] = dataFrame;
        tim.setDataframes(dataFrames);

        odeTimPayload.setTim(tim);

        String value = new String(Files.readAllBytes(Paths.get("src/test/resources/rxMsg_TIM_OdeOutput.json")));            
        OdeTimPayload odeTimPayloadTest = JsonToJavaConverter.convertTimPayloadJsonToJava(value);
        System.out.println("PACKETID: " + odeTimPayload.getTim().getPacketID());
        for (int i = 0; i < 2; i++){
            assertEquals(odeTimPayload.getTim().getDataframes()[0].getRegions()[0].getPath().getNodes()[i].getNodeLat(), odeTimPayloadTest.getTim().getDataframes()[0].getRegions()[0].getPath().getNodes()[i].getNodeLat());
            assertEquals(odeTimPayload.getTim().getDataframes()[0].getRegions()[0].getPath().getNodes()[i].getNodeLong(), odeTimPayloadTest.getTim().getDataframes()[0].getRegions()[0].getPath().getNodes()[i].getNodeLong());
            assertEquals(odeTimPayload.getTim().getDataframes()[0].getRegions()[0].getPath().getNodes()[i].getDelta(), odeTimPayloadTest.getTim().getDataframes()[0].getRegions()[0].getPath().getNodes()[i].getDelta());
        }

        assertEquals(odeTimPayload.getTim().getDataframes()[0].getRegions()[0].getAnchorPosition(), odeTimPayloadTest.getTim().getDataframes()[0].getRegions()[0].getAnchorPosition()); 
        assertEquals(odeTimPayload.getTim().getMsgCnt(), odeTimPayloadTest.getTim().getMsgCnt());
    //    assertEquals(odeTimPayload.getTim().getTimeStamp(), odeTimPayloadTest.getTim().getTimeStamp());
  
        assertEquals(odeTimPayload.getTim().getPacketID(), odeTimPayloadTest.getTim().getPacketID());
        assertEquals(odeTimPayload.getTim().getUrlB(), odeTimPayloadTest.getTim().getUrlB());     
    }

    @Test 
	public void TestConvertTimPayloadNullException() throws IOException {        
        OdeTimPayload odeTimPayload = JsonToJavaConverter.convertTimPayloadJsonToJava("");
        assertNull(odeTimPayload);    
    }

    @Test 
	public void TestConvertBsmMetadataJsonToJava() throws IOException {
        
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
        odeBsmMetadata.setBsmSource(BsmSource.RV);

        odeBsmMetadata.setOdeReceivedAt("2017-11-22T18:37:29.31Z[UTC]");

        String value = new String(Files.readAllBytes(Paths.get("src/test/resources/bsmLogDuringEvent_OdeOutput.json")));    
        
        OdeBsmMetadata odeBsmMetadataTest = JsonToJavaConverter.convertBsmMetadataJsonToJava(value);

        assertNotNull(odeBsmMetadataTest);
        assertEquals(odeBsmMetadata, odeBsmMetadataTest);
        assertEquals(odeBsmMetadata.getSecurityResultCode(), odeBsmMetadataTest.getSecurityResultCode());             
        assertEquals(odeBsmMetadata.getBsmSource(), odeBsmMetadataTest.getBsmSource());             
    }

    @Test 
	public void TestConvertBsmMetadataNullException() throws IOException {        
        OdeBsmMetadata odeBsmMetadataTest = JsonToJavaConverter.convertBsmMetadataJsonToJava("");
        assertNull(odeBsmMetadataTest);    
    }

    @Test
	public void TestConvertBsmPayloadJsonToJava() throws IOException {
        
        String value = new String(Files.readAllBytes(Paths.get("src/test/resources/bsmLogDuringEvent_OdeOutput.json")));    
        
        OdeBsmPayload odeBsmPayloadTest = JsonToJavaConverter.convertBsmPayloadJsonToJava(value);

        assertNotNull(odeBsmPayloadTest);
        assertEquals(new Integer(11), odeBsmPayloadTest.getBsm().getCoreData().getMsgCnt());        
        assertEquals("738B0000", odeBsmPayloadTest.getBsm().getCoreData().getId());        
        assertEquals(new Integer(19400), odeBsmPayloadTest.getBsm().getCoreData().getSecMark());        
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

    @Test @Ignore
	public void TestConvertJ2735SpecialVehicleExtensionsJsonToJava() throws IOException {
        
    }

    @Test 
	public void TestConvertJ2735VehicleSafetyExtensionsJsonToJava() throws IOException {
        String value = new String(Files.readAllBytes(Paths.get("src/test/resources/bsmLogDuringEvent_OdeOutput.json")));    

        J2735VehicleSafetyExtensions vse = JsonToJavaConverter.convertJ2735VehicleSafetyExtensionsJsonToJava(value, 0);
        assertEquals("-9.2", vse.getPathHistory().getCrumbData().get(0).getElevationOffset().toString());
        assertEquals("0.0000322", vse.getPathHistory().getCrumbData().get(0).getLatOffset().toString());
        assertEquals("0.0001445", vse.getPathHistory().getCrumbData().get(0).getLonOffset().toString());
        assertEquals("33.38", vse.getPathHistory().getCrumbData().get(0).getTimeOffset().toString());

        assertEquals("1", vse.getPathHistory().getCrumbData().get(1).getElevationOffset().toString());
        assertEquals("-0.0000097", vse.getPathHistory().getCrumbData().get(1).getLatOffset().toString());
        assertEquals("0.0000609", vse.getPathHistory().getCrumbData().get(1).getLonOffset().toString());
        assertEquals("225.6", vse.getPathHistory().getCrumbData().get(1).getTimeOffset().toString());
        
        assertEquals("50", vse.getPathPrediction().getConfidence().toString());
        assertEquals("0", vse.getPathPrediction().getRadiusOfCurve().toString());
    }

    @Test 
	public void TestJ2735SupplementalVehicleExtensionsJsonToJava() throws IOException {
        String value = new String(Files.readAllBytes(Paths.get("src/test/resources/bsmLogDuringEvent_OdeOutput.json")));    

        J2735SupplementalVehicleExtensions suve = JsonToJavaConverter.convertJ2735SupplementalVehicleExtensionsJsonToJava(value, 1);
        assertEquals("unknownFuel", suve.getClassDetails().getFuelType().toString());
        assertEquals("none", suve.getClassDetails().getHpmsType().toString());
        assertEquals(new Integer(0), suve.getClassDetails().getKeyType());
        assertEquals("basicVehicle", suve.getClassDetails().getRole().toString());
    }

    @Test 
	public void TestGetPart2Node() throws IOException {
        String value = new String(Files.readAllBytes(Paths.get("src/test/resources/bsmLogDuringEvent_OdeOutput.json")));    
        String testNode = "{\"classDetails\":{\"fuelType\":\"unknownFuel\",\"hpmsType\":\"none\",\"keyType\":0,\"regional\":[],\"role\":\"basicVehicle\"},\"weatherProbe\":{},\"regional\":[]}";
        JsonNode part2Node = JsonToJavaConverter.getPart2Node(value, 1);
        assertEquals(testNode, part2Node.toString());        
    }

    @Test 
    public void TestConvertDriverAlertMetadataJsonToJava() throws IOException {

        // create test objects
        ReceivedMessageDetails receivedMessageDetails = new ReceivedMessageDetails();
        OdeLogMsgMetadataLocation locationData = new OdeLogMsgMetadataLocation();     
        SerialId serialId; 

        OdeDriverAlertMetadata odeDriverAlertMetadata = new OdeDriverAlertMetadata();
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
        OdeDriverAlertMetadata odeDriverAlertMetadataTest = JsonToJavaConverter.convertDriverAlertMetadataJsonToJava(value);

        assertNotNull(odeDriverAlertMetadataTest);
        assertEquals(odeDriverAlertMetadata, odeDriverAlertMetadata);       
        assertEquals(odeDriverAlertMetadata.getSecurityResultCode(), odeDriverAlertMetadata.getSecurityResultCode());       
    }

    @Test 
    public void TestConvertDriverAlertPayloadJsonToJava() throws IOException {

        String value = new String(Files.readAllBytes(Paths.get("src/test/resources/driverAlert_OdeOutput.json"))); 
        OdeDriverAlertPayload odeDriverAlertPayloadTest = JsonToJavaConverter.convertDriverAlertPayloadJsonToJava(value);

        assertNotNull(odeDriverAlertPayloadTest);
        assertEquals("ICW", odeDriverAlertPayloadTest.getAlert());       
    }

}
