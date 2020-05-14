package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.trihydro.cvdatacontroller.services.MilepostService;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.MilepostBuffer;
import com.trihydro.library.model.WydotTim;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;

@CrossOrigin
@RestController
@ApiIgnore
public class MilepostController extends BaseController {

	private MilepostService milepostService;

	@Autowired
	public void InjectDependencies(MilepostService _milepostService) {
		this.milepostService = _milepostService;
	}

	@RequestMapping(value = "/routes", method = RequestMethod.GET)
	public ResponseEntity<List<String>> getRoutes() {
		Connection connection = null;
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		List<String> routes = new ArrayList<>();
		try {

			connection = dbInteractions.getConnectionPool();

			// build SQL query
			String statementStr = "select distinct common_name from MILEPOST_VW_NEW";
			preparedStatement = connection.prepareStatement(statementStr);
			rs = preparedStatement.executeQuery();

			while (rs.next()) {
				routes.add(rs.getString("COMMON_NAME"));
			}
			return ResponseEntity.ok(routes);
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(routes);
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

	/**
	 * Fetch mileposts from the view by their common name. Used for TimCreator tool
	 * 
	 * @param commonName
	 * @param mod
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/mileposts-common-name/{commonName}/{mod}")
	public ResponseEntity<List<Milepost>> getMilepostsCommonName(@PathVariable String commonName,
			@PathVariable Boolean mod) {
		List<Milepost> mileposts = new ArrayList<Milepost>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			// build statement SQL query
			connection = dbInteractions.getConnectionPool();
			statement = connection.createStatement();

			// build statement SQL query
			String sqlString = "select * from MILEPOST_VW_NEW where COMMON_NAME = '" + commonName + "'";

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

	// the milepost_vw
	@RequestMapping(method = RequestMethod.GET, value = "/get-milepost-range/{direction}/{fromMilepost}/{toMilepost}/{commonName}")
	public ResponseEntity<List<Milepost>> getMilepostRange(@PathVariable String direction,
			@PathVariable String commonName, @PathVariable Double fromMilepost, @PathVariable Double toMilepost) {
		List<Milepost> mileposts = new ArrayList<Milepost>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			connection = dbInteractions.getConnectionPool();
			statement = connection.createStatement();

			// build SQL query
			String statementStr = "select * from MILEPOST_VW_NEW where direction = '" + translateDirection(direction)
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

	@RequestMapping(method = RequestMethod.GET, value = "/get-milepost-range-no-direction/{fromMilepost}/{toMilepost}/{commonName}")
	public ResponseEntity<List<Milepost>> getMilepostRangeNoDirection(@PathVariable String commonName,
			@PathVariable Double fromMilepost, @PathVariable Double toMilepost) {
		List<Milepost> mileposts = new ArrayList<Milepost>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			connection = dbInteractions.getConnectionPool();
			statement = connection.createStatement();

			// build SQL query
			String statementStr = "select * from MILEPOST_VW_NEW where milepost between "
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
			case "northbound":
			case "eastbound":
			case "eastward":
				return "I";

			case "southbound":
			case "westbound":
			case "westward":
				return "D";

			case "both":
				return "B";

			default:
				return direction.toUpperCase();
		}
	}

	/**
	 * Gets a collection of Mileposts between a start and end point, along the given
	 * route. Includes a buffer point ahead of start point as an anchor
	 * 
	 * @param wydotTim
	 * @return Collection of Milepost objects representing the path
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/get-milepost-start-end")
	public ResponseEntity<Collection<com.trihydro.cvdatacontroller.model.Milepost>> getMilepostsByStartEndPoint(
			@RequestBody WydotTim wydotTim) {

		// check startPoint
		if (wydotTim.getStartPoint() == null || wydotTim.getStartPoint().getLatitude() == null
				|| wydotTim.getStartPoint().getLongitude() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}

		// check endpoint
		if (wydotTim.getEndPoint() == null || wydotTim.getEndPoint().getLatitude() == null
				|| wydotTim.getEndPoint().getLongitude() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}

		// check direction, route
		if (wydotTim.getDirection() == null || wydotTim.getRoute() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}

		Collection<com.trihydro.cvdatacontroller.model.Milepost> data = milepostService.getPathWithBuffer(
				wydotTim.getRoute(), wydotTim.getStartPoint().getLatitude(), wydotTim.getStartPoint().getLongitude(),
				wydotTim.getEndPoint().getLatitude(), wydotTim.getEndPoint().getLongitude(), wydotTim.getDirection());
		return ResponseEntity.ok(data);
	}

	/**
	 * Rewrite of getMilepostsByStartEndPoint used in testing to cut time on geojson
	 * creation to test continuity
	 * 
	 * @param wydotTim
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/get-feature-collection")
	public ResponseEntity<String> getMilepostsFeatureCollectionByStartEndPoint(@RequestBody WydotTim wydotTim) {

		// check startPoint
		if (wydotTim.getStartPoint() == null || wydotTim.getStartPoint().getLatitude() == null
				|| wydotTim.getStartPoint().getLongitude() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}

		// check endpoint
		if (wydotTim.getEndPoint() == null || wydotTim.getEndPoint().getLatitude() == null
				|| wydotTim.getEndPoint().getLongitude() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}

		// check direction, route
		if (wydotTim.getDirection() == null || wydotTim.getRoute() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}

		Collection<com.trihydro.cvdatacontroller.model.Milepost> data = milepostService.getPathWithBuffer(
				wydotTim.getRoute(), wydotTim.getStartPoint().getLatitude(), wydotTim.getStartPoint().getLongitude(),
				wydotTim.getEndPoint().getLatitude(), wydotTim.getEndPoint().getLongitude(), wydotTim.getDirection());

		List<Feature> features = new ArrayList<>();
		var coordinates = data.stream().map(x -> Position.fromCoordinates(x.getLongitude(), x.getLatitude()))
				.collect(Collectors.toList());
		LineString ls = LineString.fromCoordinates(coordinates);
		var feature = Feature.fromGeometry(ls);
		features.add(feature);
		FeatureCollection fc = FeatureCollection.fromFeatures(features);

		return ResponseEntity.ok(fc.toJson());
	}

	@RequestMapping(method = RequestMethod.POST, value = "/get-milepost-single-point")
	public ResponseEntity<Collection<com.trihydro.cvdatacontroller.model.Milepost>> getMilepostsByPointWithBuffer(
			@RequestBody MilepostBuffer milepostBuffer) {
		// check startPoint
		if (milepostBuffer.getPoint() == null || milepostBuffer.getPoint().getLatitude() == null
				|| milepostBuffer.getPoint().getLongitude() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}

		// check direction, route
		if (milepostBuffer.getDirection() == null || milepostBuffer.getCommonName() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}

		Collection<com.trihydro.cvdatacontroller.model.Milepost> data = milepostService.getPathWithSpecifiedBuffer(
				milepostBuffer.getCommonName(), milepostBuffer.getPoint().getLatitude(),
				milepostBuffer.getPoint().getLongitude(), milepostBuffer.getDirection(),
				milepostBuffer.getBufferMiles());
		return ResponseEntity.ok(data);
	}

	/**
	 * Needed for TIM Creator
	 * 
	 * @return
	 */
	@RequestMapping(value = "/mileposts-test", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<Milepost> getMilepostsTest() {

		List<Milepost> mileposts = new ArrayList<Milepost>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = dbInteractions.getConnectionPool();
			statement = connection.createStatement();
			rs = statement.executeQuery("select * from MILEPOST_TEST order by milepost asc");

			// convert result to milepost objects
			while (rs.next()) {
				Milepost milepost = new Milepost();
				// milepost.setMilepostId(rs.getInt("milepost_id"));
				milepost.setCommonName(rs.getString("route"));
				milepost.setMilepost(rs.getDouble("milepost"));
				milepost.setDirection(rs.getString("direction"));
				milepost.setLatitude(rs.getDouble("latitude"));
				milepost.setLongitude(rs.getDouble("longitude"));
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

	/**
	 * Needed for TIM Creator
	 * 
	 * @param direction
	 * @param route
	 * @param start
	 * @param end
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/get-milepost-test-range/{direction}/{start}/{end}/{route}")
	public List<Milepost> getMilepostTestRange(@PathVariable String direction, @PathVariable String route,
			@PathVariable Double start, @PathVariable Double end) {
		List<Milepost> mileposts = new ArrayList<Milepost>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			connection = dbInteractions.getConnectionPool();
			statement = connection.createStatement();

			// build SQL query
			String statementStr = "select * from MILEPOST_TEST where direction = '" + direction
					+ "' and milepost between " + Math.min(start, end) + " and " + Math.max(start, end)
					+ " and route like '%" + route + "%'";

			if (start < end)
				rs = statement.executeQuery(statementStr + "order by milepost asc");
			else
				rs = statement.executeQuery(statementStr + "order by milepost desc");

			// convert result to milepost objects
			while (rs.next()) {
				Milepost milepost = new Milepost();
				milepost.setCommonName(rs.getString("route"));
				milepost.setMilepost(rs.getDouble("milepost"));
				milepost.setDirection(rs.getString("direction"));
				milepost.setLatitude(rs.getDouble("latitude"));
				milepost.setLongitude(rs.getDouble("longitude"));
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
