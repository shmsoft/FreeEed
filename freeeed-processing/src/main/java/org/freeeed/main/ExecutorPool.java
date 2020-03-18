package org.freeeed.main;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorPool {

    private static volatile ExecutorPool mInstance;
    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private ExecutorPool() {
    }

    public static ExecutorPool getInstance() {
        if (mInstance == null) {
            synchronized (ExecutorPool.class) {
                if (mInstance == null) {
                    mInstance = new ExecutorPool();
                }
            }
        }
        return mInstance;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void shutDownPool() {
        executorService.shutdownNow();
    }
}
