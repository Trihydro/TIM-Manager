package com.trihydro.library.helpers;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.JsonNode;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import us.dot.its.jpo.ode.plugin.j2735.J2735VehicleSafetyExtensions;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region.Circle;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.DistanceUnits.DistanceUnitsEnum;

/**
 * Unit tests for JSON to Java Object Converters.
 */
public class JsonToJavaConverterTest {

    private JsonToJavaConverter jsonToJava;

    @BeforeEach
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

        Assertions.assertNotNull(odeTimMetadataTest);
        Assertions.assertEquals(odeTimMetadata, odeTimMetadataTest);
        Assertions.assertEquals(odeTimMetadata.getSecurityResultCode(), odeTimMetadataTest.getSecurityResultCode());
    }

    @Test
    public void TestConvertTimMetadataNullException() throws IOException {
        OdeLogMetadata odeTimMetadataTest = jsonToJava.convertTimMetadataJsonToJava("");
        Assertions.assertNull(odeTimMetadataTest);
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
        anchorPosition.setLatitude((BigDecimal.valueOf(263056840)).multiply(new BigDecimal(".0000001")));
        anchorPosition.setLongitude((BigDecimal.valueOf(-801481510)).multiply(new BigDecimal(".0000001")));
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

        odeTimPayload.setData(tim);

        String value = new String(
                Files.readAllBytes(Paths.get(getClass().getResource("/rxMsg_TIM_OdeOutput.json").toURI())));
        OdeTimPayload odeTimPayloadTest = jsonToJava.convertTimPayloadJsonToJava(value);
        System.out.println("PACKETID: " + getTim(odeTimPayload).getPacketID());
        for (int i = 0; i < 2; i++) {
            Assertions.assertEquals(
                    getTim(odeTimPayload).getDataframes()[0].getRegions()[0].getPath().getNodes()[i].getNodeLat(),
                    getTim(odeTimPayloadTest).getDataframes()[0].getRegions()[0].getPath().getNodes()[i].getNodeLat());
            Assertions.assertEquals(
                    getTim(odeTimPayload).getDataframes()[0].getRegions()[0].getPath().getNodes()[i].getNodeLong(),
                    getTim(odeTimPayloadTest).getDataframes()[0].getRegions()[0].getPath().getNodes()[i]
                            .getNodeLong());
            Assertions.assertEquals(
                    getTim(odeTimPayload).getDataframes()[0].getRegions()[0].getPath().getNodes()[i].getDelta(),
                    getTim(odeTimPayloadTest).getDataframes()[0].getRegions()[0].getPath().getNodes()[i].getDelta());
        }

        Assertions.assertEquals(getTim(odeTimPayload).getDataframes()[0].getRegions()[0].getAnchorPosition(),
                getTim(odeTimPayloadTest).getDataframes()[0].getRegions()[0].getAnchorPosition());
        Assertions.assertEquals(getTim(odeTimPayload).getMsgCnt(), getTim(odeTimPayloadTest).getMsgCnt());

        Assertions.assertEquals(getTim(odeTimPayload).getPacketID(), getTim(odeTimPayloadTest).getPacketID());
        Assertions.assertEquals(getTim(odeTimPayload).getUrlB(), getTim(odeTimPayloadTest).getUrlB());

        // verify number of regions = 1
        Assertions.assertEquals(1, getTim(odeTimPayloadTest).getDataframes()[0].getRegions().length);
    }

    @Test
    public void TestConvertTimPayloadJsonToJava_Path_MultipleRegions() throws IOException, URISyntaxException {

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
        anchorPosition.setLatitude((BigDecimal.valueOf(263056840)).multiply(new BigDecimal(".0000001")));
        anchorPosition.setLongitude((BigDecimal.valueOf(-801481510)).multiply(new BigDecimal(".0000001")));
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

        odeTimPayload.setData(tim);

        String value = new String(
                Files.readAllBytes(Paths.get(getClass().getResource("/rxMsg_TIM_OdeOutput_MultipleRegions.json").toURI())));
        OdeTimPayload odeTimPayloadTest = jsonToJava.convertTimPayloadJsonToJava(value);
        System.out.println("PACKETID: " + getTim(odeTimPayload).getPacketID());
        for (int i = 0; i < 2; i++) {
            Assertions.assertEquals(
                    getTim(odeTimPayload).getDataframes()[0].getRegions()[0].getPath().getNodes()[i].getNodeLat(),
                    getTim(odeTimPayloadTest).getDataframes()[0].getRegions()[0].getPath().getNodes()[i].getNodeLat());
            Assertions.assertEquals(
                    getTim(odeTimPayload).getDataframes()[0].getRegions()[0].getPath().getNodes()[i].getNodeLong(),
                    getTim(odeTimPayloadTest).getDataframes()[0].getRegions()[0].getPath().getNodes()[i]
                            .getNodeLong());
            Assertions.assertEquals(
                    getTim(odeTimPayload).getDataframes()[0].getRegions()[0].getPath().getNodes()[i].getDelta(),
                    getTim(odeTimPayloadTest).getDataframes()[0].getRegions()[0].getPath().getNodes()[i].getDelta());
        }

        Assertions.assertEquals(getTim(odeTimPayload).getDataframes()[0].getRegions()[0].getAnchorPosition(),
                getTim(odeTimPayloadTest).getDataframes()[0].getRegions()[0].getAnchorPosition());
        Assertions.assertEquals(getTim(odeTimPayload).getMsgCnt(), getTim(odeTimPayloadTest).getMsgCnt());

        Assertions.assertEquals(getTim(odeTimPayload).getPacketID(), getTim(odeTimPayloadTest).getPacketID());
        Assertions.assertEquals(getTim(odeTimPayload).getUrlB(), getTim(odeTimPayloadTest).getUrlB());

        // verify number of regions = 2
        Assertions.assertEquals(2, getTim(odeTimPayloadTest).getDataframes()[0].getRegions().length);
    }

    @Test
    public void TestConvertTimPayloadJsonToJava_SpeedLimit() throws IOException, URISyntaxException {
        // Arrange
        String value = new String(
                Files.readAllBytes(Paths.get(getClass().getResource("/rxMsg_TIM_SpeedLimit.json").toURI())));

        // Act
        OdeTimPayload odeTimPayloadTest = jsonToJava.convertTimPayloadJsonToJava(value);

        // Assert
        Assertions.assertNotNull(odeTimPayloadTest);
        Assertions.assertTrue(getTim(odeTimPayloadTest).getDataframes()[0].getItems().length > 0);
        Assertions.assertEquals("speedLimit", getTim(odeTimPayloadTest).getDataframes()[0].getContent());
        Assertions.assertArrayEquals(new String[] { "13609", "268", "12554", "8720" },
                getTim(odeTimPayloadTest).getDataframes()[0].getItems());
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
        anchorPosition.setLatitude((BigDecimal.valueOf(263056840)).multiply(new BigDecimal(".0000001")));
        anchorPosition.setLongitude((BigDecimal.valueOf(-801481510)).multiply(new BigDecimal(".0000001")));
        // anchorPosition.setElevation(new BigDecimal(20));

        region.setAnchorPosition(anchorPosition);

        geometry.setDirection("1010101010101010");
        geometry.setExtent(1);// this is an enum
        geometry.setLaneWidth(BigDecimal.valueOf(33));

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

        odeTimPayload.setData(tim);

        String value = new String(
                Files.readAllBytes(Paths.get(getClass().getResource("/rxMsg_TIM_OdeOutput_Geometry.json").toURI())));
        OdeTimPayload odeTimPayloadTest = jsonToJava.convertTimPayloadJsonToJava(value);
        System.out.println("PACKETID: " + getTim(odeTimPayload).getPacketID());

        // test geometry properties
        // direction
        Assertions.assertEquals(getTim(odeTimPayload).getDataframes()[0].getRegions()[0].getGeometry().getDirection(),
                getTim(odeTimPayloadTest).getDataframes()[0].getRegions()[0].getGeometry().getDirection());
        // extent
        Assertions.assertEquals(getTim(odeTimPayload).getDataframes()[0].getRegions()[0].getGeometry().getExtent(),
                getTim(odeTimPayloadTest).getDataframes()[0].getRegions()[0].getGeometry().getExtent());
        // laneWidth
        Assertions.assertEquals(getTim(odeTimPayload).getDataframes()[0].getRegions()[0].getGeometry().getLaneWidth(),
                getTim(odeTimPayloadTest).getDataframes()[0].getRegions()[0].getGeometry().getLaneWidth());
        // circle/radius
        Assertions.assertEquals(
                getTim(odeTimPayload).getDataframes()[0].getRegions()[0].getGeometry().getCircle().getRadius(),
                getTim(odeTimPayloadTest).getDataframes()[0].getRegions()[0].getGeometry().getCircle().getRadius());
        // circle/units
        Assertions.assertEquals(
                getTim(odeTimPayload).getDataframes()[0].getRegions()[0].getGeometry().getCircle().getUnits(),
                getTim(odeTimPayloadTest).getDataframes()[0].getRegions()[0].getGeometry().getCircle().getUnits());
        // circle/position/latitude
        Assertions.assertEquals(
                getTim(odeTimPayload).getDataframes()[0].getRegions()[0].getGeometry().getCircle().getCenter()
                        .getLatitude(),
                getTim(odeTimPayloadTest).getDataframes()[0].getRegions()[0].getGeometry().getCircle().getCenter()
                        .getLatitude());
        // circle/position/longitude
        Assertions.assertEquals(
                getTim(odeTimPayload).getDataframes()[0].getRegions()[0].getGeometry().getCircle().getCenter()
                        .getLongitude(),
                getTim(odeTimPayloadTest).getDataframes()[0].getRegions()[0].getGeometry().getCircle().getCenter()
                        .getLongitude());
        // circle/position/elevation
        Assertions.assertEquals(
                getTim(odeTimPayload).getDataframes()[0].getRegions()[0].getGeometry().getCircle().getCenter()
                        .getElevation(),
                getTim(odeTimPayloadTest).getDataframes()[0].getRegions()[0].getGeometry().getCircle().getCenter()
                        .getElevation());

        Assertions.assertEquals(getTim(odeTimPayload).getDataframes()[0].getRegions()[0].getAnchorPosition(),
                getTim(odeTimPayloadTest).getDataframes()[0].getRegions()[0].getAnchorPosition());
        Assertions.assertEquals(getTim(odeTimPayload).getMsgCnt(), getTim(odeTimPayloadTest).getMsgCnt());

        Assertions.assertEquals(getTim(odeTimPayload).getPacketID(), getTim(odeTimPayloadTest).getPacketID());
        Assertions.assertEquals(getTim(odeTimPayload).getUrlB(), getTim(odeTimPayloadTest).getUrlB());
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

        Assertions.assertEquals(1, timTest.getMsgCnt());
        Assertions.assertEquals("2018-03-15T21:18:46.719-07:00", timTest.getTimeStamp());
        Assertions.assertEquals("17e610000000000000", timTest.getPacketID());
        Assertions.assertEquals("null", timTest.getUrlB());
        Assertions.assertEquals("null", timTest.getUrlB());
    }

    @Test
    public void TestConvertTimPayloadNullException() throws IOException {
        OdeTimPayload odeTimPayload = jsonToJava.convertTimPayloadJsonToJava("");
        Assertions.assertNull(odeTimPayload);
    }

    @Test
    public void TestConvertTmcTimTopicJsonToJava_HandlesVslContentType() throws IOException {
        // Arrange
        String tim_vsl_json = new String(Files.readAllBytes(Paths.get("src/test/resources/tim_vsl.json")));

        // Act
        var tim_vsl = jsonToJava.convertTmcTimTopicJsonToJava(tim_vsl_json);

        // Assert
        Assertions.assertNotNull(tim_vsl);
        Assertions.assertEquals("speedLimit", getTim(tim_vsl).getDataframes()[0].getContent());
        Assertions.assertArrayEquals(new String[] { "268", "12604", "8720" },
                getTim(tim_vsl).getDataframes()[0].getItems());
        
        // verify number of regions = 1
        Assertions.assertEquals(1, getTim(tim_vsl).getDataframes()[0].getRegions().length);
    }

    @Test
    public void TestConvertTmcTimTopicJsonToJava_HandlesVslContentType_MultipleRegions() throws IOException {
        // Arrange
        String tim_vsl_json = new String(Files.readAllBytes(Paths.get("src/test/resources/tim_vsl_MultipleRegions.json")));

        // Act
        var tim_vsl = jsonToJava.convertTmcTimTopicJsonToJava(tim_vsl_json);

        // Assert
        Assertions.assertNotNull(tim_vsl);
        Assertions.assertEquals("speedLimit", getTim(tim_vsl).getDataframes()[0].getContent());
        Assertions.assertArrayEquals(new String[] { "268", "12604", "8720" },
                getTim(tim_vsl).getDataframes()[0].getItems());
        
        // verify number of regions = 2
        Assertions.assertEquals(2, getTim(tim_vsl).getDataframes()[0].getRegions().length);
    }

    @Test
    public void TestConvertTmcTimTopicJsonToJava_HandlesParkingContentType() throws IOException {
        // Arrange
        String tim_parking_json = new String(Files.readAllBytes(Paths.get("src/test/resources/tim_parking.json")));

        // Act
        var tim_parking = jsonToJava.convertTmcTimTopicJsonToJava(tim_parking_json);

        // Assert
        Assertions.assertNotNull(tim_parking);
        Assertions.assertEquals("exitService", getTim(tim_parking).getDataframes()[0].getContent());
        Assertions.assertArrayEquals(new String[] { "4104", "11794", "345" },
                getTim(tim_parking).getDataframes()[0].getItems());
        
        // verify number of regions = 1
        Assertions.assertEquals(1, getTim(tim_parking).getDataframes()[0].getRegions().length);
    }

    @Test
    public void TestConvertTmcTimTopicJsonToJava_HandlesParkingContentType_MultipleRegions() throws IOException {
        // Arrange
        String tim_parking_json = new String(Files.readAllBytes(Paths.get("src/test/resources/tim_parking_MultipleRegions.json")));

        // Act
        var tim_parking = jsonToJava.convertTmcTimTopicJsonToJava(tim_parking_json);

        // Assert
        Assertions.assertNotNull(tim_parking);
        Assertions.assertEquals("exitService", getTim(tim_parking).getDataframes()[0].getContent());
        Assertions.assertArrayEquals(new String[] { "4104", "11794", "345" },
                getTim(tim_parking).getDataframes()[0].getItems());
        
        // verify number of regions = 2
        Assertions.assertEquals(2, getTim(tim_parking).getDataframes()[0].getRegions().length);
    }

    @Test
    public void TestConvertTmcTimTopicJsonToJava_HandlesConstructionContentType() throws IOException {
        // Arrange
        String tim_construction_json = new String(
                Files.readAllBytes(Paths.get("src/test/resources/tim_construction.json")));

        // Act
        var tim_construction = jsonToJava.convertTmcTimTopicJsonToJava(tim_construction_json);

        // Assert
        Assertions.assertNotNull(tim_construction);
        Assertions.assertEquals("workZone", getTim(tim_construction).getDataframes()[0].getContent());
        Assertions.assertArrayEquals(new String[] { "1537", "12554", "8728" },
                getTim(tim_construction).getDataframes()[0].getItems());
        
        // verify number of regions = 1
        Assertions.assertEquals(1, getTim(tim_construction).getDataframes()[0].getRegions().length);
    }

    @Test
    public void TestConvertTmcTimTopicJsonToJava_HandlesConstructionContentType_MultipleRegions() throws IOException {
        // Arrange
        String tim_construction_json = new String(
                Files.readAllBytes(Paths.get("src/test/resources/tim_construction_MultipleRegions.json")));

        // Act
        var tim_construction = jsonToJava.convertTmcTimTopicJsonToJava(tim_construction_json);

        // Assert
        Assertions.assertNotNull(tim_construction);
        Assertions.assertEquals("workZone", getTim(tim_construction).getDataframes()[0].getContent());
        Assertions.assertArrayEquals(new String[] { "1537", "12554", "8728" },
                getTim(tim_construction).getDataframes()[0].getItems());
        
        // verify number of regions = 2
        Assertions.assertEquals(2, getTim(tim_construction).getDataframes()[0].getRegions().length);
    }


    /**
     * Helper method to get an OdeTravelerInformationMessage object given an OdeTimPayload.
     */
    private OdeTravelerInformationMessage getTim(OdeTimPayload odeTimPayload) {
        return (OdeTravelerInformationMessage) odeTimPayload.getData();
    }
}