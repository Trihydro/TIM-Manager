package com.trihydro.library.service;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.model.SecurityResultCodeType;
import com.trihydro.library.tables.BsmOracleTables;

public class CvDataServiceLibrary {

    public static DateFormat utcFormatThree;
    public static DateFormat utcFormatTwo;
    public static DateFormat mstFormat;
    public static DateTimeFormatter localDateTimeformatter;
    public static DateFormat mstLocalFormat;
    public static PreparedStatement bsmPreparedStatement;
    public static PreparedStatement bsmSuvePreparedStatement;
    public static PreparedStatement bsmVsePreparedStatement;
    public static List<SecurityResultCodeType> securityResultCodeTypes;
    public static Statement statement; 

    static {
        utcFormatThree = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z[UTC]'");
        utcFormatTwo = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z[UTC]'");
        //mstFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");	
        mstFormat = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a"); 
        mstLocalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS-07:00");   
         
        try {
			statement = DbUtility.getConnection().createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        securityResultCodeTypes = SecurityResultCodeTypeService.getSecurityResultCodeTypes(DbUtility.getConnection());        

        String bsmCoreInsertQueryStatement = BsmOracleTables.buildInsertQueryStatement("bsm_core_data", BsmOracleTables.getBsmCoreDataTable());        
                try {
			bsmPreparedStatement = DbUtility.getConnection().prepareStatement(bsmCoreInsertQueryStatement, new String[] { "bsm_core_data_id" });
		} catch (SQLException e) {
			e.printStackTrace();
		}
        
        String bsmSuveInsertQueryStatement = BsmOracleTables.buildInsertQueryStatement("bsm_part2_suve", BsmOracleTables.getBsmPart2SuveTable());        
        try{
            bsmSuvePreparedStatement = DbUtility.getConnection().prepareStatement(bsmSuveInsertQueryStatement, new String[] {"bsm_part2_suve_id"});
        } catch (SQLException e) {
			e.printStackTrace();
        }

        String bsmVseInsertQueryStatement = BsmOracleTables.buildInsertQueryStatement("bsm_part2_vse", BsmOracleTables.getBsmPart2VseTable());
        try{
            bsmVsePreparedStatement = DbUtility.getConnection().prepareStatement(bsmVseInsertQueryStatement, new String[] {"bsm_part2_vse_id"});
        } catch (SQLException e) {
			e.printStackTrace();
        }        

    }

    public static Long log(PreparedStatement preparedStatement, String type) {       
        Long id = null;		
        try {
			if(preparedStatement.executeUpdate() > 0) {			
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            try{
                if(generatedKeys != null && generatedKeys.next()){
                    id = generatedKeys.getLong(1);
                    System.out.println("------ Generated " + type + " " + id + " --------------");
                }
            }				
            finally {
                try {
                    generatedKeys.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }					
            }
        }
		    } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
	   return id;
    }

    public static boolean updateOrDelete(PreparedStatement preparedStatement) {       
    
        boolean result = false;
       
        try {
			if(preparedStatement.executeUpdate() > 0) {			
            result = true;
        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				                    
		      
	   return result;
    }

    public static Date convertDate(String incomingDate){
        
        Date convertedDate = null;
        
        if(incomingDate != null){
            if(incomingDate.contains(".")){
                try {
                    convertedDate = utcFormatThree.parse(incomingDate);
                } catch (ParseException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
            else {
                try {
                    convertedDate = utcFormatTwo.parse(incomingDate);
                } 
                catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }		           										
        }
        return convertedDate;
    }
}
    