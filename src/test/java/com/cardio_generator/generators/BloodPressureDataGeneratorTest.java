package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class BloodPressureDataGeneratorTest {
    private BloodPressureDataGenerator generator;
    private TestOutputStrategy output;

    @Before
    public void setUp() {
        generator = new BloodPressureDataGenerator(5);
        output = new TestOutputStrategy();
    }

    @Test
    public void testConstructorWithValidPatientCount() {
        try {
            new BloodPressureDataGenerator(10);
            assertTrue(true);
        } catch (Exception e) {
            fail("Should not throw exception with valid patient count");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithInvalidPatientCount() {
        new BloodPressureDataGenerator(0);
    }

    @Test
    public void testGenerateWithValidPatientId() {
        generator.generate(1, output);
        assertEquals("Should generate both systolic and diastolic values", 
            2, output.getCallCount());
    }

    @Test
    public void testGenerateWithInvalidPatientId() {
        generator.generate(999, output);
        assertEquals("Should not generate output for invalid patient ID", 
            0, output.getCallCount());
    }

    @Test
    public void testValuesStayWithinRanges() {
        for (int i = 1; i <= 5; i++) {
            generator.generate(i, output);
            int systolic = Integer.parseInt(output.getLastValue("SystolicPressure"));
            int diastolic = Integer.parseInt(output.getLastValue("DiastolicPressure"));
            
            assertTrue("Systolic pressure should be between 90 and 180",
                systolic >= 90 && systolic <= 180);
            assertTrue("Diastolic pressure should be between 60 and 120",
                diastolic >= 60 && diastolic <= 120);
        }
    }

    private static class TestOutputStrategy implements OutputStrategy {
        private int callCount = 0;
        private String lastSystolic;
        private String lastDiastolic;

        @Override
        public void output(int patientId, long timestamp, String label, String data) {
            callCount++;
            if ("SystolicPressure".equals(label)) {
                lastSystolic = data;
            } 
            else if ("DiastolicPressure".equals(label)) {
                lastDiastolic = data;
            }
        }

        public int getCallCount() { return callCount; }
        public String getLastValue(String type) {
            return "SystolicPressure".equals(type) ? lastSystolic : lastDiastolic;
        }
    }
}