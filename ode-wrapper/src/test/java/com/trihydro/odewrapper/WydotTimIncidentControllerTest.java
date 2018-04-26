package com.trihydro.odewrapper;

import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.model.WydotTimList;
import com.trihydro.odewrapper.model.WydotTravelerInputData;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.odewrapper.controller.WydotTimIncidentController;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import com.trihydro.odewrapper.spring.WebConfig;
import com.trihydro.odewrapper.spring.ApplicationConfig;

 @RunWith(SpringRunner.class)
 @WebAppConfiguration
 @SpringBootTest(classes = Application.class)
 public class WydotTimIncidentControllerTest {	
	
	@Autowired
    private WebApplicationContext wac;
	private MockMvc mockMvc;
	private static final String CONTENT_TYPE = "application/json;charset=UTF-8";

	@Before
    public void setup() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
		DbUtility.changeConnection("test");			
    }

	@Test
	public void givenWac_whenServletContext_thenItProvidesGreetController() {
		ServletContext servletContext = wac.getServletContext();
		
		Assert.assertNotNull(servletContext);
		Assert.assertTrue(servletContext instanceof MockServletContext);
		Assert.assertNotNull(wac.getBean("wydotTimIncidentController"));
	}

	@Test 
	public void testBuildTim() {

		WydotTimList wydotTimList = new WydotTimList();
		List<WydotTim> incidentList = new ArrayList<WydotTim>();
		WydotTim wydotTim = new WydotTim();

		wydotTim.setToRm(365.0);
		wydotTim.setFromRm(370.0);
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

		assertEquals(1, wydotTravelerInputData.getTim().getDataframes()[0].getSspMsgTypes());
		assertEquals(1, wydotTravelerInputData.getTim().getDataframes()[0].getSspLocationRights());
		assertEquals(1, wydotTravelerInputData.getTim().getDataframes()[0].getSspTimRights());
		assertEquals(1, wydotTravelerInputData.getTim().getDataframes()[0].getSspMsgContent());
		assertEquals("CDEF", wydotTravelerInputData.getTim().getDataframes()[0].getMsgId().getFurtherInfoID());
		assertEquals(32000, wydotTravelerInputData.getTim().getDataframes()[0].getDurationTime());
		assertEquals(5, wydotTravelerInputData.getTim().getDataframes()[0].getPriority());
		assertEquals("null", wydotTravelerInputData.getTim().getUrlB());
		assertEquals("Advisory", wydotTravelerInputData.getTim().getDataframes()[0].getContent());
		assertEquals(us.dot.its.jpo.ode.plugin.j2735.timstorage.FrameType.TravelerInfoType.advisory, wydotTravelerInputData.getTim().getDataframes()[0].getFrameType());
		assertEquals("null", wydotTravelerInputData.getTim().getDataframes()[0].getUrl());
		assertEquals("Temp", wydotTravelerInputData.getTim().getDataframes()[0].getRegions()[0].getName());
		assertEquals(0, wydotTravelerInputData.getTim().getDataframes()[0].getRegions()[0].getRegulatorID());
		assertEquals(new BigDecimal(327), wydotTravelerInputData.getTim().getDataframes()[0].getRegions()[0].getLaneWidth());
		assertEquals(3, wydotTravelerInputData.getTim().getDataframes()[0].getRegions()[0].getDirectionality());
		assertEquals(false, wydotTravelerInputData.getTim().getDataframes()[0].getRegions()[0].isClosedPath());
		assertEquals("path", wydotTravelerInputData.getTim().getDataframes()[0].getRegions()[0].getDescription());
		assertEquals(0, wydotTravelerInputData.getTim().getDataframes()[0].getRegions()[0].getPath().getScale());
		assertEquals("xy", wydotTravelerInputData.getTim().getDataframes()[0].getRegions()[0].getPath().getType());
		
	    assertEquals(6, wydotTravelerInputData.getMileposts().size());
		assertEquals("1111111111111111", wydotTravelerInputData.getTim().getDataframes()[0].getRegions()[0].getDirection());
		assertEquals(5, wydotTravelerInputData.getTim().getDataframes()[0].getRegions()[0].getPath().getNodes().length);
	}

	@Test
	public void testCreateIncidentTim() throws Exception {
	
		String incidentJson = "{\"timIncidentList\": [{ \"toRm\": 350, \"impact\": \"L\", \"fromRm\": 340, \"problem\": \"fire\", \"effect\": \"test\", \"action\": \"test\", \"pk\": 3622, \"highway\": \"I-80\", \"incidentId\": \"IN49251\", \"direction\": \"both\", \"ts\": \"2018-04-16T19:30:05.000Z\" }]}";
		  
		this.mockMvc.perform(MockMvcRequestBuilders.post("/incident-tim")
			.contentType(MediaType.APPLICATION_JSON)
			.content(incidentJson))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessage").value("successs"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultCode").value(0));
	}

	@Test @Ignore
	public void testGetIncidentTims() throws Exception {
		
		this.mockMvc.perform(MockMvcRequestBuilders.get("/incident-tim"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().contentType(CONTENT_TYPE))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].activeTimId").value(582));
	}
	
}
