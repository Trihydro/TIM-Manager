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
 public class WydotTimRwControllerTest {	
	
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
		.filter(x -> x.getType().equals("RW"))
		.findFirst()
		.orElse(null);	

		timTypeId = timType.getTimTypeId();
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

		String rwJson = "{\"timRwList\": [{\"toRm\": 375,\"fromRm\": 370,\"direction\": \"eastbound\",\"surface\": \"G\",\"buffers\": [{\"distance\": 1,\"action\": \"leftClosed\",\"units\": \"miles\"},{\"distance\": 0.5,\"action\": \"workers\",\"units\": \"miles\"}],\"startTs\": \"2016-06-23T14:31:52.404Z\",\"delays\": [{\"code\": 19,\"debug\": {\"codeStr\": \"1\",\"enabled\": true,\"key\": 8399},\"firstDay\": \"20130228\",\"lastDay\": \"20500430\",\"dailyStartTime\": \"0000\",\"id\": 8350,\"dailyEndTime\": \"0000\",\"daysOfWeek\": \"SMTWTFS\"}],\"disabled\": false,\"id\": 8359,\"highway\": \"I-80\"}]}";
		  
        ResultActions resultActions = this.mockMvc.perform(MockMvcRequestBuilders.post("/rw-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(rwJson))
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
	
		String rwJson = "{ \"timRwList\": [ {\"fromRm\": 350,\"toRm\": 360,	\"highway\": \"I-80\",\"pk\": \"15917\",\"id\": \"15917\",\"direction\": \"westbound\",\"surface\": \"P\",\"startTs\": \"2018-04-16T19:30:05.000Z\"}]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/rw-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(rwJson))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("westbound"));
	}

	@Test
	public void testCreateRwTim_bothDirections_NoItisCodes() throws Exception {
	
		String rwJson = "{ \"timRwList\": [ {\"fromRm\": 350,\"toRm\": 360,	\"highway\": \"I-80\",\"pk\": \"15917\",\"id\": \"15917\",\"direction\": \"westbound\",\"surface\": \"P\",\"startTs\": \"2018-04-16T19:30:05.000Z\"}]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/rw-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(rwJson))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].clientId").value("15917"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].route").value("I-80"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("westbound"));
	}

	@Test
	public void testCreateRwTim_oneDirection_success() throws Exception {
	
		String rwJson = "{ \"timRwList\": [ {\"fromRm\": 350,\"toRm\": 360,	\"highway\": \"I-80\",\"pk\": \"15917\",\"id\": \"15917\",\"direction\": \"westbound\",\"surface\": \"P\",\"startTs\": \"2018-04-16T19:30:05.000Z\"}]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/rw-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(rwJson))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].clientId").value("15917"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("westbound"));
	}

	@Test
	public void testCreateRwTim_oneDirection_NoMileposts() throws Exception {
	
		String rwJson = "{ \"timRwList\": [ {\"fromRm\": 350,\"toRm\": 360,	\"highway\": \"I-80\",\"pk\": \"15917\",\"id\": \"15917\",\"direction\": \"westbound\",\"surface\": \"P\",\"startTs\": \"2018-04-16T19:30:05.000Z\"}]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/rw-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(rwJson))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("westbound"));
	}

	@Test
	public void testCreateRwTim_oneDirection_NoItisCodes() throws Exception {
	
		String rwJson = "{ \"timRwList\": [ {\"fromRm\": 350,\"toRm\": 360,	\"highway\": \"I-80\",\"pk\": \"15917\",\"id\": \"15917\",\"direction\": \"westbound\",\"surface\": \"P\",\"startTs\": \"2018-04-16T19:30:05.000Z\"}]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/rw-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(rwJson))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))			
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("westbound"));
	}

	@Test
	public void testDeleteRwTimsByClientId() throws Exception {
		makeTims();

		List<ActiveTim> activeTimsBeforeDelete = ActiveTimService.getActivesTimByClientId("15917", timTypeId);
		assertEquals(1, activeTimsBeforeDelete.size());	

		this.mockMvc.perform(MockMvcRequestBuilders.delete("/rw-tim/15917"))
			.andExpect(MockMvcResultMatchers.status().isOk());

		List<ActiveTim> activeTimsAfterDelete = ActiveTimService.getActivesTimByClientId("15917", timTypeId);
		assertEquals(0, activeTimsAfterDelete.size());	
	}
    
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

	@Test 
	public void testGetRwTimsByClientId() throws Exception {
		
		makeTims();

		ResultActions resultActions = this.mockMvc.perform(MockMvcRequestBuilders.get("/rw-tim/Parking49251"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().contentType(CONTENT_TYPE));
			//.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("westbound"));
	
		MvcResult mvcResult = resultActions.andReturn();
		String result = mvcResult.getResponse().getContentAsString();
		System.out.println(result);
	}
    
	private void makeTims(){

		WydotTimList wydotTimList = new WydotTimList();
		List<WydotTim> rwList = new ArrayList<WydotTim>();
		WydotTim wydotTim = new WydotTim();

		wydotTim.setMileMarker(370.0);
	
		
       
        wydotTim.setAvailability(4223);
		
		wydotTim.setSurface("P");
		wydotTim.setDirection("westbound");
		wydotTim.setId("15917");
		wydotTim.setPk(15917);
		wydotTim.setHighway("I-80");
		wydotTim.setFromRm(370.0);
        wydotTim.setToRm(380.0);		

		rwList.add(wydotTim);
		wydotTimList.setTimRwList(rwList);

		WydotTravelerInputData wydotTravelerInputData = CreateBaseTimUtil.buildTim(wydotTim, "westbound", "80");

		OdeLogMetadataReceived odeTimMetadata = new OdeLogMetadataReceived();
		odeTimMetadata.setOdeReceivedAt(null);

		Long timId = TimService.insertTim(odeTimMetadata, wydotTravelerInputData.getTim());

		TimRsuService.insertTimRsu(timId, 1);

		ActiveTim activeTim = new ActiveTim();
		activeTim.setTimId(timId);
		activeTim.setClientId(wydotTim.getId());
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
