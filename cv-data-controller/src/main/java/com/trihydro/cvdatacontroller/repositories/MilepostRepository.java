package com.trihydro.cvdatacontroller.repositories;

import java.util.Collection;

import com.trihydro.cvdatacontroller.model.Milepost;

public interface MilepostRepository {

    Collection<Milepost> getMilepostsByCommonNameWithLimit(String commonName, int limit);

    Collection<Milepost> getPath(String commonName, Double startLat, Double startLong, Double endLat, Double endLong);
}