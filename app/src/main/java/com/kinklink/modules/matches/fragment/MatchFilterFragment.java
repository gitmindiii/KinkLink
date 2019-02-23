package com.kinklink.modules.matches.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarFinalValueListener;
import com.crystal.crystalrangeseekbar.interfaces.OnSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnSeekbarFinalValueListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.gson.Gson;
import com.kinklink.R;
import com.kinklink.application.KinkLink;
import com.kinklink.helper.AddressLocationTask;
import com.kinklink.helper.AppHelper;
import com.kinklink.helper.Constant;
import com.kinklink.helper.CustomToast;
import com.kinklink.helper.Progress;
import com.kinklink.helper.Validation;
import com.kinklink.modules.authentication.adapter.EditOtherInfoListAdapter;
import com.kinklink.modules.authentication.listener.InterestIdListener;
import com.kinklink.modules.authentication.model.EditOtherInfoModel;
import com.kinklink.modules.authentication.model.InterestsModel;
import com.kinklink.modules.matches.activity.MainActivity;
import com.kinklink.modules.matches.adapter.FilterInterestsAdapter;
import com.kinklink.server_task.WebService;
import com.kinklink.session.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import pl.droidsonroids.gif.GifTextView;

public class MatchFilterFragment extends Fragment implements View.OnClickListener {
    private ImageView iv_back;
    private Context mContext;
    private EditText ed_filter_name;
    private RelativeLayout rl_search_interests, rl_search_city;
    private Session session;
    private ArrayList<InterestsModel> interestsList, selectedInterestList;
    private ListView interest_listView;
    private FilterInterestsAdapter adapter;

    private String selected_interests = "", interests;
    private CardView cv_filter;
    private LinearLayout ly_no_network;
    private TextView btn_try_again, btn_clear, btn_search, tv_city_filter, tv_filter_interest;
    private boolean isdisplay = false;

    // variable to track event time
    private long mLastClickTime = 0;

    private RelativeLayout rl_select_gender, rl_body_type, rl_any_ethnicity;
    private TextView tv_gender, tv_body_type, tv_ethnicity;

    // Select Gender Dialog
    private Dialog genderDialog;
    private TextView btn_gender;
    private FrameLayout fl_man, fl_woman, fl_couple, fl_trans_male, fl_trans_female,fl_neutral;
    private GifTextView gif_man, gif_woman, gif_couple, gif_trans_male, gif_trans_female,gif_neutral;
    private ImageView iv_man, iv_woman, iv_couple, iv_trans_male, iv_trans_female,iv_neutral,
            iv_man_border, iv_woman_border, iv_couple_border, iv_trans_male_border, iv_trans_female_border,iv_couple_neutral_border;
    private TextView tv_man, tv_woman, tv_couple, tv_trans_male, tv_trans_female, tv_intent_heading,tv_neutral,tv_intent_title;
    private String type_you_are = "", select_type = "";
    private int you_are_selectedId = 0;

    private String selectedItem, filterLocation, city, state, country, mLatitude, mLongitude;

    private ArrayList<EditOtherInfoModel.DropDownListBean.OtherInfoBean> ethnicityList, bodyTypeList;
    private EditOtherInfoListAdapter listAdapter;
    private String fromAge, toAge, rangMin, rangMax;
    private CrystalRangeSeekbar age_rangeSeekbar;
    private CrystalSeekbar distance_rangeSeekbar;
    private TextView tv_age_min_limit, tv_age_max_limit, tv_miles_min_limit, tv_miles_max_limit;
    private Progress progress;

    public MatchFilterFragment() {
    }

    public static MatchFilterFragment newInstance() {
        Bundle args = new Bundle();
        MatchFilterFragment fragment = new MatchFilterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_match_filter, container, false);

        progress = new Progress(mContext);
        session = new Session(mContext);

        init(view);
        // Call getAllDropDownList Api
        callGetAllDropDownListApi();

        interestsList = new ArrayList<>();
        selectedInterestList = new ArrayList<>();
        ethnicityList = new ArrayList<>();
        bodyTypeList = new ArrayList<>();

        ed_filter_name.setText(session.getFilterUserName());
        isdisplay = false;
        getInterestList();

        iv_back.setOnClickListener(this);
        rl_search_interests.setOnClickListener(this);
        rl_search_city.setOnClickListener(this);
        btn_search.setOnClickListener(this);
        btn_clear.setOnClickListener(this);

        rl_select_gender.setOnClickListener(this);
        rl_body_type.setOnClickListener(this);
        rl_any_ethnicity.setOnClickListener(this);

        return view;
    }

    // Set Filter Data pre selected
    private void setPreSelectedFromSession() {
        selected_interests = session.getFilterInterests();

        if (!session.getFilterInterests().equals("")) {
            ArrayList<String> list = new ArrayList<>();
            list.addAll(Arrays.asList(session.getFilterInterests().split(",")));

            for (int i = 0; i < interestsList.size(); i++) {
                for (int j = 0; j < list.size(); j++) {
                    if (interestsList.get(i).interestId.equals(list.get(j))) {
                        interestsList.get(i).isChecked = true;
                    }
                }
            }
        }

        for (int i = 0; i < interestsList.size(); i++) {
            if (interestsList.get(i).isChecked) {
                InterestsModel model = new InterestsModel();
                model.interest = interestsList.get(i).interest;
                model.interestId = interestsList.get(i).interestId;
                selectedInterestList.add(model);
            }
        }

        setInterestStringAsText();

    }

    private void init(View view) {
        iv_back = ((MainActivity) mContext).findViewById(R.id.iv_back);
        ImageView iv_profile = ((MainActivity) mContext).findViewById(R.id.iv_profile);
        iv_profile.setVisibility(View.GONE);

        ImageView iv_teases = ((MainActivity) mContext).findViewById(R.id.iv_teases);
        iv_teases.setVisibility(View.GONE);

        RelativeLayout rl_notifications = ((MainActivity) mContext).findViewById(R.id.rl_notifications);
        rl_notifications.setVisibility(View.GONE);

        ImageView iv_menu = ((MainActivity) mContext).findViewById(R.id.iv_menu);
        RelativeLayout bottomMenu = ((MainActivity) mContext).findViewById(R.id.bottomMenu);

        TextView action_bar_heading = ((MainActivity) mContext).findViewById(R.id.action_bar_heading);

        iv_back.setVisibility(View.VISIBLE);
        iv_menu.setVisibility(View.GONE);
        bottomMenu.setVisibility(View.GONE);

        action_bar_heading.setText(getString(R.string.advance_search));

        ed_filter_name = view.findViewById(R.id.ed_filter_name);
        rl_search_interests = view.findViewById(R.id.rl_search_interests);
        rl_search_city = view.findViewById(R.id.rl_search_city);
        cv_filter = view.findViewById(R.id.cv_filter);
        ly_no_network = view.findViewById(R.id.ly_no_network);
        btn_try_again = view.findViewById(R.id.btn_try_again);
        btn_clear = view.findViewById(R.id.btn_clear);
        btn_search = view.findViewById(R.id.btn_search);
        tv_city_filter = view.findViewById(R.id.tv_city_filter);
        tv_filter_interest = view.findViewById(R.id.tv_filter_interest);

        rl_select_gender = view.findViewById(R.id.rl_select_gender);
        rl_body_type = view.findViewById(R.id.rl_body_type);
        rl_any_ethnicity = view.findViewById(R.id.rl_any_ethnicity);
        tv_gender = view.findViewById(R.id.tv_gender);
        tv_body_type = view.findViewById(R.id.tv_body_type);
        tv_ethnicity = view.findViewById(R.id.tv_ethnicity);

        age_rangeSeekbar = view.findViewById(R.id.age_rangeSeekbar);
        distance_rangeSeekbar = view.findViewById(R.id.distance_rangeSeekbar);

        tv_age_min_limit = view.findViewById(R.id.tv_age_min_limit);
        tv_age_max_limit = view.findViewById(R.id.tv_age_max_limit);

        tv_miles_min_limit = view.findViewById(R.id.tv_miles_min_limit);
        tv_miles_max_limit = view.findViewById(R.id.tv_miles_max_limit);

        // Setting Seekbar for selecting Age and Distance for filter
        setAgeDistanceSeekBar();
    }

    private void setFilterDataFromSession() {
        ed_filter_name.setText(session.getFilterUserName());
        tv_city_filter.setText(session.getFilterPlace());
        tv_filter_interest.setText(session.getFilterInterestString());


        String gender = "";
        switch (session.getFilterGender()) {
            case "man":
                gender = "Man";
                you_are_selectedId = 1;
                break;

            case "woman":
                gender = "Woman";
                you_are_selectedId = 2;
                break;

            case "couple":
                gender = "Couple";
                you_are_selectedId = 3;
                break;

            case "transgender_male":
                gender = "Transgender Male";
                you_are_selectedId = 4;
                break;

            case "transgender_female":
                gender = "Transgender Female";
                you_are_selectedId = 5;
                break;


            case "neutral":
                gender = "Neutral";
                you_are_selectedId = 6;
                break;

        }

        tv_gender.setText(gender);

        for (int i = 0; i < bodyTypeList.size(); i++) {
            if (bodyTypeList.get(i).id.equals(session.getFilterBodyType())) {
                String body_type = bodyTypeList.get(i).value;
                tv_body_type.setText(body_type);
            }
        }

        for (int i = 0; i < ethnicityList.size(); i++) {
            if (ethnicityList.get(i).id.equals(session.getFilterEthnicity())) {
                String ethnicity = ethnicityList.get(i).value;
                tv_ethnicity.setText(ethnicity);
            }
        }

        if (!session.getFilterFromAge().equals("")) {
            age_rangeSeekbar.setMinStartValue(Integer.parseInt(session.getFilterFromAge())).apply();
        }
        if (!session.getFilterToAge().equals("")) {
            age_rangeSeekbar.setMaxStartValue(Integer.parseInt(session.getFilterToAge())).apply();
        }
        if (!session.getFilterMinDistance().equals("") && !session.getFilterMaxDistance().equals("")) {
            distance_rangeSeekbar.setMinStartValue(Integer.parseInt(session.getFilterMaxDistance())).apply();
        }

    }

    // Setting Seekbar for selecting Age and Distance for filter
    private void setAgeDistanceSeekBar() {
        if (session.getFilterFromAge().equals("") && session.getFilterToAge().equals("")) {
            age_rangeSeekbar.setMinStartValue(18).setMaxStartValue(100).apply();
            fromAge = "18";
            toAge = "100";
        } else if (!session.getFilterToAge().equals("")) {
            int fAge = Integer.parseInt(session.getFilterFromAge());
            int tAge = Integer.parseInt(session.getFilterToAge());
            age_rangeSeekbar.setMinStartValue(fAge).setMaxStartValue(tAge).apply();
        } else {
            age_rangeSeekbar.setMinStartValue(Integer.parseInt(session.getFilterFromAge())).setMaxStartValue(100).apply();
        }

        // set listener
        age_rangeSeekbar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                String s = String.valueOf(minValue);
                int i = Integer.parseInt(s);

                if (i < 18) {
                    i = 18;
                    age_rangeSeekbar.setMinStartValue(18).apply();
                }

                tv_age_min_limit.setText(String.valueOf(i));
                tv_age_max_limit.setText(String.valueOf(maxValue));

                fromAge = String.valueOf(i);
                toAge = String.valueOf(maxValue);

            }
        });

        // set final value listener
        age_rangeSeekbar.setOnRangeSeekbarFinalValueListener(new OnRangeSeekbarFinalValueListener() {
            @Override
            public void finalValue(Number minValue, Number maxValue) {
                fromAge = String.valueOf(minValue);
                toAge = String.valueOf(maxValue);
            }
        });

        // get seekbar from view
        distance_rangeSeekbar.setPosition(CrystalSeekbar.Position.LEFT).apply();
        distance_rangeSeekbar.setMinStartValue(300).apply();
        rangMin = "";
        rangMax = "";

        distance_rangeSeekbar.setOnSeekbarChangeListener(new OnSeekbarChangeListener() {
            @Override
            public void valueChanged(Number value) {
                tv_miles_min_limit.setText(String.valueOf(0));
                tv_miles_max_limit.setText(String.valueOf(value));
            }
        });

        distance_rangeSeekbar.setOnSeekbarFinalValueListener(new OnSeekbarFinalValueListener() {
            @Override
            public void finalValue(Number value) {
                rangMin = String.valueOf(0);
                rangMax = String.valueOf(value);
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
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
               /* selectedInterestList.clear();*/
                ((MainActivity) mContext).replaceFragment(MatchListFragment.newInstance(), false, R.id.fragment_place);
                break;

            case R.id.rl_search_interests:    // Layout to open interests dialog
                openEditInterestsDialog();
                break;

            case R.id.btn_search:      // Filter Search Button
                if (AppHelper.isConnectingToInternet(mContext)) {
                    String name = ed_filter_name.getText().toString().trim();

                    String ed_gender = "";
                    String body_type = "";
                    String ethnicity = "";


                    for (int i = 0; i < bodyTypeList.size(); i++) {
                        if (bodyTypeList.get(i).value.equals(tv_body_type.getText().toString().trim())) {
                            body_type = bodyTypeList.get(i).id;
                        }
                    }

                    for (int i = 0; i < ethnicityList.size(); i++) {
                        if (ethnicityList.get(i).value.equals(tv_ethnicity.getText().toString().trim())) {
                            ethnicity = ethnicityList.get(i).id;
                        }
                    }

                    switch (you_are_selectedId) {
                        case 1:
                            ed_gender = "man";
                            break;

                        case 2:
                            ed_gender = "woman";
                            break;

                        case 3:
                            ed_gender = "couple";
                            break;

                        case 4:
                            ed_gender = "transgender_male";
                            break;

                        case 5:
                            ed_gender = "transgender_female";
                            break;

                        case 6:
                            ed_gender="neutral";
                            break;
                    }


                    session.setFilterUserName(name);
                    session.setFilterInterests(selected_interests);
                    session.setFilterCity(mLatitude, mLongitude);
                    session.setFilterPlaceInterest(tv_city_filter.getText().toString().trim(), interests);
                    session.setFilterAgeDistance(fromAge, toAge, rangMin, rangMax);
                    session.setGenderIntentBodyEthnicity(ed_gender, body_type, ethnicity);
                }
                ((MainActivity) mContext).replaceFragment(MatchListFragment.newInstance(), false, R.id.fragment_place);
                break;

            case R.id.btn_clear:      // Filter Clear Button
                session.setFilterUserName("");
                session.setFilterInterests("");
                session.setFilterCity("", "");
                session.setFilterPlaceInterest("", "");
                session.setFilterAgeDistance("18", "100", "0", "300");
                session.setGenderIntentBodyEthnicity("", "", "");
                session.setFilterSortBy("");
                session.setFilterRandomIndex("");
                ((MainActivity) mContext).replaceFragment(MatchListFragment.newInstance(), false, R.id.fragment_place);
                break;

            case R.id.rl_search_city:      // Search By City
                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(((MainActivity) mContext));
                    startActivityForResult(intent, Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.rl_select_gender:   // Select gender
                select_type = "you_are_a";
                openGenderDialog();
                break;

            case R.id.fl_man: {  // Select Man
                setInactiveGender();
                iv_man.setVisibility(View.GONE);
                gif_man.setVisibility(View.VISIBLE);
                iv_man_border.setVisibility(View.VISIBLE);
                tv_man.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                if (select_type.equals("you_are_a")) {
                    type_you_are = "man";
                }

            }
            break;
            case R.id.fl_woman: {     // Select Woman
                setInactiveGender();
                iv_woman.setVisibility(View.GONE);
                gif_woman.setVisibility(View.VISIBLE);
                iv_woman_border.setVisibility(View.VISIBLE);
                tv_woman.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                if (select_type.equals("you_are_a")) {
                    type_you_are = "woman";
                }


            }
            break;
            case R.id.fl_trans_male: {       // Select Transgender Male
                setInactiveGender();

                iv_trans_male.setVisibility(View.GONE);
                gif_trans_male.setVisibility(View.VISIBLE);
                iv_trans_male_border.setVisibility(View.VISIBLE);
                tv_trans_male.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));

                if (select_type.equals("you_are_a")) {
                    type_you_are = "transgender male";
                }


            }
            break;
            case R.id.fl_trans_female: {       // Select Transgender Female
                setInactiveGender();

                iv_trans_female.setVisibility(View.GONE);
                gif_trans_female.setVisibility(View.VISIBLE);
                iv_trans_female_border.setVisibility(View.VISIBLE);
                tv_trans_female.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));

                if (select_type.equals("you_are_a")) {
                    type_you_are = "transgender female";
                }


            }
            break;
            case R.id.fl_couple: {     // Select Couple
                setInactiveGender();
                iv_couple.setVisibility(View.GONE);
                gif_couple.setVisibility(View.VISIBLE);
                iv_couple_border.setVisibility(View.VISIBLE);
                tv_couple.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                if (select_type.equals("you_are_a")) {
                    type_you_are = "couple";
                }


            }
            break;
            case R.id.rl_body_type:  // Display dialog to select body type
                openDisplayOtherListDialog("body_type", bodyTypeList);
                break;

            case R.id.rl_any_ethnicity:  // Display dialog to select ethnicity
                openDisplayOtherListDialog("ethnicity", ethnicityList);
                break;

            case R.id.fl_neutral: {     // Select Couple
                setInactiveGender();
                iv_neutral.setVisibility(View.GONE);
                gif_neutral.setVisibility(View.VISIBLE);
                iv_couple_neutral_border.setVisibility(View.VISIBLE);
                tv_neutral.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                if (select_type.equals("you_are_a")) {
                    type_you_are = "neutral";
                }


            }
            break;






        }
    }



    // Display Interests List Dialog
    private void openEditInterestsDialog() {
        final Dialog interestDialog = new Dialog(mContext);
        interestDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        interestDialog.setContentView(R.layout.dialog_select_interests);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(interestDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        interestDialog.getWindow().setAttributes(lWindowParams);

        interestsList = new ArrayList<>();

        ImageView dialog_decline_button = interestDialog.findViewById(R.id.dialog_decline_button);
        interest_listView = interestDialog.findViewById(R.id.lv_interests);

        dialog_decline_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interestDialog.dismiss();
            }
        });

        TextView btn_interest = interestDialog.findViewById(R.id.btn_interest);
        btn_interest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppHelper.isConnectingToInternet(mContext)) {
                    selectedInterestList.clear();

                    for (int i = 0; i < interestsList.size(); i++) {
                        if (interestsList.get(i).isChecked) {
                            InterestsModel model = new InterestsModel();
                            model.interest = interestsList.get(i).interest;
                            model.interestId = interestsList.get(i).interestId;
                            selectedInterestList.add(model);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    // Set selected interests in Interest text view
                    setInterestStringAsText();
                    interestDialog.dismiss();
                } else {
                    interestDialog.dismiss();
                    CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.alert_no_network));
                }
            }
        });
        isdisplay = true;
        // Call getInterest List Api only if list size is 0, else only display list
        if (interestsList.size() == 0) {
            getInterestList();
        } else displayInterestsDialog(interestsList);

        interestDialog.getWindow().setGravity(Gravity.CENTER);
        interestDialog.show();
    }

    // Set selected interests in Interest text view
    private void setInterestStringAsText() {
        StringBuilder sb = new StringBuilder();
        if (selectedInterestList.size() != 0) {
            for (int a = 0; a < interestsList.size(); a++) {
                for (int b = 0; b < selectedInterestList.size(); b++) {
                    if (interestsList.get(a).interestId.equals(selectedInterestList.get(b).interestId)) {
                        String interest = interestsList.get(a).interest.substring(0, 1).toUpperCase() + interestsList.get(a).interest.substring(1);
                        sb.append(interest);
                        sb.append(",");
                    }
                }
            }
            interests = sb.deleteCharAt(sb.lastIndexOf(",")).toString();
            tv_filter_interest.setText(interests);
        } else {
            tv_filter_interest.setText("");
        }


    }

    // Display Interests List items in Interests Dialog
    private void displayInterestsDialog(final ArrayList<InterestsModel> interestsList) {
        selected_interests = session.getFilterInterests();

        // Adapter to display interests
        adapter = new FilterInterestsAdapter(selected_interests, mContext, interestsList, new InterestIdListener() {
            @Override
            public void getInterest(String interest) {
                selected_interests = interest;
            }
        });

        interest_listView.setAdapter(adapter);

        if (!session.getFilterInterests().equals("")) {
            selectedInterestList.clear();
            ArrayList<String> list = new ArrayList<>();
            list.addAll(Arrays.asList(session.getFilterInterests().split(",")));

            for (int i = 0; i < interestsList.size(); i++) {
                for (int j = 0; j < list.size(); j++) {
                    if (interestsList.get(i).interestId.equals(list.get(j))) {
                        interestsList.get(i).isChecked = true;
                    }
                }
            }
            adapter.notifyDataSetChanged();
        } else {
            String interest = tv_filter_interest.getText().toString().trim();
            if (session.getFilterInterests().equals("") && !interest.equals(mContext.getString(R.string.select_kinks))) {
                selectedInterestList.clear();

                ArrayList<String> intList = new ArrayList<>();
                intList.addAll(Arrays.asList(interest.split(",")));

                for (int i = 0; i < interestsList.size(); i++) {
                    for (int j = 0; j < intList.size(); j++) {
                        if (interestsList.get(i).interest.equals(intList.get(j).toLowerCase())) {
                            interestsList.get(i).isChecked = true;
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    // Get Interests List
    private void getInterestList() {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    interestsList.clear();
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            cv_filter.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);


                            JSONArray interestsArray = js.getJSONArray("interest");

                            for (int i = 0; i < interestsArray.length(); i++) {
                                JSONObject object = interestsArray.getJSONObject(i);

                                InterestsModel interestsModel = new InterestsModel();
                                interestsModel.interestId = object.getString("interestId");
                                interestsModel.interest = object.getString("interest");

                                interestsList.add(interestsModel);
                            }
                            if (isdisplay) {
                                displayInterestsDialog(interestsList);
                            } else {
                                setPreSelectedFromSession();     // Set selected interest from session
                            }

                        } else {
                            progress.dismiss();
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                }

            });
            api.callApi("getInterestList", Request.Method.GET, null);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            cv_filter.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getInterestList();
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Autocomplete Place Api
        if (requestCode == Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE) {    // Select city for search by city
            if (resultCode == -1) {
                Place place = PlaceAutocomplete.getPlace(mContext, data);

                // Parse Country, State and City from entered address
                getAddress(place);
            }
        }
    }

    // Dialog to select gender
    private void openGenderDialog() {
        genderDialog = new Dialog(mContext);
        genderDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        genderDialog.setContentView(R.layout.dialog_select_gender);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(genderDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        genderDialog.getWindow().setAttributes(lWindowParams);

        initGenderDialog();

        tv_intent_heading.setText(getString(R.string.looking_for));
        tv_intent_title.setVisibility(View.GONE);
        // Setting inactive all gender
        setInactiveGender();

        // If gender already selected, show already selected
        genderAlreadySelected();

        btn_gender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (select_type.equals("you_are_a")) {
                    switch (type_you_are) {
                        case "man":
                            tv_gender.setText(getString(R.string.man));
                            genderDialog.dismiss();
                            you_are_selectedId = 1;
                            break;

                        case "woman":
                            tv_gender.setText(getString(R.string.woman));
                            genderDialog.dismiss();
                            you_are_selectedId = 2;
                            break;

                        case "couple":
                            tv_gender.setText(getString(R.string.couple));
                            genderDialog.dismiss();
                            you_are_selectedId = 3;
                            break;

                        case "transgender male":
                            tv_gender.setText(getString(R.string.trans_male));
                            genderDialog.dismiss();
                            you_are_selectedId = 4;
                            break;

                        case "transgender female":
                            tv_gender.setText(getString(R.string.trans_female));
                            genderDialog.dismiss();
                            you_are_selectedId = 5;
                            break;

                        case "neutral" :
                            tv_gender.setText(getString(R.string.neutral));
                            genderDialog.dismiss();
                            you_are_selectedId=6;
                            break;

                        default:
                            genderDialog.dismiss();
                            break;
                    }
                }
            }
        });

        fl_man.setOnClickListener(this);
        fl_woman.setOnClickListener(this);
        fl_couple.setOnClickListener(this);
        fl_trans_male.setOnClickListener(this);
        fl_trans_female.setOnClickListener(this);
        fl_neutral.setOnClickListener(this);

        genderDialog.getWindow().setGravity(Gravity.CENTER);
        genderDialog.show();
    }

    private void initGenderDialog() {
        ImageView dialog_decline_button = genderDialog.findViewById(R.id.dialog_decline_button);
        dialog_decline_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type_you_are = "";
                genderDialog.dismiss();
            }
        });

        fl_man = genderDialog.findViewById(R.id.fl_man);
        fl_woman = genderDialog.findViewById(R.id.fl_woman);
        fl_couple = genderDialog.findViewById(R.id.fl_couple);
        fl_trans_male = genderDialog.findViewById(R.id.fl_trans_male);
        fl_neutral = genderDialog.findViewById(R.id.fl_neutral);
        fl_trans_female = genderDialog.findViewById(R.id.fl_trans_female);

        gif_man = genderDialog.findViewById(R.id.gif_man);
        gif_woman = genderDialog.findViewById(R.id.gif_woman);
        gif_couple = genderDialog.findViewById(R.id.gif_couple);
        gif_trans_male = genderDialog.findViewById(R.id.gif_trans_male);
        gif_trans_female = genderDialog.findViewById(R.id.gif_trans_female);
        gif_neutral = genderDialog.findViewById(R.id.gif_neutral);

        iv_man = genderDialog.findViewById(R.id.iv_man);
        iv_woman = genderDialog.findViewById(R.id.iv_woman);
        iv_couple = genderDialog.findViewById(R.id.iv_couple);
        iv_trans_male = genderDialog.findViewById(R.id.iv_trans_male);
        iv_trans_female = genderDialog.findViewById(R.id.iv_trans_female);

        tv_man = genderDialog.findViewById(R.id.tv_man);
        tv_woman = genderDialog.findViewById(R.id.tv_woman);
        tv_couple = genderDialog.findViewById(R.id.tv_couple);
        tv_trans_male = genderDialog.findViewById(R.id.tv_trans_male);
        tv_trans_female = genderDialog.findViewById(R.id.tv_trans_female);

        tv_intent_heading = genderDialog.findViewById(R.id.tv_intent_heading);
        tv_intent_title = genderDialog.findViewById(R.id.tv_intent_title);
        tv_neutral = genderDialog.findViewById(R.id.tv_neutral);
        btn_gender = genderDialog.findViewById(R.id.btn_gender);

        iv_man_border = genderDialog.findViewById(R.id.iv_man_border);
        iv_woman_border = genderDialog.findViewById(R.id.iv_woman_border);
        iv_couple_border = genderDialog.findViewById(R.id.iv_couple_border);
        iv_trans_male_border = genderDialog.findViewById(R.id.iv_trans_male_border);
        iv_trans_female_border = genderDialog.findViewById(R.id.iv_trans_female_border);
        iv_couple_neutral_border = genderDialog.findViewById(R.id.iv_couple_neutral_border);
        iv_neutral = genderDialog.findViewById(R.id.iv_neutral);
    }

    // Set selected gender active
    private void genderAlreadySelected() {
        setGender();
        if (select_type.equals("you_are_a")) {
            switch (you_are_selectedId) {
                case 0:
                    setInactiveGender();
                    break;

                case 1:
                    setInactiveGender();
                    iv_man.setVisibility(View.GONE);
                    gif_man.setVisibility(View.VISIBLE);
                    iv_man_border.setVisibility(View.VISIBLE);
                    tv_man.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                    break;

                case 2:
                    setInactiveGender();
                    iv_woman.setVisibility(View.GONE);
                    gif_woman.setVisibility(View.VISIBLE);
                    iv_woman_border.setVisibility(View.VISIBLE);
                    tv_woman.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                    break;

                case 3:
                    setInactiveGender();
                    iv_couple.setVisibility(View.GONE);
                    gif_couple.setVisibility(View.VISIBLE);
                    iv_couple_border.setVisibility(View.VISIBLE);
                    tv_couple.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                    break;

                case 4:
                    setInactiveGender();
                    iv_trans_male.setVisibility(View.GONE);
                    gif_trans_male.setVisibility(View.VISIBLE);
                    iv_trans_male_border.setVisibility(View.VISIBLE);
                    tv_trans_male.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                    break;

                case 5:
                    setInactiveGender();
                    iv_trans_female.setVisibility(View.GONE);
                    gif_trans_female.setVisibility(View.VISIBLE);
                    iv_trans_female_border.setVisibility(View.VISIBLE);
                    tv_trans_female.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                    break;

                case 6:
                    setInactiveGender();
                    iv_neutral.setVisibility(View.GONE);
                    gif_neutral.setVisibility(View.VISIBLE);
                    iv_couple_neutral_border.setVisibility(View.VISIBLE);
                    tv_neutral.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));

            }
        }
    }

    private void setGender() {
        if (select_type.equals("you_are_a")) {
            String you_are = tv_gender.getText().toString().trim().toLowerCase();
            type_you_are = you_are;
            switch (you_are) {
                case "man":
                    you_are_selectedId = 1;
                    break;

                case "woman":
                    you_are_selectedId = 2;
                    break;

                case "couple":
                    you_are_selectedId = 3;
                    break;

                case "transgender male":
                    you_are_selectedId = 4;
                    break;

                case "transgender female":
                    you_are_selectedId = 5;
                    break;

                case "neutral":
                    you_are_selectedId=6;
                    break;

            }
        }
    }

    // Set all genders inactive
    private void setInactiveGender() {
        iv_man.setVisibility(View.VISIBLE);
        gif_man.setVisibility(View.GONE);
        iv_man_border.setVisibility(View.GONE);

        iv_woman.setVisibility(View.VISIBLE);
        gif_woman.setVisibility(View.GONE);
        iv_woman_border.setVisibility(View.GONE);

        iv_couple.setVisibility(View.VISIBLE);
        gif_couple.setVisibility(View.GONE);
        iv_couple_border.setVisibility(View.GONE);

        iv_trans_male.setVisibility(View.VISIBLE);
        gif_trans_male.setVisibility(View.GONE);
        iv_trans_male_border.setVisibility(View.GONE);

        iv_trans_female.setVisibility(View.VISIBLE);
        gif_trans_female.setVisibility(View.GONE);
        iv_trans_female_border.setVisibility(View.GONE);

        iv_neutral.setVisibility(View.VISIBLE);
        gif_neutral.setVisibility(View.GONE);
        iv_couple_neutral_border.setVisibility(View.GONE);


        tv_man.setTextColor(ContextCompat.getColor(mContext,R.color.inactive_text_color));
        tv_woman.setTextColor(ContextCompat.getColor(mContext,R.color.inactive_text_color));
        tv_couple.setTextColor(ContextCompat.getColor(mContext,R.color.inactive_text_color));
        tv_trans_male.setTextColor(ContextCompat.getColor(mContext,R.color.inactive_text_color));
        tv_trans_female.setTextColor(ContextCompat.getColor(mContext,R.color.inactive_text_color));
        tv_neutral.setTextColor(ContextCompat.getColor(mContext,R.color.inactive_text_color));
    }

    // Api call to get all drop down lists
    private void callGetAllDropDownListApi() {
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
                            cv_filter.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            Gson gson = new Gson();
                            EditOtherInfoModel model = gson.fromJson(String.valueOf(js), EditOtherInfoModel.class);
                            ethnicityList.addAll(model.dropDownList.ethnicity);
                            bodyTypeList.addAll(model.dropDownList.bodyType);

                            Collections.sort(ethnicityList, new Comparator<EditOtherInfoModel.DropDownListBean.OtherInfoBean>() {
                                @Override
                                public int compare(EditOtherInfoModel.DropDownListBean.OtherInfoBean lhs, EditOtherInfoModel.DropDownListBean.OtherInfoBean rhs) {
                                    return lhs.value.compareTo(rhs.value);
                                }
                            });

                            Collections.sort(bodyTypeList, new Comparator<EditOtherInfoModel.DropDownListBean.OtherInfoBean>() {
                                @Override
                                public int compare(EditOtherInfoModel.DropDownListBean.OtherInfoBean lhs, EditOtherInfoModel.DropDownListBean.OtherInfoBean rhs) {
                                    return lhs.value.compareTo(rhs.value);
                                }
                            });

                            setFilterDataFromSession();
                        } else {
                            progress.dismiss();
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                }

            });
            api.callApi("getAllDropDownList", Request.Method.GET, null);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            cv_filter.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callGetAllDropDownListApi();
                }
            });
        }
    }

    // Open Dialog for Body-type, Ethnicity, Work, Education lists
    private void openDisplayOtherListDialog(final String list_type, ArrayList<EditOtherInfoModel.DropDownListBean.OtherInfoBean> list) {
        final Dialog listDialog = new Dialog(mContext);
        listDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        listDialog.setContentView(R.layout.dialog_other_info_list);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(listDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        listDialog.getWindow().setAttributes(lWindowParams);

        ImageView dialog_decline_button = listDialog.findViewById(R.id.dialog_decline_button);
        ListView basic_listView = listDialog.findViewById(R.id.basic_listView);
        final TextView list_header = listDialog.findViewById(R.id.list_header);

        switch (list_type) {
            case "body_type":
                list_header.setText(String.format("%s %s", getString(R.string.select), getString(R.string.body_type)));
                break;

            case "ethnicity":
                list_header.setText(String.format("%s %s", getString(R.string.select), getString(R.string.ethnicity)));
                break;
        }

        dialog_decline_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listDialog.dismiss();
            }
        });

        // Adapter to set lists
        listAdapter = new EditOtherInfoListAdapter(mContext, list, new InterestIdListener() {
            @Override
            public void getInterest(String interest) {
                // Listener to display selected item of list
                selectedItem = interest;
                listAdapter.notifyDataSetChanged();

                switch (list_type) {
                    case "body_type":
                        list_header.setText(String.format("%s %s", getString(R.string.select), getString(R.string.body_type)));
                        tv_body_type.setText(selectedItem);
                        break;

                    case "ethnicity":
                        list_header.setText(String.format("%s %s", getString(R.string.select), getString(R.string.ethnicity)));
                        tv_ethnicity.setText(selectedItem);
                        break;
                }
                listDialog.dismiss();
            }
        });

        basic_listView.setAdapter(listAdapter);

        listDialog.getWindow().setGravity(Gravity.CENTER);
        listDialog.show();
    }

    private void getAddress(final Place place) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            new AddressLocationTask(mContext, place, new AddressLocationTask.AddressLocationListner() {
                @Override
                public void getLocation(String cty, String st, String cntry, String locAddress) {
                    city = cty;
                    state = st;
                    country = cntry;

                    if (!country.equals("") && !state.equals("") && !city.equals("")) {
                        filterLocation = city + ", " + state + ", " + country;
                    } else if (!country.equals("") && !state.equals("")) {
                        filterLocation = state + ", " + country;
                    } else if (!country.equals("")) {
                        filterLocation = country;
                    }

                    if (place.getLatLng() != null) {
                        mLatitude = "" + place.getLatLng().latitude;
                        mLongitude = "" + place.getLatLng().longitude;
                    }
                    tv_city_filter.setText(filterLocation);
                }
            }).execute();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        inputFilter(ed_filter_name);
    }

    // Search by name on change in characters
    private void inputFilter(final EditText et) {
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = et.getText().toString();
                if (text.startsWith(" ")) {
                    et.setText(text.trim());
                }

                if (s.length() > 0) {
                    filterByName();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    // Filter by Name
    private void filterByName() {
        Validation validation = new Validation();
         if (!validation.isNameValid(ed_filter_name)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_name_invalid));
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

    @Override
    public void onStop() {
        super.onStop();
        if (progress != null) {
            progress.dismiss();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (progress != null) {
            progress.dismiss();
        }
    }
}
