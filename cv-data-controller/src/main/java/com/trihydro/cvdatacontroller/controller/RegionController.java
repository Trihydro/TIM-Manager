package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.cvdatacontroller.tables.TimOracleTables;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;

@CrossOrigin
@RestController
@RequestMapping("region")
@ApiIgnore
public class RegionController extends BaseController {

    @RequestMapping(method = RequestMethod.POST, value = "/update-region-name/{regionId}/{name}")
    public Boolean updateRegionName(@PathVariable Long regionId, @PathVariable String name) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		List<Pair<String, Object>> cols = new ArrayList<Pair<String, Object>>();
		cols.add(new ImmutablePair<String, Object>("NAME", name));

		try {
			connection = GetConnectionPool();
			preparedStatement = TimOracleTables.buildUpdateStatement(regionId, "REGION", "REGION_ID", cols, connection);

			// execute update statement
			Boolean success = updateOrDelete(preparedStatement);
			return success;
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
		return false;
	}
}