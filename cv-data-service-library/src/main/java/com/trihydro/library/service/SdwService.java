package com.trihydro.library.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.AdvisorySituationDataDeposit;

import org.apache.commons.lang3.StringUtils;

public class SdwService {
    public static Gson gson = new Gson();

    public static AdvisorySituationDataDeposit getSdwDataByRecordId(String recordId) {
        String apiKey = DbUtility.getConfig().getSdwApiKey();
        if (recordId == null || apiKey == null) {
            return null;
        }

        try {
            URL url = getBaseUrl("api/GetDataByRecordId?recordId=" + recordId);
            HttpURLConnection conn = Utility.getUrlConnection("GET", url, apiKey);

            InputStreamReader isr = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            String objString = br.readLine();

            if (StringUtils.isEmpty(objString) || StringUtils.isBlank(objString)) {
                return null;
            }

            // hydrate AdvisorySituationDataDeposit
            ObjectMapper mapper = new ObjectMapper();
            AdvisorySituationDataDeposit asdd = mapper.readValue(objString, AdvisorySituationDataDeposit.class);
            return asdd;

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

    }

    public static String getNewRecordId() {
        String hexChars = "ABCDEF1234567890";
        StringBuilder hexStrB = new StringBuilder();
        Random rnd = new Random();
        while (hexStrB.length() < 8) { // length of the random string.
            int index = (int) (rnd.nextFloat() * hexChars.length());
            hexStrB.append(hexChars.charAt(index));
        }
        String hexStr = hexStrB.toString();
        return hexStr;
    }

    public static HashMap<Integer, Boolean> deleteSdxDataBySatRecordId(List<String> satRecordIds) {
        HashMap<Integer, Boolean> results = null;
        String apiKey = DbUtility.getConfig().getSdwApiKey();
        if (satRecordIds == null || satRecordIds.size() == 0 || apiKey == null) {
            Utility.logWithDate("Attempting to delete satellite records failed due to null apiKey");
            return results;
        }

        try {
            URL url = getBaseUrl("api/delete-multiple-by-recordid");
            List<Long> satRecordInts = satRecordIds.stream().map(x -> Long.parseLong(x, 16))
                    .collect(Collectors.toList());
            String body = gson.toJson(satRecordInts);
            HttpURLConnection conn = Utility.getUrlConnection("DELETE", url, apiKey);
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = body.getBytes("utf-8");
                os.write(input, 0, input.length);
                os.close();
            }

            if(conn.getResponseCode() != 200){
                Utility.logWithDate("Failed to call delete-multiple-by-id on SDX api");
            }

            InputStreamReader isr = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            String objString = br.readLine();
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<HashMap<Integer, Boolean>> typeRef = new TypeReference<HashMap<Integer, Boolean>>() {
            };
            results = mapper.readValue(objString, typeRef);
            Utility.logWithDate("Results from deleting SDX data by recordId: " + gson.toJson(results));
            return results;
        } catch (IOException ex) {
            ex.printStackTrace();
            return results;
        }
    }

    private static URL getBaseUrl(String end) {
        try {
            String baseUrl = DbUtility.getConfig().getSdwRestUrl();
            if (!baseUrl.endsWith("/")) {
                baseUrl += "/";
            }
            baseUrl += end;
            return new URL(baseUrl);
        } catch (MalformedURLException ex) {
            return null;
        }
    }
}