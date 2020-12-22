package com.trihydro.tasks.actions;

import static com.trihydro.tasks.TestHelper.importJsonArray;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.trihydro.library.model.RsuIndexInfo;
import com.trihydro.library.service.RsuDataService;
import com.trihydro.tasks.models.ActiveTimMapping;
import com.trihydro.tasks.models.Collision;
import com.trihydro.tasks.models.RsuInformation;
import com.trihydro.tasks.models.RsuValidationResult;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
public class ValidateRsuTest {
    @Mock
    private RsuDataService mockRsuDataService;

    @Test
    public void call_noMessages() {
        // Arrange
        when(mockRsuDataService.getRsuDeliveryStartTimes("0.0.0.0")).thenReturn(new ArrayList<RsuIndexInfo>());
        ValidateRsu uut = new ValidateRsu(new RsuInformation("0.0.0.0"), mockRsuDataService);

        // Act
        RsuValidationResult result = uut.call();

        // Assert
        Assertions.assertFalse(result.getRsuUnresponsive());
        Assertions.assertEquals(0, result.getCollisions().size());
        Assertions.assertEquals(0, result.getStaleIndexes().size());
        Assertions.assertEquals(0, result.getMissingFromRsu().size());
        Assertions.assertEquals(0, result.getUnaccountedForIndices().size());
    }

    @Test
    public void call_unresponsiveRsu() {
        // Arrange
        // Rsu Service will return null, implying that the RSU was unresponsive
        when(mockRsuDataService.getRsuDeliveryStartTimes("0.0.0.0")).thenReturn(null);
        ValidateRsu uut = new ValidateRsu(new RsuInformation("0.0.0.0"), mockRsuDataService);

        // Act
        RsuValidationResult result = uut.call();

        // Assert
        Assertions.assertTrue(result.getRsuUnresponsive());
        Assertions.assertEquals(0, result.getCollisions().size());
        Assertions.assertEquals(0, result.getStaleIndexes().size());
        Assertions.assertEquals(0, result.getMissingFromRsu().size());
        Assertions.assertEquals(0, result.getUnaccountedForIndices().size());
    }

    @Test
    public void call_rsuServiceError() {
        // Arrange
        when(mockRsuDataService.getRsuDeliveryStartTimes("0.0.0.0")).thenThrow(new RestClientException("timeout"));
        ValidateRsu uut = new ValidateRsu(new RsuInformation("0.0.0.0"), mockRsuDataService);

        // Act
        Assertions.assertThrows(RestClientException.class, () -> uut.call());
    }

    @Test
    public void call_collisions() {
        // Arrange
        RsuIndexInfo[] rsuIndexInfos = importJsonArray("/rsuIndexInfo_1.json", RsuIndexInfo[].class);
        RsuInformation assumedRsuState = importJsonArray("/populatedRsu_1.json", RsuInformation.class);

        // Due to the way validation occurs, we can't use an array-backed list
        // (Arrays.asList(...)) since we need to be able to remove elements from it.
        List<RsuIndexInfo> rsuResponse = new ArrayList<>();
        Collections.addAll(rsuResponse, rsuIndexInfos);

        when(mockRsuDataService.getRsuDeliveryStartTimes("0.0.0.0")).thenReturn(rsuResponse);
        ValidateRsu uut = new ValidateRsu(assumedRsuState, mockRsuDataService);

        // Act
        RsuValidationResult result = uut.call();

        // Assert
        // Verify that only collisions occurred
        Assertions.assertFalse(result.getRsuUnresponsive());
        Assertions.assertEquals(0, result.getStaleIndexes().size());
        Assertions.assertEquals(0, result.getMissingFromRsu().size());
        Assertions.assertEquals(0, result.getUnaccountedForIndices().size());

        Assertions.assertEquals(2, result.getCollisions().size());

        // Verify that Active Tims 10, 11 are colliding at index 1
        Collision c = result.getCollisions().get(0);
        Assertions.assertEquals(1, (int) c.getIndex());
        Assertions.assertEquals(2, c.getTims().size());
        Assertions.assertEquals(10l, (long) c.getTims().get(0).getActiveTimId());
        Assertions.assertEquals(11l, (long) c.getTims().get(1).getActiveTimId());

        // Verify that Active Tims 12, 13 are colliding at index 2
        c = result.getCollisions().get(1);
        Assertions.assertEquals(2, (int) c.getIndex());
        Assertions.assertEquals(2, c.getTims().size());
        Assertions.assertEquals(12l, (long) c.getTims().get(0).getActiveTimId());
        Assertions.assertEquals(13l, (long) c.getTims().get(1).getActiveTimId());
    }

    @Test
    public void call_staleRecord() {
        // Arrange
        // 2 Active Tims on RSU - 1 of which is stale
        RsuIndexInfo[] rsuIndexInfos = importJsonArray("/rsuIndexInfo_2.json", RsuIndexInfo[].class);
        RsuInformation assumedRsuState = importJsonArray("/populatedRsu_2.json", RsuInformation.class);

        List<RsuIndexInfo> rsuResponse = new ArrayList<>();
        Collections.addAll(rsuResponse, rsuIndexInfos);

        when(mockRsuDataService.getRsuDeliveryStartTimes("0.0.0.0")).thenReturn(rsuResponse);
        ValidateRsu uut = new ValidateRsu(assumedRsuState, mockRsuDataService);

        // Act
        RsuValidationResult result = uut.call();

        // Assert
        Assertions.assertFalse(result.getRsuUnresponsive());
        Assertions.assertEquals(0, result.getCollisions().size());
        Assertions.assertEquals(0, result.getMissingFromRsu().size());
        Assertions.assertEquals(0, result.getUnaccountedForIndices().size());

        // Only 1 Active Tim is stale
        Assertions.assertEquals(1, result.getStaleIndexes().size());
        // Verify the details of the stale Active Tim
        ActiveTimMapping staleSet = result.getStaleIndexes().get(0);
        Assertions.assertEquals("2020-01-01 00:00:00", staleSet.getRsuIndexInfo().getDeliveryStartTime());
        Assertions.assertEquals("2020-01-01 01:00:00", staleSet.getActiveTim().getStartDateTime());
        Assertions.assertEquals(10l, (long) staleSet.getActiveTim().getActiveTimId());
    }

    @Test
    public void call_missingFromRsu() {
        // Arrange
        // 2 Active Tims, claiming to be on RSU...
        RsuInformation assumedRsuState = importJsonArray("/populatedRsu_2.json", RsuInformation.class);

        // ...but the RSU service will imply that there aren't any populated indexes on
        // the RSU
        when(mockRsuDataService.getRsuDeliveryStartTimes("0.0.0.0")).thenReturn(new ArrayList<>());
        ValidateRsu uut = new ValidateRsu(assumedRsuState, mockRsuDataService);

        // Act
        RsuValidationResult result = uut.call();

        // Assert
        Assertions.assertFalse(result.getRsuUnresponsive());
        Assertions.assertEquals(0, result.getCollisions().size());
        Assertions.assertEquals(0, result.getStaleIndexes().size());
        Assertions.assertEquals(0, result.getUnaccountedForIndices().size());

        Assertions.assertEquals(2, result.getMissingFromRsu().size());
    }

    @Test
    public void call_unaccountedForIndexes() {
        // Arrange
        RsuIndexInfo[] rsuIndexInfos = importJsonArray("/rsuIndexInfo_2.json", RsuIndexInfo[].class);

        List<RsuIndexInfo> rsuResponse = new ArrayList<>();
        Collections.addAll(rsuResponse, rsuIndexInfos);

        // RSU Service implies that 2 indexes are populated... however we don't have any
        // corresponding Active Tims
        when(mockRsuDataService.getRsuDeliveryStartTimes("0.0.0.0")).thenReturn(rsuResponse);
        ValidateRsu uut = new ValidateRsu(new RsuInformation("0.0.0.0"), mockRsuDataService);

        // Act
        RsuValidationResult result = uut.call();

        // Assert
        Assertions.assertFalse(result.getRsuUnresponsive());
        Assertions.assertEquals(0, result.getCollisions().size());
        Assertions.assertEquals(0, result.getStaleIndexes().size());
        Assertions.assertEquals(0, result.getMissingFromRsu().size());

        Assertions.assertEquals(2, result.getUnaccountedForIndices().size());
        Assertions.assertEquals(1, (int) result.getUnaccountedForIndices().get(0));
        Assertions.assertEquals(2, (int) result.getUnaccountedForIndices().get(1));
    }
}