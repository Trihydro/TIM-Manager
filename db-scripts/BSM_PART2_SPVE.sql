--------------------------------------------------------
--  File created - Tuesday-August-04-2020   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for Sequence BSM_PART2_SPVE_SEQ
--------------------------------------------------------

   CREATE SEQUENCE  "CVCOMMS"."BSM_PART2_SPVE_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 109383 CACHE 20 NOORDER  NOCYCLE   ;
--------------------------------------------------------
--  DDL for Table BSM_PART2_SPVE
--------------------------------------------------------

  CREATE TABLE "CVCOMMS"."BSM_PART2_SPVE" 
   (	"BSM_CORE_DATA_ID" NUMBER(10,0) NOT NULL, 
	"ID" VARCHAR2(255 BYTE), 
	"VA_SSPRIGHTS" NUMBER(10,0), 
	"VA_EVENTS" VARCHAR2(1000 BYTE), 
	"VA_EVENTS_SSPRIGHTS" NUMBER(10,0), 
	"VA_LIGHTSUSE" VARCHAR2(20 BYTE), 
	"VA_MULTI" VARCHAR2(20 BYTE), 
	"VA_RESPONSETYPE" VARCHAR2(25 BYTE), 
	"VA_SIRENUSE" VARCHAR2(20 BYTE), 
	"DESC_DESCRIPTION" VARCHAR2(1000 BYTE), 
	"DESC_EXTENT" VARCHAR2(25 BYTE), 
	"DESC_HEADING" VARCHAR2(1000 BYTE), 
	"DESC_PRIORITY" VARCHAR2(255 BYTE), 
	"DESC_REGIONAL" VARCHAR2(1000 BYTE), 
	"DESC_TYPEEVENT" NUMBER(10,0), 
	"TR_CONN_PIVOTOFFSET" NUMBER(38,0), 
	"TR_CONN_PIVOTANGLE" NUMBER(38,0), 
	"TR_CONN_PIVOTS" NUMBER(1,0), 
	"TR_SSPRIGHTS" NUMBER(10,0), 
	"TR_UNITS" VARCHAR2(1000 BYTE), 
	"BSM_PART2_SPVE_ID" NUMBER(10,0) CONSTRAINT "BSM_PART2_SPVE_PK" PRIMARY KEY USING INDEX,
    CONSTRAINT "FK_BSM_CORE_DATA_SPVE" FOREIGN KEY ("BSM_CORE_DATA_ID") REFERENCES "CVCOMMS"."BSM_CORE_DATA" ("BSM_CORE_DATA_ID") ON DELETE CASCADE
   )   --NO INMEMORY 
       PARTITION BY REFERENCE("FK_BSM_CORE_DATA_SPVE");

--------------------------------------------------------
--  DDL for Index IX_BSMPART2SPVE_BSMCOREDATAID
--------------------------------------------------------

  CREATE INDEX "CVCOMMS"."IX_BSMPART2SPVE_BSMCOREDATAID" ON "CVCOMMS"."BSM_PART2_SPVE" ("BSM_CORE_DATA_ID") 
  ;
--------------------------------------------------------
--  DDL for Trigger BSM_PART2_SPVE_TRIGGER
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE TRIGGER "CVCOMMS"."BSM_PART2_SPVE_TRIGGER" 
BEFORE INSERT ON BSM_PART2_SPVE 
FOR EACH ROW

BEGIN
  SELECT BSM_PART2_SPVE_seq.NEXTVAL
  INTO   :new.BSM_PART2_SPVE_ID
  FROM   dual;
END;

/
ALTER TRIGGER "CVCOMMS"."BSM_PART2_SPVE_TRIGGER" ENABLE;
--------------------------------------------------------
--  DDL for Synonymn DUAL
--------------------------------------------------------

  --CREATE OR REPLACE NONEDITIONABLE PUBLIC SYNONYM "DUAL" FOR "SYS"."DUAL";
