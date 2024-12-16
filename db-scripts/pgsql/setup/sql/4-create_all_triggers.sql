SET client_encoding TO 'UTF8';


DROP TRIGGER IF EXISTS active_tim_holding_trigger ON active_tim_holding CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_active_tim_holding_trigger() RETURNS trigger AS $BODY$
BEGIN
  SELECT nextval('active_tim_holding_seq')
  INTO STRICT   NEW.ACTIVE_TIM_HOLDING_ID
;
RETURN NEW;
END
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER active_tim_holding_trigger
	BEFORE INSERT ON active_tim_holding FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_active_tim_holding_trigger();

DROP TRIGGER IF EXISTS active_tim_trigger ON active_tim CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_active_tim_trigger() RETURNS trigger AS $BODY$
BEGIN
  SELECT nextval('active_tim_seq')
  INTO STRICT   NEW.active_tim_id
;
RETURN NEW;
END
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER active_tim_trigger
	BEFORE INSERT ON active_tim FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_active_tim_trigger();


DROP TRIGGER IF EXISTS incident_action_lut_trigger ON incident_action_lut CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_incident_action_lut_trigger() RETURNS trigger AS $BODY$
BEGIN
  SELECT nextval('incident_action_lut_seq')
  INTO STRICT   NEW.incident_action_lut_id
;
RETURN NEW;
END
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER incident_action_lut_trigger
	BEFORE INSERT ON incident_action_lut FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_incident_action_lut_trigger();

DROP TRIGGER IF EXISTS incident_effect_lut_trigger ON incident_effect_lut CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_incident_effect_lut_trigger() RETURNS trigger AS $BODY$
BEGIN
  SELECT nextval('incident_effect_lut_seq')
  INTO STRICT   NEW.incident_effect_lut_id
;
RETURN NEW;
END
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER incident_effect_lut_trigger
	BEFORE INSERT ON incident_effect_lut FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_incident_effect_lut_trigger();

DROP TRIGGER IF EXISTS incident_problem_lut_trigger ON incident_problem_lut CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_incident_problem_lut_trigger() RETURNS trigger AS $BODY$
BEGIN
  SELECT nextval('incident_problem_lut_seq')
  INTO STRICT   NEW.incident_problem_lut_id
;
RETURN NEW;
END
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER incident_problem_lut_trigger
	BEFORE INSERT ON incident_problem_lut FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_incident_problem_lut_trigger();

DROP TRIGGER IF EXISTS rw_buffer_action_lut_trigger ON rw_buffer_action_lut CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_rw_buffer_action_lut_trigger() RETURNS trigger AS $BODY$
BEGIN
  SELECT nextval('rw_buffer_action_lut_seq')
  INTO STRICT   NEW.rw_buffer_action_lut_id
;
RETURN NEW;
END
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER rw_buffer_action_lut_trigger
	BEFORE INSERT ON rw_buffer_action_lut FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_rw_buffer_action_lut_trigger();

DROP TRIGGER IF EXISTS sec_result_code_type_trigger ON security_result_code_type CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_sec_result_code_type_trigger() RETURNS trigger AS $BODY$
BEGIN
  SELECT nextval('security_result_code_type_seq')
  INTO STRICT   NEW.security_result_code_type_id
;
RETURN NEW;
END
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER sec_result_code_type_trigger
	BEFORE INSERT ON security_result_code_type FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_sec_result_code_type_trigger();

DROP TRIGGER IF EXISTS tim_trigger_ ON tim CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_tim_trigger_() RETURNS trigger AS $BODY$
BEGIN
  SELECT nextval('tim_seq')
  INTO STRICT   NEW.tim_id
;
RETURN NEW;
END
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER tim_trigger_
	BEFORE INSERT ON tim FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_tim_trigger_();

DROP TRIGGER IF EXISTS tim_type_trigger ON tim_type CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_tim_type_trigger() RETURNS trigger AS $BODY$
BEGIN
  SELECT nextval('tim_type_seq')
  INTO STRICT   NEW.tim_type_id
;
RETURN NEW;
END
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER tim_type_trigger
	BEFORE INSERT ON tim_type FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_tim_type_trigger();

DROP TRIGGER IF EXISTS trg_category_id ON category CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_trg_category_id() RETURNS trigger AS $BODY$
BEGIN
select nextval('category_id_seq')
into STRICT NEW.category_id
;
RETURN NEW;
end
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER trg_category_id
	BEFORE INSERT ON category FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_trg_category_id();

DROP TRIGGER IF EXISTS trg_computed_lane_id ON computed_lane CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_trg_computed_lane_id() RETURNS trigger AS $BODY$
BEGIN
select nextval('computed_lane_id_seq')
into STRICT NEW.computed_lane_id
;
RETURN NEW;
end
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER trg_computed_lane_id
	BEFORE INSERT ON computed_lane FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_trg_computed_lane_id();

DROP TRIGGER IF EXISTS trg_data_frame_id ON data_frame CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_trg_data_frame_id() RETURNS trigger AS $BODY$
BEGIN
select nextval('data_frame_seq')
into STRICT NEW.data_frame_id
;
RETURN NEW;
end
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER trg_data_frame_id
	BEFORE INSERT ON data_frame FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_trg_data_frame_id();

DROP TRIGGER IF EXISTS trg_data_frame_itis_code_id ON data_frame_itis_code CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_trg_data_frame_itis_code_id() RETURNS trigger AS $BODY$
BEGIN
select nextval('data_frame_itis_code_id_seq')
into STRICT NEW.data_frame_itis_code_id
;
RETURN NEW;
end
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER trg_data_frame_itis_code_id
	BEFORE INSERT ON data_frame_itis_code FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_trg_data_frame_itis_code_id();

DROP TRIGGER IF EXISTS trg_data_list_id ON data_list CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_trg_data_list_id() RETURNS trigger AS $BODY$
BEGIN
select nextval('data_list_id_seq')
into STRICT NEW.data_list_id
;
RETURN NEW;
end
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER trg_data_list_id
	BEFORE INSERT ON data_list FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_trg_data_list_id();

DROP TRIGGER IF EXISTS trg_http_logging_id ON http_logging CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_trg_http_logging_id() RETURNS trigger AS $BODY$
BEGIN
select nextval('http_logging_seq')
into STRICT NEW.http_logging_id
;
RETURN NEW;
end
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER trg_http_logging_id
	BEFORE INSERT ON http_logging FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_trg_http_logging_id();

DROP TRIGGER IF EXISTS trg_itis_code_id ON itis_code CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_trg_itis_code_id() RETURNS trigger AS $BODY$
BEGIN
select nextval('itis_code_id_seq')
into STRICT NEW.itis_code_id
;
RETURN NEW;
end
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER trg_itis_code_id
	BEFORE INSERT ON itis_code FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_trg_itis_code_id();

DROP TRIGGER IF EXISTS trg_node_ll_id ON node_ll CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_trg_node_ll_id() RETURNS trigger AS $BODY$
BEGIN
select nextval('node_ll_id_seq')
into STRICT NEW.node_ll_id
;
RETURN NEW;
end
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER trg_node_ll_id
	BEFORE INSERT ON node_ll FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_trg_node_ll_id();

DROP TRIGGER IF EXISTS trg_node_xy_id ON node_xy CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_trg_node_xy_id() RETURNS trigger AS $BODY$
BEGIN
select nextval('node_xy_id_seq')
into STRICT NEW.node_xy_id
;
RETURN NEW;
end
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER trg_node_xy_id
	BEFORE INSERT ON node_xy FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_trg_node_xy_id();

DROP TRIGGER IF EXISTS trg_old_region_id ON old_region CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_trg_old_region_id() RETURNS trigger AS $BODY$
BEGIN
select nextval('old_region_id_seq')
into STRICT NEW.old_region_id
;
RETURN NEW;
end
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER trg_old_region_id
	BEFORE INSERT ON old_region FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_trg_old_region_id();

DROP TRIGGER IF EXISTS trg_path_id ON path CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_trg_path_id() RETURNS trigger AS $BODY$
BEGIN
select nextval('path_id_seq')
into STRICT NEW.path_id
;
RETURN NEW;
end
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER trg_path_id
	BEFORE INSERT ON path FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_trg_path_id();

DROP TRIGGER IF EXISTS trg_path_node_ll_id ON path_node_ll CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_trg_path_node_ll_id() RETURNS trigger AS $BODY$
BEGIN
select nextval('path_node_ll_seq')
into STRICT NEW.path_node_ll_id
;
RETURN NEW;
end
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER trg_path_node_ll_id
	BEFORE INSERT ON path_node_ll FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_trg_path_node_ll_id();

DROP TRIGGER IF EXISTS trg_path_node_xy_id ON path_node_xy CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_trg_path_node_xy_id() RETURNS trigger AS $BODY$
BEGIN
select nextval('path_node_xy_seq')
into STRICT NEW.path_node_xy_id
;
RETURN NEW;
end
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER trg_path_node_xy_id
	BEFORE INSERT ON path_node_xy FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_trg_path_node_xy_id();

DROP TRIGGER IF EXISTS trg_region_id ON region CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_trg_region_id() RETURNS trigger AS $BODY$
BEGIN
select nextval('region_id_seq')
into STRICT NEW.region_id
;
RETURN NEW;
end
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER trg_region_id
	BEFORE INSERT ON region FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_trg_region_id();

DROP TRIGGER IF EXISTS trg_region_list_id ON region_list CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_trg_region_list_id() RETURNS trigger AS $BODY$
BEGIN
select nextval('region_list_id_seq')
into STRICT NEW.region_list_id
;
RETURN NEW;
end
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER trg_region_list_id
	BEFORE INSERT ON region_list FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_trg_region_list_id();

DROP TRIGGER IF EXISTS trg_rsu_id ON rsu CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_trg_rsu_id() RETURNS trigger AS $BODY$
BEGIN
select nextval('rsu_id_seq')
into STRICT NEW.rsu_id
;
RETURN NEW;
end
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER trg_rsu_id
	BEFORE INSERT ON rsu FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_trg_rsu_id();

DROP TRIGGER IF EXISTS trg_shape_point_id ON shape_point CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_trg_shape_point_id() RETURNS trigger AS $BODY$
BEGIN
select nextval('shape_point_id_seq')
into STRICT NEW.shape_point_id
;
RETURN NEW;
end
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER trg_shape_point_id
	BEFORE INSERT ON shape_point FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_trg_shape_point_id();

DROP TRIGGER IF EXISTS trg_shape_point_node_xy_id ON shape_point_node_xy CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_trg_shape_point_node_xy_id() RETURNS trigger AS $BODY$
BEGIN
select nextval('shape_point_node_xy_id_seq')
into STRICT NEW.shape_point_node_xy_id
;
RETURN NEW;
end
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER trg_shape_point_node_xy_id
	BEFORE INSERT ON shape_point_node_xy FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_trg_shape_point_node_xy_id();

DROP TRIGGER IF EXISTS trg_status_log_id ON status_log CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_trg_status_log_id() RETURNS trigger AS $BODY$
BEGIN
  SELECT nextval('status_log_id_seq')
  INTO STRICT NEW.LOG_ID
;
RETURN NEW;
END
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER trg_status_log_id
	BEFORE INSERT ON status_log FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_trg_status_log_id();

DROP TRIGGER IF EXISTS trg_tim_rsu_id ON tim_rsu CASCADE;
CREATE OR REPLACE FUNCTION trigger_fct_trg_tim_rsu_id() RETURNS trigger AS $BODY$
BEGIN
select nextval('tim_rsu_id_seq')
into STRICT NEW.tim_rsu_id
;
RETURN NEW;
end
$BODY$
 LANGUAGE 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER trg_tim_rsu_id
	BEFORE INSERT ON tim_rsu FOR EACH ROW
	EXECUTE PROCEDURE trigger_fct_trg_tim_rsu_id();
