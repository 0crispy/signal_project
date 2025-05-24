package com.data_management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * Comprehensive test class for DataStorage functionality
 */
public class DataStorageTest {
    private DataStorage dataStorage;

    @BeforeEach
    void setUp() {
        dataStorage = new DataStorage();
    }

    @Test
    void testAddPatientData() {
        dataStorage.addPatientData(1, 120.0, "SystolicPressure", 1000L);

        List<PatientRecord> records = dataStorage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals(120.0, records.get(0).getMeasurementValue());
        assertEquals("SystolicPressure", records.get(0).getRecordType());
    }

    @Test
    void testAddMultiplePatients() {
        dataStorage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        dataStorage.addPatientData(2, 80.0, "DiastolicPressure", 2000L);

        assertEquals(2, dataStorage.getAllPatients().size());
    }

    @Test
    void testGetRecordsInTimeRange() {
        dataStorage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        dataStorage.addPatientData(1, 125.0, "SystolicPressure", 2000L);
        dataStorage.addPatientData(1, 130.0, "SystolicPressure", 3000L);

        List<PatientRecord> records = dataStorage.getRecords(1, 1500L, 2500L);
        assertEquals(1, records.size());
        assertEquals(125.0, records.get(0).getMeasurementValue());
    }

    @Test
    void testGetRecordsNonexistentPatient() {
        List<PatientRecord> records = dataStorage.getRecords(999, 0L, Long.MAX_VALUE);
        assertTrue(records.isEmpty());
    }

    @Test
    void testGetPatientThresholdProfile() {
        dataStorage.addPatientData(1, 120.0, "SystolicPressure", 1000L);

        // Should return default profile
        assertNotNull(dataStorage.getPatientThresholdProfile(1));

        // Nonexistent patient should return null
        assertNull(dataStorage.getPatientThresholdProfile(999));
    }
}