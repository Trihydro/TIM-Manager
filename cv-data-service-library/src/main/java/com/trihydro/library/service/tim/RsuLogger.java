package com.trihydro.library.service.tim;

import java.sql.Connection;
import us.dot.its.jpo.ode.model.OdeTimMetadata;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.trihydro.library.service.CvDataLoggerLibrary;
import com.trihydro.library.service.helpers.SQLNullHandler;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;
import com.trihydro.library.service.tables.TimOracleTables;

public class RsuLogger extends CvDataLoggerLibrary {

	static PreparedStatement preparedStatement = null;

	

}

