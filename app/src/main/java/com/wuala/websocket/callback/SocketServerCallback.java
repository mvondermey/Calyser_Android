package com.wuala.websocket.callback;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

public interface SocketServerCallback {

    void onServerOpen(WebSocket conn, ClientHandshake handshake);

    void onServerClose(WebSocket conn, int code, String reason, boolean remote);

    void onServerMessage(WebSocket conn, String s);

    void onServerError(WebSocket conn, Exception ex);
}