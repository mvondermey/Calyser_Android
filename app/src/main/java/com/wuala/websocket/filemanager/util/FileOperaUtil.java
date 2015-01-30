package com.wuala.websocket.filemanager.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileOperaUtil {

    public static void deleteAll(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("not found:" + file.getName());
        }
        boolean rslt = true;
        if (!(rslt = file.delete())) {
            File subs[] = file.listFiles();
            for (int i = 0; i <= subs.length - 1; i++) {
                if (subs[i].isDirectory())
                    deleteAll(subs[i]);
                rslt = subs[i].delete();
            }
            rslt = file.delete();
        }
        if (!rslt) {
            throw new IOException("can not delete:" + file.getName());
        }
        return;
    }

    public static void moveFile(String source, String destination) {
        new File(source).renameTo(new File(destination));
    }

    public static void moveFile(File source, File destination) {
        source.renameTo(destination);
    }

    public static void copyFile(File src, File target) {

        if (src.isDirectory()) {
            if (!target.exists()) {
                target.mkdir();
            }
            File[] currentFiles;
            currentFiles = src.listFiles();
            for (int i = 0; i < currentFiles.length; i++) {
                if (currentFiles[i].isDirectory()) {
                    copyFile(new File(currentFiles[i] + "/"),
                            new File(target.getAbsolutePath() + "/"
                                    + currentFiles[i].getName() + "/"));
                } else {
                    copyFile(currentFiles[i], new File(target.getAbsolutePath()
                            + "/" + currentFiles[i].getName()));
                }
            }

        } else {
            InputStream in = null;
            OutputStream out = null;
            BufferedInputStream bin = null;
            BufferedOutputStream bout = null;
            try {
                in = new FileInputStream(src);
                out = new FileOutputStream(target);
                bin = new BufferedInputStream(in);
                bout = new BufferedOutputStream(out);

                byte[] b = new byte[8192];
                int len = bin.read(b);
                while (len != -1) {
                    bout.write(b, 0, len);
                    len = bin.read(b);
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bin != null) {
                        bin.close();
                    }
                    if (bout != null) {
                        bout.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }
}
