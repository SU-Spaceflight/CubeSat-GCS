import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.susf.cubesat.gcs.net.uplink.protocol.codec.PacketCodec;

import java.net.URI;

// This is a test for the GCS module, simulating the satelite sending a health packet
public class ClientTest extends WebSocketClient {
    static PacketCodec codec = new PacketCodec();

    public ClientTest(String url) throws Exception {
        super(new URI(url));
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Connected to server");
        send("Test 123");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Recived: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    public static void main(String[] args) throws Exception {
        ClientTest client = new ClientTest("ws://localhost:12000");

        client.connect();
    }
}
