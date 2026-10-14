package org.susf.cubesat.gcs.net.uplink.protocol;

/**
 * An envelope wraps a {@link Packet} (payload), along with additional metadata, including an id and type,
 * to assist when routing or attempting to deserialise the packet payload
 */
public record PacketEnvelope(long uptime_ms, String type, Packet payload) {

    public PacketEnvelope(Packet data) {
        this(System.currentTimeMillis(), data.getClass().getSimpleName(), data);
    }
}
