package com.wuala.websocket.view;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout.LayoutParams;

import com.wuala.websocket.R;
import com.wuala.websocket.filemanager.layout.MainLayout;
import com.wuala.websocket.util.Util;

import java.io.File;


public class ImportFileFragment extends ContainerBaseFragment {


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return init();
    }

    public MainLayout init() {
        MainLayout mainLayout = new MainLayout(getActivity(), this);
        mainLayout.setBackgroundColor(Color.WHITE);
        mainLayout.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        fileListView = mainLayout.getFileListView();
        documentRoot = Environment.getExternalStorageDirectory().toString();
        openOrBrowseTheFile(new File(documentRoot));
        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String selectedFileString = fileList.get(position)
                        .getFileName();
                if (selectedFileString
                        .startsWith(getString(R.string.back))) {
                    upOnLevel();
                } else {
                    File clickedFile = null;
                    String currentDirectory = nowDirectory.getAbsolutePath();
                    if (!currentDirectory.endsWith("/")) {
                        currentDirectory += "/";
                    }
                    clickedFile = new File(currentDirectory
                            + fileList.get(position).getFileName());
                    if (clickedFile != null) {
                        openOrBrowseTheFile(clickedFile);
                    }
                }
            }
        });
        return mainLayout;
    }

    /**
     * back to previous folder
     */
    public void upOnLevel() {
        if (this.nowDirectory.getParent() != null) {
            this.openOrBrowseTheFile(this.nowDirectory.getParentFile());
        }
    }


    @Override
    public boolean onBackKeyDown() {
        if (!nowDirectory.getAbsolutePath().equals(documentRoot))
            upOnLevel();
        return super.onBackKeyDown();
    }
}
