package com.alerts;

/**
 * Interface defining the contract for all alerts in the system.
 */
public interface IAlert {
    /**
     * gets the patient ID associated with this alert.
     * @return the patient ID
     */
    int getPatientId();

    /**
     * gets the vial measurements that triggered this alert.
     * @return the vital measurements
     */
    PatientRecord getVitals();

    /**
     * gets the timestamp when this alert was generated
     * @return the timestamp in milliseconds since epoch
     */
    long getTimestamp();

    /**
     * Gets the type of this alert
     * @return the alert type
     */
    String getAlertType();

    /**
     * gets the descripion of this alert.
     * @return the alert description
     */
    String getDescription();
} 