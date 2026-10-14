// Source - https://stackoverflow.com/a/55464326
// Posted by Dominik, modified by community. See post 'Timeline' for change history
// Retrieved 2026-10-14, License - CC BY-SA 4.0

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
        
public class Main {
        
    public static void main(String[] args) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        WebSocket ws = HttpClient
                .newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(URI.create(""), new WebSocketClient(latch))
                .join();
        ws.sendText("Hello!", true);
        latch.await();
    }
            
    private static class WebSocketClient implements WebSocket.Listener {
        private final CountDownLatch latch;
                
        public WebSocketClient(CountDownLatch latch) { this.latch = latch; }
        
        @Override
        public void onOpen(WebSocket webSocket) {
            System.out.println("onOpen using subprotocol " + webSocket.getSubprotocol());
            WebSocket.Listener.super.onOpen(webSocket);
        }
        
        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            System.out.println("onText received " + data);
            latch.countDown();
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }
        
        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            System.out.println("Bad day! " + webSocket.toString());
            WebSocket.Listener.super.onError(webSocket, error);
        }
    }
}
