package com.trihydro.tasks.helpers;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.tmdd.FullEventUpdate;

import org.springframework.stereotype.Component;

/**
 * This class is meant to convert identifiers for FullEventUpdates and
 * ActiveTims to a normalized, comparable form. The form varies, based on the
 * TIM type.
 * 
 * Examples of normalized identifiers: C20774 (RW TIM), I1017974 (Incident TIM),
 * CASPI25SDOUGI (RC TIM)
 */
@Component
public class IdNormalizer {
    /**
     * Converts FEU's eventId to normalized form.
     * 
     * @return Normalized eventId, or <code>null</code> if eventId doesn't exist
     */
    public String fromFeu(FullEventUpdate feu) {
        // Input Examples
        // RC TIM: CHUGI25NWHED
        // Incident TIM: I1293526
        // RW TIM: C18103ML80B

        String result = null;

        if (feu.getEventReference() != null && feu.getEventReference().getEventId() != null) {
            String eventId = feu.getEventReference().getEventId();

            // RW TIM, if eventId matches:
            // "C<projectKey>ML<route><direction>"
            if (eventId.matches("C\\d*ML\\d*[IDB]")) {
                result = eventId.substring(0, eventId.indexOf("ML"));
            } else {
                // Incident, RC already have normalized form
                result = eventId;
            }
        }

        return result;
    }

    public String fromActiveTim(ActiveTim activeTim) {
        // Client Ids now follow the format: CLIENTID_n 
        // where n is a number > 0, to designate TIMs in a group
        int idx = activeTim.getClientId().lastIndexOf("_");

        String clientId = activeTim.getClientId();
        if(idx > 0) {
            clientId = clientId.substring(0, idx);
        }

        String result = null;

        switch (activeTim.getTimType()) {
            case "RC":
                result = clientId;
                break;
            case "IN":
                result = clientId.replace("IN", "I");
                break;
            case "RW":
                if (activeTim.getProjectKey() != null) {
                    result = "C" + activeTim.getProjectKey();
                }
                break;
            default:
                break;
        }

        return result;
    }
}