-- Last copied: 7/2/2024

-- Initial data to deploy CV Manager for WYDOT
-- This is only recommended to be used for testing, do not use in a deployed environment
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
	('y20.23.3-b168981', 1, 'install_y20_23_3-b168981.tar', 'y20.23.3-b168981'),
	('y20.39.4-b205116', 1, 'rs4-generic-ro-secureboot-y20.39.4-b205116.tar.sig', 'y20.39.4-b205116');

INSERT INTO public.firmware_upgrade_rules(
	from_id, to_id)
	VALUES (1, 2);

-- RSU Credential
INSERT INTO public.rsu_credentials(
	username, password, nickname)
	VALUES ('username', 'password', 'default');

-- SNMP Credential
INSERT INTO public.snmp_credentials(
	username, password, encrypt_password, nickname)
	VALUES ('username', 'password', 'encryption-pw', 'snmp1');

-- SNMP Version
INSERT INTO public.snmp_versions(
	version_code, nickname)
	VALUES ('4.1', 'RSU 4.1');
INSERT INTO public.snmp_versions(
	version_code, nickname)
	VALUES ('1218', 'NTCIP 1218');

INSERT INTO public.rsus(
	geography, milepost, ipv4_address, serial_number, iss_scms_id, primary_route, model, credential_id, snmp_credential_id, snmp_version_id, firmware_version, target_firmware_version)
	VALUES (ST_GeomFromText('POINT(-105.014182 39.740422)'), 1, '10.0.0.180', 'E5672', 'E5672', 'I999', 1, 1, 1, 1, 1, 1), 
	(ST_GeomFromText('POINT(-104.967723 39.918758)'), 2, '10.0.0.78', 'E5321', 'E5321', 'I999', 1, 1, 1, 2, 2, 2);

INSERT INTO public.organizations(
	name)
	VALUES ('Test Org');

INSERT INTO public.roles(
	name)
	VALUES ('admin'), ('operator'), ('user');

INSERT INTO public.rsu_organization(
	rsu_id, organization_id)
	VALUES (1, 1), (2, 1);

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

INSERT INTO public.snmp_msgfwd_config(
	rsu_id, msgfwd_type, snmp_index, message_type, dest_ipv4, dest_port, start_datetime, end_datetime, active)
	VALUES (1, 1, 1, 'BSM', '10.0.0.80', 46800, '2024/04/01T00:00:00', '2034/04/01T00:00:00', '1'),
	(1, 1, 2, 'BSM', '10.0.0.81', 46800, '2024/04/01T00:00:00', '2034/04/01T00:00:00', '1'),
	(1, 1, 3, 'BSM', '10.0.0.82', 46800, '2024/04/01T00:00:00', '2034/04/01T00:00:00', '1'),
	(2, 2, 1, 'BSM', '10.0.0.80', 46800, '2024/04/01T00:00:00', '2034/04/01T00:00:00', '1'),
	(2, 2, 2, 'BSM', '10.0.0.81', 46800, '2024/04/01T00:00:00', '2034/04/01T00:00:00', '1'),
	(2, 3, 1, 'MAP', '10.0.0.80', 44920, '2024/04/01T00:00:00', '2034/04/01T00:00:00', '1'),
	(2, 3, 2, 'SPAT', '10.0.0.80', 44910, '2024/04/01T00:00:00', '2034/04/01T00:00:00', '1');
