package com.data_management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * Test class for Patient functionality
 */
public class PatientTest {
    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient(1);
    }

    @Test
    void testPatientCreation() {
        assertEquals(1, patient.getPatientId());
        assertTrue(patient.getAllRecords().isEmpty());
        assertNotNull(patient.getThresholdProfile());
    }

    @Test
    void testAddRecord() {
        patient.addRecord(120.0, "SystolicPressure", 1000L);

        List<PatientRecord> records = patient.getAllRecords();
        assertEquals(1, records.size());
        assertEquals(120.0, records.get(0).getMeasurementValue());
    }

    @Test
    void testGetRecordsInTimeRange() {
        patient.addRecord(120.0, "SystolicPressure", 1000L);
        patient.addRecord(125.0, "SystolicPressure", 2000L);
        patient.addRecord(130.0, "SystolicPressure", 3000L);

        List<PatientRecord> records = patient.getRecords(1500L, 2500L);
        assertEquals(1, records.size());
        assertEquals(125.0, records.get(0).getMeasurementValue());
    }

    @Test
    void testGetRecordsEmptyRange() {
        patient.addRecord(120.0, "SystolicPressure", 1000L);

        List<PatientRecord> records = patient.getRecords(2000L, 3000L);
        assertTrue(records.isEmpty());
    }

    @Test
    void testGetRecordsInclusiveBoundaries() {
        patient.addRecord(120.0, "SystolicPressure", 1000L);
        patient.addRecord(125.0, "SystolicPressure", 2000L);

        List<PatientRecord> records = patient.getRecords(1000L, 2000L);
        assertEquals(2, records.size());
    }
}