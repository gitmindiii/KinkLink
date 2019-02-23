package com.kinklink.modules.matches.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kinklink.R;
import com.kinklink.modules.authentication.activity.KinkLinkParentActivity;
import com.kinklink.modules.matches.fragment.MatchProfileFragment;

public class MatchProfileActivity extends KinkLinkParentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_profile);

        String userId = getIntent().getStringExtra("match_user_id");

        addFragment(MatchProfileFragment.newInstance(userId), false, R.id.fragment_place);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}
