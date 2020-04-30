package com.trihydro.odewrapper.helpers;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.CustomItisEnum;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.service.IncidentChoicesService;
import com.trihydro.library.service.ItisCodeService;
import com.trihydro.odewrapper.model.WydotTimRc;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;

@RunWith(StrictStubs.class)
public class SetItisCodesTest {

    @Mock
    ItisCodeService mockItisCodeService;
    @Mock
    IncidentChoicesService mockIncidentChoicesService;

    @InjectMocks
    SetItisCodes uut;

    @Before
    public void setup() {
        List<ItisCode> itisCodes = new ArrayList<>();
        ItisCode code = new ItisCode();
        code.setItisCode(268);// speed limit
        itisCodes.add(code);

        code = new ItisCode();
        code.setItisCode(770);// closed
        itisCodes.add(code);

        code = new ItisCode();
        code.setItisCode(1309);// rockfall
        itisCodes.add(code);

        code = new ItisCode();
        code.setItisCode(3084);// wildfire
        itisCodes.add(code);

        code = new ItisCode();
        code.setItisCode(4868); // snow
        itisCodes.add(code);

        code = new ItisCode();
        code.setItisCode(770); // closed
        itisCodes.add(code);

        doReturn(itisCodes).when(mockItisCodeService).selectAll();
    }

    @Test
    public void setItisCodesRc_numeric() {
        // Arrange
        WydotTimRc tim = new WydotTimRc();
        Integer[] itisCodes = new Integer[2];
        itisCodes[0] = 4868;
        itisCodes[1] = 1309;
        tim.setAdvisory(itisCodes);
        // Act
        var result = uut.setItisCodesRc(tim);

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    public void setItisCodesRc_translated() {
        // Arrange
        WydotTimRc tim = new WydotTimRc();
        Integer[] itisCodes = new Integer[2];
        itisCodes[0] = 4868;
        itisCodes[1] = 769;
        tim.setAdvisory(itisCodes);
        // Act
        var result = uut.setItisCodesRc(tim);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains("770"));
    }

    @Test
    public void setItisCodesRc_alphabetic() {
        // Arrange
        WydotTimRc tim = new WydotTimRc();
        Integer[] itisCodes = new Integer[2];
        itisCodes[0] = 4868;
        itisCodes[1] = CustomItisEnum.blowOver.getValue();
        tim.setAdvisory(itisCodes);
        // Act
        var result = uut.setItisCodesRc(tim);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains("Extreme blow over risk"));
    }
}