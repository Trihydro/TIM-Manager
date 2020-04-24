package com.trihydro.library.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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

import org.junit.Test;

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
        assertEquals(555, results.size());
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
        assertEquals(166166271, (int) header.getMessageNumber());
        assertEquals(2, (int) header.getMessageTypeVersion());
        assertEquals("20200414", header.getMessageTimeStamp().getDate());
        assertEquals("-0600", header.getMessageTimeStamp().getOffset());
        assertEquals("101720", header.getMessageTimeStamp().getTime());
        assertEquals("WYDOT", header.getOrganizationSending().getOrganizationId());
        assertNull(header.getMessageExpiryTime());

        // Event Reference
        EventReference ref = feu.getEventReference();
        assertEquals("AFTNUS89NB", ref.getEventId());
        assertEquals(418, (int) ref.getEventUpdate());
        assertEquals("20200412", ref.getUpdateTime().getDate());
        assertEquals("-0600", ref.getUpdateTime().getOffset());
        assertEquals("140821", ref.getUpdateTime().getTime());

        // Event Headline
        EventHeadline headline = feu.getEventHeadline();
        assertEquals("system-information", headline.getHeadline().getType());
        assertEquals("travel information", headline.getHeadline().getValue());

        // Event Element Details...
        assertEquals(1, feu.getEventElementDetails().size());
        EventElementDetail details = feu.getEventElementDetails().get(0);

        // ...event-times
        assertEquals("20200412", details.getEventTimes().getUpdateTime().getDate());
        assertEquals("-0600", details.getEventTimes().getUpdateTime().getOffset());
        assertEquals("140821", details.getEventTimes().getUpdateTime().getTime());

        // ...event-descriptions
        assertEquals(1, details.getEventDescriptions().size());
        EventDescription description = details.getEventDescriptions().get(0);
        assertEquals("pavement-conditions", description.getPhrase().getType());
        assertEquals("dry pavement", description.getPhrase().getValue());

        // ...event-locations
        assertEquals(1, details.getEventLocations().size());
        LinkLocation location = details.getEventLocations().get(0).getLocationOnLink();
        assertEquals("WYDOT", location.getLinkOwnership());
        assertEquals("both directions", location.getLinkDirection());
        assertEquals("US89", location.getLinkDesignator());
        assertEquals("Afton", location.getPrimaryLocation().getPointName());
        assertEquals("86.24", location.getPrimaryLocation().getLinearReference());
        assertEquals(42739996, (int) location.getPrimaryLocation().getGeoLocation().getLatitude());
        assertEquals(-110933278, (int) location.getPrimaryLocation().getGeoLocation().getLongitude());
        assertEquals("Alpine Jct", location.getSecondaryLocation().getPointName());
        assertEquals("119.2", location.getSecondaryLocation().getLinearReference());
        assertEquals(43175668, (int) location.getSecondaryLocation().getGeoLocation().getLatitude());
        assertEquals(-111001784, (int) location.getSecondaryLocation().getGeoLocation().getLongitude());

    }

    private String readFile(String fileName) {
        InputStream is = TmddDeserializationTest.class.getResourceAsStream(fileName);
        InputStreamReader isr = new InputStreamReader(is);
        String contents = null;
        try {
            contents = CharStreams.toString(isr);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contents;
    }
}