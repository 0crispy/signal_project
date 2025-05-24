package com.alerts;

import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * Test class for AlertManager functionality
 */
public class AlertManagerTest {
    private AlertManager alertManager;
    private PatientRecord testRecord;

    @BeforeEach
    void setUp() {
        alertManager = new AlertManager();
        testRecord = new PatientRecord(1, 120.0, "SystolicPressure", 1000L);
    }

    @Test
    void testHandleAlert() {
        Alert alert = new Alert(1, testRecord, 1000L, "TestAlert", "Test description");

        alertManager.handleAlert(alert);

        List<Alert> alerts = alertManager.getAllAlerts();
        assertEquals(1, alerts.size());
        assertEquals("TestAlert", alerts.get(0).getAlertType());
    }

    @Test
    void testAddListener() {
        AtomicInteger callCount = new AtomicInteger(0);

        alertManager.addListener(alert -> callCount.incrementAndGet());

        Alert alert = new Alert(1, testRecord, 1000L, "TestAlert", "Test description");
        alertManager.handleAlert(alert);

        assertEquals(1, callCount.get());
    }

    @Test
    void testMultipleListeners() {
        AtomicInteger count1 = new AtomicInteger(0);
        AtomicInteger count2 = new AtomicInteger(0);

        alertManager.addListener(alert -> count1.incrementAndGet());
        alertManager.addListener(alert -> count2.incrementAndGet());

        Alert alert = new Alert(1, testRecord, 1000L, "TestAlert", "Test description");
        alertManager.handleAlert(alert);

        assertEquals(1, count1.get());
        assertEquals(1, count2.get());
    }

    @Test
    void testGetAlertsForPatient() {
        Alert alert1 = new Alert(1, testRecord, 1000L, "TestAlert1", "Description 1");
        Alert alert2 = new Alert(2, testRecord, 2000L, "TestAlert2", "Description 2");
        Alert alert3 = new Alert(1, testRecord, 3000L, "TestAlert3", "Description 3");

        alertManager.handleAlert(alert1);
        alertManager.handleAlert(alert2);
        alertManager.handleAlert(alert3);

        List<Alert> patient1Alerts = alertManager.getAlertsForPatient(1);
        assertEquals(2, patient1Alerts.size());

        List<Alert> patient2Alerts = alertManager.getAlertsForPatient(2);
        assertEquals(1, patient2Alerts.size());
    }

    @Test
    void testClearAlerts() {
        Alert alert = new Alert(1, testRecord, 1000L, "TestAlert", "Test description");
        alertManager.handleAlert(alert);

        assertEquals(1, alertManager.getAllAlerts().size());

        alertManager.clearAlerts();

        assertTrue(alertManager.getAllAlerts().isEmpty());
    }

    @Test
    void testRemoveListener() {
        AtomicInteger callCount = new AtomicInteger(0);
        AlertManager.AlertListener listener = alert -> callCount.incrementAndGet();

        alertManager.addListener(listener);
        alertManager.removeListener(listener);

        Alert alert = new Alert(1, testRecord, 1000L, "TestAlert", "Test description");
        alertManager.handleAlert(alert);

        assertEquals(0, callCount.get());
    }
    @Test
    void testHandleNullAlert() {
        assertThrows(IllegalArgumentException.class, () ->
                alertManager.handleAlert(null));
    }

    @Test
    void testDuplicateAlertHandling() {
        Alert alert = new Alert(1, testRecord, 1000L, "TestAlert", "Test");
        alertManager.handleAlert(alert);
        alertManager.handleAlert(alert); // Duplicate
        assertEquals(1, alertManager.getAllAlerts().size());
    }

    @Test
    void testConcurrentAlertHandling() throws InterruptedException {
        final int NUM_THREADS = 10;
        final CountDownLatch latch = new CountDownLatch(NUM_THREADS);
        AtomicInteger alertsProcessed = new AtomicInteger(0);

        // Add listener that counts alerts
        alertManager.addListener(alert -> alertsProcessed.incrementAndGet());

        // Create and start threads
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        for (int i = 0; i < NUM_THREADS; i++) {
            executor.execute(() -> {
                Alert alert = new Alert(1, testRecord, System.currentTimeMillis(),
                        "ConcurrentAlert-" + Thread.currentThread().getId(), "Test");
                alertManager.handleAlert(alert);
                latch.countDown();
            });
        }

        // Wait for all threads to complete
        latch.await();
        executor.shutdown();

        // Verify results
        assertEquals(NUM_THREADS, alertsProcessed.get());
        assertEquals(NUM_THREADS, alertManager.getAllAlerts().size());
    }
}