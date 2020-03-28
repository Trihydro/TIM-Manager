package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.trihydro.cvdatacontroller.services.MilepostService;
import com.trihydro.library.model.Milepost;
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

			String sqlQuery = "select * from MILEPOST_VW_NEW where MOD(milepost, 1) = 0 order by milepost asc";
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
				return "'I', 'B'";

			case "westward":
				return "'D','B'";

			case "both":
				return "'B'";

			default:
				return direction;
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/route-exists/{route}")
	public ResponseEntity<Boolean> routeExists(@PathVariable String route) {
		Connection connection = null;
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		try {

			connection = GetConnectionPool();

			// build SQL query
			// TODO: this view name might change later
			String statementStr = "select distinct common_name from milepost_vw_new where common_name = ?";
			preparedStatement = connection.prepareStatement(statementStr);
			preparedStatement.setString(1, route);
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

	@RequestMapping(method = RequestMethod.GET, value = "/get-by-common-name/{commonName}/{limit}")
	public ResponseEntity<Collection<com.trihydro.cvdatacontroller.model.Milepost>> getMilepostsByCommonName(
			@PathVariable String commonName, @PathVariable Integer limit) {
		return ResponseEntity.ok(milepostService.getMilepostsByCommonNameWithLimit(commonName, limit));
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

		Collection<com.trihydro.cvdatacontroller.model.Milepost> data = milepostService.getPathWithBuffer(
				wydotTim.getRoute(), wydotTim.getStartPoint().getLatitude(), wydotTim.getStartPoint().getLongitude(),
				wydotTim.getEndPoint().getLatitude(), wydotTim.getEndPoint().getLongitude(), wydotTim.getDirection());
		return ResponseEntity.ok(data);
	}
}
