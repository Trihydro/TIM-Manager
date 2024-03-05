package com.trihydro.library.helpers;

import com.trihydro.library.model.WydotTravelerInputData;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import us.dot.its.jpo.ode.plugin.SNMP;

public class SnmpHelperTest {
    @Test
    public void getSnmp_SUCCESS() {
        // Arrange
        var uut = new SnmpHelper();
        var startDateTime = "2020-12-12T00:00:00-06:00";
        var endDateTime = "2020-12-15T00:00:00-06:00";
        WydotTravelerInputData timToSend = new WydotTravelerInputData();

        // Act
        var snmp = uut.getSnmp(startDateTime, endDateTime, timToSend);

        // Assert
        Assertions.assertTrue(snmp instanceof SNMP);
        Assertions.assertEquals("83", snmp.getRsuid());
        Assertions.assertEquals(31, snmp.getMsgid());
        Assertions.assertEquals(1, snmp.getMode());
        Assertions.assertEquals(183, snmp.getChannel());
        Assertions.assertEquals(2, snmp.getInterval());
        Assertions.assertEquals(startDateTime, snmp.getDeliverystart());
        Assertions.assertEquals(endDateTime, snmp.getDeliverystop());
        Assertions.assertEquals(1, snmp.getEnable());
        Assertions.assertEquals(4, snmp.getStatus());

    }
}
