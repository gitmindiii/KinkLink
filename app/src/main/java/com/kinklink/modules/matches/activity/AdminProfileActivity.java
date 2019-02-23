package com.kinklink.modules.matches.activity;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinklink.R;
import com.kinklink.modules.authentication.activity.KinkLinkParentActivity;
import com.kinklink.modules.matches.fragment.AdminProfileFragment;
import com.kinklink.modules.matches.fragment.MyProfileFragment;
import com.kinklink.modules.matches.fragment.SettingsFragment;

public class AdminProfileActivity extends KinkLinkParentActivity {

    private String adminId;
    private ImageView iv_back;
    private TextView action_bar_heading;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);
        init();
        Intent intent=getIntent();
        if (intent!=null){
            adminId=intent.getStringExtra("adminId");
        }
        addFragment(AdminProfileFragment.newInstance(adminId), false, R.id.fragment_place);
    }

    private void init() {
        action_bar_heading = findViewById(R.id.action_bar_heading);
        iv_back = findViewById(R.id.iv_back);
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
                        //iv_settings.setVisibility(View.VISIBLE);

                    } else if (fragment instanceof SettingsFragment) {
                        iv_back.setVisibility(View.VISIBLE);
                        action_bar_heading.setText(getResources().getString(R.string.settings));
                        //iv_settings.setVisibility(View.GONE);
                    }
                }
            };
            getSupportFragmentManager().addOnBackStackChangedListener(listener);

        } else {
            super.onBackPressed();
        }
    }






    // Method to add fragment


}
