-- create countyrds.county_roads_v1_h with columns:
-- - objectid: integer
-- - globalid: character varying(38)
-- - cr_id: integer
-- - authority_organization_id: integer
-- - county: character varying(16)
-- - type: charvvar(8)
-- - rd_no: character varying(8)
-- - name: character varying(64)
-- - description: character varying(255)
-- - point_from: character varying(64)
-- - point_to: character varying(64)
-- - active: smallint
-- - closed: smallint
-- - c2lhpv: smallint
-- - ntt: smallint
-- - info_last_updated_user: character varying(255)
-- - conditions_last_updated_date: timestamp without time zone
-- - gdb_from_date: timestamp without time zone
-- - gb_to_date: timestamp without time zone
-- - gdb_archive_old: smallint
-- - shape: geometry

CREATE TABLE countyrds.county_roads_v1_h (
    objectid integer,
    globalid character varying(38),
    cr_id integer,
    authority_organization_id integer,
    county character varying(16),
    type character varying(8),
    rd_no character varying(8),
    name character varying(64),
    description character varying(255),
    point_from character varying(64),
    point_to character varying(64),
    active smallint,
    closed smallint,
    c2lhpv smallint,
    ntt smallint,
    info_last_updated_user character varying(255),
    conditions_last_updated_date timestamp without time zone,
    gdb_from_date timestamp without time zone,
    gdb_to_date timestamp without time zone,
    gdb_archive_old smallint,
    shape geometry
);

-- insert mock data
INSERT INTO countyrds.county_roads_v1_h (objectid, globalid, cr_id, authority_organization_id, county, type, rd_no, name, description, point_from, point_to, active, closed, c2lhpv, ntt, info_last_updated_user, conditions_last_updated_date, gdb_from_date, gdb_to_date, gdb_archive_old, shape) VALUES
(1, '1', 1, 1, 'county', 'type', 'rd_no', 'name', 'description', 'point_from', 'point_to', 1, 0, 0, 0, 'info_last_updated_user', '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 0, 'POINT(0 0)'),
(2, '2', 2, 2, 'county', 'type', 'rd_no', 'name', 'description', 'point_from', 'point_to', 1, 0, 0, 0, 'info_last_updated_user', '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 0, 'POINT(0 0)'),
(3, '3', 3, 3, 'county', 'type', 'rd_no', 'name', 'description', 'point_from', 'point_to', 1, 0, 0, 0, 'info_last_updated_user', '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 0, 'POINT(0 0)');

-- create database trigger that calls pg_notify with a custom payload that includes row data
CREATE OR REPLACE FUNCTION notify_county_roads_v1_h() RETURNS trigger AS $$
DECLARE
    payload json;
BEGIN
    payload := json_build_object(
        'table', TG_TABLE_NAME,
        'action', TG_OP,
        'before', row_to_json(OLD),
        'after', row_to_json(NEW)
    );
    PERFORM pg_notify('adhoc_listener_channel', payload::text);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS notify_county_roads_v1_h ON countyrds.county_roads_v1_h;
CREATE TRIGGER notify_county_roads_v1_h
AFTER INSERT OR UPDATE OR DELETE ON countyrds.county_roads_v1_h
FOR EACH ROW EXECUTE FUNCTION notify_county_roads_v1_h();

-- alter table to set replica identity to allow deletions from the table
ALTER TABLE countyrds.county_roads_v1_h REPLICA IDENTITY FULL;