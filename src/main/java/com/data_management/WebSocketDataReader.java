package com.data_management;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Reads patient data from a websocket server,
 * connects to a server and processes incoming data messages.
 */
public class WebSocketDataReader implements DataReader {
    private final String serverUri;
    private PatientDataWebSocketClient client;
    private final CountDownLatch connectionLatch;
    private static final int CONNECTION_TIMEOUT_SECONDS = 10;

    /**
     * Creates a new reader that connects to a websocket server
     * 
     * @param serverUri The address of the server to connect to
     */
    public WebSocketDataReader(String serverUri) {
        this.serverUri = serverUri;
        this.connectionLatch = new CountDownLatch(1);
    }

    /**
     * connects to the server and starts  reading data
     * 
     * @param dataStorage Where to store the received data
     * @throws IOException If can't connect to server
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        try {
             
             //make a new client that will store data
            client = new PatientDataWebSocketClient(serverUri, dataStorage) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    super.onOpen(handshake);
                    // tell waiting threads we're connected
                    connectionLatch.countDown();
                }
            };

            //try to connect
            client.connect();

            //wait for connection or timeout
            if (!connectionLatch.await(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new IOException("Failed to connect within " + 
                    CONNECTION_TIMEOUT_SECONDS + " seconds");
            }

            //check if really connected
            if (!client.isOpen()) {
                throw new IOException("Failed to connect to server");
            }
        } catch (URISyntaxException e) {
            throw new IOException("Bad server address: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Connection was interrupted", e);
        }
    }

    /**
     * disconnects from the server
     */
    public void stop() {
        if (client != null) {
            client.close();
        }
    }

    /**
     * Handles the actual websocket connection and processes messages,
     * converts json messages into patient data
     */
    private static class PatientDataWebSocketClient extends WebSocketClient {
        private final DataStorage dataStorage;
        private final ObjectMapper objectMapper;

        /**
         * creates new client that will store data
         * 
         * @param serverUri Server to connect to
         * @param dataStorage Where to store the data
         */
        public PatientDataWebSocketClient(String serverUri, DataStorage dataStorage) throws URISyntaxException {
            super(new URI(serverUri));
            this.dataStorage = dataStorage;
            this.objectMapper = new ObjectMapper();
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            System.out.println("Connected to server");
        }

        /**
         * handles data messages from the server,
         * expects json with patientId, value, recordType and timestamp
         */
        @Override
        public void onMessage(String message) {
            try {
                // parse the json message
                 JsonNode data = objectMapper.readTree(message);
                

                // make sure all required fields exist
                if (!data.has("patientId") || !data.has("value") || 
                    !data.has("recordType") || !data.has("timestamp")) {
                    System.err.println("Missing required fields in message: " + message);
                    return;

                }

                // check that fields have correct types
                JsonNode patientIdNode = data.get("patientId");
                JsonNode valueNode = data.get("value");
                JsonNode recordTypeNode = data.get("recordType");
                JsonNode timestampNode = data.get("timestamp");

                if (!patientIdNode.isInt() || !valueNode.isNumber() || 
                    !recordTypeNode.isTextual() || !timestampNode.isNumber()) {
                    System.err.println("Invalid field types in message: " + message);
                    return;
                }

                // get the values from json
                int patientId = patientIdNode.asInt();
                double value = valueNode.asDouble();
                String recordType = recordTypeNode.asText();
                long timestamp = timestampNode.asLong();

                // store the data
                dataStorage.addPatientData(patientId, value, recordType, timestamp);

            } 
            catch (JsonProcessingException e) {
                System.err.println("Bad json message: " + e.getMessage());
            } 
            catch (Exception e) {
                System.err.println("Error processing message: " + e.getMessage());
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
             System.out.println("Connection closed by " + (remote ? "server" : "us") + 
                " Code: " + code + " Reason: " + reason);
        }

        @Override
        public void onError(Exception ex) {

            System.err.println("WebSocket error: " + ex.getMessage());
        }
    }
} 