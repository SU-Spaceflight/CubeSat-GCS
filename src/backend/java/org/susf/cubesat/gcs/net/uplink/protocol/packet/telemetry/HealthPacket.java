package org.susf.cubesat.gcs.net.uplink.protocol.packet.telemetry;

import org.susf.cubesat.gcs.net.uplink.protocol.Packet;
import org.susf.cubesat.gcs.util.Env;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

    public File buildReportAttachment() throws IOException {
        File file = new File(Env.get("TELEMETRY_REPORT_ATTACHMENT_NAME", "telemetry-report.txt"));
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(this.toString() + "\n");
        }
        return file;
    }
}
