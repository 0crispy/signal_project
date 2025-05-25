package com.alerts.strategy;

import com.alerts.Alert;
import com.alerts.PatientRecord;
import java.util.List;

public class BloodPressureStrategy implements AlertStrategy {
    private static final double SYSTOLIC_HIGH_THRESHOLD = 180.0;
    private static final double SYSTOLIC_LOW_THRESHOLD = 90.0;
    private static final double DIASTOLIC_HIGH_THRESHOLD = 120.0;
    private static final double DIASTOLIC_LOW_THRESHOLD = 60.0;
    private static final double BP_TREND_THRESHOLD = 10.0;
    
    @Override
    public Alert checkAlert(int patientId, PatientRecord currentRecord, List<com.data_management.PatientRecord> history) {
        String recordType =  currentRecord.vitalType;
        double value = Double.parseDouble(currentRecord.vitalValue );
        Alert immediateAlert = checkThresholds(patientId, currentRecord,  value);
        if (immediateAlert != null ) {
            return immediateAlert;
        }
        if (history != null && history.size() >= 3) {
            return checkTrend(patientId, currentRecord, history);
        }
        
        return null; 
    }
    
    private Alert checkThresholds(int patientId, PatientRecord record, double value) {
        if ("SystolicPressure".equals(record.vitalType)) {

            if (value > SYSTOLIC_HIGH_THRESHOLD) {
                return new Alert(patientId, record, System.currentTimeMillis(),
                    "CriticalHighSystolicPressure",
                    String.format("Critical high systolic pressure: %.1f mmHg (>180)", value));
            } 
            else if (value < SYSTOLIC_LOW_THRESHOLD) {
                return new Alert(patientId, record, System.currentTimeMillis(),
                    "CriticalLowSystolicPressure",
                    String.format("Critical low systolic pressure: %.1f mmHg (<90)", value));
            }

        } 
        else if ("DiastolicPressure".equals(record.vitalType)) {

            if (value > DIASTOLIC_HIGH_THRESHOLD) {
                return new Alert(patientId, record, System.currentTimeMillis(),
                    "CriticalHighDiastolicPressure",
                    String.format("Critical high diastolic pressure: %.1f mmHg (>120)", value));
            } 
            else if (value < DIASTOLIC_LOW_THRESHOLD) {
                return new Alert(patientId, record, System.currentTimeMillis(),
                    "CriticalLowDiastolicPressure",
                    String.format("Critical low diastolic pressure: %.1f mmHg (<60)", value));
            }

        }
        return null;
    }
    
    private Alert checkTrend(int patientId, PatientRecord currentRecord, List<com.data_management.PatientRecord> history) {
        int lastIndex = history.size() - 1;

        double val1 = history.get(lastIndex - 2).getMeasurementValue();
        double val2 = history.get(lastIndex - 1).getMeasurementValue();
        double val3 = Double.parseDouble(currentRecord.vitalValue);
        
        String pressureType = currentRecord.vitalType.contains("Systolic") ? "systolic" : "diastolic";
         
        if ((val2 - val1) > BP_TREND_THRESHOLD  && (val3 - val2) >  BP_TREND_THRESHOLD
        ) {
            return new Alert(patientId, currentRecord, System.currentTimeMillis(),
                "BloodPressureIncreasingTrend",
                String.format("Increasing trend in %s blood pressure: %.1f → %.1f → %.1f mmHg",
                    pressureType, val1, val2, val3));
        }
        
        if ((val1 - val2) >  BP_TREND_THRESHOLD && (val2 - val3) > BP_TREND_THRESHOLD) {
            return new Alert(patientId,  currentRecord, System.currentTimeMillis(),
                "BloodPressureDecreasingTrend",
                String.format("Decreasing trend in %s blood pressure: %.1f → %.1f → %.1f mmHg",
                    pressureType, val1, val2, val3));
        }
        
        return null;
    }
    
    @Override
    public String getVitalType() {
        return "BloodPressure";
    }
} 