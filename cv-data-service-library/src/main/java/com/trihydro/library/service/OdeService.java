package com.trihydro.library.service;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.TimQuery;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.WydotTravelerInputData;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import us.dot.its.jpo.ode.plugin.SNMP;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame;

public class OdeService {

    public static Gson gson = new Gson();
    public static RestTemplate restTemplate = new RestTemplate();

    public static void sendNewTimToRsu(WydotTravelerInputData timToSend, String endDateTime, String odeUrl) {
        DataFrame df = timToSend.getTim().getDataframes()[0];
        timToSend.getRequest().setSnmp(getSnmp(df.getStartDateTime(), endDateTime, timToSend));

        // set msgCnt to 1 and create new packetId
        timToSend.getTim().setMsgCnt(1);

        WydotRsu wydotRsu = (WydotRsu) timToSend.getRequest().getRsus()[0];
        TimQuery timQuery = submitTimQuery(wydotRsu, 0, odeUrl);

        // query failed, don't send TIM
        if (timQuery == null) {
            Utility.logWithDate(
                    "Returning without sending TIM to RSU. submitTimQuery failed for RSU " + gson.toJson(wydotRsu));
            return;
        }

        timToSend.getRequest().getRsus()[0]
                .setRsuIndex(findFirstAvailableIndexWithRsuIndex(timQuery.getIndicies_set()));

        String timToSendJson = gson.toJson(timToSend);

        // send TIM if not a test
        try {
            Utility.logWithDate("Sending new TIM to RSU");
            restTemplate.postForObject(odeUrl + "/tim", timToSendJson, String.class);
            TimeUnit.SECONDS.sleep(10);
        } catch (RuntimeException targetException) {
            System.out.println("Send new TIM to RSU exception: " + targetException.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected static int findFirstAvailableIndexWithRsuIndex(List<Integer> indicies) {

        List<Integer> setIndexList = new ArrayList<Integer>();

        for (Integer index : indicies) {
            setIndexList.add(index);
        }

        for (int i = 2; i < 100; i++) {
            if (!setIndexList.contains(i)) {
                return i;
            }
        }

        return 0;
    }

    public static TimQuery submitTimQuery(WydotRsu rsu, int counter, String odeUrl) {

        // stop if this fails twice
        if (counter == 2)
            return null;

        // tim query to ODE
        String rsuJson = gson.toJson(rsu);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(rsuJson, headers);

        String responseStr = null;

        try {
            responseStr = restTemplate.postForObject(odeUrl + "/tim/query", entity, String.class);
        } catch (RestClientException e) {
            return submitTimQuery(rsu, counter + 1, odeUrl);
        }

        String[] items = responseStr.replaceAll("\\\"", "").replaceAll("\\:", "").replaceAll("indicies_set", "")
                .replaceAll("\\{", "").replaceAll("\\}", "").replaceAll("\\[", "").replaceAll(" ", "")
                .replaceAll("\\]", "").replaceAll("\\s", "").split(",");

        List<Integer> results = new ArrayList<Integer>();

        for (int i = 0; i < items.length; i++) {
            try {
                results.add(Integer.parseInt(items[i]));
            } catch (NumberFormatException nfe) {
                // NOTE: write something here if you need to recover from formatting errors
            }
        }

        Collections.sort(results);

        TimQuery timQuery = new TimQuery();
        timQuery.setIndicies_set(results);
        return timQuery;
    }

    public static SNMP getSnmp(String startDateTime, String endDateTime, WydotTravelerInputData timToSend) {
        SNMP snmp = new SNMP();
        snmp.setChannel(178);
        snmp.setRsuid("83");// RSU wants hex 83, and the ODE is expecting a hex value to parse. This parses
                            // to hex string 8003 when p-encoded
        snmp.setMsgid(31);
        snmp.setMode(1);
        snmp.setChannel(178);
        snmp.setInterval(2);
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
                System.out.println("Illegal Argument exception for endDate: " + illArg.getMessage());
                endDateTime = java.time.Clock.systemUTC().instant().plus(2, ChronoUnit.WEEKS).toString();
            }
        }

        snmp.setDeliverystop(endDateTime);
        snmp.setEnable(1);
        snmp.setStatus(4);

        return snmp;
    }
}