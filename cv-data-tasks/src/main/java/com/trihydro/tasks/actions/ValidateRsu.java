package com.trihydro.tasks.actions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.RsuIndexInfo;
import com.trihydro.library.service.RsuDataService;
import com.trihydro.tasks.models.ActiveTimMapping;
import com.trihydro.tasks.models.Collision;
import com.trihydro.tasks.models.RsuInformation;
import com.trihydro.tasks.models.RsuValidationResult;

public class ValidateRsu implements Callable<RsuValidationResult> {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat dateFormatWithMs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private RsuDataService rsuDataService;

    private String ipv4Address;
    private List<ActiveTim> activeTims;
    private List<RsuIndexInfo> rsuIndices;
    private RsuValidationResult result;

    public ValidateRsu(RsuInformation rsu, RsuDataService rsuDataService) {
        this.ipv4Address = rsu.getIpv4Address();
        this.rsuDataService = rsuDataService;
        // We need to copy this list since we'll be manipulating the list contents
        activeTims = new ArrayList<>(rsu.getRsuActiveTims());
    }

    public String getIpv4Address() {
        return ipv4Address;
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
        result = new RsuValidationResult();

        // Retrieve info for populated indexes on RSU
        rsuIndices = rsuDataService.getRsuDeliveryStartTimes(ipv4Address);

        // Check if error occurred querying indices.
        if (rsuIndices == null) {
            result.setRsuUnresponsive(true);
            return result;
        }

        // Check if there are any ActiveTims claiming the same index
        calculateCollisions();

        // Verify Active TIMs
        for (ActiveTim tim : activeTims) {

            // Check if index claimed by ActiveTim is populated on RSU
            int pos = Collections.binarySearch(rsuIndices, new RsuIndexInfo(tim.getRsuIndex(), null), findByIndex);

            if (pos < 0) {
                result.getMissingFromRsu().add(tim);
            } else {
                // We've mapped an ActiveTim to the RSU index. Remove this RSU index
                // from the list of indexes, since we've accounted for it
                RsuIndexInfo rsuInfo = rsuIndices.get(pos);

                try {
                    if (!datesEqual(tim.getStartDateTime(), rsuInfo.getDeliveryStartTime())) {
                        // The message at this index on the RSU is stale.
                        result.getStaleIndexes().add(new ActiveTimMapping(tim, rsuInfo));
                    }
                } catch(ParseException ex) {
                    // Assume the index is stale if we can't verify the start dates.
                    // Resubmitting this TIM should hopefully fix any issue in the DateTime
                    // format on either the RSU or in the Database.
                    result.getStaleIndexes().add(new ActiveTimMapping(tim, rsuInfo));
                }

                rsuIndices.remove(pos);
            }
        }

        // Check if there are any remaining, unaccounted for
        // indexes on the RSU
        if (rsuIndices.size() > 0) {
            result.setUnaccountedForIndices(
                    rsuIndices.stream().map((item) -> item.getIndex()).collect(Collectors.toList()));
        }

        return result;
    }

    private void calculateCollisions() {
        Map<Integer, List<ActiveTim>> indexAssignments = new HashMap<>();

        // Iterate over the Active Tims on this rsu. Group by assigned rsu index
        for (ActiveTim tim : activeTims) {
            int currentIndex = tim.getRsuIndex();
            // If this RSU index isn't present in the map, initialize value
            // to be an empty list of ActiveTims.
            if (!indexAssignments.containsKey(currentIndex)) {
                indexAssignments.put(currentIndex, new ArrayList<>());
            }

            // Push Active Tim onto map at that index
            indexAssignments.get(currentIndex).add(tim);
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
        if (pos >= 0) {
            rsuIndices.remove(pos);
        }

        activeTims.removeIf((t) -> t.getRsuIndex().equals(index));
    }

    // Checks if the dates are roughly equivalent (within 1 minute of eachother)
    private boolean datesEqual(String first, String second) throws ParseException {
        Date firstDate = getDate(first);
        Date secondDate = getDate(second);

        long diff = Math.abs(firstDate.getTime() - secondDate.getTime());

        return diff < 60000; // 60,000 ms in 1 minute
    }

    private Date getDate(String toParse) throws ParseException {
        if (toParse.contains(".")) {
            return dateFormatWithMs.parse(toParse);
        } else {
            return dateFormat.parse(toParse);
        }
    }
}