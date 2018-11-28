package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandler;
import java.sql.SQLException;
import com.trihydro.library.tables.TimOracleTables;

public class PathService extends CvDataServiceLibrary {

	public static Long insertPath() {

		PreparedStatement preparedStatement = null;
		Connection connection = null;

		try {
			connection = DbUtility.getConnectionPool();
			String insertQueryStatement = TimOracleTables.buildInsertQueryStatement("path",
					TimOracleTables.getPathTable());
			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "path_id" });
			int fieldNum = 1;

			for (String col : TimOracleTables.getPathTable()) {
				if (col.equals("SCALE"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, 0);
				fieldNum++;
			}
			// execute insert statement
			Long pathId = log(preparedStatement, "pathId");
			return pathId;
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
		return new Long(0);
	}

}
