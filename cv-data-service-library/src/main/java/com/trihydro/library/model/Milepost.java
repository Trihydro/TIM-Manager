package com.trihydro.library.model;

public class Milepost {
    private static final double RE = 6371000.0; // Earth radius for distance calculations

    private String commonName;
    private Double milepost;
    private String direction;
    private Double latitude;
    private Double longitude;
    private Double bearing;

    public Milepost(){}

      /**
	 * A copy constructor to avoid using the clonable interface.
	 *
	 * @param Milepost original Milepost to be copied
	 * @return a copy of the original Milepost
	 */
    public Milepost(Milepost mp){
        commonName=mp.commonName;
        milepost = mp.milepost;
		direction = mp.direction;
		latitude = mp.latitude;
		longitude = mp.longitude;
		bearing = mp.bearing;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String route) {
        this.commonName = route;
    }

    public Double getMilepost() {
        return milepost;
    }

    public void setMilepost(Double milepost) {
        this.milepost = milepost;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    // TODO: this is used to determine tim direction, but is no longer in the view
    public Double getBearing() {
        return bearing;
    }

    public void setBearing(Double bearing) {
        this.bearing = bearing;
    }

    /**
     * Calculates distance that the offset node is from the direct line between the
     * current node and end node. Distance measured in meters (m).
     *
     * @param offNode Milepost offset from line between current and end nodes
     * @param endNode Milepost end node
     * @return distance that offset node is from line between current and next nodes
     *         (m)
     *
     */
    public double offsetDistance(Milepost offNode, Milepost endNode) {

        double bse = this.bearingTo(endNode); // bearing start to end node
        double bso = this.bearingTo(offNode); // bearing start to offset node
        double dso = this.angularDistanceTo(offNode); // angular distance start to offset node

        double dXt = Math.asin(Math.sin(dso) * Math.sin(bso - bse)) * Milepost.RE;

        return dXt;

    }

    /**
     * Calculates great arc bearing angle between this node and the given next node.
     * Returned angle measurement is in radians.
     *
     * @param nextNode Milepost to calculate bearing to
     * @return bearing angle (radians) of great arc between this node and next node
     */
    public double bearingTo(Milepost nextNode) {

        double lat1 = this.getLatitude();
        double lat2 = nextNode.getLatitude();
        double dlon = nextNode.getLongitude() - this.getLongitude();

        double y = Math.sin(dlon) * Math.cos(lat2);
        double x = (Math.cos(lat1) * Math.sin(lat2)) - (Math.sin(lat1) * Math.cos(lat2) * Math.cos(dlon));

        double bearing = Math.atan2(y, x);

        // convert bearing into a compass bearing (0 - 360 in radians)
        return Math.IEEEremainder(bearing + (2.0 * Math.PI), 2.0 * Math.PI);

    }

    /**
     * Calculates the angular distance from this node to the nextNode. The haversine
     * formula is used as the basis of this calculation. The returned angular
     * distance is in radians.
     *
     * @param nextNode Milepost to calculate angular distance to
     * @return angular distance (radians) of great arc between this node and next
     *         node
     */
    public double angularDistanceTo(Milepost nextNode) {

        double lat1 = this.getLatitude();
        double lat2 = nextNode.getLatitude();
        double dlat = lat2 - lat1;
        double dlon = nextNode.getLongitude() - this.getLongitude();

        double a = (Math.sin(dlat / 2.0) * Math.sin(dlat / 2.0))
                + (Math.cos(lat1) * Math.cos(lat2) * Math.sin(dlon / 2.0) * Math.sin(dlon / 2.0));
        double c = 2.0 * (Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a)));

        return c;

    }
}