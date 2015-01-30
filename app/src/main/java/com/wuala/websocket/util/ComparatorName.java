package com.wuala.websocket.util;


import java.io.File;
import java.util.Comparator;

public class ComparatorName implements Comparator<File> {
    boolean isup = true;

    public ComparatorName(boolean isup) {
        this.isup = isup;
    }

    @Override
    public int compare(File file1, File file2) {
        if (file1 != null && file2 != null) {
            return isup ? file1.getName().compareTo(file2.getName()) : file2.getName().compareTo(file1.getName());
        } else {
            throw new IllegalArgumentException("file not exist");
        }
    }
}
