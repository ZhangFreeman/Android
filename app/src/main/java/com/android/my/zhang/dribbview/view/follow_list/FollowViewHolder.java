package com.android.my.zhang.dribbview.view.follow_list;

import android.view.View;
import android.widget.TextView;

import com.android.my.zhang.dribbview.R;
import com.android.my.zhang.dribbview.view.base.BaseViewHolder;
import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.BindView;

public class FollowViewHolder extends BaseViewHolder {

    @BindView(R.id.follow_author_picture)SimpleDraweeView followPicture;
    @BindView(R.id.follow_author_name)TextView followName;
    @BindView(R.id.follow_layout) View followLayout;
    @BindView(R.id.follow_author_shot_num) TextView shotNum;

    public FollowViewHolder(View itemView) {
        super(itemView);
    }
}
