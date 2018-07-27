package com.trihydro.library.helpers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.TimeZone;

import org.apache.ibatis.jdbc.ScriptRunner;

import org.apache.ibatis.io.Resources;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DbUtility {
    
    public static String connectionEnvironment = "dev";
    private static String dbDriver = "oracle.jdbc.pool.OracleDataSource";
    private static String dbDriverH = "oracle.jdbc.driver.OracleDriver";
    private static String dbUrl = "jdbc:oracle:thin:@10.145.9.22:1521/cvdev.gisits.local";
    private static String dbUsername = "CVCOMMS";
    private static String dbPassword = "C0ll1s10n";
    private static String dbDriverTest = "org.h2.Driver";
    private static String dbUrlTest = "jdbc:h2:mem:db";
    private static HikariDataSource hds = null;
    private static HikariConfig config = new HikariConfig();

    public static Connection getConnectionHikari(){
        if(hds == null){

            config.addDataSourceProperty( "cachePrepStmts" , "true" );
            config.setUsername(dbUsername);
            config.setPassword(dbPassword);
            config.setJdbcUrl(dbUrl);
            config.setDriverClassName(dbDriverH);
            config.setMaximumPoolSize(5);
            
            hds = new HikariDataSource(config);
        }

        try {
			return hds.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
        }
        
        return null;
    }

    // get connection
    public static Connection getConnectionPool(){
                
        // create pool if not already done
        if(hds == null){
        
            TimeZone timeZone = TimeZone.getTimeZone("America/Denver");
            TimeZone.setDefault(timeZone);

            if(connectionEnvironment.equals("test")){
                // in-memory test database
                try {
                    config.addDataSourceProperty( "cachePrepStmts" , "true" );
                    config.setUsername(dbUsername);
                    config.setPassword(dbPassword);
                    config.setJdbcUrl(dbUrlTest);
                    config.setDriverClassName(dbDriverTest);
                    config.setMaximumPoolSize(10);

                    hds = new HikariDataSource(config);

                    ScriptRunner scriptRunner = new ScriptRunner(hds.getConnection());
                    scriptRunner.runScript(Resources.getResourceAsReader("db/unitTestSql.sql"));
                    hds.getConnection().commit();
                   
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (IOException e){
                    e.printStackTrace();
                }	
            }
            else{
                config.addDataSourceProperty( "cachePrepStmts" , "true" );
                config.setUsername(dbUsername);
                config.setPassword(dbPassword);
                config.setJdbcUrl(dbUrl);
                config.setDriverClassName(dbDriverH);
                config.setMaximumPoolSize(10);
                hds = new HikariDataSource(config);               
            }        
        }
        
        // return a connection
        try {
			return hds.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
        }
        
        return null;
    }

    public static void changeConnection(String connectionEnvironmentInput){
        connectionEnvironment = connectionEnvironmentInput;
        hds = null;
    } 

    public static String getConnectionEnvironment(){
        return connectionEnvironment;
    } 
}