package com.trihydro.odewrapper;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TimType;
import com.trihydro.library.model.WydotTravelerInputData;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.TimRsuService;
import com.trihydro.library.service.TimService;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;
import com.trihydro.odewrapper.model.TimIncidentList;
import com.trihydro.odewrapper.model.WydotTimIncident;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import us.dot.its.jpo.ode.model.OdeLogMetadata;

//TODO: update once logic is moved
@Ignore
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@WebAppConfiguration
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class WydotTimIncidentControllerTest {

	@Autowired
	private WebApplicationContext wac;
	private MockMvc mockMvc;
	private static final String CONTENT_TYPE = "application/json;charset=UTF-8";

	protected static BasicConfiguration configuration;
	protected static TimTypeService timTypeService;

	@Autowired
	public void setConfiguration(BasicConfiguration configurationRhs, TimTypeService _timTypeService) {
		configuration = configurationRhs;
		timTypeService = _timTypeService;
	}

	@Before
	public void setup() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	public static Long getTimTypeId() throws Exception {
		List<TimType> timTypes = timTypeService.selectAll();

		TimType timType = timTypes.stream().filter(x -> x.getType().equals("I")).findFirst().orElse(null);

		Long timTypeId = timType.getTimTypeId();

		return timTypeId;
	}

	@Test
	public void givenWac_whenServletContext_thenItProvidesGreetController() {
		ServletContext servletContext = wac.getServletContext();

		Assert.assertNotNull(servletContext);
		Assert.assertTrue(servletContext instanceof MockServletContext);
		Assert.assertNotNull(wac.getBean("wydotTimIncidentController"));
	}

	@Test
	public void testCreateIncidentTim_bothDirections_success() throws Exception {

		String incidentJson = "{\"timIncidentList\": [{ \"toRm\": 370, \"impact\": \"L\", \"fromRm\": 360, \"problem\": \"fire\", \"effect\": \"test\", \"action\": \"test\", \"pk\": 3622, \"highway\": \"I-80\", \"incidentId\": \"IN49251\", \"direction\": \"both\", \"ts\": \"2018-04-16T19:30:05.000Z\" }]}";

		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/incident-tim").contentType(MediaType.APPLICATION_JSON)
						.content(incidentJson))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].clientId").value("IN49251"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].route").value("I-80"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("both"));
	}

	@Test
	public void testCreateIncidentTim_bothDirections_NoMileposts() throws Exception {

		String incidentJson = "{\"timIncidentList\": [{ \"toRm\": 370, \"impact\": \"L\", \"fromRm\": 360, \"problem\": \"fire\", \"effect\": \"test\", \"action\": \"test\", \"pk\": 3622, \"highway\": \"I-25\", \"incidentId\": \"IN49251\", \"direction\": \"both\", \"ts\": \"2018-04-16T19:30:05.000Z\" }]}";

		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/incident-tim").contentType(MediaType.APPLICATION_JSON)
						.content(incidentJson))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("route not supported"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("both"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].clientId").value("IN49251"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].route").value("I-25"));
	}

	@Test
	public void testCreateIncidentTim_bothDirections_NoItisCodes() throws Exception {

		String incidentJson = "{\"timIncidentList\": [{ \"toRm\": 370, \"impact\": \"L\", \"fromRm\": 360, \"problem\": \"test\", \"effect\": \"test\", \"action\": \"test\", \"pk\": 3622, \"highway\": \"I-80\", \"incidentId\": \"IN49251\", \"direction\": \"both\", \"schedStart\": \"2018-04-16\" }]}";

		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/incident-tim").contentType(MediaType.APPLICATION_JSON)
						.content(incidentJson))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("both"));
	}

	@Test
	public void testCreateIncidentTim_oneDirection_success() throws Exception {

		String incidentJson = "{\"timIncidentList\": [{ \"toRm\": 370, \"impact\": \"L\", \"fromRm\": 360, \"problem\": \"fire\", \"effect\": \"test\", \"action\": \"test\", \"pk\": 3622, \"highway\": \"I-80\", \"incidentId\": \"OD49251\", \"direction\": \"eastbound\", \"ts\": \"2018-04-16T19:30:05.000Z\" }]}";

		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/incident-tim").contentType(MediaType.APPLICATION_JSON)
						.content(incidentJson))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("eastbound"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].clientId").value("OD49251"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].route").value("I-80"));
	}

	@Test
	public void testCreateIncidentTim_oneDirection_NoMileposts() throws Exception {

		String incidentJson = "{\"timIncidentList\": [{ \"toRm\": 370, \"impact\": \"L\", \"fromRm\": 360, \"problem\": \"fire\", \"effect\": \"test\", \"action\": \"test\", \"pk\": 3622, \"highway\": \"I-25\", \"incidentId\": \"IN49251\", \"direction\": \"eastbound\", \"ts\": \"2018-04-16T19:30:05.000Z\" }]}";

		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/incident-tim").contentType(MediaType.APPLICATION_JSON)
						.content(incidentJson))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("route not supported"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("eastbound"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].clientId").value("IN49251"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].route").value("I-25"));
	}

	@Test
	public void testCreateIncidentTim_oneDirection_NoItisCodes() throws Exception {

		String incidentJson = "{\"timIncidentList\": [{ \"toRm\": 370, \"impact\": \"L\", \"fromRm\": 360, \"problem\": \"test\", \"effect\": \"test\", \"action\": \"test\", \"pk\": 3622, \"highway\": \"I-80\", \"incidentId\": \"IN49251\", \"direction\": \"eastbound\", \"ts\": \"2018-04-16T19:30:05.000Z\" }]}";

		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/incident-tim").contentType(MediaType.APPLICATION_JSON)
						.content(incidentJson))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("eastbound"));
	}

	// TODO: once we migrate functionality to REST service, we want to move these
	// tests there as well
	@Ignore
	@Test
	public void testGetIncidentTims() throws Exception {

		makeTims();

		ResultActions resultActions = this.mockMvc.perform(MockMvcRequestBuilders.get("/incident-tim"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(CONTENT_TYPE))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].pk").value(3622))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].pk").value(3622))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("westbound"));

		MvcResult mvcResult = resultActions.andReturn();
		String result = mvcResult.getResponse().getContentAsString();
		System.out.println(result);
	}

	@Ignore
	@Test
	public void testGetIncidentTimsByClientId() throws Exception {

		makeTims();

		ResultActions resultActions = this.mockMvc.perform(MockMvcRequestBuilders.get("/incident-tim/IN49251"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(CONTENT_TYPE))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].pk").value(3622))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].pk").value(3622))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("westbound"));

		MvcResult mvcResult = resultActions.andReturn();
		String result = mvcResult.getResponse().getContentAsString();
		System.out.println(result);
	}

	@Test
	public void testUpdateIncidentTim_oneDirection_success() throws Exception {

		String incidentJson = "{\"timIncidentList\": [{ \"toRm\": 370, \"impact\": \"L\", \"fromRm\": 360, \"problem\": \"fire\", \"effect\": \"test\", \"action\": \"test\", \"pk\": 3622, \"highway\": \"I-80\", \"incidentId\": \"OD49251\", \"direction\": \"eastbound\", \"ts\": \"2018-04-16T19:30:05.000Z\" }]}";

		this.mockMvc
				.perform(MockMvcRequestBuilders.put("/incident-tim").contentType(MediaType.APPLICATION_JSON)
						.content(incidentJson))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("eastbound"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].clientId").value("OD49251"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].route").value("I-80"));
	}

	@Test
	public void testUpdateIncidentTim_bothDirections_success() throws Exception {

		String incidentJson = "{\"timIncidentList\": [{ \"toRm\": 370, \"impact\": \"L\", \"fromRm\": 360, \"problem\": \"crash\", \"effect\": \"leftClosed\", \"action\": \"caution\", \"pk\": 3622, \"highway\": \"I-80\", \"incidentId\": \"IN49251\", \"direction\": \"both\" }]}";

		this.mockMvc
				.perform(MockMvcRequestBuilders.put("/incident-tim").contentType(MediaType.APPLICATION_JSON)
						.content(incidentJson))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("both"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].clientId").value("IN49251"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].route").value("I-80"));
	}

	@Ignore
	@Test
	public void testDeleteIncidentTimsByClientId() throws Exception {

		makeTims();

		List<ActiveTim> activeTimsBeforeDelete = ActiveTimService.getActiveTimsByClientIdDirection("IN49251",
				getTimTypeId(), null);
		assertEquals(1, activeTimsBeforeDelete.size());

		this.mockMvc.perform(MockMvcRequestBuilders.delete("/incident-tim/IN49251"))
				.andExpect(MockMvcResultMatchers.status().isOk());

		List<ActiveTim> activeTimsAfterDelete = ActiveTimService.getActiveTimsByClientIdDirection("IN49251",
				getTimTypeId(), null);
		assertEquals(0, activeTimsAfterDelete.size());
	}

	private void makeTims() {

		TimIncidentList timIncidentList = new TimIncidentList();
		List<WydotTimIncident> incidentList = new ArrayList<WydotTimIncident>();
		WydotTimIncident wydotTim = new WydotTimIncident();

		wydotTim.setToRm(370.0);
		wydotTim.setFromRm(360.0);
		wydotTim.setImpact("L");
		wydotTim.setProblem("mudslide");
		wydotTim.setEffect("leftClosed");
		wydotTim.setAction("caution");
		wydotTim.setPk(3622);
		wydotTim.setHighway("I-80");
		wydotTim.setIncidentId("IN49251");
		wydotTim.setDirection("both");
		wydotTim.setSchedStart("2018-04-16T19:30:05.000Z");

		incidentList.add(wydotTim);
		timIncidentList.setTimIncidentList(incidentList);

		WydotTravelerInputData wydotTravelerInputData = CreateBaseTimUtil.buildTim(wydotTim, "westbound", "80",
				configuration);

		OdeLogMetadata odeTimMetadata = new OdeLogMetadata();
		odeTimMetadata.setOdeReceivedAt(null);

		Long timId = TimService.insertTim(odeTimMetadata, null, wydotTravelerInputData.getTim(), null, null, null, null,
				null);

		TimRsuService.insertTimRsu(timId, 1, 1);

		ActiveTim activeTim = new ActiveTim();
		activeTim.setTimId(timId);
		activeTim.setClientId(wydotTim.getIncidentId());
		activeTim.setDirection("westbound");
		activeTim.setMilepostStart(wydotTim.getFromRm());
		activeTim.setMilepostStop(wydotTim.getToRm());
		activeTim.setStartDateTime(wydotTravelerInputData.getTim().getDataframes()[0].getStartDateTime());
		activeTim.setPk(wydotTim.getPk());
		activeTim.setRoute(wydotTim.getHighway());
		try {
			activeTim.setTimTypeId(getTimTypeId());
		} catch (Exception e) {
			e.printStackTrace();
		}

		ActiveTimService.insertActiveTim(activeTim);
	}

}
