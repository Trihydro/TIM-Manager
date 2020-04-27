package com.trihydro.library.model;

public class TmddItisCode {
    private String elementType;
    private String elementValue;
    private Integer itisCode;

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public String getElementValue() {
        return elementValue;
    }

    public void setElementValue(String elementValue) {
        this.elementValue = elementValue;
    }

    public Integer getItisCode() {
        return itisCode;
    }

    public void setItisCode(Integer itisCode) {
        this.itisCode = itisCode;
    }

    public String normalized() {
        // "type:valuetext"
        return (elementType + ":" + elementValue).toLowerCase().replaceAll("[\\s-]", "");
    }
}