package com.android.my.zhang.dribbview.view.shot_list;

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.my.zhang.dribbview.R;
import com.android.my.zhang.dribbview.dribbble.auth.Dribbble;
import com.android.my.zhang.dribbview.model.Shot;
import com.android.my.zhang.dribbview.view.base.SpaceItemDecoration;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShotListFragment extends Fragment {

    @BindView(R.id.recycler_view) RecyclerView recyclerView;

    public static final String KEY_SHOT_MODE = "bucketed_shot";
    public static final String SHOT_CONTENT_ALL = "shots";
    public static final String KEY_SHOT_CONTAINER_ID = "shotsContainerId";

    public static final int LIST_TYPE_ALL = 1;
    public static final int LIST_TYPE_BUCKET = 2;
    public static final int LIST_TYPE_LIKE = 3;
    public static final int LIST_TYPE_FOLLOW = 4;

    private ShotListAdapter adapter;
    private int listType = LIST_TYPE_ALL;


    public static ShotListFragment newInstance(int listType) {
        ShotListFragment fragment = new ShotListFragment();
        Bundle data = new Bundle();
        data.putInt(KEY_SHOT_MODE, listType);

        fragment.setArguments(data);
        return fragment;
    }

    public static ShotListFragment newInstance (String id) {
        ShotListFragment fragment = new ShotListFragment();
        Bundle data = new Bundle();
        data.putInt(KEY_SHOT_MODE, LIST_TYPE_BUCKET);
        data.putString(KEY_SHOT_CONTAINER_ID, id);

        fragment.setArguments(data);
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        listType = getArguments().getInt(KEY_SHOT_MODE);

        Log.d("shot List", "mode String = " + listType);
        //if (mode != null) shotMode = mode;

        if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        else{
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        }

        //recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new SpaceItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.spacing_medium)));

        adapter = new ShotListAdapter(new ArrayList<Shot>(), new ShotListAdapter.LoadMoreListener() {
            @Override
            public void onLoadMore() {
                // this method will be called when the RecyclerView is display page starts from 1
                //AsyncTaskCompat.executeParallel(new LoadTypeShotTask());
                new LoadTypeShotTask().execute();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //if in bucket mode
        if (listType == LIST_TYPE_BUCKET) {
            inflater.inflate(R.menu.bucket_menu_chooing_mode, menu);
        }
    }

    private class LoadTypeShotTask extends AsyncTask<Void, Void, List<Shot>> {

        public LoadTypeShotTask( ) {
        }
        @Override
        protected List<Shot> doInBackground(Void... params) {

            int page = adapter.getDataCount() / Dribbble.COUNT_PER_PAGE + 1;
            try {

                switch(listType) {
                    case LIST_TYPE_LIKE:
                        return Dribbble.getLikeShots(page);
                    case LIST_TYPE_BUCKET:
                        String containerId = getArguments().getString(KEY_SHOT_CONTAINER_ID);
                        return Dribbble.getBucketShots(containerId, page);
                    case LIST_TYPE_FOLLOW:
                        return Dribbble.geFollowShots(page);
                    default:
                        return Dribbble.getShots(page);
                }

            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                //return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Shot> shots) {

            if (shots != null) {
                adapter.append(shots);
                adapter.setShowLoading(shots.size() == Dribbble.COUNT_PER_PAGE);
            } else {
                //shots = new ArrayList<>();
                adapter.setShowLoading(false);
                Snackbar.make(getView(), "Connection Error!", Snackbar.LENGTH_LONG).show();
            }
        }
    }

}