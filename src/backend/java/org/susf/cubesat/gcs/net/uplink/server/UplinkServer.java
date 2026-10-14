package org.susf.cubesat.gcs.net.uplink.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.susf.cubesat.gcs.net.uplink.protocol.codec.PacketCodec;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

public class UplinkServer {

    private static final Logger LOGGER = LogManager.getLogger(UplinkServer.class);

    private final ServerSocket socket;
    private final Thread acceptThread;
    private volatile CompletableFuture<UplinkConnection> currentConnection = new CompletableFuture<>();

    private final PacketCodec packetCodec;

    public UplinkServer(InetAddress ipAddress, int port, PacketCodec packetCodec) {
        this.packetCodec = packetCodec;

        try {
            System.out.print("\n");
            LOGGER.info("===============================================");
            LOGGER.info(" Starting uplink server on:");
            LOGGER.info(" - Local: http://" + ipAddress + ":" + port);
            LOGGER.info(" - Network: http://" + InetAddress.getLocalHost().getHostAddress() + ":" + port);
            LOGGER.info("===============================================\n");

            // Open a new socket on the given port
            socket = new ServerSocket(port);

            // Start a separate thread to handle connections to the socket concurrently
            acceptThread = new Thread(this::acceptConnections, "UPLINK_CONNECTION_HANDLER");
            acceptThread.start();

        } catch (Exception e) {
            throw new RuntimeException("Error starting server: " + e.getMessage());
        }

        LOGGER.info("Server started successfully\n");
    }

    public void acceptConnections() {
        // Loop on the uplink handler thread until interrupted (signal to stop)
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Block until a client connects
                Socket client = socket.accept();
                LOGGER.info("-----------------------------------------------");
                LOGGER.info("Client connected: " + client.getRemoteSocketAddress());
                LOGGER.info("-----------------------------------------------");

                // Create a new connection object
                UplinkConnection connection = new UplinkConnection(client, this);

                // Complete the currentConnection future object, so any code blocked on it can be awakend
                currentConnection.complete(connection);

            } catch (Exception e) {
                System.out.print("\n");
                LOGGER.error("Error accepting connection:", e);
                System.out.print("\n");
            }
        }

        close();
        LOGGER.info("Server closed");
    }

    public void onClientDisconnect(UplinkConnection connection) {
        LOGGER.info("Unregistered connection for client with address: " + connection.getClient().getRemoteSocketAddress() + "\n");

        // Reset future object so the getOrAwaitConnection method blocks until a new connection is made
        currentConnection = new CompletableFuture<>();
    }

    public UplinkConnection getOrAwaitConnection() {
        try {
            return currentConnection.get();
        } catch (Exception e) {
            System.out.print("\n");
            LOGGER.error("Error waiting for connection: ", e);
            System.out.print("\n");
            return null;
        }
    }

    public void close() {
        try {
            // If not the accept thread, interrupt the accept thread to break out of its while loop, which will then cause it to enter this method as well
            // Block on the accept thread until it has finished, then return
            if (Thread.currentThread() != acceptThread && acceptThread.isAlive()) {
                acceptThread.interrupt();
                acceptThread.join();

            // When the accept thread enters this method it ensures the current connection is terminated, and then closes the socket
            // In the case where the current thread is not the accept thread, but the accept thread it not alive, the current thread will enter this
            // else statement regardless, to ensure the connection is always closed
            } else {
                if (currentConnection.isDone()) {
                    currentConnection.get().terminate();
                }

                if (socket != null && !socket.isClosed()) socket.close();
            }
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
