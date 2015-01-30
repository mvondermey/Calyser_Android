package com.wuala.websocket.socket;

import com.wuala.websocket.callback.SocketClientCallback;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Wang on 10/22/14.
 */
public class ClientManager {

    private WebSocketClient mWebSocketClient;

    public WebSocketClient startClient(String ip,int port, final SocketClientCallback callback) {
        URI uri;
        try {
            uri = new URI("ws://" + ip + ":" + port);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }

        mWebSocketClient = new WebSocketClient(uri, new Draft_17()) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                callback.onClientOpen(serverHandshake);
            }

            @Override
            public void onMessage(String s) {
                callback.onClientMessage(s);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                callback.onClientClose(i, s, b);
            }

            @Override
            public void onError(Exception e) {
                callback.onClientError(e);
            }
        };
        mWebSocketClient.connect();
        return mWebSocketClient;
    }

}
