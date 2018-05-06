package com.trihydro.library.helpers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.TimeZone;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.io.Resources; 

public class DbUtility {
    
    private static Connection connection = null;
    public static String connectionEnvironment = "dev";
    private static String dbDriver = "oracle.jdbc.driver.OracleDriver";
    private static String dbUrl = "jdbc:oracle:thin:@10.145.9.22:1521/cvdev.gisits.local";
    private static String dbUsername = "CVCOMMS";
    private static String dbPassword = "C0ll1s10n";

    private static String dbDriverTest = "org.h2.Driver";
    private static String dbUrlTest = "jdbc:h2:mem:db";

    public static void changeConnection(String connectionEnvironmentInput){
        connectionEnvironment = connectionEnvironmentInput;
        connection = null;
    } 

    public static String getConnectionEnvironment(){
        return connectionEnvironment;
    } 

    // database connection, dependent on the application.properties variables                 
    public static Connection getConnection() {
        // return the connection if its already created
        if (connection != null) {
            return connection;
        }
        // else create the connection
        else {
            try {

                // set timezone
                TimeZone timeZone = TimeZone.getTimeZone("America/Denver");
                TimeZone.setDefault(timeZone);

                // make connection
                if(connectionEnvironment == "test"){
                    Class.forName(dbDriverTest);
                    connection = DriverManager.getConnection(dbUrlTest, dbUsername, null); 
                }
                else{
                    Class.forName(dbDriver);
                    connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword); 
                }
                
                // connection successful
                if (connection != null) {
                    System.out.println("Connection Successful! Enjoy. Now it's time to push data");

                    // if using an in memory database for unit tests                        
                     if(connectionEnvironment.equals("test")) {               
                        // Initialize object for ScriptRunner to read in a script to create tables and insert data
                        ScriptRunner scriptRunner = new ScriptRunner(connection);
                        try {
                            // run script
                            scriptRunner.runScript(Resources.getResourceAsReader("db/unitTestSql.sql"));
                            connection.commit();
                        } 
                        catch (Exception e) {
                            throw new IllegalStateException("ScriptRunner failed", e);
                        }                               
                    }                                        
                } 
                // else connection failed
                else {
                    System.out.println("Failed to make connection!");
                }
            } 
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            } 
            catch (SQLException e) {
                e.printStackTrace();            
            } 
            return connection;
        }
    }
}