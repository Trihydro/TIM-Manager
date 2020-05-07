package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.trihydro.library.helpers.DbInteractions;
import com.trihydro.library.model.SecurityResultCodeType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BaseService {

    protected DbInteractions dbInteractions;

    private DateFormat utcFormatMilliSec = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private DateFormat utcFormatSec = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private DateFormat utcFormatMin = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    public DateFormat mstFormat = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a");

    @Autowired
    public void InjectBaseDependencies(DbInteractions _dbInteractions) {
        dbInteractions = _dbInteractions;
    }

    public Date convertDate(String incomingDate) {

        Date convertedDate = null;

        try {
            if (incomingDate != null) {
                if (incomingDate.contains("."))
                    convertedDate = utcFormatMilliSec.parse(incomingDate);
                else if (incomingDate.length() == 22)
                    convertedDate = utcFormatMin.parse(incomingDate);
                else
                    convertedDate = utcFormatSec.parse(incomingDate);
            }
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        return convertedDate;
    }

    public List<SecurityResultCodeType> GetSecurityResultCodeTypes() {
        SecurityResultCodeType securityResultCodeType = null;
        List<SecurityResultCodeType> securityResultCodeTypes = new ArrayList<SecurityResultCodeType>();
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;

        try {

            connection = dbInteractions.getConnectionPool();
            statement = connection.createStatement();
            rs = statement.executeQuery("select * from SECURITY_RESULT_CODE_TYPE");

            // convert to ActiveTim object
            while (rs.next()) {
                securityResultCodeType = new SecurityResultCodeType();
                securityResultCodeType.setSecurityResultCodeTypeId(rs.getInt("SECURITY_RESULT_CODE_TYPE_ID"));
                securityResultCodeType.setSecurityResultCodeType(rs.getString("SECURITY_RESULT_CODE_TYPE"));
                securityResultCodeTypes.add(securityResultCodeType);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                // close prepared statement
                if (statement != null)
                    statement.close();
                // return connection back to pool
                if (connection != null)
                    connection.close();
                // close result set
                if (rs != null)
                    rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return securityResultCodeTypes;
    }
}