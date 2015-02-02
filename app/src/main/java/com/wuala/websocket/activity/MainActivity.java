package com.wuala.websocket.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dropbox.chooser.android.DbxChooser;
import com.wuala.websocket.LoginActivity;
import com.wuala.websocket.R;
import com.wuala.websocket.callback.SendFileListener;
import com.wuala.websocket.httpserver.WebService;
import com.wuala.websocket.util.Util;
import com.wuala.websocket.view.BaseFragment;
import com.wuala.websocket.view.ChatFragment;
import com.wuala.websocket.view.ContainerFragment;
import com.wuala.websocket.view.DocumentsFragment;
import com.wuala.websocket.view.FragmentType;
import com.wuala.websocket.view.MediaContentFragment;
import com.wuala.websocket.view.MenuFragment;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;


import com.couchbase.lite.util.Log;
import com.wuala.websocket.view.MediaAlbumFragment;
import com.wuala.websocket.view.MyGalleryFragment;
import com.wuala.websocket.view.SettingFragment;
import com.wuala.websocket.view.UserFragment;

/**
 * Created by Wang on 10/30/14.
 */
public class MainActivity extends FragmentActivity {
    final String TAG = "MyActivity";

    /**
     * Title Bar
     */
    private RelativeLayout mActionBar;
    /**
     * current fragment
     */
    public static FragmentType CURRENT_FRAGMENT;
    /**
     * MainActivity Context
     */
    public static Context mContext;
    /**
     * the top fragment of fragment stack
     */
    public static BaseFragment mTopFragment;
    /**
     * wifi status change listener
     */
    private NetworkConnectChangedReceiver mWifiReceiver;
    /**
     * the bottom tab index
     */
    private int currentIndex = 0;
    /**
     * user tab button
     */
    private ImageView userButton;
    /**
     * chat tab button
     */
    private ImageView chatButton;
    /**
     * container tab button
     */
    private ImageView containerButton;
    /**
     * setting tab button
     */
    private ImageView settingButton;
    /**
     * user tab layout
     */
    private RelativeLayout userLayout;
    /**
     * chat tab layout
     */
    private RelativeLayout chatLayout;
    /**
     * container tab layout
     */
    private RelativeLayout containerLayout;
    /**
     * setting tab layout
     */
    private RelativeLayout settingLayout;
    /**
     * dropbox callback
     */
    private DropBoxSelectCallBack dropBoxSelectCallBack;
    /**
     * take photo or take video callback
     */
    private TakeCallBack takeCallBack;
    /**
     * tabs container
     */
    private LinearLayout bottomLayout;
    /**
     * disconnection notice dialog
     */
    private Dialog disConnectionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Login
        android.util.Log.d("MainActivity", "OnCreate");
        //
        LoginDialog();
        //
        registerWifiStatus();
        initServer();
        initView();
        openMenuView(1);
        getScreenInformation();

    }

    public void LoginDialog() {
//
        Intent login = new Intent(this, LoginActivity.class);
        startActivityForResult(login,1);
        //
    }
//
    /**
     * start the https server and socket server
     */
    public void initServer() {
        Intent intent = new Intent(this, WebService.class);
        intent.setAction("com.start.webService");

        ((MainApplication) getApplication()).startHttpServer(intent);
        ((MainApplication) getApplication()).startSocketServer();
    }

    /**
     * init the tab bar and action bar
     */
    public void initView() {
        mActionBar = (RelativeLayout) findViewById(R.id.action_bar);

        bottomLayout = (LinearLayout) findViewById(R.id.layout_bottom);

        userLayout = (RelativeLayout) findViewById(R.id.layout_user);
        userLayout.setOnClickListener(new TabClickListener(0));
        userLayout.setTag(R.drawable.tab_user_selected);
        userButton = (ImageView) userLayout.findViewById(R.id.btn_user);

        chatLayout = (RelativeLayout) findViewById(R.id.layout_chats);
        chatLayout.setOnClickListener(new TabClickListener(1));
        chatLayout.setTag(R.drawable.tab_chats_selected);
        chatLayout.setBackgroundColor(Color.argb(255, 218, 218, 218));
        chatButton = (ImageView) chatLayout.findViewById(R.id.btn_chats);
        chatButton.setBackgroundResource(R.drawable.tab_chats_selected);


        containerLayout = (RelativeLayout) findViewById(R.id.layout_container);
        containerLayout.setOnClickListener(new TabClickListener(2));
        containerLayout.setTag(R.drawable.tab_container_selected);
        containerButton = (ImageView) containerLayout.findViewById(R.id.btn_container);


        settingLayout = (RelativeLayout) findViewById(R.id.layout_setting);
        settingLayout.setOnClickListener(new TabClickListener(3));
        settingLayout.setTag(R.drawable.tab_settings_selected);
        settingButton = (ImageView) settingLayout.findViewById(R.id.btn_setting);

        mContext = this;
        disConnectionDialog = new Dialog(MainActivity.this, R.style.dialog);
    }

    /**
     * get screen width and height of the device
     */
    public void getScreenInformation() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Util.screenWidth = dm.widthPixels;
        Util.screenHeight = dm.heightPixels;
    }

    /**
     * get action bar
     *
     * @return action bar
     */
    public View getMyActionBar() {
        return mActionBar;
    }

    @Override
    protected void onResume() {
        Util.isImportSelect = false;
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        android.util.Log.d("MainActivity", "OnPause");
    }

    @Override
    protected void onDestroy() {
        Intent server = new Intent(this, WebService.class);
        ((MainApplication) getApplication()).closeServer(server);
        unregisterReceiver(mWifiReceiver);
        super.onDestroy();
        android.util.Log.d("MainActivity", "OnDestroy");
    }

    /**
     * network connected
     *
     * @return yes or no
     */
    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null) {
            MainApplication.notice.showToast(getString(R.string.network_error));
            return false;
        } else {
            return true;
        }
    }

    /**
     * get phone's IP address
     *
     * @return IP
     */
    public String getPhoneIP() {
        String ip = "";
        ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectMgr.getActiveNetworkInfo();
        if (info == null) {
            MainApplication.notice.showToast(getString(R.string.connect_wifi));
        } else {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                ip = getWifiIP();
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                ip = getGPRSIP();
            }
        }
        return ip;
    }

    /**
     * if use wifi will return wifi IP address
     *
     * @return IP address
     */
    private String getWifiIP() {
        String ip;
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int i = wifiInfo.getIpAddress();
        ip = (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
        return ip;
    }

    /**
     * if use GPRS will return GPRS IP address
     *
     * @return GPRS IP address
     */
    private String getGPRSIP() {
        String ip = "";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface networkInterface = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddress = networkInterface.getInetAddresses(); enumIpAddress.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddress.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(MainApplication.TAG, ex.toString(), ex);
        }
        return ip;
    }

    /**
     * register wifi status receiver
     */
    private void registerWifiStatus() {
        mWifiReceiver = new NetworkConnectChangedReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mWifiReceiver, filter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (CURRENT_FRAGMENT == FragmentType.FRAGMENT_CHAT || CURRENT_FRAGMENT == FragmentType.FRAGMENT_GALLERY) {
                return mTopFragment.onBackKeyDown();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

        }

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

        }
    }

    /**
     * jump to chat fragment
     */
    public void openChatView(boolean isServer) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right,
                R.anim.slide_in_left, R.anim.slide_out_left);
        if (Util.isReadOnly) {
            getBottomLayout().setVisibility(View.GONE);
            transaction.replace(R.id.container, ChatFragment.newInstance(FragmentType.FRAGMENT_CHAT, isServer));
            transaction.addToBackStack(null);
        } else
            transaction.replace(R.id.container, ChatFragment.newInstance(FragmentType.FRAGMENT_CHAT, isServer));
        transaction.commit();
    }

    /**
     * jump to menu fragment
     */
    public void openMenuView(int index) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        setAnimation(transaction, index);
        transaction.replace(R.id.container, MenuFragment.newInstance(FragmentType.FRAGMENT_MENU), FragmentType.FRAGMENT_MENU.toString());
        transaction.commit();
        currentIndex = index;
    }

    /**
     * jump to user fragment
     */
    public void openUserView(int index) {
        CURRENT_FRAGMENT = FragmentType.FRAGMENT_USER;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        setAnimation(transaction, index);
        transaction.replace(R.id.container, UserFragment.newInstance(null, null));
        transaction.commit();
        currentIndex = index;
    }

    /**
     * jump to setting fragment
     */
    public void openSettingView(int index) {
        CURRENT_FRAGMENT = FragmentType.FRAGMENT_SETTING;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        setAnimation(transaction, index);
        transaction.replace(R.id.container, SettingFragment.newInstance(null, null));
        transaction.commit();
        currentIndex = index;
    }

    public void setAnimation(FragmentTransaction transaction, int index) {
        if (currentIndex < index)
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right,
                    R.anim.slide_in_left, R.anim.slide_out_left);
        else
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
    }

    /**
     * jump to container fragment
     */

    public void openContainerView(SendFileListener callback, int index) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (getBottomLayout().getVisibility() == View.VISIBLE)
            setAnimation(transaction, index);
        if (Util.isReadOnly) {
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right,
                    R.anim.slide_in_left, R.anim.slide_out_left);
            transaction.replace(R.id.container, ContainerFragment.newInstance(FragmentType.FRAGMENT_CONTAINER, callback));
            transaction.addToBackStack(null);
        } else {
            transaction.replace(R.id.container, ContainerFragment.newInstance(FragmentType.FRAGMENT_CONTAINER, callback));
        }
        transaction.commit();
        currentIndex = index;
    }


    public LinearLayout getBottomLayout() {
        return bottomLayout;
    }

    /**
     * jump to documents fragment
     */
    public void openDocumentView(SendFileListener callback) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right,
                R.anim.slide_in_left, R.anim.slide_out_left);
        if (Util.isReadOnly)
            transaction.replace(R.id.container, DocumentsFragment.newInstance(FragmentType.FRAGMENT_DOCUMENT, callback));
        else
            transaction.replace(R.id.container, DocumentsFragment.newInstance(FragmentType.FRAGMENT_DOCUMENT, callback));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * jump to pictures fragment
     */
    public void openPictureView(SendFileListener callback) {
        Util.isPhoto = true;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right,
                R.anim.slide_in_left, R.anim.slide_out_left);
        if (Util.isReadOnly)
            transaction.replace(R.id.container, MediaAlbumFragment.newInstance(FragmentType.FRAGMENT_PICTURE, callback));
        else
            transaction.replace(R.id.container, MediaAlbumFragment.newInstance(FragmentType.FRAGMENT_PICTURE, callback));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * jump to videos fragment
     */
    public void openVideosView(SendFileListener callback) {
        Util.isPhoto = false;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right,
                R.anim.slide_in_left, R.anim.slide_out_left);
        if (Util.isReadOnly) {
            transaction.replace(R.id.container, MediaAlbumFragment.newInstance(FragmentType.FRAGMENT_VIDEO, callback));
        } else {
            transaction.replace(R.id.container, MediaAlbumFragment.newInstance(FragmentType.FRAGMENT_VIDEO, callback));
        }
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * jump to mediaContents fragment
     */
    public void openMediaContentView(SendFileListener callback, String albumName) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putString("albumName", albumName);
        MediaContentFragment mediaContentFragment = MediaContentFragment.newInstance(FragmentType.FRAGMENT_MEDIA_CONTENT, callback);
        mediaContentFragment.setArguments(bundle);
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right,
                R.anim.slide_in_left, R.anim.slide_out_left);
        if (Util.isReadOnly)
            transaction.replace(R.id.container, mediaContentFragment);
        else
            transaction.replace(R.id.container, mediaContentFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * jump to mediaContents fragment
     */
    public void openMyGalleryView(SendFileListener callback, List<String> fileURLList, String currentLoaction) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, MyGalleryFragment.newInstance(FragmentType.FRAGMENT_GALLERY, callback, fileURLList, currentLoaction));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * set drop callback listener
     *
     * @param dropBoxSelectCallBack
     */
    public void setDropBoxSelectCallBack(DropBoxSelectCallBack dropBoxSelectCallBack) {
        this.dropBoxSelectCallBack = dropBoxSelectCallBack;
    }

    /**
     * user click the bottom tab listener
     */
    public class TabClickListener implements View.OnClickListener {

        int index;

        public TabClickListener(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View view) {
            if (index != currentIndex)
                switch (view.getId()) {
                    case R.id.layout_container:
                        Util.isReadOnly = false;
                        openContainerView(null, index);
                        break;
                    case R.id.layout_chats:
                        openMenuView(index);
                        break;
                    case R.id.layout_user:
                        openUserView(index);
                        break;
                    case R.id.layout_setting:
                        openSettingView(index);
                        break;
                }
            changeBackground(view);
        }

        public void changeBackground(View view) {
            userButton.setBackgroundResource(R.drawable.tab_user_normal);
            chatButton.setBackgroundResource(R.drawable.tab_chats_normal);
            containerButton.setBackgroundResource(R.drawable.tab_container_normal);
            settingButton.setBackgroundResource(R.drawable.tab_settings_normal);
            userLayout.setBackgroundColor(Color.argb(255, 241, 241, 241));
            chatLayout.setBackgroundColor(Color.argb(255, 241, 241, 241));
            containerLayout.setBackgroundColor(Color.argb(255, 241, 241, 241));
            settingLayout.setBackgroundColor(Color.argb(255, 241, 241, 241));
            ((ImageView) ((RelativeLayout) view).getChildAt(0)).setBackgroundResource((Integer) view.getTag());
            view.setBackgroundColor(Color.argb(255, 218, 218, 218));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == 100 && data != null) {
                DbxChooser.Result result = new DbxChooser.Result(data);
                dropBoxSelectCallBack.dropBoxSelectCallBack(result);
            } else {
                takeCallBack.takeCallBack(requestCode, resultCode, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * take photo or video function
     *
     * @param currentLocation the media location
     */
    public void takeMedia(String currentLocation) {
        Intent intent = new Intent();
        Intent intent_camera = getPackageManager().getLaunchIntentForPackage("com.android.camera");
        if (intent_camera != null) {
            intent.setPackage("com.android.camera");
        }
        File imageFile;
        if (Util.isPhoto) {
            imageFile = new File(currentLocation + "/" + System.currentTimeMillis() + ".jpg");
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        } else {
            imageFile = new File(currentLocation + "/" + System.currentTimeMillis() + ".mp4");
            intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        startActivityForResult(intent, 1000);
    }

    /**
     * network changed listener
     */
    public class NetworkConnectChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        LayoutInflater inflater = LayoutInflater.from(context);
                        View convertView = inflater.inflate(R.layout.dialog_notice, null);
                        disConnectionDialog.setContentView(convertView);
                        TextView txtView = (TextView) convertView.findViewById(R.id.txt_content);
                        txtView.setText(context.getString(R.string.wifi_disconnect));
                        TextView txtTitle = (TextView) convertView.findViewById(R.id.txt_title);
                        txtTitle.setText(context.getString(R.string.warn));
                        Button btnYes = (Button) convertView.findViewById(R.id.btn_yes);
                        btnYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (!(mTopFragment instanceof MenuFragment)) {
                                    getSupportFragmentManager().beginTransaction()
                                            .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right)
                                            .replace(R.id.container, MenuFragment.newInstance(FragmentType.FRAGMENT_MENU), FragmentType.FRAGMENT_MENU.toString())
                                            .commit();
                                }
                                disConnectionDialog.cancel();
                            }
                        });
                        disConnectionDialog.show();
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        Log.e(MainApplication.TAG, "WIFI_STATE_DISABLING");
                        break;
                }
            }
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                Parcelable parcelableExtra = intent
                        .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (null != parcelableExtra) {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    NetworkInfo.State state = networkInfo.getState();
                    boolean isConnected = state == NetworkInfo.State.CONNECTED;
                    if (isConnected) {
                        MenuFragment.txtIp.setText(getPhoneIP());
                        disConnectionDialog.cancel();
                        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FragmentType.FRAGMENT_MENU.toString());
                        if (fragment == null) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, MenuFragment.newInstance(FragmentType.FRAGMENT_MENU), FragmentType.FRAGMENT_MENU.toString())
                                    .commit();
                        }
                    }
                }
            }
        }
    }

    /**
     * set take photo or video listener
     *
     * @param takeCallBack
     */
    public void setTakeCallBack(TakeCallBack takeCallBack) {
        this.takeCallBack = takeCallBack;
    }

    public interface DropBoxSelectCallBack {
        public void dropBoxSelectCallBack(DbxChooser.Result result);
    }

    public interface TakeCallBack {
        public void takeCallBack(int requestCode, int resultCode, Intent data);
    }

}