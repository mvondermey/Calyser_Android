package com.wuala.websocket.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Wang on 10/11/14.
 */
public class ChatMessage {

    public static int MESSAGE_TYPE_ME = 1;
    public static int MESSAGE_TYPE_OTHER = 2;
    public static int MESSAGE_TYPE_SYSTEM = 3;

    @Expose
    @SerializedName("type")
    private String type;
    @Expose
    @SerializedName("message")
    private String message;
    @Expose
    @SerializedName("date")
    private String date;
    @Expose
    @SerializedName("senderName")
    private String senderName;
    @Expose
    @SerializedName("fileItem")
    private FileItem fileItem;

    private int from;

    private int index;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return senderName;
    }

    public void setName(String name) {
        this.senderName = name;
    }


    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }


    public FileItem getFileItem() {
        return fileItem;
    }

    public void setFileItem(FileItem fileItem) {
        this.fileItem = fileItem;
    }


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
