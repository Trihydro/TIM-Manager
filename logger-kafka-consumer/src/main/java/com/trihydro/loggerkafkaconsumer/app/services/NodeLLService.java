package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimDbTables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;

@Component
public class NodeLLService extends BaseService {

    private TimDbTables timDbTables;
    private SQLNullHandler sqlNullHandler;

    @Autowired
    public void InjectDependencies(TimDbTables _timDbTables, SQLNullHandler _sqlNullHandler) {
        timDbTables = _timDbTables;
        sqlNullHandler = _sqlNullHandler;
    }

    public Long AddNodeLL(OdeTravelerInformationMessage.NodeXY nodeXY) {

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {

            connection = dbInteractions.getConnectionPool();
            String insertQueryStatement = timDbTables.buildInsertQueryStatement("node_ll",
                    timDbTables.getNodeLLTable());
            preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "node_ll_id" });
            int fieldNum = 1;

            for (String col : timDbTables.getNodeLLTable()) {
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
                        preparedStatement.setNull(fieldNum, java.sql.Types.NUMERIC);
                else if (col.equals("ATTRIBUTES_DELEVATION"))
                    if (nodeXY.getAttributes() != null)
                        sqlNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum,
                                nodeXY.getAttributes().getDelevation());
                    else
                        preparedStatement.setNull(fieldNum, java.sql.Types.NUMERIC);
                fieldNum++;
            }
            Long nodeLLId = dbInteractions.executeAndLog(preparedStatement, "nodell");
            return nodeLLId;
        } catch (SQLException e) {
            e.printStackTrace();
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
        return Long.valueOf(0);
    }
}