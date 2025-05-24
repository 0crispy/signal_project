package com.alerts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alerts.thresholds.PatientThresholdProfile;
import com.alerts.thresholds.VitalsThreshold;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

/**
 * Enhanced AlertGenerator that fully implements all required alert types
 */
public class AlertGenerator {
    private DataStorage dataStorage;
    private AlertManager alertManager;

    // History tracking for trend analysis
    private Map<Integer, List<PatientRecord>> systolicHistory;
    private Map<Integer, List<PatientRecord>> diastolicHistory;
    private Map<Integer, List<PatientRecord>> saturationHistory;
    private Map<Integer, List<PatientRecord>> ecgHistory;

    // Constants for alert thresholds
    private static final double SYSTOLIC_HIGH_THRESHOLD = 180.0;
    private static final double SYSTOLIC_LOW_THRESHOLD = 90.0;
    private static final double DIASTOLIC_HIGH_THRESHOLD = 120.0;
    private static final double DIASTOLIC_LOW_THRESHOLD = 60.0;
    private static final double SATURATION_LOW_THRESHOLD = 92.0;
    private static final double BP_TREND_THRESHOLD = 10.0;
    private static final double SATURATION_DROP_THRESHOLD = 5.0;
    private static final long TEN_MINUTES_MS = 10 * 60 * 1000;
    private static final long FIVE_MINUTES_MS = 5 * 60 * 1000;
    private static final int ECG_WINDOW_SIZE = 10;
    private static final double ECG_ANOMALY_MULTIPLIER = 1.5;

    public AlertGenerator(DataStorage dataStorage, AlertManager alertManager) {
        this.dataStorage = dataStorage;
        this.alertManager = alertManager;
        this.systolicHistory = new HashMap<>();
        this.diastolicHistory = new HashMap<>();
        this.saturationHistory = new HashMap<>();
        this.ecgHistory = new HashMap<>();
    }

    public void evaluateData(Patient patient) {
        int patientId = patient.getPatientId();
        List<PatientRecord> records = patient.getAllRecords();

        // Sort records by timestamp
        records.sort((r1, r2) -> Long.compare(r1.getTimestamp(), r2.getTimestamp()));

        // Process each record and update histories
        for (PatientRecord record : records) {
            updateHistory(patientId, record);

            // Check immediate threshold violations
            checkImmediateThresholds(patientId, record);

            // Check for manual alerts
            checkManualAlert(patientId, record);
        }

        // Perform trend and pattern analysis
        checkBloodPressureTrends(patientId);
        checkSaturationRapidDrop(patientId);
        checkHypotensiveHypoxemia(patientId);
        checkECGAnomalies(patientId);
    }

    /**
     * Updates patient history for different vital types
     */
    private void updateHistory(int patientId, PatientRecord record) {
        String recordType = record.getRecordType();

        switch (recordType) {
            case "SystolicPressure":
                systolicHistory.computeIfAbsent(patientId, k -> new ArrayList<>()).add(record);
                break;
            case "DiastolicPressure":
                diastolicHistory.computeIfAbsent(patientId, k -> new ArrayList<>()).add(record);
                break;
            case "BloodSaturation":
            case "Saturation":
                saturationHistory.computeIfAbsent(patientId, k -> new ArrayList<>()).add(record);
                break;
            case "ECG":
                ecgHistory.computeIfAbsent(patientId, k -> new ArrayList<>()).add(record);
                break;
        }
    }

    /**
     * 1. Blood Pressure Critical Threshold Alerts
     */
    private void checkImmediateThresholds(int patientId, PatientRecord record) {
        String recordType = record.getRecordType();
        double value = record.getMeasurementValue();

        switch (recordType) {
            case "SystolicPressure":
                if (value > SYSTOLIC_HIGH_THRESHOLD) {
                    triggerAlert(new Alert(patientId, record, System.currentTimeMillis(),
                            "CriticalHighSystolicPressure",
                            "Critical high systolic pressure: " + value + " mmHg (>180)"));
                } else if (value < SYSTOLIC_LOW_THRESHOLD) {
                    triggerAlert(new Alert(patientId, record, System.currentTimeMillis(),
                            "CriticalLowSystolicPressure",
                            "Critical low systolic pressure: " + value + " mmHg (<90)"));
                }
                break;

            case "DiastolicPressure":
                if (value > DIASTOLIC_HIGH_THRESHOLD) {
                    triggerAlert(new Alert(patientId, record, System.currentTimeMillis(),
                            "CriticalHighDiastolicPressure",
                            "Critical high diastolic pressure: " + value + " mmHg (>120)"));
                } else if (value < DIASTOLIC_LOW_THRESHOLD) {
                    triggerAlert(new Alert(patientId, record, System.currentTimeMillis(),
                            "CriticalLowDiastolicPressure",
                            "Critical low diastolic pressure: " + value + " mmHg (<60)"));
                }
                break;

            case "BloodSaturation":
            case "Saturation":
                // 2. Blood Saturation Low Alert
                if (value < SATURATION_LOW_THRESHOLD) {
                    triggerAlert(new Alert(patientId, record, System.currentTimeMillis(),
                            "LowBloodSaturation",
                            "Low blood oxygen saturation: " + value + "% (<92%)"));
                }
                break;
        }
    }

    /**
     * 1. Blood Pressure Trend Alerts
     */
    private void checkBloodPressureTrends(int patientId) {
        checkPressureTrend(patientId, systolicHistory.get(patientId), "Systolic");
        checkPressureTrend(patientId, diastolicHistory.get(patientId), "Diastolic");
    }

    private void checkPressureTrend(int patientId, List<PatientRecord> history, String pressureType) {
        if (history == null || history.size() < 3) {
            return;
        }

        // Check last 3 readings for trends
        for (int i = 2; i < history.size(); i++) {
            double val1 = history.get(i-2).getMeasurementValue();
            double val2 = history.get(i-1).getMeasurementValue();
            double val3 = history.get(i).getMeasurementValue();

            // Check for increasing trend (each reading increases by >10 mmHg)
            if ((val2 - val1) > BP_TREND_THRESHOLD && (val3 - val2) > BP_TREND_THRESHOLD) {
                triggerAlert(new Alert(patientId, history.get(i), System.currentTimeMillis(),
                        "BloodPressureIncreasingTrend",
                        "Increasing trend in " + pressureType.toLowerCase() + " blood pressure: " +
                                val1 + " → " + val2 + " → " + val3 + " mmHg"));
            }

            // Check for decreasing trend (each reading decreases by >10 mmHg)
            if ((val1 - val2) > BP_TREND_THRESHOLD && (val2 - val3) > BP_TREND_THRESHOLD) {
                triggerAlert(new Alert(patientId, history.get(i), System.currentTimeMillis(),
                        "BloodPressureDecreasingTrend",
                        "Decreasing trend in " + pressureType.toLowerCase() + " blood pressure: " +
                                val1 + " → " + val2 + " → " + val3 + " mmHg"));
            }
        }
    }

    /**
     * 2. Blood Saturation Rapid Drop Alert
     */
    private void checkSaturationRapidDrop(int patientId) {
        List<PatientRecord> history = saturationHistory.get(patientId);
        if (history == null || history.size() < 2) {
            return;
        }

        for (int i = 1; i < history.size(); i++) {
            PatientRecord current = history.get(i);

            // Check all previous readings within 10-minute window
            for (int j = i - 1; j >= 0; j--) {
                PatientRecord previous = history.get(j);
                long timeDiff = current.getTimestamp() - previous.getTimestamp();

                if (timeDiff > TEN_MINUTES_MS) {
                    break; // Outside 10-minute window
                }

                double drop = previous.getMeasurementValue() - current.getMeasurementValue();
                if (drop >= SATURATION_DROP_THRESHOLD) {
                    triggerAlert(new Alert(patientId, current, System.currentTimeMillis(),
                            "RapidSaturationDrop",
                            "Rapid drop in blood oxygen saturation: " +
                                    previous.getMeasurementValue() + "% to " + current.getMeasurementValue() +
                                    "% (drop of " + drop + "%) within " + (timeDiff / 60000) + " minutes"));
                    break; // Only alert once per reading
                }
            }
        }
    }

    /**
     * 3. Hypotensive Hypoxemia Alert
     */
    private void checkHypotensiveHypoxemia(int patientId) {
        List<PatientRecord> systolicRecords = systolicHistory.get(patientId);
        List<PatientRecord> saturationRecords = saturationHistory.get(patientId);

        if (systolicRecords == null || saturationRecords == null) {
            return;
        }

        // Check for concurrent low systolic pressure and low saturation
        for (PatientRecord systolicRecord : systolicRecords) {
            if (systolicRecord.getMeasurementValue() < SYSTOLIC_LOW_THRESHOLD) {

                for (PatientRecord satRecord : saturationRecords) {
                    if (satRecord.getMeasurementValue() < SATURATION_LOW_THRESHOLD) {

                        long timeDiff = Math.abs(systolicRecord.getTimestamp() - satRecord.getTimestamp());
                        if (timeDiff <= FIVE_MINUTES_MS) {
                            triggerAlert(new Alert(patientId, systolicRecord, System.currentTimeMillis(),
                                    "HypotensiveHypoxemia",
                                    "Critical condition: Low systolic pressure (" +
                                            systolicRecord.getMeasurementValue() + " mmHg) and low oxygen saturation (" +
                                            satRecord.getMeasurementValue() + "%) detected within 5 minutes"));
                            return; // Only alert once per evaluation
                        }
                    }
                }
            }
        }
    }

    /**
     * 4. ECG Anomaly Detection using sliding window
     */
    private void checkECGAnomalies(int patientId) {
        List<PatientRecord> history = ecgHistory.get(patientId);
        if (history == null || history.size() <= ECG_WINDOW_SIZE) {
            return;
        }

        for (int i = ECG_WINDOW_SIZE; i < history.size(); i++) {
            // Calculate average of previous window
            double sum = 0;
            for (int j = i - ECG_WINDOW_SIZE; j < i; j++) {
                sum += Math.abs(history.get(j).getMeasurementValue());
            }
            double average = sum / ECG_WINDOW_SIZE;

            PatientRecord current = history.get(i);
            double currentValue = Math.abs(current.getMeasurementValue());

            // Check if current reading is significantly above average
            if (currentValue > average * ECG_ANOMALY_MULTIPLIER && average > 0) {
                triggerAlert(new Alert(patientId, current, System.currentTimeMillis(),
                        "ECGAnomaly",
                        "Abnormal ECG reading detected: " + current.getMeasurementValue() +
                                " (average: " + String.format("%.2f", average) + ", threshold: " +
                                String.format("%.2f", average * ECG_ANOMALY_MULTIPLIER) + ")"));
            }
        }
    }

    /**
     * 5. Manual/Triggered Alert
     */
    private void checkManualAlert(int patientId, PatientRecord record) {
        if ("Alert".equals(record.getRecordType()) && record.getMeasurementValue() == 1.0) {
            triggerAlert(new Alert(patientId, record, System.currentTimeMillis(),
                    "ManualAlert",
                    "Manual alert triggered by patient or medical staff"));
        }
    }

    /**
     * Triggers an alert through the alert manager
     */
    private void triggerAlert(Alert alert) {
        alertManager.handleAlert(alert);
    }

    // Getter for testing
    public AlertManager getAlertManager() {
        return alertManager;
    }
}