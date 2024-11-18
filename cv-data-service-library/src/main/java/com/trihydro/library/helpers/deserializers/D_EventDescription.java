package com.trihydro.library.helpers.deserializers;

import com.trihydro.library.model.tmdd.EventDescription;

public class D_EventDescription extends ListDeserializer<EventDescription> {

    @Override
    Class<EventDescription> getClazz() {
        return EventDescription.class;
    }

    @Override
    String getProxyName() {
        return "event-description";
    }

}