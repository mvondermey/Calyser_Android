package com.wuala.websocket.view;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.wuala.websocket.R;
import com.wuala.websocket.activity.MainActivity;
import com.wuala.websocket.activity.MainApplication;
import com.wuala.websocket.activity.VideoPlayActivity;
import com.wuala.websocket.callback.SendFileListener;
import com.wuala.websocket.filemanager.util.FileOperaUtil;
import com.wuala.websocket.model.FileItem;
import com.wuala.websocket.util.DisplayUtil;
import com.wuala.websocket.util.Util;

import org.apache.http.protocol.HTTP;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


public class MediaContentFragment extends ContainerBaseFragment implements View.OnClickListener, MainActivity.TakeCallBack {

    private String albumName;
    private final int INTENT_ALBUM = 1001;
    private final int INTENT_TAKEPHOTO = 1000;


    /**
     * file map the key is date sort by month
     */
    private Map<String, List<FileItem>> fileByDateMap;
    /**
     * the content listview
     */
    private StickyListHeadersListView mainListView;
    /**
     * the layout of normal style
     */
    private LinearLayout normalButtonLayout;
    /**
     * the layout of edit style
     */
    private LinearLayout editButtonLayout;
    /**
     * key map for fileByDateMap
     */
    private Map<Integer, String> keyMap = new HashMap<Integer, String>();
    /**
     * checked files
     */
    private List<String> selectedFiles = new ArrayList<String>();
    /**
     * whether or not to select medias
     */
    private boolean enableSelect = false;
    /**
     * file path list
     */
    private List<String> fileURLList = new ArrayList<String>();
    /**
     * share button
     */
    private ImageView btnShare;
    /**
     * move button
     */
    private ImageView btnMove;
    /**
     * delete button
     */
    private ImageView btnDelete;
    /**
     * share button layout
     */
    private RelativeLayout shareLayout;
    /**
     * move button layout
     */
    private RelativeLayout moveLayout;
    /**
     * delete button layout
     */
    private RelativeLayout deleteLayout;
    /**
     * whether or not is select all
     */
    private boolean isSelectAll = false;
    /**
     * the left button of action bar
     */
    private ImageButton btnLeft;
    /**
     * enable run the callback
     */
    private Boolean isCallBack = false;
    /**
     * the right button of action bar
     */
    private ImageButton btnRight;
    /**
     * share text in share layout
     */
    private TextView shareText;
    /**
     * move text in move layout
     */
    private TextView moveText;
    /**
     * delete text in delete layout
     */
    private TextView deleteText;
    /**
     * main liste view adapter
     */
    private ContentListViewAdapter mContentListViewAdapter;
    /**
     * send file listener
     */
    private static SendFileListener mSendFileListener;

    public static MediaContentFragment newInstance(FragmentType type, SendFileListener sendCallback) {
        MainActivity.CURRENT_FRAGMENT = type;
        mSendFileListener = sendCallback;
        MediaContentFragment fragment = new MediaContentFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        albumName = getArguments().getString("albumName");
        currentLocation = MainApplication.CONTAINER_PATH + (Util.isPhoto ? "/photos/" : "/videos/") + albumName;
        View rootView = inflater.inflate(R.layout.fragment_media_content, container, false);
        initActionBar();
        init(rootView);
        return rootView;
    }

    private void init(View rootView) {
        isDocument = false;
        emptyLayout = (LinearLayout) rootView.findViewById(R.id.layout_empty);
        mainListView = (StickyListHeadersListView) rootView.findViewById(R.id.lv_content);
        normalButtonLayout = (LinearLayout) rootView.findViewById(R.id.layout_normal);
        editButtonLayout = (LinearLayout) rootView.findViewById(R.id.layout_edit);
        ((TextView) rootView.findViewById(R.id.txt_take_photo)).setText(Util.isPhoto ? getActivity().getResources().getString(R.string.take_photo) : getActivity().getResources().getString(R.string.take_vedio));
        shareLayout = (RelativeLayout) rootView.findViewById(R.id.layout_share);
        shareLayout.setOnClickListener(this);

        moveLayout = (RelativeLayout) rootView.findViewById(R.id.layout_move);
        moveLayout.setOnClickListener(this);

        deleteLayout = (RelativeLayout) rootView.findViewById(R.id.layout_delete);
        deleteLayout.setOnClickListener(this);

        rootView.findViewById(R.id.layout_import).setOnClickListener(this);
        rootView.findViewById(R.id.layout_take_photo).setOnClickListener(this);
        btnShare = (ImageView) rootView.findViewById(R.id.btn_share);
        btnDelete = (ImageView) rootView.findViewById(R.id.btn_delete);
        btnMove = (ImageView) rootView.findViewById(R.id.btn_move);
        shareText = (TextView) rootView.findViewById(R.id.txt_share);
        moveText = (TextView) rootView.findViewById(R.id.txt_move);
        deleteText = (TextView) rootView.findViewById(R.id.txt_delete);
        enableEditButton(false);
        setTakeCallBack(this);
        refreshList();
        if (Util.isReadOnly)
            rootView.findViewById(R.id.layout_bottom_tools).setVisibility(View.GONE);
        else
            rootView.findViewById(R.id.layout_bottom_tools).setVisibility(View.VISIBLE);
    }

    /**
     * change the style in different status
     *
     * @param enable
     */
    public void enableEditButton(boolean enable) {
        if (!enable) {
            shareLayout.setEnabled(false);
            deleteLayout.setEnabled(false);
            moveLayout.setEnabled(false);
            btnShare.setBackgroundResource(R.drawable.tab_share_disable);
            shareText.setTextColor(Color.argb(255, 199, 199, 199));
            btnMove.setBackgroundResource(R.drawable.tab_move_disable);
            moveText.setTextColor(Color.argb(255, 199, 199, 199));
            btnDelete.setBackgroundResource(R.drawable.tab_delete_disable);
            deleteText.setTextColor(Color.argb(255, 199, 199, 199));
        } else {
            shareLayout.setEnabled(true);
            deleteLayout.setEnabled(true);
            moveLayout.setEnabled(true);
            btnShare.setBackgroundResource(R.drawable.btn_share_style);
            shareText.setTextColor(Color.argb(255, 0, 0, 0));
            btnMove.setBackgroundResource(R.drawable.btn_move_style);
            moveText.setTextColor(Color.argb(255, 0, 0, 0));
            btnDelete.setBackgroundResource(R.drawable.btn_delete_style);
            deleteText.setTextColor(Color.argb(255, 0, 0, 0));
        }
        if ((new File(MainApplication.CONTAINER_PATH + (Util.isPhoto ? "/photos/" : "/videos/")).listFiles().length <= 1)) {
            moveLayout.setEnabled(false);
            btnMove.setBackgroundResource(R.drawable.tab_move_disable);
            moveText.setTextColor(Color.argb(255, 199, 199, 199));
        }
    }

    /**
     * refreash  the main listview
     */
    private void refreshList() {
        selectedFiles.clear();
        getFileDir(currentLocation);
        if (!Util.isReadOnly) {
            if (fileByDateMap.isEmpty())
                emptyLayout.setVisibility(View.VISIBLE);
            else
                emptyLayout.setVisibility(View.GONE);
        } else {
            emptyLayout.setVisibility(View.GONE);
        }
        if (!Util.isPhoto)
            changeEmptyText(emptyLayout);
        if (mContentListViewAdapter == null) {
            mainListView.setAdapter(new ContentListViewAdapter(fileByDateMap));
            mainListView.setDrawingListUnderStickyHeader(true);
            mainListView.setAreHeadersSticky(true);
        } else
            mContentListViewAdapter.notifyDataSetChanged();
        if (isCallBack) {
            File lastFile = getLastFile(Arrays.asList((new File(currentLocation)).listFiles()));
            if (lastFile != null)
                writeToConfig(currentLocation, lastFile.getAbsolutePath());
        }

        if ((new File(currentLocation)).listFiles().length == 0) {
            btnRight.setEnabled(false);
            btnRight.setImageResource(R.drawable.btn_edit_disable);
        } else {
            btnRight.setEnabled(true);
        }
    }

    /**
     * set the empty layout by photo or video
     *
     * @param linearLayout
     */
    public void changeEmptyText(LinearLayout linearLayout) {
        String mainString = ((TextView) linearLayout.findViewById(R.id.txt_main_text)).getText().toString();
        mainString = mainString.replace("photo", "video");
        mainString = mainString.replace("Photo", "Video");
        ((TextView) linearLayout.findViewById(R.id.txt_main_text)).setText(mainString);
        ((TextView) linearLayout.findViewById(R.id.txt_firsrt)).setText(((TextView) linearLayout.findViewById(R.id.txt_firsrt)).getText().toString().replace("photo", "video"));
        ((TextView) linearLayout.findViewById(R.id.txt_second)).setText(((TextView) linearLayout.findViewById(R.id.txt_second)).getText().toString().replace("photo", "video"));
        ((TextView) linearLayout.findViewById(R.id.txt_third)).setText(((TextView) linearLayout.findViewById(R.id.txt_third)).getText().toString().replace("photo", "video"));
    }

    /**
     * init action bar
     */
    private void initActionBar() {
        setActionBarTitle(albumName);

        btnLeft = getLeftButton();
        btnLeft.setVisibility(View.VISIBLE);
        btnLeft.setImageResource(R.drawable.btn_back_style);
        final File currentFile = new File(currentLocation);
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (enableSelect) {
                    refreshList();
                    if (isSelectAll) {
                        btnLeft.setImageResource(R.drawable.btn_select_all);
                        setActionBarTitle("Select " + (Util.isPhoto ? "Photo(s)" : "Video(s)"));
                        enableEditButton(false);
                        isSelectAll = false;
                    } else {
                        btnLeft.setImageResource(R.drawable.btn_unselect_all);
                        setActionBarTitle(currentFile.listFiles().length + (Util.isPhoto ? " Photo(s)" : "Video(s)") + " selected");
                        enableEditButton(true);
                        isSelectAll = true;
                    }

                } else {
                    onBackKeyDown();
                }
            }
        });

        btnRight = getRightButton();
        if (Util.isReadOnly) {
            btnRight.setVisibility(View.INVISIBLE);
        } else {
            btnRight.setVisibility(View.VISIBLE);
            btnLeft.setVisibility(View.VISIBLE);
        }
        btnRight.setImageResource(R.drawable.btn_edit);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnbtnRightClicked();
            }
        });

    }

    /**
     * the function of after right button of action bar clicked
     */
    public void OnbtnRightClicked() {
        isSelectAll = false;
        if (normalButtonLayout.getVisibility() == View.VISIBLE) {
            btnRight.setImageResource(R.drawable.btn_cancel);
            normalButtonLayout.setVisibility(View.GONE);
            editButtonLayout.setVisibility(View.VISIBLE);
            btnLeft.setImageResource(R.drawable.btn_select_all);
            enableSelect = true;

        } else {
            btnLeft.setImageResource(R.drawable.btn_back_style);
            btnRight.setImageResource(R.drawable.btn_edit);
            normalButtonLayout.setVisibility(View.VISIBLE);
            editButtonLayout.setVisibility(View.GONE);
            enableSelect = false;
            setActionBarTitle(albumName);
        }
        isCallBack = true;
        refreshList();
        isCallBack = false;
    }

    /**
     * open the the gallary of android system
     */
    private void openAlbum() {
        Intent intent = new Intent();
        intent.setType(Util.isPhoto ? "image/*" : "video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, INTENT_ALBUM);
    }

    /**
     * get photo or video location
     *
     * @param uri
     * @param filePathColumn
     * @return
     */
    public String getLocation(Uri uri, String[] filePathColumn) {
        Cursor cursor = getActivity().getContentResolver().query(uri,
                filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        return cursor.getString(columnIndex);
    }

    /**
     * import file
     */
    private void getFileDir(String path) {
        fileURLList = new ArrayList<String>();
        File preFile = null;
        fileByDateMap = new LinkedHashMap<String, List<FileItem>>();
        List<FileItem> mFileItems = new ArrayList<FileItem>();
        File f = new File(path);
        List<File> files = Arrays.asList(f.listFiles());
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                Date date1 = new Date(lhs.lastModified());
                Date date2 = new Date(rhs.lastModified());
                if (date1.before(date2)) {
                    return 1;
                }
                return -1;
            }
        });
        if (files.size() > 0) {
            int i = 0;
            for (File sf : files) {
                if (preFile == null || !areSameMonth(new Date(preFile.lastModified()), new Date(sf.lastModified()))) {
                    preFile = sf;
                    mFileItems = new ArrayList<FileItem>();
                    String date = String.format("%tB", new Date(sf.lastModified())) + " " + String.format("%tY", new Date(sf.lastModified()));
                    keyMap.put(i, date);
                    fileByDateMap.put(date, mFileItems);
                    i++;
                }
                if (sf.isFile()) {
                    FileItem item = new FileItem();
                    item.setID(System.currentTimeMillis() + "");
                    item.setFileName(sf.getName());
                    String filename = sf.getName();
                    String filepath = sf.getAbsolutePath().replace(MainApplication.CONTAINER_PATH + "/", "").replace(filename, "");
                    String prefix = filename.substring(filename.lastIndexOf(".") + 1);
                    item.setSize(formatFileSize(sf.length()));
                    item.setTotalSize(sf.length());
                    item.setPrefix(prefix);

                    item.setUrl(convert("https://" + getIPAddress() + ":" + MainApplication.HTTP_PORT + "/" + filepath + filename));
                    item.setFile(true);
                    fileURLList.add(currentLocation + "/" + item.getFileName());
                    mFileItems.add(item);

                }
            }
        }
    }


    /**
     * check whether or not the two date in same month
     *
     * @param dateA
     * @param dateB
     * @return
     */
    public boolean areSameMonth(Date dateA, Date dateB) {
        Calendar calDateA = Calendar.getInstance();
        calDateA.setTime(dateA);

        Calendar calDateB = Calendar.getInstance();
        calDateB.setTime(dateB);

        return calDateA.get(Calendar.YEAR) == calDateB.get(Calendar.YEAR)
                && calDateA.get(Calendar.MONTH) == calDateB.get(Calendar.MONTH);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_import:
                openAlbum();
                break;
            case R.id.layout_take_photo:
                takePhoto(currentLocation);
                break;
            case R.id.layout_share:
                break;
            case R.id.layout_move:
                isDocument = false;
                documentRoot = Util.isPhoto ? MainApplication.CONTAINER_PATH + "/photos/" : MainApplication.CONTAINER_PATH + "/videos";
                List<File> files = new ArrayList<File>();
                for (String str : selectedFiles)
                    files.add(new File(str));
                getFloderPopupWindow(files);
                floderPopupWindow.showAtLocation(mainListView, Gravity.TOP, 0, 0);
                setOptionCallBackListener(new OptionCallBackListener() {
                    @Override
                    public void optionCallBackListener() {
                        refreshList();
                    }
                });
                enableSelect = false;
                OnbtnRightClicked();
                break;
            case R.id.layout_delete:
                enableSelect = false;
                deleteFiles();

                break;
        }
    }

    /**
     * delete files
     */
    public void deleteFiles() {
        final List<File> urlList = new ArrayList<File>();
        for (String str : selectedFiles)
            urlList.add(new File(str));
        String title = "Delete " + (Util.isPhoto ? "Photo" : "Video");
        String text = getActivity().getResources().getString(R.string.are_you_sure_you_want_to_delete_the_selected) + (Util.isPhoto ? " photo(s)" : " video(s)") + "?";
        confirmDialog(title, text,
                new View.OnClickListener() {
                    public void onClick(View view) {
                        showImportLoadingDialog("Deleting", getActivity().getResources().getString(R.string.delete_file_please_wait));

                        (new AsyncTask<Void, Void, Boolean>() {
                            @Override
                            protected Boolean doInBackground(Void... voids) {
                                for (File file : urlList)
                                    try {
                                        FileOperaUtil.deleteAll(file);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                return true;
                            }

                            @Override
                            protected void onPostExecute(Boolean aBoolean) {
                                mProgressDialog.cancel();
                                OnbtnRightClicked();
                            }
                        }).execute();
                    }
                });
    }

    @Override
    public void takeCallBack(int requestCode, int resultCode, Intent data) {
        if (requestCode != INTENT_TAKEPHOTO) {
            final Uri uri = data.getData();
            confirmDialog("Import", "Do you want to import this " + (Util.isPhoto ? "Photo" : "Video"),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showImportLoadingDialog(getString(R.string.loading), getString
                                    (R.string.import_file));
                            (new AsyncTask<Void, Void, Boolean>() {
                                @Override
                                protected Boolean doInBackground(Void... voids) {
                                    try {
                                        String[] filePathColumn;
                                        if (Util.isPhoto) {
                                            filePathColumn = new String[]{MediaStore.Images.Media.DATA};
                                        } else {
                                            filePathColumn = new String[]{MediaStore.Video.Media.DATA};
                                        }
                                        isCut = false;
                                        myTmpFile = new File(getLocation(uri, filePathColumn));
                                        nowDirectory = new File(currentLocation);
                                        PasteFile();
                                        getFileDir(currentLocation);
                                    } catch (Exception e) {
                                        try {
                                            if (uri != null) {
                                                ContentResolver resolver = getActivity().getContentResolver();
                                                if (Util.isPhoto) {
                                                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(resolver, uri);
                                                    if (bitmap != null) {
                                                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(currentLocation + "/" + System.currentTimeMillis() + ".jpg")));
                                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                                                        bos.flush();
                                                        bos.close();
                                                    }
                                                } else {
                                                    FileOutputStream fileOutputStream = new FileOutputStream(new File(currentLocation + "/" + System.currentTimeMillis() + ".mp4"));
                                                    InputStream inputStream = resolver.openInputStream(uri);
                                                    byte[] buf = new byte[2048];
                                                    int count;
                                                    while ((count = inputStream.read(buf)) != -1) {
                                                        fileOutputStream.write(buf, 0, count);
                                                    }
                                                    fileOutputStream.flush();
                                                    inputStream.close();
                                                    fileOutputStream.close();
                                                }
                                            }
                                        } catch (IOException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                    return true;
                                }

                                @Override
                                protected void onPostExecute(Boolean aBoolean) {
                                    isCallBack = true;
                                    mProgressDialog.cancel();
                                    refreshList();
                                    isCallBack = false;
                                    if ((new File(currentLocation)).listFiles().length == 0) {
                                        btnRight.setEnabled(false);
                                        btnRight.setImageResource(R.drawable.btn_edit_disable);
                                    } else {
                                        btnRight.setEnabled(true);
                                        btnRight.setImageResource(R.drawable.btn_edit);
                                    }
                                }
                            }).execute();
                        }
                    });
        } else {
            isCallBack = true;
            refreshList();
            isCallBack = false;
            if ((new File(currentLocation)).listFiles().length == 0) {
                btnRight.setEnabled(false);
                btnRight.setImageResource(R.drawable.btn_edit_disable);
            } else {
                btnRight.setEnabled(true);
                btnRight.setImageResource(R.drawable.btn_edit);
            }
        }
    }

    /**
     * write the lastModified file to preferences
     *
     * @param albumLocation
     * @param lastFileLocation
     */
    public void writeToConfig(String albumLocation, String lastFileLocation) {
        SharedPreferences mSharedPreferences = getActivity().getSharedPreferences("CONFIG", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(albumLocation, lastFileLocation);
        editor.commit();
    }


    public class ContentListViewAdapter extends BaseAdapter implements StickyListHeadersAdapter {

        private Map<String, List<FileItem>> fileByDateMap;

        public ContentListViewAdapter(Map<String, List<FileItem>> fileByDateMap) {
            this.fileByDateMap = fileByDateMap;
        }


        @Override
        public int getCount() {
            return fileByDateMap.size();
        }

        @Override
        public Object getItem(int i) {
            return fileByDateMap.get(keyMap.get(i));
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_content_item, null, false);
            String key = keyMap.get(i);
            List<FileItem> fileItemList = fileByDateMap.get(key);
            GridView gridView = (GridView) view.findViewById(R.id.gv_content);
            gridView.setAdapter(new ContentGridItemAdapter(getActivity(), fileItemList, Util.isPhoto));
            int imageWidth = 0;
            imageWidth = Util.screenWidth / 4 + DisplayUtil.dip2px(getActivity(), 10);
            gridView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (imageWidth * Math.ceil((fileItemList.size() / 4.0)))));
            return view;
        }


        @Override
        public View getHeaderView(int i, View view, ViewGroup viewGroup) {
            TextView textView = new TextView(getActivity());
            textView.setBackgroundColor(Color.WHITE);
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setPadding(DisplayUtil.dip2px(getActivity(), 10), 0, 0, 0);

            String key = keyMap.get(i);
            textView.setText(key);
            return textView;
        }

        @Override
        public long getHeaderId(int i) {
            return i;
        }

    }

    static class ViewHolder {
        ImageView imageItem;
        CheckBox selcetBox;
        ImageView playButton;
    }

    public class ContentGridItemAdapter extends BaseAdapter {

        private Context context;
        private List<FileItem> mFileItems;
        private boolean isPhoto;

        public ContentGridItemAdapter(Context context, List<FileItem> mFileItems, boolean isPhoto) {
            this.context = context;
            this.mFileItems = mFileItems;
            this.isPhoto = isPhoto;
        }

        @Override
        public int getCount() {
            return mFileItems.size();
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
        public View getView(final int i, View view, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.layout_content_grid_item, viewGroup, false);
                holder = new ViewHolder();
                holder.imageItem = (ImageView) view.findViewById(R.id.img_content);
                if (!Util.isPhoto)
                    holder.imageItem.setImageResource(R.drawable.icon_video_temp);
                holder.selcetBox = (CheckBox) view.findViewById(R.id.cb_select);
                holder.playButton = (ImageView) view.findViewById(R.id.img_play);
                int imageWidth = 0;
                imageWidth = Util.screenWidth / 4 - DisplayUtil.dip2px(getActivity(), 5);
                holder.imageItem.setLayoutParams(new RelativeLayout.LayoutParams(imageWidth, imageWidth));
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            final FileItem fileItem = mFileItems.get(i);
            ImageSize mImageSize = new ImageSize(100, 100);
            if (isPhoto) {
                holder.playButton.setVisibility(View.GONE);
                final ViewHolder finalHolder1 = holder;
                ImageLoader.getInstance().loadImage("file://" + currentLocation + "/" + fileItem.getFileName(), mImageSize, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View myView,
                                                  Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, myView, loadedImage);
                        finalHolder1.imageItem.setImageBitmap(loadedImage);
                    }
                });
            } else {
                holder.playButton.setVisibility(View.VISIBLE);
                final ViewHolder finalHolder2 = holder;
                ImageLoader.getInstance().loadImage("file://" + currentLocation + "/" + mFileItems.get(i).getFileName(), mImageSize, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View myView,
                                                  Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, myView, loadedImage);
                        finalHolder2.imageItem.setImageBitmap(loadedImage);
                    }
                });
                holder.playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (Util.isReadOnly) {
                            mSendFileListener.onFileSend(fileItem);
                            getFragmentManager().popBackStack();
                            getFragmentManager().popBackStack();
                            getFragmentManager().popBackStack();
                            return;
                        }
                        Intent intent = new Intent(getActivity(), VideoPlayActivity.class);
                        intent.putExtra("url", currentLocation + "/" + mFileItems.get(i).getFileName());
                        getActivity().startActivity(intent);
                    }
                });
            }
            final ViewHolder finalHolder = holder;
            finalHolder.imageItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Util.isReadOnly) {
                        mSendFileListener.onFileSend(fileItem);
                        getActivity().getSupportFragmentManager().popBackStack();
                        getActivity().getSupportFragmentManager().popBackStack();
                        getActivity().getSupportFragmentManager().popBackStack();
                        return;
                    }
                    if (enableSelect) {
                        isSelectAll = false;
                        btnLeft.setImageResource(R.drawable.btn_select_all);
                        finalHolder.selcetBox.setChecked(finalHolder.selcetBox.isChecked() ? false : true);
                    } else if (isPhoto) {
                        openMyGalleryView(null, fileURLList, currentLocation + "/" + fileItem.getFileName());
                        ((RelativeLayout) btnRight.getParent()).setVisibility(View.GONE);
                    }
                }
            });
            holder.selcetBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        selectedFiles.add(currentLocation + "/" + fileItem.getFileName());
                    } else {
                        selectedFiles.remove(currentLocation + "/" + fileItem.getFileName());
                    }
                    if (!isSelectAll)
                        if (selectedFiles.isEmpty()) {
                            setActionBarTitle(context.getString(R.string.select) + " " + (Util.isPhoto ? context.getString(R.string.photos) : context.getString(R.string.videos)));
                            enableEditButton(false);
                        } else {
                            setActionBarTitle(selectedFiles.size() + " " + (Util.isPhoto ? " " + context.getString(R.string.photos) : context.getString(R.string.videos)) + " " + context.getString(R.string.selected));
                            enableEditButton(true);
                        }
                    if (selectedFiles.size() == mFileItems.size()) {
                        btnLeft.setImageResource(R.drawable.btn_unselect_all);
                        isSelectAll = true;
                        setActionBarTitle(selectedFiles.size() + " " + (Util.isPhoto ? " " + context.getString(R.string.photos) : context.getString(R.string.videos)) + " " + context.getString(R.string.selected));
                    } else {
                        btnLeft.setImageResource(R.drawable.btn_select_all);
                    }
                }
            });
            if (enableSelect) {
                holder.selcetBox.setVisibility(View.VISIBLE);
                holder.playButton.setVisibility(View.GONE);
                if (isSelectAll) {
                    holder.selcetBox.setChecked(true);
                } else {
                    holder.selcetBox.setChecked(false);
                }
            } else {
                setActionBarTitle(context.getString(R.string.select) + (Util.isPhoto ? context.getString(R.string.photos) : context.getString(R.string.videos)));
                setActionBarTitle(albumName);
                holder.selcetBox.setVisibility(View.GONE);
                if (!isPhoto)
                    holder.playButton.setVisibility(View.VISIBLE);
            }
            return view;
        }

    }

    public interface OptionCallBackListener {
        public void optionCallBackListener();
    }

    @Override
    public boolean onBackKeyDown() {
        try {
            getFragmentManager().popBackStack();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onBackKeyDown();
    }
}
