package com.trihydro.odewrapper.helpers;

import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.CustomItisEnum;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.service.IncidentChoicesService;
import com.trihydro.library.service.ItisCodeService;
import com.trihydro.odewrapper.helpers.SetItisCodes.WeightNotSupportedException;
import com.trihydro.odewrapper.model.WydotTimBowr;
import com.trihydro.odewrapper.model.WydotTimRc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SetItisCodesTest {

    @Mock
    ItisCodeService mockItisCodeService;
    @Mock
    IncidentChoicesService mockIncidentChoicesService;

    @InjectMocks
    SetItisCodes uut;

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
        setup();
        WydotTimRc tim = new WydotTimRc();
        Integer[] itisCodes = new Integer[2];
        itisCodes[0] = 4868;
        itisCodes[1] = 1309;
        tim.setAdvisory(itisCodes);
        // Act
        var result = uut.setItisCodesRc(tim);

        // Assert
        Assertions.assertEquals(2, result.size());
    }

    @Test
    public void setItisCodesRc_nonExistent() {
        // Arrange
        setup();
        WydotTimRc tim = new WydotTimRc();
        Integer[] itisCodes = new Integer[2];
        itisCodes[0] = 0;
        itisCodes[1] = 13;
        tim.setAdvisory(itisCodes);
        // Act
        var result = uut.setItisCodesRc(tim);

        // Assert
        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void setItisCodesRc_translated() {
        // Arrange
        setup();
        WydotTimRc tim = new WydotTimRc();
        Integer[] itisCodes = new Integer[2];
        itisCodes[0] = 4868;
        itisCodes[1] = 769;
        tim.setAdvisory(itisCodes);
        // Act
        var result = uut.setItisCodesRc(tim);

        // Assert
        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.contains("770"));
    }

    @Test
    public void setItisCodesRc_alphabetic() {
        // Arrange
        setup();
        WydotTimRc tim = new WydotTimRc();
        Integer[] itisCodes = new Integer[2];
        itisCodes[0] = 4868;
        itisCodes[1] = CustomItisEnum.blowOver.getValue();
        tim.setAdvisory(itisCodes);
        // Act
        var result = uut.setItisCodesRc(tim);

        // Assert
        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.contains("Extreme blow over risk"));
    }

    @Test
    public void setItisCodesBowr_SUCCESS() throws WeightNotSupportedException {
        // Arrange
        // calling setup() not necessary here
        int weightInPounds = 20000;
        String weightAsItisCode = "11589";
        WydotTimBowr tim = new WydotTimBowr();
        tim.setData(weightInPounds);
        List<String> expectedResult = List.of("5127", "2563", "2569", "7682", "2577", weightAsItisCode, "8739");

        // Act
        List<String> result = uut.setItisCodesBowr(tim);

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void setItisCodesBowr_FAILURE() throws WeightNotSupportedException {
        // Arrange
        // calling setup() not necessary here
        int weightInPounds = 23456;
        WydotTimBowr tim = new WydotTimBowr();
        tim.setData(weightInPounds);

        // Act & Assert
        Assertions.assertThrows(WeightNotSupportedException.class, () -> uut.setItisCodesBowr(tim));
    }
}