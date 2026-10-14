package org.susf.cubesat.gcs.core;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;


import org.apache.commons.mail.EmailException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.susf.cubesat.gcs.net.api.SpringServer;
import org.susf.cubesat.gcs.net.email.EmailClient;
import org.susf.cubesat.gcs.net.uplink.protocol.codec.PacketCodec;
import org.susf.cubesat.gcs.net.uplink.protocol.packet.telemetry.CpuTempPacket;
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
    private EmailClient emailClient;

    public GroundControlServer() {
        // Register all packet classes with codec, so it knows how to deserialise them
        codec = new PacketCodec()
                .register(HealthPacket.class)
                .register(CpuTempPacket.class);

        // Fetch env variables from file
        int port = Integer.valueOf(Env.get("UPLINK_PORT", "12000"));

        uplinkServer = new UplinkServer(port, codec);
        uplinkServer.start();

        // Initialise email client
        emailClient = new EmailClient(
                Env.get("MAIL_USERNAME"),
                Env.get("MAIL_PASSWORD"),
                Env.get("MAIL_HOST"));
    }

    public void run() throws IOException, ExecutionException, InterruptedException {

        // Start spring server as java bean
        springServer = new SpringApplicationBuilder(SpringServer.class)
                .initializers((GenericApplicationContext ctx) ->
                        ctx.getBeanFactory().registerSingleton("GCS Frontend", this))
                .run();

        // Wait until a client connects to the uplink server, then get the connection
        connection = uplinkServer.getOrAwaitConnection();

        // Register a test listener to handle recived packets
        connection.onPacketReceived(packet -> {
            LOGGER.info("Recived packet: " + packet);

            if (Objects.requireNonNull(packet.payload()) instanceof CpuTempPacket tempPacket) {
                System.out.println(tempPacket);
            }
        });
    }

    public void stop() {
        if (springServer != null) springServer.close();
        uplinkServer.close();
    }

    public void testTelemetryEmail(HealthPacket packet) {
        try {
            String recipient = System.getenv("MAIL_RECIPIENT");
            String from = System.getenv("MAIL_USERNAME");

            String html = """
                <html>
                    <h4>CubeSat health telemetry</h4>
                    <p>See attatched file</p>
                </html>
            """;

            File report = packet.buildReportAttachment();
            emailClient.sendHtml(
                    html,
                    recipient,
                    "Health report telemetry",
                    from,
                    List.of(report)
            );
        } catch (IOException | EmailException e) {
            LOGGER.error("Failed to create or send health telemetry email: ", e);
        }
    }
}
