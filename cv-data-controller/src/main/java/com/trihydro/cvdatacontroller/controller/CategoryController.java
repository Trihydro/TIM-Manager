package com.trihydro.cvdatacontroller.controller;

import org.springframework.web.bind.annotation.RestController;
import com.trihydro.library.service.category.CategoryService;
import com.trihydro.library.model.Category;

import io.swagger.annotations.Api;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.beans.factory.annotation.Autowired;
import com.trihydro.cvdatacontroller.helpers.DBUtility;

@CrossOrigin
@RestController
@ApiIgnore
public class CategoryController {

	public static DBUtility dbUtility;
	
	@Autowired
	public void setDBUtility(DBUtility dbUtilityRh) {
		dbUtility = dbUtilityRh;
	}

	// select all ITIS codes
	@RequestMapping(value="/categories",method = RequestMethod.GET,headers="Accept=application/json")
  	public List<Category> selectAllCategories() { 
   		List<Category> categories = CategoryService.selectAll(dbUtility.getConnection());
   		return categories;
  	}
}
