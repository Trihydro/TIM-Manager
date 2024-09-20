
BEGIN;
ALTER TABLE incident_effect_lut DISABLE TRIGGER USER;

SET client_encoding TO 'UTF8';
SET synchronous_commit TO off;

INSERT INTO incident_effect_lut (description,code,itis_code_id,incident_effect_lut_id) VALUES (E'Left lane blocked',E'leftClosed',NULL,1);
INSERT INTO incident_effect_lut (description,code,itis_code_id,incident_effect_lut_id) VALUES (E'Center lane blocked',E'centerClosed',NULL,2);
INSERT INTO incident_effect_lut (description,code,itis_code_id,incident_effect_lut_id) VALUES (E'Right lane blocked',E'rightClosed',NULL,3);
INSERT INTO incident_effect_lut (description,code,itis_code_id,incident_effect_lut_id) VALUES (E'All lanes closed',E'allClosed',NULL,4);
INSERT INTO incident_effect_lut (description,code,itis_code_id,incident_effect_lut_id) VALUES (E'Shoulder closed',E'shoulderClosed',NULL,5);
INSERT INTO incident_effect_lut (description,code,itis_code_id,incident_effect_lut_id) VALUES (E'Travel lane blocked',E'travelBlocked',NULL,6);

COMMIT;

