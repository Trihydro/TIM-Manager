package com.trihydro.cvdatacontroller.controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;

import com.trihydro.cvdatacontroller.services.DbInteractions;
import com.trihydro.library.helpers.Utility;

import org.springframework.beans.factory.annotation.Autowired;

import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
public class BaseController {
    protected Utility utility;
    protected DbInteractions dbInteractions;

    private DateFormat utcFormatMilliSec = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private DateFormat utcFormatSec = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private DateFormat utcFormatMin = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    protected DateFormat mstFormat = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a");

    @Autowired
    public void InjectBaseDependencies(DbInteractions _dbInteractions, Utility _utility) {
        dbInteractions = _dbInteractions;
        utility = _utility;
    }

    public Date convertDate(String incomingDate) {

        Date convertedDate = null;

        try {
            if (incomingDate != null) {
                if (incomingDate.contains("."))
                    convertedDate = utcFormatMilliSec.parse(incomingDate);
                else if (incomingDate.length() == 22)
                    convertedDate = utcFormatMin.parse(incomingDate);
                else
                    convertedDate = utcFormatSec.parse(incomingDate);
            }
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        return convertedDate;
    }

    public String getOneMonthPrior() {
        DateFormat sdf = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a");
        TimeZone toTimeZone = TimeZone.getTimeZone("MST");
        sdf.setTimeZone(toTimeZone);
        Date dte = java.sql.Date.valueOf(LocalDate.now().minus(1, ChronoUnit.MONTHS));
        String strDate = sdf.format(dte.getTime());
        return strDate;
    }
}
