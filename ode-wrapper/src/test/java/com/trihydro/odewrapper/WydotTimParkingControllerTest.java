package com.trihydro.odewrapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.trihydro.library.helpers.CreateBaseTimUtil;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.library.service.WydotTimService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.controller.WydotTimParkingController;
import com.trihydro.odewrapper.helpers.SetItisCodes;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.TimParkingList;

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
	@Mock
	Utility utility;

	@InjectMocks
	@Spy
	WydotTimParkingController uut;

	private Gson gson = new Gson();
	private ActiveTim at;
	private List<ActiveTim> parkingTims;

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
		lenient().doReturn(itisCodesIncident).when(setItisCodes).setItisCodesParking(any());
		lenient().doReturn(itisCodes).when(setItisCodes).getItisCodes();

		lenient().doNothing().when(uut).processRequestAsync(any());
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
		Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertNotNull(resultArr);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("success", resultArr[0].resultMessages.get(0));
		Assertions.assertEquals("d", resultArr[0].direction);
		Assertions.assertEquals("Parking49251", resultArr[0].clientId);
		Assertions.assertEquals("I-80", resultArr[0].route);
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
		Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertNotNull(resultArr);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("route not supported", resultArr[0].resultMessages.get(0));
		Assertions.assertEquals("d", resultArr[0].direction);
	}

	@Test
	public void testCreateParkingTim_oneDirection_NoItisCodes() throws Exception {

		// Arrange
		String parkingJson = "{\"timParkingList\": [{ \"mileMarker\": 360,\"route\": \"I-80\", \"direction\": \"d\", \"availability\": 345678,\"clientId\": \"Parking49251\" }]}";
		TimParkingList tpl = gson.fromJson(parkingJson, TimParkingList.class);

		// Act
		ResponseEntity<String> data = uut.createParkingTim(tpl);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertNotNull(resultArr);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("success", resultArr[0].resultMessages.get(0));
		Assertions.assertEquals("d", resultArr[0].direction);
		Assertions.assertEquals("Parking49251", resultArr[0].clientId);
		Assertions.assertEquals("I-80", resultArr[0].route);
	}

	@Test
	public void testUpdateParkingTim_oneDirection_success() throws Exception {

		String parkingJson = "{\"timParkingList\": [{ \"mileMarker\": 360,\"route\": \"I-80\", \"direction\": \"d\", \"availability\": 4103,\"clientId\": \"Parking49251\" }]}";
		TimParkingList tpl = gson.fromJson(parkingJson, TimParkingList.class);

		// Act
		ResponseEntity<String> data = uut.createParkingTim(tpl);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertNotNull(resultArr);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("success", resultArr[0].resultMessages.get(0));
		Assertions.assertEquals("Parking49251", resultArr[0].clientId);
		Assertions.assertEquals("I-80", resultArr[0].route);
		Assertions.assertEquals("d", resultArr[0].direction);
	}

	@Test
	public void testDeleteParkingTimsByClientId() throws Exception {

		// Arrange
		String id = "Parking49251";
		// Act
		ResponseEntity<String> data = uut.deleteParkingTim(id);
		// Assert
		Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
		Assertions.assertEquals("success", data.getBody());
	}

	@Test
	public void testGetParkingTims() throws Exception {

		// Arrange

		// Act
		Collection<ActiveTim> data = uut.getParkingTims();

		// Assert
		Assertions.assertEquals(1, data.size());
		Assertions.assertEquals(at, data.iterator().next());
	}

	@Test
	public void testGetParkingTimsByClientId() throws Exception {

		// Arrange
		String clientId = "Parking49251";
		doReturn(parkingTims).when(mockWydotTimService).selectTimByClientId("P", clientId);

		// Act
		Collection<ActiveTim> data = uut.getParkingTimById(clientId);

		// Assert
		Assertions.assertEquals(1, data.size());
		Assertions.assertEquals(at, data.iterator().next());
	}
}
