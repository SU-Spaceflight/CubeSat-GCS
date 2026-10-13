import org.susf.cubesat.gcs.net.uplink.protocol.codec.PacketCodec;
import org.susf.cubesat.gcs.net.uplink.protocol.packet.telemetry.HealthPacket;
import org.susf.cubesat.gcs.util.Env;

static PacketCodec codec = new PacketCodec();

// This is a test for the GCS module, simulating the satelite sending a health packet
void main() throws IOException {
    String host = Env.get("UPLINK_HOST", "localhost");
    int port = Integer.valueOf(Env.get("UPLINK_PORT", "12000"));

    Socket socket = new Socket(InetAddress.getByName(host), port);
    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

    HealthPacket healthPacket = new HealthPacket();
    outputStream.writeUTF(codec.encode(healthPacket));
    outputStream.flush();

    socket.close();
}