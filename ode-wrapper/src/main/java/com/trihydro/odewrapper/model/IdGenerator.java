package com.trihydro.odewrapper.model;

public class IdGenerator {
    private int currentId;
    public IdGenerator() {
        currentId = 0;
    }

    public int getNextId() {
        return currentId++;
    }
}
