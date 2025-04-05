package com.trihydro.mongologger.app.loggers;

import java.util.ArrayList;

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

import java.util.List;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MongoLogger {
    private final Utility utility;
    private final EmailHelper emailHelper;
    private final String databaseName;
    private final String[] alertAddresses;
    private final MongoClient mongoClient;

    @Autowired
    public MongoLogger(MongoLoggerConfiguration config, Utility utility, EmailHelper emailHelper) {
        this.emailHelper = emailHelper;
        this.utility = utility;
        this.mongoClient = configureMongoClient(config);
        this.databaseName = config.getMongoDatabase(); // the name of the database to deposit records into
        this.alertAddresses = config.getAlertAddresses(); // the email addresses to send alerts to
    }

    private MongoClient configureMongoClient(MongoLoggerConfiguration config) {
        MongoCredential credential = MongoCredential.createCredential(config.getMongoUsername(), config.getMongoAuthDatabase(), config.getMongoPassword().toCharArray());
        var hosts = List.of(new ServerAddress(config.getMongoHost(), 27017));
        var settings = MongoClientSettings.builder()
            .applyToClusterSettings(builder -> builder.hosts(hosts)).credential(credential)
            .build();
        return MongoClients.create(settings);
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
        ArrayList<Document> docs = new ArrayList<>();

        for (String rec : records) {
            docs.add(Document.parse(rec));
        }

        if (!docs.isEmpty()) {
            try {
                MongoDatabase database = mongoClient.getDatabase(databaseName);
                MongoCollection<Document> collection = database.getCollection(collectionName);
                collection.insertMany(docs);
            } catch (Exception ex) {
                System.out.println("Error logging to mongo collection: " + ex.getMessage());

                String body = "The MongoLogger failed attempting to insert a record to ";
                body += collectionName;
                body += "<br/><br/>";
                body += "Exception: <br/>";
                body += ex.getMessage();
                try {
                    emailHelper.SendEmail(alertAddresses, "MongoLogger Failed to Connect to MongoDB", body);
                } catch (Exception e) {
                    System.out.println("Error sending email: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}