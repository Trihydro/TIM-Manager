package com.trihydro.odewrapper.helpers;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.trihydro.library.model.IncidentChoice;
import com.trihydro.odewrapper.model.WydotTimIncident;
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
  public void setItisCodesBowr_FAILURE() {
    // Arrange
    // calling setup() not necessary here
    int weightInPounds = 23456;
    WydotTimBowr tim = new WydotTimBowr();
    tim.setData(weightInPounds);

    // Act & Assert
    Assertions.assertThrows(WeightNotSupportedException.class, () -> uut.setItisCodesBowr(tim));
  }

  @Test
  public void testSetItisCodesIncident_ReturnsDefaultIncidentCode() {
    // Arrange
    WydotTimIncident mockIncident = new WydotTimIncident(); // Mock incident object
    List<String> expectedCodes = List.of("531"); // Expected default incident code

    // Act
    List<String> result = uut.setItisCodesIncident(mockIncident);

    // Assert
    Assertions.assertNotNull(result, "Resulting list should not be null.");
    Assertions.assertEquals(expectedCodes, result, "Default incident code should be returned.");
  }

  @Test
  public void testSetItisCodesIncident_WithExistingProblemItisCode_ShouldReturnExistingItisCode() {
    // Arrange
    WydotTimIncident incident = new WydotTimIncident();
    incident.setProblem("268");
    List<String> expectedItisCodes = List.of("268");

    IncidentChoice mockIncidentChoice = new IncidentChoice();
    mockIncidentChoice.setCode("268");
    mockIncidentChoice.setItisCodeId(1);
    mockIncidentChoice.setDescription("Speed Limit");
    when(mockIncidentChoicesService.selectAllIncidentProblems()).thenReturn(List.of(mockIncidentChoice));

    ItisCode mockItisCode = new ItisCode();
    mockItisCode.setItisCode(268);
    mockItisCode.setItisCodeId(1);
    when(mockItisCodeService.selectAll()).thenReturn(List.of(mockItisCode));

    // Act
    List<String> actualItisCodes = uut.setItisCodesIncident(incident);

    // Assert
    Assertions.assertNotNull(actualItisCodes, "Resulting list should not be null.");
    Assertions.assertEquals(expectedItisCodes, actualItisCodes, "The ITIS code for the incident problem should be returned.");
  }

  @Test
  public void testSetItisCodesIncident_OtherProblemGVW_ShouldReturnGVWItisCodes() {
    // Arrange
    WydotTimIncident incident = new WydotTimIncident();
    incident.setProblem("other");
    incident.setProblemOtherText("Weight limit of 60,000 GVW is in effect");
    List<String> expectedItisCodes = List.of("2563", "2577", "11605", "8739");

    // Act
    List<String> actualItisCodes = uut.setItisCodesIncident(incident);

    // Assert
    Assertions.assertNotNull(actualItisCodes, "Resulting list should not be null.");
    Assertions.assertEquals(expectedItisCodes, actualItisCodes, "The ITIS codes for a Gross Vehicle Weight restriction (60000 pounds) should be returned.");
  }

}