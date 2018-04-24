package com.trihydro.odewrapper;

import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.model.WydotTimList;
import com.trihydro.odewrapper.model.WydotTravelerInputData;
import com.trihydro.odewrapper.controller.WydotTimIncidentController;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * Unit tests for JSON to Java Object Converters.
 */
public class WydotTimIncidentControllerTest {	

	// @Test 
	// public void testCreateIncidentTim() {
        
	// 	WydotTimList wydotTimList = new WydotTimList();
	// 	List<WydotTim> incidentList = new ArrayList<WydotTim>();
	// 	WydotTim wydotTim1 = new WydotTim();

	// 	wydotTim1.setToRm(260.0);
	// 	wydotTim1.setFromRm(250.0);
	// 	wydotTim1.setImpact("L");
	// 	wydotTim1.setProblem("mudslide");
	// 	wydotTim1.setEffect("leftClosed");
	// 	wydotTim1.setAction("caution");
	// 	wydotTim1.setPk(3622);
	// 	wydotTim1.setHighway("I-80");
	// 	wydotTim1.setIncidentId("IN49251");
	// 	wydotTim1.setDirection("both");
	// 	wydotTim1.setTs("2018-04-16T19:30:05.000Z");

	// 	incidentList.add(wydotTim1);
	// 	wydotTimList.setTimIncidentList(incidentList);



	// }
	
	@Test 
	public void testBuildTim() {

		WydotTimList wydotTimList = new WydotTimList();
		List<WydotTim> incidentList = new ArrayList<WydotTim>();
		WydotTim wydotTim = new WydotTim();

		wydotTim.setToRm(260.0);
		wydotTim.setFromRm(250.0);
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
		
	    assertEquals(51, wydotTravelerInputData.getMileposts().size());
		assertEquals("1111111111111111", wydotTravelerInputData.getTim().getDataframes()[0].getRegions()[0].getDirection());
		assertEquals(50, wydotTravelerInputData.getTim().getDataframes()[0].getRegions()[0].getPath().getNodes().length);
	}
}
