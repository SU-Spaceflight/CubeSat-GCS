package org.susf.cubesat.gcs;

import org.susf.cubesat.gcs.core.GroundControlServer;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        GroundControlServer controlServer = new GroundControlServer();
        controlServer.run();
    }
}
