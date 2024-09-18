-- Note: This should only be run on a local deployment database. The actual view is provided by WYDOT.

CREATE SCHEMA IF NOT EXISTS countyrds;
CREATE TABLE countyrds.county_road_geometry_v1_mv (
    pt_id bigint,
    cr_id integer,
    common_name text,
    direction text,
    milepost double precision,
    longitude double precision,
    latitude double precision,
    info_last_updated_date timestamp without time zone
);