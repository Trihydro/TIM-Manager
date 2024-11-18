package com.trihydro.odewrapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.trihydro.library.helpers.CreateBaseTimUtil;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.model.TimRwList;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.library.service.WydotTimService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.controller.WydotTimRwController;
import com.trihydro.odewrapper.helpers.SetItisCodes;
import com.trihydro.odewrapper.model.ControllerResult;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
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
	@Mock
	Utility utility;

	@InjectMocks
	@Spy
	WydotTimRwController uut;

	private Gson gson = new Gson();

	@BeforeEach
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
		lenient().doReturn(itisCodesIncident).when(setItisCodes).setItisCodesRw(any());
		lenient().doReturn(itisCodes).when(setItisCodes).getItisCodes();

		lenient().doReturn(true).when(uut).routeSupported(isA(String.class));

		lenient().doNothing().when(uut).processRequestAsync();
	}

	@Test
	public void testCreateRwTim_oneDirection_SUCCESS() throws Exception {

		// Arrange
		String rwJson = "{\"timRwList\": [{\"startPoint\": {\"latitude\": 41.161446, \"longitude\": -104.653162},\"endPoint\": {\"latitude\": 41.170465, \"longitude\": -104.085578},\"direction\": \"i\",\"surface\": \"G\",\"buffers\":[{\"distance\": 1,\"action\": \"leftClosed\",\"units\":\"miles\"},{\"distance\": 0.5,\"action\": \"workers\",\"units\":\"miles\"}],\"schedStart\": \"2016-06-23\",\"delays\": [{\"code\":19,\"debug\": {\"codeStr\": \"1\",\"enabled\": true,\"key\":8399},\"firstDay\": \"20130228\",\"lastDay\":\"20500430\",\"dailyStartTime\": \"0000\",\"id\": 8350,\"projectKey\": 19185,\"dailyEndTime\":\"0000\",\"daysOfWeek\": \"SMTWTFS\"}],\"disabled\": false,\"id\":8359,\"projectKey\": 19185,\"highway\": \"I-80\", \"advisory\":[]}]}";
		TimRwList timRwList = gson.fromJson(rwJson, TimRwList.class);

		// Act
		ResponseEntity<String> data = uut.createRoadContructionTim(timRwList);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertNotNull(resultArr);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("success", resultArr[0].resultMessages.get(0));
		Assertions.assertEquals("8359", resultArr[0].clientId);
		Assertions.assertEquals("i", resultArr[0].direction);
		Assertions.assertEquals("I-80", resultArr[0].route);
	}

	@Test
	public void testCreateRwTim_bothDirections_NoMileposts() throws Exception {

		// Arrange
		String rwJson = "{ \"timRwList\": [ {\"startPoint\": {\"latitude\": 41.161446, \"longitude\": -104.653162},\"endPoint\": {\"latitude\": 41.170465, \"longitude\": -104.085578},\"highway\": \"I-80\",\"pk\": \"15917\",\"id\": \"15917\",\"projectKey\": 19185,\"direction\":\"d\",\"surface\": \"P\",\"schedStart\": \"2018-04-16\"}]}";
		TimRwList timRwList = gson.fromJson(rwJson, TimRwList.class);

		// Act
		ResponseEntity<String> data = uut.createRoadContructionTim(timRwList);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertNotNull(resultArr);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("success", resultArr[0].resultMessages.get(0));
		Assertions.assertEquals("d", resultArr[0].direction);
	}

	@Test
	public void testCreateRwTim_bothDirections_NoItisCodes() throws Exception {

		String rwJson = "{ \"timRwList\": [ {\"startPoint\": {\"latitude\": 41.161446, \"longitude\": -104.653162},\"endPoint\": {\"latitude\": 41.170465, \"longitude\": -104.085578},\"highway\": \"I-80\",\"pk\": \"15917\",\"id\": \"15917\",\"projectKey\": 19185,\"direction\":\"d\",\"surface\": \"P\",\"schedStart\": \"2018-04-16\"}]}";
		TimRwList timRwList = gson.fromJson(rwJson, TimRwList.class);

		// Act
		ResponseEntity<String> data = uut.createRoadContructionTim(timRwList);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertNotNull(resultArr);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("success", resultArr[0].resultMessages.get(0));
		Assertions.assertEquals("15917", resultArr[0].clientId);
		Assertions.assertEquals("I-80", resultArr[0].route);
		Assertions.assertEquals("d", resultArr[0].direction);
	}

	@Test
	public void testDeleteRwTimsByClientId() throws Exception {

		// Arrange
		String id = "15917";

		// Act
		ResponseEntity<String> data = uut.deleteRoadContructionTim(id);

		// Assert
		verify(mockWydotTimService).clearTimsById("RW", "15917", null, true);
		Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
		Assertions.assertEquals("success", data.getBody());
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
		Assertions.assertEquals(1, data.size());
		Assertions.assertEquals(at, data.iterator().next());
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
		Assertions.assertEquals(1, data.size());
		Assertions.assertEquals(at, data.iterator().next());
	}

}
