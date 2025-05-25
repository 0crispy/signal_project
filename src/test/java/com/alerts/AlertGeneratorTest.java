package com.alerts;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import com.data_management.DataStorage;
import com.data_management.Patient;
import java.util.List;

public class AlertGeneratorTest {
    private AlertGenerator alertGenerator;
    private AlertManager alertManager;
    private DataStorage dataStorage;
    private static final int TEST_PATIENT_ID = 999;

    @Before
    public void setUp() {
        dataStorage = DataStorage.getInstance();
        alertManager = new AlertManager();
        alertGenerator = new AlertGenerator(dataStorage, alertManager);
    }

    @After
    public void stop() {
        dataStorage = null;
        alertManager = null;
        alertGenerator = null;
    }

    @Test
    public void testAlertGeneration() {
        
        // Add test data
        long timestamp = System.currentTimeMillis();
        dataStorage.addPatientData(TEST_PATIENT_ID, 180.0, "SystolicPressure", timestamp);
        
        List<Patient> patients = dataStorage.getAllPatients();
        Patient testPatient = null;
        for (Patient p : patients) {
            if (p.getPatientId() == TEST_PATIENT_ID) {
                testPatient = p;
                break;
            }
        }
        assertNotNull("Test patient should exist", testPatient);
        alertGenerator.evaluateData(testPatient);
        
        List<Alert> alerts = alertManager.getAllAlerts();
        assertFalse("Should have generated alerts", alerts.isEmpty());
        assertEquals("Should have generated one alert", 1, alerts.size());
        
        Alert alert = alerts.get(0);
        assertNotNull("Alert should not be null", alert);
        assertEquals("Should be critical high systolic pressure alert", "CriticalHighSystolicPressure", alert.getAlertType());
    }

    @Test
    public void testNoAlertForNormalValues() {
        long timestamp = System.currentTimeMillis();
        dataStorage.addPatientData(TEST_PATIENT_ID + 1, 120.0, "SystolicPressure", timestamp);
        
        List<Patient> patients = dataStorage.getAllPatients();
        Patient testPatient = null;
        for (Patient p : patients) {
            if (p.getPatientId() == TEST_PATIENT_ID + 1) {
                testPatient = p;
                break;
            }
        }
        assertNotNull("Test patient should exist", testPatient);
        alertGenerator.evaluateData(testPatient);
        
        List<Alert> alerts = alertManager.getAllAlerts();
        assertTrue("Should not have generated alerts for normal values", alerts.isEmpty());
    }

    @Test
    public void testMultipleAlerts() {
        long timestamp = System.currentTimeMillis();
        dataStorage.addPatientData(TEST_PATIENT_ID + 2, 180.0, "SystolicPressure", timestamp);
        dataStorage.addPatientData(TEST_PATIENT_ID + 2, 45.0, "DiastolicPressure", timestamp + 1000);
        
        List<Patient> patients = dataStorage.getAllPatients();
        Patient testPatient = null;
        for (Patient p : patients) {
            if (p.getPatientId() == TEST_PATIENT_ID + 2) {
                testPatient = p;
                break;
            }
        }
        assertNotNull("Test patient should exist", testPatient);
        alertGenerator.evaluateData(testPatient);
        
        List<Alert> alerts = alertManager.getAllAlerts();
        assertNotNull("Alerts list should not be null", alerts);
        assertTrue("Should have generated multiple alerts", alerts.size() > 1);
        
        boolean hasHighSystolic = false;
        boolean hasLowDiastolic = false;
        for (Alert alert : alerts) {
            assertNotNull("Alert should not be null", alert);
            if ("CriticalHighSystolicPressure".equals(alert.getAlertType())) hasHighSystolic = true;
            if ("CriticalLowDiastolicPressure".equals(alert.getAlertType())) hasLowDiastolic = true;
        }
        assertTrue("Should have critical high systolic pressure alert", hasHighSystolic);
        assertTrue("Should have critical low diastolic pressure alert", hasLowDiastolic);
    }

    @Test
    public void testInvalidData() {
        long timestamp = System.currentTimeMillis();
        dataStorage.addPatientData(TEST_PATIENT_ID + 3, Double.NaN, "SystolicPressure", timestamp);
        
        List<Patient> patients = dataStorage.getAllPatients();
        Patient testPatient = null;
        for (Patient p : patients) {
            if (p.getPatientId() == TEST_PATIENT_ID + 3) {
                testPatient = p;
                break;
            }
        }
        assertNotNull("Test patient should exist", testPatient);
        
        try {
            alertGenerator.evaluateData(testPatient);
        } catch (Exception e) {
            fail("Should not throw exception for invalid data: " + e.getMessage());
        }
        
        List<Alert> alerts = alertManager.getAllAlerts();
        assertNotNull("Alerts list should not be null", alerts);
        assertTrue("Should not generate alerts for invalid data", alerts.isEmpty());
    }
}