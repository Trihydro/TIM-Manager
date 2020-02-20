package com.trihydro.library.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
// import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.AdvisorySituationDataDeposit;
import com.trihydro.library.model.ConfigProperties;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SdwService {
    public Gson gson = new Gson();
    private ConfigProperties configProperties;

    @Autowired
    public void InjectDependencies(ConfigProperties _config) {
        configProperties = _config;
    }

    public AdvisorySituationDataDeposit getSdwDataByRecordId(String recordId) {
        if (recordId == null || configProperties.getSdwApiKey() == null) {
            return null;
        }

        try {
            URL url = getBaseUrl("api/GetDataByRecordId?recordId=" + recordId);
            HttpURLConnection conn = Utility.getSdxUrlConnection("GET", url, configProperties.getSdwApiKey());

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

    public String getNewRecordId() {
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

    public HashMap<Integer, Boolean> deleteSdxDataBySatRecordId(List<String> satRecordIds) {
        HashMap<Integer, Boolean> results = null;
        if (satRecordIds == null || satRecordIds.size() == 0 || configProperties.getSdwApiKey() == null) {
            if (configProperties.getSdwApiKey() == null) {
                Utility.logWithDate("Attempting to delete satellite records failed due to null apiKey");
            } else {
                Utility.logWithDate("Attempting to delete satellite records failed due to no satRecordIds passed in");
            }
            return results;
        }

        try {
            URL url = getBaseUrl("api/delete-multiple-by-recordid");
            List<Integer> satRecordInts = satRecordIds.stream().map(x -> Integer.parseUnsignedInt(x, 16))
                    .collect(Collectors.toList());
            String body = gson.toJson(satRecordInts);
            HttpURLConnection conn = Utility.getSdxUrlConnection("DELETE", url, configProperties.getSdwApiKey());
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = body.getBytes("utf-8");
                os.write(input, 0, input.length);
                os.close();
            }

            if (conn.getResponseCode() != 200) {
                Utility.logWithDate("Failed to call delete-multiple-by-id on SDX api");
            }

            InputStreamReader isr = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            String objString = br.lines().collect(Collectors.joining());// br.readLine();
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<HashMap<Integer, Boolean>> typeRef = new TypeReference<HashMap<Integer, Boolean>>() {
            };
            results = mapper.readValue(objString, typeRef);
            Utility.logWithDate("Results from deleting SDX data by recordId: " + results.toString());
            return results;
        } catch (IOException ex) {
            ex.printStackTrace();
            return results;
        }
    }

    private URL getBaseUrl(String end) {
        try {
            String baseUrl = configProperties.getSdwRestUrl();
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