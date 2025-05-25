package com.cardio_generator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

public class MainTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Before
    public void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(outContent));
    }

    @After
    public void stop() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        HealthDataSimulator simulator = HealthDataSimulator.getInstance();
        simulator.stopSimulation();
        outContent.reset();
    }

    @Test
    public void main_NoArguments_ExecutesWithoutErrors() {
        try {
            String[] args = new String[]{"--simulator", "--duration=1000"};
            Main.main(args);
            Thread.sleep(1500);
            String output = outContent.toString();
            assertFalse("Expected simulator output but got nothing", output.isEmpty());
        } catch (Exception e) {
            fail("Main execution failed: " + e.getMessage());
        }
    }

    @Test
    public void main_SimulatorArgument_RunsSimulator() {
        try {
            String[] args = new String[]{"--simulator", "--duration=1000"};
            Main.main(args);
            Thread.sleep(1500);
            String output = outContent.toString();
            assertFalse("Expected simulator output but got nothing", output.isEmpty());
        } catch (Exception e) {
            fail("Main execution failed: " + e.getMessage());
        }
    }

    @Test
    public void main_InvalidArgument_HandlesGracefully() {
        try {
            String[] args = new String[]{"--invalid"};
            Main.main(args);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            String output = outContent.toString();
            assertTrue("Expected error message", output.contains("Unknown command") || output.contains("Usage"));
        } catch (Exception e) {
            fail("Wrong exception type: " + e.getClass().getName());
        }
    }

    @Test
    public void main_HelpArgument_ShowsHelp() {
        try {
            String[] args = new String[]{"--help"};
            Main.main(args);
            String output = outContent.toString();
            assertTrue("Expected help message", output.contains("Usage") || output.contains("help"));
        } catch (Exception e) {
            fail("Main execution failed: " + e.getMessage());
        }
    }
}