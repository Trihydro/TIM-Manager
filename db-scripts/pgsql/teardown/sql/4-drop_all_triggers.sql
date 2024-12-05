DROP TRIGGER IF EXISTS active_tim_holding_trigger ON active_tim_holding CASCADE;
drop function if exists trigger_fct_active_tim_holding_trigger cascade;

DROP TRIGGER IF EXISTS active_tim_trigger ON active_tim CASCADE;
drop function if exists trigger_fct_active_tim_trigger cascade;

DROP TRIGGER IF EXISTS incident_action_lut_trigger ON incident_action_lut CASCADE;
drop function if exists trigger_fct_incident_action_lut_trigger cascade;

DROP TRIGGER IF EXISTS incident_effect_lut_trigger ON incident_effect_lut CASCADE;
drop function if exists trigger_fct_incident_effect_lut_trigger cascade;

DROP TRIGGER IF EXISTS incident_problem_lut_trigger ON incident_problem_lut CASCADE;
drop function if exists trigger_fct_incident_problem_lut_trigger cascade;

DROP TRIGGER IF EXISTS rw_buffer_action_lut_trigger ON rw_buffer_action_lut CASCADE;
drop function if exists trigger_fct_rw_buffer_action_lut_trigger cascade;

DROP TRIGGER IF EXISTS sec_result_code_type_trigger ON security_result_code_type CASCADE;
drop function if exists trigger_fct_sec_result_code_type_trigger cascade;

DROP TRIGGER IF EXISTS tim_trigger_ ON tim CASCADE;
drop function if exists trigger_fct_tim_trigger_ cascade;

DROP TRIGGER IF EXISTS tim_type_trigger ON tim_type CASCADE;
drop function if exists trigger_fct_tim_type_trigger cascade;

DROP TRIGGER IF EXISTS trg_category_id ON category CASCADE;
drop function if exists trigger_fct_trg_category_id cascade;

DROP TRIGGER IF EXISTS trg_computed_lane_id ON computed_lane CASCADE;
drop function if exists trigger_fct_trg_computed_lane_id cascade;

DROP TRIGGER IF EXISTS trg_data_frame_id ON data_frame CASCADE;
drop function if exists trigger_fct_trg_data_frame_id cascade;

DROP TRIGGER IF EXISTS trg_data_frame_itis_code_id ON data_frame_itis_code CASCADE;
drop function if exists trigger_fct_trg_data_frame_itis_code_id cascade;

DROP TRIGGER IF EXISTS trg_data_list_id ON data_list CASCADE;
drop function if exists trigger_fct_trg_data_list_id cascade;

DROP TRIGGER IF EXISTS trg_http_logging_id ON http_logging CASCADE;
drop function if exists trigger_fct_trg_http_logging_id cascade;

DROP TRIGGER IF EXISTS trg_itis_code_id ON itis_code CASCADE;
drop function if exists trigger_fct_trg_itis_code_id cascade;

DROP TRIGGER IF EXISTS trg_node_ll_id ON node_ll CASCADE;
drop function if exists trigger_fct_trg_node_ll_id cascade;

DROP TRIGGER IF EXISTS trg_node_xy_id ON node_xy CASCADE;
drop function if exists trigger_fct_trg_node_xy_id cascade;

DROP TRIGGER IF EXISTS trg_old_region_id ON old_region CASCADE;
drop function if exists trigger_fct_trg_old_region_id cascade;

DROP TRIGGER IF EXISTS trg_path_id ON path CASCADE;
drop function if exists trigger_fct_trg_path_id cascade;

DROP TRIGGER IF EXISTS trg_path_node_ll_id ON path_node_ll CASCADE;
drop function if exists trigger_fct_trg_path_node_ll_id cascade;

DROP TRIGGER IF EXISTS trg_path_node_xy_id ON path_node_xy CASCADE;
drop function if exists trigger_fct_trg_path_node_xy_id cascade;

DROP TRIGGER IF EXISTS trg_region_id ON region CASCADE;
drop function if exists trigger_fct_trg_region_id cascade;

DROP TRIGGER IF EXISTS trg_region_list_id ON region_list CASCADE;
drop function if exists trigger_fct_trg_region_list_id cascade;

DROP TRIGGER IF EXISTS trg_rsu_id ON rsu CASCADE;
drop function if exists trigger_fct_trg_rsu_id cascade;

DROP TRIGGER IF EXISTS trg_shape_point_id ON shape_point CASCADE;
drop function if exists trigger_fct_trg_shape_point_id cascade;

DROP TRIGGER IF EXISTS trg_shape_point_node_xy_id ON shape_point_node_xy CASCADE;
drop function if exists trigger_fct_trg_shape_point_node_xy_id cascade;

DROP TRIGGER IF EXISTS trg_status_log_id ON status_log CASCADE;
drop function if exists trigger_fct_trg_status_log_id cascade;

DROP TRIGGER IF EXISTS trg_tim_rsu_id ON tim_rsu CASCADE;
drop function if exists trigger_fct_trg_tim_rsu_id cascade;