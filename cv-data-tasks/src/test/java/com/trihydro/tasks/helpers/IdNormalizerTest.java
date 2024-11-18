package com.trihydro.tasks.helpers;

import com.trihydro.library.model.tmdd.EventReference;
import com.trihydro.library.model.tmdd.FullEventUpdate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IdNormalizerTest {
    @Test
    public void tmdd_rw_successes() {
        // Arrange
        String[] rwIds = { "C18103ML80B", "C18414ML90I", "C19325ML1206D", "C16922ML25B", "C19693ML34B" };
        String[] expected = { "C18103", "C18414", "C19325", "C16922", "C19693" };

        IdNormalizer uut = new IdNormalizer();

        for (int i = 0; i < rwIds.length; i++) {
            FullEventUpdate feu = new FullEventUpdate();
            feu.setEventReference(new EventReference());
            feu.getEventReference().setEventId(rwIds[i]);

            // Act
            String result = uut.fromFeu(feu);

            // Assert
            Assertions.assertEquals(expected[i], result);
        }
    }

    @Test
    public void tmdd_rc_success() {
        // Arrange
        String rcId = "CHUGI25NWHED";
        FullEventUpdate feu = new FullEventUpdate();
        feu.setEventReference(new EventReference());
        feu.getEventReference().setEventId(rcId);

        IdNormalizer uut = new IdNormalizer();

        // Act
        String result = uut.fromFeu(feu);

        // Assert
        // Id should be unchanged
        Assertions.assertEquals(rcId, result);
    }

    @Test
    public void tmdd_in_success() {
        // Arrange
        String inId = "I1293526";
        FullEventUpdate feu = new FullEventUpdate();
        feu.setEventReference(new EventReference());
        feu.getEventReference().setEventId(inId);

        IdNormalizer uut = new IdNormalizer();

        // Act
        String result = uut.fromFeu(feu);

        // Assert
        // Id should be unchanged
        Assertions.assertEquals(inId, result);
    }

    @Test
    public void tmdd_noEventReference() {
        // Arrange
        FullEventUpdate feu = new FullEventUpdate();
        IdNormalizer uut = new IdNormalizer();

        // Act
        String result = uut.fromFeu(feu);

        // Assert
        Assertions.assertNull(result);
    }
}