package com.trihydro.library.service;

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
		
		try (Statement statement = DbUtility.getConnection().createStatement()) {
			// build SQL statement   		    
			ResultSet rs = statement.executeQuery("select * from CATEGORY");
			try {
   			// convert to Category objects   			
   			while (rs.next()) {   			
			    Category category = new Category();
			    category.setCategoryId(rs.getInt("category_id"));
			    category.setCategory(rs.getString("category"));			   
			    categories.add(category);
			   }
			}
			finally {
				try { rs.close(); } catch (Exception ignore) { }
			}
  		} 
  		catch (SQLException e) {
   			e.printStackTrace();
  		}
  		return categories;
	}
}