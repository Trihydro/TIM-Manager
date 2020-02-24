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
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.controller.WydotTimVslController;
import com.trihydro.odewrapper.helpers.SetItisCodes;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.TimVslList;
import com.trihydro.odewrapper.service.WydotTimService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations = "classpath:application-test.properties")
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

	@InjectMocks
	@Spy
	WydotTimVslController uut;

	private Gson gson = new Gson();

	@Before
	public void setup() throws Exception {
		PowerMockito.mockStatic(ActiveTimService.class);
		List<ItisCode> itisCodes = new ArrayList<>();
		ItisCode ic = new ItisCode();
		ic.setCategoryId(-1);
		ic.setDescription("description");
		ic.setItisCode(-2);
		ic.setItisCodeId(-3);
		itisCodes.add(ic);
		List<String> itisCodesIncident = new ArrayList<>();
		itisCodesIncident.add("531");
		doReturn(itisCodesIncident).when(mockSetItisCodes).setItisCodesVsl(any());
		doReturn(itisCodes).when(mockSetItisCodes).getItisCodes();

		doNothing().when(uut).processRequestAsync(any());
		doReturn(true).when(uut).routeSupported(isA(String.class));
	}

	@Test
	public void testCreateVSLTim_bothDirections_success() throws Exception {

		// Arrange
		String incidentJson = "{\"timVslList\": [{ \"toRm\": 370, \"fromRm\": 360, \"route\": \"I-80\", \"direction\": \"both\", \"speed\": 45, \"deviceId\": \"V004608\"  }]}";
		TimVslList timVslList = gson.fromJson(incidentJson, TimVslList.class);

		// Act
		ResponseEntity<String> data = uut.createUpdateVslTim(timVslList);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals(1, resultArr.length);
		assertEquals("success", resultArr[0].resultMessages.get(0));
		assertEquals("both", resultArr[0].direction);
	}

	@Test
	public void testCreateVSLTim_oneDirection_success() throws Exception {

		// Arrange
		String incidentJson = "{\"timVslList\": [{ \"toRm\": 370, \"fromRm\": 360,\"route\": \"I-80\", \"direction\": \"eastbound\", \"speed\": 45, \"deviceId\":\"V004608\" }]}";
		TimVslList timVslList = gson.fromJson(incidentJson, TimVslList.class);

		// Act
		ResponseEntity<String> data = uut.createUpdateVslTim(timVslList);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals(1, resultArr.length);
		assertEquals("success", resultArr[0].resultMessages.get(0));
		assertEquals("eastbound", resultArr[0].direction);
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
		assertEquals(1, data.size());
		assertEquals(at, data.iterator().next());
	}

}
