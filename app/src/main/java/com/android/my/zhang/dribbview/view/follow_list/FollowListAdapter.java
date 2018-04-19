package com.android.my.zhang.dribbview.view.follow_list;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.my.zhang.dribbview.R;
import com.android.my.zhang.dribbview.model.User;
import com.android.my.zhang.dribbview.utils.ModelUtils;
import com.android.my.zhang.dribbview.view.user.UserActivity;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class FollowListAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_LOADING = 2;

    private List<User> data;
    boolean showLoading = true;
    private LoadMoreListener loadMoreListener;

    public FollowListAdapter(@NonNull List<User> data, @NonNull LoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_loading, parent, false);
            return new RecyclerView.ViewHolder(view) {};
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_item, parent, false);
            return new FollowViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_LOADING) {
            loadMoreListener.onLoadMore();
        } else {
            final User user = data.get(position);
            final FollowViewHolder followViewHolder = (FollowViewHolder) holder;
            followViewHolder.followName.setText(user.name);
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(Uri.parse(user.avatar_url))
                    .setAutoPlayAnimations(true)
                    .build();
            followViewHolder.followPicture.setController(controller);
            followViewHolder.shotNum.setText(user.shots_count + "");


            followViewHolder.followPicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = followViewHolder.itemView.getContext();
                    Intent intent = new Intent(context, UserActivity.class);
                    intent.putExtra(UserActivity.KEY_USER,
                            ModelUtils.toString(user, new TypeToken<User>(){}));
                    intent.putExtra(UserActivity.KEY_USER_TITLE, user.name);
                    Log.d("starting useractivity:", "...");
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return showLoading ? data.size() + 1 : data.size();
    }


    @Override
    public int getItemViewType(int position) {
        return position < data.size()
                ? VIEW_TYPE_USER
                : VIEW_TYPE_LOADING;
    }

    public void append(@NonNull List<User> moreShots) {
        data.addAll(moreShots);
        notifyDataSetChanged();
    }

    public int getDataCount() {
        return data.size();
    }

    public void setShowLoading(boolean showLoading) {
        this.showLoading = showLoading;
        notifyDataSetChanged();
    }

    public interface LoadMoreListener {
        void onLoadMore();
    }
}
