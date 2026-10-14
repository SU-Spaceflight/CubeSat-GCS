package org.susf.cubesat.gcs.net.uplink.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.susf.cubesat.gcs.net.uplink.callback.PacketListener;
import org.susf.cubesat.gcs.net.uplink.protocol.Packet;
import org.susf.cubesat.gcs.net.uplink.protocol.PacketEnvelope;

import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import org.java_websocket.WebSocket;

public class UplinkConnection {

    private static final Logger LOGGER = LogManager.getLogger(UplinkConnection.class);

    private final WebSocket client;
    private final LinkedBlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    private final UplinkServer server;

    private volatile PacketListener listener = envelop -> LOGGER.error("NO LISTENER SET => PacketEnvelope received: " + envelop);
    private final Thread listeningThread;

    public UplinkConnection(WebSocket client, UplinkServer server) {
        this.client = client;
        this.server = server;

        LOGGER.info("Initialising client connection for address: " + client.getRemoteSocketAddress());

        if (!isConnected())
            throw new RuntimeException("Cannot create connection for: " + client.getRemoteSocketAddress() + ": socket not connected");

        System.out.print("\n");

        // Start a new thread to concurrently listen for packets arriving on the socket
        listeningThread = new Thread(this::consumePackets, "UPLINK_CONNECTION_CONSUMER");
        listeningThread.start();
    }

    public void onMessage(String message) {
        messageQueue.add(message);
    }

    private void consumePackets() {

        // Loop on the consumer thread until interrupted (signal to stop)
        while (isConnected() && !Thread.interrupted()) {
            try {
                PacketEnvelope envelop = awaitPacket();

                // If a listener has been registered, then invoke its callback, passing in the envelope
                if (listener != null) listener.onPacketReceived(envelop);

            } catch (EOFException e) {
                break; // Client closed connection
            } catch (IOException | IllegalArgumentException | InterruptedException e) {
                System.out.print("\n");
                LOGGER.error("Error in client connection: " + client.getRemoteSocketAddress(), e);
                System.out.print("\n");

                break;
            }
        }

        // Close the connection
        if (!client.isClosed()) client.close();

        LOGGER.info("Consumer terminated: " + client.getRemoteSocketAddress());
    }

    public PacketEnvelope awaitPacket() throws IOException, InterruptedException {
        if (!isConnected()) {
            throw new IOException("Cannot get envelop: Client not connected");
        }

        // Take the next message from the queue, blocking if empty
        String in = messageQueue.take();

        // Get the codec, and use it to decode the input, deserialising it into an envelope object,
        // including its internal packet object
        PacketEnvelope envelop = server.getCodec().decode(in);

        if (envelop == null) {
            throw new IOException("Failed to decode envelop: " + in);
        }

        return envelop;
    }

    public void sendPacket(Packet packet) throws IOException {
        if (!isConnected()) {
            throw new IOException("Cannot send packet: Client not connected");
        }

        // Get the codec, and use it to encode the packet, serialising it into json, then wrighting it to the output stream
        String out = server.getCodec().encode(packet);
        client.send(out);
    }

    public void close() {
        LOGGER.info("Terminating client connection for address: " + client.getRemoteSocketAddress());

        // If not the listening thread, interrupt the listening thread to break out of its while loop
        // Block on the listening thread until it has finished, then return
        if (listeningThread != Thread.currentThread() && listeningThread.isAlive()) {
            try {
                listeningThread.interrupt();
                listeningThread.join();

            } catch ( InterruptedException e) {
                LOGGER.error("Error closing connection:", e);
                System.out.print("\n");

                // Close the connection forcefully
                if (!client.isClosed()) client.close();
            }

        // In the case where the current thread is not the listening thread, but for some reason the listening thread it not alive,
        // close the connection from this thread instead
        } else {
            if (!client.isClosed()) client.close();
        }
    }

    public void onPacketReceived(PacketListener listener) {
        this.listener = listener;
    }

    public boolean isConnected() {
        return client != null && !client.isClosed();
    }

    public WebSocket getClient() {
        return client;
    }
}
