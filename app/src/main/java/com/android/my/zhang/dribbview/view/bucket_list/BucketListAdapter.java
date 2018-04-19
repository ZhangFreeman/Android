package com.android.my.zhang.dribbview.view.bucket_list;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.my.zhang.dribbview.R;
import com.android.my.zhang.dribbview.model.Bucket;
import com.android.my.zhang.dribbview.view.shot_list.BucketedShotsActivity;
import com.android.my.zhang.dribbview.view.shot_list.ShotListFragment;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class BucketListAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_BUCKET = 1;
    private static final int VIEW_TYPE_LOADING = 2;

    private List<Bucket> data;
    private LoadMoreListener loadMoreListener;
    private final BucketListFragment fragment;
    private boolean showLoading;
    private boolean onChoosing;

    public BucketListAdapter(@NonNull List<Bucket> data,
                             @NonNull LoadMoreListener loadMoreListener,
                             BucketListFragment fragment,
                             boolean onChoosing) {
        this.data = data;
        this.loadMoreListener = loadMoreListener;
        this.fragment = fragment;
        this.showLoading = true;
        this.onChoosing = onChoosing;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_BUCKET) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_bucket, parent, false);
            return new BucketViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_loading, parent, false);
            return new RecyclerView.ViewHolder(view) {};
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_LOADING) {
            loadMoreListener.onLoadMore();
        } else {
            final Bucket bucket = data.get(position);
            Context context = holder.itemView.getContext();
            // 0 -> 0 shot
            // 1 -> 1 shot
            // 2 -> 2 shots
            String bucketShotCountString = MessageFormat.format(
                    holder.itemView.getContext().getResources().getString(R.string.shot_count),
                    bucket.shots_count);

            BucketViewHolder bucketViewHolder = (BucketViewHolder) holder;
            bucketViewHolder.bucketName.setText(bucket.name);
            bucketViewHolder.bucketShotCount.setText(bucketShotCountString);
            if (onChoosing) {
                bucketViewHolder.bucketChosen.setVisibility(View.VISIBLE);
                bucketViewHolder.bucketChosen.setImageDrawable(
                        bucket.isChoosing ? ContextCompat.getDrawable(context, R.drawable.ic_check_box_black_24dp)
                                : ContextCompat.getDrawable(context, R.drawable.ic_check_box_outline_blank_black_24dp)
                );
                bucketViewHolder.bucketChosen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bucket.isChoosing = !bucket.isChoosing;
                        notifyDataSetChanged();
                    }
                });
            } else {
                bucketViewHolder.bucketChosen.setVisibility(View.GONE);
                bucketViewHolder.bucketLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shots(v.getContext(), bucket.id);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return showLoading ? data.size() + 1 : data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position < data.size()
                ? VIEW_TYPE_BUCKET
                : VIEW_TYPE_LOADING;
    }

    private void shots(Context context, String id) {
        Intent intent = new Intent(context, BucketedShotsActivity.class);
        intent.putExtra(ShotListFragment.KEY_SHOT_CONTAINER_ID, id);
        Log.d("bucket List to shots ", "is = " + id);
        fragment.startActivity(intent);
    }

    public void append(@NonNull List<Bucket> moreBuckets) {
        data.addAll(moreBuckets);
        notifyDataSetChanged();
    }

    public int getDataCount() {
        return data.size();
    }

    public void setShowLoading(boolean showLoading) {
        this.showLoading = showLoading;
        notifyDataSetChanged();
    }

    public void preappand(Bucket bucket) {
        data.add(0, bucket);
        notifyDataSetChanged();
    }

    public void setChosen(List<String> chosen) {

        for (Bucket bucket : data) {
            if (chosen.contains(bucket.id)) bucket.isChoosing = true;
        }
        notifyDataSetChanged();
    }

    public ArrayList<String> getBucketIds() {
        ArrayList<String> chosenBucketIds = new ArrayList<>();
        for (Bucket bucket : data) {
            if (bucket.isChoosing) chosenBucketIds.add(bucket.id);
        }
        return chosenBucketIds;
    }

    public interface LoadMoreListener {
        void onLoadMore();
    }
}