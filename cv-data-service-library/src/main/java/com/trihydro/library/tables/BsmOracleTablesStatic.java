package com.trihydro.library.tables;

import java.util.ArrayList;
import java.util.List;

@Deprecated
/**@deprecated functionality moved to non-static version
 * 
 */
public class BsmOracleTablesStatic extends OracleTablesStatic {
    
    private static List<String> bsmCoreDataTable;
    private static List<String> bsmPart2SuveTable;
    private static List<String> bsmPart2VseTable;
    private static List<String> bsmPart2SpveTable;

    public static List<String> getBsmCoreDataTable(){
        if(bsmCoreDataTable != null)
            return bsmCoreDataTable;
        else{
            bsmCoreDataTable = new ArrayList<String>();

            bsmCoreDataTable.add("ID");
            bsmCoreDataTable.add("MSGCNT");
            bsmCoreDataTable.add("SECMARK");
            bsmCoreDataTable.add("POSITION_LAT");
            bsmCoreDataTable.add("POSITION_LONG");
            bsmCoreDataTable.add("POSITION_ELEV");
            bsmCoreDataTable.add("ACCELSET_ACCELLAT");
            bsmCoreDataTable.add("ACCELSET_ACCELLONG");
            bsmCoreDataTable.add("ACCELSET_ACCELVERT");
            bsmCoreDataTable.add("ACCELSET_ACCELYAW");
            bsmCoreDataTable.add("ACCURACY_SEMIMAJOR");
            bsmCoreDataTable.add("ACCURACY_SEMIMINOR");
            bsmCoreDataTable.add("ACCURACY_ORIENTATION");
            bsmCoreDataTable.add("TRANSMISSION");
            bsmCoreDataTable.add("SPEED");
            bsmCoreDataTable.add("HEADING");
            bsmCoreDataTable.add("ANGLE");
            bsmCoreDataTable.add("BRAKES_WHEELBRAKES");
            bsmCoreDataTable.add("BRAKES_TRACTION");
            bsmCoreDataTable.add("BRAKES_ABS");
            bsmCoreDataTable.add("BRAKES_SCS");
            bsmCoreDataTable.add("BRAKES_BRAKEBOOST");
            bsmCoreDataTable.add("BRAKES_AUXBRAKES");
            bsmCoreDataTable.add("SIZE_LENGTH");
            bsmCoreDataTable.add("SIZE_WIDTH");
            bsmCoreDataTable.add("LOG_FILE_NAME");
            bsmCoreDataTable.add("RECORD_GENERATED_AT");
            bsmCoreDataTable.add("SECURITY_RESULT_CODE");
            bsmCoreDataTable.add("SANITIZED");
            bsmCoreDataTable.add("SERIAL_ID_STREAM_ID");
            bsmCoreDataTable.add("SERIAL_ID_BUNDLE_SIZE");
            bsmCoreDataTable.add("SERIAL_ID_BUNDLE_ID");
            bsmCoreDataTable.add("SERIAL_ID_RECORD_ID");
            bsmCoreDataTable.add("SERIAL_ID_SERIAL_NUMBER");
            bsmCoreDataTable.add("ODE_RECEIVED_AT");
            bsmCoreDataTable.add("RECORD_TYPE");
            bsmCoreDataTable.add("PAYLOAD_TYPE");
            bsmCoreDataTable.add("SCHEMA_VERSION");
            bsmCoreDataTable.add("RECORD_GENERATED_BY");
            bsmCoreDataTable.add("BSM_SOURCE");
            
            return bsmCoreDataTable;
        }
    }

    public static List<String> getBsmPart2SuveTable(){
        if(bsmPart2SuveTable != null)
            return bsmPart2SuveTable;
        else{
            bsmPart2SuveTable = new ArrayList<String>();
            bsmPart2SuveTable.add("BSM_CORE_DATA_ID");
            bsmPart2SuveTable.add("ID");
            bsmPart2SuveTable.add("CLASSIFICATION");
            bsmPart2SuveTable.add("CD_FUELTYPE");
            bsmPart2SuveTable.add("CD_HPMSTYPE");
            bsmPart2SuveTable.add("CD_ISO3883");
            bsmPart2SuveTable.add("CD_KEYTYPE");
            bsmPart2SuveTable.add("CD_REGIONAL");
            bsmPart2SuveTable.add("CD_RESPONDERTYPE");
            bsmPart2SuveTable.add("CD_RESPONSEEQUIP_NAME");
            bsmPart2SuveTable.add("CD_RESPONSEEQUIP_VALUE");
            bsmPart2SuveTable.add("CD_ROLE");
            bsmPart2SuveTable.add("CD_VEHICLETYPE_NAME");
            bsmPart2SuveTable.add("CD_VEHICLETYPE_VALUE");
            bsmPart2SuveTable.add("VD_BUMPERS_FRONT");
            bsmPart2SuveTable.add("VD_BUMPERS_REAR");
            bsmPart2SuveTable.add("VD_HEIGHT");
            bsmPart2SuveTable.add("VD_MASS");
            bsmPart2SuveTable.add("VD_TRAILERWEIGHT");
            bsmPart2SuveTable.add("WR_FRICTION");
            bsmPart2SuveTable.add("WR_ISRAINING");
            bsmPart2SuveTable.add("WR_PRECIPSITUATION");
            bsmPart2SuveTable.add("WR_RAINRATE");
            bsmPart2SuveTable.add("WR_ROADFRICTION");
            bsmPart2SuveTable.add("WR_SOLARRADIATION");
            bsmPart2SuveTable.add("WP_AIRPRESSURE");
            bsmPart2SuveTable.add("WP_AIRTEMP");
            bsmPart2SuveTable.add("WP_RAINRATES_RATEFRONT");
            bsmPart2SuveTable.add("WP_RAINRATES_RATEREAR");
            bsmPart2SuveTable.add("WP_RAINRATES_STATUSFRONT");
            bsmPart2SuveTable.add("WP_RAINRATES_STATUSREAR");
            bsmPart2SuveTable.add("OB_DATETIME");
            bsmPart2SuveTable.add("OB_DESCRIPTION");
            bsmPart2SuveTable.add("OB_LOCATIONDETAILS_NAME");
            bsmPart2SuveTable.add("OB_LOCATIONDETAILS_VALUE");
            bsmPart2SuveTable.add("OB_OBDIRECT");
            bsmPart2SuveTable.add("OB_OBDIST");
            bsmPart2SuveTable.add("OB_VERTEVENT");
            bsmPart2SuveTable.add("ST_STATUSDETAILS");
            bsmPart2SuveTable.add("ST_LOCATIONDETAILS_NAME");
            bsmPart2SuveTable.add("ST_LOCATIONDETAILS_VALUE");
            bsmPart2SuveTable.add("SP_SPEEDREPORTS");
            bsmPart2SuveTable.add("RTCM_MSGS");
            bsmPart2SuveTable.add("RTCM_HEADER_ANTOFFSETX");
            bsmPart2SuveTable.add("RTCM_HEADER_ANTOFFSETY");
            bsmPart2SuveTable.add("RTCM_HEADER_ANTOFFSETZ");
            bsmPart2SuveTable.add("RTCM_RTCMHEADER_STATUS");
            bsmPart2SuveTable.add("REGIONAL");

            return bsmPart2SuveTable;
        }
    }

    public static List<String> getBsmPart2VseTable(){
        if(bsmPart2VseTable != null)
            return bsmPart2VseTable;
        else {
            bsmPart2VseTable = new ArrayList<String>();
            bsmPart2VseTable.add("BSM_CORE_DATA_ID");
            bsmPart2VseTable.add("ID");
            bsmPart2VseTable.add("EVENTS");
            bsmPart2VseTable.add("PH_INITPOS_LAT");
            bsmPart2VseTable.add("PH_INITPOS_LONG");
            bsmPart2VseTable.add("PH_INITPOS_ELEV");
            bsmPart2VseTable.add("PH_INITPOS_HEADING");
            bsmPart2VseTable.add("PH_INITPOS_POSACCRCY_SEMIMAJ");
            bsmPart2VseTable.add("PH_INITPOS_POSACCRCY_SEMIMIN");
            bsmPart2VseTable.add("PH_INITPOS_POSACCRCY_ORIEN");
            bsmPart2VseTable.add("PH_INITPOS_POSCONFIDENCE_POS");
            bsmPart2VseTable.add("PH_INITPOS_POSCONFIDENCE_ELEV");
            bsmPart2VseTable.add("PH_INITPOS_SPEED");
            bsmPart2VseTable.add("PH_INITPOS_TRANSMISSION");
            bsmPart2VseTable.add("PH_INITPOS_SPEEDCONF_HEADING");
            bsmPart2VseTable.add("PH_INITPOS_SPEEDCONF_SPEED");
            bsmPart2VseTable.add("PH_INITPOS_SPEEDCONF_THROTTLE");
            bsmPart2VseTable.add("PH_INITPOS_TIMECONF");
            bsmPart2VseTable.add("PH_INITPOS_UTCTIME_DAY");
            bsmPart2VseTable.add("PH_INITPOS_UTCTIME_HOUR");
            bsmPart2VseTable.add("PH_INITPOS_UTCTIME_MINUTE");
            bsmPart2VseTable.add("PH_INITPOS_UTCTIME_MONTH");
            bsmPart2VseTable.add("PH_INITPOS_UTCTIME_OFFSET");
            bsmPart2VseTable.add("PH_INITPOS_UTCTIME_SECOND");
            bsmPart2VseTable.add("PH_INITPOS_UTCTIME_YEAR");
            bsmPart2VseTable.add("PH_CURRGNSSSTATUS");
            bsmPart2VseTable.add("PH_CRUMBDATA");
            bsmPart2VseTable.add("PP_CONFIDENCE");
            bsmPart2VseTable.add("PP_RADIUSOFCURVE");
            bsmPart2VseTable.add("LIGHTS");
            return bsmPart2VseTable;
        }
    }

    public static List<String> getBsmPart2SpveTable(){
        if(bsmPart2SpveTable != null)
            return bsmPart2SpveTable;
        else {
            bsmPart2SpveTable = new ArrayList<String>();
            bsmPart2SpveTable.add("BSM_CORE_DATA_ID");
            bsmPart2SpveTable.add("ID");
            bsmPart2SpveTable.add("VA_SSPRIGHTS");
            bsmPart2SpveTable.add("VA_EVENTS");
            bsmPart2SpveTable.add("VA_EVENTS_SSPRIGHTS");
            bsmPart2SpveTable.add("VA_LIGHTSUSE");
            bsmPart2SpveTable.add("VA_MULTI");
            bsmPart2SpveTable.add("VA_RESPONSETYPE");
            bsmPart2SpveTable.add("VA_SIRENUSE");
            bsmPart2SpveTable.add("DESC_DESCRIPTION");
            bsmPart2SpveTable.add("DESC_EXTENT");
            bsmPart2SpveTable.add("DESC_HEADING");
            bsmPart2SpveTable.add("DESC_PRIORITY");
            bsmPart2SpveTable.add("DESC_REGIONAL");
            bsmPart2SpveTable.add("DESC_TYPEEVENT");
            bsmPart2SpveTable.add("TR_CONN_PIVOTOFFSET");
            bsmPart2SpveTable.add("TR_CONN_PIVOTANGLE");
            bsmPart2SpveTable.add("TR_CONN_PIVOTS");
            bsmPart2SpveTable.add("TR_SSPRIGHTS");
            bsmPart2SpveTable.add("TR_UNITS");
            return bsmPart2SpveTable;
        }
    }
}


