package com.trihydro.rsudatacontroller.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.trihydro.rsudatacontroller.model.RsuTim;
import com.trihydro.rsudatacontroller.process.ProcessFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ImageBanner;
import org.springframework.stereotype.Component;

@Component
public class RsuService {
    private static final String oid_rsuSRMDeliveryStart = "1.0.15628.4.1.4.1.7";

    private static final DateTimeFormatter rsuDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private ProcessFactory processFactory;

    @Autowired
    public void InjectDependencies(ProcessFactory processFactory) {
        this.processFactory = processFactory;
    }

    /**
     * Gets the deliveryStart times for each index of the provided RSU
     * 
     * @param rsuIpv4Address IPv4 address of the RSU
     * @return null if timeout occurred (unable to establish snmp session with RSU)
     * @throws Exception if unable to invoke command to perform SNMP communication
     */
    public List<RsuTim> getAllDeliveryStartTimes(String rsuIpv4Address) throws Exception {
        Process p = null; // OBFUSCATED until I move this to the config
        
        String snmpWalkOutput = "iso.0.15628.4.1.4.1.7.2 = Hex-STRING: 07 E4 03 14 11 3B \n";
        snmpWalkOutput += "iso.0.15628.4.1.4.1.7.3 = Hex-STRING: 07 E4 03 14 13 1E \n";
        snmpWalkOutput += "iso.0.15628.4.1.4.1.7.4 = Hex-STRING: 07 E4 03 14 13 1E \n";
        snmpWalkOutput += "iso.0.15628.4.1.4.1.7.5 = Hex-STRING: 07 E4 03 14 13 1E \n";
        snmpWalkOutput += "iso.0.15628.4.1.4.1.7.6 = Hex-STRING: 07 E4 03 14 13 1E \n";
        snmpWalkOutput += "iso.0.15628.4.1.4.1.7.7 = Hex-STRING: 07 E4 03 14 0E 20 \n";
        snmpWalkOutput += "iso.0.15628.4.1.4.1.7.8 = Hex-STRING: 07 E4 03 14 0E 20 \n";
        snmpWalkOutput += "iso.0.15628.4.1.4.1.7.9 = Hex-STRING: 07 E4 03 14 11 09 \n";
        snmpWalkOutput += "iso.0.15628.4.1.4.1.7.10 = Hex-STRING: 07 E4 03 0E 0E 2A \n";
        snmpWalkOutput += "iso.0.15628.4.1.4.1.7.11 = Hex-STRING: 07 E4 03 14 11 09 \n"; //getProcessOutput(p);

        // If timeout occurred, return null
        if(snmpWalkOutput.matches("snmpwalk: Timeout")) {
            return null;
        }

        List<RsuTim> tims = new ArrayList<>();

        Pattern ip = Pattern.compile("\\.(\\d*) =");
        Pattern hp = Pattern.compile("Hex-STRING: ((?:[0-9|A-F]{2} )*)");
        Matcher im;
        Matcher hm;
        for(String line : snmpWalkOutput.split("\n")) {
            im = ip.matcher(line);
            hm = hp.matcher(line);

            if(im.find() && hm.find()) {
                RsuTim tim = new RsuTim();
                tim.setIndex(Integer.parseInt(im.group(1)));
                tim.setDeliveryStartTime(hexStringToDateTime(hm.group(1)));

                tims.add(tim);
            }
            else {
                // throw exception?
            }
        }
        return tims;
    }

    private String getProcessOutput(Process p) throws IOException {
        String output = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            output += line + "\n";
        }
        if (output.length() > 0) {
            output = output.substring(0, output.length() - 1);
        }

        return output;
    }

    private String hexStringToDateTime(String hexString) {
        String[] octets = hexString.split(" ");
        if(octets.length != 6) {
            return null;
        }

        int year = Integer.parseInt(octets[0] + octets[1], 16);
        int month = Integer.parseInt(octets[2], 16);
        int day = Integer.parseInt(octets[3], 16);
        int hour = Integer.parseInt(octets[4], 16);
        int minute = Integer.parseInt(octets[5], 16);

        LocalDateTime date = LocalDateTime.of(year, month, day, hour, minute, 0);
        return date.format(rsuDateTimeFormatter);
    }
}