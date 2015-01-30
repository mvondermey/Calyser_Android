package com.wuala.websocket.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.wuala.websocket.R;
import com.wuala.websocket.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class VideoPlayActivity extends Activity implements MediaPlayer.OnCompletionListener {

    private VideoView mVideoView;
    //video path
    private String url = "";


    /**
     * the videoView play the video by the time after user rotary screen
     *
     * @param outState
     */
    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        int sec = (int) outState.getLong("time");
        mVideoView.seekTo(sec);
        super.onRestoreInstanceState(outState);
    }

    /**
     * store the time before user rotary screen
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        int sec = mVideoView.getCurrentPosition();
        outState.putLong("time", sec);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avtivity_video_play);
        url = getIntent().getExtras().getString("url");
        mVideoView = (VideoView) findViewById(R.id.videoview);
        mVideoView.setVideoPath(url);
        mVideoView.setOnCompletionListener(this);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(mVideoView);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        mVideoView.setLayoutParams(params);
        mVideoView.setMediaController(mediaController);
        mVideoView.start();
        // if the video can not play this video it will open other video player
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
//                if (url.equals(currentPath)) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(url)), "video/*");
                startActivity(intent);
                finish();
                return true;
            }
        });
    }

//    public void makePlayList() {
//        urlList.clear();
//        File parentFile = new File((new File(url)).getParent());
//        for (File file : parentFile.listFiles()) {
//            if (file.getAbsolutePath().equals(url))
//                urlList.add(0, file.getAbsolutePath());
//            else
//                urlList.add(file.getAbsolutePath());
//        }
//    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
//        i++;
//        if (i < urlList.size()) {
//            currentPath = urlList.get(i);
//            mVideoView.setVideoPath(urlList.get(i));
//            mVideoView.start();
//        }
    }
}
