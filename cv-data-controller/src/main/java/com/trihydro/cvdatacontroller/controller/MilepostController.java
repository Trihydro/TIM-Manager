package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
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
		boolean exception = false;

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
			exception = true;
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
		if (exception && mileposts.size() == 0) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mileposts);
		}
		return ResponseEntity.ok(mileposts);
	}

	// TODO: after the view updates are completed, we need to update anything with
	// the milepost_vw
	@RequestMapping(method = RequestMethod.GET, value = "/get-milepost-range/{direction}/{fromMilepost}/{toMilepost}/{route}")
	public ResponseEntity<List<Milepost>> getMilepostRange(@PathVariable String direction, @PathVariable String route,
			@PathVariable Double fromMilepost, @PathVariable Double toMilepost) {
		List<Milepost> mileposts = new ArrayList<Milepost>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		boolean exception = false;

		try {

			connection = GetConnectionPool();
			statement = connection.createStatement();

			// build SQL query
			String statementStr = "select * from MILEPOST_VW where direction = '" + direction
					+ "' and milepost between " + Math.min(fromMilepost, toMilepost) + " and "
					+ Math.max(fromMilepost, toMilepost) + " and route like '%" + route + "%'";

			if (fromMilepost < toMilepost)
				rs = statement.executeQuery(statementStr + " order by milepost asc");
			else
				rs = statement.executeQuery(statementStr + " order by milepost desc");

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

			if (mileposts.size() == 0) {
				System.out.println("Unable to find mileposts with query: " + statementStr);
			}
		} catch (SQLException e) {
			exception = true;
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

		if (exception && mileposts.size() == 0) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mileposts);
		}
		return ResponseEntity.ok(mileposts);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/mileposts-route/{route}/{mod}")
	public List<Milepost> getMilepostsRoute(@PathVariable String route, @PathVariable Boolean mod) {
		List<Milepost> mileposts = new ArrayList<Milepost>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			// build statement SQL query
			connection = GetConnectionPool();
			statement = connection.createStatement();

			// build statement SQL query
			String sqlString = "select * from MILEPOST_VW where route like '%" + route + "%'";

			if (mod)
				sqlString += " and MOD(milepost, 1) = 0";

			rs = statement.executeQuery(sqlString);

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

	@RequestMapping(method = RequestMethod.GET, value = "/get-milepost-range-no-direction/{fromMilepost}/{toMilepost}/{route}")
	public List<Milepost> getMilepostRangeNoDirection(@PathVariable String route, @PathVariable Double fromMilepost,
			@PathVariable Double toMilepost) {
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
					+ " and route like '%" + route + "%'";

			if (fromMilepost < toMilepost)
				rs = statement.executeQuery(statementStr + " order by milepost asc");
			else
				rs = statement.executeQuery(statementStr + " order by milepost desc");

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

	@RequestMapping(method = RequestMethod.GET, value = "/get-milepost-test-range/{direction}/{fromMilepost}/{toMilepost}/{route}")
	public List<Milepost> getMilepostTestRange(@PathVariable String direction, @PathVariable String route,
			@PathVariable Double fromMilepost, @PathVariable Double toMilepost) {
		List<Milepost> mileposts = new ArrayList<Milepost>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			connection = GetConnectionPool();
			statement = connection.createStatement();

			// build SQL query
			String statementStr = "select * from MILEPOST_TEST where direction = '" + direction
					+ "' and milepost between " + Math.min(fromMilepost, toMilepost) + " and "
					+ Math.max(fromMilepost, toMilepost) + " and route like '%" + route + "%'";

			if (fromMilepost < toMilepost)
				rs = statement.executeQuery(statementStr + " order by milepost asc");
			else
				rs = statement.executeQuery(statementStr + " order by milepost desc");

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

	@RequestMapping(value = "/mileposts-test", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<Milepost> getMilepostsTest() {
		List<Milepost> mileposts = new ArrayList<Milepost>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			connection = GetConnectionPool();
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
