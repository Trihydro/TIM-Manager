package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.NodeXY;

@CrossOrigin
@RestController
@RequestMapping("path-node-xy")
@ApiIgnore
public class PathNodeXYController extends BaseController {

    @RequestMapping(method = RequestMethod.GET, value = "/get-nodexy-path/{pathId}")
    public NodeXY[] GetNodeXYForPath(@PathVariable int pathId){
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		List<NodeXY> nodeXYs = new ArrayList<>();

		try {
			connection = GetConnectionPool();

			statement = connection.createStatement();

			String selectStatement = "select * from node_xy where node_xy_id in (select node_xy_id from path_node_xy where path_id = ";
			selectStatement += pathId;
			selectStatement += ")";

			rs = statement.executeQuery(selectStatement);

			// convert to ActiveTim object
			while (rs.next()) {
				NodeXY nodexy = new NodeXY();
				nodexy.setDelta(rs.getString("DELTA"));
				nodexy.setNodeLat(rs.getBigDecimal("NODE_LAT"));
				nodexy.setNodeLong(rs.getBigDecimal("NODE_LONG"));
				nodexy.setX(rs.getBigDecimal("X"));
				nodexy.setY(rs.getBigDecimal("Y"));

				nodeXYs.add(nodexy);
			}
		} catch (Exception e) {
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

		return nodeXYs.toArray(new NodeXY[nodeXYs.size()]);
	}
}