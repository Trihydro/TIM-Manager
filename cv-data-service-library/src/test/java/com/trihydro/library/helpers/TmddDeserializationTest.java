package com.trihydro.library.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trihydro.library.model.tmdd.EventDescription;
import com.trihydro.library.model.tmdd.EventElementDetail;
import com.trihydro.library.model.tmdd.EventHeadline;
import com.trihydro.library.model.tmdd.EventReference;
import com.trihydro.library.model.tmdd.FullEventUpdate;
import com.trihydro.library.model.tmdd.LinkLocation;
import com.trihydro.library.model.tmdd.MessageHeader;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

@Slf4j
public class TmddDeserializationTest {

    @Test
    public void deserializes_realData() {
        // Arrange
        Gson gson = new GsonFactory().getTmddDeserializer();
        String json = readFile("/tmdd_FEUs.json");

        // Act
        Type type = new TypeToken<List<FullEventUpdate>>() {
        }.getType();
        List<FullEventUpdate> results = gson.fromJson(json, type);

        // Assert
        // if we made it this far, we were able to deserialize the JSON
        Assertions.assertEquals(555, results.size());
    }

    @Test
    public void deserializes_feuCorrectly() {
        // Arrange
        Gson gson = new GsonFactory().getTmddDeserializer();
        String json = readFile("/tmdd_FEU.json");

        // Act
        FullEventUpdate feu = gson.fromJson(json, FullEventUpdate.class);

        // Assert
        // Message Header
        MessageHeader header = feu.getMessageHeader();
        Assertions.assertEquals(166166271, (int) header.getMessageNumber());
        Assertions.assertEquals(2, (int) header.getMessageTypeVersion());
        Assertions.assertEquals("20200414", header.getMessageTimeStamp().getDate());
        Assertions.assertEquals("-0600", header.getMessageTimeStamp().getOffset());
        Assertions.assertEquals("101720", header.getMessageTimeStamp().getTime());
        Assertions.assertEquals("WYDOT", header.getOrganizationSending().getOrganizationId());
        Assertions.assertNull(header.getMessageExpiryTime());

        // Event Reference
        EventReference ref = feu.getEventReference();
        Assertions.assertEquals("AFTNUS89NB", ref.getEventId());
        Assertions.assertEquals(418, (int) ref.getEventUpdate());
        Assertions.assertEquals("20200412", ref.getUpdateTime().getDate());
        Assertions.assertEquals("-0600", ref.getUpdateTime().getOffset());
        Assertions.assertEquals("140821", ref.getUpdateTime().getTime());

        // Event Headline
        EventHeadline headline = feu.getEventHeadline();
        Assertions.assertEquals("system-information", headline.getHeadline().getType());
        Assertions.assertEquals("travel information", headline.getHeadline().getValue());

        // Event Element Details...
        Assertions.assertEquals(1, feu.getEventElementDetails().size());
        EventElementDetail details = feu.getEventElementDetails().get(0);

        // ...event-times
        Assertions.assertEquals("20200412", details.getEventTimes().getUpdateTime().getDate());
        Assertions.assertEquals("-0600", details.getEventTimes().getUpdateTime().getOffset());
        Assertions.assertEquals("140821", details.getEventTimes().getUpdateTime().getTime());

        // ...event-descriptions
        Assertions.assertEquals(1, details.getEventDescriptions().size());
        EventDescription description = details.getEventDescriptions().get(0);
        Assertions.assertEquals("pavement-conditions", description.getPhrase().getType());
        Assertions.assertEquals("dry pavement", description.getPhrase().getValue());

        // ...event-locations
        Assertions.assertEquals(1, details.getEventLocations().size());
        LinkLocation location = details.getEventLocations().get(0).getLocationOnLink();
        Assertions.assertEquals("WYDOT", location.getLinkOwnership());
        Assertions.assertEquals("both directions", location.getLinkDirection());
        Assertions.assertEquals("US89", location.getLinkDesignator());
        Assertions.assertEquals("Afton", location.getPrimaryLocation().getPointName());
        Assertions.assertEquals("86.24", location.getPrimaryLocation().getLinearReference());
        Assertions.assertEquals(42739996, (int) location.getPrimaryLocation().getGeoLocation().getLatitude());
        Assertions.assertEquals(-110933278, (int) location.getPrimaryLocation().getGeoLocation().getLongitude());
        Assertions.assertEquals("Alpine Jct", location.getSecondaryLocation().getPointName());
        Assertions.assertEquals("119.2", location.getSecondaryLocation().getLinearReference());
        Assertions.assertEquals(43175668, (int) location.getSecondaryLocation().getGeoLocation().getLatitude());
        Assertions.assertEquals(-111001784, (int) location.getSecondaryLocation().getGeoLocation().getLongitude());

    }

    private String readFile(String fileName) {
        InputStream is = TmddDeserializationTest.class.getResourceAsStream(fileName);
        InputStreamReader isr = new InputStreamReader(is);
        String contents = null;
        try {
            contents = CharStreams.toString(isr);
        } catch (IOException e) {
            log.error("Exception", e);
        }

        return contents;
    }
}