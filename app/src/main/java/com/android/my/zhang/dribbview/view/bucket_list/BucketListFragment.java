package com.android.my.zhang.dribbview.view.bucket_list;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.my.zhang.dribbview.R;
import com.android.my.zhang.dribbview.dribbble.auth.Dribbble;
import com.android.my.zhang.dribbview.model.Bucket;
import com.android.my.zhang.dribbview.view.base.SpaceItemDecoration;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BucketListFragment extends Fragment {

    public static final int REQ_CODE_NEW_BUCKET = 100;
    public static final String KEY_CHOOSING_MODE = "choose_mode";
    public static final String KEY_CHOOSING_LIST = "choose_list";

    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.fab) FloatingActionButton fab;

    private BucketListAdapter adapter;
    private boolean isChoosingMode;
    private List<String> chosenBucketList;

    public static BucketListFragment newInstance(boolean mode, ArrayList<String> chosenBucketList) {
        BucketListFragment fragment = new BucketListFragment();
        Bundle data = new Bundle();
        data.putBoolean(KEY_CHOOSING_MODE, mode);
        data.putStringArrayList(KEY_CHOOSING_LIST, chosenBucketList);

        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fab_recycler_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        isChoosingMode = getArguments().getBoolean(KEY_CHOOSING_MODE);
        if (isChoosingMode) {
            chosenBucketList = getArguments().getStringArrayList(KEY_CHOOSING_LIST);
            Log.d("chosen list", "chosen = " + chosenBucketList);
            if (chosenBucketList == null) chosenBucketList = new ArrayList<>();
            //Log.d("chosen list", "chosen size = " + chosenBucketList.size());
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new SpaceItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.spacing_medium)));

        adapter = new BucketListAdapter(new ArrayList<Bucket>(), new BucketListAdapter.LoadMoreListener() {
            @Override
            public void onLoadMore() {

                new LoadBucketTask(adapter.getDataCount() / Dribbble.COUNT_PER_PAGE + 1).execute();
            }
        }, this, isChoosingMode);
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewBucketDialogFragment dialogFragment = NewBucketDialogFragment.newInstance();
                dialogFragment.setTargetFragment(BucketListFragment.this, REQ_CODE_NEW_BUCKET);
                dialogFragment.show(getFragmentManager(), NewBucketDialogFragment.TAG);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_NEW_BUCKET && resultCode == Activity.RESULT_OK) {
            String bucketName = data.getStringExtra(NewBucketDialogFragment.KEY_BUCKET_NAME);
            String bucketDescription = data.getStringExtra(NewBucketDialogFragment.KEY_BUCKET_DESCRIPTION);
            if (!TextUtils.isEmpty(bucketName)) {
                new newBucketTask(bucketName, bucketDescription).execute();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isChoosingMode)
            inflater.inflate(R.menu.bucket_menu_chooing_mode, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            ArrayList<String> chosenBucketIds = adapter.getBucketIds();
            Intent intent = new Intent();
            intent.putStringArrayListExtra(KEY_CHOOSING_LIST, chosenBucketIds);
            getActivity().setResult(Activity.RESULT_OK, intent);
            Log.d("on save", "List size = " + chosenBucketIds.size());
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class newBucketTask extends AsyncTask<Void, Void, Bucket> {

        String name;
        String description;

        public newBucketTask(String name, String description) {
            this.name = name;
            this.description = description;
        }

        @Override
        protected Bucket doInBackground(Void... voids) {

            try {
                return Dribbble.newBucket(name, description);
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bucket bucket) {
            if (bucket != null) {

                adapter.preappand(bucket);
            } else {
                Snackbar.make(getView(), "Create Error!", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private class LoadBucketTask extends AsyncTask<Void, Void, List<Bucket>> {

        int page;
        public LoadBucketTask(int page) {
            this.page = page;
        }

        @Override
        protected List<Bucket> doInBackground(Void... params) {

            try {
                return Dribbble.getUserBuckets(page);
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Bucket> buckets) {
            
            if (buckets != null) {
                if (isChoosingMode) {
                    for (Bucket bucket : buckets) {
                        if (chosenBucketList.contains(bucket.id)) bucket.isChoosing = true;;
                    }
                }

                adapter.append(buckets);
                adapter.setShowLoading(buckets.size() == Dribbble.COUNT_PER_PAGE);
            } else {
                Snackbar.make(getView(), "Error!", Snackbar.LENGTH_LONG).show();
            }
        }
    }
}