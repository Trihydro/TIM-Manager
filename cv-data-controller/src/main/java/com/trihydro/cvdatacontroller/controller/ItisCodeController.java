package com.trihydro.cvdatacontroller.controller;

import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import com.trihydro.library.service.itiscode.ItisCodeService;
import com.trihydro.library.model.ItisCode;

import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.beans.factory.annotation.Autowired;
import com.trihydro.cvdatacontroller.helpers.DBUtility;

@CrossOrigin
@RestController
@ApiIgnore
public class ItisCodeController {

	public static DBUtility dbUtility;
	
	@Autowired
	public void setDBUtility(DBUtility dbUtilityRh) {
		dbUtility = dbUtilityRh;
	}
	
	@RequestMapping(value="/itiscodes",method = RequestMethod.GET,headers="Accept=application/json")
  	public List<ItisCode> selectAllItisCodes() { 
   		List<ItisCode> itisCodes = ItisCodeService.selectAll(dbUtility.getConnection());
   		return itisCodes;
  	}

}
