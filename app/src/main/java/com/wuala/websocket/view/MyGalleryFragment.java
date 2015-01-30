package com.wuala.websocket.view;

import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.wuala.websocket.R;
import com.wuala.websocket.activity.MainActivity;
import com.wuala.websocket.activity.MainApplication;
import com.wuala.websocket.callback.SendFileListener;
import com.wuala.websocket.util.Util;
import com.wuala.websocket.widget.TouchImageView;


import java.io.File;
import java.util.List;


public class MyGalleryFragment extends ContainerBaseFragment implements View.OnClickListener {
    private ViewPager viewPager;
    /**
     * file url list
     */
    private static List<String> myfileURLList;
    /**
     * current url locations
     */
    private static String myCurrentLoaction;
    /**
     * buttom layout
     */
    private LinearLayout bottomLayout;
    /**
     * main activity action bar
     */
    private RelativeLayout actionBar;
    /**
     * gallery fragment action bar
     */
    private FrameLayout myActionBar;
    private boolean enableDisplay = true;
    /**
     * timestamp
     */
    private long timeMark = 0;
    /**
     * match timestamp
     */
    private long matchMark = 0;
    /**
     * gallery action bar title
     */
    private TextView myActionBarHeader;
    /**
     * enable run callback
     */
    private boolean enableCallback = true;

    public static MyGalleryFragment newInstance(FragmentType type, SendFileListener sendCallback, List<String> fileURLList, String currentLoaction) {
        MainActivity.CURRENT_FRAGMENT = type;
        myfileURLList = fileURLList;
        myCurrentLoaction = currentLoaction;
        MyGalleryFragment fragment = new MyGalleryFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.layout_my_gallery, container, false);
        init(rootView);
        initActionBar();
        return rootView;
    }

    private void init(View rootView) {
        isDocument = false;
        tempFolder = new File(myCurrentLoaction);
        currentLocation = tempFolder.getParentFile().getAbsolutePath();
        bottomLayout = (LinearLayout) rootView.findViewById(R.id.layout_bottom);
        viewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
        viewPager.setAdapter(new ImageAdapter());
        viewPager.setOffscreenPageLimit(3);
        viewPager.setCurrentItem(myfileURLList.indexOf(myCurrentLoaction));
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                myActionBarHeader.setText((i + 1) + "/" + myfileURLList.size());
                tempFolder = new File(myfileURLList.get(i));
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        rootView.findViewById(R.id.layout_share).setOnClickListener(this);
        rootView.findViewById(R.id.layout_move).setOnClickListener(this);
        rootView.findViewById(R.id.layout_delete).setOnClickListener(this);
        timeMark = System.currentTimeMillis();
        matchMark = timeMark;
        myActionBar = (FrameLayout) rootView.findViewById(R.id.action_bar);
        myActionBarHeader = (TextView) rootView.findViewById(R.id.txt_title);
        rootView.findViewById(R.id.btn_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionBar.setVisibility(View.VISIBLE);
                timeMark = System.currentTimeMillis();
                getFragmentManager().popBackStack();
            }
        });
        TextView send = (TextView) rootView.findViewById(R.id.btn_right);
        if (Util.isReadOnly) {
            bottomLayout.setVisibility(View.GONE);
            send.setVisibility(View.VISIBLE);
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        } else {
            send.setVisibility(View.GONE);
        }

        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (timeMark == matchMark) {
                    bottomLayout.setVisibility(View.GONE);
                    myActionBar.setVisibility(View.GONE);
                }
            }
        }, 3000);
        if ((new File(MainApplication.CONTAINER_PATH + (Util.isPhoto ? "/photos/" : "/videos/")).listFiles().length <= 1)) {
            rootView.findViewById(R.id.layout_move).setEnabled(false);
            rootView.findViewById(R.id.btn_move).setBackgroundResource(R.drawable.tab_move_disable);
            ((TextView) rootView.findViewById(R.id.txt_move)).setTextColor(Color.argb(255, 199, 199, 199));
        }
    }

    /**
     * init action bar
     */
    private void initActionBar() {
        myActionBarHeader.setText((myfileURLList.indexOf(myCurrentLoaction) + 1) + "/" + myfileURLList.size());
        ImageButton btnLeft = getLeftButton();
        actionBar = (RelativeLayout) btnLeft.getParent();
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_share:
                break;
            case R.id.layout_move:
                isDocument = false;
                enableCallback = true;
                setOptionCallBackListener(new MediaContentFragment.OptionCallBackListener() {
                    @Override
                    public void optionCallBackListener() {
                        if (enableCallback) {
                            enableCallback = false;

                            onBackKeyDown();
                        }
                    }
                });
                getFloderPopupWindow(tempFolder, Util.isPhoto ? MainApplication.CONTAINER_PATH + "/photos" : MainApplication.CONTAINER_PATH + "/videos");
                floderPopupWindow.showAtLocation(viewPager, Gravity.BOTTOM, 0, 0);
                break;
            case R.id.layout_delete:
                isDocument = false;
                setOptionCallBackListener(new MediaContentFragment.OptionCallBackListener() {
                    @Override
                    public void optionCallBackListener() {
                        timeMark = System.currentTimeMillis();
                        getFragmentManager().popBackStack();
                        actionBar.setVisibility(View.VISIBLE);
                    }
                });
                deleteFile(tempFolder);
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public class ImageAdapter extends PagerAdapter {


        @Override
        public int getCount() {
            return myfileURLList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            final TouchImageView imageView = new TouchImageView(getActivity());
            final String location = myfileURLList.get(position);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

            ImageLoader.getInstance().loadImage("file://" + location, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view,
                                              Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);
                    imageView.setImageBitmap(loadedImage);
                }
            });

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (bottomLayout.getVisibility() == View.VISIBLE) {
                        bottomLayout.setVisibility(View.GONE);
                        myActionBar.setVisibility(View.GONE);
                    } else {
                        if (Util.isReadOnly)
                            bottomLayout.setVisibility(View.GONE);
                        else
                            bottomLayout.setVisibility(View.VISIBLE);
                        myActionBar.setVisibility(View.VISIBLE);
                    }
                }
            });
            container.addView(imageView, 0);
            return imageView;
        }
    }


    @Override
    public boolean onBackKeyDown() {
        actionBar.setVisibility(View.VISIBLE);
        timeMark = System.currentTimeMillis();
        getFragmentManager().popBackStack();
        return super.onBackKeyDown();
    }
}