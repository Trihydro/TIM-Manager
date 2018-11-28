package com.trihydro.library.service;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.model.IncidentChoice;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;
import com.trihydro.library.service.CvDataServiceLibrary;

import java.sql.Connection;
import java.sql.ResultSet;

public class IncidentChoicesService extends CvDataServiceLibrary {

	public static List<IncidentChoice> selectAllIncidentActions() {

		List<IncidentChoice> incidentActions = new ArrayList<IncidentChoice>();

		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();

			// select all from incident_action_lut table
			rs = statement.executeQuery("select * from incident_action_lut");

			// convert to IncidentChoice objects
			while (rs.next()) {
				IncidentChoice incidentAction = new IncidentChoice();
				incidentAction.setItisCodeId(rs.getInt("itis_code_id"));
				incidentAction.setDescription(rs.getString("description"));
				incidentAction.setCode(rs.getString("code"));
				incidentActions.add(incidentAction);
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
		return incidentActions;
	}

	public static List<IncidentChoice> selectAllIncidentEffects() {

		List<IncidentChoice> incidentEffects = new ArrayList<IncidentChoice>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			// select all from incident_effect_lut table
			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();
			rs = statement.executeQuery("select * from incident_effect_lut");

			// convert to IncidentChoice objects
			while (rs.next()) {
				IncidentChoice incidentEffect = new IncidentChoice();
				incidentEffect.setItisCodeId(rs.getInt("itis_code_id"));
				incidentEffect.setDescription(rs.getString("description"));
				incidentEffect.setCode(rs.getString("code"));
				incidentEffects.add(incidentEffect);
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
		return incidentEffects;
	}

	public static List<IncidentChoice> selectAllIncidentProblems() {

		List<IncidentChoice> incidentProblems = new ArrayList<IncidentChoice>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();

			// select all from incident_problem_lut table
			rs = statement.executeQuery("select * from incident_problem_lut");

			// convert to IncidentChoice objects
			while (rs.next()) {
				IncidentChoice incidentProblem = new IncidentChoice();
				incidentProblem.setItisCodeId(rs.getInt("itis_code_id"));
				incidentProblem.setDescription(rs.getString("description"));
				incidentProblem.setCode(rs.getString("code"));
				incidentProblems.add(incidentProblem);
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
		return incidentProblems;
	}
}