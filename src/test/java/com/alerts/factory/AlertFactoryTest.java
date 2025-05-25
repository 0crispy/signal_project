package com.alerts.factory;

import com.alerts.Alert;
import com.alerts.PatientRecord;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class AlertFactoryTest {
    private BloodPressureAlertFactory bpFactory;
    private BloodOxygenAlertFactory boFactory;
    private ECGAlertFactory ecgFactory;
    
    @Before
    public void setUp() {
        bpFactory = new BloodPressureAlertFactory();
        boFactory = new BloodOxygenAlertFactory();
        ecgFactory = new ECGAlertFactory();
    }
    
    @Test
    public void testBloodPressureHigh() {
        PatientRecord vitals = new PatientRecord("SystolicPressure", "185.0");
        Alert alert = bpFactory.createAlert(1, vitals, System.currentTimeMillis());
        
        assertNotNull(alert);
        assertEquals("CriticalHighSystolicPressure", alert.getAlertType());
        assertTrue(alert.getDescription().contains("185.0"));
    }
    
    @Test
    public void testBloodPressureNormal() {
        PatientRecord vitals = new PatientRecord("SystolicPressure", "120.0");
        Alert alert = bpFactory.createAlert(1, vitals, System.currentTimeMillis());
        
        assertNull(alert); // No alert for normal values
    }
    
    @Test
    public void testBloodOxygenLow() {
        PatientRecord vitals = new PatientRecord("BloodSaturation", "90.0");
        Alert alert = boFactory.createAlert(1, vitals, System.currentTimeMillis());
        
        assertNotNull(alert);
        assertEquals("LowBloodSaturation", alert.getAlertType());
        assertTrue(alert.getDescription().contains("90.0"));
    }
    
    @Test
    public void testBloodOxygenNormal() {
        PatientRecord vitals = new PatientRecord("BloodSaturation", "95.0");
        Alert alert = boFactory.createAlert(1, vitals, System.currentTimeMillis());
        
        assertNull(alert);
        
    }
    
    @Test
    public void testECGAnomaly() {
        for (int i = 0; i < 10; i++) {
            PatientRecord normalVitals = new PatientRecord("ECG", "1.0");
            ecgFactory.createAlert(1, normalVitals, System.currentTimeMillis());
        }
        
        PatientRecord anomalousVitals = new PatientRecord("ECG", "2.5");
        Alert alert = ecgFactory.createAlert(1, anomalousVitals, System.currentTimeMillis());
        
        assertNotNull(alert);
        assertEquals("ECGAnomaly", alert.getAlertType());
        assertTrue(alert.getDescription().contains("2.5"));
    }
    
    @Test
    public void testInvalidVitalType() {
        PatientRecord invalidVitals = new PatientRecord("InvalidType", "100.0");
        
        try {
            bpFactory.createAlert(1, invalidVitals, System.currentTimeMillis());
            fail("Should throw IllegalArgumentException for invalid vital type");
        } catch (IllegalArgumentException expected) {
        }
        
        try {
            boFactory.createAlert(1, invalidVitals, System.currentTimeMillis());
            fail("Should throw IllegalArgumentException for invalid vital type");
        } catch (IllegalArgumentException expected) {
        }
        
        try {
            ecgFactory.createAlert(1, invalidVitals, System.currentTimeMillis());
            fail("Should throw IllegalArgumentException for invalid vital type");
        } catch (IllegalArgumentException expected) {
        }
    }
    
    @Test
    public void testNullVitals() {
        assertThrows(IllegalArgumentException.class, () -> 
            bpFactory.createAlert(1, null, System.currentTimeMillis()));
    }
    
    @Test
    public void testInvalidTimestamp() {
        PatientRecord vitals = new PatientRecord("SystolicPressure", "120.0");
        assertThrows(IllegalArgumentException.class, () -> 
            bpFactory.createAlert(1, vitals, -1));
    }
} 