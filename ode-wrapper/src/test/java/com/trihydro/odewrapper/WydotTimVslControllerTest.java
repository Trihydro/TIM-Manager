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
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.library.service.WydotTimService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.controller.WydotTimVslController;
import com.trihydro.odewrapper.helpers.SetItisCodes;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.TimVslList;

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
public class WydotTimVslControllerTest {

	@Mock
	BasicConfiguration mockBasicConfiguration;
	@Mock
	TimTypeService mockTimTypeService;
	@Mock
	WydotTimService mockWydotTimService;
	@Mock
	CreateBaseTimUtil mockCreateBaseTimUtil;
	@Mock
	SetItisCodes mockSetItisCodes;
	@Mock
	ActiveTimService mockActiveTimService;
	@Mock
	Utility utility;

	@InjectMocks
	@Spy
	WydotTimVslController uut;

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
		lenient().doReturn(itisCodesIncident).when(mockSetItisCodes).setItisCodesVsl(any());
		lenient().doReturn(itisCodes).when(mockSetItisCodes).getItisCodes();

		lenient().doNothing().when(uut).processRequestAsync(any());
		lenient().doReturn(true).when(uut).routeSupported(isA(String.class));
	}

	@Test
	public void testCreateVSLTim_bothDirections_success() throws Exception {

		// Arrange
		String incidentJson = "{\"timVslList\": [{ \"startPoint\": {\"latitude\": 41.161446, \"longitude\": -104.653162},\"endPoint\": {\"latitude\": 41.170465, \"longitude\": -104.085578},\"route\": \"I-80\", \"direction\": \"b\", \"speed\": 45, \"deviceId\":\"V004608\" }]}";
		TimVslList timVslList = gson.fromJson(incidentJson, TimVslList.class);

		// Act
		ResponseEntity<String> data = uut.createUpdateVslTim(timVslList);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertNotNull(resultArr);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("success", resultArr[0].resultMessages.get(0));
		Assertions.assertEquals("b", resultArr[0].direction);
	}

	@Test
	public void testCreateVSLTim_oneDirection_success() throws Exception {

		// Arrange
		String incidentJson = "{\"timVslList\": [{ \"startPoint\": {\"latitude\": 41.161446, \"longitude\": -104.653162},\"endPoint\": {\"latitude\": 41.170465, \"longitude\": -104.085578},\"route\": \"I-80\", \"direction\": \"i\", \"speed\": 45, \"deviceId\":\"V004608\" }]}";
		TimVslList timVslList = gson.fromJson(incidentJson, TimVslList.class);

		// Act
		ResponseEntity<String> data = uut.createUpdateVslTim(timVslList);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertNotNull(resultArr);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("success", resultArr[0].resultMessages.get(0));
		Assertions.assertEquals("i", resultArr[0].direction);
	}

	@Test
	public void getVslTims_SUCCESS() {
		// Arrange
		List<ActiveTim> ats = new ArrayList<>();
		ActiveTim at = new ActiveTim();
		at.setActiveTimId(-1l);
		at.setClientId("clientId");
		at.setDirection("direction");
		ats.add(at);
		doReturn(ats).when(mockWydotTimService).selectTimsByType("VSL");
		// Act
		Collection<ActiveTim> data = uut.getVslTims();

		// Assert
		Assertions.assertEquals(1, data.size());
		Assertions.assertEquals(at, data.iterator().next());
	}

}
