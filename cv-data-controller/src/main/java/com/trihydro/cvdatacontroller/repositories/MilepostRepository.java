package com.trihydro.cvdatacontroller.repositories;

import java.math.BigDecimal;
import java.util.Collection;

import com.trihydro.cvdatacontroller.model.Milepost;

public interface MilepostRepository {

    Collection<Milepost> getMilepostsByCommonNameWithLimit(String commonName, int limit);

    /**
     * Finds a path along commonName route between the given endpoints. Adds a
     * buffer point to the start point to help with anchoring
     * 
     * @param commonName the common name of the route to use
     * @param startLat   starting latitude
     * @param startLong  starting longitude
     * @param endLat     ending latitude
     * @param endLong    ending longitude
     * @param direction  direction of travel (I/D/B)
     * @return Collection of Milepost objects within given boundaries
     */
    Collection<Milepost> getPathWithBuffer(String commonName, BigDecimal startLat, BigDecimal startLong, BigDecimal endLat,
    BigDecimal endLong, String direction);

    /**
     * Finds a path along commonName route with the given point. Creates a buffer
     * with a mileage of bufferInMiles around that point dependent upon direction
     * field. Accounts for edge cases of path terminus
     * 
     * @param commonName
     * @param lat
     * @param lon
     * @param direction
     * @param bufferInMiles
     * @return
     */
    Collection<Milepost> getPathWithSpecifiedBuffer(String commonName, BigDecimal lat, BigDecimal lon, String direction,
            Double bufferInMiles);
}