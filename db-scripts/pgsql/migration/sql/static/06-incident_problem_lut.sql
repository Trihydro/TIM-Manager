
BEGIN;
ALTER TABLE incident_problem_lut DISABLE TRIGGER USER;

SET client_encoding TO 'UTF8';
SET synchronous_commit TO off;

INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Mudslide',E'mudslide',6,1);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Multiple crashes',E'crashes',NULL,2);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Crash',E'crash',NULL,3);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Hazardous material clean-up',E'hazMat',NULL,4);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Train derailment',E'trainDerail',NULL,5);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Livestock on highway',E'livestock',10,6);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Local event',E'local',NULL,7);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Stalled vehicle',E'stall',NULL,8);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Stalled semi truck',E'stallSemi',NULL,9);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Slow moving traffic',E'slow',NULL,10);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Slow, oversize load',E'slowOver',NULL,11);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Stopped traffic',E'stop',NULL,12);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Area flooding',E'flood',NULL,13);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Avalanche',E'avalanche',216,14);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Avalanche control',E'avalancheControl',8,15);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Landslide',E'landslide',11,16);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Rockslide',E'rockslide',NULL,17);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Wildfire',E'wildfire',16,18);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Downed power line',E'downPowerline',NULL,19);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Sign installation',E'signInstall',220,20);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Road damage',E'roadDamage',NULL,21);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Pilot car in operation',E'pilotCar',NULL,22);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Watch for maintenance personnel',E'maintenance',NULL,23);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Mowing operations',E'mowing',225,24);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Law enforcement activity',E'cops',NULL,25);
INSERT INTO incident_problem_lut (description,code,itis_code_id,incident_problem_lut_id) VALUES (E'Emergency vehicles',E'emerVeh',NULL,26);

COMMIT;

