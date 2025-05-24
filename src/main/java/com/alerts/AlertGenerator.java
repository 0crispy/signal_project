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
 * Generates alerts by checking patient data.
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

    /**
     * Analyzes patient data and triggers alerts as needed.
     * @param patient the patient to evaluate
     */
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
     * Updates the history for each vital record.
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
     * Checks for immediate BP and saturation violations.
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
                if (value < SATURATION_LOW_THRESHOLD) {
                    triggerAlert(new Alert(patientId, record, System.currentTimeMillis(),
                            "LowBloodSaturation",
                            "Low blood oxygen saturation: " + value + "% (<92%)"));
                }
                break;
        }
    }

    /**
     * Checks for BP trends (increasing or decreasing).
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

            if ((val2 - val1) > BP_TREND_THRESHOLD && (val3 - val2) > BP_TREND_THRESHOLD) {
                triggerAlert(new Alert(patientId, history.get(i), System.currentTimeMillis(),
                        "BloodPressureIncreasingTrend",
                        "Increasing trend in " + pressureType.toLowerCase() + " blood pressure: " +
                                val1 + " → " + val2 + " → " + val3 + " mmHg"));
            }

            if ((val1 - val2) > BP_TREND_THRESHOLD && (val2 - val3) > BP_TREND_THRESHOLD) {
                triggerAlert(new Alert(patientId, history.get(i), System.currentTimeMillis(),
                        "BloodPressureDecreasingTrend",
                        "Decreasing trend in " + pressureType.toLowerCase() + " blood pressure: " +
                                val1 + " → " + val2 + " → " + val3 + " mmHg"));
            }
        }
    }

    /**
     * Checks if blood saturation drops quickly.
     */
    private void checkSaturationRapidDrop(int patientId) {
        List<PatientRecord> history = saturationHistory.get(patientId);
        if (history == null || history.size() < 2) {
            return;
        }

        for (int i = 1; i < history.size(); i++) {
            PatientRecord current = history.get(i);

            for (int j = i - 1; j >= 0; j--) {
                PatientRecord previous = history.get(j);
                long timeDiff = current.getTimestamp() - previous.getTimestamp();

                if (timeDiff > TEN_MINUTES_MS) {
                    break;
                }

                double drop = previous.getMeasurementValue() - current.getMeasurementValue();
                if (drop >= SATURATION_DROP_THRESHOLD) {
                    triggerAlert(new Alert(patientId, current, System.currentTimeMillis(),
                            "RapidSaturationDrop",
                            "Rapid drop in blood oxygen saturation: " +
                                    previous.getMeasurementValue() + "% to " + current.getMeasurementValue() +
                                    "% (drop of " + drop + "%) within " + (timeDiff / 60000) + " minutes"));
                    break;
                }
            }
        }
    }

    /**
     * Triggers an alert when both low BP and low saturation happen close in time.
     */
    private void checkHypotensiveHypoxemia(int patientId) {
        List<PatientRecord> systolicRecords = systolicHistory.get(patientId);
        List<PatientRecord> saturationRecords = saturationHistory.get(patientId);

        if (systolicRecords == null || saturationRecords == null) {
            return;
        }

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
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Detects ECG anomalies using a sliding window.
     */
    private void checkECGAnomalies(int patientId) {
        List<PatientRecord> history = ecgHistory.get(patientId);
        if (history == null || history.size() <= ECG_WINDOW_SIZE) {
            return;
        }

        for (int i = ECG_WINDOW_SIZE; i < history.size(); i++) {
            double sum = 0;
            for (int j = i - ECG_WINDOW_SIZE; j < i; j++) {
                sum += Math.abs(history.get(j).getMeasurementValue());
            }
            double average = sum / ECG_WINDOW_SIZE;

            PatientRecord current = history.get(i);
            double currentValue = Math.abs(current.getMeasurementValue());

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
     * Triggers a manual alert (button press).
     */
    private void checkManualAlert(int patientId, PatientRecord record) {
        if ("Alert".equals(record.getRecordType()) && record.getMeasurementValue() == 1.0) {
            triggerAlert(new Alert(patientId, record, System.currentTimeMillis(),
                    "ManualAlert",
                    "Manual alert triggered by patient or medical staff"));
        }
    }

    /**
     * Sends the alert through the alert manager.
     */
    private void triggerAlert(Alert alert) {
        alertManager.handleAlert(alert);
    }

    // Getter for testing
    public AlertManager getAlertManager() {
        return alertManager;
    }
}
