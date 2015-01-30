package com.wuala.websocket.model;

import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.wuala.websocket.util.Util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by Wang on 10/24/14.
 */
public class FileItem {

    @Expose
    @SerializedName("fileName")
    private String fileName;
    @Expose
    @SerializedName("url")
    private String url;
    @Expose
    @SerializedName("size")
    private String size;
    @Expose
    @SerializedName("prefix")
    private String prefix;

    private int type;
    private boolean isFile;
    private long totalSize;

    private String ID;
    private long downloadSize;
    private int downloadState;
    private int index;

    public static int FILE_TYPE_BACK = 0;
    public static int FILE_TYPE_FOLDER = 1;


    public FileItem() {
        setID(System.currentTimeMillis() + "");
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String myurl) {
        this.url = myurl;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean isFile) {
        this.isFile = isFile;
    }


    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }


    public int getDownloadState() {
        return downloadState;
    }

    public void setDownloadState(int downloadState) {
        this.downloadState = downloadState;
    }


    public long getDownloadSize() {
        return downloadSize;
    }

    public void setDownloadSize(long downloadSize) {
        this.downloadSize = downloadSize;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
