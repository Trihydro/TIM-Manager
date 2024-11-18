package com.trihydro.cvdatacontroller.services;

import java.math.BigDecimal;
import java.util.Collection;

import com.trihydro.cvdatacontroller.model.Milepost;
import com.trihydro.cvdatacontroller.repositories.MilepostRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MilepostService {
    private final MilepostRepository milepostRepository;

    public MilepostService(MilepostRepository _milepostRepository) {
        this.milepostRepository = _milepostRepository;
    }

    @Transactional(readOnly = true)
    public Collection<Milepost> getMilepostsByCommonNameWithLimit(String commonName, int limit) {
        Collection<Milepost> result = milepostRepository.getMilepostsByCommonNameWithLimit(commonName, limit);
        return result;
    }

    @Transactional(readOnly = true)
    public Collection<Milepost> getPathWithBuffer(String commonName, BigDecimal startLat, BigDecimal startLong, BigDecimal endLat,
    BigDecimal endLong, String direction) {
        return milepostRepository.getPathWithBuffer(commonName, startLat, startLong, endLat, endLong, direction);
    }

    @Transactional(readOnly = true)
    public Collection<Milepost> getPathWithSpecifiedBuffer(String commonName, BigDecimal lat, BigDecimal lon, String direction,
            Double bufferInMiles) {
        return milepostRepository.getPathWithSpecifiedBuffer(commonName, lat, lon, direction, bufferInMiles);
    }
}