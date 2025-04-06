package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.Category;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("category")
public class CategoryController extends BaseController {
    private static final Logger LOG = LoggerFactory.getLogger(CategoryController.class);

    // select all ITIS codes
    @RequestMapping(value = "/all", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<List<Category>> SelectAllCategories() {
		List<Category> categories = new ArrayList<Category>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			connection = dbInteractions.getConnectionPool();
			statement = connection.createStatement();

			// build SQL statement
			rs = statement.executeQuery("select * from CATEGORY");

			// convert to Category objects
			while (rs.next()) {
				Category category = new Category();
				category.setCategoryId(rs.getInt("category_id"));
				category.setCategory(rs.getString("category"));
				categories.add(category);
			}
		} catch (SQLException e) {
            LOG.error("Exception", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(categories);
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
                LOG.error("Exception", e);
			}
		}
		return ResponseEntity.ok(categories);
	}
}
