package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.Milepost;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;

@CrossOrigin
@RestController
@ApiIgnore
public class MilepostController extends BaseController {

	@RequestMapping(value = "/mileposts", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<List<Milepost>> getMileposts() {
		List<Milepost> mileposts = new ArrayList<Milepost>();

		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			// build statement SQL query
			connection = GetConnectionPool();
			statement = connection.createStatement();

			String sqlQuery = "select * from MILEPOST_VW where MOD(milepost, 1) = 0 order by milepost asc";
			rs = statement.executeQuery(sqlQuery);

			// convert result to milepost objects
			while (rs.next()) {
				Milepost milepost = new Milepost();
				// milepost.setMilepostId(rs.getInt("milepost_id"));
				milepost.setCommonName(rs.getString("COMMON_NAME"));
				milepost.setMilepost(rs.getDouble("MILEPOST"));
				milepost.setDirection(rs.getString("DIRECTION"));
				milepost.setLatitude(rs.getDouble("LATITUDE"));
				milepost.setLongitude(rs.getDouble("LONGITUDE"));
				// milepost.setBearing(rs.getDouble("BEARING"));
				mileposts.add(milepost);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mileposts);
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
		return ResponseEntity.ok(mileposts);
	}

	private String translateDirection(String direction) {
		switch (direction.toLowerCase()) {
			case "eastward":
				return "I";

			case "westward":
				return "D";

			case "both":
				return "B";

			default:
				return direction;
		}
	}

	// the milepost_vw
	@RequestMapping(method = RequestMethod.GET, value = "/get-milepost-range/{direction}/{fromMilepost}/{toMilepost}/{commonName}")
	public ResponseEntity<List<Milepost>> getMilepostRange(@PathVariable String direction,
			@PathVariable String commonName, @PathVariable Double fromMilepost, @PathVariable Double toMilepost) {
		List<Milepost> mileposts = new ArrayList<Milepost>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			connection = GetConnectionPool();
			statement = connection.createStatement();

			// build SQL query
			String statementStr = "select * from MILEPOST_VW where direction = '" + translateDirection(direction)
					+ "' and milepost between " + Math.min(fromMilepost, toMilepost) + " and "
					+ Math.max(fromMilepost, toMilepost) + " and common_name = '" + commonName + "'";

			if (fromMilepost < toMilepost)
				rs = statement.executeQuery(statementStr + " order by milepost asc");
			else
				rs = statement.executeQuery(statementStr + " order by milepost desc");

			// convert result to milepost objects
			while (rs.next()) {
				Milepost milepost = new Milepost();
				milepost.setCommonName(rs.getString("COMMON_NAME"));
				milepost.setMilepost(rs.getDouble("MILEPOST"));
				milepost.setDirection(rs.getString("DIRECTION"));
				milepost.setLatitude(rs.getDouble("LATITUDE"));
				milepost.setLongitude(rs.getDouble("LONGITUDE"));
				// milepost.setBearing(rs.getDouble("BEARING"));
				mileposts.add(milepost);
			}

			if (mileposts.size() == 0) {
				System.out.println("Unable to find mileposts with query: " + statementStr);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mileposts);
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
		return ResponseEntity.ok(mileposts);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/mileposts-common-name/{commonName}/{mod}")
	public ResponseEntity<List<Milepost>> getMilepostsCommonName(@PathVariable String commonName,
			@PathVariable Boolean mod) {
		List<Milepost> mileposts = new ArrayList<Milepost>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			// build statement SQL query
			connection = GetConnectionPool();
			statement = connection.createStatement();

			// build statement SQL query
			String sqlString = "select * from MILEPOST_VW where COMMON_NAME = '" + commonName + "'";

			if (mod)
				sqlString += " and MOD(milepost, 1) = 0";

			rs = statement.executeQuery(sqlString);

			// convert result to milepost objects
			while (rs.next()) {
				Milepost milepost = new Milepost();
				milepost.setCommonName(rs.getString("COMMON_NAME"));
				milepost.setMilepost(rs.getDouble("MILEPOST"));
				milepost.setDirection(rs.getString("DIRECTION"));
				milepost.setLatitude(rs.getDouble("LATITUDE"));
				milepost.setLongitude(rs.getDouble("LONGITUDE"));
				// milepost.setBearing(rs.getDouble("BEARING"));
				mileposts.add(milepost);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mileposts);
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
		return ResponseEntity.ok(mileposts);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/get-milepost-range-no-direction/{fromMilepost}/{toMilepost}/{commonName}")
	public ResponseEntity<List<Milepost>> getMilepostRangeNoDirection(@PathVariable String commonName,
			@PathVariable Double fromMilepost, @PathVariable Double toMilepost) {
		List<Milepost> mileposts = new ArrayList<Milepost>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			connection = GetConnectionPool();
			statement = connection.createStatement();

			// build SQL query
			String statementStr = "select * from MILEPOST_VW where milepost between "
					+ Math.min(fromMilepost, toMilepost) + " and " + Math.max(fromMilepost, toMilepost)
					+ " and common_name = '" + commonName + "'";

			if (fromMilepost < toMilepost)
				rs = statement.executeQuery(statementStr + " order by milepost asc");
			else
				rs = statement.executeQuery(statementStr + " order by milepost desc");

			// convert result to milepost objects
			while (rs.next()) {
				Milepost milepost = new Milepost();
				milepost.setCommonName(rs.getString("COMMON_NAME"));
				milepost.setMilepost(rs.getDouble("MILEPOST"));
				milepost.setLatitude(rs.getDouble("LATITUDE"));
				milepost.setLongitude(rs.getDouble("LONGITUDE"));
				// milepost.setBearing(rs.getDouble("BEARING"));
				mileposts.add(milepost);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mileposts);
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
		return ResponseEntity.ok(mileposts);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/route-exists/{route}")
	public ResponseEntity<Boolean> routeExists(@PathVariable String route) {
		Connection connection = null;
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		try {

			connection = GetConnectionPool();

			// build SQL query
			String statementStr = "select distinct common_name from milepost_vw where common_name = '?'";
			preparedStatement = connection.prepareStatement(statementStr);
			preparedStatement.setString(0, route);
			rs = preparedStatement.executeQuery();

			return ResponseEntity.ok(rs.next());
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
		} finally {
			try {
				// close prepared statement
				if (preparedStatement != null)
					preparedStatement.close();
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
