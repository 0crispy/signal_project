package com.alerts.thresholds;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a patient’s vital thresholds. Comes with default settings.
 */
public class PatientThresholdProfile {
    private List<VitalsThreshold> thresholds;

    /**
     * Builds a profile with default thresholds.
     */
    public PatientThresholdProfile() {
        this.thresholds = new ArrayList<>();
        initializeDefaultThresholds();
    }

    /**
     * Sets up default thresholds for common vitals.
     */
    private void initializeDefaultThresholds() {
        // Blood Pressure thresholds
        thresholds.add(new VitalsThreshold("BloodPressure", 90, 180, 60, 120,
                "Blood pressure outside normal range"));

        // Blood Saturation thresholds
        thresholds.add(new VitalsThreshold("BloodSaturation", 92, 100, 92, 100,
                "Blood oxygen saturation below normal"));

        // Heart Rate thresholds
        thresholds.add(new VitalsThreshold("HeartRate", 60, 100, 40, 150,
                "Heart rate outside normal range"));

        // ECG thresholds (will be handled by sliding window analysis)
        thresholds.add(new VitalsThreshold("ECG", -1000, 1000, -2000, 2000,
                "ECG reading abnormal"));
    }

    /**
     * Returns a copy of the thresholds.
     */
    public List<VitalsThreshold> getThresholds() {
        return new ArrayList<>(thresholds);
    }

    /**
     * Adds a new vital threshold.
     */
    public void addThreshold(VitalsThreshold threshold) {
        thresholds.add(threshold);
    }

    /**
     * Removes threshold by type.
     */
    public void removeThreshold(String recordType) {
        thresholds.removeIf(threshold -> threshold.getRecordType().equals(recordType));
    }

    /**
     * Finds the threshold for the given vital type.
     */
    public VitalsThreshold getThresholdForType(String recordType) {
        return thresholds.stream()
                .filter(threshold -> threshold.getRecordType().equals(recordType))
                .findFirst()
                .orElse(null);
    }
}
