package org.freeeed.services;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcessProgress {
    private static final ProcessProgress INSTANCE = new ProcessProgress();

    private volatile String activeThreadName = null; // Tracks the active process
    private final AtomicInteger progress = new AtomicInteger(0); // Progress of the active process
    private final AtomicBoolean isRunning = new AtomicBoolean(false); // Status of the active process
    private final AtomicBoolean isInterrupt = new AtomicBoolean(false); // Interrupt at next opportunity

    private ProcessProgress() {} // Private constructor

    public static ProcessProgress getInstance() {
        return INSTANCE;
    }

    // Register and start a process
    public synchronized boolean startProcess(String threadName) {
        if (isRunning.get()) {
            return false; // Another process is already running
        }
        activeThreadName = threadName;
        progress.set(0);
        isRunning.set(true);
        return true;
    }

    // Register process interrupt
    public synchronized boolean doInterrupt(String threadName) {
        if (!isRunning.get()) {
            return false; // If no process is running, there is nothing to interrupt
        }
        if (!threadName.equalsIgnoreCase(activeThreadName)) {
            return false; // If trying to interrupt a wrong thread, this is wrong
        }
        progress.set(0);
        isRunning.set(false);
        return true;
    }

    // Update the progress of the active process
    public void updateProgress(int value) {
        if (isRunning.get() && Thread.currentThread().getName().equals(activeThreadName)) {
            progress.set(value);
        }
    }

    // Get the current progress of the active process
    public int getProgress() {
        return isRunning.get() ? progress.get() : -1; // Return -1 if no process is running
    }

    // Cancel the current process
    public synchronized void cancelProcess() {
        if (isRunning.get()) {
            System.out.println("Process canceled: " + activeThreadName);
            isRunning.set(false);
            activeThreadName = null;
            progress.set(0);
        }
    }

    // Check if a process is currently running
    public boolean isRunning() {
        return isRunning.get();
    }

    // Check if there was a request to interrupt at next opportunity
    public boolean getIsInterrupt() {
        return isInterrupt.get();
    }

    // Get the name of the active process
    public String getActiveThreadName() {
        return activeThreadName;
    }
}

