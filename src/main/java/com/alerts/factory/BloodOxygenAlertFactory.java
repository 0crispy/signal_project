package com.alerts.factory;

import com.alerts.Alert;
import com.alerts.PatientRecord;

public class BloodOxygenAlertFactory extends AlertFactory {
    
    private static final double SATURATION_LOW_THRESHOLD = 92.0;
    
    @Override
    public Alert createAlert(int patientId, PatientRecord vitals, long timestamp) {
        validateParameters(patientId, vitals, timestamp);
        
        if (!("BloodSaturation".equals(vitals.vitalType) || "Saturation".equals(vitals.vitalType))) {
            throw new IllegalArgumentException("Invalid vital type for blood oxygen alert: " + vitals.vitalType);
        }
        
        double value = Double.parseDouble(vitals.vitalValue);
        
        if (value < SATURATION_LOW_THRESHOLD) {
            String alertType = "LowBloodSaturation";
            String description = String.format("Low blood oxygen saturation: %.1f%% (<92%%)", value);
            return new Alert(patientId, vitals, timestamp, alertType, description);
        }
        
        return null;
    }
} 