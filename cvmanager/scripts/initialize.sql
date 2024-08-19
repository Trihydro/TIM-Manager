-- Last copied: 7/2/2024

-- Initial data to deploy CV Manager for WYDOT
-- This script is intended to be run manually using pgAdmin4 after the database has been created and the schema has been applied

-- Manufacturers
INSERT INTO public.manufacturers(name) 
  VALUES ('Commsignia'), ('Yunex');

-- Model
INSERT INTO public.rsu_models(
	name, supported_radio, manufacturer)
	VALUES ('ITS-RS4-M', 'DSRC,C-V2X', 1), ('RSU2X US', 'DSRC,C-V2X', 2);

-- Firmware Version
INSERT INTO public.firmware_images(
	name, model, install_package, version)
	VALUES ('y20.0.0', 1, 'install_y20_0_0.tar', 'y20.0.0'),
	('y20.1.0', 1, 'install_y20_1_0.tar', 'y20.1.0'),
	('y20.23.3', 1, 'rs4-generic-ro-secureboot-y20.23.3-b168981.tar.sig', 'y20.23.3-b168981'),
	('y20.39.2', 1, 'rs4-generic-ro-secureboot-y20.39.2-b197756.tar.sig', 'y20.39.2-b197756'),
	('y20.39.4', 1, 'rs4-generic-ro-secureboot-y20.39.4-b205116.tar.sig', 'y20.39.4-b205116'),
	('y20.41.3', 1, 'rs4-generic-ro-secureboot-y20.41.3-b214979.tar.sig', 'y20.41.3-b214979'),
	('y20.48.2', 1, 'rs4-generic-ro-secureboot-y20.48.2-b228647.tar.sig', 'y20.48.2-b228647');

INSERT INTO public.firmware_upgrade_rules(
	from_id, to_id)
	VALUES (1, 2);

-- RSU Credential
INSERT INTO public.rsu_credentials(
	username, password, nickname)
	VALUES ('username', 'password', 'default'),
	('username', 'password', 'wydot-rsu'); -- Need to manually update the username/password in the database

-- SNMP Credential
INSERT INTO public.snmp_credentials(
	username, password, encrypt_password, nickname)
	VALUES ('username', 'password', 'encryption-pw', 'default'),
	('username', 'password', 'encryption-pw', 'wydot-snmp'); -- Need to manually update the username/password in the database

-- SNMP Version
INSERT INTO public.snmp_versions(
	version_code, nickname)
	VALUES ('4.1', 'RSU 4.1');
INSERT INTO public.snmp_versions(
	version_code, nickname)
	VALUES ('1218', 'NTCIP 1218');

INSERT INTO public.organizations(
	name)
	VALUES ('WYDOT');

INSERT INTO public.roles(
	name)
	VALUES ('admin'), ('operator'), ('user');


-- Replace user with a real gmail to test GCP OAuth2.0 support
INSERT INTO public.users(
	email, first_name, last_name, super_user, receive_error_emails)
	VALUES ('test@gmail.com', 'Test', 'User', '1', '1');

INSERT INTO public.user_organization(
	user_id, organization_id, role_id)
	VALUES (1, 1, 1);

INSERT INTO public.snmp_msgfwd_type(
	name)
	VALUES ('rsuDsrcFwd'), ('rsuReceivedMsg'), ('rsuXmitMsgFwding');
