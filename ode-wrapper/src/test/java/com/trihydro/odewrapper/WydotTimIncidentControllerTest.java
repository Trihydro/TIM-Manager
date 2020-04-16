package com.trihydro.odewrapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.controller.WydotTimIncidentController;
import com.trihydro.odewrapper.helpers.SetItisCodes;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.TimIncidentList;
import com.trihydro.odewrapper.service.WydotTimService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@RunWith(StrictStubs.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class WydotTimIncidentControllerTest {

	@Mock
	BasicConfiguration mockBasicConfiguration;
	@Mock
	WydotTimService mockWydotTimService;
	@Mock
	TimTypeService mockTimTypeService;
	@Mock
	SetItisCodes setItisCodes;

	@InjectMocks
	@Spy
	WydotTimIncidentController uut;

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
		lenient().doReturn(itisCodesIncident).when(setItisCodes).setItisCodesIncident(any());
		lenient().doReturn(itisCodes).when(setItisCodes).getItisCodes();

		lenient().doNothing().when(uut).makeTimsAsync(any());
		lenient().doReturn(true).when(uut).routeSupported(isA(String.class));
	}

	@Test
	public void testCreateIncidentTim_bothDirections_success() throws Exception {

		// Arrange
		String incidentJson = "{\"timIncidentList\": [{ \"startPoint\": {\"latitude\": 41.161446, \"longitude\": -104.653162},\"endPoint\": {\"latitude\": 41.170465, \"longitude\": -104.085578}, \"impact\": \"L\", \"problem\": \"fire\", \"effect\": \"test\", \"action\": \"test\", \"pk\": 3622, \"highway\": \"I-80\", \"incidentId\": \"IN49251\", \"direction\": \"b\", \"ts\": \"2018-04-16T19:30:05.000Z\" }]}";
		TimIncidentList til = gson.fromJson(incidentJson, TimIncidentList.class);

		// Act
		ResponseEntity<String> data = uut.createIncidentTim(til);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals(1, resultArr.length);
		assertEquals("success", resultArr[0].resultMessages.get(0));
		assertEquals("IN49251", resultArr[0].clientId);
		assertEquals("I-80", resultArr[0].route);
		assertEquals("b", resultArr[0].direction);
	}

	@Test
	public void testCreateIncidentTim_bothDirections_NoMileposts() throws Exception {

		// Arrange
		String incidentJson = "{\"timIncidentList\": [{ \"startPoint\": {\"latitude\": 41.161446, \"longitude\": -104.653162},\"endPoint\": {\"latitude\": 41.170465, \"longitude\": -104.085578}, \"impact\":\"L\", \"problem\": \"fire\", \"effect\": \"test\",\"action\": \"test\", \"pk\": 3622, \"highway\": \"I-25\", \"incidentId\":\"IN49251\", \"direction\": \"b\", \"ts\": \"2018-04-16T19:30:05.000Z\"}]}";
		TimIncidentList til = gson.fromJson(incidentJson, TimIncidentList.class);
		doReturn(false).when(uut).routeSupported("I-25");

		// Act
		ResponseEntity<String> data = uut.createIncidentTim(til);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);

		assertEquals("route not supported", resultArr[0].resultMessages.get(0));
		assertEquals("b", resultArr[0].direction);
		assertEquals("IN49251", resultArr[0].clientId);
	}

	@Test
	public void testCreateIncidentTim_bothDirections_NoItisCodes() throws Exception {

		// Arrange
		String incidentJson = "{\"timIncidentList\": [{ \"startPoint\": {\"latitude\": 41.161446, \"longitude\": -104.653162},\"endPoint\": {\"latitude\": 41.170465, \"longitude\": -104.085578}, \"impact\":\"L\", \"problem\": \"test\", \"effect\": \"test\",\"action\": \"test\", \"pk\": 3622, \"highway\": \"I-80\", \"incidentId\":\"IN49251\", \"direction\": \"b\", \"schedStart\": \"2018-04-16\" }]}";
		TimIncidentList til = gson.fromJson(incidentJson, TimIncidentList.class);

		// Act
		ResponseEntity<String> data = uut.createIncidentTim(til);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals("success", resultArr[0].resultMessages.get(0));
		assertEquals("b", resultArr[0].direction);
	}

	@Test
	public void testCreateIncidentTim_oneDirection_success() throws Exception {

		String incidentJson = "{\"timIncidentList\": [{ \"startPoint\": {\"latitude\": 41.161446, \"longitude\": -104.653162},\"endPoint\": {\"latitude\": 41.170465, \"longitude\": -104.085578}, \"impact\":\"L\", \"problem\": \"fire\", \"effect\": \"test\",\"action\": \"test\", \"pk\": 3622, \"highway\": \"I-80\", \"incidentId\":\"OD49251\", \"direction\": \"i\", \"ts\":\"2018-04-16T19:30:05.000Z\" }]}";
		TimIncidentList til = gson.fromJson(incidentJson, TimIncidentList.class);

		// Act
		ResponseEntity<String> data = uut.createIncidentTim(til);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals("success", resultArr[0].resultMessages.get(0));
		assertEquals("i", resultArr[0].direction);
		assertEquals("OD49251", resultArr[0].clientId);
		assertEquals("I-80", resultArr[0].route);
	}

	@Test
	public void testCreateIncidentTim_oneDirection_NoMileposts() throws Exception {

		String incidentJson = "{\"timIncidentList\": [{ \"startPoint\": {\"latitude\": 41.161446, \"longitude\": -104.653162},\"endPoint\": {\"latitude\": 41.170465, \"longitude\": -104.085578}, \"impact\":\"L\", \"problem\": \"fire\", \"effect\": \"test\",\"action\": \"test\", \"pk\": 3622, \"highway\": \"I-25\", \"incidentId\":\"IN49251\", \"direction\": \"i\", \"ts\":\"2018-04-16T19:30:05.000Z\" }]}";
		TimIncidentList til = gson.fromJson(incidentJson, TimIncidentList.class);
		doReturn(false).when(uut).routeSupported("I-25");

		// Act
		ResponseEntity<String> data = uut.createIncidentTim(til);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals("route not supported", resultArr[0].resultMessages.get(0));
		assertEquals("i", resultArr[0].direction);
		assertEquals("IN49251", resultArr[0].clientId);
	}

	@Test
	public void testCreateIncidentTim_oneDirection_NoItisCodes() throws Exception {

		String incidentJson = "{\"timIncidentList\": [{ \"startPoint\": {\"latitude\": 41.161446, \"longitude\": -104.653162},\"endPoint\": {\"latitude\": 41.170465, \"longitude\": -104.085578}, \"impact\":\"L\", \"problem\": \"test\", \"effect\": \"test\",\"action\": \"test\", \"pk\": 3622, \"highway\": \"I-80\", \"incidentId\":\"IN49251\", \"direction\": \"i\", \"ts\":\"2018-04-16T19:30:05.000Z\" }]}";
		TimIncidentList til = gson.fromJson(incidentJson, TimIncidentList.class);

		// Act
		ResponseEntity<String> data = uut.createIncidentTim(til);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals("success", resultArr[0].resultMessages.get(0));
		assertEquals("i", resultArr[0].direction);
	}

	@Test
	public void testGetIncidentTims() throws Exception {

		// Arrange
		List<ActiveTim> ats = new ArrayList<>();
		ActiveTim at = new ActiveTim();
		at.setActiveTimId(-1l);
		at.setClientId("clientId");
		at.setDirection("direction");
		ats.add(at);
		doReturn(ats).when(mockWydotTimService).selectTimsByType("I");

		// Act
		Collection<ActiveTim> data = uut.getIncidentTims();

		// Assert
		assertEquals(1, data.size());
		assertEquals(at, data.iterator().next());
	}

	@Test
	public void testGetIncidentTimsByClientId() throws Exception {

		// Arrange
		String incidentId = "IN49251";
		List<ActiveTim> ats = new ArrayList<>();
		ActiveTim at = new ActiveTim();
		at.setActiveTimId(-1l);
		at.setClientId("clientId");
		at.setDirection("direction");
		ats.add(at);
		doReturn(ats).when(mockWydotTimService).selectTimByClientId("I", incidentId);

		// Act
		Collection<ActiveTim> data = uut.getIncidentTimById(incidentId);

		// Assert
		assertEquals(1, data.size());
		assertEquals(at, data.iterator().next());
	}

	@Test
	public void testUpdateIncidentTim_oneDirection_success() throws Exception {

		// Arrange
		String incidentJson = "{\"timIncidentList\": [{ \"startPoint\": {\"latitude\": 41.161446, \"longitude\": -104.653162},\"endPoint\": {\"latitude\": 41.170465, \"longitude\": -104.085578}, \"impact\":\"L\", \"problem\": \"fire\", \"effect\": \"test\",\"action\": \"test\", \"pk\": 3622, \"highway\": \"I-80\", \"incidentId\":\"OD49251\", \"direction\": \"i\", \"ts\":\"2018-04-16T19:30:05.000Z\" }]}";
		TimIncidentList til = gson.fromJson(incidentJson, TimIncidentList.class);

		// Act
		ResponseEntity<String> data = uut.updateIncidentTim(til);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals("success", resultArr[0].resultMessages.get(0));
		assertEquals("i", resultArr[0].direction);
		assertEquals("OD49251", resultArr[0].clientId);
		assertEquals("I-80", resultArr[0].route);
	}

	@Test
	public void testUpdateIncidentTim_bothDirections_success() throws Exception {

		// Arrange
		String incidentJson = "{\"timIncidentList\": [{ \"startPoint\": {\"latitude\": 41.161446, \"longitude\": -104.653162},\"endPoint\": {\"latitude\": 41.170465, \"longitude\": -104.085578}, \"impact\":\"L\", \"problem\": \"crash\", \"effect\": \"leftClosed\",\"action\": \"caution\", \"pk\": 3622, \"highway\": \"I-80\", \"incidentId\":\"IN49251\", \"direction\": \"b\" }]}";
		TimIncidentList til = gson.fromJson(incidentJson, TimIncidentList.class);

		// Act
		ResponseEntity<String> data = uut.updateIncidentTim(til);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals("success", resultArr[0].resultMessages.get(0));
		assertEquals("b", resultArr[0].direction);
		assertEquals("IN49251", resultArr[0].clientId);
		assertEquals("I-80", resultArr[0].route);
	}

	@Test
	public void testDeleteIncidentTimsByClientId() throws Exception {

		// Arrange
		String incidentId = "IN49251";

		// Act
		ResponseEntity<String> data = uut.deleteIncidentTim(incidentId);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		assertEquals("success", data.getBody());
	}
}
