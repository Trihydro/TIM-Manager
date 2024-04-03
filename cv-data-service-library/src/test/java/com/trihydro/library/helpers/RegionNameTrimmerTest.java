package com.trihydro.library.helpers;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RegionNameTrimmerTest {

    @InjectMocks
    private RegionNameTrimmer uut;

    @Test
    public void testTrimRegionName_LessThanMaxLength() {
        String regionName = "Region Name";
        String trimmedRegionName = uut.trimRegionNameIfTooLong(regionName);
        assertEquals(regionName, trimmedRegionName);
    }

    @Test
    public void testTrimRegionName_EqualsMaxLength() {
        String regionName = "123456789012345678901234567890123456789012345678901234567890123";
        String trimmedRegionName = uut.trimRegionNameIfTooLong(regionName);
        assertEquals(regionName, trimmedRegionName);
    }

    @Test
    public void testTrimRegionName_GreaterThanMaxLength() {
        String regionName = "1234567890123456789012345678901234567890123456789012345678901234";
        String trimmedRegionName = uut.trimRegionNameIfTooLong(regionName);
        String expectedTrimmedRegionName = "123456789012345678901234567890123456789012345678901234567890...";
        assertEquals(expectedTrimmedRegionName, trimmedRegionName);
    }

    @Test
    public void testTrimRegionName_Empty() {
        String regionName = "";
        String trimmedRegionName = uut.trimRegionNameIfTooLong(regionName);
        assertEquals(regionName, trimmedRegionName);
    }

    @Test
    public void testTrimRegionName_Null() {
        String regionName = null;
        String trimmedRegionName = uut.trimRegionNameIfTooLong(regionName);
        assertEquals(regionName, trimmedRegionName);
    }

    @Test
    public void testTrimRegionName_RegionNameWithCascadeTimIdDelimiter() {
        String regionName = "123456789012345678901234567890123456789012345678901234567890123_trgd_12345";
        String trimmedRegionName = uut.trimRegionNameIfTooLong(regionName);
        String expectedTrimmedRegionName = "1234567890123456789012345678901234567890123456789..._trgd_12345";
        assertEquals(expectedTrimmedRegionName, trimmedRegionName);
    }

    @Test
    public void testTrimRegionName_RegionNameWithCascadeTimIdDelimiter_RsuData_ClientIdCutOffPartly() {
        String regionName = "I_Prairie Center Cir_RSU-10.145.1.100_RC_somelongclientid_trgd_12345";
        String trimmedRegionName = uut.trimRegionNameIfTooLong(regionName);
        String expectedTrimmedRegionName = "I_Prairie Center Cir_RSU-10.145.1.100_RC_somelong..._trgd_12345";
        assertEquals(expectedTrimmedRegionName, trimmedRegionName);
    }

    @Test
    public void testTrimRegionName_RegionNameWithCascadeTimIdDelimiter_RsuData_ClientIdCutOffFully() {
        String regionName = "I_Prairie Center Circle Drive_RSU-10.145.1.100_RC_somelongclientid_trgd_12345";
        String trimmedRegionName = uut.trimRegionNameIfTooLong(regionName);
        String expectedTrimmedRegionName = "I_Prairie Center Circle Drive_RSU-10.145.1.100_RC..._trgd_12345";
        assertEquals(expectedTrimmedRegionName, trimmedRegionName);
    }
    
}
