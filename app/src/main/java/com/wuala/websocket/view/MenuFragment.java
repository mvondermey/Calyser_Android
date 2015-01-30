package com.wuala.websocket.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wuala.websocket.R;
import com.wuala.websocket.activity.MainActivity;
import com.wuala.websocket.activity.MainApplication;
import com.wuala.websocket.model.ChatMessage;
import com.wuala.websocket.model.User;
import com.wuala.websocket.util.Marco;
import com.wuala.websocket.util.Util;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.util.TreeMap;

public class MenuFragment extends BaseFragment implements View.OnClickListener {

    private long mExitTime;
    private boolean mCloseConnect = false;
    public static TextView txtIp;

    public static MenuFragment newInstance(FragmentType type) {
        MainActivity.CURRENT_FRAGMENT = type;
        MenuFragment fragment = new MenuFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_menu, container, false);
        initActionBar();
        initView(rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.CURRENT_FRAGMENT = FragmentType.FRAGMENT_MENU;
        setCallbacks();
        initActionBar();
        showUserNameDialog();
        getBottomLayout().setVisibility(View.VISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    /**
     * if first run or the user name is null
     * the dialog will be shown
     */
    private void showUserNameDialog() {
        String username = getUserName();
        if (TextUtils.isEmpty(username)) {
            final Dialog dialog = new Dialog(getActivity(), R.style.dialog);
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View convertView = inflater.inflate(R.layout.dialog_username, null);
            dialog.setContentView(convertView);
            final EditText editText = (EditText) convertView.findViewById(R.id.edt_name);
            editText.setHint(getString(R.string.input_name));
            Button btnYes = (Button) convertView.findViewById(R.id.btn_yes);
            btnYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String username = editText.getText().toString();
                    if (username.trim().length() > 0) {
                        dialog.dismiss();
                        saveCache(Marco.SP_USERNAME, username);
                        setActionBarTitle(String.format(getString(R.string.welcome), username));
                    } else {
                        MainApplication.notice.showToast(getString(R.string.input_name));
                    }
                }
            });
            dialog.show();
        } else {
            setActionBarTitle(String.format(getString(R.string.welcome), username));
        }
    }

    /**
     * init view by ID
     *
     * @param view
     */
    private void initView(View view) {
        Button btnClient = (Button) view.findViewById(R.id.btn_client);
        btnClient.setOnClickListener(this);
        txtIp = (TextView) view.findViewById(R.id.txt_ip);
        txtIp.setText(getIPAddress());
        TextView txtDate = (TextView) view.findViewById(R.id.txt_date);
        txtDate.setText(Marco.RELEASE_DATE);
    }

    /**
     * init action bar
     */
    private void initActionBar() {
        ImageButton btnLeft = getLeftButton();
        btnLeft.setVisibility(View.INVISIBLE);
        ImageButton btnRight = getRightButton();
        btnRight.setVisibility(View.INVISIBLE);
    }

    /**
     * the loading dialog will be shown when client request connect server side
     */
    private void showClientLoadingDialog() {
        mProgressDialog = new Dialog(getActivity(), R.style.dialog);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View convertView = inflater.inflate(R.layout.dialog_progress, null);
        mProgressDialog.setContentView(convertView);
        TextView titleView = (TextView) convertView.findViewById(R.id.txt_title);
        titleView.setText(getString(R.string.loading));
        TextView txtView = (TextView) convertView.findViewById(R.id.progress_text);
        txtView.setText(getString(R.string.waiting_server));
        mProgressDialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_client:
                if (isNetworkConnected()) {
                    showClientIPDialog();
                } else {
                    MainApplication.notice.showToast(getString(R.string.connect_wifi));
                }
                break;
        }
    }

    /**
     * if client want to connect server the dialog will be shown
     * client must input an ip to connect server
     */
    private void showClientIPDialog() {
        final Dialog dialog = new Dialog(getActivity(), R.style.dialog);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View convertView = inflater.inflate(R.layout.dialog_client, null);
        dialog.setContentView(convertView);
        final EditText editText = (EditText) convertView.findViewById(R.id.edt_ip);
        editText.setText(getCache(Marco.SP_IP));
        Button btnYes = (Button) convertView.findViewById(R.id.btn_yes);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Util.isFastDoubleClick()) {
                    return;
                }
                if (editText.getText().toString().length() > 0) {
                    mServerIP = editText.getText().toString();
                    if (validIPAddress(mServerIP)) {
                        showClientLoadingDialog();
                        startSocketClient(mServerIP);
                    } else {
                        MainApplication.notice.showToast(getString(R.string.invalid_ip));
                    }
                } else {
                    MainApplication.notice.showToast(getString(R.string.input_ip));
                }
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
    public boolean onBackKeyDown() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mCloseConnect = true;
        } else {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                MainApplication.notice.showToast(getString(R.string.back_exit));
                mExitTime = System.currentTimeMillis();
            } else {
                System.exit(0);
            }
        }

        return true;
    }

    @Override
    public void onClientOpen(ServerHandshake serverHandshake) {
        super.onClientOpen(serverHandshake);

        // send request to server side
        final WebSocketClient client = getClient();
        if (client == null) {
            return;
        }
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (mCloseConnect) {
                    client.close();
                }
            }
        });
        client.send(createRequestMessage());
    }

    @Override
    public void onServerMessage(WebSocket conn, String s) {
        super.onServerMessage(conn, s);
        // server side get the request from client
    }

    @Override
    public void onClientMessage(String s) {
        super.onClientMessage(s);
        Message hMsg = chartRequestHandler.obtainMessage();
        ChatMessage msgChat = mGson.fromJson(s, ChatMessage.class);
        if (msgChat.getType().equals(Marco.MSG_RESPONSE_CONNECT)) {
            hMsg.what = HMSG_RESPONSE_SHOW;
            hMsg.obj = msgChat;
        }

        if (msgChat.getType().equals(Marco.MSG_REFRESH_TITLE)) {
            hMsg.obj = msgChat.getMessage();
            hMsg.what = HMSG_REFRESH_TITLE;
        }
        chartRequestHandler.sendMessage(hMsg);
    }

    @Override
    public void onServerError(WebSocket conn, Exception ex) {
        if (conn != null) {
            try {
                super.onServerError(conn, ex);
                Message hMsg = chartRequestHandler.obtainMessage();
                hMsg.what = HMSG_ERROR;
                hMsg.obj = ex;
                chartRequestHandler.sendMessage(hMsg);
                if (conn != null)
                    conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClientError(Exception e) {
        super.onClientError(e);
        Message hMsg = chartRequestHandler.obtainMessage();
        hMsg.what = HMSG_ERROR;
        hMsg.obj = e;
        chartRequestHandler.sendMessage(hMsg);
    }

    private boolean validIPAddress(String ip) {
        String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
        return ip.matches(regex);
    }
}