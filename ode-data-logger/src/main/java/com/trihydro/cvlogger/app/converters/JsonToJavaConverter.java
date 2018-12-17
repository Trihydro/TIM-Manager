package com.trihydro.cvlogger.app.converters;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.J2735Bsm;
import us.dot.its.jpo.ode.plugin.j2735.J2735BsmCoreData;
import us.dot.its.jpo.ode.plugin.j2735.J2735BsmPart2Content;
import us.dot.its.jpo.ode.plugin.j2735.J2735SpecialVehicleExtensions;
import us.dot.its.jpo.ode.plugin.j2735.J2735SupplementalVehicleExtensions;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.J2735VehicleSafetyExtensions;
import us.dot.its.jpo.ode.util.*;
import java.io.IOException;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import java.math.BigDecimal;

import us.dot.its.jpo.ode.model.OdeBsmMetadata;
import us.dot.its.jpo.ode.model.OdeBsmPayload;
import us.dot.its.jpo.ode.model.OdeDriverAlertPayload;
import us.dot.its.jpo.ode.model.OdeLogMetadata;
import us.dot.its.jpo.ode.model.OdeRequestMsgMetadata;
import us.dot.its.jpo.ode.model.OdeTimPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonToJavaConverter {

    static PreparedStatement preparedStatement = null;
    static Statement statement = null;
    static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static OdeBsmMetadata convertBsmMetadataJsonToJava(String value) {

        JsonNode metaDataNode = null;
        OdeBsmMetadata odeBsmMetadata = null;

        try {
            metaDataNode = JsonUtils.getJsonNode(value, "metadata");
            odeBsmMetadata = mapper.treeToValue(metaDataNode, OdeBsmMetadata.class);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return odeBsmMetadata;
    }

    public static OdeBsmPayload convertBsmPayloadJsonToJava(String value) {

        JsonNode bsmCoreDataNode = null;
        JsonNode part2Node = JsonUtils.getJsonNode(value, "payload").get("data").get("partII");
        OdeBsmPayload odeBsmPayload = null;
        J2735Bsm bsm = new J2735Bsm();
        List<J2735BsmPart2Content> partII = new ArrayList<J2735BsmPart2Content>();

        try {
            bsmCoreDataNode = JsonUtils.getJsonNode(value, "payload").get("data").get("coreData");
            part2Node = JsonUtils.getJsonNode(value, "payload").get("data").get("partII");
            J2735BsmCoreData bsmCoreData = mapper.treeToValue(bsmCoreDataNode, J2735BsmCoreData.class);
            J2735BsmPart2Content[] part2List = mapper.treeToValue(part2Node, J2735BsmPart2Content[].class);

            bsm.setPartII(Arrays.asList(part2List));

            bsm.setCoreData(bsmCoreData);
            odeBsmPayload = new OdeBsmPayload(bsm);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return odeBsmPayload;
    }

    public static J2735VehicleSafetyExtensions convertJ2735VehicleSafetyExtensionsJsonToJava(String value, int i) {

        JsonNode part2Node = getPart2Node(value, i);
        J2735VehicleSafetyExtensions vse = null;
        try {
            vse = mapper.treeToValue(part2Node, J2735VehicleSafetyExtensions.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return vse;
    }

    public static J2735SpecialVehicleExtensions convertJ2735SpecialVehicleExtensionsJsonToJava(String value, int i) {

        JsonNode part2Node = getPart2Node(value, i);
        J2735SpecialVehicleExtensions spve = null;
        try {
            spve = mapper.treeToValue(part2Node, J2735SpecialVehicleExtensions.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return spve;
    }

    public static J2735SupplementalVehicleExtensions convertJ2735SupplementalVehicleExtensionsJsonToJava(String value,
            int i) {

        JsonNode part2Node = getPart2Node(value, i);
        J2735SupplementalVehicleExtensions suve = null;
        try {
            suve = mapper.treeToValue(part2Node, J2735SupplementalVehicleExtensions.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return suve;
    }

    public static JsonNode getPart2Node(String value, int i) {
        JsonNode part2Node = JsonUtils.getJsonNode(value, "payload").get("data").get("partII").get(i).get("value");
        return part2Node;
    }

    public static OdeLogMetadata convertTimMetadataJsonToJava(String value) {

        OdeLogMetadata odeTimMetadata = null;

        try {
            JsonNode metaDataNode = JsonUtils.getJsonNode(value, "metadata");
            JsonNode receivedMessageDetailsNode = JsonUtils.getJsonNode(value, "metadata")
                    .get("receivedMessageDetails");

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

    public static OdeRequestMsgMetadata convertBroadcastTimMetadataJsonToJava(String value) {

        OdeRequestMsgMetadata odeTimMetadata = null;

        try {
            JsonNode metaDataNode = JsonUtils.getJsonNode(value, "metadata");
            // JsonNode receivedMessageDetailsNode = JsonUtils.getJsonNode(value, "metadata")
            //         .get("receivedMessageDetails");

            // // check for null rxSource for Distress Notifications
            // if (receivedMessageDetailsNode != null) {
            //     String rxSource = mapper.treeToValue(receivedMessageDetailsNode.get("rxSource"), String.class);
            //     if (rxSource.equals("")) {
            //         ((ObjectNode) receivedMessageDetailsNode).remove("rxSource");
            //         ((ObjectNode) metaDataNode).replace("receivedMessageDetails", receivedMessageDetailsNode);
            //     }
            // }

            odeTimMetadata = mapper.treeToValue(metaDataNode, OdeRequestMsgMetadata.class);
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

            // JsonNode payloadNode = JsonUtils.getJsonNode(value, "payload");
            JsonNode timNode = JsonUtils.getJsonNode(value, "payload").get("data").get("MessageFrame").get("value")
                    .get("TravelerInformation");
            JsonNode anchorNode = JsonUtils.getJsonNode(value, "payload").get("data").get("MessageFrame").get("value")
                    .get("TravelerInformation").get("dataFrames").get("TravelerDataFrame").get("regions")
                    .get("GeographicalPath").get("anchor");
            JsonNode nodeXYArrNode = JsonUtils.getJsonNode(value, "payload").get("data").get("MessageFrame")
                    .get("value").get("TravelerInformation").get("dataFrames").get("TravelerDataFrame").get("regions")
                    .get("GeographicalPath").get("description").get("path").get("offset").get("xy").get("nodes")
                    .get("NodeXY");
            JsonNode sequenceArrNode = JsonUtils.getJsonNode(value, "payload").get("data").get("MessageFrame")
                    .get("value").get("TravelerInformation").get("dataFrames").get("TravelerDataFrame").get("content")
                    .get("advisory").get("SEQUENCE");
            JsonNode regionNameNode = JsonUtils.getJsonNode(value, "payload").get("data").get("MessageFrame")
                    .get("value").get("TravelerInformation").get("dataFrames").get("TravelerDataFrame").get("regions")
                    .get("GeographicalPath").get("name");

            timNode.get("timeStamp").asInt();

            LocalDate now = LocalDate.now();
            LocalDate firstDay = now.with(firstDayOfYear());
            int timeStampInt = timNode.get("timeStamp").asInt();
            LocalDateTime timeStampDate = firstDay.atStartOfDay().plus(timNode.get("timeStamp").asInt(),
                    ChronoUnit.MINUTES);
            OdeTravelerInformationMessage tim = new OdeTravelerInformationMessage();
            tim.setTimeStamp(timeStampDate.toString());
            tim.setMsgCnt(timNode.get("msgCnt").asInt());

            tim.setPacketID(timNode.get("packetID").asText());

            BigDecimal anchorLat = mapper.treeToValue(anchorNode.get("lat"), BigDecimal.class);
            BigDecimal anchorLong = mapper.treeToValue(anchorNode.get("long"), BigDecimal.class);

            List<OdeTravelerInformationMessage.NodeXY> nodeXYs = new ArrayList<OdeTravelerInformationMessage.NodeXY>();

            // set region anchor
            OdePosition3D anchorPosition = new OdePosition3D();
            anchorPosition.setLatitude(anchorLat.multiply(new BigDecimal(.0000001)));
            anchorPosition.setLongitude(anchorLong.multiply(new BigDecimal(.0000001)));
            // TODO elevation

            region.setAnchorPosition(anchorPosition);

            OdeTravelerInformationMessage.NodeXY nodeXY = new OdeTravelerInformationMessage.NodeXY();

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

            OdeTravelerInformationMessage.NodeXY[] nodeXYArr = new OdeTravelerInformationMessage.NodeXY[nodeXYs.size()];
            nodeXYArr = nodeXYs.toArray(nodeXYArr);

            path.setNodes(nodeXYArr);

            if (regionNameNode != null)
                region.setName(mapper.treeToValue(regionNameNode, String.class));

            region.setPath(path);

            // if ITIS codes are in an array
            List<String> itemsList = new ArrayList<String>();
            String item = null;
            if (sequenceArrNode.isArray()) {
                for (final JsonNode objNode : sequenceArrNode) {
                    if (objNode.get("item").get("itis") != null)
                        item = mapper.treeToValue(objNode.get("item").get("itis"), String.class);
                    else if (objNode.get("item").get("text") != null)
                        item = mapper.treeToValue(objNode.get("item").get("text"), String.class);

                    itemsList.add(item);
                }
            }

            // ADD NON ARRAY ELEMENT
            if (!sequenceArrNode.isArray()) {
                if (sequenceArrNode.get("item").get("itis") != null)
                    item = mapper.treeToValue(sequenceArrNode.get("item").get("itis"), String.class);
                else if (sequenceArrNode.get("item").get("text") != null)
                    item = mapper.treeToValue(sequenceArrNode.get("item").get("text"), String.class);

                itemsList.add(item);
            }

            String[] items = new String[itemsList.size()];
            items = itemsList.toArray(items);

            regions[0] = region;
            dataFrame.setRegions(regions);
            dataFrame.setItems(items);
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

    public static OdeTravelerInformationMessage convertBroadcastTimPayloadJsonToJava(String value) {

        OdeTravelerInformationMessage odeTim = null;

        try {
            OdeTravelerInformationMessage.DataFrame[] dataFrames = new OdeTravelerInformationMessage.DataFrame[1];
            OdeTravelerInformationMessage.DataFrame dataFrame = new OdeTravelerInformationMessage.DataFrame();
            OdeTravelerInformationMessage.DataFrame.Region[] regions = new OdeTravelerInformationMessage.DataFrame.Region[1];
            OdeTravelerInformationMessage.DataFrame.Region region = new OdeTravelerInformationMessage.DataFrame.Region();
            OdeTravelerInformationMessage.DataFrame.Region.Path path = new OdeTravelerInformationMessage.DataFrame.Region.Path();

            // JsonNode payloadNode = JsonUtils.getJsonNode(value, "payload");
            JsonNode timNode = JsonUtils.getJsonNode(value, "payload").get("data");
            odeTim = mapper.treeToValue(timNode, OdeTravelerInformationMessage.class);

            // JsonNode anchorNode = JsonUtils.getJsonNode(value,
            // "payload").get("data").get("dataframes").get("regions").get("anchorPosition");
            // JsonNode nodeXYArrNode = JsonUtils.getJsonNode(value,
            // "payload").get("data").get("dataframes").get("regions").get("path").get("nodes");

            // timNode.get("timeStamp").asInt();

            // LocalDate now = LocalDate.now();
            // LocalDate firstDay = now.with(firstDayOfYear());
            // int timeStampInt = timNode.get("timeStamp").asInt();
            // LocalDateTime timeStampDate =
            // firstDay.atStartOfDay().plus(timNode.get("timeStamp").asInt(),
            // ChronoUnit.MINUTES);
            // J2735TravelerInformationMessage tim = new J2735TravelerInformationMessage();
            // tim.setTimeStamp(timeStampDate.toString());
            // tim.setMsgCnt(timNode.get("msgCnt").asInt());

            // tim.setPacketID(timNode.get("packetID").asText());

            // // BigDecimal anchorLat = mapper.treeToValue(anchorNode.get("lat"),
            // BigDecimal.class);
            // // BigDecimal anchorLong = mapper.treeToValue(anchorNode.get("long"),
            // BigDecimal.class);

            // List<J2735TravelerInformationMessage.NodeXY> nodeXYs = new
            // ArrayList<J2735TravelerInformationMessage.NodeXY>();

            // // set region anchor
            // OdePosition3D anchorPosition = new OdePosition3D();
            // // anchorPosition.setLatitude(anchorLat.multiply(new BigDecimal(.0000001)));
            // // anchorPosition.setLongitude(anchorLong.multiply(new
            // BigDecimal(.0000001)));
            // // TODO elevation

            // region.setAnchorPosition(anchorPosition);

            // J2735TravelerInformationMessage.NodeXY nodeXY = new
            // J2735TravelerInformationMessage.NodeXY();

            // // if (nodeXYArrNode.isArray()) {
            // // for (final JsonNode objNode : nodeXYArrNode) {
            // // nodeXY = new J2735TravelerInformationMessage.NodeXY();
            // // BigDecimal lat =
            // mapper.treeToValue(objNode.get("delta").get("node-LatLon").get("lat"),
            // BigDecimal.class);
            // // BigDecimal lon =
            // mapper.treeToValue(objNode.get("delta").get("node-LatLon").get("lon"),
            // BigDecimal.class);
            // // nodeXY.setNodeLat(lat.multiply(new BigDecimal(.0000001)));
            // // nodeXY.setNodeLong(lon.multiply(new BigDecimal(.0000001)));
            // // nodeXY.setDelta("node-LatLon");
            // // nodeXYs.add(nodeXY);
            // // }
            // // }

            // J2735TravelerInformationMessage.NodeXY[] nodeXYArr = new
            // J2735TravelerInformationMessage.NodeXY[nodeXYs.size()];
            // nodeXYArr = nodeXYs.toArray(nodeXYArr);

            // path.setNodes(nodeXYArr);

            // region.setPath(path);

            // regions[0] = region;
            // dataFrame.setRegions(regions);
            // dataFrames[0] = dataFrame;
            // tim.setDataframes(dataFrames);
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }

        return odeTim;
    }

    public static OdeLogMetadata convertDriverAlertMetadataJsonToJava(String value) {
        OdeLogMetadata odeDriverAlertMetadata = null;
        JsonNode metaDataNode = JsonUtils.getJsonNode(value, "metadata");
        try {
            odeDriverAlertMetadata = mapper.treeToValue(metaDataNode, OdeLogMetadata.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
        return odeDriverAlertMetadata;
    }

    public static OdeDriverAlertPayload convertDriverAlertPayloadJsonToJava(String value) {

        OdeDriverAlertPayload odeDriverAlertPayload = null;
        JsonNode alertNode = JsonUtils.getJsonNode(value, "payload").get("alert");

        try {
            String alert = mapper.treeToValue(alertNode, String.class);
            odeDriverAlertPayload = new OdeDriverAlertPayload(alert);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }

        return odeDriverAlertPayload;
    }
}