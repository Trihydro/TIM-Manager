package com.trihydro.cvdatacontroller.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.List;

import com.trihydro.library.model.Category;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class CategoryControllerTest extends TestBase<CategoryController> {

    @Test
    public void SelectAllCategories_SUCCESS() throws SQLException {
        // Arrange

        // Act
        ResponseEntity<List<Category>> data = uut.SelectAllCategories();

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertEquals(1, data.getBody().size());
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
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        Assertions.assertEquals(0, data.getBody().size());
        verify(mockRs).close();
        verify(mockStatement).close();
        verify(mockConnection).close();
    }
}