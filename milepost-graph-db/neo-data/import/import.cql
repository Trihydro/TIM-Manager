load csv with headers from "file:///export.csv" as row
merge (m:Milepost {CommonName:row.COMMON_NAME, Milepost:toFloat(row.MILEPOST), Direction: row.DIRECTION, Latitude:toFloat(row.LATITUDE), Longitude:toFloat(row.LONGITUDE)});

////////////////////////////////////////////////////
match (n:Milepost)
where n.Direction in ["I", "B"]
call apoc.cypher.run('with {n} as n match (m:Milepost) where m.CommonName = n.CommonName and n.Milepost < m.Milepost and m.Direction in ["I","B"] with m, distance(point({longitude:n.Longitude, latitude:n.Latitude}), point({longitude:m.Longitude, latitude:m.Latitude})) as dst where dst < 8046.72 return m order by m.Milepost,dst limit 1', {n:n}) yield value as val
with n, val.m as nxt
call apoc.create.relationship(n, replace(n.CommonName, " ", "_")+"_I", {Direction:n.Direction, CommonName:n.CommonName},nxt) yield rel
return rel;


match (n:Milepost)
where n.Direction in ["D", "B"]
call apoc.cypher.run('with {n} as n match (m:Milepost) where m.CommonName = n.CommonName and n.Milepost > m.Milepost and m.Direction in ["D","B"] with m, distance(point({longitude:n.Longitude, latitude:n.Latitude}), point({longitude:m.Longitude, latitude:m.Latitude})) as dst where dst < 8046.72 return m order by m.Milepost desc, dst limit 1', {n:n}) yield value as val
with n, val.m as nxt
call apoc.create.relationship(n, replace(n.CommonName, " ", "_")+"_D", {Direction:n.Direction, CommonName:n.CommonName},nxt) yield rel
return rel;