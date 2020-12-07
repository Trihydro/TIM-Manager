package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.NodeXY;

@CrossOrigin
@RestController
@RequestMapping("path-node-ll")
@ApiIgnore
public class PathNodeLLController extends BaseController {

    @RequestMapping(method = RequestMethod.GET, value = "/get-nodell-path/{pathId}")
    public ResponseEntity<NodeXY[]> GetNodeLLForPath(@PathVariable int pathId) {
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        List<NodeXY> nodeXYs = new ArrayList<>();

        try {
            connection = dbInteractions.getConnectionPool();

            statement = connection.createStatement();

            String selectStatement = "select * from node_ll where node_ll_id in (select node_ll_id from path_node_ll where path_id = ";
            selectStatement += pathId;
            selectStatement += ")";

            rs = statement.executeQuery(selectStatement);

            // convert to ActiveTim object
            while (rs.next()) {
                // note that even though we are working with node-LL type here, the ODE only has
                // a NodeXY object, as the structure is the same.
                NodeXY nodexy = new NodeXY();
                nodexy.setDelta(rs.getString("DELTA"));
                nodexy.setNodeLat(rs.getBigDecimal("NODE_LAT"));
                nodexy.setNodeLong(rs.getBigDecimal("NODE_LONG"));
                nodeXYs.add(nodexy);
            }
            return ResponseEntity.ok(nodeXYs.toArray(new NodeXY[nodeXYs.size()]));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(nodeXYs.toArray(new NodeXY[nodeXYs.size()]));
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
    }
}
