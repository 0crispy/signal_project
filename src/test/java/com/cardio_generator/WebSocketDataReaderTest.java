package com.cardio_generator;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import com.data_management.WebSocketDataReader;

public class WebSocketDataReaderTest {
    private WebSocketDataReader reader;
    private DataStorage dataStorage;
    private TestWebSocketServer server;
    private static final String TEST_URI = "ws://localhost:8025/test";
    private static final int SERVER_PORT = 8025;

    @Before
    public void setUp() throws Exception {
        server = new TestWebSocketServer(SERVER_PORT);
        server.start();
        Thread.sleep(100);
        
        dataStorage = DataStorage.getInstance();
        reader = new WebSocketDataReader(TEST_URI);
        
        for (Patient patient : dataStorage.getAllPatients()) {
            dataStorage.clearRecords(patient.getPatientId());
        }
    }

    @After
    public void tearDown() throws Exception {
        if (reader != null) {
            reader.stop();
        }
        if (server != null) {
            server.stop(1000);
        }
        Thread.sleep(100);
    }

    @Test
    public void testWebSocketConnection() throws IOException {
        reader.readData(dataStorage);
        assertTrue("WebSocket connection should be established", server.isClientConnected());
    }

    @Test
    public void testDataReception() throws Exception {
        CountDownLatch dataLatch = new CountDownLatch(1);
        final int testPatientId = 1;
        
        reader.readData(dataStorage);
        
        Thread.sleep(500);
        
        server.sendTestData(String.format(
            "{\"patientId\":%d,\"value\":120.5,\"recordType\":\"HeartRate\",\"timestamp\":%d}",
            testPatientId, System.currentTimeMillis()
        ));

        Thread.sleep(1000);

        List<PatientRecord> records = dataStorage.getRecords(testPatientId, 0L, Long.MAX_VALUE);
        assertFalse("Should have received and stored data", records.isEmpty());
        assertEquals("Should have correct measurement value", 120.5, records.get(0).getMeasurementValue(), 0.001);
    }

    @Test(expected = IOException.class)
    public void testConnectionTimeout() throws IOException, InterruptedException {
        server.stop(1000);
        Thread.sleep(100);
        reader.readData(dataStorage);
    }

    @Test
    public void testMalformedDataHandling() throws Exception {
        reader.readData(dataStorage);
         Thread.sleep(500);

        server.sendTestData("invalid json data");
        server.sendTestData("{\"patientId\": \"not a number\"}");
        
        int testPatientId = 2;
        server.sendTestData(String.format(
            "{\"patientId\":%d,\"value\":120.5,\"recordType\":\"HeartRate\",\"timestamp\":%d}",
            testPatientId, System.currentTimeMillis()
        ));

        Thread.sleep(1000 );
        
        List<PatientRecord> records = dataStorage.getRecords(testPatientId, 0L, Long.MAX_VALUE);
        assertEquals("Should have stored only the valid data", 1, records.size());
    }

    @Test
    public void testMultipleDataMessages() throws Exception {
        reader.readData(dataStorage);
        Thread.sleep(500); 
        for (int i = 0; i < 5; i++) {
            server.sendTestData(String.format(
                "{\"patientId\":1,\"value\":%f,\"recordType\":\"HeartRate\",\"timestamp\":%d}",
                60.0 + i, System.currentTimeMillis() 
            ));
            Thread.sleep(100); 
        }

        Thread.sleep(1000);  
        
        List<PatientRecord> records = dataStorage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals("Should have stored all messages", 5, records.size());
    }

    @Test
    public void testMissingFields() throws Exception {
        reader.readData(dataStorage);
        Thread.sleep(500);  
        server.sendTestData("{\"patientId\":1,\"value\":120.5}"); // Missing recordType and timestamp
        
        Thread.sleep(1000);
        
        List<PatientRecord> records = dataStorage.getRecords(1, 0L, Long.MAX_VALUE);
        assertTrue("Should not store data with missing required fields", records.isEmpty());
    }

    @Test
    public void testInvalidFieldTypes() throws Exception {
        reader.readData(dataStorage);
        Thread.sleep(500);

        server.sendTestData("{\"patientId\":\"invalid\",\"value\":\"not a number\",\"recordType\":123,\"timestamp\":\"invalid\"}");
        //
        Thread.sleep(1000);
     
        List<Patient> patients = dataStorage.getAllPatients();
        assertTrue("Should not store data with invalid field types", patients.isEmpty());
    }
} 