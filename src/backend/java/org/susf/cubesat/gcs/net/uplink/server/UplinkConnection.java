package org.susf.cubesat.gcs.net.uplink.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.susf.cubesat.gcs.net.uplink.callback.PacketListener;
import org.susf.cubesat.gcs.net.uplink.protocol.Packet;
import org.susf.cubesat.gcs.net.uplink.protocol.PacketEnvelope;

import java.io.IOException;
import org.java_websocket.WebSocket;

public class UplinkConnection {

    private static final Logger LOGGER = LogManager.getLogger(UplinkConnection.class);

    private final WebSocket client;
    private final UplinkServer server;

    private volatile PacketListener listener = envelop -> LOGGER.error("NO LISTENER SET => PacketEnvelope received: " + envelop);

    public UplinkConnection(WebSocket client, UplinkServer server) {
        this.client = client;
        this.server = server;

        LOGGER.info("Initialising client connection for address: " + client.getRemoteSocketAddress());

        if (!isConnected())
            throw new RuntimeException("Cannot create connection for: " + client.getRemoteSocketAddress() + ": socket not connected");

        System.out.print("\n");
    }

    public void onMessage(String message) throws IOException {

            // Get the codec, and use it to decode the input, deserialising it into an envelope object,
            // including its internal packet object
            PacketEnvelope envelop = server.getCodec().decode(message);

            if (envelop == null) {
                throw new IOException("Failed to decode envelop: " + message    );
            }

            // If a listener has been registered, then invoke its callback, passing in the envelope
            if (listener != null) listener.onPacketReceived(envelop);
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
        if (!client.isClosed()) client.close();
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