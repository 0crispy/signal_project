package com.data_management;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;


public class TestWebSocketServer extends WebSocketServer {

    private WebSocket clientConnection;

    private final AtomicBoolean clientConnected = new AtomicBoolean(false);

    public TestWebSocketServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        this.clientConnection = conn;

        clientConnected.set(true);
        System.out.println("Test server: New connection from " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        if (conn == clientConnection) {
            this.clientConnection = null;
            clientConnected.set(false);
        }

        System.out.println("Test server: Connection closed");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("Test server error: " + ex.getMessage());
        if (conn == clientConnection) {

            clientConnection = null;
            clientConnected.set(false);
        }
    }

    @Override
    public void onStart() {
        System.out.println("Test WebSocket server started");
    }

    public boolean isClientConnected() {
        return clientConnected.get();
        
    }

    public void sendTestData(String data) {
        if (clientConnection != null && clientConnection.isOpen()) {
            clientConnection.send(data);
        } else {
            throw new RuntimeException("No client connected to send test data");
        }
    }
} 