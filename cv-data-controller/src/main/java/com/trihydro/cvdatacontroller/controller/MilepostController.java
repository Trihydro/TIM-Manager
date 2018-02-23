package com.trihydro.cvdatacontroller.controller;

import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import com.trihydro.library.service.milepost.MilepostService;
import com.trihydro.library.model.Milepost;

import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.beans.factory.annotation.Autowired;
import com.trihydro.cvdatacontroller.helpers.DBUtility;

@CrossOrigin
@RestController
@ApiIgnore
public class MilepostController {
	
  	public static DBUtility dbUtility;
  
	@Autowired
	public void setDBUtility(DBUtility dbUtilityRh) {
		dbUtility = dbUtilityRh;
	}

	@RequestMapping(value="/mileposts",method = RequestMethod.GET,headers="Accept=application/json")
  	public List<Milepost> getMileposts() { 
   		List<Milepost> mileposts = MilepostService.selectAll(dbUtility.getConnection());
   		return mileposts;
  	}

  	@RequestMapping(method = RequestMethod.GET, value = "/get-milepost-range/{direction}/{route}/{lowerMilepost}/{higherMilepost}")
  	public List<Milepost> getMilepostRange(@PathVariable String direction, @PathVariable String route, @PathVariable Double lowerMilepost, @PathVariable Double higherMilepost) { 
   		List<Milepost> mileposts = MilepostService.selectMilepostRange(direction, route, lowerMilepost, higherMilepost, dbUtility.getConnection());
   		return mileposts;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/mileposts-route/{route}")
	public List<Milepost> getMilepostsRoute(@PathVariable String route) { 
		 List<Milepost> mileposts = MilepostService.getMilepostsRoute(route, dbUtility.getConnection());
		 return mileposts;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/get-milepost-range-no-direction/{route}/{lowerMilepost}/{higherMilepost}")
  	public List<Milepost> getMilepostRange(@PathVariable String route, @PathVariable Double lowerMilepost, @PathVariable Double higherMilepost) { 
   		List<Milepost> mileposts = MilepostService.selectMilepostRangeNoDirection(route, lowerMilepost, higherMilepost, dbUtility.getConnection());
   		return mileposts;
  	}
}
