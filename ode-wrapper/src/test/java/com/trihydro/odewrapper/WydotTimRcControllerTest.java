package com.trihydro.odewrapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.controller.WydotTimRcController;
import com.trihydro.odewrapper.helpers.SetItisCodes;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.TimRcList;
import com.trihydro.odewrapper.service.WydotTimService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class WydotTimRcControllerTest {

	@Mock
	BasicConfiguration mockBasicConfiguration;
	@Mock
	TimTypeService mockTimTypeService;
	@Mock
	WydotTimService mockWydotTimService;
	@Mock
	CreateBaseTimUtil mockCreateBaseTimUtil;
	@Mock
	SetItisCodes setItisCodes;

	@InjectMocks
	@Spy
	WydotTimRcController uut;

	private Gson gson = new Gson();

	@Before
	public void setup() throws Exception {
		List<ItisCode> itisCodes = new ArrayList<>();
		ItisCode ic = new ItisCode();
		ic.setCategoryId(-1);
		ic.setDescription("description");
		ic.setItisCode(-2);
		ic.setItisCodeId(-3);
		itisCodes.add(ic);
		List<String> itisCodesIncident = new ArrayList<>();
		itisCodesIncident.add("531");
		doReturn(itisCodesIncident).when(setItisCodes).setItisCodesRc(any());
		doReturn(itisCodes).when(setItisCodes).getItisCodes();
	}

	@Test
	public void testCreateRcTim_bothDirections_success() throws Exception {

		// Arrange
		String rcJson = "{\"timRcList\": [{ \"route\": \"I80\", \"fromRm\": 350, \"toRm\": 360, \"roadCode\": \"LARI80WQDHLD\", \"direction\": \"both\",\"advisory\": [4871]} ]}";
		TimRcList timRcList = gson.fromJson(rcJson, TimRcList.class);

		// Act
		ResponseEntity<String> data = uut.createUpdateRoadConditionsTim(timRcList);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals(1, resultArr.length);
		assertEquals("success", resultArr[0].resultMessages.get(0));
		assertEquals("both", resultArr[0].direction);
		assertEquals("I80", resultArr[0].route);
	}

	@Test
	public void testCreateRcTim_bothDirections_NoMileposts() throws Exception {

		// Arrange
		String rcJson = "{\"timRcList\": [{ \"route\": \"I70\", \"fromRm\": 350,\"roadCode\": \"LARI80WQDHLD\", \"toRm\": 360, \"direction\":\"both\",\"advisory\": [4871]} ]}";
		TimRcList timRcList = gson.fromJson(rcJson, TimRcList.class);

		// Act
		ResponseEntity<String> data = uut.createUpdateRoadConditionsTim(timRcList);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals(1, resultArr.length);
		assertEquals("route not supported", resultArr[0].resultMessages.get(0));
		assertEquals("both", resultArr[0].direction);
		assertEquals("I70", resultArr[0].route);
	}

	@Test
	public void testCreateRcTim_bothDirections_NoItisCodes() throws Exception {

		// Arrange
		String rcJson = "{\"timRcList\": [{ \"route\": \"I80\", \"fromRm\": 350,\"roadCode\": \"LARI80WQDHLD\", \"toRm\": 360, \"direction\":\"both\",\"advisory\": [11]} ]}";
		TimRcList timRcList = gson.fromJson(rcJson, TimRcList.class);

		// Act
		ResponseEntity<String> data = uut.createUpdateRoadConditionsTim(timRcList);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals(1, resultArr.length);
		assertEquals("success", resultArr[0].resultMessages.get(0));
		assertEquals("both", resultArr[0].direction);
		assertEquals("I80", resultArr[0].route);
	}

	@Test
	public void testCreateRcTim_oneDirection_success() throws Exception {

		// Arrange
		String rcJson = "{\"timRcList\": [{ \"route\": \"I80\", \"fromRm\": 350,\"roadCode\": \"LARI80WQDHLD\", \"toRm\": 360, \"direction\":\"eastbound\",\"advisory\": [4871]} ]}";
		TimRcList timRcList = gson.fromJson(rcJson, TimRcList.class);

		// Act
		ResponseEntity<String> data = uut.createUpdateRoadConditionsTim(timRcList);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals(1, resultArr.length);
		assertEquals("success", resultArr[0].resultMessages.get(0));
		assertEquals("eastbound", resultArr[0].direction);
		assertEquals("I80", resultArr[0].route);
	}

	@Test
	public void testCreateVslTim_oneDirection_NoMileposts() throws Exception {

		// Arrange
		String rcJson = "{\"timRcList\": [{ \"route\": \"I80\", \"fromRm\": 350,\"roadCode\": \"LARI80WQDHLD\", \"toRm\": 360, \"direction\":\"eastbound\",\"advisory\": [4871]} ]}";
		TimRcList timRcList = gson.fromJson(rcJson, TimRcList.class);

		// Act
		ResponseEntity<String> data = uut.createUpdateRoadConditionsTim(timRcList);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals(1, resultArr.length);
		assertEquals("success", resultArr[0].resultMessages.get(0));
		assertEquals("eastbound", resultArr[0].direction);
		assertEquals("I80", resultArr[0].route);
	}

	@Test
	public void testCreateRcTim_oneDirection_NoItisCodes() throws Exception {

		// Arrange
		String rcJson = "{\"timRcList\": [{ \"route\": \"I80\", \"fromRm\": 350,\"roadCode\": \"LARI80WQDHLD\", \"toRm\": 360, \"direction\":\"eastbound\",\"advisory\": [11]} ]}";
		TimRcList timRcList = gson.fromJson(rcJson, TimRcList.class);

		// Act
		ResponseEntity<String> data = uut.createUpdateRoadConditionsTim(timRcList);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals(1, resultArr.length);
		assertEquals("success", resultArr[0].resultMessages.get(0));
		assertEquals("eastbound", resultArr[0].direction);
		assertEquals("I80", resultArr[0].route);
	}

	@Test
	public void testAllClear() throws Exception {

		// Arrange
		String rcJson = "{\"timRcList\": [{ \"route\": \"I80\", \"fromRm\": 360,\"roadCode\": \"LARI80WQDHLD\", \"toRm\": 370, \"direction\":\"westbound\",\"advisory\": [5378]} ]}";
		TimRcList timRcList = gson.fromJson(rcJson, TimRcList.class);

		// Act
		ResponseEntity<String> data = uut.submitAllClearRoadConditionsTim(timRcList);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals(1, resultArr.length);
		assertEquals("success", resultArr[0].resultMessages.get(0));
		assertEquals("westbound", resultArr[0].direction);
		assertEquals("I80", resultArr[0].route);
	}
}
