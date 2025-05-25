package com.cardio_generator.outputs;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * sends data to connected websocket clients,
 * this class starts a websocket server and broadcasts data to all connected clients
 */
public class WebSocketOutputStrategy implements OutputStrategy {
    // the websocket server that handles client connections
    private final SimpleWebSocketServer server;
    // flag to track if server is running
    private volatile boolean isRunning;

    /**
     * creates a new websocket server on the given port
     * 
     * @param port the port number to listen on
     */
    public WebSocketOutputStrategy(int port) {
        this.server = new SimpleWebSocketServer(port);
        startServer();
    }

    // starts the websocket server
    private void startServer() {
        try {
            server.start();
            isRunning = true;
        } catch (Exception e) {
            throw new RuntimeException("Could not start WebSocket server", e);
        }
    }

    /**
     * sends data to all connected clients
     * 
     * @param patientId Id of the patient
     * @param timestamp When the data was recorded
     * @param label Type of measurement (like heartrate)
     * @param data The actual measurement value
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        if (!isRunning) {
            return;
        }

        // make a json message with the data
        String message = String.format(
            "{\"patientId\":%d,\"timestamp\":%d,\"recordType\":\"%s\",\"value\":%s}",
            patientId, timestamp, label, data
        );

        // send to all clients
        server.broadcast(message);
    }

    /**
     * internal server class that handles websocket connections
     */
    private static class SimpleWebSocketServer extends WebSocketServer {
        // keeps track of all connected clients
        private final Set<WebSocket> connections;

        /**
         * creates server on specified port
         */
        public SimpleWebSocketServer(int port) {
            super(new InetSocketAddress(port));
            this.connections = Collections.newSetFromMap(new ConcurrentHashMap<>());
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            // add new client to our list
            connections.add(conn);
            System.out.println("New connection from " + conn.getRemoteSocketAddress());
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            // remove client when they disconnect
            connections.remove(conn);
            System.out.println("Closed connection to " + conn.getRemoteSocketAddress());
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            // log error and remove client if there's a problem
            System.err.println("WebSocket error: " + ex.getMessage());
            if (conn != null) {
                connections.remove(conn);
            }
        }

        @Override
        public void onStart() {
            // log when server starts up

            System.out.println("WebSocket server started successfully");
        }

        /**
         * sends a message to all connected clients
         */
        public void broadcast(String message) {
            synchronized (connections) {
                // loop through all clients and send message
                for (WebSocket conn : connections) {
                    if (conn.isOpen()) {

                        conn.send(message);
                    }
                }
            }
        }
    }

    /**
     * stops the websocket server and cleans up
     */
    public void stop() {
        isRunning = false; 
        if (server != null ) {
            try {
                server.stop();
            } catch (Exception e) {
                System.err.println ("Error stopping WebSocket server: " + e.getMessage());
            }
        }
    }
}
