package com.trihydro.tasks.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.RsuIndexInfo;
import com.trihydro.library.model.TimQuery;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.service.OdeService;
import com.trihydro.library.service.RsuDataService;
import com.trihydro.tasks.config.DataTasksConfiguration;
import com.trihydro.tasks.models.Collision;
import com.trihydro.tasks.models.EnvActiveTim;
import com.trihydro.tasks.models.PopulatedRsu;
import com.trihydro.tasks.models.RsuValidationResult;

public class ValidateRsu implements Callable<RsuValidationResult> {
    private RsuDataService rsuDataService;

    private PopulatedRsu rsu;
    private List<RsuIndexInfo> rsuIndices;
    private RsuValidationResult result;

    public ValidateRsu(PopulatedRsu rsu, RsuDataService rsuDataService) {
        this.rsu = rsu;
        this.rsuDataService = rsuDataService;
    }

    public PopulatedRsu getRsu() {
        return rsu;
    }

    private static Comparator<RsuIndexInfo> findByIndex;

    static {
        findByIndex = new Comparator<RsuIndexInfo>() {
            public int compare(RsuIndexInfo o1, RsuIndexInfo o2) {
                return o1.getIndex().compareTo(o2.getIndex());
            }
        };
    }

    @Override
    public RsuValidationResult call() {
        System.out.println("Processing " + rsu.getIpv4Address());

        result = new RsuValidationResult(rsu.getIpv4Address());

        // Retrieve info for populates indexes on RSU
        rsuIndices = rsuDataService.getRsuDeliveryStartTimes(rsu.getIpv4Address());

        // Check if error occurred querying indices.
        if (rsuIndices == null) {
            result.setRsuUnresponsive(true);
            return result;
        }

        // Check if there are any ActiveTims claiming the same index
        calculateCollisions();

        // Verify ActiveTims against RSU index info
        for (EnvActiveTim record : rsu.getRsuActiveTims()) {
            // TODO: should we check for null rsuIndex?
            ActiveTim tim = record.getActiveTim();
            int pos = Collections.binarySearch(rsuIndices, new RsuIndexInfo(tim.getRsuIndex(), null), findByIndex);

            if (pos < 0) {
                result.getMissingFromRsu().add(record);
            } else {
                // TODO: verify deliveryStart
                rsuIndices.remove(pos);
            }
        }

        // Check if there are any remaining, unaccounted for
        // indices on the RSU
        if (rsuIndices.size() > 0) {
            result.setUnaccountedForIndices(
                    rsuIndices.stream().map((item) -> item.getIndex()).collect(Collectors.toList()));
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
        int pos = Collections.binarySearch(rsuIndices, new RsuIndexInfo(index, null), findByIndex);
        if(pos >= 0) {
            rsuIndices.remove(pos);
        }
        
        rsu.getRsuActiveTims().removeIf((t) -> t.getActiveTim().getRsuIndex().equals(index));
    }
}