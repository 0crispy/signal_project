package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class AlertGeneratorTest {
    private DataStorage dataStorage;
    private AlertManager alertManager;
    private AlertGenerator alertGenerator;

    @BeforeEach
    void setUp() {
        dataStorage = new DataStorage();
        alertManager = new AlertManager();
        alertGenerator = new AlertGenerator(dataStorage, alertManager);
    }

    @Test
    void testSystolicPressureIncreasingTrend() {
        // Add three consecutive readings with >10 mmHg increases
        dataStorage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        dataStorage.addPatientData(1, 135.0, "SystolicPressure", 2000L);  // +15
        dataStorage.addPatientData(1, 150.0, "SystolicPressure", 3000L);  // +15

        Patient patient = dataStorage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);

        List<Alert> alerts = alertManager.getAllAlerts();
        assertTrue(alerts.stream().anyMatch(alert ->
                alert.getAlertType().equals("BloodPressureIncreasingTrend")));
    }

    @Test
    void testDiastolicPressureDecreasingTrend() {
        // Add three consecutive readings with >10 mmHg decreases
        dataStorage.addPatientData(1, 90.0, "DiastolicPressure", 1000L);
        dataStorage.addPatientData(1, 75.0, "DiastolicPressure", 2000L);  // -15
        dataStorage.addPatientData(1, 60.0, "DiastolicPressure", 3000L);  // -15

        Patient patient = dataStorage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);

        List<Alert> alerts = alertManager.getAllAlerts();
        assertTrue(alerts.stream().anyMatch(alert ->
                alert.getAlertType().equals("BloodPressureDecreasingTrend")));
    }

    @Test
    void testCriticalBloodPressureThresholds() {
        // Test all critical thresholds
        dataStorage.addPatientData(1, 185.0, "SystolicPressure", 1000L);  // High
        dataStorage.addPatientData(1, 85.0, "SystolicPressure", 2000L);   // Low
        dataStorage.addPatientData(1, 125.0, "DiastolicPressure", 3000L); // High
        dataStorage.addPatientData(1, 55.0, "DiastolicPressure", 4000L);  // Low

        Patient patient = dataStorage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);

        List<Alert> alerts = alertManager.getAllAlerts();
        assertEquals(4, alerts.size());

        assertTrue(alerts.stream().anyMatch(alert ->
                alert.getAlertType().equals("CriticalHighSystolicPressure")));
        assertTrue(alerts.stream().anyMatch(alert ->
                alert.getAlertType().equals("CriticalLowSystolicPressure")));
        assertTrue(alerts.stream().anyMatch(alert ->
                alert.getAlertType().equals("CriticalHighDiastolicPressure")));
        assertTrue(alerts.stream().anyMatch(alert ->
                alert.getAlertType().equals("CriticalLowDiastolicPressure")));
    }

    @Test
    void testLowSaturationAlert() {
        dataStorage.addPatientData(1, 90.0, "BloodSaturation", 1000L);

        Patient patient = dataStorage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);

        List<Alert> alerts = alertManager.getAllAlerts();
        assertEquals(1, alerts.size());
        assertEquals("LowBloodSaturation", alerts.get(0).getAlertType());
    }

    @Test
    void testRapidSaturationDrop() {
        // 6% drop within 8 minutes
        dataStorage.addPatientData(1, 98.0, "BloodSaturation", 1000L);
        dataStorage.addPatientData(1, 92.0, "BloodSaturation", 481000L); // 8 minutes later

        Patient patient = dataStorage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);

        List<Alert> alerts = alertManager.getAllAlerts();
        assertTrue(alerts.stream().anyMatch(alert ->
                alert.getAlertType().equals("RapidSaturationDrop")));
    }

    @Test
    void testHypotensiveHypoxemiaAlert() {
        // Low systolic pressure and low saturation within 5 minutes
        dataStorage.addPatientData(1, 85.0, "SystolicPressure", 1000L);
        dataStorage.addPatientData(1, 90.0, "BloodSaturation", 180000L); // 3 minutes later

        Patient patient = dataStorage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);

        List<Alert> alerts = alertManager.getAllAlerts();
        assertTrue(alerts.stream().anyMatch(alert ->
                alert.getAlertType().equals("HypotensiveHypoxemia")));
    }

    @Test
    void testECGAnomalyDetection() {
        // Add baseline readings
        for (int i = 0; i < 10; i++) {
            dataStorage.addPatientData(1, 0.8, "ECG", 1000L + i * 1000);
        }
        // Add anomalous reading (>1.5x average)
        dataStorage.addPatientData(1, 2.5, "ECG", 11000L);

        Patient patient = dataStorage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);

        List<Alert> alerts = alertManager.getAllAlerts();
        assertTrue(alerts.stream().anyMatch(alert ->
                alert.getAlertType().equals("ECGAnomaly")));
    }

    @Test
    void testManualAlert() {
        dataStorage.addPatientData(1, 1.0, "Alert", 1000L);

        Patient patient = dataStorage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);

        List<Alert> alerts = alertManager.getAllAlerts();
        assertEquals(1, alerts.size());
        assertEquals("ManualAlert", alerts.get(0).getAlertType());
    }

}