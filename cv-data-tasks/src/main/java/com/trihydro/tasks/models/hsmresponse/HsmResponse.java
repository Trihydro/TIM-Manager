package com.trihydro.tasks.models.hsmresponse;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HsmResponse {
    @JsonProperty("message-signed") 
    public String messageSigned;
    @JsonProperty("message-decoded") 
    public MessageDecoded messageDecoded;
    @JsonProperty("message-expiry") 
    public int messageExpiry;
}
