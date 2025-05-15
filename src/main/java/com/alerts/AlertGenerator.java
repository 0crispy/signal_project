package com.alerts;

import com.alerts.thresholds.PatientThresholdProfile;
import com.alerts.thresholds.VitalsThreshold;
import com.data_management.DataStorage;
import com.data_management.Patient;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private DataStorage dataStorage;
    private AlertManager alertManager;
    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage, AlertManager alertManager) {
        this.dataStorage = dataStorage;
        this.alertManager = alertManager;
    }

    public void evaluateData(Patient patient) {
        // Some stuff
        
        /*
        PatientThresholdProfile profile = dataStorage.getPatientThresholdProfile(patientId);
        for (VitalsThreshold threshold : profile.getThresholds()) {
            if (threshold.checkThreshold(vitals)){
                // Threshold has been exceeded.
                // Generate alert.
                Alert alert = new Alert(patientId, vitals, timestamp);
                alertManager.handleAlert(alert);
                break;
            }   
        }
            */
    }

    private void triggerAlert(Alert alert) {
        alertManager.handleAlert(alert);
    }
}