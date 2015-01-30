package com.wuala.websocket.activity;

import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.wuala.websocket.R;
import com.wuala.websocket.callback.SocketClientCallback;
import com.wuala.websocket.callback.SocketServerCallback;
import com.wuala.websocket.model.ChatMessage;
import com.wuala.websocket.model.User;
import com.wuala.websocket.socket.ClientManager;
import com.wuala.websocket.task.SocketServerTask;
import com.wuala.websocket.util.FileHelper;
import com.wuala.websocket.util.Marco;
import com.wuala.websocket.util.Notice;
import com.wuala.websocket.util.Util;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Wang on 10/11/14.
 */
public class MainApplication extends Application implements Thread.UncaughtExceptionHandler, SocketServerCallback, SocketClientCallback {

    public static final String TAG = "FileShare";
    public static final int SOCKET_PORT = 8002;
    public static final int HTTP_PORT = 8001;
    public static String CONTAINER_PATH;

    public static Notice notice;

    private SocketServerCallback mServerCallBack;
    private SocketClientCallback mClientCallBack;

    private WebSocketServer mWebSocketServer;
    private WebSocketClient mWebSocketClient;

    private Map<WebSocket, User> mUsers;

    @Override

    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(this);
        notice = new Notice(this);
        // init container path
        initContainerPath();
        // start WebSocket server
        // startSocketServer();
        // start http server
        // startHttpServer();
        initFolder();
        setImageLoader();
    }

    /**
     * init photo video and documents folder
     */
    public void initFolder() {
        File photoAlbumFile = new File(MainApplication.CONTAINER_PATH + "/photos/" + getString(R.string.photo_album));
        if (!photoAlbumFile.exists()) {
            photoAlbumFile.mkdirs();
        }
        File videoAlbumFile = new File(MainApplication.CONTAINER_PATH + "/videos/" + getString(R.string.video_album));
        if (!videoAlbumFile.exists()) {
            videoAlbumFile.mkdirs();
        }
        File documentAlbumFile = new File(MainApplication.CONTAINER_PATH + "/documents/" + getString(R.string.my_document));
        if (!documentAlbumFile.exists()) {
            documentAlbumFile.mkdirs();
        }
    }

    /**
     * init ImageLoader
     */
    public void setImageLoader() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
//                .cacheOnDisk(true)
//                .showImageOnLoading(R.drawable.coming_soon)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .diskCacheExtraOptions(300, 400, null)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .memoryCache(new WeakMemoryCache())
                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheExtraOptions(300, 400)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .defaultDisplayImageOptions(options)
                .writeDebugLogs()
                .build();
        ImageLoader.getInstance().init(config);
    }

    /**
     * init container path
     */
    private void initContainerPath() {
        CONTAINER_PATH = FileHelper.instance(this).getSDFilesPath() + "/container";
        File dir = new File(CONTAINER_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
//        new CopyUtil(this).assetsCopy();
    }

    /**
     * start socket server
     */
    public void startSocketServer() {
        SocketServerTask task = new SocketServerTask(SOCKET_PORT, mWebSocketServer, this);
        task.execute();
    }

    /**
     * start http server
     */
    public void startHttpServer(Intent intent) {
        startService(intent);
    }

    /**
     * start socket client
     *
     * @param host
     */
    public void startSocketClient(String host) {
        ClientManager manager = new ClientManager();
        mWebSocketClient = manager.startClient(host, SOCKET_PORT, this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        String fileName = "crash.txt";
        writeToFile(fileName, ex);
        Log.e(MainApplication.TAG, ex.toString(), ex);
        System.exit(0);
    }

    /**
     * write exception to sd card
     *
     * @param filename
     * @param ex
     */
    private void writeToFile(String filename, Throwable ex) {

        ByteArrayOutputStream baos = null;
        PrintStream printStream = null;

        BufferedWriter bw = null;
        String errorStr;
        try {
            // get error message
            baos = new ByteArrayOutputStream();
            printStream = new PrintStream(baos);
            ex.printStackTrace(printStream);
            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printStream);
                cause = cause.getCause();
            }
            byte[] errorData = baos.toByteArray();
            errorStr = new String(errorData);

            // write file
            File dir = new File(FileHelper.instance(this).getSDFilesPath() + "/crash");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File errorFile = new File(dir, filename);
            if (!errorFile.exists()) {
                errorFile.createNewFile();
            }

            bw = new BufferedWriter(new FileWriter(errorFile, true));
            bw.append("***********************************************************\n");
            bw.append("             " + Util.getTime()
                    + "                    \n");
            bw.append("***********************************************************\n");
            bw.append(errorStr);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (printStream != null) {
                try {
                    printStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (baos != null) {
                try {
                    baos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (bw != null) {
                try {
                    bw.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void closeServer(Intent intent) {
        try {
            if (mWebSocketServer != null)
                mWebSocketServer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stopService(intent);
    }

    public void closeClient() {
        if (mWebSocketClient != null)
            mWebSocketClient.close();
    }

    public WebSocketClient getClient() {
        return mWebSocketClient;
    }

    public WebSocketServer getServer() {
        return mWebSocketServer;
    }

    public Map<WebSocket, User> getUsers() {
        return mUsers;
    }

    public String getUserName() {
        SharedPreferences sharedPreferences = getSharedPreferences(MainApplication.TAG, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(Marco.SP_USERNAME, "");
        return username;
    }

    /**
     * if server receive a request from client
     * the dialog will be shown
     *
     * @param username, yesListener, noListener
     */
    public void showConfirmDialog(Dialog dialog, String username, View.OnClickListener yesListener, View.OnClickListener noListener) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View convertView = inflater.inflate(R.layout.dialog_confirm, null);
        dialog.setContentView(convertView);
        TextView txtView = (TextView) convertView.findViewById(R.id.txt_content);
        String message = String.format(getString(R.string.request_message), username);
        txtView.setText(message);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.txt_title);
        txtTitle.setText("Connect Request");
        Button btnYes = (Button) convertView.findViewById(R.id.btn_yes);
        btnYes.setText(getString(R.string.allow));
        btnYes.setOnClickListener(yesListener);
        Button btnCancel = (Button) convertView.findViewById(R.id.btn_cancel);
        btnCancel.setText(getString(R.string.deny));
        btnCancel.setOnClickListener(noListener);
        dialog.show();
    }

    /**
     * create confirm message
     * if agree the chat view will be opened
     *
     * @param agree
     * @return json
     */
    public String createConfirmMessage(boolean agree) {
        ChatMessage message = new ChatMessage();
        message.setName(getUserName());
        message.setType(Marco.MSG_RESPONSE_CONNECT);
        if (agree) {
            message.setMessage(Marco.COMMAND_AGREE);
        } else {
            message.setMessage(Marco.COMMAND_DISAGREE);
        }
        message.setDate(Util.getTime());
        Gson gson = new Gson();
        String json = gson.toJson(message);
        return json;
    }

    /**
     * create request message
     * if agree the chat view will be opened
     *
     * @return json
     */
    public String createRequestMessage() {
        ChatMessage message = new ChatMessage();
        message.setName(getUserName());
        message.setType(Marco.MSG_REQUEST_CONNECT);
        message.setMessage(String.format(getString(R.string.request_message), getUserName()));
        message.setDate(Util.getTime());
        Gson gson = new Gson();
        String json = gson.toJson(message);
        return json;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public void setServerCallBack(SocketServerCallback callback) {
        if (callback != null)
            this.mServerCallBack = callback;
    }

    public void setClientCallBack(SocketClientCallback callback) {
        if (callback != null)
            this.mClientCallBack = callback;
    }

    @Override
    public void onServerOpen(WebSocket conn, ClientHandshake handshake) {
        Log.e(MainApplication.TAG, "onServerOpen");
        if (mUsers == null) {
            mUsers = new HashMap<WebSocket, User>();
        }
        mServerCallBack.onServerOpen(conn, handshake);
    }

    @Override
    public void onServerClose(WebSocket conn, int code, String reason, boolean remote) {
        Log.e(MainApplication.TAG, "onServerClose");
        mServerCallBack.onServerClose(conn, code, reason, remote);
    }

    @Override
    public void onServerMessage(WebSocket conn, String s) {
        Log.e(MainApplication.TAG, "onServerMessage");
        mServerCallBack.onServerMessage(conn, s);
    }

    @Override
    public void onServerError(WebSocket conn, Exception ex) {
        Log.e(MainApplication.TAG, ex.toString(), ex);
        mServerCallBack.onServerError(conn, ex);
    }

    @Override
    public void onClientOpen(ServerHandshake serverHandshake) {
        Log.e(MainApplication.TAG, "onClientOpen");
        mClientCallBack.onClientOpen(serverHandshake);
    }

    @Override
    public void onClientMessage(String s) {
        Log.e(MainApplication.TAG, "onClientMessage");
        mClientCallBack.onClientMessage(s);
    }

    @Override
    public void onClientClose(int i, String s, boolean b) {
        Log.e(MainApplication.TAG, "onClientClose");
        mClientCallBack.onClientClose(i, s, b);
    }

    @Override
    public void onClientError(Exception e) {
        Log.e(MainApplication.TAG, e.toString(), e);
        mClientCallBack.onClientError(e);
    }
}