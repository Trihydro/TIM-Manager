package com.trihydro.library.helpers.deserializers;

import com.trihydro.library.model.tmdd.EventLocation;

public class D_EventLocation extends ListDeserializer<EventLocation> {

    @Override
    Class<EventLocation> getClazz() {
        return EventLocation.class;
    }

    @Override
    String getProxyName() {
        return "event-location";
    }

}