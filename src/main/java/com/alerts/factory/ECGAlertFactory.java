package com.alerts.factory;

import com.alerts.Alert;
import com.alerts.PatientRecord;

public class ECGAlertFactory extends AlertFactory {
    
    private static final double ECG_ANOMALY_MULTIPLIER = 1.5;
    private static final int ECG_WINDOW_SIZE = 10;

    private double[] recentReadings;

    private int currentIndex;
    
    public ECGAlertFactory() {
        this.recentReadings = new double[ECG_WINDOW_SIZE];
        this.currentIndex = 0;
    }
    
    @Override
    public Alert createAlert(int patientId, PatientRecord vitals, long timestamp) {
        validateParameters(patientId, vitals, timestamp);
        if (!"ECG".equals(vitals.vitalType)) {
            throw new IllegalArgumentException("Invalid vital type for ECG alert: " + vitals.vitalType);
        }
        double value = Double.parseDouble(vitals.vitalValue);

        recentReadings[currentIndex] = Math.abs(value);
        currentIndex = (currentIndex + 1) % ECG_WINDOW_SIZE;
        
        if (hasEnoughReadings()) {
            double average = calculateAverage();
            if (Math.abs(value) > average * ECG_ANOMALY_MULTIPLIER && average > 0) {
                String alertType = "ECGAnomaly";
                String description = String.format("Abnormal ECG reading detected: %.2f (average: %.2f, threshold: %.2f)",
                        value, average, average * ECG_ANOMALY_MULTIPLIER);
                return new Alert(patientId, vitals, timestamp, alertType, description);
            }
        }
        
        return null;
    }
    
    private boolean hasEnoughReadings() {
        for (double reading : recentReadings) {
            if (reading == 0.0) {
                return false;
            }
        }
        return true;
    }
    
    private double calculateAverage() {
        double sum = 0;
        for (double reading : recentReadings) {
            sum += reading;
        }
        return sum / ECG_WINDOW_SIZE;
    }
} 