package com.wuala.websocket.callback;

import org.java_websocket.handshake.ServerHandshake;

public interface SocketClientCallback {

    void onClientOpen(ServerHandshake serverHandshake);

    void onClientMessage(String s);

    void onClientClose(int i, String s, boolean b);

    void onClientError(Exception e);
}