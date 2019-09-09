package com.trihydro.library.model;

import java.sql.PreparedStatement;

public class SharedFieldsModel {
    private PreparedStatement preparedStatement;
    private int fieldCount;

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    public int getFieldCount() {
        return fieldCount;
    }

    public void setFieldCount(int fieldCount) {
        this.fieldCount = fieldCount;
    }

    public void setPreparedStatement(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

}