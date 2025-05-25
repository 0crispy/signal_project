package com.alerts.strategy;

import com.alerts.Alert;
import com.alerts.PatientRecord;
import java.util.List;

public class OxygenSaturationStrategy implements AlertStrategy {
    private static final double SATURATION_LOW_THRESHOLD = 92.0;
    private static final double SATURATION_CRITICAL_THRESHOLD = 88.0;
    private static final double SATURATION_DROP_THRESHOLD = 5.0;
    private static final long RAPID_DROP_TIME_WINDOW = 10 * 60 * 1000; // 10 mins, idk
    
    @Override
    public Alert checkAlert(int patientId, PatientRecord currentRecord, List<com.data_management.PatientRecord> history) {
        
        if (!("BloodSaturation".equals(currentRecord.vitalType) || 
              "Saturation".equals(currentRecord.vitalType))) {

            return null;
        }
        
        double currentSaturation = Double.parseDouble(currentRecord.vitalValue);
        Alert thresholdAlert = checkThresholds(patientId, currentRecord, currentSaturation);
        if (thresholdAlert != null) {
            return thresholdAlert;
        }
        if (history != null && !history.isEmpty()) {
            return checkRapidDrop(patientId, currentRecord, history);
        }
        
        return null;
    }
    
    private Alert checkThresholds(int patientId, PatientRecord record, double saturation) {
        if (saturation <= SATURATION_CRITICAL_THRESHOLD) {
            return new Alert(patientId, record, System.currentTimeMillis(),
                "CriticalLowOxygenSaturation",
                String.format("Critical low oxygen saturation: %.1f%% (≤88%%)", saturation));
        } else if (saturation < SATURATION_LOW_THRESHOLD) {
            return new Alert(patientId, record, System.currentTimeMillis(),
                "LowOxygenSaturation",
                String.format("Low oxygen saturation: %.1f%% (<92%%)", saturation));
        }
        return null;
    }
    
    private Alert checkRapidDrop(int patientId, PatientRecord currentRecord, List<com.data_management.PatientRecord> history) {
        double currentSaturation = Double.parseDouble(currentRecord.vitalValue);
        long currentTime = System.currentTimeMillis();
        
        for (com.data_management.PatientRecord previousRecord : history) {
            long timeDiff = currentTime - previousRecord.getTimestamp();
            if (timeDiff > RAPID_DROP_TIME_WINDOW) {
                continue;
            }
            
            double previousSaturation = previousRecord.getMeasurementValue();
            double drop = previousSaturation - currentSaturation;
            
            if (drop >= SATURATION_DROP_THRESHOLD) {
                return new Alert(patientId, currentRecord, System.currentTimeMillis(),
                    "RapidSaturationDrop",
                    String.format("Rapid drop in oxygen saturation: %.1f%% to %.1f%% (drop of %.1f%%) in %d minutes",
                        previousSaturation, currentSaturation, drop, timeDiff / 60000));
            }
        }
        return null;
    }
    
    @Override
    public String getVitalType() {
        return "OxygenSaturation";
    }
} 