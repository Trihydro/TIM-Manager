package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.SecurityResultCodeType;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("security-result-code-type")
public class SecurityResultCodeTypeController extends BaseController {
	@RequestMapping(value = "/get-all", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<List<SecurityResultCodeType>> GetSecurityResultCodeTypes() {

		SecurityResultCodeType securityResultCodeType = null;
		List<SecurityResultCodeType> securityResultCodeTypes = new ArrayList<SecurityResultCodeType>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			connection = GetConnectionPool();
			statement = connection.createStatement();
			rs = statement.executeQuery("select * from SECURITY_RESULT_CODE_TYPE");

			// convert to ActiveTim object
			while (rs.next()) {
				securityResultCodeType = new SecurityResultCodeType();
				securityResultCodeType.setSecurityResultCodeTypeId(rs.getInt("SECURITY_RESULT_CODE_TYPE_ID"));
				securityResultCodeType.setSecurityResultCodeType(rs.getString("SECURITY_RESULT_CODE_TYPE"));
				securityResultCodeTypes.add(securityResultCodeType);
			}
			return ResponseEntity.ok(securityResultCodeTypes);
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(securityResultCodeTypes);
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