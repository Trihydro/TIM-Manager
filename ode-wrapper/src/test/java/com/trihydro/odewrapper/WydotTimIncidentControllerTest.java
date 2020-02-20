package com.trihydro.odewrapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
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
import com.trihydro.odewrapper.controller.WydotTimIncidentController;
import com.trihydro.odewrapper.helpers.SetItisCodes;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.TimIncidentList;
import com.trihydro.odewrapper.service.WydotTimService;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@WebAppConfiguration
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class WydotTimIncidentControllerTest {

	protected static BasicConfiguration configuration;
	protected static TimTypeService timTypeService;

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
		doReturn(itisCodesIncident).when(setItisCodes).setItisCodesIncident(any());
		doReturn(itisCodes).when(setItisCodes).getItisCodes();

		doNothing().when(uut).makeTims(any());
	}

	@Test
	public void testCreateIncidentTim_bothDirections_success() throws Exception {

		// Arrange
		String incidentJson = "{\"timIncidentList\": [{ \"toRm\": 370, \"impact\": \"L\", \"fromRm\": 360, \"problem\": \"fire\", \"effect\": \"test\", \"action\": \"test\", \"pk\": 3622, \"highway\": \"I-80\", \"incidentId\": \"IN49251\", \"direction\": \"both\", \"ts\": \"2018-04-16T19:30:05.000Z\" }]}";
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
		assertEquals("both", resultArr[0].direction);
	}

	@Test
	public void testCreateIncidentTim_bothDirections_NoMileposts() throws Exception {

		// Arrange
		String incidentJson = "{\"timIncidentList\": [{ \"toRm\": 370, \"impact\":\"L\", \"fromRm\": 360, \"problem\": \"fire\", \"effect\": \"test\",\"action\": \"test\", \"pk\": 3622, \"highway\": \"I-25\", \"incidentId\":\"IN49251\", \"direction\": \"both\", \"ts\": \"2018-04-16T19:30:05.000Z\"}]}";
		TimIncidentList til = gson.fromJson(incidentJson, TimIncidentList.class);

		// Act
		ResponseEntity<String> data = uut.createIncidentTim(til);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);

		assertEquals("route not supported", resultArr[0].resultMessages.get(0));
		assertEquals("both", resultArr[0].direction);
		assertEquals("IN49251", resultArr[0].clientId);
		assertEquals("I-25", resultArr[0].route);
	}

	@Test
	public void testCreateIncidentTim_bothDirections_NoItisCodes() throws Exception {

		// Arrange
		String incidentJson = "{\"timIncidentList\": [{ \"toRm\": 370, \"impact\":\"L\", \"fromRm\": 360, \"problem\": \"test\", \"effect\": \"test\",\"action\": \"test\", \"pk\": 3622, \"highway\": \"I-80\", \"incidentId\":\"IN49251\", \"direction\": \"both\", \"schedStart\": \"2018-04-16\" }]}";
		TimIncidentList til = gson.fromJson(incidentJson, TimIncidentList.class);

		// Act
		ResponseEntity<String> data = uut.createIncidentTim(til);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals("success", resultArr[0].resultMessages.get(0));
		assertEquals("both", resultArr[0].direction);
	}

	@Test
	public void testCreateIncidentTim_oneDirection_success() throws Exception {

		String incidentJson = "{\"timIncidentList\": [{ \"toRm\": 370, \"impact\":\"L\", \"fromRm\": 360, \"problem\": \"fire\", \"effect\": \"test\",\"action\": \"test\", \"pk\": 3622, \"highway\": \"I-80\", \"incidentId\":\"OD49251\", \"direction\": \"eastbound\", \"ts\":\"2018-04-16T19:30:05.000Z\" }]}";
		TimIncidentList til = gson.fromJson(incidentJson, TimIncidentList.class);

		// Act
		ResponseEntity<String> data = uut.createIncidentTim(til);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals("success", resultArr[0].resultMessages.get(0));
		assertEquals("eastbound", resultArr[0].direction);
		assertEquals("OD49251", resultArr[0].clientId);
		assertEquals("I-80", resultArr[0].route);
	}

	@Test
	public void testCreateIncidentTim_oneDirection_NoMileposts() throws Exception {

		String incidentJson = "{\"timIncidentList\": [{ \"toRm\": 370, \"impact\":\"L\", \"fromRm\": 360, \"problem\": \"fire\", \"effect\": \"test\",\"action\": \"test\", \"pk\": 3622, \"highway\": \"I-25\", \"incidentId\":\"IN49251\", \"direction\": \"eastbound\", \"ts\":\"2018-04-16T19:30:05.000Z\" }]}";
		TimIncidentList til = gson.fromJson(incidentJson, TimIncidentList.class);

		// Act
		ResponseEntity<String> data = uut.createIncidentTim(til);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals("route not supported", resultArr[0].resultMessages.get(0));
		assertEquals("eastbound", resultArr[0].direction);
		assertEquals("IN49251", resultArr[0].clientId);
		assertEquals("I-25", resultArr[0].route);
	}

	@Test
	public void testCreateIncidentTim_oneDirection_NoItisCodes() throws Exception {

		String incidentJson = "{\"timIncidentList\": [{ \"toRm\": 370, \"impact\":\"L\", \"fromRm\": 360, \"problem\": \"test\", \"effect\": \"test\",\"action\": \"test\", \"pk\": 3622, \"highway\": \"I-80\", \"incidentId\":\"IN49251\", \"direction\": \"eastbound\", \"ts\":\"2018-04-16T19:30:05.000Z\" }]}";
		TimIncidentList til = gson.fromJson(incidentJson, TimIncidentList.class);

		// Act
		ResponseEntity<String> data = uut.createIncidentTim(til);

		// Assert
		assertEquals(HttpStatus.OK, data.getStatusCode());
		ControllerResult[] resultArr = gson.fromJson(data.getBody(), ControllerResult[].class);
		assertNotNull(resultArr);
		assertEquals("success", resultArr[0].resultMessages.get(0));
		assertEquals("eastbound", resultArr[0].direction);
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

	// @Test
	// public void testUpdateIncidentTim_oneDirection_success() throws Exception {

	// String incidentJson = "{\"timIncidentList\": [{ \"toRm\": 370, \"impact\":
	// \"L\", \"fromRm\": 360, \"problem\": \"fire\", \"effect\": \"test\",
	// \"action\": \"test\", \"pk\": 3622, \"highway\": \"I-80\", \"incidentId\":
	// \"OD49251\", \"direction\": \"eastbound\", \"ts\":
	// \"2018-04-16T19:30:05.000Z\" }]}";

	// this.mockMvc
	// .perform(MockMvcRequestBuilders.put("/incident-tim").contentType(MediaType.APPLICATION_JSON)
	// .content(incidentJson))
	// .andExpect(MockMvcResultMatchers.status().isOk())
	// .andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
	// .andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("eastbound"))
	// .andExpect(MockMvcResultMatchers.jsonPath("$[0].clientId").value("OD49251"))
	// .andExpect(MockMvcResultMatchers.jsonPath("$[0].route").value("I-80"));
	// }

	// @Test
	// public void testUpdateIncidentTim_bothDirections_success() throws Exception {

	// String incidentJson = "{\"timIncidentList\": [{ \"toRm\": 370, \"impact\":
	// \"L\", \"fromRm\": 360, \"problem\": \"crash\", \"effect\": \"leftClosed\",
	// \"action\": \"caution\", \"pk\": 3622, \"highway\": \"I-80\", \"incidentId\":
	// \"IN49251\", \"direction\": \"both\" }]}";

	// this.mockMvc
	// .perform(MockMvcRequestBuilders.put("/incident-tim").contentType(MediaType.APPLICATION_JSON)
	// .content(incidentJson))
	// .andExpect(MockMvcResultMatchers.status().isOk())
	// .andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
	// .andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("both"))
	// .andExpect(MockMvcResultMatchers.jsonPath("$[0].clientId").value("IN49251"))
	// .andExpect(MockMvcResultMatchers.jsonPath("$[0].route").value("I-80"));
	// }

	// @Ignore
	// @Test
	// public void testDeleteIncidentTimsByClientId() throws Exception {

	// makeTims();

	// List<ActiveTim> activeTimsBeforeDelete =
	// ActiveTimService.getActiveTimsByClientIdDirection("IN49251",
	// getTimTypeId(), null);
	// assertEquals(1, activeTimsBeforeDelete.size());

	// this.mockMvc.perform(MockMvcRequestBuilders.delete("/incident-tim/IN49251"))
	// .andExpect(MockMvcResultMatchers.status().isOk());

	// List<ActiveTim> activeTimsAfterDelete =
	// ActiveTimService.getActiveTimsByClientIdDirection("IN49251",
	// getTimTypeId(), null);
	// assertEquals(0, activeTimsAfterDelete.size());
	// }

	// private void makeTims() {

	// TimIncidentList timIncidentList = new TimIncidentList();
	// List<WydotTimIncident> incidentList = new ArrayList<WydotTimIncident>();
	// WydotTimIncident wydotTim = new WydotTimIncident();

	// wydotTim.setToRm(370.0);
	// wydotTim.setFromRm(360.0);
	// wydotTim.setImpact("L");
	// wydotTim.setProblem("mudslide");
	// wydotTim.setEffect("leftClosed");
	// wydotTim.setAction("caution");
	// wydotTim.setPk(3622);
	// wydotTim.setHighway("I-80");
	// wydotTim.setIncidentId("IN49251");
	// wydotTim.setDirection("both");
	// wydotTim.setSchedStart("2018-04-16T19:30:05.000Z");

	// incidentList.add(wydotTim);
	// timIncidentList.setTimIncidentList(incidentList);

	// WydotTravelerInputData wydotTravelerInputData =
	// createBaseTimUtil.buildTim(wydotTim, "westbound", "80",
	// configuration);

	// OdeLogMetadata odeTimMetadata = new OdeLogMetadata();
	// odeTimMetadata.setOdeReceivedAt(null);

	// Long timId = TimService.insertTim(odeTimMetadata, null,
	// wydotTravelerInputData.getTim(), null, null, null, null,
	// null);

	// TimRsuService.insertTimRsu(timId, 1, 1);

	// ActiveTim activeTim = new ActiveTim();
	// activeTim.setTimId(timId);
	// activeTim.setClientId(wydotTim.getIncidentId());
	// activeTim.setDirection("westbound");
	// activeTim.setMilepostStart(wydotTim.getFromRm());
	// activeTim.setMilepostStop(wydotTim.getToRm());
	// activeTim.setStartDateTime(wydotTravelerInputData.getTim().getDataframes()[0].getStartDateTime());
	// activeTim.setPk(wydotTim.getPk());
	// activeTim.setRoute(wydotTim.getHighway());
	// try {
	// activeTim.setTimTypeId(getTimTypeId());
	// } catch (Exception e) {
	// e.printStackTrace();
	// }

	// ActiveTimService.insertActiveTim(activeTim);
	// }

}
