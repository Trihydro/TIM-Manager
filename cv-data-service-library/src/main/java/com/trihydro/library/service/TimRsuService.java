package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.model.TimRsu;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class TimRsuService extends CvDataServiceLibrary {

	public static Long insertTimRsu(Long timId, Integer rsuId, Integer rsuIndex) {
		String url = String.format("%s/tim-rsu/add-tim-rsu/%d/%d/%d", CVRestUrl, timId, rsuId, rsuIndex);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<Long> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.POST, entity,
				Long.class);
		return response.getBody();
	}

	public static List<TimRsu> getTimRsusByTimId(Long timId) {

		Statement statement = null;
		Connection connection = null;
		ResultSet rs = null;
		List<TimRsu> timRsus = new ArrayList<TimRsu>();

		try {

			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();
			// build SQL statement
			rs = statement.executeQuery("select * from TIM_RSU where tim_id = " + timId);

			// convert to DriverAlertType objects
			while (rs.next()) {
				TimRsu timRsu = new TimRsu();
				timRsu.setTimId(rs.getLong("TIM_ID"));
				timRsu.setRsuId(rs.getLong("RSU_ID"));
				timRsu.setRsuIndex(rs.getInt("RSU_INDEX"));
				timRsus.add(timRsu);
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
		return timRsus;
	}

	public static List<TimRsu> selectAll() {

		Statement statement = null;
		Connection connection = null;
		ResultSet rs = null;
		List<TimRsu> timRsus = new ArrayList<TimRsu>();

		try {

			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();
			// select all RSUs from RSU table
			rs = statement.executeQuery("select * from TIM_RSU");

			while (rs.next()) {
				TimRsu timRsu = new TimRsu();
				timRsu.setTimId(rs.getLong("TIM_ID"));
				timRsu.setRsuId(rs.getLong("RSU_ID"));
				timRsus.add(timRsu);
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
		return timRsus;
	}

	public static TimRsu getTimRsu(Long timId, Integer rsuId) {

		Statement statement = null;
		Connection connection = null;
		ResultSet rs = null;
		TimRsu timRsu = new TimRsu();

		try {
			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();
			// build SQL statement
			rs = statement.executeQuery("select * from TIM_RSU where rsu_id = " + rsuId + " and tim_id = " + timId);

			// convert to DriverAlertType objects
			while (rs.next()) {
				timRsu.setTimRsuId(rs.getLong("TIM_RSU_ID"));
				timRsu.setTimId(rs.getLong("TIM_ID"));
				timRsu.setRsuId(rs.getLong("RSU_ID"));
				timRsu.setRsuIndex(rs.getInt("RSU_INDEX"));
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
		return timRsu;
	}

	public static boolean deleteTimRsu(Long timRsuId) {

		boolean deleteTimRsuResult = false;
		PreparedStatement preparedStatement = null;
		Connection connection = null;

		String deleteSQL = "DELETE FROM TIM_RSU WHERE TIM_RSU_ID = ?";

		try {
			connection = DbUtility.getConnectionPool();
			preparedStatement = connection.prepareStatement(deleteSQL);
			preparedStatement.setLong(1, timRsuId);

			// execute delete SQL stetement
			deleteTimRsuResult = updateOrDelete(preparedStatement);

			System.out.println("TIM RSU is deleted!");

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
		return deleteTimRsuResult;
	}
}
