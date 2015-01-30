package com.wuala.websocket.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.wuala.websocket.R;
import com.wuala.websocket.activity.MainActivity;
import com.wuala.websocket.activity.MainApplication;
import com.wuala.websocket.filemanager.data.FileInfo;
import com.wuala.websocket.filemanager.data.FileInfoBaseAdapter;
import com.wuala.websocket.filemanager.util.FileOperaUtil;
import com.wuala.websocket.filemanager.util.FileOperationListener;
import com.wuala.websocket.model.FileItem;
import com.wuala.websocket.util.ComparatorDate;
import com.wuala.websocket.util.ComparatorName;
import com.wuala.websocket.util.Marco;
import com.wuala.websocket.util.Util;

import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ContainerBaseFragment extends BaseFragment implements FileOperationListener {
    /**
     * current folder
     */
    protected File nowDirectory = new File(MainApplication.CONTAINER_PATH);
    /**
     * the children files of current folder
     */
    protected List<FileInfo> fileList = new ArrayList<FileInfo>();
    /**
     * listView for document fragment
     */
    protected ListView fileListView = null;
    /**
     * document root path
     */
    protected String documentRoot = MainApplication.CONTAINER_PATH + "/documents";
    /**
     * the path for photo or video root path
     */
    protected String mediaAlbumRootLocation;
    /**
     * album name list for photo or video
     */
    protected List<String> albumNames;
    /**
     * album listView for video or photo
     */
    protected ListView albumListView;
    /**
     * temp file for operation file or folder
     */
    protected File myTmpFile = null;
    /**
     * is cut operation or paste
     */
    protected boolean isCut = false;
    /**
     * the popupWindow of choose operation tab
     */
    protected PopupWindow popupWindow = null;
    /**
     * the popupWindow of choose folder or album
     */
    protected PopupWindow floderPopupWindow = null;
    /**
     * current page is document or media
     */
    protected boolean isDocument = true;
    /**
     * temp folder for move file
     */
    protected File tempFolder = null;
    /**
     * progress dialog for runtime
     */
    protected Dialog mProgressDialog;
    /**
     * current loaction
     */
    protected String currentLocation;
    /**
     * no media in album show this layout
     */
    protected LinearLayout emptyLayout;
    /**
     * folder list view for move
     */
    private ListView floderListView = null;
    /**
     * folder list for move
     */
    private List<String> floderList = new ArrayList<String>();
    /**
     * target folder for move
     */
    private File tempTargetFloderDirectory;
    /**
     * share,move,delete,rename, operation callback
     */
    private MediaContentFragment.OptionCallBackListener mOptionCallBackListener;
    /**
     * is sort by date up
     */
    private boolean dateIsup = true;
    /**
     * is sort by name up
     */
    private boolean nameIsup = true;


    @Override
    public void onResume() {
        super.onResume();
        if (floderPopupWindow != null)
            floderPopupWindow.dismiss();
        if (popupWindow != null)
            popupWindow.dismiss();
    }

    /**
     * refresh listview for media album
     */
    protected void refreshView() {
        if (mediaAlbumRootLocation != null && !mediaAlbumRootLocation.isEmpty()) {
            File pictureRootFile = new File(mediaAlbumRootLocation);
            if (!pictureRootFile.exists()) {
                pictureRootFile.mkdir();
            }
            albumNames = new ArrayList<String>();
            for (File file : pictureRootFile.listFiles()) {
                if (file.getName().equals(getString(R.string.photo_album)) || file.getName().equals(getString(R.string.video_album)))
                    albumNames.add(0, file.getName());
                else
                    albumNames.add(file.getName());
            }
            albumListView.setAdapter(new AlbumAdapter(getActivity(), albumNames, Util.isPhoto));
        }
    }

    /**
     * delete file and if the file is a folder it will delete the all files of the folder
     *
     * @param file the target file
     */
    protected void deleteFile(final File file) {
        if (file.getName().equals(getString(R.string.photo_album)) || file.getName().equals(getString(R.string.video_album)) || file.getName().equals(getString(R.string.my_document))) {
            showMessageDialog(getActivity().getResources().getString(R.string.notice), getActivity().getResources().getString(R.string.you_can_not_change_the_folder));
            return;
        }
        String title = getActivity().getResources().getString(R.string.delete_album);
        String text = "";
        if (isDocument) {
            title = file.isFile() ? getActivity().getResources().getString(R.string.delete_file) : getActivity().getResources().getString(R.string.delete_folder);
            text = file.isFile() ? getActivity().getResources().getString(R.string.delete_file_confirm) : getActivity().getResources().getString(R.string.delete_folder_confirm);
        } else if (Util.isPhoto) {
            if (file.isFile())
                text = getActivity().getResources().getString(R.string.delete_single_photo_message);
            else
                text = getActivity().getResources().getString(R.string.delete_album_confirm_photos);
        } else {
            text = getActivity().getResources().getString(R.string.delete_album_confirm_videos);
        }
        confirmDialog(title, text,
                new View.OnClickListener() {
                    public void onClick(View view) {
                        try {
                            FileOperaUtil.deleteAll(file);
                            openOrBrowseTheFile(nowDirectory);
                        } catch (IOException e) {
                            e.printStackTrace();
                            showMessageDialog(getActivity().getResources().getString(R.string.delete_file), getActivity().getResources().getString(R.string.delete_file) + file.getName()
                                    + getActivity().getResources().getString(R.string.failed));
                        }

                    }
                });
    }

    /**
     * rename a file or a folder
     *
     * @param file the target file
     */
    protected void renameByFile(final File file) {
        if (file.getName().equals(getString(R.string.photo_album)) || file.getName().equals(getString(R.string.video_album)) || file.getName().equals(getString(R.string.my_document))) {
            showMessageDialog(getActivity().getResources().getString(R.string.notice), getActivity().getResources().getString(R.string.you_can_not_change_the_folder));
            return;
        }
        String title = "";
        if (isDocument) {
            title = file.isFile() ? getActivity().getResources().getString(R.string.rename_file) : getActivity().getResources().getString(R.string.rename_folder);
        } else {
            title = getString(R.string.edit_album_name);
        }

        final String finalTitle = title;
        final String finalTitle1 = title;
        String message = getActivity().getResources().getString(R.string.input_new_name);
        if (!isDocument) {
            message = getActivity().getResources().getString(R.string.enter_new_album_name);
        }
        showCustomDialog(file, title, message,
                new View.OnClickListener() {

                    public void onClick(View view) {
                        EditText editText = (EditText) view.findViewById(R.id.edt_ip);
                        String newName = editText.getText()
                                .toString();
                        if (!newName.equals(file.getName())) {
                            String currentDirectory = nowDirectory
                                    .getAbsolutePath();
                            if (!currentDirectory.endsWith("/")) {
                                currentDirectory += "/";
                            }
                            final String allName = currentDirectory + newName;
                            if (new File(allName).exists()) {
                                showMessageDialog(getActivity().getResources().getString(R.string.rename), "The " + (isDocument ? "folder" : "album") + " name is already existing... Please select another one.");

                            } else {
                                boolean flag = file.renameTo(new File(allName));
                                if (flag == true) {
                                    openOrBrowseTheFile(nowDirectory);
                                } else {
                                    showMessageDialog(finalTitle1, getActivity().getResources().getString(R.string.rename_failed));
                                }
                            }
                        }
                    }
                }
        );
    }

    /**
     * create a new folder or a album on current folder
     */
    protected void createNewFile() {
        createNewFile(nowDirectory
                .getAbsolutePath());
    }

    /**
     * create a new folder or a album on provide location
     *
     * @param location
     */
    protected void createNewFile(final String location) {
        String title = getActivity().getResources().getString(R.string.new_folder);
        String text = getActivity().getResources().getString(R.string.input_flolder_name);
        if (!isDocument) {
            title = getString(R.string.new_album);
            text = getString(R.string.enter_new_album_name);
        }
        showCustomDialog(null, title, text,
                new View.OnClickListener() {
                    public void onClick(View view) {
                        EditText editText = (EditText) view.findViewById(R.id.edt_ip);
                        String pathName = editText.getText()
                                .toString();
                        String currentDirectory = location;
                        if (!currentDirectory.endsWith("/")) {
                            currentDirectory += "/";
                        }
                        final String allName = currentDirectory + pathName;
                        final File file = new File(allName);
                        if (file.exists()) {
                            showMessageDialog(getActivity().getResources().getString(R.string.new_folder), "The " + (isDocument ? "folder" : "album") + " name is already existing... Please select another one.");
                        } else {
                            boolean creadok = file.mkdirs();
                            if (creadok) {
                                openOrBrowseTheFile(nowDirectory);
                            } else {
                                showMessageDialog(getActivity().getResources().getString(R.string.new_folder), getActivity().getResources().getString(R.string.failed));
                            }
                        }

                    }
                });
    }

    @Override
    public void fileOperation(int ActionType, int position, View myView) {
        switch (ActionType) {
            case Marco.FILE_RENAME:
                renameByFile(new File(nowDirectory.getAbsolutePath() + "/"
                        + fileList.get(position).getFileName()));
                break;
            case Marco.FILE_DELETE:
                deleteFile(new File(nowDirectory.getAbsolutePath() + "/"
                        + fileList.get(position).getFileName()));
                break;
            case Marco.FILE_CREATE:
                createNewFile();
                break;
            case Marco.FILE_OPEN_OPTION:
                getPopupWindow(new File(nowDirectory.getAbsolutePath() + "/"
                        + fileList.get(position).getFileName()), true);
                popupWindow.showAsDropDown(myView);
                break;
            case Marco.FILE_SORT_BY_DATE:
                ((Button) myView).setCompoundDrawablesWithIntrinsicBounds(null, null, dateIsup ? getResources().getDrawable(R.drawable.icon_sort_down) : getResources().getDrawable(R.drawable.icon_sort_up), null);
                ((Button) (((LinearLayout) myView.getParent().getParent()).findViewById(R.id.btn_sort_name))).setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.icon_sort_null), null);
                fillListView((new File(nowDirectory.getAbsolutePath())).listFiles(), Marco.FILE_SORT_BY_DATE);
                dateIsup = !dateIsup;
                break;
            case Marco.FILE_SORT_BY_NAME:
                if (Util.isImportSelect)
                    ((Button) myView).setCompoundDrawablesWithIntrinsicBounds(null, null, nameIsup ? getResources().getDrawable(R.drawable.icon_sort_up) : getResources().getDrawable(R.drawable.icon_sort_down), null);
                else
                    ((Button) myView).setCompoundDrawablesWithIntrinsicBounds(null, null, nameIsup ? getResources().getDrawable(R.drawable.icon_sort_up) : getResources().getDrawable(R.drawable.icon_sort_down), null);
                ((Button) (((LinearLayout) myView.getParent().getParent()).findViewById(R.id.btn_sort_date))).setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.icon_sort_null), null);
                nameIsup = !nameIsup;
                fillListView((new File(nowDirectory.getAbsolutePath())).listFiles(), Marco.FILE_SORT_BY_NAME);

                break;
            case Marco.FILE_SEARCH:
                final EditText editText = (EditText) myView;
                nowDirectory = new File(documentRoot);
                if (!editText.getText().toString().isEmpty()) {
                    (new AsyncTask<Void, Void, List<File>>() {
                        @Override
                        protected List<File> doInBackground(Void... voids) {
                            List<File> fileList = FindFile(new File(documentRoot), editText.getText().toString());
                            return fileList;
                        }

                        @Override
                        protected void onPostExecute(List<File> files) {
                            fillListView((File[]) files.toArray(new File[files.size()]));
                        }
                    }).execute();
                } else {
                    openOrBrowseTheFile(nowDirectory);
                }
                break;
        }
    }

    /**
     * find files by name in provide folder
     *
     * @param file       provide folder
     * @param key_search key words
     * @return the file list include the key words
     */
    private static List<File> FindFile(File file, String key_search) {
        List<File> list = new ArrayList<File>();
        if (file.isDirectory()) {
            File[] all_file = file.listFiles();
            if (all_file != null) {
                for (File tempf : all_file) {
                    if (tempf.isDirectory()) {
                        if (tempf.getName().toLowerCase().lastIndexOf(key_search.toLowerCase()) > -1) {
                            list.add(tempf);
                        }
                        list.addAll(FindFile(tempf, key_search));
                    } else {
                        if (tempf.getName().toLowerCase().lastIndexOf(key_search.toLowerCase()) > -1) {
                            list.add(tempf);
                        }
                    }
                }
            }
        }
        return list;
    }

    /**
     * open a folder refrest the file list on document fragment
     *
     * @param file target folder
     */
    protected void openOrBrowseTheFile(File file) {
        if (!isDocument) {
            refreshView();
            if (mOptionCallBackListener != null)
                mOptionCallBackListener.optionCallBackListener();
            return;
        }
        if (file.isDirectory()) {
            if (file.listFiles() != null) {
                this.nowDirectory = file;
                fillListView(file.listFiles());
            } else {
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.no_access),
                        Toast.LENGTH_SHORT).show();
            }

        } else {
            openFile(file);
        }
    }

    /**
     * show the dialog on import file to container
     *
     * @param title
     * @param text
     */
    protected void showImportLoadingDialog(String title, String text) {
        mProgressDialog = new Dialog(getActivity(), R.style.dialog);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View convertView = inflater.inflate(R.layout.dialog_progress, null);
        mProgressDialog.setContentView(convertView);
        mProgressDialog.setCancelable(false);
        TextView titleView = (TextView) convertView.findViewById(R.id.txt_title);
        titleView.setText(title);
        TextView txtView = (TextView) convertView.findViewById(R.id.progress_text);
        txtView.setText(text);
        mProgressDialog.show();
    }

    /**
     * convert the file size
     *
     * @param len
     * @return
     */
    protected String formatFileSize(long len) {
        if (len < 1024)
            return len + " B";
        else if (len < 1024 * 1024)
            return len / 1024 + "." + (len % 1024 / 10 % 100) + " KB";
        else if (len < 1024 * 1024 * 1024)
            return len / (1024 * 1024) + "." + len % (1024 * 1024) / 10 % 100 + " MB";
        else
            return len / (1024 * 1024 * 1024) + "." + len % (1024 * 1024 * 1024) / 10 % 100 + " MB";
    }

    /**
     * open a folder or a file
     *
     * @param openFileName
     */
    protected void openFile(final File openFileName) {
        if (Util.isReadOnly) {
            FileItem fileItem = new FileItem();
            fileItem.setID(System.currentTimeMillis() + "");
            fileItem.setFileName(openFileName.getName());
            fileItem.setSize(formatFileSize(openFileName.length()));
            fileItem.setTotalSize(openFileName.length());
            fileItem.setPrefix(openFileName.getName().substring(openFileName.getName().lastIndexOf(".") + 1));
            String filepath = openFileName.getAbsolutePath().replace(MainApplication.CONTAINER_PATH + "/", "").replace(openFileName.getName(), "");
//                fileItem.setUrl("https://" + getIPAddress() + ":" + MainApplication.HTTP_PORT + "/" + filepath + openFileName.getName
            fileItem.setUrl(convert("https://" + getIPAddress() + ":" + MainApplication.HTTP_PORT + "/" + filepath + openFileName.getName()));

            DocumentsFragment.mSendCallback.onFileSend(fileItem);
            getFragmentManager().popBackStack();
            getFragmentManager().popBackStack();
            return;
        }
        if (!Util.isImportSelect) {
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            File file = new File(openFileName.getAbsolutePath());
            String fileName = file.getName();
            if (checkFileType(fileName,
                    getResources().getStringArray(R.array.fileEndingImage))) {
                intent.setDataAndType(Uri.fromFile(file), "image/*");
            } else if (checkFileType(fileName,
                    getResources().getStringArray(R.array.fileEndingAudio))) {
                intent.setDataAndType(Uri.fromFile(file), "audio/*");

            } else if (checkFileType(fileName,
                    getResources().getStringArray(R.array.fileEndingVideo))) {
                intent.setDataAndType(Uri.fromFile(file), "video/*");
            } else if (checkFileType(fileName,
                    getResources().getStringArray(R.array.fileEndingAPK))) {
                intent.setDataAndType(Uri.fromFile(file),
                        "application/vnd.android.package-archive");
            }

            startActivity(intent);
        } else {
            if (!openFileName.getName()
                    .startsWith(getString(R.string.back))) {
                confirmDialog(getActivity().getResources().getString(R.string.notice), getActivity().getResources().getString(R.string.are_you_want_import_this_file), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        isCut = false;
                        showImportLoadingDialog(getString(R.string.loading), getString(R.string.import_file));
                        setOptionCallBackListener(new MediaContentFragment.OptionCallBackListener() {
                            @Override
                            public void optionCallBackListener() {
                                getActivity().finish();
                            }
                        });
                        (new AsyncTask<Void, Void, Boolean>() {
                            @Override
                            protected Boolean doInBackground(Void... voids) {
                                PasteFile(openFileName, Util.currentFile);
                                return true;
                            }

                            @Override
                            protected void onPostExecute(Boolean aBoolean) {
                                mProgressDialog.cancel();
                            }
                        }).execute();


                    }
                });
            }
        }
    }

    /**
     * create file listview sort by name and provide file array
     *
     * @param files
     */
    protected void fillListView(File[] files) {
        fillListView(files, Marco.FILE_SORT_BY_NAME);
    }

    /**
     * create file list view by provide sort type and file array
     *
     * @param files
     * @param sortType
     */
    protected void fillListView(File[] files, int sortType) {
        Boolean isDocument = true;
        this.fileList.clear();
        Drawable currentIcon = null;
        if (!this.nowDirectory.toString().equals(documentRoot)) {
            String fileName = nowDirectory.getParentFile().getName();
            if (fileName.toLowerCase().equals("documents"))
                fileName = getActivity().getResources().getString(R.string.documents);
            fileList.add(new FileInfo(this.getString(R.string.back) + " " + fileName,
                    0, ".", this.getResources().getDrawable(
                    R.drawable.uponelevel)));
        }
        sortFileList(sortType, Arrays.asList(files));
        for (File file : files) {
            isDocument = true;
            String fileName = file.getName();
            if (file.isDirectory()) {
                if (file.getName().equals(getString(R.string.my_document)))
                    currentIcon = getResources().getDrawable(R.drawable.icon_doc_readonly);
                else
                    currentIcon = getResources().getDrawable(R.drawable.icon_doc);

            } else {
                if (checkFileType(fileName,
                        getResources().getStringArray(R.array.fileEndingVideo))) {
                    isDocument = false;
                } else if (checkFileType(fileName,
                        getResources().getStringArray(R.array.fileEndingImage))) {
                    isDocument = false;
                } else {
                    currentIcon = getResources().getDrawable(R.drawable.icon_document);
                }
            }
            if (isDocument) {
                long updateTime = file.lastModified();
                long TmpFileSize = file.length();
                String fileSize = FormatFileSize(TmpFileSize);
                if (!file.isDirectory()) {
                    this.fileList.add(new FileInfo(fileName, updateTime, fileSize,
                            currentIcon));
                } else {
                    if (file.getName().equals(getActivity().getResources().getString(R.string.my_document)))
                        this.fileList.add(0, new FileInfo(fileName, updateTime, fileSize,
                                currentIcon));
                    else
                        this.fileList.add(new FileInfo(fileName, updateTime, getActivity().getResources().getString(R.string.floder),
                                currentIcon));
                }
            }
        }

        FileInfoBaseAdapter adapter = new FileInfoBaseAdapter(getActivity(), false, this);
        adapter.setFileList(this.fileList);
        fileListView.setAdapter(adapter);
    }

    /**
     * sort filelist by provide type
     *
     * @param type
     * @param filelist
     */
    public void sortFileList(int type, List<File> filelist) {
        switch (type) {
            case Marco.FILE_SORT_BY_DATE:
                Collections.sort(filelist, new ComparatorDate(dateIsup));
                break;
            case Marco.FILE_SORT_BY_NAME:
                Collections.sort(filelist, new ComparatorName(nameIsup));
                break;
        }
    }

    /**
     * check file type by provide file name and extend name array
     *
     * @param fileName
     * @param extendName
     * @return
     */
    protected boolean checkFileType(String fileName, String[] extendName) {
        for (String aEnd : extendName) {
            if (fileName.endsWith(aEnd.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * convert file size
     *
     * @param TmpFileSize
     * @return
     */
    protected String FormatFileSize(long TmpFileSize) {
        String fileSize = "";
        DecimalFormat df = new DecimalFormat("#.00");
        if (TmpFileSize < 1024) {
            fileSize = df.format((double) TmpFileSize) + "B";
        } else if (TmpFileSize < 1048576) {
            fileSize = df.format((double) TmpFileSize / 1024) + "K";
        } else if (TmpFileSize < 1073741824) {
            fileSize = df.format((double) TmpFileSize / 1048576) + "M";
        } else {
            fileSize = (int) (TmpFileSize / 1073741824) + "G";
        }
        return fileSize;
    }

    /**
     * paste file by current operate file to now folder
     */
    protected void PasteFile() {
        PasteFile(this.myTmpFile, this.nowDirectory);
    }

    /**
     * paste file by provide file to provide folder
     *
     * @param myTmpFile
     * @param nowDirectory
     */
    protected void PasteFile(final File myTmpFile, final File nowDirectory) {
        if (myTmpFile.getName().equals(getString(R.string.photo_album)) || myTmpFile.getName().equals(getString(R.string.video_album)) || myTmpFile.getName().equals(getString(R.string.my_document))) {
            showMessageDialog(getActivity().getResources().getString(R.string.notice), getActivity().getResources().getString(R.string.you_can_not_change_the_folder));
            return;
        }
        if (myTmpFile == null) {
            showMessageDialog(getActivity().getResources().getString(R.string.notice), getActivity().getResources().getString(R.string.no_file));
        } else {
            String currentDirectory = nowDirectory.getAbsolutePath();
            if (!currentDirectory.endsWith("/")) {
                currentDirectory += "/";
            }
            final String allName = currentDirectory + myTmpFile.getName();
            final File targetFile = new File(allName);
            if (!isCut) {
                if (targetFile.exists()) {
                    FileOperaUtil.copyFile(myTmpFile,
                            targetFile);
                } else {
                    FileOperaUtil.copyFile(myTmpFile, targetFile);
                }
            } else {
                if (!targetFile.exists()) {
                    FileOperaUtil.moveFile(myTmpFile, targetFile);
                }
            }
        }
        if (mOptionCallBackListener != null) {
            mOptionCallBackListener.optionCallBackListener();
        }
    }

    /**
     * folder popupWindow ClickListener
     */
    public class FolderPopupWindowClickListener implements View.OnClickListener {
        private List<File> files;

        public FolderPopupWindowClickListener(List<File> files) {
            this.files = files;
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.layout_move:
                    if (tempTargetFloderDirectory.getAbsolutePath().equals(files.get(0).getParentFile().getParentFile().getAbsolutePath()) && !isDocument) {
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.move_successful), Toast.LENGTH_LONG).show();
                        return;
                    }
                    isCut = true;
                    for (File file : files) {
                        PasteFile(file, tempTargetFloderDirectory);
                    }
                    openOrBrowseTheFile(nowDirectory);
                    if (mOptionCallBackListener != null)
                        mOptionCallBackListener.optionCallBackListener();
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.move_successful), Toast.LENGTH_LONG).show();
                    break;
                case R.id.btn_cancel:
                    break;
                case R.id.btn_dismiss:
                    break;
            }
            floderPopupWindow.dismiss();
        }
    }


    /**
     * album listview adapter
     */
    public class AlbumAdapter extends BaseAdapter {

        private Context context;
        private List<String> albumNames;
        private boolean isPhoto;

        public AlbumAdapter(Context context, List<String> albumNames, boolean isPhoto) {
            this.context = context;
            this.albumNames = albumNames;
            this.isPhoto = isPhoto;
        }

        @Override
        public int getCount() {
            return albumNames.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_album_item, viewGroup, false);
            final String albumNameString = albumNames.get(i);
            final ImageView albumIcon = (ImageView) view.findViewById(R.id.img_album_icon);
            if (i == 0)
                albumIcon.setBackgroundResource(isPhoto ? R.drawable.icon_photo_album_readonly : R.drawable.icon_video_album_readonly);
            else
                albumIcon.setBackgroundResource(isPhoto ? R.drawable.icon_photo_album : R.drawable.icon_video_album);
            TextView albumName = (TextView) view.findViewById(R.id.txt_album_name);
            final ImageView optionImage = (ImageView) view.findViewById(R.id.btn_show_menu);
            if (Util.isReadOnly)
                optionImage.setVisibility(View.GONE);
            else
                optionImage.setVisibility(View.VISIBLE);
            optionImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View myView) {
                    getPopupWindow(new File(mediaAlbumRootLocation + "" + albumNameString), false);
                    popupWindow.showAsDropDown(myView);
                }
            });
            albumName.setText(albumNameString + " (" + (new File(mediaAlbumRootLocation + "" + albumNameString)).listFiles().length + ")");
            if ((new File(mediaAlbumRootLocation + "" + albumNameString)).listFiles().length > 0) {
                ImageSize mImageSize = new ImageSize(100, 100);
                ImageLoader.getInstance().loadImage("file://" + getLastFileLocation(mediaAlbumRootLocation + "" + albumNameString), mImageSize, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        albumIcon.setImageBitmap(loadedImage);
                    }
                });
            }
            return view;
        }
    }

    /**
     * get the last modified file of provide album from Preference
     *
     * @param albumlocation album path
     * @return
     */
    public String getLastFileLocation(String albumlocation) {
        SharedPreferences mSharedPreferences = getActivity().getSharedPreferences("CONFIG", Context.MODE_PRIVATE);
        String backupUrl = getLastFile(Arrays.asList((new File(albumlocation)).listFiles())).getAbsolutePath();
        String url = mSharedPreferences.getString(albumlocation, backupUrl);
        if ((new File(url)).exists())
            return url;
        else
            return backupUrl;
    }

    /**
     * get the last modified file of provide album from folder
     *
     * @param filelist
     * @return
     */
    public File getLastFile(List<File> filelist) {
        dateIsup = false;
        sortFileList(Marco.FILE_SORT_BY_DATE, filelist);
        if (filelist.isEmpty())
            return null;
        else
            return filelist.get(filelist.size() - 1);
    }

    /**
     * create operation popupWindow
     *
     * @param file
     * @param isDocument
     */
    public void initPopuptWindow(File file, boolean isDocument) {
        View popupWindow_view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_popupwindow, null, false);
        popupWindow = new PopupWindow(popupWindow_view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        if (file.getName().equals(getString(R.string.photo_album)) || file.getName().equals(getString(R.string.video_album)) || file.getName().equals(getString(R.string.my_document))) {
            popupWindow_view.findViewById(R.id.layout_delete).setVisibility(View.GONE);
            popupWindow_view.findViewById(R.id.layout_rename).setVisibility(View.GONE);
            popupWindow_view.findViewById(R.id.layout_move).setVisibility(View.GONE);
        }
        popupWindow_view.findViewById(R.id.layout_rename).setOnClickListener(new PopupWindowClickListener(file));
        popupWindow_view.findViewById(R.id.layout_delete).setOnClickListener(new PopupWindowClickListener(file));
        if (isDocument) {
            popupWindow_view.findViewById(R.id.layout_move).setOnClickListener(new PopupWindowClickListener(file));
        } else {
            popupWindow_view.findViewById(R.id.layout_move).setVisibility(View.GONE);
        }
        if (file.isFile()) {
            popupWindow_view.findViewById(R.id.layout_rename).setVisibility(View.GONE);
        }
        popupWindow.setOutsideTouchable(true);
        popupWindow_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    popupWindow = null;
                }
                return false;
            }
        });
    }

    /**
     * operation popupWindow listener
     */
    public class PopupWindowClickListener implements View.OnClickListener {
        private File file;

        public PopupWindowClickListener(File file) {
            this.file = file;
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.layout_share:
                    break;
                case R.id.layout_privacy:
                    break;
                case R.id.layout_rename:
                    renameByFile(file);
                    break;
                case R.id.layout_delete:
                    deleteFile(file);
                    break;
                case R.id.layout_move:
                    tempFolder = file;
                    getFloderPopupWindow(file);
                    floderPopupWindow.showAtLocation(fileListView, Gravity.BOTTOM, 0, 0);
                    break;
            }
            popupWindow.dismiss();
        }
    }

    private void getPopupWindow(File file, Boolean isDocument) {
        initPopuptWindow(file, isDocument);
    }

    protected void getFloderPopupWindow(File file) {
        List<File> files = new ArrayList<File>();
        files.add(file);
        initFloderListPopuptWindow(files);
    }

    protected void getFloderPopupWindow(File file, String rootLocation) {
        List<File> files = new ArrayList<File>();
        files.add(file);
        initFloderListPopuptWindow(files, rootLocation, true);
    }

    protected void getFloderPopupWindow(List<File> files) {
        initFloderListPopuptWindow(files);
    }

    public void initFloderListPopuptWindow(List<File> files) {
        initFloderListPopuptWindow(files, documentRoot, true);
    }

    private void initFloderListPopuptWindow(List<File> files, final String rootLocation, final boolean isFolder) {
        tempTargetFloderDirectory = new File(rootLocation);
        View popupWindow_view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_folder_listview, null, false);
        floderPopupWindow = new PopupWindow(popupWindow_view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);
        final LinearLayout moveButton = (LinearLayout) popupWindow_view.findViewById(R.id.layout_move);
        moveButton.setOnClickListener(new FolderPopupWindowClickListener(files));
        popupWindow_view.findViewById(R.id.btn_cancel).setOnClickListener(new FolderPopupWindowClickListener(files));
        popupWindow_view.findViewById(R.id.btn_dismiss).setOnClickListener(new FolderPopupWindowClickListener(files));
        floderListView = (ListView) popupWindow_view.findViewById(R.id.lv_floder);
        floderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!floderList.get(i).startsWith(getActivity().getResources().getString(R.string.back)))
                    tempTargetFloderDirectory = new File(tempTargetFloderDirectory.getAbsoluteFile() + "/" + floderList.get(i));
                else
                    tempTargetFloderDirectory = tempTargetFloderDirectory.getParentFile();
                refreshFolder(tempTargetFloderDirectory.getAbsolutePath(), rootLocation, isFolder, moveButton);
            }
        });
        refreshFolder(tempTargetFloderDirectory.getAbsolutePath(), rootLocation, isFolder, moveButton);

    }

    /**
     * refresh folder list
     *
     * @param location
     * @param rootlocation
     * @param isFolder
     * @param moveButton
     */
    public void refreshFolder(String location, String rootlocation, boolean isFolder, LinearLayout moveButton) {
        floderList.clear();
        if (!location.endsWith("/")) {
            location += "/";
        }
        if (!rootlocation.endsWith("/")) {
            rootlocation += "/";
        }
        if (!location.equals(rootlocation)) {
            floderList.add(getActivity().getResources().getString(R.string.back) + " " + (new File(location)).getParentFile().getName());
            if (!isDocument) {
                moveButton.setEnabled(true);
                ((ImageView) moveButton.findViewById(R.id.btn_user)).setImageResource(R.drawable.btn_move_style);
                ((TextView) moveButton.findViewById(R.id.txt_move)).setTextColor(Color.argb(255, 0, 0, 0));
            }
        } else {
            if (!isDocument) {
                moveButton.setEnabled(false);
                ((ImageView) moveButton.findViewById(R.id.btn_user)).setImageResource(R.drawable.tab_move_disable);
                ((TextView) moveButton.findViewById(R.id.txt_move)).setTextColor(Color.argb(255, 199, 199, 199));
            }
        }

        for (File file : (new File(location)).listFiles()) {
            if (isFolder) {
                if (file.isDirectory() && (tempFolder != null ? !tempFolder.getAbsolutePath().equals(file.getAbsolutePath()) : true)) {
                    if (isDocument) {
                        floderList.add(file.getName());
                    } else if (currentLocation != null && !currentLocation.equals(file.getAbsolutePath()))
                        floderList.add(file.getName());
                }
            } else {
                if (file.isFile())
                    floderList.add(file.getName());
            }

        }
        floderListView.setAdapter(new FloderListViewAdapter(floderList));
    }

    /**
     * folder listview adapter
     */
    public class FloderListViewAdapter extends BaseAdapter {

        private List<String> floderList;

        public FloderListViewAdapter(List<String> floderList) {
            this.floderList = floderList;

        }

        @Override
        public int getCount() {
            return floderList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_folder_item, null, false);
            ImageView imageView = (ImageView) view.findViewById(R.id.img_icon);
            if (isDocument) {
                imageView.setImageResource(R.drawable.icon_doc);
            } else {
                if (Util.isPhoto)
                    imageView.setImageResource(R.drawable.icon_photo_album);
                else
                    imageView.setImageResource(R.drawable.icon_video_album);
            }
            TextView folderName = (TextView) view.findViewById(R.id.txt_folder_name);
            folderName.setText(floderList.get(i));
            return view;
        }
    }

    public void setOptionCallBackListener(MediaContentFragment.OptionCallBackListener mOptionCallBackListener) {
        this.mOptionCallBackListener = mOptionCallBackListener;
    }
}
