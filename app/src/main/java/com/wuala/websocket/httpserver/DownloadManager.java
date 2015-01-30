package com.wuala.websocket.httpserver;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wuala.websocket.R;
import com.wuala.websocket.activity.MainApplication;
import com.wuala.websocket.model.FileItem;
import com.wuala.websocket.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 * Created by Wang on 10/26/14.
 */
public class DownloadManager {
    // download：normal, pause, downloading, downloaded, waiting
    public static final int DOWNLOAD_STATE_NORMAL = 0x00;
    public static final int DOWNLOAD_STATE_PAUSE = 0x01;
    public static final int DOWNLOAD_STATE_DOWNLOADING = 0x02;
    public static final int DOWNLOAD_STATE_FINISH = 0x03;
    public static final int DOWNLOAD_STATE_WAITING = 0x04;

    private static int DOWNLOAD_CACHE = 2 * 1024;

    private Map<String, FileItem> downloadFiles = new HashMap<String, FileItem>();
    private ArrayList<DownloadTask> taskList = new ArrayList<DownloadTask>();
    private Handler mHandler;
    private final static Object syncObj = new Object();
    private static DownloadManager instance;
    private ExecutorService executorService;
    private static Context mContext;

    private DownloadManager() {
        // up to 3 tasks
        executorService = Executors.newFixedThreadPool(3);
    }

    public static DownloadManager getInstance(Context context) {
        mContext = context;
        if (!Util.isReadOnly) {
            return new DownloadManager();
        }
        if (null == instance) {
            synchronized (syncObj) {
                instance = new DownloadManager();
            }
            return instance;
        }
        return instance;
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    // start download
    public void startDownload(FileItem file) {
        downloadFiles.put(file.getID(), file);
        DownloadTask task = new DownloadTask(file.getID());
        taskList.add(task);
        executorService.submit(task);
    }

    public void stopAllDownloadTask() {
        while (taskList.size() != 0) {
            for (DownloadTask d : taskList) {
                d.stopTask();
                (new File(d.getLoaction())).delete();
            }
            taskList.clear();
        }
        // will stop current task or refuse new task
//        executorService.shutdownNow();
    }

    protected boolean checkFileType(String fileName, String[] extendName) {
        for (String aEnd : extendName) {
            if (fileName.toLowerCase().endsWith(aEnd)) {
                return true;
            }
        }
        return false;
    }

    // download task
    class DownloadTask implements Runnable {

        private boolean isWorking = false;
        private String downloadId;
        private String location;

        public DownloadTask(String id) {
            this.isWorking = true;
            this.downloadId = id;
        }

        public void stopTask() {
            this.isWorking = false;
        }

        public String getLoaction() {
            return location;
        }

        // update item in ListView
        private void update(FileItem downloadFile) {
            Message msg = mHandler.obtainMessage();
            msg.obj = downloadFile;
            msg.sendToTarget();
        }

        public void run() {
            // update status
            FileItem downloadFile = downloadFiles.get(downloadId);
            downloadFile.setDownloadState(DOWNLOAD_STATE_DOWNLOADING);

            // download file
            String urlStr = downloadFile.getUrl();
            String newFilename = urlStr.substring(urlStr.lastIndexOf("/") + 1);
            if (Util.isReadOnly) {
                if (checkFileType(newFilename,
                        mContext.getResources().getStringArray(R.array.fileEndingVideo))) {
                    newFilename = MainApplication.CONTAINER_PATH + "/videos/Video Album/" + newFilename.replace("%20", " ");
                } else if (checkFileType(newFilename,
                        mContext.getResources().getStringArray(R.array.fileEndingImage))) {
                    newFilename = MainApplication.CONTAINER_PATH + "/photos/Photo Album/" + newFilename.replace("%20", " ");
                } else
                    newFilename = MainApplication.CONTAINER_PATH + "/documents/My Document/" + newFilename.replace("%20", " ");
            } else {
                newFilename = Util.currentFile.getAbsolutePath() + File.separator + urlStr.substring(urlStr.lastIndexOf("/") + 1).replace("%20", " ");
            }
            File file = new File(newFilename);
            if (file.exists()) {
                file.delete();
            }
            location = newFilename;
            // https
            X509PinningTrustManager trustManager = new X509PinningTrustManager("");
            SSLContext sc;
            HttpsURLConnection urlConnection = null;
            try {
                URL url = new URL(urlStr);
                urlConnection = (HttpsURLConnection) url.openConnection();

                urlConnection.setHostnameVerifier(trustManager.new HostnameVerifier());
                sc = SSLContext.getInstance("TLS");
                sc.init(null, new TrustManager[]{trustManager}, new SecureRandom());
                urlConnection.setSSLSocketFactory(sc.getSocketFactory());
                // Set our SSL settings settings
                if (!Util.isReadOnly) {
                    downloadFile.setTotalSize(urlConnection.getContentLength());
                } else {
                    downloadFile.setTotalSize(Long.parseLong(urlConnection.getHeaderField("Content-Length")));
                }
                InputStream is = urlConnection.getInputStream();
                byte[] bs = new byte[DOWNLOAD_CACHE];
                // read length
                int len;
                OutputStream os = new FileOutputStream(newFilename);
                // start read
                while (isWorking && (len = is.read(bs)) != -1) {
                    os.write(bs, 0, len);
                    long downloadSize = downloadFile.getDownloadSize();
                    downloadFile.setDownloadSize(downloadSize + DOWNLOAD_CACHE);
                    update(downloadFile);
                }

                downloadFile.setDownloadState(DOWNLOAD_STATE_FINISH);
                update(downloadFile);
                downloadFiles.remove(downloadFile.getID());
                taskList.remove(this);
                // close stream
                os.close();
                is.close();
            } catch (Exception e) {
                Log.e(MainApplication.TAG, e.toString(), e);
                if (urlConnection != null)
                    urlConnection.disconnect();
                downloadFile.setDownloadState(DOWNLOAD_STATE_PAUSE);
                update(downloadFile);
                downloadFiles.remove(downloadId);
            }

            // http
//            try {
//                // create URL
//                URL url = new URL(urlStr);
//                // open link
//                URLConnection con = url.openConnection();
//                // get file size
//                int contentLength = con.getContentLength();
//                downloadFile.setTotalSize(contentLength);
//
//                InputStream is = con.getInputStream();
//                // 2k cache
//                byte[] bs = new byte[DOWNLOAD_CACHE];
//                // read length
//                int len;
//                OutputStream os = new FileOutputStream(newFilename);
//                // start read
//                while ((len = is.read(bs)) != -1) {
//                    os.write(bs, 0, len);
//                    long downloadSize = downloadFile.getDownloadSize();
//                    downloadFile.setDownloadSize(downloadSize + DOWNLOAD_CACHE);
//                    update(downloadFile);
//                }
//                downloadFile.setDownloadState(DOWNLOAD_STATE_FINISH);
//                update(downloadFile);
//                downloadFiles.remove(downloadFile.getID());
//                taskList.remove(this);
//                // close stream
//                os.close();
//                is.close();
//
//            } catch (Exception e) {
//                Log.e(MainApplication.TAG, e.toString(), e);
//                downloadFile.setDownloadState(DOWNLOAD_STATE_PAUSE);
//                update(downloadFile);
//                downloadFiles.remove(downloadId);
//            }
        }
    }
}
