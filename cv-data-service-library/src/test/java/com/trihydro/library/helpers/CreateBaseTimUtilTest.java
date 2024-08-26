package com.trihydro.library.helpers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.trihydro.library.model.ContentEnum;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.WydotTim;
import com.trihydro.library.service.MilepostService;
import com.trihydro.library.service.TimGenerationProps;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.NodeXY;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.FrameType.TravelerInfoType;

@ExtendWith(MockitoExtension.class)
public class CreateBaseTimUtilTest {
    @Mock
    Utility mockUtility;
    @Mock
    MilepostService mockMilepostService;
    @Mock
    MilepostReduction mockMilepostReduction;
    @Mock
    TimGenerationProps genProps;

    @InjectMocks
    CreateBaseTimUtil uut;

    private List<Milepost> allMileposts;
    private List<Milepost> milepostsReduced;
    private Milepost anchor;

    private void setupMilepostsSimple() {
        allMileposts = new ArrayList<>();
        milepostsReduced = new ArrayList<>();
        anchor = new Milepost();

        anchor.setCommonName("80");
        anchor.setLatitude(BigDecimal.valueOf(100));
        anchor.setLongitude(BigDecimal.valueOf(100));

        var mp = new Milepost();
        mp = new Milepost();
        mp.setLatitude(BigDecimal.valueOf(200));
        mp.setLongitude(BigDecimal.valueOf(200));
        allMileposts.add(mp);
        milepostsReduced.add(mp);

        mp = new Milepost();
        mp.setLatitude(BigDecimal.valueOf(300));
        mp.setLongitude(BigDecimal.valueOf(300));
        allMileposts.add(mp);

        mp = new Milepost();
        mp.setLatitude(BigDecimal.valueOf(400));
        mp.setLongitude(BigDecimal.valueOf(400));
        allMileposts.add(mp);
        milepostsReduced.add(mp);

        mp = new Milepost();
        mp.setLatitude(BigDecimal.valueOf(500));
        mp.setLongitude(BigDecimal.valueOf(500));
        allMileposts.add(mp);
        milepostsReduced.add(mp);
    }

    private void setupMilepostsMany(int numMileposts) {
        allMileposts = new ArrayList<>();
        milepostsReduced = new ArrayList<>();
        anchor = new Milepost();

        anchor.setCommonName("80");
        anchor.setLatitude(BigDecimal.valueOf(0));
        anchor.setLongitude(BigDecimal.valueOf(0));


        for (int i = 0; i < numMileposts; i++) {
            var mp = new Milepost();
            mp.setLatitude(BigDecimal.valueOf(i));
            mp.setLongitude(BigDecimal.valueOf(i));
            allMileposts.add(mp);
            milepostsReduced.add(mp);
        }
    }

    @Test
    public void buildTim_EndpointSUCCESS() {
        // Arrange
        setupMilepostsSimple();
        var wydotTim = new WydotTim();
        wydotTim.setStartPoint(new Coordinate());
        wydotTim.setEndPoint(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2)));
        var itisCodes = new ArrayList<String>();
        itisCodes.add("1309");
        itisCodes.add("8888");
        wydotTim.setItisCodes(itisCodes);
        wydotTim.setClientId("testclientid");

        var content = ContentEnum.advisory;
        var frameType = TravelerInfoType.advisory;

        // Act
        var data = uut.buildTim(wydotTim, genProps, content, frameType, allMileposts, milepostsReduced, anchor);

        // Assert
        // validate dataFrame
        var dataFrame = data.getTim().getDataframes()[0];
        Assertions.assertNotNull(dataFrame);
        Assertions.assertEquals("advisory", dataFrame.getContent());
        Assertions.assertEquals(32000, dataFrame.getDurationTime());

        var region = dataFrame.getRegions()[0];
        Assertions.assertNotNull(region);

        // validate anchor
        var anchor = region.getAnchorPosition();
        Assertions.assertNotNull(anchor);
        Assertions.assertEquals(anchor.getLatitude(), anchor.getLatitude());
        Assertions.assertEquals(anchor.getLongitude(), anchor.getLongitude());

        // validate path
        var path = region.getPath();
        Assertions.assertNotNull(path);
        Assertions.assertEquals("ll", path.getType());
        Assertions.assertEquals(0, path.getScale());

        // validate nodes
        Assertions.assertEquals(3, path.getNodes().length);
        for (int i = 0; i < path.getNodes().length; i++) {
            var node = path.getNodes()[i];
            Assertions.assertEquals("node-LL", node.getDelta());
        }
    }

    @Test
    public void buildTim_singlePointSUCCESS() {
        // Arrange
        setupMilepostsSimple();
        var wydotTim = new WydotTim();
        wydotTim.setRoute("80");
        wydotTim.setStartPoint(new Coordinate());
        var itisCodes = new ArrayList<String>();
        itisCodes.add("1309");
        itisCodes.add("8888");
        wydotTim.setItisCodes(itisCodes);
        wydotTim.setClientId("testclientid");

        var content = ContentEnum.advisory;
        var frameType = TravelerInfoType.advisory;

        // Act
        var data = uut.buildTim(wydotTim, genProps, content, frameType, allMileposts, milepostsReduced, anchor);

        // Assert
        // validate dataFrame
        var dataFrame = data.getTim().getDataframes()[0];
        Assertions.assertNotNull(dataFrame);
        Assertions.assertEquals("advisory", dataFrame.getContent());
        Assertions.assertEquals(32000, dataFrame.getDurationTime());

        var region = dataFrame.getRegions()[0];
        Assertions.assertNotNull(region);

        // validate anchor
        var anchor = region.getAnchorPosition();
        Assertions.assertNotNull(anchor);
        Assertions.assertEquals(anchor.getLatitude(), anchor.getLatitude());
        Assertions.assertEquals(anchor.getLongitude(), anchor.getLongitude());

        // validate path
        var path = region.getPath();
        Assertions.assertNotNull(path);
        Assertions.assertEquals("ll", path.getType());
        Assertions.assertEquals(0, path.getScale());

        // validate nodes
        Assertions.assertEquals(3, path.getNodes().length);
        for (int i = 0; i < path.getNodes().length; i++) {
            var node = path.getNodes()[i];
            Assertions.assertEquals("node-LL", node.getDelta());
        }
    }

    @Test
    public void buildTim_singleRegion_SUCCESS() {
        // Arrange
        setupMilepostsSimple();
        var wydotTim = new WydotTim();
        wydotTim.setRoute("80");
        wydotTim.setStartPoint(new Coordinate());
        var itisCodes = new ArrayList<String>();
        itisCodes.add("1309");
        itisCodes.add("8888");
        wydotTim.setItisCodes(itisCodes);
        wydotTim.setClientId("testclientid");

        var content = ContentEnum.advisory;
        var frameType = TravelerInfoType.advisory;

        // Act
        var data = uut.buildTim(wydotTim, genProps, content, frameType, allMileposts, milepostsReduced, anchor);

        // Assert
        Assertions.assertEquals(1, data.getTim().getDataframes()[0].getRegions().length);
        Region region = data.getTim().getDataframes()[0].getRegions()[0];

        // verify nodes
        Assertions.assertEquals(3, region.getPath().getNodes().length);
        for (int i = 0; i < region.getPath().getNodes().length; i++) {
            var node = region.getPath().getNodes()[i];
            Assertions.assertEquals("node-LL", node.getDelta());
        }
    }

    @Test
    public void buildTim_twoRegions_SUCCESS() {
        // Arrange
        setupMilepostsMany(100);
        var wydotTim = new WydotTim();
        wydotTim.setRoute("80");
        wydotTim.setStartPoint(new Coordinate());
        var itisCodes = new ArrayList<String>();
        itisCodes.add("1309");
        itisCodes.add("8888");
        wydotTim.setItisCodes(itisCodes);
        wydotTim.setClientId("testclientid");

        var content = ContentEnum.advisory;
        var frameType = TravelerInfoType.advisory;

        // Act
        var data = uut.buildTim(wydotTim, genProps, content, frameType, allMileposts, milepostsReduced, anchor);

        // Assert
        Assertions.assertEquals(2, data.getTim().getDataframes()[0].getRegions().length);
        Region region1 = data.getTim().getDataframes()[0].getRegions()[0];
        Region region2 = data.getTim().getDataframes()[0].getRegions()[1];
        
        // verify nodes
        Assertions.assertEquals(63, region1.getPath().getNodes().length);
        for (int i = 0; i < region1.getPath().getNodes().length; i++) {
            var node = region1.getPath().getNodes()[i];
            Assertions.assertEquals("node-LL", node.getDelta());
        }
        Assertions.assertEquals(37, region2.getPath().getNodes().length);
        for (int i = 0; i < region2.getPath().getNodes().length; i++) {
            var node = region2.getPath().getNodes()[i];
            Assertions.assertEquals("node-LL", node.getDelta());
        }
        
        // verify anchor of second region is last milepost of first region
        Milepost lastMilepostOfFirstRegion = milepostsReduced.get(62);
        OdePosition3D anchorOfSecondRegion = region2.getAnchorPosition();
        Assertions.assertEquals(lastMilepostOfFirstRegion.getLatitude().doubleValue(), anchorOfSecondRegion.getLatitude().doubleValue());
        Assertions.assertEquals(lastMilepostOfFirstRegion.getLongitude().doubleValue(), anchorOfSecondRegion.getLongitude().doubleValue());
    }

    @Test
    public void buildTim_threeRegions_SUCCESS() {
        // Arrange
        setupMilepostsMany(150);
        var wydotTim = new WydotTim();
        wydotTim.setRoute("80");
        wydotTim.setStartPoint(new Coordinate());
        var itisCodes = new ArrayList<String>();
        itisCodes.add("1309");
        itisCodes.add("8888");
        wydotTim.setItisCodes(itisCodes);
        wydotTim.setClientId("testclientid");

        var content = ContentEnum.advisory;
        var frameType = TravelerInfoType.advisory;

        // Act
        var data = uut.buildTim(wydotTim, genProps, content, frameType, allMileposts, milepostsReduced, anchor);

        // Assert
        Assertions.assertEquals(3, data.getTim().getDataframes()[0].getRegions().length);
        Region region1 = data.getTim().getDataframes()[0].getRegions()[0];
        Region region2 = data.getTim().getDataframes()[0].getRegions()[1];
        Region region3 = data.getTim().getDataframes()[0].getRegions()[2];
        
        // verify nodes
        Assertions.assertEquals(63, region1.getPath().getNodes().length);
        for (int i = 0; i < region1.getPath().getNodes().length; i++) {
            var node = region1.getPath().getNodes()[i];
            Assertions.assertEquals("node-LL", node.getDelta());
        }
        Assertions.assertEquals(63, region2.getPath().getNodes().length);
        for (int i = 0; i < region2.getPath().getNodes().length; i++) {
            var node = region2.getPath().getNodes()[i];
            Assertions.assertEquals("node-LL", node.getDelta());
        }
        Assertions.assertEquals(24, region3.getPath().getNodes().length);
        for (int i = 0; i < region3.getPath().getNodes().length; i++) {
            var node = region3.getPath().getNodes()[i];
            Assertions.assertEquals("node-LL", node.getDelta());
        }
        
        // verify anchor of second region is last milepost of first region
        Milepost lastMilepostOfFirstRegion = milepostsReduced.get(62);
        OdePosition3D anchorOfSecondRegion = region2.getAnchorPosition();
        Assertions.assertEquals(lastMilepostOfFirstRegion.getLatitude().doubleValue(), anchorOfSecondRegion.getLatitude().doubleValue());
        Assertions.assertEquals(lastMilepostOfFirstRegion.getLongitude().doubleValue(), anchorOfSecondRegion.getLongitude().doubleValue());
    
        // verify anchor of third region is last milepost of second region
        Milepost lastMilepostOfSecondRegion = milepostsReduced.get(125);
        OdePosition3D anchorOfThirdRegion = region3.getAnchorPosition();
        Assertions.assertEquals(lastMilepostOfSecondRegion.getLatitude().doubleValue(), anchorOfThirdRegion.getLatitude().doubleValue());
        Assertions.assertEquals(lastMilepostOfSecondRegion.getLongitude().doubleValue(), anchorOfThirdRegion.getLongitude().doubleValue());
    }
}