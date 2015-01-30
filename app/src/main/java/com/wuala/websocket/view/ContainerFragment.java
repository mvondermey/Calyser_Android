package com.wuala.websocket.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wuala.websocket.R;
import com.wuala.websocket.activity.MainActivity;
import com.wuala.websocket.activity.MainApplication;
import com.wuala.websocket.callback.SendFileListener;
import com.wuala.websocket.model.FileItem;
import com.wuala.websocket.util.Util;

import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class ContainerFragment extends BaseFragment implements View.OnClickListener {

    private static SendFileListener mSendCallback;

    public static ContainerFragment newInstance(FragmentType type, SendFileListener sendCallback) {
        MainActivity.CURRENT_FRAGMENT = type;
        mSendCallback = sendCallback;
        ContainerFragment fragment = new ContainerFragment();
        return fragment;
    }

    public ContainerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        if (Util.isReadOnly == true) {
            getBottomLayout().setVisibility(View.GONE);
        } else {
            getBottomLayout().setVisibility(View.VISIBLE);
        }
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
        View rootView = inflater.inflate(R.layout.fragment_container, container, false);
        init(rootView);
        initActionBar();
        return rootView;
    }

    private void init(View rootView) {
        rootView.findViewById(R.id.btn_videos).setOnClickListener(this);
        rootView.findViewById(R.id.btn_documents).setOnClickListener(this);
        rootView.findViewById(R.id.btn_photos).setOnClickListener(this);
    }

    /**
     * init action bar
     */
    private void initActionBar() {
        setActionBarTitle(getString(R.string.container));
        ImageButton btnLeft = getLeftButton();
        if (Util.isReadOnly)
            btnLeft.setVisibility(View.VISIBLE);
        else
            btnLeft.setVisibility(View.GONE);
        btnLeft.setImageResource(R.drawable.btn_back_style);
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack();
            }
        });
        ImageButton btnRight = getRightButton();
        btnRight.setVisibility(View.GONE);
        btnRight.setImageResource(R.drawable.btn_add_style);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
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
    public void onDestroy() {
        super.onDestroy();
        if (mSendCallback != null)
            mSendCallback.onCloseContainer();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_photos:
                openPictureView(mSendCallback);
                break;
            case R.id.btn_videos:
                openVideoView(mSendCallback);
                break;
            case R.id.btn_documents:
                openDocumentView(mSendCallback);
                break;
        }
    }
}