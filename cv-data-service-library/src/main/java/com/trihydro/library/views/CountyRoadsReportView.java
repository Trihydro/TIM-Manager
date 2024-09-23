package com.trihydro.library.views;

import org.springframework.stereotype.Component;

/**
 * This class is intended to contain information about the county roads report view.
 */
@Component
public class CountyRoadsReportView {
    public static final String crIdColumnName = "cr_id";
    public static final String countyColumnName = "county";
    public static final String typeColumnName = "type";
    public static final String rdNoColumnName = "rd_no";
    public static final String nameColumnName = "name";
    public static final String pointFromColumnName = "point_from";
    public static final String pointToColumnName = "point_to";
    public static final String activeColumnName = "active";
    public static final String adhocClosedColumnName = "adhoc_closed";
    public static final String loctColumnName = "loct";
    public static final String adhocC2lhpvColumnName = "adhoc_c2lhpv";
    public static final String adhocNttColumnName = "adhoc_ntt";
    public static final String plannedClosureColumnName = "planned_closure";
    public static final String plannedClosureFromDateColumnName = "planned_closure_from_date";
    public static final String plannedClosureToDateColumnName = "planned_closure_to_date";
    public static final String plannedClosureActiveColumnName = "planned_closure_active";
    public static final String plannedLoctColumnName = "planned_loct";
    public static final String plannedLoctFromDateColumnName = "planned_loct_from_date";
    public static final String plannedLoctToDateColumnName = "planned_loct_to_date";
    public static final String plannedLoctActiveColumnName = "planned_loct_active";
    public static final String triggeredClosedColumnName = "triggered_closed";
    public static final String triggeredLoctColumnName = "triggered_loct";
    public static final String triggeredC2lhpvColumnName = "triggered_c2lhpv";
    public static final String triggeredNttColumnName = "triggered_ntt";
    public static final String finalResultColumnName = "final_result";
    public static final String mFromColumnName = "m_from";
    public static final String mToColumnName = "m_to";
    public static final String xFromColumnName = "x_from";
    public static final String xToColumnName = "x_to";
    public static final String yFromColumnName = "y_from";
    public static final String yToColumnName = "y_to";
}
