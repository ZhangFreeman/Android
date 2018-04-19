package com.android.my.zhang.dribbview.view.user;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.my.zhang.dribbview.R;
import com.android.my.zhang.dribbview.dribbble.auth.Dribbble;
import com.android.my.zhang.dribbview.model.Shot;
import com.android.my.zhang.dribbview.model.User;
import com.android.my.zhang.dribbview.utils.ModelUtils;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserFragment extends Fragment {

    User user;
    UserAdapter adapter;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;


    public static UserFragment newInstance(@NonNull Bundle args) {
        UserFragment fragment = new UserFragment();
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
        user = ModelUtils.toObject(getArguments().getString(UserActivity.KEY_USER),
                new TypeToken<User>(){});

        adapter = new UserAdapter(user, this, new UserAdapter.LoadMoreListener() {
            @Override
            public void onLoadMore() {
                new LoadShotTask().execute();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        // fix the insteresting bug
        new CheckFollowed().execute();
    }


    private class LoadShotTask extends AsyncTask<Void, Void, List<Shot>> {

        public LoadShotTask( ) {
        }
        @Override
        protected List<Shot> doInBackground(Void... params) {
            int page = adapter.getDataCount() / Dribbble.COUNT_PER_PAGE + 1;
            try {
                return Dribbble.getAuthorShots(user.username, page);
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                //return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Shot> shots) {
            // this method is executed on UI thread!!!!
            if (shots != null) {
                adapter.append(shots);
                adapter.setShowLoading(shots.size() == Dribbble.COUNT_PER_PAGE);
            } else {
                //shots = new ArrayList<>();
                //adapter.setShowLoading(false);
                Snackbar.make(getView(), "Connection Error!", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private class CheckFollowed extends AsyncTask<Void, Void, Boolean> {

        //UserViewHolder viewHolder;
        public CheckFollowed() {
            //this.viewHolder = viewHolder;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                boolean follow = Dribbble.getIfFollow(user.username);
                Log.d("already followed", ": " + follow);
                return follow;
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean follow) {
            adapter.followed = follow;
            user.followed = follow;
            Log.d("result followed", ": " + follow);
            adapter.notifyDataSetChanged();
            //viewHolder.userFollow.setText((follow ? R.string.unfollow : R.string.follow));
            //notifyDataSetChanged();
        }
    }

}
