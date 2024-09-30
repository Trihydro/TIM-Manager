package com.trihydro.cvdatacontroller.controller;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.CountyRoadSegment;
import com.trihydro.library.model.CountyRoadsProps;
import com.trihydro.library.model.JCSCacheProps;
import com.trihydro.library.model.TriggerRoad;
import com.trihydro.library.views.CountyRoadsGeometryView;
import com.trihydro.library.views.CountyRoadsReportView;
import com.trihydro.library.views.CountyRoadsWtiSectionsView;

import springfox.documentation.annotations.ApiIgnore;

@CrossOrigin
@RestController
@ApiIgnore
@RequestMapping("cascade")
public class CascadeController extends BaseController {
    private Utility utility;
    private JCSCacheProps jcsCacheProps;
    private CountyRoadsProps countyRoadsProps;
    private TriggerRoadCache triggerRoadCache;

    @Autowired
    public void InjectBaseDependencies(Utility _utility, JCSCacheProps _jcsCacheProps, CountyRoadsProps _countyRoadsProps) {
        utility = _utility;
        jcsCacheProps = _jcsCacheProps;
        countyRoadsProps = _countyRoadsProps;
        triggerRoadCache = new TriggerRoadCache(utility, jcsCacheProps);
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
    public ResponseEntity<String> getTriggerRoad(@PathVariable String roadCode) {
        TriggerRoad triggerRoad = null;
        boolean cached = false;

        List<Integer> countyRoadIds = triggerRoadCache.getSegmentIdsAssociatedWithTriggerRoad(roadCode);
        if (countyRoadIds != null) {
            cached = true;
            if (countyRoadIds.size() == 0) {
                // avoid hitting the database if we know there are no segments associated with this road code
                String json = new TriggerRoad(roadCode).toJson();
                return new ResponseEntity<String>(json, HttpStatus.OK);
            }
        }

        try {
            triggerRoad = retrieveTriggerRoadFromDatabase(roadCode);
            if (!cached) {
                addToCache(triggerRoad);
            }
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        String json = triggerRoad.toJson();
        return new ResponseEntity<String>(json, HttpStatus.OK);
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
            connection = dbInteractions.getCountyRoadsConnectionPool(); // target county roads database
            statement = connection.createStatement();

            // build SQL statement
            String viewName = countyRoadsProps.getCountyRoadsGeometryViewName();
            String query = String.format("select %s, %s, %s, %s, %s from %s where %s = %d order by %s asc",
                    CountyRoadsGeometryView.commonNameColumnName,
                    CountyRoadsGeometryView.directionColumnName,
                    CountyRoadsGeometryView.milepostColumnName,
                    CountyRoadsGeometryView.longitudeColumnName,
                    CountyRoadsGeometryView.latitudeColumnName,
                    viewName,
                    "cr_id",
                    countyRoadId,
                    CountyRoadsGeometryView.milepostColumnName);
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
     * Retrieve all active TIMs that are associated with the given segment
     * @param segmentId the segment id
     * @return the list of active TIMs (empty if no records found)
     */
    @RequestMapping(value = "/get-active-tims-with-itis-codes-for-segment/{segmentId}", method = RequestMethod.GET, headers = "Accept=application/json")
    public ResponseEntity<List<ActiveTim>> getActiveTimsWithItisCodesForSegment(@PathVariable int segmentId) {
        List<ActiveTim> activeTims = retrieveActiveTimsWithItisCodesForSegmentFromDatabase(segmentId);
        if (activeTims == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return new ResponseEntity<List<ActiveTim>>(activeTims, HttpStatus.OK);
    }

    /**
     * Retrieve all active TIMs that are associated with the given segment from the database that are not marked for deletion
     * @param segmentId the segment id
     * @return the list of active TIMs (empty if no records found)
     * @throws SQLException if there is an error retrieving the active TIMs
     */
    private List<ActiveTim> retrieveActiveTimsWithItisCodesForSegmentFromDatabase(int segmentId) {
        List<ActiveTim> results = new ArrayList<ActiveTim>();
		ActiveTim activeTim = null;
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = dbInteractions.getConnectionPool(); // target primary database
			statement = connection.createStatement();

			String query = "select active_tim.*, tim_type.type, itis_code.itis_code from active_tim";
			query += " left join tim_type on active_tim.tim_type_id = tim_type.tim_type_id";
			query += " left join data_frame on active_tim.tim_id = data_frame.tim_id";
			query += " left join data_frame_itis_code on data_frame.data_frame_id = data_frame_itis_code.data_frame_id";
			query += " left join itis_code on data_frame_itis_code.itis_code_id = itis_code.itis_code_id";
            query += " where client_id like '%_trgd_" + segmentId + "%'"; // segmentId is part of the client_id
            query += " and marked_for_deletion = '0'";
			query += " order by active_tim.active_tim_id, data_frame_itis_code.position asc";

			rs = statement.executeQuery(query);

			// convert to ActiveTim object
			while (rs.next()) {
				Long activeTimId = rs.getLong("ACTIVE_TIM_ID");

				// If we're looking at the first record or the record doesn't have
				// the same ACTIVE_TIM_ID as the record we just processed...
				if (activeTim == null || !activeTim.getActiveTimId().equals(activeTimId)) {
					if (activeTim != null) {
						results.add(activeTim);
					}

					// Create a new record and set the ActiveTim properties.
					activeTim = new ActiveTim();
					activeTim.setActiveTimId(activeTimId);
					activeTim.setTimId(rs.getLong("TIM_ID"));
					activeTim.setDirection(rs.getString("DIRECTION"));
					activeTim.setStartDateTime(rs.getString("TIM_START"));
					activeTim.setEndDateTime(rs.getString("TIM_END"));
					activeTim.setExpirationDateTime(rs.getString("EXPIRATION_DATE"));
					activeTim.setRoute(rs.getString("ROUTE"));
					activeTim.setClientId(rs.getString("CLIENT_ID"));
					activeTim.setSatRecordId(rs.getString("SAT_RECORD_ID"));
					activeTim.setPk(rs.getInt("PK"));
					activeTim.setItisCodes(new ArrayList<Integer>());

					Coordinate startPoint = null;
					Coordinate endPoint = null;

					// Set startPoint
					BigDecimal startLat = rs.getBigDecimal("START_LATITUDE");
					BigDecimal startLon = rs.getBigDecimal("START_LONGITUDE");
					if (!rs.wasNull()) {
						startPoint = new Coordinate(startLat, startLon);
					}
					activeTim.setStartPoint(startPoint);

					// Set endPoint
					BigDecimal endLat = rs.getBigDecimal("END_LATITUDE");
					BigDecimal endLon = rs.getBigDecimal("END_LONGITUDE");
					if (!rs.wasNull()) {
						endPoint = new Coordinate(endLat, endLon);
					}
					activeTim.setEndPoint(endPoint);

					// Set timType
					long timTypeId = rs.getLong("TIM_TYPE_ID");
					if (!rs.wasNull()) {
						activeTim.setTimTypeId(timTypeId);
						activeTim.setTimType(rs.getString("TYPE"));
					}

					// Set projectKey
					int projectKey = rs.getInt("PROJECT_KEY");
					if (!rs.wasNull()) {
						activeTim.setProjectKey(projectKey);
					}
				}

				// Add the ITIS code to the ActiveTim's ITIS codes, if not null
				var itisCode = rs.getInt("ITIS_CODE");
				if (!rs.wasNull()) {
					activeTim.getItisCodes().add(itisCode);
				}
			}

			if (activeTim != null) {
				results.add(activeTim);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
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

		return results;
    }

    private TriggerRoad retrieveTriggerRoadFromDatabase(String roadCode) throws SQLException {
        TriggerRoad triggerRoad = null;

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            connection = dbInteractions.getCountyRoadsConnectionPool(); // target county roads database
            statement = connection.createStatement();

            // build SQL statement
            String countyRoadsReportViewName = countyRoadsProps.getCountyRoadsReportViewName();
            String countyRoadsWtiSectionsViewName = countyRoadsProps.getCountyRoadsWtiSectionsViewName();
            String query = "select * from " + countyRoadsReportViewName + " where " + CountyRoadsReportView.crIdColumnName + " in (select " + CountyRoadsWtiSectionsView.crIdColumnName + " from " + countyRoadsWtiSectionsViewName + " where " + CountyRoadsWtiSectionsView.roadCodeColumnName + "='" + roadCode + "');";
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
        } catch (SQLException e) {
            throw e;
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
        return triggerRoad;
    }

    /**
     * Build a county road segment from the given result set
     * @param rs the result set
     * @return the county road segment
     * @throws SQLException if there is an error reading the result set
     * @throws RecordNotFoundException if no record found
     */
    private CountyRoadSegment buildCountyRoadSegment(ResultSet rs) throws SQLException, RecordNotFoundException {
        int countyRoadId = rs.getInt(CountyRoadsReportView.crIdColumnName);
        String name = rs.getString(CountyRoadsReportView.nameColumnName);

        if (name == null) {
            throw new RecordNotFoundException("No record found");
        }

        Double mFrom = rs.getDouble(CountyRoadsReportView.mFromColumnName);
        Double mTo = rs.getDouble(CountyRoadsReportView.mToColumnName);
        Double xFrom = rs.getDouble(CountyRoadsReportView.xFromColumnName);
        Double yFrom = rs.getDouble(CountyRoadsReportView.yFromColumnName);
        Double xTo = rs.getDouble(CountyRoadsReportView.xToColumnName);
        Double yTo = rs.getDouble(CountyRoadsReportView.yToColumnName);

        // identify if closed
        boolean triggered_closed = (rs.getInt(CountyRoadsReportView.triggeredClosedColumnName) == 0) ? false : true;
        boolean planned_closure = (rs.getInt(CountyRoadsReportView.plannedClosureColumnName) == 0) ? false : true;
        boolean adhoc_closed = (rs.getInt(CountyRoadsReportView.adhocClosedColumnName) == 0) ? false : true;
        boolean isCountyRoadClosed = triggered_closed || planned_closure || adhoc_closed;

        // identify if c2lhpv
        boolean triggered_c2lhpv = (rs.getInt(CountyRoadsReportView.triggeredC2lhpvColumnName) == 0) ? false : true;
        boolean adhoc_c2lhpv = (rs.getInt(CountyRoadsReportView.adhocC2lhpvColumnName) == 0) ? false : true;
        boolean isC2lhpv = triggered_c2lhpv || adhoc_c2lhpv;

        // identify if loct
        boolean triggered_loct = (rs.getInt(CountyRoadsReportView.triggeredLoctColumnName) == 0) ? false : true;
        boolean planned_loct = (rs.getInt(CountyRoadsReportView.plannedLoctColumnName) == 0) ? false : true;
        boolean loct = (rs.getInt(CountyRoadsReportView.loctColumnName) == 0) ? false : true; // not sure if this differs from triggered_loct
        boolean isLoct = triggered_loct || planned_loct || loct;

        // identify if ntt
        boolean triggered_ntt = (rs.getInt(CountyRoadsReportView.triggeredNttColumnName) == 0) ? false : true;
        boolean adhoc_ntt = (rs.getInt(CountyRoadsReportView.adhocNttColumnName) == 0) ? false : true;
        boolean isNtt = triggered_ntt || adhoc_ntt;

        return new CountyRoadSegment(countyRoadId, name, mFrom, mTo, xFrom, yFrom, xTo, yTo, isCountyRoadClosed, isC2lhpv, isLoct, isNtt);
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