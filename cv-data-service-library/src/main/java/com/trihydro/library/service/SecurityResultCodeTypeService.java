package com.trihydro.library.service;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

import com.trihydro.library.model.SecurityResultCodeType;

import org.springframework.http.ResponseEntity;

public class SecurityResultCodeTypeService extends CvDataServiceLibrary {

	static PreparedStatement preparedStatement = null;

	public static List<SecurityResultCodeType> getSecurityResultCodeTypes() {
		String url = String.format("/%s/security-result-code-type/get-all", CVRestUrl);
		ResponseEntity<SecurityResultCodeType[]> response = RestTemplateProvider.GetRestTemplate().getForEntity(url,
				SecurityResultCodeType[].class);
		return Arrays.asList(response.getBody());

	}
}
