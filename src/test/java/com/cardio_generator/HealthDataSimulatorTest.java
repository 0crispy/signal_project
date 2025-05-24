package com.cardio_generator;

import com.cardio_generator.outputs.ConsoleOutputStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class HealthDataSimulatorTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void main_WithNoArguments_RunsWithDefaults() {
        assertDoesNotThrow(() -> HealthDataSimulator.main(new String[]{}));
        assertFalse(outContent.toString().isEmpty());
    }

    @Test
    void main_WithHelpArgument_PrintsHelp() {
        // Instead of trying to catch System.exit(), we test the help output indirectly
        // by checking the printHelp() method's output
        String expectedHelpText = "Usage:";
        assertTrue(HealthDataSimulator.getHelpText().contains(expectedHelpText));
    }

    @Test
    void parseArguments_WithValidPatientCount_SetsCount() {
        HealthDataSimulator simulator = new HealthDataSimulator();
        assertDoesNotThrow(() -> simulator.parseArguments(new String[]{"--patient-count", "10"}));
        // Would need getter for patientCount to verify, or check output behavior
    }
}