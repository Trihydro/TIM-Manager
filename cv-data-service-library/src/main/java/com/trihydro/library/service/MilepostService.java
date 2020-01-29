package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.model.Milepost;

import org.springframework.http.ResponseEntity;

public class MilepostService extends CvDataServiceLibrary {

	// select all mileposts
	public static List<Milepost> selectAll() {
		String url = String.format("/%s/mileposts", CVRestUrl);
		ResponseEntity<Milepost[]> response = RestTemplateProvider.GetRestTemplate().getForEntity(url,
				Milepost[].class);
		return Arrays.asList(response.getBody());
	}

	public static List<Milepost> getMilepostsRoute(String route, Boolean mod) {
		String url = String.format("/%s/mileposts-route/%s/%b", CVRestUrl, route, mod);
		ResponseEntity<Milepost[]> response = RestTemplateProvider.GetRestTemplate().getForEntity(url,
				Milepost[].class);
		return Arrays.asList(response.getBody());
	}

	/**
	 * Calls out to the cv-data-controller REST service to select all mileposts
	 * within a range in one direction
	 * 
	 * @param direction
	 * @param route
	 * @param fromMilepost
	 * @param toMilepost
	 * @return Mileposts found within range
	 */
	public static List<Milepost> selectMilepostRange(String direction, String route, Double fromMilepost,
			Double toMilepost) {
		String url = String.format("/%s/get-milepost-range/%s/%d/%d/%s", CVRestUrl, direction, fromMilepost, toMilepost,
				route);
		ResponseEntity<Milepost[]> response = RestTemplateProvider.GetRestTemplate().getForEntity(url,
				Milepost[].class);
		return Arrays.asList(response.getBody());
	}

	// select all mileposts within a range in one direction
	public static List<Milepost> selectMilepostTestRange(String direction, String route, Double fromMilepost,
			Double toMilepost) {

		List<Milepost> mileposts = new ArrayList<Milepost>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();

			// build SQL query
			String statementStr = "select * from MILEPOST_TEST where direction = '" + direction
					+ "' and milepost between " + Math.min(fromMilepost, toMilepost) + " and "
					+ Math.max(fromMilepost, toMilepost) + " and route like '%" + route + "%'";

			if (fromMilepost < toMilepost)
				rs = statement.executeQuery(statementStr + "order by milepost asc");
			else
				rs = statement.executeQuery(statementStr + "order by milepost desc");

			// convert result to milepost objects
			while (rs.next()) {
				Milepost milepost = new Milepost();
				milepost.setRoute(rs.getString("route"));
				milepost.setMilepost(rs.getDouble("milepost"));
				milepost.setDirection(rs.getString("direction"));
				milepost.setLatitude(rs.getDouble("latitude"));
				milepost.setLongitude(rs.getDouble("longitude"));
				milepost.setElevation(rs.getDouble("elevation_ft"));
				milepost.setBearing(rs.getDouble("bearing"));
				mileposts.add(milepost);
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
		return mileposts;
	}

	// select all mileposts within a range in one direction
	public static List<Milepost> selectMilepostRangeNoDirection(String route, Double fromMilepost, Double toMilepost) {

		List<Milepost> mileposts = new ArrayList<Milepost>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();

			// build SQL query
			String statementStr = "select * from MILEPOST_VW where milepost between "
					+ Math.min(fromMilepost, toMilepost) + " and " + Math.max(fromMilepost, toMilepost)
					+ " and route like '%" + route + "%'";

			rs = statement.executeQuery(statementStr + "order by milepost asc");

			if (fromMilepost < toMilepost)
				rs = statement.executeQuery(statementStr + "order by milepost asc");
			else
				rs = statement.executeQuery(statementStr + "order by milepost desc");

			// convert result to milepost objects
			while (rs.next()) {
				Milepost milepost = new Milepost();
				milepost.setRoute(rs.getString("route"));
				milepost.setMilepost(rs.getDouble("milepost"));
				milepost.setLatitude(rs.getDouble("latitude"));
				milepost.setLongitude(rs.getDouble("longitude"));
				milepost.setElevation(rs.getDouble("elevation_ft"));
				milepost.setBearing(rs.getDouble("bearing"));
				mileposts.add(milepost);
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
		return mileposts;
	}

	// select all mileposts
	public static List<Milepost> selectAllTest() {

		List<Milepost> mileposts = new ArrayList<Milepost>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();
			rs = statement.executeQuery("select * from MILEPOST_TEST order by milepost asc");

			// convert result to milepost objects
			while (rs.next()) {
				Milepost milepost = new Milepost();
				// milepost.setMilepostId(rs.getInt("milepost_id"));
				milepost.setRoute(rs.getString("route"));
				milepost.setMilepost(rs.getDouble("milepost"));
				milepost.setDirection(rs.getString("direction"));
				milepost.setLatitude(rs.getDouble("latitude"));
				milepost.setLongitude(rs.getDouble("longitude"));
				milepost.setElevation(rs.getDouble("elevation_ft"));
				milepost.setBearing(rs.getDouble("bearing"));
				mileposts.add(milepost);
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
		return mileposts;
	}
}
