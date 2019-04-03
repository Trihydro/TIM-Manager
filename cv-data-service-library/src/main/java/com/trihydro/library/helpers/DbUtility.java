package com.trihydro.library.helpers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.TimeZone;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.apache.ibatis.io.Resources;

import com.trihydro.library.model.ConfigProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@ComponentScan
@EnableConfigurationProperties(ConfigProperties.class)
public class DbUtility {

    private static HikariDataSource hds = null;
    private static HikariConfig config = new HikariConfig();
    private static ConfigProperties dbConfig;

    public static void setConfig(ConfigProperties configProperties) {
        dbConfig = configProperties;
    }

    public static ConfigProperties getConfig() {
        return dbConfig;
    }

    // get connection
    public static Connection getConnectionPool() {

        // create pool if not already done
        if (hds == null) {

            TimeZone timeZone = TimeZone.getTimeZone("America/Denver");
            TimeZone.setDefault(timeZone);

            config.addDataSourceProperty("cachePrepStmts", "true");
            config.setUsername(dbConfig.getDbUsername());
            config.setPassword(dbConfig.getDbPassword());
            config.setJdbcUrl(dbConfig.getDbUrl());
            config.setDriverClassName(dbConfig.getDbDriver());
            config.setMaximumPoolSize(5);

            hds = new HikariDataSource(config);

            if (dbConfig.getEnv().equals("test")) {
                // run scripts for in-memory test database
                try {

                    ScriptRunner scriptRunner = new ScriptRunner(hds.getConnection());
                    scriptRunner.runScript(Resources.getResourceAsReader("db/unitTestSql.sql"));
                    hds.getConnection().commit();

                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

}