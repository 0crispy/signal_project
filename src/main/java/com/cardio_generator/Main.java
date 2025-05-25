package com.cardio_generator;

import java.io.IOException;
import com.cardio_generator.outputs.OutputStrategy;
import com.cardio_generator.outputs.ConsoleOutputStrategy;
import com.cardio_generator.outputs.FileOutputStrategy;
import com.cardio_generator.outputs.TcpOutputStrategy;
import com.cardio_generator.outputs.WebSocketOutputStrategy;

/**
 * Routes commands to the right part of the app.
 */
public class Main {
    /**
     * Main router that calls the appropriate module.
     * @param args command line args
     * @throws IOException if something goes wrong
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            String command = args[0].toLowerCase();

            switch (command) {
                case "datastorage":
                case "--datastorage":
                    // Remove the first argument and pass the rest to DataStorage
                    String[] dataStorageArgs = new String[args.length - 1];
                    System.arraycopy(args, 1, dataStorageArgs, 0, args.length - 1);
                    com.data_management.DataStorage.main(dataStorageArgs);
                    break;

                case "simulator":
                case "--simulator":
                case "healthdatasimulator":
                    // Remove the first argument and pass the rest to HealthDataSimulator
                    String[] simulatorArgs = new String[args.length - 1];
                    System.arraycopy(args, 1, simulatorArgs, 0, args.length - 1);
                    runSimulator(simulatorArgs);
                    break;

                case "help":
                case "--help":
                case "-h":
                    printUsage();
                    break;

                default:
                    System.err.println("Unknown command: " + args[0]);
                    printUsage();
                    throw new IllegalArgumentException("Unknown command: " + args[0]);
            }
        } else {
            // Default to HealthDataSimulator for backward compatibility
            runSimulator(args);
        }
    }

    /**
     * runs the simulator with proper initialization and shutdown
     * @param args command line arguments for the simulator
     */
    private static void runSimulator(String[] args) {
        HealthDataSimulator simulator = HealthDataSimulator.getInstance();
        
        String outputType = "console";
        String outputPath = null;
        long duration = 1000;
        
        for (String arg : args) {
            if (arg.startsWith("--output=")) {
                outputType = arg.substring("--output=".length());
            } 
            else if (arg.startsWith("--path=")) {
                outputPath = arg.substring("--path=".length());
            } 
            else if (arg.startsWith("--duration=")) {

                try {
                    duration = Long.parseLong(arg.substring("--duration=".length()));
                } 
                catch (NumberFormatException e) {
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
        simulator.initializeGenerators(5);
        simulator.startSimulation(100, duration);

        try {
            Thread.sleep(duration + 100);
            simulator.stopSimulation();
        } catch (InterruptedException e) {
            simulator.stopSimulation();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Prints usage instructions.
     */
    private static void printUsage() {
        System.out.println("Usage: java -jar cardio_generator.jar [COMMAND] [OPTIONS]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  datastorage    Run the data storage and alert system");
        System.out.println("  simulator      Run the health data simulator (default)");
        System.out.println("  help           Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar cardio_generator.jar datastorage --input=/path/to/data");
        System.out.println("  java -jar cardio_generator.jar simulator --output file:/path/to/output");
    }
}
