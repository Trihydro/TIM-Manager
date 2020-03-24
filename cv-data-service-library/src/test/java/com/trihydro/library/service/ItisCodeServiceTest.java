package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.trihydro.library.model.ItisCode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.ResponseEntity;

@RunWith(PowerMockRunner.class)

public class ItisCodeServiceTest extends BaseServiceTest {

    @Mock
    private ResponseEntity<ItisCode[]> mockResponseEntityItisCodeArray;

    @Test
    public void selectAll() {
        // Arrange
        String url = String.format("null/itiscodes");
        ItisCode[] ics = new ItisCode[1];
        ItisCode ic = new ItisCode();
        ic.setCategoryId(-1);
        ic.setDescription("description");
        ic.setItisCode(99);
        ics[0] = ic;
        doReturn(ics).when(mockResponseEntityItisCodeArray).getBody();
        when(mockRestTemplate.getForEntity(url, ItisCode[].class)).thenReturn(mockResponseEntityItisCodeArray);

        // Act
        List<ItisCode> data = ItisCodeService.selectAll();

        // Assert
        verify(mockRestTemplate).getForEntity(url, ItisCode[].class);
        assertEquals(1, data.size());
        assertEquals(ic, data.get(0));
    }
}