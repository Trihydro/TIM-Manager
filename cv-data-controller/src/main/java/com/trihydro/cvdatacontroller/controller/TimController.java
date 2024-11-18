package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.SecurityResultCodeType;
import com.trihydro.library.model.TimInsertModel;
import com.trihydro.library.model.WydotOdeTravelerInformationMessage;
import com.trihydro.library.tables.TimDbTables;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import us.dot.its.jpo.ode.model.OdeMsgMetadata;
import us.dot.its.jpo.ode.model.ReceivedMessageDetails;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;

@CrossOrigin
@RestController
public class TimController extends BaseController {

    private TimDbTables timDbTables;
    private SQLNullHandler sqlNullHandler;
    private SecurityResultCodeTypeController securityResultCodeTypeController;

    @Autowired
    public void InjectDependencies(TimDbTables _timDbTables, SQLNullHandler _sqlNullHandler,
            SecurityResultCodeTypeController _securityResultCodeTypeController) {
        timDbTables = _timDbTables;
        sqlNullHandler = _sqlNullHandler;
        securityResultCodeTypeController = _securityResultCodeTypeController;
    }

    @RequestMapping(value = "/get-tim/{timId}", method = RequestMethod.GET)
    public ResponseEntity<WydotOdeTravelerInformationMessage> GetTim(@PathVariable Long timId) {

        WydotOdeTravelerInformationMessage tim = new WydotOdeTravelerInformationMessage();

        Statement statement = null;
        ResultSet rs = null;
        Connection connection = null;

        try {
            // build SQL statement
            connection = dbInteractions.getConnectionPool();
            statement = connection.createStatement();
            rs = statement.executeQuery("select * from tim where tim_id = " + timId);

            // convert to DriverAlertType objects
            while (rs.next()) {
                tim.setPacketID(rs.getString("PACKET_ID"));
                tim.setMsgCnt(rs.getInt("MSG_CNT"));
                tim.setTimeStamp(rs.getString("TIME_STAMP"));
                tim.setUrlB(rs.getString("URL_B"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(tim);
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

        return ResponseEntity.ok(tim);
    }

    @RequestMapping(value = "/add-tim", method = RequestMethod.POST, headers = "Accept=application/json")
    public Long AddTim(@RequestBody TimInsertModel tim) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;

        try {

            String insertQueryStatement = timDbTables.buildInsertQueryStatement("tim",
                    timDbTables.getTimTable());
            connection = dbInteractions.getConnectionPool();
            preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "tim_id" });
            int fieldNum = 1;
            OdeTravelerInformationMessage j2735 = tim.getJ2735TravelerInformationMessage();
            OdeMsgMetadata odeTimMetadata = tim.getOdeTimMetadata();
            ReceivedMessageDetails receivedMessageDetails = tim.getReceivedMessageDetails();

            for (String col : timDbTables.getTimTable()) {
                // default to null
                preparedStatement.setString(fieldNum, null);
                if (j2735 != null) {
                    if (col.equals("MSG_CNT"))
                        sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, j2735.getMsgCnt());
                    else if (col.equals("PACKET_ID"))
                        sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, j2735.getPacketID());
                    else if (col.equals("URL_B"))
                        sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, j2735.getUrlB());
                    else if (col.equals("TIME_STAMP")) {
                        String timeStamp = j2735.getTimeStamp();
                        java.sql.Timestamp ts = null;
                        if (StringUtils.isNotEmpty(timeStamp) && StringUtils.isNotBlank(timeStamp)) {
                            ts = java.sql.Timestamp
                                    .valueOf(LocalDateTime.parse(timeStamp, DateTimeFormatter.ISO_DATE_TIME));
                        }
                        sqlNullHandler.setTimestampOrNull(preparedStatement, fieldNum, ts);
                    }
                }
                if (odeTimMetadata != null) {
                    if (col.equals("RECORD_GENERATED_BY")) {
                        if (odeTimMetadata.getRecordGeneratedBy() != null)
                            sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                                    odeTimMetadata.getRecordGeneratedBy().toString());
                        else
                            preparedStatement.setString(fieldNum, null);
                    } else if (col.equals("RECORD_GENERATED_AT")) {
                        if (odeTimMetadata.getRecordGeneratedAt() != null) {
                            java.util.Date recordGeneratedAtDate = utility.convertDate(odeTimMetadata.getRecordGeneratedAt());
                            sqlNullHandler.setTimestampOrNull(preparedStatement, fieldNum,
                                    new java.sql.Timestamp(recordGeneratedAtDate.getTime()));
                        } else {
                            preparedStatement.setTimestamp(fieldNum, null);
                        }
                    } else if (col.equals("SCHEMA_VERSION")) {
                        sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, odeTimMetadata.getSchemaVersion());
                    } else if (col.equals("SANITIZED")) {
                        if (odeTimMetadata.isSanitized())
                            preparedStatement.setInt(fieldNum, 1);
                        else
                            preparedStatement.setInt(fieldNum, 0);
                    } else if (col.equals("PAYLOAD_TYPE")) {
                        sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getPayloadType());
                    } else if (col.equals("ODE_RECEIVED_AT")) {
                        if (odeTimMetadata.getOdeReceivedAt() != null) {
                            java.util.Date receivedAtDate = utility.convertDate(odeTimMetadata.getOdeReceivedAt());
                            sqlNullHandler.setTimestampOrNull(preparedStatement, fieldNum,
                                    new java.sql.Timestamp(receivedAtDate.getTime()));
                        } else {
                            preparedStatement.setTimestamp(fieldNum, null);
                        }
                    }

                    if (odeTimMetadata.getSerialId() != null) {
                        if (col.equals("SERIAL_ID_STREAM_ID"))
                            sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                                    odeTimMetadata.getSerialId().getStreamId());
                        else if (col.equals("SERIAL_ID_BUNDLE_SIZE"))
                            sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum,
                                    odeTimMetadata.getSerialId().getBundleSize());
                        else if (col.equals("SERIAL_ID_BUNDLE_ID"))
                            sqlNullHandler.setLongOrNull(preparedStatement, fieldNum,
                                    odeTimMetadata.getSerialId().getBundleId());
                        else if (col.equals("SERIAL_ID_RECORD_ID"))
                            sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum,
                                    odeTimMetadata.getSerialId().getRecordId());
                        else if (col.equals("SERIAL_ID_SERIAL_NUMBER"))
                            sqlNullHandler.setLongOrNull(preparedStatement, fieldNum,
                                    odeTimMetadata.getSerialId().getSerialNumber());
                    }
                }
                if (receivedMessageDetails != null) {
                    if (receivedMessageDetails.getLocationData() != null) {
                        if (col.equals("RMD_LD_ELEVATION")) {
                            sqlNullHandler.setDoubleOrNull(preparedStatement, fieldNum,
                                    Double.parseDouble(receivedMessageDetails.getLocationData().getElevation()));
                        } else if (col.equals("RMD_LD_HEADING")) {
                            sqlNullHandler.setDoubleOrNull(preparedStatement, fieldNum,
                                    Double.parseDouble(receivedMessageDetails.getLocationData().getHeading()));
                        } else if (col.equals("RMD_LD_LATITUDE")) {
                            sqlNullHandler.setDoubleOrNull(preparedStatement, fieldNum,
                                    Double.parseDouble(receivedMessageDetails.getLocationData().getLatitude()));
                        } else if (col.equals("RMD_LD_LONGITUDE")) {
                            sqlNullHandler.setDoubleOrNull(preparedStatement, fieldNum,
                                    Double.parseDouble(receivedMessageDetails.getLocationData().getLongitude()));
                        } else if (col.equals("RMD_LD_SPEED")) {
                            sqlNullHandler.setDoubleOrNull(preparedStatement, fieldNum,
                                    Double.parseDouble(receivedMessageDetails.getLocationData().getSpeed()));
                        }
                    }
                    else {
                        // location data is null, set all to null (with correct type)
                        if (col.equals("RMD_LD_ELEVATION") || col.equals("RMD_LD_HEADING") || col.equals("RMD_LD_LATITUDE")
                                || col.equals("RMD_LD_LONGITUDE") || col.equals("RMD_LD_SPEED")) {
                            preparedStatement.setNull(fieldNum, java.sql.Types.NUMERIC);
                        }
                    }
                    if (col.equals("RMD_RX_SOURCE") && receivedMessageDetails.getRxSource() != null) {
                        sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                                receivedMessageDetails.getRxSource().toString());
                    } else if (col.equals("SECURITY_RESULT_CODE")) {
                        SecurityResultCodeType securityResultCodeType = securityResultCodeTypeController
                                .GetSecurityResultCodeTypes().getBody().stream().filter(x -> x
                                        .getSecurityResultCodeType().equals(tim.getSecurityResultCode().toString()))
                                .findFirst().orElse(null);
                        if (securityResultCodeType != null) {
                            preparedStatement.setInt(fieldNum, securityResultCodeType.getSecurityResultCodeTypeId());
                        }
                        else {
                            preparedStatement.setNull(fieldNum, java.sql.Types.INTEGER);
                        }
                    }
                }
                else {
                    // message details are null, set all to null (with correct type)
                    if (col.equals("RMD_LD_ELEVATION") || col.equals("RMD_LD_HEADING") || col.equals("RMD_LD_LATITUDE")
                            || col.equals("RMD_LD_LONGITUDE") || col.equals("RMD_LD_SPEED")) {
                        preparedStatement.setNull(fieldNum, java.sql.Types.NUMERIC);
                    } else if (col.equals("RMD_RX_SOURCE")) {
                        preparedStatement.setString(fieldNum, null);
                    } else if (col.equals("SECURITY_RESULT_CODE")) {
                        preparedStatement.setNull(fieldNum, java.sql.Types.INTEGER);
                    }
                }

                if (col.equals("SAT_RECORD_ID"))
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, tim.getSatRecordId());
                else if (col.equals("TIM_NAME"))
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, tim.getRegionName());
                else if (col.equals("LOG_FILE_NAME")) {
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, tim.getLogFileName());
                } else if (col.equals("RECORD_TYPE") && tim.getRecordType() != null) {
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, tim.getRecordType().toString());
                }
                fieldNum++;
            }
            // execute insert statement
            Long timId = dbInteractions.executeAndLog(preparedStatement, "timID");
            return timId;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                // close prepared statement
                if (preparedStatement != null)
                    preparedStatement.close();
                // return connection back to pool
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return Long.valueOf(0);
    }

    @RequestMapping(value = "/delete-old-tim", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<Boolean> deleteOldTim() {
        // delete all tim and related over 30 days old
        boolean deleteResult = true;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        Timestamp oneMonthPriorTimestamp = getOneMonthPriorTimestamp();

        try {
            // 1. delete old tim_rsu records
            deleteResult = deleteOldTimRsus(oneMonthPriorTimestamp);
            if (!deleteResult) {
                utility.logWithDate("Failed to cleanup old tim_rsu records");
                return ResponseEntity.ok(false);
            }

            // 2. delete old data_frame_itis_code records
            deleteResult &= deleteOldDataFrameItisCodes(oneMonthPriorTimestamp);
            if (!deleteResult) {
                utility.logWithDate("Failed to cleanup old data_frame_itis_code records");
                return ResponseEntity.ok(false);
            }

            // 3. delete old region records
            deleteResult &= deleteOldRegions(oneMonthPriorTimestamp);
            if (!deleteResult) {
                utility.logWithDate("Failed to cleanup old region records");
                return ResponseEntity.ok(false);
            }

            // 4. delete old path_node_ll records
            deleteResult &= deleteOldPathNodeLL(oneMonthPriorTimestamp);
            if (!deleteResult) {
                utility.logWithDate("Failed to cleanup old path_node_ll records");
                return ResponseEntity.ok(false);
            }

            // 5. delete old path records
            deleteResult &= deleteOldPaths(oneMonthPriorTimestamp);
            if (!deleteResult) {
                utility.logWithDate("Failed to cleanup old path records");
                return ResponseEntity.ok(false);
            }

            // 6. delete old node_ll records
            deleteResult &= deleteOldNodeLL(oneMonthPriorTimestamp);
            if (!deleteResult) {
                utility.logWithDate("Failed to cleanup old node_ll records");
                return ResponseEntity.ok(false);
            }

            // 7. delete old data_frame records
            deleteResult &= deleteOldDataFrames(oneMonthPriorTimestamp);
            if (!deleteResult) {
                utility.logWithDate("Failed to cleanup old data_frame records");
                return ResponseEntity.ok(false);
            }

            // 8. delete old tim records
            String deleteSQL = "DELETE FROM tim WHERE ode_received_at < ? and tim_id NOT IN (SELECT tim_id FROM active_tim)";
            connection = dbInteractions.getConnectionPool();
            preparedStatement = connection.prepareStatement(deleteSQL);
            preparedStatement.setTimestamp(1, oneMonthPriorTimestamp);

            // execute delete SQL statement
            deleteResult &= dbInteractions.deleteWithPossibleZero(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        } finally {
            try {
                // close prepared statement
                if (preparedStatement != null)
                    preparedStatement.close();
                // return connection back to pool
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return ResponseEntity.ok(deleteResult);
    }

    private boolean deleteOldDataFrames(Timestamp timestamp) {
        boolean deleteResult = false;
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            String deleteSQL = "DELETE FROM data_frame WHERE tim_id IN";
            deleteSQL += " (SELECT tim_id FROM tim WHERE ode_received_at < ? AND tim_id NOT IN (SELECT tim_id FROM active_tim))";
            connection = dbInteractions.getConnectionPool();
            preparedStatement = connection.prepareStatement(deleteSQL);
            preparedStatement.setTimestamp(1, timestamp);

            // execute delete SQL stetement
            deleteResult = dbInteractions.deleteWithPossibleZero(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                // close prepared statement
                if (preparedStatement != null)
                    preparedStatement.close();
                // return connection back to pool
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (!deleteResult) {
            utility.logWithDate("Failed to delete data_frame");
        }
        return deleteResult;
    }

    private boolean deleteOldDataFrameItisCodes(Timestamp timestamp) {
        boolean deleteResult = false;
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            String deleteSQL = "DELETE FROM DATA_FRAME_ITIS_CODE where data_frame_id in";
            deleteSQL += " (select data_frame_id from data_frame WHERE tim_id IN";
            deleteSQL += " (SELECT tim_id FROM tim WHERE ode_received_at < ? AND tim_id NOT IN (SELECT tim_id FROM active_tim)))";
            connection = dbInteractions.getConnectionPool();
            preparedStatement = connection.prepareStatement(deleteSQL);
            preparedStatement.setTimestamp(1, timestamp);

            // execute delete SQL stetement
            deleteResult = dbInteractions.deleteWithPossibleZero(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                // close prepared statement
                if (preparedStatement != null)
                    preparedStatement.close();
                // return connection back to pool
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (!deleteResult) {
            utility.logWithDate("Failed to delete DATA_FRAME_ITIS_CODE");
        }
        return deleteResult;
    }

    private boolean deleteOldRegions(Timestamp timestamp) {
        boolean deleteResult = false;
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            String deleteSQL = "DELETE FROM region where data_frame_id in";
            deleteSQL += " (select data_frame_id from data_frame WHERE tim_id IN";
            deleteSQL += " (SELECT tim_id FROM tim WHERE ode_received_at < ? AND tim_id NOT IN (SELECT tim_id FROM active_tim)))";
            connection = dbInteractions.getConnectionPool();
            preparedStatement = connection.prepareStatement(deleteSQL);
            preparedStatement.setTimestamp(1, timestamp);

            // execute delete SQL stetement
            deleteResult = dbInteractions.deleteWithPossibleZero(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                // close prepared statement
                if (preparedStatement != null)
                    preparedStatement.close();
                // return connection back to pool
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (!deleteResult) {
            utility.logWithDate("Failed to delete region");
        }
        return deleteResult;
    }

    private boolean deleteOldPaths(Timestamp timestamp) {
        boolean deleteResult = false;
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            String deleteSQL = "DELETE FROM path WHERE path_id in (SELECT path_id from region where data_frame_id in";
            deleteSQL += " (select data_frame_id from data_frame WHERE tim_id IN";
            deleteSQL += " (SELECT tim_id FROM tim WHERE ode_received_at < ? AND tim_id NOT IN (SELECT tim_id FROM active_tim))))";
            connection = dbInteractions.getConnectionPool();
            preparedStatement = connection.prepareStatement(deleteSQL);
            preparedStatement.setTimestamp(1, timestamp);

            // execute delete SQL stetement
            deleteResult = dbInteractions.deleteWithPossibleZero(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                // close prepared statement
                if (preparedStatement != null)
                    preparedStatement.close();
                // return connection back to pool
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (!deleteResult) {
            utility.logWithDate("Failed to delete path");
        }
        return deleteResult;
    }

    private boolean deleteOldPathNodeLL(Timestamp timestamp) {
        boolean deleteResult = false;
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            String deleteSQL = "DELETE FROM path_node_ll WHERE path_id in (SELECT path_id from region where data_frame_id in";
            deleteSQL += " (select data_frame_id from data_frame WHERE tim_id IN";
            deleteSQL += " (SELECT tim_id FROM tim WHERE ode_received_at < ? AND tim_id NOT IN (SELECT tim_id FROM active_tim))))";
            connection = dbInteractions.getConnectionPool();
            preparedStatement = connection.prepareStatement(deleteSQL);
            preparedStatement.setTimestamp(1, timestamp);

            // execute delete SQL stetement
            deleteResult = dbInteractions.deleteWithPossibleZero(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                // close prepared statement
                if (preparedStatement != null)
                    preparedStatement.close();
                // return connection back to pool
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (!deleteResult) {
            utility.logWithDate("Failed to delete path_node_ll");
        }
        return deleteResult;
    }

    private boolean deleteOldNodeLL(Timestamp timestamp) {
        boolean deleteResult = false;
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            String deleteSQL = "DELETE FROM node_ll WHERE node_ll_id IN";
            deleteSQL += " (SELECT node_ll_id from path_node_ll WHERE path_id in (SELECT path_id from region where data_frame_id in";
            deleteSQL += " (select data_frame_id from data_frame WHERE tim_id IN";
            deleteSQL += " (SELECT tim_id FROM tim WHERE ode_received_at < ? AND tim_id NOT IN (SELECT tim_id FROM active_tim)))))";
            connection = dbInteractions.getConnectionPool();
            preparedStatement = connection.prepareStatement(deleteSQL);
            preparedStatement.setTimestamp(1, timestamp);

            // execute delete SQL stetement
            deleteResult = dbInteractions.deleteWithPossibleZero(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                // close prepared statement
                if (preparedStatement != null)
                    preparedStatement.close();
                // return connection back to pool
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (!deleteResult) {
            utility.logWithDate("Failed to delete node_ll");
        }
        return deleteResult;
    }

    private boolean deleteOldTimRsus(Timestamp timestamp) {
        boolean deleteResult = false;
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            String deleteSQL = "DELETE FROM tim_rsu WHERE tim_id IN";
            deleteSQL += " (SELECT tim_id FROM tim WHERE ode_received_at < ? AND tim_id NOT IN (SELECT tim_id FROM active_tim))";
            connection = dbInteractions.getConnectionPool();
            preparedStatement = connection.prepareStatement(deleteSQL);
            preparedStatement.setTimestamp(1, timestamp);

            // execute delete SQL stetement
            deleteResult = dbInteractions.deleteWithPossibleZero(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                // close prepared statement
                if (preparedStatement != null)
                    preparedStatement.close();
                // return connection back to pool
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (!deleteResult) {
            utility.logWithDate("Failed to delete tim_rsus");
        }
        return deleteResult;
    }
}
