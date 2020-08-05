--------------------------------------------------------
--  File created - Tuesday-August-04-2020   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for Sequence BSM_CORE_DATA_ID_SEQ
--------------------------------------------------------

   CREATE SEQUENCE  "CVCOMMS"."BSM_CORE_DATA_ID_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 49225383 CACHE 20 NOORDER  NOCYCLE   ;
--------------------------------------------------------
--  DDL for Table BSM_CORE_DATA
--------------------------------------------------------

  CREATE TABLE "CVCOMMS"."BSM_CORE_DATA" 
   (	"BSM_CORE_DATA_ID" NUMBER(10,0) NOT NULL CONSTRAINT "BSM_CORE_DATA_PK" PRIMARY KEY USING INDEX, 
	"ID" VARCHAR2(255 BYTE), 
	"MSGCNT" NUMBER(10,0), 
	"SECMARK" NUMBER(10,0), 
	"POSITION_LAT" NUMBER(30,8), 
	"POSITION_LONG" NUMBER(20,8), 
	"POSITION_ELEV" NUMBER(30,8), 
	"ACCELSET_ACCELLAT" NUMBER(20,8), 
	"ACCELSET_ACCELLONG" NUMBER(20,8), 
	"ACCELSET_ACCELVERT" NUMBER(15,5), 
	"ACCELSET_ACCELYAW" NUMBER(15,5), 
	"ACCURACY_SEMIMAJOR" NUMBER(15,5), 
	"ACCURACY_SEMIMINOR" NUMBER(15,5), 
	"ACCURACY_ORIENTATION" NUMBER(15,5), 
	"TRANSMISSION" VARCHAR2(20 BYTE), 
	"SPEED" NUMBER(15,5), 
	"HEADING" NUMBER(15,5), 
	"ANGLE" NUMBER(15,5), 
	"BRAKES_WHEELBRAKES" VARCHAR2(1000 BYTE), 
	"BRAKES_TRACTION" VARCHAR2(255 BYTE), 
	"BRAKES_ABS" VARCHAR2(255 BYTE), 
	"BRAKES_SCS" VARCHAR2(255 BYTE), 
	"BRAKES_BRAKEBOOST" VARCHAR2(255 BYTE), 
	"BRAKES_AUXBRAKES" VARCHAR2(255 BYTE), 
	"SIZE_LENGTH" NUMBER(10,0), 
	"SIZE_WIDTH" NUMBER(10,0), 
	"LOG_FILE_NAME" VARCHAR2(255 BYTE), 
	"RECORD_GENERATED_AT" TIMESTAMP (6), 
	"SANITIZED" NUMBER(1,0), 
	"SERIAL_ID_STREAM_ID" VARCHAR2(255 BYTE), 
	"SERIAL_ID_BUNDLE_SIZE" NUMBER(10,0), 
	"SERIAL_ID_BUNDLE_ID" NUMBER(10,0), 
	"SERIAL_ID_RECORD_ID" NUMBER(10,0), 
	"SERIAL_ID_SERIAL_NUMBER" NUMBER(10,0), 
	"ODE_RECEIVED_AT" TIMESTAMP (6), 
	"RECORD_TYPE" VARCHAR2(255 BYTE), 
	"PAYLOAD_TYPE" VARCHAR2(255 BYTE), 
	"SCHEMA_VERSION" NUMBER(10,0), 
	"RECORD_GENERATED_BY" VARCHAR2(5 BYTE), 
	"SECURITY_RESULT_CODE" NUMBER, 
	"BSM_SOURCE" VARCHAR2(5 BYTE)
   ) PARTITION BY RANGE (RECORD_GENERATED_AT) INTERVAL (NUMTODSINTERVAL(1, 'day'))
     (
         PARTITION part_01 VALUES LESS THAN (TO_TIMESTAMP('2020-01-01', 'YYYY-MM-DD'))
     ) 
     NO INMEMORY ;

--------------------------------------------------------
--  DDL for Index BSM_U
--------------------------------------------------------

  CREATE UNIQUE INDEX "CVCOMMS"."BSM_UI" ON "CVCOMMS"."BSM_CORE_DATA" ("MSGCNT", "SECMARK", "POSITION_LAT", "POSITION_LONG", "POSITION_ELEV", "RECORD_GENERATED_AT", "LOG_FILE_NAME") 
  ;
--------------------------------------------------------
--  DDL for Index BSM_U1
--------------------------------------------------------

  CREATE UNIQUE INDEX "CVCOMMS"."BSM_UI1" ON "CVCOMMS"."BSM_CORE_DATA" ("MSGCNT", "SECMARK", "POSITION_LAT", "POSITION_LONG", "POSITION_ELEV", "RECORD_GENERATED_AT", "LOG_FILE_NAME", "BSM_SOURCE") 
  ;
--------------------------------------------------------
--  DDL for Trigger TRG_BSM_CORE_DATA_ID
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE TRIGGER "CVCOMMS"."TRG_BSM_CORE_DATA_ID" 
before insert on bsm_core_data
for each row
begin
select bsm_core_data_id_seq.nextval
into :new.bsm_core_data_id
from dual;
end;

/
ALTER TRIGGER "CVCOMMS"."TRG_BSM_CORE_DATA_ID" ENABLE;
--------------------------------------------------------
--  DDL for Synonymn DUAL
--------------------------------------------------------

  CREATE OR REPLACE NONEDITIONABLE PUBLIC SYNONYM "DUAL" FOR "SYS"."DUAL";
--------------------------------------------------------
--  Constraints for Table BSM_CORE_DATA
--------------------------------------------------------

  ALTER TABLE "CVCOMMS"."BSM_CORE_DATA" ADD CONSTRAINT "UNIQUE_CONSTRAINT" UNIQUE ("MSGCNT", "SECMARK", "POSITION_LAT", "POSITION_LONG", "LOG_FILE_NAME", "POSITION_ELEV", "RECORD_GENERATED_AT") DISABLE;
  ALTER TABLE "CVCOMMS"."BSM_CORE_DATA" ADD CONSTRAINT "BSM_U" UNIQUE ("MSGCNT", "SECMARK", "POSITION_LAT", "POSITION_LONG", "POSITION_ELEV", "RECORD_GENERATED_AT", "LOG_FILE_NAME", "BSM_SOURCE");

--------------------------------------------------------
--  Ref Constraints for Table BSM_CORE_DATA
--------------------------------------------------------

  ALTER TABLE "CVCOMMS"."BSM_CORE_DATA" ADD CONSTRAINT "FK_SEC_RESULT_CODE_TYPE_BSM" FOREIGN KEY ("SECURITY_RESULT_CODE")
	  REFERENCES "CVCOMMS"."SECURITY_RESULT_CODE_TYPE" ("SECURITY_RESULT_CODE_TYPE_ID") ON DELETE CASCADE ENABLE;
