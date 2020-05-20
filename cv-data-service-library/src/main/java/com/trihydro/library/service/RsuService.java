package com.trihydro.library.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.WydotRsuTim;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class RsuService extends CvDataServiceLibrary {

	private Utility utility;

	@Autowired
	public void InjectDependencies(Utility _utility) {
		this.utility = _utility;
	}

	public List<WydotRsu> selectAll() {
		String url = String.format("%s/rsus", config.getCvRestService());
		ResponseEntity<WydotRsu[]> response = restTemplateProvider.GetRestTemplate().getForEntity(url,
				WydotRsu[].class);
		return Arrays.asList(response.getBody());
	}

	public List<WydotRsu> selectRsusByRoute(String route) {
		String url = String.format("%s/rsus-by-route/%s", config.getCvRestService(), route);
		ResponseEntity<WydotRsu[]> response = restTemplateProvider.GetRestTemplate().getForEntity(url,
				WydotRsu[].class);
		return Arrays.asList(response.getBody());
	}

	public List<WydotRsuTim> getFullRsusTimIsOn(Long timId) {
		String url = String.format("%s/rsus-for-tim/%d", config.getCvRestService(), timId);
		ResponseEntity<WydotRsuTim[]> response = restTemplateProvider.GetRestTemplate().getForEntity(url,
				WydotRsuTim[].class);
		return Arrays.asList(response.getBody());
	}

	public List<WydotRsu> getRsusByLatLong(String direction, Coordinate startPoint, Coordinate endPoint, String route) {
		List<WydotRsu> rsus = new ArrayList<>();
		Comparator<WydotRsu> compLat = (l1, l2) -> l1.getLatitude().compareTo(l2.getLatitude());
		Comparator<WydotRsu> compLong = (l1, l2) -> l1.getLongitude().compareTo(l2.getLongitude());
		WydotRsu entryRsu = null;
		Integer numericRoute = Integer.parseInt(route.replaceAll("\\D+", ""));
		// WydotRsu rsuHigher;

		// if there are no rsus on this route
		List<WydotRsu> mainRsus = getRsusByRouteWithRetryAndTimeout(route);
		if (mainRsus.size() == 0) {
			utility.logWithDate("No RSUs found for route " + route);
			return rsus;
		} else {
			utility.logWithDate("Found " + mainRsus.size() + " RSUs for route " + route);
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
				rsusLower = mainRsus.stream().filter(x -> x.getLongitude().compareTo(startPoint.getLongitude()) < 0)
						.collect(Collectors.toList());
			} else {
				rsusLower = mainRsus.stream().filter(x -> x.getLatitude().compareTo(startPoint.getLatitude()) < 0)
						.collect(Collectors.toList());
			}

			if (rsusLower.size() == 0) {
				// if no rsus found farther west/south than startPoint
				// find milepost furthest west/south than longitude of TIM location
				if (numericRoute % 2 == 0) {
					rsusLower = mainRsus.stream().filter(x -> x.getLongitude().compareTo(endPoint.getLongitude()) < 0)
							.collect(Collectors.toList());
					if (rsusLower.size() > 0) {
						entryRsu = rsusLower.stream().min(compLong).get();
					}
				} else {
					rsusLower = mainRsus.stream().filter(x -> x.getLatitude().compareTo(endPoint.getLatitude()) < 0)
							.collect(Collectors.toList());
					if (rsusLower.size() > 0) {
						entryRsu = rsusLower.stream().min(compLat).get();
					}
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
		} else { // d

			List<WydotRsu> rsusHigher = new ArrayList<>();
			// get rsus at mileposts greater than your milepost
			if (numericRoute % 2 == 0) {
				rsusHigher = mainRsus.stream().filter(x -> x.getLongitude().compareTo(endPoint.getLongitude()) > 0)
						.collect(Collectors.toList());
			} else {
				rsusHigher = mainRsus.stream().filter(x -> x.getLatitude().compareTo(endPoint.getLatitude()) > 0)
						.collect(Collectors.toList());
			}

			if (rsusHigher.size() == 0) {

				if (numericRoute % 2 == 0) {
					rsusHigher = mainRsus.stream()
							.filter(x -> x.getLongitude().compareTo(startPoint.getLongitude()) > 0)
							.collect(Collectors.toList());
					if (rsusHigher.size() > 0) {
						entryRsu = rsusHigher.stream().max(compLong).get();
					}
				} else {
					rsusHigher = mainRsus.stream().filter(x -> x.getLatitude().compareTo(startPoint.getLatitude()) > 0)
							.collect(Collectors.toList());
					if (rsusHigher.size() > 0) {
						entryRsu = rsusHigher.stream().max(compLat).get();
					}
				}

				if (rsusHigher.size() == 0) {
					utility.logWithDate("No RSUs found higher than 'low' point");
				}

			} else {
				if (numericRoute % 2 == 0) {
					entryRsu = rsusHigher.stream().min(compLong).get();
				} else {
					entryRsu = rsusHigher.stream().min(compLat).get();
				}
			}
		}

		// Check distance to entry RSU
		if (entryRsu != null) {
			GlobalCoordinates start = new GlobalCoordinates(startPoint.getLatitude().doubleValue(),
					startPoint.getLongitude().doubleValue());
			GlobalCoordinates end = new GlobalCoordinates(entryRsu.getLatitude().doubleValue(),
					entryRsu.getLongitude().doubleValue());
			GeodeticCalculator geoCalc = new GeodeticCalculator();
			GeodeticCurve curve = geoCalc.calculateGeodeticCurve(reference, start, end);
			double miles = 0.000621371 * curve.getEllipsoidalDistance();

			if (miles > 20) {
				// don't send to RSU if its further that X amount of miles away
				entryRsu = null;
			}
		}

		if (numericRoute % 2 == 0) {
			// rsus = mainRsus.stream().filter(
			// x -> x.getLongitude() >= startPoint.getLongitude() && x.getLongitude() <=
			// endPoint.getLongitude())
			// .collect(Collectors.toList());
			rsus = mainRsus.stream().filter(x -> x.getLongitude().compareTo(startPoint.getLongitude()) >= 0
					&& x.getLongitude().compareTo(endPoint.getLongitude()) <= 0).collect(Collectors.toList());
		} else {
			rsus = mainRsus.stream().filter(x -> x.getLatitude().compareTo(startPoint.getLatitude()) >= 0
					&& x.getLatitude().compareTo(endPoint.getLatitude()) <= 0).collect(Collectors.toList());
		}

		if (entryRsu != null)
			rsus.add(entryRsu);

		return rsus;
	}

	private List<WydotRsu> getRsusByRouteWithRetryAndTimeout(String route) {
		List<WydotRsu> rsus = selectRsusByRoute(route);
		for (WydotRsu rsu : rsus) {
			rsu.setRsuRetries(3);
			rsu.setRsuTimeout(5000);
		}
		return rsus;
	}
}