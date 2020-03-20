//relationship match, updated
match (n:Milepost{CommonName: "COMMON_NAME"})
where n.Direction in ["I", "B"]
match (m:Milepost)
where m.CommonName = n.CommonName and m.Milepost > n.Milepost and m.Direction in ["I", "B"] and distance(point({longitude:n.Longitude, latitude:n.Latitude}), point({longitude:m.Longitude, latitude:m.Latitude})) < 804.672
with n, min(m.Milepost) as minM
match (nxt:Milepost)
where nxt.CommonName = n.CommonName and nxt.Milepost = minM and nxt.Direction in ["I", "B"] and distance(point({longitude:n.Longitude, latitude:n.Latitude}), point({longitude:nxt.Longitude, latitude:nxt.Latitude})) < 804.672
call apoc.create.relationship(n, replace(n.CommonName, " ", "_")+"_I", {Direction:n.Direction, CommonName:n.CommonName},nxt) yield rel
return rel;


//relationship match, updated
match (n:Milepost{CommonName: "COMMON_NAME"})
where n.Direction in ["D", "B"]
match (m:Milepost)
where m.CommonName = n.CommonName and m.Milepost < n.Milepost and m.Direction in ["D", "B"] and distance(point({longitude:n.Longitude, latitude:n.Latitude}), point({longitude:m.Longitude, latitude:m.Latitude})) < 804.672
with n, max(m.Milepost) as maxM
match (nxt:Milepost)
where nxt.CommonName = n.CommonName and nxt.Milepost = maxM and nxt.Direction in ["D", "B"] and distance(point({longitude:n.Longitude, latitude:n.Latitude}), point({longitude:nxt.Longitude, latitude:nxt.Latitude})) < 804.672
call apoc.create.relationship(n, replace(n.CommonName, " ", "_")+"_D", {Direction:n.Direction, CommonName:n.CommonName},nxt) yield rel
return rel;