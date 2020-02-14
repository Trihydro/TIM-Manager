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
import com.trihydro.odewrapper.model.TimRwList;
import com.trihydro.odewrapper.model.WydotTimRw;

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

@Ignore
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@WebAppConfiguration
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class WydotTimRwControllerTest {

	@Autowired
	private WebApplicationContext wac;
	private MockMvc mockMvc;
	private static final String CONTENT_TYPE = "application/json;charset=UTF-8";

	protected static BasicConfiguration configuration;

	@Autowired
	public void setConfiguration(BasicConfiguration configurationRhs) {
		configuration = configurationRhs;
	}

	@Before
	public void setup() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	public static Long getTimTypeId() throws Exception {
		List<TimType> timTypes = TimTypeService.selectAll();

		TimType timType = timTypes.stream().filter(x -> x.getType().equals("RW")).findFirst().orElse(null);

		Long timTypeId = timType.getTimTypeId();

		return timTypeId;
	}

	@Test
	public void givenWac_whenServletContext_thenItProvidesGreetController() {
		ServletContext servletContext = wac.getServletContext();

		Assert.assertNotNull(servletContext);
		Assert.assertTrue(servletContext instanceof MockServletContext);
		Assert.assertNotNull(wac.getBean("wydotTimRwController"));
	}

	@Test
	public void testCreateRwTim_oneDirection_success2() throws Exception {

		String rwJson = "{\"timRwList\": [{\"toRm\": 375,\"fromRm\": 370,\"direction\": \"eastbound\",\"surface\": \"G\",\"buffers\": [{\"distance\": 1,\"action\": \"leftClosed\",\"units\": \"miles\"},{\"distance\": 0.5,\"action\": \"workers\",\"units\": \"miles\"}],\"schedStart\": \"2016-06-23\",\"delays\": [{\"code\": 19,\"debug\": {\"codeStr\": \"1\",\"enabled\": true,\"key\": 8399},\"firstDay\": \"20130228\",\"lastDay\": \"20500430\",\"dailyStartTime\": \"0000\",\"id\": 8350,\"dailyEndTime\": \"0000\",\"daysOfWeek\": \"SMTWTFS\"}],\"disabled\": false,\"id\": 8359,\"highway\": \"I-80\"}]}";

		ResultActions resultActions = this.mockMvc
				.perform(MockMvcRequestBuilders.post("/rw-tim").contentType(MediaType.APPLICATION_JSON).content(rwJson))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].clientId").value("8359"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].route").value("I-80"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("eastbound"));

		MvcResult mvcResult = resultActions.andReturn();
		String result = mvcResult.getResponse().getContentAsString();
		System.out.println(result);
	}

	@Test
	public void testCreateRwTim_bothDirections_NoMileposts() throws Exception {

		String rwJson = "{ \"timRwList\": [ {\"fromRm\": 350,\"toRm\": 360,	\"highway\": \"I-80\",\"pk\": \"15917\",\"id\": \"15917\",\"direction\": \"westbound\",\"surface\": \"P\",\"schedStart\": \"2018-04-16\"}]}";

		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/rw-tim").contentType(MediaType.APPLICATION_JSON).content(rwJson))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("westbound"));
	}

	@Test
	public void testCreateRwTim_bothDirections_NoItisCodes() throws Exception {

		String rwJson = "{ \"timRwList\": [ {\"fromRm\": 350,\"toRm\": 360,	\"highway\": \"I-80\",\"pk\": \"15917\",\"id\": \"15917\",\"direction\": \"westbound\",\"surface\": \"P\",\"schedStart\": \"2018-04-16\"}]}";

		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/rw-tim").contentType(MediaType.APPLICATION_JSON).content(rwJson))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].clientId").value("15917"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].route").value("I-80"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("westbound"));
	}

	@Test
	public void testCreateRwTim_oneDirection_success() throws Exception {

		String rwJson = "{ \"timRwList\": [ {\"fromRm\": 350,\"toRm\": 360,	\"highway\": \"I-80\",\"pk\": \"15917\",\"id\": \"15917\",\"direction\": \"westbound\",\"surface\": \"P\",\"schedStart\": \"2018-04-16\"}]}";

		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/rw-tim").contentType(MediaType.APPLICATION_JSON).content(rwJson))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].clientId").value("15917"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("westbound"));
	}

	@Test
	public void testCreateRwTim_oneDirection_NoMileposts() throws Exception {

		String rwJson = "{ \"timRwList\": [ {\"fromRm\": 350,\"toRm\": 360,	\"highway\": \"I-80\",\"pk\": \"15917\",\"id\": \"15917\",\"direction\": \"westbound\",\"surface\": \"P\",\"schedStart\": \"2018-04-16\"}]}";

		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/rw-tim").contentType(MediaType.APPLICATION_JSON).content(rwJson))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("westbound"));
	}

	@Test
	public void testCreateRwTim_oneDirection_NoItisCodes() throws Exception {

		String rwJson = "{ \"timRwList\": [ {\"fromRm\": 350,\"toRm\": 360,	\"highway\": \"I-80\",\"pk\": \"15917\",\"id\": \"15917\",\"direction\": \"westbound\",\"surface\": \"P\",\"schedStart\": \"2018-04-16\"}]}";

		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/rw-tim").contentType(MediaType.APPLICATION_JSON).content(rwJson))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("westbound"));
	}

	@Ignore
	@Test
	public void testDeleteRwTimsByClientId() throws Exception {
		makeTims();

		List<ActiveTim> activeTimsBeforeDelete = ActiveTimService.getActiveTimsByClientIdDirection("15917",
				getTimTypeId(), null);
		assertEquals(1, activeTimsBeforeDelete.size());

		this.mockMvc.perform(MockMvcRequestBuilders.delete("/rw-tim/15917"))
				.andExpect(MockMvcResultMatchers.status().isOk());

		List<ActiveTim> activeTimsAfterDelete = ActiveTimService.getActiveTimsByClientIdDirection("15917",
				getTimTypeId(), null);
		assertEquals(0, activeTimsAfterDelete.size());
	}

	@Ignore
	@Test
	public void testGetRwTims() throws Exception {

		makeTims();

		ResultActions resultActions = this.mockMvc.perform(MockMvcRequestBuilders.get("/rw-tim"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(CONTENT_TYPE))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("westbound"));

		MvcResult mvcResult = resultActions.andReturn();
		String result = mvcResult.getResponse().getContentAsString();
		System.out.println(result);
	}

	@Ignore
	@Test
	public void testGetRwTimsByClientId() throws Exception {

		makeTims();

		ResultActions resultActions = this.mockMvc.perform(MockMvcRequestBuilders.get("/rw-tim/Parking49251"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(CONTENT_TYPE));
		// .andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("westbound"));

		MvcResult mvcResult = resultActions.andReturn();
		String result = mvcResult.getResponse().getContentAsString();
		System.out.println(result);
	}

	private void makeTims() {

		TimRwList timRwList = new TimRwList();
		List<WydotTimRw> rwList = new ArrayList<WydotTimRw>();
		WydotTimRw wydotTim = new WydotTimRw();

		wydotTim.setSurface("P");
		wydotTim.setDirection("westbound");
		wydotTim.setId("15917");
		wydotTim.setHighway("I-80");
		wydotTim.setFromRm(370.0);
		wydotTim.setToRm(380.0);

		rwList.add(wydotTim);
		timRwList.setTimRwList(rwList);

		WydotTravelerInputData wydotTravelerInputData = CreateBaseTimUtil.buildTim(wydotTim, "westbound", "80",
				configuration);

		OdeLogMetadata odeTimMetadata = new OdeLogMetadata();
		odeTimMetadata.setOdeReceivedAt(null);

		Long timId = TimService.insertTim(odeTimMetadata, null, wydotTravelerInputData.getTim(), null, null, null, null,
				null);

		TimRsuService.insertTimRsu(timId, 1, 1);

		ActiveTim activeTim = new ActiveTim();
		activeTim.setTimId(timId);
		activeTim.setClientId(wydotTim.getId());
		activeTim.setDirection("westbound");
		activeTim.setMilepostStart(wydotTim.getFromRm());
		activeTim.setMilepostStop(wydotTim.getToRm());
		activeTim.setStartDateTime(wydotTravelerInputData.getTim().getDataframes()[0].getStartDateTime());
		activeTim.setRoute(wydotTim.getHighway());
		try {
			activeTim.setTimTypeId(getTimTypeId());
		} catch (Exception e) {
			e.printStackTrace();
		}

		ActiveTimService.insertActiveTim(activeTim);
	}

}
