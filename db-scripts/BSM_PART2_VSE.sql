--------------------------------------------------------
--  File created - Tuesday-August-04-2020   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for Sequence BSM_PART2_VSE_SEQ
--------------------------------------------------------

   CREATE SEQUENCE  "CVCOMMS"."BSM_PART2_VSE_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 36176046 CACHE 20 NOORDER  NOCYCLE   ;
--------------------------------------------------------
--  DDL for Table BSM_PART2_VSE
--------------------------------------------------------

  CREATE TABLE "CVCOMMS"."BSM_PART2_VSE" 
   (	"BSM_PART2_VSE_ID" NUMBER(10,0) NOT NULL, 
	"BSM_CORE_DATA_ID" NUMBER(10,0) NOT NULL, 
	"ID" VARCHAR2(255 BYTE), 
	"EVENTS" VARCHAR2(1000 BYTE), 
	"PH_INITPOS_LAT" NUMBER(15,5), 
	"PH_INITPOS_LONG" NUMBER(15,5), 
	"PH_INITPOS_ELEV" NUMBER(15,5), 
	"PH_INITPOS_HEADING" NUMBER(15,5), 
	"PH_INITPOS_POSACCRCY_SEMIMAJ" NUMBER(15,5), 
	"PH_INITPOS_POSACCRCY_SEMIMIN" NUMBER(15,5), 
	"PH_INITPOS_POSACCRCY_ORIEN" NUMBER(15,5), 
	"PH_INITPOS_POSCONFIDENCE_POS" VARCHAR2(20 BYTE), 
	"PH_INITPOS_SPEED" NUMBER(15,5), 
	"PH_INITPOS_TRANSMISSION" VARCHAR2(20 BYTE), 
	"PH_INITPOS_SPEEDCONF_HEADING" VARCHAR2(20 BYTE), 
	"PH_INITPOS_SPEEDCONF_SPEED" VARCHAR2(20 BYTE), 
	"PH_INITPOS_SPEEDCONF_THROTTLE" VARCHAR2(20 BYTE), 
	"PH_INITPOS_TIMECONF" VARCHAR2(30 BYTE), 
	"PH_INITPOS_UTCTIME_DAY" NUMBER(15,5), 
	"PH_INITPOS_UTCTIME_HOUR" NUMBER(15,5), 
	"PH_INITPOS_UTCTIME_MINUTE" NUMBER(15,5), 
	"PH_INITPOS_UTCTIME_MONTH" NUMBER(15,5), 
	"PH_INITPOS_UTCTIME_OFFSET" NUMBER(15,5), 
	"PH_INITPOS_UTCTIME_SECOND" NUMBER(15,5), 
	"PH_INITPOS_UTCTIME_YEAR" NUMBER(15,5), 
	"PH_CURRGNSSSTATUS" VARCHAR2(1000 BYTE), 
	"PH_CRUMBDATA" VARCHAR2(4000 BYTE), 
	"PP_CONFIDENCE" NUMBER(15,5), 
	"PP_RADIUSOFCURVE" NUMBER(15,5), 
	"LIGHTS" VARCHAR2(1000 BYTE), 
	"PH_INITPOS_POSCONFIDENCE_ELEV" VARCHAR2(20 BYTE),
    CONSTRAINT "FK_BSM_CORE_DATA" FOREIGN KEY ("BSM_CORE_DATA_ID") REFERENCES "CVCOMMS"."BSM_CORE_DATA" ("BSM_CORE_DATA_ID") ON DELETE CASCADE
   )  -- NO INMEMORY 
      -- PARTITION BY REFERENCE("FK_BSM_CORE_DATA");

--------------------------------------------------------
--  DDL for Index IX_BSMPART2VSE_BSMCOREDATAID
--------------------------------------------------------

  CREATE INDEX "CVCOMMS"."IX_BSMPART2VSE_BSMCOREDATAID" ON "CVCOMMS"."BSM_PART2_VSE" ("BSM_CORE_DATA_ID") 
  ;
--------------------------------------------------------
--  DDL for Trigger TRG_BSM_PART2_VSE_ID
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE TRIGGER "CVCOMMS"."TRG_BSM_PART2_VSE_ID" 
before insert on bsm_part2_vse
for each row
begin
select bsm_part2_vse_seq.nextval
into :new.bsm_part2_vse_id
from dual;
end;

/
ALTER TRIGGER "CVCOMMS"."TRG_BSM_PART2_VSE_ID" ENABLE;
--------------------------------------------------------
--  DDL for Synonymn DUAL
--------------------------------------------------------

  --CREATE OR REPLACE NONEDITIONABLE PUBLIC SYNONYM "DUAL" FOR "SYS"."DUAL";
--------------------------------------------------------
--  Constraints for Table BSM_PART2_VSE
--------------------------------------------------------
