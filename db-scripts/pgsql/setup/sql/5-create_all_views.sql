SET client_encoding TO 'UTF8';


CREATE OR REPLACE VIEW milepost_vw_new (common_name, direction, milepost, latitude, longitude) AS SELECT DISTINCT COMMON_NAME,
DIRECTION,
MILEPOST,
LATITUDE,
LONGITUDE
FROM MILEPOSTS;