package com.alerts.thresholds;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a patient's threshold profile containing all vital sign thresholds.
 */
public class PatientThresholdProfile {
    private List<VitalsThreshold> thresholds;

    public PatientThresholdProfile() {
        this.thresholds = new ArrayList<>();
        initializeDefaultThresholds();
    }

    /**
     * Initialize default thresholds for common vital signs
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

    public List<VitalsThreshold> getThresholds() {
        return new ArrayList<>(thresholds);
    }

    public void addThreshold(VitalsThreshold threshold) {
        thresholds.add(threshold);
    }

    public void removeThreshold(String recordType) {
        thresholds.removeIf(threshold -> threshold.getRecordType().equals(recordType));
    }

    public VitalsThreshold getThresholdForType(String recordType) {
        return thresholds.stream()
                .filter(threshold -> threshold.getRecordType().equals(recordType))
                .findFirst()
                .orElse(null);
    }
}