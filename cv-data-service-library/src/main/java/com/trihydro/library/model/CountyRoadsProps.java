package com.trihydro.library.model;

public interface CountyRoadsProps {
    // primary
    String getCountyRoadsTriggerViewName();
    void setCountyRoadsTriggerViewName(String countyRoadsTriggerViewName);
    String getCountyRoadsGeometryViewName();
    void setCountyRoadsGeometryViewName(String countyRoadsGeometryViewName);
    String getCountyRoadsReportViewName();
    void setCountyRoadsReportViewName(String countyRoadsReportViewName);
    String getCountyRoadsWtiSectionsViewName();

    // secondary
    String getCountyRoadsAdhocClosuresCurrentViewName();
    void setCountyRoadsAdhocClosuresCurrentViewName(String countyRoadsAdhocClosuresCurrentViewName);
    String getCountyRoadsAdhocLoctCurrentViewName();
    void setCountyRoadsAdhocLoctCurrentViewName(String countyRoadsAdhocLoctCurrentViewName);
    String getCountyRoadsAdhocNttCurrentViewName();
    void setCountyRoadsAdhocNttCurrentViewName(String countyROadsAdhocNttCurrentViewName);
    String getCountyRoadsPlannedClosuresCurrentViewName();
    void setCountyRoadsPlannedClosuresCurrentViewName(String countyRoadsPlannedClosuresCurrentViewName);
    String getCountyRoadsPlannedClosuresEViewName();
    void setCountyRoadsPlannedClosuresEViewName(String countyRoadsPlannedClosuresEViewName);
    String getCountyRoadsPlannedLoctCurrentViewName();
    void setCountyRoadsPlannedLoctCurrentViewName(String countyRoadsPlannedLoctCurrentViewName);
    String getCountyRoadsPlannedLoctEViewName();
    void setCountyRoadsPlannedLoctEViewName(String countyRoadsPlannedLoctEViewName);
}