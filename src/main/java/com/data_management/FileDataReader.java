package com.data_management;

import java.io.*;
import java.nio.file.*;
import java.util.stream.Stream;

/**
 * Reads patient data from .txt files in a given directory.
 */
public class FileDataReader implements DataReader {
    private String directoryPath;

    /**
     * Sets the folder path to read files from.
     * @param directoryPath path of the folder containing data files
     */
    public FileDataReader(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    /**
     * Reads all .txt files in the specified folder and feeds the data into DataStorage.
     * @param dataStorage storage for patient data
     * @throws IOException if folder or files can’t be read
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        Path dir = Paths.get(directoryPath);

        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            throw new IOException("Directory does not exist or is not a directory: " + directoryPath);
        }

        // Read all .txt files in the directory
        try (Stream<Path> files = Files.walk(dir)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".txt"))
                    .forEach(file -> {
                        try {
                            readFile(file, dataStorage);
                        } catch (IOException e) {
                            System.err.println("Error reading file " + file + ": " + e.getMessage());
                        }
                    });
        }
    }

    /**
     * Reads a single file and adds its content to storage.
     * @param filePath path of the file to read
     * @param dataStorage where to put the data
     * @throws IOException if the file can’t be read
     */
    private void readFile(Path filePath, DataStorage dataStorage) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Skip empty lines and comments
                }

                try {
                    parseLine(line, dataStorage);
                } catch (Exception e) {
                    System.err.println("Error parsing line " + lineNumber + " in file " +
                            filePath.getFileName() + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Parses a line and adds it to DataStorage.
     * Expected: PatientID,Timestamp,RecordType,Value 
     * or: PatientID,Value,RecordType,Timestamp.
     * @param line the line to parse
     * @param dataStorage storage for parsed data
     */
    private void parseLine(String line, DataStorage dataStorage) {
        String[] parts = line.split(",");

        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid line format: " + line);
        }

        try {
            // Try format: PatientID,Timestamp,RecordType,Value
            int patientId = Integer.parseInt(parts[0].trim());
            long timestamp = Long.parseLong(parts[1].trim());
            String recordType = parts[2].trim();
            double value = Double.parseDouble(parts[3].trim());

            dataStorage.addPatientData(patientId, value, recordType, timestamp);

        } catch (NumberFormatException e1) {
            try {
                // Try alternative format: PatientID,Value,RecordType,Timestamp
                int patientId = Integer.parseInt(parts[0].trim());
                double value = Double.parseDouble(parts[1].trim());
                String recordType = parts[2].trim();
                long timestamp = Long.parseLong(parts[3].trim());

                dataStorage.addPatientData(patientId, value, recordType, timestamp);

            } catch (NumberFormatException e2) {
                throw new IllegalArgumentException("Unable to parse numeric values in line: " + line);
            }
        }
    }
}
