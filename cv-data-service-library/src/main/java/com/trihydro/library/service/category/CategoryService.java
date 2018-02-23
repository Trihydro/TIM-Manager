package com.trihydro.library.service.category;

import com.trihydro.library.model.Category;

import java.sql.Statement;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.sql.Connection;

@Component
public class CategoryService 
{	

	// select all ITIS Codes from the database
	public static List<Category> selectAll(Connection connection) {

		List<Category> categories = new ArrayList<Category>();
		
		try {
			// build SQL statement
   		    Statement statement = connection.createStatement();
   			ResultSet rs = statement.executeQuery("select * from CATEGORY");
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
  		return categories;
	}
}