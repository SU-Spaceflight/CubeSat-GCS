package org.susf.cubesat.gcs.net.uplink.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.susf.cubesat.gcs.net.uplink.callback.PacketListener;
import org.susf.cubesat.gcs.net.uplink.protocol.Packet;
import org.susf.cubesat.gcs.net.uplink.protocol.PacketEnvelope;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

public class UplinkConnection {

    private static final Logger LOGGER = LogManager.getLogger(UplinkConnection.class);

    private final Socket client;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    private final UplinkServer server;

    private volatile PacketListener listener = envelop -> LOGGER.error("NO LISTENER SET => PacketEnvelope received: " + envelop);
    private final Thread listeningThread;

    public UplinkConnection(Socket client, UplinkServer server) throws IOException {
        this.client = client;
        this.server = server;

        LOGGER.info("Initialising client connection for address: " + client.getRemoteSocketAddress());

        // Get the sockets input and output streams
        inputStream = new DataInputStream(client.getInputStream());
        outputStream = new DataOutputStream(client.getOutputStream());

        if (!isConnected())
            throw new IOException("Cannot create connection for: " + client.getRemoteSocketAddress() + ": socket not connected");

        System.out.print("\n");

        // Start a new thread to concurrently listen for packets arriving on the socket
        listeningThread = new Thread(this::consumePackets, "UPLINK_CONNECTION_CONSUMER");
        listeningThread.start();
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
            } catch (IOException e) {
                System.out.print("\n");
                LOGGER.error("Error in client connection: " + client.getRemoteSocketAddress(), e);
                System.out.print("\n");

                break;
            }
        }

        System.out.print("\n");
        LOGGER.info("-----------------------------------------------");
        LOGGER.info("Client disconnected: " + client.getRemoteSocketAddress());
        LOGGER.info("-----------------------------------------------");

        terminate();
        LOGGER.info("Consumer terminated: " + client.getRemoteSocketAddress());
    }

    public PacketEnvelope awaitPacket() throws IOException {
        if (!isConnected()) {
            throw new IOException("Cannot get envelop: Client not connected");
        }

        // Read UTF in from the input stream (blocks until data is recived)
        String in = inputStream.readUTF();

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
        outputStream.writeUTF(out);
        outputStream.flush();
    }

    public void terminate() {
        LOGGER.info("Terminating client connection for address: " + client.getRemoteSocketAddress());
        try {
            // If not the listening thread, interrupt the listening thread to break out of its while loop, which will then cause it to enter this method as well
            // Block on the listening thread until it has finished, then return
            if (listeningThread != Thread.currentThread() && listeningThread.isAlive()) {
                listeningThread.interrupt();
                listeningThread.join();

            // When the accept thread enters this method it ensures the streams are closed, and then closes the client connection
            // In the case where the current thread is not the listening thread, but the listening thread it not alive, the current thread will enter this
            // else statement regardless, to ensure the connection is always closed
            } else {

                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
                if (!client.isClosed()) client.close();

                // Inform the server that this connection has been closed, so it can be unregistered
                // (We do this ONLY IF we have closed the connection on this thread, and have not just interupted the listening thread)
                server.onClientDisconnect(this);
            }

        } catch (IOException | InterruptedException e) {
            LOGGER.error("Error closing connection:", e);
            System.out.print("\n");

            // Ensure that the server is informed that the connection has been closed, even if an error was encountered
            server.onClientDisconnect(this);
        }
    }

    public void onPacketReceived(PacketListener listener) {
        this.listener = listener;
    }

    public boolean isConnected() {
        return client != null && !client.isClosed();
    }

    public Socket getClient() {
        return client;
    }
}
