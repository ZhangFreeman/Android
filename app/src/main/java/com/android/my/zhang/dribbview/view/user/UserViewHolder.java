package com.android.my.zhang.dribbview.view.user;

import android.view.View;
import android.widget.TextView;

import com.android.my.zhang.dribbview.R;
import com.android.my.zhang.dribbview.view.base.BaseViewHolder;
import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.BindView;

public class UserViewHolder extends BaseViewHolder {

    @BindView(R.id.author_name) TextView authorName;
    @BindView(R.id.author_picture) SimpleDraweeView authorImage;
    @BindView(R.id.user_follow) TextView userFollow;

    public UserViewHolder(View itemView) {
        super(itemView);
    }
}
