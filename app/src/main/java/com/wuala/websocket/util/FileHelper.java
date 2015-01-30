package com.wuala.websocket.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class FileHelper {
    private Context context;
    /**
     * SD is exist *
     */
    private boolean hasSD = false;
    /**
     * SD path *
     */
    private String SD_FILES_PATH;
    /**
     * file path *
     */
    private String FILES_PATH;
    public static FileHelper helper;

    public static FileHelper instance(Context context) {
        if (helper == null) {
            helper = new FileHelper(context);
        }
        return helper;
    }

    // Environment.getDataDirectory() = /data
    // Environment.getDownloadCacheDirectory() = /cache
    // Environment.getExternalStorageDirectory() = /mnt/sdcard
    // Environment.getRootDirectory() = /system
    // context.getCacheDir() = /data/data/com.mt.mtpp/cache
    // context.getExternalCacheDir() =
    // /mnt/sdcard/Android/data/com.mt.mtpp/cache
    // context.getFilesDir() = /data/data/com.mt.mtpp/files
    public FileHelper(Context context) {
        this.context = context;
        hasSD = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        if (hasSD) {
            try {
                SD_FILES_PATH = this.context.getExternalFilesDir(null).getPath();
            } catch (NullPointerException ex) {
                SD_FILES_PATH = this.context.getFilesDir().getPath();
            } catch (NoSuchMethodError e) {
                SD_FILES_PATH = this.context.getFilesDir().getPath();
            }

        } else {
            SD_FILES_PATH = this.context.getFilesDir().getPath();
        }
        FILES_PATH = this.context.getFilesDir().getPath();
    }

    /**
     * Create file from SD card
     *
     * @throws java.io.IOException
     */

    public File createSDFile(String fileName) throws IOException {
        File file = new File(SD_FILES_PATH + "/" + fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    /**
     * Create file from DATA
     *
     * @throws java.io.IOException
     */

    public File createFile(String fileName) throws IOException {
        File file = new File(FILES_PATH + "/" + fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    /**
     * Get files list
     *
     * @param filter
     * @return
     */
    public String[] getSDFilesList(FilenameFilter filter) {
        File file = new File(SD_FILES_PATH);
        String[] fileNames = file.list(filter);
        return fileNames;
    }

    /**
     * Get files list
     *
     * @param filter
     * @return
     */
    public String[] getFilesList(FilenameFilter filter) {
        File file = new File(FILES_PATH);
        String[] fileNames = file.list(filter);
        return fileNames;
    }

    /**
     * Delete file from SD card
     *
     * @param fileName
     */

    public boolean deleteSDFile(String fileName) {
        File file = new File(SD_FILES_PATH + "/" + fileName);
        if (file == null || !file.exists() || file.isDirectory())
            return false;
        return file.delete();
    }

    /**
     * Delete file from DATA
     *
     * @param fileName
     */

    public boolean deleteFile(String fileName) {
        File file = new File(FILES_PATH + "/" + fileName);
        if (file == null || !file.exists() || file.isDirectory())
            return false;
        return file.delete();
    }

    /**
     * Delete files
     *
     * @param file
     */
    public void deleteFiles(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    this.deleteFiles(files[i]);
                }
            }
            file.delete();
        }
    }

    /**
     * File is Exist
     *
     * @param fileName
     * @return boolean
     */
    public boolean SDfilesIsExist(String fileName) {
        File file = new File(SD_FILES_PATH + "/" + fileName);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    /**
     * File is Exist
     *
     * @param fileName
     * @return boolean
     */
    public boolean fileIsExist(String fileName) {
        File file = new File(FILES_PATH + "/" + fileName);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    public String getFilesPath() {
        return FILES_PATH;
    }

    public String getSDFilesPath() {
        return SD_FILES_PATH;
    }

    public boolean hasSD() {
        return hasSD;
    }

}