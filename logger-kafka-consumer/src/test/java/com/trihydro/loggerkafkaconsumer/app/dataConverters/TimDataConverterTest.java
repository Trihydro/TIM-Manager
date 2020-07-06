package com.trihydro.loggerkafkaconsumer.app.dataConverters;

import java.io.IOException;
import java.math.BigDecimal;
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
import us.dot.its.jpo.ode.model.OdeLogMetadata;
import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeLogMetadata.SecurityResultCode;
import us.dot.its.jpo.ode.model.OdeLogMsgMetadataLocation;
import us.dot.its.jpo.ode.model.OdeMsgMetadata.GeneratedBy;
import us.dot.its.jpo.ode.model.OdeTimPayload;
import us.dot.its.jpo.ode.model.ReceivedMessageDetails;
import us.dot.its.jpo.ode.model.RxSource;
import us.dot.its.jpo.ode.model.SerialId;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;

@ExtendWith(MockitoExtension.class)
public class TimDataConverterTest {

        @Spy
        private JsonToJavaConverter jsonToJava = new JsonToJavaConverter();

        @InjectMocks
        private TimDataConverter uut;

        @Test
        public void processTimJson() throws IOException {

                String value = new String(Files.readAllBytes(Paths.get("src/test/resources/rxMsg_TIM_OdeOutput.json")));

                // Arrange
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
                anchorPosition.setLatitude((new BigDecimal("263056840")).multiply(new BigDecimal(".0000001")));
                anchorPosition.setLongitude((new BigDecimal("-801481510")).multiply(new BigDecimal(".0000001")));
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

                // Act
                OdeData odeDataTest = uut.processTimJson(value);
                OdeLogMetadata odeTimMetadataTest = ((OdeLogMetadata) odeDataTest.getMetadata());
                OdeTimPayload odeTimPayloadTest = (OdeTimPayload) odeDataTest.getPayload();

                // Assert
                Assertions.assertNotNull(odeTimMetadataTest);
                Assertions.assertEquals(odeTimMetadata, odeTimMetadataTest);

                for (int i = 0; i < 2; i++) {
                        Assertions.assertEquals(
                                        odeTimPayload.getTim().getDataframes()[0].getRegions()[0].getPath()
                                                        .getNodes()[i].getNodeLat(),
                                        odeTimPayloadTest.getTim().getDataframes()[0].getRegions()[0].getPath()
                                                        .getNodes()[i].getNodeLat());
                        Assertions.assertEquals(
                                        odeTimPayload.getTim().getDataframes()[0].getRegions()[0].getPath()
                                                        .getNodes()[i].getNodeLong(),
                                        odeTimPayloadTest.getTim().getDataframes()[0].getRegions()[0].getPath()
                                                        .getNodes()[i].getNodeLong());
                        Assertions.assertEquals(
                                        odeTimPayload.getTim().getDataframes()[0].getRegions()[0].getPath()
                                                        .getNodes()[i].getDelta(),
                                        odeTimPayloadTest.getTim().getDataframes()[0].getRegions()[0].getPath()
                                                        .getNodes()[i].getDelta());
                }

                Assertions.assertEquals(odeTimPayload.getTim().getDataframes()[0].getRegions()[0].getAnchorPosition(),
                                odeTimPayloadTest.getTim().getDataframes()[0].getRegions()[0].getAnchorPosition());
                Assertions.assertEquals(odeTimPayload.getTim().getMsgCnt(), odeTimPayloadTest.getTim().getMsgCnt());
                Assertions.assertEquals(odeTimPayload.getTim().getPacketID(), odeTimPayloadTest.getTim().getPacketID());
                Assertions.assertEquals(odeTimPayload.getTim().getUrlB(), odeTimPayloadTest.getTim().getUrlB());
        }

        @Test
        public void processTimJson_FAIL_Metadata() throws IOException {
                // Arrange
                String value = new String(Files.readAllBytes(Paths.get("src/test/resources/rxMsg_TIM_OdeOutput_NullMetadata.json")));

                // Act
                OdeData odeDataTest = uut.processTimJson(value);

                // Assert
                Assertions.assertNull(odeDataTest);
        }

        @Test
        public void processTimJson_FAIL_Payload() throws IOException {
                // Arrange
                String value = new String(Files.readAllBytes(Paths.get("src/test/resources/rxMsg_TIM_OdeOutput_NullPayload.json")));

                // Act
                OdeData odeDataTest = uut.processTimJson(value);

                // Assert
                Assertions.assertNull(odeDataTest);
        }
}