package com.trihydro.library.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.model.AdvisorySituationDataDeposit;

import org.apache.commons.lang3.StringUtils;

public class SdwService {
    public static AdvisorySituationDataDeposit getSdwDataByRecordId(String recordId) {
        String token = getToken();
        if (token == null) {
            return null;
        }

        try {
            URL url = getBaseUrl("api/GetDataByRecordId?recordId=" + recordId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String objString = br.readLine();

            if (StringUtils.isEmpty(objString) || StringUtils.isBlank(objString)) {
                return null;
            }

            // hydrate AdvisorySituationDataDeposit
            ObjectMapper mapper = new ObjectMapper();
            AdvisorySituationDataDeposit asdd = mapper.readValue(objString, AdvisorySituationDataDeposit.class);
            return asdd;

        } catch (IOException ex) {
            return null;
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

    public static String getToken() {
        URL url = getBaseUrl("Token");
        String token = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            String input = String.format("{\"Email\": \"%s\",\"Password\": \"%s\"}",
                    DbUtility.getConfig().getSdwUsername(), DbUtility.getConfig().getSdwPassword());

            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();

            int status = conn.getResponseCode();
            if(status != 200){
                return token;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            token = br.readLine();

            conn.disconnect();
        } catch (IOException ex) {
            System.out.println(String.format("Failed to fetch SDW token: {0}", ex.getMessage()));
            token = null;
        }
        return token;
    }
}