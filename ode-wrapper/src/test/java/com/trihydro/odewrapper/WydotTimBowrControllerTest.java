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
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.controller.WydotTimBowrController;
import com.trihydro.odewrapper.helpers.SetItisCodes;
import com.trihydro.odewrapper.helpers.SetItisCodes.WeightNotSupportedException;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.TimBowrList;
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
public class WydotTimBowrControllerTest {

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
	WydotTimBowrController uut;

	private Gson gson = new Gson();

	@BeforeEach
	public void setup() {
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
	public void testCreateOrUpdateBowrTim_success() throws Exception {
		// Arrange
		String bowrJson = "{\"timBowrList\":[{\"direction\":\"b\",\"type\":\"BlowOverWeightRestriction\",\"route\":\"I 80\",\"clientId\":\"bowrtestid\",\"startPoint\":{\"latitude\":41.295045,\"longitude\":-105.585043,\"valid\":true},\"endPoint\":{\"latitude\":41.291126,\"longitude\":-105.548155,\"valid\":true},\"startDateTime\":\"2024-06-04T17:34:54.835371Z\",\"endDateTime\":\"2024-06-05T17:34:54.835371Z\",\"data\":25000}]}";
		TimBowrList timBowrList = gson.fromJson(bowrJson, TimBowrList.class);

		// Act
		ResponseEntity<String> data = uut.createOrUpdateBowrTim(timBowrList);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("success", resultArr[0].resultMessages.get(0));
		Assertions.assertEquals("b", resultArr[0].direction);
		Assertions.assertEquals("I 80", resultArr[0].route);
	}

	@Test
	public void testCreateOrUpdateBowrTim_RouteNotSupported() throws Exception {
		// Arrange
		String bowrJson = "{\"timBowrList\":[{\"direction\":\"b\",\"type\":\"BlowOverWeightRestriction\",\"route\":\"notasupportedroute\",\"clientId\":\"bowrtestid\",\"startPoint\":{\"latitude\":41.295045,\"longitude\":-105.585043,\"valid\":true},\"endPoint\":{\"latitude\":41.291126,\"longitude\":-105.548155,\"valid\":true},\"startDateTime\":\"2024-06-04T17:34:54.835371Z\",\"endDateTime\":\"2024-06-05T17:34:54.835371Z\",\"data\":25000}]}";
		TimBowrList timBowrList = gson.fromJson(bowrJson, TimBowrList.class);
		doReturn(false).when(uut).routeSupported("notasupportedroute");

		// Act
		ResponseEntity<String> data = uut.createOrUpdateBowrTim(timBowrList);

		// Assert
		Assertions.assertEquals(HttpStatus.BAD_REQUEST, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("route not supported", resultArr[0].resultMessages.get(0));
		Assertions.assertEquals("b", resultArr[0].direction);
	}

	@Test
	public void testCreateOrUpdateBowrTim_WeightNotSupported() throws Exception {
		// Arrange
		String bowrJson = "{\"timBowrList\":[{\"direction\":\"b\",\"type\":\"BlowOverWeightRestriction\",\"route\":\"I 80\",\"clientId\":\"bowrtestid\",\"startPoint\":{\"latitude\":41.295045,\"longitude\":-105.585043,\"valid\":true},\"endPoint\":{\"latitude\":41.291126,\"longitude\":-105.548155,\"valid\":true},\"startDateTime\":\"2024-06-04T17:34:54.835371Z\",\"endDateTime\":\"2024-06-05T17:34:54.835371Z\",\"data\":12345}]}";
		TimBowrList timBowrList = gson.fromJson(bowrJson, TimBowrList.class);
		lenient().doThrow(WeightNotSupportedException.class).when(setItisCodes).setItisCodesBowr(any());

		// Act
		ResponseEntity<String> data = uut.createOrUpdateBowrTim(timBowrList);

		// Assert
		Assertions.assertEquals(HttpStatus.BAD_REQUEST, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("Weight not supported", resultArr[0].resultMessages.get(0));
		Assertions.assertEquals("b", resultArr[0].direction);
	}

	@Test
	public void testCreateOrUpdateBowrTim_InvalidStartDateTime() {
		// Arrange
		String bowrJson = "{\"timBowrList\":[{\"direction\":\"b\",\"type\":\"BlowOverWeightRestriction\",\"route\":\"I 80\",\"clientId\":\"bowrtestid\",\"startPoint\":{\"latitude\":41.295045,\"longitude\":-105.585043,\"valid\":true},\"endPoint\":{\"latitude\":41.291126,\"longitude\":-105.548155,\"valid\":true},\"startDateTime\":\"20240604T173454Z\",\"endDateTime\":\"2024-06-05T17:34:54.835371Z\",\"data\":25000}]}";
		TimBowrList timBowrList = gson.fromJson(bowrJson, TimBowrList.class);

		// Act
		ResponseEntity<String> data = uut.createOrUpdateBowrTim(timBowrList);

		// Assert
		Assertions.assertEquals(HttpStatus.BAD_REQUEST, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("Invalid startDateTime. Must be in ISO8601-2019 format.", resultArr[0].resultMessages.get(0));
	}

	@Test
	public void testCreateOrUpdateBowrTim_InvalidEndDateTime() {
		// Arrange
		String bowrJson = "{\"timBowrList\":[{\"direction\":\"b\",\"type\":\"BlowOverWeightRestriction\",\"route\":\"I 80\",\"clientId\":\"bowrtestid\",\"startPoint\":{\"latitude\":41.295045,\"longitude\":-105.585043,\"valid\":true},\"endPoint\":{\"latitude\":41.291126,\"longitude\":-105.548155,\"valid\":true},\"startDateTime\":\"2024-06-04T17:34:54.835371Z\",\"endDateTime\":\"20240605T173454Z\",\"data\":25000}]}";
		TimBowrList timBowrList = gson.fromJson(bowrJson, TimBowrList.class);

		// Act
		ResponseEntity<String> data = uut.createOrUpdateBowrTim(timBowrList);

		// Assert
		Assertions.assertEquals(HttpStatus.BAD_REQUEST, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("Invalid endDateTime. Must be in ISO8601-2019 format.", resultArr[0].resultMessages.get(0));
	}

	/**
	 * The ODE expects a format of "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
	 * If milliseconds are not included then the ODE fails to process the message.
	 */
	@Test
	public void testCreateOrUpdateBowrTim_InvalidStartDateTime_MissingMilliseconds() {
		// Arrange
		String bowrJson = "{\"timBowrList\":[{\"direction\":\"b\",\"type\":\"BlowOverWeightRestriction\",\"route\":\"I 80\",\"clientId\":\"bowrtestid\",\"startPoint\":{\"latitude\":41.295045,\"longitude\":-105.585043,\"valid\":true},\"endPoint\":{\"latitude\":41.291126,\"longitude\":-105.548155,\"valid\":true},\"startDateTime\":\"2024-06-04T17:34:54Z\",\"endDateTime\":\"2024-06-05T17:34:54.835371Z\",\"data\":25000}]}";
		TimBowrList timBowrList = gson.fromJson(bowrJson, TimBowrList.class);

		// Act
		ResponseEntity<String> data = uut.createOrUpdateBowrTim(timBowrList);

		// Assert
		Assertions.assertEquals(HttpStatus.BAD_REQUEST, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("Invalid startDateTime. Must be in ISO8601-2019 format.", resultArr[0].resultMessages.get(0));
	}

	@Test
	public void testCreateOrUpdateBowrTim_InvalidStartDateTime_StartTimeIsAfterEndTime() {
		// Arrange
		String bowrJson = "{\"timBowrList\":[{\"direction\":\"b\",\"type\":\"BlowOverWeightRestriction\",\"route\":\"I 80\",\"clientId\":\"bowrtestid\",\"startPoint\":{\"latitude\":41.295045,\"longitude\":-105.585043,\"valid\":true},\"endPoint\":{\"latitude\":41.291126,\"longitude\":-105.548155,\"valid\":true},\"startDateTime\":\"2024-06-06T17:34:54.835371Z\",\"endDateTime\":\"2024-06-05T17:34:54.835371Z\",\"data\":25000}]}";
		TimBowrList timBowrList = gson.fromJson(bowrJson, TimBowrList.class);

		// Act
		ResponseEntity<String> data = uut.createOrUpdateBowrTim(timBowrList);

		// Assert
		Assertions.assertEquals(HttpStatus.BAD_REQUEST, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		Assertions.assertEquals(1, resultArr.length);
		Assertions.assertEquals("Invalid startDateTime. Start time must be before end time.", resultArr[0].resultMessages.get(0));
	
	}

	@Test
	public void testSubmitBowrClear_success() throws Exception {
		// Arrange
		String clientId = "test";
		List<ActiveTim> activeTims = getActiveTims(false);
		when(mockActiveTimService.getActiveTimsByClientIdDirection(any(), any(), any())).thenReturn(activeTims);

		// Act
		ResponseEntity<String> data = uut.submitBowrClear(clientId);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
	}

	@Test
	public void testSubmitBowrClear_CallsResubmitToOde_ExpiresExistingTims() throws Exception {
		// Arrange
		String clientId = "test";
		List<ActiveTim> activeTims = getActiveTims(false);
		when(mockActiveTimService.getActiveTimsByClientIdDirection(any(), any(), any())).thenReturn(activeTims);

		// Act
		ResponseEntity<String> data = uut.submitBowrClear(clientId);

		// Assert
		verify(mockTimGenerationHelper).expireTimAndResubmitToOde(any());
		Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
	}

	/**
	 * If no TIMs are found for the client id then a 400 response is returned.
	 */
	@Test
	public void testSubmitBowrClear_400_NoTimsFound() throws Exception {
		// Arrange
		String clientId = "test";
		List<ActiveTim> activeTims = new ArrayList<>();
		when(mockActiveTimService.getActiveTimsByClientIdDirection(any(), any(), any())).thenReturn(activeTims);

		// Act
		ResponseEntity<String> data = uut.submitBowrClear(clientId);

		// Assert
		Assertions.assertEquals(HttpStatus.BAD_REQUEST, data.getStatusCode());
		Assertions.assertEquals("No active TIMs found for client id: " + clientId, data.getBody());
	}
}
