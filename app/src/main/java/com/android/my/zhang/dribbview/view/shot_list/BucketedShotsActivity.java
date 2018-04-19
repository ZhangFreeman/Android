package com.android.my.zhang.dribbview.view.shot_list;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.android.my.zhang.dribbview.view.base.SingleFragmentActivity;

public class BucketedShotsActivity extends SingleFragmentActivity {

    @NonNull
    @Override
    protected Fragment newFragment() {
        String id = getIntent().getStringExtra(ShotListFragment.KEY_SHOT_CONTAINER_ID);
        return ShotListFragment.newInstance(id);
    }

    @NonNull
    @Override
    protected String getActivityTitle() {
        return "bucketed shot";
    }
}