package com.wuala.websocket.task;

import android.os.AsyncTask;

import com.wuala.websocket.callback.SocketServerCallback;
import com.wuala.websocket.socket.ServerManager;

import org.java_websocket.server.WebSocketServer;

public class SocketServerTask extends AsyncTask<String, Void, WebSocketServer> {

    WebSocketServer mWebSocketServer;
    private int mPort;

    private SocketServerCallback mCallback;

    public SocketServerTask(int port, WebSocketServer webSocketServer, SocketServerCallback callback) {
        this.mPort = port;
        this.mCallback = callback;
        this.mWebSocketServer = webSocketServer;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected WebSocketServer doInBackground(String... arg) {
        ServerManager manager = new ServerManager();
        return manager.startServer(mPort, mCallback);
    }

    @Override
    protected void onPostExecute(WebSocketServer result) {
        if (result == null) {
            mCallback.onServerError(null, null);
        }
    }
}