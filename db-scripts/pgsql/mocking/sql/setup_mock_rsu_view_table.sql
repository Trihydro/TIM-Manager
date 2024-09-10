-- Note: This should only be run on a local deployment database. The actual view is provided by WYDOT.

CREATE TABLE rsu_view (
    deviceid numeric(6),
    sitename character varying(255),
    devicename character varying(100),
    devicetype character varying(50),
    manufactname character varying(50),
    modeldescription character varying(250),
    modelnumber character varying(50),
    status text,
    latitude numeric(15,8),
    longitude numeric(15,8),
    category character varying(2),
    idnumber numeric(10),
    direction character varying(1),
    route character varying(250),
    milepost numeric(6,3),
    powertype text,
    commdesc character varying(50),
    district numeric(1),
    ip4v_address text,
    ipv6_address text
);