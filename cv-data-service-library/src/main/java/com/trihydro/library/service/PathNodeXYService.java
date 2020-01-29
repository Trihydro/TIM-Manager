package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimOracleTables;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

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

	public static NodeXY[] GetNodeXYForPath(int pathId) {
		String url = String.format("/%s/path-node-xy/get-nodexy-path/%d", CVRestUrl, pathId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<NodeXY[]> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.PUT, entity,
				NodeXY[].class);
		return response.getBody();
	}
}