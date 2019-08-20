package com.trihydro.timrefresh.service;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
// import com.trihydro.odewrapper.model.TimQuery;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.timrefresh.config.BasicConfiguration;
// import com.trihydro.odewrapper.config.BasicConfiguration;
// import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;
import com.trihydro.library.model.TimType;
import com.trihydro.library.model.WydotOdeTravelerInformationMessage;
import com.trihydro.library.model.WydotTravelerInputData;
// import com.trihydro.odewrapper.model.WydotTim;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import us.dot.its.jpo.ode.plugin.SNMP;
import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW;
import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW.TimeToLive;
import us.dot.its.jpo.ode.plugin.j2735.OdeGeoRegion;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;

import com.google.gson.Gson;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.TimRsuService;
import com.trihydro.library.service.TimService;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.Milepost;
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

    public static void updateTimOnRsu(WydotTravelerInputData timToSend) {

        String timToSendJson = gson.toJson(timToSend);
        restTemplate.put(configuration.getOdeUrl() + "/tim", timToSendJson, String.class);
    }

    public static void updateTimOnSdw(WydotTravelerInputData timToSend) {
        String timToSendJson = gson.toJson(timToSend);

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

    public static OdeGeoRegion getServiceRegion(List<Milepost> mileposts) {

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
}