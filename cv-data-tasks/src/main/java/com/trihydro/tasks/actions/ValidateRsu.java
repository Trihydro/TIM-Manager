package com.trihydro.tasks.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.google.gson.Gson;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TimQuery;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.service.OdeService;
import com.trihydro.tasks.config.DataTasksConfiguration;
import com.trihydro.tasks.models.Collision;
import com.trihydro.tasks.models.EnvActiveTim;
import com.trihydro.tasks.models.PopulatedRsu;
import com.trihydro.tasks.models.RsuValidationResult;

public class ValidateRsu implements Callable<RsuValidationResult> {
    private DataTasksConfiguration config;

    private PopulatedRsu rsu;
    private List<Integer> rsuIndices;
    private RsuValidationResult result;

    public ValidateRsu(PopulatedRsu rsu, DataTasksConfiguration config) {
        this.rsu = rsu;
        this.config = config;
    }

    public PopulatedRsu getRsu() {
        return rsu;
    }

    @Override
    public RsuValidationResult call() {
        System.out.println("Processing " + rsu.getIpv4Address());

        result = new RsuValidationResult(rsu.getIpv4Address());
        // Retrieve set indices from RSU
        WydotRsu wyRsu = new WydotRsu();
        // TODO: push these values to a config?
        wyRsu.setRsuTarget(rsu.getIpv4Address());
        wyRsu.setRsuRetries(3);
        wyRsu.setRsuTimeout(5000);

        TimQuery odeResult = OdeService.submitTimQuery(wyRsu, 0, config.getOdeUrl());

        // Check if error occurred querying indices.
        // If it did, return null.
        if (odeResult == null) {
            result.setRsuUnresponsive(true);
            return result;
        }

        rsuIndices = odeResult.getIndicies_set();

        // Check if there are any ActiveTims claiming the same index
        calculateCollisions();

        for (EnvActiveTim record : rsu.getRsuActiveTims()) {
            // TODO: should we check for null rsuIndex?
            int pos = rsuIndices.indexOf(record.getActiveTim().getRsuIndex());
            if (pos < 0) {
                result.getMissingFromRsu().add(record);
            } else {
                rsuIndices.remove(pos);
            }
        }

        // Check if there are any remaining, unaccounted for
        // indices on the RSU
        if (rsuIndices.size() > 0) {
            result.setUnaccountedForIndices(rsuIndices);
        }

        Gson gson = new Gson();
        System.out.println(gson.toJson(result));
        return result;
    }

    private void calculateCollisions() {
        Map<Integer, List<EnvActiveTim>> indexAssignments = new HashMap<>();

        // Iterate over the Active Tims on this rsu. Group by assigned rsu index
        for (EnvActiveTim record : rsu.getRsuActiveTims()) {
            int currentIndex = record.getActiveTim().getRsuIndex();
            // If this RSU index isn't present in the map, initialize value
            // to be an empty list of ActiveTims.
            if (!indexAssignments.containsKey(currentIndex)) {
                indexAssignments.put(currentIndex, new ArrayList<>());
            }

            // Push Active Tim onto map at that index
            indexAssignments.get(currentIndex).add(record);
        }

        // Iterate over indexes and find any that have > 1 Active Tim assigned
        indexAssignments.forEach((index, tims) -> {
            if (tims.size() > 1) {
                Collision c = new Collision(index, tims);
                result.getCollisions().add(c);

                // Remove index from RSU indices and ActiveTims claiming
                // that index. This will ensure that index won't be reported
                // as "unaccounted for" and the ActiveTims won't be reported
                // as "missing from RSU"
                removeCollisionFromRsu(index);
            }
        });
    }

    private void removeCollisionFromRsu(Integer index) {
        rsuIndices.remove(index);
        rsu.getRsuActiveTims().removeIf((t) -> t.getActiveTim().getRsuIndex().equals(index));
    }
}