package com.wuala.websocket.callback;

import com.wuala.websocket.model.FileItem;

/**
 * Created by Wang on 10/24/14.
 */
public interface SendFileListener {

    void onFileSend(FileItem item);

    void onCloseContainer();

}
