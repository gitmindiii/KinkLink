package com.kinklink.modules.matches.adapter;

import android.content.Context;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.kinklink.R;
import com.kinklink.modules.authentication.adapter.ProfileImageAdapter;
import com.kinklink.modules.authentication.listener.ProfileImageListener;
import com.kinklink.modules.authentication.model.AvtarModel;
import com.kinklink.modules.authentication.model.ProfileImageModel;

import java.util.ArrayList;

public class AvatarViewAdapter extends RecyclerView.Adapter<AvatarViewAdapter.ViewHolder> {
    private ArrayList<AvtarModel.AvtarData> imagesList;
    private ProfileImageListener listener;
    private Context mContext;
    // variable to track event time
    private long mLastClickTime = 0;

    public AvatarViewAdapter(ArrayList<AvtarModel.AvtarData> imagesList, Context mContext, ProfileImageListener listener) {
        this.imagesList = imagesList;
        this.mContext = mContext;
        this.listener = listener;
    }

    @Override
    public AvatarViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_upload_images_recycler, parent, false);
        return new AvatarViewAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AvatarViewAdapter.ViewHolder holder, int position) {
        AvtarModel.AvtarData imageModel = imagesList.get(position);
        holder.iv_user_image.setVisibility(View.VISIBLE);
        if (imagesList.get(position).avatarUrl != null) {
            //Picasso.with(mContext).load(imageModel.profileUrl).placeholder(R.drawable.app_icon).into(holder.iv_user_image);
            Glide.with(mContext).load(imageModel.avatarUrl).apply(new RequestOptions().placeholder(R.drawable.app_icon)).into(holder.iv_user_image);
        }

            holder.image_cancel.setVisibility(View.VISIBLE);

    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private RoundedImageView iv_user_image;
        private ImageView image_cancel;

        ViewHolder(View itemView) {
            super(itemView);
            //    itemView.setOnClickListener(this);
            iv_user_image = itemView.findViewById(R.id.iv_user_image);
            image_cancel = itemView.findViewById(R.id.image_cancel);

            image_cancel.setOnClickListener(this);
            iv_user_image.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Preventing multiple clicks, using threshold of 1/2 second
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            switch (v.getId()) {
                case R.id.image_cancel:
                    listener.getPosition(getAdapterPosition(), false);
                    break;

                case R.id.iv_user_image:
                    listener.getPosition(getAdapterPosition(), true);
                    break;
            }

        }
    }
}

