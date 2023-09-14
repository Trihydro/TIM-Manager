--------------------------------------------------------
--  File created - Wednesday-June-21-2023
--------------------------------------------------------
--------------------------------------------------------
--  Truncate script for TIM related tables
--  NOTE: This script is not able to rollback, we just truncate the tables
--------------------------------------------------------

TRUNCATE TABLE tim_rsu;
TRUNCATE TABLE DATA_FRAME_ITIS_CODE;
TRUNCATE TABLE path_node_ll;

ALTER TABLE path_node_ll disable constraint FK_PATH_NODE_LL_NODE_LL;
TRUNCATE TABLE node_ll;
ALTER TABLE path_node_ll enable constraint FK_PATH_NODE_LL_NODE_LL;

TRUNCATE TABLE region;

ALTER TABLE path_node_ll disable constraint FK_PATH_NODE_LL_PATH;
ALTER TABLE REGION disable constraint FK_PATH;
TRUNCATE TABLE path;
ALTER TABLE path_node_ll enable constraint FK_PATH_NODE_LL_PATH;
ALTER TABLE REGION enable constraint FK_PATH;

ALTER TABLE REGION disable constraint FK_DATA_FRAME;
ALTER TABLE DATA_FRAME_ITIS_CODE disable constraint FK_DATA_FRAME_ITIS_CODE;
TRUNCATE TABLE data_frame;
ALTER TABLE REGION enable constraint FK_DATA_FRAME;
ALTER TABLE DATA_FRAME_ITIS_CODE enable constraint FK_DATA_FRAME_ITIS_CODE;

TRUNCATE TABLE active_tim;

ALTER TABLE DATA_FRAME disable constraint FK_TIM_DF;
ALTER TABLE ACTIVE_TIM disable constraint FK_TIM_ACTIVE_TIM;
ALTER TABLE TIM_RSU disable constraint FK_TIM_RSU_TIM;
TRUNCATE TABLE tim;
ALTER TABLE DATA_FRAME enable constraint FK_TIM_DF;
ALTER TABLE ACTIVE_TIM enable constraint FK_TIM_ACTIVE_TIM;
ALTER TABLE TIM_RSU enable constraint FK_TIM_RSU_TIM;