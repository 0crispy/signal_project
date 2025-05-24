package com.data_management;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileDataReaderTest {
    private DataStorage dataStorage;
    private FileDataReader fileDataReader;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        dataStorage = new DataStorage();
    }

    @Test
    public void testReadValidData() throws IOException {
        File testFile = tempFolder.newFile("patient_data.txt");
        String testData = "1,1609459200000,SystolicPressure,120\n" +
                "1,1609459260000,BloodSaturation,98\n" +
                "2,1609459320000,HeartRate,75\n";
        Files.write(testFile.toPath(), testData.getBytes());

        fileDataReader = new FileDataReader(tempFolder.getRoot().getAbsolutePath());
        fileDataReader.readData(dataStorage);

        List<PatientRecord> patient1Records = dataStorage.getRecords(1, 0, Long.MAX_VALUE);
        assertEquals(2, patient1Records.size());

        List<PatientRecord> patient2Records = dataStorage.getRecords(2, 0, Long.MAX_VALUE);
        assertEquals(1, patient2Records.size());
    }

    @Test
    public void testReadAlternativeFormat() throws IOException {
        File testFile = tempFolder.newFile("alt_format.txt");
        String testData = "1,120,SystolicPressure,1609459200000\n" +
                "1,98,BloodSaturation,1609459260000\n";
        Files.write(testFile.toPath(), testData.getBytes());

        fileDataReader = new FileDataReader(tempFolder.getRoot().getAbsolutePath());
        fileDataReader.readData(dataStorage);

        List<PatientRecord> records = dataStorage.getRecords(1, 0, Long.MAX_VALUE);
        assertEquals(2, records.size());
    }

    @Test
    public void testSkipInvalidLines() throws IOException {
        File testFile = tempFolder.newFile("mixed_data.txt");
        String testData = "# This is a comment\n" +
                "1,1609459200000,SystolicPressure,120\n" +
                "invalid,line,format\n" +
                "\n" +
                "2,1609459260000,HeartRate,75\n";
        Files.write(testFile.toPath(), testData.getBytes());

        fileDataReader = new FileDataReader(tempFolder.getRoot().getAbsolutePath());
        fileDataReader.readData(dataStorage);

        assertEquals(2, dataStorage.getAllPatients().size());
    }

    @Test(expected = IOException.class)
    public void testNonexistentDirectory() throws IOException {
        fileDataReader = new FileDataReader("/nonexistent/directory");
        fileDataReader.readData(dataStorage);
    }

    @Test
    public void testEmptyDirectory() throws IOException {
        fileDataReader = new FileDataReader(tempFolder.getRoot().getAbsolutePath());
        fileDataReader.readData(dataStorage);
        assertTrue(dataStorage.getAllPatients().isEmpty());
    }

    @Test
    public void testMultipleFiles() throws IOException {
        File file1 = tempFolder.newFile("data1.txt");
        File file2 = tempFolder.newFile("data2.txt");

        Files.write(file1.toPath(), "1,1609459200000,SystolicPressure,120\n".getBytes());
        Files.write(file2.toPath(), "2,1609459260000,HeartRate,75\n".getBytes());

        fileDataReader = new FileDataReader(tempFolder.getRoot().getAbsolutePath());
        fileDataReader.readData(dataStorage);

        assertEquals(2, dataStorage.getAllPatients().size());
    }
}