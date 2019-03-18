package com.kinklink.modules.matches.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ablanco.zoomy.TapListener;
import com.ablanco.zoomy.Zoomy;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.kinklink.R;
import com.kinklink.modules.authentication.listener.AdapterPositionListener;
import com.kinklink.modules.authentication.model.ProfileImageModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MatchPhotosAdapter extends RecyclerView.Adapter<MatchPhotosAdapter.ViewHolder> {
    private ArrayList<ProfileImageModel> imagesList;
    private Context mContext;
    private AdapterPositionListener listener;

    // variable to track event time
    private long mLastClickTime = 0;

    public MatchPhotosAdapter(ArrayList<ProfileImageModel> imagesList, Activity mContext, AdapterPositionListener listener) {
        this.imagesList = imagesList;
        this.mContext = mContext;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_upload_images_recycler, parent, false);
        return new MatchPhotosAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ProfileImageModel imageModel = imagesList.get(position);
        holder.iv_user_image.setVisibility(View.VISIBLE);
        if (imagesList.get(position).profileUrl != null) {

            Glide.with(mContext).load(imageModel.profileUrl_thumb).apply(new RequestOptions().placeholder(R.drawable.app_icon)).into(holder.iv_user_image);

            //Picasso.with(mContext).load(imageModel.profileUrl).placeholder(R.drawable.app_icon).into(holder.iv_user_image);
        }

    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private RoundedImageView iv_user_image;
        private ImageView image_cancel;

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            iv_user_image = itemView.findViewById(R.id.iv_user_image);
            image_cancel = itemView.findViewById(R.id.image_cancel);
            image_cancel.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {
            // Preventing multiple clicks, using threshold of 1/2 second
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            listener.getPosition(getAdapterPosition());
        }
    }
}
