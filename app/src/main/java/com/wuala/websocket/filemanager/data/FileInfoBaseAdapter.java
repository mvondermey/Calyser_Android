package com.wuala.websocket.filemanager.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.wuala.websocket.R;
import com.wuala.websocket.filemanager.layout.FileInfoShowLayout;
import com.wuala.websocket.filemanager.util.FileOperationListener;
import com.wuala.websocket.util.Marco;
import com.wuala.websocket.util.Util;


public class FileInfoBaseAdapter extends BaseAdapter {
    public Context context;
    List<FileInfo> fileList = new ArrayList<FileInfo>();
    boolean isSelect;
    FileOperationListener fileOperationListener;

    public FileInfoBaseAdapter(Context context, boolean isSelect, FileOperationListener fileOperationListener) {
        this.context = context;
        this.isSelect = isSelect;
        this.fileOperationListener = fileOperationListener;
    }

    public void addFileItem(FileInfo fileitem) {
        fileList.add(fileitem);
    }

    public boolean isAllFileItemCanSelect() {
        return false;
    }

    public boolean isSelectable(int position) {
        return fileList.get(position).isSelected();

    }

    public void setFileList(List<FileInfo> fileList) {
        this.fileList = fileList;
    }

    @Override
    public int getCount() {
        return fileList.size();
    }

    @Override
    public Object getItem(int position) {
        return fileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View covertView, ViewGroup parent) {
        FileInfoShowLayout fileview;
        if (covertView == null) {
            fileview = new FileInfoShowLayout(context, fileList.get(position), isSelect);

        } else {
            fileview = (FileInfoShowLayout) covertView;
            fileview.setFileName(fileList.get(position).getFileName());
//            SimpleDateFormat dateformat = new SimpleDateFormat(
//                    "yyyy-MM-dd HH:mm:ss");
            fileview.setFileIcon(fileList.get(position).getfileIcon());
            fileview.setFileSize(fileList.get(position).getFileSize());
        }

        if (Util.isImportSelect)
            fileview.getOptionImageView().setVisibility(View.GONE);
        else
            fileview.getOptionImageView().setVisibility(View.VISIBLE);
        if (Util.isReadOnly)
            fileview.getOptionImageView().setVisibility(View.GONE);
        else {
            fileview.getOptionImageView().setVisibility(View.VISIBLE);
            fileview.getOptionImageView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fileOperationListener.fileOperation(Marco.FILE_OPEN_OPTION, position, view);
                }
            });
        }
        if (fileList.get(position).getFileName().startsWith(context.getString(R.string.back)))
            fileview.getOptionImageView().setVisibility(View.GONE);
        return fileview;
    }

}
