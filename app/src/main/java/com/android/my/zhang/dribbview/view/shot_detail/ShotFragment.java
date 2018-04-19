package com.android.my.zhang.dribbview.view.shot_detail;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.my.zhang.dribbview.R;
import com.android.my.zhang.dribbview.dribbble.auth.Dribbble;
import com.android.my.zhang.dribbview.model.Shot;
import com.android.my.zhang.dribbview.model.Bucket;
import com.android.my.zhang.dribbview.utils.ModelUtils;
import com.android.my.zhang.dribbview.view.bucket_list.BucketListFragment;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShotFragment extends Fragment {

    public static final String KEY_SHOT = "shot";
    public static final int REQ_CHOOSE_BUCKETS = 100;

    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    Shot shot;
    ShotAdapter adapter;

    public static ShotFragment newInstance(@NonNull Bundle args) {
        ShotFragment fragment = new ShotFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        shot = ModelUtils.toObject(getArguments().getString(KEY_SHOT),
                new TypeToken<Shot>(){});
        adapter = new ShotAdapter(shot, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        //AsyncTaskCompat.executeParallel(new LoadLike());
        //AsyncTaskCompat.executeParallel(new LoadBucketsTask());
        new LoadLike().execute();
        new LoadBucketsTask().execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQ_CHOOSE_BUCKETS) {
            Log.d("on Result", "List size = " + data.getStringArrayListExtra(BucketListFragment.KEY_CHOOSING_LIST).size());
            List<String> added = new ArrayList<>();
            List<String> removed = new ArrayList<>();
            List<String> currentBucketIds = adapter.bucketIdList;
            List<String> updatadBucketIds = data.getStringArrayListExtra(BucketListFragment.KEY_CHOOSING_LIST);

            for (String id : currentBucketIds) {
                if (!updatadBucketIds.contains(id)) removed.add(id);
            }
            for (String id : updatadBucketIds) {
                if (!currentBucketIds.contains(id)) added.add(id);
            }

            AsyncTaskCompat.executeParallel(new UpdateShotBuckets(added, removed));
        }
    }

    private class LoadBucketsTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            try {
                List<Bucket> shotBucket = Dribbble.getShotBucket(shot.id);
                List<Bucket> userBucket = Dribbble.getUserBucketsAll();

                //Log.d("Getted List", "id " + shot.id + " shotLists size = " + shotBucket.size()
                //+ "userList size = " + userBucket.size());
                Set<String> userBucketIds = new HashSet<>();
                for (Bucket bucket : userBucket) {
                    userBucketIds.add(bucket.id);
                }

                List<String> userPutBucketIds = new ArrayList<>();
                for (Bucket bucket : shotBucket) {
                    if (userBucketIds.contains(bucket.id)) userPutBucketIds.add(bucket.id);
                }

                Log.d("Intersection", "intersectList size = " + userPutBucketIds.size());
                return userPutBucketIds.isEmpty() ? null : userPutBucketIds;

            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            adapter.updateShotBucket(strings);
        }
    }

    private class UpdateShotBuckets extends AsyncTask<Void, Void, Void> {

        private List<String> added;
        private List<String> removed;
        private Exception e;

        public UpdateShotBuckets(List<String> added, List<String> removed) {
            this.added = added;
            this.removed = removed;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                for (String id : added) {
                    Dribbble.addBucketShot(id, shot.id);
                }

                for (String id : removed) {
                    Dribbble.removeBucketShot(id, shot.id);
                }


            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void Void) {
            if (e == null) {
                adapter.updateShotBucket(added, removed);
            } else {
                e.printStackTrace();

            }
        }
    }

    private class LoadLike extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {

            try {
                return Dribbble.getIfLike(shot.id);
            } catch(IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            adapter.like = aBoolean;
            shot.liked = adapter.like;
            //adapter.setLike(shot.liked);
        }
    }

}