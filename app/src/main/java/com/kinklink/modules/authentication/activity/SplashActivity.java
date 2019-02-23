package com.kinklink.modules.authentication.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import com.kinklink.R;
import com.kinklink.modules.matches.activity.MainActivity;
import com.kinklink.session.Session;

import java.security.MessageDigest;

public class SplashActivity extends AppCompatActivity {
    private Session session;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        session = new Session(this);


        mRunnable = new Runnable() {
            @Override
            public void run() {
                // If user is logged in, Make User direct logged in
                if (session.getUserLoggedIn() && session.getRegistration() != null) {
                    // If user registration is not completed
                    if (session.getRegistration().userDetail.is_profile_complete.equals("0")) {
                        if (session.getRegistration().userDetail.profile_step.equals("0")
                                && session.getRegistration().userDetail.isEmailVerified.equals("1")) {

                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                            finish();


                        } else {    // If user is not logged in, Make user login
                            session.logout();
                        }
                    } else if (session.getRegistration().userDetail.is_profile_complete.equals("1")) {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    }
                } else {    // If user is not logged in, Make user login
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        mHandler.postDelayed(mRunnable, 3000);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mHandler.removeCallbacks(mRunnable);
    }

    //Get Hask Key for facebook integration
    private void getKeyHashFacebook() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
