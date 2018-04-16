package com.trihydro.library.service;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.model.IncidentChoice;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;
import com.trihydro.library.service.CvDataServiceLibrary;

import java.sql.ResultSet;

public class IncidentChoicesService extends CvDataServiceLibrary {

	public static List<IncidentChoice> selectAllIncidentActions(){
		List<IncidentChoice> incidentActions = new ArrayList<IncidentChoice>();
		try (Statement statement = DbUtility.getConnection().createStatement()) {
			// select all from incident_action_lut table   			   		    
			ResultSet rs = statement.executeQuery("select * from incident_action_lut");
			try {
				// convert to IncidentChoice objects   			
				while (rs.next()) {   			
					IncidentChoice incidentAction = new IncidentChoice();
					incidentAction.setItisCodeId(rs.getInt("itis_code_id"));
					incidentAction.setDescription(rs.getString("description"));
					incidentAction.setCode(rs.getString("code"));    
					incidentActions.add(incidentAction);
				}
			}
			finally {
				try { rs.close(); } catch (Exception ignore) { }
			}
  		} 
  		catch (SQLException e) {
   			e.printStackTrace();
  		}
  		return incidentActions;
    }
    
    public static List<IncidentChoice> selectAllIncidentEffects(){
		List<IncidentChoice> incidentEffects = new ArrayList<IncidentChoice>();
		try (Statement statement = DbUtility.getConnection().createStatement()) {
			// select all from incident_effect_lut table   			   		    
			ResultSet rs = statement.executeQuery("select * from incident_effect_lut");
			try {
				// convert to IncidentChoice objects   			
				while (rs.next()) {   			
					IncidentChoice incidentEffect = new IncidentChoice();
					incidentEffect.setItisCodeId(rs.getInt("itis_code_id"));
					incidentEffect.setDescription(rs.getString("description"));
					incidentEffect.setCode(rs.getString("code"));    
					incidentEffects.add(incidentEffect);
				}
			}
			finally {
				try { rs.close(); } catch (Exception ignore) { }
			}
  		} 
  		catch (SQLException e) {
   			e.printStackTrace();
  		}
  		return incidentEffects;
    }
    
    public static List<IncidentChoice> selectAllIncidentProblems(){
		List<IncidentChoice> incidentProblems = new ArrayList<IncidentChoice>();
		try (Statement statement = DbUtility.getConnection().createStatement()) {
			// select all from incident_problem_lut table   			   		    
			ResultSet rs = statement.executeQuery("select * from incident_problem_lut");
			try {
				// convert to IncidentChoice objects   			
				while (rs.next()) {   			
					IncidentChoice incidentProblem = new IncidentChoice();
					incidentProblem.setItisCodeId(rs.getInt("itis_code_id"));
					incidentProblem.setDescription(rs.getString("description"));
					incidentProblem.setCode(rs.getString("code"));    
					incidentProblems.add(incidentProblem);
				}
			}
			finally {
				try { rs.close(); } catch (Exception ignore) { }
			}
  		} 
  		catch (SQLException e) {
   			e.printStackTrace();
  		}
  		return incidentProblems;
	}

}