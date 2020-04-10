package com.trihydro.odewrapper.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import com.trihydro.library.model.ActiveRsuTimQueryModel;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ActiveTimHolding;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.TimQuery;
import com.trihydro.library.model.TimRsu;
import com.trihydro.library.model.TimType;
import com.trihydro.library.model.WydotOdeTravelerInformationMessage;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.WydotTim;
import com.trihydro.library.model.WydotTravelerInputData;
import com.trihydro.library.service.ActiveTimHoldingService;
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

    protected BasicConfiguration configuration;
    protected EmailHelper emailHelper;
    protected TimTypeService timTypeService;
    private SdwService sdwService;
    private Utility utility;
    private OdeService odeService;
    private CreateBaseTimUtil createBaseTimUtil;
    private ActiveTimHoldingService activeTimHoldingService;
    private ActiveTimService activeTimService;

    @Autowired
    public void InjectDependencies(BasicConfiguration configurationRhs, EmailHelper _emailHelper,
            TimTypeService _timTypeService, SdwService _sdwService, Utility _utility, OdeService _odeService,
            CreateBaseTimUtil _createBaseTimUtil, ActiveTimHoldingService _activeTimHoldingService,
            ActiveTimService _activeTimService) {
        configuration = configurationRhs;
        emailHelper = _emailHelper;
        timTypeService = _timTypeService;
        sdwService = _sdwService;
        utility = _utility;
        odeService = _odeService;
        createBaseTimUtil = _createBaseTimUtil;
        activeTimHoldingService = _activeTimHoldingService;
        activeTimService = _activeTimService;
    }

    public RestTemplate restTemplate = RestTemplateProvider.GetRestTemplate();
    public Gson gson = new Gson();
    private List<WydotRsu> rsus;
    private List<TimType> timTypes;
    WydotRsu[] rsuArr = new WydotRsu[1];
    DateTimeFormatter utcformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public WydotTravelerInputData createTim(WydotTim wydotTim, String direction, String timTypeStr,
            String startDateTime, String endDateTime) {

        // build base TIM
        WydotTravelerInputData timToSend = createBaseTimUtil.buildTim(wydotTim, direction, configuration);

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
            int durationTime = utility.getMinutesDurationBetweenTwoDates(startDateTime, endDateTime);
            timToSend.getTim().getDataframes()[0].setDurationTime(durationTime);
        }

        // if parking TIM
        if (timTypeStr.equals("P")) {
            // set duration for two hours
            timToSend.getTim().getDataframes()[0].setDurationTime(120);
        }

        // set PacketId to a random 18 character hex value
        Random rand = new Random();
        StringBuffer sb = new StringBuffer();
        while (sb.length() < 18) {
            sb.append(Integer.toHexString(rand.nextInt()));
        }
        timToSend.getTim().setPacketID(sb.toString().substring(0, 18).toUpperCase());

        return timToSend;
    }

    public void sendTimToSDW(WydotTim wydotTim, WydotTravelerInputData timToSend, String regionNamePrev,
            String direction, TimType timType, Integer pk) {

        List<ActiveTim> activeSatTims = null;

        // find active TIMs by client Id and direction
        activeSatTims = activeTimService.getActiveTimsByClientIdDirection(wydotTim.getClientId(),
                timType.getTimTypeId(), direction);

        // filter by SAT TIMs
        activeSatTims = activeSatTims.stream().filter(x -> x.getSatRecordId() != null).collect(Collectors.toList());

        String recordId = activeSatTims != null && activeSatTims.size() > 0 ? activeSatTims.get(0).getSatRecordId()
                : sdwService.getNewRecordId();

        // save new active_tim_holding record
        ActiveTimHolding activeTimHolding = new ActiveTimHolding(wydotTim, null, recordId);
        activeTimHolding.setDirection(direction);// we are overriding the direction from the tim here
        activeTimHoldingService.insertActiveTimHolding(activeTimHolding);

        String regionNameTemp = regionNamePrev + "_SAT-" + recordId + "_" + timType.getType();
        if (wydotTim.getClientId() != null)
            regionNameTemp += "_" + wydotTim.getClientId();

        // add on wydot primary key if it exists
        if (pk != null)
            regionNameTemp += "_" + pk;

        timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNameTemp);

        if (activeSatTims != null && activeSatTims.size() > 0) {

            WydotOdeTravelerInformationMessage tim = TimService.getTim(activeSatTims.get(0).getTimId());
            updateTimOnSdw(timToSend, activeSatTims.get(0).getTimId(), activeSatTims.get(0).getSatRecordId(), tim);
        } else {
            sendNewTimToSdw(timToSend, recordId);
        }
    }

    public void sendTimToRsus(WydotTim wydotTim, WydotTravelerInputData timToSend, String regionNamePrev,
            String direction, TimType timType, Integer pk, String endDateTime) {

        // FIND ALL RSUS TO SEND TO
        // TODO: should this query a graph db instead to follow with milepost?
        List<WydotRsu> rsus = utility.getRsusByLatLong(direction, wydotTim.getStartPoint(), wydotTim.getEndPoint(),
                wydotTim.getRoute());

        // if no RSUs found
        if (rsus.size() == 0) {
            utility.logWithDate("No RSUs found to place TIM on, returning");
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
            ActiveRsuTimQueryModel artqm = new ActiveRsuTimQueryModel(wydotTim.getDirection(), wydotTim.getClientId(),
                    rsu.getRsuTarget());
            ActiveTim activeTim = activeTimService.getActiveRsuTim(artqm);

            // create new active_tim_holding record
            ActiveTimHolding activeTimHolding = new ActiveTimHolding(wydotTim, rsu.getRsuTarget(), null);
            activeTimHolding.setDirection(direction);

            // if active tims exist, update tim
            if (activeTim != null) {
                activeTimHoldingService.insertActiveTimHolding(activeTimHolding);
                WydotOdeTravelerInformationMessage tim = TimService.getTim(activeTim.getTimId());

                // update TIM rsu
                // add rsu to tim
                rsuArr[0] = rsu;
                timToSend.getRequest().setRsus(rsuArr);
                updateTimOnRsu(timToSend, activeTim.getTimId(), tim, rsu.getRsuId(), endDateTime);
            } else {
                // send new tim to rsu
                // first fetch existing active_tim_holding records
                List<ActiveTimHolding> existingHoldingRecords = activeTimHoldingService
                        .getActiveTimHoldingForRsu(rsu.getRsuTarget());

                TimQuery timQuery = OdeService.submitTimQuery(rsu, 0, configuration.getOdeUrl());
                // append existing holding indices
                existingHoldingRecords.forEach(x -> timQuery.appendIndex(x.getRsuIndex()));
                Integer nextRsuIndex = OdeService.findFirstAvailableIndexWithRsuIndex(timQuery.getIndicies_set());
                activeTimHolding.setRsuIndex(nextRsuIndex);
                activeTimHoldingService.insertActiveTimHolding(activeTimHolding);

                // add rsu to tim
                rsuArr[0] = rsu;
                timToSend.getRequest().setRsus(rsuArr);
                odeService.sendNewTimToRsu(timToSend, endDateTime, configuration.getOdeUrl(), nextRsuIndex);
            }
        }
    }

    public void deleteTimsFromRsusAndSdx(List<ActiveTim> activeTims) {

        WydotRsu rsu = null;

        // split activeTims into sat and rsu for processing
        List<ActiveTim> satTims = activeTims.stream().filter(x -> StringUtils.isNotBlank(x.getSatRecordId()))
                .collect(Collectors.toList());
        List<ActiveTim> rsuTims = activeTims.stream().filter(x -> StringUtils.isBlank(x.getSatRecordId()))
                .collect(Collectors.toList());

        for (ActiveTim activeTim : rsuTims) {
            // get RSU TIM is on
            List<TimRsu> timRsus = TimRsuService.getTimRsusByTimId(activeTim.getTimId());
            // get full RSU

            if (timRsus.size() > 0) {
                for (TimRsu timRsu : timRsus) {
                    rsu = getRsu(timRsu.getRsuId());
                    // delete tim off rsu
                    utility.logWithDate("Deleting TIM from RSU. Corresponding tim_id: " + activeTim.getTimId());
                    deleteTimFromRsu(rsu, timRsu.getRsuIndex());
                }
            }
            // delete active tim
            activeTimService.deleteActiveTim(activeTim.getActiveTimId());
        }

        if (satTims != null && satTims.size() > 0) {
            // Get the sat_record_id values and active_tim_id values
            List<String> satRecordIds = satTims.stream().map(ActiveTim::getSatRecordId).collect(Collectors.toList());
            List<Long> activeSatTimIds = satTims.stream().map(ActiveTim::getActiveTimId).collect(Collectors.toList());

            // Issue one delete call to the REST service, encompassing all sat_record_ids
            HashMap<Integer, Boolean> sdxDelResults = sdwService.deleteSdxDataBySatRecordId(satRecordIds);

            // Determine if anything failed
            Boolean errorsOccurred = sdxDelResults.entrySet().stream()
                    .anyMatch(x -> x.getValue() != null && x.getValue() == false);
            if (errorsOccurred) {
                // pull out failed deletions for corresponding active_tim records so we don't
                // orphan them
                Stream<Entry<Integer, Boolean>> failedStream = sdxDelResults.entrySet().stream()
                        .filter(x -> x.getValue() == false);
                List<Integer> failedSatRecords = failedStream.map(x -> x.getKey()).collect(Collectors.toList());

                activeSatTimIds = satTims.stream()
                        .filter(x -> !failedSatRecords.contains(Integer.parseUnsignedInt(x.getSatRecordId(), 16)))
                        .map(ActiveTim::getActiveTimId).collect(Collectors.toList());
                String failedResultsText = sdxDelResults.entrySet().stream().filter(x -> x.getValue() == false)
                        .map(x -> x.getKey().toString()).collect(Collectors.joining(","));
                if (StringUtils.isNotBlank(failedResultsText)) {
                    String body = "The following recordIds failed to delete from the SDX: " + failedResultsText;
                    try {
                        emailHelper.SendEmail(configuration.getAlertAddresses(), null, "SDX Delete Fail", body,
                                configuration.getMailPort(), configuration.getMailHost(), configuration.getFromEmail());
                    } catch (Exception ex) {
                        utility.logWithDate(body + ", and the email failed to send to support");
                        ex.printStackTrace();
                    }
                }
            }

            activeTimService.deleteActiveTimsById(activeSatTimIds);
        }
    }

    public boolean clearTimsById(String timTypeStr, String clientId, String direction) {

        List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
        TimType timType = getTimType(timTypeStr);
        activeTims = activeTimService.getActiveTimsByClientIdDirection(clientId, timType.getTimTypeId(), direction);
        utility.logWithDate(activeTims.size() + " active_tim found for deletion");

        deleteTimsFromRsusAndSdx(activeTims);

        return true;
    }

    public boolean deleteWydotTimsByType(List<? extends WydotTim> wydotTims, String timTypeStr) {
        List<ActiveTim> aTims = new ArrayList<ActiveTim>();
        TimType timType = getTimType(timTypeStr);
        aTims = activeTimService.getActiveTimsByWydotTim(wydotTims, timType.getTimTypeId());
        deleteTimsFromRsusAndSdx(aTims);

        return true;
    }

    public List<ActiveTim> selectTimByClientId(String timTypeStr, String clientId) {

        TimType timType = getTimType(timTypeStr);

        List<ActiveTim> activeTims = activeTimService.getActiveTimsByClientIdDirection(clientId, timType.getTimTypeId(),
                null);

        return activeTims;
    }

    public List<ActiveTim> selectTimsByType(String timTypeStr) {

        TimType timType = getTimType(timTypeStr);

        List<ActiveTim> activeTims = activeTimService.getActivesTimByType(timType.getTimTypeId());

        return activeTims;
    }

    public List<WydotRsu> getRsus() {
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
            timTypes = timTypeService.selectAll();
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

    public void sendNewTimToSdw(WydotTravelerInputData timToSend, String recordId) {

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
            utility.logWithDate("Sending new TIM to SDW. sat_record_id: " + recordId);
            restTemplate.postForObject(configuration.getOdeUrl() + "/tim", timToSendJson, String.class);
        } catch (RuntimeException targetException) {
            System.out.println("Failed to send new TIM to SDW");
            targetException.printStackTrace();
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

    public void updateTimOnRsu(WydotTravelerInputData timToSend, Long timId, WydotOdeTravelerInformationMessage tim,
            Integer rsuId, String endDateTime) {

        WydotTravelerInputData updatedTim = updateTim(timToSend, timId, tim);

        // set rsu index here
        DataFrame df = timToSend.getTim().getDataframes()[0];
        TimRsu timRsu = TimRsuService.getTimRsu(timId, rsuId);
        timToSend.getRequest().getRsus()[0].setRsuIndex(timRsu.getRsuIndex());
        timToSend.getRequest().setSnmp(OdeService.getSnmp(df.getStartDateTime(), endDateTime, timToSend));

        String timToSendJson = gson.toJson(updatedTim);

        try {
            utility.logWithDate("Updating TIM on RSU. tim_id: " + timId);
            restTemplate.put(configuration.getOdeUrl() + "/tim", timToSendJson, String.class);
        } catch (RestClientException ex) {
            utility.logWithDate("Failed to send update to RSU");
        }
    }

    public void updateTimOnSdw(WydotTravelerInputData timToSend, Long timId, String recordId,
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
            utility.logWithDate("Updating TIM on SDW. tim_id: " + timId + ", sat_record_id: " + recordId);
            restTemplate.postForObject(configuration.getOdeUrl() + "/tim", timToSendJson, String.class);
        } catch (RuntimeException targetException) {
            utility.logWithDate("exception updating tim on SDW");
            targetException.printStackTrace();
        }
    }

    public WydotTravelerInputData updateTim(WydotTravelerInputData timToSend, Long timId,
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

    public void deleteTimFromRsu(WydotRsu rsu, Integer index) {

        String rsuJson = gson.toJson(rsu);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(rsuJson, headers);

        try {
            utility.logWithDate("deleting TIM on index " + index.toString() + " from rsu " + rsu.getRsuTarget());
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

    protected OdeGeoRegion getServiceRegion(List<Milepost> mileposts) {

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