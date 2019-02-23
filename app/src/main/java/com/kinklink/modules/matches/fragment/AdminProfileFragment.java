package com.kinklink.modules.matches.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.kinklink.modules.authentication.activity.MatchUserAdminActivity;
import com.kinklink.modules.authentication.listener.AdapterPositionListener;
import com.kinklink.modules.authentication.model.BasicListInfoModel;
import com.kinklink.modules.authentication.model.GetAdminDetailModel;
import com.kinklink.modules.authentication.model.InterestsModel;
import com.kinklink.modules.authentication.model.ProfileImageModel;
import com.kinklink.modules.chat.activity.ChatActivity;
import com.kinklink.modules.matches.activity.AdminProfileActivity;
import com.kinklink.modules.matches.adapter.MatchPhotosAdapter;
import com.kinklink.server_task.WebService;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AdminProfileFragment extends Fragment implements View.OnClickListener {
    private static final String USER_ID = "";
    private Context mContext;
    private TextView action_bar_heading, tv_my_name, tv_my_age, tv_my_gender, tv_na_item,
            tv_intent, tv_body_type, tv_ethnicity, tv_city_town, tv_work, tv_education,
            tv_about_user, btn_try_again, tv_interest, tv_preferences, btn_verify_me;
    private ImageView iv_user_image, iv_back, iv_settings, iv_verify_check;
    private RelativeLayout rl_edit, rl_main_activity;
    private RecyclerView photos_recycler_view;
    private ImageView iv_user_chat;

    private ArrayList<ProfileImageModel> imagesList;
    private ArrayList<InterestsModel> interestsList;
    private ArrayList<BasicListInfoModel> prefList;

    private MatchPhotosAdapter matchPhotosAdapter;
    private GetAdminDetailModel getUser;
    private LinearLayout ly_no_network;

    private String gender;

    private Progress progress;

    // variable to track event time
    private long mLastClickTime = 0;
    private String userId;


    public AdminProfileFragment() {
    }

    public static AdminProfileFragment newInstance(String adminId) {
        Bundle args = new Bundle();
        AdminProfileFragment fragment = new AdminProfileFragment();
        fragment.setArguments(args);
        args.putString(USER_ID, adminId);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            if (getArguments().getString(USER_ID) != null) {
                userId = getArguments().getString(USER_ID);
            }
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_profile, container, false);
        initView(view);
        progress = new Progress(mContext);
        imagesList = new ArrayList<>();
        interestsList = new ArrayList<>();

        action_bar_heading.setText(R.string.admin_profile);
        iv_back.setOnClickListener(this);
        iv_user_chat.setOnClickListener(this);


        getUserDetails();
        // Setting Adapter to display Image gallery and Interests
        galleryInterestAdapter();

        return view;

    }

    private void initView(View view) {
        action_bar_heading = ((AdminProfileActivity) mContext).findViewById(R.id.action_bar_heading);

        tv_my_name = view.findViewById(R.id.tv_my_name);
        iv_user_chat = view.findViewById(R.id.iv_user_chat);
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

        rl_main_activity = ((AdminProfileActivity) mContext).findViewById(R.id.rl_main_activity);
        ly_no_network = ((AdminProfileActivity) mContext).findViewById(R.id.ly_no_network);
        btn_try_again = ((AdminProfileActivity) mContext).findViewById(R.id.btn_try_again);

        iv_back = ((AdminProfileActivity) mContext).findViewById(R.id.iv_back);

        iv_back.setVisibility(View.VISIBLE);

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    // Setting Adapter to display Image gallery and Interests
    private void galleryInterestAdapter() {
        // Display images in horizontal recycler view
        matchPhotosAdapter = new MatchPhotosAdapter(imagesList, getActivity(), new AdapterPositionListener() {
            @Override
            public void getPosition(int position) {
                Intent intent = new Intent(mContext, MatchUserAdminActivity.class);
                intent.putExtra("image_index", position);
                intent.putExtra("from","admin");
                intent.putExtra("getAdminDetail", getUser);
                startActivity(intent);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        photos_recycler_view.setLayoutManager(layoutManager);
        photos_recycler_view.setAdapter(matchPhotosAdapter);

    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        progress.dismiss();

        switch (v.getId()) {
            case R.id.iv_back:
                ((AdminProfileActivity) mContext).onBackPressed();
                break;


            case R.id.iv_user_chat:
                if (AppHelper.isConnectingToInternet(mContext)) {
                    Intent chatIntent = new Intent(mContext, ChatActivity.class);
                    chatIntent.putExtra("otherUID", userId);
                    startActivity(chatIntent);
                } else {
                    CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.alert_no_network));
                }
                break;
        }
    }

    // Get User details Api
    private void getUserDetails() {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

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
                            getUser = gson.fromJson(String.valueOf(js), GetAdminDetailModel.class);

                            setUserData();
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
            api.callApi("user/getAdminProfile/?id=" + userId, Request.Method.GET, null);
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

                    getUserDetails();
                }
            });
        }
    }


    // Set Get User details response
    private void setUserData() {
        if (getUser.getAdmin().getProfile_image() != null) {
            //Picasso.with(mContext).load(getUser.getAdmin().getProfile_image()).placeholder(R.drawable.user_place).fit().into(iv_user_image);
            Glide.with(mContext).load(getUser.getAdmin().getProfile_image()).apply(new RequestOptions().placeholder(R.drawable.user_place)).into(iv_user_image);

        } else {
            //Picasso.with(mContext).load(getUser.getAdmin().getProfile_image()).placeholder(R.drawable.user_place).fit().into(iv_user_image);

        }
        tv_my_name.setText(getUser.getAdmin().getName());

        SimpleDateFormat dateFormatprev = new SimpleDateFormat("yyyy-MM-dd");
        Date d = null;
        try {
            d = dateFormatprev.parse(getUser.getAdmin().getDate_of_birth());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM-dd-yyyy");
        String changedDate = dateFormat.format(d);

        tv_my_age.setText(changedDate + ",");
        switch (getUser.getAdmin().getGender()) {
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
        tv_ethnicity.setText(getUser.getAdmin().getEthnicityName());
        tv_education.setText(getUser.getAdmin().getEducationName());
        if (getUser.getAdmin().getKink().size() != 0) {
            for (int i = 0; i < getUser.getAdmin().getKink().size(); i++) {
                InterestsModel model = new InterestsModel();
                model.interest = getUser.getAdmin().getKink().get(i).getInterest();
                model.interestId = getUser.getAdmin().getKink().get(i).getInterestId();
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

        tv_preferences.setText(str_interest);

        if (!getUser.getAdmin().getWork().equals("")) {
            String work = getUser.getAdmin().getWork().substring(0, 1).toUpperCase() + getUser.getAdmin().getWork().substring(1);
            tv_work.setText(work);
        }

        tv_about_user.setText(getUser.getAdmin().getAbout());


        if (getUser.getAdmin().getProfile_image() != null) {

            ProfileImageModel imageModal = new ProfileImageModel();
            imageModal.profileUrl = getUser.getAdmin().getProfile_image();
            imagesList.add(imageModal);

            matchPhotosAdapter.notifyDataSetChanged();
            tv_na_item.setVisibility(View.GONE);
            photos_recycler_view.setVisibility(View.VISIBLE);

        } else {
            tv_na_item.setVisibility(View.VISIBLE);
            photos_recycler_view.setVisibility(View.GONE);
        }


    }


}
