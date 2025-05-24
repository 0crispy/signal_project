package com.thresholds;

import com.alerts.thresholds.VitalsThreshold;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for VitalsThreshold functionality.
 */
public class VitalsThresholdTest {
    private VitalsThreshold systolicThreshold;
    private VitalsThreshold diastolicThreshold;
    private VitalsThreshold saturationThreshold;

    @BeforeEach
    void setUp() {
        // Create thresholds that match the actual implementation
        systolicThreshold = new VitalsThreshold("SystolicPressure", 90, 180, 70, 200, "Systolic BP threshold");
        diastolicThreshold = new VitalsThreshold("DiastolicPressure", 60, 120, 40, 140, "Diastolic BP threshold");
        saturationThreshold = new VitalsThreshold("BloodSaturation", 92, 100, 88, 100, "Saturation threshold");
    }

    @Test
    void testSystolicPressureThresholdViolation() {
        // Test high systolic (>180)
        PatientRecord highSystolic = new PatientRecord(1, 190.0, "SystolicPressure", 1000L);
        assertTrue(systolicThreshold.checkThreshold(highSystolic), "High systolic pressure should trigger threshold violation");

        // Test low systolic (<90)
        PatientRecord lowSystolic = new PatientRecord(1, 85.0, "SystolicPressure", 2000L);
        assertTrue(systolicThreshold.checkThreshold(lowSystolic), "Low systolic pressure should trigger threshold violation");

        // Test normal systolic
        PatientRecord normalSystolic = new PatientRecord(1, 120.0, "SystolicPressure", 3000L);
        assertFalse(systolicThreshold.checkThreshold(normalSystolic), "Normal systolic pressure should not trigger threshold violation");
    }

    @Test
    void testDiastolicPressureThresholdViolation() {
        // Test high diastolic (>120)
        PatientRecord highDiastolic = new PatientRecord(1, 125.0, "DiastolicPressure", 1000L);
        assertTrue(diastolicThreshold.checkThreshold(highDiastolic), "High diastolic pressure should trigger threshold violation");

        // Test low diastolic (<60)
        PatientRecord lowDiastolic = new PatientRecord(1, 55.0, "DiastolicPressure", 2000L);
        assertTrue(diastolicThreshold.checkThreshold(lowDiastolic), "Low diastolic pressure should trigger threshold violation");

        // Test normal diastolic
        PatientRecord normalDiastolic = new PatientRecord(1, 80.0, "DiastolicPressure", 3000L);
        assertFalse(diastolicThreshold.checkThreshold(normalDiastolic), "Normal diastolic pressure should not trigger threshold violation");
    }

    @Test
    void testSaturationThresholdViolation() {
        // Test low saturation (<92)
        PatientRecord lowSat = new PatientRecord(1, 88.0, "BloodSaturation", 1000L);
        assertTrue(saturationThreshold.checkThreshold(lowSat), "Low saturation should trigger threshold violation");

        // Test normal saturation
        PatientRecord normalSat = new PatientRecord(1, 98.0, "BloodSaturation", 2000L);
        assertFalse(saturationThreshold.checkThreshold(normalSat), "Normal saturation should not trigger threshold violation");
    }

    @Test
    void testWrongRecordType() {
        PatientRecord ecgRecord = new PatientRecord(1, 0.8, "ECG", 1000L);
        assertFalse(systolicThreshold.checkThreshold(ecgRecord), "Wrong record type should not trigger threshold violation");
        assertFalse(saturationThreshold.checkThreshold(ecgRecord), "Wrong record type should not trigger threshold violation");
    }

    @Test
    void testBoundaryValues() {
        // Test exact boundary values for systolic
        PatientRecord systolic90 = new PatientRecord(1, 90.0, "SystolicPressure", 1000L);
        assertFalse(systolicThreshold.checkThreshold(systolic90), "Systolic pressure of exactly 90 should not trigger violation");

        PatientRecord systolic180 = new PatientRecord(1, 180.0, "SystolicPressure", 2000L);
        assertFalse(systolicThreshold.checkThreshold(systolic180), "Systolic pressure of exactly 180 should not trigger violation");

        // Test just outside boundaries
        PatientRecord systolic89 = new PatientRecord(1, 89.0, "SystolicPressure", 3000L);
        assertTrue(systolicThreshold.checkThreshold(systolic89), "Systolic pressure of 89 should trigger violation");

        PatientRecord systolic181 = new PatientRecord(1, 181.0, "SystolicPressure", 4000L);
        assertTrue(systolicThreshold.checkThreshold(systolic181), "Systolic pressure of 181 should trigger violation");
    }

    @Test
    void testGetters() {
        assertEquals("SystolicPressure", systolicThreshold.getRecordType());
        assertEquals(90.0, systolicThreshold.getMinNormal());
        assertEquals(180.0, systolicThreshold.getMaxNormal());
        assertEquals(70.0, systolicThreshold.getCriticalLow());
        assertEquals(200.0, systolicThreshold.getCriticalHigh());
        assertEquals("Systolic BP threshold", systolicThreshold.getDescription());
    }
}