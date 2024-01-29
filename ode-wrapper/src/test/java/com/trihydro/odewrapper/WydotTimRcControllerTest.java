package com.trihydro.odewrapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.trihydro.library.helpers.CreateBaseTimUtil;
import com.trihydro.library.helpers.TimGenerationHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.controller.WydotTimRcController;
import com.trihydro.odewrapper.helpers.SetItisCodes;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.TimRcList;

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
public class WydotTimRcControllerTest {

	@Mock
	BasicConfiguration mockBasicConfiguration;
	@Mock
	TimTypeService mockTimTypeService;
	@Mock
	TimGenerationHelper mockTimGenerationHelper;
	@Mock
	CreateBaseTimUtil mockCreateBaseTimUtil;
	@Mock
	SetItisCodes setItisCodes;
	@Mock
	Utility utility;
	@Mock
	ActiveTimService mockActiveTimService;

	@InjectMocks
	@Spy
	WydotTimRcController uut;

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
		lenient().doReturn(itisCodesIncident).when(setItisCodes).setItisCodesRc(any());
		lenient().doReturn(itisCodes).when(setItisCodes).getItisCodes();

		lenient().doReturn(true).when(uut).routeSupported(isA(String.class));
	}

	private List<ActiveTim> getActiveTims(boolean isSat) {
        List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
        ActiveTim aTim = new ActiveTim();
        ActiveTim aTim2 = new ActiveTim();
        aTim.setActiveTimId(-1l);
        aTim2.setActiveTimId(-2l);
        if (isSat) {
            aTim.setSatRecordId("C27CBB9F");
            aTim2.setSatRecordId("86E03786");
        } else {
            aTim.setStartPoint(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2)));
            aTim.setEndPoint(new Coordinate(BigDecimal.valueOf(3), BigDecimal.valueOf(4)));
            aTim.setTimId(-10l);
            aTim2.setStartPoint(new Coordinate(BigDecimal.valueOf(5), BigDecimal.valueOf(6)));
            aTim2.setEndPoint(new Coordinate(BigDecimal.valueOf(7), BigDecimal.valueOf(8)));
            aTim2.setTimId(-20l);
        }
        activeTims.add(aTim);
        activeTims.add(aTim2);

        return activeTims;
    }

	@Test
	public void testCreateRcTim_bothDirections_success() throws Exception {

		// Arrange
		String rcJson = "{\"timRcList\": [{ \"route\": \"I80\", \"startPoint\": {\"latitude\": 41.161446, \"longitude\": -104.653162},\"endPoint\": {\"latitude\": 41.170465, \"longitude\": -104.085578}, \"roadCode\": \"LARI80WQDHLD\", \"direction\": \"b\",\"advisory\": [4871]} ]}";
		TimRcList timRcList = gson.fromJson(rcJson, TimRcList.class);

		// Act
		ResponseEntity<String> data = uut.createUpdateRoadConditionsTim(timRcList);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertNotNull(resultArr);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("success", resultArr[0].resultMessages.get(0));
		Assertions.assertEquals("b", resultArr[0].direction);
		Assertions.assertEquals("I80", resultArr[0].route);
	}

	@Test
	public void testCreateRcTim_bothDirections_NoMileposts() throws Exception {

		// Arrange
		String rcJson = "{\"timRcList\": [{ \"route\": \"I70\", \"roadCode\": \"LARI80WQDHLD\", \"startPoint\": {\"latitude\": 41.161446, \"longitude\": -104.653162},\"endPoint\": {\"latitude\": 41.170465, \"longitude\": -104.085578}, \"direction\":\"b\",\"advisory\": [4871]} ]}";
		TimRcList timRcList = gson.fromJson(rcJson, TimRcList.class);
		doReturn(false).when(uut).routeSupported("I70");

		// Act
		ResponseEntity<String> data = uut.createUpdateRoadConditionsTim(timRcList);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertNotNull(resultArr);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("route not supported", resultArr[0].resultMessages.get(0));
		Assertions.assertEquals("b", resultArr[0].direction);
	}

	@Test
	public void testCreateRcTim_bothDirections_NoItisCodes() throws Exception {

		// Arrange
		String rcJson = "{\"timRcList\": [{ \"route\": \"I80\", \"startPoint\": {\"latitude\": 41.161446, \"longitude\": -104.653162},\"endPoint\": {\"latitude\": 41.170465, \"longitude\": -104.085578},\"roadCode\": \"LARI80WQDHLD\", \"direction\":\"b\",\"advisory\": [11]} ]}";
		TimRcList timRcList = gson.fromJson(rcJson, TimRcList.class);

		// Act
		ResponseEntity<String> data = uut.createUpdateRoadConditionsTim(timRcList);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertNotNull(resultArr);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("success", resultArr[0].resultMessages.get(0));
		Assertions.assertEquals("b", resultArr[0].direction);
		Assertions.assertEquals("I80", resultArr[0].route);
	}

	@Test
	public void testCreateRcTim_oneDirection_success() throws Exception {

		// Arrange
		String rcJson = "{\"timRcList\": [{ \"route\": \"I80\", \"startPoint\": {\"latitude\": 41.161446, \"longitude\": -104.653162},\"endPoint\": {\"latitude\": 41.170465, \"longitude\": -104.085578},\"roadCode\": \"LARI80WQDHLD\", \"direction\":\"i\",\"advisory\": [4871]} ]}";
		TimRcList timRcList = gson.fromJson(rcJson, TimRcList.class);

		// Act
		ResponseEntity<String> data = uut.createUpdateRoadConditionsTim(timRcList);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertNotNull(resultArr);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("success", resultArr[0].resultMessages.get(0));
		Assertions.assertEquals("i", resultArr[0].direction);
		Assertions.assertEquals("I80", resultArr[0].route);
	}

	@Test
	public void testCreateVslTim_oneDirection_NoMileposts() throws Exception {

		// Arrange
		String rcJson = "{\"timRcList\": [{ \"route\": \"I80\", \"startPoint\": {\"latitude\": 41.161446, \"longitude\": -104.653162},\"endPoint\": {\"latitude\": 41.170465, \"longitude\": -104.085578},\"roadCode\": \"LARI80WQDHLD\", \"direction\":\"i\",\"advisory\": [4871]} ]}";
		TimRcList timRcList = gson.fromJson(rcJson, TimRcList.class);

		// Act
		ResponseEntity<String> data = uut.createUpdateRoadConditionsTim(timRcList);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertNotNull(resultArr);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("success", resultArr[0].resultMessages.get(0));
		Assertions.assertEquals("i", resultArr[0].direction);
		Assertions.assertEquals("I80", resultArr[0].route);
	}

	@Test
	public void testCreateRcTim_oneDirection_NoItisCodes() throws Exception {

		// Arrange
		String rcJson = "{\"timRcList\": [{ \"route\": \"I80\", \"startPoint\": {\"latitude\": 41.161446, \"longitude\": -104.653162},\"endPoint\": {\"latitude\": 41.170465, \"longitude\": -104.085578},\"roadCode\": \"LARI80WQDHLD\", \"direction\":\"i\",\"advisory\": [11]} ]}";
		TimRcList timRcList = gson.fromJson(rcJson, TimRcList.class);

		// Act
		ResponseEntity<String> data = uut.createUpdateRoadConditionsTim(timRcList);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertNotNull(resultArr);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("success", resultArr[0].resultMessages.get(0));
		Assertions.assertEquals("i", resultArr[0].direction);
		Assertions.assertEquals("I80", resultArr[0].route);
	}

	@Test
	public void testAllClear() throws Exception {

		// Arrange
		String rcJson = "{\"timRcList\": [{ \"route\": \"I80\", \"startPoint\": {\"latitude\": 41.161446, \"longitude\": -104.653162},\"endPoint\": {\"latitude\": 41.170465, \"longitude\": -104.085578},\"roadCode\": \"LARI80WQDHLD\", \"direction\":\"d\",\"advisory\": [5378]} ]}";
		TimRcList timRcList = gson.fromJson(rcJson, TimRcList.class);

		// Act
		ResponseEntity<String> data = uut.submitAllClearRoadConditionsTim(timRcList);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertNotNull(resultArr);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("success", resultArr[0].resultMessages.get(0));

		// Parameters required for AC
		Assertions.assertEquals("d", resultArr[0].direction);
		Assertions.assertEquals("LARI80WQDHLD", resultArr[0].clientId);

		// Route isn't required for an AC, so it isn't set in the response.
		Assertions.assertEquals(null, resultArr[0].route);
	}

	@Test
	public void testAllClear_CallsResubmitToOde_ExpiresExistingTims() throws Exception {
		// Arrange
		String rcJson = "{\"timRcList\": [{ \"route\": \"I80\", \"startPoint\": {\"latitude\": 41.161446, \"longitude\": -104.653162},\"endPoint\": {\"latitude\": 41.170465, \"longitude\": -104.085578},\"roadCode\": \"LARI80WQDHLD\", \"direction\":\"d\",\"advisory\": [5378]} ]}";
		TimRcList timRcList = gson.fromJson(rcJson, TimRcList.class);
		List<ActiveTim> activeTims = getActiveTims(false);
		when(mockActiveTimService.getActiveTimsByClientIdDirection(any(), any(), any())).thenReturn(activeTims);

		// Act
		ResponseEntity<String> data = uut.submitAllClearRoadConditionsTim(timRcList);

		// Assert
		verify(mockTimGenerationHelper).expireTimAndResubmitToOde(any());
		Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
	}

	@Test
	public void testAllClear_Bidirectional() throws Exception {
		// Arrange
		String rcJson = "{\"timRcList\": [{ \"route\": \"I80\", \"startPoint\": {\"latitude\": 41.161446, \"longitude\": -104.653162},\"endPoint\": {\"latitude\": 41.170465, \"longitude\": -104.085578},\"roadCode\": \"LARI80WQDHLD\", \"direction\":\"b\",\"advisory\": [5378]} ]}";
		TimRcList timRcList = gson.fromJson(rcJson, TimRcList.class);

		// Act
		ResponseEntity<String> data = uut.submitAllClearRoadConditionsTim(timRcList);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertNotNull(resultArr);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("success", resultArr[0].resultMessages.get(0));

		// Parameters required for AC
		Assertions.assertEquals("b", resultArr[0].direction);
		Assertions.assertEquals("LARI80WQDHLD", resultArr[0].clientId);

		// Route isn't required for an AC, so it isn't set in the response.
		Assertions.assertEquals(null, resultArr[0].route);
		verify(mockActiveTimService).getActiveTimsByClientIdDirection("LARI80WQDHLD", null, null);
	}
}
