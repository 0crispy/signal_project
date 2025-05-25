package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates simulated patient alert events for monitoring systems.
 * This class simulates patient alerts that can be triggered and resolved over time,
 * following a probabilistic model to create realistic alert scenarios.
 * <p>
 * Usage: Instantiate with the number of patients to monitor, then call generate()
 * periodically to potentially trigger or resolve alerts for specific patients.
 */
public class AlertGenerator implements PatientDataGenerator {

    /** Random number generator for determining alert state changes */
    public static Random randomGenerator = new Random();
    
    /** Tracks the current alert state for each patient (true = triggered, false = resolved) */
    boolean[] AlertStates;
    private final int maxPatients;
    private static final double TRIGGER_PROBABILITY = 0.4; // 40% chance to trigger
    private static final double RESOLVE_PROBABILITY = 0.3; // 30% chance to resolve

    /**
     * Constructs an alert generator for a specified number of patients.
     * Initializes each patient with a resolved alert state (false).
     *
     * @param patientCount the number of patients for which to generate alerts
     */
    public AlertGenerator(int patientCount) {
        this.maxPatients = patientCount;
        AlertStates = new boolean[patientCount + 1];
    }

    /**
     * Generates potential alert events for the specified patient based on probability
     * calculations and the current alert state. Alert events include both triggers
     * and resolutions, which are sent to the provided output strategy.
     *
     * @param patientId the identifier of the patient for whom to generate alerts
     * @param outputStrategy the strategy used to output the generated alerts
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

            double rand = randomGenerator.nextDouble();
            if (AlertStates[patientId]) {
                if (rand < RESOLVE_PROBABILITY) {
                    AlertStates[patientId] = false;
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } 
            else {
                if (rand < TRIGGER_PROBABILITY) {
                    AlertStates[patientId] = true;
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alerts for patient " + patientId);
            e.printStackTrace();
        }
    }

    /**
     * gets the current alert state for a patient.
     * 
     * @param patientId the patient Id
     * @return true if the patient has an active alert, false otherwise
     * @throws IllegalArgumentException if the patient Id is invalid
     */
    public boolean isAlertActive(int patientId) {
        if (patientId <= 0 || patientId > maxPatients) {
            throw new IllegalArgumentException("Invalid patient ID: " + patientId);
        }
        return AlertStates[patientId];
    }

    /**
     * forces some alert state for testing purposes.
     * 
     * @param patientId the patient to set
     * @param state the alert state to set
     * @param outputStrategy the strategy used to output the state change
     * @throws IllegalArgumentException if the patient Idis invalid
     */
    public void setAlertState(int patientId, boolean state, OutputStrategy outputStrategy) {
        if (patientId <= 0 || patientId > maxPatients) {
            throw new IllegalArgumentException("Invalid patient ID: " + patientId);
        }
        if (outputStrategy == null) {
            throw new IllegalArgumentException("Output strategy cannot be null");
            
        }
        AlertStates[patientId] = state;
        // Always output the state change
        if (state) {
            outputStrategy.output(patientId, System.currentTimeMillis(),  "Alert", "triggered");
        } 
        else {
            outputStrategy.output(patientId, System.currentTimeMillis(),  "Alert", "resolved");
        }
    }
}
