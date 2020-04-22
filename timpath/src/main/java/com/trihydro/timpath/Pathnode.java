package com.trihydro.timpath;

import java.lang.Double;
import java.lang.Math;


public class Pathnode {

	static final double RE = 6371000.0; //Earth radius for distance calculations

	private int index;
	private String route;
	private double milepost;
	private String direction;
	private double latitude;
	private double longitude;
	private double elevation;
	private double bearing;

	public Pathnode(int idx, String rt, String mp, String di, String lat, String lon, String el, String br) {

		index = idx;
		route = rt;
		milepost = Double.parseDouble(mp);
		direction = di;
		latitude = Double.parseDouble(lat);
		longitude = Double.parseDouble(lon);
		elevation = Double.parseDouble(el);
		bearing = Double.parseDouble(br);

	}

	/**
	 * A copy constructor to avoid using the clonable interface.
	 *
	 * @param Pathnode original Pathnode to be copied
	 * @return a copy of the original Pathnode
	 */
	public Pathnode(Pathnode node) {
		index = node.index;
		route = node.route;
		milepost = node.milepost;
		direction = node.direction;
		latitude = node.latitude;
		longitude = node.longitude;
		elevation = node.elevation;
		bearing = node.bearing;
	}

	public double getLat() {
		return latitude * (Math.PI/180.0);
	}

	public double getLon() {
		return longitude * (Math.PI/180.0);
	}

	public int getIndex() {
		return index;
	}


	/**
	 * Calculates distance that the offset node is from the 
	 * direct line between the current node and end node. Distance 
	 * measured in meters (m).
	 *
	 * @param offNode Pathnode offset from line between current and end nodes
	 * @param endNode Pathnode end node 
	 * @return distance that offset node is from line between current and next nodes (m)
	 *
	 */
	public double offsetDistance(Pathnode offNode, Pathnode endNode) {

		double bse = this.bearingTo(endNode);  		  // bearing start to end node
		double bso = this.bearingTo(offNode);		  // bearing start to offset node
		double dso = this.angularDistanceTo(offNode); // angular distance start to offset node

		double dXt = Math.asin(Math.sin(dso) * Math.sin(bso - bse)) * Pathnode.RE;

		return dXt;

	}

	/**
	 * Calculates great arc bearing angle between this node and 
	 * the given next node. Returned angle measurement is in radians.
	 *
	 * @param nextNode Pathnode to calculate bearing to
	 * @return bearing angle (radians) of great arc between this node and next node
	 */
	public double bearingTo(Pathnode nextNode) {

		double lat1 = this.getLat();
		double lat2 = nextNode.getLat();
		double dlon = nextNode.getLon() - this.getLon();

		double y = Math.sin(dlon) * Math.cos(lat2);
		double x = (Math.cos(lat1) * Math.sin(lat2)) - (Math.sin(lat1) * Math.cos(lat2) * Math.cos(dlon));

		double bearing = Math.atan2(y,x);

		// convert bearing into a compass bearing (0 - 360 in radians)
		return Math.IEEEremainder(bearing + (2.0 * Math.PI), 2.0 * Math.PI);

	}

	/**
	 * Calculates the angular distance from this node to the nextNode.
	 * The haversine formula is used as the basis of this calculation. 
	 * The returned angular distance is in radians.
	 *
	 * @param nextNode Pathnode to calculate angular distance to
	 * @return angular distance (radians) of great arc between this node and next node
	 */
	public double angularDistanceTo(Pathnode nextNode) {

		double lat1 = this.getLat();
		double lat2 = nextNode.getLat();
		double dlat = lat2 - lat1;
		double dlon = nextNode.getLon() - this.getLon();

		double a = (Math.sin(dlat/2.0) * Math.sin(dlat/2.0)) + 
					(Math.cos(lat1) * Math.cos(lat2) * Math.sin(dlon/2.0) * Math.sin(dlon/2.0));
		double c = 2.0 * (Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a)));

		return c;

	}




}