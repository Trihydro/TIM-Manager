package com.trihydro.cvdatacontroller.controller;

import org.springframework.web.bind.annotation.RestController;
import com.trihydro.library.service.rsu.RsuService;
import com.trihydro.library.model.WydotRsu;
//import us.dot.its.jpo.ode.plugin.RoadSideUnit.RSU;

import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.net.URI;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import springfox.documentation.annotations.ApiIgnore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import com.trihydro.cvdatacontroller.helpers.DBUtility;

@CrossOrigin
@RestController
@ApiIgnore
public class RsuController {

	public static DBUtility dbUtility;
	
	@Autowired
	public void setDBUtility(DBUtility dbUtilityRh) {
		dbUtility = dbUtilityRh;
	}

	@RequestMapping(value="/rsus", method = RequestMethod.GET, headers="Accept=application/json")
	public List<WydotRsu> selectAllRsus() { 
 		List<WydotRsu> rsus = RsuService.selectAll(dbUtility.getConnection());
 		return rsus;      
	}

	@RequestMapping(value="/selectActiveRSUs", method = RequestMethod.GET, headers="Accept=application/json")
		public List<WydotRsu> selectActiveRsus() { 
		List<WydotRsu> rsus = RsuService.selectActiveRSUs(dbUtility.getConnection());
		return rsus;      
	}

	@RequestMapping(method = RequestMethod.GET, value = "/selectRsusInBuffer/{direction}/{startingMilepost}/{endingMilepost}")
		public List<WydotRsu> selectRsusInBuffer(@PathVariable String direction, @PathVariable Double startingMilepost, @PathVariable Double endingMilepost) { 
		List<WydotRsu> rsus = RsuService.selectRsusInBuffer(direction, startingMilepost, endingMilepost, dbUtility.getConnection());
		return rsus;      
	}
}
