package com.kinklink.modules.matches.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.ablanco.zoomy.TapListener;
import com.ablanco.zoomy.Zoomy;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.kinklink.R;
import com.kinklink.modules.authentication.model.ProfileImageModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GalleryViewPagerAdapter extends PagerAdapter {
    private Activity mContext;
    private ArrayList<ProfileImageModel> imageList;
    private LayoutInflater inflater;


    public GalleryViewPagerAdapter(Activity mContext, ArrayList<ProfileImageModel> imageList) {
        this.mContext = mContext;
        this.imageList = imageList;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = inflater.inflate(R.layout.view_match_gallery_view_pager, container, false);

        // create a ImageView programmatically
        ImageView imageView = view.findViewById(R.id.iv_user_image);
        final ProgressBar img_progress=view.findViewById(R.id.img_progress);
        Zoomy.Builder builder = new Zoomy.Builder(mContext).target(imageView);

        builder.tapListener(new TapListener() {
            @Override
            public void onTap(View v) {
                mContext.finish();
            }
        });
        builder.register();


       // Glide.with(mContext).load(imageList.get(position).profileUrl).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)).into(imageView);


        Glide.with(mContext).load(imageList.get(position).profileUrl).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                img_progress.setVisibility(View.GONE);
                return false;

            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, com.bumptech.glide.request.target.Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                img_progress.setVisibility(View.GONE);

                return false;
            }




        }).into(imageView);


        //Picasso.with(mContext).load(imageList.get(position).profileUrl).placeholder(R.drawable.user_place).fit().into(imageView);
        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        (container).removeView((View) object);
    }
}
