package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

public class BloodLevelsDataGenerator implements PatientDataGenerator {
    private static final Random random = new Random();
    final double[] baselineCholesterol;
    final double[] baselineWhiteCells;
    final double[] baselineRedCells;
    final double[] baselineGlucose;
    private final int maxPatients;

    public BloodLevelsDataGenerator(int patientCount) {
        this.maxPatients = patientCount;
        baselineCholesterol = new double[patientCount + 1];
        baselineWhiteCells = new double[patientCount + 1];
        baselineRedCells = new double[patientCount + 1];
        baselineGlucose = new double[patientCount + 1];

        for (int i = 1; i <= patientCount; i++) {
            baselineCholesterol[i] = 150 + random.nextDouble() * 50; // Initial random baseline
            baselineWhiteCells[i] = 4 + random.nextDouble() * 6; // Initial random baseline
            baselineRedCells[i] = 4.5 + random.nextDouble() * 1.5; // Initial random baseline
            baselineGlucose[i] = 80 + random.nextDouble() * 40; // Initial random baseline (80-120 mg/dL)
        }
    }

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

            //some random vals
            double cholesterol = baselineCholesterol[patientId] + (random.nextDouble() - 0.5) * 10; 
            double whiteCells = baselineWhiteCells[patientId] + (random.nextDouble() - 0.5) * 1; 
            double redCells = baselineRedCells[patientId] + (random.nextDouble() - 0.5) * 0.2; 
            double glucose = baselineGlucose[patientId] + (random.nextDouble() - 0.5) * 5; 

            //format the stuff
            String data = String.format("cholesterol=%.1f,glucose=%.1f,hemoglobin=%.1f,platelets=%.1f", 
                cholesterol, glucose, redCells, whiteCells);

            outputStrategy.output(patientId, System.currentTimeMillis(), "BloodLevels", data);
        } catch (Exception e) {
            System.err.println("An error occurred while generating blood levels data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
