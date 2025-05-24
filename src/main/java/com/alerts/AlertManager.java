package com.alerts;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages alerts in a thread-safe way.
 */
public class AlertManager {
    private final Set<Alert> alerts;
    private final List<AlertListener> listeners;
    private final Object lock = new Object();

    public AlertManager() {
        this.alerts = Collections.synchronizedSet(new HashSet<>());
        this.listeners = new CopyOnWriteArrayList<>();
    }

    /**
     * Listener interface for alert notifications.
     */
    public interface AlertListener {
        /**
         * Called when a new alert comes in.
         * @param alert the new alert
         */
        void onAlert(Alert alert);
    }

    /**
     * Handles a new alert and notifies listeners if it's unique.
     * @param alert the new alert (cannot be null)
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
     * Adds a listener for alerts.
     * @param listener a listener to add
     */
    public void addListener(AlertListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes an alert listener.
     * @param listener the listener to remove
     */
    public void removeListener(AlertListener listener) {
        listeners.remove(listener);
    }

    /**
     * Returns all alerts.
     * @return a list of alerts
     */
    public List<Alert> getAllAlerts() {
        synchronized (lock) {
            return new ArrayList<>(alerts);
        }
    }

    /**
     * Returns alerts for a specific patient.
     * @param patientId the patient’s ID
     * @return a list of alerts for that patient
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
     * Clears all alerts.
     */
    public void clearAlerts() {
        synchronized (lock) {
            alerts.clear();
        }
    }

    /**
     * Notifies all listeners about a new alert.
     * @param alert the alert to send
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
