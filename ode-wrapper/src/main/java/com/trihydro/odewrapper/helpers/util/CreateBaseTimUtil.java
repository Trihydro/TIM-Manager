package com.trihydro.odewrapper.helpers.util;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
    public void InjectDependencies(Utility _utility, MilepostService _milepostService, MilepostReduction _milepostReduction) {
        utility = _utility;
        milepostService = _milepostService;
        milepostReduction=_milepostReduction;
    }

    public WydotTravelerInputData buildTim(WydotTim wydotTim, String direction, BasicConfiguration config,
            ContentEnum content) {

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

        // assume the given start/stop points are correct and send them on to calculate
        // mileposts
        List<Milepost> mileposts = new ArrayList<>();
        List<Milepost> milepostsAll = new ArrayList<>();
        if (wydotTim.getEndPoint() != null) {
            milepostsAll = milepostService.getMilepostsByStartEndPointDirection(wydotTim);
        } else {
            // point incident
            MilepostBuffer mpb = new MilepostBuffer();
            mpb.setBufferMiles(config.getPointIncidentBufferMiles());
            mpb.setCommonName(wydotTim.getRoute());
            mpb.setDirection(wydotTim.getDirection());
            mpb.setPoint(wydotTim.getStartPoint());
            milepostsAll = milepostService.getMilepostsByPointWithBuffer(mpb);
        }
        // reduce the mileposts by removing straight away posts
        mileposts = milepostReduction.applyMilepostReductionAlorithm(milepostsAll, config.getPathDistanceLimit());
        timToSend.setMileposts(mileposts);

        OdePosition3D anchorPosition = new OdePosition3D();
        if (timToSend.getMileposts().size() > 0) {
            anchorPosition.setLatitude(new BigDecimal(timToSend.getMileposts().get(0).getLatitude()));
            anchorPosition.setLongitude(new BigDecimal(timToSend.getMileposts().get(0).getLongitude()));
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
        if (milepostsAll != null && milepostsAll.size() > 0) {
            double startLat = milepostsAll.get(0).getLatitude();
            double startLon = milepostsAll.get(0).getLongitude();
            for (int j = 1; j < milepostsAll.size(); j++) {
                OdeTravelerInformationMessage.NodeXY node = new OdeTravelerInformationMessage.NodeXY();
                double lat = milepostsAll.get(j).getLatitude();
                double lon = milepostsAll.get(j).getLongitude();
                node.setNodeLat(new BigDecimal(lat));
                node.setNodeLong(new BigDecimal(lon));
                node.setDelta("node-LatLon");
                nodes.add(node);

                timDirection |= utility.getDirection(calculateBearing(startLat, startLon, lat, lon));
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

    private double calculateBearing(double startLat, double startLon, double destLat, double destLon) {
        // these calculations must be done in radians, and we are given degrees
        double lonDiff_rad = Math.toRadians(destLon - startLon);
        double startLat_rad = Math.toRadians(startLat);
        double destLon_rad = Math.toRadians(destLon);
        double destLat_rad = Math.toRadians(destLat);

        double y = Math.sin(lonDiff_rad) * Math.cos(destLon_rad);
        double x = Math.cos(startLat_rad) * Math.sin(destLat_rad)
                - Math.sin(startLat_rad) * Math.cos(destLat_rad) * Math.cos(lonDiff_rad);

        // gives -180 to 180
        double brng = Math.toDegrees(Math.atan2(y, x));

        // normalize to compass degrees
        double norm_brng = (brng + 360) % 360;
        return norm_brng;
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