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
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.J2735VehicleSafetyExtensions;
import us.dot.its.jpo.ode.util.*;
import java.io.IOException;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import java.math.BigDecimal;

import us.dot.its.jpo.ode.model.OdeBsmMetadata;
import us.dot.its.jpo.ode.model.OdeBsmPayload;
import us.dot.its.jpo.ode.model.OdeDriverAlertPayload;
import us.dot.its.jpo.ode.model.OdeLogMetadataReceived;
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
        
    static{
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static OdeBsmMetadata convertBsmMetadataJsonToJava(String value){
    
        JsonNode metaDataNode = null;
        OdeBsmMetadata odeBsmMetadata = null;

        try {
            metaDataNode = JsonUtils.getJsonNode(value, "metadata");
            odeBsmMetadata = mapper.treeToValue(metaDataNode, OdeBsmMetadata.class);
        }
        catch(NullPointerException e){  
			System.out.println(e.getMessage());
        }
        catch (JsonProcessingException e) {
			e.printStackTrace();
        }
        
        return odeBsmMetadata;
    }

    public static OdeBsmPayload convertBsmPayloadJsonToJava(String value){
    
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
        }
        catch(NullPointerException e){  
			System.out.println(e.getMessage());
        }
        catch (JsonProcessingException e) {
			e.printStackTrace();
        }
        
        return odeBsmPayload;
    }

    public static J2735VehicleSafetyExtensions convertJ2735VehicleSafetyExtensionsJsonToJava(String value, int i){
     
        JsonNode part2Node = getPart2Node(value, i);	
        J2735VehicleSafetyExtensions vse = null;
        try {
            vse = mapper.treeToValue(part2Node, J2735VehicleSafetyExtensions.class);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return vse;
    }

    public static J2735SpecialVehicleExtensions convertJ2735SpecialVehicleExtensionsJsonToJava(String value, int i){      
      
        JsonNode part2Node = getPart2Node(value, i);	
        J2735SpecialVehicleExtensions spve = null;
        try {
            spve = mapper.treeToValue(part2Node, J2735SpecialVehicleExtensions.class);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return spve;
    }

    public static J2735SupplementalVehicleExtensions convertJ2735SupplementalVehicleExtensionsJsonToJava(String value, int i){

        JsonNode part2Node = getPart2Node(value, i);	
        J2735SupplementalVehicleExtensions suve = null;
        try {
            suve = mapper.treeToValue(part2Node, J2735SupplementalVehicleExtensions.class);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return suve;
    }

    public static JsonNode getPart2Node(String value, int i){        
        JsonNode part2Node = JsonUtils.getJsonNode(value, "payload").get("data").get("partII").get(i).get("value");
        return part2Node;
    }

    public static OdeLogMetadataReceived convertTimMetadataJsonToJava(String value){
        
        OdeLogMetadataReceived odeTimMetadata = null;
    
        try {       
            JsonNode metaDataNode = JsonUtils.getJsonNode(value, "metadata");	           
            JsonNode receivedMessageDetailsNode = JsonUtils.getJsonNode(value, "metadata").get("receivedMessageDetails");

            // check for null rxSource for Distress Notifications
            if(receivedMessageDetailsNode != null){
                String rxSource = mapper.treeToValue(receivedMessageDetailsNode.get("rxSource"), String.class);            
                if(rxSource.equals("")){			
                    ((ObjectNode)receivedMessageDetailsNode).remove("rxSource");			
                    ((ObjectNode)metaDataNode).replace("receivedMessageDetails", receivedMessageDetailsNode);								
                }
            }
       
            odeTimMetadata = mapper.treeToValue(metaDataNode, OdeLogMetadataReceived.class);	            
		}
		catch (IOException e) {
            System.out.println("IOException");
			System.out.println(e.getStackTrace());
		}
		catch(NullPointerException e){  
			System.out.println(e.getMessage());
        }
        
        return odeTimMetadata;
    }

    public static OdeTimPayload convertTimPayloadJsonToJava(String value){

        OdeTimPayload odeTimPayload = null;

        try {
            J2735TravelerInformationMessage.DataFrame[] dataFrames = new J2735TravelerInformationMessage.DataFrame[1];
            J2735TravelerInformationMessage.DataFrame dataFrame = new J2735TravelerInformationMessage.DataFrame();
            J2735TravelerInformationMessage.DataFrame.Region[] regions = new J2735TravelerInformationMessage.DataFrame.Region[1];
            J2735TravelerInformationMessage.DataFrame.Region region = new J2735TravelerInformationMessage.DataFrame.Region();
            J2735TravelerInformationMessage.DataFrame.Region.Path path = new J2735TravelerInformationMessage.DataFrame.Region.Path();

            //JsonNode payloadNode = JsonUtils.getJsonNode(value, "payload");
			JsonNode timNode = JsonUtils.getJsonNode(value, "payload").get("data").get("MessageFrame").get("value").get("TravelerInformation");
			JsonNode anchorNode = JsonUtils.getJsonNode(value, "payload").get("data").get("MessageFrame").get("value").get("TravelerInformation").get("dataFrames").get("TravelerDataFrame").get("regions").get("GeographicalPath").get("anchor");
			JsonNode nodeXYArrNode = JsonUtils.getJsonNode(value, "payload").get("data").get("MessageFrame").get("value").get("TravelerInformation").get("dataFrames").get("TravelerDataFrame").get("regions").get("GeographicalPath").get("description").get("path").get("offset").get("xy").get("nodes").get("NodeXY");					
            
            timNode.get("timeStamp").asInt();
            
            LocalDate now = LocalDate.now();
            LocalDate firstDay = now.with(firstDayOfYear());
            int timeStampInt = timNode.get("timeStamp").asInt();
            LocalDateTime timeStampDate = firstDay.atStartOfDay().plus(timNode.get("timeStamp").asInt(), ChronoUnit.MINUTES); 
            J2735TravelerInformationMessage tim = new J2735TravelerInformationMessage();
            tim.setTimeStamp(timeStampDate.toString());
            tim.setMsgCnt(timNode.get("msgCnt").asInt());     
            
            tim.setPacketID(timNode.get("packetID").asText());
            
			BigDecimal anchorLat = mapper.treeToValue(anchorNode.get("lat"), BigDecimal.class);
			BigDecimal anchorLong = mapper.treeToValue(anchorNode.get("long"), BigDecimal.class);
            
            List<J2735TravelerInformationMessage.NodeXY> nodeXYs = new ArrayList<J2735TravelerInformationMessage.NodeXY>();

            // set region anchor 
            OdePosition3D anchorPosition = new OdePosition3D();
            anchorPosition.setLatitude(anchorLat.multiply(new BigDecimal(.0000001)));
            anchorPosition.setLongitude(anchorLong.multiply(new BigDecimal(.0000001)));
            // TODO elevation

            region.setAnchorPosition(anchorPosition);

            J2735TravelerInformationMessage.NodeXY nodeXY = new J2735TravelerInformationMessage.NodeXY();			
            
			if (nodeXYArrNode.isArray()) {
				for (final JsonNode objNode : nodeXYArrNode) {					
					nodeXY = new J2735TravelerInformationMessage.NodeXY();					
					BigDecimal lat = mapper.treeToValue(objNode.get("delta").get("node-LatLon").get("lat"), BigDecimal.class);
					BigDecimal lon = mapper.treeToValue(objNode.get("delta").get("node-LatLon").get("lon"), BigDecimal.class);				
					nodeXY.setNodeLat(lat.multiply(new BigDecimal(.0000001)));
					nodeXY.setNodeLong(lon.multiply(new BigDecimal(.0000001)));
                    nodeXY.setDelta("node-LatLon");	
                    nodeXYs.add(nodeXY);		
				}
            }

            J2735TravelerInformationMessage.NodeXY[] nodeXYArr = new J2735TravelerInformationMessage.NodeXY[nodeXYs.size()];
            nodeXYArr = nodeXYs.toArray(nodeXYArr);
            
            path.setNodes(nodeXYArr);

            region.setPath(path);

            regions[0] = region;
            dataFrame.setRegions(regions);
            dataFrames[0] = dataFrame;
            tim.setDataframes(dataFrames);
            odeTimPayload = new OdeTimPayload();
            odeTimPayload.setTim(tim);
		}
		catch (IOException e) {
			System.out.println(e.getStackTrace());
		}
		catch(NullPointerException e){
			System.out.println(e.getMessage());
        }
        
        return odeTimPayload;
    }

    public static J2735TravelerInformationMessage convertBroadcastTimPayloadJsonToJava(String value){
        
        J2735TravelerInformationMessage odeTim = null;

        try {
            J2735TravelerInformationMessage.DataFrame[] dataFrames = new J2735TravelerInformationMessage.DataFrame[1];
            J2735TravelerInformationMessage.DataFrame dataFrame = new J2735TravelerInformationMessage.DataFrame();
            J2735TravelerInformationMessage.DataFrame.Region[] regions = new J2735TravelerInformationMessage.DataFrame.Region[1];
            J2735TravelerInformationMessage.DataFrame.Region region = new J2735TravelerInformationMessage.DataFrame.Region();
            J2735TravelerInformationMessage.DataFrame.Region.Path path = new J2735TravelerInformationMessage.DataFrame.Region.Path();

            //JsonNode payloadNode = JsonUtils.getJsonNode(value, "payload");
            JsonNode timNode = JsonUtils.getJsonNode(value, "payload").get("data");
            odeTim = mapper.treeToValue(timNode, J2735TravelerInformationMessage.class);	            
            
            //JsonNode anchorNode = JsonUtils.getJsonNode(value, "payload").get("data").get("dataframes").get("regions").get("anchorPosition");
            //JsonNode nodeXYArrNode = JsonUtils.getJsonNode(value, "payload").get("data").get("dataframes").get("regions").get("path").get("nodes");					
            
            // timNode.get("timeStamp").asInt();
            
            // LocalDate now = LocalDate.now();
            // LocalDate firstDay = now.with(firstDayOfYear());
            // int timeStampInt = timNode.get("timeStamp").asInt();
            // LocalDateTime timeStampDate = firstDay.atStartOfDay().plus(timNode.get("timeStamp").asInt(), ChronoUnit.MINUTES); 
            // J2735TravelerInformationMessage tim = new J2735TravelerInformationMessage();
            // tim.setTimeStamp(timeStampDate.toString());
            // tim.setMsgCnt(timNode.get("msgCnt").asInt());     
            
            // tim.setPacketID(timNode.get("packetID").asText());
            
            // // BigDecimal anchorLat = mapper.treeToValue(anchorNode.get("lat"), BigDecimal.class);
            // // BigDecimal anchorLong = mapper.treeToValue(anchorNode.get("long"), BigDecimal.class);
            
            // List<J2735TravelerInformationMessage.NodeXY> nodeXYs = new ArrayList<J2735TravelerInformationMessage.NodeXY>();

            // // set region anchor 
            // OdePosition3D anchorPosition = new OdePosition3D();
            // // anchorPosition.setLatitude(anchorLat.multiply(new BigDecimal(.0000001)));
            // // anchorPosition.setLongitude(anchorLong.multiply(new BigDecimal(.0000001)));
            // // TODO elevation

            // region.setAnchorPosition(anchorPosition);

            // J2735TravelerInformationMessage.NodeXY nodeXY = new J2735TravelerInformationMessage.NodeXY();			
            
            // // if (nodeXYArrNode.isArray()) {
            // //     for (final JsonNode objNode : nodeXYArrNode) {					
            // //         nodeXY = new J2735TravelerInformationMessage.NodeXY();					
            // //         BigDecimal lat = mapper.treeToValue(objNode.get("delta").get("node-LatLon").get("lat"), BigDecimal.class);
            // //         BigDecimal lon = mapper.treeToValue(objNode.get("delta").get("node-LatLon").get("lon"), BigDecimal.class);				
            // //         nodeXY.setNodeLat(lat.multiply(new BigDecimal(.0000001)));
            // //         nodeXY.setNodeLong(lon.multiply(new BigDecimal(.0000001)));
            // //         nodeXY.setDelta("node-LatLon");	
            // //         nodeXYs.add(nodeXY);		
            // //     }
            // // }

            // J2735TravelerInformationMessage.NodeXY[] nodeXYArr = new J2735TravelerInformationMessage.NodeXY[nodeXYs.size()];
            // nodeXYArr = nodeXYs.toArray(nodeXYArr);
            
            // path.setNodes(nodeXYArr);

            // region.setPath(path);

            // regions[0] = region;
            // dataFrame.setRegions(regions);
            // dataFrames[0] = dataFrame;
            // tim.setDataframes(dataFrames);
        }
        catch (IOException e) {
            System.out.println(e.getStackTrace());
        }
        catch(NullPointerException e){
            System.out.println(e.getMessage());
        }
        
        return odeTim;
    }

    public static OdeLogMetadataReceived convertDriverAlertMetadataJsonToJava(String value){    
        OdeLogMetadataReceived odeDriverAlertMetadata = null;
        JsonNode metaDataNode = JsonUtils.getJsonNode(value, "metadata");	
        try {
			odeDriverAlertMetadata = mapper.treeToValue(metaDataNode, OdeLogMetadataReceived.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
        }	
        catch(NullPointerException e){
			System.out.println(e.getMessage());
        }
        return odeDriverAlertMetadata;
    }

    public static OdeDriverAlertPayload convertDriverAlertPayloadJsonToJava(String value){
        
        OdeDriverAlertPayload odeDriverAlertPayload = null;        
        JsonNode alertNode = JsonUtils.getJsonNode(value, "payload").get("alert");

        try {
            String alert = mapper.treeToValue(alertNode, String.class);	
            odeDriverAlertPayload = new OdeDriverAlertPayload(alert);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
        }	
        catch(NullPointerException e){
			System.out.println(e.getMessage());
        }
        
        return odeDriverAlertPayload;
    }
}