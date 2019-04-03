package com.trihydro.cvdatacontroller.controller;

import org.springframework.web.bind.annotation.RestController;
import com.trihydro.library.service.CategoryService;
import com.trihydro.library.model.Category;

import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
@RestController
@ApiIgnore
public class CategoryController {
	
	// select all ITIS codes
	@RequestMapping(value="/categories",method = RequestMethod.GET,headers="Accept=application/json")
  	public List<Category> selectAllCategories() { 
   		List<Category> categories = CategoryService.selectAll();
   		return categories;
  	}
}
