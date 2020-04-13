package com.trihydro.timrefresh.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import com.google.gson.Gson;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.WydotOdeTravelerInformationMessage;
import com.trihydro.library.model.WydotTravelerInputData;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.timrefresh.config.TimRefreshConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW;
import us.dot.its.jpo.ode.plugin.j2735.OdeGeoRegion;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;

@Component
public class WydotTimService {

    protected static TimRefreshConfiguration configuration;

    @Autowired
    public void setConfiguration(TimRefreshConfiguration configurationRhs, RestTemplateProvider _restTemplateProvider) {
        configuration = configurationRhs;
        restTemplate = _restTemplateProvider.GetRestTemplate();
    }

    public static RestTemplate restTemplate;
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

    public static void sendNewTimToSdw(WydotTravelerInputData timToSend, String recordId, List<Milepost> mps) {

        // set msgCnt to 1 and create new packetId
        timToSend.getTim().setMsgCnt(1);

        SDW sdw = new SDW();

        // calculate service region
        sdw.setServiceRegion(getServiceRegion(mps));

        // set time to live
        sdw.setTtl(configuration.getSdwTtl());
        // set new record id
        sdw.setRecordId(recordId);

        // set sdw block in TIM
        timToSend.getRequest().setSdw(sdw);

        // send to ODE
        String timToSendJson = gson.toJson(timToSend);

        try {
            restTemplate.postForObject(configuration.getOdeUrl() + "/tim", timToSendJson, String.class);
            System.out.println("Successfully sent POST to ODE to send new TIM: " + timToSendJson);
        } catch (RuntimeException targetException) {
            System.out.println("Failed to POST new SDX TIM: " + timToSendJson);
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

    public static OdeGeoRegion getServiceRegion(List<Milepost> mileposts) {

        Comparator<Milepost> compLat = (l1, l2) -> Double.compare(l1.getLatitude(), l2.getLatitude());
        Comparator<Milepost> compLong = (l1, l2) -> Double.compare(l1.getLongitude(), l2.getLongitude());
        OdeGeoRegion serviceRegion = new OdeGeoRegion();

        if (mileposts.size() > 0) {

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

            serviceRegion.setNwCorner(nwCorner);
            serviceRegion.setSeCorner(seCorner);
        } else {
            System.out.println("getServiceRegion fails due to no mileposts");
        }
        return serviceRegion;
    }
}