package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Random;

public class AlertGeneratorTest {
    private AlertGenerator generator;
    private TestOutputStrategy outputStrategy;
    private Random originalRandom;

    private class TestOutputStrategy implements OutputStrategy {
        private int patientId;
        private String label;
        private String data;
        private boolean wasCalled;

        @Override
        public void output(int patientId, long timestamp, String label, String data) {
            this.patientId = patientId;
            this.label = label;
            this.data = data;
            this.wasCalled = true;
        }

        public void reset() {
            this.patientId = 0;
            this.label = null;
            this.data = null;
            this.wasCalled = false;
        }

        public int getPatientId() { return patientId; }
        public String getLabel() { return label; }
        public String getData() { return data; }
        public boolean wasCalled() { return wasCalled; }
    }

    @Before
    public void setUp() {
        generator = new AlertGenerator(5);
        outputStrategy = new TestOutputStrategy();
        originalRandom = AlertGenerator.randomGenerator;
    }

    @After
    public void tearDown() {
        AlertGenerator.randomGenerator = originalRandom;
        outputStrategy.reset();
    }

    @Test
    public void testConstructorInitialization() {
        for (int i = 1; i <= 5; i++) {
            assertFalse("Alert state should be initialized to false", 
                generator.AlertStates[i]);
        }
    }

    @Test
    public void testAlertTrigger() {
        AlertGenerator.randomGenerator = new Random() {
            @Override
            public double nextDouble() {
                return 0.05; // Below threshold to trigger
            }
        };

        generator.generate(1, outputStrategy);
        assertTrue("Output strategy should have been called", outputStrategy.wasCalled());
        assertEquals("Patient ID should match", 1, outputStrategy.getPatientId());
        assertEquals("Label should be Alert", "Alert", outputStrategy.getLabel());
        assertEquals("Data should indicate trigger", "triggered", outputStrategy.getData());
    }

    @Test
    public void testAlertResolution() {
        AlertGenerator.randomGenerator = new Random() {
            @Override
            public double nextDouble() {
                return 0.05; // Below threshold to trigger
            }
        };
        generator.generate(1, outputStrategy);
        outputStrategy.reset();

        AlertGenerator.randomGenerator = new Random() {
            @Override
            public double nextDouble() {
                return 0.2; // Below 0.3 threshold to resolve
            }
        };

        generator.generate(1, outputStrategy);
        assertTrue("Output strategy should have been called", outputStrategy.wasCalled());
        assertEquals("Patient ID should match", 1, outputStrategy.getPatientId());
        assertEquals("Label should be Alert", "Alert", outputStrategy.getLabel());
        assertEquals("Data should indicate resolution", "resolved", outputStrategy.getData());
    }

    @Test
    public void testInvalidPatientId() {
        generator.generate(0, outputStrategy);
        assertFalse("Should not call output for invalid patient ID", outputStrategy.wasCalled());
        
        generator.generate(6, outputStrategy);
        assertFalse("Should not call output for patient ID > max", outputStrategy.wasCalled());
    }

    @Test
    public void testNoChangeInNormalRange() {
        AlertGenerator.randomGenerator = new Random() {
            @Override
            public double nextDouble() {
                return 0.5; // In normal range
            }
        };

        generator.generate(1, outputStrategy);
        assertFalse("Output strategy should not be called", outputStrategy.wasCalled());
    }
}