package com.wuala.websocket.util;

/**
 * Created by Wang on 10/17/14.
 */
public final class Marco {
    /* Release date*/
    public static final String RELEASE_DATE = "Release Date: 2014 - 12- 31";

    /* SharedPreferences Start*/
    public static final String SP_IS_FIRST_RUN = "is_first_run";
    public static final String SP_USERNAME = "username";
    public static final String SP_IP = "ip_address";
    /* SharedPreferences End */

    /* MessageType Start*/
    public static final String MSG_SYSTEM_MESSAGE = "SYSTEM_MESSAGE";
    public static final String MSG_TEXT_MESSAGE = "TEXT_MESSAGE";
    public static final String MSG_FILE_MESSAGE = "FILE_MESSAGE";
    public static final String MSG_REQUEST_CONNECT = "REQUEST_CONNECT";
    public static final String MSG_RESPONSE_CONNECT = "RESPONSE_CONNECT";
    public static final String MSG_REFRESH_TITLE = "REFRESH_TITLE";
    public static final String MSG_IMAGE_MESSAGE = "IMAGE_MESSAGE";
    public static final String MSG_DOCUMENT_MESSAGE = "DOCUMENT_MESSAGE";
    /* MessageType End*/

    /* Command Start*/
    public static final String COMMAND_AGREE = "AGREE";
    public static final String COMMAND_DISAGREE = "DISAGREE";
    /* Command End*/

    /* File Opertion Start*/
    public static final int FILE_RENAME = 1;
    public static final int FILE_DELETE = 2;
    public static final int FILE_CREATE = 3;
    public static final int FILE_OPEN_OPTION = 4;
    public static final int FILE_SORT_BY_NAME = 5;
    public static final int FILE_SORT_BY_DATE = 6;
    public static final int FILE_SEARCH = 7;
    /* ile Opertion End*/
}
