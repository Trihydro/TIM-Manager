package com.trihydro.cvdatacontroller.repositories;

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
    Collection<Milepost> getPathWithBuffer(String commonName, Double startLat, Double startLong, Double endLat,
            Double endLong, String direction);
}