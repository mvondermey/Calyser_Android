package com.wuala.websocket.view;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.dropbox.chooser.android.DbxChooser;

import java.io.File;

import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wuala.websocket.R;
import com.wuala.websocket.activity.ImportFromSDActivity;
import com.wuala.websocket.activity.MainActivity;
import com.wuala.websocket.callback.SendFileListener;
import com.wuala.websocket.filemanager.layout.MainLayout;
import com.wuala.websocket.httpserver.DownloadManager;
import com.wuala.websocket.model.FileItem;
import com.wuala.websocket.util.Util;


public class DocumentsFragment extends ContainerBaseFragment implements MainActivity.DropBoxSelectCallBack, View.OnClickListener {
    /**
     * drop box app key
     */
    static final String APP_KEY = "pu4on2updhbjxxg";
    /**
     * open dropbox request code
     */
    static final int DBX_CHOOSER_REQUEST = 100;
    /**
     * dropbox chooser
     */
    private DbxChooser mChooser;
    /**
     * download file manager
     */
    private DownloadManager mDownloadManager;
    /**
     * progressbar of dialog in downloading
     */
    private ProgressBar progressBar;
    /**
     * show the percent of download
     */
    private TextView progressText;

    private Dialog dialog;
    public static SendFileListener mSendCallback;
    private int rootBottom = Integer.MIN_VALUE;
    private LinearLayout bottomLayout;

    public static DocumentsFragment newInstance(FragmentType type, SendFileListener sendCallback) {
        MainActivity.CURRENT_FRAGMENT = type;
        mSendCallback = sendCallback;
        DocumentsFragment fragment = new DocumentsFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initActionBar();
        setDropBoxSelectCallBack(this);
        mChooser = new DbxChooser(APP_KEY);
        final ViewGroup fileViewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_documents, container, false);
        ((RelativeLayout) fileViewGroup.findViewById(R.id.layout_main)).addView(init());
        if (!Util.isReadOnly) {
            mDownloadManager = DownloadManager.getInstance(getActivity());
            mDownloadManager.setHandler(downloadHandler);
        }
        fileViewGroup.findViewById(R.id.layout_import).setOnClickListener(this);
        bottomLayout = (LinearLayout) fileViewGroup.findViewById(R.id.layout_bottom);
        if (Util.isReadOnly)
            bottomLayout.setVisibility(View.GONE);
        else {
            bottomLayout.setVisibility(View.VISIBLE);
            fileViewGroup.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Rect r = new Rect();
                    fileViewGroup.getGlobalVisibleRect(r);
                    if (rootBottom == Integer.MIN_VALUE) {
                        rootBottom = r.bottom;
                        return;
                    }
                    if (r.bottom < rootBottom) {
                        bottomLayout.setVisibility(View.GONE);
                    } else {
                        bottomLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
        return fileViewGroup;
    }

    /**
     * download file handler
     */
    private Handler downloadHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (!Util.isReadOnly) {
                FileItem downloadFile = (FileItem) msg.obj;
                // update item
                updateView(downloadFile);
            }
        }
    };

    /**
     * uodate the progress and percent textview when downloading
     *
     * @param downloadFile
     */
    public void updateView(FileItem downloadFile) {
        switch (downloadFile.getDownloadState()) {
            case DownloadManager.DOWNLOAD_STATE_FINISH:
                dialog.dismiss();
                openOrBrowseTheFile(Util.currentFile);
                break;
            case DownloadManager.DOWNLOAD_STATE_DOWNLOADING:
                int progress = (int) (downloadFile.getDownloadSize() * 100.0f / downloadFile.getTotalSize());
                progressBar.setProgress(progress);
                progressText.setText(progress + "%");
                openOrBrowseTheFile(Util.currentFile);
                break;
        }
    }

    @Override
    public void onResume() {
        openOrBrowseTheFile(nowDirectory);
        getBottomLayout().setVisibility(View.GONE);
        super.onResume();
    }

    public MainLayout init() {
        MainLayout mainLayout = new MainLayout(getActivity(), this);
        mainLayout.setBackgroundColor(Color.WHITE);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        fileListView = mainLayout.getFileListView();
        File file = new File(documentRoot);
        if (!file.exists()) {
            file.mkdir();
        }
        browseTheRootAllFile();

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
     * show the root file in document
     */
    public void browseTheRootAllFile() {
        openOrBrowseTheFile(new File(documentRoot));
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
        else
            getFragmentManager().popBackStack();
        return super.onBackKeyDown();
    }

    private void initActionBar() {
        setActionBarTitle(getActivity().getResources().getString(R.string.documents));
        ImageButton btnLeft = getLeftButton();
        btnLeft.setVisibility(View.VISIBLE);
        btnLeft.setImageResource(R.drawable.btn_back_style);
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack();
            }
        });
        ImageButton btnRight = getRightButton();
        btnRight.setVisibility(View.INVISIBLE);
        btnRight.setImageResource(R.drawable.btn_add_style);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectDialog();
            }
        });
    }

    /**
     * show a dialog for choose import file form dropbox or sd card
     */
    public void showSelectDialog() {
        final Dialog dialog = new Dialog(getActivity(), R.style.dialog);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View convertView = inflater.inflate(R.layout.dialog_import_choose, null);
        dialog.setContentView(convertView);
        RelativeLayout btnSD = (RelativeLayout) convertView.findViewById(R.id.btn_sd);
        btnSD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.currentFile = nowDirectory;
                Intent intent = new Intent(getActivity(), ImportFromSDActivity.class);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        RelativeLayout btnDropBox = (RelativeLayout) convertView.findViewById(R.id.btn_dropbox);
        btnDropBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.currentFile = nowDirectory;
                openDropBox();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void openDropBox() {
        mChooser.forResultType(DbxChooser.ResultType.DIRECT_LINK).launch(getActivity(),
                DBX_CHOOSER_REQUEST);
    }

    /**
     * the function after user choose a file from drop box
     * if the file is a video or picture type ,it will not be download
     *
     * @param result
     */
    @Override
    public void dropBoxSelectCallBack(DbxChooser.Result result) {
        FileItem fileItem = new FileItem();
        fileItem.setID(result.getLink().toString());
        fileItem.setUrl(result.getLink().toString());
        if (checkFileType(result.getLink().toString(),
                getResources().getStringArray(R.array.fileEndingImage)) || checkFileType(result.getLink().toString(),
                getResources().getStringArray(R.array.fileEndingVideo))) {
            showMessageDialog(getActivity().getResources().getString(R.string.notice), getActivity().getResources().getString(R.string.unable_import));
            return;
        }
        mDownloadManager.startDownload(fileItem);
        showProgressDialog();
    }

    /**
     * show a dialog for download the file from dropbox
     */
    public void showProgressDialog() {
        dialog = new Dialog(getActivity(), R.style.dialog);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View convertView = inflater.inflate(R.layout.dialog_download, null);
        dialog.setContentView(convertView);
        Button btnCancel = (Button) convertView.findViewById(R.id.btn_cancel);
        progressBar = (ProgressBar) convertView.findViewById(R.id.progress);
        progressText = (TextView) convertView.findViewById(R.id.txt_progress);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDownloadManager.stopAllDownloadTask();
                dialog.dismiss();
                openOrBrowseTheFile(Util.currentFile);
            }
        });
        dialog.show();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_import:
                showSelectDialog();
                break;
        }
    }
}
