package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

/**
 * Implementation of the OutputStrategy interface that transmits data over TCP.
 * This class creates a TCP server on the specified port and sends output data
 * to connected clients in a comma-separated format.
 */
public class TcpOutputStrategy implements OutputStrategy {

    /** The server socket listening for client connections */
    private ServerSocket serverSocket;
    
    /** The client socket representing a connected client */
    private Socket clientSocket;
    
    /** PrintWriter to send data to the connected client */
    private PrintWriter out;

    /**
     * Creates a new TCP output strategy that listens on the specified port.
     * The server starts immediately and begins accepting client connections
     * in a separate thread.
     *
     * @param port The TCP port to listen on for client connections
     */
    public TcpOutputStrategy(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server started on port " + port);

            // Accept clients in a new thread to not block the main thread
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends data to connected clients in a comma-separated format.
     * The data is formatted as: patientId,timestamp,label,data
     * If no client is connected, the output is silently dropped.
     *
     * @param patientId The ID of the patient associated with this data
     * @param timestamp The timestamp when the data was captured
     * @param label The label or type of the data
     * @param data The actual data content to be transmitted
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        if (out != null) {
            String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
            out.println(message);
        }
    }
}
