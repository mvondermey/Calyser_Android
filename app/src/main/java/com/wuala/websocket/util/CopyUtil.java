package com.wuala.websocket.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.wuala.websocket.activity.MainApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CopyUtil {

    private AssetManager manager;

    public CopyUtil(Context context) {
        manager = context.getAssets();
    }

    public boolean assetsCopy() {
        try {
            assetsCopy("android.png", MainApplication.CONTAINER_PATH + "/android.png");
        } catch (IOException e) {
            Log.e(MainApplication.TAG, e.toString(), e);
            return false;
        }
        return true;
    }

    public void assetsCopy(String assetsPath, String dirPath) throws IOException {
        String[] list = manager.list(assetsPath);
        // files
        if (list.length == 0) {
            InputStream in = manager.open(assetsPath);
            File file = new File(dirPath);
            file.getParentFile().mkdirs();
            file.createNewFile();
            FileOutputStream out = new FileOutputStream(file);
            /* copy */
            byte[] buf = new byte[1024];
            int count;
            while ((count = in.read(buf)) != -1) {
                out.write(buf, 0, count);
                out.flush();
            }
            in.close();
            out.close();
        } else {
            // folder
            for (String path : list) {
                assetsCopy(assetsPath + "/" + path, dirPath + "/" + path);
            }
        }
    }
}
