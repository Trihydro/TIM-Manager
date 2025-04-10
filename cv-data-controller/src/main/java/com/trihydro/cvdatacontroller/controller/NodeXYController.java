package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimDbTables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;

@CrossOrigin
@RestController
@RequestMapping("nodexy")
@ApiIgnore
public class NodeXYController extends BaseController {

    private TimDbTables timDbTables;
    private SQLNullHandler sqlNullHandler;

    @Autowired
    public void InjectDependencies(TimDbTables _timDbTables, SQLNullHandler _sqlNullHandler) {
        timDbTables = _timDbTables;
        sqlNullHandler = _sqlNullHandler;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/add-nodexy")
    public ResponseEntity<Long> AddNodeXY(@RequestBody OdeTravelerInformationMessage.NodeXY nodeXY) {

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {

            connection = dbInteractions.getConnectionPool();
            String insertQueryStatement = timDbTables.buildInsertQueryStatement("node_xy",
                    timDbTables.getNodeXYTable());
            preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "node_xy_id" });
            int fieldNum = 1;

            for (String col : timDbTables.getNodeXYTable()) {
                if (col.equals("DELTA"))
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, nodeXY.getDelta());
                else if (col.equals("NODE_LAT"))
                    sqlNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, nodeXY.getNodeLat());
                else if (col.equals("NODE_LONG"))
                    sqlNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, nodeXY.getNodeLong());
                else if (col.equals("X"))
                    sqlNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, nodeXY.getXpos());
                else if (col.equals("Y"))
                    sqlNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, nodeXY.getYpos());
                else if (col.equals("ATTRIBUTES_DWIDTH"))
                    if (nodeXY.getAttributes() != null)
                        sqlNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum,
                                nodeXY.getAttributes().getDwidth());
                    else
                        preparedStatement.setString(fieldNum, null);
                else if (col.equals("ATTRIBUTES_DELEVATION"))
                    if (nodeXY.getAttributes() != null)
                        sqlNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum,
                                nodeXY.getAttributes().getDelevation());
                    else
                        preparedStatement.setString(fieldNum, null);
                fieldNum++;
            }
            Long nodeXYId = dbInteractions.executeAndLog(preparedStatement, "nodexy");
            return ResponseEntity.ok(nodeXYId);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Long.valueOf(0));
        } finally {
            try {
                // close prepared statement
                if (preparedStatement != null)
                    preparedStatement.close();
                // return connection back to pool
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}