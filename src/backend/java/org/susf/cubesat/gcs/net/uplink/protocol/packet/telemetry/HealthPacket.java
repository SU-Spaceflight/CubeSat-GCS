package org.susf.cubesat.gcs.net.uplink.protocol.packet.telemetry;

import org.susf.cubesat.gcs.net.uplink.protocol.Packet;

public class HealthPacket extends Packet {
    private int batteryLevel;
    private float temperature;

    public HealthPacket() {}

    public HealthPacket(int batteryLevel, float temperature) {
        this.batteryLevel = batteryLevel;
        this.temperature = temperature;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public float getTemperature() {
        return temperature;
    }

    @Override
    public String toString() {
        return "HealthPacket {" +
                "batteryLevel= " + batteryLevel +
                ", temperature= " + temperature +
                "}";
    }
}
