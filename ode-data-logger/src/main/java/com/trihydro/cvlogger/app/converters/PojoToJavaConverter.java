package com.trihydro.cvlogger.app.converters;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;
import us.dot.its.jpo.ode.util.*;
import java.io.IOException;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import java.math.BigDecimal;

import us.dot.its.jpo.ode.model.OdeLogMetadata;
import us.dot.its.jpo.ode.model.OdeTimPayload;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class PojoToJavaConverter {

    static PreparedStatement preparedStatement = null;
    static Statement statement = null;
    static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static OdeLogMetadata convertTimMetadataJsonToJava(String value) {

        OdeLogMetadata odeTimMetadata = null;

        try {
            JsonNode metaDataNode = JsonUtils.getJsonNode(value, "metadata");
            JsonNode receivedMessageDetailsNode = metaDataNode.get("receivedMessageDetails");

            // check for null rxSource for Distress Notifications
            if (receivedMessageDetailsNode != null) {
                String rxSource = mapper.treeToValue(receivedMessageDetailsNode.get("rxSource"), String.class);
                if (rxSource.equals("")) {
                    ((ObjectNode) receivedMessageDetailsNode).remove("rxSource");
                    ((ObjectNode) metaDataNode).replace("receivedMessageDetails", receivedMessageDetailsNode);
                }
            }

            odeTimMetadata = mapper.treeToValue(metaDataNode, OdeLogMetadata.class);
        } catch (IOException e) {
            System.out.println("IOException");
            System.out.println(e.getStackTrace());
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }

        return odeTimMetadata;
    }

    public static OdeTimPayload convertTimPayloadJsonToJava(String value) {

        OdeTimPayload odeTimPayload = null;

        try {
            OdeTravelerInformationMessage.DataFrame[] dataFrames = new OdeTravelerInformationMessage.DataFrame[1];
            OdeTravelerInformationMessage.DataFrame dataFrame = new OdeTravelerInformationMessage.DataFrame();
            OdeTravelerInformationMessage.DataFrame.Region[] regions = new OdeTravelerInformationMessage.DataFrame.Region[1];
            OdeTravelerInformationMessage.DataFrame.Region region = new OdeTravelerInformationMessage.DataFrame.Region();
            OdeTravelerInformationMessage.DataFrame.Region.Path path = new OdeTravelerInformationMessage.DataFrame.Region.Path();

            List<OdeTravelerInformationMessage.NodeXY> nodeXYs = new ArrayList<OdeTravelerInformationMessage.NodeXY>();
            OdeTravelerInformationMessage.NodeXY nodeXY = new OdeTravelerInformationMessage.NodeXY();

            // JsonNode payloadNode = JsonUtils.getJsonNode(value, "payload");
            JsonNode timNode = JsonUtils.getJsonNode(value, "payload").get("data").get("MessageFrame").get("value")
                    .get("TravelerInformation");
            JsonNode geoPathNode = timNode.get("dataFrames").get("TravelerDataFrame").get("regions")
                    .get("GeographicalPath");
            JsonNode anchorNode = geoPathNode.get("anchor");
            JsonNode descripNode = geoPathNode.get("description");
            if (descripNode != null) {
                JsonNode pathNode = descripNode.get("path");// description is a CHOICE, path is just one option
                if (pathNode != null) {
                    JsonNode xyNode = pathNode.get("offset").get("xy");// offset is a CHOICE, xy is just one option
                    if (xyNode != null) {
                        JsonNode nodeXYArrNode = xyNode.get("nodes").get("NodeXY");
                        if (nodeXYArrNode.isArray()) {
                            for (final JsonNode objNode : nodeXYArrNode) {
                                nodeXY = new OdeTravelerInformationMessage.NodeXY();
                                BigDecimal lat = mapper.treeToValue(objNode.get("delta").get("node-LatLon").get("lat"),
                                        BigDecimal.class);
                                BigDecimal lon = mapper.treeToValue(objNode.get("delta").get("node-LatLon").get("lon"),
                                        BigDecimal.class);
                                nodeXY.setNodeLat(lat.multiply(new BigDecimal(.0000001)));
                                nodeXY.setNodeLong(lon.multiply(new BigDecimal(.0000001)));
                                nodeXY.setDelta("node-LatLon");
                                nodeXYs.add(nodeXY);
                            }
                        }
                    }
                }
            }

            timNode.get("timeStamp").asInt();

            LocalDate now = LocalDate.now();
            LocalDate firstDay = now.with(firstDayOfYear());
            LocalDateTime timeStampDate = firstDay.atStartOfDay().plus(timNode.get("timeStamp").asInt(),
                    ChronoUnit.MINUTES);
            OdeTravelerInformationMessage tim = new OdeTravelerInformationMessage();
            tim.setTimeStamp(timeStampDate.toString());
            tim.setMsgCnt(timNode.get("msgCnt").asInt());

            tim.setPacketID(timNode.get("packetID").asText());

            if (anchorNode != null) {
                BigDecimal anchorLat = mapper.treeToValue(anchorNode.get("lat"), BigDecimal.class);
                BigDecimal anchorLong = mapper.treeToValue(anchorNode.get("long"), BigDecimal.class);

                // set region anchor
                OdePosition3D anchorPosition = new OdePosition3D();
                anchorPosition.setLatitude(anchorLat.multiply(new BigDecimal(.0000001)));
                anchorPosition.setLongitude(anchorLong.multiply(new BigDecimal(.0000001)));
                // TODO elevation

                region.setAnchorPosition(anchorPosition);
            }

            OdeTravelerInformationMessage.NodeXY[] nodeXYArr = new OdeTravelerInformationMessage.NodeXY[nodeXYs.size()];
            nodeXYArr = nodeXYs.toArray(nodeXYArr);
            path.setNodes(nodeXYArr);
            region.setPath(path);

            regions[0] = region;
            dataFrame.setRegions(regions);
            dataFrames[0] = dataFrame;
            tim.setDataframes(dataFrames);
            odeTimPayload = new OdeTimPayload();
            odeTimPayload.setTim(tim);
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }

        return odeTimPayload;
    }
}