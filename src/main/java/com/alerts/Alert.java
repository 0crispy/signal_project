package com.alerts;

// Represents an alert
public class Alert {
    private int patientId;
    private PatientRecord vitals;
    private long timestamp;

    public Alert(int patientId, PatientRecord vitals, long timestamp) {
        this.patientId = patientId;
        this.vitals = vitals;
        this.timestamp = timestamp;
    }

    public int getPatientId() {
        return this.patientId;
    }

    public PatientRecord getVitals() {
        return this.vitals;
    }

    public long getTimestamp() {
        return this.timestamp;
    }
}
