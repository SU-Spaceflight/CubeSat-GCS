package org.susf.cubesat.gcs.net.uplink.callback;

import org.susf.cubesat.gcs.net.uplink.protocol.PacketEnvelope;
import org.susf.cubesat.gcs.net.uplink.server.UplinkConnection;

@FunctionalInterface
public interface PacketListener {

    /** This is the callback method that is invoked by an {@link UplinkConnection} whenever it receives a {@link PacketEnvelope}. </n>
     * To register a listener, use the {@link UplinkConnection#onPacketReceived(PacketListener)} method
     **/
    void onPacketReceived(PacketEnvelope envelop);
}
