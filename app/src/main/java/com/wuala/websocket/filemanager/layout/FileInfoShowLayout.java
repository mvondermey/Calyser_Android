package com.wuala.websocket.filemanager.layout;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wuala.websocket.R;
import com.wuala.websocket.filemanager.data.FileInfo;
import com.wuala.websocket.util.DisplayUtil;
import com.wuala.websocket.util.Util;

import java.util.concurrent.locks.ReadWriteLock;


public class FileInfoShowLayout extends RelativeLayout {
    private TextView fileNameTextView = null;
    private ImageView fileIconImageView = null;
    private TextView fileSizeTextView = null;
    private ImageView optionImageView;

    public FileInfoShowLayout(Context context, FileInfo fileInfo, boolean isSelect) {
        super(context);

        fileNameTextView = new TextView(context);
        fileIconImageView = new ImageView(context);
        fileSizeTextView = new TextView(context);
        this.setPadding(DisplayUtil.dip2px(context, 10), 10, 0, 0);
        //file type imageview
        fileIconImageView.setId(100001);
        fileIconImageView.setImageDrawable(fileInfo.getfileIcon());
        LayoutParams layoutParamsOne = new LayoutParams(
                DisplayUtil.dip2px(context, 40), DisplayUtil.dip2px(context, 40));
        layoutParamsOne.addRule(RelativeLayout.ALIGN_TOP, RelativeLayout.TRUE);
        fileIconImageView.setScaleType(ImageView.ScaleType.CENTER);
        fileIconImageView.setLayoutParams(layoutParamsOne);
        RelativeLayout.LayoutParams relativeLayoutParams = (LayoutParams) fileIconImageView.getLayoutParams();
        relativeLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        fileIconImageView.setLayoutParams(relativeLayoutParams);
        this.addView(fileIconImageView);
        // alert operation button
        optionImageView = new ImageView(context);
        optionImageView.setId(100003);
        optionImageView.setImageResource(R.drawable.btn_item_menu);
        LayoutParams layoutParams = new LayoutParams(DisplayUtil.dip2px(context, 25), DisplayUtil.dip2px(context, 25));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutParams.setMargins(0, 0, DisplayUtil.dip2px(context, 10), 0);
        optionImageView.setLayoutParams(layoutParams);
        this.addView(optionImageView);
        //file name text view
        fileNameTextView.setId(100002);
        fileNameTextView.setSingleLine(true);
        LayoutParams layoutParamsTwo = new LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParamsTwo.leftMargin = 20;
        layoutParamsTwo.addRule(RelativeLayout.RIGHT_OF,
                fileIconImageView.getId());
        layoutParamsTwo.addRule(RelativeLayout.LEFT_OF,
                optionImageView.getId());
        layoutParamsTwo.addRule(RelativeLayout.CENTER_VERTICAL);
        fileNameTextView.setLayoutParams(layoutParamsTwo);
        fileNameTextView.setText(fileInfo.getFileName());
        fileNameTextView.setTextSize(18);
        fileNameTextView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        fileNameTextView.setTextColor(ColorStateList.valueOf(0xFF000000));
        this.addView(fileNameTextView);

    }

    public void setFileName(String filename) {
        this.fileNameTextView.setText(filename);
    }

    public void setFileIcon(Drawable icon) {
        this.fileIconImageView.setImageDrawable(icon);
    }

    public void setFileSize(String filesize) {
        this.fileSizeTextView.setText(filesize);
    }

    public ImageView getOptionImageView() {
        return optionImageView;
    }
}
