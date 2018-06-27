package com.trihydro.library.helpers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TimeZone;

import javax.sql.PooledConnection;

import org.apache.ibatis.jdbc.ScriptRunner;

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

    private static PoolDataSource pds;

    // constructor 
    static {
        if(connectionEnvironment == "test"){
            // Class.forName(dbDriverTest);
            // connection = DriverManager.getConnection(dbUrlTest, dbUsername, null); 
            System.out.println("test");

            
        }
        
        pds = PoolDataSourceFactory.getPoolDataSource();
        try {
			pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");				
            pds.setURL(dbUrl);		       
            pds.setUser(dbUsername);    		     
            pds.setPassword(dbPassword);		        
            pds.setInitialPoolSize(5);
            pds.setValidateConnectionOnBorrow(true);
            pds.setSQLForValidateConnection("select * from rsu");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnectionPool(){
        
        if(connectionEnvironment.equals("test")){
            return connection;
        }
        
        try {
			return pds.getConnection();
		} catch (SQLException e) {
            e.printStackTrace();
            return null;
		}
    }

    public static void changeConnection(String connectionEnvironmentInput){
        connectionEnvironment = connectionEnvironmentInput;
        connection = null;
        
        TimeZone timeZone = TimeZone.getTimeZone("America/Denver");
        TimeZone.setDefault(timeZone);

        if(connectionEnvironmentInput.equals("test")){
        
            try {
				Class.forName(dbDriverTest);			
                connection = DriverManager.getConnection(dbUrlTest, dbUsername, null); 
                ScriptRunner scriptRunner = new ScriptRunner(connection);
                scriptRunner.runScript(Resources.getResourceAsReader("db/unitTestSql.sql"));
                connection.commit();
            } 
            catch (ClassNotFoundException e) {           
                e.printStackTrace();
            } 
            catch (SQLException e) {				
				e.printStackTrace();
            }
            catch (Exception e) {
                throw new IllegalStateException("ScriptRunner failed", e);
            }             
        }
    } 

    public static String getConnectionEnvironment(){
        return connectionEnvironment;
    } 

    public static void resetConnection(){
        connection = null;
    }

    public static Connection getConnectionDep() {
        // return the connection if its already created
        if (connection != null) {
            return connection;
        }
        return null;
        // else create the connection
        // else {
        //     try {

        //         // set timezone
        //         TimeZone timeZone = TimeZone.getTimeZone("America/Denver");
        //         TimeZone.setDefault(timeZone);

        //         // make connection
        //         if(connectionEnvironmenverTest);t == "test"){
        //             Class.forName(dbDri
        //             connection = DriverManager.getConnection(dbUrlTest, dbUsername, null); 
        //         }
        //         else{
        //             Class.forName(dbDriver);
        //             connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword); 
        //         }
                
        //         // connection successful
        //         if (connection != null) {
        //             System.out.println("Connection Successful! Enjoy. Now it's time to push data");

        //             // if using an in memory database for unit tests                        
        //              if(connectionEnvironment.equals("test")) {               
        //                 // Initialize object for ScriptRunner to read in a script to create tables and insert data
        //                 ScriptRunner scriptRunner = new ScriptRunner(connection);
        //                 try {
        //                     // run script
        //                     scriptRunner.runScript(Resources.getResourceAsReader("db/unitTestSql.sql"));
        //                     connection.commit();
        //                 } 
        //                 catch (Exception e) {
        //                     throw new IllegalStateException("ScriptRunner failed", e);
        //                 }                               
        //             }                                        
        //         } 
        //         // else connection failed
        //         else {
        //             System.out.println("Failed to make connection!");
        //         }
        //     } 
        //     catch (ClassNotFoundException e) {
        //         e.printStackTrace();
        //     } 
        //     catch (SQLException e) {
        //         e.printStackTrace();            
        //     } 
        //     return connection;
        // }
    }

    public static Connection getConnectionPoolTest() {

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
                pds.setInitialPoolSize(5);
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