package com.alerts.factory;

import com.alerts.Alert;
import com.alerts.PatientRecord;

public class BloodPressureAlertFactory extends AlertFactory {
    private static final double CRITICAL_HIGH_SYSTOLIC = 180.0;
    private static final double HIGH_SYSTOLIC = 140.0;
    private static final double LOW_SYSTOLIC = 90.0;
    private static final double CRITICAL_LOW_SYSTOLIC = 80.0;
    
    private static final double CRITICAL_HIGH_DIASTOLIC = 120.0;
    private static final double HIGH_DIASTOLIC = 90.0;
    private static final double LOW_DIASTOLIC = 60.0;
    private static final double CRITICAL_LOW_DIASTOLIC = 50.0;

    @Override
    public Alert createAlert(int patientId, PatientRecord vitals, long timestamp) {
        if ( vitals == null) {
            throw new IllegalArgumentException("Vitals cannot be null");
        }
        if (timestamp < 0) 
        {
            throw new IllegalArgumentException("Timestamp must be non-negative");
        }

        String type = vitals.vitalType;
        double value = Double.parseDouble(vitals.vitalValue);


        
        if ("SystolicPressure".equals(type)) {

            if (value >= CRITICAL_HIGH_SYSTOLIC) {
                return new Alert(patientId, vitals, timestamp, "CriticalHighSystolicPressure",
                    String.format("Critical high systolic pressure: %.1f mmHg", value));
            } else if (value >= HIGH_SYSTOLIC) {
                return new Alert(patientId, vitals, timestamp, "HighSystolicPressure",
                    String.format("High systolic pressure: %.1f mmHg", value));
            } else if (value <= CRITICAL_LOW_SYSTOLIC) {
                return new Alert(patientId, vitals, timestamp, "CriticalLowSystolicPressure",
                    String.format("Critical low systolic pressure: %.1f mmHg", value));
            } else if (value <= LOW_SYSTOLIC) {
                return new Alert(patientId, vitals, timestamp, "LowSystolicPressure",
                    String.format("Low systolic pressure: %.1f mmHg", value));

            }
        } 
        
        else if ("DiastolicPressure".equals(type)) {
            if (value >= CRITICAL_HIGH_DIASTOLIC) {
                return new Alert(patientId, vitals, timestamp, "CriticalHighDiastolicPressure",
                    String.format("Critical high diastolic pressure: %.1f mmHg", value));
            } else if (value >= HIGH_DIASTOLIC) {
                return new Alert(patientId, vitals, timestamp, "HighDiastolicPressure",
                    String.format("High diastolic pressure: %.1f mmHg", value));
            } else if (value <= CRITICAL_LOW_DIASTOLIC) {
                return new Alert(patientId, vitals, timestamp, "CriticalLowDiastolicPressure",
                    String.format("Critical low diastolic pressure: %.1f mmHg", value));
            } else if (value <= LOW_DIASTOLIC) {
                return new Alert(patientId, vitals, timestamp, "LowDiastolicPressure",
                    String.format("Low diastolic pressure: %.1f mmHg", value));

            }
        } 
        else {
            throw new IllegalArgumentException("Invalid vital type: " + type);
        }

        return null; // No alert needed for normal values
    }
} 