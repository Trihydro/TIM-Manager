package com.trihydro.tasks.models.hsmresponse;

import java.util.List;

public class ToBeSigned {
    public Id id;
    public String cracaId;
    public int crlSeries;
    public ValidityPeriod validityPeriod;
    public Region region;
    public List<AppPermission> appPermissions;
    public VerifyKeyIndicator verifyKeyIndicator;
}