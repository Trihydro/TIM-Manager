package com.trihydro.library.service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ConfigProperties;

public class CvDataServiceLibrary {

    public static DateFormat utcFormatMilliSec;
    public static DateFormat utcFormatSec;
    public static DateFormat utcFormatMin;
    public static DateFormat mstFormat;
    public static DateTimeFormatter localDateTimeformatter;
    public static DateFormat mstLocalFormat;

    protected static String CVRestUrl;

    static {
        utcFormatMilliSec = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        utcFormatSec = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // 25
        utcFormatMin = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        // mstFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        mstFormat = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a");
        mstLocalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS-07:00");
    }

    public static void setConfig(ConfigProperties config) {
        DbUtility.setConfig(config);
    }

    public static void setCVRestUrl(String url){
        CVRestUrl = url;
    }

    public static Long log(PreparedStatement preparedStatement, String type) {
        Long id = null;
        try {
            if (preparedStatement.executeUpdate() > 0) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                try {
                    if (generatedKeys != null && generatedKeys.next()) {
                        id = generatedKeys.getLong(1);
                        Utility.logWithDate("------ Generated " + type + " " + id + " --------------");
                    }
                } finally {
                    try {
                        generatedKeys.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public static boolean updateOrDelete(PreparedStatement preparedStatement) {

        boolean result = false;

        try {
            if (preparedStatement.executeUpdate() > 0) {
                result = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    }
