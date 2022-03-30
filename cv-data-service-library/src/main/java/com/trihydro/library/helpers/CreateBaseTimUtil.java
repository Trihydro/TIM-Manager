package com.trihydro.library.helpers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.grum.geocalc.Coordinate;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;
import com.trihydro.library.model.ContentEnum;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.WydotTim;
import com.trihydro.library.model.WydotTravelerInputData;
import com.trihydro.library.service.TimGenerationProps;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.plugin.ServiceRequest;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.MsgId;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.RoadSignID;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.FrameType.TravelerInfoType;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.MutcdCode.MutcdCodeEnum;

@Component
public class CreateBaseTimUtil {

    private Utility utility;

    @Autowired
    public void InjectDependencies(Utility _utility) {
        utility = _utility;
    }

    public WydotTravelerInputData buildTim(WydotTim wydotTim, TimGenerationProps genProps, ContentEnum content,
            TravelerInfoType frameType, List<Milepost> allMileposts, List<Milepost> reducedMileposts, Milepost anchor) {

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

        dataFrame.setContent(content.getStringValue());
        dataFrame.setFrameType(frameType);
        dataFrame.setUrl("null");
        // add itis codes to tim
        dataFrame.setItems(wydotTim.getItisCodes().toArray(new String[wydotTim.getItisCodes().size()]));

        List<OdeTravelerInformationMessage.DataFrame.Region> regions = new ArrayList<OdeTravelerInformationMessage.DataFrame.Region>();
        OdeTravelerInformationMessage.DataFrame.Region region = new OdeTravelerInformationMessage.DataFrame.Region();
        region.setName("Temp");
        region.setRegulatorID(0);

        region.setLaneWidth(genProps.getDefaultLaneWidth());
        region.setDirectionality("3");
        region.setClosedPath(false);

        // path
        region.setDescription("path");
        OdeTravelerInformationMessage.DataFrame.Region.Path path = new OdeTravelerInformationMessage.DataFrame.Region.Path();
        path.setScale(0);
        path.setType("ll");

        OdePosition3D anchorPosition = new OdePosition3D();
        anchorPosition.setLatitude(anchor.getLatitude());
        anchorPosition.setLongitude(anchor.getLongitude());
        region.setAnchorPosition(anchorPosition);

        MsgId msgId = new MsgId();
        RoadSignID roadSignID = new RoadSignID();
        roadSignID.setPosition(anchorPosition);
        // if we are coming in with content=speedLimit and frameType=roadSignage,
        // we need to set the mutcdCode to regulatory to display the regulatory signage
        if (content == ContentEnum.speedLimit && frameType == TravelerInfoType.roadSignage) {
            roadSignID.setMutcdCode(MutcdCodeEnum.regulatory);
        } else {
            roadSignID.setMutcdCode(MutcdCodeEnum.warning);
        }
        roadSignID.setViewAngle("1111111111111111");
        msgId.setRoadSignID(roadSignID);
        dataFrame.setMsgId(msgId);

        int timDirection = 0;
        // path list - change later
        if (allMileposts != null && allMileposts.size() > 0) {
            double startLat = anchor.getLatitude().doubleValue();
            double startLon = anchor.getLongitude().doubleValue();
            for (int j = 0; j < allMileposts.size(); j++) {
                double lat = allMileposts.get(j).getLatitude().doubleValue();
                double lon = allMileposts.get(j).getLongitude().doubleValue();

                Point standPoint = Point.at(Coordinate.fromDegrees(startLat), Coordinate.fromDegrees(startLon));
                Point forePoint = Point.at(Coordinate.fromDegrees(lat), Coordinate.fromDegrees(lon));

                timDirection |= utility.getDirection(EarthCalc.bearing(standPoint, forePoint));
                // reset for next round
                startLat = lat;
                startLon = lon;
            }
        }

        // set direction based on bearings
        String dirTest = Integer.toBinaryString(timDirection);
        dirTest = StringUtils.repeat("0", 16 - dirTest.length()) + dirTest;
        dirTest = StringUtils.reverse(dirTest);
        region.setDirection(dirTest); // heading slice

        // set path nodes
        if (reducedMileposts != null && reducedMileposts.size() > 0) {
            ArrayList<OdeTravelerInformationMessage.NodeXY> nodes = new ArrayList<OdeTravelerInformationMessage.NodeXY>();
            var startMp = anchor;

            // Per J2735, NodeSetLL's must contain at least 2 nodes. ODE will fail to
            // PER-encode TIM if we supply less than 2. If we only have 1 node for the path,
            // include a node with an offset of (0, 0) which is effectively a point that's
            // right on top of the anchor point.
            if (reducedMileposts.size() == 1) {
                OdeTravelerInformationMessage.NodeXY node = new OdeTravelerInformationMessage.NodeXY();
                node.setNodeLat(BigDecimal.valueOf(0));
                node.setNodeLong(BigDecimal.valueOf(0));
                node.setDelta("node-LL");
                nodes.add(node);
            }

            for (int i = 0; i < reducedMileposts.size(); i++) {
                // note that even though we are setting node-LL type here, the ODE only has a
                // NodeXY object, as the structure is the same.
                OdeTravelerInformationMessage.NodeXY node = new OdeTravelerInformationMessage.NodeXY();
                BigDecimal lat = reducedMileposts.get(i).getLatitude().subtract(startMp.getLatitude());
                BigDecimal lon = reducedMileposts.get(i).getLongitude().subtract(startMp.getLongitude());
                node.setNodeLat(lat);
                node.setNodeLong(lon);
                node.setDelta("node-LL");
                nodes.add(node);
                startMp = reducedMileposts.get(i);
            }
            path.setNodes(nodes.toArray(new OdeTravelerInformationMessage.NodeXY[nodes.size()]));
            region.setPath(path);
        }

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