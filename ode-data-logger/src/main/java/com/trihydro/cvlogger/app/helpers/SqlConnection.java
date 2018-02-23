package com.trihydro.cvlogger.app.helpers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlConnection {
    
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
}


