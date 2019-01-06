package com.trihydro.odewrapper.service;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import com.trihydro.odewrapper.model.ControllerResult;
import com.trihydro.odewrapper.model.TimQuery;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.odewrapper.model.WydotTravelerInputData;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;
import com.trihydro.library.model.TimType;
import com.trihydro.library.model.WydotOdeTravelerInformationMessage;
import com.trihydro.odewrapper.model.WydotTim;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import us.dot.its.jpo.ode.plugin.SNMP;
import us.dot.its.jpo.ode.plugin.ServiceRequest;
import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW;
import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW.TimeToLive;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.OdeGeoRegion;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;

import com.google.gson.Gson;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.TimRsuService;
import com.trihydro.library.service.TimService;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.RsuIndex;
import com.trihydro.library.model.TimRsu;

import org.springframework.http.MediaType;
import static java.lang.Math.toIntExact;

@Component
public class WydotTimService {

    protected static BasicConfiguration configuration;

    @Autowired
    public void setConfiguration(BasicConfiguration configurationRhs) {
        configuration = configurationRhs;
    }

    public static RestTemplate restTemplate = new RestTemplate();
    public static Gson gson = new Gson();
    private ArrayList<WydotRsu> rsus;
    private List<TimType> timTypes;
    WydotRsu[] rsuArr = new WydotRsu[1];
    DateTimeFormatter utcformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public WydotTravelerInputData createTim(WydotTim wydotTim, String direction, String timTypeStr,
            String startDateTime, String endDateTime) {

        String route = wydotTim.getRoute().replaceAll("\\D+", "");
        // build base TIM
        WydotTravelerInputData timToSend = CreateBaseTimUtil.buildTim(wydotTim, direction, route);

        // add itis codes to tim
        timToSend.getTim().getDataframes()[0]
                .setItems(wydotTim.getItisCodes().toArray(new String[wydotTim.getItisCodes().size()]));

        // overwrite start date/time if one is provided (start date/time has been set to
        // the current time in base tim creation)
        if (startDateTime != null) {
            timToSend.getTim().getDataframes()[0].setStartDateTime(startDateTime);
        }

        // set the duration if there is an enddate
        if (endDateTime != null) {
            long durationTime = getMinutesDurationBetweenTwoDates(startDateTime, endDateTime);
            timToSend.getTim().getDataframes()[0].setDurationTime(toIntExact(durationTime));
        }

        // if parking TIM
        if (timTypeStr.equals("P")) {
            // set duration for two hours
            timToSend.getTim().getDataframes()[0].setDurationTime(120);
        }

        return timToSend;
    }

    public void sendTimToSDW(WydotTim wydotTim, WydotTravelerInputData timToSend, String regionNamePrev,
            String direction, TimType timType, Integer pk) {

        List<ActiveTim> activeSatTims = null;

        // find active TIMs by client Id and direction
        activeSatTims = ActiveTimService.getActiveTimsByClientIdDirection(wydotTim.getClientId(),
                timType.getTimTypeId(), direction);

        // filter by SAT TIMs
        activeSatTims = activeSatTims.stream().filter(x -> x.getSatRecordId() != null).collect(Collectors.toList());

        if (activeSatTims != null && activeSatTims.size() > 0) {

            WydotOdeTravelerInformationMessage tim = TimService.getTim(activeSatTims.get(0).getTimId());

            String regionNameTemp = regionNamePrev + "_SAT-" + activeSatTims.get(0).getSatRecordId() + "_"
                    + timType.getType();
            if (wydotTim.getClientId() != null)
                regionNameTemp += "_" + wydotTim.getClientId();

            // add on wydot primary key if it exists
            if (pk != null)
                regionNameTemp += "_" + pk;

            timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNameTemp);
            updateTimOnSdw(timToSend, activeSatTims.get(0).getTimId(), activeSatTims.get(0).getSatRecordId(), tim);
        } else {
            String recordId = getNewRecordId();
            String regionNameTemp = regionNamePrev + "_SAT-" + recordId + "_" + timType.getType();

            if (wydotTim.getClientId() != null)
                regionNameTemp += "_" + wydotTim.getClientId();

            // add on wydot primary key if it exists
            if (pk != null)
                regionNameTemp += "_" + pk;

            timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNameTemp);
            sendNewTimToSdw(timToSend, recordId);
        }
    }

    public void sendTimToRsus(WydotTim wydotTim, WydotTravelerInputData timToSend, String regionNamePrev,
            String direction, TimType timType, Integer pk) {

        // FIND ALL RSUS TO SEND TO
        List<WydotRsu> rsus = getRsusInBuffer(direction, Math.min(wydotTim.getToRm(), wydotTim.getFromRm()),
                Math.max(wydotTim.getToRm(), wydotTim.getFromRm()), "80");

        // if no RSUs found
        if (rsus.size() == 0)
            return;

        // for each rsu in range
        for (WydotRsu rsu : rsus) {

            // update region name for active tim logger
            String regionNameTemp = regionNamePrev + "_RSU-" + rsu.getRsuTarget() + "_" + timType.getType();

            // add clientId to region name
            if (wydotTim.getClientId() != null)
                regionNameTemp += "_" + wydotTim.getClientId();

            // add on wydot primary key to region name if it exists
            if (pk != null)
                regionNameTemp += "_" + pk;

            // set region name -- used for active tim logging
            timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNameTemp);

            // look for active tim on this rsu
            ActiveTim activeTim = ActiveTimService.getActiveRsuTim(wydotTim.getClientId(), wydotTim.getDirection(), rsu.getRsuTarget());

            // if active tims exist, update tim
            if (activeTim != null) {

                WydotOdeTravelerInformationMessage tim = TimService.getTim(activeTim.getTimId());

                // update TIM rsu
                // add rsu to tim
                rsuArr[0] = rsu;
                timToSend.getRequest().setRsus(rsuArr);
                updateTimOnRsu(timToSend, activeTim.getTimId(), tim, rsu.getRsuId());
            } else {
                // send new tim to rsu
                // add rsu to tim
                rsuArr[0] = rsu;
                timToSend.getRequest().setRsus(rsuArr);
                sendNewTimToRsu(timToSend, rsu);
            }
        }
    }

    public void deleteTimsFromRsusAndSdw(List<ActiveTim> activeTims) {

        WydotRsu rsu = null;
        WydotTim wydotTim = new WydotTim();

        for (ActiveTim activeTim : activeTims) {

            wydotTim.setFromRm(activeTim.getMilepostStart());
            wydotTim.setToRm(activeTim.getMilepostStop());

            // get all tims
            WydotOdeTravelerInformationMessage tim = TimService.getTim(activeTim.getTimId());
            // get RSU TIM is on
            List<TimRsu> timRsus = TimRsuService.getTimRsusByTimId(activeTim.getTimId());
            // get full RSU

            if (timRsus.size() == 1) {
                rsu = getRsu(timRsus.get(0).getRsuId());
                // delete tim off rsu
                deleteTimFromRsu(rsu, timRsus.get(0).getRsuIndex());
            } else {
                // is satellite tim
                String route = activeTim.getRoute().replaceAll("\\D+", "");
                WydotTravelerInputData timToSend = CreateBaseTimUtil.buildTim(wydotTim, activeTim.getDirection(),
                        route);
                String[] items = new String[1];
                items[0] = "4868";
                timToSend.getTim().getDataframes()[0].setDurationTime(1);
                timToSend.getTim().getDataframes()[0].setItems(items);
                deleteTimFromSdw(timToSend, activeTim.getSatRecordId(), activeTim.getTimId(), tim);
            }

            // delete active tim
            ActiveTimService.deleteActiveTim(activeTim.getActiveTimId());
        }
    }

    public boolean clearTimsById(String timTypeStr, String clientId, String direction) {

        List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
        TimType timType = getTimType(timTypeStr);
        activeTims = ActiveTimService.getActiveTimsByClientIdDirection(clientId, timType.getTimTypeId(), direction);

        deleteTimsFromRsusAndSdw(activeTims);

        return true;
    }

    public List<ActiveTim> selectTimByClientId(String timTypeStr, String clientId) {

        TimType timType = getTimType(timTypeStr);

        List<ActiveTim> activeTims = ActiveTimService.getActiveTimsByClientIdDirection(clientId, timType.getTimTypeId(),
                null);

        return activeTims;
    }

    public List<ActiveTim> selectTimsByType(String timTypeStr) {

        TimType timType = getTimType(timTypeStr);

        List<ActiveTim> activeTims = ActiveTimService.getActivesTimByType(timType.getTimTypeId());

        return activeTims;
    }

    public long getMinutesDurationBetweenTwoDates(String startDateTime, String endDateTime) {

        ZonedDateTime zdtStart = ZonedDateTime.parse(startDateTime);
        ZonedDateTime zdtEnd = ZonedDateTime.parse(endDateTime);

        java.time.Duration dateDuration = java.time.Duration.between(zdtStart, zdtEnd);
        Math.abs(dateDuration.toMinutes());
        long durationTime = Math.abs(dateDuration.toMinutes());

        return durationTime;
    }

    public ArrayList<WydotRsu> getRsus() {
        if (rsus != null)
            return rsus;
        else {
            rsus = RsuService.selectAll();
            for (WydotRsu rsu : rsus) {
                rsu.setRsuRetries(2);
                rsu.setRsuTimeout(2000);
            }
            return rsus;
        }
    }

    public ArrayList<WydotRsu> getRsusByRoute(String route) {
        if (rsus != null)
            return rsus;
        else {
            rsus = RsuService.selectRsusByRoute(route);
            for (WydotRsu rsu : rsus) {
                rsu.setRsuRetries(3);
                rsu.setRsuTimeout(5000);
            }
            return rsus;
        }
    }

    public List<TimType> getTimTypes() {
        if (timTypes != null)
            return timTypes;
        else {
            timTypes = TimTypeService.selectAll();
            return timTypes;
        }
    }

    public List<WydotRsu> getRsusInBuffer(String direction, Double lowerMilepost, Double higherMilepost, String route) {

        List<WydotRsu> rsus = new ArrayList<>();
        Integer closestIndexOutsideRange = null;
        int i;
        Comparator<WydotRsu> compMilepost = (l1, l2) -> Double.compare(l1.getMilepost(), l2.getMilepost());
        WydotRsu entryRsu = null;
        // WydotRsu rsuHigher;

        // if there are no rsus on this route
        if (getRsusByRoute(route).size() == 0)
            return rsus;

        if (direction.equals("eastbound")) {

            // get rsus at mileposts less than your milepost
            List<WydotRsu> rsusLower = getRsusByRoute(route).stream().filter(x -> x.getMilepost() < lowerMilepost)
                    .collect(Collectors.toList());

            if (rsusLower.size() == 0) {
                // if no rsus found farther west than lowerMilepost
                // example: higherMilepost = 12, lowerMilepost = 2, no RSUs at mileposts < 2
                // find milepost furthest west than milepost of TIM location
                rsusLower = getRsusByRoute(route).stream().filter(x -> x.getMilepost() < higherMilepost)
                        .collect(Collectors.toList());

                // example: RSU at milepost 7.5 found
                entryRsu = rsusLower.stream().min(compMilepost).get();

                if ((lowerMilepost - entryRsu.getMilepost()) > 20) {
                    // don't send to RSU if its further that X amount of miles away
                    entryRsu = null;
                }
            }
            // else find milepost closest to lowerMilepost
            else {
                // get max from that list
                entryRsu = rsusLower.stream().max(compMilepost).get();
            }
            if (entryRsu == null)
                closestIndexOutsideRange = getRsusByRoute(route).indexOf(entryRsu);

        } else { // westbound

            // get rsus at mileposts greater than your milepost
            List<WydotRsu> rsusHigher = getRsusByRoute(route).stream().filter(x -> x.getMilepost() > higherMilepost)
                    .collect(Collectors.toList());

            if (rsusHigher.size() == 0) {
                rsusHigher = getRsusByRoute(route).stream().filter(x -> x.getMilepost() > lowerMilepost)
                        .collect(Collectors.toList());

                // get min from that list
                entryRsu = rsusHigher.stream().max(compMilepost).get();

                if ((entryRsu.getMilepost() - higherMilepost) > 20) {
                    // don't send to RSU if its further that X amount of miles away
                    entryRsu = null;
                }
            } else {
                entryRsu = rsusHigher.stream().min(compMilepost).get();
            }
            // get min from that list
            if (entryRsu == null)
                closestIndexOutsideRange = getRsusByRoute(route).indexOf(entryRsu);
        }

        // for (i = 0; i < getRsusByRoute(route).size(); i++) {
        // if (getRsusByRoute(route).get(i).getMilepost() >= lowerMilepost
        // && getRsusByRoute(route).get(i).getMilepost() <= higherMilepost)
        // rsus.add(getRsusByRoute(route).get(i));
        // }

        rsus = getRsusByRoute(route).stream()
                .filter(x -> x.getMilepost() >= lowerMilepost && x.getMilepost() <= higherMilepost)
                .collect(Collectors.toList());

        if (entryRsu != null)
            rsus.add(entryRsu);

        // add RSU closest in range
        if (closestIndexOutsideRange != null)
            rsus.add(getRsusByRoute(route).get(closestIndexOutsideRange));

        return rsus;
    }

    public WydotRsu getRsu(Long rsuId) {

        WydotRsu wydotRsu = null;
        try {
            wydotRsu = getRsus().stream().filter(x -> x.getRsuId() == rsuId.intValue()).findFirst().orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return wydotRsu;
    }

    public static void sendNewTimToRsu(WydotTravelerInputData timToSend, WydotRsu rsu) {

        // add snmp
        SNMP snmp = new SNMP();
        snmp.setChannel(178);
        snmp.setRsuid("00000083");
        snmp.setMsgid(31);
        snmp.setMode(1);
        snmp.setChannel(178);
        snmp.setInterval(2);
        snmp.setDeliverystart("2018-01-01T00:00:00-06:00");
        snmp.setDeliverystop("2020-01-01T00:00:00-06:00");
        snmp.setEnable(1);
        snmp.setStatus(4);
        timToSend.getRequest().setSnmp(snmp);

        // set msgCnt to 1 and create new packetId
        timToSend.getTim().setMsgCnt(1);

        Random rand = new Random();
        int randomNum = rand.nextInt(1000000) + 100000;
        String packetIdHexString = Integer.toHexString(randomNum);
        packetIdHexString = String.join("", Collections.nCopies(18 - packetIdHexString.length(), "0"))
                + packetIdHexString;
        timToSend.getTim().setPacketID(packetIdHexString);

        TimQuery timQuery = submitTimQuery(rsu, 0);

        // query failed, don't send TIM
        if(timQuery == null){
            return;
        }

        timToSend.getRequest().getRsus()[0].setRsuIndex(findFirstAvailableIndexWithRsuIndex(timQuery.getIndicies_set()));
        rsu.setRsuIndex(timToSend.getRequest().getRsus()[0].getRsuIndex());

        String timToSendJson = gson.toJson(timToSend);

        // send TIM if not a test
        try {
            restTemplate.postForObject(configuration.getOdeUrl() + "/tim", timToSendJson, String.class);
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (RuntimeException targetException) {
            System.out.println("exception");
        }
    }

    public static void sendNewTimToSdw(WydotTravelerInputData timToSend, String recordId) {

        // set msgCnt to 1 and create new packetId
        timToSend.getTim().setMsgCnt(1);

        Random rand = new Random();
        int randomNum = rand.nextInt(1000000) + 100000;
        String packetIdHexString = Integer.toHexString(randomNum);
        packetIdHexString = String.join("", Collections.nCopies(18 - packetIdHexString.length(), "0"))
                + packetIdHexString;
        timToSend.getTim().setPacketID(packetIdHexString);

        SDW sdw = new SDW();

        // calculate service region
        sdw.setServiceRegion(getServiceRegion(timToSend.getMileposts()));

        // set time to live
        sdw.setTtl(configuration.getSdwTtl());
        // set new record id
        sdw.setRecordId(recordId);

        // set sdw block in TIM
        timToSend.getRequest().setSdw(sdw);

        // send to ODE
        String timToSendJson = gson.toJson(timToSend);

        // if (!DbUtility.getConnectionEnvironment().equals("test"))
        try {
            restTemplate.postForObject(configuration.getOdeUrl() + "/tim", timToSendJson, String.class);
        } catch (RuntimeException targetException) {
            System.out.println("exception");
        }
    }

    protected static String getNewRecordId() {
        String hexChars = "ABCDEF1234567890";
        StringBuilder hexStrB = new StringBuilder();
        Random rnd = new Random();
        while (hexStrB.length() < 8) { // length of the random string.
            int index = (int) (rnd.nextFloat() * hexChars.length());
            hexStrB.append(hexChars.charAt(index));
        }
        String hexStr = hexStrB.toString();
        return hexStr;
    }

    public String convertUtcDateTimeToLocal(String utcDateTime) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime startDate = LocalDateTime.parse(utcDateTime, formatter);
        ZoneId mstZoneId = ZoneId.of("America/Denver");
        ZonedDateTime mstZonedDateTime = startDate.atZone(mstZoneId);
        String startDateTime = mstZonedDateTime.toLocalDateTime().toString() + "-06:00";

        return startDateTime;
    }

    public static void updateTimOnRsu(WydotTravelerInputData timToSend, Long timId,
            WydotOdeTravelerInformationMessage tim, Integer rsuId) {
       
        WydotTravelerInputData updatedTim = updateTim(timToSend, timId, tim);

        // set rsu index here
        TimRsu timRsu = TimRsuService.getTimRsu(timId, rsuId);
        timToSend.getRequest().getRsus()[0].setRsuIndex(timRsu.getRsuIndex());

        SNMP snmp = new SNMP();
        snmp.setChannel(178);
        snmp.setRsuid("00000083");
        snmp.setMsgid(31);
        snmp.setMode(1);
        snmp.setChannel(178);
        snmp.setInterval(2);
        snmp.setDeliverystart("2018-01-01T00:00:00-06:00");
        snmp.setDeliverystop("2020-01-01T00:00:00-06:00");
        snmp.setEnable(1);
        snmp.setStatus(4);
        timToSend.getRequest().setSnmp(snmp);

        String timToSendJson = gson.toJson(updatedTim);

        restTemplate.put(configuration.getOdeUrl() + "/tim", timToSendJson, String.class);
    }

    public static void updateTimOnSdw(WydotTravelerInputData timToSend, Long timId, String recordId,
            WydotOdeTravelerInformationMessage tim) {

        WydotTravelerInputData updatedTim = updateTim(timToSend, timId, tim);

        SDW sdw = new SDW();

        // calculate service region
        sdw.setServiceRegion(getServiceRegion(timToSend.getMileposts()));

        // set time to live
        sdw.setTtl(configuration.getSdwTtl());
        // set new record id
        sdw.setRecordId(recordId);

        // set sdw block in TIM
        updatedTim.getRequest().setSdw(sdw);

        String timToSendJson = gson.toJson(updatedTim);

        // send TIM
        try {
            restTemplate.postForObject(configuration.getOdeUrl() + "/tim", timToSendJson, String.class);
        } catch (RuntimeException targetException) {
            System.out.println("exception");
        }
    }

    public static WydotTravelerInputData updateTim(WydotTravelerInputData timToSend, Long timId,
            WydotOdeTravelerInformationMessage tim) {

        // set TIM packetId
        timToSend.getTim().setPacketID(tim.getPacketID());

        // roll msgCnt over to 1 if at 127
        if (tim.getMsgCnt() == 127)
            timToSend.getTim().setMsgCnt(1);
        // else increment msgCnt
        else
            timToSend.getTim().setMsgCnt(tim.getMsgCnt() + 1);

        return timToSend;
    }

    protected static TimQuery submitTimQuery(WydotRsu rsu, int counter) {

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
            responseStr = restTemplate.postForObject(configuration.getOdeUrl() + "/tim/query", entity, String.class);
        } catch (RestClientException e) {
            return submitTimQuery(rsu, counter + 1);
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
        // TimQuery timQuery = gson.fromJson(responseStr, TimQuery.class);

        return timQuery;
    }

    public static void deleteTimFromRsu(WydotRsu rsu, Integer index) {

        String rsuJson = gson.toJson(rsu);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(rsuJson, headers);

        try {
            System.out.println("deleting TIM " + index.toString() + " from rsu");            
            restTemplate.exchange(configuration.getOdeUrl() + "/tim?index=" + index.toString(), HttpMethod.DELETE,
                    entity, String.class);
        } catch (HttpClientErrorException e) {
            System.out.println(e.getMessage());
        } catch (RuntimeException targetException) {
            System.out.println("exception");
        }
    }

    public static void deleteTimFromSdw(WydotTravelerInputData timToSend, String recordId, Long timId,
            WydotOdeTravelerInformationMessage tim) {

        WydotTravelerInputData updatedTim = updateTim(timToSend, timId, tim);

        SDW sdw = new SDW();

        // calculate service region
        sdw.setServiceRegion(getServiceRegion(timToSend.getMileposts()));

        // set time to live
        sdw.setTtl(TimeToLive.oneminute);
        // set new record id
        sdw.setRecordId(recordId);
        // increment msgCnt
        updatedTim.getTim().setMsgCnt(updatedTim.getTim().getMsgCnt() + 1);

        // set sdw block in TIM
        updatedTim.getRequest().setSdw(sdw);

        String timToSendJson = gson.toJson(updatedTim);

        try {
            restTemplate.postForObject(configuration.getOdeUrl() + "/tim", timToSendJson, String.class);
        } catch (RuntimeException targetException) {
            System.out.println("exception");
        }
    }

    public TimType getTimType(String timTypeName) {

        // get tim type
        TimType timType = getTimTypes().stream().filter(x -> x.getType().equals(timTypeName)).findFirst().orElse(null);

        return timType;
    }

    protected static OdeGeoRegion getServiceRegion(List<Milepost> mileposts) {

        Comparator<Milepost> compLat = (l1, l2) -> Double.compare(l1.getLatitude(), l2.getLatitude());
        Comparator<Milepost> compLong = (l1, l2) -> Double.compare(l1.getLongitude(), l2.getLongitude());

        Milepost maxLat = mileposts.stream().max(compLat).get();

        Milepost minLat = mileposts.stream().min(compLat).get();

        Milepost maxLong = mileposts.stream().max(compLong).get();

        Milepost minLong = mileposts.stream().min(compLong).get();

        OdePosition3D nwCorner = new OdePosition3D();
        nwCorner.setLatitude(new BigDecimal(maxLat.getLatitude()));
        nwCorner.setLongitude(new BigDecimal(minLong.getLongitude()));

        OdePosition3D seCorner = new OdePosition3D();
        seCorner.setLatitude(new BigDecimal(minLat.getLatitude()));
        seCorner.setLongitude(new BigDecimal(maxLong.getLongitude()));

        OdeGeoRegion serviceRegion = new OdeGeoRegion();
        serviceRegion.setNwCorner(nwCorner);
        serviceRegion.setSeCorner(seCorner);
        return serviceRegion;
    }

    protected static int findFirstAvailableIndex(List<Integer> indicies) {
        for (int i = 2; i < 100; i++) {
            if (!indicies.contains(i)) {
                return i;
            }
        }
        return 0;
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

    public static boolean contains(final int[] array, final int v) {
        boolean result = false;
        for (int i : array) {
            if (i == v) {
                result = true;
                break;
            }
        }
        return result;
    }

    public Integer[] setBufferItisCodes(String action) {

        Integer[] codes = null;

        if (action.equals("leftClosed")) {
            codes = new Integer[2];
            codes[0] = 777;
            codes[1] = 13580;
        } else if (action.equals("rightClosed")) { // Right lane closed
            codes = new Integer[2];
            codes[0] = 777;
            codes[1] = 13579;
        } else if (action.equals("workers")) {
            codes = new Integer[1];
            codes[0] = 6952;
        } else if (action.equals("surfaceGravel")) { // Gravel
            codes = new Integer[1];
            codes[0] = 5933;
        } else if (action.equals("surfaceMilled")) {
            codes = new Integer[1];
            codes[0] = 6017;
        } else if (action.equals("surfaceDirt")) {
            codes = new Integer[1];
            codes[0] = 6016;
        } else if (action.contains("delay_")) { // XX Minute delays
            codes = new Integer[3];

            // Delay ITIS code
            codes[0] = 1537;

            // number (minutes) ITIS code
            String[] result = action.split("_");
            String number = result[1];
            codes[1] = Integer.parseInt(number) + 12544;

            // mintues ITIS code
            codes[2] = 8720;
        } else if (action.equals("prepareStop")) {
            codes = new Integer[1];
            codes[0] = 7186;
        } else if (action.contains("reduceSpeed_")) { // Construction zone speed limit XX from delay on Con Admin
            codes = new Integer[3];
            // Reduced speed ITIS code
            codes[0] = 7443;

            // number ITIS code
            String[] result = action.split("_");
            String number = result[1];
            codes[1] = Integer.parseInt(number) + 12544;

            // MPH ITIS code
            codes[2] = 8720;
        }

        return codes;
    }

}