package com.data_management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import com.alerts.AlertGenerator;
import com.alerts.AlertManager;
import com.alerts.thresholds.PatientThresholdProfile;

/**
 * Manages storage and retrieval of patient data within a healthcare monitoring
 * system.
 * This class serves as a repository for all patient records, organized by
 * patient IDs.
 */
public class DataStorage {
    private Map<Integer, Patient> patientMap; // Stores patient objects indexed by their unique patient ID.

    /**
     * Constructs a new instance of DataStorage, initializing the underlying storage
     * structure.
     */
    public DataStorage() {
        this.patientMap = new HashMap<>();
    }

    /**
     * Adds or updates patient data in the storage.
     * If the patient does not exist, a new Patient object is created and added to
     * the storage.
     * Otherwise, the new data is added to the existing patient's records.
     *
     * @param patientId        the unique identifier of the patient
     * @param measurementValue the value of the health metric being recorded
     * @param recordType       the type of record, e.g., "HeartRate",
     *                         "BloodPressure"
     * @param timestamp        the time at which the measurement was taken, in
     *                         milliseconds since the Unix epoch
     */
    public void addPatientData(int patientId, double measurementValue, String recordType, long timestamp) {
        Patient patient = patientMap.get(patientId);
        if (patient == null) {
            patient = new Patient(patientId);
            patientMap.put(patientId, patient);
        }
        patient.addRecord(measurementValue, recordType, timestamp);
    }

    /**
     * Retrieves a list of PatientRecord objects for a specific patient, filtered by
     * a time range.
     *
     * @param patientId the unique identifier of the patient whose records are to be
     *                  retrieved
     * @param startTime the start of the time range, in milliseconds since the Unix
     *                  epoch
     * @param endTime   the end of the time range, in milliseconds since the Unix
     *                  epoch
     * @return a list of PatientRecord objects that fall within the specified time
     *         range
     */
    public List<PatientRecord> getRecords(int patientId, long startTime, long endTime) {
        Patient patient = patientMap.get(patientId);
        if (patient != null) {
            return patient.getRecords(startTime, endTime);
        }
        else{
            return new ArrayList<>(); // return an empty list if no patient is found
        }
    }
    
    /**
     * Gets the threshold profile for a specific patient
     * 
     * @param patientId the ID of the patient
     * @return the patient's threshold profile, or null if the patient doesn't exist
     */
    public PatientThresholdProfile getPatientThresholdProfile(int patientId){
        Patient patient = patientMap.get(patientId);
        if (patient != null) {
            return patient.getThresholdProfile();
        }
        else{
            return null;
        }
    }

    /**
     * Retrieves a collection of all patients stored in the data storage.
     *
     * @return a list of all patients
     */
    public List<Patient> getAllPatients() {
        return new ArrayList<>(patientMap.values());
    }

    /**
     * The main method for the DataStorage class.
     * Initializes the system, reads data into storage, and continuously monitors
     * and evaluates patient data.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        DataStorage storage = new DataStorage();
        AlertManager alertManager = new AlertManager();
        
        // Example alert listener implementation to handle alerts
        alertManager.addListener(alert -> {
            System.out.println("ALERT RECEIVED: " + alert.getDescription());
            System.out.println("Patient ID: " + alert.getPatientId());
            System.out.println("Alert Type: " + alert.getAlertType());
            System.out.println("Timestamp: " + alert.getTimestamp());
            System.out.println("------------------------------------");
        });

        // Process command line arguments to find data directory
        String dataDir = null;
        for (String arg : args) {
            if (arg.startsWith("--input=")) {
                dataDir = arg.substring("--input=".length());
            }
        }

        if (dataDir != null) {
            try {
                // Initialize FileDataReader with the specified directory
                FileDataReader reader = new FileDataReader(dataDir);
                reader.readData(storage);
                System.out.println("Data loaded successfully from: " + dataDir);
            } catch (IOException e) {
                System.err.println("Error reading data: " + e.getMessage());
                System.exit(1);
            }
        } else {
            System.out.println("No input directory specified. Use --input=<directory> to load data.");
            // Continue with empty storage for demonstration
        }

        // Example of using DataStorage to retrieve and print records for a patient
        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        if (!records.isEmpty()) {
            System.out.println("Found " + records.size() + " records for patient ID 1");
            for (PatientRecord record : records) {
                System.out.println("Record: " + record.getRecordType() +
                        ", Value: " + record.getMeasurementValue() +
                        ", Timestamp: " + record.getTimestamp());
            }
        } else {
            System.out.println("No records found for patient ID 1");
        }

        // Initialize the AlertGenerator with the storage
        AlertGenerator alertGenerator = new AlertGenerator(storage, alertManager);

        // Evaluate all patients' data to check for conditions that may trigger alerts
        for (Patient patient : storage.getAllPatients()) {
            alertGenerator.evaluateData(patient);
        }
        
        System.out.println("Alert evaluation complete.");
    }
}
