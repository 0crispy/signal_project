package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates ECG data using a simple waveform simulation.
 */
public class ECGDataGenerator implements PatientDataGenerator {
    private static final Random random = new Random();
    double[] lastEcgValues;
    private static final double PI = Math.PI;
    private final int maxPatients;

    /**
     * Sets up initial ECG values for each patient.
     */
    public ECGDataGenerator(int patientCount) {
        this.maxPatients = patientCount;
        lastEcgValues = new double[patientCount + 1];
        // Initialize the last ECG value for each patient
        for (int i = 1; i <= patientCount; i++) {
            lastEcgValues[i] = 0; // Initial ECG value can be set to 0
        }
    }

    /**
     * Generates ECG data and sends it via the output strategy.
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (patientId <= 0 || patientId > maxPatients) {
                System.err.println("Invalid patient ID: " + patientId + " (valid range: 1-" + maxPatients + ")");
                return;
            }

            if (outputStrategy == null) {
                System.err.println("Output strategy cannot be null");
                return;
            }

            double ecgValue = simulateEcgWaveform(patientId, lastEcgValues[patientId]);
            outputStrategy.output(patientId, System.currentTimeMillis(), "ECG", Double.toString(ecgValue));
            lastEcgValues[patientId] = ecgValue;
        } catch (Exception e) {
            System.err.println("An error occurred while generating ECG data for patient " + patientId);
            e.printStackTrace();
        }
    }

    double simulateEcgWaveform(int patientId, double lastEcgValue) {
        // Simplified ECG waveform generation based on sinusoids
        double hr = 60.0 + random.nextDouble() * 20.0; // Simulate heart rate variability between 60 and 80 bpm
        double t = System.currentTimeMillis() / 1000.0; // Use system time to simulate continuous time
        double ecgFrequency = hr / 60.0; // Convert heart rate to Hz

        // Simulate different components of the ECG signal
        double pWave = 0.1 * Math.sin(2 * PI * ecgFrequency * t);
        double qrsComplex = 0.5 * Math.sin(2 * PI * 3 * ecgFrequency * t); // QRS is higher frequency
        double tWave = 0.2 * Math.sin(2 * PI * 2 * ecgFrequency * t + PI / 4); // T wave is offset

        return pWave + qrsComplex + tWave + random.nextDouble() * 0.05; // Add small noise
    }
}
