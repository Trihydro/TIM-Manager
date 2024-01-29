package com.trihydro.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

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

    public static <T> String readFile(String path, Class<T> clazz) throws IOException {
        String file = null;
        try (InputStream inputStream = clazz.getResourceAsStream(path);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            file = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        return file;
    }
}