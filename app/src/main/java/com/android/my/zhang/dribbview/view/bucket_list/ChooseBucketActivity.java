package com.android.my.zhang.dribbview.view.bucket_list;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.android.my.zhang.dribbview.R;
import com.android.my.zhang.dribbview.view.base.SingleFragmentActivity;

import java.util.ArrayList;

public class ChooseBucketActivity extends SingleFragmentActivity {

    public static final String KEY_SHOT_TITLE = "chooseBucket_title";

    @NonNull
    @Override
    protected String getActivityTitle() {
        return getString(R.string.choose_bucket);
    }

    @NonNull
    @Override
    protected Fragment newFragment() {
        ArrayList<String> chosenBucketIds = getIntent().getStringArrayListExtra(
                BucketListFragment.KEY_CHOOSING_LIST);
        return BucketListFragment.newInstance(true, chosenBucketIds);
    }
}
