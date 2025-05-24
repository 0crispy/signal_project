package com.cardio_generator.generators;

import java.util.Random;
import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates blood pressure data with small variations.
 */
public class BloodPressureDataGenerator implements PatientDataGenerator {
    private static final Random random = new Random();
    private final int[] lastSystolicValues;
    private final int[] lastDiastolicValues;
    private final int patientCount;

    /**
     * Initializes the generator with baseline values.
     * 
     * @param patientCount the number of patients to generate data for
     * @throws IllegalArgumentException if patientCount is not positive
     */
    public BloodPressureDataGenerator(int patientCount) {
        if (patientCount <= 0) {
            throw new IllegalArgumentException("Patient count must be positive");
        }

        this.patientCount = patientCount;
        this.lastSystolicValues = new int[patientCount + 1]; // +1 because patient IDs start at 1
        this.lastDiastolicValues = new int[patientCount + 1];

        // Initialize with baseline values for each patient
        for (int i = 1; i <= patientCount; i++) {
            lastSystolicValues[i] = 110 + random.nextInt(20); // 110-130 mmHg
            lastDiastolicValues[i] = 70 + random.nextInt(15); // 70-85 mmHg
        }
    }

    /**
     * Generates and sends blood pressure data for a patient.
     * 
     * @param patientId the ID of the patient
     * @param outputStrategy the strategy to output the generated data
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            // Validate patient ID
            if (patientId <= 0 || patientId > patientCount) {
                System.err.printf("Invalid patient ID: %d (valid range: 1-%d)%n",
                        patientId, patientCount);
                return;
            }

            // Generate small variations
            int systolicVariation = random.nextInt(5) - 2; // -2 to +2
            int diastolicVariation = random.nextInt(5) - 2;

            // Calculate new values with bounds checking
            int newSystolic = Math.min(Math.max(
                    lastSystolicValues[patientId] + systolicVariation, 90), 180);
            int newDiastolic = Math.min(Math.max(
                    lastDiastolicValues[patientId] + diastolicVariation, 60), 120);

            // Update stored values
            lastSystolicValues[patientId] = newSystolic;
            lastDiastolicValues[patientId] = newDiastolic;

            // Output the data
            outputStrategy.output(patientId, System.currentTimeMillis(),
                    "SystolicPressure", Integer.toString(newSystolic));
            outputStrategy.output(patientId, System.currentTimeMillis(),
                    "DiastolicPressure", Integer.toString(newDiastolic));

        } catch (Exception e) {
            System.err.printf("Error generating BP data for patient %d: %s%n",
                    patientId, e.getMessage());
        }
    }
}
