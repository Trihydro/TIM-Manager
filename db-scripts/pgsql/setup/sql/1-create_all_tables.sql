SET client_encoding TO 'UTF8';



-- not part of a dependency relationship --
CREATE TABLE active_tim_holding (
	active_tim_holding_id bigint NOT NULL,
	client_id varchar(255),
	direction varchar(50) NOT NULL,
	rsu_target varchar(20),
	sat_record_id varchar(8),
	start_latitude double precision NOT NULL,
	start_longitude decimal(18,8) NOT NULL,
	end_latitude double precision NOT NULL,
	end_longitude double precision NOT NULL,
	rsu_index smallint,
	date_created timestamp,
	project_key bigint,
	expiration_date timestamp,
	packet_id varchar(50)
) ;
ALTER TABLE active_tim_holding ADD PRIMARY KEY (active_tim_holding_id);
ALTER TABLE active_tim_holding ADD UNIQUE (client_id,direction,rsu_target,sat_record_id);
ALTER TABLE active_tim_holding ALTER COLUMN ACTIVE_TIM_HOLDING_ID SET NOT NULL;
ALTER TABLE active_tim_holding ALTER COLUMN START_LATITUDE SET NOT NULL;
ALTER TABLE active_tim_holding ALTER COLUMN START_LONGITUDE SET NOT NULL;
ALTER TABLE active_tim_holding ALTER COLUMN END_LATITUDE SET NOT NULL;
ALTER TABLE active_tim_holding ALTER COLUMN END_LONGITUDE SET NOT NULL;
ALTER TABLE active_tim_holding ALTER COLUMN DIRECTION SET NOT NULL;


CREATE TABLE http_logging (
	http_logging_id bigint NOT NULL,
	request_time timestamp NOT NULL,
	rest_request varchar(2000) NOT NULL,
	response_time timestamp
) ;
ALTER TABLE http_logging ADD PRIMARY KEY (http_logging_id);
ALTER TABLE http_logging ALTER COLUMN HTTP_LOGGING_ID SET NOT NULL;
ALTER TABLE http_logging ALTER COLUMN REQUEST_TIME SET NOT NULL;
ALTER TABLE http_logging ALTER COLUMN REST_REQUEST SET NOT NULL;


CREATE TABLE milepost_test (
	route varchar(255),
	milepost decimal(38,8),
	direction varchar(255),
	latitude decimal(38,8),
	longitude decimal(38,8),
	elevation_ft decimal(38,8),
	bearing decimal(38,8)
) ;


CREATE TABLE mileposts (
	common_name varchar(20),
	direction varchar(1),
	milepost decimal(38,8),
	latitude decimal(38,8),
	longitude decimal(38,8),
	road_code varchar(50),
	lrs_route varchar(20)
) ;


CREATE TABLE rw_buffer_action_lut (
	description varchar(50),
	code varchar(20),
	rw_buffer_action_lut_id bigint NOT NULL
) ;
ALTER TABLE rw_buffer_action_lut ADD PRIMARY KEY (rw_buffer_action_lut_id);


CREATE TABLE tim_type (
	type varchar(10),
	description varchar(255),
	tim_type_id bigint NOT NULL
) ;
ALTER TABLE tim_type ADD PRIMARY KEY (tim_type_id);


CREATE TABLE entity_type (
	entity_type_id bigint NOT NULL,
	name varchar(128) NOT NULL,
	description varchar(255)
) ;
ALTER TABLE entity_type ADD PRIMARY KEY (entity_type_id);
ALTER TABLE entity_type ALTER COLUMN ENTITY_TYPE_ID SET NOT NULL;
ALTER TABLE entity_type ALTER COLUMN NAME SET NOT NULL;


CREATE TABLE status_type (
	status_type_id bigint NOT NULL,
	name varchar(128) NOT NULL,
	description varchar(255)
) ;
ALTER TABLE status_type ADD PRIMARY KEY (status_type_id);
ALTER TABLE status_type ALTER COLUMN STATUS_TYPE_ID SET NOT NULL;
ALTER TABLE status_type ALTER COLUMN NAME SET NOT NULL;


CREATE TABLE security_result_code_type (
	security_result_code_type varchar(255) NOT NULL,
	security_result_code_type_id bigint NOT NULL
) ;
ALTER TABLE security_result_code_type ADD PRIMARY KEY (security_result_code_type_id);
ALTER TABLE security_result_code_type ALTER COLUMN SECURITY_RESULT_CODE_TYPE SET NOT NULL;


CREATE TABLE category (
	category_id bigint NOT NULL,
	category varchar(255)
) ;
ALTER TABLE category ADD PRIMARY KEY (category_id);
ALTER TABLE category ALTER COLUMN CATEGORY_ID SET NOT NULL;


CREATE TABLE node_xy (
	node_xy_id bigint NOT NULL,
	delta varchar(255),
	node_lat double precision,
	node_long double precision,
	x bigint,
	y bigint,
	attributes_dwidth bigint,
	attributes_delevation bigint
) ;
ALTER TABLE node_xy ADD PRIMARY KEY (node_xy_id);
ALTER TABLE node_xy ALTER COLUMN NODE_XY_ID SET NOT NULL;


CREATE TABLE node_ll (
	node_ll_id bigint NOT NULL,
	delta varchar(255),
	node_lat double precision,
	node_long double precision,
	x bigint,
	y bigint,
	attributes_dwidth bigint,
	attributes_delevation bigint
) ;
ALTER TABLE node_ll ADD PRIMARY KEY (node_ll_id);
ALTER TABLE node_ll ALTER COLUMN NODE_LL_ID SET NOT NULL;


CREATE TABLE computed_lane (
	computed_lane_id bigint NOT NULL,
	lane_id bigint NOT NULL,
	offset_small_x bigint NOT NULL,
	offset_large_x bigint NOT NULL,
	offset_small_y bigint NOT NULL,
	offset_large_y bigint NOT NULL,
	angle bigint,
	x_scale bigint,
	y_scale bigint
) ;
ALTER TABLE computed_lane ADD PRIMARY KEY (computed_lane_id);
ALTER TABLE computed_lane ALTER COLUMN COMPUTED_LANE_ID SET NOT NULL;
ALTER TABLE computed_lane ALTER COLUMN LANE_ID SET NOT NULL;
ALTER TABLE computed_lane ALTER COLUMN OFFSET_SMALL_X SET NOT NULL;
ALTER TABLE computed_lane ALTER COLUMN OFFSET_LARGE_X SET NOT NULL;
ALTER TABLE computed_lane ALTER COLUMN OFFSET_SMALL_Y SET NOT NULL;
ALTER TABLE computed_lane ALTER COLUMN OFFSET_LARGE_Y SET NOT NULL;


CREATE TABLE rsu_firmware (
	firmware_id varchar(128) NOT NULL,
	firmware_file varchar(255) NOT NULL,
	update_process varchar(255),
	release_date timestamp(0) NOT NULL
) ;
ALTER TABLE rsu_firmware ADD PRIMARY KEY (firmware_id);
ALTER TABLE rsu_firmware ALTER COLUMN FIRMWARE_FILE SET NOT NULL;
ALTER TABLE rsu_firmware ALTER COLUMN RELEASE_DATE SET NOT NULL;


CREATE TABLE status_log (
	log_id bigint NOT NULL,
	status_time timestamp NOT NULL,
	entity_id integer NOT NULL,
	entity_type_id bigint NOT NULL,
	status_type_id bigint NOT NULL,
	status varchar(255)
) ;
ALTER TABLE status_log ADD PRIMARY KEY (log_id);
ALTER TABLE status_log ALTER COLUMN LOG_ID SET NOT NULL;
ALTER TABLE status_log ALTER COLUMN STATUS_TIME SET NOT NULL;
ALTER TABLE status_log ALTER COLUMN ENTITY_ID SET NOT NULL;
ALTER TABLE status_log ALTER COLUMN ENTITY_TYPE_ID SET NOT NULL;
ALTER TABLE status_log ALTER COLUMN STATUS_TYPE_ID SET NOT NULL;
ALTER TABLE status_log ADD CONSTRAINT entypetolog_fk FOREIGN KEY (entity_type_id) REFERENCES entity_type(entity_type_id) ON DELETE CASCADE NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE status_log ADD CONSTRAINT sttypetolog_fk FOREIGN KEY (status_type_id) REFERENCES status_type(status_type_id) ON DELETE CASCADE NOT DEFERRABLE INITIALLY IMMEDIATE;


CREATE TABLE itis_code (
	itis_code_id bigint NOT NULL,
	description varchar(255) NOT NULL,
	category_id bigint NOT NULL,
	itis_code bigint
) ;
ALTER TABLE itis_code ADD PRIMARY KEY (itis_code_id);
ALTER TABLE itis_code ALTER COLUMN ITIS_CODE_ID SET NOT NULL;
ALTER TABLE itis_code ALTER COLUMN DESCRIPTION SET NOT NULL;
ALTER TABLE itis_code ALTER COLUMN CATEGORY_ID SET NOT NULL;
ALTER TABLE itis_code ADD CONSTRAINT fk_category FOREIGN KEY (category_id) REFERENCES category(category_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;


CREATE TABLE tim (
	msg_cnt varchar(255),
	url_b varchar(255),
	time_stamp timestamp,
	record_generated_by varchar(20),
	rmd_ld_elevation double precision,
	rmd_ld_heading double precision,
	rmd_ld_latitude double precision,
	rmd_ld_longitude double precision,
	rmd_ld_speed double precision,
	rmd_rx_source varchar(5),
	schema_version bigint,
	valid_signature smallint,
	log_file_name varchar(255),
	record_generated_at timestamp,
	sanitized smallint,
	serial_id_stream_id varchar(255),
	serial_id_bundle_size bigint,
	serial_id_bundle_id bigint,
	serial_id_record_id bigint,
	serial_id_serial_number bigint,
	payload_type varchar(255),
	record_type varchar(255),
	ode_received_at timestamp,
	packet_id varchar(50),
	security_result_code bigint,
	is_satellite smallint,
	tim_id bigint NOT NULL,
	sat_record_id varchar(10),
	tim_name varchar(100)
) ;
CREATE UNIQUE INDEX tim_u ON tim (msg_cnt, rmd_ld_latitude, rmd_ld_longitude, rmd_ld_elevation, log_file_name, record_generated_at, time_stamp, packet_id);
ALTER TABLE tim ADD UNIQUE (msg_cnt,rmd_ld_latitude,rmd_ld_longitude,rmd_ld_elevation,log_file_name,record_generated_at,time_stamp,packet_id,tim_name);
ALTER TABLE tim ADD PRIMARY KEY (tim_id);
ALTER TABLE tim ADD CONSTRAINT fk_sec_result_code_type_tim FOREIGN KEY (security_result_code) REFERENCES security_result_code_type(security_result_code_type_id) ON DELETE CASCADE NOT DEFERRABLE INITIALLY IMMEDIATE;


CREATE TABLE shape_point (
	shape_point_id bigint NOT NULL,
	lane_width bigint,
	directionality bigint,
	node_type varchar(255),
	computed_lane_id bigint,
	position_lat double precision,
	position_long double precision,
	position_elev double precision
) ;
ALTER TABLE shape_point ADD PRIMARY KEY (shape_point_id);
ALTER TABLE shape_point ALTER COLUMN SHAPE_POINT_ID SET NOT NULL;
ALTER TABLE shape_point ADD CONSTRAINT fk_computed_lane_shape_point FOREIGN KEY (computed_lane_id) REFERENCES computed_lane(computed_lane_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;


CREATE TABLE path (
	path_id bigint NOT NULL,
	scale bigint,
	type varchar(255),
	computed_lane_id bigint
) ;
ALTER TABLE path ADD PRIMARY KEY (path_id);
ALTER TABLE path ALTER COLUMN PATH_ID SET NOT NULL;
ALTER TABLE path ADD CONSTRAINT fk_computed_lane_path FOREIGN KEY (computed_lane_id) REFERENCES computed_lane(computed_lane_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;


CREATE TABLE data_list (
	data_list_id bigint NOT NULL,
	node_xy_id bigint NOT NULL,
	path_endpoint_angle bigint NOT NULL,
	lane_crown_center bigint NOT NULL,
	lane_crown_left bigint NOT NULL,
	lane_crown_right bigint NOT NULL,
	lane_angle bigint NOT NULL
) ;
ALTER TABLE data_list ADD PRIMARY KEY (data_list_id);
ALTER TABLE data_list ALTER COLUMN DATA_LIST_ID SET NOT NULL;
ALTER TABLE data_list ALTER COLUMN NODE_XY_ID SET NOT NULL;
ALTER TABLE data_list ALTER COLUMN PATH_ENDPOINT_ANGLE SET NOT NULL;
ALTER TABLE data_list ALTER COLUMN LANE_CROWN_CENTER SET NOT NULL;
ALTER TABLE data_list ALTER COLUMN LANE_CROWN_LEFT SET NOT NULL;
ALTER TABLE data_list ALTER COLUMN LANE_CROWN_RIGHT SET NOT NULL;
ALTER TABLE data_list ALTER COLUMN LANE_ANGLE SET NOT NULL;
ALTER TABLE data_list ADD CONSTRAINT fk_node_xy_data_list FOREIGN KEY (node_xy_id) REFERENCES node_xy(node_xy_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;


CREATE TABLE rsu (
	rsu_id bigint NOT NULL,
	deviceid integer,
	update_username varchar(255) DEFAULT 'ADMIN',
	update_password varchar(128),
	firmware_id varchar(128)
) ;
ALTER TABLE rsu ADD PRIMARY KEY (rsu_id);
ALTER TABLE rsu ALTER COLUMN RSU_ID SET NOT NULL;
ALTER TABLE rsu ADD CONSTRAINT fk_rsu2firmware FOREIGN KEY (firmware_id) REFERENCES rsu_firmware(firmware_id) ON DELETE SET NULL NOT DEFERRABLE INITIALLY IMMEDIATE;


CREATE TABLE incident_action_lut (
	description varchar(60),
	code varchar(10),
	itis_code_id bigint,
	incident_action_lut_id bigint NOT NULL
) ;
ALTER TABLE incident_action_lut ADD PRIMARY KEY (incident_action_lut_id);
ALTER TABLE incident_action_lut ADD CONSTRAINT fk_itis_code_incident_action FOREIGN KEY (itis_code_id) REFERENCES itis_code(itis_code_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;


CREATE TABLE incident_effect_lut (
	description varchar(50),
	code varchar(20),
	itis_code_id bigint,
	incident_effect_lut_id bigint NOT NULL
) ;
ALTER TABLE incident_effect_lut ADD PRIMARY KEY (incident_effect_lut_id);
ALTER TABLE incident_effect_lut ADD CONSTRAINT fk_itis_code_incident_effect FOREIGN KEY (itis_code_id) REFERENCES itis_code(itis_code_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;


CREATE TABLE incident_problem_lut (
	description varchar(50),
	code varchar(20),
	itis_code_id bigint,
	incident_problem_lut_id bigint NOT NULL
) ;
ALTER TABLE incident_problem_lut ADD PRIMARY KEY (incident_problem_lut_id);
ALTER TABLE incident_problem_lut ADD CONSTRAINT fk_itis_code_incident_problem FOREIGN KEY (itis_code_id) REFERENCES itis_code(itis_code_id) ON DELETE CASCADE NOT DEFERRABLE INITIALLY IMMEDIATE;


CREATE TABLE active_tim (
	tim_id bigint,
	milepost_start double precision,
	milepost_stop double precision,
	direction varchar(50),
	tim_start timestamp,
	tim_end timestamp,
	tim_type_id bigint,
	route varchar(255),
	client_id varchar(255),
	sat_record_id varchar(8),
	pk bigint,
	active_tim_id bigint NOT NULL,
	start_latitude double precision,
	start_longitude double precision,
	end_latitude double precision,
	end_longitude double precision,
	project_key bigint,
	expiration_date timestamp,
	marked_for_deletion boolean DEFAULT false
) ;
ALTER TABLE active_tim ADD PRIMARY KEY (active_tim_id);
ALTER TABLE active_tim ADD CONSTRAINT fk_tim_active_tim FOREIGN KEY (tim_id) REFERENCES tim(tim_id) ON DELETE CASCADE NOT DEFERRABLE INITIALLY IMMEDIATE;


CREATE TABLE data_frame (
	data_frame_id bigint NOT NULL,
	tim_id bigint NOT NULL,
	ssp_tim_rights integer,
	frame_type bigint,
	msg_id varchar(255),
	further_info_id varchar(255),
	view_angle varchar(255),
	mutcd bigint,
	crc varchar(255),
	duration_time bigint,
	priority bigint,
	ssp_location_rights integer,
	ssp_msg_types integer,
	ssp_msg_content integer,
	content varchar(255),
	url varchar(255),
	position_lat double precision,
	position_long double precision,
	position_elev double precision,
	start_date_time timestamp
) ;
CREATE INDEX df_tim_id_ix ON data_frame (tim_id);
ALTER TABLE data_frame ADD PRIMARY KEY (data_frame_id);
ALTER TABLE data_frame ALTER COLUMN DATA_FRAME_ID SET NOT NULL;
ALTER TABLE data_frame ALTER COLUMN TIM_ID SET NOT NULL;
ALTER TABLE data_frame ADD CONSTRAINT fk_tim_df FOREIGN KEY (tim_id) REFERENCES tim(tim_id) ON DELETE CASCADE NOT DEFERRABLE INITIALLY IMMEDIATE;


CREATE TABLE path_node_ll (
	path_node_ll_id bigint NOT NULL,
	path_id bigint NOT NULL,
	node_ll_id bigint NOT NULL
) ;
ALTER TABLE path_node_ll ADD PRIMARY KEY (path_node_ll_id);
ALTER TABLE path_node_ll ALTER COLUMN PATH_NODE_LL_ID SET NOT NULL;
ALTER TABLE path_node_ll ALTER COLUMN PATH_ID SET NOT NULL;
ALTER TABLE path_node_ll ALTER COLUMN NODE_LL_ID SET NOT NULL;
ALTER TABLE path_node_ll ADD CONSTRAINT fk_path_node_ll_node_ll FOREIGN KEY (node_ll_id) REFERENCES node_ll(node_ll_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE path_node_ll ADD CONSTRAINT fk_path_node_ll_path FOREIGN KEY (path_id) REFERENCES path(path_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;


CREATE TABLE path_node_xy (
	path_node_xy_id bigint NOT NULL,
	path_id bigint NOT NULL,
	node_xy_id bigint NOT NULL
) ;
ALTER TABLE path_node_xy ADD PRIMARY KEY (path_node_xy_id);
ALTER TABLE path_node_xy ALTER COLUMN PATH_NODE_XY_ID SET NOT NULL;
ALTER TABLE path_node_xy ALTER COLUMN PATH_ID SET NOT NULL;
ALTER TABLE path_node_xy ALTER COLUMN NODE_XY_ID SET NOT NULL;
ALTER TABLE path_node_xy ADD CONSTRAINT fk_node_xy_path FOREIGN KEY (node_xy_id) REFERENCES node_xy(node_xy_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE path_node_xy ADD CONSTRAINT fk_path_node_xy FOREIGN KEY (path_id) REFERENCES path(path_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;


CREATE TABLE shape_point_node_xy (
	shape_point_node_xy_id bigint NOT NULL,
	shape_point_id bigint NOT NULL,
	node_xy_id bigint NOT NULL
) ;
ALTER TABLE shape_point_node_xy ADD PRIMARY KEY (shape_point_node_xy_id);
ALTER TABLE shape_point_node_xy ALTER COLUMN SHAPE_POINT_NODE_XY_ID SET NOT NULL;
ALTER TABLE shape_point_node_xy ALTER COLUMN SHAPE_POINT_ID SET NOT NULL;
ALTER TABLE shape_point_node_xy ALTER COLUMN NODE_XY_ID SET NOT NULL;
ALTER TABLE shape_point_node_xy ADD CONSTRAINT fk_node_xy_shape_point FOREIGN KEY (node_xy_id) REFERENCES node_xy(node_xy_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE shape_point_node_xy ADD CONSTRAINT fk_shape_point_node_xy FOREIGN KEY (shape_point_id) REFERENCES shape_point(shape_point_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;


CREATE TABLE old_region (
	old_region_id bigint NOT NULL,
	direction varchar(255) NOT NULL,
	extent bigint,
	area varchar(255) NOT NULL,
	circle_radius bigint NOT NULL,
	circle_units bigint NOT NULL,
	shape_point_id bigint,
	region_point_scale bigint,
	circle_position_lat double precision,
	circle_position_long double precision,
	circle_position_elev double precision,
	region_point_lat double precision,
	region_point_long double precision,
	region_point_elev double precision
) ;
ALTER TABLE old_region ADD PRIMARY KEY (old_region_id);
ALTER TABLE old_region ALTER COLUMN OLD_REGION_ID SET NOT NULL;
ALTER TABLE old_region ALTER COLUMN DIRECTION SET NOT NULL;
ALTER TABLE old_region ALTER COLUMN AREA SET NOT NULL;
ALTER TABLE old_region ALTER COLUMN CIRCLE_RADIUS SET NOT NULL;
ALTER TABLE old_region ALTER COLUMN CIRCLE_UNITS SET NOT NULL;
ALTER TABLE old_region ADD CONSTRAINT fk_shape_point FOREIGN KEY (shape_point_id) REFERENCES shape_point(shape_point_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;


CREATE TABLE tim_rsu (
	tim_rsu_id bigint NOT NULL,
	rsu_id bigint NOT NULL,
	tim_id bigint NOT NULL,
	rsu_index bigint
) ;
ALTER TABLE tim_rsu ADD UNIQUE (rsu_id,tim_id,rsu_index);
ALTER TABLE tim_rsu ADD PRIMARY KEY (tim_rsu_id);
ALTER TABLE tim_rsu ALTER COLUMN TIM_RSU_ID SET NOT NULL;
ALTER TABLE tim_rsu ALTER COLUMN RSU_ID SET NOT NULL;
ALTER TABLE tim_rsu ALTER COLUMN TIM_ID SET NOT NULL;
ALTER TABLE tim_rsu ADD CONSTRAINT sys_c0021538 FOREIGN KEY (rsu_id) REFERENCES rsu(rsu_id) ON DELETE CASCADE NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE tim_rsu ADD CONSTRAINT sys_c0021539 FOREIGN KEY (tim_id) REFERENCES tim(tim_id) ON DELETE CASCADE NOT DEFERRABLE INITIALLY IMMEDIATE;


-- dependent level 3 (dependent on level 2 tables and possibly level 1/level 0 tables) --
CREATE TABLE data_frame_itis_code (
	data_frame_itis_code_id bigint NOT NULL,
	itis_code_id bigint,
	data_frame_id bigint NOT NULL,
	text varchar(250),
	position smallint
) ;
ALTER TABLE data_frame_itis_code ADD PRIMARY KEY (data_frame_itis_code_id);
ALTER TABLE data_frame_itis_code ALTER COLUMN DATA_FRAME_ITIS_CODE_ID SET NOT NULL;
ALTER TABLE data_frame_itis_code ALTER COLUMN DATA_FRAME_ID SET NOT NULL;
ALTER TABLE data_frame_itis_code ADD CONSTRAINT fk_data_frame_itis_code FOREIGN KEY (data_frame_id) REFERENCES data_frame(data_frame_id) ON DELETE CASCADE NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE data_frame_itis_code ADD CONSTRAINT fk_itis_code FOREIGN KEY (itis_code_id) REFERENCES itis_code(itis_code_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;


CREATE TABLE region (
	region_id bigint NOT NULL,
	data_frame_id bigint NOT NULL,
	name varchar(255),
	regulator_id bigint,
	segment_id bigint,
	lane_width bigint,
	directionality bigint,
	direction varchar(255),
	region_type varchar(255),
	description varchar(255),
	path_id bigint,
	old_region_id bigint,
	geometry_direction varchar(255),
	geometry_extent bigint,
	geometry_lane_width bigint,
	anchor_elev double precision,
	anchor_lat double precision,
	anchor_long double precision,
	geometry_circle_position_lat double precision,
	geometry_circle_position_long double precision,
	geometry_circle_position_elev double precision,
	geometry_circle_radius bigint,
	geometry_circle_units bigint,
	closed_path smallint
) ;
CREATE INDEX p_df_id_ix ON region (path_id);
CREATE INDEX r_df_id_ix ON region (data_frame_id);
ALTER TABLE region ADD PRIMARY KEY (region_id);
ALTER TABLE region ALTER COLUMN REGION_ID SET NOT NULL;
ALTER TABLE region ALTER COLUMN DATA_FRAME_ID SET NOT NULL;
ALTER TABLE region ADD CONSTRAINT fk_data_frame FOREIGN KEY (data_frame_id) REFERENCES data_frame(data_frame_id) ON DELETE CASCADE NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE region ADD CONSTRAINT fk_old_region FOREIGN KEY (old_region_id) REFERENCES old_region(old_region_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE region ADD CONSTRAINT fk_path FOREIGN KEY (path_id) REFERENCES path(path_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;


CREATE TABLE region_list (
	region_list_id bigint NOT NULL,
	old_region_id bigint NOT NULL,
	x_offset bigint NOT NULL,
	y_offset bigint NOT NULL,
	z_offset bigint NOT NULL
) ;
ALTER TABLE region_list ADD PRIMARY KEY (region_list_id);
ALTER TABLE region_list ALTER COLUMN REGION_LIST_ID SET NOT NULL;
ALTER TABLE region_list ALTER COLUMN OLD_REGION_ID SET NOT NULL;
ALTER TABLE region_list ALTER COLUMN X_OFFSET SET NOT NULL;
ALTER TABLE region_list ALTER COLUMN Y_OFFSET SET NOT NULL;
ALTER TABLE region_list ALTER COLUMN Z_OFFSET SET NOT NULL;
ALTER TABLE region_list ADD CONSTRAINT fk_old_region_region_list FOREIGN KEY (old_region_id) REFERENCES old_region(old_region_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;
