package com.trihydro.odewrapper.helpers.util;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.WydotTim;
import com.trihydro.library.model.WydotTravelerInputData;
import com.trihydro.library.service.MilepostService;
import com.trihydro.odewrapper.config.BasicConfiguration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.plugin.ServiceRequest;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.MsgId;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.RoadSignID;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.MutcdCode.MutcdCodeEnum;

@Component
public class CreateBaseTimUtil {

    private Utility utility;

    @Autowired
    public void InjectDependencies(Utility _utility) {
        utility = _utility;
    }

    public WydotTravelerInputData buildTim(WydotTim wydotTim, String direction, String route,
            BasicConfiguration config) {

        // build TIM object with data
        WydotTravelerInputData timToSend = new WydotTravelerInputData();
        OdeTravelerInformationMessage tim = new OdeTravelerInformationMessage();
        tim.setUrlB("null");

        // set TIM Properties
        OdeTravelerInformationMessage.DataFrame dataFrame = new OdeTravelerInformationMessage.DataFrame();
        dataFrame.setSspTimRights((short) 1);
        dataFrame.setSspLocationRights((short) 1);
        dataFrame.setSspMsgContent((short) 1);
        dataFrame.setSspMsgTypes((short) 1);

        // set TIM TimeStamp and StartDateTime to current time in UTC
        String nowAsISO = Instant.now().toString();
        tim.setTimeStamp(nowAsISO);
        dataFrame.setStartDateTime(nowAsISO);

        // duration time set to 22 days worth of minutes
        dataFrame.setDurationTime(32000);

        dataFrame.setPriority(5);
        dataFrame.setContent("Advisory");
        dataFrame.setFrameType(us.dot.its.jpo.ode.plugin.j2735.timstorage.FrameType.TravelerInfoType.advisory);
        dataFrame.setUrl("null");

        List<OdeTravelerInformationMessage.DataFrame.Region> regions = new ArrayList<OdeTravelerInformationMessage.DataFrame.Region>();
        OdeTravelerInformationMessage.DataFrame.Region region = new OdeTravelerInformationMessage.DataFrame.Region();
        region.setName("Temp");
        region.setRegulatorID(0);

        region.setLaneWidth(config.getDefaultLaneWidth());// new BigDecimal(327));
        region.setDirectionality("3");
        region.setClosedPath(false);

        // path
        region.setDescription("path");
        OdeTravelerInformationMessage.DataFrame.Region.Path path = new OdeTravelerInformationMessage.DataFrame.Region.Path();
        path.setScale(0);
        path.setType("xy");

        double startingMP = 0, endingMP = 0;
        if (direction.toLowerCase().equals("d")) {
            startingMP = Math.max(wydotTim.getFromRm(), wydotTim.getToRm());
            endingMP = Math.min(wydotTim.getFromRm(), wydotTim.getToRm());
            startingMP = (Math.ceil(startingMP * 10) / 10) + .1;
            endingMP = (Math.floor(endingMP * 10) / 10);
        } else if (direction.toLowerCase().equals("i")) {
            startingMP = Math.min(wydotTim.getFromRm(), wydotTim.getToRm());
            endingMP = Math.max(wydotTim.getFromRm(), wydotTim.getToRm());
            startingMP = (Math.floor(startingMP * 10) / 10) - .1;
            endingMP = (Math.ceil(endingMP * 10) / 10);
        }

        timToSend.setMileposts(MilepostService.selectMilepostRange(direction, route, startingMP, endingMP));

        List<Milepost> sizeRestrictedMilepostList = timToSend.getMileposts();

        int mod = 2;

        List<Milepost> tempList = new ArrayList<Milepost>();

        tempList = sizeRestrictedMilepostList;

        while (tempList.size() > 60) {

            tempList = new ArrayList<Milepost>();
            tempList.add(sizeRestrictedMilepostList.get(0));
            tempList.add(sizeRestrictedMilepostList.get(1));

            for (int i = 2; i < sizeRestrictedMilepostList.size() - 1; i++) {

                if (Math.round(sizeRestrictedMilepostList.get(i).getMilepost() * 10 % mod) == 0) {
                    tempList.add(sizeRestrictedMilepostList.get(i));
                }
            }

            tempList.add(sizeRestrictedMilepostList.get(sizeRestrictedMilepostList.size() - 1));
            // sizeRestrictedMilepostList = tempList;
            mod += 2;
        }

        timToSend.setMileposts(tempList);

        OdePosition3D anchorPosition = new OdePosition3D();
        if (timToSend.getMileposts().size() > 0) {
            anchorPosition.setLatitude(new BigDecimal(timToSend.getMileposts().get(0).getLatitude()));
            anchorPosition.setLongitude(new BigDecimal(timToSend.getMileposts().get(0).getLongitude()));
            // anchorPosition.setElevation(new
            // BigDecimal(timToSend.getMileposts().get(0).getElevation() * 0.3048));
        } else {
            anchorPosition.setLatitude(new BigDecimal(0));
            anchorPosition.setLongitude(new BigDecimal(0));
            anchorPosition.setElevation(new BigDecimal(0));
        }

        MsgId msgId = new MsgId();
        RoadSignID roadSignID = new RoadSignID();
        OdePosition3D position = new OdePosition3D();
        position.setLatitude(anchorPosition.getLatitude());
        position.setLongitude(anchorPosition.getLongitude());
        roadSignID.setPosition(position);
        roadSignID.setMutcdCode(MutcdCodeEnum.warning);
        roadSignID.setViewAngle("1111111111111111");
        msgId.setRoadSignID(roadSignID);
        dataFrame.setMsgId(msgId);

        region.setAnchorPosition(anchorPosition);

        ArrayList<OdeTravelerInformationMessage.NodeXY> nodes = new ArrayList<OdeTravelerInformationMessage.NodeXY>();

        int timDirection = 0;
        // path list - change later
        for (int j = 1; j < timToSend.getMileposts().size(); j++) {
            OdeTravelerInformationMessage.NodeXY node = new OdeTravelerInformationMessage.NodeXY();
            node.setNodeLat(new BigDecimal(timToSend.getMileposts().get(j).getLatitude()));
            node.setNodeLong(new BigDecimal(timToSend.getMileposts().get(j).getLongitude()));
            node.setDelta("node-LatLon");
            nodes.add(node);
            timDirection |= utility.getDirection(timToSend.getMileposts().get(j).getBearing());
        }

        // set direction based on bearings
        String dirTest = Integer.toBinaryString(timDirection);
        dirTest = StringUtils.repeat("0", 16 - dirTest.length()) + dirTest;
        dirTest = StringUtils.reverse(dirTest);
        region.setDirection(dirTest); // heading slice

        // set path nodes
        path.setNodes(nodes.toArray(new OdeTravelerInformationMessage.NodeXY[nodes.size()]));
        region.setPath(path);

        regions.add(region);
        dataFrame.setRegions(regions.toArray(new OdeTravelerInformationMessage.DataFrame.Region[regions.size()]));

        OdeTravelerInformationMessage.DataFrame[] dataFrames = new OdeTravelerInformationMessage.DataFrame[1];
        dataFrames[0] = dataFrame;
        tim.setDataframes(dataFrames);

        timToSend.setTim(tim);
        timToSend.setRequest(new ServiceRequest());

        return timToSend;
    }

    protected String getDelta(Double distance) {
        if (distance >= -.0002048 && distance < .0002048)
            return "node-LL1";
        else if (distance >= -.0008192 && distance < .0008192)
            return "node-LL2";
        else if (distance >= -.0032768 && distance < .0032768)
            return "node-LL3";
        else if (distance >= -.0131072 && distance < .0131072)
            return "node-LL4";
        else if (distance >= -.2097152 && distance < .2097152)
            return "node-LL5";
        else
            return "node-LL6";
    }
}