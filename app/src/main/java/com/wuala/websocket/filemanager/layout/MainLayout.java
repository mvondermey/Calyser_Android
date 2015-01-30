package com.wuala.websocket.filemanager.layout;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.wuala.websocket.R;
import com.wuala.websocket.filemanager.util.FileOperationListener;
import com.wuala.websocket.util.Marco;
import com.wuala.websocket.util.Util;


public class MainLayout extends LinearLayout implements View.OnClickListener {
    /**
     * document file list
     */
    private ListView fileListView;
    /**
     *
     */
    private FileOperationListener fileOperationListener;
    private EditText searchEdit;
    private Context context;
    private ImageView closeButton;

    public ListView getFileListView() {
        return fileListView;
    }


    public MainLayout(final Context context, final FileOperationListener fileOperationListener) {
        super(context);
        this.context = context;
        this.fileOperationListener = fileOperationListener;

        LinearLayout searchAndSortView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.layout_main_search_sort, null, false);
        searchAndSortView.findViewById(R.id.btn_add_folder).setOnClickListener(this);
        searchAndSortView.findViewById(R.id.btn_sort_name).setOnClickListener(this);
        searchAndSortView.findViewById(R.id.btn_sort_date).setOnClickListener(this);
        if (Util.isReadOnly)
            searchAndSortView.setVisibility(View.GONE);
        else
            searchAndSortView.setVisibility(View.VISIBLE);
        searchEdit = (EditText) searchAndSortView.findViewById(R.id.edit_search);
        closeButton = (ImageView) searchAndSortView.findViewById(R.id.btn_close);
        closeButton.setOnClickListener(this);
        searchEdit.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    InputMethodManager inputmanger = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });
        this.setOrientation(LinearLayout.VERTICAL);
        fileListView = new ListView(context);
        fileListView.setCacheColorHint(0);
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                fileOperationListener.fileOperation(Marco.FILE_SEARCH, 0, searchEdit);
                if (charSequence.length() != 0) {
                    closeButton.setVisibility(View.VISIBLE);
                } else {
                    searchEdit.clearFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        this.addView(searchAndSortView);
        this.addView(fileListView, new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));

    }

    /**
     * hide the keybord
     *
     * @param context
     * @param view
     */
    public void hideKeybord(Context context, View view) {
        InputMethodManager inputmanger = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onClick(View view) {
        hideKeybord(context, searchEdit);
        switch (view.getId()) {
            case R.id.btn_add_folder:
                fileOperationListener.fileOperation(Marco.FILE_CREATE, 0, view);
                break;
            case R.id.btn_sort_name:
                fileOperationListener.fileOperation(Marco.FILE_SORT_BY_NAME, 0, view);
                break;
            case R.id.btn_sort_date:
                fileOperationListener.fileOperation(Marco.FILE_SORT_BY_DATE, 0, view);
                break;
            case R.id.btn_close:
                closeButton.setVisibility(View.GONE);
                searchEdit.setText("");
                break;
        }
    }
}
