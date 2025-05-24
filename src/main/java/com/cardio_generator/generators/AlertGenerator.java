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

    /**
     * Constructs an alert generator for a specified number of patients.
     * Initializes each patient with a resolved alert state (false).
     *
     * @param patientCount the number of patients for which to generate alerts
     */
    public AlertGenerator(int patientCount) {
        AlertStates = new boolean[patientCount + 1];
    }

    /**
     * Generates potential alert events for the specified patient based on probability
     * calculations and the current alert state. Alert events include both triggers
     * and resolutions, which are sent to the provided output strategy.
     *
     * @param patientId the identifier of the patient for whom to generate alerts
     * @param outputStrategy the strategy used to output the generated alerts
     * @return void This method doesn't return a value
     * @throws Exception if an error occurs during alert generation or output
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (AlertStates[patientId]) {
                if (randomGenerator.nextDouble() < 0.9) { // 90% chance to resolve
                    AlertStates[patientId] = false;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                double Lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                double p = -Math.expm1(-Lambda); // Probability of at least one alert in the period
                boolean alertTriggered = randomGenerator.nextDouble() < p;

                if (alertTriggered) {
                    AlertStates[patientId] = true;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
