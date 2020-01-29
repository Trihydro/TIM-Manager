package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;

@CrossOrigin
@RestController
@RequestMapping("data-frame")
@ApiIgnore
public class DataFrameController extends BaseController {

    @RequestMapping(method = RequestMethod.GET, value = "/itis-for-data-frame/{dataFrameId}")
    public String[] getItisCodesForDataFrameId(@PathVariable Integer dataFrameId) {
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		List<String> itisCodes = new ArrayList<>();

		try {
			connection = GetConnectionPool();

			statement = connection.createStatement();

			String selectStatement = "select distinct ic.itis_code";
			selectStatement += " from data_frame_itis_Code dfic inner join itis_code ic on dfic.itis_code_id = ic.itis_code_id";
			selectStatement += " where data_frame_id =  ";
			selectStatement += dataFrameId;

			rs = statement.executeQuery(selectStatement);

			// convert to ActiveTim object
			while (rs.next()) {
				itisCodes.add(rs.getString("ITIS_CODE"));
			}
		} catch (Exception e) {
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

		return itisCodes.toArray(new String[itisCodes.size()]);
	}
}