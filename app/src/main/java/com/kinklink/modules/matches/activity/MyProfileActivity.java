package com.kinklink.modules.matches.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinklink.R;
import com.kinklink.modules.authentication.activity.KinkLinkParentActivity;
import com.kinklink.modules.matches.fragment.MyProfileFragment;
import com.kinklink.modules.matches.fragment.SettingsFragment;
import com.kinklink.modules.matches.fragment.VerifyPhotoFragment;

public class MyProfileActivity extends KinkLinkParentActivity {
    private TextView action_bar_heading;
    private ImageView iv_back, iv_settings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        init();

        if (getIntent() != null && getIntent().getStringExtra("verify_photo_notification") != null && !getIntent().getStringExtra("verify_photo_notification").equals("")) {
            addFragment(VerifyPhotoFragment.newInstance(), false, R.id.fragment_place);
        } else {
            addFragment(MyProfileFragment.newInstance(), false, R.id.fragment_place);
        }
    }

    private void init() {
        action_bar_heading = findViewById(R.id.action_bar_heading);
        iv_back = findViewById(R.id.iv_back);
        iv_settings = findViewById(R.id.iv_settings);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();

            final FragmentManager.OnBackStackChangedListener listener = new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_place);

                    if (fragment instanceof MyProfileFragment) {    // Setting My Profile Fragment Active on back press
                        iv_back.setVisibility(View.VISIBLE);
                        action_bar_heading.setText(getString(R.string.my_profile));
                        iv_settings.setVisibility(View.VISIBLE);

                    } else if (fragment instanceof SettingsFragment) {
                        iv_back.setVisibility(View.VISIBLE);
                        action_bar_heading.setText(getResources().getString(R.string.settings));
                        iv_settings.setVisibility(View.GONE);
                    }
                }
            };
            getSupportFragmentManager().addOnBackStackChangedListener(listener);

        } else {
            super.onBackPressed();
        }
    }
}
