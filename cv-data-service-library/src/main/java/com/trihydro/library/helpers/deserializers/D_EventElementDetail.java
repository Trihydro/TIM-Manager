package com.trihydro.library.helpers.deserializers;

import com.trihydro.library.model.tmdd.EventElementDetail;

public class D_EventElementDetail extends ListDeserializer<EventElementDetail> {

    @Override
    Class<EventElementDetail> getClazz() {
        return EventElementDetail.class;
    }

    @Override
    String getProxyName() {
        return "event-element-detail";
    }

}