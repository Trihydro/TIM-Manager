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

import com.trihydro.library.helpers.Utility;
import com.trihydro.rsudatacontroller.config.BasicConfiguration;
import com.trihydro.rsudatacontroller.model.RsuTim;
import com.trihydro.rsudatacontroller.process.ProcessFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RsuService {
    private static final String oid_rsuSRMDeliveryStart = "1.0.15628.4.1.4.1.7";
    private static final DateTimeFormatter rsuDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ProcessFactory processFactory;
    private BasicConfiguration config;
    private Utility utility;

    @Autowired
    public void InjectDependencies(ProcessFactory processFactory, BasicConfiguration config, Utility utility) {
        this.processFactory = processFactory;
        this.config = config;
        this.utility = utility;
    }

    /**
     * Gets the deliveryStart times for each index of the provided RSU
     * 
     * @param rsuIpv4Address IPv4 address of the RSU
     * @return null if timeout occurred (unable to establish snmp session with RSU)
     * @throws Exception if unable to invoke command to perform SNMP communication
     */
    public List<RsuTim> getAllDeliveryStartTimes(String rsuIpv4Address) throws Exception {
        Process p = processFactory.buildAndStartProcess("snmpwalk", "-v", "3", "-r",
                Integer.toString(config.getSnmpRetries()), "-t", Integer.toString(config.getSnmpTimeoutSeconds()), "-u",
                config.getSnmpUserName(), "-l", config.getSnmpSecurityLevel(), "-a", config.getSnmpAuthProtocol(), "-A",
                config.getSnmpAuthPassphrase(), rsuIpv4Address, oid_rsuSRMDeliveryStart);

        String snmpWalkOutput = getProcessOutput(p);

        // If timeout occurred, return null
        if (snmpWalkOutput.matches("snmpwalk: Timeout")) {
            System.out.println("SNMP Timeout occurred (RSU: " + rsuIpv4Address + ")");
            return null;
        }

        List<RsuTim> tims = new ArrayList<>();

        Pattern ip = Pattern.compile("\\.(\\d*) =");
        Pattern hp = Pattern.compile("Hex-STRING: ((?:[0-9|A-F]{2}\\s?)*)");
        Matcher im;
        Matcher hm;
        for (String line : snmpWalkOutput.split("\n")) {
            im = ip.matcher(line);
            hm = hp.matcher(line);

            if (im.find() && hm.find()) {
                RsuTim tim = new RsuTim();
                tim.setIndex(Integer.parseInt(im.group(1)));
                tim.setDeliveryStartTime(hexStringToDateTime(hm.group(1)));

                tims.add(tim);
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
        if (octets.length != 6) {
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