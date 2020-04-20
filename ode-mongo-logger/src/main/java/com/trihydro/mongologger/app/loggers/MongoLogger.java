package com.trihydro.mongologger.app.loggers;

import java.util.ArrayList;
import java.util.Arrays;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.trihydro.mongologger.app.MongoLoggerConfiguration;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MongoLogger {

        private String serverAddress;
        private String username;
        private String password;
        private String databaseName;
        private MongoCredential credential;

        @Autowired
        public void InjectDependencies(MongoLoggerConfiguration config) {
                username = config.getMongoUsername(); // the user name
                databaseName = config.getMongoDatabase(); // the name of the database in which the user is defined
                password = config.getMongoPassword(); // the password as a character array
                serverAddress = config.getMongoHost();
                credential = MongoCredential.createCredential(username, databaseName, password.toCharArray());
        }

        public void logTim(String[] timRecord) {
                logMultipleToCollection(timRecord, "tim");
        }

        public void logBsm(String[] bsmRecord) {
                logMultipleToCollection(bsmRecord, "bsm");
        }

        public void logDriverAlert(String[] driverAlertRecord) {
                logMultipleToCollection(driverAlertRecord, "driverAlert");
        }

        public void logMultipleToCollection(String[] records, String collectionName) {
                ArrayList<Document> docs = new ArrayList<Document>();

                for (String rec : records) {
                        docs.add(Document.parse(rec));
                }

                if (docs.size() > 0) {
                        MongoClient mongoClient = MongoClients.create(MongoClientSettings.builder()
                                        .applyToClusterSettings(builder -> builder
                                                        .hosts(Arrays.asList(new ServerAddress(serverAddress, 27017))))
                                        .credential(credential).build());

                        MongoDatabase database = mongoClient.getDatabase(databaseName);
                        MongoCollection<Document> collection = database.getCollection(collectionName);
                        collection.insertMany(docs);
                        mongoClient.close();
                }
        }
}