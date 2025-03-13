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
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.Utility;
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
    private String authDatabaseName;
    private MongoCredential credential;
    private Utility utility;
    private EmailHelper emailHelper;
    private MongoLoggerConfiguration config;
    private MongoClient _mongoClient;

    @Autowired
    public void InjectDependencies(MongoLoggerConfiguration _config, Utility _utility, EmailHelper _emailHelper) {
        config = _config;
        username = config.getMongoUsername(); // the user name
        databaseName = config.getMongoDatabase(); // the name of the database to deposit records into
        authDatabaseName = config.getMongoAuthDatabase(); // the name of the database in which the user is defined
        password = config.getMongoPassword(); // the password as a character array
        serverAddress = config.getMongoHost();
        credential = MongoCredential.createCredential(username, authDatabaseName, password.toCharArray());
        utility = _utility;
        emailHelper = _emailHelper;
        configureMongoClient();
    }

    private void configureMongoClient(){
        _mongoClient = MongoClients.create(MongoClientSettings.builder()
            .applyToClusterSettings(
                builder -> builder.hosts(Arrays.asList(new ServerAddress(serverAddress, 27017))))
            .credential(credential).build());
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
            try {
                MongoDatabase database = _mongoClient.getDatabase(databaseName);
                MongoCollection<Document> collection = database.getCollection(collectionName);
                collection.insertMany(docs);
            } catch (Exception ex) {
                utility.logWithDate("Error logging to mongo collection: " + ex.getMessage());

                String body = "The MongoLogger failed attempting to insert a record to ";
                body += collectionName;
                body += "<br/><br/>";
                body += "Exception: <br/>";
                body += ex.getMessage();
                try {
                    emailHelper.SendEmail(config.getAlertAddresses(), "MongoLogger Failed to Connect to MongoDB", body);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}