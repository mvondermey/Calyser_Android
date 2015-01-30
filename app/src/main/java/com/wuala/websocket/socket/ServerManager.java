package com.wuala.websocket.socket;

import com.wuala.websocket.callback.SocketServerCallback;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

/**
 * Created by Wang on 10/22/14.
 */
public class ServerManager {

    private WebSocketServer mWebSocketServer;

    public WebSocketServer startServer(int port, final SocketServerCallback callback) {
        mWebSocketServer = new WebSocketServer(new InetSocketAddress(port)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                callback.onServerOpen(conn, handshake);
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                callback.onServerClose(conn, code, reason, remote);
            }

            @Override
            public void onMessage(WebSocket conn, String s) {
                callback.onServerMessage(conn, s);
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                callback.onServerError(conn, ex);
            }
        };
        mWebSocketServer.start();
        return mWebSocketServer;
    }

}
