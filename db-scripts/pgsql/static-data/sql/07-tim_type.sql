
BEGIN;
ALTER TABLE tim_type DISABLE TRIGGER USER;

SET client_encoding TO 'UTF8';
SET synchronous_commit TO off;

INSERT INTO tim_type (type,description,tim_type_id) VALUES (E'VSL',E'Variable Speed Limit',1);
INSERT INTO tim_type (type,description,tim_type_id) VALUES (E'RC',E'Road Condition',2);
INSERT INTO tim_type (type,description,tim_type_id) VALUES (E'I',E'Incident',3);
INSERT INTO tim_type (type,description,tim_type_id) VALUES (E'RW',E'Road Construction',4);
INSERT INTO tim_type (type,description,tim_type_id) VALUES (E'CC',E'Chain Controls',5);
INSERT INTO tim_type (type,description,tim_type_id) VALUES (E'P',E'Parking',6);

COMMIT;

