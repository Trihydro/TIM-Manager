package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.model.Category;

import org.springframework.stereotype.Component;

@Component
public class CategoryService extends CvDataServiceLibrary
{	

	// select all ITIS Codes from the database
	public static List<Category> selectAll() {

		List<Category> categories = new ArrayList<Category>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			connection = DbUtility.getConnectionPool();
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
  		} 
  		catch (SQLException e) {
   			e.printStackTrace();
		}
		finally {			
			try {
				// close prepared statement
				if(statement != null)
					statement.close();
				// return connection back to pool
				if(connection != null)
					connection.close();
				// close result set
				if(rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
  		return categories;
	}
}