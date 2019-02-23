package com.kinklink.modules.authentication.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.kinklink.R;
import com.kinklink.application.KinkLink;
import com.kinklink.helper.AppHelper;
import com.kinklink.helper.Constant;
import com.kinklink.helper.CustomToast;


import com.kinklink.helper.Utils;
import com.kinklink.modules.authentication.fragment.EditBasicInfoFragment;
import com.kinklink.modules.authentication.fragment.EditOtherInfoFragment;
import com.kinklink.modules.authentication.model.BasicListInfoModel;
import com.kinklink.server_task.WebService;
import com.kinklink.session.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class EditProfileActivity extends KinkLinkParentActivity {
    private boolean doubleBackToExitPressedOnce;

    private ImageView iv_step_one, iv_step_one_bullet, iv_step_two, iv_step_two_bullet, iv_step_three, iv_back;
    private TextView tv_step_two, tv_step_three, tv_basic_info, tv_other_info, tv_profile_pictures;
    private View status_view_1, status_view_2;

    public ArrayList<BasicListInfoModel> prefList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_profile);
        Session session = new Session(EditProfileActivity.this);
        session.setUserLoggedIn();   // Set User Logged In
        Utils.goToOnlineStatus(EditProfileActivity.this, Constant.ONLINE_STATUS); //Set Online Offline Status firebase
        init();

        prefList = new ArrayList<>();

        // Value to call Edit Basic Info from MyProfile
        if (getIntent().getStringExtra("EditProfile") != null) {
            // Setting Specific Fragment in Edit Activity
            addFragment(EditBasicInfoFragment.newInstance("enable_back"), false, R.id.edit_fragment_place);
        } else {
            // Setting Specific Fragment in Edit Activity
            addFragment(EditOtherInfoFragment.newInstance(), false, R.id.edit_fragment_place);
        }

        // Api calling to get Preference List
        getPreferenceIntentList();
    }

    private void init() {
        TextView action_bar_heading = findViewById(R.id.action_bar_heading);
        action_bar_heading.setText(getString(R.string.edit_profile));

        iv_back = findViewById(R.id.iv_back);
        iv_step_one = findViewById(R.id.iv_step_one);
        iv_step_one_bullet = findViewById(R.id.iv_step_one_bullet);
        iv_step_two = findViewById(R.id.iv_step_two);
        iv_step_two_bullet = findViewById(R.id.iv_step_two_bullet);
        iv_step_three = findViewById(R.id.iv_step_three);
        tv_step_two = findViewById(R.id.tv_step_two);
        tv_step_three = findViewById(R.id.tv_step_three);
        tv_basic_info = findViewById(R.id.tv_basic_info);
        tv_other_info = findViewById(R.id.tv_other_info);
        tv_profile_pictures = findViewById(R.id.tv_profile_pictures);
        status_view_1 = findViewById(R.id.status_view_1);
        status_view_2 = findViewById(R.id.status_view_2);

    }

    @Override
    public void onBackPressed() {
        Handler handler = new Handler();
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();

            FragmentManager.OnBackStackChangedListener listener = new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.edit_fragment_place);

                    if (fragment instanceof EditOtherInfoFragment) {
                        setOtherInfoFragmentActive();
                        
                    } else if (fragment instanceof EditBasicInfoFragment) {
                        setBasicInfoFragmentActive();
                    }
                }
            };
            getSupportFragmentManager().addOnBackStackChangedListener(listener);

        } else {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.edit_fragment_place);
            if (fragment instanceof EditOtherInfoFragment) {
                setBasicInfoFragmentActive();
                replaceFragment(EditBasicInfoFragment.newInstance(""), false, R.id.edit_fragment_place);

            } else if (fragment instanceof EditBasicInfoFragment) {
                if (getIntent().getStringExtra("EditProfile") != null) {
                    finish();

                } else if (!doubleBackToExitPressedOnce) {
                    this.doubleBackToExitPressedOnce = true;
                    CustomToast.getInstance(this).showToast(this, getString(R.string.click_again_to_exit));

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doubleBackToExitPressedOnce = false;
                        }
                    }, 2000);

                }else {
                    super.onBackPressed();
                }
            } else {
                super.onBackPressed();
            }
        }
    }

    // Set Basic Info Fragment active
    private void setBasicInfoFragmentActive() {
        // Value to call Edit Basic Info from MyProfile
        if (getIntent().getStringExtra("EditProfile") != null) {
            iv_back.setVisibility(View.VISIBLE);
        } else {
            iv_back.setVisibility(View.GONE);
        }
        iv_step_one.setVisibility(View.GONE);
        iv_step_one_bullet.setVisibility(View.VISIBLE);
        iv_step_two.setVisibility(View.GONE);
        iv_step_two_bullet.setVisibility(View.GONE);
        tv_step_two.setVisibility(View.VISIBLE);
        iv_step_three.setVisibility(View.GONE);
        tv_step_three.setVisibility(View.VISIBLE);
        Utils.setTypeface(tv_basic_info, this, R.font.lato_bold);
        Utils.setTypeface(tv_other_info, this, R.font.lato_regular);
        Utils.setTypeface(tv_profile_pictures, this, R.font.lato_regular);

        status_view_1.setBackgroundColor(getResources().getColor(R.color.gray));
        status_view_2.setBackgroundColor(getResources().getColor(R.color.gray));
    }

    // Set Other Info Fragment active
    private void setOtherInfoFragmentActive() {
        iv_back.setVisibility(View.VISIBLE);
        iv_step_one.setVisibility(View.VISIBLE);
        iv_step_one_bullet.setVisibility(View.GONE);
        iv_step_two.setVisibility(View.GONE);
        iv_step_two_bullet.setVisibility(View.VISIBLE);
        tv_step_two.setVisibility(View.GONE);
        iv_step_three.setVisibility(View.GONE);
        tv_step_three.setVisibility(View.VISIBLE);
        Utils.setTypeface(tv_basic_info, this, R.font.lato_regular);
        Utils.setTypeface(tv_other_info, this, R.font.lato_bold);
        Utils.setTypeface(tv_profile_pictures, this, R.font.lato_regular);

        status_view_1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        status_view_2.setBackgroundColor(getResources().getColor(R.color.gray));

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    // Get Preferences Intent List
    private void getPreferenceIntentList() {
        if (AppHelper.isConnectingToInternet(this)) {

            WebService api = new WebService(this, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            JSONObject intentObject = js.getJSONObject("intentList");

                            Iterator<String> educationIterator = intentObject.keys();
                            while (educationIterator.hasNext()) {
                                String key = educationIterator.next();
                                BasicListInfoModel model = new BasicListInfoModel();
                                model.item_key = key;
                                model.selected_item = intentObject.optString(key);
                               /* if (model.selected_item.equals("Gender Neutral")){
                                    model.selected_item="Neutral";
                                }*/
                                prefList.add(model);
                            }
                        } else {
                            CustomToast.getInstance(EditProfileActivity.this).showToast(EditProfileActivity.this, message);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {

                }

            });
            api.callApi("getIntent", Request.Method.GET, null);
        }
    }
}
