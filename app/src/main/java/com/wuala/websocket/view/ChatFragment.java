package com.wuala.websocket.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.wuala.websocket.R;
import com.wuala.websocket.activity.MainActivity;
import com.wuala.websocket.activity.MainApplication;
import com.wuala.websocket.callback.SendFileListener;
import com.wuala.websocket.httpserver.DownloadManager;
import com.wuala.websocket.model.ChatMessage;
import com.wuala.websocket.model.FileItem;
import com.wuala.websocket.model.User;
import com.wuala.websocket.util.Marco;
import com.wuala.websocket.util.Util;

import org.java_websocket.WebSocket;

import java.io.File;
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.List;


public class ChatFragment extends BaseFragment implements View.OnClickListener, SendFileListener {

    private ListView mListView;
    public static List<ChatMessage> mChatMessages;
    private MessageAdapter mAdapter;

    private EditText mEdtMsg;
    private Button mBtnSend;

    private Button mBtnFile;

    private String mUsername;

    private final int HMSG_CONFIRM_SHOW = 1;
    private final int HMSG_REFRESH_LIST = 2;
    private final int HMSG_CLIENT_CLOSE = 3;
    private final int HMSG_SERVER_CLOSE = 4;
    private final int HMSG_REFRESH_TITLE = 5;

    private static boolean mIsServer;

    private DownloadManager mDownloadManager;

    private Gson mGson;

    private String mTitle = "";

    private int rootBottom = Integer.MIN_VALUE;

    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(FragmentType type, boolean isServer) {
        MainActivity.CURRENT_FRAGMENT = type;
        ChatFragment fragment = new ChatFragment();
        mIsServer = isServer;
        return fragment;
    }

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.isReadOnly = true;
        mTitle = getTitleView().getText().toString();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(MainApplication.TAG, Context.MODE_PRIVATE);
        mUsername = sharedPreferences.getString(Marco.SP_USERNAME, "");
        mDownloadManager = DownloadManager.getInstance(getActivity());
        mDownloadManager.setHandler(downloadHandler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        // set chat view title
        if (mIsServer) {
            setClientTitle();
            mTitle = getTitle();
        }
        initActionBar();

        initMessageArray();
        initView(rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.CURRENT_FRAGMENT = FragmentType.FRAGMENT_CHAT;
        setCallbacks();
        getTitleView().setFocusableInTouchMode(true);
        getTitleView().setFocusable(true);
        getTitleView().requestFocus();
        mListView.setSelection(mChatMessages.size() - 1);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void initMessageArray() {
        if (ChatFragment.mChatMessages == null)
            mChatMessages = new ArrayList<ChatMessage>();
        mAdapter = new MessageAdapter(getActivity());
    }

    /**
     * init view by id
     *
     * @param view
     */
    private void initView(final View view) {
        mEdtMsg = (EditText) view.findViewById(R.id.edt_msg);
        mBtnSend = (Button) view.findViewById(R.id.btn_send);
        mBtnSend.setOnClickListener(this);

        mBtnFile = (Button) view.findViewById(R.id.btn_file);
        mBtnFile.setOnClickListener(this);

        mListView = (ListView) view.findViewById(R.id.list_msg_history);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        mEdtMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mListView.setSelection(mListView.getBottom());
                    }
                }, 500);

            }
        });
    }

    /**
     * init action bar
     */
    private void initActionBar() {
        Log.e(MainApplication.TAG, "mTitle = " + mTitle);
        if (!TextUtils.isEmpty(mTitle)) {
            setActionBarTitle(mTitle);
        }
        ImageButton btnLeft = getLeftButton();
        btnLeft.setVisibility(View.VISIBLE);
        btnLeft.setImageResource(R.drawable.btn_back_style);
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapseSoftInputMethod(mEdtMsg);
                showConfirmDialog();
            }
        });
        ImageButton btnRight = getRightButton();
        btnRight.setVisibility(View.INVISIBLE);
    }

    /**
     * send message
     *
     * @param message
     */
    private void sendMsg(ChatMessage message) {
        message.setName(mUsername);
        message.setDate(Util.getTime());
        message.setFrom(ChatMessage.MESSAGE_TYPE_ME);

        String msg = mGson.toJson(message);

        if (mIsServer) {
            for (WebSocket conn : getUsers().keySet()) {
                conn.send(msg);
            }
        } else {
            if (getClient() != null) {
                getClient().send(msg);
            }
        }
        addMessageToList(message);
        mAdapter.notifyDataSetChanged();
        mListView.setSelection(mListView.getBottom());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                if (isNetworkConnected()) {
                    String msg = mEdtMsg.getText().toString();
                    if (msg.trim().length() > 0) {
                        ChatMessage message = new ChatMessage();
                        message.setMessage(new String(msg));
                        message.setType(Marco.MSG_TEXT_MESSAGE);
                        sendMsg(message);
                        mEdtMsg.setText("");
                    } else {
                        MainApplication.notice.showToast(getString(R.string.empty_message));
                    }
                } else {
                    MainApplication.notice.showToast(getString(R.string.network_error));
                }

                break;
            case R.id.btn_file:
                collapseSoftInputMethod(mEdtMsg);
                Util.isReadOnly = true;
                openContainerView(this);
                break;
        }
    }

    @Override
    public boolean onBackKeyDown() {
        showConfirmDialog();
        return super.onBackKeyDown();
    }

    /**
     * show request confirm dialog
     */
    private void showConfirmDialog() {
        final Dialog dialog = new Dialog(getActivity(), R.style.dialog);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View convertView = inflater.inflate(R.layout.dialog_confirm, null);
        dialog.setContentView(convertView);
        TextView txtView = (TextView) convertView.findViewById(R.id.txt_content);
        if (mIsServer) {
            txtView.setText(getString(R.string.close_server_confirm));
        } else {
            txtView.setText(getString(R.string.close_client_confirm));
        }

        TextView txtTitle = (TextView) convertView.findViewById(R.id.txt_title);
        txtTitle.setText(getString(R.string.confirm));
        Button btnYes = (Button) convertView.findViewById(R.id.btn_yes);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mIsServer) {
                    for (WebSocket conn : getUsers().keySet()) {
                        conn.close();
                    }
                    getUsers().clear();
                } else {
                    closeClient();
                }
                setActionBarTitle(String.format(getString(R.string.welcome), mUsername));
                getFragmentManager().popBackStack();
                Util.isReadOnly = false;
                dialog.cancel();
            }
        });
        Button btnCancel = (Button) convertView.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    @Override
    public void onServerMessage(WebSocket conn, String s) {
        super.onServerMessage(conn, s);
        Log.e(MainApplication.TAG, "msg = " + s);
        // send all user
        ChatMessage msgChat = mGson.fromJson(s, ChatMessage.class);
        if (msgChat.getType().equals(Marco.MSG_REQUEST_CONNECT)) {
            Message hMsg = mHandler.obtainMessage();
            hMsg.what = HMSG_CONFIRM_SHOW;
            Object[] objs = {conn, msgChat};
            hMsg.obj = objs;
            mHandler.sendMessage(hMsg);
        } else {
            for (WebSocket wconn : getUsers().keySet()) {
                if (wconn != conn) {
                    wconn.send(s);
                }
            }
            ChatMessage msg = mGson.fromJson(s, ChatMessage.class);
            msg.setFrom(ChatMessage.MESSAGE_TYPE_OTHER);
            addMessageToList(msg);
            Message hMsg = mHandler.obtainMessage();
            hMsg.what = HMSG_REFRESH_LIST;
            mHandler.sendMessage(hMsg);
        }
    }

    @Override
    public void onClientMessage(String s) {
        super.onClientMessage(s);
        Log.e(MainApplication.TAG, "msg = " + s);
        ChatMessage msg = mGson.fromJson(s, ChatMessage.class);
        msg.setFrom(ChatMessage.MESSAGE_TYPE_OTHER);
        if (msg.getType().equals(Marco.MSG_SYSTEM_MESSAGE)) {
            msg.setFrom(ChatMessage.MESSAGE_TYPE_SYSTEM);
        }

        Message hMsg = mHandler.obtainMessage();
        if (msg.getType().equals(Marco.MSG_REFRESH_TITLE)) {
            mTitle = msg.getMessage();
            hMsg.what = HMSG_REFRESH_TITLE;
        } else {
            addMessageToList(msg);
            hMsg.what = HMSG_REFRESH_LIST;
        }
        mHandler.sendMessage(hMsg);
    }

    /**
     * server get client close information
     *
     * @param conn
     * @param code
     * @param reason
     * @param remote
     */
    @Override
    public void onServerClose(WebSocket conn, int code, String reason, boolean remote) {
        super.onServerClose(conn, code, reason, remote);
        // remove the client who close socket
        removeClient(conn);
        Message hMsg = mHandler.obtainMessage();
        hMsg.what = HMSG_CLIENT_CLOSE;
        mHandler.sendMessage(hMsg);
    }

    @Override
    public void onClientClose(int i, String s, boolean b) {
        super.onClientClose(i, s, b);
        Message hMsg = mHandler.obtainMessage();
        hMsg.what = HMSG_SERVER_CLOSE;
        mHandler.sendMessage(hMsg);
    }

    private void addMessageToList(ChatMessage message) {
        message.setIndex(mChatMessages.size());
        if (message.getFileItem() != null) {
            message.getFileItem().setIndex(message.getIndex());
        }
        mChatMessages.add(message);
    }

    /**
     * ui handle
     */
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HMSG_CONFIRM_SHOW:
                    if (getActivity() != null) {
                        Object[] objects = (Object[]) msg.obj;
                        final WebSocket socket = (WebSocket) objects[0];
                        final ChatMessage message = (ChatMessage) objects[1];
                        final Dialog dialog = new Dialog(getActivity(), R.style.dialog);
                        View.OnClickListener yesLis = new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                socket.send(createConfirmMessage(true));
                                // create a map to save user info
                                addClient(message.getName(), socket);
                                dialog.cancel();
                            }
                        };

                        View.OnClickListener noLis = new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                socket.send(createConfirmMessage(false));
                                dialog.cancel();
                            }
                        };
                        showConfirmDialog(dialog, message.getName(), yesLis, noLis);
                    }
                    break;
                case HMSG_REFRESH_LIST:
                    refreshListView();
                    break;
                case HMSG_REFRESH_TITLE:
                    initActionBar();
                    break;
                case HMSG_CLIENT_CLOSE:
                    refreshListView();
                    mTitle = getTitle();
                    initActionBar();
                    break;
                case HMSG_SERVER_CLOSE:
                    showCloseDialog();
                    break;
            }
        }
    };

    /**
     * refresh list view
     */
    private void refreshListView() {
        mAdapter.notifyDataSetChanged();
        mListView.setSelection(mListView.getBottom());
    }

    /**
     * download handler
     */
    private Handler downloadHandler = new Handler() {
        public void handleMessage(Message msg) {
            FileItem downloadFile = (FileItem) msg.obj;
            // update item
            updateView(downloadFile);
        }
    };

    /**
     * update item view
     *
     * @param fileItem
     */
    private void updateView(FileItem fileItem) {
        int visiblePos = mListView.getFirstVisiblePosition();
        int offset = fileItem.getIndex() - visiblePos;
        // update item that just shown
        if (offset < 0) return;
        View view = mListView.getChildAt(offset);
        if (fileItem == null) {
            return;
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        view.setVisibility(View.VISIBLE);
        final TextView txtStatus = (TextView) holder.loadingLayout.findViewById(R.id.txt_status);
        final ProgressBar progressBar = (ProgressBar) holder.loadingLayout.findViewById(R.id.progress);

        switch (fileItem.getDownloadState()) {
            case DownloadManager.DOWNLOAD_STATE_FINISH:
                progressBar.setVisibility(View.GONE);
                txtStatus.setVisibility(View.VISIBLE);
                txtStatus.setText(getString(R.string.finished));
                refreshListView();
                mListView.setSelection(mListView.getBottom());
                break;
            case DownloadManager.DOWNLOAD_STATE_DOWNLOADING:
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress((int) (fileItem.getDownloadSize() * 100.0f / fileItem.getTotalSize()));
                txtStatus.setVisibility(View.GONE);
//                mListView.setSelection(mListView.getBottom());
                break;
        }
    }

    /**
     * show close chat dialog (if server close clients will show the dialog)
     */
    private void showCloseDialog() {
        final Dialog dialog = new Dialog(getActivity(), R.style.dialog);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View convertView = inflater.inflate(R.layout.dialog_notice, null);
        dialog.setContentView(convertView);
        TextView txtView = (TextView) convertView.findViewById(R.id.txt_content);
        txtView.setText(getString(R.string.server_close));
        TextView txtTitle = (TextView) convertView.findViewById(R.id.txt_title);
        txtTitle.setText(getString(R.string.warn));
        Button btnYes = (Button) convertView.findViewById(R.id.btn_yes);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                if (getFragmentManager() == null) {
                    return;
                }
                getFragmentManager().popBackStack();
            }
        });
        dialog.show();
    }

    @Override
    public void onFileSend(final FileItem item) {
        ChatMessage message = new ChatMessage();
        message.setType(Marco.MSG_FILE_MESSAGE);
        message.setFileItem(item);
        message.setMessage(getString(R.string.share_file, item.getFileName()));
        sendMsg(message);
    }

    @Override
    public void onCloseContainer() {
        MainActivity.mTopFragment = this;
        initActionBar();
    }

    /**
     * Message Adapter
     */
    private class MessageAdapter extends BaseAdapter {
        private LayoutInflater li;

        public MessageAdapter(Context context) {
            super();
            li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            ChatMessage msg = mChatMessages.get(position);

            final ViewHolder holder = new ViewHolder();

            if (msg.getFrom() == ChatMessage.MESSAGE_TYPE_ME) {
                convertView = li.inflate(R.layout.layout_message_me, null);
            } else if (msg.getFrom() == ChatMessage.MESSAGE_TYPE_OTHER) {
                convertView = li.inflate(R.layout.layout_message_other, null);
            } else {
                convertView = li.inflate(R.layout.layout_message_sys, null);
            }
            holder.loadingLayout = (RelativeLayout) convertView.findViewById(R.id.loading_layout);
            holder.imgCover = (ImageView) convertView.findViewById(R.id.img_cover);
            holder.imgPlay = (ImageView) convertView.findViewById(R.id.img_play);
            holder.txtMessage = (TextView) convertView.findViewById(R.id.txt_text);
            holder.txtName = (TextView) convertView.findViewById(R.id.txt_name);
            convertView.setTag(holder);
            holder.txtMessage.setText("" + msg.getMessage());
            holder.txtName.setText(msg.getName() + ":");

            if (msg.getType().equals(Marco.MSG_FILE_MESSAGE)) {
                final FileItem item = msg.getFileItem();
                if (item != null) {
                    if (msg.getFrom() == ChatMessage.MESSAGE_TYPE_OTHER) {
                        holder.txtMessage.setText(msg.getName() + " " + msg.getMessage() + "\n" + item.getSize());
                        holder.loadingLayout.setVisibility(View.VISIBLE);
                        final TextView txtStatus = (TextView) holder.loadingLayout.findViewById(R.id.txt_status);
                        final ProgressBar progressBar = (ProgressBar) holder.loadingLayout.findViewById(R.id.progress);
                        if (item.getDownloadState() == DownloadManager.DOWNLOAD_STATE_DOWNLOADING) {
                            txtStatus.setVisibility(View.GONE);
                            progressBar.setVisibility(View.VISIBLE);
                            progressBar.setProgress((int) (item.getDownloadSize() * 100.0f / item.getTotalSize()));
                        } else if (item.getDownloadState() == DownloadManager.DOWNLOAD_STATE_FINISH) {
                            txtStatus.setVisibility(View.VISIBLE);
                            txtStatus.setText(getString(R.string.finished));
                            progressBar.setVisibility(View.GONE);
                        } else if (item.getDownloadState() == DownloadManager.DOWNLOAD_STATE_NORMAL) {
                            txtStatus.setVisibility(View.VISIBLE);
                            txtStatus.setText(getString(R.string.click_download));
                            progressBar.setVisibility(View.GONE);
                        } else if (item.getDownloadState() == DownloadManager.DOWNLOAD_STATE_WAITING) {
                            txtStatus.setVisibility(View.VISIBLE);
                            txtStatus.setText(getString(R.string.waiting));
                            progressBar.setVisibility(View.GONE);
                        } else if (item.getDownloadState() == DownloadManager.DOWNLOAD_STATE_PAUSE) {
                            txtStatus.setVisibility(View.VISIBLE);
                            txtStatus.setText(getString(R.string.pause));
                            progressBar.setVisibility(View.GONE);
                        }
                        holder.imgCover.setVisibility(View.VISIBLE);
                        holder.imgCover.setImageResource(getResourceId(item));
                        holder.imgPlay.setVisibility(View.GONE);
                        setChatImage(item, holder);
                        convertView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                holder.loadingLayout.setVisibility(View.VISIBLE);
                                if (item.getDownloadState() == DownloadManager.DOWNLOAD_STATE_NORMAL || item.getDownloadState() == DownloadManager.DOWNLOAD_STATE_PAUSE) {
                                    item.setDownloadState(DownloadManager.DOWNLOAD_STATE_WAITING);
                                    txtStatus.setVisibility(View.GONE);
                                    txtStatus.setText(getString(R.string.waiting));
                                    mDownloadManager.startDownload(item);
                                } else if (item.getDownloadState() == DownloadManager.DOWNLOAD_STATE_FINISH) {
                                    openFile(getActivity(), item);
                                }
                            }
                        });
                    } else if (msg.getFrom() == ChatMessage.MESSAGE_TYPE_ME) {
                        holder.imgCover.setVisibility(View.VISIBLE);
                        holder.imgCover.setImageResource(getResourceId(item));
                        setChatImage(item, holder);
                        holder.txtMessage.setText("You " + msg.getMessage() + "\n" + item.getSize());
                        convertView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                openFile(getActivity(), item);
                            }
                        });
                    }
                }
            }
            return convertView;
        }

        public void setChatImage(final FileItem fileItem, final ViewHolder holder) {
            String previewImageUrl = getPreviewImage(fileItem);
            if (previewImageUrl != null) {
                ImageSize mImageSize = new ImageSize(100, 100);
                ImageLoader.getInstance().loadImage("file://" + previewImageUrl, mImageSize, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View myView,
                                                  Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, myView, loadedImage);
                        holder.imgCover.setImageBitmap(loadedImage);
                        if (checkFileType(fileItem.getFileName(),
                                getActivity().getResources().getStringArray(R.array.fileEndingVideo))) {
                            holder.imgPlay.setVisibility(View.VISIBLE);
                        }

                    }
                });
            }
        }

        /**
         * get the file path from phone after download finish the video or photos
         *
         * @param item
         * @return
         */
        public String getPreviewImage(FileItem item) {
            File mediaFile = null;
            if (checkFileType(item.getFileName(),
                    getActivity().getResources().getStringArray(R.array.fileEndingVideo))) {
                mediaFile = new File(MainApplication.CONTAINER_PATH + "/videos/" + getString(R.string.video_album) + "/" + item.getFileName());
            } else if (checkFileType(item.getFileName(),
                    getActivity().getResources().getStringArray(R.array.fileEndingImage))) {
                mediaFile = new File(MainApplication.CONTAINER_PATH + "/photos/" + getString(R.string.photo_album) + "/" + item.getFileName());
            }
            if (mediaFile != null && mediaFile.exists())
                return mediaFile.getAbsolutePath();
            return null;
        }

        /**
         * set image id by file type
         *
         * @param item
         * @return
         */
        public int getResourceId(FileItem item) {
            int resId = R.drawable.unknow;
            if (checkFileType(item.getFileName(),
                    getActivity().getResources().getStringArray(R.array.fileEndingVideo))) {
                resId = R.drawable.icon_file_video;
            } else if (checkFileType(item.getFileName(),
                    getActivity().getResources().getStringArray(R.array.fileEndingImage))) {

                resId = R.drawable.icon_file_photo;
            } else {
                if (item.getPrefix().toLowerCase().equals("pdf"))
                    resId = R.drawable.icon_file_pdf;
                else
                    resId = R.drawable.icon_file_normal;
            }
            return resId;
        }

        public int getCount() {
            return mChatMessages.size();
        }

        public Object getItem(int arg0) {
            return null;
        }

        public long getItemId(int arg0) {
            return arg0;
        }
    }

    static class ViewHolder {
        RelativeLayout loadingLayout;
        ImageView imgPlay;
        ImageView imgCover;
        TextView txtMessage;
        TextView txtName;
    }

    protected boolean checkFileType(String fileName, String[] extendName) {
        for (String aEnd : extendName) {
            if (fileName.toLowerCase().endsWith(aEnd)) {
                return true;
            }
        }
        return false;
    }

    /**
     * open send file
     *
     * @param context
     * @param item
     */
    public void openFile(Context context, FileItem item) {
        try {
            String fileLocation = "";
            if (checkFileType(item.getFileName(),
                    getActivity().getResources().getStringArray(R.array.fileEndingVideo))) {
                fileLocation = MainApplication.CONTAINER_PATH + "/videos/Video Album/" + item.getFileName();
            } else if (checkFileType(item.getFileName(),
                    getActivity().getResources().getStringArray(R.array.fileEndingImage))) {
                fileLocation = MainApplication.CONTAINER_PATH + "/photos/Photo Album/" + item.getFileName();
            } else {
                fileLocation = MainApplication.CONTAINER_PATH + "/documents/My Document/" + item.getFileName();
            }
            openFile(new File(fileLocation));
        } catch (ActivityNotFoundException e) {
            Log.e(MainApplication.TAG, e.toString(), e);
            MainApplication.notice.showToast(getString(R.string.not_open));
        }
    }

    /**
     * open file by provide file
     *
     * @param file
     */
    private void openFile(File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        String type = getMIMEType(file);
        intent.setDataAndType(Uri.fromFile(file), type);
        startActivity(intent);

    }

    /**
     * get the file type
     *
     * @param file
     * @return
     */
    private String getMIMEType(File file) {
        String type = "*/*";
        String fName = file.getName();
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (end.equals("")) return type;
        for (int i = 0; i < Util.MIME_MapTable.length; i++) {
            if (end.equals(Util.MIME_MapTable[i][0]))
                type = Util.MIME_MapTable[i][1];
        }
        return type;
    }

    /**
     * create the action bar string
     *
     * @return
     */
    private String getTitle() {
        String title = "";
        String ipAddress = getIPAddress();
        if (getUsers().size() >= 1) {
            title = getActivity().getResources().getString(R.string.host) + ipAddress + "\n" +
                    getActivity().getResources().getString(R.string.members) + mUsername + ", ";
            for (WebSocket conn : getUsers().keySet()) {
                title += getUsers().get(conn).getUsername() + ", ";
            }
            title = title.substring(0, title.length() - 2);
        } else {
            title = getActivity().getResources().getString(R.string.host) + ipAddress;
        }
        return title;
    }

    /**
     * add a user client
     *
     * @param username
     * @param socket
     */
    private void addClient(String username, WebSocket socket) {
        User user = new User();
        user.setUsername(username);
        getUsers().put(socket, user);
        mTitle = getTitle();
        initActionBar();
        // who join message
        ChatMessage message = new ChatMessage();
        message.setMessage(String.format(getString(R.string.user_join), user.getUsername()));
        message.setFrom(ChatMessage.MESSAGE_TYPE_SYSTEM);
        message.setType(Marco.MSG_SYSTEM_MESSAGE);
        addMessageToList(message);

        for (WebSocket wconn : getUsers().keySet()) {
            wconn.send(mGson.toJson(message));
        }
        refreshListView();
        setClientTitle();
    }

    /**
     * remove a user client
     *
     * @param socket
     */
    private void removeClient(WebSocket socket) {
        try {
            ChatMessage message = new ChatMessage();
            message.setMessage(String.format(getString(R.string.user_quit), getUsers().get(socket).getUsername()));
            message.setFrom(ChatMessage.MESSAGE_TYPE_SYSTEM);
            message.setType(Marco.MSG_SYSTEM_MESSAGE);
            if (socket != null)
                getUsers().remove(socket);
            for (WebSocket wconn : getUsers().keySet()) {
                wconn.send(mGson.toJson(message));
            }
            addMessageToList(message);
            setClientTitle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setClientTitle() {
        // set client title
        try {
            ChatMessage titleMsg = new ChatMessage();
            titleMsg.setMessage(getTitle());
            titleMsg.setType(Marco.MSG_REFRESH_TITLE);
            for (WebSocket conn : getUsers().keySet()) {
                conn.send(mGson.toJson(titleMsg));
            }
        } catch (NotYetConnectedException e) {
            e.printStackTrace();
        }
    }
}