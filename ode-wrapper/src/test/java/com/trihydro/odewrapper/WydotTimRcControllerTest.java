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
import org.junit.Ignore;
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
 public class WydotTimRcControllerTest {	
	
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
		.filter(x -> x.getType().equals("RC"))
		.findFirst()
		.orElse(null);	

		timTypeId = timType.getTimTypeId();
	}
	
	@Test 
	public void givenWac_whenServletContext_thenItProvidesGreetController() {
		ServletContext servletContext = wac.getServletContext();
		
		Assert.assertNotNull(servletContext);
		Assert.assertTrue(servletContext instanceof MockServletContext);
		Assert.assertNotNull(wac.getBean("wydotTimRcController"));
	}

	@Test 
	public void testCreateRcTim_bothDirections_success() throws Exception {
	
		String rcJson = "{\"timRcList\": [{ \"route\": \"I80\", \"fromRm\": 350, \"toRm\": 360, \"direction\": \"both\",\"advisory\": [4871]} ]} }]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/create-update-rc-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(rcJson))
			.andExpect(MockMvcResultMatchers.status().isOk());
			// .andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessage").value("success"))
			// .andExpect(MockMvcResultMatchers.jsonPath("$[0].resultCode").value(0))
			// .andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("eastbound"))
			// .andExpect(MockMvcResultMatchers.jsonPath("$[1].resultMessage").value("success"))
			// .andExpect(MockMvcResultMatchers.jsonPath("$[1].resultCode").value(0))
			// .andExpect(MockMvcResultMatchers.jsonPath("$[1].direction").value("westbound"));
	}

	@Test 
	public void testCreateRcTim_bothDirections_NoMileposts() throws Exception {
	
		String rcJson = "{\"timRcList\": [{ \"route\": \"I70\", \"fromRm\": 350, \"toRm\": 360, \"direction\": \"both\",\"advisory\": [4871]} ]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/create-update-rc-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(rcJson))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("route not supported"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].route").value("I70"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("both"));
            
	}

	@Test 
	public void testCreateRcTim_bothDirections_NoItisCodes() throws Exception {
	
		String rcJson = "{\"timRcList\": [{ \"route\": \"I80\", \"fromRm\": 350, \"toRm\": 360, \"direction\": \"both\",\"advisory\": [11]} ]} }]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/create-update-rc-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(rcJson))
			.andExpect(MockMvcResultMatchers.status().isOk());
			// .andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
			// .andExpect(MockMvcResultMatchers.jsonPath("$[0].route").value("I80"))
			// .andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("both"));
	}

	@Test 
	public void testCreateRcTim_oneDirection_success() throws Exception {
	
		String rcJson = "{\"timRcList\": [{ \"route\": \"I80\", \"fromRm\": 350, \"toRm\": 360, \"direction\": \"eastbound\",\"advisory\": [4871]} ]} }]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/create-update-rc-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(rcJson))
			.andExpect(MockMvcResultMatchers.status().isOk());
			// .andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessage").value("success"))
			// .andExpect(MockMvcResultMatchers.jsonPath("$[0].resultCode").value(0))
			// .andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("eastbound"));
	}

	@Test 
	public void testCreateVslTim_oneDirection_NoMileposts() throws Exception {
	
		String rcJson = "{\"timRcList\": [{ \"route\": \"I70\", \"fromRm\": 350, \"toRm\": 360, \"direction\": \"eastbound\",\"advisory\": [4871]} ]} }]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/create-update-rc-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(rcJson))
			.andExpect(MockMvcResultMatchers.status().isOk());
			// .andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessage").value("No mileposts found"))
			// .andExpect(MockMvcResultMatchers.jsonPath("$[0].resultCode").value(1))
			// .andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("eastbound"));
	}

	@Test
	public void testCreateRcTim_oneDirection_NoItisCodes() throws Exception {
	
		String rcJson = "{\"timRcList\": [{ \"route\": \"I80\", \"fromRm\": 350, \"toRm\": 360, \"direction\": \"eastbound\",\"advisory\": [11]} ]} }]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/create-update-rc-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(rcJson))
			.andExpect(MockMvcResultMatchers.status().isOk());
			// .andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessage").value("No ITIS codes found, TIM not sent"))
			// .andExpect(MockMvcResultMatchers.jsonPath("$[0].resultCode").value(2))
			// .andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("eastbound"));
	}
 
	@Test 
	public void testUpdateRcTim_oneDirection_success() throws Exception {
	
		String rcJson = "{\"timRcList\": [{ \"route\": \"I80\", \"fromRm\": 350, \"toRm\": 360, \"direction\": \"eastbound\",\"advisory\": [5378]} ]} }]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/create-update-rc-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(rcJson))
		    .andExpect(MockMvcResultMatchers.status().isOk());
			// .andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessage").value("success"))
			// .andExpect(MockMvcResultMatchers.jsonPath("$[0].resultCode").value(0))
			// .andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("eastbound"));
	}

	@Test
	public void testUpdateRcTim_bothDirections_success() throws Exception {
	
		String rcJson = "{\"timRcList\": [{ \"route\": \"I80\", \"fromRm\": 350, \"toRm\": 360, \"direction\": \"both\",\"advisory\": [5378]} ]} }]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/create-update-rc-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(rcJson))
			.andExpect(MockMvcResultMatchers.status().isOk());
			// .andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessage").value("success"))
			// .andExpect(MockMvcResultMatchers.jsonPath("$[0].resultCode").value(0))
			// .andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("eastbound"))
			// .andExpect(MockMvcResultMatchers.jsonPath("$[1].resultMessage").value("success"))
			// .andExpect(MockMvcResultMatchers.jsonPath("$[1].resultCode").value(0))
			// .andExpect(MockMvcResultMatchers.jsonPath("$[1].direction").value("westbound"));
	}

	@Test
	public void testAllClear() throws Exception {
		
        makeTims();
        
		String rcJson = "{\"timRcList\": [{ \"route\": \"I80\", \"fromRm\": 360, \"toRm\": 370, \"direction\": \"westbound\",\"advisory\": [5378]} ]} }]}";

		List<ActiveTim> activeTimsBeforeDelete = ActiveTimService.getActivesTimByType(timTypeId);
		assertEquals(1, activeTimsBeforeDelete.size());	

        this.mockMvc.perform(MockMvcRequestBuilders.put("/submit-rc-ac")
            .contentType(MediaType.APPLICATION_JSON)
            .content(rcJson))
            .andExpect(MockMvcResultMatchers.status().isOk());
            
        List<ActiveTim> activeTimsAfterDelete = ActiveTimService.getActivesTimByType(timTypeId);
		assertEquals(0, activeTimsAfterDelete.size());	
	}
	
	private void makeTims(){

		WydotTimList wydotTimList = new WydotTimList();
		List<WydotTim> incidentList = new ArrayList<WydotTim>();
		WydotTim wydotTim = new WydotTim();

		wydotTim.setToRm(370.0);
		wydotTim.setFromRm(360.0);
		wydotTim.setRoute("I-80");
        wydotTim.setDirection("westbound");
        wydotTim.setAdvisory(new Integer[] {4871});

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
