SET client_encoding TO 'UTF8';


-- \set ON_ERROR_STOP ON

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


CREATE TABLE excluded_road_codes (
	road_code varchar(50)
) ;


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


CREATE TABLE mileposts_temp (
	common_name varchar(20),
	direction varchar(1),
	milepost decimal(38,8),
	latitude decimal(38,8),
	longitude decimal(38,8),
	road_code varchar(50),
	lrs_route varchar(20)
) ;


CREATE TABLE notifications (
	log_id bigint NOT NULL,
	notification_time timestamp NOT NULL,
	entity_id integer NOT NULL
) ;
ALTER TABLE notifications ADD PRIMARY KEY (log_id);
ALTER TABLE notifications ALTER COLUMN LOG_ID SET NOT NULL;
ALTER TABLE notifications ALTER COLUMN NOTIFICATION_TIME SET NOT NULL;
ALTER TABLE notifications ALTER COLUMN ENTITY_ID SET NOT NULL;


CREATE TABLE obu_firmware (
	firmware_id bigint NOT NULL,
	update_file bytea,
	version_number varchar(255),
	active smallint
) ;
ALTER TABLE obu_firmware ADD PRIMARY KEY (firmware_id);
ALTER TABLE obu_firmware ALTER COLUMN FIRMWARE_ID SET NOT NULL;


CREATE TABLE rw_buffer_action_lut (
	description varchar(50),
	code varchar(20),
	rw_buffer_action_lut_id bigint NOT NULL
) ;
ALTER TABLE rw_buffer_action_lut ADD PRIMARY KEY (rw_buffer_action_lut_id);


CREATE TABLE sw_application (
	application_id integer NOT NULL,
	application_group_id bigint NOT NULL,
	description varchar(255)
) ;
ALTER TABLE sw_application ADD PRIMARY KEY (application_id);
ALTER TABLE sw_application ALTER COLUMN APPLICATION_ID SET NOT NULL;
ALTER TABLE sw_application ALTER COLUMN APPLICATION_GROUP_ID SET NOT NULL;


CREATE TABLE tim_type (
	type varchar(10),
	description varchar(255),
	tim_type_id bigint NOT NULL
) ;
ALTER TABLE tim_type ADD PRIMARY KEY (tim_type_id);


CREATE TABLE tokens (
	id bigint NOT NULL,
	payload varchar(255) NOT NULL
) ;
ALTER TABLE tokens ADD PRIMARY KEY (id);
ALTER TABLE tokens ALTER COLUMN ID SET NOT NULL;
ALTER TABLE tokens ALTER COLUMN PAYLOAD SET NOT NULL;


-- dependent level 0 (dependent on nothing but is depended on) --
CREATE TABLE hmi_log_type (
	hmi_log_type_id smallint NOT NULL,
	short_name varchar(20),
	description varchar(255)
) ;
ALTER TABLE hmi_log_type ADD PRIMARY KEY (hmi_log_type_id);
ALTER TABLE hmi_log_type ALTER COLUMN HMI_LOG_TYPE_ID SET NOT NULL;


-- CREATE TABLE poc_bsm_core ( -- SKIPPING THIS TABLE, WE'RE NOT WORRYING ABOUT BSMs
-- 	bsm_core_data_id numeric(38) NOT NULL,
-- 	record_generated_at timestamp
-- ) PARTITION BY RANGE (record_generated_at) ;
-- ALTER TABLE poc_bsm_core ADD PRIMARY KEY (bsm_core_data_id,record_generated_at);


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


CREATE TABLE driver_alert_type (
	short_name varchar(10),
	description varchar(255),
	driver_alert_type_id bigint NOT NULL
) ;
ALTER TABLE driver_alert_type ADD PRIMARY KEY (driver_alert_type_id);


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


CREATE TABLE trac_message_type (
	trac_message_type varchar(50),
	trac_message_description varchar(255),
	trac_message_type_id bigint NOT NULL
) ;
ALTER TABLE trac_message_type ADD PRIMARY KEY (trac_message_type_id);


-- dependent level 1 (dependent on level 0 tables only) --
CREATE TABLE hmi_log (
	hmi_log_type_id smallint NOT NULL,
	position_lat double precision,
	position_long double precision,
	position_elv_m double precision,
	log_file_name varchar(255),
	record_generated_at timestamp,
	log_level varchar(20),
	log_message varchar(2000),
	received_at timestamp,
	hmi_log_id bigint NOT NULL
) ;
ALTER TABLE hmi_log ADD PRIMARY KEY (hmi_log_id);
ALTER TABLE hmi_log ALTER COLUMN HMI_LOG_TYPE_ID SET NOT NULL;
ALTER TABLE hmi_log ADD CONSTRAINT hmi_log_fk1 FOREIGN KEY (hmi_log_type_id) REFERENCES hmi_log_type(hmi_log_type_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;


-- CREATE TABLE poc_bsm_child ( -- SKIPPING THIS TABLE, WE'RE NOT WORRYING ABOUT BSMs
-- 	bsm_core_data_id numeric(38) NOT NULL,
-- 	description varchar(50)
-- ) -- Unsupported partition type, please check
--  ;
-- ALTER TABLE poc_bsm_child ALTER COLUMN BSM_CORE_DATA_ID SET NOT NULL;
-- ALTER TABLE poc_bsm_child ADD CONSTRAINT child_fk FOREIGN KEY (bsm_core_data_id) REFERENCES poc_bsm_core(bsm_core_data_id) ON DELETE CASCADE NOT DEFERRABLE INITIALLY IMMEDIATE;
-- ALTER TABLE poc_bsm_child ADD CONSTRAINT child_uk UNIQUE (bsm_core_data_id, description); -- manually added


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


-- CREATE TABLE bsm_core_data ( -- SKIPPING THIS TABLE, WE'RE NOT WORRYING ABOUT BSMs
-- 	bsm_core_data_id bigint NOT NULL,
-- 	id varchar(255),
-- 	msgcnt bigint,
-- 	secmark bigint,
-- 	position_lat decimal(30,8),
-- 	position_long decimal(20,8),
-- 	position_elev decimal(30,8),
-- 	accelset_accellat decimal(20,8),
-- 	accelset_accellong decimal(20,8),
-- 	accelset_accelvert double precision,
-- 	accelset_accelyaw double precision,
-- 	accuracy_semimajor double precision,
-- 	accuracy_semiminor double precision,
-- 	accuracy_orientation double precision,
-- 	transmission varchar(20),
-- 	speed double precision,
-- 	heading double precision,
-- 	angle double precision,
-- 	brakes_wheelbrakes varchar(1000),
-- 	brakes_traction varchar(255),
-- 	brakes_abs varchar(255),
-- 	brakes_scs varchar(255),
-- 	brakes_brakeboost varchar(255),
-- 	brakes_auxbrakes varchar(255),
-- 	size_length bigint,
-- 	size_width bigint,
-- 	log_file_name varchar(255),
-- 	record_generated_at timestamp,
-- 	sanitized smallint,
-- 	serial_id_stream_id varchar(255),
-- 	serial_id_bundle_size bigint,
-- 	serial_id_bundle_id bigint,
-- 	serial_id_record_id bigint,
-- 	serial_id_serial_number bigint,
-- 	ode_received_at timestamp,
-- 	record_type varchar(255),
-- 	payload_type varchar(255),
-- 	schema_version bigint,
-- 	record_generated_by varchar(5),
-- 	security_result_code bigint,
-- 	bsm_source varchar(5)
-- ) PARTITION BY RANGE (record_generated_at) ;
-- CREATE INDEX position ON bsm_core_data (position_lat, position_long);
-- CREATE INDEX record_generated_at ON bsm_core_data (record_generated_at);
-- ALTER TABLE bsm_core_data ADD PRIMARY KEY (bsm_core_data_id,record_generated_at);
-- ALTER TABLE bsm_core_data ADD UNIQUE (msgcnt,secmark,position_lat,position_long,position_elev,record_generated_at,log_file_name,bsm_source);
-- ALTER TABLE bsm_core_data ALTER COLUMN BSM_CORE_DATA_ID SET NOT NULL;
-- ALTER TABLE bsm_core_data ADD CONSTRAINT fk_sec_result_code_type_bsm FOREIGN KEY (security_result_code) REFERENCES security_result_code_type(security_result_code_type_id) ON DELETE CASCADE NOT DEFERRABLE INITIALLY IMMEDIATE;


CREATE TABLE driver_alert (
	latitude double precision,
	longitude double precision,
	heading double precision,
	elevation_m double precision,
	speed double precision,
	driver_alert_type_id bigint,
	log_file_name varchar(255),
	record_type varchar(255),
	payload_type varchar(255),
	serial_id_stream_id varchar(255),
	serial_id_bundle_size bigint,
	serial_id_bundle_id bigint,
	serial_id_record_id bigint,
	serial_id_serial_number bigint,
	ode_received_at timestamp,
	schema_version bigint,
	record_generated_at timestamp,
	record_generated_by varchar(5),
	valid_signature smallint,
	sanitized smallint,
	security_result_code bigint,
	driver_alert_id bigint NOT NULL
) ;
ALTER TABLE driver_alert ADD UNIQUE (latitude,longitude,log_file_name,heading,record_generated_at,elevation_m,speed);
ALTER TABLE driver_alert ADD PRIMARY KEY (driver_alert_id);
ALTER TABLE driver_alert ADD CONSTRAINT fk_driver_alert_type FOREIGN KEY (driver_alert_type_id) REFERENCES driver_alert_type(driver_alert_type_id) ON DELETE CASCADE NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE driver_alert ADD CONSTRAINT fk_sec_result_code_type_da FOREIGN KEY (security_result_code) REFERENCES security_result_code_type(security_result_code_type_id) ON DELETE CASCADE NOT DEFERRABLE INITIALLY IMMEDIATE;


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


CREATE TABLE disabled_list (
	disabled_list_id bigint NOT NULL,
	node_xy_id bigint NOT NULL,
	type bigint NOT NULL
) ;
ALTER TABLE disabled_list ADD PRIMARY KEY (disabled_list_id);
ALTER TABLE disabled_list ALTER COLUMN DISABLED_LIST_ID SET NOT NULL;
ALTER TABLE disabled_list ALTER COLUMN NODE_XY_ID SET NOT NULL;
ALTER TABLE disabled_list ALTER COLUMN TYPE SET NOT NULL;
ALTER TABLE disabled_list ADD CONSTRAINT fk_node_xy_disabled_list FOREIGN KEY (node_xy_id) REFERENCES node_xy(node_xy_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;


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


CREATE TABLE enabled_list (
	enabled_list_id bigint NOT NULL,
	node_xy_id bigint NOT NULL,
	type bigint NOT NULL
) ;
ALTER TABLE enabled_list ADD PRIMARY KEY (enabled_list_id);
ALTER TABLE enabled_list ALTER COLUMN ENABLED_LIST_ID SET NOT NULL;
ALTER TABLE enabled_list ALTER COLUMN NODE_XY_ID SET NOT NULL;
ALTER TABLE enabled_list ALTER COLUMN TYPE SET NOT NULL;
ALTER TABLE enabled_list ADD CONSTRAINT fk_node_xy_enabled_list FOREIGN KEY (node_xy_id) REFERENCES node_xy(node_xy_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;


CREATE TABLE local_node (
	local_node_id bigint NOT NULL,
	node_xy_id bigint NOT NULL,
	type bigint NOT NULL
) ;
ALTER TABLE local_node ADD PRIMARY KEY (local_node_id);
ALTER TABLE local_node ALTER COLUMN LOCAL_NODE_ID SET NOT NULL;
ALTER TABLE local_node ALTER COLUMN NODE_XY_ID SET NOT NULL;
ALTER TABLE local_node ALTER COLUMN TYPE SET NOT NULL;
ALTER TABLE local_node ADD CONSTRAINT fk_node_xy_local_node FOREIGN KEY (node_xy_id) REFERENCES node_xy(node_xy_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;


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


CREATE TABLE trac_message_sent (
	trac_message_type_id bigint,
	date_time_sent timestamp,
	message_text varchar(500),
	packet_id varchar(50),
	trac_message_sent_id bigint NOT NULL,
	rest_response_code bigint,
	rest_response_message varchar(2000),
	message_sent smallint NOT NULL DEFAULT 1,
	email_sent smallint NOT NULL DEFAULT 0
) ;
ALTER TABLE trac_message_sent ADD PRIMARY KEY (trac_message_sent_id);
ALTER TABLE trac_message_sent ALTER COLUMN MESSAGE_SENT SET NOT NULL;
ALTER TABLE trac_message_sent ALTER COLUMN EMAIL_SENT SET NOT NULL;
ALTER TABLE trac_message_sent ADD CONSTRAINT trac_message_sent_chk1 CHECK (message_sent IN (1,0));
ALTER TABLE trac_message_sent ADD CONSTRAINT trac_message_sent_chk2 CHECK (email_sent IN (1,0));
ALTER TABLE trac_message_sent ADD CONSTRAINT fk_trac_message_type FOREIGN KEY (trac_message_type_id) REFERENCES trac_message_type(trac_message_type_id) ON DELETE CASCADE NOT DEFERRABLE INITIALLY IMMEDIATE;


-- dependent level 2 (dependent on level 1 tables and possibly level 0 tables) --
-- CREATE TABLE bsm_part2_spve ( -- SKIPPING THIS TABLE, WE'RE NOT WORRYING ABOUT BSMs
-- 	bsm_core_data_id bigint NOT NULL,
-- 	id varchar(255),
-- 	va_ssprights bigint,
-- 	va_events varchar(1000),
-- 	va_events_ssprights bigint,
-- 	va_lightsuse varchar(20),
-- 	va_multi varchar(20),
-- 	va_responsetype varchar(25),
-- 	va_sirenuse varchar(20),
-- 	desc_description varchar(1000),
-- 	desc_extent varchar(25),
-- 	desc_heading varchar(1000),
-- 	desc_priority varchar(255),
-- 	desc_regional varchar(1000),
-- 	desc_typeevent bigint,
-- 	tr_conn_pivotoffset numeric(38),
-- 	tr_conn_pivotangle numeric(38),
-- 	tr_conn_pivots smallint,
-- 	tr_ssprights bigint,
-- 	tr_units varchar(1000),
-- 	bsm_part2_spve_id bigint NOT NULL
-- ) ;
-- CREATE INDEX ix_bsmpart2spve_bsmcoredataid ON bsm_part2_spve (bsm_core_data_id);
-- ALTER TABLE bsm_part2_spve ADD PRIMARY KEY (bsm_part2_spve_id);
-- ALTER TABLE bsm_part2_spve ALTER COLUMN BSM_CORE_DATA_ID SET NOT NULL;
-- ALTER TABLE bsm_part2_spve ADD CONSTRAINT fk_bsm_core_data_spve FOREIGN KEY (bsm_core_data_id) REFERENCES bsm_core_data(bsm_core_data_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;


-- CREATE TABLE bsm_part2_suve ( -- SKIPPING THIS TABLE, WE'RE NOT WORRYING ABOUT BSMs
-- 	bsm_part2_suve_id bigint NOT NULL,
-- 	bsm_core_data_id bigint NOT NULL,
-- 	id varchar(255),
-- 	classification bigint,
-- 	cd_fueltype varchar(20),
-- 	cd_hpmstype varchar(20),
-- 	cd_iso3883 bigint,
-- 	cd_keytype bigint,
-- 	cd_regional varchar(1000),
-- 	cd_respondertype varchar(50),
-- 	cd_responseequip_name varchar(255),
-- 	cd_responseequip_value bigint,
-- 	cd_role varchar(20),
-- 	cd_vehicletype_name varchar(255),
-- 	cd_vehicletype_value bigint,
-- 	vd_bumpers_front double precision,
-- 	vd_bumpers_rear double precision,
-- 	vd_height double precision,
-- 	vd_mass bigint,
-- 	vd_trailerweight bigint,
-- 	wr_friction bigint,
-- 	wr_israining varchar(10),
-- 	wr_precipsituation varchar(30),
-- 	wr_rainrate double precision,
-- 	wr_roadfriction double precision,
-- 	wr_solarradiation bigint,
-- 	wp_airpressure bigint,
-- 	wp_airtemp bigint,
-- 	wp_rainrates_ratefront bigint,
-- 	wp_rainrates_raterear bigint,
-- 	wp_rainrates_statusfront varchar(20),
-- 	wp_rainrates_statusrear varchar(20),
-- 	ob_datetime timestamp(0),
-- 	ob_description bigint,
-- 	ob_locationdetails_name varchar(255),
-- 	ob_locationdetails_value bigint,
-- 	ob_obdirect numeric(38),
-- 	ob_obdist bigint,
-- 	ob_vertevent varchar(1000),
-- 	st_statusdetails bigint,
-- 	st_locationdetails_name varchar(255),
-- 	st_locationdetails_value bigint,
-- 	sp_speedreports varchar(1000),
-- 	rtcm_msgs varchar(1000),
-- 	rtcm_header_antoffsetx double precision,
-- 	rtcm_header_antoffsety double precision,
-- 	rtcm_header_antoffsetz double precision,
-- 	rtcm_rtcmheader_status varchar(1000),
-- 	regional varchar(1000)
-- ) ;
-- CREATE INDEX ix_bsmpart2suve_bsmcoredataid ON bsm_part2_suve (bsm_core_data_id);
-- ALTER TABLE bsm_part2_suve ALTER COLUMN BSM_PART2_SUVE_ID SET NOT NULL;
-- ALTER TABLE bsm_part2_suve ALTER COLUMN BSM_CORE_DATA_ID SET NOT NULL;
-- ALTER TABLE bsm_part2_suve ADD CONSTRAINT fk_bsm_core_data_suve FOREIGN KEY (bsm_core_data_id) REFERENCES bsm_core_data(bsm_core_data_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;


-- CREATE TABLE bsm_part2_vse ( -- SKIPPING THIS TABLE, WE'RE NOT WORRYING ABOUT BSMs
-- 	bsm_part2_vse_id bigint NOT NULL,
-- 	bsm_core_data_id bigint NOT NULL,
-- 	id varchar(255),
-- 	events varchar(1000),
-- 	ph_initpos_lat double precision,
-- 	ph_initpos_long double precision,
-- 	ph_initpos_elev double precision,
-- 	ph_initpos_heading double precision,
-- 	ph_initpos_posaccrcy_semimaj double precision,
-- 	ph_initpos_posaccrcy_semimin double precision,
-- 	ph_initpos_posaccrcy_orien double precision,
-- 	ph_initpos_posconfidence_pos varchar(20),
-- 	ph_initpos_speed double precision,
-- 	ph_initpos_transmission varchar(20),
-- 	ph_initpos_speedconf_heading varchar(20),
-- 	ph_initpos_speedconf_speed varchar(20),
-- 	ph_initpos_speedconf_throttle varchar(20),
-- 	ph_initpos_timeconf varchar(30),
-- 	ph_initpos_utctime_day double precision,
-- 	ph_initpos_utctime_hour double precision,
-- 	ph_initpos_utctime_minute double precision,
-- 	ph_initpos_utctime_month double precision,
-- 	ph_initpos_utctime_offset double precision,
-- 	ph_initpos_utctime_second double precision,
-- 	ph_initpos_utctime_year double precision,
-- 	ph_currgnssstatus varchar(1000),
-- 	ph_crumbdata varchar(4000),
-- 	pp_confidence double precision,
-- 	pp_radiusofcurve double precision,
-- 	lights varchar(1000),
-- 	ph_initpos_posconfidence_elev varchar(20)
-- ) ;
-- CREATE INDEX ix_bsmpart2vse_bsmcoredataid ON bsm_part2_vse (bsm_core_data_id);
-- ALTER TABLE bsm_part2_vse ALTER COLUMN BSM_PART2_VSE_ID SET NOT NULL;
-- ALTER TABLE bsm_part2_vse ALTER COLUMN BSM_CORE_DATA_ID SET NOT NULL;
-- ALTER TABLE bsm_part2_vse ADD CONSTRAINT fk_bsm_core_data FOREIGN KEY (bsm_core_data_id) REFERENCES bsm_core_data(bsm_core_data_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;


CREATE TABLE driver_alert_itis_code (
	driver_alert_id bigint,
	itis_code_id bigint,
	driver_alert_itis_code_id bigint NOT NULL
) ;
ALTER TABLE driver_alert_itis_code ADD PRIMARY KEY (driver_alert_itis_code_id);
ALTER TABLE driver_alert_itis_code ADD CONSTRAINT fk_da_itis_code_it_fk FOREIGN KEY (itis_code_id) REFERENCES itis_code(itis_code_id) ON DELETE CASCADE NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE driver_alert_itis_code ADD CONSTRAINT fk_driver_alert FOREIGN KEY (driver_alert_id) REFERENCES driver_alert(driver_alert_id) ON DELETE CASCADE NOT DEFERRABLE INITIALLY IMMEDIATE;


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
	expiration_date timestamp
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


CREATE TABLE speed_limits (
	speed_limits_id bigint NOT NULL,
	data_list_id bigint NOT NULL,
	type bigint NOT NULL,
	velocity bigint NOT NULL
) ;
ALTER TABLE speed_limits ADD PRIMARY KEY (speed_limits_id);
ALTER TABLE speed_limits ALTER COLUMN SPEED_LIMITS_ID SET NOT NULL;
ALTER TABLE speed_limits ALTER COLUMN DATA_LIST_ID SET NOT NULL;
ALTER TABLE speed_limits ALTER COLUMN TYPE SET NOT NULL;
ALTER TABLE speed_limits ALTER COLUMN VELOCITY SET NOT NULL;
ALTER TABLE speed_limits ADD CONSTRAINT fk_data_list FOREIGN KEY (data_list_id) REFERENCES data_list(data_list_id) ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;


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
