package com.trihydro.library.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RegionNameTrimmerTest {

    @Mock
    private Utility utility;

    @InjectMocks
    private RegionNameTrimmer uut;

    @Test
    public void testTrimRegionName_Empty_SUCCESS() {
        String regionName = "";
        String trimmedRegionName = uut.trimRegionNameIfTooLong(regionName);
        assertEquals(regionName, trimmedRegionName);
    }

    @Test
    public void testTrimRegionName_Null_SUCCESS() {
        String regionName = null;
        String trimmedRegionName = uut.trimRegionNameIfTooLong(regionName);
        assertEquals(regionName, trimmedRegionName);
    }

    @Test
    public void testTrimRegionName_LessThanMaxLength_SUCCESS() {
        String regionName = "I_Prairie Center Cir_RSU-10.145.1.100_RC_clientid";
        String trimmedRegionName = uut.trimRegionNameIfTooLong(regionName);
        assertEquals(regionName, trimmedRegionName);
    }

    @Test
    public void testTrimRegionName_EqualsMaxLength_SUCCESS() {
        String regionName = "I_Prairie Center Circle Drive_RSU-10.145.1.100_RC_alongclientid";
        String trimmedRegionName = uut.trimRegionNameIfTooLong(regionName);
        assertEquals(regionName, trimmedRegionName);
    }

    @Test
    public void testTrimRegionName_GreaterThanMaxLength_FAILURE() {
        String regionName = "I_Prairie Center Circle Drive_RSU-10.145.1.100_RC_averyveryveryongclientidfortestingpurposes";
        assertThrows(IllegalArgumentException.class, () -> uut.trimRegionNameIfTooLong(regionName));
    }
}
