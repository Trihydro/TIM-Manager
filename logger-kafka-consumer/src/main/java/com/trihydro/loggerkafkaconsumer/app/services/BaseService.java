package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.DbInteractions;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.SecurityResultCodeType;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BaseService {

    protected DbInteractions dbInteractions;
    protected Utility utility;

    @Autowired
    public void InjectBaseDependencies(DbInteractions _dbInteractions, Utility _utility) {
        dbInteractions = _dbInteractions;
        utility = _utility;
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
            log.error("Exception", e);
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
                log.error("Exception", e);
            }
        }

        return securityResultCodeTypes;
    }
}