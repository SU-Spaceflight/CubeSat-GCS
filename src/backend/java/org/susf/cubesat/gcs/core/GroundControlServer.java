package org.susf.cubesat.gcs.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.susf.cubesat.gcs.net.api.SpringServer;
import org.susf.cubesat.gcs.net.uplink.protocol.codec.PacketCodec;
import org.susf.cubesat.gcs.net.uplink.protocol.packet.telemetry.HealthPacket;
import org.susf.cubesat.gcs.net.uplink.server.UplinkConnection;
import org.susf.cubesat.gcs.net.uplink.server.UplinkServer;
import org.susf.cubesat.gcs.util.Env;

public class GroundControlServer {

    private static final Logger LOGGER = LogManager.getLogger(GroundControlServer.class);

    private final PacketCodec codec;
    private final UplinkServer uplinkServer;
    private UplinkConnection connection;
    private ConfigurableApplicationContext springServer;

    public GroundControlServer() {
        // Register all packet classes with codec, so it knows how to deserialise them
        codec = new PacketCodec()
                .register(HealthPacket.class);

        // Fetch env variables from file
        String host = Env.get("UPLINK_HOST", "localhost");
        int port = Integer.valueOf(Env.get("UPLINK_PORT", "12000"));

        try {
            // Try to resolve the address, and initialise uplink server
            InetAddress address = InetAddress.getByName(host);
            uplinkServer = new UplinkServer(address, port, codec);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {

        // Start spring server as java bean
        springServer = new SpringApplicationBuilder(SpringServer.class)
                .initializers((GenericApplicationContext ctx) ->
                        ctx.getBeanFactory().registerSingleton("GCS Frontend", this))
                .run();

        // Wait until a client connects to the uplink server, then get the connection
        connection = uplinkServer.getOrAwaitConnection();

        // Register a test listener to handle recived packets
        connection.onPacketReceived(packet -> {
            if (Objects.requireNonNull(packet.data()) instanceof HealthPacket health) {
                LOGGER.info("Recived: " + health);
            }
        });
    }

    public void stop() {
        if (springServer != null) springServer.close();
        uplinkServer.close();
    }
}
