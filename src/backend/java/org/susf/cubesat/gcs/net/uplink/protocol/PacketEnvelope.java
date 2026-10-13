package org.susf.cubesat.gcs.net.uplink.protocol;

/**
 * An envelope wraps a {@link Packet} (data), along with additional metadata, including an id and type,
 * to assist when routing or attempting to deserialise the packet data
 */
public record PacketEnvelope(int id, long timestamp, String type, Packet data) {

    private static int nextId = 0;

    public PacketEnvelope(Packet data) {
        this(nextId++, System.currentTimeMillis(), data.getClass().getSimpleName(), data);
    }
}
