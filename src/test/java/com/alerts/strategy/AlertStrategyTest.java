package com.alerts.strategy;

import com.alerts.Alert;
import com.alerts.PatientRecord;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class AlertStrategyTest {
    private BloodPressureStrategy bpStrategy;
    private HeartRateStrategy hrStrategy;
    private OxygenSaturationStrategy o2Strategy;
    private long baseTime;
    
    @Before
    public void setUp() {
        bpStrategy = new BloodPressureStrategy();
        hrStrategy = new HeartRateStrategy();
        o2Strategy = new OxygenSaturationStrategy();
        baseTime = System.currentTimeMillis();
    }
    

    @Test
    public void testBloodPressureHighSystolic() {
        PatientRecord record = new PatientRecord("SystolicPressure", "185.0");
        Alert alert = bpStrategy.checkAlert(1, record, null);
        
        assertNotNull(alert);

        assertEquals("CriticalHighSystolicPressure", alert.getAlertType());
        assertTrue(alert.getDescription().contains("185.0"));

    }
    
    
    @Test
    public void testBloodPressureTrend() {
        List<com.data_management.PatientRecord> history = new ArrayList<>();
        history.add(new com.data_management.PatientRecord(1, 120.0, "SystolicPressure", baseTime - 3000));
        history.add(new com.data_management.PatientRecord(1, 135.0, "SystolicPressure", baseTime - 2000));
        history.add(new  com.data_management.PatientRecord(1, 150.0, "SystolicPressure", baseTime - 1000));
        
        PatientRecord current = new PatientRecord("SystolicPressure", "165.0");
        Alert alert = bpStrategy.checkAlert(1, current, history);
        
        assertNotNull("Should generate alert for increasing blood pressure trend", alert);
        assertEquals("BloodPressureIncreasingTrend", alert.getAlertType());

        assertTrue(alert.getDescription().contains("120.0"));
        assertTrue(alert.getDescription().contains("165.0"));
    }
    
    @Test
    public void testHeartRateCriticalHigh() {
        PatientRecord record = new PatientRecord("HeartRate", "155.0");
        Alert alert = hrStrategy.checkAlert(1, record, null);
        
        assertNotNull(alert);

        assertEquals("CriticalTachycardia", alert.getAlertType());
        assertTrue(alert.getDescription().contains("155"));
    }
    
    @Test
    public void testHeartRateRapidChange() {
        List<com.data_management.PatientRecord> history = new ArrayList<>();
        com.data_management.PatientRecord previous = new com.data_management.PatientRecord(1, 70.0, "HeartRate", baseTime - 60000);
        history.add(previous);

        
        PatientRecord current = new PatientRecord("HeartRate", "110.0");
        Alert alert = hrStrategy.checkAlert(1, current, history);
        
        assertNotNull(alert);
        assertEquals("RapidHeartRateChange", alert.getAlertType());
        assertTrue(alert.getDescription().contains("increase"));
    }
    
    @Test
    public void testOxygenSaturationCritical() {

        PatientRecord record = new PatientRecord("BloodSaturation", "87.0");
        Alert alert = o2Strategy.checkAlert(1, record, null);
        
        assertNotNull(alert);
        assertEquals("CriticalLowOxygenSaturation", alert.getAlertType());
        assertTrue(alert.getDescription().contains("87.0"));
    }
    
    @Test
    public void testOxygenSaturationRapidDrop() {
        List<com.data_management.PatientRecord> history = new ArrayList<>();
        com.data_management.PatientRecord previous = new com.data_management.PatientRecord(1, 98.0, "BloodSaturation", baseTime - 300000);
        history.add(previous);
        
        PatientRecord current = new PatientRecord("BloodSaturation", "92.0");
        Alert alert = o2Strategy.checkAlert(1, current, history);
        
        assertNotNull(alert);
        assertEquals("RapidSaturationDrop", alert.getAlertType());
        
        assertTrue(alert.getDescription().contains("98.0"));
        assertTrue(alert.getDescription().contains("92.0"));
    }
    
    @Test
    public void testInvalidVitalTypes() {
        PatientRecord invalidRecord = new PatientRecord("InvalidType", "100.0");
        
        assertNull(bpStrategy.checkAlert(1, invalidRecord, null));
        assertNull(hrStrategy.checkAlert(1, invalidRecord, null));
        assertNull(o2Strategy.checkAlert(1, invalidRecord, null));
    }
    
    @Test
    public void testNormalValues() {
        PatientRecord normalBP = new PatientRecord("SystolicPressure", "120.0");
        PatientRecord normalHR = new PatientRecord("HeartRate", "75.0");
        PatientRecord normalO2 = new PatientRecord("BloodSaturation", "98.0");
        
        assertNull(bpStrategy.checkAlert(1, normalBP, null));
        assertNull(hrStrategy.checkAlert(1, normalHR, null));
        assertNull(o2Strategy.checkAlert(1, normalO2, null));
    }
} 