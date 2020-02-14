package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.IncidentChoice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;

@CrossOrigin
@RestController
@RequestMapping("incident-choice")
@ApiIgnore
public class IncidentChoiceController extends BaseController {

    @RequestMapping(value = "/incident-actions", method = RequestMethod.GET)
    public ResponseEntity<List<IncidentChoice>> SelectAllIncidentActions() {

        List<IncidentChoice> incidentActions = new ArrayList<IncidentChoice>();

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;

        try {

            connection = GetConnectionPool();
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(incidentActions);
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
        return ResponseEntity.ok(incidentActions);
    }

    @RequestMapping(value = "/incident-effects", method = RequestMethod.GET)
    public ResponseEntity<List<IncidentChoice>> SelectAllIncidentEffects() {

        List<IncidentChoice> incidentEffects = new ArrayList<IncidentChoice>();
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            // select all from incident_effect_lut table
            connection = GetConnectionPool();
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(incidentEffects);
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
        return ResponseEntity.ok(incidentEffects);
    }

    @RequestMapping(value = "/incident-problems", method = RequestMethod.GET)
    public ResponseEntity<List<IncidentChoice>> SelectAllIncidentProblems() {
        List<IncidentChoice> incidentProblems = new ArrayList<IncidentChoice>();
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;

        try {

            connection = GetConnectionPool();
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
            return ResponseEntity.ok(incidentProblems);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(incidentProblems);
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
    }
}