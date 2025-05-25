package com.data_management;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Connects to a websocket server and receives patient data,
 * automatically tries to reconnect if connection is lost
 */
public class PatientDataWebSocketClient extends WebSocketClient {
    private final DataStorage dataStorage;
    private final ObjectMapper objectMapper;
    private static final int RECONNECT_DELAY_MS = 5000;
    private boolean shouldReconnect = true;

    /**
     * Creates new client to receive patient data
     * 
     * @param serverUri Address of the server to connect to
     * @param dataStorage Where to store the received data
     */
    public PatientDataWebSocketClient(String serverUri, DataStorage dataStorage) throws URISyntaxException {
        super(new URI(serverUri));
        this.dataStorage = dataStorage;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Called when we connect to the server
     */
    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Connected to server");
    }

    /**
     * Handles data messages from the server,
     * expects json with patientId, value, recordType and timestamp
     */
    @Override
    public void onMessage(String message) {
        try {
            JsonNode data = objectMapper.readTree(message);
            
            //get all the values from the message
            int patientId = data.get("patientId").asInt();
            double value = data.get("value").asDouble();
            String recordType = data.get("recordType").asText();
            long timestamp = data.get("timestamp").asLong();

            //save
            dataStorage.addPatientData(patientId, value, recordType, timestamp);
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    /**
     * Called when connection closes, tries to reconnect if enabled
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println( "Connection closed by " + (remote ? "server" : "us") + " Code: " + code + " Reason: " + reason);
        //try to reconnect if we should
        if (shouldReconnect) {
            handleReconnect();
        }
    }

    /**
     * Called when there's a connection error
     */
    @Override
    public void onError(Exception ex) {

        System.err.println("WebSocket error: " + ex.getMessage());
    }

    /**
     * Tries to reconnect to the server after waiting a bit
     */
    private void handleReconnect() {

        new Thread(() -> {
            try {
                // wait before trying to reconnect
                Thread.sleep(RECONNECT_DELAY_MS);
                if (shouldReconnect) {
                    // try connecting again
                    reconnect();

                }
            } 
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        ).start();
    }

    /**
     * stops trying to reconnect and closes connection
     */
    public void stopReconnecting() {
        shouldReconnect = false;
        
        close();
    }
} 