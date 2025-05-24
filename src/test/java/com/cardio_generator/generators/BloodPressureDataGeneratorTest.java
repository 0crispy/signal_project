package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BloodPressureDataGeneratorTest {
    private BloodPressureDataGenerator generator;
    private TestOutputStrategy output;

    @BeforeEach
    void setUp() {
        generator = new BloodPressureDataGenerator(5); // Test with 5 patients
        output = new TestOutputStrategy();
    }

    @Test
    void constructor_ValidPatientCount_InitializesArrays() {
        assertDoesNotThrow(() -> new BloodPressureDataGenerator(10));
    }

    @Test
    void constructor_InvalidPatientCount_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new BloodPressureDataGenerator(0));
    }

    @Test
    void generate_ValidPatientId_ProducesOutput() {
        generator.generate(1, output);
        assertEquals(2, output.getCallCount()); // Both systolic and diastolic
    }

    @Test
    void generate_InvalidPatientId_NoOutput() {
        generator.generate(999, output);
        assertEquals(0, output.getCallCount());
    }

    @Test
    void generate_ValuesStayWithinRanges() {
        for (int i = 1; i <= 5; i++) {
            generator.generate(i, output);
            int systolic = Integer.parseInt(output.getLastValue("SystolicPressure"));
            int diastolic = Integer.parseInt(output.getLastValue("DiastolicPressure"));
            assertTrue(systolic >= 90 && systolic <= 180);
            assertTrue(diastolic >= 60 && diastolic <= 120);
        }
    }

    // Helper test output class
    static class TestOutputStrategy implements OutputStrategy {
        private int callCount = 0;
        private String lastSystolic;
        private String lastDiastolic;

        @Override
        public void output(int patientId, long timestamp, String label, String data) {
            callCount++;
            if ("SystolicPressure".equals(label)) {
                lastSystolic = data;
            } else if ("DiastolicPressure".equals(label)) {
                lastDiastolic = data;
            }
        }

        public int getCallCount() { return callCount; }
        public String getLastValue(String type) {
            return "SystolicPressure".equals(type) ? lastSystolic : lastDiastolic;
        }
    }
}