package com.trihydro.cvdatacontroller.repositories;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.trihydro.cvdatacontroller.model.Milepost;

import org.neo4j.ogm.session.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class MilepostRepositoryImplementation implements MilepostRepository {
    private final Session session;

    public MilepostRepositoryImplementation(Session session) {
        this.session = session;
    }

    @Override
    public Collection<Milepost> getMilepostsByCommonNameWithLimit(String commonName, int limit) {
        String query = "MATCH (m:Milepost) WHERE m.CommonName = $commonName RETURN m LIMIT {limit}";
        Map<String, Object> map = new HashMap<>();
        map.put("commonName", commonName);
        map.put("limit", limit);
        Iterable<Milepost> mp = session.query(Milepost.class, query, map);
        return StreamSupport.stream(mp.spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public Collection<Milepost> getPathWithBuffer(String commonName, Double startLat, Double startLong, Double endLat,
            Double endLong, String direction) {
        String dirQuery = "[";
        if (direction.toUpperCase() != "B") {
            dirQuery += "'" + direction.toUpperCase() + "', ";
        }
        dirQuery += "'B']";

        String query = "match(startMp:Milepost{CommonName: $commonName})";
        query += " where startMp.Direction in " + dirQuery;
        query += " with startMp, distance(point({longitude:$startLong,latitude:$startLat}), point({longitude:startMp.Longitude,latitude:startMp.Latitude})) as d1 ";
        query += " with startMp, d1 ORDER BY d1 ASC LIMIT 1";
        query += " match(startAdjust:Milepost)-[rel]->(startMp)";
        query += " with coalesce(startAdjust, startMp) as newStart";// coalesce gets first non-null in list
        query += " match(endMp:Milepost{CommonName: $commonName})";
        query += " where endMp.Direction in " + dirQuery;
        query += " with newStart, endMp, distance(point({longitude:$endLong,latitude:$endLat}), point({longitude:endMp.Longitude,latitude:endMp.Latitude})) as d2 ";
        query += " with newStart, endMp, d2 ORDER BY d2 ASC LIMIT 1";
        query += " with newStart, endMp ";
        query += " call algo.shortestPath.stream(newStart,endMp) yield nodeId";
        query += " match(other:Milepost) ";
        query += " where id(other) = nodeId return other;";

        Map<String, Object> map = new HashMap<>();
        map.put("commonName", commonName);
        map.put("startLat", startLat);
        map.put("startLong", startLong);
        map.put("endLat", endLat);
        map.put("endLong", endLong);
        Iterable<Milepost> mp = session.query(Milepost.class, query, map);
        return StreamSupport.stream(mp.spliterator(), false).collect(Collectors.toList());
    }
}