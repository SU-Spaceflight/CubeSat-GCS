package org.susf.cubesat.gcs.net.api.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.susf.cubesat.gcs.net.api.video.VideoFrame;
import org.susf.cubesat.gcs.net.api.video.VideoRelay;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    private static final Logger LOGGER = LogManager.getLogger(VideoController.class);
    private final VideoRelay relay;

    public VideoController(VideoRelay relay) {
        this.relay = relay;
    }

    @GetMapping("/stream")
    public ResponseEntity<StreamingResponseBody> stream(HttpServletRequest request) {
        LOGGER.info("Video client connected: " + request.getRemoteAddr());

        StreamingResponseBody body = out -> {
            try {
                long lastId = -1;

                while (true) {
                    // Wait for next frame to arrive
                    VideoFrame frame = relay.getOrAwaitNextFrame(lastId, 5000);
                    if (frame == null) {
                        throw new IOException("Timed out waiting for video frame");
                    }

                    byte[] data = frame.data();
                    if (data == null) {
                        return;
                    }

                    // Keep track of last frames id
                    lastId = frame.id();

                    // Construct response
                    String header = "--frame\r\n"
                            + "Content-Type: image/jpeg\r\n"
                            + "Content-Length: " + data.length + "\r\n\r\n";
                    out.write(header.getBytes(StandardCharsets.US_ASCII));
                    out.write(data);
                    out.write("\r\n".getBytes(StandardCharsets.US_ASCII));
                    out.flush();
                }
            } catch (IOException e) {
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            LOGGER.info("Video client disconnected: " + request.getRemoteAddr());
        };

        // Send response
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("multipart/x-mixed-replace;boundary=frame"))
                .cacheControl(CacheControl.noStore())
                .body(body);
    }
}