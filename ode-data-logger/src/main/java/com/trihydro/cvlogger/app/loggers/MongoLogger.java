package com.trihydro.cvlogger.app.loggers;

import java.util.Arrays;

import org.bson.Document;

import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.trihydro.library.model.ConfigProperties;
import com.mongodb.MongoCredential;
import com.mongodb.MongoClientSettings;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;

public class MongoLogger {

        private static String username;
        private static String password;
        private static String databaseName;
        private static MongoCredential credential;

        public static void setConfig(ConfigProperties config){
                username = config.getMongoUsername(); // the user name
                databaseName = config.getMongoDatabase(); // the name of the database in which the user is defined
                password = config.getMongoPassword(); // the password as a character array
                credential = MongoCredential.createCredential(username, databaseName, password.toCharArray());
        }

        public static void logTim(String timRecord) {

                MongoClient mongoClient = MongoClients.create(MongoClientSettings.builder()
                        .applyToClusterSettings(
                                builder -> builder.hosts(Arrays.asList(new ServerAddress("10.145.9.225", 27017))))
                        .credential(credential).build());
                
                MongoDatabase database = mongoClient.getDatabase(databaseName);

                MongoCollection<Document> collection = database.getCollection("tim");

                Document doc = Document.parse(timRecord);

                collection.insertOne(doc);

                mongoClient.close();
        }

        public static void logBsm(String bsmRecord) {
        
                MongoClient mongoClient = MongoClients.create(MongoClientSettings.builder()
                        .applyToClusterSettings(
                                builder -> builder.hosts(Arrays.asList(new ServerAddress("10.145.9.225", 27017))))
                        .credential(credential).build());

                MongoDatabase database = mongoClient.getDatabase(databaseName);

                MongoCollection<Document> collection = database.getCollection("bsm");

                Document doc = Document.parse(bsmRecord);

                collection.insertOne(doc);

                mongoClient.close();
        }

        public static void logDriverAlert(String driverAlertRecord) {

                MongoClient mongoClient = MongoClients.create(MongoClientSettings.builder()
                        .applyToClusterSettings(
                                builder -> builder.hosts(Arrays.asList(new ServerAddress("10.145.9.225", 27017))))
                        .credential(credential).build());

                MongoDatabase database = mongoClient.getDatabase(databaseName);

                MongoCollection<Document> collection = database.getCollection("driverAlert");

                Document doc = Document.parse(driverAlertRecord);

                collection.insertOne(doc);

                mongoClient.close();
        }
}