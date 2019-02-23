package com.kinklink.modules.authentication.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
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
import com.kinklink.helper.Utils;
import com.kinklink.modules.authentication.activity.EditProfileActivity;
import com.kinklink.modules.authentication.activity.RegistrationActivity;
import com.kinklink.modules.authentication.adapter.EditOtherInfoListAdapter;
import com.kinklink.modules.authentication.adapter.HorizontalEditInterestAdapter;
import com.kinklink.modules.authentication.adapter.InterestsAdapter;
import com.kinklink.modules.authentication.listener.AdapterPositionListener;
import com.kinklink.modules.authentication.listener.InterestIdListener;
import com.kinklink.modules.authentication.model.BasicListInfoModel;
import com.kinklink.modules.authentication.model.EditOtherInfoModel;
import com.kinklink.modules.authentication.model.InterestsModel;
import com.kinklink.modules.authentication.model.RegistrationInfo;
import com.kinklink.modules.matches.adapter.PreferencesAdapter;
import com.kinklink.server_task.WebService;
import com.kinklink.session.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class EditOtherInfoFragment extends Fragment implements View.OnClickListener {
    private Session session;
    private EditProfileActivity activity;
    private RegistrationInfo registrationInfo;

    private ImageView iv_back, iv_step_one, iv_step_two, iv_step_three, iv_step_one_bullet, iv_step_two_bullet;
    private TextView tv_step_two, tv_other_info, tv_step_three, tv_basic_info;
    private View status_view_1;
    private EditOtherInfoListAdapter listAdapter;
    private TextView btn_skip;

    private ArrayList<EditOtherInfoModel.DropDownListBean.OtherInfoBean> educationList, ethnicityList, bodyTypeList;

    private RelativeLayout rl_interests;
    private RelativeLayout rl_body_type;
    private RelativeLayout rl_ethnicity;
    private RelativeLayout rl_work;
    private RelativeLayout rl_education;
    private RelativeLayout rl_city_town;
    private RelativeLayout rl_preferences;
    private TextView tv_body_type, tv_ethnicity, tv_education, tv_city_town, tv_preferences;
    private EditText ed_about, tv_work;
    private String selectedItem;
    private TextView btn_edit_other_info;

    private ArrayList<InterestsModel> interestsList;
    private ArrayList<InterestsModel> selectedInterestList;
    private String selected_interests = "";
    private InterestsAdapter adapter;
    private ListView interest_listView;
    private RecyclerView horizontal_recycler_view;
    private HorizontalEditInterestAdapter horizontalAdapter;
    private String interestId = "";

    // variable to track event time
    private long mLastClickTime = 0;
    private String country="", state="", mLatitude="", mLongitude="", city="", editLocation="";

    private LinearLayout ly_no_network, ly_edit_profile;
    private TextView btn_try_again;

    private PreferencesAdapter prefAdapter;
    private ArrayList<BasicListInfoModel> prefList, selectedPrefList;
    private String selectItem = "";
    private Dialog prefDialog;
    private Progress progress;

    public EditOtherInfoFragment() {
    }

    public static EditOtherInfoFragment newInstance() {
        Bundle args = new Bundle();
        EditOtherInfoFragment fragment = new EditOtherInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefList = new ArrayList<>();
        prefList = activity.prefList;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_other_info, container, false);
        init(view);

        progress = new Progress(activity);
        session = new Session(activity);
        registrationInfo = session.getRegistration();

        //   workList = new ArrayList<>();
        educationList = new ArrayList<>();
        ethnicityList = new ArrayList<>();
        bodyTypeList = new ArrayList<>();
        selectedPrefList = new ArrayList<>();
        selectedInterestList = new ArrayList<>();

        // getPreferenceIntentList();

        // Call getAllDropDownList Api
       // callGetAllDropDownListApi();

        getMyProfile();


        // Set Basic Info Fragment Active
        setOtherInfoFragmentActive();

        iv_back.setOnClickListener(this);
        rl_interests.setOnClickListener(this);
        //    rl_intent.setOnClickListener(this);
        rl_body_type.setOnClickListener(this);
        rl_ethnicity.setOnClickListener(this);
        rl_work.setOnClickListener(this);
        rl_education.setOnClickListener(this);
        rl_preferences.setOnClickListener(this);
        rl_city_town.setOnClickListener(this);
        btn_skip.setOnClickListener(this);

        btn_edit_other_info.setOnClickListener(this);

        return view;
    }

    // Set prefilled the registration data
    public void setOtherInfoData() {
        registrationInfo = session.getRegistration();
        if (registrationInfo.userDetail.interests.size() != 0) {
            selectedInterestList.clear();
            for (int i = 0; i < registrationInfo.userDetail.interests.size(); i++) {
                InterestsModel model = new InterestsModel();
                model.interest = registrationInfo.userDetail.interests.get(i).interest;
                model.interestId = registrationInfo.userDetail.interests.get(i).interestId;
                model.isChecked = true;
                selectedInterestList.add(model);
            }
        }

        // Adapter to display selected interests in horizontal list
        horizontalAdapter = new HorizontalEditInterestAdapter(selectedInterestList, new AdapterPositionListener() {
            @Override
            public void getPosition(int position) {
                // Listener to remove selected interest
                selectedInterestList.remove(position);
                horizontalAdapter.notifyDataSetChanged();

            }
        });
      //  LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(activity);
        horizontal_recycler_view.setLayoutManager(horizontalLayoutManager);
        horizontal_recycler_view.setAdapter(horizontalAdapter);

        if (!registrationInfo.userDetail.body_type_name.equals("")) {
            tv_body_type.setText(registrationInfo.userDetail.body_type_name);

            for (int i = 0; i < bodyTypeList.size(); i++) {
                if (bodyTypeList.get(i).id.equals(registrationInfo.userDetail.body_type)) {
                    bodyTypeList.get(i).isChecked = true;
                }
            }
        }

        if (!registrationInfo.userDetail.ethnicity_name.equals("")) {
            tv_ethnicity.setText(registrationInfo.userDetail.ethnicity_name);

            for (int i = 0; i < ethnicityList.size(); i++) {
                if (ethnicityList.get(i).id.equals(registrationInfo.userDetail.ethnicity)) {
                    ethnicityList.get(i).isChecked = true;
                }
            }
        }


        if (!registrationInfo.userDetail.work.equals("")) {
            String work = registrationInfo.userDetail.work.substring(0, 1).toUpperCase() + registrationInfo.userDetail.work.substring(1);
            tv_work.setText(work);
        }

        if (!registrationInfo.userDetail.education_name.equals("")) {
            tv_education.setText(registrationInfo.userDetail.education_name);

            for (int i = 0; i < educationList.size(); i++) {
                if (educationList.get(i).id.equals(registrationInfo.userDetail.education)) {
                    educationList.get(i).isChecked = true;
                }
            }
        }

        String address=session.getRegistration().userDetail.full_address;
        tv_city_town.setText(address);



        if (!registrationInfo.userDetail.full_address.equals("")) {
            editLocation = registrationInfo.userDetail.full_address;
            tv_city_town.setText(registrationInfo.userDetail.full_address);
            city = registrationInfo.userDetail.city;
            mLatitude = registrationInfo.userDetail.latitude;
            mLongitude = registrationInfo.userDetail.longitude;
        }

        if (!registrationInfo.userDetail.about.equals("")) {
            ed_about.setText(registrationInfo.userDetail.about);
        }

        if (registrationInfo.userDetail.preference != null && !registrationInfo.userDetail.preference.equals("")) {
            ArrayList<String> list = new ArrayList<>();
            list.addAll(Arrays.asList(registrationInfo.userDetail.preference.split(",")));

            if (prefList != null && prefList.size() > 0) {
                setPreferencesFromListToText(list);
            } else {
                prefList = new ArrayList<>();
                prefList = activity.prefList;

                setPreferencesFromListToText(list);
            }

            for (int i = 0; i < prefList.size(); i++) {
                if (prefList.get(i).isChecked) {
                    BasicListInfoModel model = new BasicListInfoModel();
                    model.item_key = prefList.get(i).item_key;
                    if (model.item_key.contains("neutral")){
                        model.selected_item="Neutral";
                    }else {
                        model.selected_item = prefList.get(i).selected_item;
                    }

                    selectedPrefList.add(model);
                }
            }

        }
    }

    private void setPreferencesFromListToText(ArrayList<String> list) {
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

    private void init(View view) {
        iv_back = activity.findViewById(R.id.iv_back);
        iv_step_one = activity.findViewById(R.id.iv_step_one);
        iv_step_one_bullet = activity.findViewById(R.id.iv_step_one_bullet);
        iv_step_two_bullet = activity.findViewById(R.id.iv_step_two_bullet);
        iv_step_two = activity.findViewById(R.id.iv_step_two);
        tv_step_two = activity.findViewById(R.id.tv_step_two);
        btn_skip = view.findViewById(R.id.btn_skip);
        tv_other_info = activity.findViewById(R.id.tv_other_info);
        tv_basic_info = activity.findViewById(R.id.tv_basic_info);
        status_view_1 = activity.findViewById(R.id.status_view_1);
        iv_step_three = activity.findViewById(R.id.iv_step_three);
        tv_step_three = activity.findViewById(R.id.tv_step_three);

        rl_interests = view.findViewById(R.id.rl_interests);

        rl_body_type = view.findViewById(R.id.rl_body_type);
        rl_ethnicity = view.findViewById(R.id.rl_ethnicity);
        rl_work = view.findViewById(R.id.rl_work);
        rl_education = view.findViewById(R.id.rl_education);
        rl_preferences = view.findViewById(R.id.rl_preferences);
        rl_city_town = view.findViewById(R.id.rl_city_town);

        tv_body_type = view.findViewById(R.id.tv_body_type);
        tv_ethnicity = view.findViewById(R.id.tv_ethnicity);
        tv_work = view.findViewById(R.id.tv_work);
        tv_education = view.findViewById(R.id.tv_education);
        tv_preferences = view.findViewById(R.id.tv_preferences);
        tv_city_town = view.findViewById(R.id.tv_city_town);
        ed_about = view.findViewById(R.id.ed_about);

        btn_edit_other_info = view.findViewById(R.id.btn_edit_basic_info);

        horizontal_recycler_view = view.findViewById(R.id.horizontal_recycler_view);

        ly_edit_profile = activity.findViewById(R.id.ly_edit_profile);
        ly_no_network = activity.findViewById(R.id.ly_no_network);
        btn_try_again = activity.findViewById(R.id.btn_try_again);
    }

    // Set other info fragment active
    public void setOtherInfoFragmentActive() {
        iv_back.setVisibility(View.VISIBLE);
        iv_step_one.setVisibility(View.VISIBLE);
        iv_step_one_bullet.setVisibility(View.GONE);
        iv_step_two.setVisibility(View.GONE);
        iv_step_two_bullet.setVisibility(View.VISIBLE);
        tv_step_two.setVisibility(View.GONE);
        iv_step_three.setVisibility(View.GONE);
        tv_step_three.setVisibility(View.VISIBLE);
        Utils.setTypeface(tv_basic_info, activity, R.font.lato_regular);
        Utils.setTypeface(tv_other_info, activity, R.font.lato_bold);
        status_view_1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (EditProfileActivity) context;
    }

    @Override
    public void onClick(View v) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (v.getId()) {
            case R.id.iv_back:  // Back Icon Press
                activity.onBackPressed();
                break;

            case R.id.rl_interests:   // Open Edit Interests Dialog
                openEditInterestsDialog();
                break;

            case R.id.rl_body_type:  // Display dialog to select body type
                openDisplayOtherListDialog("body_type", bodyTypeList);
                break;

            case R.id.rl_ethnicity:  // Display dialog to select ethnicity
                openDisplayOtherListDialog("ethnicity", ethnicityList);
                break;

            case R.id.rl_education:  // Display dialog to select education
                openDisplayOtherListDialog("education", educationList);
                break;

            case R.id.rl_city_town:  // Open place picker to enter city/town
                try {
                    rl_city_town.setEnabled(false);
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(activity);
                    startActivityForResult(intent, Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.btn_edit_basic_info:
                String fullAddress = tv_city_town.getText().toString().trim();
                String work = tv_work.getText().toString().trim();
                String body_type = "";
                String ethnicity = "";
                String education = "";

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

                for (int i = 0; i < educationList.size(); i++) {
                    if (educationList.get(i).value.equals(tv_education.getText().toString().trim())) {
                        education = educationList.get(i).id;
                    }
                }

                if (isValid()) {
                    callUpdateOtherInfoApi(fullAddress, body_type, ethnicity, work, education);
                }

                break;

            case R.id.rl_preferences:
                openSetPreferencesDialog();
                break;


            case R.id.btn_skip:
                activity.addFragment(EditProfilePictureFragment.newInstance(), true, R.id.edit_fragment_place);
                break;

        }
    }

    // Validations for updating other info
    private boolean isValid() {
        if (selectedInterestList.size() == 0) {
            CustomToast.getInstance(activity).showToast(activity, getString(R.string.acc_interest_null));
            return false;
        } /*else if (editLocation == null || editLocation.equals("")) {
            CustomToast.getInstance(activity).showToast(activity, getString(R.string.acc_city_null));
            return false;
        } else if (tv_body_type.getText().toString().equals(getString(R.string.select))) {
            CustomToast.getInstance(activity).showToast(activity, getString(R.string.acc_body_type_null));
            return false;
        } else if (tv_ethnicity.getText().toString().equals(getString(R.string.select))) {
            CustomToast.getInstance(activity).showToast(activity, getString(R.string.acc_ethnicity_null));
            return false;
        } else if (tv_work.getText().toString().equals("")) {
            CustomToast.getInstance(activity).showToast(activity, getString(R.string.acc_work_null));
            return false;
        } else if (tv_education.getText().toString().equals(getString(R.string.select))) {
            CustomToast.getInstance(activity).showToast(activity, getString(R.string.acc_education_null));
            return false;
        } else if (tv_preferences.getText().toString().equals(getString(R.string.select))) {
            CustomToast.getInstance(activity).showToast(activity, getString(R.string.preference_null));
            return false;
        } else if (ed_about.getText().toString().trim().equals("")) {
            CustomToast.getInstance(activity).showToast(activity, getString(R.string.acc_about_null));
            return false;
        }*/
        return true;
    }

    // Api to update other info
    private void callUpdateOtherInfoApi(final String fullAddress, final String body_type, final String ethnicity, final String work, final String education) {
        if (AppHelper.isConnectingToInternet(activity)) {
            progress.show();

            interestId = "";
            for (int i = 0; i < selectedInterestList.size(); i++) {
                interestId = String.format("%s,%s", selectedInterestList.get(i).interestId, interestId);
            }

            if (interestId.endsWith(",")) {
                interestId = interestId.substring(0, interestId.length() - 1);
            }

            Map<String, String> map = getHashMapData(fullAddress, body_type, ethnicity, work, education);

            WebService api = new WebService(activity, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            ly_edit_profile.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            Gson gson = new Gson();
                            RegistrationInfo registrationInfo = gson.fromJson(String.valueOf(js), RegistrationInfo.class);
                            registrationInfo.userDetail.city = city;
                            registrationInfo.userDetail.latitude = mLatitude;
                            registrationInfo.userDetail.longitude = mLongitude;

                            session.showPopup(false);
                            session.createRegistration(registrationInfo);

                            activity.addFragment(EditProfilePictureFragment.newInstance(), true, R.id.edit_fragment_place);

                        } else {
                            progress.dismiss();
                            CustomToast.getInstance(activity).showToast(activity, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        CustomToast.getInstance(activity).showToast(activity, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                }

            });
            api.callApi("user/updateOtherInfo", Request.Method.POST, map);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, activity);
            }

            ly_edit_profile.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Preventing multiple clicks, using threshold of 1/2 second
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    callUpdateOtherInfoApi(fullAddress, body_type, ethnicity, work, education);
                }
            });
        }
    }

    // Input data for Update Edit Other Info Api
    private Map<String, String> getHashMapData(String fullAddress, String body_type, String ethnicity, String work, String education) {
        final Map<String, String> map = new HashMap<>();
        if (!city.equals("")) {
            map.put("city", city);
        } else {
            map.put("city", "");
        }

        if (!fullAddress.equals(getString(R.string.select))) {
            map.put("full_address", fullAddress);
        } else {
            map.put("full_address", "");
        }

        if (mLatitude != null) {
            map.put("latitude", mLatitude);
        } else {
            map.put("latitude", "");
        }

        if (mLongitude != null) {
            map.put("longitude", mLongitude);
        } else {
            map.put("longitude", "");
        }

        map.put("body_type", body_type);
        map.put("ethnicity", ethnicity);
        map.put("work", work);
        map.put("education", education);
        map.put("about", ed_about.getText().toString().trim());
        map.put("interest", interestId);

        ArrayList<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(tv_preferences.getText().toString().split(", ")));

        for (int i = 0; i < prefList.size(); i++) {
            for (int j = 0; j < list.size(); j++) {
                if (prefList.get(i).selected_item.equals(list.get(j))) {
                    prefList.get(i).isChecked = true;
                }
            }
        }

        StringBuilder str = new StringBuilder();
        for (int p = 0; p < prefList.size(); p++) {
            if (prefList.get(p).isChecked) {
                if (str.toString().equals("")) {
                    str = new StringBuilder(prefList.get(p).item_key + "");
                } else {
                    str.append(",").append(prefList.get(p).item_key);
                }
            }
        }

        map.put("preference", str.toString());

        return map;
    }

    // Api call to get all drop down lists
    private void callGetAllDropDownListApi() {
        if (AppHelper.isConnectingToInternet(activity)) {
            progress.show();

            WebService api = new WebService(activity, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            ly_edit_profile.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            Gson gson = new Gson();
                            EditOtherInfoModel model = gson.fromJson(String.valueOf(js), EditOtherInfoModel.class);
                            educationList.addAll(model.dropDownList.education);
                            ethnicityList.addAll(model.dropDownList.ethnicity);
                            bodyTypeList.addAll(model.dropDownList.bodyType);

                            Collections.sort(educationList, new Comparator<EditOtherInfoModel.DropDownListBean.OtherInfoBean>() {
                                @Override
                                public int compare(EditOtherInfoModel.DropDownListBean.OtherInfoBean lhs, EditOtherInfoModel.DropDownListBean.OtherInfoBean rhs) {
                                    return lhs.value.compareTo(rhs.value);
                                }
                            });

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

                            // Setting Other Info Data
                            setOtherInfoData();
                        } else {
                            progress.dismiss();
                            CustomToast.getInstance(activity).showToast(activity, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        CustomToast.getInstance(activity).showToast(activity, getString(R.string.went_wrong));
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
                AppHelper.hideKeyboard(view, activity);
            }

            ly_edit_profile.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Preventing multiple clicks, using threshold of 1/2 second
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    callGetAllDropDownListApi();
                }
            });
        }
    }

    // Open Dialog for Body-type, Ethnicity, Work, Education lists
    private void openDisplayOtherListDialog(final String list_type, ArrayList<EditOtherInfoModel.DropDownListBean.OtherInfoBean> list) {
        final Dialog listDialog = new Dialog(activity);
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

            case "education":
                list_header.setText(String.format("%s %s", getString(R.string.select), getString(R.string.education)));
                break;

        }

        dialog_decline_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                listDialog.dismiss();
            }
        });

        // Adapter to set lists
        listAdapter = new EditOtherInfoListAdapter(activity, list, new InterestIdListener() {
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

                    case "education":
                        list_header.setText(String.format("%s %s", getString(R.string.select), getString(R.string.education)));
                        tv_education.setText(selectedItem);
                        break;

                }
                listDialog.dismiss();
            }
        });
        basic_listView.setAdapter(listAdapter);

        listDialog.getWindow().setGravity(Gravity.CENTER);
        listDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Autocomplete Place Api
        if (requestCode == Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            rl_city_town.setEnabled(true);
            if (resultCode == -1) {
                Place place = PlaceAutocomplete.getPlace(activity, data);

                // Parse Country, State and City from entered address
                getAddress(place);
            }
        }
    }


    // Display Interests List Dialog
    private void openEditInterestsDialog() {
        final Dialog interestDialog = new Dialog(activity);
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
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                interestDialog.dismiss();
            }
        });

        TextView btn_interest = interestDialog.findViewById(R.id.btn_interest);
        btn_interest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                selectedInterestList.clear();

                for (int i = 0; i < interestsList.size(); i++) {
                    if (interestsList.get(i).isChecked) {
                        InterestsModel model = new InterestsModel();
                        model.interest = interestsList.get(i).interest;
                        model.interestId = interestsList.get(i).interestId;
                        selectedInterestList.add(model);
                    }
                }

                if (selectedInterestList.size() == 0) {
                    CustomToast.getInstance(activity).showToast(activity, getString(R.string.acc_interest_null));
                } else {
                    adapter.notifyDataSetChanged();
                    if(horizontalAdapter!=null)
                    horizontalAdapter.notifyDataSetChanged();
                    interestDialog.dismiss();
                }
            }
        });


        // Call getInterest List Api only if list size is 0, else only display list
        if (interestsList.size() == 0) {
            getInterestList();
        } else displayInterestsDialog(interestsList);

        interestDialog.getWindow().setGravity(Gravity.CENTER);
        interestDialog.show();
    }

    // Display Interests List items in Interests Dialog
    private void displayInterestsDialog(final ArrayList<InterestsModel> interestsList) {
        if (selectedInterestList.size() != 0) {
            for (int a = 0; a < interestsList.size(); a++) {
                for (int b = 0; b < selectedInterestList.size(); b++) {
                    if (interestsList.get(a).interestId.equals(selectedInterestList.get(b).interestId)) {
                        interestsList.get(a).isChecked = true;
                    }
                }
            }
        }

        // Adapter to display interests
        adapter = new InterestsAdapter(selected_interests, activity, interestsList, new InterestIdListener() {
            @Override
            public void getInterest(String interest) {
                selected_interests = interest;
            }
        });

        interest_listView.setAdapter(adapter);
    }

    // Get Interests List
    private void getInterestList() {
        if (AppHelper.isConnectingToInternet(activity)) {
            progress.show();

            WebService api = new WebService(activity, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    interestsList.clear();
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            ly_edit_profile.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            JSONArray interestsArray = js.getJSONArray("interest");

                            for (int i = 0; i < interestsArray.length(); i++) {
                                JSONObject object = interestsArray.getJSONObject(i);

                                InterestsModel interestsModel = new InterestsModel();
                                interestsModel.interestId = object.getString("interestId");
                                interestsModel.interest = object.getString("interest");

                                interestsList.add(interestsModel);
                            }
                            displayInterestsDialog(interestsList);

                        } else {
                            progress.dismiss();
                            CustomToast.getInstance(activity).showToast(activity, message);
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
                AppHelper.hideKeyboard(view, activity);
            }

            ly_edit_profile.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Preventing multiple clicks, using threshold of 1/2 second
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    getInterestList();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        View view = getView();
        if (view != null) {
            AppHelper.hideKeyboard(view, activity);
        }


    }

    // Parse Country, State and City from entered address
    private void getAddress(final Place place) {
        if (AppHelper.isConnectingToInternet(activity)) {
            new AddressLocationTask(activity, place, new AddressLocationTask.AddressLocationListner() {
                @Override
                public void getLocation(String cty, String st, String cntry, String locAddress) {
                    city = cty;
                    state = st;
                    country = cntry;

                    if (!country.equals("") && !state.equals("") && !city.equals("")) {
                        editLocation = city + ", " + state + ", " + country;
                    } else if (!country.equals("") && !state.equals("")) {
                        editLocation = state + ", " + country;
                    } else if (!country.equals("")) {
                        editLocation = country;
                    }

                    if (place.getLatLng() != null) {
                        mLatitude = "" + place.getLatLng().latitude;
                        mLongitude = "" + place.getLatLng().longitude;
                    }

                    tv_city_town.setText(editLocation);
                }
            }).execute();
        }
    }

    //////////////////////// Intent Preferences
    private void  openSetPreferencesDialog() {
        prefDialog = new Dialog(activity);
        prefDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        prefDialog.setContentView(R.layout.dialog_select_preferences);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(prefDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        prefDialog.getWindow().setAttributes(lWindowParams);

        ImageView dialog_decline_button = prefDialog.findViewById(R.id.dialog_decline_button);
        ListView prefListView = prefDialog.findViewById(R.id.lv_interests);

        // Adapter to display interests
        prefAdapter = new PreferencesAdapter(selectItem, activity, prefList, new InterestIdListener() {
            @Override
            public void getInterest(String interest) {
                selectItem = interest;
            }
        });
        prefListView.setAdapter(prefAdapter);

        dialog_decline_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                prefDialog.dismiss();
            }
        });

        TextView btn_interest = prefDialog.findViewById(R.id.btn_interest);
        btn_interest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                selectedPrefList.clear();

                for (int i = 0; i < prefList.size(); i++) {
                    if (prefList.get(i).isChecked) {
                        BasicListInfoModel model = new BasicListInfoModel();
                        model.item_key = prefList.get(i).item_key;
                        model.selected_item = prefList.get(i).selected_item;
                        selectedPrefList.add(model);
                    }
                }

                if (selectedPrefList.size() == 0) {
                    CustomToast.getInstance(activity).showToast(activity, getString(R.string.preference_null));
                } else {
                    prefAdapter.notifyDataSetChanged();
                    setPreferenceStringAsText();
                    prefDialog.dismiss();
                }
            }
        });

        displayPreferenceInterestDialog(prefList);

        prefDialog.getWindow().setGravity(Gravity.CENTER);
        prefDialog.show();
    }


    private void displayPreferenceInterestDialog(ArrayList<BasicListInfoModel> prefList) {
        if (selectedPrefList.size() != 0) {
            for (int a = 0; a < prefList.size(); a++) {
                for (int b = 0; b < selectedPrefList.size(); b++) {
                    if (prefList.get(a).item_key.equals(selectedPrefList.get(b).item_key)) {
                        prefList.get(a).isChecked = true;
                    }
                }
            }
        }
    }

    // Set selected interests in Interest text view
    private void setPreferenceStringAsText() {
        StringBuilder sb = new StringBuilder();
        if (selectedPrefList.size() != 0) {
            for (int a = 0; a < prefList.size(); a++) {
                for (int b = 0; b < selectedPrefList.size(); b++) {
                    if (prefList.get(a).item_key.equals(selectedPrefList.get(b).item_key)) {
                        String pref = prefList.get(a).selected_item.substring(0, 1).toUpperCase() + prefList.get(a).selected_item.substring(1);
                        sb.append(pref);
                        sb.append(",");
                    }
                }
            }
            String pref_txt = sb.deleteCharAt(sb.lastIndexOf(",")).toString();
            tv_preferences.setText(pref_txt);
        } else {
            tv_preferences.setText(getString(R.string.select));
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


    // Get User details Api
    private void getMyProfile() {
        if (AppHelper.isConnectingToInternet(activity)) {

            WebService api = new WebService(activity, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {

                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            ly_no_network.setVisibility(View.GONE);

                            Gson gson = new Gson();


                            RegistrationInfo registrationInfo = gson.fromJson(String.valueOf(js), RegistrationInfo.class);
                            session.createRegistration(registrationInfo);
                            callGetAllDropDownListApi();



                            // Setting My Profile Details
                            // setMyProfile();

                        } else {
                            progress.dismiss();
                            ly_no_network.setVisibility(View.GONE);
                            CustomToast.getInstance(activity).showToast(activity, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();

                        ly_no_network.setVisibility(View.GONE);
                        CustomToast.getInstance(activity).showToast(activity, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    ly_no_network.setVisibility(View.GONE);
                }

            });
            api.callApi("user/userDetail", Request.Method.GET, null);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, activity);
            }

            //rl_main_activity.setVisibility(View.GONE);
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

}
