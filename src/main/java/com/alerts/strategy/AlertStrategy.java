package com.alerts.strategy;

import com.alerts.Alert;
import com.alerts.PatientRecord;
import java.util.List;

public interface AlertStrategy {
    /**
     * checks if an alert should be generated based on the patient's vital records
     * 
     * @param patientId The Id of the patient
     * @param currentRecord The current vital record being evaluated
     * @param history Previous records for trend analysis
     * @return Alert if conditions are met, null otherwise
     */
    Alert checkAlert(int patientId, PatientRecord currentRecord, List<com.data_management.PatientRecord> history);
    
    /**
     * gets the type of vital sign of strategy monitor
     * 
     * @return the vital sign type (for example"BloodPressure", "HeartRate", etc.)
     */
    String getVitalType();
} 