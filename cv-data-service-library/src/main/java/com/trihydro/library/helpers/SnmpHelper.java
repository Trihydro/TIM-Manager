package com.trihydro.library.helpers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.trihydro.library.model.WydotTravelerInputData;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.plugin.SNMP;

@Component
@Slf4j
public class SnmpHelper {

    public SNMP getSnmp(String startDateTime, String endDateTime, WydotTravelerInputData timToSend) {
        SNMP snmp = new SNMP();
        snmp.setChannel(183);
        snmp.setRsuid("83");// RSU wants hex 83, and the ODE is expecting a hex value to parse. This parses
                            // to hex string 8003 when p-encoded
        snmp.setMsgid(31);
        snmp.setMode(1);
        snmp.setInterval(2000); // specified in milliseconds, as required by 4.1 & NTCIP1218
        snmp.setDeliverystart(startDateTime);// "2018-01-01T00:00:00-06:00");

        if (endDateTime == null || StringUtils.isBlank(endDateTime)) {
            try {
                int durationTime = timToSend.getTim().getDataframes()[0].getDurationTime();
                Calendar cal = javax.xml.bind.DatatypeConverter.parseDateTime(startDateTime);
                cal.add(Calendar.MINUTE, durationTime);
                Date endDate = cal.getTime();

                TimeZone tz = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
                df.setTimeZone(tz);
                endDateTime = df.format(endDate);
            } catch (IllegalArgumentException illArg) {
                // if we failed here, set the endDateTime for 2 weeks from current time
                log.error("Illegal Argument exception for endDate: {}", illArg.getMessage(), illArg);
                endDateTime = java.time.Clock.systemUTC().instant().plus(2, ChronoUnit.WEEKS).toString();
            }
        }

        snmp.setDeliverystop(endDateTime);
        snmp.setEnable(1);
        snmp.setStatus(4);

        return snmp;
    }
}
