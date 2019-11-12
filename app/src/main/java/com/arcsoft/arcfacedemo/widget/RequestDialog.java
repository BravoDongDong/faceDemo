package com.arcsoft.arcfacedemo.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.arcsoft.arcfacedemo.R;

public class RequestDialog extends AlertDialog {
    private ProgressBar progressBar;

    protected RequestDialog(@NonNull Context context) {
        super(context);
        progressBar = (ProgressBar) LayoutInflater.from(context).inflate(R.layout.horizontal_progress_bar, null);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.addView(progressBar);
        setView(linearLayout, 50, 50, 50,50);
        setCanceledOnTouchOutside(false);
        setTitle("提交请求中，请稍等");
    }



}
