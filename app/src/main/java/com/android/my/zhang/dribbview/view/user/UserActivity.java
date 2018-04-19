package com.android.my.zhang.dribbview.view.user;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.android.my.zhang.dribbview.view.base.SingleFragmentActivity;

public class UserActivity extends SingleFragmentActivity {

    public static final String KEY_USER = "key_user";
    public static final String KEY_USER_TITLE = "key_user_title";

    @NonNull
    @Override
    protected Fragment newFragment() {
        return UserFragment.newInstance(getIntent().getExtras());
    }

    @NonNull
    @Override
    protected String getActivityTitle() {
        return getIntent().getStringExtra(KEY_USER_TITLE);
    }
}
