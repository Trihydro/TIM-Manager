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
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.NodeXY;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.FrameType.TravelerInfoType;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.MutcdCode.MutcdCodeEnum;

@Component
public class CreateBaseTimUtil {

    private Utility utility;

    @Autowired
    public void InjectDependencies(Utility _utility) {
        utility = _utility;
    }

    /**
     * Builds a WydotTravelerInputData object with the provided data.
     *
     * @param wydotTim The WydotTim object containing the data for the TIM.
     * @param genProps The TimGenerationProps object containing the generation properties.
     * @param content The ContentEnum object representing the content of the TIM.
     * @param frameType The TravelerInfoType object representing the frame type of the TIM.
     * @param allMileposts The list of Milepost objects representing all mileposts.
     * @param reducedMileposts The list of Milepost objects representing reduced mileposts.
     * @param anchor The Milepost object representing the anchor milepost.
     * @return The WydotTravelerInputData object containing the built TIM.
     */
    public WydotTravelerInputData buildTim(WydotTim wydotTim, TimGenerationProps genProps, ContentEnum content,
            TravelerInfoType frameType, List<Milepost> allMileposts, List<Milepost> reducedMileposts, Milepost anchor) {

        // build TIM object with data
        WydotTravelerInputData timToSend = new WydotTravelerInputData();
        OdeTravelerInformationMessage tim = new OdeTravelerInformationMessage();
        tim.setUrlB("null");

        // set TIM Properties
        OdeTravelerInformationMessage.DataFrame dataFrame = new OdeTravelerInformationMessage.DataFrame();
        dataFrame.setDoNotUse1((short) 0); // as of J2735 2020 this should be set to 0 and is ignored
        dataFrame.setDoNotUse2((short) 0); // as of J2735 2020 this should be set to 0 and is ignored
        dataFrame.setDoNotUse3((short) 0); // as of J2735 2020 this should be set to 0 and is ignored
        dataFrame.setDoNotUse4((short) 0); // as of J2735 2020 this should be set to 0 and is ignored

        // set TIM TimeStamp and StartDateTime to current time in UTC
        String nowAsISO = Instant.now().toString();
        tim.setTimeStamp(nowAsISO);
        dataFrame.setStartDateTime(nowAsISO);

        // duration time set to 22 days worth of minutes
        dataFrame.setDurationTime(32000);

        // default priority
        dataFrame.setPriority(5);

        dataFrame.setContent(content.getStringValue());
        dataFrame.setFrameType(frameType);
        dataFrame.setUrl("null");
        // add itis codes to tim
        dataFrame.setItems(wydotTim.getItisCodes().toArray(new String[wydotTim.getItisCodes().size()]));

        // create anchor for the msgId
        OdePosition3D anchorPosition = new OdePosition3D();
        anchorPosition.setLatitude(anchor.getLatitude());
        anchorPosition.setLongitude(anchor.getLongitude());
        
        // build msgId
        MsgId msgId = buildMsgId(anchorPosition, content, frameType);
        dataFrame.setMsgId(msgId);

        // set regions. note that we now support multiple regions in a single TIM package
        BigDecimal defaultLaneWidth = genProps.getDefaultLaneWidth();
        List<OdeTravelerInformationMessage.DataFrame.Region> regions = buildRegions(defaultLaneWidth, allMileposts, reducedMileposts, anchor);
        dataFrame.setRegions(regions.toArray(new OdeTravelerInformationMessage.DataFrame.Region[regions.size()]));

        // set dataframes, currently assuming a single dataframe
        OdeTravelerInformationMessage.DataFrame[] dataFrames = new OdeTravelerInformationMessage.DataFrame[1];
        dataFrames[0] = dataFrame;
        tim.setDataframes(dataFrames);

        timToSend.setTim(tim);
        timToSend.setRequest(new ServiceRequest());

        return timToSend;
    }

    /**
     * Builds a list of regions based on the given parameters.
     * If the number of reduced mileposts is less than or equal to 63, a single region is built.
     * If the number of reduced mileposts is greater than 63, multiple regions are built.
     *
     * @param defaultLaneWidth   the default lane width
     * @param allMileposts       a list of all mileposts
     * @param reducedMileposts   a list of reduced mileposts
     * @param anchor             the anchor milepost
     * @return a list of regions
     */
    protected List<OdeTravelerInformationMessage.DataFrame.Region> buildRegions(BigDecimal defaultLaneWidth, List<Milepost> allMileposts, List<Milepost> reducedMileposts, Milepost anchor) {
        if (reducedMileposts.size() <= 63) {
            utility.logWithDate("Less than 63 mileposts, building a single region.", CreateBaseTimUtil.class);
            List<OdeTravelerInformationMessage.DataFrame.Region> regions = new ArrayList<OdeTravelerInformationMessage.DataFrame.Region>();
            OdeTravelerInformationMessage.DataFrame.Region singleRegion = buildSingleRegion(defaultLaneWidth, allMileposts, reducedMileposts, anchor);
            regions.add(singleRegion);
            return regions;
        } else {
            utility.logWithDate("More than 63 mileposts, building multiple regions.", CreateBaseTimUtil.class);
            return buildMultipleRegions(defaultLaneWidth, allMileposts, reducedMileposts, anchor);
        }
    }

    /**
     * Builds multiple regions based on the given parameters.
     * 
     * @param defaultLaneWidth    the default lane width
     * @param allMileposts        a list of all mileposts
     * @param reducedMileposts    a list of reduced mileposts
     * @param anchor              the anchor milepost
     * @return                    a list of OdeTravelerInformationMessage.DataFrame.Region objects representing the built regions
     */
    protected List<OdeTravelerInformationMessage.DataFrame.Region> buildMultipleRegions(BigDecimal defaultLaneWidth, List<Milepost> allMileposts, List<Milepost> reducedMileposts, Milepost anchor) {
        List<OdeTravelerInformationMessage.DataFrame.Region> regions = new ArrayList<OdeTravelerInformationMessage.DataFrame.Region>(); 

        int maxMilepostsPerRegion = 63;

        List<Milepost> milepostsForNextRegion = new ArrayList<Milepost>();
        Milepost nextAnchor = new Milepost(anchor);

        for (int i = 0; i < reducedMileposts.size(); i++) {
            milepostsForNextRegion.add(reducedMileposts.get(i));
            // if we have reached the max number of mileposts per region, or if we are at the end of the list
            if (milepostsForNextRegion.size() == maxMilepostsPerRegion || i == reducedMileposts.size() - 1) {
                OdeTravelerInformationMessage.DataFrame.Region region = buildSingleRegion(defaultLaneWidth, allMileposts, milepostsForNextRegion, nextAnchor);
                regions.add(region);
                milepostsForNextRegion.clear();
                nextAnchor = reducedMileposts.get(i);
            }
        }

        utility.logWithDate("Built " + regions.size() + " regions.", CreateBaseTimUtil.class);
        return regions;
    }

    /**
     * Builds a single region.
     *
     * @param defaultLaneWidth    The default lane width for the region.
     * @param allMileposts        The list of all mileposts.
     * @param reducedMileposts    The list of reduced mileposts.
     * @param anchor              The anchor milepost.
     * @return                    The built region.
     */
    protected OdeTravelerInformationMessage.DataFrame.Region buildSingleRegion(BigDecimal defaultLaneWidth, List<Milepost> allMileposts, List<Milepost> reducedMileposts, Milepost anchor) {
        OdeTravelerInformationMessage.DataFrame.Region region = new OdeTravelerInformationMessage.DataFrame.Region();
        region.setName("Temp");
        region.setRegulatorID(0);

        // set lane width
        region.setLaneWidth(defaultLaneWidth);

        // set directionality
        region.setDirectionality("3");

        // set closed path
        region.setClosedPath(false);

        // set anchor position
        OdePosition3D anchorPosition = new OdePosition3D();
        anchorPosition.setLatitude(anchor.getLatitude());
        anchorPosition.setLongitude(anchor.getLongitude());
        region.setAnchorPosition(anchorPosition);

        // set description
        region.setDescription("path");

        // set direction
        String directionString = buildHeadingSliceFromMileposts(allMileposts, anchorPosition);
        region.setDirection(directionString); // heading slice

        // set path nodes
        if (reducedMileposts != null && reducedMileposts.size() > 0) {
            OdeTravelerInformationMessage.NodeXY[] nodes = buildNodePathFromMileposts(reducedMileposts, anchor);
            OdeTravelerInformationMessage.DataFrame.Region.Path path = new OdeTravelerInformationMessage.DataFrame.Region.Path();
            path.setScale(0);
            path.setType("ll");
            path.setNodes(nodes);
            region.setPath(path);
        }

        return region;
    }

    public NodeXY[] buildNodePathFromMileposts(List<Milepost> reducedMileposts, Milepost anchor) {
        ArrayList<OdeTravelerInformationMessage.NodeXY> nodes = new ArrayList<OdeTravelerInformationMessage.NodeXY>();

        var previousMp = anchor;

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
            BigDecimal lat = reducedMileposts.get(i).getLatitude().subtract(previousMp.getLatitude());
            BigDecimal lon = reducedMileposts.get(i).getLongitude().subtract(previousMp.getLongitude());
            node.setNodeLat(lat);
            node.setNodeLong(lon);
            node.setDelta("node-LL");
            nodes.add(node);
            previousMp = reducedMileposts.get(i);
        }
        return nodes.toArray(new OdeTravelerInformationMessage.NodeXY[nodes.size()]);
    }

    public String buildHeadingSliceFromMileposts(List<Milepost> allMileposts, OdePosition3D anchorPosition) {
        int timDirection = 0;
        // this is a regular tim, so we need to set the direction normally
        // path list - change later
        if (allMileposts != null && allMileposts.size() > 0) {
            double startLat = anchorPosition.getLatitude().doubleValue();
            double startLon = anchorPosition.getLongitude().doubleValue();
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
        String headingSliceString = Integer.toBinaryString(timDirection);
        headingSliceString = StringUtils.repeat("0", 16 - headingSliceString.length()) + headingSliceString;
        headingSliceString = StringUtils.reverse(headingSliceString);
        return headingSliceString;
    }

    /**
     * Builds a message ID based on the provided anchor position, content, and frame type.
     * 
     * @param anchorPosition The anchor position for the road sign.
     * @param content The content of the message.
     * @param frameType The type of the frame.
     * @return The built message ID.
     */
    protected MsgId buildMsgId(OdePosition3D anchorPosition, ContentEnum content, TravelerInfoType frameType) {
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
        // set view angle to 360 degrees
        roadSignID.setViewAngle("1111111111111111");
        msgId.setRoadSignID(roadSignID);
        return msgId;
    }
}