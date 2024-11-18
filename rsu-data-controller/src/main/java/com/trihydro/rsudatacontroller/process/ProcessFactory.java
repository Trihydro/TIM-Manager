package com.trihydro.rsudatacontroller.process;

import java.io.IOException;

import org.springframework.stereotype.Component;

@Component
public class ProcessFactory {
    public Process buildAndStartProcess(String... args) throws RuntimeException {
        ProcessBuilder pb = new ProcessBuilder(args);
        // Merge the error output into the standard output so we only have to consume 1 stream
        pb.redirectErrorStream(true);

        try {
            return pb.start();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}