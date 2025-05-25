package com.alerts.factory;

import com.alerts.Alert;
import com.alerts.PatientRecord;

public abstract class AlertFactory {
    
    /**
     * factory method to create an alert based on patient data
     * 
     * @param patientId The Id of the patient
     * @param vitals The vital measurements that triggered the alert
     * @param timestamp The time when the alert was generated
     * @return A new Alert instance
     */
    public abstract Alert createAlert(int patientId, PatientRecord vitals, long timestamp);
    
    /**
     * Helper method to validate common alert parameters
     * 
     * @param patientId The Id of the patient
     * @param vitals The vital measurements
     * @param timestamp The timestamp
     * @throws IllegalArgumentException if any parameter is invalid
     */
    protected void validateParameters(int patientId, PatientRecord vitals, long timestamp) {
        if (vitals == null) {
            throw new IllegalArgumentException("Vitals cannot be null");
        }
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Timestamp must be positive");
        }
    }
} 