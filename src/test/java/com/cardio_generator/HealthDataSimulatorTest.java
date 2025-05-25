package com.cardio_generator;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.ArrayList;

import com.cardio_generator.outputs.ConsoleOutputStrategy;
import com.cardio_generator.outputs.OutputStrategy;

public class HealthDataSimulatorTest {
    private HealthDataSimulator simulator;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUp() {
        System.setOut(new PrintStream(outContent));
        simulator = HealthDataSimulator.getInstance();
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
        simulator.stopSimulation();
    }

    @Test
    public void testSimulatorInitialization() {
        assertNotNull("Simulator instance should not be null", simulator);
    }

    @Test
    public void testAddOutputStrategy() {
        OutputStrategy strategy = new ConsoleOutputStrategy();
        simulator.addOutputStrategy(strategy);
        simulator.startSimulation(100, 500); // Run for 500ms
        
        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test interrupted");
        }
        
        String output = outContent.toString();
        assertFalse("Should have generated some output", output.isEmpty());
    }

    @Test
    public void testConcurrentAccess() throws InterruptedException {
        final int threadCount = 5;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(threadCount);
        final List<HealthDataSimulator> instances = new ArrayList<>();
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for signal to start
                    instances.add(HealthDataSimulator.getInstance());
                    endLatch.countDown();
                } catch (InterruptedException e) {
                    fail("Thread interrupted");
                }
            });
        }
        
        startLatch.countDown();
        
        assertTrue(endLatch.await(5, TimeUnit.SECONDS));
        
        HealthDataSimulator firstInstance = instances.get(0);
        for (HealthDataSimulator instance : instances) {
            assertSame(firstInstance, instance);
        }
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    public void testInitializeGenerators() {
        try {
            simulator.initializeGenerators(10);
            simulator.initializeGenerators(1);
            simulator.initializeGenerators(1000);
            assertTrue(true);
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testSimulationLifecycle() throws InterruptedException {
        // Set up simulation
        simulator.initializeGenerators(5);
        TestOutputStrategy testStrategy = new TestOutputStrategy();
        simulator.addOutputStrategy(testStrategy);
        
        simulator.startSimulation(100);
        Thread.sleep(500);
        simulator.stopSimulation();
        assertTrue("Simulation should have generated some data", 
            testStrategy.getDataGenerationCount() > 0);
    }

    @Test
    public void testMultipleStartStopCycles() throws InterruptedException {
        simulator.initializeGenerators(5);
        simulator.addOutputStrategy(new ConsoleOutputStrategy());
        
        try {
            simulator.startSimulation(100);
            Thread.sleep(200);
            simulator.stopSimulation();
            
            simulator.startSimulation(100);
            Thread.sleep(200);
            simulator.stopSimulation();
        
            simulator.startSimulation(100);
            simulator.stopSimulation();
            
            assertTrue(true);
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }

    private static class TestOutputStrategy implements OutputStrategy {
        private int dataGenerationCount = 0;

        @Override
        public void output(int patientId, long timestamp, String label, String value) {
            dataGenerationCount++;
        }

        public int getDataGenerationCount() {
            return dataGenerationCount;
        }
    }
}