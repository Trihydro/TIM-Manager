package com.trihydro.odewrapper.helpers.util;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.grum.geocalc.Coordinate;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;
import com.trihydro.library.helpers.MilepostReduction;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ContentEnum;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.MilepostBuffer;
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
    private MilepostService milepostService;
    MilepostReduction milepostReduction;

    @Autowired
    public void InjectDependencies(Utility _utility, MilepostService _milepostService,
            MilepostReduction _milepostReduction) {
        utility = _utility;
        milepostService = _milepostService;
        milepostReduction = _milepostReduction;
    }

    public WydotTravelerInputData buildTim(WydotTim wydotTim, String direction, BasicConfiguration config,
            ContentEnum content) {

        // assume the given start/stop points are correct and send them on to calculate
        // mileposts
        List<Milepost> mileposts = new ArrayList<>();
        List<Milepost> milepostsAll = new ArrayList<>();
        if (wydotTim.getEndPoint() != null && wydotTim.getEndPoint().getLatitude() != null
                && wydotTim.getEndPoint().getLongitude() != null) {
            // make sure we carry through correct direction here
            wydotTim.setDirection(direction);
            milepostsAll = milepostService.getMilepostsByStartEndPointDirection(wydotTim);
        } else {
            // point incident
            MilepostBuffer mpb = new MilepostBuffer();
            mpb.setBufferMiles(config.getPointIncidentBufferMiles());
            mpb.setCommonName(wydotTim.getRoute());
            mpb.setDirection(direction);
            mpb.setPoint(wydotTim.getStartPoint());
            milepostsAll = milepostService.getMilepostsByPointWithBuffer(mpb);
        }

        // don't continue if we have no mileposts
        if (milepostsAll.size() == 0) {
            utility.logWithDate("Found 0 mileposts, unable to generate TIM");
            return null;
        }

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

        dataFrame.setContent(content.getStringValue());// "Advisory");
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

        // reduce the mileposts by removing straight away posts
        mileposts = milepostReduction.applyMilepostReductionAlorithm(milepostsAll, config.getPathDistanceLimit());
        timToSend.setMileposts(mileposts);

        OdePosition3D anchorPosition = new OdePosition3D();
        if (timToSend.getMileposts().size() > 0) {
            anchorPosition.setLatitude(timToSend.getMileposts().get(0).getLatitude());
            anchorPosition.setLongitude(timToSend.getMileposts().get(0).getLongitude());
        } else {
            anchorPosition.setLatitude(BigDecimal.valueOf(0));
            anchorPosition.setLongitude(BigDecimal.valueOf(0));
            anchorPosition.setElevation(BigDecimal.valueOf(0));
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

        int timDirection = 0;
        // path list - change later
        if (milepostsAll != null && milepostsAll.size() > 0) {
            double startLat = milepostsAll.get(0).getLatitude().doubleValue();
            double startLon = milepostsAll.get(0).getLongitude().doubleValue();
            for (int j = 1; j < milepostsAll.size(); j++) {
                double lat = milepostsAll.get(j).getLatitude().doubleValue();
                double lon = milepostsAll.get(j).getLongitude().doubleValue();

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
        if (mileposts != null && mileposts.size() > 0) {
            ArrayList<OdeTravelerInformationMessage.NodeXY> nodes = new ArrayList<OdeTravelerInformationMessage.NodeXY>();
            var startMp = mileposts.get(0);
            for (int i = 1; i < mileposts.size(); i++) {
                OdeTravelerInformationMessage.NodeXY node = new OdeTravelerInformationMessage.NodeXY();
                BigDecimal lat = mileposts.get(i).getLatitude().subtract(startMp.getLatitude());
                BigDecimal lon = mileposts.get(i).getLongitude().subtract(startMp.getLongitude());
                node.setNodeLat(lat);
                node.setNodeLong(lon);
                node.setDelta("node-LL");
                nodes.add(node);
                startMp = mileposts.get(i);
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