package org.susf.cubesat.gcs.net.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
public class SpringServer {

    private static final Logger LOGGER = LogManager.getLogger(SpringServer.class);

    @EventListener
    public void onReady(ApplicationReadyEvent event) throws UnknownHostException {
        Environment env = event.getApplicationContext().getEnvironment();

        String port = env.getProperty("local.server.port");
        String host = env.getProperty("server.address", "localhost");

        System.out.print("\n");

        LOGGER.info("===============================================");
        LOGGER.info(" Starting spring server on:");
        LOGGER.info(" - Local: http://" + host + ":" + port);
        LOGGER.info(" - Network: http://" + InetAddress.getLocalHost().getHostAddress() + ":" + port);
        LOGGER.info("===============================================\n");

    }
}
