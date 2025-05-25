package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class BloodSaturationDataGeneratorTest {
    private BloodSaturationDataGenerator generator;
    private TestOutputStrategy outputStrategy;

    private class TestOutputStrategy implements OutputStrategy {
        private int patientId;
        private String label;
        private String data;

        @Override
        public void output(int patientId, long timestamp, String label, String data) {
            this.patientId = patientId;
            this.label = label;
            this.data = data;
        }

        public int getPatientId() { return patientId; }
        public String getLabel() { return label; }
        public String getData() { return data; }
    }

    @Before
    public void setUp() {
        generator = new BloodSaturationDataGenerator(5);
        outputStrategy = new TestOutputStrategy();
    }

    @Test
    public void testConstructorInitialization() {
        for (int i = 1; i <= 5; i++) {
            int value = generator.lastSaturationValues[i];
            assertTrue("Initial saturation value should be between 95 and 100", 
                value >= 95 && value <= 100);
        }
    }

    @Test
    public void testGenerateValidValues() {
        generator.generate(1, outputStrategy);
        int value = generator.lastSaturationValues[1];
        assertTrue("Generated saturation value should be between 90 and 100",
            value >= 90 && value <= 100);
    }

    @Test
    public void testOutputFormat() {
        generator.generate(1, outputStrategy);
        
        assertEquals("Patient ID should match", 1, outputStrategy.getPatientId());
        assertEquals("Label should be Saturation", "Saturation", outputStrategy.getLabel());
        assertNotNull("Data should not be null", outputStrategy.getData());
        assertTrue("Data should end with %", outputStrategy.getData().endsWith("%"));
    }

    @Test
    public void testInvalidPatientId() {
        generator.generate(0, outputStrategy);
        assertNull("Should not generate data for invalid patient ID", outputStrategy.getData());
        
        generator.generate(6, outputStrategy);
        assertNull("Should not generate data for patient ID > max", outputStrategy.getData());
    }
}