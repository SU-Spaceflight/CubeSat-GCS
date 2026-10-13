package org.susf.cubesat.gcs;

import org.susf.cubesat.gcs.core.GroundControlServer;

public class Main {
    public static void main(String[] args) {
        GroundControlServer controlServer = new GroundControlServer();
        controlServer.run();
    }
}
