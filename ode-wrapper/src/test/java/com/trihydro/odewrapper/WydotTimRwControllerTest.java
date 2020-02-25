package com.trihydro.odewrapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.controller.WydotTimRwController;
import com.trihydro.odewrapper.helpers.SetItisCodes;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.TimRwList;
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
public class WydotTimRwControllerTest {

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
	WydotTimRwController uut;

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
		doReturn(itisCodesIncident).when(setItisCodes).setItisCodesRw(any());
		doReturn(itisCodes).when(setItisCodes).getItisCodes();

		doReturn(true).when(uut).routeSupported(isA(String.class));

		doNothing().when(uut).processRequestAsync();
	}

	@Test
	public void testCreateRwTim_oneDirection_SUCCESS() throws Exception {

		// Arrange
		String rwJson = "{\"timRwList\": [{\"toRm\": 375,\"fromRm\":370,\"direction\": \"i\",\"surface\": \"G\",\"buffers\":[{\"distance\": 1,\"action\": \"leftClosed\",\"units\":\"miles\"},{\"distance\": 0.5,\"action\": \"workers\",\"units\":\"miles\"}],\"schedStart\": \"2016-06-23\",\"delays\": [{\"code\":19,\"debug\": {\"codeStr\": \"1\",\"enabled\": true,\"key\":8399},\"firstDay\": \"20130228\",\"lastDay\":\"20500430\",\"dailyStartTime\": \"0000\",\"id\": 8350,\"dailyEndTime\":\"0000\",\"daysOfWeek\": \"SMTWTFS\"}],\"disabled\": false,\"id\":8359,\"highway\": \"I-80\", \"advisory\":[]}]}";
		TimRwList timRwList = gson.fromJson(rwJson, TimRwList.class);

		// Act
		ResponseEntity<String> data = uut.createRoadContructionTim(timRwList);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals(1, resultArr.length);
		assertEquals("success", resultArr[0].resultMessages.get(0));
		assertEquals("8359", resultArr[0].clientId);
		assertEquals("i", resultArr[0].direction);
		assertEquals("I-80", resultArr[0].route);
	}

	@Test
	public void testCreateRwTim_bothDirections_NoMileposts() throws Exception {

		// Arrange
		String rwJson = "{ \"timRwList\": [ {\"fromRm\": 350,\"toRm\": 360,\"highway\": \"I-80\",\"pk\": \"15917\",\"id\": \"15917\",\"direction\":\"d\",\"surface\": \"P\",\"schedStart\": \"2018-04-16\"}]}";
		TimRwList timRwList = gson.fromJson(rwJson, TimRwList.class);

		// Act
		ResponseEntity<String> data = uut.createRoadContructionTim(timRwList);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals(1, resultArr.length);
		assertEquals("success", resultArr[0].resultMessages.get(0));
		assertEquals("d", resultArr[0].direction);
	}

	@Test
	public void testCreateRwTim_bothDirections_NoItisCodes() throws Exception {

		String rwJson = "{ \"timRwList\": [ {\"fromRm\": 350,\"toRm\": 360,\"highway\": \"I-80\",\"pk\": \"15917\",\"id\": \"15917\",\"direction\":\"d\",\"surface\": \"P\",\"schedStart\": \"2018-04-16\"}]}";
		TimRwList timRwList = gson.fromJson(rwJson, TimRwList.class);

		// Act
		ResponseEntity<String> data = uut.createRoadContructionTim(timRwList);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals(1, resultArr.length);
		assertEquals("success", resultArr[0].resultMessages.get(0));
		assertEquals("15917", resultArr[0].clientId);
		assertEquals("I-80", resultArr[0].route);
		assertEquals("d", resultArr[0].direction);
	}

	@Test
	public void testDeleteRwTimsByClientId() throws Exception {

		// Arrange
		String id = "15917";

		// Act
		ResponseEntity<String> data = uut.deleteRoadContructionTim(id);
		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		assertEquals("success", data.getBody());
	}

	@Test
	public void testGetRwTims() throws Exception {

		// Arrange
		List<ActiveTim> ats = new ArrayList<>();
		ActiveTim at = new ActiveTim();
		at.setActiveTimId(-1l);
		at.setClientId("clientId");
		at.setDirection("direction");
		ats.add(at);
		doReturn(ats).when(mockWydotTimService).selectTimsByType("RW");

		// Act
		Collection<ActiveTim> data = uut.getRoadConstructionTim();

		// Assert
		assertEquals(1, data.size());
		assertEquals(at, data.iterator().next());
	}

	@Test
	public void testGetRwTimsByClientId() throws Exception {

		// Arrange
		String id = "Parking49251";
		List<ActiveTim> ats = new ArrayList<>();
		ActiveTim at = new ActiveTim();
		at.setActiveTimId(-1l);
		at.setClientId("clientId");
		at.setDirection("direction");
		ats.add(at);
		doReturn(ats).when(mockWydotTimService).selectTimByClientId("RW", id);

		// Act
		Collection<ActiveTim> data = uut.getRoadContructionTimById(id);

		// Assert
		assertEquals(1, data.size());
		assertEquals(at, data.iterator().next());
	}

}
