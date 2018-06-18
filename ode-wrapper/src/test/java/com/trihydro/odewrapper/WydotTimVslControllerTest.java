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
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Categories.IncludeCategory;
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
 public class WydotTimVslControllerTest {	
	
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
		.filter(x -> x.getType().equals("VSL"))
		.findFirst()
		.orElse(null);	

		timTypeId = timType.getTimTypeId();
	}
	
	@Test
	public void givenWac_whenServletContext_thenItProvidesGreetController() {
		ServletContext servletContext = wac.getServletContext();
		
		Assert.assertNotNull(servletContext);
		Assert.assertTrue(servletContext instanceof MockServletContext);
		Assert.assertNotNull(wac.getBean("wydotTimVslController"));
	}

	@Test
	public void testCreateVSLTim_bothDirections_success() throws Exception {
     
		String incidentJson = "{\"timVslList\": [{ \"toRm\": 370, \"fromRm\": 360, \"route\": \"I-80\", \"direction\": \"both\", \"speed\": 45  }]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/vsl-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(incidentJson))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessage").value("success"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultCode").value(0))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("eastbound"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].resultMessage").value("success"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].resultCode").value(0))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].direction").value("westbound"));
	}

	@Test 
	public void testCreateIncidentTim_bothDirections_NoMileposts() throws Exception {
	
		String incidentJson = "{\"timVslList\": [{ \"toRm\": 370, \"fromRm\": 360, \"route\": \"I-70\", \"direction\": \"both\", \"speed\": 45  }]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/vsl-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(incidentJson))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessage").value("No mileposts found"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultCode").value(1))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("eastbound"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].resultMessage").value("No mileposts found"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].resultCode").value(1))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].direction").value("westbound"));
		
	}

	@Test
	public void testCreateVSLTim_bothDirections_NoSpeedItisCode() throws Exception {
	
		String incidentJson = "{\"timVslList\": [{ \"toRm\": 370, \"fromRm\": 360, \"route\": \"I-80\", \"direction\": \"both\", \"speed\": 100  }]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/vsl-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(incidentJson))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessage").value("No ITIS codes found, TIM not sent"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultCode").value(2))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("eastbound"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].resultMessage").value("No ITIS codes found, TIM not sent"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].resultCode").value(2))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].direction").value("westbound"));
	}

	@Test
	public void testCreateVSLTim_oneDirection_success() throws Exception {
	
		String incidentJson = "{\"timVslList\": [{ \"toRm\": 370, \"fromRm\": 360, \"route\": \"I-80\", \"direction\": \"both\", \"speed\": 45  }]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/vsl-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(incidentJson))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessage").value("success"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultCode").value(0))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("eastbound"));
	}
 
	@Test 
	public void testCreateVSLTim_oneDirection_NoMileposts() throws Exception {
	
		String incidentJson = "{\"timVslList\": [{ \"toRm\": 370, \"fromRm\": 360, \"route\": \"I-60\", \"direction\": \"eastbound\", \"speed\": 45  }]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/vsl-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(incidentJson))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessage").value("No mileposts found"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultCode").value(1))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("eastbound"));
	}

	@Test
	public void testCreateVSLTim_oneDirection_NoItisCodes() throws Exception {
	
		String incidentJson = "{\"timVslList\": [{ \"toRm\": 370, \"fromRm\": 360, \"route\": \"I-80\", \"direction\": \"eastbound\", \"speed\": 100  }]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/vsl-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(incidentJson))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessage").value("No ITIS codes found, TIM not sent"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultCode").value(2))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("eastbound"));
	}

	@Test 
	public void testUpdateVSLTim_oneDirection_success() throws Exception {
	
		String incidentJson = "{\"timVslList\": [{ \"toRm\": 370, \"fromRm\": 360, \"route\": \"I-80\", \"direction\": \"eastbound\", \"speed\": 40  }]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/vsl-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(incidentJson))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessage").value("success"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultCode").value(0))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("eastbound"));
	}

	@Test 
	public void testUpdateVSLTim_bothDirections_success() throws Exception {
	
		String incidentJson = "{\"timVslList\": [{ \"toRm\": 370, \"fromRm\": 360, \"route\": \"I-80\", \"direction\": \"both\", \"speed\": 40  }]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/vsl-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(incidentJson))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessage").value("success"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultCode").value(0))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("eastbound"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].resultMessage").value("success"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].resultCode").value(0))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].direction").value("westbound"));
	}

}
