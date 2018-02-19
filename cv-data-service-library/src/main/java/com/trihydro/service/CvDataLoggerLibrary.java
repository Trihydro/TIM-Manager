package com.trihydro.service;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class CvDataLoggerLibrary {

    public static DateFormat utcFormatThree;
    public static DateFormat utcFormatTwo;
    public static DateFormat mstFormat;
    public static DateTimeFormatter localDateTimeformatter;

    static {
        utcFormatThree = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z[UTC]'");
        utcFormatTwo = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z[UTC]'");
        //mstFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");	
        mstFormat = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a"); 
        localDateTimeformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
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
    