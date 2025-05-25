package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ECGDataGeneratorTest {
    private ECGDataGenerator generator;
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
        generator = new ECGDataGenerator(5);
        outputStrategy = new TestOutputStrategy();
    }

    @Test
    public void testConstructorInitialization() {
        for (int i = 1; i <= 5; i++) {
            assertEquals("Initial ECG value should be 0.0", 0.0, generator.lastEcgValues[i], 0.001);
        }
    }

    @Test
    public void testGenerateOutput() {
        generator.generate(1, outputStrategy);
        
        assertEquals("Patient ID should match", 1, outputStrategy.getPatientId());
        assertEquals("Label should be ECG", "ECG", outputStrategy.getLabel());
        assertNotNull("Data should not be null", outputStrategy.getData());
    }

    @Test
    public void testEcgWaveformSimulation() {
        double value = generator.simulateEcgWaveform(1, 0.0);
        assertNotEquals("ECG waveform value should not be 0", 0.0, value, 0.001);
    }

    @Test
    public void testInvalidPatientId() {
        generator.generate(0, outputStrategy);
        assertNull("Should not generate data for invalid patient ID", outputStrategy.getData());
        
        generator.generate(6, outputStrategy);
        assertNull("Should not generate data for patient ID > max", outputStrategy.getData());
    }
}