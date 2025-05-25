package com.alerts;

import java.util.Objects;

/**
 * Represents an alert triggered for a patient based on vital signs monitoring.
 * Contains information about the patient, the vital sign reading that triggered
 * the alert, and the time the alert was generated.
 */
public class Alert implements IAlert {
    private final int patientId;
    private final PatientRecord vitals;
    private final long timestamp;
    private final String alertType;
    private final String description;

    /**
     * Creates a new alert for a specific patient
     *
     * @param patientId  the ID of the patient
     * @param vitals     the vital measurements that triggered the alert
     * @param timestamp  the time when the alert was generated (milliseconds since epoch)
     * @param alertType  the type of alert (e.g., "BloodPressure", "BloodSaturation")
     * @param description a description of the alert condition
     * @throws IllegalArgumentException if any required field is null or invalid
     */
    public Alert(int patientId, PatientRecord vitals, long timestamp, String alertType, String description) {
        if (vitals == null) {
            throw new IllegalArgumentException("Vitals cannot be null");
        }
        if (alertType == null || alertType.trim().isEmpty()) {
            throw new IllegalArgumentException("Alert type cannot be null or empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Timestamp must be positive");
        }

        this.patientId = patientId;
        this.vitals = vitals;
        this.timestamp = timestamp;
        this.alertType = alertType.trim();
        this.description = description.trim();
    }

    @Override
    public int getPatientId() {
        return this.patientId;
    }

    @Override
    public PatientRecord getVitals() {
        return this.vitals;
    }

    @Override
    public long getTimestamp() {
        return this.timestamp;
    }

    @Override
    public String getAlertType() {
        return this.alertType;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * Equality comparison based on patient ID, alert type, and timestamp
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Alert alert = (Alert) o;
        return patientId == alert.patientId &&
                timestamp == alert.timestamp &&
                alertType.equals(alert.alertType);
    }

    /**
     * Hash code consistent with equals() implementation
     */
    @Override
    public int hashCode() {
        return Objects.hash(patientId, alertType, timestamp);
    }

    @Override
    public String toString() {
        return "Alert{" +
                "patientId=" + patientId +
                ", type='" + alertType + '\'' +
                ", description='" + description + '\'' +
                ", timestamp=" + timestamp +
                ", vitals=" + vitals +
                '}';
    }
}