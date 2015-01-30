package com.wuala.websocket.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.wuala.websocket.R;
import com.wuala.websocket.util.Util;
import com.wuala.websocket.view.BaseFragment;
import com.wuala.websocket.view.ChatFragment;
import com.wuala.websocket.view.ContainerFragment;
import com.wuala.websocket.view.DocumentsFragment;
import com.wuala.websocket.view.FragmentType;
import com.wuala.websocket.view.ImportFileFragment;
import com.wuala.websocket.view.MediaAlbumFragment;
import com.wuala.websocket.view.MediaContentFragment;
import com.wuala.websocket.view.MenuFragment;
import com.wuala.websocket.view.MyGalleryFragment;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by Wang on 12/05/14.
 */
public class ImportFromSDActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_from_sd);
        Util.isImportSelect = true;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new ImportFileFragment())
                .commit();
    }
}