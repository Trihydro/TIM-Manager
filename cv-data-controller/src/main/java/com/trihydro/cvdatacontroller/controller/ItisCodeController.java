package com.trihydro.cvdatacontroller.controller;

import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import com.trihydro.library.service.ItisCodeService;
import com.trihydro.library.model.ItisCode;

import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.beans.factory.annotation.Autowired;

@CrossOrigin
@RestController
@ApiIgnore
public class ItisCodeController {
	
	@RequestMapping(value="/itiscodes",method = RequestMethod.GET,headers="Accept=application/json")
  	public List<ItisCode> selectAllItisCodes() { 
   		List<ItisCode> itisCodes = ItisCodeService.selectAll();
   		return itisCodes;
  	}

}
