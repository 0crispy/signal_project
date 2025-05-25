package com.cardio_generator.generators;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import com.cardio_generator.outputs.OutputStrategy;

public class BloodLevelsDataGeneratorTest {
    private BloodLevelsDataGenerator generator;
    private TestOutputStrategy outputStrategy;

    private class TestOutputStrategy implements OutputStrategy {
        private int patientId;
        private String label;
        private String data;

        @Override
        public void output(int patientId, long timestamp, String label,  String data) 
        {
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
        generator = new BloodLevelsDataGenerator(5); // 5 patients
        outputStrategy = new TestOutputStrategy();
    }

    @Test
    public void testGenerateValidData() {
        generator.generate(1, outputStrategy);
        
        assertEquals("Patient ID should match", 1, outputStrategy.getPatientId());
        assertEquals("Label should be BloodLevels", "BloodLevels", outputStrategy.getLabel());
        assertNotNull("Data should not be null", outputStrategy.getData());
        
        String data = outputStrategy.getData();
        assertTrue("Data should contain glucose level", data.contains("glucose="));
        assertTrue("Data should contain hemoglobin level", data.contains("hemoglobin="));
        assertTrue("Data should contain platelet count", data.contains("platelets="));
    }

    @Test
    public void testGenerateForDifferentPatients() {
        generator.generate(1, outputStrategy);
        String data1 = outputStrategy.getData();
        
        generator.generate(2, outputStrategy);
        String data2 = outputStrategy.getData();
        
        assertNotEquals("Different patients should have different data", data1, data2);
    }

    @Test
    public void testInvalidPatientId() {
        generator.generate(0, outputStrategy);
        assertNull("Should not generate data for invalid patient ID", outputStrategy.getData());
        
        generator.generate(6, outputStrategy);
        assertNull("Should not generate data for patient ID > max", outputStrategy.getData());
    }

    @Test
    public void testNullOutputStrategy() {
        generator.generate(1, null);
    }
}