-- Script to add EXPIRATION_DATE (nullable) to the active_tim_holding table 
-- This is needed to alleviate a race condition when updating expiration_date of existing active_tim records
ALTER TABLE "CVCOMMS"."ACTIVE_TIM_HOLDING" ADD EXPIRATION_DATE TIMESTAMP(6);
COMMIT;

ALTER TABLE "CVCOMMS"."ACTIVE_TIM_HOLDING" ADD PACKET_ID VARCHAR2(50 BYTE);
COMMIT;