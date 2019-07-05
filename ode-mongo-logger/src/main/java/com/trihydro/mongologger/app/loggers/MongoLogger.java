package com.trihydro.mongologger.app.loggers;

import java.util.Arrays;
import java.util.ArrayList;

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

        private static String serverAddress;
        private static String username;
        private static String password;
        private static String databaseName;
        private static MongoCredential credential;

        public static void setConfig(ConfigProperties config){
                username = config.getMongoUsername(); // the user name
                databaseName = config.getMongoDatabase(); // the name of the database in which the user is defined
                password = config.getMongoPassword(); // the password as a character array
                serverAddress = config.getMongoHost();
                credential = MongoCredential.createCredential(username, databaseName, password.toCharArray());
        }

        public static void logTim(String[] timRecord) {
                MongoLogger.logMultipleToCollection(timRecord, "tim");
        }

        public static void logBsm(String[] bsmRecord) {
                MongoLogger.logMultipleToCollection(bsmRecord, "bsm");
        }

        public static void logDriverAlert(String[] driverAlertRecord) {
                MongoLogger.logMultipleToCollection(driverAlertRecord, "driverAlert");
        }

        public static void logMultipleToCollection(String[] records, String collectionName){
                ArrayList<Document> docs = new ArrayList<Document>();
                
                for (String rec : records) {
                        docs.add(Document.parse(rec));     
                }

                if(docs.size() > 0){
                        MongoClient mongoClient = MongoClients.create(MongoClientSettings.builder()
                        .applyToClusterSettings(
                                builder -> builder.hosts(Arrays.asList(new ServerAddress(serverAddress, 27017))))
                        .credential(credential).build());
                        
                        MongoDatabase database = mongoClient.getDatabase(databaseName);
                        MongoCollection<Document> collection = database.getCollection(collectionName);
                        collection.insertMany(docs);
                        mongoClient.close();
                }
        }
}