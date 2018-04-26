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

    public static DateFormat utcFormatMilliSec;
    public static DateFormat utcFormatSec;
    public static DateFormat utcFormatMin;
    public static DateFormat mstFormat;
    public static DateTimeFormatter localDateTimeformatter;
    public static DateFormat mstLocalFormat;
   
    //public static PreparedStatement bsmPreparedStatement;
    //public static PreparedStatement bsmSuvePreparedStatement;
    //public static PreparedStatement bsmVsePreparedStatement;
    public static List<SecurityResultCodeType> securityResultCodeTypes;

    static {
        utcFormatMilliSec = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z[UTC]'");
        utcFormatSec = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z[UTC]'"); //25
        utcFormatMin = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z[UTC]'");
        //mstFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");	
        mstFormat = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a"); 
        mstLocalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS-07:00");   
                 
        //securityResultCodeTypes = SecurityResultCodeTypeService.getSecurityResultCodeTypes(DbUtility.getConnection());        


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
        
        try{        
            if(incomingDate != null){
                if(incomingDate.contains("."))               
                    convertedDate = utcFormatMilliSec.parse(incomingDate);                                   
                else if(incomingDate.length() == 22)
                    convertedDate = utcFormatMin.parse(incomingDate);                
                else                   
                    convertedDate = utcFormatSec.parse(incomingDate);                                              	           									
            }
        }
        catch (ParseException e1) {
            e1.printStackTrace();
        }
        return convertedDate;
    }
}
    