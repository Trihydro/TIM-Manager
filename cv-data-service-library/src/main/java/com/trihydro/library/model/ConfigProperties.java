package com.trihydro.library.model;

public class ConfigProperties {

    private String odeUrl;
    private String dbDriver;
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;
    private String env;
    private String mongoDatabase;
    private String mongoUsername;
    private String mongoPassword;
    private String trackUrl;

    public String getOdeUrl() {
        return odeUrl;
    }

    public void setOdeUrl(String odeUrl) {
        this.odeUrl = odeUrl;
    }

    public String getDbDriver() {
        return dbDriver;
    }

    public void setDbDriver(String dbDriver) {
        this.dbDriver = dbDriver;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }    

    public String getMongoDatabase() {
        return mongoDatabase;
    }

    public void setMongoDatabase(String mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }    

    public String getMongoUsername() {
        return mongoUsername;
    }

    public void setMongoUsername(String mongoUsername) {
        this.mongoUsername = mongoUsername;
    }    

    public String getMongoPassword() {
        return mongoPassword;
    }

    public void setMongoPassword(String mongoPassword) {
        this.mongoPassword = mongoPassword;
    }    

    public void setTracUrl(String url){
        this.trackUrl = url;
    }
    public String getGetTrackUrl(){
        return this.trackUrl;
    }
}