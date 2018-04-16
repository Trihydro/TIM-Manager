package com.trihydro.cvdatacontroller.controller;

import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import com.trihydro.library.service.MilepostService;
import com.trihydro.library.model.Milepost;

import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;


@CrossOrigin
@RestController
@ApiIgnore
public class MilepostController {

	@RequestMapping(value="/mileposts",method = RequestMethod.GET,headers="Accept=application/json")
  	public List<Milepost> getMileposts() { 
   		List<Milepost> mileposts = MilepostService.selectAll();
   		return mileposts;
  	}

  	@RequestMapping(method = RequestMethod.GET, value = "/get-milepost-range/{direction}/{fromMilepost}/{toMilepost}/{route}")
  	public List<Milepost> getMilepostRange(@PathVariable String direction, @PathVariable String route, @PathVariable Double fromMilepost, @PathVariable Double toMilepost) { 
   		List<Milepost> mileposts = MilepostService.selectMilepostRange(direction, route, fromMilepost, toMilepost);
   		return mileposts;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/mileposts-route/{route}/{mod}")
	public List<Milepost> getMilepostsRoute(@PathVariable String route, @PathVariable Boolean mod) { 
		 List<Milepost> mileposts = MilepostService.getMilepostsRoute(route, mod);
		 return mileposts;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/get-milepost-range-no-direction/{fromMilepost}/{toMilepost}/{route}")
  	public List<Milepost> getMilepostRange(@PathVariable String route, @PathVariable Double fromMilepost, @PathVariable Double toMilepost) { 
   		List<Milepost> mileposts = MilepostService.selectMilepostRangeNoDirection(route, fromMilepost, toMilepost);
   		return mileposts;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/get-milepost-test-range/{direction}/{start}/{end}/{route}")
  	public List<Milepost> getMilepostTestRange(@PathVariable String direction, @PathVariable String route, @PathVariable Double start, @PathVariable Double end) { 
   		List<Milepost> mileposts = MilepostService.selectMilepostTestRange(direction, route, start, end);
   		return mileposts;
	}

	@RequestMapping(value="/mileposts-test",method = RequestMethod.GET,headers="Accept=application/json")
	public List<Milepost> getMilepostsTest() { 
		 List<Milepost> mileposts = MilepostService.selectAllTest();
		 return mileposts;
	}
	  
}
