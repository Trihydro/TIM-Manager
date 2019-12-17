package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.model.ItisCode;

public class ItisCodeService extends CvDataServiceLibrary {

	public static List<ItisCode> selectAll() {
		List<ItisCode> itisCodes = new ArrayList<ItisCode>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		try {
			connection = DbUtility.getConnectionPool();
			// select all Itis Codes from ItisCode table
			statement = connection.createStatement();
			rs = statement.executeQuery("select * from itis_code");
			// convert to ItisCode objects
			while (rs.next()) {
				ItisCode itisCode = new ItisCode();
				itisCode.setItisCodeId(rs.getInt("itis_code_id"));
				itisCode.setItisCode(rs.getInt("itis_code"));
				itisCode.setDescription(rs.getString("description").toLowerCase());
				itisCode.setCategoryId(rs.getInt("category_id"));
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