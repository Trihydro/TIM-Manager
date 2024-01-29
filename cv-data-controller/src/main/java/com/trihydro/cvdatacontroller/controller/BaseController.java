package com.trihydro.cvdatacontroller.controller;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;

import com.trihydro.library.helpers.DbInteractions;
import com.trihydro.library.helpers.Utility;

import org.springframework.beans.factory.annotation.Autowired;

import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
public class BaseController {
    protected Utility utility;
    protected DbInteractions dbInteractions;
    
    @Autowired
    public void InjectBaseDependencies(DbInteractions _dbInteractions, Utility _utility) {
        dbInteractions = _dbInteractions;
        utility = _utility;
    }

    public String getOneMonthPriorString() {
        DateFormat sdf = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a");
        TimeZone toTimeZone = TimeZone.getTimeZone("UTC");
        sdf.setTimeZone(toTimeZone);
        Date dte = java.sql.Date.valueOf(LocalDate.now().minus(1, ChronoUnit.MONTHS));
        String strDate = sdf.format(dte.getTime());
        return strDate;
    }

    public Timestamp getOneMonthPriorTimestamp() {
        Timestamp ts = Timestamp.valueOf(LocalDate.now().minus(1, ChronoUnit.MONTHS).atStartOfDay());
        return ts;
    }
}
