package org.susf.cubesat.gcs.net.uplink.protocol.packet.telemetry;

import com.google.gson.annotations.SerializedName;
import org.susf.cubesat.gcs.net.uplink.protocol.Packet;

public class CpuTempPacket extends Packet {
    private float cpuTempC;

    public CpuTempPacket(float cpuTempC) {
        this.cpuTempC = cpuTempC;
    }

    public float getCpuTempC() {
        return cpuTempC;
    }

    @Override
    public String toString() {
        return "CpuTempPacket{" +
                "cpu_temp_c=" + cpuTempC +
                '}';
    }
}
