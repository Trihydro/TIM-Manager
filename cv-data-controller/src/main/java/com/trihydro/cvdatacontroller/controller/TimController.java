package com.trihydro.cvdatacontroller.controller;

import org.springframework.web.bind.annotation.RestController;
// import com.trihydro.timCreator.model.WydotTravelerInputData;
// import com.trihydro.timCreator.model.WydotDataFrame;
// import com.trihydro.timCreator.dao.J2735TravelerInformationMessageService;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
// import com.trihydro.timCreator.dao.DataFrameService;
// import com.trihydro.timCreator.dao.TravelerInputDataService;
// import com.trihydro.timCreator.dao.RegionService;
// import com.trihydro.timCreator.dao.PathService;
// import com.trihydro.timCreator.dao.NodeXYService;
// import com.trihydro.timCreator.dao.DataFrameItisCodeService;
// import com.trihydro.timCreator.dao.PathNodeXYService;
// import com.trihydro.timCreator.dao.ComputedLaneService;
// import com.trihydro.timCreator.dao.LocalNodeService;
// import com.trihydro.timCreator.dao.DisabledListService;
// import com.trihydro.timCreator.dao.EnabledListService;
// import com.trihydro.timCreator.dao.DataListService;
// import com.trihydro.timCreator.dao.SpeedLimitService;
// import com.trihydro.timCreator.dao.OldRegionService;
// import com.trihydro.timCreator.dao.ShapePointService;
// import com.trihydro.timCreator.dao.RegionListService;
// import com.trihydro.timCreator.dao.ShapePointNodeXYService;
// import com.trihydro.timCreator.dao.MilepostService;
// import com.trihydro.timCreator.dao.RSUService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.net.URI;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import springfox.documentation.annotations.ApiIgnore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;

@CrossOrigin
@RestController
@ApiIgnore
public class TimController {

    // services
    // private final J2735TravelerInformationMessageService j2735TravelerInformationMessageService;
    // private final DataFrameService dataFrameService;
    // private final TravelerInputDataService travelerInputDataService;    
    // private final EnabledListService enabledListService;
    // private final DataListService dataListService; 
    // private final RegionService regionService;
    // private final DataFrameItisCodeService dataFrameItisCodeService;
    // private final PathService pathService;
    // private final NodeXYService nodeXYService;
    // private final PathNodeXYService pathNodeXYService;
    // private final ComputedLaneService computedLaneService;
    // private final LocalNodeService localNodeService;
    // private final DisabledListService disabledListService;
    // private final SpeedLimitService speedLimitService;
    // private final OldRegionService oldRegionService;
    // private final ShapePointService shapePointService;
    // private final RegionListService regionListService;
    // private final ShapePointNodeXYService shapePointNodeXYService;
    // private final MilepostService milepostService;
    // private final RSUService rsuService;

    // @Autowired
    // TIMController(ShapePointNodeXYService shapePointNodeXYService, RegionListService regionListService, ShapePointService shapePointService, OldRegionService oldRegionService, SpeedLimitService speedLimitService, DisabledListService disabledListService, LocalNodeService localNodeService, ComputedLaneService computedLaneService, PathNodeXYService pathNodeXYService, NodeXYService nodeXYService, PathService pathService, DataFrameItisCodeService dataFrameItisCodeService, RegionService regionService, EnabledListService enabledListService, DataListService dataListService, J2735TravelerInformationMessageService j2735TravelerInformationMessageService, DataFrameService dataFrameService, TravelerInputDataService travelerInputDataService, MilepostService milepostService, RSUService rsuService) {
    //     this.enabledListService = enabledListService;
    //     this.dataListService = dataListService;
    //     this.j2735TravelerInformationMessageService = j2735TravelerInformationMessageService;
    //     this.dataFrameService = dataFrameService;
    //     this.travelerInputDataService = travelerInputDataService;
    //     this.regionService = regionService;
    //     this.dataFrameItisCodeService = dataFrameItisCodeService;
    //     this.pathService = pathService;
    //     this.nodeXYService = nodeXYService;
    //     this.pathNodeXYService = pathNodeXYService;        
    //     this.computedLaneService = computedLaneService;
    //     this.localNodeService = localNodeService;
    //     this.disabledListService = disabledListService;
    //     this.speedLimitService = speedLimitService;
    //     this.oldRegionService = oldRegionService;
    //     this.shapePointService = shapePointService;
    //     this.regionListService = regionListService;
    //     this.shapePointNodeXYService = shapePointNodeXYService;
    //     this.milepostService = milepostService;
    //     this.rsuService = rsuService;
    // }
    
    // @RequestMapping(value="/sendTimToDB", method = RequestMethod.POST, headers="Accept=application/json")
    // public ResponseEntity<?> sendTimToDB(@RequestBody WydotTravelerInputData travelerInputData) { 
    //     // insert tim	
    //     Long timId = j2735TravelerInformationMessageService.insertTIM(travelerInputData.getTim());
    //     System.out.println("tim id: " + timId);

    //     Long dataFrameId;
    //     Long pathId = null;
    //     Long nodeXYId = null;
    //     Long computedLaneId = null;
    //     Long oldRegionId = null;
    //     Long shapePointId = null;        
        
    //     // for each data frames 
    //     for(int i = 0; i < travelerInputData.getTim().getDataframes().length; i++) {
    //         // insert data frame  
    //         dataFrameId = dataFrameService.insertDataFrame(((WydotDataFrame)travelerInputData.getTim().getDataframes()[i]), timId); 
    //         // if there is at least one region
    //         if(travelerInputData.getTim().getDataframes()[i].getRegions() != null){
    //         	// for each region region
	//             for(J2735TravelerInformationMessage.DataFrame.Region region : travelerInputData.getTim().getDataframes()[i].getRegions()){
	//                 // if region has path
	//                 if(region.getPath() != null){ 
	//                 	if(region.getPath().getComputedLane() != null){
	//                 		computedLaneId = computedLaneService.insertComputedLane(region.getPath().getComputedLane());
	//                 	}
	//                     // insert path
	//                     pathId = pathService.insertPath(region.getPath(), computedLaneId);               
	//                     // for each node in path
	//                     if(region.getPath().getNodes() != null){
	// 	                    for(J2735TravelerInformationMessage.NodeXY node : region.getPath().getNodes()){
	// 	                        nodeXYId = nodeXYService.insertNodeXY(node);
	// 	                        pathNodeXYService.insertPathNodeXY(pathId, nodeXYId);
	// 	                        // attributes
	// 	                        if(node.getAttributes() != null){
	// 	                        	addNodeAttributes(node, nodeXYId);	                        	                                            
	// 	                        }
	// 	                    }   
	//                     }
	//                 }
	//                 // if region has old region
	//                 if(region.getOldRegion() != null){
	//                 	if(region.getOldRegion().getShapepoint() != null){
	//                 		if(region.getOldRegion().getShapepoint().getComputedLane() != null){
	//                 			computedLaneId = computedLaneService.insertComputedLane(region.getOldRegion().getShapepoint().getComputedLane());
	//                 		}
	//                 		shapePointId = shapePointService.insertShapePoint(region.getOldRegion().getShapepoint(), computedLaneId);
	//                 		if(region.getOldRegion().getShapepoint().getNodexy() != null){
	//                 			for(J2735TravelerInformationMessage.NodeXY node: region.getOldRegion().getShapepoint().getNodexy()){
	//                 				nodeXYId = nodeXYService.insertNodeXY(node);
	//                 				shapePointNodeXYService.insertShapePointNodeXY(shapePointId, nodeXYId);
	//                 				// attributes
	// 		                        if(node.getAttributes() != null){
	// 		                        	addNodeAttributes(node, nodeXYId);	                        	                                            
	// 		                        }
	//                 			}
	//                 		}	                		
	//                 	}
	//                 	oldRegionId = oldRegionService.insertOldRegion(region.getOldRegion(), shapePointId);
	//                 	if(region.getOldRegion().getRegionPoint().getRegionList() != null){
	//                 		for(J2735TravelerInformationMessage.DataFrame.Region.OldRegion.RegionPoint.RegionList regionList: region.getOldRegion().getRegionPoint().getRegionList()){
	//                 			regionListService.insertRegionList(regionList, oldRegionId);
	//                 		}
	//                 	}	               
	//                 }
	//                 // insert region
	//                 regionService.insertRegion(region, dataFrameId, oldRegionId, pathId);    
	//             }
    //         }
    //         dataFrameItisCodeService.insertDataFrameItisCode(dataFrameId, ((WydotDataFrame)travelerInputData.getTim().getDataframes()[i])); 
    //     }

    //     // insert tim rsus
    //     Long newTimId = travelerInputDataService.insertTIMRSUs(travelerInputData, timId);    

    //     URI location = ServletUriComponentsBuilder
    //     .fromCurrentRequest().path("/{id}")
    //     .buildAndExpand(newTimId)
    //     .toUri();

    //     return ResponseEntity.created(location).build();   		
    // }
    
    // protected void addNodeAttributes(J2735TravelerInformationMessage.NodeXY node, Long nodeXYId){    
    // 	Long dataListId = null;
    	
    // 	// local nodes;
    // 	if(node.getAttributes().getLocalNodes() != null){
    //     	for(J2735TravelerInformationMessage.LocalNode localNode : node.getAttributes().getLocalNodes()){
    //     		localNodeService.insertLocalNode(localNode, nodeXYId);
    //     	}
    // 	}
    // 	// disabled lists
    // 	if(node.getAttributes().getDisabledLists() != null){
    //     	for(J2735TravelerInformationMessage.DisabledList disabledList : node.getAttributes().getDisabledLists()){
    //     		disabledListService.insertDisabledList(disabledList, nodeXYId);
    //     	}
    // 	}		                        
    // 	// enabled lists
    // 	if(node.getAttributes().getEnabledLists() != null){
    //     	for(J2735TravelerInformationMessage.EnabledList enabledList : node.getAttributes().getEnabledLists()){
    //     		enabledListService.insertEnabledList(enabledList, nodeXYId);
    //     	}
    // 	}	
    // 	// data lists
    // 	if(node.getAttributes().getDataLists() != null){
    //     	for(J2735TravelerInformationMessage.DataList dataList : node.getAttributes().getDataLists()){
    //     		dataListId = dataListService.insertDataList(dataList, nodeXYId);
    //     		// speed limits
    //     		if(dataList.getSpeedLimits() != null){
    //     			for(J2735TravelerInformationMessage.SpeedLimits speedLimit : dataList.getSpeedLimits()){
    //     				speedLimitService.insertSpeedLimit(speedLimit, dataListId);
    //     			}			                        	
    //     		}
    //     	}
    // 	}
    // }
}
