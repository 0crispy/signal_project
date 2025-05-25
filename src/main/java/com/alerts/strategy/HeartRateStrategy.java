package com.alerts.strategy;

import com.alerts.Alert;
import com.alerts.PatientRecord;
import java.util.List;

public class HeartRateStrategy implements AlertStrategy {
    private static final double HEART_RATE_HIGH_THRESHOLD = 120.0;
    private static final double HEART_RATE_LOW_THRESHOLD = 50.0;
    private static final double HEART_RATE_CRITICAL_HIGH = 150.0;
    private static final double HEART_RATE_CRITICAL_LOW = 40.0;
    private static final double RAPID_CHANGE_THRESHOLD = 30.0; // bpm
    private static final long RAPID_CHANGE_TIME_WINDOW = 5 * 60 * 1000; // 5 mins
    
    @Override
    public Alert checkAlert(int patientId, PatientRecord currentRecord, List<com.data_management.PatientRecord> history) {
        if (!"HeartRate".equals(currentRecord.vitalType)) {
            return null;
        }
        double currentRate = Double.parseDouble(currentRecord.vitalValue );
        Alert thresholdAlert = checkThresholds(patientId, currentRecord, currentRate);
        if (thresholdAlert != null) {
            return thresholdAlert;
        }
        if (history != null && !history.isEmpty()) {
            return checkRapidChanges(patientId, currentRecord, history);
        }
        return null;
    }
    
    private Alert checkThresholds(int patientId, PatientRecord record, double rate) {
        if (rate >= HEART_RATE_CRITICAL_HIGH) {
            return new Alert(patientId, record, System.currentTimeMillis(),
                "CriticalTachycardia",
                String.format("Critical high heart rate: %.0f bpm (>150)", rate));
        } 
        else if (rate <= HEART_RATE_CRITICAL_LOW) {
            return new Alert(patientId, record, System.currentTimeMillis(),
                "CriticalBradycardia",
                String.format("Critical low heart rate: %.0f bpm (<40)", rate));
        } 
        else if (rate > HEART_RATE_HIGH_THRESHOLD) {
            return new Alert(patientId, record, System.currentTimeMillis(),
                "Tachycardia",
                String.format("High heart rate: %.0f bpm (>120)", rate));
        } 
        else if (rate < HEART_RATE_LOW_THRESHOLD) {
            return new Alert(patientId, record, System.currentTimeMillis(),
                "Bradycardia",
                String.format("Low heart rate: %.0f bpm (<50)", rate));
        }
        return null;
    }
    
    private Alert checkRapidChanges(int patientId, PatientRecord currentRecord, List<com.data_management.PatientRecord> history) {
       
        double currentRate = Double.parseDouble(currentRecord.vitalValue);

        long currentTime = System.currentTimeMillis();
        
        for (com.data_management.PatientRecord previousRecord : history) {
            long timeDiff = currentTime - previousRecord.getTimestamp();
            if (timeDiff > RAPID_CHANGE_TIME_WINDOW) {
                continue;
                 
            } 
            
            double previousRate  = previousRecord.getMeasurementValue();
            double rateChange = Math.abs(currentRate - previousRate) ;
            
            if (rateChange >= RAPID_CHANGE_THRESHOLD) {
                String changeType = currentRate > previousRate ? "increase" : "decrease";
                return new Alert(patientId, currentRecord,  System.currentTimeMillis(),
                    "RapidHeartRateChange",
                    String.format("Rapid heart rate %s: %.0f to %.0f bpm (change of %.0f bpm) in %d minutes",
                        changeType, previousRate, currentRate, rateChange, timeDiff / 60000));
            }
        }
        
        return null;
    }
    
    @Override
    public String getVitalType() {
        return "HeartRate";
    }
} 