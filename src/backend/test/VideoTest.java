import org.susf.cubesat.gcs.util.Env;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;

public class VideoTest {

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(Env.get("VIDEO_PORT"));
        InputStream inputStream = VideoTest.class.getClassLoader().getResourceAsStream("test.jpg");
        byte[] frame = inputStream.readAllBytes();

        try (ServerSocket server = new ServerSocket(port)) {
            while (true) {
                try (Socket client = server.accept()) {
                    System.out.println("Client connected: " + client.getRemoteSocketAddress());
                    client.setTcpNoDelay(true);

                    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(client.getOutputStream(), 1 << 16));

                    while (true) {
                        out.writeInt(frame.length);
                        out.write(frame);
                        out.flush();
                        Thread.sleep(30);
                    }
                } catch (IOException e) {
                    System.out.println("Client disconnected: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}