package com.trihydro.library.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.OdeProps;
import com.trihydro.library.model.TimQuery;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.WydotTravelerInputData;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import us.dot.its.jpo.ode.plugin.SNMP;
import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW;
import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW.TimeToLive;
import us.dot.its.jpo.ode.plugin.j2735.OdeGeoRegion;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame;

@Component
public class OdeService {

    private Gson gson = new Gson();
    private RestTemplateProvider restTemplateProvider;
    private Utility utility;
    private OdeProps odeProps;

    @Autowired
    public void InjectDependencies(Utility _utility, RestTemplateProvider _restTemplateProvider, OdeProps _odeProps) {
        utility = _utility;
        restTemplateProvider = _restTemplateProvider;
        odeProps = _odeProps;
    }

    public String sendNewTimToRsu(WydotTravelerInputData timToSend, String endDateTime, Integer index) {
        DataFrame df = timToSend.getTim().getDataframes()[0];
        timToSend.getRequest().setSnmp(getSnmp(df.getStartDateTime(), endDateTime, timToSend));

        // set msgCnt to 1 and create new packetId
        timToSend.getTim().setMsgCnt(1);
        timToSend.getRequest().getRsus()[0].setRsuIndex(index);

        // String timToSendJson = gson.toJson(timToSend);

        String exMsg = "";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<WydotTravelerInputData> entity = new HttpEntity<WydotTravelerInputData>(timToSend, headers);
        ResponseEntity<String> response = restTemplateProvider.GetRestTemplate_NoErrors()
                .exchange(odeProps.getOdeUrl() + "/tim", HttpMethod.POST, entity, String.class);
        if (response.getStatusCode().series() != HttpStatus.Series.SUCCESSFUL) {
            exMsg = "Failed to send new TIM to RSU: " + response.getBody();
            utility.logWithDate(exMsg);
        }
        return exMsg;
    }

    /**
     * 
     * @param timToSend The TIM to submit to the ODE
     * @return String representing any errors. If string is empty, no errors
     *         occured.
     */
    public String updateTimOnRsu(WydotTravelerInputData timToSend) {
        String exMsg = "";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<WydotTravelerInputData> entity = new HttpEntity<WydotTravelerInputData>(timToSend, headers);
        ResponseEntity<String> response = restTemplateProvider.GetRestTemplate_NoErrors()
                .exchange(odeProps.getOdeUrl() + "/tim", HttpMethod.PUT, entity, String.class);
        if (response.getStatusCode().series() != HttpStatus.Series.SUCCESSFUL) {
            exMsg = "Failed to update TIM on RSU: " + response.getBody();
            utility.logWithDate(exMsg);
        }
        return exMsg;
    }

    public String sendNewTimToSdw(WydotTravelerInputData timToSend, String recordId, List<Milepost> mps,
            TimeToLive ttl) {
        String exMsg = "";
        // set msgCnt to 1 and create new packetId
        timToSend.getTim().setMsgCnt(1);

        SDW sdw = new SDW();

        // calculate service region
        sdw.setServiceRegion(getServiceRegion(mps));

        // set time to live
        sdw.setTtl(ttl);
        // set new record id
        sdw.setRecordId(recordId);

        // set sdw block in TIM
        timToSend.getRequest().setSdw(sdw);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<WydotTravelerInputData> entity = new HttpEntity<WydotTravelerInputData>(timToSend, headers);
        ResponseEntity<String> response = restTemplateProvider.GetRestTemplate_NoErrors()
                .exchange(odeProps.getOdeUrl() + "/tim", HttpMethod.POST, entity, String.class);
        if (response.getStatusCode().series() != HttpStatus.Series.SUCCESSFUL) {
            exMsg = "Failed to send new TIM to SDX: " + response.getBody();
            utility.logWithDate(exMsg);
        }
        return exMsg;
    }

    public String updateTimOnSdw(WydotTravelerInputData timToSend) {

        String exMsg = "";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<WydotTravelerInputData> entity = new HttpEntity<WydotTravelerInputData>(timToSend, headers);
        ResponseEntity<String> response = restTemplateProvider.GetRestTemplate_NoErrors()
                .exchange(odeProps.getOdeUrl() + "/tim", HttpMethod.PUT, entity, String.class);
        if (response.getStatusCode().series() != HttpStatus.Series.SUCCESSFUL) {
            exMsg = "Failed to update TIM on SDX: " + response.getBody();
            utility.logWithDate(exMsg);
        }
        return exMsg;
    }

    public Integer findFirstAvailableIndexWithRsuIndex(List<Integer> indicies) {
        if (indicies == null) {
            return null;
        }

        for (int i = 2; i < 100; i++) {
            if (!indicies.contains(i)) {
                return i;
            }
        }

        return null;
    }

    public TimQuery submitTimQuery(WydotRsu rsu, int counter) {

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
            responseStr = restTemplateProvider.GetRestTemplate().postForObject(odeProps.getOdeUrl() + "/tim/query",
                    entity, String.class);
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
        return timQuery;
    }

    public SNMP getSnmp(String startDateTime, String endDateTime, WydotTravelerInputData timToSend) {
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

    public OdeGeoRegion getServiceRegion(List<Milepost> mileposts) {

        Comparator<Milepost> compLat = (l1, l2) -> l1.getLatitude().compareTo(l2.getLatitude());
        Comparator<Milepost> compLong = (l1, l2) -> l1.getLongitude().compareTo(l2.getLongitude());
        OdeGeoRegion serviceRegion = new OdeGeoRegion();

        if (mileposts.size() > 0) {

            Milepost maxLat = mileposts.stream().max(compLat).get();

            Milepost minLat = mileposts.stream().min(compLat).get();

            Milepost maxLong = mileposts.stream().max(compLong).get();

            Milepost minLong = mileposts.stream().min(compLong).get();

            OdePosition3D nwCorner = new OdePosition3D();
            nwCorner.setLatitude(maxLat.getLatitude());
            nwCorner.setLongitude(minLong.getLongitude());

            OdePosition3D seCorner = new OdePosition3D();
            seCorner.setLatitude(minLat.getLatitude());
            seCorner.setLongitude(maxLong.getLongitude());

            serviceRegion.setNwCorner(nwCorner);
            serviceRegion.setSeCorner(seCorner);
        } else {
            System.out.println("getServiceRegion fails due to no mileposts");
        }
        return serviceRegion;
    }

}