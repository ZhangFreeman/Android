package com.android.my.zhang.dribbview.view.shot_detail;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.my.zhang.dribbview.R;
import com.android.my.zhang.dribbview.view.base.BaseViewHolder;

import butterknife.BindView;


public class CommentViewHolder extends BaseViewHolder {

    @BindView(R.id.comment_list)LinearLayout commentList;
    @BindView(R.id.comment_button)TextView commentButton;
    @BindView(R.id.comment_loading) ProgressBar commentLoading;

    public CommentViewHolder(View itemView) {
        super(itemView);
    }
}
