package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.ItisCode;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;

@CrossOrigin
@RestController
@ApiIgnore
public class ItisCodeController extends BaseController{
	
	@RequestMapping(value="/itiscodes",method = RequestMethod.GET,headers="Accept=application/json")
  	public ResponseEntity<List<ItisCode>> selectAllItisCodes() throws SQLException { 
		List<ItisCode> itisCodes = new ArrayList<ItisCode>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		try {
			connection = GetConnectionPool();
			// select all Itis Codes from ItisCode table
			statement = connection.createStatement();
			rs = statement.executeQuery("select * from itis_code");
			// convert to ItisCode objects
			while (rs.next()) {
				ItisCode itisCode = new ItisCode();
				itisCode.setItisCodeId(rs.getInt("ITIS_CODE_ID"));
				itisCode.setItisCode(rs.getInt("ITIS_CODE"));
				itisCode.setDescription(rs.getString("DESCRIPTION").toLowerCase());
				itisCode.setCategoryId(rs.getInt("CATEGORY_ID"));
				itisCodes.add(itisCode);
			}
			return ResponseEntity.ok(itisCodes);
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(itisCodes);
		} finally {

			try {
				if (statement != null)
					statement.close();

				if (connection != null)
					connection.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
  	}

}
