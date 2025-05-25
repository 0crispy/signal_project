package com.data_management;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

/**
 * Comprehensive test class for DataStorage functionality
 */
public class DataStorageTest {
    private DataStorage dataStorage;
    private Field instanceField;

    @Before
    public void setUp() throws Exception {
        instanceField = DataStorage.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        resetSingleton();
        dataStorage = DataStorage.getInstance();
    }

    @After
    public void stop() throws Exception {
        resetSingleton();
    }

    private void resetSingleton() throws Exception {
        instanceField.set(null, null);
    }

    @Test
    public void testAddPatientData() {
        dataStorage.addPatientData(1, 120.0, "SystolicPressure", 1000L);

        List<PatientRecord> records = dataStorage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals(120.0, records.get(0).getMeasurementValue(), 0.001);
        assertEquals("SystolicPressure", records.get(0).getRecordType());
    }

    @Test
    public void testAddMultiplePatients() {
        dataStorage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        dataStorage.addPatientData(2, 80.0, "DiastolicPressure", 2000L);

        assertEquals(2, dataStorage.getAllPatients().size());
    }

    @Test
    public void testGetRecordsInTimeRange() {
        dataStorage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        dataStorage.addPatientData(1, 125.0, "SystolicPressure", 2000L);
        dataStorage.addPatientData(1, 130.0, "SystolicPressure", 3000L);

        List<PatientRecord> records = dataStorage.getRecords(1, 1500L, 2500L);
        assertEquals(1, records.size());
        assertEquals(125.0, records.get(0).getMeasurementValue(), 0.001);
    }

    @Test
    public void testGetRecordsNonexistentPatient() {
        List<PatientRecord> records = dataStorage.getRecords(999, 0L, Long.MAX_VALUE);
        assertTrue(records.isEmpty());
    }

    @Test
    public void testGetPatientThresholdProfile() {
        dataStorage.addPatientData(1, 120.0, "SystolicPressure", 1000L);

        assertNotNull(dataStorage.getPatientThresholdProfile(1));

        assertNull(dataStorage.getPatientThresholdProfile(999));
    }

    @Test
    public void testSingletonBehavior() {
        DataStorage instance1 = DataStorage.getInstance();
        DataStorage instance2 = DataStorage.getInstance();
        
        assertSame(instance1, instance2);
        instance1.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        List<PatientRecord> records = instance2.getRecords(1, 0L, Long.MAX_VALUE);
        assertFalse(records.isEmpty());
        assertEquals(120.0, records.get(0).getMeasurementValue(), 0.001);
    }

    @Test
    public void testThreadSafeSingleton() throws Exception {
        final int threadCount = 10;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(threadCount);
        final List<DataStorage> instances = new ArrayList<>();
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    instances.add(DataStorage.getInstance());
                    endLatch.countDown();
                } catch (InterruptedException e) {
                    fail("Thread interrupted");
                }
            });
        }
        
        startLatch.countDown();
        
        assertTrue("Timeout waiting for threads", endLatch.await(5, TimeUnit.SECONDS));
        
        DataStorage firstInstance = instances.get(0);
        for (DataStorage instance : instances) {
            assertSame("All threads should get the same instance", firstInstance, instance);
        }
        
        executor.shutdown();
        assertTrue("Executor shutdown timed out", executor.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    public void testConcurrentDataAccess() throws Exception {
        final int threadCount = 5;
        final int operationsPerThread = 100;
        final CountDownLatch endLatch = new CountDownLatch(threadCount);
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        // Add data
                        dataStorage.addPatientData(threadId, j, "TestRecord", System.currentTimeMillis());
                        // Read data
                        List<PatientRecord> records = dataStorage.getRecords(threadId, 0L, Long.MAX_VALUE);
                        assertFalse("Records should not be empty", records.isEmpty());
                    }
                    endLatch.countDown();
                } catch (Exception e) {
                    fail("Concurrent operation failed: " + e.getMessage());
                }
            });
        }
        
        assertTrue("Timeout waiting for threads", endLatch.await(10, TimeUnit.SECONDS));
        
        executor.shutdown();
        assertTrue("Executor shutdown timed out", executor.awaitTermination(5, TimeUnit.SECONDS));
        
        assertEquals("Should have one patient per thread", threadCount, dataStorage.getAllPatients().size());
    }
}