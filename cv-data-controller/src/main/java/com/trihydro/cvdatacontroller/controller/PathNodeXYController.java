package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimDbTables;

import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("path-node-xy")
@ApiIgnore
public class PathNodeXYController extends BaseController {

	private TimDbTables timDbTables;
	private SQLNullHandler sqlNullHandler;

	@Autowired
	public void InjectDependencies(TimDbTables _timDbTables, SQLNullHandler _sqlNullHandler) {
		timDbTables = _timDbTables;
		sqlNullHandler = _sqlNullHandler;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/get-nodexy-path/{pathId}")
	public ResponseEntity<NodeXY[]> GetNodeXYForPath(@PathVariable int pathId) {
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		List<NodeXY> nodeXYs = new ArrayList<>();

		try {
			connection = dbInteractions.getConnectionPool();

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
				nodexy.setXpos(rs.getBigDecimal("X"));
				nodexy.setYpos(rs.getBigDecimal("Y"));

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

	@RequestMapping(method = RequestMethod.POST, value = "add-path-nodexy/{nodeXYId}/{pathId}")
	public ResponseEntity<Long> AddPathNodeXY(@PathVariable Long nodeXYId, @PathVariable Long pathId) {

		PreparedStatement preparedStatement = null;
		Connection connection = null;

		try {

			connection = dbInteractions.getConnectionPool();
			String insertQueryStatement = timDbTables.buildInsertQueryStatement("path_node_xy",
					timDbTables.getPathNodeXYTable());
			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "path_node_xy_id" });
			int fieldNum = 1;

			for (String col : timDbTables.getPathNodeXYTable()) {
				if (col.equals("NODE_XY_ID"))
					sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, nodeXYId);
				else if (col.equals("PATH_ID"))
					sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, pathId);
				fieldNum++;
			}
			// execute insert statement
			Long pathNodeXYId = dbInteractions.executeAndLog(preparedStatement, "pathnodexyid");
			return ResponseEntity.ok(pathNodeXYId);

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