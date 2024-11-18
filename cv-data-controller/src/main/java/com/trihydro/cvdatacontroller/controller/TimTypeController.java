package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.TimType;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("tim-type")
public class TimTypeController extends BaseController {

    @RequestMapping(value = "/tim-types")
    public ResponseEntity<List<TimType>> SelectAll() {
        List<TimType> timTypes = new ArrayList<TimType>();
        Connection connection = null;
        ResultSet rs = null;
        Statement statement = null;

        try {
            connection = dbInteractions.getConnectionPool();
            statement = connection.createStatement();

            // build SQL statement
            rs = statement.executeQuery("select * from TIM_TYPE");
            // convert to tim type objects
            while (rs.next()) {
                TimType timType = new TimType();
                timType.setTimTypeId(rs.getLong("TIM_TYPE_ID"));
                timType.setType(rs.getString("TYPE"));
                timType.setDescription(rs.getString("DESCRIPTION"));
                timTypes.add(timType);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(timTypes);
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
        return ResponseEntity.ok(timTypes);
    }
}