package com.alerts;


public class PatientRecordAdapter {
    
    /**
     * 
     * @param record the data management record to convert
     * @return a new alert system PatientRecord
     */
    public static com.alerts.PatientRecord toAlertRecord(com.data_management.PatientRecord record) {
        return new com.alerts.PatientRecord(
            record.getRecordType(),
            String.valueOf(record.getMeasurementValue())
        );
    }
    
    /**
     * this creates an alert system PatientRecord from individual values
     * 
     * @param recordType The type of record
     * @param value The measurement value
     * @return A new alert system PatientRecord
     */
    public static com.alerts.PatientRecord createAlertRecord(String recordType, double value) {
        return new com.alerts.PatientRecord(recordType, String.valueOf(value));
    }
} 