package com.data_management;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileDataReaderTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    private FileDataReader reader;
    private DataStorage dataStorage;
    private File testDataDir;

    @Before
    public void setUp() throws IOException {
        testDataDir = tempFolder.newFolder("testData");
        reader = new FileDataReader(testDataDir.getAbsolutePath());
        dataStorage = DataStorage.getInstance();
        for (int i = 1; i <= 100; i++) {
            dataStorage.clearRecords(i);
        }
    }

    @After
    public void tearDown() {
        tempFolder.delete();
    }

    @Test
    public void testReadValidDataFile() throws IOException {
        File dataFile = new File(testDataDir, "patient1.csv");
        Files.write(dataFile.toPath(), 
            "1,1000,SystolicPressure,120\n1,2000,HeartRate,75".getBytes());

        reader.readData(dataStorage);

        List<PatientRecord> records = dataStorage.getRecords(1, 0L, Long.MAX_VALUE);
        assertFalse("Should have read some records", records.isEmpty());
        assertEquals("Should have read 2 records", 2, records.size());
    }

    @Test
    public void testReadEmptyDirectory() throws IOException {
        reader.readData(dataStorage);
        assertTrue("No records should be read from empty directory",
            dataStorage.getRecords(1, 0L, Long.MAX_VALUE).isEmpty());
    }

    @Test(expected = IOException.class)
    public void testInvalidDirectory() throws IOException {
        File invalidDir = new File(testDataDir, "nonexistent");
        reader = new FileDataReader(invalidDir.getAbsolutePath());
        reader.readData(dataStorage);
    }

    @Test
    public void testInvalidDataFormat() throws IOException {
        File dataFile = new File(testDataDir, "invalid.csv");
        Files.write(dataFile.toPath(), "invalid data format".getBytes());

        reader.readData(dataStorage);
        assertTrue("Invalid data should be skipped",
            dataStorage.getRecords(1, 0L, Long.MAX_VALUE).isEmpty());
    }
}