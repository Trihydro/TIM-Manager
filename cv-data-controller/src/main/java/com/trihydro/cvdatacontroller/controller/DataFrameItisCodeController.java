package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimDbTables;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;

@CrossOrigin
@RestController
@RequestMapping("data-frame-itis-code")
@ApiIgnore
public class DataFrameItisCodeController extends BaseController {
    private static final Logger LOG = LoggerFactory.getLogger(DataFrameItisCodeController.class);

    private TimDbTables timDbTables;
    private SQLNullHandler sqlNullHandler;

    @Autowired
    public void InjectDependencies(TimDbTables _timDbTables, SQLNullHandler _sqlNullHandler) {
        timDbTables = _timDbTables;
        sqlNullHandler = _sqlNullHandler;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/add-data-frame-itis-code/{dataFrameId}/{itis}")
    public ResponseEntity<Long> AddDataFrameItisCode(@PathVariable Long dataFrameId, @PathVariable String itis) {

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {

            String insertQueryStatement = timDbTables.buildInsertQueryStatement("data_frame_itis_code",
                    timDbTables.getDataFrameItisCodeTable());
            connection = dbInteractions.getConnectionPool();
            preparedStatement = connection.prepareStatement(insertQueryStatement,
                    new String[] { "data_frame_itis_code_id" });
            int fieldNum = 1;

            for (String col : timDbTables.getDataFrameItisCodeTable()) {
                if (col.equals("ITIS_CODE_ID")) {
                    if (StringUtils.isNumeric(itis))
                        sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, Long.parseLong(itis));
                    else
                        sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, null);
                } else if (col.equals("TEXT")) {
                    if (!StringUtils.isNumeric(itis))
                        sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, itis);
                    else
                        sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, null);
                } else if (col.equals("DATA_FRAME_ID")) {
                    sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, dataFrameId);
                } else if (col.equals("POSITION")) {
                    sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, null); // should this be null?
                }
                fieldNum++;
            }

            Long dataFrameItisCodeId = dbInteractions.executeAndLog(preparedStatement, "dataFrameItisCode");
            return ResponseEntity.ok(dataFrameItisCodeId);
        } catch (SQLException e) {
            LOG.error("Exception", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Long.valueOf(0));
        } finally {
            try {
                // close prepared statement
                if (preparedStatement != null)
                    preparedStatement.close();
                // return connection back to pool
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                LOG.error("Exception", e);
            }
        }
    }
}