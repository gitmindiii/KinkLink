package com.kinklink.modules.authentication.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.kinklink.R;
import com.kinklink.helper.CustomToast;
import com.kinklink.modules.authentication.fragment.CreateAccountFragment;
import com.kinklink.modules.authentication.fragment.SelectInterestFragment;
import com.kinklink.modules.authentication.fragment.SignUpVerifyPhotoFragment;
import com.kinklink.modules.authentication.fragment.UploadPhotosFragment;
import com.kinklink.session.Session;

public class RegistrationActivity extends KinkLinkParentActivity implements View.OnClickListener {
    private ImageView iv_back;
    private Session session;
    private String screenName;
    private String gender, looking_for;
    private boolean doubleBackToExitPressedOnce;

    // variable to track event time
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_registration);
        init();
        session = new Session(RegistrationActivity.this);
        screenName = session.getScreen();

        // Get gender from RegisterGenderActivity
        if (getIntent().getStringExtra("Gender") != null) {
            gender = getIntent().getStringExtra("Gender");
            looking_for = getIntent().getStringExtra("Looking_for");
        }

        // Setting Specific Fragment in Registration Activity
        setResgistrationScreen();

        // Click Listeners
        iv_back.setOnClickListener(this);
    }

    private void init() {
        iv_back = findViewById(R.id.iv_back);
    }

    // Set Registration Screen specific fragment
    private void setResgistrationScreen() {
        switch (screenName) {
            case "":
                addFragment(CreateAccountFragment.newInstance(gender, looking_for), false, R.id.reg_fragment_place);
                break;

            case "CreateAccountFragment":
                replaceFragment(CreateAccountFragment.newInstance(gender, looking_for), false, R.id.reg_fragment_place);
                break;

            case "SignUpVerifyPhotoFragment":
                replaceFragment(SignUpVerifyPhotoFragment.newInstance(), false, R.id.reg_fragment_place);
                break;

            case "UploadPhotosFragment":
                replaceFragment(UploadPhotosFragment.newInstance(), false, R.id.reg_fragment_place);
                break;

            case "SelectInterestFragment":
                replaceFragment(SelectInterestFragment.newInstance(), false, R.id.reg_fragment_place);


                break;


        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        session.setScreen(screenName);
    }

    @Override
    public void onBackPressed() {
        screenName = session.getScreen();
        if (screenName.equals("") || screenName.equals("CreateAccountFragment")) {
            super.onBackPressed();
            finish();
        } else {
            Handler handler = new Handler();
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();

            } else if (!doubleBackToExitPressedOnce) {

                this.doubleBackToExitPressedOnce = true;
                CustomToast.getInstance(this).showToast(this, getString(R.string.click_again_to_exit));

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            } else {
                super.onBackPressed();
                finishAffinity();
            }
        }
    }

    @Override
    public void onClick(View view) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (view.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
        }
    }

}
