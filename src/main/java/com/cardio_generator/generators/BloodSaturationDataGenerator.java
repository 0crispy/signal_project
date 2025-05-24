package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates simulated blood saturation data for patients.
 * This class creates realistic blood oxygen saturation values that fluctuate 
 * within normal physiological ranges for monitoring purposes.
 * Usage: Instantiate with the number of patients to monitor, then call generate()
 * periodically to produce new saturation values for specific patients.
 */
public class BloodSaturationDataGenerator implements PatientDataGenerator {
    /** Random number generator for producing natural variations in saturation values */
    private static final Random random = new Random();
    
    /** Stores the last generated saturation value for each patient */
    int[] lastSaturationValues;

    /**
     * Constructs a blood saturation data generator for a specified number of patients.
     * Initializes each patient with a baseline saturation value between 95% and 100%.
     *
     * @param patientCount the number of patients for which to generate saturation data
     */
    public BloodSaturationDataGenerator(int patientCount) {
        lastSaturationValues = new int[patientCount + 1];

        // Initialize with baseline saturation values for each patient
        for (int i = 1; i <= patientCount; i++) {
            lastSaturationValues[i] = 95 + random.nextInt(6); // Initializes with a value between 95 and 100
        }
    }

    /**
     * Generates a new blood saturation reading for the specified patient and sends it
     * to the provided output strategy. The generated value fluctuates realistically
     * around the previous value while staying within normal physiological limits (90-100%).
     *
     * @param patientId the identifier of the patient for whom to generate data
     * @param outputStrategy the strategy used to output the generated data
     * @return void This method doesn't return a value
     * @throws Exception if an error occurs during data generation or output
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            // Simulate blood saturation values
            int variation = random.nextInt(3) - 1; // -1, 0, or 1 to simulate small fluctuations
            int newSaturationValue = lastSaturationValues[patientId] + variation;

            // Ensure the saturation stays within a realistic and healthy range
            newSaturationValue = Math.min(Math.max(newSaturationValue, 90), 100);
            lastSaturationValues[patientId] = newSaturationValue;
            outputStrategy.output(patientId, System.currentTimeMillis(), "Saturation",
                    Double.toString(newSaturationValue) + "%");
        } catch (Exception e) {
            System.err.println("An error occurred while generating blood saturation data for patient " + patientId);
            e.printStackTrace(); // This will print the stack trace to help identify where the error occurred.
        }
    }
}
