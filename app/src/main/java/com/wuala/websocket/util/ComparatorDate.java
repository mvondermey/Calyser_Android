package com.wuala.websocket.util;


import java.io.File;
import java.util.Comparator;
import java.util.Date;

public class ComparatorDate implements Comparator<File> {
    boolean isup = true;

    public ComparatorDate(boolean isup) {
        this.isup = isup;
    }

    @Override
    public int compare(File file1, File file2) {
        Date date1 = new Date(file1.lastModified());
        Date date2 = new Date(file2.lastModified());
        if (date1.before(date2)) {
            return isup ? 1 : -1;
        }
        return isup ? -1 : 1;
    }
}
