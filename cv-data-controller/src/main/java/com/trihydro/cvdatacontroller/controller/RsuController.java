package com.trihydro.cvdatacontroller.controller;

import org.springframework.web.bind.annotation.RestController;
import com.trihydro.library.service.RsuService;
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

@CrossOrigin
@RestController
@ApiIgnore
public class RsuController {

	@RequestMapping(value="/rsus", method = RequestMethod.GET, headers="Accept=application/json")
	public List<WydotRsu> selectAllRsus() throws Exception { 
 		List<WydotRsu> rsus = RsuService.selectAll();
 		return rsus;      
	}

	@RequestMapping(value="/selectActiveRSUs", method = RequestMethod.GET, headers="Accept=application/json")
		public List<WydotRsu> selectActiveRsus() { 
		List<WydotRsu> rsus = RsuService.selectActiveRSUs();
		return rsus;      
	}

	@RequestMapping(method = RequestMethod.GET, value = "/selectRsusInBuffer/{direction}/{startingMilepost}/{endingMilepost}")
		public List<WydotRsu> selectRsusInBuffer(@PathVariable String direction, @PathVariable Double startingMilepost, @PathVariable Double endingMilepost) { 
		List<WydotRsu> rsus = RsuService.selectRsusInBuffer(direction, startingMilepost, endingMilepost);
		return rsus;      
	}
}
