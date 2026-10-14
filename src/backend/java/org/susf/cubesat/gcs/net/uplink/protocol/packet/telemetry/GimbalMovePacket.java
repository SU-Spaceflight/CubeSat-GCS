package org.susf.cubesat.gcs.net.uplink.protocol.packet.telemetry;

import org.susf.cubesat.gcs.net.uplink.protocol.Packet;

public class GimbalMovePacket extends Packet {

    private float dx;
    private float dy;

    public GimbalMovePacket(float dx, float dy) {
        this.dx = dx;
        this.dy = dy;
    }
}
