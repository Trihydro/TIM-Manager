package com.trihydro.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.Gson;

public class TestHelper {
    public static <T> T importJsonArray(String fileName, Class<T> clazz, Gson gson) {
        InputStream is = TestHelper.class.getResourceAsStream(fileName);
        InputStreamReader isr = new InputStreamReader(is);

        T data = gson.fromJson(isr, clazz);

        try {
            isr.close();
        } catch (IOException ex) {

        }

        return data;
    }

    public static <T> T importJsonArray(String fileName, Class<T> clazz) {
        Gson gson = new Gson();
        return importJsonArray(fileName, clazz, gson);
    }
}