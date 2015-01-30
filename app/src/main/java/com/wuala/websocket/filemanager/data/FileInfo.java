package com.wuala.websocket.filemanager.data;

import android.graphics.drawable.Drawable;

public class FileInfo {
    // file name
    private String fileName = "";
    // last modified time
    private long fileLastUpdateTime;
    // file size
    private String fileSize = "";
    // file icon
    private Drawable fileIcon = null;
    // enable select
    private boolean Selectable = true;

    public FileInfo() {
    }

    public FileInfo(String fileName, long fileLastUpdateTime,
                    String fileSize, Drawable fileIcon) {
        super();
        this.fileName = fileName;
        this.fileLastUpdateTime = fileLastUpdateTime;
        this.fileIcon = fileIcon;
        this.fileSize = fileSize;

    }

    public String getFileName() {
        return fileName;

    }

    public String setFileName() {
        return fileName;

    }

    public long getFileLastUpdateTime() {
        return fileLastUpdateTime;
    }

    public long setFileLastUpdateTime() {
        return fileLastUpdateTime;
    }

    public String getFileSize() {
        return fileSize;
    }

    public String setFileSize() {
        return fileSize;
    }

    public Drawable getfileIcon() {
        return fileIcon;
    }

    public void setfileIcon(Drawable fileIcon) {
        this.fileIcon = fileIcon;
    }

    public boolean isSelected() {
        return Selectable;
    }

    public void setSelectable(boolean selectable) {
        Selectable = selectable;
    }

}
