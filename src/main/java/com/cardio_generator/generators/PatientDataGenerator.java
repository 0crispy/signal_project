package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Interface for generating patient data
 */
public interface PatientDataGenerator {
    /**
     * Generates data for a specific patient and then outputs it using
     * the specified strategy.
     * @param patientId the ID of the patient
     * @param outputStrategy the strategy to use for output (TCP, WebSocket, file, etc)
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
