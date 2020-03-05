package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.List;

import com.trihydro.library.model.Category;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class CategoryControllerTest extends TestBase<CategoryController> {

    @Test
    public void SelectAllCategories_SUCCESS() throws SQLException {
        // Arrange

        // Act
        ResponseEntity<List<Category>> data = uut.SelectAllCategories();

        // Assert
        assertEquals(HttpStatus.OK, data.getStatusCode());
        assertEquals(1, data.getBody().size());
        verify(mockRs).getInt("category_id");
        verify(mockRs).getString("category");
        verify(mockRs).close();
        verify(mockStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void SelectAllCategories_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockRs).getInt("category_id");

        // Act
        ResponseEntity<List<Category>> data = uut.SelectAllCategories();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        assertEquals(0, data.getBody().size());
        verify(mockRs).close();
        verify(mockStatement).close();
        verify(mockConnection).close();
    }
}