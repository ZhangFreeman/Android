package com.android.my.zhang.dribbview.view.user;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
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
import com.android.my.zhang.dribbview.view.shot_detail.ShotActivity;
import com.android.my.zhang.dribbview.view.shot_detail.ShotFragment;
import com.android.my.zhang.dribbview.view.shot_list.ShotViewHolder;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_USER = 0;
    private static final int VIEW_TYPE_SHOT = 1;
    private static final int VIEW_TYPE_LOADING = 2;

    private final User user;
    private final UserFragment fragment;
    private LoadMoreListener loadMoreListener;

    private List<Shot> data;
    boolean followed = false;
    boolean showLoading;

    public UserAdapter(@NonNull User user, @NonNull UserFragment fragment, @NonNull LoadMoreListener loadMoreListener) {
        this.user = user;
        this.fragment = fragment;
        this.loadMoreListener = loadMoreListener;
        data = new ArrayList<Shot>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_USER:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_header, parent, false);
                return new UserViewHolder(view);
            case VIEW_TYPE_SHOT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_shot, parent, false);
                return new ShotViewHolder(view);
            case VIEW_TYPE_LOADING:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_loading, parent, false);
                return new RecyclerView.ViewHolder(view){};
            default:
                return null;
        }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_LOADING) {
            loadMoreListener.onLoadMore();
        } else if (viewType == VIEW_TYPE_USER) {
            final UserViewHolder viewHolder = (UserViewHolder) holder;

            viewHolder.authorName.setText(user.name);
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(Uri.parse(user.avatar_url))
                    .setAutoPlayAnimations(true)
                    .build();
            viewHolder.authorImage.setController(controller);
            /* TODO: Insteresting BUG found here*/
            //new CheckFollowed(viewHolder).execute();

            viewHolder.userFollow.setText((followed ? R.string.unfollow : R.string.follow));
            Log.d("has followed", ": " + followed + " in user " + user.followed);
            viewHolder.userFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (followed) {
                        Log.d("Follow click", "pos");
                        new Unfollow(viewHolder).execute();
                    } else {
                        Log.d("Follow click", "neg");
                        new Follow(viewHolder).execute();
                    }
                }
            });
        } else {
            final Shot shot = data.get(position - 1);
            ShotViewHolder shotViewHolder = (ShotViewHolder) holder;
            shotViewHolder.commentCount.setText(String.valueOf(shot.comments_count));
            shotViewHolder.likeCount.setText(String.valueOf(shot.likes_count));
            shotViewHolder.bucketCount.setText(String.valueOf(shot.buckets_count));
            shotViewHolder.viewCount.setText(String.valueOf(shot.views_count));
            shot.user = user;

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(Uri.parse(shot.getImageUrl()))
                    .setAutoPlayAnimations(true)
                    .build();
            shotViewHolder.image.setController(controller);

            shotViewHolder.cover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = holder.itemView.getContext();
                    Intent intent = new Intent(context, ShotActivity.class);
                    intent.putExtra(ShotFragment.KEY_SHOT,
                            ModelUtils.toString(shot, new TypeToken<Shot>() {
                            }));
                    Log.d("Starting shot info.",  "Shot title" + shot.title);
                    intent.putExtra(ShotActivity.KEY_SHOT_TITLE, shot.title);

                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_USER
                : (position <= data.size()
                ? VIEW_TYPE_SHOT
                : VIEW_TYPE_LOADING);
    }

    @Override
    public int getItemCount() {
        return data.size() + 2;
    }

    public interface LoadMoreListener {
        void onLoadMore();
    }

    public void append(@NonNull List<Shot> moreShots) {
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

    private class Follow extends AsyncTask<Void, Void, Boolean> {

        UserViewHolder viewHolder;
        public Follow(UserViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                boolean followed = Dribbble.postFollow(user.username);
                Log.d("Check follow", "followed: " + followed);
                return followed;
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
            } catch(Exception e) {
                e.printStackTrace();
                Log.d("Unknow EXception", "");
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean follow) {
            followed = follow;
            viewHolder.userFollow.setText((follow ? R.string.unfollow : R.string.follow));
            notifyDataSetChanged();
        }
    }

    private class Unfollow extends AsyncTask<Void, Void, Boolean> {

        UserViewHolder viewHolder;
        public Unfollow(UserViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                boolean unfollowed = Dribbble.deleteFollow(user.username);
                Log.d("unfollow", "followed: " + unfollowed);
                return unfollowed;
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
            } catch(Exception e) {
                e.printStackTrace();
                Log.d("Unknow Exception", "");
            }
            return false;

        }

        @Override
        protected void onPostExecute(Boolean unfollow) {
            followed = !unfollow;
            viewHolder.userFollow.setText((followed ? R.string.unfollow : R.string.follow));
            notifyDataSetChanged();
        }
    }

    private class CheckFollowed extends AsyncTask<Void, Void, Boolean> {

        UserViewHolder viewHolder;
        public CheckFollowed(UserViewHolder viewHolder) {
            this.viewHolder = viewHolder;
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
            followed = follow;
            viewHolder.userFollow.setText((followed ? R.string.unfollow : R.string.follow));
            notifyDataSetChanged();
        }
    }
}
