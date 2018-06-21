package com.trihydro.library.helpers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TimeZone;

import javax.sql.PooledConnection;

import org.apache.ibatis.jdbc.ScriptRunner;

import oracle.jdbc.pool.OracleConnectionPoolDataSource;
import oracle.jdbc.pool.OracleDataSource;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

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
    private static boolean first = true;

    public static void changeConnection(String connectionEnvironmentInput){
        connectionEnvironment = connectionEnvironmentInput;
        connection = null;
    } 

    public static String getConnectionEnvironment(){
        return connectionEnvironment;
    } 

    public static void resetConnection(){
        connection = null;
    }

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
    // public static Connection getPooledConnection() throws Exception{
        
    //     OracleConnectionPoolDataSource ocpds = new OracleConnectionPoolDataSource();
    //     ocpds.setURL(dbUrl);
    //     ocpds.setUser(dbUsername);
    //     ocpds.setPassword(dbPassword);
    
    //     PooledConnection pc_1 = ocpds.getPooledConnection();
    
    //     Connection conn_1 = pc_1.getConnection();
    //    // Statement stmt = conn_1.createStatement();
    
    //     // ResultSet rs = stmt.executeQuery("SELECT count(*) FROM v$session WHERE username = 'SYS'");
    //     // rs.next();
    //     // String msg = "Total connections after ";
    //     // System.out.println(msg + "conn_1: " + rs.getString(1));
        
    //     return conn_1;
    
    //     // Connection conn_2 = pc_1.getConnection();
    //     // stmt = conn_2.createStatement();
    //     // rs = stmt.executeQuery("SELECT count(*) FROM v$session WHERE username = 'SYS'");
    //     // rs.next();
    //     // System.out.println(msg + "conn_2: " + rs.getString(1));
    
    //     // PooledConnection pc_2 = ocpds.getPooledConnection();
    //     // rs = stmt.executeQuery("SELECT count(*) FROM v$session WHERE username = 'SYS'");
    //     // rs.next();
    //     // System.out.println(msg + "pc_2: " + rs.getString(1));
    
    //     // conn_1.close();
    //     // conn_2.close();
    //     // pc_1.close();
    //     // pc_2.close();

    // }

    public static Connection getConnectionPool() {

        PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
        
        // testing connection
        if(connectionEnvironment == "test"){
            if (connection != null) {
                return connection;
            }
            else {
                // set timezone                
                TimeZone timeZone = TimeZone.getTimeZone("America/Denver");
                TimeZone.setDefault(timeZone);

                // make connection                  
                try {
                    Class.forName(dbDriverTest);
                } catch (ClassNotFoundException e1) {				
                    e1.printStackTrace();
                }
                try {
                    connection = DriverManager.getConnection(dbUrlTest, dbUsername, null);
                    //connection = pds.getConnection();      
                } catch (SQLException e1) {					
                    e1.printStackTrace();
                } 
                //connection = pds.getConnection();
                                    
                // connection successful
                if (connection != null) {
                    System.out.println("Connection Successful! Enjoy. Now it's time to push data");

                    // if using an in memory database for unit tests                        
                        if(connectionEnvironment.equals("test") && first) {               
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
                        first = false;
                    }                                        
                } 
                // else connection failed
                else {
                    System.out.println("Failed to make connection!");
                }
                return connection;
            }
        }
        else {
            try {
                pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");		
                pds.setURL(dbUrl);		       
                pds.setUser(dbUsername);    		     
                pds.setPassword(dbPassword);		        
                pds.setInitialPoolSize(20);
            }
            catch (SQLException e1) {        
                e1.printStackTrace();
            }	
            try {

                // set timezone
                TimeZone timeZone = TimeZone.getTimeZone("America/Denver");
                TimeZone.setDefault(timeZone);

                // make connection                   
                Class.forName(dbDriver);
                connection = pds.getConnection();                   
                
                // connection successful
                if (connection != null) {
                    System.out.println("Connection Successful! Enjoy. Now it's time to push data");                                                          
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