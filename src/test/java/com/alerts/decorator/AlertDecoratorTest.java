package com.alerts.decorator;

import com.alerts.Alert;
import com.alerts.PatientRecord;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class AlertDecoratorTest {
    private Alert baseAlert;
    private PatientRecord testRecord;

    @Before
    public void setUp() {
        testRecord = new PatientRecord("SystolicPressure", "120.0");
        baseAlert = new Alert(1, testRecord, 1000L, "TestAlert", "Test description");
    }

    @Test
    public void testPriorityDecorator() {
        PriorityAlertDecorator priorityAlert = new PriorityAlertDecorator(baseAlert, 
            PriorityAlertDecorator.Priority.HIGH, "Critical condition");
        
        assertEquals(PriorityAlertDecorator.Priority.HIGH, priorityAlert.getPriority());
        assertEquals(1, priorityAlert.getPatientId());
        assertEquals(testRecord, priorityAlert.getVitals());
        assertEquals(1000L, priorityAlert.getTimestamp());
        assertEquals("TestAlert", priorityAlert.getAlertType());
        assertEquals("Test description", priorityAlert.getDescription());
    }

    @Test
    public void testPriorityUpdate() {
        PriorityAlertDecorator priorityAlert = new PriorityAlertDecorator(baseAlert, 
            PriorityAlertDecorator.Priority.LOW, "Initial condition");
        
        priorityAlert.updatePriority(PriorityAlertDecorator.Priority.CRITICAL, "Condition worsened");
        assertEquals(PriorityAlertDecorator.Priority.CRITICAL, priorityAlert.getPriority());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullAlert() {
        new PriorityAlertDecorator(null, PriorityAlertDecorator.Priority.LOW, "Test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPriority() {
        new PriorityAlertDecorator(baseAlert, null, "Test");
    }

    @Test
    public void testRepeatedDecorator() {
        RepeatedAlertDecorator repeatedAlert = new RepeatedAlertDecorator(baseAlert, 1000L, 3);
        
        assertEquals(1, repeatedAlert.getPatientId());
        assertEquals(testRecord, repeatedAlert.getVitals());
        assertEquals(1000L, repeatedAlert.getTimestamp());
        assertEquals("TestAlert", repeatedAlert.getAlertType());
        assertEquals("Test description", repeatedAlert.getDescription());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidRepeatInterval() {
        new RepeatedAlertDecorator(baseAlert, 0L, 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeMaxRepeatCount() {
        new RepeatedAlertDecorator(baseAlert, 1000L, -1);
    }
} 