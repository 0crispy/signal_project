package com.cardio_generator;

import java.io.IOException;

/**
 * Main router class to handle execution of different application components
 */
public class Main {
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
                    com.cardio_generator.HealthDataSimulator.main(simulatorArgs);
                    break;

                case "help":
                case "--help":
                case "-h":
                    printUsage();
                    break;

                default:
                    System.err.println("Unknown command: " + args[0]);
                    printUsage();
                    System.exit(1);
            }
        } else {
            // Default to HealthDataSimulator for backward compatibility
            com.cardio_generator.HealthDataSimulator.main(args);
        }
    }

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