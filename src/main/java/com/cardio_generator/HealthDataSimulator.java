package com.cardio_generator;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.cardio_generator.generators.AlertGenerator;
import com.cardio_generator.generators.BloodPressureDataGenerator;
import com.cardio_generator.generators.BloodSaturationDataGenerator;
import com.cardio_generator.generators.BloodLevelsDataGenerator;
import com.cardio_generator.generators.ECGDataGenerator;
import com.cardio_generator.outputs.ConsoleOutputStrategy;
import com.cardio_generator.outputs.FileOutputStrategy;
import com.cardio_generator.outputs.OutputStrategy;
import com.cardio_generator.outputs.TcpOutputStrategy;
import com.cardio_generator.outputs.WebSocketOutputStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Simulates health data for patients and sends it out via different strategies.
 * this implements the Singleton pattern to ensure that only single simulator instamce exists
 */
public class HealthDataSimulator {
    private static HealthDataSimulator instance;
    private static final Object LOCK = new Object();
    
    private ScheduledExecutorService scheduler;
    private final List<OutputStrategy> outputStrategies;
    private final Random random;
    private boolean isRunning;
    
    private BloodPressureDataGenerator bpGenerator;
    private BloodSaturationDataGenerator satGenerator;
    private BloodLevelsDataGenerator levelsGenerator;
    private ECGDataGenerator ecgGenerator;
    private AlertGenerator alertGenerator;

    //default settings
    private HealthDataSimulator() {
        this.outputStrategies = Collections.synchronizedList(new ArrayList<>());
        this.random = new Random();
        this.isRunning = false;
    }

    /**
     * gets the singleton instance of HealthDataSimulator. and 
     * creates it if it doesn't exist using double-checked locking.
     * 
     * @return the singleton instance of HealthDataSimulator
     */
    public static HealthDataSimulator getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new HealthDataSimulator();
                }
            }
        }
        return instance;
    }

    /**
     * adds an output strategy to the simulator.
     * 
     * @param strategy the output strategy to add
     */
    public void addOutputStrategy(OutputStrategy strategy) {
        if (strategy != null) {
            outputStrategies.add(strategy);
        }
    }

    /**
     * initializes the data generators with the specified number of patients
     * 
     * @param patientCount number of patients to simulate data for
     */
    public void initializeGenerators(int patientCount) {
        bpGenerator = new BloodPressureDataGenerator(patientCount);
        satGenerator = new BloodSaturationDataGenerator(patientCount);
        levelsGenerator = new BloodLevelsDataGenerator(patientCount);
        ecgGenerator = new ECGDataGenerator(patientCount);
        alertGenerator = new AlertGenerator(patientCount);
    }

    /**
     * starts the simulation with the specified interval and runs for the specified duration
     * 
     * @param intervalMs the interval between data generations in milliseconds
     * @param durationMs how long to run the simulation in milliseconds (0  for indefinite)
     */
    public void startSimulation(long intervalMs, long durationMs) {
        synchronized (LOCK) {
            if (!isRunning) {
                if (scheduler == null || scheduler.isShutdown()) {
                    scheduler = Executors.newScheduledThreadPool(5);
                }
                
                isRunning = true;
                scheduler.scheduleAtFixedRate(this::generateAndOutputData, 0, intervalMs, TimeUnit.MILLISECONDS);
                
                if (durationMs > 0) {
                    scheduler.schedule(() -> {
                        stopSimulation();
                    }, durationMs, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    /**
     * starts the simulation with the specified interval
     * 
     * @param intervalMs the interval between data generations in milliseconds
     */
    public void startSimulation(long intervalMs) {
        startSimulation(intervalMs, 0); // Run indefinitely
    }

    /**
     * Stops the simulation.
     */
    public void stopSimulation() {
        synchronized (LOCK) {
            if (isRunning) {
                isRunning = false;
                if (scheduler != null && !scheduler.isShutdown()) {
                    scheduler.shutdown();
                    try {
                        if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                            scheduler.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        scheduler.shutdownNow();
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    /**
     * generates and outputs data for all configured patients
     */
    private void generateAndOutputData() {
        if (!isRunning) {
            return;
        }

        for (OutputStrategy strategy : outputStrategies) {
            for (int patientId = 1; patientId <= 5; patientId++) {
                if (bpGenerator != null) {
                    bpGenerator.generate(patientId, strategy);
                }
                if (satGenerator != null) {
                    satGenerator.generate(patientId, strategy);
                }
                if (levelsGenerator != null) {
                    levelsGenerator.generate(patientId, strategy);
                }
                if (ecgGenerator != null) {
                    ecgGenerator.generate(patientId, strategy);
                }
                if (alertGenerator != null) {
                    alertGenerator.generate(patientId, strategy);
                }
            }
        }
    }

    public static void main(String[] args) {
        HealthDataSimulator simulator = HealthDataSimulator.getInstance();
        
        String outputType = "console";
        String outputPath = null;
        long duration = 0;
        
        for (String arg : args) {
            if (arg.startsWith("--output=")) {
                outputType = arg.substring("--output=".length());
            } else if (arg.startsWith("--path=")) {
                outputPath = arg.substring("--path=".length());
            } else if (arg.startsWith("--duration=")) {
                try {
                    duration = Long.parseLong(arg.substring("--duration=".length()));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid duration format. Using default.");
                }
            }
        }

        OutputStrategy outputStrategy;
        switch (outputType.toLowerCase()) {
            case "file":
                if (outputPath == null) {
                    System.err.println("File output requires --path argument");
                    return;
                }
                outputStrategy = new FileOutputStrategy(outputPath);
                break;
            case "tcp":
                outputStrategy = new TcpOutputStrategy(8080);
                break;
            case "websocket":
                outputStrategy = new WebSocketOutputStrategy(8081);
                break;
            case "console":
            default:
                outputStrategy = new ConsoleOutputStrategy();
                break;
        }

        simulator.addOutputStrategy(outputStrategy);
        simulator.initializeGenerators(10);
        simulator.startSimulation(1000, duration);

        if (duration == 0) {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                simulator.stopSimulation();
            }
        } else {
            try {
                Thread.sleep(duration + 100);
            } catch (InterruptedException e) {
                simulator.stopSimulation();
            }
        }
    }
}
