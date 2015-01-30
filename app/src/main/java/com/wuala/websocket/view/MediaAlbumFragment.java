package com.wuala.websocket.view;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.wuala.websocket.R;
import com.wuala.websocket.activity.MainActivity;
import com.wuala.websocket.activity.MainApplication;
import com.wuala.websocket.callback.SendFileListener;
import com.wuala.websocket.util.Util;

import java.io.File;


public class MediaAlbumFragment extends ContainerBaseFragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    /**
     * the listener of send file
     */
    private static SendFileListener mSendCallback;
    /**
     * the wifi button on bottom layout
     */
    private RelativeLayout wifiButton;
    /**
     * the right button of action bars
     */
    private ImageButton btnRight;
    /**
     * the bottom layout
     */
    private LinearLayout bottomLayout;
    private int rootBottom = Integer.MIN_VALUE;

    public static MediaAlbumFragment newInstance(FragmentType type, SendFileListener sendCallback) {
        MainActivity.CURRENT_FRAGMENT = type;
        mSendCallback = sendCallback;
        MediaAlbumFragment fragment = new MediaAlbumFragment();
        return fragment;
    }

    public MediaAlbumFragment() {
    }

    @Override
    public void onResume() {
        btnRight.setEnabled(true);
        getBottomLayout().setVisibility(View.GONE);
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_media_album, container, false);
        init(rootView);
        initActionBar();
        return rootView;
    }

    private void init(final View rootView) {
        isDocument = false;
        albumListView = (ListView) rootView.findViewById(R.id.lv_album);
        if (Util.isPhoto) {
            mediaAlbumRootLocation = MainApplication.CONTAINER_PATH + "/photos/";
        } else {
            mediaAlbumRootLocation = MainApplication.CONTAINER_PATH + "/videos/";
        }
        nowDirectory = new File(mediaAlbumRootLocation);
        refreshView();
        albumListView.setOnItemClickListener(this);
        wifiButton = (RelativeLayout) rootView.findViewById(R.id.layout_wifiupload);
        wifiButton.setOnClickListener(this);
        bottomLayout = (LinearLayout) rootView.findViewById(R.id.layout_album_bottom);
        if (Util.isReadOnly) {
            bottomLayout.setVisibility(View.GONE);
        } else {
            bottomLayout.setVisibility(View.VISIBLE);
        }

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getGlobalVisibleRect(r);
                if (rootBottom == Integer.MIN_VALUE) {
                    rootBottom = r.bottom;
                    return;
                }
                if (!Util.isReadOnly) {
                    if (r.bottom < rootBottom) {
                        bottomLayout.setVisibility(View.INVISIBLE);
                    } else {
                        bottomLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }


    private void initActionBar() {
        setActionBarTitle(Util.isPhoto ? "Photos" : "Videos");
        ImageButton btnLeft = getLeftButton();
        btnLeft.setVisibility(View.VISIBLE);
        btnLeft.setImageResource(R.drawable.btn_back_style);
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackKeyDown();
            }
        });
        btnRight = getRightButton();
        if (Util.isReadOnly) {
            btnRight.setVisibility(View.INVISIBLE);
        } else {
            btnRight.setVisibility(View.VISIBLE);
        }
        btnRight.setImageResource(R.drawable.btn_add_style);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewFile(mediaAlbumRootLocation);
                refreshView();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        openMediaContentView(mSendCallback, albumNames.get(i));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_wifiupload:
                break;
        }
    }

    @Override
    public boolean onBackKeyDown() {
        getFragmentManager().popBackStack();
        return super.onBackKeyDown();
    }
}
