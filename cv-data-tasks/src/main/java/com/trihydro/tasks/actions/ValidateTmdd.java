package com.trihydro.tasks.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.tmdd.FullEventUpdate;
import com.trihydro.library.service.TmddService;
import com.trihydro.tasks.helpers.IdNormalizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidateTmdd implements Runnable {
    private TmddService tmddService;
    private Utility utility;
    private IdNormalizer idNormalizer;

    @Autowired
    public void InjectDependencies(TmddService tmddService, Utility utility, IdNormalizer idNormalizer) {
        this.tmddService = tmddService;
        this.utility = utility;
        this.idNormalizer = idNormalizer;
    }

    public void run() {
        utility.logWithDate("Running...", this.getClass());

        try {
            validateTmdd();
        } catch (Exception ex) {
            utility.logWithDate("Error while validating Oracle with TMDD:", this.getClass());
            ex.printStackTrace();
            // don't rethrow error, or the task won't be reran until the service is
            // restarted.
        }
    }

    private void validateTmdd() {
        // Get FEUs from TMDD
        List<FullEventUpdate> feus = null;
        try {
            feus = tmddService.getTmddEvents();
        } catch (Exception ex) {
            utility.logWithDate("Error fetching FEUs from TMDD:", this.getClass());
            ex.printStackTrace();
            return;
        }

        // Initialize FEU map with initial capacity of 1024
        // This allows us to store 0.75*1024 elements before the map resizes (which
        // requires a rehashing of all elements). Currently, the TMDD is reporting 560
        // FEUs.
        Map<String, FullEventUpdate> feuMap = new HashMap<>(1024);
        for (FullEventUpdate feu : feus) {
            String id = idNormalizer.fromFeu(feu);

            if (id != null) {
                feuMap.put(id, feu);
            }
        }
    }
}