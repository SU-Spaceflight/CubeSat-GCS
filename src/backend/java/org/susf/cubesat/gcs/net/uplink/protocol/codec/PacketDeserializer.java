package org.susf.cubesat.gcs.net.uplink.protocol.codec;

import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.susf.cubesat.gcs.net.uplink.protocol.Packet;
import org.susf.cubesat.gcs.net.uplink.protocol.PacketEnvelope;

import java.lang.reflect.Type;

/**
 * We use this class to store a {@link Gson} context with a registered type adaptor for the {@link Packet}
 * subclass type passed to the constructor.<br><br>
 * This allows the {@link PacketCodec} to unambiguasly deserialise the {@link Packet} object contained in the
 * {@link PacketEnvelope} into the correct subclass which the envolope otherwise stores as just the abstract
 * {@link Packet} type.<br><br>
 * If we were to try to deserialise an envelope without registering a custom type adapter, Gson would try and fail
 * to create an instance of the Packet class, which due to being abstract, is uninstantiable.<br><br>
 * Instead we create a {@link PacketDeserializer} for each {@link Packet} type registered to the {@link PacketCodec}, explicitly
 * telling Gson which class it should use when deserialising. We then store a map of deserialisers keyed by name,
 * and use the 'type' field in the envelope to select the correct deserialiser (and hence {@link Gson} context)
 * to use when attempting to decode an envelope.
 */
public class PacketDeserializer {

    private static final Logger LOGGER = LogManager.getLogger(PacketDeserializer.class);

    private final Gson gson;

    public PacketDeserializer(Class<? extends Packet> packetType) {
        // Create a new Gson context specifically for this packet type, and register a new deserialiser with it
        gson = new GsonBuilder()
                .registerTypeAdapter(Packet.class, new DataDeserializer(packetType))
                .create();
    }

    public PacketEnvelope deserialize(String json) {
        LOGGER.info("JSON: " + json);
        // Deserialise the json, using this deserialisers specific Gson context
        return gson.fromJson(json, PacketEnvelope.class);
    }

    // Nested class to implement Gson's JsonDeserialiser interface
    // Holds a reference to the packet types class, and uses it to explicitly tell gson which class the json should be deserialised into
    private static class DataDeserializer implements JsonDeserializer<Packet> {
        private final Class<? extends Packet> clazz;

        public DataDeserializer(Class<? extends Packet> clazz) {
            this.clazz = clazz;
        }

        @Override
        // Gson invokes this method when it encounters a json object of class type typeOfT (the same class type we registered this deserialiser for with the gson context)
        public Packet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            // We simply pass in the class type we recived in the constructor, so Gson can now deserialise the packet correctly
            return context.deserialize(json, clazz);
        }
    }
}
