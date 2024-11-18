package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.ItisCode;

import org.springframework.stereotype.Component;

@Component
public class ItisCodeService extends BaseService {

	public List<ItisCode> selectAllItisCodes() {
		List<ItisCode> itisCodes = new ArrayList<ItisCode>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		try {
			connection = dbInteractions.getConnectionPool();
			// select all Itis Codes from ItisCode table
			statement = connection.createStatement();
			rs = statement.executeQuery("select * from itis_code");
			// convert to ItisCode objects
			while (rs.next()) {
				ItisCode itisCode = new ItisCode();
				itisCode.setItisCodeId(rs.getInt("ITIS_CODE_ID"));
				itisCode.setItisCode(rs.getInt("ITIS_CODE"));
				String desc = rs.getString("DESCRIPTION");
				if (desc != null && desc.length() > 0) {
					desc = desc.toLowerCase();
				}
				itisCode.setDescription(desc);
				itisCode.setCategoryId(rs.getInt("CATEGORY_ID"));
				itisCodes.add(itisCode);
			}
		} catch (SQLException e) {
			e.printStackTrace();
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
		return itisCodes;
	}
}