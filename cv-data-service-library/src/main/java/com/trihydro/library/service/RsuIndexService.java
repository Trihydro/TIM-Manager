package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.RsuIndex;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.tables.TimOracleTables;

public class RsuIndexService extends CvDataServiceLibrary {

    public static Long insertRsuIndex(Integer rsuId, int index) {

        PreparedStatement preparedStatement = null;
        Connection connection = null;

        try {

            connection = DbUtility.getConnectionPool();
            String insertQueryStatement = TimOracleTables.buildInsertQueryStatement("rsu_index",
                    TimOracleTables.getRsuIndexTable());
            preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "rsu_index_id" });
            int fieldNum = 1;

            for (String col : TimOracleTables.getRsuIndexTable()) {
                if (col.equals("RSU_ID"))
                    SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, rsuId);
                else if (col.equals("RSU_INDEX"))
                    SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, index);
                fieldNum++;
            }
            // execute insert statement
            Long rsuIndexId = log(preparedStatement, "rsuindexid");
            return rsuIndexId;

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
        return new Long(0);
    }

    public static List<RsuIndex> selectByRsuId(int rsuId) {
        List<RsuIndex> rsuIndicies = new ArrayList<RsuIndex>();
        Connection connection = DbUtility.getConnectionPool();
        try (Statement statement = connection.createStatement()) {
            // select all Itis Codes from ItisCode table
            ResultSet rs = statement.executeQuery("select * from rsu_index where rsu_id = " + rsuId);
            try {
                // convert to ItisCode objects
                while (rs.next()) {
                    RsuIndex rsuIndex = new RsuIndex();
                    rsuIndex.setRsuIndexId(rs.getLong("rsu_index_id"));
                    rsuIndex.setRsuId(rs.getInt("rsu_id"));
                    rsuIndex.setRsuIndex(rs.getInt("rsu_index"));
                    rsuIndicies.add(rsuIndex);
                }
            } finally {
                try {
                    rs.close();
                } catch (Exception ignore) {
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rsuIndicies;
    }

    public static boolean deleteRsuIndex(Integer rsuId, Integer rsuIndex) {

        boolean deleteRsuIndexResult = false;

        String deleteSQL = "DELETE FROM RSU_INDEX WHERE RSU_ID = ? and RSU_INDEX = ?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {

            connection = DbUtility.getConnectionPool();
            preparedStatement = connection.prepareStatement(deleteSQL);
            preparedStatement.setInt(1, rsuId);
            preparedStatement.setInt(2, rsuIndex);

            // execute delete SQL stetement
            deleteRsuIndexResult = updateOrDelete(preparedStatement);

            System.out.println("RSU Index is deleted!");

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

        return deleteRsuIndexResult;
    }

}
