package com.kinklink.modules.matches.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ablanco.zoomy.TapListener;
import com.ablanco.zoomy.Zoomy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.kinklink.R;
import com.kinklink.application.KinkLink;
import com.kinklink.helper.AppHelper;
import com.kinklink.helper.Constant;
import com.kinklink.helper.CustomToast;
import com.kinklink.helper.Progress;
import com.kinklink.helper.TimeSpan;
import com.kinklink.modules.authentication.listener.AdapterPositionListener;
import com.kinklink.modules.authentication.model.GetUserDetailModel;
import com.kinklink.modules.authentication.model.InterestsModel;
import com.kinklink.modules.authentication.model.NotInterestModel;
import com.kinklink.modules.authentication.model.OnlineInfo;
import com.kinklink.modules.authentication.model.ProfileImageModel;
import com.kinklink.modules.chat.activity.ChatActivity;
import com.kinklink.modules.chat.model.Chat;
import com.kinklink.modules.matches.activity.MatchGalleryActivity;
import com.kinklink.modules.matches.activity.MatchProfileActivity;
import com.kinklink.modules.matches.adapter.MatchPhotosAdapter;
import com.kinklink.server_task.WebService;
import com.kinklink.session.Session;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MatchProfileFragment extends Fragment implements View.OnClickListener {
    private Context mContext;
    private String userId, gender, userName;

    private static final String USER_ID = "userId";
    private TextView tv_match_name, tv_match_age, tv_match_gender, tv_match_percentage, tv_intent, tv_body_type,
            tv_ethnicity, tv_city_town, tv_work, tv_education, tv_user_name, tv_about_user, tv_na_item, tv_on_offline,
            tv_miles, tv_full_address, tv_na_interest;
    private RecyclerView photos_recycler_view;
    private ArrayList<ProfileImageModel> imagesList;
    private ArrayList<InterestsModel> interestsList;
    private ArrayList<NotInterestModel> notInterestModelArrayList;
    private MatchPhotosAdapter matchPhotosAdapter;

    private ImageView iv_user_image, iv_back, iv_menu, iv_like_unlike, iv_favorite, iv_match_check, iv_user_chat;
    private LinearLayout ly_no_network, ly_offer_no_network;
    private TextView btn_make_offer, btn_try_again;

    private RelativeLayout rl_main_activity, rl_display_miles;
    private GetUserDetailModel getUser;
    private LinearLayout llTease, llMatchedKinks, llunMatchedKinks;

    private Dialog reportDialog, blockDialog;
    private TextView tv_user_block, tv_user_report;
    private CardView cv_profile_menu;
    private String likeRequestType, favoriteRequestType, isUserOnline = "offline";
    private Map<String, OnlineInfo> onlineList;
    private TextView tv_not_interst;
    private Progress progress;

    // variable to track event time
    private long mLastClickTime = 0;

    private Dialog makeOfferDialog;
    private TextView tv_free_love, tv_i_will_pay, tv_you_pay_me, btn_offer;
    private ImageView iv_active_love_ring, iv_free_love, iv_active_i_pay_ring, iv_i_pay, iv_active_you_pay_ring, iv_you_pay;

    private EditText ed_offer_price, ed_message;
    private LinearLayout ly_free_love, ly_i_will_pay, ly_you_pay_me, ly_enter_price, ly_enter_message;
    private String offerType = "", payBy;
    private RelativeLayout rl_make_offer;
    private TextView txtStatus;

    private String myUId, otherUId;
    private String chatNode, blockedId = "";
    private FirebaseDatabase firebaseDatabase;


    public MatchProfileFragment() {
    }

    public static MatchProfileFragment newInstance(String userId) {
        Bundle args = new Bundle();
        MatchProfileFragment fragment = new MatchProfileFragment();
        fragment.setArguments(args);
        args.putString(USER_ID, userId);

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
        View view = inflater.inflate(R.layout.fragment_match_profile, container, false);
        init(view);
        progress = new Progress(mContext);
        Session session = new Session(mContext);
        firebaseDatabase = FirebaseDatabase.getInstance();
        myUId = session.getRegistration().userDetail.userId;
        otherUId = userId;

        chatNode = gettingNotes();
        getBlockUserData();

        // Api call to view for profile
        callViewProfileApi(userId);

        imagesList = new ArrayList<>();
        notInterestModelArrayList = new ArrayList<>();
        interestsList = new ArrayList<>();
        onlineList = new HashMap<>();

        // Api call to get user details
        getUserDetails();

        // Setting adapter for gallery and interests
        galleryInterestAdapter();

        // Get Online Table data from firebase
        getOnlineStatusFromFirebase();

        // Click Listeners
        iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                if (cv_profile_menu.getVisibility() == View.VISIBLE) {
                    cv_profile_menu.setVisibility(View.GONE);
                } else if (cv_profile_menu.getVisibility() == View.GONE) {
                    cv_profile_menu.setVisibility(View.VISIBLE);
                }
            }
        });

        iv_back.setOnClickListener(this);
        iv_like_unlike.setOnClickListener(this);
        iv_favorite.setOnClickListener(this);
        btn_make_offer.setOnClickListener(this);
        iv_user_chat.setOnClickListener(this);
        tv_user_block.setOnClickListener(this);
        tv_user_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                openReportAdminDialog();
                cv_profile_menu.setVisibility(View.GONE);
            }
        });

        return view;
    }

    // Get Online Table data from firebase
    private void getOnlineStatusFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(Constant.ONLINE_TABLE).child(userId).child("lastOnline");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setOnlineData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Set Online Offline status in Online Info type list
    private void setOnlineData(DataSnapshot dataSnapshot) {
        isUserOnline = String.valueOf(dataSnapshot.getValue());

        if (isUserOnline.equals("online")) {
            tv_on_offline.setText(mContext.getResources().getString(R.string.online));
            tv_on_offline.setTextColor(ContextCompat.getColor(mContext, R.color.accept_color));
        } else if (isUserOnline.equals("offline")) {
            tv_on_offline.setText(mContext.getResources().getString(R.string.offline));
            tv_on_offline.setTextColor(ContextCompat.getColor(mContext, R.color.yellow));
        }
    }

    // Setting adapter for gallery and interests
    private void galleryInterestAdapter() {
        // Display images in horizontal recycler view
        matchPhotosAdapter = new MatchPhotosAdapter(imagesList, getActivity(), new AdapterPositionListener() {
            @Override
            public void getPosition(int position) {
                if (AppHelper.isConnectingToInternet(mContext)) {
                    Intent intent = new Intent(mContext, MatchGalleryActivity.class);
                    intent.putExtra("image_index", position);
                    intent.putExtra("from", "user");
                    intent.putExtra("getUserDetail", getUser);
                    startActivity(intent);
                } else {
                    CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.alert_no_network));
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        photos_recycler_view.setLayoutManager(layoutManager);
        photos_recycler_view.setAdapter(matchPhotosAdapter);

        /*// Interest List Adapter with horizontal display
        interestAdapter = new MatchInterestAdapter(interestsList);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        interest_recycler_view.setLayoutManager(horizontalLayoutManager);
        interest_recycler_view.setAdapter(interestAdapter);*/

    }

    private void init(View view) {
       /* cl_profile_view = view.findViewById(R.id.cl_profile_view);
        cl_profile_view.setVisibility(View.GONE);*/

        iv_back = ((MatchProfileActivity) mContext).findViewById(R.id.iv_back);


        iv_menu = ((MatchProfileActivity) mContext).findViewById(R.id.iv_menu);

        cv_profile_menu = ((MatchProfileActivity) mContext).findViewById(R.id.cv_profile_menu);

        TextView action_bar_heading = ((MatchProfileActivity) mContext).findViewById(R.id.action_bar_heading);
        action_bar_heading.setText(getString(R.string.matches));

        iv_back.setVisibility(View.VISIBLE);
        iv_menu.setVisibility(View.VISIBLE);

        iv_user_image = view.findViewById(R.id.iv_user_image);
        llMatchedKinks = view.findViewById(R.id.llMatchedKinks);
        llunMatchedKinks = view.findViewById(R.id.llunMatchedKinks);
        txtStatus = view.findViewById(R.id.txtStatus);
        tv_not_interst = view.findViewById(R.id.tv_not_interst);
        iv_like_unlike = view.findViewById(R.id.iv_like_unlike);
        iv_favorite = view.findViewById(R.id.iv_favorite);
        iv_match_check = view.findViewById(R.id.iv_match_check);
        btn_make_offer = view.findViewById(R.id.btn_make_offer);
        tv_match_age = view.findViewById(R.id.tv_match_age);
        tv_match_gender = view.findViewById(R.id.tv_match_gender);
        tv_on_offline = view.findViewById(R.id.tv_on_offline);
        tv_match_percentage = view.findViewById(R.id.tv_match_percentage);

        iv_user_image = view.findViewById(R.id.iv_user_image);

        tv_match_name = view.findViewById(R.id.tv_match_name);
        llTease = view.findViewById(R.id.llTease);
        tv_match_age = view.findViewById(R.id.tv_match_age);
        tv_match_gender = view.findViewById(R.id.tv_match_gender);
        tv_match_percentage = view.findViewById(R.id.tv_match_percentage);

        tv_intent = view.findViewById(R.id.tv_intent);
        tv_body_type = view.findViewById(R.id.tv_body_type);
        tv_ethnicity = view.findViewById(R.id.tv_ethnicity);
        tv_city_town = view.findViewById(R.id.tv_city_town);
        tv_work = view.findViewById(R.id.tv_work);
        tv_education = view.findViewById(R.id.tv_education);
        tv_user_name = view.findViewById(R.id.tv_user_name);
        tv_about_user = view.findViewById(R.id.tv_about_user);
        tv_na_item = view.findViewById(R.id.tv_na_item);
        tv_miles = view.findViewById(R.id.tv_miles);
        tv_full_address = view.findViewById(R.id.tv_full_address);
        rl_display_miles = view.findViewById(R.id.rl_display_miles);
        tv_na_interest = view.findViewById(R.id.tv_na_interest);
        iv_user_chat = view.findViewById(R.id.iv_user_chat);

        photos_recycler_view = view.findViewById(R.id.photos_recycler_view);
        llTease.setOnClickListener(this);

        rl_main_activity = ((MatchProfileActivity) mContext).findViewById(R.id.rl_main_activity);
        ly_no_network = ((MatchProfileActivity) mContext).findViewById(R.id.ly_no_network);
        btn_try_again = ((MatchProfileActivity) mContext).findViewById(R.id.btn_try_again);

        tv_user_block = ((MatchProfileActivity) mContext).findViewById(R.id.tv_user_block);
        tv_user_report = ((MatchProfileActivity) mContext).findViewById(R.id.tv_user_report);

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    // Get User details Api
    private void getUserDetails() {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {

                        Log.i("userdetail1344", "" + response);
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            rl_main_activity.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            Gson gson = new Gson();
                            getUser = gson.fromJson(String.valueOf(js), GetUserDetailModel.class);

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
            api.callApi("user/userDetail?userId=" + userId, Request.Method.GET, null);
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


        if (isAdded()) {
            if (getUser.userDetail.images.size() > 0) {
                Glide.with(mContext).load(getUser.userDetail.images.get(0).image).apply(new RequestOptions().placeholder(R.drawable.user_place)).into(iv_user_image);
                //Picasso.with(mContext).load(getUser.userDetail.images.get(0).image).placeholder(R.drawable.user_place).fit().into(iv_user_image);
            } else {
                //Picasso.with(mContext).load(getUser.userDetail.defaultImg).placeholder(R.drawable.user_place).fit().into(iv_user_image);

                Glide.with(mContext).load(getUser.userDetail.defaultImg).apply(new RequestOptions().placeholder(R.drawable.user_place)).into(iv_user_image);

            }

            /* cl_profile_view.setVisibility(View.VISIBLE);*/
            progress.dismiss();

            if (getUser.userDetail.block_status.equals("block")) {
                tv_user_block.setText(getString(R.string.unblock));
            } else {
                tv_user_block.setText(getString(R.string.block));
            }

            //Create iterator on Set

            for (Object o : onlineList.entrySet()) {
                Map.Entry mapEntry = (Map.Entry) o;
                // Get Key
                String key = (String) mapEntry.getKey();
                //Get Value
                OnlineInfo onlineInfo = (OnlineInfo) mapEntry.getValue();
                String value = onlineInfo.lastOnline;

                if (getUser.userDetail.userId.equals(key)) {
                    isUserOnline = value;
                }
            }

            if (getUser.userDetail.chat_status.equals("0")) {
                iv_user_chat.setVisibility(View.GONE);
            } else if (getUser.userDetail.chat_status.equals("1")) {
                iv_user_chat.setVisibility(View.VISIBLE);
                llTease.setVisibility(View.GONE);
            }

           /* if (getUser.userDetail.offer_status.equals("0")) {
                btn_make_offer.setText(getString(R.string.make_offer));
                btn_make_offer.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_login_btn));
            } else {
                btn_make_offer.setText(getString(R.string.pending));
                btn_make_offer.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_pending_btn));
            }*/


            if (isUserOnline.equals("online")) {
                tv_on_offline.setText(mContext.getResources().getString(R.string.online));
                tv_on_offline.setTextColor(ContextCompat.getColor(mContext, R.color.accept_color));
            } else if (isUserOnline.equals("offline")) {
                tv_on_offline.setText(mContext.getResources().getString(R.string.offline));
                tv_on_offline.setTextColor(ContextCompat.getColor(mContext, R.color.yellow));
            }

            switch (getUser.userDetail.is_verify) {
                case "0":
                    iv_match_check.setVisibility(View.GONE);
                    break;
                case "3":
                    iv_match_check.setVisibility(View.VISIBLE);
                    break;
                default:
                    iv_match_check.setVisibility(View.GONE);
                    break;
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

            if (getUser.userDetail.like_status.equals("0")) {
                iv_like_unlike.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.active_like));
                iv_like_unlike.setVisibility(View.VISIBLE);
            } else if (getUser.userDetail.like_status.equals("1")) {
                iv_like_unlike.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.active_like));
                iv_like_unlike.setVisibility(View.VISIBLE);
            }

            if (getUser.userDetail.favorite_status.equals("0")) {
                iv_favorite.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.active_star_ico));
            } else if (getUser.userDetail.favorite_status.equals("1")) {
                iv_favorite.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_favourites_filled_star_symbol));
            }

            String[] nameArray = getUser.userDetail.full_name.split(" ");
            StringBuilder builder = new StringBuilder();
            for (String s : nameArray) {
                if (s.length() != 0) {
                    String cap = (s.length() > 0 && s.length() == 1) ? s.substring(0, 1).toUpperCase() : s.substring(0, 1).toUpperCase() + s.substring(1);
                    builder.append(cap).append(" ");
                }
            }

            userName = builder.toString();
            tv_match_name.setText(userName);

            tv_user_name.setText(userName);
            tv_match_age.setText(String.format(Locale.ENGLISH, "%d %s,", getUser.userDetail.age, getString(R.string.yr)));

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

            tv_match_gender.setText(gender);

            switch (getUser.userDetail.like_status) {
                case "1":
                    txtStatus.setText("Pending");
                    break;
                case "2":
                    iv_user_chat.setVisibility(View.VISIBLE);
                    txtStatus.setText("Accepted");
                    break;

                case "3":
                    txtStatus.setText(R.string.send_request);
                    break;

                case "0":
                    txtStatus.setText(R.string.send_request);
                    break;


            }
            tv_match_percentage.setText(String.format("%s%%", getUser.userDetail.match));

            if (!getUser.userDetail.distance_in_mi.equals("")) {
                tv_miles.setText(getUser.userDetail.distance_in_mi);
            } else {
                rl_display_miles.setVisibility(View.GONE);
            }

            if (!getUser.userDetail.current_address.equals("") && !getUser.userDetail.current_address.equals("0")) {
                tv_full_address.setText(getUser.userDetail.current_address);
            }

            if (getUser.userDetail.images.size() > 0) {
                imagesList.clear();
                for (int i = 0; i < getUser.userDetail.images.size(); i++) {
                    ProfileImageModel imageModal = new ProfileImageModel();
                    imageModal.imageId = getUser.userDetail.images.get(i).userImageId;
                    imageModal.profileUrl = getUser.userDetail.images.get(i).image;
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

                    if (s.equals("neutral")) {
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

            if (!getUser.userDetail.work.equals("")) {
                String work = getUser.userDetail.work.substring(0, 1).toUpperCase() + getUser.userDetail.work.substring(1);
                tv_work.setText(work);
            }

            if (getUser.userDetail.commonInterest.size() > 0 && getUser.userDetail.show_kink.equals("1")) {
                interestsList.clear();
                for (int i = 0; i < getUser.userDetail.interests.size(); i++) {
                    for (int j = 0; j < getUser.userDetail.commonInterest.size(); j++) {
                        if (getUser.userDetail.interests.get(i).interestId.equals(getUser.userDetail.commonInterest.get(j))) {
                            InterestsModel model = new InterestsModel();
                            model.interest = getUser.userDetail.interests.get(i).interest;
                            model.interestId = getUser.userDetail.interests.get(i).interestId;
                            model.isChecked = true;
                            interestsList.add(model);
                        }
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
                llMatchedKinks.setVisibility(View.VISIBLE);
                tv_not_interst.setText(str_interest);


            } else {

                if (getUser.userDetail.show_kink.equals("0")) {
                    llMatchedKinks.setVisibility(View.GONE);
                } else {
                    switch (getUser.userDetail.like_status) {
                        case "3":
                            llMatchedKinks.setVisibility(View.GONE);
                            tv_not_interst.setText(getString(R.string.kinks_not_matched));
                            break;

                        case "0":
                            llMatchedKinks.setVisibility(View.GONE);
                            //tv_not_interst.setText(getString(R.string.kinks_not_matched));
                            break;

                        default:
                            llMatchedKinks.setVisibility(View.VISIBLE);
                            tv_not_interst.setText(getString(R.string.kinks_not_matched));
                            break;
                    }
                }


            }

            if (!getUser.userDetail.full_address.equals("")) {
                String city = getUser.userDetail.full_address.substring(0, 1).toUpperCase() + getUser.userDetail.full_address.substring(1);
                tv_city_town.setText(city);
            }

            if (!getUser.userDetail.work.equals("")) {
                String work = getUser.userDetail.work.substring(0, 1).toUpperCase() + getUser.userDetail.work.substring(1);
                tv_work.setText(work);
            }

            if (getUser.userDetail.differentInterest.size() > 0 && getUser.userDetail.show_kink.equals("1")) {
                notInterestModelArrayList.clear();
                for (int i = 0; i < getUser.userDetail.interests.size(); i++) {
                    for (int j = 0; j < getUser.userDetail.differentInterest.size(); j++) {
                        if (getUser.userDetail.interests.get(i).interestId.equals(getUser.userDetail.differentInterest.get(j))) {
                            NotInterestModel model = new NotInterestModel();
                            model.interest = getUser.userDetail.interests.get(i).interest;
                            model.interestId = getUser.userDetail.interests.get(i).interestId;
                            model.isChecked = true;
                            notInterestModelArrayList.add(model);
                        }
                    }
                }

                String str_interest = "";
                for (int i = 0; i < notInterestModelArrayList.size(); i++) {
                    String interest = notInterestModelArrayList.get(i).interest.substring(0, 1).toUpperCase() + notInterestModelArrayList.get(i).interest.substring(1);
                    str_interest = String.format("%s, %s", interest, str_interest);
                }

                if (str_interest.endsWith(", ")) {
                    str_interest = str_interest.substring(0, str_interest.length() - 2);
                }
               // llunMatchedKinks.setVisibility(View.VISIBLE);
                tv_na_interest.setText(str_interest);


            } else {

                if (getUser.userDetail.show_kink.equals("0")) {
                    llunMatchedKinks.setVisibility(View.GONE);
                } else {
                    if (getUser.userDetail.like_status.equals("0") || getUser.userDetail.like_status.equals("3")) {
                        llunMatchedKinks.setVisibility(View.GONE);
                        tv_na_interest.setText("Kinks not Available");
                    } else {
                     //   llunMatchedKinks.setVisibility(View.VISIBLE);
                        tv_na_interest.setText("Kinks not Available");
                    }
                }


            }

            if (!getUser.userDetail.about.equals("")) {
                String about = getUser.userDetail.about.substring(0, 1).toUpperCase() + getUser.userDetail.about.substring(1);
                tv_about_user.setText(about);
            }

            if (!(getUser.userDetail == null)) {
                if (getUser.userDetail.block_status.equals("unblock")) {

                    getTotalDayBtwTimeStamp();
                }
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

        switch (v.getId()) {
            case R.id.iv_back:  // Back Icon Click
                ((MatchProfileActivity) mContext).onBackPressed();
                break;

            case R.id.iv_menu:   // Menu Click
                if (cv_profile_menu.getVisibility() == View.VISIBLE) {
                    cv_profile_menu.setVisibility(View.GONE);
                } else if (cv_profile_menu.getVisibility() == View.GONE) {
                    cv_profile_menu.setVisibility(View.VISIBLE);
                }

                break;


            case R.id.iv_like_unlike:    // Like Unlike Click
                if (getUser != null && getUser.userDetail != null) {
                    if (getUser.userDetail.block_status.equals("unblock")) {
                        if (getUser.userDetail.like_status.equals("0")) {
                            likeRequestType = "1";
                        } else if (getUser.userDetail.like_status.equals("1")) {
                            likeRequestType = "0";
                        }
                        // Api call to like unlike profile
                        callLikeUnlikeApi(userId, likeRequestType);
                    } else {
                        CustomToast.getInstance(mContext).showToast(mContext, "You have blocked " + userName);
                    }
                }
                break;


            case R.id.iv_favorite:    // Favorite Unfavorite Click
                if (getUser != null && getUser.userDetail != null) {
                    if (getUser.userDetail.block_status.equals("unblock")) {
                        if (getUser.userDetail.favorite_status.equals("0")) {
                            favoriteRequestType = "1";
                        } else if (getUser.userDetail.favorite_status.equals("1")) {
                            favoriteRequestType = "0";
                        }
                        // Api call to favorite unfavorite profile
                        callFavoriteUnfavoriteApi(userId, favoriteRequestType);
                    } else {
                        CustomToast.getInstance(mContext).showToast(mContext, "You have blocked " + userName);
                    }
                }
                break;

            case R.id.iv_user_chat:
                if (AppHelper.isConnectingToInternet(mContext)) {
                    if (getUser != null && getUser.userDetail != null) {
                        if (getUser.userDetail.block_status.equals("unblock")) {
                            Intent chatIntent = new Intent(mContext, ChatActivity.class);
                            chatIntent.putExtra("otherUID", userId);
                            startActivity(chatIntent);
                        } else {
                            CustomToast.getInstance(mContext).showToast(mContext, "You have blocked " + userName);
                        }
                    }
                } else {
                    CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.alert_no_network));
                }
                break;

            case R.id.tv_user_block:
                cv_profile_menu.setVisibility(View.GONE);
                //  CustomToast.getInstance(mContext).showToast(mContext, "Under Development");
                if (getUser != null && getUser.userDetail != null) {
                    if (getUser.userDetail.block_status.equals("unblock")) {
                        blockUserDialog(getString(R.string.block), "Block " + userName + "? Blocked user will no longer be able to see your profile and contact you.");
                    } else {
                        blockUserDialog(getString(R.string.unblock), "Are you sure, you want to Unblock " + userName + "?");
                    }
                }
                break;


            case R.id.btn_make_offer:    // Message Now btn Click
                if (getUser != null && getUser.userDetail != null) {
                    if (getUser.userDetail.offer_status.equals("0")) {
                        if (getUser.userDetail.block_status.equals("unblock")) {
                            openMakeOfferDialog();
                        } else {
                            CustomToast.getInstance(mContext).showToast(mContext, "You have blocked " + userName);
                        }
                    }
                }
                break;

            case R.id.ly_free_love:     // Select Free Love
                setFreeLoveActive();
                break;

            case R.id.ly_i_will_pay:    // Select I will pay
                setIWillPayActive();
                break;

            case R.id.ly_you_pay_me:    // Select You pay me
                setYouPayMeActive();
                break;

            case R.id.btn_offer:       // Make Offer Button
                if (AppHelper.isConnectingToInternet(mContext)) {
                    String message = ed_message.getText().toString().trim();
                    String amount = ed_offer_price.getText().toString().trim();
                    if (getUser != null && getUser.userDetail != null) {
                        String id = getUser.userDetail.userId;

                        if (!offerType.equals("")) {
                            if (offerType.equals("0")) {
                                callMakeOfferApi(id, offerType, amount, payBy, message);
                            } else {
                                if (!amount.equals("")) {
                                    double amt = Double.parseDouble(amount);
                                    if (amt > 0) {
                                        callMakeOfferApi(id, offerType, amount, payBy, message);
                                    } else {
                                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.offer_amount_null));
                                    }
                                } else {
                                    CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.offer_amount_null));
                                }
                            }
                        } else {
                            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.offer_type_null));
                        }
                    }
                } else {
                    makeOfferDialog.dismiss();
                    CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.alert_no_network));
                }
                break;

            case R.id.llTease:
                switch (getUser.userDetail.like_status) {
                    case "0":
                        if (!(getUser.userDetail == null)) {
                            if (getUser.userDetail.block_status.equals("unblock")) {
                                likeRequestType = "1";
                              /*  if (getUser.userDetail.like_status.equals("0")) {
                                    likeRequestType = "1";
                                } else if (getUser.userDetail.like_status.equals("1")) {
                                    likeRequestType = "0";
                                }*/
                                // Api call to like unlike profile
                                callLikeUnlikeApi(userId, likeRequestType);
                            } else {
                                CustomToast.getInstance(mContext).showToast(mContext, "You have blocked " + userName);
                            }
                        }
                        break;
                    case "3":
                        if (getUser != null && getUser.userDetail != null) {
                            if (getUser.userDetail.block_status.equals("unblock")) {
                                likeRequestType = "1";
                               /* if (getUser.userDetail.like_status.equals("0")) {
                                    likeRequestType = "1";
                                } else if (getUser.userDetail.like_status.equals("1")) {
                                    likeRequestType = "0";
                                }*/
                                // Api call to like unlike profile
                                callLikeUnlikeApi(userId, likeRequestType);
                            } else {
                                CustomToast.getInstance(mContext).showToast(mContext, "You have blocked " + userName);
                            }
                        }
                        break;
                    default:

                        break;
                }

        }
    }

    // Api call to view for profile
    private void callViewProfileApi(final String viewFor) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            Map<String, String> map = new HashMap<>();
            map.put("viewFor", viewFor);

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    rl_main_activity.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    rl_main_activity.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                }

            });
            api.callApi("user/viewProfile", Request.Method.POST, map);
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

                    // Api call to view for profile
                    callViewProfileApi(viewFor);

                }
            });
        }
    }

    // Api call to like unlike profile
    private void callLikeUnlikeApi(final String likeFor, final String requestType) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            Map<String, String> map = new HashMap<>();
            map.put("likeFor", likeFor);
            map.put("requestType", requestType);

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        rl_main_activity.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            if (message.equals(getResources().getString(R.string.teases_sent_successfully))) {
                                getUser.userDetail.like_status = "1";
                                txtStatus.setText("Pending");
                                //iv_like_unlike.setVisibility(View.GONE);
                                CustomToast.getInstance(mContext).showToast(mContext, getResources().getString(R.string.teases_sent_successfully));

                            }
                        } else {
                            progress.dismiss();
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        rl_main_activity.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    rl_main_activity.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                }

            });
            api.callApi("user/likeUnLikeUser", Request.Method.POST, map);
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

    // Api call to favorite unfavorite profile
    private void callFavoriteUnfavoriteApi(final String favoriteFor, final String requestType) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            Map<String, String> map = new HashMap<>();
            map.put("favoriteFor", favoriteFor);
            map.put("requestType", requestType);

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        rl_main_activity.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            if (message.equals(getResources().getString(R.string.favorite_response_message))) {
                                iv_favorite.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_favourites_filled_star_symbol));
                                getUser.userDetail.favorite_status = "1";
                            } else if (message.equals(getResources().getString(R.string.unfavorite_response_message))) {
                                iv_favorite.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_line_star));
                                getUser.userDetail.favorite_status = "0";
                            }
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
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    rl_main_activity.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                }

            });
            api.callApi("user/favoriteUnFavorite", Request.Method.POST, map);
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

    // Report to admin dialog
    private void openReportAdminDialog() {
        reportDialog = new Dialog(mContext);
        reportDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        reportDialog.setCancelable(false);
        reportDialog.setContentView(R.layout.dialog_report_admin);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(reportDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        reportDialog.getWindow().setAttributes(lWindowParams);

        final TextView btn_report = reportDialog.findViewById(R.id.btn_report);

        final EditText ed_title = reportDialog.findViewById(R.id.ed_title);
        ed_title.setHorizontallyScrolling(false);
        ed_title.setMaxLines(3);

        final EditText ed_description = reportDialog.findViewById(R.id.ed_description);
        ed_description.setHorizontallyScrolling(false);
        ed_description.setMaxLines(10);

        ImageView dialog_decline_button = reportDialog.findViewById(R.id.dialog_decline_button);

        dialog_decline_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                reportDialog.dismiss();
            }
        });

        btn_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                if (ed_title.getText().toString().trim().equals("")) {
                    CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.title_null));
                } else if (ed_description.getText().toString().trim().equals("")) {
                    CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.description_null));
                } else {
                    // Calling Report to Admin Api
                    String title = ed_title.getText().toString().trim();
                    String description = ed_description.getText().toString().trim();

                    callReportAdminApi(title, description);
                }
            }
        });

        reportDialog.getWindow().setGravity(Gravity.CENTER);
        reportDialog.show();
    }

    // Calling Report to Admin Api
    private void callReportAdminApi(final String title, final String description) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            Map<String, String> map = new HashMap<>();
            map.put("reportFor", userId);
            map.put("title", title);
            map.put("description", description);

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            ly_no_network.setVisibility(View.GONE);
                        } else {
                            progress.dismiss();
                            ly_no_network.setVisibility(View.GONE);
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        ly_no_network.setVisibility(View.GONE);
                    }

                    reportDialog.dismiss();
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    reportDialog.dismiss();
                    ly_no_network.setVisibility(View.GONE);
                }

            });
            api.callApi("user/reportUser", Request.Method.POST, map);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            reportDialog.dismiss();
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Preventing multiple clicks, using threshold of 1/2 second
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    // Calling Report to Admin Api
                    callReportAdminApi(title, description);
                }
            });
        }
    }

    private void blockUserDialog(String heading, String msg) {
        blockDialog = new Dialog(mContext);
        blockDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        blockDialog.setContentView(R.layout.dialog_block_user);
        blockDialog.setCancelable(false);
        blockDialog.setCanceledOnTouchOutside(false);

        TextView dialog_heading = blockDialog.findViewById(R.id.tv_dialog_heading);
        dialog_heading.setText(heading);

        TextView alert_message = blockDialog.findViewById(R.id.alert_message);
        alert_message.setText(msg);

        TextView btn_cancel = blockDialog.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                blockDialog.dismiss();
            }
        });

        ImageView dialog_decline_button = blockDialog.findViewById(R.id.dialog_decline_button);
        dialog_decline_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                blockDialog.dismiss();
            }
        });

        TextView btn_alert = blockDialog.findViewById(R.id.btn_alert);
        btn_alert.setText(heading);
        btn_alert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                // Api call to block unblock user
                callBlockUnblockUserApi(userId);
            }
        });

        blockDialog.show();
    }

    // Api call to block unblock user
    private void callBlockUnblockUserApi(final String userId) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            Map<String, String> map = new HashMap<>();
            map.put("userId", userId);

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        rl_main_activity.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            blockUserChat();

                            if (message.equals("User blocked successfully")) {
                                getUser.userDetail.block_status = "block";
                                tv_user_block.setText(getString(R.string.unblock));

                            } else if (message.equals("User unBlocked successfully")) {
                                getUser.userDetail.block_status = "unblock";
                                tv_user_block.setText(getString(R.string.block));

                            }

                        } else {
                            progress.dismiss();
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                        }

                        blockDialog.dismiss();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        blockDialog.dismiss();
                        progress.dismiss();
                        rl_main_activity.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    blockDialog.dismiss();
                    progress.dismiss();
                    rl_main_activity.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                }

            });
            api.callApi("user/userBlockUnblock", Request.Method.POST, map);
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

                    // Api call to block unblock user
                    callBlockUnblockUserApi(userId);

                }
            });
        }
    }

    private void getBlockUserData() {
        firebaseDatabase.getReference().child(Constant.CHAT_BLOCK_TABLE).child(chatNode).child(Constant.CHAT_BLOCKED_BY).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(String.class) != null) {
                    blockedId = dataSnapshot.getValue(String.class);
                    assert blockedId != null;
                    if (isAdded()) {
                        if (blockedId.equals("Both")) {
                            tv_user_block.setText(getString(R.string.unblock));
                        } else if (blockedId.equals("")) {
                            tv_user_block.setText(getString(R.string.block));
                        } else if (blockedId.equals(otherUId)) {
                            tv_user_block.setText(getString(R.string.block));
                        } else if (blockedId.equals(myUId)) {
                            tv_user_block.setText(getString(R.string.unblock));
                        }
                    }

                } else {
                    blockedId = "";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void blockUserChat() {
        if (blockedId.equals("Both")) {
            firebaseDatabase.getReference().child(Constant.CHAT_BLOCK_TABLE).child(chatNode).child(Constant.CHAT_BLOCKED_BY).setValue(otherUId);

        } else if (blockedId.equals("")) {
            firebaseDatabase.getReference().child(Constant.CHAT_BLOCK_TABLE).child(chatNode).child(Constant.CHAT_BLOCKED_BY).setValue(myUId);

        } else if (blockedId.equals(otherUId)) {
            firebaseDatabase.getReference().child(Constant.CHAT_BLOCK_TABLE).child(chatNode).child(Constant.CHAT_BLOCKED_BY).setValue("Both");

        } else if (blockedId.equals(myUId)) {
            firebaseDatabase.getReference().child(Constant.CHAT_BLOCK_TABLE).child(chatNode).child(Constant.CHAT_BLOCKED_BY).setValue(null);
        }

        if (isAdded()) {
            getBlockUserData();
        }
    }

    //create note for chatroom
    private String gettingNotes() {
        if (myUId != null && otherUId != null) {
            int myUid_ = Integer.parseInt(myUId);
            int otherUID_ = Integer.parseInt(otherUId);

            if (myUid_ < otherUID_) {
                chatNode = myUId + "_" + otherUId;
            } else {
                chatNode = otherUId + "_" + myUId;
            }
        }
        return chatNode;
    }

    // Dialog to make offer
    private void openMakeOfferDialog() {
        makeOfferDialog = new Dialog(mContext, R.style.SplashTheme);
        makeOfferDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        makeOfferDialog.setContentView(R.layout.dialog_make_offer);

        LinearLayout ly_make_offer = makeOfferDialog.findViewById(R.id.ly_make_offer);

        int dp = Math.round(getStatusBarHeight() / (Resources.getSystem().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));

        /*Rect rectangle = new Rect();
        Window window = makeOfferDialog.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        int contentViewTop =
                window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight= contentViewTop - statusBarHeight;*/

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(0, dp, 0, 0);

        ly_make_offer.setLayoutParams(layoutParams);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(makeOfferDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        makeOfferDialog.getWindow().setAttributes(lWindowParams);

        makeOfferDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        initMakeOfferDialog();
        setFreeLoveActive();

        ly_free_love.setOnClickListener(this);
        ly_i_will_pay.setOnClickListener(this);
        ly_you_pay_me.setOnClickListener(this);
        btn_offer.setOnClickListener(this);

        makeOfferDialog.getWindow().setGravity(Gravity.CENTER);
        makeOfferDialog.show();
    }

    private void initMakeOfferDialog() {
        TextView tv_offer_user_name = makeOfferDialog.findViewById(R.id.tv_offer_user_name);
        tv_free_love = makeOfferDialog.findViewById(R.id.tv_free_love);
        tv_i_will_pay = makeOfferDialog.findViewById(R.id.tv_i_will_pay);
        tv_you_pay_me = makeOfferDialog.findViewById(R.id.tv_you_pay_me);
        btn_offer = makeOfferDialog.findViewById(R.id.btn_offer);

        iv_active_love_ring = makeOfferDialog.findViewById(R.id.iv_active_love_ring);
        iv_free_love = makeOfferDialog.findViewById(R.id.iv_free_love);
        iv_active_i_pay_ring = makeOfferDialog.findViewById(R.id.iv_active_i_pay_ring);
        iv_i_pay = makeOfferDialog.findViewById(R.id.iv_i_pay);
        iv_active_you_pay_ring = makeOfferDialog.findViewById(R.id.iv_active_you_pay_ring);
        iv_you_pay = makeOfferDialog.findViewById(R.id.iv_you_pay);

        ed_offer_price = makeOfferDialog.findViewById(R.id.ed_offer_price);
        ed_message = makeOfferDialog.findViewById(R.id.ed_message);
        //  ed_message.setFilters(new InputFilter[]{filter});
        ed_message.setHorizontallyScrolling(false);
        ed_message.setMaxLines(4);

        ly_free_love = makeOfferDialog.findViewById(R.id.ly_free_love);
        ly_i_will_pay = makeOfferDialog.findViewById(R.id.ly_i_will_pay);
        ly_you_pay_me = makeOfferDialog.findViewById(R.id.ly_you_pay_me);
        ly_enter_price = makeOfferDialog.findViewById(R.id.ly_enter_price);
        ly_enter_message = makeOfferDialog.findViewById(R.id.ly_enter_message);

        ly_enter_price.setVisibility(View.GONE);
        ly_enter_message.setVisibility(View.GONE);

        ImageView iv_offer_back = makeOfferDialog.findViewById(R.id.iv_offer_back);
        iv_offer_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();


                //    AppHelper.hideKeyboard(ed_message, mContext);

                payBy = "";
                offerType = "";
                makeOfferDialog.dismiss();
            }
        });

        ImageView iv_offer_user_image = makeOfferDialog.findViewById(R.id.iv_offer_user_image);

        if (getUser.userDetail.images.size() > 0) {
           // Picasso.with(mContext).load(getUser.userDetail.images.get(0).image).placeholder(R.drawable.user_place).fit().into(iv_offer_user_image);
            Glide.with(mContext).load(getUser.userDetail.images.get(0).image).apply(new RequestOptions().placeholder(R.drawable.user_place)).into(iv_offer_user_image);

        } else {
            //Picasso.with(mContext).load(getUser.userDetail.defaultImg).placeholder(R.drawable.user_place).fit().into(iv_offer_user_image);
            Glide.with(mContext).load(getUser.userDetail.defaultImg).apply(new RequestOptions().placeholder(R.drawable.user_place)).into(iv_offer_user_image);

        }

        ly_offer_no_network = makeOfferDialog.findViewById(R.id.ly_offer_no_network);
        rl_make_offer = makeOfferDialog.findViewById(R.id.rl_make_offer);

        tv_offer_user_name.setText(String.format("%s, %s %s", getString(R.string.hopefully), getUser.userDetail.full_name, getString(R.string.make_offer_text)));
    }

    // Set Free Love active
    private void setFreeLoveActive() {
        iv_active_love_ring.setVisibility(View.VISIBLE);
        iv_free_love.setImageResource(R.drawable.active_free_love);
        tv_free_love.setTextColor(getResources().getColor(R.color.colorPrimary));

        iv_active_i_pay_ring.setVisibility(View.GONE);
        iv_i_pay.setImageResource(R.drawable.inactive_i_pay_you);
        tv_i_will_pay.setTextColor(getResources().getColor(R.color.gender_image_bg));

        iv_active_you_pay_ring.setVisibility(View.GONE);
        iv_you_pay.setImageResource(R.drawable.inactive_you_pay_me);
        tv_you_pay_me.setTextColor(getResources().getColor(R.color.gender_image_bg));

        ly_enter_price.setVisibility(View.GONE);
        ly_enter_message.setVisibility(View.VISIBLE);

        offerType = "0";
        payBy = "";
    }

    // Set I will pay active
    private void setIWillPayActive() {
        iv_active_love_ring.setVisibility(View.GONE);
        iv_free_love.setImageResource(R.drawable.inactive_free_love);
        tv_free_love.setTextColor(getResources().getColor(R.color.gender_image_bg));

        iv_active_i_pay_ring.setVisibility(View.VISIBLE);
        iv_i_pay.setImageResource(R.drawable.active_i_pay_you);
        tv_i_will_pay.setTextColor(getResources().getColor(R.color.colorPrimary));

        iv_active_you_pay_ring.setVisibility(View.GONE);
        iv_you_pay.setImageResource(R.drawable.inactive_you_pay_me);
        tv_you_pay_me.setTextColor(getResources().getColor(R.color.gender_image_bg));

        ly_enter_price.setVisibility(View.VISIBLE);
        ly_enter_message.setVisibility(View.VISIBLE);

        offerType = "1";
        payBy = getUser.userDetail.userId;
    }

    // Set You pay me active
    private void setYouPayMeActive() {
        iv_active_love_ring.setVisibility(View.GONE);
        iv_free_love.setImageResource(R.drawable.inactive_free_love);
        tv_free_love.setTextColor(getResources().getColor(R.color.gender_image_bg));

        iv_active_i_pay_ring.setVisibility(View.GONE);
        iv_i_pay.setImageResource(R.drawable.inactive_i_pay_you);
        tv_i_will_pay.setTextColor(getResources().getColor(R.color.gender_image_bg));

        iv_active_you_pay_ring.setVisibility(View.VISIBLE);
        iv_you_pay.setImageResource(R.drawable.active_you_pay_me);
        tv_you_pay_me.setTextColor(getResources().getColor(R.color.colorPrimary));

        ly_enter_price.setVisibility(View.VISIBLE);
        ly_enter_message.setVisibility(View.VISIBLE);

        offerType = "1";
        payBy = getUser.userDetail.userId;
    }

    // Api call to create offer
    private void callMakeOfferApi(final String id, final String offerType, final String amount, final String payBy, final String message) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            Map<String, String> map = new HashMap<>();
            map.put("offerFor", id);
            map.put("offerType", offerType);
            map.put("offerAmount", amount);
            map.put("payBy", payBy);
            map.put("message", message);

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            rl_make_offer.setVisibility(View.VISIBLE);
                            ly_offer_no_network.setVisibility(View.GONE);

                            getUser.userDetail.offer_status = "1";
                            btn_make_offer.setText(getString(R.string.pending));
                            btn_make_offer.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_pending_btn));

                            makeOfferDialog.dismiss();
                        } else {
                            progress.dismiss();
                            rl_make_offer.setVisibility(View.VISIBLE);
                            ly_offer_no_network.setVisibility(View.GONE);

                            CustomToast.getInstance(mContext).showToast(mContext, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        rl_make_offer.setVisibility(View.VISIBLE);
                        ly_offer_no_network.setVisibility(View.GONE);

                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    rl_make_offer.setVisibility(View.VISIBLE);
                    ly_offer_no_network.setVisibility(View.GONE);
                }

            });
            api.callApi("offer/makeOffer", Request.Method.POST, map);
        } else {
            makeOfferDialog.dismiss();
            rl_make_offer.setVisibility(View.VISIBLE);
            ly_offer_no_network.setVisibility(View.GONE);
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        //getUserDetails();
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
        cv_profile_menu.setVisibility(View.GONE);
        if (progress != null) {
            progress.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cv_profile_menu.setVisibility(View.GONE);
        if (progress != null) {
            progress.dismiss();
        }
    }

    public void getTotalDayBtwTimeStamp() {

        firebaseDatabase.getReference().child(Constant.CHAT_ROOMS_TABLE).child(chatNode).limitToLast(1).orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                firebaseDatabase.getReference().child(Constant.CHAT_ROOMS_TABLE).child(chatNode).child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot!=null) {
                            Chat chat = dataSnapshot.getValue(Chat.class);
                            Date d1 = new Date();
                            Date d2 = new Timestamp((Long) chat.timeStamp);
                            TimeSpan ts = TimeSpan.subtract(d1, d2);
                            long days = ts.getDays();

                            if (days > Constant.TEASE_EXPIRE_DAYS) {
                                getUser.userDetail.like_status = "3";
                                txtStatus.setText(R.string.send_request);
                                llMatchedKinks.setVisibility(View.GONE);
                                iv_user_chat.setVisibility(View.GONE);
                                llTease.setVisibility(View.VISIBLE);
                            }

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
