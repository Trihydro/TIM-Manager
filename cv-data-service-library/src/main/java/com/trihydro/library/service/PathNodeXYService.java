package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandler;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.tables.TimOracleTables;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.NodeXY;

public class PathNodeXYService extends CvDataServiceLibrary {

	public static Long insertPathNodeXY(Long nodeXYId, Long pathId) {

		PreparedStatement preparedStatement = null;
		Connection connection = null;

		try {

			connection = DbUtility.getConnectionPool();
			String insertQueryStatement = TimOracleTables.buildInsertQueryStatement("path_node_xy",
					TimOracleTables.getPathNodeXYTable());
			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "path_node_xy_id" });
			int fieldNum = 1;

			for (String col : TimOracleTables.getPathNodeXYTable()) {
				if (col.equals("NODE_XY_ID"))
					SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, nodeXYId);
				else if (col.equals("PATH_ID"))
					SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, pathId);
				fieldNum++;
			}
			// execute insert statement
			Long pathNodeXYId = log(preparedStatement, "pathnodexyid");
			return pathNodeXYId;

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
		return new Long(0);
	}

	public static NodeXY[] GetNodeXYForPath(int pathId){
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		List<NodeXY> nodeXYs = new ArrayList<>();

		try {
			connection = DbUtility.getConnectionPool();

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