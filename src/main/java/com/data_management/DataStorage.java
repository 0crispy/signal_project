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
 * system using the Singleton pattern.
 * This class serves as a repository for all patient records, organized by
 * patient IDs.
 */
public class DataStorage {
    private Map<Integer, Patient> patientMap; // Stores patient objects indexed by their unique patient ID.
    private static DataStorage instance;
    private static final Object LOCK = new Object();

    /**
     * Private constructor to prevent direct instantiation.
     * Initializes the underlying storage structure.
     */
    private DataStorage() {
        this.patientMap = new HashMap<>();
    }

    /**
     * Gets the singleton instance of DataStorage.
     * Creates it if it doesn't exist using double-checked locking.
     * 
     * @return the singleton instance of DataStorage
     */
    public static DataStorage getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new DataStorage();
                }
            }
        }
        return instance;
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
     * Clears all records from the data storage.
     */
    public void clearAllRecords() {
        patientMap.clear();
    }

    /**
     * Clears all records for a specific patient.
     * @param patientId the ID of the patient whose records should be cleared
     */
    public void clearRecords(int patientId) {
        patientMap.remove(patientId);
    }

    /**
     * The main method for the DataStorage class.
     * Initializes the system, reads data into storage, and continuously monitors
     * and evaluates patient data.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        DataStorage storage = DataStorage.getInstance();
        AlertManager alertManager = new AlertManager();
        
        // Example alert listener implementation to handle alerts
        alertManager.addListener(alert -> {
            System.out.println("ALERT RECEIVED: " + alert.getDescription());
            System.out.println("Patient ID: " + alert.getPatientId());
            System.out.println("Alert Type: " + alert.getAlertType());
            System.out.println("Timestamp: " + alert.getTimestamp());
            System.out.println("------------------------------------");
        });

        // Process command line arguments
        String dataDir = null;
        String wsUrl = null;
        DataReader reader = null;

        for (String arg : args) {
            if (arg.startsWith("--input=")) {
                dataDir = arg.substring("--input=".length());
            } else if (arg.startsWith("--websocket=")) {
                wsUrl = arg.substring("--websocket=".length());
            }
        }

        try {
            if (wsUrl != null) {
                reader = new WebSocketDataReader(wsUrl);
                System.out.println("Connecting to WebSocket server at: " + wsUrl);
            } else if (dataDir != null) {
                reader = new FileDataReader(dataDir);
                System.out.println("Reading data from directory: " + dataDir);
            } else {
                System.out.println("No input source specified. Use --input=<directory> for file input or --websocket=<url> for WebSocket connection.");
                System.exit(1);
            }

            reader.readData(storage);

            // initialize the AlertGenerator with the storag
            AlertGenerator alertGenerator = new AlertGenerator(storage, alertManager);

            // for WebSocket connections, keep the application running
            if (wsUrl != null) {
                System.out.println("WebSocket connection established. Waiting for real-time data...");
                // Keep the main thread alive
                while (true) {
                    Thread.sleep(1000);
                    // Evaluate  all patients' data periodically
                    for (Patient  patient : storage.getAllPatients()) {
                        alertGenerator.evaluateData(patient);
                    }
                }
            } 
            else {
                // For file input, evaluate once and exit
                for (Patient patient : storage.getAllPatients()) {
                    alertGenerator.evaluateData(patient);
                }
                System.out.println("Data processing complete.");
            }

        } 
        catch (IOException e) {
            System.err.println("Error processing data: " + e.getMessage());
            System.exit(1);
        } 
        catch (InterruptedException e) {
            System.err.println("Application interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            System.exit(1);
        }
    }
}
