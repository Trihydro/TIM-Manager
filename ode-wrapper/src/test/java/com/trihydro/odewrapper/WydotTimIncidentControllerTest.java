package com.trihydro.odewrapper;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TimType;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.TimRsuService;
import com.trihydro.library.service.TimService;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;
import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.model.WydotTimList;
import com.trihydro.odewrapper.model.WydotTravelerInputData;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import us.dot.its.jpo.ode.model.OdeLogMetadataReceived;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

 @RunWith(SpringRunner.class)
 @FixMethodOrder(MethodSorters.NAME_ASCENDING)
 @WebAppConfiguration
 @SpringBootTest(classes = Application.class)
 public class WydotTimIncidentControllerTest {	
	
	@Autowired
    private WebApplicationContext wac;
	private MockMvc mockMvc;
	private static Long timTypeId;
	private static final String CONTENT_TYPE = "application/json;charset=UTF-8";

	@Before
    public void setup() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}
	
	@BeforeClass
    public static void setConnection() throws Exception {
		DbUtility.changeConnection("test");			
	}

	@BeforeClass
	public static void getTimTypeId() throws Exception {
		List<TimType> timTypes = TimTypeService.selectAll();		

		TimType timType = timTypes.stream()
		.filter(x -> x.getType().equals("I"))
		.findFirst()
		.orElse(null);	

		timTypeId = timType.getTimTypeId();
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
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/incident-tim")
			.contentType(MediaType.APPLICATION_JSON)
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
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/incident-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(incidentJson))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("route not supported"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("both"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].clientId").value("IN49251"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].route").value("I-25"));
	}

	@Test
	public void testCreateIncidentTim_bothDirections_NoItisCodes() throws Exception {
	
		String incidentJson = "{\"timIncidentList\": [{ \"toRm\": 370, \"impact\": \"L\", \"fromRm\": 360, \"problem\": \"test\", \"effect\": \"test\", \"action\": \"test\", \"pk\": 3622, \"highway\": \"I-80\", \"incidentId\": \"IN49251\", \"direction\": \"both\", \"ts\": \"2018-04-16T19:30:05.000Z\" }]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/incident-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(incidentJson))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("both"));
	}

	@Test
	public void testCreateIncidentTim_oneDirection_success() throws Exception {
	
		String incidentJson = "{\"timIncidentList\": [{ \"toRm\": 370, \"impact\": \"L\", \"fromRm\": 360, \"problem\": \"fire\", \"effect\": \"test\", \"action\": \"test\", \"pk\": 3622, \"highway\": \"I-80\", \"incidentId\": \"OD49251\", \"direction\": \"eastbound\", \"ts\": \"2018-04-16T19:30:05.000Z\" }]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/incident-tim")
			.contentType(MediaType.APPLICATION_JSON)
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
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/incident-tim")
			.contentType(MediaType.APPLICATION_JSON)
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
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/incident-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(incidentJson))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("eastbound"));
	}

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
		  
		this.mockMvc.perform(MockMvcRequestBuilders.put("/incident-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(incidentJson))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("eastbound"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].clientId").value("OD49251"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].route").value("I-80"));
	}

	@Test
	public void testUpdateIncidentTim_bothDirections_success() throws Exception {
	
		String incidentJson = "{\"timIncidentList\": [{ \"toRm\": 370, \"impact\": \"L\", \"fromRm\": 360, \"problem\": \"fire\", \"effect\": \"test\", \"action\": \"test\", \"pk\": 3622, \"highway\": \"I-80\", \"incidentId\": \"IN49251\", \"direction\": \"both\", \"ts\": \"2018-04-16T19:30:05.000Z\" }]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.put("/incident-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(incidentJson))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("both"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].clientId").value("IN49251"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].route").value("I-80"));
	}

	@Test
	public void testDeleteIncidentTimsByClientId() throws Exception {
		
		makeTims();

		List<ActiveTim> activeTimsBeforeDelete = ActiveTimService.getActiveTimsByClientIdTimId("IN49251", timTypeId);
		assertEquals(1, activeTimsBeforeDelete.size());	

		this.mockMvc.perform(MockMvcRequestBuilders.delete("/incident-tim/IN49251"))
			.andExpect(MockMvcResultMatchers.status().isOk());

		List<ActiveTim> activeTimsAfterDelete = ActiveTimService.getActiveTimsByClientIdTimId("IN49251", timTypeId);
		assertEquals(0, activeTimsAfterDelete.size());	
	}
	
	private void makeTims(){

		WydotTimList wydotTimList = new WydotTimList();
		List<WydotTim> incidentList = new ArrayList<WydotTim>();
		WydotTim wydotTim = new WydotTim();

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
		wydotTim.setTs("2018-04-16T19:30:05.000Z");

		incidentList.add(wydotTim);
		wydotTimList.setTimIncidentList(incidentList);

		WydotTravelerInputData wydotTravelerInputData = CreateBaseTimUtil.buildTim(wydotTim, "westbound", "80");

		OdeLogMetadataReceived odeTimMetadata = new OdeLogMetadataReceived();
		odeTimMetadata.setOdeReceivedAt(null);

		Long timId = TimService.insertTim(odeTimMetadata, wydotTravelerInputData.getTim());

		TimRsuService.insertTimRsu(timId, 1);

		ActiveTim activeTim = new ActiveTim();
		activeTim.setTimId(timId);
		activeTim.setClientId(wydotTim.getIncidentId());
		activeTim.setDirection("westbound");
		activeTim.setMilepostStart(wydotTim.getFromRm());
		activeTim.setMilepostStop(wydotTim.getToRm());
		activeTim.setStartDateTime(wydotTravelerInputData.getTim().getDataframes()[0].getStartDateTime());
		activeTim.setPk(wydotTim.getPk());
		activeTim.setRoute(wydotTim.getRoute());
		activeTim.setTimTypeId(timTypeId);

		ActiveTimService.insertActiveTim(activeTim);		
	}

}
