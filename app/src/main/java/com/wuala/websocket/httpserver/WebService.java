package com.wuala.websocket.httpserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import com.wuala.websocket.activity.MainApplication;

public class WebService extends Service {

    private WebServer webServer;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        webServer = new WebServer(getApplicationContext(), MainApplication.HTTP_PORT, MainApplication.CONTAINER_PATH);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action) && "com.start.webService".equals(action)) {
                try {
                    webServer.setDaemon(true);
                    webServer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        webServer.close();
        super.onDestroy();
    }

}
