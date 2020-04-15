package com.trihydro.library.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.JsonNode;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;

import us.dot.its.jpo.ode.model.OdeBsmMetadata;
import us.dot.its.jpo.ode.model.OdeBsmMetadata.BsmSource;
import us.dot.its.jpo.ode.model.OdeBsmPayload;
import us.dot.its.jpo.ode.model.OdeDriverAlertPayload;
import us.dot.its.jpo.ode.model.OdeLogMetadata;
import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeLogMetadata.SecurityResultCode;
import us.dot.its.jpo.ode.model.OdeLogMsgMetadataLocation;
import us.dot.its.jpo.ode.model.OdeMsgMetadata.GeneratedBy;
import us.dot.its.jpo.ode.model.OdeTimPayload;
import us.dot.its.jpo.ode.model.ReceivedMessageDetails;
import us.dot.its.jpo.ode.model.RxSource;
import us.dot.its.jpo.ode.model.SerialId;
import us.dot.its.jpo.ode.plugin.j2735.J2735SupplementalVehicleExtensions;
import us.dot.its.jpo.ode.plugin.j2735.J2735TransmissionState;
import us.dot.its.jpo.ode.plugin.j2735.J2735VehicleSafetyExtensions;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region.Circle;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.DistanceUnits.DistanceUnitsEnum;

/**
 * Unit tests for JSON to Java Object Converters.
 */
@RunWith(StrictStubs.class)
public class JsonToJavaConverterTest {

    private JsonToJavaConverter jsonToJava;

    @Before
    public void setup() {
        jsonToJava = new JsonToJavaConverter();
    }

    @Test
    public void TestConvertTimMetadataJsonToJava() throws IOException {

        // create test objects
        ReceivedMessageDetails receivedMessageDetails = new ReceivedMessageDetails();
        OdeLogMsgMetadataLocation locationData = new OdeLogMsgMetadataLocation();
        SerialId serialId;

        OdeLogMetadata odeTimMetadata = new OdeLogMetadata();
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

        OdeLogMetadata odeTimMetadataTest = jsonToJava.convertTimMetadataJsonToJava(value);

        assertNotNull(odeTimMetadataTest);
        assertEquals(odeTimMetadata, odeTimMetadataTest);
        assertEquals(odeTimMetadata.getSecurityResultCode(), odeTimMetadataTest.getSecurityResultCode());
    }

    @Test
    public void TestConvertTimMetadataNullException() throws IOException {
        OdeLogMetadata odeTimMetadataTest = jsonToJava.convertTimMetadataJsonToJava("");
        assertNull(odeTimMetadataTest);
    }

    @Test
    public void TestConvertTimPayloadJsonToJava_Path() throws IOException, URISyntaxException {

        // create test objects
        OdeTravelerInformationMessage tim = new OdeTravelerInformationMessage();

        OdeTimPayload odeTimPayload = new OdeTimPayload();

        OdeTravelerInformationMessage.DataFrame[] dataFrames = new OdeTravelerInformationMessage.DataFrame[1];
        OdeTravelerInformationMessage.DataFrame dataFrame = new OdeTravelerInformationMessage.DataFrame();
        OdeTravelerInformationMessage.DataFrame.Region[] regions = new OdeTravelerInformationMessage.DataFrame.Region[1];
        OdeTravelerInformationMessage.DataFrame.Region region = new OdeTravelerInformationMessage.DataFrame.Region();
        OdeTravelerInformationMessage.DataFrame.Region.Path path = new OdeTravelerInformationMessage.DataFrame.Region.Path();

        tim.setMsgCnt(0);
        tim.setPacketID("EC9C236B0000000000");
        tim.setTimeStamp("2017-10-11T21:32");

        OdePosition3D anchorPosition = new OdePosition3D();
        anchorPosition.setLatitude((new BigDecimal(263056840)).multiply(new BigDecimal(".0000001")));
        anchorPosition.setLongitude((new BigDecimal(-801481510)).multiply(new BigDecimal(".0000001")));
        // anchorPosition.setElevation(new BigDecimal(20));

        region.setAnchorPosition(anchorPosition);

        OdeTravelerInformationMessage.NodeXY nodeXY0 = new OdeTravelerInformationMessage.NodeXY();
        nodeXY0.setNodeLat((new BigDecimal("405744807")).multiply(new BigDecimal(".0000001")));
        nodeXY0.setNodeLong((new BigDecimal("-1050524251")).multiply(new BigDecimal(".0000001")));
        nodeXY0.setDelta("node-LatLon");

        OdeTravelerInformationMessage.NodeXY[] nodeXYArr = new OdeTravelerInformationMessage.NodeXY[2];
        nodeXYArr[0] = nodeXY0;

        OdeTravelerInformationMessage.NodeXY nodeXY1 = new OdeTravelerInformationMessage.NodeXY();
        nodeXY1.setNodeLat((new BigDecimal("405735393")).multiply(new BigDecimal(".0000001")));
        nodeXY1.setNodeLong((new BigDecimal("-1050500237")).multiply(new BigDecimal(".0000001")));
        nodeXY1.setDelta("node-LatLon");
        nodeXYArr[1] = nodeXY1;

        path.setNodes(nodeXYArr);
        region.setPath(path);
        regions[0] = region;
        dataFrame.setRegions(regions);
        dataFrames[0] = dataFrame;
        tim.setDataframes(dataFrames);

        odeTimPayload.setTim(tim);

        String value = new String(
                Files.readAllBytes(Paths.get(getClass().getResource("/rxMsg_TIM_OdeOutput.json").toURI())));
        OdeTimPayload odeTimPayloadTest = jsonToJava.convertTimPayloadJsonToJava(value);
        System.out.println("PACKETID: " + odeTimPayload.getTim().getPacketID());
        for (int i = 0; i < 2; i++) {
            assertEquals(odeTimPayload.getTim().getDataframes()[0].getRegions()[0].getPath().getNodes()[i].getNodeLat(),
                    odeTimPayloadTest.getTim().getDataframes()[0].getRegions()[0].getPath().getNodes()[i].getNodeLat());
            assertEquals(
                    odeTimPayload.getTim().getDataframes()[0].getRegions()[0].getPath().getNodes()[i].getNodeLong(),
                    odeTimPayloadTest.getTim().getDataframes()[0].getRegions()[0].getPath().getNodes()[i]
                            .getNodeLong());
            assertEquals(odeTimPayload.getTim().getDataframes()[0].getRegions()[0].getPath().getNodes()[i].getDelta(),
                    odeTimPayloadTest.getTim().getDataframes()[0].getRegions()[0].getPath().getNodes()[i].getDelta());
        }

        assertEquals(odeTimPayload.getTim().getDataframes()[0].getRegions()[0].getAnchorPosition(),
                odeTimPayloadTest.getTim().getDataframes()[0].getRegions()[0].getAnchorPosition());
        assertEquals(odeTimPayload.getTim().getMsgCnt(), odeTimPayloadTest.getTim().getMsgCnt());
        // assertEquals(odeTimPayload.getTim().getTimeStamp(),
        // odeTimPayloadTest.getTim().getTimeStamp());

        assertEquals(odeTimPayload.getTim().getPacketID(), odeTimPayloadTest.getTim().getPacketID());
        assertEquals(odeTimPayload.getTim().getUrlB(), odeTimPayloadTest.getTim().getUrlB());
    }

    @Test
    public void TestConvertTimPayloadJsonToJava_Geometry() throws IOException, URISyntaxException {

        // create test objects
        OdeTravelerInformationMessage tim = new OdeTravelerInformationMessage();

        OdeTimPayload odeTimPayload = new OdeTimPayload();

        OdeTravelerInformationMessage.DataFrame[] dataFrames = new OdeTravelerInformationMessage.DataFrame[1];
        OdeTravelerInformationMessage.DataFrame dataFrame = new OdeTravelerInformationMessage.DataFrame();
        OdeTravelerInformationMessage.DataFrame.Region[] regions = new OdeTravelerInformationMessage.DataFrame.Region[1];
        OdeTravelerInformationMessage.DataFrame.Region region = new OdeTravelerInformationMessage.DataFrame.Region();
        OdeTravelerInformationMessage.DataFrame.Region.Geometry geometry = new OdeTravelerInformationMessage.DataFrame.Region.Geometry();

        tim.setMsgCnt(0);
        tim.setPacketID("EC9C236B0000000000");
        tim.setTimeStamp("2017-10-11T21:32");

        OdePosition3D anchorPosition = new OdePosition3D();
        anchorPosition.setLatitude((new BigDecimal(263056840)).multiply(new BigDecimal(".0000001")));
        anchorPosition.setLongitude((new BigDecimal(-801481510)).multiply(new BigDecimal(".0000001")));
        // anchorPosition.setElevation(new BigDecimal(20));

        region.setAnchorPosition(anchorPosition);

        geometry.setDirection("1010101010101010");
        geometry.setExtent(1);// this is an enum
        geometry.setLaneWidth(new BigDecimal(33));

        Circle circle = new Circle();
        circle.setRadius(15);
        circle.setUnits(DistanceUnitsEnum.mile);
        OdePosition3D position = new OdePosition3D(new BigDecimal("41.678473"), new BigDecimal("-108.782775"),
                new BigDecimal("917.1432"));
        circle.setCenter(position);
        geometry.setCircle(circle);

        region.setGeometry(geometry);

        regions[0] = region;
        dataFrame.setRegions(regions);
        dataFrames[0] = dataFrame;
        tim.setDataframes(dataFrames);

        odeTimPayload.setTim(tim);

        String value = new String(
                Files.readAllBytes(Paths.get(getClass().getResource("/rxMsg_TIM_OdeOutput_Geometry.json").toURI())));
        OdeTimPayload odeTimPayloadTest = jsonToJava.convertTimPayloadJsonToJava(value);
        System.out.println("PACKETID: " + odeTimPayload.getTim().getPacketID());

        // test geometry properties
        // direction
        assertEquals(odeTimPayload.getTim().getDataframes()[0].getRegions()[0].getGeometry().getDirection(),
                odeTimPayloadTest.getTim().getDataframes()[0].getRegions()[0].getGeometry().getDirection());
        // extent
        assertEquals(odeTimPayload.getTim().getDataframes()[0].getRegions()[0].getGeometry().getExtent(),
                odeTimPayloadTest.getTim().getDataframes()[0].getRegions()[0].getGeometry().getExtent());
        // laneWidth
        assertEquals(odeTimPayload.getTim().getDataframes()[0].getRegions()[0].getGeometry().getLaneWidth(),
                odeTimPayloadTest.getTim().getDataframes()[0].getRegions()[0].getGeometry().getLaneWidth());
        // circle/radius
        assertEquals(odeTimPayload.getTim().getDataframes()[0].getRegions()[0].getGeometry().getCircle().getRadius(),
                odeTimPayloadTest.getTim().getDataframes()[0].getRegions()[0].getGeometry().getCircle().getRadius());
        // circle/units
        assertEquals(odeTimPayload.getTim().getDataframes()[0].getRegions()[0].getGeometry().getCircle().getUnits(),
                odeTimPayloadTest.getTim().getDataframes()[0].getRegions()[0].getGeometry().getCircle().getUnits());
        // circle/position/latitude
        assertEquals(
                odeTimPayload.getTim().getDataframes()[0].getRegions()[0].getGeometry().getCircle().getCenter()
                        .getLatitude(),
                odeTimPayloadTest.getTim().getDataframes()[0].getRegions()[0].getGeometry().getCircle().getCenter()
                        .getLatitude());
        // circle/position/longitude
        assertEquals(
                odeTimPayload.getTim().getDataframes()[0].getRegions()[0].getGeometry().getCircle().getCenter()
                        .getLongitude(),
                odeTimPayloadTest.getTim().getDataframes()[0].getRegions()[0].getGeometry().getCircle().getCenter()
                        .getLongitude());
        // circle/position/elevation
        assertEquals(
                odeTimPayload.getTim().getDataframes()[0].getRegions()[0].getGeometry().getCircle().getCenter()
                        .getElevation(),
                odeTimPayloadTest.getTim().getDataframes()[0].getRegions()[0].getGeometry().getCircle().getCenter()
                        .getElevation());

        assertEquals(odeTimPayload.getTim().getDataframes()[0].getRegions()[0].getAnchorPosition(),
                odeTimPayloadTest.getTim().getDataframes()[0].getRegions()[0].getAnchorPosition());
        assertEquals(odeTimPayload.getTim().getMsgCnt(), odeTimPayloadTest.getTim().getMsgCnt());
        // assertEquals(odeTimPayload.getTim().getTimeStamp(),
        // odeTimPayloadTest.getTim().getTimeStamp());

        assertEquals(odeTimPayload.getTim().getPacketID(), odeTimPayloadTest.getTim().getPacketID());
        assertEquals(odeTimPayload.getTim().getUrlB(), odeTimPayloadTest.getTim().getUrlB());
    }

    @Test
    public void convertBroadcastTimPayloadJsonToJava() throws IOException {

        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        System.out.println("Current relative path is: " + s);

        String value = new String(Files.readAllBytes(Paths.get("src/test/resources/broadcastTim_OdeOutput.json")));
        // String value = new
        // String(Files.readAllBytes(Paths.get("broadcastTim_OdeOutput.json")));
        OdeTravelerInformationMessage timTest = jsonToJava.convertBroadcastTimPayloadJsonToJava(value);

        assertEquals(1, timTest.getMsgCnt());
        assertEquals("2018-03-15T21:18:46.719-07:00", timTest.getTimeStamp());
        assertEquals("17e610000000000000", timTest.getPacketID());
        assertEquals("null", timTest.getUrlB());
        assertEquals("null", timTest.getUrlB());
    }

    @Test
    public void TestConvertTimPayloadNullException() throws IOException {
        OdeTimPayload odeTimPayload = jsonToJava.convertTimPayloadJsonToJava("");
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

        OdeBsmMetadata odeBsmMetadataTest = jsonToJava.convertBsmMetadataJsonToJava(value);

        assertNotNull(odeBsmMetadataTest);
        assertEquals(odeBsmMetadata, odeBsmMetadataTest);
        assertEquals(odeBsmMetadata.getSecurityResultCode(), odeBsmMetadataTest.getSecurityResultCode());
        assertEquals(odeBsmMetadata.getBsmSource(), odeBsmMetadataTest.getBsmSource());
    }

    @Test
    public void TestConvertBsmMetadataNullException() throws IOException {
        OdeBsmMetadata odeBsmMetadataTest = jsonToJava.convertBsmMetadataJsonToJava("");
        assertNull(odeBsmMetadataTest);
    }

    @Test
    public void TestConvertBsmPayloadJsonToJava() throws IOException {

        String value = new String(Files.readAllBytes(Paths.get("src/test/resources/bsmLogDuringEvent_OdeOutput.json")));

        OdeBsmPayload odeBsmPayloadTest = jsonToJava.convertBsmPayloadJsonToJava(value);

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

    @Test
    @Ignore
    public void TestConvertJ2735SpecialVehicleExtensionsJsonToJava() throws IOException {

    }

    @Test
    public void TestConvertJ2735VehicleSafetyExtensionsJsonToJava() throws IOException {
        String value = new String(Files.readAllBytes(Paths.get("src/test/resources/bsmLogDuringEvent_OdeOutput.json")));

        J2735VehicleSafetyExtensions vse = jsonToJava.convertJ2735VehicleSafetyExtensionsJsonToJava(value, 0);
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

        J2735SupplementalVehicleExtensions suve = jsonToJava.convertJ2735SupplementalVehicleExtensionsJsonToJava(value,
                1);
        assertEquals("unknownFuel", suve.getClassDetails().getFuelType().toString());
        assertEquals("none", suve.getClassDetails().getHpmsType().toString());
        assertEquals(new Integer(0), suve.getClassDetails().getKeyType());
        assertEquals("basicVehicle", suve.getClassDetails().getRole().toString());
    }

    @Test
    public void TestGetPart2Node() throws IOException {
        String value = new String(Files.readAllBytes(Paths.get("src/test/resources/bsmLogDuringEvent_OdeOutput.json")));
        String testNode = "{\"classDetails\":{\"fuelType\":\"unknownFuel\",\"hpmsType\":\"none\",\"keyType\":0,\"regional\":[],\"role\":\"basicVehicle\"},\"weatherProbe\":{},\"regional\":[]}";
        JsonNode part2Node = jsonToJava.getPart2Node(value, 1);
        assertEquals(testNode, part2Node.toString());
    }

    @Test
    public void TestConvertDriverAlertMetadataJsonToJava() throws IOException {

        // create test objects
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
        OdeLogMetadata odeDriverAlertMetadataTest = jsonToJava.convertDriverAlertMetadataJsonToJava(value);

        assertNotNull(odeDriverAlertMetadataTest);
        assertEquals(odeDriverAlertMetadata, odeDriverAlertMetadata);
        assertEquals(odeDriverAlertMetadata.getSecurityResultCode(), odeDriverAlertMetadata.getSecurityResultCode());
    }

    @Test
    public void TestConvertDriverAlertPayloadJsonToJava() throws IOException {

        String value = new String(Files.readAllBytes(Paths.get("src/test/resources/driverAlert_OdeOutput.json")));
        OdeDriverAlertPayload odeDriverAlertPayloadTest = jsonToJava.convertDriverAlertPayloadJsonToJava(value);

        assertNotNull(odeDriverAlertPayloadTest);
        assertEquals("ICW", odeDriverAlertPayloadTest.getAlert());
    }

}
