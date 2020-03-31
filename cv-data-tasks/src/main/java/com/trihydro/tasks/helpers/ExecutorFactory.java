package com.trihydro.tasks.helpers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Component;

@Component
public class ExecutorFactory {
    public ExecutorService getFixedThreadPool(int threadCount) {
        return Executors.newFixedThreadPool(threadCount);
    }
}