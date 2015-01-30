package com.wuala.websocket.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wuala.websocket.R;
import com.wuala.websocket.activity.MainActivity;
import com.wuala.websocket.activity.MainApplication;
import com.wuala.websocket.callback.SendFileListener;
import com.wuala.websocket.callback.SocketClientCallback;
import com.wuala.websocket.callback.SocketServerCallback;
import com.wuala.websocket.model.ChatMessage;
import com.wuala.websocket.model.User;
import com.wuala.websocket.util.DisplayUtil;
import com.wuala.websocket.util.Marco;
import com.wuala.websocket.util.Util;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class BaseFragment extends Fragment implements SocketServerCallback, SocketClientCallback {

    /**
     * action bar title
     */
    private TextView mTitleView;
    /**
     * action bar left button an right button
     */
    private ImageButton mLeftActionButton, mRightActionButton;
    /**
     * loading progress dialog
     */
    protected Dialog mProgressDialog;

    protected final int HMSG_CONFIRM_SHOW = 1;
    protected final int HMSG_RESPONSE_SHOW = 2;
    protected final int HMSG_REFRESH_TITLE = 3;
    protected final int HMSG_ERROR = 4;
    /**
     * server ip address
     */
    protected String mServerIP;
    protected Gson mGson;
    /**
     * request dialog
     */
    private Dialog myRequestDialog;
    private Context mContext;

    public BaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        try {
            View view = ((MainActivity) getActivity()).getMyActionBar();
            mTitleView = (TextView) view.findViewById(R.id.txt_title);
            mLeftActionButton = (ImageButton) view.findViewById(R.id.btn_left);
            mRightActionButton = (ImageButton) view.findViewById(R.id.btn_right);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            MainActivity.mTopFragment = this;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void setCallbacks() {
        ((MainApplication) getActivity().getApplication()).setServerCallBack(this);
        ((MainApplication) getActivity().getApplication()).setClientCallBack(this);
    }

    public String getUserName() {
        return ((MainApplication) getActivity().getApplication()).getUserName();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void openChatView(boolean isServer) {
        ((MainActivity) mContext).openChatView(isServer);
    }

    public void openContainerView(SendFileListener callback) {
        ((MainActivity) getActivity()).openContainerView(callback, 3);
    }

    public void openDocumentView(SendFileListener callback) {
        ((MainActivity) getActivity()).openDocumentView(callback);
    }

    public void openPictureView(SendFileListener callback) {
        ((MainActivity) getActivity()).openPictureView(callback);
    }

    public void openVideoView(SendFileListener callback) {
        ((MainActivity) getActivity()).openVideosView(callback);
    }

    public void openMediaContentView(SendFileListener callback, String albumName) {
        ((MainActivity) getActivity()).openMediaContentView(callback, albumName);
    }

    public void openMyGalleryView(SendFileListener callback, List<String> fileItemList, String currentLoaction) {
        ((MainActivity) getActivity()).openMyGalleryView(callback, fileItemList, currentLoaction);
    }

    public void setDropBoxSelectCallBack(MainActivity.DropBoxSelectCallBack dropBoxSelectCallBack) {
        ((MainActivity) getActivity()).setDropBoxSelectCallBack(dropBoxSelectCallBack);
    }

    public void setTakeCallBack(MainActivity.TakeCallBack takeCallBack) {
        ((MainActivity) getActivity()).setTakeCallBack(takeCallBack);
    }

    public void setActionBarTitle(String title) {

        if (title.contains("\n")) {
            String[] titleString = title.split("\n");
            mTitleView.setTextColor(Color.argb(255, 0, 168, 255));
            SpannableString builder = new SpannableString(title);
            builder.setSpan(new AbsoluteSizeSpan(DisplayUtil.dip2px(getActivity(), 16)), 0, titleString[0].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new StyleSpan(Typeface.BOLD), 0, titleString[0].length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            mTitleView.setSingleLine(false);
            mTitleView.setPadding(DisplayUtil.dip2px(getActivity(), 10), 0, 0, 0);
            mTitleView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            ((RelativeLayout) (mTitleView.getParent().getParent())).findViewById(R.id.view_line).setVisibility(View.VISIBLE);
            getTitleView().setTextSize(DisplayUtil.px2dip(getActivity(), getResources().getDimension(R.dimen.title_bar_font_size_small)));
            mTitleView.setText(builder);
        } else {
            mTitleView.setPadding(0, 0, 0, 0);
            mTitleView.setSingleLine(true);
            mTitleView.setTextColor(Color.argb(255, 0, 0, 0));
            mTitleView.setGravity(Gravity.CENTER);
            ((RelativeLayout) (mTitleView.getParent().getParent())).findViewById(R.id.view_line).setVisibility(View.GONE);
            getTitleView().setTextSize(DisplayUtil.px2dip(getActivity(), getResources().getDimension(R.dimen.title_bar_font_size)));
            mTitleView.setText(title);
        }
    }

    public TextView getTitleView() {
        return mTitleView;
    }

    public ImageButton getLeftButton() {
        return mLeftActionButton;
    }

    public ImageButton getRightButton() {
        return mRightActionButton;
    }

    public boolean isNetworkConnected() {
        return ((MainActivity) getActivity()).isNetworkConnected();
    }

    public String getIPAddress() {
        return ((MainActivity) getActivity()).getPhoneIP();
    }

    public WebSocketServer getServer() {
        return ((MainApplication) getActivity().getApplication()).getServer();
    }

    public void startSocketClient(String host) {
        ((MainApplication) getActivity().getApplication()).startSocketClient(host);
    }

    public WebSocketClient getClient() {
        return ((MainApplication) getActivity().getApplication()).getClient();
    }

    public Map<WebSocket, User> getUsers() {
        return ((MainApplication) (((MainActivity) mContext).getApplication())).getUsers();
    }

    public LinearLayout getBottomLayout() {
        return ((MainActivity) getActivity()).getBottomLayout();
    }

    public void takePhoto(String currentLocation) {
        ((MainActivity) getActivity()).takeMedia(currentLocation);
    }

    public void closeClient() {
        ((MainApplication) getActivity().getApplication()).closeClient();
    }

    public String createConfirmMessage(boolean isServer) {
        return ((MainApplication) (((MainActivity) mContext).getApplication())).createConfirmMessage(isServer);
    }

    public String createRequestMessage() {
        return ((MainApplication) getActivity().getApplication()).createRequestMessage();
    }

    public void showConfirmDialog(Dialog dialog, String username, View.OnClickListener yesListener, View.OnClickListener noListener) {
        ((MainApplication) (((MainActivity) mContext).getApplication())).showConfirmDialog(dialog, username, yesListener, noListener);
    }

    public boolean onBackKeyDown() {
        return false;
    }

    @Override
    public void onClientOpen(ServerHandshake serverHandshake) {

    }

    @Override
    public void onClientMessage(String s) {

    }

    @Override
    public void onClientClose(int i, String s, boolean b) {

    }

    @Override
    public void onClientError(Exception e) {

    }

    @Override
    public void onServerOpen(WebSocket conn, ClientHandshake handshake) {

    }

    @Override
    public void onServerClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override
    public void onServerMessage(WebSocket conn, String s) {
        ChatMessage msgChat = mGson.fromJson(s, ChatMessage.class);
        if (msgChat.getType().equals(Marco.MSG_REQUEST_CONNECT)) {
            Message hMsg = chartRequestHandler.obtainMessage();
            hMsg.what = HMSG_CONFIRM_SHOW;
            Object[] objs = {conn, msgChat};
            hMsg.obj = objs;
            chartRequestHandler.sendMessage(hMsg);
        }
    }

    @Override
    public void onServerError(WebSocket conn, Exception ex) {

    }


    public void saveCache(String param, String value) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(MainApplication.TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(param, value);
        editor.commit();
    }

    public String getCache(String param) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(MainApplication.TAG, Context.MODE_PRIVATE);
        String cache = sharedPreferences.getString(param, "");
        return cache;
    }

    public void hideKeybord(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void showKeybord(Context context, View view) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 10);
    }

    public void collapseSoftInputMethod(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * create base confirm dialog
     *
     * @param title
     * @param message
     * @param positiveButtonEventHandle
     */
    protected void confirmDialog(String title, String message,
                                 final View.OnClickListener positiveButtonEventHandle) {
//        Looper.prepare();
        final Dialog dialog = new Dialog(getActivity(), R.style.dialog);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View convertView = inflater.inflate(R.layout.dialog_confirm, null);
        dialog.setContentView(convertView);
        TextView txtView = (TextView) convertView.findViewById(R.id.txt_content);
        txtView.setText(message);

        TextView txtTitle = (TextView) convertView.findViewById(R.id.txt_title);
        txtTitle.setText(title);
        Button btnYes = (Button) convertView.findViewById(R.id.btn_yes);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                positiveButtonEventHandle.onClick(view);
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

    /**
     * create base message dialog
     *
     * @param title
     * @param message
     */
    protected void showMessageDialog(String title, String message) {
        final Dialog dialog = new Dialog(getActivity(), R.style.dialog);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View convertView = inflater.inflate(R.layout.dialog_notice, null);
        dialog.setContentView(convertView);
        TextView txtView = (TextView) convertView.findViewById(R.id.txt_content);
        txtView.setText(message);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.txt_title);
        txtTitle.setText(title);
        Button btnYes = (Button) convertView.findViewById(R.id.btn_yes);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    /**
     * create base custom dialog
     *
     * @param file
     * @param title
     * @param message
     * @param positiveButtonEventHandle
     */
    protected void showCustomDialog(File file, String title, String message,
                                    final View.OnClickListener positiveButtonEventHandle) {
        final Dialog dialog = new Dialog(getActivity(), R.style.dialog);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View convertView = inflater.inflate(R.layout.dialog_client, null);
        dialog.setContentView(convertView);
        final TextView titleText = (TextView) convertView.findViewById(R.id.txt_title);
        titleText.setText(title);
        final EditText editText = (EditText) convertView.findViewById(R.id.edt_ip);
        editText.requestFocus();
        if (file != null)
            editText.setText(file.getName());
        else
            editText.setText("");

        editText.setHint(message);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        final Button btnYes = (Button) convertView.findViewById(R.id.btn_yes);
        btnYes.setText("OK");
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                positiveButtonEventHandle.onClick(convertView);
                hideKeybord(getActivity(), editText);
                dialog.cancel();
            }
        });
        Button btnCancel = (Button) convertView.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                hideKeybord(getActivity(), editText);
            }
        });
        if (editText.getText().length() == 0) {
            btnYes.setEnabled(false);
            btnYes.setTextColor(Color.argb(255, 199, 199, 199));
        }
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (charSequence.length() == 0) {
                    btnYes.setEnabled(false);
                    btnYes.setTextColor(Color.argb(255, 199, 199, 199));
                } else {
                    btnYes.setEnabled(true);
                    btnYes.setTextColor(Color.argb(255, 0, 170, 255));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        dialog.show();
        showKeybord(getActivity(), editText);
    }

    /**
     * format url
     *
     * @param s
     * @return
     */
    public String convert(String s) {
        try {
            URL url = new URL(s);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            return uri.toURL().toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * UI handler
     */
    protected Handler chartRequestHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj != null) {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.cancel();
                }
                switch (msg.what) {
                    case HMSG_CONFIRM_SHOW:
                        if (MainActivity.CURRENT_FRAGMENT == FragmentType.FRAGMENT_SETTING || MainActivity.CURRENT_FRAGMENT == FragmentType.FRAGMENT_USER || MainActivity.CURRENT_FRAGMENT == FragmentType.FRAGMENT_CONTAINER || MainActivity.CURRENT_FRAGMENT == FragmentType.FRAGMENT_MENU)
                            try {
                                Object[] objects = (Object[]) msg.obj;
                                final WebSocket socket = (WebSocket) objects[0];
                                final ChatMessage message = (ChatMessage) objects[1];
                                myRequestDialog = new Dialog(mContext, R.style.dialog);
                                View.OnClickListener yesLis = new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        socket.send(createConfirmMessage(true));
                                        Util.isReadOnly = true;
                                        ChatFragment.mChatMessages = null;
                                        openChatView(true);
                                        // create a map to save user info
                                        User user = new User();
                                        user.setUsername(message.getName());
                                        getUsers().put(socket, user);
                                        myRequestDialog.cancel();
                                    }
                                };

                                View.OnClickListener noLis = new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        socket.send(createConfirmMessage(false));
                                        myRequestDialog.cancel();
                                    }
                                };
                                showConfirmDialog(myRequestDialog, message.getName(), yesLis, noLis);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        break;
                    case HMSG_RESPONSE_SHOW:
                        ChatMessage messageR = (ChatMessage) msg.obj;
                        if (messageR.getMessage().equals(Marco.COMMAND_AGREE)) {
                            saveCache(Marco.SP_IP, mServerIP);
                            Util.isReadOnly = true;
                            ChatFragment.mChatMessages = null;
                            openChatView(false);
                        } else if (messageR.getMessage().equals(Marco.COMMAND_DISAGREE)) {
                            if (getClient() != null) {
                                getClient().close();
                            }
                        }
                        MainApplication.notice.showToast(String.format(getString(R.string.reply_request), messageR.getName(), messageR.getMessage().toLowerCase()));
                        break;
                    case HMSG_REFRESH_TITLE:
                        String title = (String) msg.obj;
                        setActionBarTitle(title);
                        break;
                    case HMSG_ERROR:
                        Exception e = (Exception) msg.obj;
                        Log.e(MainApplication.TAG, e.toString(), e);
                        String errorMsg;
                        if (e.getMessage().contains("host=null")) {
                            errorMsg = getString(R.string.invalid_ip);
                        } else if (e.getMessage().contains(mContext.getString(R.string.connecation_refused))) {
                            errorMsg = getString(R.string.server_not_found);
                        } else {
                            errorMsg = mContext.getString(R.string.unable_to_connect_to) + mServerIP;
                        }
                        MainApplication.notice.showToast(errorMsg);
                        break;
                }
            }
        }
    };
}
