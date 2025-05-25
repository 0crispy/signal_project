package com.alerts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alerts.factory.AlertFactory;
import com.alerts.factory.BloodPressureAlertFactory;
import com.alerts.factory.BloodOxygenAlertFactory;
import com.alerts.factory.ECGAlertFactory;
import com.alerts.strategy.AlertStrategy;
import com.alerts.strategy.BloodPressureStrategy;
import com.alerts.strategy.HeartRateStrategy;
import com.alerts.strategy.OxygenSaturationStrategy;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

public class AlertGenerator {
    private DataStorage dataStorage;
    private AlertManager alertManager;
    private Map<String, AlertFactory> alertFactories;
    private List<AlertStrategy> alertStrategies;
    
    private Map<String, Map<Integer, List<PatientRecord>>> vitalHistories;
    
    public AlertGenerator(DataStorage dataStorage, AlertManager alertManager) {
        this.dataStorage = dataStorage;
        this.alertManager = alertManager;
        this.vitalHistories = new HashMap<>();
        initializeFactories();
        initializeStrategies();
    }
    
    private void initializeFactories() {
        alertFactories = new HashMap<>();
        
        BloodPressureAlertFactory bpFactory = new BloodPressureAlertFactory();
        alertFactories.put("SystolicPressure", bpFactory);
        alertFactories.put("DiastolicPressure", bpFactory);
        BloodOxygenAlertFactory boFactory = new BloodOxygenAlertFactory();
        alertFactories.put("BloodSaturation", boFactory);
        alertFactories.put("Saturation", boFactory);
        ECGAlertFactory ecgFactory = new ECGAlertFactory();
        alertFactories.put("ECG", ecgFactory);
    }
    
    private void initializeStrategies() {
        alertStrategies = new ArrayList<>();
        alertStrategies.add(new BloodPressureStrategy());
        alertStrategies.add(new HeartRateStrategy());
        alertStrategies.add(new OxygenSaturationStrategy());
    }
    
    /**
     * Analyzes patient data and triggers alerts using appropriate strategies.
     * @param patient the patient to evaluate
     */
    public void evaluateData(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Patient cannot be null");
        }

        int patientId = patient.getPatientId();
        List<PatientRecord> records = patient.getAllRecords();
        
        records.sort((r1, r2) -> Long.compare(r1.getTimestamp(), r2.getTimestamp()));
        
        for (PatientRecord record : records) {
            updateHistory(patientId, record);
            processRecord(patientId, record);
        }
    }

    private void updateHistory(int patientId, PatientRecord record) {
        String vitalType = record.getRecordType();
        vitalHistories.computeIfAbsent(vitalType, k -> new HashMap<>());
        vitalHistories.get(vitalType).computeIfAbsent(patientId, k -> new ArrayList<>());
        vitalHistories.get(vitalType).get(patientId).add(record);
    }

    private void processRecord(int patientId, PatientRecord record) {
        String vitalType = record.getRecordType();
        long timestamp = record.getTimestamp();

        AlertFactory factory = alertFactories.get(vitalType);
        if (factory != null) {
            Alert alert = factory.createAlert(patientId, new com.alerts.PatientRecord(vitalType, String.valueOf(record.getMeasurementValue())), timestamp);
            if (alert != null) {
                alertManager.handleAlert(alert);
            }
        }

        List<PatientRecord> history = vitalHistories.getOrDefault(vitalType, new HashMap<>())
                .getOrDefault(patientId, new ArrayList<>());
        for (AlertStrategy strategy : alertStrategies) {
            Alert alert = strategy.checkAlert(patientId, 
                new com.alerts.PatientRecord(vitalType, String.valueOf(record.getMeasurementValue())), 
                history);
            if (alert != null) {
                alertManager.handleAlert(alert);
            }
        }
    }
    
    public AlertManager getAlertManager() {
        return alertManager;
    }
}
