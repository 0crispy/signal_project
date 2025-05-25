package com.thresholds;

import com.alerts.thresholds.VitalsThreshold;
import com.data_management.PatientRecord;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for VitalsThreshold functionality.
 */
public class VitalsThresholdTest {
    private VitalsThreshold systolicThreshold;
    private VitalsThreshold diastolicThreshold;
    private VitalsThreshold saturationThreshold;

    @Before
    public void setUp() {
        systolicThreshold = new VitalsThreshold("SystolicPressure", 90, 180, 70, 200, "Systolic BP threshold");
        diastolicThreshold = new VitalsThreshold("DiastolicPressure", 60, 120, 40, 140, "Diastolic BP threshold");
        saturationThreshold = new VitalsThreshold("BloodSaturation", 92, 100, 88, 100, "Saturation threshold");
    }

    @Test
    public void testSystolicPressureThresholdViolation() {
        PatientRecord highSystolic = new PatientRecord(1, 190.0, "SystolicPressure", 1000L);
        assertTrue("High systolic pressure should trigger threshold violation", 
            systolicThreshold.checkThreshold(highSystolic));

        PatientRecord lowSystolic = new PatientRecord(1, 85.0, "SystolicPressure", 2000L);
        assertTrue("Low systolic pressure should trigger threshold violation", 
            systolicThreshold.checkThreshold(lowSystolic));

        PatientRecord normalSystolic = new PatientRecord(1, 120.0, "SystolicPressure", 3000L);
        assertFalse("Normal systolic pressure should not trigger threshold violation", 
            systolicThreshold.checkThreshold(normalSystolic));
    }

    @Test
    public void testDiastolicPressureThresholdViolation() {
        PatientRecord highDiastolic = new PatientRecord(1, 130.0, "DiastolicPressure", 1000L);
        assertTrue("High diastolic pressure should trigger threshold violation", 
            diastolicThreshold.checkThreshold(highDiastolic));

        PatientRecord lowDiastolic = new PatientRecord(1, 55.0, "DiastolicPressure", 2000L);
        assertTrue("Low diastolic pressure should trigger threshold violation", 
            diastolicThreshold.checkThreshold(lowDiastolic));

        PatientRecord normalDiastolic = new PatientRecord(1, 80.0, "DiastolicPressure", 3000L);
        assertFalse("Normal diastolic pressure should not trigger threshold violation", 
            diastolicThreshold.checkThreshold(normalDiastolic));
    }

    @Test
    public void testSaturationThresholdViolation() {
        PatientRecord lowSaturation = new PatientRecord(1, 90.0, "BloodSaturation", 1000L);
        assertTrue("Low blood saturation should trigger threshold violation", 
            saturationThreshold.checkThreshold(lowSaturation));

        PatientRecord criticalSaturation = new PatientRecord(1, 88.0, "BloodSaturation", 2000L);
        assertTrue("Critical blood saturation should trigger threshold violation", 
            saturationThreshold.checkThreshold(criticalSaturation));

        PatientRecord normalSaturation = new PatientRecord(1, 95.0, "BloodSaturation", 3000L);
        assertFalse("Normal blood saturation should not trigger threshold violation", 
            saturationThreshold.checkThreshold(normalSaturation));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidVitalType() {
        PatientRecord invalidRecord = new PatientRecord(1, 100.0, "InvalidType", 1000L);
        systolicThreshold.checkThreshold(invalidRecord);
    }

    @Test
    public void testWrongRecordType() {
        PatientRecord ecgRecord = new PatientRecord(1, 0.8, "ECG", 1000L);
        assertFalse("Wrong record type should not trigger threshold violation",
                systolicThreshold.checkThreshold(ecgRecord));
        assertFalse("Wrong record type should not trigger threshold violation",
                saturationThreshold.checkThreshold(ecgRecord));
    }

    @Test
    public void testBoundaryValues() {
        PatientRecord systolic90 = new PatientRecord(1, 90.0, "SystolicPressure", 1000L);
        assertFalse("Systolic pressure of exactly 90 should not trigger violation",
                systolicThreshold.checkThreshold(systolic90));

        PatientRecord systolic180 = new PatientRecord(1, 180.0, "SystolicPressure", 2000L);
        assertFalse("Systolic pressure of exactly 180 should not trigger violation",
                systolicThreshold.checkThreshold(systolic180));

        PatientRecord systolic89 = new PatientRecord(1, 89.0, "SystolicPressure", 3000L);
        assertTrue("Systolic pressure of 89 should trigger violation",
                systolicThreshold.checkThreshold(systolic89));

        PatientRecord systolic181 = new PatientRecord(1, 181.0, "SystolicPressure", 4000L);
        assertTrue("Systolic pressure of 181 should trigger violation",
                systolicThreshold.checkThreshold(systolic181));
    }

    @Test
    public void testGetters() {
        assertEquals("SystolicPressure", systolicThreshold.getRecordType());
        assertEquals(90.0, systolicThreshold.getMinNormal(), 0.001);
        assertEquals(180.0, systolicThreshold.getMaxNormal(), 0.001);
        assertEquals(70.0, systolicThreshold.getCriticalLow(), 0.001);
        assertEquals(200.0, systolicThreshold.getCriticalHigh(), 0.001);
        assertEquals("Systolic BP threshold", systolicThreshold.getDescription());
    }
}