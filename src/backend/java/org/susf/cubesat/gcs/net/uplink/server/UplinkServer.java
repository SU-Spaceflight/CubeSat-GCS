package org.susf.cubesat.gcs.net.uplink.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.susf.cubesat.gcs.net.uplink.protocol.codec.PacketCodec;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class UplinkServer extends WebSocketServer {

    private static final Logger LOGGER = LogManager.getLogger(UplinkServer.class);

    private final Object lock = new Object();
    private volatile CompletableFuture<UplinkConnection> connectionFuture = new CompletableFuture<>();
    private boolean closed;

    private final PacketCodec packetCodec;

    public UplinkServer(int port, PacketCodec packetCodec) {
        super(new InetSocketAddress(port));
        this.packetCodec = packetCodec;
    }

    @Override
    public void onStart() {
        try {
            System.out.print("\n");
            LOGGER.info("===============================================");
            LOGGER.info(" Starting uplink server on:");
            LOGGER.info(" - Local: http://" + getAddress() + ":" + getPort());
            LOGGER.info(" - Network: http://" + InetAddress.getLocalHost().getHostAddress() + ":" + getPort());
            LOGGER.info("===============================================\n");
        } catch (UnknownHostException e) {
            LOGGER.warn("Failed to resolve host:", e);
        }

        LOGGER.info("Server started successfully\n");
    }

    @Override
    public void onOpen(WebSocket socket, ClientHandshake handshake) {
        synchronized (lock) {
            if (connectionFuture.isDone()) {
                LOGGER.error("Rejecting connection from " + socket.getRemoteSocketAddress() + ": only one connection permitted");
                socket.close(1008, "Only one connection is permitted at a time");
                return;
            }

            System.out.print("\n");
            LOGGER.info("-----------------------------------------------");
            LOGGER.info("Client connected: " + socket.getRemoteSocketAddress());
            LOGGER.info("-----------------------------------------------\n");

            UplinkConnection connection = new UplinkConnection(socket, this);
            connectionFuture.complete(connection);
        }
    }

    @Override
    public void onMessage(WebSocket socket, String message) {
        UplinkConnection connection = currentConnection();

        if (connection == null || connection.getClient() != socket) {
            LOGGER.error("Secondary client at " + socket.getRemoteSocketAddress() + " attempted to send message: " + message + ". Only one connection is permitted at a time");
            socket.close(1008, "Only one connection is permitted at a time");
            return;
        }

        try {
            connection.onMessage(message);
        } catch (Exception e) {
            LOGGER.error("Error whilst processing message: ", e);
        }
    }


    @Override
    public void onError(WebSocket socket, Exception e) {
        if (socket == null) {
            LOGGER.error("Fatal server error:", e);

            synchronized (lock) {
                closed = true;
                connectionFuture.completeExceptionally(e);
            }
            return;
        }

        LOGGER.error("WebSocket error for client: " + socket.getRemoteSocketAddress(), e);
        socket.close(1011, "Internal error");

    }

    @Override
    public void onClose(WebSocket socket, int code, String reason, boolean remote) {
        synchronized (lock) {
            if (connectionFuture.isDone() && !connectionFuture.isCompletedExceptionally() && connectionFuture.join().getClient() == socket) {

                System.out.print("\n");
                LOGGER.info("-----------------------------------------------");
                LOGGER.info("Client disconnected: " + socket.getRemoteSocketAddress());
                LOGGER.info("Reason: " + reason);
                LOGGER.info("-----------------------------------------------\n");

                if (!closed) {
                    connectionFuture = new CompletableFuture<>();
                } else {
                    CompletableFuture<UplinkConnection> failed = new CompletableFuture<>();
                    failed.completeExceptionally(new CancellationException("Server closed"));
                    connectionFuture = failed;
                }
            }
        }
    }

    // if client looses connection, thread should keep waiting until reconnect
    public UplinkConnection getOrAwaitConnection() throws InterruptedException, ExecutionException {
        CompletableFuture<UplinkConnection> future;

        // Get a reference to future, but block outside of syncronised to prevent deadlock
        synchronized (lock) {
            future = connectionFuture;
        }
        return future.get();
    }

    private UplinkConnection currentConnection() {
        synchronized (lock) {
            return connectionFuture.isDone() && !connectionFuture.isCompletedExceptionally() ? connectionFuture.join() : null;
        }
    }

    public void close() {
        synchronized (lock) {
            closed = true;
            connectionFuture.completeExceptionally(new CancellationException("Server closed"));
        }

        try {
            stop(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.print("\n");
            LOGGER.error("Error closing server: ", e);
            System.out.print("\n");
        }
    }

    public PacketCodec getCodec() {
        return packetCodec;
    }
}
