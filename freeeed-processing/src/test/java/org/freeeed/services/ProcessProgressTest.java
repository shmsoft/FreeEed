package org.freeeed.services;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ProcessProgressTest {

    private ProcessProgress progress;

    @Before
    public void setUp() {
        progress = ProcessProgress.getInstance();
        progress.cancelProcess(); // Reset state before each test
    }

    @Test
    public void testStartProcessSuccessfully() {
        boolean started = progress.startProcess("TestThread");
        assertTrue("Process should start successfully when no other process is running.", started);
        assertEquals("TestThread", progress.getActiveThreadName());
    }

    @Test
    public void testUpdateProgress() throws Exception {
        // Arrange
        progress.startProcess(Thread.currentThread().getName());

        // Act
        progress.updateProgress(50);

        // Assert
        assertEquals("Progress should update to the provided value.", 50, progress.getProgress());
    }

    @Test
    public void testCancelProcess() {
        // Arrange
        progress.startProcess("MainThread");
        progress.updateProgress(80);

        // Act
        progress.cancelProcess();

        // Assert
        assertFalse("Process should be marked as not running after cancellation.", progress.isRunning());
        assertEquals("Progress should reset after cancellation.", -1, progress.getProgress());
        assertNull("Active thread name should be cleared after cancellation.", progress.getActiveThreadName());
    }
}
