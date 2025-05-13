package com.cardio_generator.outputs;

/**
 * The interface for an output strategy (TCP, WebSocket, file, etc)
 */
public interface OutputStrategy {
    /**
     * Outputs the data using a specific strategy
     * @param patientId - the patient ID
     * @param timestamp - a timestamp indicating when the data was generated
     * @param label     - label describing the type of data
     * @param data      - the actual data to output
     */
    void output(int patientId, long timestamp, String label, String data);
}
