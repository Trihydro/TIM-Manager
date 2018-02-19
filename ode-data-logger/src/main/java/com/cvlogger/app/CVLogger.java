package com.cvlogger.app;

import java.sql.*;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import us.dot.its.jpo.ode.plugin.j2735.J2735VehicleSafetyExtensions;
import us.dot.its.jpo.ode.plugin.j2735.J2735SpecialVehicleExtensions;
import us.dot.its.jpo.ode.plugin.j2735.J2735SupplementalVehicleExtensions;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import us.dot.its.jpo.ode.util.*;
import java.io.IOException;
import java.math.BigInteger;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;

import us.dot.its.jpo.ode.model.OdeBsmData;
import us.dot.its.jpo.ode.model.OdeBsmMetadata;
import us.dot.its.jpo.ode.model.OdeBsmPayload;
import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeTimMetadata;
import us.dot.its.jpo.ode.model.OdeTimPayload;
import us.dot.its.jpo.ode.model.OdeDriverAlertMetadata;
import us.dot.its.jpo.ode.model.OdeDriverAlertPayload;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trihydro.service.bsm.*;
import com.trihydro.service.tim.*;
import com.trihydro.service.driveralert.*;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import com.cvlogger.app.converters.JsonToJavaConverter;
import com.cvlogger.app.services.TracManager;

public class CVLogger {
	
	static PreparedStatement preparedStatement = null;
	static Statement statement = null;
	static ObjectMapper mapper;

	public static Connection makeJDBCConnection() {
		Connection connection = null;
 		System.out.println("attempting DB connection");	
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			System.out.println("Congrats - Seems your Oracle JDBC Driver Registered!");
		} catch (ClassNotFoundException e) {
			System.out.println("Sorry, couldn't find JDBC driver. Make sure you have added JDBC Maven Dependency Correctly");
			e.printStackTrace();
			return null;
		}
 
		try {
			// DriverManager: The basic service for managing a set of JDBC drivers.
			connection = DriverManager.getConnection("jdbc:oracle:thin:@10.145.9.22:1521/cvdev.gisits.local", "CVCOMMS", "C0ll1s10n");
			if (connection != null) {
				System.out.println("Connection Successful! Enjoy. Now it's time to push data");
			} else {
				System.out.println("Failed to make connection!");
			}
		} catch (SQLException e) {
			System.out.println("Oracle Connection Failed!");
			e.printStackTrace();
			return null;
		}
		return connection;
	}

	private static void submitDNMsgToTrac(String value, Connection connection) {
		
	
		OdeTimMetadata metadata = JsonToJavaConverter.convertTimMetadataJsonToJava(value);
		OdeTimPayload payload = JsonToJavaConverter.convertTimPayloadJsonToJava(value);		

		// check if packetId is in trac message sent table
		if(TracManager.isDnMsgInTrac(connection, payload.getTim().getPacketID()) != null ){
			// if so, return	
			return;
		}

		// if not, add to db and sent to trac

		RestTemplate restTemplate = new RestTemplate();
		String url = "http://gisd01:8070/trac/newtask.text";	
		
		String latitude = null;
		String longitude = null;	 
			
		
		latitude = metadata.getReceivedMessageDetails().getLocationData().getLatitude();
		longitude = metadata.getReceivedMessageDetails().getLocationData().getLongitude();			

		String descUrl = "<b>";
		descUrl += "Distress Notification Issued at ";		
		descUrl += "<a href='https://www.google.com/maps/place/" + latitude + "," + longitude + "'>" + latitude + "/" + longitude + "</a></b>";		
	
		// Query parameters
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
			.queryParam("priority", "1")
			.queryParam("description", descUrl)
			.queryParam("createdBy", "Connected Vehicle Emergency Notification")
			.queryParam("source", "CV System");
			
		System.out.println(builder.buildAndExpand().toUri());
		
		HttpHeaders responseHeaders = new HttpHeaders();
		//String result = restTemplate.postForObject(builder.buildAndExpand().toUri(), new HttpEntity<String>(null, responseHeaders), String.class);		
		TracManager.logNewDistressNotification(connection, payload.getTim().getPacketID(), "Distress Notification Issued at " + latitude + "/" + longitude);
	}

	private static void addBSMToOracleDB(OdeData odeData, String value, Connection connection) {		
					
		Long bsmCoreDataId = BsmCoreDataService.insertBSMCoreData((OdeBsmMetadata)odeData.getMetadata(), ((OdeBsmPayload)odeData.getPayload()).getBsm(), connection);

		if(bsmCoreDataId != null && !bsmCoreDataId.equals(new Long(0))){
			for(int i = 0; i < ((OdeBsmPayload)odeData.getPayload()).getBsm().getPartII().size(); i++) {																			
				if(((OdeBsmPayload)odeData.getPayload()).getBsm().getPartII().get(i).getId().name() == "VehicleSafetyExtensions") {					
					J2735VehicleSafetyExtensions vse = JsonToJavaConverter.convertJ2735VehicleSafetyExtensionsJsonToJava(value, i);
					if(vse != null)
						BsmPart2VseService.insertBSMPart2VSE(((OdeBsmPayload)odeData.getPayload()).getBsm().getPartII().get(i), vse, bsmCoreDataId, connection);
				}
				else if(((OdeBsmPayload)odeData.getPayload()).getBsm().getPartII().get(i).getId().name() == "SpecialVehicleExtensions"){
					J2735SpecialVehicleExtensions spve = JsonToJavaConverter.convertJ2735SpecialVehicleExtensionsJsonToJava(value, i);
					if(spve != null)
						BsmPart2SpveService.insertBSMPart2SPVE(((OdeBsmPayload)odeData.getPayload()).getBsm().getPartII().get(i), spve, bsmCoreDataId, connection);
				}
				else if(((OdeBsmPayload)odeData.getPayload()).getBsm().getPartII().get(i).getId().name() == "SupplementalVehicleExtensions"){
					J2735SupplementalVehicleExtensions suve = JsonToJavaConverter.convertJ2735SupplementalVehicleExtensionsJsonToJava(value, i);
					if(suve != null)
						BsmPart2SuveService.insertBSMPart2SUVE(((OdeBsmPayload)odeData.getPayload()).getBsm().getPartII().get(i), suve, bsmCoreDataId, connection);
				}					
			}	
		}							
	}

	public static void addTimToOracleDB(OdeData odeData, Connection connection){
		
		try {
			Long timId = TimLogger.insertTim((OdeTimMetadata)odeData.getMetadata(), ((OdeTimPayload)odeData.getPayload()).getTim(), connection);
			Long dataFrameId = DataFrameLogger.insertDataFrame(timId, connection);
			Long pathId = PathLogger.insertPath(connection);
			Long regionId = RegionLogger.insertRegion(dataFrameId, pathId, ((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getRegions()[0].getAnchorPosition(), connection);		
			Long nodeXYId;

			for (J2735TravelerInformationMessage.NodeXY nodeXY : ((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getRegions()[0].getPath().getNodes()) {
				nodeXYId = NodeXYLogger.insertNodeXY(nodeXY, connection);
				PathNodeXYLogger.insertPathNodeXY(nodeXYId, pathId, connection);
			}							
		}
		catch(NullPointerException e){
			System.out.println(e.getMessage());
		}
	}

	public static void addDriverAlertToOracleDB(OdeData odeData, Connection connection) {
		Long driverAlertId = DriverAlertLogger.insertDriverAlert((OdeDriverAlertMetadata)odeData.getMetadata(), ((OdeDriverAlertPayload)odeData.getPayload()).getAlert(), connection);	
	}

	public static OdeData processBsmJson(String value){
		OdeData odeData = null;
		OdeBsmMetadata odeBsmMetadata = JsonToJavaConverter.convertBsmMetadataJsonToJava(value);
		OdeBsmPayload odeBsmPayload = JsonToJavaConverter.convertBsmPayloadJsonToJava(value);
			if(odeBsmMetadata != null && odeBsmPayload != null)
	        	odeData = new OdeData(odeBsmMetadata, odeBsmPayload);
		return odeData;
	}

	public static OdeData processTimJson(String value){
		OdeData odeData = null;
		OdeTimMetadata odeTimMetadata = JsonToJavaConverter.convertTimMetadataJsonToJava(value);
		OdeTimPayload odeTimPayload = JsonToJavaConverter.convertTimPayloadJsonToJava(value);
		if(odeTimMetadata != null && odeTimPayload != null)
			odeData = new OdeData(odeTimMetadata, odeTimPayload);
		return odeData;
	}

	public static OdeData processDriverAlertJson(String value){
		OdeData odeData = null;
		OdeDriverAlertMetadata odeDriverAlertMetadata = JsonToJavaConverter.convertDriverAlertMetadataJsonToJava(value);
		OdeDriverAlertPayload odeDriverAlertPayload = JsonToJavaConverter.convertDriverAlertPayloadJsonToJava(value);
		if(odeDriverAlertMetadata != null && odeDriverAlertPayload != null)
			odeData = new OdeData(odeDriverAlertMetadata, odeDriverAlertPayload);
		return odeData;
	}

	public static void main( String[] args ) throws IOException
	 {
		Options options = new Options();

		Option topic_option = new Option("t", "topic", true, "Topic Name");
		topic_option.setRequired(true);
		options.addOption(topic_option);

		Option group_option = new Option("g", "group", true, "Consumer Group");
		group_option.setRequired(true);
		options.addOption(group_option);

		Option type_option = new Option("type", "type", true, "string|byte message type");
		type_option.setRequired(true);
		options.addOption(type_option);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("Consumer Example", options);
			System.exit(1);
			return;
		}

		String topic = cmd.getOptionValue("topic");
		String group = cmd.getOptionValue("group");
		String type = cmd.getOptionValue("type");

  		System.out.println("starting..............");   
		Connection connection = makeJDBCConnection();		
		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		//String endpoint = "https://ode.wyoroad.info:9092";		
		String endpoint = "10.145.9.204:9092";
	
		List<String> topics = new ArrayList<String>();
		topics.add("topic.OdeBsmJson");

		// Properties for the kafka topic
		Properties props = new Properties();
		props.put("bootstrap.servers", endpoint);
		props.put("group.id", group);
		props.put("enable.auto.commit", "false");
		props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		if (type.equals("byte")){ 
			props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
		} else {
			props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		}

		if (type.equals("byte")) {
			KafkaConsumer<String, byte[]> byteArrayConsumer = new KafkaConsumer<String, byte[]>(props);

			byteArrayConsumer.subscribe(Arrays.asList(topic));
			System.out.println("Subscribed to topic " + topic);
			while (true) {
				ConsumerRecords<String, byte[]> records = byteArrayConsumer.poll(100);
				for (ConsumerRecord<String, byte[]> record : records) {
					// Serialize the record value
					try{					
				    	SerializationUtils<OdeBsmData> serializer = new SerializationUtils<OdeBsmData>();
						OdeBsmData bsm = serializer.deserialize(record.value());
						System.out.println(bsm.getMetadata().getPayloadType());
						//SerializationUtils<OdeTimData> serializer = new SerializationUtils<OdeTimData>();
						//Object tim = serializer.deserialize(record.value());
			   			//System.out.print(record.value()); 
						//System.out.println("adding to db...");
						//addDataToOracleDB((OdeBsmMetadata)bsm.getMetadata(), (J2735Bsm)bsm.getPayload().getData());
					}
					catch(Exception e){
						System.out.print(e);
					}
				}
			}
		} else {
			KafkaConsumer<String, String> stringConsumer = new KafkaConsumer<String, String>(props);

			stringConsumer.subscribe(Arrays.asList(topic));
			System.out.println("Subscribed to topic " + topic);
			try{
				
			    while (true) {						
					ConsumerRecords<String, String> records = stringConsumer.poll(100);
					for (ConsumerRecord<String, String> record : records) {						
						//String payload = JsonUtils.toObjectNode(record.value()).get("metadata").get("payloadType").toString();
						//payload = payload.substring(1, payload.length() - 1);
						System.out.println(records.count());
						if(topic.equals("topic.OdeDNMsgJson")){
							submitDNMsgToTrac(record.value(), connection);
						}
						else if(topic.equals("topic.OdeTimJson")) {	
							OdeData odeData = processTimJson(record.value());
							if(odeData != null)
								addTimToOracleDB(odeData, connection);				
						}													
						else if(topic.equals("topic.OdeBsmJson")){
							OdeData odeData = processBsmJson(record.value());
							if(odeData != null)
								addBSMToOracleDB(odeData, record.value(), connection);		
						}
						else if(topic.equals("topic.OdeDriverAlertJson")){
							OdeData odeData = processDriverAlertJson(record.value());
							if(odeData != null)
								addDriverAlertToOracleDB(odeData, connection);		
						}
					}
				}
			}
			finally{
				stringConsumer.close();
			}		
		}
	}
}