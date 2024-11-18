package com.trihydro.certexpiration.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoopControllerTest {
    
    private LoopController uut;

    @BeforeEach
    public void setUp() {
        uut = new LoopController();
    }

    @Test
    public void loop_SUCCESS(){
        // Arrange
        
        // Act
        var loopResult = uut.loop();
    
        // Assert
        Assertions.assertTrue(loopResult);
    }
}
