
BEGIN;
ALTER TABLE category DISABLE TRIGGER USER;

SET client_encoding TO 'UTF8';
SET synchronous_commit TO off;

INSERT INTO category (category_id,category) VALUES (1,E'speedLimit');
INSERT INTO category (category_id,category) VALUES (2,E'advisory');
INSERT INTO category (category_id,category) VALUES (3,E'workZone');
INSERT INTO category (category_id,category) VALUES (4,E'exitService');

COMMIT;

