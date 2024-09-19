
BEGIN;
ALTER TABLE incident_action_lut DISABLE TRIGGER USER;

SET client_encoding TO 'UTF8';
SET synchronous_commit TO off;

INSERT INTO incident_action_lut (description,code,itis_code_id,incident_action_lut_id) VALUES (E'Proceed with caution',E'caution',41,1);
INSERT INTO incident_action_lut (description,code,itis_code_id,incident_action_lut_id) VALUES (E'Prepare to slow down',E'slow',NULL,2);
INSERT INTO incident_action_lut (description,code,itis_code_id,incident_action_lut_id) VALUES (E'Expect delays',E'delays',172,3);
INSERT INTO incident_action_lut (description,code,itis_code_id,incident_action_lut_id) VALUES (E'Be prepared to stop, expect delays',E'stop',43,4);
INSERT INTO incident_action_lut (description,code,itis_code_id,incident_action_lut_id) VALUES (E'Traffic being diverted onto interchange ramps',E'toRamp',NULL,5);
INSERT INTO incident_action_lut (description,code,itis_code_id,incident_action_lut_id) VALUES (E'Traffic being diverted onto shoulder.  Expect delays',E'toShoulder',NULL,6);
INSERT INTO incident_action_lut (description,code,itis_code_id,incident_action_lut_id) VALUES (E'Traffic being diverted onto [HIGHWAY]',E'toHighway',NULL,7);
INSERT INTO incident_action_lut (description,code,itis_code_id,incident_action_lut_id) VALUES (E'Use alternate route',E'useAlt',NULL,8);

COMMIT;

