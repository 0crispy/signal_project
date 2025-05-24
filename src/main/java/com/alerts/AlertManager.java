package com.alerts;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe manager for handling and tracking patient alerts.
 */
public class AlertManager {
    private final Set<Alert> alerts;
    private final List<AlertListener> listeners;
    private final Object lock = new Object();

    public AlertManager() {
        this.alerts = Collections.synchronizedSet(new HashSet<>());
        this.listeners = new CopyOnWriteArrayList<>();
    }

    public interface AlertListener {
        void onAlert(Alert alert);
    }

    /**
     * Handles a new alert, ignoring duplicates
     * @param alert the alert to handle
     * @throws IllegalArgumentException if alert is null
     */
    public void handleAlert(Alert alert) {
        if (alert == null) {
            throw new IllegalArgumentException("Alert cannot be null");
        }

        synchronized (lock) {
            if (alerts.add(alert)) {
                notifyListeners(alert);
            }
        }
    }

    /**
     * Adds a listener to be notified of new alerts
     * @param listener the listener to add
     */
    public void addListener(AlertListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener
     * @param listener the listener to remove
     */
    public void removeListener(AlertListener listener) {
        listeners.remove(listener);
    }

    /**
     * Gets all stored alerts
     * @return a copy of all alerts
     */
    public List<Alert> getAllAlerts() {
        synchronized (lock) {
            return new ArrayList<>(alerts);
        }
    }

    /**
     * Gets alerts for a specific patient
     * @param patientId the patient ID
     * @return list of alerts for the patient
     */
    public List<Alert> getAlertsForPatient(int patientId) {
        synchronized (lock) {
            List<Alert> patientAlerts = new ArrayList<>();
            for (Alert alert : alerts) {
                if (alert.getPatientId() == patientId) {
                    patientAlerts.add(alert);
                }
            }
            return patientAlerts;
        }
    }

    /**
     * Clears all stored alerts
     */
    public void clearAlerts() {
        synchronized (lock) {
            alerts.clear();
        }
    }

    /**
     * Notifies all listeners about a new alert
     * @param alert the alert to notify about
     */
    private void notifyListeners(Alert alert) {
        for (AlertListener listener : listeners) {
            try {
                listener.onAlert(alert);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }
}