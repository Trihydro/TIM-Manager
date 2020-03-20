package com.trihydro.cvdatacontroller.services;

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
    public Collection<Milepost> getPath(String commonName, Double startLat, Double startLong, Double endLat,
            Double endLong) {
        return milepostRepository.getPath(commonName, startLat, startLong, endLat, endLong);
    }
}