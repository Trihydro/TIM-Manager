package com.trihydro.tasks.models;

import com.trihydro.library.model.ActiveTim;

public class EnvActiveTim implements Comparable<EnvActiveTim> {
    private ActiveTim activeTim;
    private Environment environment;

    public EnvActiveTim(ActiveTim activeTim, Environment environment) {
        this.activeTim = activeTim;
        this.environment = environment;
    }

    public ActiveTim getActiveTim() {
        return activeTim;
    }

    public void setActiveTim(ActiveTim activeTim) {
        this.activeTim = activeTim;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public int compareTo(EnvActiveTim o) {
        int result = 0;

        // If the object we're comparing to is null, then this one is greater.
        if (o == null)
            return 1;

        // If our activeTim is null, check if the one we're comparing to is and return
        // appropriately
        if (activeTim == null) {
            if (o.getActiveTim() == null)
                return 0;
            else
                return -1;
        }

        // At this point, our active isn't null but the one we're comparing to is.
        // So this object is greater than the other.
        if (o.getActiveTim() == null)
            return 1;

        // Compare rsuTarget
        if (activeTim.getRsuTarget() == null)
            result = o.getActiveTim().getRsuTarget() == null ? 0 : -1;
        else
            result = activeTim.getRsuTarget().compareTo(o.getActiveTim().getRsuTarget());

        if (result != 0)
            return result;

        // Compare index
        if (activeTim.getRsuIndex() == null)
            result = o.getActiveTim().getRsuIndex() == null ? 0 : -1;
        else
            result = activeTim.getRsuIndex().compareTo(o.getActiveTim().getRsuIndex());

        if (result != 0)
            return result;

        // Compare activeTimId
        if (activeTim.getActiveTimId() == null)
            result = o.getActiveTim().getActiveTimId() == null ? 0 : -1;
        else
            result = activeTim.getActiveTimId().compareTo(o.getActiveTim().getActiveTimId());

        if (result != 0)
            return result;

        // Finally, compare environment. If they're still equal, then the objects are
        // effectively equivalent. Return result regardless.
        if (environment == null)
            result = o.getEnvironment() == null ? 0 : -1;
        else
            result = environment.compareTo(o.getEnvironment());

        return result;
    }
}