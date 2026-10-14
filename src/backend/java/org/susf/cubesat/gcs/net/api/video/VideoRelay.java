package org.susf.cubesat.gcs.net.api.video;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.susf.cubesat.gcs.util.Env;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

@Service
public class VideoRelay {

    private static final Logger LOGGER = LogManager.getLogger(VideoRelay.class);

    private final String host;
    private final int port;

    private Thread relayThread;
    private final Object lock = new Object();
    private VideoFrame latest = new VideoFrame(0, null);
    private boolean connected = true; // Flag to stop failed conections spaming console

    public VideoRelay() {
        host = Env.get("VIDEO_HOST");
        port = Integer.parseInt(Env.get("VIDEO_PORT"));
    }

    @EventListener
    public void onReady(ApplicationReadyEvent event) throws UnknownHostException {
        // Start a new thread to asyncronously relay video stream
        relayThread = new Thread(this::loop, "VIDEO_RELAY");
        relayThread.setDaemon(true);
        relayThread.start();
    }

    @PreDestroy
    void stop() {
        relayThread.interrupt();
    }

    private void loop() {
        // Loop on the relay thread until thread is interrupted
        while (!Thread.currentThread().isInterrupted()) {
            try (Socket socket = new Socket()) {

                // Attempt a connection to the specified host
                socket.connect(new InetSocketAddress(host, port), 3000);

                // Disable Nagles algorithm, to prevent packets being combined
                socket.setTcpNoDelay(true);
                socket.setSoTimeout(5000);

                LOGGER.info("Video relay connected to " + host + ":" + port);

                // Create a new input buffer with a size of 1 << 16 (64KiB), and bind it to the sockets stream
                BufferedInputStream in = new BufferedInputStream(socket.getInputStream(), 1 << 16);
                streamFrames(in);

            } catch (IOException e) {
                if (connected) {
                    connected = false;

                    LOGGER.error("Error attempting to connect to video stream: ", e);
                    System.out.print("\n");

                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    return;
                }
            }
        }

        LOGGER.info("Video relay terminated");
    }

    private void streamFrames(BufferedInputStream stream) throws IOException {
        // Wrap buffer stream in payload stream so we can use read methods
        DataInputStream dataStream = new DataInputStream(stream);

        // Loop on the relay thread, publishing new frames whenever they arrive, until the thread is interrupted
        while (!Thread.currentThread().isInterrupted()) {
            // Read in the frame length (1st int)
            int len = dataStream.readInt();
            if (len <= 0 || len > 2_000_000) throw new IOException("Bad frame length: " + len);

            // Create a new buffer for the frame with the correct length
            byte[] frame = new byte[len];

            // Copy the frame payload into the new buffer and publish
            dataStream.readFully(frame);
            publishFrame(frame);
            connected = true;
        }

        // Reset interrupted flag so outer while loop exits
        Thread.currentThread().interrupt();
    }

    private void publishFrame(byte[] frame) {
        synchronized (lock) {
            // Construct a new VideoFrame record, incrementing the latest id, and assign it to the latest variable
            latest = new VideoFrame(latest.id() + 1, frame);

            // Notify all threads waiting on a new frame that one has been recived
            lock.notifyAll();
        }
    }

    public VideoFrame getOrAwaitNextFrame(long lastFrameId, long timeout) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeout;
        synchronized (lock) {
            while (latest.data() == null || latest.id() <= lastFrameId) {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0) return null;

                // Block until notified of a new frame via publish() or timeout is exceeded
                lock.wait(remaining);
            }
            return latest;
        }
    }
}