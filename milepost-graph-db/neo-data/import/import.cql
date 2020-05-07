load csv with headers from "file:///export.csv" as row
merge (m:Milepost {CommonName:row.COMMON_NAME, Milepost:toFloat(row.MILEPOST), Direction: row.DIRECTION, Latitude:toFloat(row.LATITUDE), Longitude:toFloat(row.LONGITUDE)});

////////////////////////////////////////////////////
match (n:Milepost)
where n.Direction in ["I", "B"]
call apoc.cypher.run('with {n} as n match (m:Milepost) where m.CommonName = n.CommonName and n.Milepost < m.Milepost < (n.Milepost + 5) and m.Direction in ["I","B"] and distance(point({longitude:n.Longitude, latitude:n.Latitude}), point({longitude:m.Longitude, latitude:m.Latitude})) < 1207 return m order by m.Milepost limit 1', {n:n}) yield value as val
with n, val.m as nxt
call apoc.create.relationship(n, replace(n.CommonName, " ", "_")+"_I", {Direction:n.Direction, CommonName:n.CommonName},nxt) yield rel
return rel;


match (n:Milepost)
where n.Direction in ["D", "B"]
call apoc.cypher.run('with {n} as n match (m:Milepost) where m.CommonName = n.CommonName and n.Milepost > m.Milepost > (n.Milepost - 5) and m.Direction in ["D","B"] and distance(point({longitude:n.Longitude, latitude:n.Latitude}), point({longitude:m.Longitude, latitude:m.Latitude})) < 1207 return m order by m.Milepost desc limit 1', {n:n}) yield value as val
with n, val.m as nxt
call apoc.create.relationship(n, replace(n.CommonName, " ", "_")+"_D", {Direction:n.Direction, CommonName:n.CommonName},nxt) yield rel
return rel;