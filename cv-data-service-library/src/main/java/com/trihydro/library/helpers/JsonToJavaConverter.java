package com.trihydro.library.helpers;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.trihydro.library.model.ContentEnum;

import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.model.OdeLogMetadata;
import us.dot.its.jpo.ode.model.OdeMsgMetadata.GeneratedBy;
import us.dot.its.jpo.ode.model.OdeRequestMsgMetadata;
import us.dot.its.jpo.ode.model.OdeTimPayload;
import us.dot.its.jpo.ode.model.SerialId;
import us.dot.its.jpo.ode.plugin.RoadSideUnit.RSU;
import us.dot.its.jpo.ode.plugin.SNMP;
import us.dot.its.jpo.ode.plugin.ServiceRequest;
import us.dot.its.jpo.ode.plugin.SnmpProtocol;
import us.dot.its.jpo.ode.plugin.j2735.J2735SpecialVehicleExtensions;
import us.dot.its.jpo.ode.plugin.j2735.J2735SupplementalVehicleExtensions;
import us.dot.its.jpo.ode.plugin.j2735.J2735VehicleSafetyExtensions;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region.Circle;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.DistanceUnits.DistanceUnitsEnum;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.FrameType.TravelerInfoType;
import us.dot.its.jpo.ode.util.JsonUtils;

@Component
public class JsonToJavaConverter {

    private ObjectMapper mapper = new ObjectMapper();

    public JsonToJavaConverter() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public J2735SpecialVehicleExtensions convertJ2735SpecialVehicleExtensionsJsonToJava(String value, int i) {

        JsonNode part2Node = getPart2Node(value, i);
        J2735SpecialVehicleExtensions spve = null;
        try {
            spve = mapper.treeToValue(part2Node, J2735SpecialVehicleExtensions.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return spve;
    }

    public J2735SupplementalVehicleExtensions convertJ2735SupplementalVehicleExtensionsJsonToJava(String value, int i) {

        JsonNode part2Node = getPart2Node(value, i);
        J2735SupplementalVehicleExtensions suve = null;
        try {
            suve = mapper.treeToValue(part2Node, J2735SupplementalVehicleExtensions.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return suve;
    }

    public JsonNode getPart2Node(String value, int i) {
        JsonNode part2 = JsonUtils.getJsonNode(value, "payload").get("data").get("partII");
        if (part2 != null)
            return part2.get(i).get("value");
        return null;
    }

    public OdeLogMetadata convertTimMetadataJsonToJava(String value) {

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
            // System.out.println(metaDataNode);
            odeTimMetadata = mapper.treeToValue(metaDataNode, OdeLogMetadata.class);
        } catch (IOException e) {
            System.out.println("IOException");
            System.out.println(e.getStackTrace());
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }

        return odeTimMetadata;
    }

    public OdeRequestMsgMetadata convertBroadcastTimMetadataJsonToJava(String value) {

        OdeRequestMsgMetadata odeTimMetadata = null;

        try {
            JsonNode metaDataNode = JsonUtils.getJsonNode(value, "metadata");

            JsonNode rsusNode = metaDataNode.get("request").get("rsus");// J2735 Broadcast TIM should be array of
                                                                        // RoadSiteUnit.RSU, embedded in JSON with
                                                                        // {rsus} key

            String rsuTarget = null;
            int rsuIndex;
            if (rsusNode == null) {
                odeTimMetadata = mapper.treeToValue(metaDataNode, OdeRequestMsgMetadata.class);
            } else {
                odeTimMetadata = new OdeRequestMsgMetadata();
                ServiceRequest serviceRequest = new ServiceRequest();

                String timStartDateTime = "";
                if (metaDataNode.get("odeTimStartDateTime") != null) {
                    timStartDateTime = metaDataNode.get("odeTimStartDateTime").asText();
                }
                RSU rsuTemp = new RSU();
                rsuTemp.setSnmpProtocol(SnmpProtocol.NTCIP1218);
                var rsu = rsusNode.get("rsus");
                if (rsu != null) {
                    rsuTarget = rsu.get("rsuTarget").asText();
                    rsuIndex = rsu.get("rsuIndex").asInt();
                    rsuTemp.setRsuIndex(rsuIndex);
                    rsuTemp.setRsuTarget(rsuTarget);
                }

                RSU[] rsuArr = new RSU[1];
                rsuArr[0] = rsuTemp;
                serviceRequest.setRsus(rsuArr);

                JsonNode snmpNode = metaDataNode.get("request").get("snmp");

                SNMP snmp = mapper.treeToValue(snmpNode, SNMP.class);

                serviceRequest.setSnmp(snmp);
                odeTimMetadata.setRequest(serviceRequest);
                odeTimMetadata
                        .setRecordGeneratedBy(GeneratedBy.valueOf(metaDataNode.get("recordGeneratedBy").asText()));
                odeTimMetadata.setSchemaVersion(metaDataNode.get("schemaVersion").asInt());
                odeTimMetadata.setPayloadType(metaDataNode.get("payloadType").asText());

                JsonNode serialIdNode = metaDataNode.get("serialId");
                SerialId serialId = mapper.treeToValue(serialIdNode, SerialId.class);
                odeTimMetadata.setSerialId(serialId);

                odeTimMetadata.setSanitized(metaDataNode.get("sanitized").asBoolean());
                odeTimMetadata.setRecordGeneratedAt(metaDataNode.get("recordGeneratedAt").asText());
                odeTimMetadata.setOdeReceivedAt(metaDataNode.get("odeReceivedAt").asText());
                odeTimMetadata.setOdeTimStartDateTime(timStartDateTime);
            }

        } catch (IOException e) {
            System.out.println("IOException");
            System.out.println(e.getStackTrace());
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
        return odeTimMetadata;
    }

    private OdeTravelerInformationMessage.DataFrame.Region getRegion(JsonNode regionNode)
            throws JsonProcessingException {
        if (regionNode == null) {
            return null;
        }
        OdeTravelerInformationMessage.DataFrame.Region region = new OdeTravelerInformationMessage.DataFrame.Region();
        JsonNode anchorNode = null;
        JsonNode regionNameNode = null;
        JsonNode regionDirectionalityNode = null;
        JsonNode regionLaneWidthNode = null;
        JsonNode regionClosedPathNode = null;
        JsonNode regionDirectionNode = null;
        OdeTravelerInformationMessage.DataFrame.Region.Path path = null;
        OdeTravelerInformationMessage.DataFrame.Region.Geometry geometry = null;

        // get node values
        anchorNode = regionNode.get("anchor");
        regionNameNode = regionNode.get("name");
        regionDirectionalityNode = regionNode.get("directionality");
        regionLaneWidthNode = regionNode.get("laneWidth");
        regionClosedPathNode = regionNode.get("closedPath");
        regionDirectionNode = regionNode.get("direction");

        // anchor is an optional property, check for null
        if (anchorNode != null) {
            BigDecimal anchorLat = mapper.treeToValue(anchorNode.get("lat"), BigDecimal.class);
            BigDecimal anchorLong = mapper.treeToValue(anchorNode.get("long"), BigDecimal.class);
            // set region anchor
            OdePosition3D anchorPosition = new OdePosition3D();
            anchorPosition.setLatitude(anchorLat.multiply(new BigDecimal(".0000001")));
            anchorPosition.setLongitude(anchorLong.multiply(new BigDecimal(".0000001")));
            // TODO elevation

            region.setAnchorPosition(anchorPosition);
        }

        // name
        if (regionNameNode != null)
            region.setName(mapper.treeToValue(regionNameNode, String.class));

        // Directionality
        if (regionDirectionalityNode != null) {
            // J2735 7.31 DirectionOfUse
            JsonNode unavailable = regionDirectionalityNode.get("unavailable");// 0
            JsonNode forward = regionDirectionalityNode.get("forward");// 1
            JsonNode reverse = regionDirectionalityNode.get("reverse");// 2
            // JsonNode both = regionDirectionalityNode.get("both");// 3
            if (unavailable != null)
                region.setDirectionality("0");
            else if (forward != null)
                region.setDirectionality("1");
            else if (reverse != null)
                region.setDirectionality("2");
            else
                region.setDirectionality("3");
        }

        // lane width
        if (regionLaneWidthNode != null) {
            region.setLaneWidth(mapper.treeToValue(regionLaneWidthNode, BigDecimal.class));
        }

        // closed path
        if (regionClosedPathNode != null) {
            region.setClosedPath(regionClosedPathNode.get("true") != null);
        }

        if (regionDirectionNode != null) {
            region.setDirection(mapper.treeToValue(regionDirectionNode, String.class));
        }

        JsonNode descriptionNode = regionNode.get("description");
        if (descriptionNode != null) {
            path = GetPathData(descriptionNode.get("path"));
            geometry = GetGeometryData(descriptionNode.get("geometry"));

            if (path != null)
                region.setPath(path);
            else if (geometry != null)
                region.setGeometry(geometry);
        }

        return region;
    }

    public OdeTimPayload convertTimPayloadJsonToJava(String value) {

        OdeTimPayload odeTimPayload = null;

        try {
            OdeTravelerInformationMessage.DataFrame[] dataFrames = new OdeTravelerInformationMessage.DataFrame[1];
            OdeTravelerInformationMessage.DataFrame dataFrame = new OdeTravelerInformationMessage.DataFrame();
            List<OdeTravelerInformationMessage.DataFrame.Region> regions = new ArrayList<OdeTravelerInformationMessage.DataFrame.Region>();

            // JsonNode payloadNode = JsonUtils.getJsonNode(value, "payload");
            JsonNode timNode = JsonUtils.getJsonNode(value, "payload").get("data").get("MessageFrame").get("value")
                    .get("TravelerInformation");
            JsonNode travelerDataFrame = timNode.get("dataFrames").get("TravelerDataFrame");
            JsonNode regionsNode = travelerDataFrame.get("regions");

            JsonNode sequenceArrNode = null;
            JsonNode contentNode = travelerDataFrame.get("content");
            if (contentNode.has(ContentEnum.advisory.getStringValue())) {
                sequenceArrNode = contentNode.get(ContentEnum.advisory.getStringValue()).get("SEQUENCE");
                dataFrame.setContent(ContentEnum.advisory.getStringValue());
            } else if (contentNode.has(ContentEnum.speedLimit.getStringValue())) {
                sequenceArrNode = contentNode.get(ContentEnum.speedLimit.getStringValue()).get("SEQUENCE");
                dataFrame.setContent(ContentEnum.speedLimit.getStringValue());
            } else if (contentNode.has(ContentEnum.exitService.getStringValue())) {
                sequenceArrNode = contentNode.get(ContentEnum.exitService.getStringValue()).get("SEQUENCE");
                dataFrame.setContent(ContentEnum.exitService.getStringValue());
            } else if (contentNode.has(ContentEnum.genericSign.getStringValue())) {
                sequenceArrNode = contentNode.get(ContentEnum.genericSign.getStringValue()).get("SEQUENCE");
                dataFrame.setContent(ContentEnum.genericSign.getStringValue());
            } else if (contentNode.has(ContentEnum.workZone.getStringValue())) {
                sequenceArrNode = contentNode.get(ContentEnum.workZone.getStringValue()).get("SEQUENCE");
                dataFrame.setContent(ContentEnum.workZone.getStringValue());
            }

            LocalDate now = LocalDate.now();
            LocalDate firstDay = now.with(firstDayOfYear());
            OdeTravelerInformationMessage tim = new OdeTravelerInformationMessage();

            JsonNode timeStampNode = timNode.get("timeStamp");
            if (timeStampNode != null) {
                LocalDateTime timeStampDate = firstDay.atStartOfDay().plus(timeStampNode.asInt(), ChronoUnit.MINUTES);
                tim.setTimeStamp(timeStampDate.toString());
            }
            tim.setMsgCnt(timNode.get("msgCnt").asInt());

            JsonNode packetIDNode = timNode.get("packetID");
            if (packetIDNode != null) {
                tim.setPacketID(packetIDNode.asText());
            }

            // if ITIS codes are in an array
            List<String> itemsList = new ArrayList<String>();
            String item = null;
            if (sequenceArrNode != null && sequenceArrNode.isArray()) {
                for (final JsonNode objNode : sequenceArrNode) {
                    if (objNode.get("item").get("itis") != null)
                        item = mapper.treeToValue(objNode.get("item").get("itis"), String.class);
                    else if (objNode.get("item").get("text") != null)
                        item = mapper.treeToValue(objNode.get("item").get("text"), String.class);

                    itemsList.add(item);
                }
            }

            // ADD NON ARRAY ELEMENT
            if (sequenceArrNode != null && !sequenceArrNode.isArray()) {
                if (sequenceArrNode.get("item").get("itis") != null)
                    item = mapper.treeToValue(sequenceArrNode.get("item").get("itis"), String.class);
                else if (sequenceArrNode.get("item").get("text") != null)
                    item = mapper.treeToValue(sequenceArrNode.get("item").get("text"), String.class);

                itemsList.add(item);
            }

            String[] items = new String[itemsList.size()];
            items = itemsList.toArray(items);

            JsonNode geographicalPathNode = regionsNode.get("GeographicalPath");

            // geographicalPathNode may be an object or an array; if it is an object, treat
            // it as a region
            if (geographicalPathNode.isObject()) {
                // single region
                JsonNode regionNode = geographicalPathNode;
                Region region = getRegion(regionNode);
                regions.add(region);
            } else if (geographicalPathNode.isArray()) {
                // multiple regions
                for (final JsonNode regionNode : geographicalPathNode) {
                    Region region = getRegion(regionNode);
                    regions.add(region);
                }
            } else {
                System.out.println("warning: geographicalPathNode is not an object or an array");
            }

            dataFrame.setRegions(regions.toArray(new OdeTravelerInformationMessage.DataFrame.Region[regions.size()]));
            dataFrame.setItems(items);
            dataFrames[0] = dataFrame;
            tim.setDataframes(dataFrames);
            odeTimPayload = new OdeTimPayload();
            odeTimPayload.setData(tim);
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }

        return odeTimPayload;
    }

    public OdeTravelerInformationMessage.DataFrame.Region.Path GetPathData(JsonNode pathNode) {
        try {
            if (pathNode == null)
                return null;
            JsonNode xyNode = pathNode.get("offset").get("xy");
            Boolean isXy = true;

            // switching to using ll offset produces "ll" instead of "xy"
            if (xyNode == null) {
                xyNode = pathNode.get("offset").get("ll");
                isXy = false;
            }

            if (xyNode == null)
                return null;

            JsonNode nodesNode = xyNode.get("nodes");
            if (nodesNode == null)
                return null;

            JsonNode nodeXYArrNode = isXy ? nodesNode.get("NodeXY") : nodesNode.get("NodeLL");
            OdeTravelerInformationMessage.DataFrame.Region.Path path = new OdeTravelerInformationMessage.DataFrame.Region.Path();
            List<OdeTravelerInformationMessage.NodeXY> nodeXYs = new ArrayList<OdeTravelerInformationMessage.NodeXY>();
            OdeTravelerInformationMessage.NodeXY nodeXY = new OdeTravelerInformationMessage.NodeXY();

            if (nodeXYArrNode.isArray()) {
                for (final JsonNode objNode : nodeXYArrNode) {
                    nodeXY = new OdeTravelerInformationMessage.NodeXY();
                    var deltaObj = objNode.get("delta");
                    var firstObj = deltaObj.fields().next();
                    JsonNode nodeLatLon = firstObj.getValue();// objNode.get("delta").get("node-LatLon");
                    if (nodeLatLon != null) {
                        BigDecimal lat = mapper.treeToValue(nodeLatLon.get("lat"), BigDecimal.class);
                        BigDecimal lon = mapper.treeToValue(nodeLatLon.get("lon"), BigDecimal.class);
                        nodeXY.setNodeLat(lat.multiply(new BigDecimal(".0000001")));
                        nodeXY.setNodeLong(lon.multiply(new BigDecimal(".0000001")));
                        nodeXY.setDelta(firstObj.getKey());
                        nodeXYs.add(nodeXY);
                    }
                }
            }

            OdeTravelerInformationMessage.NodeXY[] nodeXYArr = new OdeTravelerInformationMessage.NodeXY[nodeXYs.size()];
            nodeXYArr = nodeXYs.toArray(nodeXYArr);

            path.setNodes(nodeXYArr);
            return path;
        } catch (Exception ex) {
            return null;
        }
    }

    public OdeTravelerInformationMessage.DataFrame.Region.Geometry GetGeometryData(JsonNode geometryNode) {
        try {
            if (geometryNode == null)
                return null;

            OdeTravelerInformationMessage.DataFrame.Region.Geometry geometry = new OdeTravelerInformationMessage.DataFrame.Region.Geometry();
            String direction = mapper.treeToValue(geometryNode.get("direction"), String.class);
            Integer extent = mapper.treeToValue(geometryNode.get("extent"), Integer.class);// optional
            BigDecimal laneWidth = mapper.treeToValue(geometryNode.get("laneWidth"), BigDecimal.class);// optional
            JsonNode circleNode = geometryNode.get("circle");
            JsonNode circleCenterNode = circleNode.get("center");
            Integer circleRadius = mapper.treeToValue(circleNode.get("radius"), Integer.class);

            Circle circle = new Circle();

            // circle.setCenter(OdePosition3D);
            BigDecimal latitude = mapper.treeToValue(circleCenterNode.get("lat"), BigDecimal.class);
            BigDecimal longitude = mapper.treeToValue(circleCenterNode.get("long"), BigDecimal.class);
            BigDecimal elevation = mapper.treeToValue(circleCenterNode.get("elevation"), BigDecimal.class);
            OdePosition3D center = new OdePosition3D();
            center.setLatitude(latitude);
            center.setLongitude(longitude);
            if (elevation != null)
                center.setElevation(elevation);
            circle.setCenter(center);
            circle.setRadius(circleRadius);

            DistanceUnitsEnum units = mapper.treeToValue(circleNode.get("units"), DistanceUnitsEnum.class);
            circle.setUnits(units);

            geometry.setDirection(direction);
            if (extent != null)
                geometry.setExtent(extent);
            if (laneWidth != null)
                geometry.setLaneWidth(laneWidth);

            geometry.setCircle(circle);
            return geometry;
        } catch (Exception e) {
            return null;
        }
    }

    public OdeTimPayload convertTmcTimTopicJsonToJava(String value) {

        OdeTimPayload odeTimPayload = null;

        try {
            OdeTravelerInformationMessage.DataFrame[] dataFrames = new OdeTravelerInformationMessage.DataFrame[1];
            OdeTravelerInformationMessage.DataFrame dataFrame = new OdeTravelerInformationMessage.DataFrame();
            List<OdeTravelerInformationMessage.DataFrame.Region> regions = new ArrayList<OdeTravelerInformationMessage.DataFrame.Region>();

            // JsonNode timNode = JsonUtils.getJsonNode(value,
            // "payload").get("data").get("MessageFrame").get("value")
            // .get("TravelerInformation");
            JsonNode timNode = JsonUtils.getJsonNode(value, "payload").findValue("TravelerInformation");
            JsonNode travelerDataFrame = timNode.findValue("TravelerDataFrame");

            JsonNode sequenceArrNode = null;
            JsonNode contentNode = travelerDataFrame.get("content");
            if (contentNode.has(ContentEnum.advisory.getStringValue())) {
                sequenceArrNode = contentNode.get(ContentEnum.advisory.getStringValue()).get("SEQUENCE");
                dataFrame.setContent(ContentEnum.advisory.getStringValue());
            } else if (contentNode.has(ContentEnum.speedLimit.getStringValue())) {
                sequenceArrNode = contentNode.get(ContentEnum.speedLimit.getStringValue()).get("SEQUENCE");
                dataFrame.setContent(ContentEnum.speedLimit.getStringValue());
            } else if (contentNode.has(ContentEnum.exitService.getStringValue())) {
                sequenceArrNode = contentNode.get(ContentEnum.exitService.getStringValue()).get("SEQUENCE");
                dataFrame.setContent(ContentEnum.exitService.getStringValue());
            } else if (contentNode.has(ContentEnum.genericSign.getStringValue())) {
                sequenceArrNode = contentNode.get(ContentEnum.genericSign.getStringValue()).get("SEQUENCE");
                dataFrame.setContent(ContentEnum.genericSign.getStringValue());
            } else if (contentNode.has(ContentEnum.workZone.getStringValue())) {
                sequenceArrNode = contentNode.get(ContentEnum.workZone.getStringValue()).get("SEQUENCE");
                dataFrame.setContent(ContentEnum.workZone.getStringValue());
            }

            List<String> itemsList = new ArrayList<String>();
            String item = null;
            if (sequenceArrNode != null && sequenceArrNode.isArray()) {
                for (final JsonNode objNode : sequenceArrNode) {
                    if (objNode.get("item").get("itis") != null)
                        item = mapper.treeToValue(objNode.get("item").get("itis"), String.class);
                    else if (objNode.get("item").get("text") != null)
                        item = mapper.treeToValue(objNode.get("item").get("text"), String.class);

                    itemsList.add(item);
                }
            }

            // ADD NON ARRAY ELEMENT
            if (sequenceArrNode != null && !sequenceArrNode.isArray()) {
                if (sequenceArrNode.get("item").get("itis") != null)
                    item = mapper.treeToValue(sequenceArrNode.get("item").get("itis"), String.class);
                else if (sequenceArrNode.get("item").get("text") != null)
                    item = mapper.treeToValue(sequenceArrNode.get("item").get("text"), String.class);

                itemsList.add(item);
            }

            // TravelerInfoType.valueOf();
            JsonNode frameTypeNode = travelerDataFrame.get("frameType");
            if (frameTypeNode != null) {
                if (frameTypeNode.fieldNames().hasNext()) {
                    TravelerInfoType frameType = TravelerInfoType.valueOf(frameTypeNode.fieldNames().next());
                    if (frameType != null) {
                        dataFrame.setFrameType(frameType);
                    }
                }
            }

            JsonNode startTimeNode = travelerDataFrame.get("startTime");
            JsonNode durationNode = travelerDataFrame.get("durationTime");
            JsonNode priorityNode = travelerDataFrame.get("priority");
            JsonNode doNotUse2Node = travelerDataFrame.get("doNotUse2");
            JsonNode doNotUse1Node = travelerDataFrame.get("doNotUse1");

            LocalDate now = LocalDate.now();
            LocalDate firstDay = now.with(firstDayOfYear());

            OdeTravelerInformationMessage tim = new OdeTravelerInformationMessage();

            JsonNode timeStampNode = timNode.get("timeStamp");
            if (timeStampNode != null) {
                LocalDateTime timeStampDate = firstDay.atStartOfDay().plus(timeStampNode.asInt(), ChronoUnit.MINUTES);
                tim.setTimeStamp(timeStampDate.toString() + "Z");
            }

            LocalDateTime startDate = firstDay.atStartOfDay().plus(startTimeNode.asInt(), ChronoUnit.MINUTES);

            dataFrame.setStartDateTime(startDate.toString() + "Z");
            dataFrame.setDurationTime(durationNode.asInt());
            dataFrame.setPriority(priorityNode.asInt());
            dataFrame.setDoNotUse2((short) doNotUse2Node.asInt());
            dataFrame.setDoNotUse1((short) doNotUse1Node.asInt());

            tim.setMsgCnt(timNode.get("msgCnt").asInt());

            JsonNode packetIDNode = timNode.get("packetID");
            if (packetIDNode != null) {
                tim.setPacketID(packetIDNode.asText());
            }

            String[] items = new String[itemsList.size()];
            items = itemsList.toArray(items);

            JsonNode geographicalPathNode = travelerDataFrame.findValue("GeographicalPath");

            // geographicalPathNode may be an object or an array; if it is an object, treat
            // it as a region
            if (geographicalPathNode.isObject()) {
                // single region
                JsonNode regionNode = geographicalPathNode;
                Region region = getRegion(regionNode);
                regions.add(region);
            } else if (geographicalPathNode.isArray()) {
                // multiple regions
                for (final JsonNode regionNode : geographicalPathNode) {
                    Region region = getRegion(regionNode);
                    regions.add(region);
                }
            } else {
                System.out.println("warning: geographicalPathNode is not an object or an array");
            }

            dataFrame.setRegions(regions.toArray(new OdeTravelerInformationMessage.DataFrame.Region[regions.size()]));
            dataFrame.setItems(items);
            dataFrames[0] = dataFrame;
            tim.setDataframes(dataFrames);
            odeTimPayload = new OdeTimPayload();
            odeTimPayload.setData(tim);
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }

        return odeTimPayload;
    }

    public OdeTravelerInformationMessage convertBroadcastTimPayloadJsonToJava(String value) {

        OdeTravelerInformationMessage odeTim = null;

        try {
            JsonNode timNode = JsonUtils.getJsonNode(value, "payload").get("data");
            odeTim = mapper.treeToValue(timNode, OdeTravelerInformationMessage.class);
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }

        return odeTim;
    }

}