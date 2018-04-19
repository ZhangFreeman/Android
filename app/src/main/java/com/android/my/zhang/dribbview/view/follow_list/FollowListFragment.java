package com.android.my.zhang.dribbview.view.follow_list;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.my.zhang.dribbview.R;
import com.android.my.zhang.dribbview.dribbble.auth.Dribbble;
import com.android.my.zhang.dribbview.model.User;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class FollowListFragment extends Fragment {


    FollowListAdapter adapter;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;

    public static FollowListFragment newInstance() {
        FollowListFragment fragment = new FollowListFragment();
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

        adapter = new FollowListAdapter(new ArrayList<User>(), new FollowListAdapter.LoadMoreListener() {
            @Override
            public void onLoadMore() {
                // this method will be called when the RecyclerView is displayed
                // page starts from 1
                AsyncTaskCompat.executeParallel(
                        new LoadUserTask());
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }



    private class LoadUserTask extends AsyncTask<Void, Void, List<User>> {

        public LoadUserTask( ) {
        }
        @Override
        protected List<User> doInBackground(Void... params) {
            // this method is executed on non-UI thread
            int page = adapter.getDataCount() / Dribbble.COUNT_PER_PAGE + 1;
            try {
                return Dribbble.getFollowUsers(page);

            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                //return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<User> users) {
            if (users != null) {
                adapter.append(users);
                adapter.setShowLoading(users.size() == Dribbble.COUNT_PER_PAGE);
            } else {
                Snackbar.make(getView(), "Connection Error!", Snackbar.LENGTH_LONG).show();
            }
        }
    }

}
