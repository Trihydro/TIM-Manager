package com.trihydro.library.service;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.service.CvDataServiceLibrary;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;
import org.springframework.stereotype.Component;

@Component
public class RsuService extends CvDataServiceLibrary {

	public static ArrayList<WydotRsu> selectAll() {

		ArrayList<WydotRsu> rsus = new ArrayList<WydotRsu>();
		Connection connection = null;
		ResultSet rs = null;
		Statement statement = null;

		try {
			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();

			// select all RSUs from RSU table
			rs = statement.executeQuery(
					"select * from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid order by milepost asc");

			while (rs.next()) {
				WydotRsu rsu = new WydotRsu();
				rsu.setRsuId(rs.getInt("rsu_id"));
				rsu.setRsuTarget(rs.getString("ipv4_address"));
				rsu.setLatitude(rs.getDouble("latitude"));
				rsu.setLongitude(rs.getDouble("longitude"));
				rsu.setRoute(rs.getString("route"));
				rsu.setMilepost(rs.getDouble("milepost"));
				rsus.add(rsu);
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

		return rsus;
	}

	public static ArrayList<WydotRsu> selectRsusByRoute(String route) {

		ArrayList<WydotRsu> rsus = new ArrayList<WydotRsu>();
		Connection connection = null;
		ResultSet rs = null;
		Statement statement = null;

		try {
			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();

			// select all RSUs from RSU table
			rs = statement.executeQuery(
					"select * from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid where rsu_vw.route like '%"
							+ route + "%' order by milepost asc");

			while (rs.next()) {
				WydotRsu rsu = new WydotRsu();
				rsu.setRsuId(rs.getInt("rsu_id"));
				rsu.setRsuTarget(rs.getString("ipv4_address"));
				rsu.setLatitude(rs.getDouble("latitude"));
				rsu.setLongitude(rs.getDouble("longitude"));
				rsu.setRoute(rs.getString("route"));
				rsu.setMilepost(rs.getDouble("milepost"));
				rsus.add(rsu);
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
		return rsus;
	}

	public static List<WydotRsu> selectActiveRSUs() {
		List<WydotRsu> rsus = new ArrayList<WydotRsu>();
		Connection connection = null;
		ResultSet rs = null;
		Statement statement = null;

		try {
			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();

			// select all RSUs that are labeled as 'Existing' in the WYDOT view
			rs = statement.executeQuery(
					"select rsu.*, rsu_vw.latitude, rsu_vw.longitude, rsu_vw.ipv4_address from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid where rsu_vw.status = 'Existing'");

			while (rs.next()) {
				WydotRsu rsu = new WydotRsu();
				// rsu.setRsuId(rs.getInt("rsu_id"));
				rsu.setRsuTarget(rs.getString("ipv4_address"));
				rsu.setLatitude(rs.getDouble("latitude"));
				rsu.setLongitude(rs.getDouble("longitude"));
				rsus.add(rsu);
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
		return rsus;
	}

	public static List<WydotRsu> selectRsusInBuffer(String direction, Double lowerMilepost, Double higherMilepost) {

		Connection connection = null;
		ResultSet rs = null;
		Statement statement = null;
		List<WydotRsu> rsus = new ArrayList<WydotRsu>();
		int buffer = 5;

		try {
			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();

			if (direction.toLowerCase().equals("eastbound")) {
				Double startBuffer = lowerMilepost - buffer;
				rs = statement.executeQuery(
						"select rsu.*, rsu_vw.latitude, rsu_vw.longitude, rsu_vw.ipv4_address from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid where rsu_vw.status = 'Existing' and rsu_vw.milepost >= "
								+ startBuffer + " and rsu_vw.milepost <= " + higherMilepost
								+ " and rsu_vw.route like '%80%'");
			} else {
				Double startBuffer = higherMilepost + buffer;
				rs = statement.executeQuery(
						"select rsu.*, rsu_vw.latitude, rsu_vw.longitude, rsu_vw.ipv4_address from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid where rsu_vw.status = 'Existing' and rsu_vw.milepost >= "
								+ lowerMilepost + "and rsu_vw.milepost <= " + startBuffer
								+ " and rsu_vw.route like '%80%'");
			}

			while (rs.next()) {
				WydotRsu rsu = new WydotRsu();
				rsu.setRsuId(rs.getInt("rsu_id"));
				rsu.setRsuTarget(rs.getString("ipv4_address"));
				rsu.setLatitude(rs.getDouble("latitude"));
				rsu.setLongitude(rs.getDouble("longitude"));
				rsus.add(rsu);
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
		return rsus;
	}
}