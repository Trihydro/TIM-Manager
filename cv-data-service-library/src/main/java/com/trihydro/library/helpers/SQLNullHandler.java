package com.trihydro.library.helpers;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.springframework.stereotype.Component;

@Component
public class SQLNullHandler {

	public void setLongOrNull(PreparedStatement ps, int column, Long value) throws SQLException {
		if (value != null)
			ps.setLong(column, value);
		else
			ps.setNull(column, java.sql.Types.NUMERIC);
	}

	public void setIntegerOrNull(PreparedStatement ps, int column, Integer value) throws SQLException {
		if (value != null)
			ps.setLong(column, value);
		else
			ps.setNull(column, java.sql.Types.NUMERIC);
	}

	public void setStringOrNull(PreparedStatement ps, int column, String value) throws SQLException {
		if (value != null)
			ps.setString(column, value);
		else
			ps.setNull(column, java.sql.Types.VARCHAR);
	}

	public void setIntegerFromBool(PreparedStatement ps, int column, boolean value) throws SQLException {
		int val = value ? 1 : 0;
		ps.setInt(column, val);
	}

	public void setBigDecimalOrNull(PreparedStatement ps, int column, BigDecimal value) throws SQLException {
		if (value != null)
			ps.setBigDecimal(column, value);
		else
			ps.setNull(column, java.sql.Types.NUMERIC);
	}

	public void setBigDecimalOrNull(PreparedStatement ps, int column, String value) throws SQLException {
		if (value != null && value != "") {
			try {
				BigDecimal bd = new BigDecimal(value);
				ps.setBigDecimal(column, bd);
			} catch (NumberFormatException ex) {
				ps.setNull(column, java.sql.Types.NUMERIC);
			}
		} else
			ps.setNull(column, java.sql.Types.NUMERIC);
	}

	public void setTimestampOrNull(PreparedStatement ps, int column, Timestamp value) throws SQLException {
		if (value != null)
			ps.setTimestamp(column, value);
		else
			ps.setNull(column, java.sql.Types.TIMESTAMP);
	}

	public void setShortOrNull(PreparedStatement ps, int column, Short value) throws SQLException {
		if (value != null)
			ps.setShort(column, value);
		else
			ps.setNull(column, java.sql.Types.NUMERIC);
	}

	public void setDoubleOrNull(PreparedStatement ps, int column, Double value) throws SQLException {
		if (value != null)
			ps.setDouble(column, value);
		else
			ps.setNull(column, java.sql.Types.NUMERIC);
	}

}