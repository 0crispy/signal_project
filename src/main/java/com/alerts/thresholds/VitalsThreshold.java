package com.alerts.thresholds;

import com.data_management.PatientRecord;

/**
 * Holds threshold limits for a vital sign.
 */
public class VitalsThreshold {
    private String recordType;
    private double minNormal;
    private double maxNormal;
    private double criticalLow;
    private double criticalHigh;
    private String description;

    /**
     * Creates a threshold with the given limits and a short description.
     */
    public VitalsThreshold(String recordType, double minNormal, double maxNormal,
                           double criticalLow, double criticalHigh, String description) {
        this.recordType = recordType;
        this.minNormal = minNormal;
        this.maxNormal = maxNormal;
        this.criticalLow = criticalLow;
        this.criticalHigh = criticalHigh;
        this.description = description;
    }

    /**
     * Checks if a record breaks the threshold.
     * @param record the record to check
     * @return true if it violates the limit, false otherwise
     */
    public boolean checkThreshold(PatientRecord record) {
        if (!isValidRecordType(record.getRecordType())) {
            throw new IllegalArgumentException("Invalid vital type: " + record.getRecordType());
        }

        if (!record.getRecordType().equals(this.recordType)) {
            return false;
        }

        double value = record.getMeasurementValue();

        // For blood pressure, handle special format if needed
        if ("BloodPressure".equals(recordType)) {
            return checkBloodPressureThreshold(value);
        }

        // For individual pressure readings
        if ("SystolicPressure".equals(recordType)) {
            return value < 90 || value > 180; // Critical thresholds for systolic
        }

        if ("DiastolicPressure".equals(recordType)) {
            return value < 60 || value > 120; // Critical thresholds for diastolic
        }

        // For other vitals, check against configured ranges
        return value < criticalLow || value > criticalHigh ||
                value < minNormal || value > maxNormal;
    }

    /**
     * Special check for combined blood pressure values.
     */
    private boolean checkBloodPressureThreshold(double value) {
        // If value is encoded as systolic.diastolic (e.g., 190.80 for 190/80)
        String valueStr = String.valueOf(value);
        if (valueStr.contains(".")) {
            String[] parts = valueStr.split("\\.");
            if (parts.length == 2) {
                try {
                    double systolic = Double.parseDouble(parts[0]);
                    double diastolic = Double.parseDouble(parts[1]);

                    // Check systolic thresholds (90-180)
                    boolean systolicViolation = systolic < 90 || systolic > 180;
                    // Check diastolic thresholds (60-120)
                    boolean diastolicViolation = diastolic < 60 || diastolic > 120;

                    return systolicViolation || diastolicViolation;
                } catch (NumberFormatException e) {
                    // Fall back to treating as single value
                }
            }
        }

        // Treat as single value
        return value < criticalLow || value > criticalHigh;
    }

    /**
     * checks if a record type is valid
     * @param recordType the record type to check
     * @return true if valid, false otherwise
     */
    private boolean isValidRecordType(String recordType) {
        return "SystolicPressure".equals(recordType) ||
               "DiastolicPressure".equals(recordType) ||
               "BloodPressure".equals(recordType) ||
               "BloodSaturation".equals(recordType) ||
               "HeartRate".equals(recordType) ||
               "ECG".equals(recordType);
    }

    // Getters
    public String getRecordType() { return recordType; }
    public double getMinNormal() { return minNormal; }
    public double getMaxNormal() { return maxNormal; }
    public double getCriticalLow() { return criticalLow; }
    public double getCriticalHigh() { return criticalHigh; }
    public String getDescription() { return description; }

    // Setters
    public void setMinNormal(double minNormal) { this.minNormal = minNormal; }
    public void setMaxNormal(double maxNormal) { this.maxNormal = maxNormal; }
    public void setCriticalLow(double criticalLow) { this.criticalLow = criticalLow; }
    public void setCriticalHigh(double criticalHigh) { this.criticalHigh = criticalHigh; }
    public void setDescription(String description) { this.description = description; }
}
