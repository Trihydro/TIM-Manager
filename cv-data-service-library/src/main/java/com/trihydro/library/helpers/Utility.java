package com.trihydro.library.helpers;

import static java.lang.Math.toIntExact;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.service.RsuService;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.springframework.stereotype.Component;

@Component
public class Utility {
	public Gson gson = new Gson();

	public <T> void logWithDate(String msg, Class<T> clazz) {
		logWithDate(clazz.getSimpleName() + ": " + msg);
	}

	public void logWithDate(String msg) {
		Date date = new Date();
		System.out.println(date + " " + msg);
	}

	public int getMinutesDurationBetweenTwoDates(String startDateTime, String endDateTime) {

		int duration = getMinutesDurationWithSimpleDateFormat(startDateTime, endDateTime);
		if (duration == -1) {
			duration = getMinutesDurationWithZonedDateTime(startDateTime, endDateTime);
		}
		if (duration == -1) {
			System.out.println(
					"Failed to parse dates when getting minutes between: " + startDateTime + " and " + endDateTime);

		}
		return duration;
	}

	/**
	 * Attempt to get duration in minutes between two dates parsed as ZonedDateTime.
	 * If this fails, return -1
	 * 
	 * @param startDateTime
	 * @param endDateTime
	 * @return The duration in minutes between the two given dates. If parsing
	 *         fails, returns -1
	 */
	private int getMinutesDurationWithZonedDateTime(String startDateTime, String endDateTime) {
		try {
			ZonedDateTime zdtStart = ZonedDateTime.parse(startDateTime);
			ZonedDateTime zdtEnd = ZonedDateTime.parse(endDateTime);

			java.time.Duration dateDuration = java.time.Duration.between(zdtStart, zdtEnd);
			long durationTime = Math.abs(dateDuration.toMinutes());

			return toIntExact(durationTime);
		} catch (DateTimeParseException exception) {
			return -1;
		}
	}

	/**
	 * Attempt to get duration in minutes between two dates parsed in
	 * SimpleDateFormat("dd-MMM-yy HH.MM.SS"). If parsing fails, returns -1
	 * 
	 * @param startDateTime
	 * @param endDateTime
	 * @return The duration in minutes between the two given dates. If parsing
	 *         fails, returns -1
	 */
	private int getMinutesDurationWithSimpleDateFormat(String startDateTime, String endDateTime) {
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yy HH.MM.SS");
			Date startDate = simpleDateFormat.parse(startDateTime);
			Date endDate = simpleDateFormat.parse(endDateTime);

			long duration = (endDate.getTime() - startDate.getTime()) / 60000; // milliseconds to minutes is 1/60000
			return toIntExact(duration);
		} catch (Exception ex) {
			return -1;
		}
	}

	/**
	 * Returns the value presented by the ResultSet at key if available. If the
	 * value presented is null, defaults to 1 (rather than short default of 0). Used
	 * for our purposes to default to 1 for various values.
	 * 
	 * @param rs  The ResultSet to pull data from
	 * @param key The key value to use to get data from the ResultSet
	 * @return Short value defaulted to 1 if not found
	 */
	public short GetShortValueFromResultSet(ResultSet rs, String key) {
		try {
			String value = rs.getString(key);
			if (value != null) {
				return new Short(value);
			}
		} catch (SQLException ex) {
			System.out.println("Error attempting to get short value '" + key + "' from ResultSet");
		}
		return (short) 1;
	}

	public int getDirection(Double bearing) {

		int direction = 0;

		if (bearing >= 0 && bearing <= 22.5)
			direction = 1;
		else if (bearing > 22.5 && bearing <= 45)
			direction = 2;
		else if (bearing > 45 && bearing <= 67.5)
			direction = 4;
		else if (bearing > 67.5 && bearing <= 90)
			direction = 8;
		else if (bearing > 90 && bearing <= 112.5)
			direction = 16;
		else if (bearing > 112.5 && bearing <= 135)
			direction = 32;
		else if (bearing > 135 && bearing <= 157.5)
			direction = 64;
		else if (bearing > 157.5 && bearing <= 180)
			direction = 128;
		else if (bearing > 180 && bearing <= 202.5)
			direction = 256;
		else if (bearing > 202.5 && bearing <= 225)
			direction = 512;
		else if (bearing > 225 && bearing <= 247.5)
			direction = 1024;
		else if (bearing > 247.5 && bearing <= 270)
			direction = 2048;
		else if (bearing > 270 && bearing <= 292.5)
			direction = 4096;
		else if (bearing > 292.5 && bearing <= 315)
			direction = 8192;
		else if (bearing > 315 && bearing <= 337.5)
			direction = 16384;
		else if (bearing > 337.5 && bearing <= 360)
			direction = 32768;

		return direction;
	}

	private List<WydotRsu> getRsusByRoute(String route) {
		List<WydotRsu> rsus = RsuService.selectRsusByRoute(route);
		for (WydotRsu rsu : rsus) {
			rsu.setRsuRetries(3);
			rsu.setRsuTimeout(5000);
		}
		return rsus;
	}

	public List<WydotRsu> getRsusByLatLong(String direction, Coordinate startPoint, Coordinate endPoint, String route) {
		List<WydotRsu> rsus = new ArrayList<>();
		Comparator<WydotRsu> compLat = (l1, l2) -> Double.compare(l1.getLatitude(), l2.getLatitude());
		Comparator<WydotRsu> compLong = (l1, l2) -> Double.compare(l1.getLongitude(), l2.getLongitude());
		WydotRsu entryRsu = null;
		Integer numericRoute = Integer.parseInt(route.replaceAll("\\D+", ""));
		// WydotRsu rsuHigher;

		// if there are no rsus on this route
		List<WydotRsu> mainRsus = getRsusByRoute(route);
		if (mainRsus.size() == 0) {
			logWithDate("No RSUs found for route " + route);
			return rsus;
		} else {
			logWithDate("Found " + mainRsus.size() + " RSUs for route " + route);
		}

		Ellipsoid reference = Ellipsoid.WGS84;
		if (direction.toLowerCase().equals("i")) {

			// get rsus at mileposts less than your milepost
			// Note that in the future this logic may need to be refactored.
			// Currently we rely on east/west routes to be even-numbered and nort/south
			// routes to be odd-numbered. If we add additional RSUs, we'll need to consult
			// the databse for exceptions to this rule. Currently, RSUs only exist along
			// I 80 and parts of I 25
			List<WydotRsu> rsusLower = new ArrayList<>();
			if (numericRoute % 2 == 0) {
				rsusLower = mainRsus.stream().filter(x -> x.getLongitude() < startPoint.getLongitude())
						.collect(Collectors.toList());
			} else {
				rsusLower = mainRsus.stream().filter(x -> x.getLatitude() < startPoint.getLatitude())
						.collect(Collectors.toList());
			}

			if (rsusLower.size() == 0) {
				// if no rsus found farther west/south than startPoint
				// find milepost furthest west/south than longitude of TIM location
				if (numericRoute % 2 == 0) {
					rsusLower = mainRsus.stream().filter(x -> x.getLongitude() < endPoint.getLongitude())
							.collect(Collectors.toList());
					entryRsu = rsusLower.stream().min(compLong).get();
				} else {
					rsusLower = mainRsus.stream().filter(x -> x.getLatitude() < endPoint.getLatitude())
							.collect(Collectors.toList());
					entryRsu = rsusLower.stream().min(compLat).get();
				}
			} else {
				// else find milepost closest to lowerMilepost
				// get max from that list
				if (numericRoute % 2 == 0) {
					entryRsu = rsusLower.stream().max(compLong).get();
				} else {
					entryRsu = rsusLower.stream().max(compLat).get();
				}
			}

			GlobalCoordinates start = new GlobalCoordinates(startPoint.getLatitude(), startPoint.getLongitude());
			GlobalCoordinates end = new GlobalCoordinates(entryRsu.getLatitude(), entryRsu.getLongitude());
			GeodeticCalculator geoCalc = new GeodeticCalculator();
			GeodeticCurve curve = geoCalc.calculateGeodeticCurve(reference, start, end);
			double miles = 0.000621371 * curve.getEllipsoidalDistance();

			if (miles > 20) {
				// don't send to RSU if its further that X amount of miles away
				entryRsu = null;
			}

		} else { // d

			List<WydotRsu> rsusHigher = new ArrayList<>();
			// get rsus at mileposts greater than your milepost
			if (numericRoute % 2 == 0) {
				rsusHigher = mainRsus.stream().filter(x -> x.getLongitude() > endPoint.getLongitude())
						.collect(Collectors.toList());
			} else {
				rsusHigher = mainRsus.stream().filter(x -> x.getLatitude() > endPoint.getLatitude())
						.collect(Collectors.toList());
			}

			if (rsusHigher.size() == 0) {

				if (numericRoute % 2 == 0) {
					rsusHigher = mainRsus.stream().filter(x -> x.getLongitude() > startPoint.getLongitude())
							.collect(Collectors.toList());
					entryRsu = rsusHigher.stream().max(compLong).get();
				} else {
					rsusHigher = mainRsus.stream().filter(x -> x.getLatitude() > startPoint.getLatitude())
							.collect(Collectors.toList());
					entryRsu = rsusHigher.stream().max(compLat).get();
				}

				if (rsusHigher.size() == 0) {
					logWithDate("No RSUs found higher than 'low' point");
				}

			} else {
				if (numericRoute % 2 == 0) {
					entryRsu = rsusHigher.stream().min(compLong).get();
				} else {
					entryRsu = rsusHigher.stream().min(compLat).get();
				}
			}

			GlobalCoordinates start = new GlobalCoordinates(endPoint.getLatitude(), endPoint.getLongitude());
			GlobalCoordinates end = new GlobalCoordinates(entryRsu.getLatitude(), entryRsu.getLongitude());
			GeodeticCalculator geoCalc = new GeodeticCalculator();
			GeodeticCurve curve = geoCalc.calculateGeodeticCurve(reference, start, end);
			double miles = 0.000621371 * curve.getEllipsoidalDistance();// returns in meters, so convert to miles

			if (miles > 20) {
				// don't send to RSU if its further than 20 miles away
				logWithDate("Entry RSU is > 20 miles from the affected area, removing it from the list");
				entryRsu = null;
			}
		}

		if (numericRoute % 2 == 0) {
			rsus = mainRsus.stream().filter(
					x -> x.getLongitude() >= startPoint.getLongitude() && x.getLongitude() <= endPoint.getLongitude())
					.collect(Collectors.toList());
		} else {
			rsus = mainRsus.stream().filter(
					x -> x.getLatitude() >= startPoint.getLatitude() && x.getLatitude() <= endPoint.getLatitude())
					.collect(Collectors.toList());
		}

		if (entryRsu != null)
			rsus.add(entryRsu);

		return rsus;
	}

	/**
	 * Creates a connection with authentication via an apikey and returning JSON.
	 * Used to send HTTP requests to the SDX api
	 * 
	 * @param method The HTTP method to use (GET,POST,PUT,DELETE)
	 * @param url    The URL to send the request to
	 * @param apiKey The apikey value to apply in the header
	 * @return
	 * @throws IOException
	 */
	public HttpURLConnection getSdxUrlConnection(String method, URL url, String apiKey) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod(method);
		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestProperty("apikey", apiKey);

		return conn;
	}
}