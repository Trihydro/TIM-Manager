SET client_encoding TO 'UTF8';






CREATE OR REPLACE FUNCTION milepost_cn_lu (LRS_ROUTE text, CN_ROUTE_LU text, LRS_MP bigint) RETURNS varchar AS $body$
DECLARE

RESULT varchar(100) := NULL;
  intWtiCount integer;
  intHighwaysCount integer;

  numFromTemp bigint;
  numToTemp bigint;

  vc2CommonNameTemp varchar(100);

  vc2HighwayTemp varchar(100);
  vc2SignTemp varchar(50);
  vc2NumTemp varchar(50);
  vc2SuffixTemp varchar(50);

  
  WtiCursor REFCURSOR;
  HighwayCursor REFCURSOR;

BEGIN
  SELECT COUNT(*) INTO STRICT intWtiCount
  FROM MILEPOST_CN_LU_OV
  WHERE ROUTE = LRS_ROUTE
  AND LRS_MP >= FROM_MP AND LRS_MP <= TO_MP;

  SELECT COUNT(*) INTO STRICT intHighwaysCount
  FROM GIS.HIGHWAY_EVENTS_O
  WHERE CATEGORY || ID_NUMBER || DIRECTION = CN_ROUTE_LU
  AND LRS_MP >= FROM_MP AND LRS_MP <= TO_MP
  AND DOMINANT_FLAG = 'Y';

  IF intWtiCount > 0 THEN
    OPEN WtiCursor FOR SELECT DOMINANT_ROUTE_NAME, FROM_MP, TO_MP FROM MILEPOST_CN_LU_OV
      WHERE ROUTE = LRS_ROUTE
      AND LRS_MP >= FROM_MP AND LRS_MP <= TO_MP;

      LOOP
      FETCH WtiCursor INTO vc2CommonNameTemp, numFromTemp, numToTemp;
      EXIT WHEN NOT FOUND; /* apply on WtiCursor */

        RESULT := vc2CommonNameTemp;

      END LOOP;

    CLOSE WtiCursor;

  ELSIF intHighwaysCount > 0 THEN
    OPEN HighwayCursor FOR SELECT SIGN, NUM, SUFFIX, FROM_MP, TO_MP FROM GIS.HIGHWAY_EVENTS_O
      WHERE CATEGORY || ID_NUMBER || DIRECTION = CN_ROUTE_LU
      AND LRS_MP >= FROM_MP AND LRS_MP <= TO_MP
      AND DOMINANT_FLAG = 'Y'
      ORDER BY SIGN, NUM, SUFFIX, FROM_MP;

      LOOP
      FETCH HighwayCursor INTO vc2SignTemp, vc2NumTemp, vc2SuffixTemp, numFromTemp, numToTemp;
      EXIT WHEN NOT FOUND; /* apply on HighwayCursor */

      IF RESULT IS NULL THEN
        IF vc2SuffixTemp = 'A' THEN
          RESULT := vc2SignTemp || ' ' || vc2NumTemp || ' A';
        ELSIF vc2SuffixTemp = 'B' THEN
          RESULT := vc2SignTemp || ' ' || vc2NumTemp || ' Bus.';
        ELSIF vc2SuffixTemp = 'E' THEN
          RESULT := vc2SignTemp || ' ' || vc2NumTemp;
        ELSIF vc2SuffixTemp = 'P' THEN
          RESULT := vc2SignTemp || ' ' || vc2NumTemp || ' Bypass';
        ELSE
          RESULT := vc2SignTemp || ' ' || vc2NumTemp || ' ' || vc2SuffixTemp;
        END IF;
      END IF;
    END LOOP;
    CLOSE HighwayCursor;
  END IF;

  RETURN RESULT;
END;
$body$
LANGUAGE PLPGSQL
SECURITY DEFINER
 STABLE;




CREATE OR REPLACE FUNCTION milepost_rc_lu (LRS_ROUTE text, LRS_MP bigint) RETURNS varchar AS $body$
DECLARE

RESULT varchar(25) := NULL;
  intWtiCount integer;

  numFromTemp bigint;
  numToTemp bigint;

  vc2RoadCodeTemp varchar(25);
  vc2LrsRoute varchar(25);

  
  WtiCursor REFCURSOR;

BEGIN

  IF LRS_ROUTE = 'ML107D' THEN
      vc2LrsRoute := 'ML107B';
    ELSIF LRS_ROUTE = 'ML107I' THEN
      vc2LrsRoute := 'ML107B';
    ELSIF LRS_ROUTE = 'ML10D' THEN
      vc2LrsRoute := 'ML10B';
    ELSIF LRS_ROUTE = 'ML10I' THEN
      vc2LrsRoute := 'ML10B';
    ELSIF LRS_ROUTE = 'ML1108D' THEN
      vc2LrsRoute := 'ML1108B';
    ELSIF LRS_ROUTE = 'ML1108I' THEN
      vc2LrsRoute := 'ML1108B';
    ELSIF LRS_ROUTE = 'ML12D' THEN
      vc2LrsRoute := 'ML12B';
    ELSIF LRS_ROUTE = 'ML12I' THEN
      vc2LrsRoute := 'ML12B';
    ELSIF LRS_ROUTE = 'ML180D' THEN
      vc2LrsRoute := 'ML180B';
    ELSIF LRS_ROUTE = 'ML180I' THEN
      vc2LrsRoute := 'ML180B';
    ELSIF LRS_ROUTE = 'ML21D' THEN
      vc2LrsRoute := 'ML21B';
    ELSIF LRS_ROUTE = 'ML21I' THEN
      vc2LrsRoute := 'ML21B';
    ELSIF LRS_ROUTE = 'ML224D' THEN
      vc2LrsRoute := 'ML224B';
    ELSIF LRS_ROUTE = 'ML224I' THEN
      vc2LrsRoute := 'ML224B';
    ELSIF LRS_ROUTE = 'ML23D' THEN
      vc2LrsRoute := 'ML23B';
    ELSIF LRS_ROUTE = 'ML23I' THEN
      vc2LrsRoute := 'ML23B';
    ELSIF LRS_ROUTE = 'ML27D' THEN
      vc2LrsRoute := 'ML27B';
    ELSIF LRS_ROUTE = 'ML27I' THEN
      vc2LrsRoute := 'ML27B';
    ELSIF LRS_ROUTE = 'ML29D' THEN
      vc2LrsRoute := 'ML29B';
    ELSIF LRS_ROUTE = 'ML29I' THEN
      vc2LrsRoute := 'ML29B';
    ELSIF LRS_ROUTE = 'ML30D' THEN
      vc2LrsRoute := 'ML30B';
    ELSIF LRS_ROUTE = 'ML30I' THEN
      vc2LrsRoute := 'ML30B';
    ELSIF LRS_ROUTE = 'ML34D' THEN
      vc2LrsRoute := 'ML34B';
    ELSIF LRS_ROUTE = 'ML34I' THEN
      vc2LrsRoute := 'ML34B';
    ELSIF LRS_ROUTE = 'ML411D' THEN
      vc2LrsRoute := 'ML411B';
    ELSIF LRS_ROUTE = 'ML411I' THEN
      vc2LrsRoute := 'ML411B';
    ELSIF LRS_ROUTE = 'ML47D' THEN
      vc2LrsRoute := 'ML47B';
    ELSIF LRS_ROUTE = 'ML47I' THEN
      vc2LrsRoute := 'ML47B';
    ELSIF LRS_ROUTE = 'ML505D' THEN
      vc2LrsRoute := 'ML505B';
    ELSIF LRS_ROUTE = 'ML505I' THEN
      vc2LrsRoute := 'ML505B';
    ELSIF LRS_ROUTE = 'ML52D' THEN
      vc2LrsRoute := 'ML52B';
    ELSIF LRS_ROUTE = 'ML52I' THEN
      vc2LrsRoute := 'ML52B';
    ELSIF LRS_ROUTE = 'ML54D' THEN
      vc2LrsRoute := 'ML54B';
    ELSIF LRS_ROUTE = 'ML54I' THEN
      vc2LrsRoute := 'ML54B';
    ELSIF LRS_ROUTE = 'ML55D' THEN
      vc2LrsRoute := 'ML55B';
    ELSIF LRS_ROUTE = 'ML55I' THEN
      vc2LrsRoute := 'ML55B';
    ELSIF LRS_ROUTE = 'ML56D' THEN
      vc2LrsRoute := 'ML56B';
    ELSIF LRS_ROUTE = 'ML56I' THEN
      vc2LrsRoute := 'ML56B';
    ELSIF LRS_ROUTE = 'ML58D' THEN
      vc2LrsRoute := 'ML58B';
    ELSIF LRS_ROUTE = 'ML58I' THEN
      vc2LrsRoute := 'ML58B';
    ELSIF LRS_ROUTE = 'ML60D' THEN
      vc2LrsRoute := 'ML60B';
    ELSIF LRS_ROUTE = 'ML60I' THEN
      vc2LrsRoute := 'ML60B';
    ELSIF LRS_ROUTE = 'ML76D' THEN
      vc2LrsRoute := 'ML76B';
    ELSIF LRS_ROUTE = 'ML76I' THEN
      vc2LrsRoute := 'ML76B';
    ELSIF LRS_ROUTE = 'ML85D' THEN
      vc2LrsRoute := 'ML85B';
    ELSIF LRS_ROUTE = 'ML85I' THEN
      vc2LrsRoute := 'ML85B';
    ELSE
      vc2LrsRoute := LRS_ROUTE;
    END IF;

  SELECT COUNT(*) INTO STRICT intWtiCount
  FROM MILEPOST_RC_LU_OV
  WHERE ROUTE = vc2LrsRoute
  AND LRS_MP >= FROM_MP AND LRS_MP <= TO_MP;

  IF intWtiCount > 0 THEN
    OPEN WtiCursor FOR SELECT ROAD_CODE, FROM_MP, TO_MP FROM MILEPOST_RC_LU_OV
      WHERE ROUTE = vc2LrsRoute
      AND LRS_MP >= FROM_MP AND LRS_MP <= TO_MP;

      LOOP
      FETCH WtiCursor INTO vc2RoadCodeTemp, numFromTemp, numToTemp;
      EXIT WHEN NOT FOUND; /* apply on WtiCursor */

        RESULT := vc2RoadCodeTemp;

      END LOOP;

    CLOSE WtiCursor;

  ELSE
    vc2RoadCodeTemp := '';

  END IF;

  RETURN RESULT;
END;
$body$
LANGUAGE PLPGSQL
SECURITY DEFINER
 STABLE;




CREATE OR REPLACE FUNCTION route_name (ROUTE text) RETURNS varchar AS $body$
DECLARE

  vc2RouteName varchar(25);


BEGIN
  IF ROUTE LIKE '%A' OR ROUTE LIKE '%P' OR ROUTE LIKE '%S' THEN
    IF ROUTE LIKE 'I%' THEN
      vc2RouteName := SUBSTR(ROUTE, 1, 1) || ' ' || SUBSTR(ROUTE, 2, LENGTH(ROUTE)-2) || ' ' || SUBSTR(ROUTE, LENGTH(ROUTE), LENGTH(ROUTE));
    ELSIF ROUTE LIKE 'US%' OR ROUTE LIKE 'WY%' THEN
      vc2RouteName := SUBSTR(ROUTE, 1, 2) || ' ' || SUBSTR(ROUTE, 3, LENGTH(ROUTE)-3) || ' ' || SUBSTR(ROUTE, LENGTH(ROUTE), LENGTH(ROUTE));
    END IF; --IF ROUTE LIKE 'I%' THEN
  ELSE
    IF ROUTE LIKE 'I%' THEN
      vc2RouteName := SUBSTR(ROUTE, 1, 1) || ' ' || SUBSTR(ROUTE, 2, LENGTH(ROUTE));
    ELSIF ROUTE LIKE 'US%' OR ROUTE LIKE 'WY%' THEN
      vc2RouteName := SUBSTR(ROUTE, 1, 2) || ' ' || SUBSTR(ROUTE, 3, LENGTH(ROUTE));
    END IF; --IF ROUTE LIKE 'I%' THEN
  END IF;

  RETURN vc2RouteName;

END;
$body$
LANGUAGE PLPGSQL
SECURITY DEFINER
 STABLE;




CREATE OR REPLACE FUNCTION rsu_route (ROUTE text, MP bigint) RETURNS varchar AS $body$
DECLARE

  vc2Route varchar(100) := NULL;


BEGIN
  SELECT DISTINCT(COMMON_NAME) INTO STRICT vc2Route
  FROM MILEPOSTS
  WHERE LRS_ROUTE = ROUTE
    AND MILEPOST = MP 
  ORDER BY COMMON_NAME LIMIT 1;

  RETURN vc2Route;
END;
$body$
LANGUAGE PLPGSQL
SECURITY DEFINER
 STABLE;