package com.trihydro.odewrapper.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.TimRsu;
import com.trihydro.library.model.TimType;
import com.trihydro.library.model.WydotOdeTravelerInformationMessage;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.WydotTim;
import com.trihydro.library.model.WydotTravelerInputData;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.OdeService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.SdwService;
import com.trihydro.library.service.TimRsuService;
import com.trihydro.library.service.TimService;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW;
import us.dot.its.jpo.ode.plugin.j2735.OdeGeoRegion;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame;

@Component
public class WydotTimService {

    protected static BasicConfiguration configuration;

    @Autowired
    public WydotTimService(BasicConfiguration configurationRhs) {
        configuration = configurationRhs;
    }

    public static RestTemplate restTemplate = RestTemplateProvider.GetRestTemplate();
    public static Gson gson = new Gson();
    private ArrayList<WydotRsu> rsus;
    private List<TimType> timTypes;
    WydotRsu[] rsuArr = new WydotRsu[1];
    DateTimeFormatter utcformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public WydotTravelerInputData createTim(WydotTim wydotTim, String direction, String timTypeStr,
            String startDateTime, String endDateTime) {

        String route = wydotTim.getRoute().replaceAll("\\D+", "");
        // build base TIM
        WydotTravelerInputData timToSend = CreateBaseTimUtil.buildTim(wydotTim, direction, route, configuration);

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
            int durationTime = Utility.getMinutesDurationBetweenTwoDates(startDateTime, endDateTime);
            timToSend.getTim().getDataframes()[0].setDurationTime(durationTime);
        }

        // if parking TIM
        if (timTypeStr.equals("P")) {
            // set duration for two hours
            timToSend.getTim().getDataframes()[0].setDurationTime(120);
        }

        Random rand = new Random();
        int randomNum = rand.nextInt(1000000) + 100000;
        String packetIdHexString = Integer.toHexString(randomNum);
        packetIdHexString = String.join("", Collections.nCopies(18 - packetIdHexString.length(), "0"))
                + packetIdHexString;
        timToSend.getTim().setPacketID(packetIdHexString);

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
            String recordId = SdwService.getNewRecordId();
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
            String direction, TimType timType, Integer pk, String endDateTime) {

        // FIND ALL RSUS TO SEND TO
        List<WydotRsu> rsus = Utility.getRsusInBuffer(direction, Math.min(wydotTim.getToRm(), wydotTim.getFromRm()),
                Math.max(wydotTim.getToRm(), wydotTim.getFromRm()), "80");

        // if no RSUs found
        if (rsus.size() == 0) {
            Utility.logWithDate("No RSUs found to place TIM on, returning");
            return;
        }

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
            ActiveTim activeTim = ActiveTimService.getActiveRsuTim(wydotTim.getClientId(), wydotTim.getDirection(),
                    rsu.getRsuTarget());

            // if active tims exist, update tim
            if (activeTim != null) {

                WydotOdeTravelerInformationMessage tim = TimService.getTim(activeTim.getTimId());

                // update TIM rsu
                // add rsu to tim
                rsuArr[0] = rsu;
                timToSend.getRequest().setRsus(rsuArr);
                updateTimOnRsu(timToSend, activeTim.getTimId(), tim, rsu.getRsuId(), endDateTime);
            } else {
                // send new tim to rsu
                // add rsu to tim
                rsuArr[0] = rsu;
                timToSend.getRequest().setRsus(rsuArr);
                OdeService.sendNewTimToRsu(timToSend, endDateTime, configuration.getOdeUrl());
            }
        }
    }

    public void deleteTimsFromRsusAndSdx(List<ActiveTim> activeTims) {

        WydotRsu rsu = null;
        WydotTim wydotTim = new WydotTim();

        // split activeTims into sat and rsu for processing
        List<ActiveTim> satTims = activeTims.stream().filter(x -> StringUtils.isNotBlank(x.getSatRecordId()))
                .collect(Collectors.toList());
        List<ActiveTim> rsuTims = activeTims.stream().filter(x -> StringUtils.isBlank(x.getSatRecordId()))
                .collect(Collectors.toList());

        for (ActiveTim activeTim : rsuTims) {

            wydotTim.setFromRm(activeTim.getMilepostStart());
            wydotTim.setToRm(activeTim.getMilepostStop());

            // get RSU TIM is on
            List<TimRsu> timRsus = TimRsuService.getTimRsusByTimId(activeTim.getTimId());
            // get full RSU

            if (timRsus.size() == 1) {
                rsu = getRsu(timRsus.get(0).getRsuId());
                // delete tim off rsu
                Utility.logWithDate("Deleting TIM from RSU. Corresponding tim_id: " + activeTim.getTimId());
                deleteTimFromRsu(rsu, timRsus.get(0).getRsuIndex());
            }
            // delete active tim
            ActiveTimService.deleteActiveTim(activeTim.getActiveTimId());
        }

        if (satTims != null && satTims.size() > 0) {
            // Get the sat_record_id values and active_tim_id values
            List<String> satRecordIds = satTims.stream().map(ActiveTim::getSatRecordId).collect(Collectors.toList());
            List<Long> activeSatTimIds = satTims.stream().map(ActiveTim::getActiveTimId).collect(Collectors.toList());

            // Issue one delete call to the REST service, encompassing all sat_record_ids
            HashMap<Long, Boolean> sdxDelResults = SdwService.deleteSdxDataBySatRecordId(satRecordIds);

            // Determine if anything failed
            Stream<Entry<Long, Boolean>> failedStream = sdxDelResults.entrySet().stream()
                    .filter(x -> x.getValue() == false);
            List<Long> failedSatRecords = failedStream.map(x -> new Long(x.getKey())).collect(Collectors.toList());
            if (failedSatRecords.size() > 0) {
                // pull out failed deletions for corresponding active_tim records so we don't
                // orphan them
                activeSatTimIds = satTims.stream()
                        .filter(x -> !failedSatRecords.contains(Long.parseLong(x.getSatRecordId(), 16)))
                        .map(ActiveTim::getActiveTimId).collect(Collectors.toList());
                String failedResultsText = sdxDelResults.entrySet().stream().filter(x -> x.getValue() == false)
                        .map(x -> x.getKey().toString()).collect(Collectors.joining(","));
                if (StringUtils.isNotBlank(failedResultsText)) {
                    String body = "The following recordIds failed to delete from the SDX: " + failedResultsText;
                    try {
                        EmailHelper.SendEmail(configuration.getAlertAddresses(), null, "SDX Delete Fail", body,
                                configuration);
                    } catch (Exception ex) {
                        Utility.logWithDate(body + ", and the email failed to send to support");
                        ex.printStackTrace();                        
                    }
                }
            }

            ActiveTimService.deleteActiveTimsById(activeSatTimIds);
        }
    }

    public boolean clearTimsById(String timTypeStr, String clientId, String direction) {

        List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
        TimType timType = getTimType(timTypeStr);
        activeTims = ActiveTimService.getActiveTimsByClientIdDirection(clientId, timType.getTimTypeId(), direction);
        Utility.logWithDate(activeTims.size() + " active_tim found for deletion");

        deleteTimsFromRsusAndSdx(activeTims);

        return true;
    }

    public boolean deleteWydotTimsByType(List<? extends WydotTim> wydotTims, String timTypeStr) {
        List<ActiveTim> aTims = new ArrayList<ActiveTim>();
        TimType timType = getTimType(timTypeStr);
        aTims = ActiveTimService.getActiveTimsByWydotTim(wydotTims, timType.getTimTypeId());
        deleteTimsFromRsusAndSdx(aTims);

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

    public List<TimType> getTimTypes() {
        if (timTypes != null)
            return timTypes;
        else {
            timTypes = TimTypeService.selectAll();
            return timTypes;
        }
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

    public static void sendNewTimToSdw(WydotTravelerInputData timToSend, String recordId) {

        // set msgCnt to 1 and create new packetId
        timToSend.getTim().setMsgCnt(1);

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

        try {
            Utility.logWithDate("Sending new TIM to SDW. sat_record_id: " + recordId);
            restTemplate.postForObject(configuration.getOdeUrl() + "/tim", timToSendJson, String.class);
        } catch (RuntimeException targetException) {
            System.out.println("exception");
        }
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
            WydotOdeTravelerInformationMessage tim, Integer rsuId, String endDateTime) {

        WydotTravelerInputData updatedTim = updateTim(timToSend, timId, tim);

        // set rsu index here
        DataFrame df = timToSend.getTim().getDataframes()[0];
        TimRsu timRsu = TimRsuService.getTimRsu(timId, rsuId);
        timToSend.getRequest().getRsus()[0].setRsuIndex(timRsu.getRsuIndex());
        timToSend.getRequest().setSnmp(OdeService.getSnmp(df.getStartDateTime(), endDateTime, timToSend));

        String timToSendJson = gson.toJson(updatedTim);

        try {
            Utility.logWithDate("Updating TIM on RSU. tim_id: " + timId);
            restTemplate.put(configuration.getOdeUrl() + "/tim", timToSendJson, String.class);
        } catch (RestClientException ex) {
            Utility.logWithDate("Failed to send update to RSU");
        }
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
            Utility.logWithDate("Updating TIM on SDW. tim_id: " + timId + ", sat_record_id: " + recordId);
            restTemplate.postForObject(configuration.getOdeUrl() + "/tim", timToSendJson, String.class);
        } catch (RuntimeException targetException) {
            Utility.logWithDate("exception updating tim on SDW");
            targetException.printStackTrace();
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

    public static void deleteTimFromRsu(WydotRsu rsu, Integer index) {

        String rsuJson = gson.toJson(rsu);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(rsuJson, headers);

        try {
            Utility.logWithDate("deleting TIM on index " + index.toString() + " from rsu " + rsu.getRsuTarget());
            restTemplate.exchange(configuration.getOdeUrl() + "/tim?index=" + index.toString(), HttpMethod.DELETE,
                    entity, String.class);
        } catch (HttpClientErrorException e) {
            System.out.println(e.getMessage());
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