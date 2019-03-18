package com.kinklink.modules.matches.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.kinklink.R;
import com.kinklink.application.KinkLink;
import com.kinklink.helper.AppHelper;
import com.kinklink.helper.CustomToast;
import com.kinklink.helper.Progress;
import com.kinklink.modules.authentication.activity.EditProfileActivity;
import com.kinklink.modules.authentication.listener.AdapterPositionListener;
import com.kinklink.modules.authentication.model.BasicListInfoModel;
import com.kinklink.modules.authentication.model.GetUserDetailModel;
import com.kinklink.modules.authentication.model.InterestsModel;
import com.kinklink.modules.authentication.model.ProfileImageModel;
import com.kinklink.modules.matches.activity.MatchGalleryActivity;
import com.kinklink.modules.matches.activity.MyProfileActivity;
import com.kinklink.modules.matches.adapter.MatchPhotosAdapter;
import com.kinklink.server_task.WebService;
import com.kinklink.session.Session;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import static com.facebook.FacebookSdk.getApplicationContext;

public class MyProfileFragment extends Fragment implements View.OnClickListener {
    private Context mContext;
    private TextView action_bar_heading, tv_my_name, tv_my_age, tv_my_gender, tv_na_item,
            tv_intent, tv_body_type, tv_ethnicity, tv_city_town, tv_work, tv_education,
            tv_about_user, btn_try_again, tv_interest, tv_preferences, btn_verify_me,tv_full_address;
    private ImageView iv_user_image, iv_back, iv_settings, iv_verify_check;
    private RelativeLayout rl_edit, rl_main_activity;
    private RecyclerView photos_recycler_view;

    private ArrayList<ProfileImageModel> imagesList;
    private ArrayList<InterestsModel> interestsList;
    private ArrayList<BasicListInfoModel> prefList;

    private MatchPhotosAdapter matchPhotosAdapter;
    private GetUserDetailModel getUser;
    private LinearLayout ly_no_network;

    private String gender;
    private Session session;

    private Progress progress;

    // variable to track event time
    private long mLastClickTime = 0;

    public MyProfileFragment() {
    }

    public static MyProfileFragment newInstance() {
        Bundle args = new Bundle();
        MyProfileFragment fragment = new MyProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefList = new ArrayList<>();
        getPreferenceIntentList();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);
        session=new Session(mContext);
        init(view);

        progress = new Progress(mContext);
        imagesList = new ArrayList<>();
        interestsList = new ArrayList<>();


        // Setting Adapter to display Image gallery and Interests
        galleryInterestAdapter();

        action_bar_heading.setText(getString(R.string.my_profile));

        iv_back.setOnClickListener(this);
        iv_settings.setOnClickListener(this);
        rl_edit.setOnClickListener(this);
        btn_verify_me.setOnClickListener(this);

        return view;
    }

    private void init(View view) {
        tv_full_address=view.findViewById(R.id.tv_full_address);
        action_bar_heading = ((MyProfileActivity) mContext).findViewById(R.id.action_bar_heading);

        tv_my_name = view.findViewById(R.id.tv_my_name);
        tv_my_age = view.findViewById(R.id.tv_my_age);
        tv_my_gender = view.findViewById(R.id.tv_my_gender);
        tv_na_item = view.findViewById(R.id.tv_na_item);
        tv_intent = view.findViewById(R.id.tv_intent);
        tv_body_type = view.findViewById(R.id.tv_body_type);
        tv_ethnicity = view.findViewById(R.id.tv_ethnicity);
        tv_city_town = view.findViewById(R.id.tv_city_town);
        tv_work = view.findViewById(R.id.tv_work);
        tv_education = view.findViewById(R.id.tv_education);
        tv_about_user = view.findViewById(R.id.tv_about_user);
        tv_preferences = view.findViewById(R.id.tv_preferences);

        iv_verify_check = view.findViewById(R.id.iv_verify_check);
        iv_user_image = view.findViewById(R.id.iv_user_image);
        btn_verify_me = view.findViewById(R.id.btn_verify_me);

        rl_edit = view.findViewById(R.id.rl_edit);

        photos_recycler_view = view.findViewById(R.id.photos_recycler_view);
        tv_interest = view.findViewById(R.id.tv_interest);

        rl_main_activity = ((MyProfileActivity) mContext).findViewById(R.id.rl_main_activity);
        ly_no_network = ((MyProfileActivity) mContext).findViewById(R.id.ly_no_network);
        btn_try_again = ((MyProfileActivity) mContext).findViewById(R.id.btn_try_again);

        iv_back = ((MyProfileActivity) mContext).findViewById(R.id.iv_back);
        iv_settings = ((MyProfileActivity) mContext).findViewById(R.id.iv_settings);

        iv_back.setVisibility(View.VISIBLE);
        iv_settings.setVisibility(View.VISIBLE);
        Animation aniFade = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in);
        if (session.getRegistration().userDetail.is_profile_complete.equals("0")){
            rl_edit.startAnimation(aniFade);
        }

        iv_user_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MatchGalleryActivity.class);
                intent.putExtra("image_index", 0);
                intent.putExtra("from", "user");
                intent.putExtra("getUserDetail", getUser);
                startActivity(intent);
            }
        });



    }

    // Setting Adapter to display Image gallery and Interests
    private void galleryInterestAdapter() {
        // Display images in horizontal recycler view
        matchPhotosAdapter = new MatchPhotosAdapter(imagesList, getActivity(), new AdapterPositionListener() {
            @Override
            public void getPosition(int position) {
                Intent intent = new Intent(mContext, MatchGalleryActivity.class);
                intent.putExtra("image_index", position);
                intent.putExtra("from","user");
                intent.putExtra("getUserDetail", getUser);
                startActivity(intent);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        photos_recycler_view.setLayoutManager(layoutManager);
        photos_recycler_view.setAdapter(matchPhotosAdapter);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    // Get User details Api
    private void getMyProfile() {
        if (AppHelper.isConnectingToInternet(mContext)) {

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            rl_main_activity.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            Gson gson = new Gson();
                            getUser = gson.fromJson(String.valueOf(js), GetUserDetailModel.class);

                            // Setting My Profile Details
                            setMyProfile();

                        } else {
                            progress.dismiss();
                            rl_main_activity.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        rl_main_activity.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    rl_main_activity.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                }

            });
            api.callApi("user/userDetail", Request.Method.GET, null);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            rl_main_activity.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Preventing multiple clicks, using threshold of 1/2 second
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    getMyProfile();
                }
            });
        }
    }

    // Setting My Profile Details
    private void setMyProfile() {
        if (isAdded()) {
            if (getUser.userDetail.images.size() > 0) {
                Glide.with(mContext).load(getUser.userDetail.images.get(0).image).apply(new RequestOptions().placeholder(R.drawable.user_place)).into(iv_user_image);
                // Picasso.with(mContext).load(getUser.userDetail.images.get(0).image).placeholder(R.drawable.user_place).fit().into(iv_user_image);
            } else {
                //Picasso.with(mContext).load(getUser.userDetail.defaultImg).placeholder(R.drawable.user_place).fit().into(iv_user_image);
                Glide.with(mContext).load(getUser.userDetail.defaultImg).apply(new RequestOptions().placeholder(R.drawable.user_place)).into(iv_user_image);

            }

            if (getUser.userDetail.is_verify.equals("3")) {
                iv_verify_check.setVisibility(View.VISIBLE);
                btn_verify_me.setBackground(ContextCompat.getDrawable(mContext,R.drawable.bg_offer_accept_btn));
                btn_verify_me.setText(getResources().getString(R.string.verified));
            } else {
                iv_verify_check.setVisibility(View.GONE);
                btn_verify_me.setBackground(ContextCompat.getDrawable(mContext,R.drawable.bg_login_btn));
                btn_verify_me.setText(getResources().getString(R.string.verify_me));
            }

            if (!getUser.userDetail.body_type_name.equals("")) {
                tv_body_type.setText(getUser.userDetail.body_type_name);
            }

            if (!getUser.userDetail.ethnicity_name.equals("")) {
                tv_ethnicity.setText(getUser.userDetail.ethnicity_name);
            }

            if (!getUser.userDetail.education_name.equals("")) {
                tv_education.setText(getUser.userDetail.education_name);
            }

            String[] nameArray = getUser.userDetail.full_name.split(" ");
            StringBuilder builder = new StringBuilder();
            for (String s : nameArray) {
                if (s.length() != 0) {
                    String cap = (s.length() > 0 && s.length() == 1) ? s.substring(0, 1).toUpperCase() : s.substring(0, 1).toUpperCase() + s.substring(1);
                    builder.append(cap).append(" ");
                }
            }

            tv_my_name.setText(builder.toString());
            tv_my_age.setText(String.format(Locale.ENGLISH, "%d %s,", getUser.userDetail.age, getString(R.string.yr)));

            switch (getUser.userDetail.gender) {
                case "man":
                    gender = "Man";
                    break;

                case "woman":
                    gender = "Woman";
                    break;

                case "couple":
                    gender = "Couple";
                    break;

                case "transgender_male":
                    gender = "Transgender Male";
                    break;

                case "transgender_female":
                    gender = "Transgender Female";
                    break;

                case "neutral":
                    gender = "Non Binary";
                    break;


            }

            tv_my_gender.setText(gender);

            if (getUser.userDetail.images.size() > 0) {
                for (int i = 0; i < getUser.userDetail.images.size(); i++) {
                    ProfileImageModel imageModal = new ProfileImageModel();
                    imageModal.imageId = getUser.userDetail.images.get(i).userImageId;
                    imageModal.profileUrl = getUser.userDetail.images.get(i).image;
                    imageModal.profileUrl_thumb = getUser.userDetail.images.get(i).imageOriginal;
                    imagesList.add(imageModal);
                }
                matchPhotosAdapter.notifyDataSetChanged();
                tv_na_item.setVisibility(View.GONE);
                photos_recycler_view.setVisibility(View.VISIBLE);

            } else {
                tv_na_item.setVisibility(View.VISIBLE);
                photos_recycler_view.setVisibility(View.GONE);
            }


            String[] lookingArray = getUser.userDetail.looking_for.split(",");
            StringBuilder lookingBuilder = new StringBuilder();
            for (String s : lookingArray) {
                if (s.length() != 0) {
                    if (s.equals("man")) {
                        lookingBuilder.append("Man").append(", ");
                    }

                    if (s.equals("woman")) {
                        lookingBuilder.append("Woman").append(", ");
                    }

                    if (s.equals("couple")) {
                        lookingBuilder.append("Couple").append(", ");
                    }

                    if (s.equals("transgender_male")) {
                        lookingBuilder.append("Transgender Male").append(", ");
                    }

                    if (s.equals("transgender_female")) {
                        lookingBuilder.append("Transgender Female").append(", ");
                    }

                    if (s.equals("neutral")){
                        lookingBuilder.append("Non Binary").append(", ");
                    }
                }
            }
            String withoutLastComma = lookingBuilder.substring(0, lookingBuilder.length() - ", ".length());
            tv_intent.setText(withoutLastComma);

            if (!getUser.userDetail.full_address.equals("")) {
                String city = getUser.userDetail.full_address.substring(0, 1).toUpperCase() + getUser.userDetail.full_address.substring(1);
                tv_city_town.setText(city);
            }

            if (!getUser.userDetail.current_address.equals("") && !getUser.userDetail.current_address.equals("0")) {
                tv_full_address.setText(getUser.userDetail.current_address);
            }


            if (!getUser.userDetail.work.equals("")) {
                String work = getUser.userDetail.work.substring(0, 1).toUpperCase() + getUser.userDetail.work.substring(1);
                tv_work.setText(work);
            }

            if (!getUser.userDetail.preference.equals("")) {
                ArrayList<String> list = new ArrayList<>();
                list.addAll(Arrays.asList(getUser.userDetail.preference.split(",")));

                for (int i = 0; i < prefList.size(); i++) {
                    for (int j = 0; j < list.size(); j++) {
                        if (prefList.get(i).item_key.equals(list.get(j))) {
                            prefList.get(i).isChecked = true;
                        }
                    }
                }

                StringBuilder str = new StringBuilder();
                for (int p = 0; p < prefList.size(); p++) {
                    if (prefList.get(p).isChecked) {
                        String pref = prefList.get(p).selected_item.substring(0, 1).toUpperCase() + prefList.get(p).selected_item.substring(1);
                        if (str.toString().equals("")) {
                            str = new StringBuilder(pref + "");
                        } else {
                            str.append(", ").append(pref);
                        }
                    }
                }

                if (str.toString().endsWith(", ")) {
                    str = new StringBuilder(str.substring(0, str.length() - 2));
                }
                tv_preferences.setText(str.toString());
            }

            if (getUser.userDetail.interests.size() != 0) {
                for (int i = 0; i < getUser.userDetail.interests.size(); i++) {
                    InterestsModel model = new InterestsModel();
                    model.interest = getUser.userDetail.interests.get(i).interest;
                    model.interestId = getUser.userDetail.interests.get(i).interestId;
                    model.isChecked = true;
                    interestsList.add(model);
                }
            }

            String str_interest = "";
            for (int i = 0; i < interestsList.size(); i++) {
                String interest = interestsList.get(i).interest.substring(0, 1).toUpperCase() + interestsList.get(i).interest.substring(1);
                str_interest = String.format("%s, %s", interest, str_interest);
            }

            if (str_interest.endsWith(", ")) {
                str_interest = str_interest.substring(0, str_interest.length() - 2);
            }

            tv_interest.setText(str_interest);

            if (!getUser.userDetail.about.equals("")) {
                String about = getUser.userDetail.about.substring(0, 1).toUpperCase() + getUser.userDetail.about.substring(1);
                tv_about_user.setText(about);
            }

        }
    }

    @Override
    public void onClick(View v) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        progress.dismiss();

        switch (v.getId()) {
            case R.id.iv_back:
                ((MyProfileActivity) mContext).onBackPressed();
                break;

            case R.id.iv_settings:
                ((MyProfileActivity) mContext).addFragment(SettingsFragment.newInstance(), true, R.id.fragment_place);
                break;

            case R.id.rl_edit:       // Edit Profile Option Click
                Intent intent = new Intent(mContext, EditProfileActivity.class);
                intent.putExtra("EditProfile", "EditBasicInfo");
                startActivity(intent);
                break;

            case R.id.btn_verify_me:
                ((MyProfileActivity) mContext).addFragment(VerifyPhotoFragment.newInstance(), true, R.id.fragment_place);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        imagesList.clear();
        interestsList.clear();

        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();
        }
        // Get User details Api
        getMyProfile();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (progress != null) {
            progress.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (progress != null) {
            progress.dismiss();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (progress != null) {
            progress.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progress != null) {
            progress.dismiss();
        }
    }

    // Get Preferences Intent List
    private void getPreferenceIntentList() {
        if (AppHelper.isConnectingToInternet(mContext)) {

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            /*cv_registration.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);*/

                            JSONObject intentObject = js.getJSONObject("intentList");

                            Iterator<String> educationIterator = intentObject.keys();
                            while (educationIterator.hasNext()) {
                                String key = educationIterator.next();
                                BasicListInfoModel model = new BasicListInfoModel();
                                model.item_key = key;
                                model.selected_item = intentObject.optString(key);
                              /*  if (model.selected_item.equals("Gender Neutral")){
                                    model.selected_item="Non Binary";
                                }*/
                                prefList.add(model);
                            }
                        } else {
                            CustomToast.getInstance(mContext).showToast(mContext, message);
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
