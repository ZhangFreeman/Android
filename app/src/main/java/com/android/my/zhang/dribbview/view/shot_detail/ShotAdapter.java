package com.android.my.zhang.dribbview.view.shot_detail;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.my.zhang.dribbview.R;
import com.android.my.zhang.dribbview.dribbble.auth.Dribbble;
import com.android.my.zhang.dribbview.model.Comment;
import com.android.my.zhang.dribbview.model.Shot;
import com.android.my.zhang.dribbview.model.User;
import com.android.my.zhang.dribbview.utils.ModelUtils;
import com.android.my.zhang.dribbview.view.bucket_list.BucketListFragment;
import com.android.my.zhang.dribbview.view.bucket_list.ChooseBucketActivity;
import com.android.my.zhang.dribbview.view.user.UserActivity;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// ShotAdapter is used to display a Shot object as items in RecyclerView
class ShotAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_SHOT_IMAGE = 0;
    private static final int VIEW_TYPE_SHOT_INFO = 1;
    private static final int VIEW_TYPE_SHOT_COMMENT = 2;

    private final Shot shot;
    private final ShotFragment shotFragment;
    public ArrayList<String> bucketIdList;
    private List<Comment> comments;

    Boolean like;

    public ShotAdapter(@NonNull Shot shot, @NonNull ShotFragment shotFragment) {
        this.shotFragment = shotFragment;
        this.shot = shot;
        //bucketIdList = new ArrayList<String>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_SHOT_IMAGE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.shot_item_image, parent, false);
                return new ImageViewHolder(view);
            case VIEW_TYPE_SHOT_INFO:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.shot_item_info, parent, false);
                return new InfoViewHolder(view);
            case VIEW_TYPE_SHOT_COMMENT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.shot_item_comment, parent, false);
                return new CommentViewHolder(view);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_TYPE_SHOT_IMAGE:
                // play gif automatically

                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(Uri.parse(shot.getImageUrl()))
                        .setAutoPlayAnimations(true)
                        .build();
                ((ImageViewHolder) holder).image.setController(controller);

                break;
            case VIEW_TYPE_SHOT_INFO:
                InfoViewHolder shotDetailViewHolder = (InfoViewHolder) holder;
                shotDetailViewHolder.title.setText(shot.title);
                shotDetailViewHolder.authorName.setText(shot.user.name);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if(shot.description != null)
                        shotDetailViewHolder.description.setText(Html.fromHtml(shot.description, Html.FROM_HTML_MODE_COMPACT));
                } else {
                    if(shot.description != null)
                        shotDetailViewHolder.description.setText(Html.fromHtml(shot.description));
                }

                shotDetailViewHolder.authorPicture.setImageURI(Uri.parse(shot.user.avatar_url));

                shotDetailViewHolder.likeCount.setText(String.valueOf(shot.likes_count));
                shotDetailViewHolder.bucketCount.setText(String.valueOf(shot.buckets_count));
                shotDetailViewHolder.viewCount.setText(String.valueOf(shot.views_count));

                Drawable bucketIcon = shot.bucketed ?
                        ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_drafts_dribbble_18dp) :
                        ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_drafts_grey_18dp);

                Drawable likeIcon = shot.liked ?
                        ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_favorite_dribbble_18dp) :
                        ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_favorite_gray_18dp);
                shotDetailViewHolder.bucketButton.setImageDrawable(bucketIcon);
                shotDetailViewHolder.likeButton.setImageDrawable(likeIcon);

                shotDetailViewHolder.shareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        share(v.getContext());
                    }
                });
                shotDetailViewHolder.bucketButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bucket(v.getContext());
                    }
                });
                shotDetailViewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        likeClick();
                    }
                });
                shotDetailViewHolder.authorPicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context context = holder.itemView.getContext();
                        Intent intent = new Intent(context, UserActivity.class);
                        intent.putExtra(UserActivity.KEY_USER,
                                ModelUtils.toString(shot.user, new TypeToken<User>(){}));
                        intent.putExtra(UserActivity.KEY_USER_TITLE, shot.user.name);
                        Log.d("starting useractivity:", "...");
                        context.startActivity(intent);
                    }
                });

                break;
            case VIEW_TYPE_SHOT_COMMENT:

                final CommentViewHolder shotCommentViewHolder = (CommentViewHolder) holder;
                shotCommentViewHolder.commentButton.setText("Show Comments...(" + shot.comments_count + ")");
                shotCommentViewHolder.commentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shotCommentViewHolder.commentButton.setVisibility(View.GONE);
                        shotCommentViewHolder.commentLoading.setVisibility(View.VISIBLE);
                        loadComments(shotCommentViewHolder);
                    }
                });

                break;
            default:break;
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public void setLike(boolean islike) {
        like = islike;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_SHOT_IMAGE;
        } else if (position == 1) {
            return VIEW_TYPE_SHOT_INFO;
        } else {
            return VIEW_TYPE_SHOT_COMMENT;
        }
    }

    public void updateShotBucket(List<String> bucketsId) {
        //Log.d("on Result", "List = " + (bucketsId != null ? bucketIdList.size() : null));

        if (bucketIdList == null) {
            bucketIdList = new ArrayList<>();
        }

        if (bucketsId != null) bucketIdList.addAll(bucketsId);
        shot.bucketed = bucketIdList.isEmpty() ? false : true;

        notifyDataSetChanged();
    }

    public void updateShotBucket(List<String> added, List<String> removed) {
        if (bucketIdList == null)
            bucketIdList = new ArrayList<>();

        bucketIdList.addAll(added);
        bucketIdList.removeAll(removed);

        shot.buckets_count += added.size();
        shot.buckets_count -= removed.size();

        shot.bucketed = shot.buckets_count > 0 ? true : false;

        notifyDataSetChanged();
    }

    private void share(Context context) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shot.title + " " + shot.html_url);
        shareIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(shareIntent,
                context.getString(R.string.share_shot)));
    }

    private void loadComments(CommentViewHolder layout) {
        new loadCommentTask(layout).execute();
        //setupComments(layout);
    }

    private void bucket(Context context) {
        if (bucketIdList == null) return ;
        Intent intent = new Intent(context, ChooseBucketActivity.class);
        intent.putStringArrayListExtra(BucketListFragment.KEY_CHOOSING_LIST,
                bucketIdList);

        Log.d("bucket List send size", "size = " + bucketIdList.size());
        shotFragment.startActivityForResult(intent, ShotFragment.REQ_CHOOSE_BUCKETS);
    }

    private void likeClick() {
        if(like != null) {
            if (like) {
                new deleteLikeTask().execute();
            } else {
                new LikeTask().execute();
            }
        }
    }

    private void setupComments (LinearLayout layout) {

        for (Comment comment : comments) {
            View view = shotFragment.getLayoutInflater().inflate(R.layout.comment_item, null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ((TextView)view.findViewById(R.id.comment_content)).setText(Html.fromHtml(comment.body, Html.FROM_HTML_MODE_COMPACT));
            } else {
                ((TextView)view.findViewById(R.id.comment_content)).setText(Html.fromHtml(comment.body));
            }
            ((TextView)view.findViewById(R.id.comment_like_num)).setText((comment.likes_count + ""));
            ((TextView)view.findViewById(R.id.comment_author_name)).setText(comment.user.name);
            ((SimpleDraweeView)view.findViewById(R.id.comment_author_picture))
                    .setImageURI(Uri.parse(comment.user.avatar_url));
            layout.addView(view);
        }
    }

    private class loadCommentTask extends AsyncTask<Void, Void, Void> {

        public CommentViewHolder commentViewHolder;

        public loadCommentTask(CommentViewHolder viewHolder) {
            this.commentViewHolder = viewHolder;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                comments = Dribbble.getComments(shot.id);
            } catch (IOException e) {
                e.printStackTrace();

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (comments != null) {
                commentViewHolder.commentLoading.setVisibility(View.GONE);
                setupComments(commentViewHolder.commentList);
            } else {
                Snackbar.make(shotFragment.getView(), "Connection Error!", Snackbar.LENGTH_LONG).show();
                commentViewHolder.commentLoading.setVisibility(View.GONE);
                commentViewHolder.commentButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private class deleteLikeTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                return Dribbble.deleteLike(shot.id);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean succeed) {
            if (succeed) {
                like = false;
                shot.liked = like;
                notifyDataSetChanged();
            }
        }
    }

    private class LikeTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                return Dribbble.postLike(shot.id);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean succeed) {
            if (succeed) {
                like = succeed;
                shot.liked = like;
                notifyDataSetChanged();
            }

        }
    }

}

