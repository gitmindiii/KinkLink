package com.kinklink.modules.authentication.activity;

import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.kinklink.R;
import com.kinklink.helper.Progress;
import com.kinklink.modules.authentication.listener.AdapterPositionListener;
import com.kinklink.modules.authentication.model.GetAdminDetailModel;
import com.kinklink.modules.authentication.model.GetUserDetailModel;
import com.kinklink.modules.authentication.model.ProfileImageModel;
import com.kinklink.modules.matches.activity.MatchGalleryActivity;
import com.kinklink.modules.matches.adapter.GalleryRecyclerAdapter;
import com.kinklink.modules.matches.adapter.GalleryViewPagerAdapter;

import java.util.ArrayList;

public class MatchUserAdminActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {
    private ImageView iv_back;
    private int image_index;

    private ArrayList<ProfileImageModel> imagesList;
    private RelativeLayout rl_match_gallery;
    private ViewPager gallery_view_pager;
    private GalleryViewPagerAdapter pagerAdapter;
    private RecyclerView galleryRecyclerView;
    private GalleryRecyclerAdapter recyclerAdapter;
    private Progress progress;

    // variable to track event time
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_gallery);

        progress = new Progress(MatchUserAdminActivity.this);
        imagesList = new ArrayList<>();
        GetAdminDetailModel getAdminDetailModel = (GetAdminDetailModel) getIntent().getSerializableExtra("getAdminDetail");

        // Get User Id whose gallery Images has to display
        // Image Index on which end user clicked
        if (getIntent() != null) {
            if (getIntent().getIntExtra("image_index", 0) != 0) {
                image_index = getIntent().getIntExtra("image_index", 0);
            }
        }

        init();

        // Setting Gallery Adapter
        galleryAdapters();

        // Getting User Images and adding in list to display
        setUserData(getAdminDetailModel);

        // Click Listeners
        iv_back.setOnClickListener(this);
    }

    // Setting Gallery Adapter
    private void galleryAdapters() {
        // View pager adapter to display profile image
        pagerAdapter = new GalleryViewPagerAdapter(MatchUserAdminActivity.this, imagesList);
        gallery_view_pager.setAdapter(pagerAdapter);
        gallery_view_pager.setOnPageChangeListener(this);

        // Adapter to set recycler view to display images in bottom as gallery
        recyclerAdapter = new GalleryRecyclerAdapter(imagesList, this, new AdapterPositionListener() {
            @Override
            public void getPosition(int position) {
                gallery_view_pager.setCurrentItem(position, true);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        galleryRecyclerView.setLayoutManager(layoutManager);
        galleryRecyclerView.setAdapter(recyclerAdapter);

    }

    private void init() {
        rl_match_gallery = findViewById(R.id.rl_match_gallery);
        gallery_view_pager = findViewById(R.id.gallery_view_pager);
        rl_match_gallery.setVisibility(View.GONE);
        iv_back = findViewById(R.id.iv_back);
        galleryRecyclerView = findViewById(R.id.gallery_recycler_view);
    }

    // Getting User Images and adding in list to display
    private void setUserData(GetAdminDetailModel getUser) {
        if (getUser.getAdmin().getProfile_image()!=null) {

                ProfileImageModel imageModal = new ProfileImageModel();
                imageModal.imageId = "";
                imageModal.profileUrl = getUser.getAdmin().getProfile_image();
                imagesList.add(imageModal);


            imagesList.get(image_index).isSelected = true;

            rl_match_gallery.setVisibility(View.VISIBLE);
            pagerAdapter.notifyDataSetChanged();
            gallery_view_pager.setCurrentItem(image_index, true);
            galleryRecyclerView.scrollToPosition(image_index);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {     // Setting view pager item
        for (ProfileImageModel model : imagesList) {
            model.isSelected = false;
        }
        imagesList.get(position).isSelected = true;
        galleryRecyclerView.smoothScrollToPosition(position);
        recyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(progress.isShowing()){
            progress.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(progress.isShowing()){
            progress.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(progress.isShowing()){
            progress.dismiss();
        }

    }
}
