package com.trihydro.library.helpers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    public void setupSuccessfulTest() {
        doReturn(BigDecimal.valueOf(50)).when(genProps).getDefaultLaneWidth();
        doReturn(.4 * 50).when(genProps).getPathDistanceLimit();
        setupMileposts();
    }

    private void setupMileposts() {
        allMileposts = new ArrayList<>();
        milepostsReduced = new ArrayList<>();
        anchor = new Milepost();

        anchor.setCommonName("80");
        anchor.setLatitude(BigDecimal.valueOf(100));
        anchor.setLongitude(BigDecimal.valueOf(100));
        allMileposts.add(anchor);

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

        doReturn(milepostsReduced).when(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());
    }

    @Test
    public void buildTim_MilepostFAIL() {
        // Arrange
        var wydotTim = new WydotTim();
        wydotTim.setStartPoint(new Coordinate());
        wydotTim.setEndPoint(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2)));
        String direction = "I";
        var content = ContentEnum.advisory;
        var frameType = TravelerInfoType.advisory;

        // Act
        var data = uut.buildTim(wydotTim, direction, genProps, content, frameType);

        // Assert
        Assertions.assertNull(data);
        verify(mockUtility).logWithDate("Found 0 mileposts, unable to generate TIM");
    }

    @Test
    public void buildTim_EndpointSUCCESS() {
        // Arrange
        setupSuccessfulTest();
        var wydotTim = new WydotTim();
        wydotTim.setStartPoint(new Coordinate());
        wydotTim.setEndPoint(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2)));
        var itisCodes = new ArrayList<String>();
        itisCodes.add("1309");
        itisCodes.add("8888");
        wydotTim.setItisCodes(itisCodes);

        String direction = "I";
        var content = ContentEnum.advisory;
        var frameType = TravelerInfoType.advisory;

        doReturn(allMileposts).when(mockMilepostService).getMilepostsByStartEndPointDirection(any());

        // Act
        var data = uut.buildTim(wydotTim, direction, genProps, content, frameType);

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
        setupSuccessfulTest();
        doReturn(1d).when(genProps).getPointIncidentBufferMiles();
        var wydotTim = new WydotTim();
        wydotTim.setRoute("80");
        wydotTim.setStartPoint(new Coordinate());
        var itisCodes = new ArrayList<String>();
        itisCodes.add("1309");
        itisCodes.add("8888");
        wydotTim.setItisCodes(itisCodes);

        String direction = "I";
        var content = ContentEnum.advisory;
        var frameType = TravelerInfoType.advisory;

        doReturn(allMileposts).when(mockMilepostService).getMilepostsByPointWithBuffer(any());

        // Act
        var data = uut.buildTim(wydotTim, direction, genProps, content, frameType);

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
}