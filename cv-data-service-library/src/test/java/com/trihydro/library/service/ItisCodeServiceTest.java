package com.trihydro.library.service;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.trihydro.library.model.CVRestServiceProps;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.model.TmddItisCode;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;

public class ItisCodeServiceTest extends BaseServiceTest {

    @Mock
    private ResponseEntity<ItisCode[]> mockResponseEntityItisCodeArray;

    @Mock
    private ResponseEntity<TmddItisCode[]> mockResponseEntityTmddItisCodeArray;

    private String baseUrl = "baseUrl";

    @Mock
    private CVRestServiceProps mockConfig;

    @InjectMocks
    private ItisCodeService uut;

    @BeforeEach
    public void setupSubTest() {
        doReturn(baseUrl).when(mockConfig).getCvRestService();
    }

    @Test
    public void selectAll() {
        // Arrange
        String url = String.format("%s/itiscodes", baseUrl);
        ItisCode[] ics = new ItisCode[1];
        ItisCode ic = new ItisCode();
        ic.setCategoryId(-1);
        ic.setDescription("description");
        ic.setItisCode(99);
        ics[0] = ic;
        doReturn(ics).when(mockResponseEntityItisCodeArray).getBody();
        when(mockRestTemplate.getForEntity(url, ItisCode[].class)).thenReturn(mockResponseEntityItisCodeArray);

        // Act
        List<ItisCode> data = uut.selectAll();

        // Assert
        verify(mockRestTemplate).getForEntity(url, ItisCode[].class);
        Assertions.assertEquals(1, data.size());
        Assertions.assertEquals(ic, data.get(0));
    }

    @Test
    public void selectAllTmddItisCodes() {
        // Arrange
        String url = String.format("%s/tmdd-itiscodes", baseUrl);
        TmddItisCode result = new TmddItisCode();
        result.setElementType("type");
        result.setElementValue("value");
        result.setItisCode(5);
        doReturn(new TmddItisCode[] { result }).when(mockResponseEntityTmddItisCodeArray).getBody();
        when(mockRestTemplate.getForEntity(url, TmddItisCode[].class)).thenReturn(mockResponseEntityTmddItisCodeArray);

        // Act
        List<TmddItisCode> data = uut.selectAllTmddItisCodes();

        // Assert
        verify(mockRestTemplate).getForEntity(url, TmddItisCode[].class);
        Assertions.assertEquals(1, data.size());
        Assertions.assertEquals("type", data.get(0).getElementType());
        Assertions.assertEquals("value", data.get(0).getElementValue());
        Assertions.assertEquals(5, (int) data.get(0).getItisCode());
    }
}