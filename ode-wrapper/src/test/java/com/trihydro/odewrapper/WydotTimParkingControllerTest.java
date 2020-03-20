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
import com.trihydro.odewrapper.controller.WydotTimParkingController;
import com.trihydro.odewrapper.helpers.SetItisCodes;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.TimParkingList;
import com.trihydro.odewrapper.service.WydotTimService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class WydotTimParkingControllerTest {

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
	WydotTimParkingController uut;

	private Gson gson = new Gson();
	private ActiveTim at;
	private List<ActiveTim> parkingTims;

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
		lenient().doReturn(itisCodesIncident).when(setItisCodes).setItisCodesParking(any());
		lenient().doReturn(itisCodes).when(setItisCodes).getItisCodes();

		lenient().doNothing().when(uut).processRequest(any());
		lenient().doReturn(true).when(uut).routeSupported(isA(String.class));

		parkingTims = new ArrayList<>();
		at = new ActiveTim();
		at.setActiveTimId(-1l);
		at.setClientId("clientId");
		at.setDirection("direction");
		parkingTims.add(at);
		lenient().doReturn(parkingTims).when(mockWydotTimService).selectTimsByType("P");
	}

	@Test
	public void testCreateParkingTim_oneDirection_success() throws Exception {

		// Arrange
		String parkingJson = "{\"timParkingList\": [{ \"mileMarker\": 360, \"route\": \"I-80\", \"direction\": \"d\", \"availability\": 4103, \"clientId\": \"Parking49251\" }]}";
		TimParkingList tpl = gson.fromJson(parkingJson, TimParkingList.class);

		// Act
		ResponseEntity<String> data = uut.createParkingTim(tpl);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals(1, resultArr.length);
		assertEquals("success", resultArr[0].resultMessages.get(0));
		assertEquals("d", resultArr[0].direction);
		assertEquals("Parking49251", resultArr[0].clientId);
		assertEquals("I-80", resultArr[0].route);
	}

	@Test
	public void testCreateParkingTimTim_oneDirection_NoMileposts() throws Exception {

		// Arrange
		String parkingJson = "{\"timParkingList\": [{ \"mileMarker\": 360,\"route\": \"I-70\", \"direction\": \"d\", \"availability\": 4103,\"clientId\": \"Parking49251\" }]}";
		TimParkingList tpl = gson.fromJson(parkingJson, TimParkingList.class);
		doReturn(false).when(uut).routeSupported("I-70");

		// Act
		ResponseEntity<String> data = uut.createParkingTim(tpl);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals(1, resultArr.length);
		assertEquals("route not supported", resultArr[0].resultMessages.get(0));
		assertEquals("d", resultArr[0].direction);
	}

	@Test
	public void testCreateParkingTim_oneDirection_NoItisCodes() throws Exception {

		// Arrange
		String parkingJson = "{\"timParkingList\": [{ \"mileMarker\": 360,\"route\": \"I-80\", \"direction\": \"d\", \"availability\": 345678,\"clientId\": \"Parking49251\" }]}";
		TimParkingList tpl = gson.fromJson(parkingJson, TimParkingList.class);

		// Act
		ResponseEntity<String> data = uut.createParkingTim(tpl);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals(1, resultArr.length);
		assertEquals("success", resultArr[0].resultMessages.get(0));
		assertEquals("d", resultArr[0].direction);
		assertEquals("Parking49251", resultArr[0].clientId);
		assertEquals("I-80", resultArr[0].route);
	}

	@Test
	public void testUpdateParkingTim_oneDirection_success() throws Exception {

		String parkingJson = "{\"timParkingList\": [{ \"mileMarker\": 360,\"route\": \"I-80\", \"direction\": \"d\", \"availability\": 4103,\"clientId\": \"Parking49251\" }]}";
		TimParkingList tpl = gson.fromJson(parkingJson, TimParkingList.class);

		// Act
		ResponseEntity<String> data = uut.createParkingTim(tpl);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals(1, resultArr.length);
		assertEquals("success", resultArr[0].resultMessages.get(0));
		assertEquals("Parking49251", resultArr[0].clientId);
		assertEquals("I-80", resultArr[0].route);
		assertEquals("d", resultArr[0].direction);
	}

	@Test
	public void testDeleteParkingTimsByClientId() throws Exception {

		// Arrange
		String id = "Parking49251";
		// Act
		ResponseEntity<String> data = uut.deleteParkingTim(id);
		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		assertEquals("success", data.getBody());
	}

	@Test
	public void testGetParkingTims() throws Exception {

		// Arrange

		// Act
		Collection<ActiveTim> data = uut.getParkingTims();

		// Assert
		assertEquals(1, data.size());
		assertEquals(at, data.iterator().next());
	}

	@Test
	public void testGetParkingTimsByClientId() throws Exception {

		// Arrange
		String clientId = "Parking49251";
		doReturn(parkingTims).when(mockWydotTimService).selectTimByClientId("P", clientId);

		// Act
		Collection<ActiveTim> data = uut.getParkingTimById(clientId);

		// Assert
		assertEquals(1, data.size());
		assertEquals(at, data.iterator().next());
	}
}
