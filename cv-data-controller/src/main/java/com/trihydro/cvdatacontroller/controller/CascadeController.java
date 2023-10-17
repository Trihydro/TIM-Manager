package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.trihydro.cvdatacontroller.model.Milepost;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.helpers.caches.TriggerRoadCache;
import com.trihydro.library.model.CountyRoadSegment;
import com.trihydro.library.model.TriggerRoad;
import com.trihydro.library.views.CountyRoadsGeometryView;
import com.trihydro.library.views.CountyRoadsTriggerView;

import springfox.documentation.annotations.ApiIgnore;

@CrossOrigin
@RestController
@ApiIgnore
@RequestMapping("cascade")
public class CascadeController extends BaseController {
    private Utility utility;
    private TriggerRoadCache triggerRoadCache;

    public CascadeController() {
        this(new Utility());
    }

    public CascadeController(Utility _utility) {
        super();
        utility = _utility;
        triggerRoadCache = new TriggerRoadCache(utility);
    }

    /**
     * Add the given trigger road to the cache. Used in getTriggerRoad() and tests.
     * @param triggerRoad the trigger road to add
     */
    public void addToCache(TriggerRoad triggerRoad) {
        triggerRoadCache.updateCache(triggerRoad.getRoadCode(), triggerRoad);
    }

    /**
     * Clear the trigger road cache. Used in tests.
     */
    public void clearCache() {
        triggerRoadCache.clear();
    }

    /**
     * Get the trigger road for the given road code
     * @param roadCode the road code
     * @return the trigger road (county road segments list will be empty if no records found)
     */
    @RequestMapping(value = "/trigger-road/{roadCode}", method = RequestMethod.GET, headers = "Accept=application/json")
    public ResponseEntity<TriggerRoad> getTriggerRoad(@PathVariable String roadCode) {
        TriggerRoad triggerRoad = triggerRoadCache.getTriggerRoad(roadCode);
        if (triggerRoad == null) {
            Connection connection = null;
            Statement statement = null;
            ResultSet rs = null;

            try {
                connection = dbInteractions.getConnectionPool();
                statement = connection.createStatement();

                // build SQL statement
                String viewName = CountyRoadsTriggerView.countyRoadsTriggerViewName;
                String query = "select * from " + viewName + " where road_code = '" + roadCode + "'";
                rs = statement.executeQuery(query);

                List<CountyRoadSegment> countyRoadSegments = new ArrayList<CountyRoadSegment>();
                while (rs.next()) {
                    CountyRoadSegment countyRoadSegment;
                    try {
                        countyRoadSegment = buildCountyRoadSegment(rs);
                    } catch (RecordNotFoundException e) {
                        continue;
                    }
                    countyRoadSegments.add(countyRoadSegment);
                }
                triggerRoad = new TriggerRoad(roadCode, countyRoadSegments);
                addToCache(triggerRoad);
            } catch (SQLException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(triggerRoad);
            } finally {
                try {
                    // close prepared statement
                    if (statement != null)
                        statement.close();
                    // return connection back to pool
                    if (connection != null)
                        connection.close();
                    // close result set
                    if (rs != null)
                        rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return new ResponseEntity<TriggerRoad>(triggerRoad, HttpStatus.OK);
    }

    /**
     * Get the mileposts for the given county road
     * @param countyRoadId the county road id
     * @return the list of mileposts (empty if no records found)
     */
    @RequestMapping(value = "/mileposts/{countyRoadId}", method = RequestMethod.GET, headers = "Accept=application/json")
    public ResponseEntity<List<Milepost>> getMileposts(@PathVariable int countyRoadId) {
        List<Milepost> mileposts = new ArrayList<Milepost>();
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            connection = dbInteractions.getConnectionPool();
            statement = connection.createStatement();

            // build SQL statement
            String viewName = CountyRoadsGeometryView.countyRoadsGeometryViewName;
            String query = "select * from " + viewName + " where cr_id = " + countyRoadId;
            rs = statement.executeQuery(query);

            while (rs.next()) {
                Milepost milepostObj;
                try {
                    milepostObj = buildMilepost(rs);
                } catch (RecordNotFoundException e) {
                    continue;
                }
                mileposts.add(milepostObj);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mileposts);
        } finally {
            try {
                // close prepared statement
                if (statement != null)
                    statement.close();
                // return connection back to pool
                if (connection != null)
                    connection.close();
                // close result set
                if (rs != null)
                    rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return new ResponseEntity<List<Milepost>>(mileposts, HttpStatus.OK);
    }

    /**
     * Build a county road segment from the given result set
     * @param rs the result set
     * @return the county road segment
     * @throws SQLException if there is an error reading the result set
     * @throws RecordNotFoundException if no record found
     */
    private CountyRoadSegment buildCountyRoadSegment(ResultSet rs) throws SQLException, RecordNotFoundException {
        int countyRoadId = rs.getInt(CountyRoadsTriggerView.countyRoadIdColumnName);
        String commonName = rs.getString(CountyRoadsTriggerView.commonNameColumnName);

        if (commonName == null) {
            throw new RecordNotFoundException("No record found");
        }

        Double mFrom = rs.getDouble(CountyRoadsTriggerView.mFromColumnName);
        Double mTo = rs.getDouble(CountyRoadsTriggerView.mToColumnName);
        Double xFrom = rs.getDouble(CountyRoadsTriggerView.xFromColumnName);
        Double yFrom = rs.getDouble(CountyRoadsTriggerView.yFromColumnName);
        Double xTo = rs.getDouble(CountyRoadsTriggerView.xToColumnName);
        Double yTo = rs.getDouble(CountyRoadsTriggerView.yToColumnName);
        boolean closed = (rs.getInt(CountyRoadsTriggerView.closedColumnName) == 0) ? false : true;
        boolean c2lhpv = (rs.getInt(CountyRoadsTriggerView.c2lhpvColumnName) == 0) ? false : true;
        boolean loct = (rs.getInt(CountyRoadsTriggerView.loctColumnName) == 0) ? false : true;
        boolean ntt = (rs.getInt(CountyRoadsTriggerView.nttColumnName) == 0) ? false : true;
        return new CountyRoadSegment(countyRoadId, commonName, mFrom, mTo, xFrom, yFrom, xTo, yTo, closed, c2lhpv, loct, ntt);
    }

    /**
     * Build a milepost from the given result set
     * @param rs the result set
     * @return the milepost
     * @throws SQLException if there is an error reading the result set
     * @throws RecordNotFoundException if no record found
     */
    private Milepost buildMilepost(ResultSet rs) throws SQLException, RecordNotFoundException {
        String commonName = rs.getString(CountyRoadsGeometryView.commonNameColumnName);

        if (commonName == null) {
            throw new RecordNotFoundException("No record found");
        }

        String direction = rs.getString(CountyRoadsGeometryView.directionColumnName);
        Double milepost = rs.getDouble(CountyRoadsGeometryView.milepostColumnName);
        Double longitude = rs.getDouble(CountyRoadsGeometryView.longitudeColumnName);
        Double latitude = rs.getDouble(CountyRoadsGeometryView.latitudeColumnName);

        Milepost milepostObj = new Milepost();
        milepostObj.setCommonName(commonName);
        milepostObj.setDirection(direction);
        milepostObj.setMilepost(milepost);
        milepostObj.setLongitude(longitude);
        milepostObj.setLatitude(latitude);
        return milepostObj;
    }

    public class RecordNotFoundException extends Exception {
        public RecordNotFoundException(String errorMessage) {
            super(errorMessage);
        }
    }
}