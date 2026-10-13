package org.susf.cubesat.gcs.net.uplink.protocol.codec;

import com.google.gson.*;
import org.susf.cubesat.gcs.net.uplink.protocol.Packet;
import org.susf.cubesat.gcs.net.uplink.protocol.PacketEnvelope;

import java.util.HashMap;
import java.util.Map;

public class PacketCodec {
    private final Gson encoderGson;

    // Hold a map of deserialiser instances, keyed by class name, so we can look up which deserialiser to use for each packet given to the codec
    private final Map<String, PacketDeserializer> deserializers = new HashMap<>();

    public PacketCodec() {
        encoderGson = new Gson();
    }

    public PacketCodec register(Class<? extends Packet> packetType) {
        String type = packetType.getSimpleName();

        if (deserializers.containsKey(type)) {
            throw new IllegalArgumentException("PacketEnvelope type is already registered");
        }

        deserializers.put(type, new PacketDeserializer(packetType));
        return this;
    }

    public String encode(Packet data) {
        if (data == null) {
            throw new IllegalArgumentException("Data must not be null");
        }

        // Wrap the packet in a new envelope to carry its metadata
        PacketEnvelope envelop = new PacketEnvelope(data);

        // Use a standard Gson context to serialise the envelope and packet data. Gson knows what the class type of the packet is
        return encoderGson.toJson(envelop);
    }

    public PacketEnvelope decode(String json) {
        JsonElement parsed = JsonParser.parseString(json);

        if (!parsed.isJsonObject()) {
            throw new IllegalArgumentException("Envelope must be a JSON object");
        }

        JsonObject envelope = parsed.getAsJsonObject();

        // We get the 'type' field from the envelope to look up which class the serialised json represents
        // and which deserialiser we need to use to reconstruct it. This lets us explicitly tell Gson which
        // class it should use when attempting to deserialise the Packet object contained within the envelope
        // See PacketDeserializer for more details
        String type = envelope.get("type").getAsString();

        // Use the type field, and look up which deserialiser instance we should use
        PacketDeserializer deserializer = deserializers.get(type);

        if (deserializer == null) {
            throw new IllegalArgumentException("Unregistered packet type: " + type);
        }

        // Use the deserialiser to reconstruct the envelope and packet data objects
        return deserializer.deserialize(json);
    }
}
