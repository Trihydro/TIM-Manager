package com.trihydro.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.Gson;

public class TestHelper {
    public static <T> T importJsonArray(String fileName, Class<T> clazz) {
        InputStream is = TestHelper.class.getResourceAsStream(fileName);
        InputStreamReader isr = new InputStreamReader(is);

        Gson gson = new Gson();
        T data = gson.fromJson(isr, clazz);

        try {
            isr.close();
        } catch (IOException ex) {

        }

        return data;
    }
}