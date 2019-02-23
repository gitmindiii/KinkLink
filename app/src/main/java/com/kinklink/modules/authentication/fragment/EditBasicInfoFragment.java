package com.kinklink.modules.authentication.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.kinklink.R;
import com.kinklink.application.KinkLink;
import com.kinklink.helper.AppHelper;
import com.kinklink.helper.Constant;
import com.kinklink.helper.CustomToast;
import com.kinklink.helper.Progress;
import com.kinklink.helper.Utils;
import com.kinklink.helper.Validation;
import com.kinklink.modules.authentication.activity.EditProfileActivity;
import com.kinklink.modules.authentication.model.FirebaseUserModel;
import com.kinklink.modules.authentication.model.RegistrationInfo;
import com.kinklink.server_task.WebService;
import com.kinklink.session.Session;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import pl.droidsonroids.gif.GifTextView;

public class EditBasicInfoFragment extends Fragment implements View.OnClickListener {
    private Session session;

    private Context mContext;
    private RegistrationInfo registrationInfo;
    private DatePickerDialog date_picker;
    private String date, gender, ed_gender;
    private Dialog genderDialog;
    private EditText ed_edit_name, ed_edit_email;
    private LinearLayout ly_date_picker, ly_edit_gender;
    private TextView tv_edit_dob, ed_edit_gender;
    private TextView btn_skip, btn_edit_basic_info;

    private ImageView iv_back, iv_step_one, iv_step_two, iv_step_three, iv_step_one_bullet, iv_step_two_bullet;
    private TextView tv_step_two, tv_step_three, tv_account_info;

    private ImageView dialog_decline_button;
    private TextView btn_gender;
    private FrameLayout fl_man, fl_woman, fl_couple, fl_trans_male, fl_trans_female,fl_neutral;
    private GifTextView gif_man, gif_woman, gif_couple, gif_trans_male, gif_trans_female,gif_neutral;
    private ImageView iv_man, iv_woman, iv_couple, iv_trans_male, iv_trans_female,iv_neutral,
            iv_man_border, iv_woman_border, iv_couple_border, iv_trans_male_border, iv_trans_female_border,iv_couple_neutral_border;
    private TextView tv_man, tv_woman, tv_couple, tv_trans_male, tv_trans_female, tv_intent_heading,tv_intent_title,tv_neutral;
    private String type_you_are = "",  select_type = "";
    private int you_are_selectedId = 0;

    // variable to track event time
    private long mLastClickTime = 0;

    private LinearLayout ly_no_network, ly_edit_profile;
    private TextView btn_try_again, tv_intent;
    private RelativeLayout rl_intent;
    private static final String ACC_BACK = "enable_back";
    private Progress progress;

    private String selected_looking_for = "", selected_looking_id = "";

    public EditBasicInfoFragment() {
    }

    public static EditBasicInfoFragment newInstance(String enable_back) {
        Bundle args = new Bundle();
        EditBasicInfoFragment fragment = new EditBasicInfoFragment();
        fragment.setArguments(args);
        args.putString(ACC_BACK, enable_back);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iv_back = ((EditProfileActivity) mContext).findViewById(R.id.iv_back);

        // Getting Back management
        if (getArguments() != null) {
            if ((getArguments().getString(ACC_BACK) != null)) {
                String manage_back = getArguments().getString(ACC_BACK);

                assert manage_back != null;
                if (iv_back != null) {
                    if (manage_back.equals("enable_back")) {
                        iv_back.setVisibility(View.VISIBLE);
                        iv_back.setOnClickListener(this);
                    } else {
                        iv_back.setVisibility(View.GONE);
                    }
                }
            } else {
                iv_back.setVisibility(View.GONE);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_basic_info, container, false);
        init(view);

        progress = new Progress(mContext);
        session = new Session(mContext);
        registrationInfo = session.getRegistration();

        // Set Basic Info Fragment Active
        setBasicInfoFragmentActive();

        // set Basic Info Data
        setBasicInfoData();

        // Click Listeners
        btn_skip.setOnClickListener(this);
        btn_edit_basic_info.setOnClickListener(this);
        ly_date_picker.setOnClickListener(this);
        ly_edit_gender.setOnClickListener(this);
        rl_intent.setOnClickListener(this);

        return view;
    }

    private void init(View view) {
        ed_edit_name = view.findViewById(R.id.ed_edit_name);
        tv_edit_dob = view.findViewById(R.id.tv_edit_dob);
        ed_edit_email = view.findViewById(R.id.ed_edit_email);
        ed_edit_gender = view.findViewById(R.id.ed_edit_gender);

        btn_skip = view.findViewById(R.id.btn_skip);
        btn_edit_basic_info = view.findViewById(R.id.btn_edit_basic_info);

        ly_date_picker = view.findViewById(R.id.ly_date_picker);
        ly_edit_gender = view.findViewById(R.id.ly_edit_gender);

        iv_back = ((EditProfileActivity) mContext).findViewById(R.id.iv_back);
        iv_step_one = ((EditProfileActivity) mContext).findViewById(R.id.iv_step_one);
        iv_step_one_bullet = ((EditProfileActivity) mContext).findViewById(R.id.iv_step_one_bullet);
        iv_step_two_bullet = ((EditProfileActivity) mContext).findViewById(R.id.iv_step_two_bullet);
        iv_step_two = ((EditProfileActivity) mContext).findViewById(R.id.iv_step_two);
        tv_step_two = ((EditProfileActivity) mContext).findViewById(R.id.tv_step_two);
        iv_step_three = ((EditProfileActivity) mContext).findViewById(R.id.iv_step_three);
        tv_step_three = ((EditProfileActivity) mContext).findViewById(R.id.tv_step_three);
        tv_account_info = ((EditProfileActivity) mContext).findViewById(R.id.tv_basic_info);

        ly_edit_profile = ((EditProfileActivity) mContext).findViewById(R.id.ly_edit_profile);
        ly_no_network = ((EditProfileActivity) mContext).findViewById(R.id.ly_no_network);
        btn_try_again = ((EditProfileActivity) mContext).findViewById(R.id.btn_try_again);

        rl_intent = view.findViewById(R.id.rl_intent);
        tv_intent = view.findViewById(R.id.tv_intent);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    // Set Basic Info Fragment Active
    private void setBasicInfoFragmentActive() {
        iv_step_one.setVisibility(View.GONE);
        iv_step_one_bullet.setVisibility(View.VISIBLE);
        iv_step_two.setVisibility(View.GONE);
        tv_step_two.setVisibility(View.VISIBLE);
        iv_step_two_bullet.setVisibility(View.GONE);
        iv_step_three.setVisibility(View.GONE);
        tv_step_three.setVisibility(View.VISIBLE);

        Utils.setTypeface(tv_account_info, mContext, R.font.lato_bold);
    }

    // set Basic Info Data from session
    public void setBasicInfoData() {
        registrationInfo = session.getRegistration();
        if (registrationInfo != null) {
            ed_edit_name.setText(registrationInfo.userDetail.full_name);

            // Changing date format from YYYY-MM-DD to DD-MM-YYYY
            tv_edit_dob.setText(Utils.dateInMDYFormat(registrationInfo.userDetail.date_of_birth));

            ed_edit_email.setText(registrationInfo.userDetail.email);

            String[] lookingArray = registrationInfo.userDetail.looking_for.split(",");
            StringBuilder lookingBuilder = new StringBuilder();
            for (String s : lookingArray) {
                if (s.length() != 0) {
                    if (s.equals("man")) {
                        lookingBuilder.append("Man").append(", ");
                        getManIntentString();
                    }

                    if (s.equals("woman")) {
                        lookingBuilder.append("Woman").append(", ");
                        getWomanIntentString();
                    }

                    if (s.equals("couple")) {
                        lookingBuilder.append("Couple").append(", ");
                        getCoupleIntentString();
                    }

                    if (s.equals("transgender_male")) {
                        lookingBuilder.append("Transgender Male").append(", ");
                        getTransMaleIntentString();

                    }

                    if (s.equals("transgender_female")) {
                        lookingBuilder.append("Transgender Female").append(", ");
                        getTransFemaleIntentString();
                    }

                    if (s.equals("neutral")){
                        lookingBuilder.append("Non Binary").append(", ");
                        getNeutralIntent();
                    }
                }
            }
            String withoutLastComma = lookingBuilder.substring(0, lookingBuilder.length() - ", ".length());
            tv_intent.setText(withoutLastComma);

            switch (registrationInfo.userDetail.gender) {
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
                    gender="Non Birnary";
                    you_are_selectedId=6;
                    break;
            }

            ed_edit_gender.setText(gender);
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
            case R.id.ly_date_picker:  // Date picker for select birth date
                openDatePicker();
                break;

            case R.id.btn_skip:        // Click of Skip button
                if (!isDataUpdated()) {  // Data updated and clicked skip
                    openDiscardChangesAlertDialog();
                } else {                 // Data not updated and clicked skip
                    ((EditProfileActivity) mContext).replaceFragment(EditOtherInfoFragment.newInstance(), true, R.id.edit_fragment_place);
                }
                break;

            case R.id.btn_edit_basic_info:
                String name = ed_edit_name.getText().toString().trim();
                String dob = Utils.dateInYMDFormat(tv_edit_dob.getText().toString().trim());
                String email = ed_edit_email.getText().toString().trim();

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

                if (!isDataUpdated()) {
                    if (isValid()) {   // Call Update Basic Info Api
                        String looking_for = getLookingForString();
                        callUpdateBasicInfoApi(name, dob, email, ed_gender, looking_for);
                    }
                } else {
                    ((EditProfileActivity) mContext).replaceFragment(EditOtherInfoFragment.newInstance(), true, R.id.edit_fragment_place);
                }
                break;

            case R.id.ly_edit_gender:   // Select you _are
                select_type = "you_are_a";
                openGenderDialog();
                break;

            case R.id.rl_intent:   // Select intent
                select_type = "looking_for";
                openGenderDialog();
                break;

            case R.id.dialog_decline_button:   // Gender selection dialog decline button
                type_you_are = "";
                selected_looking_for = "";
                selected_looking_id = "";

                String looking_gender = tv_intent.getText().toString().trim();

                if (looking_gender.contains("Man")) {
                    getManIntentString();
                }

                if (looking_gender.contains("Woman")) {
                    getWomanIntentString();
                }

                if (looking_gender.contains("Couple")) {
                    getCoupleIntentString();
                }

                if (looking_gender.contains("Transgender Male")) {
                    getTransMaleIntentString();
                }

                if (looking_gender.contains("Transgender Female")) {
                    getTransFemaleIntentString();
                }if (looking_gender.contains("Neutral")){
                    getNeutralIntent();
            }
                genderDialog.dismiss();
                break;

            case R.id.iv_back:  // Back Icon Press
                ((EditProfileActivity) mContext).onBackPressed();
                break;

            ////////////////////////////////////////////////////////////////////////////////

            case R.id.fl_man: {  // Select Man

                if (select_type.equals("you_are_a")) {
                    type_you_are = "man";
                    setInactiveGender();
                    iv_man.setVisibility(View.GONE);
                    gif_man.setVisibility(View.VISIBLE);
                    iv_man_border.setVisibility(View.VISIBLE);
                    tv_man.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                }

                if (select_type.equals("looking_for")) {
                    if (iv_man.getVisibility() == View.VISIBLE) {
                        iv_man.setVisibility(View.GONE);
                        gif_man.setVisibility(View.VISIBLE);
                        iv_man_border.setVisibility(View.VISIBLE);
                        tv_man.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));

                        getManIntentString();

                    } else {
                        iv_man.setVisibility(View.VISIBLE);
                        gif_man.setVisibility(View.GONE);
                        iv_man_border.setVisibility(View.GONE);
                        tv_man.setTextColor(ContextCompat.getColor(mContext,R.color.inactive_text_color));

                        getManIntentDeselected();
                    }
                }
            }
            break;

            case R.id.fl_woman: {     // Select Woman
                if (select_type.equals("you_are_a")) {
                    type_you_are = "woman";
                    setInactiveGender();
                    iv_woman.setVisibility(View.GONE);
                    gif_woman.setVisibility(View.VISIBLE);
                    iv_woman_border.setVisibility(View.VISIBLE);
                    tv_woman.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                }

                if (select_type.equals("looking_for")) {
                    if (iv_woman.getVisibility() == View.VISIBLE) {
                        iv_woman.setVisibility(View.GONE);
                        gif_woman.setVisibility(View.VISIBLE);
                        iv_woman_border.setVisibility(View.VISIBLE);
                        tv_woman.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));

                        getWomanIntentString();

                    } else {
                        iv_woman.setVisibility(View.VISIBLE);
                        gif_woman.setVisibility(View.GONE);
                        iv_woman_border.setVisibility(View.GONE);
                        tv_woman.setTextColor(ContextCompat.getColor(mContext,R.color.inactive_text_color));

                        getWomanIntentDeselected();
                    }
                }
            }
            break;

            case R.id.fl_trans_male: {       // Select Transgender Male
                if (select_type.equals("you_are_a")) {
                    type_you_are = "transgender male";
                    setInactiveGender();
                    iv_trans_male.setVisibility(View.GONE);
                    gif_trans_male.setVisibility(View.VISIBLE);
                    iv_trans_male_border.setVisibility(View.VISIBLE);
                    tv_trans_male.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                }

                if (select_type.equals("looking_for")) {
                    if (iv_trans_male.getVisibility() == View.VISIBLE) {
                        iv_trans_male.setVisibility(View.GONE);
                        gif_trans_male.setVisibility(View.VISIBLE);
                        iv_trans_male_border.setVisibility(View.VISIBLE);
                        tv_trans_male.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));

                        getTransMaleIntentString();

                    } else {
                        iv_trans_male.setVisibility(View.VISIBLE);
                        gif_trans_male.setVisibility(View.GONE);
                        iv_trans_male_border.setVisibility(View.GONE);
                        tv_trans_male.setTextColor(ContextCompat.getColor(mContext,R.color.inactive_text_color));

                        getTransMaleDeselected();
                    }
                }
            }
            break;

            case R.id.fl_trans_female: {       // Select Transgender Female
                if (select_type.equals("you_are_a")) {
                    type_you_are = "transgender female";
                    setInactiveGender();
                    iv_trans_female.setVisibility(View.GONE);
                    gif_trans_female.setVisibility(View.VISIBLE);
                    iv_trans_female_border.setVisibility(View.VISIBLE);
                    tv_trans_female.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                }

                if (select_type.equals("looking_for")) {
                    if (iv_trans_female.getVisibility() == View.VISIBLE) {
                        iv_trans_female.setVisibility(View.GONE);
                        gif_trans_female.setVisibility(View.VISIBLE);
                        iv_trans_female_border.setVisibility(View.VISIBLE);
                        tv_trans_female.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));

                        getTransFemaleIntentString();

                    } else {
                        iv_trans_female.setVisibility(View.VISIBLE);
                        gif_trans_female.setVisibility(View.GONE);
                        iv_trans_female_border.setVisibility(View.GONE);
                        tv_trans_female.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));

                        getTransFemaleDeselected();
                    }
                }
            }
            break;

            case R.id.fl_couple: {     // Select Couple
                if (select_type.equals("you_are_a")) {
                    type_you_are = "couple";
                    setInactiveGender();
                    iv_couple.setVisibility(View.GONE);
                    gif_couple.setVisibility(View.VISIBLE);
                    iv_couple_border.setVisibility(View.VISIBLE);
                    tv_couple.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                }

                if (select_type.equals("looking_for")) {

                    if (iv_couple.getVisibility() == View.VISIBLE) {
                        iv_couple.setVisibility(View.GONE);
                        gif_couple.setVisibility(View.VISIBLE);
                        iv_couple_border.setVisibility(View.VISIBLE);
                        tv_couple.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));

                        getCoupleIntentString();

                    } else {
                        iv_couple.setVisibility(View.VISIBLE);
                        gif_couple.setVisibility(View.GONE);
                        iv_couple_border.setVisibility(View.GONE);
                        tv_couple.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));

                        getCoupleIntentDeselected();
                    }
                }
            }
            break;


            case R.id.fl_neutral:

                if (select_type.equals("you_are_a")) {
                    type_you_are = "neutral";
                    setInactiveGender();
                    iv_neutral.setVisibility(View.GONE);
                    gif_neutral.setVisibility(View.VISIBLE);
                    iv_couple_neutral_border.setVisibility(View.VISIBLE);
                    tv_neutral.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                }

                if (select_type.equals("looking_for")) {

                    if (iv_neutral.getVisibility() == View.VISIBLE) {
                        iv_neutral.setVisibility(View.GONE);
                        gif_neutral.setVisibility(View.VISIBLE);
                        iv_couple_neutral_border.setVisibility(View.VISIBLE);
                        tv_neutral.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));

                        getNeutralIntent();

                    } else {
                        iv_neutral.setVisibility(View.VISIBLE);
                        gif_neutral.setVisibility(View.GONE);
                        iv_couple_neutral_border.setVisibility(View.GONE);
                        tv_neutral.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));

                        getNeutralDeselected();
                    }
                }



        }
    }

    private String getLookingForString() {
        String looking_gender = tv_intent.getText().toString().trim();
        setInactiveGender();
        if (looking_gender.contains("Man")) {
            getManIntentString();
        }

        if (looking_gender.contains("Woman")) {
            getWomanIntentString();
        }

        if (looking_gender.contains("Couple")) {
            getCoupleIntentString();
        }

        if (looking_gender.contains("Transgender Female")) {
            getTransFemaleIntentString();
        }

        if (looking_gender.contains("Transgender Male")) {
            getTransMaleIntentString();


        } if (looking_gender.contains("Neutral")) {
            getNeutralIntent();
        }

        return selected_looking_id;
    }

    // Get Selected Couple Intent in String for display in textview and get id for storing in database
    private void getNeutralIntent() {
        if (selected_looking_for.length() == 0) {
            selected_looking_for = "Neutral" + selected_looking_for;
            selected_looking_id = "neutral" + selected_looking_id;
        } else if (!selected_looking_for.contains("Neutral")) {
            selected_looking_for = selected_looking_for + ", " + "Neutral";
            selected_looking_id = selected_looking_id + "," + "neutral";
        }
    }

    private boolean isValid() {
        Validation v = new Validation();

        if (!v.isEditNull(ed_edit_name)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_name_null));
            return false;
        } else if (!v.isNameValid(ed_edit_name)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_name_invalid));
            return false;
        }else if (!v.isEditNull(ed_edit_email)) {
            CustomToast.getInstance(mContext).showToast(mContext, "Please enter Email Address");
            return false;
        }
        return true;
    }

    // Api call to update basic info
    private void callUpdateBasicInfoApi(final String name, final String dob, final String email, final String ed_gender, final String ed_intent) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            final Map<String, String> map = new HashMap<>();
            map.put("name", name);
            map.put("email", email);
            map.put("birthday", dob);
            map.put("gender", ed_gender);
            map.put("looking", ed_intent);

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
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
                            session.createRegistration(registrationInfo);

                            // Write data to user table in firebase
                            addUserFirebaseDatabase();

                            ((EditProfileActivity) mContext).replaceFragment(EditOtherInfoFragment.newInstance(), true, R.id.edit_fragment_place);

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
            api.callApi("user/updateBasicInfo", Request.Method.POST, map);
        } else {
            //     CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.alert_no_network));

            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            ly_edit_profile.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callUpdateBasicInfoApi(name, dob, email, ed_gender, ed_intent);
                }
            });
        }
    }

    // Alert dialog to ask discard the changes
    private void openDiscardChangesAlertDialog() {
        final Dialog discardDialog = new Dialog(mContext);
        discardDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        discardDialog.setContentView(R.layout.dialog_custom_alert);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(discardDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        discardDialog.getWindow().setAttributes(lWindowParams);

        ImageView dialog_decline_button = discardDialog.findViewById(R.id.dialog_decline_button);
        TextView btn_cancel = discardDialog.findViewById(R.id.btn_cancel);
        TextView btn_alert = discardDialog.findViewById(R.id.btn_alert);
        TextView alert_message = discardDialog.findViewById(R.id.alert_message);

        alert_message.setText(getString(R.string.discard_changes));

        dialog_decline_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                discardDialog.dismiss();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                selected_looking_for = "";
                selected_looking_id = "";
                discardDialog.dismiss();
            }
        });

        btn_alert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                selected_looking_for = "";
                selected_looking_id = "";
                setBasicInfoData();
                ((EditProfileActivity) mContext).replaceFragment(EditOtherInfoFragment.newInstance(), true, R.id.edit_fragment_place);
                discardDialog.dismiss();
            }
        });

        discardDialog.getWindow().setGravity(Gravity.CENTER);
        discardDialog.show();
    }

    boolean isDataUpdated() {
        String name = ed_edit_name.getText().toString().trim();
        String dob = tv_edit_dob.getText().toString().trim();
        String email = ed_edit_email.getText().toString().trim();
        String gen = ed_edit_gender.getText().toString().trim();

        if (!name.equalsIgnoreCase(registrationInfo.userDetail.full_name)) {
            return false;
        } else if (!dob.equalsIgnoreCase(Utils.dateInMDYFormat(registrationInfo.userDetail.date_of_birth))) {
            return false;
        } else if (!email.equalsIgnoreCase(registrationInfo.userDetail.email)) {
            return false;
        } else if (!gen.equalsIgnoreCase(gender)) {
            return false;
        } else if (!selected_looking_id.equalsIgnoreCase(registrationInfo.userDetail.looking_for)) {
            return false;
        } else {
            return true;
        }
    }

    // Date picker to select birth date
    private void openDatePicker() {
        if (tv_edit_dob.getText().toString().trim().equals("")) {  //Set date for first time
            Calendar now = Calendar.getInstance();
            int mYear = 1900;   // Open Calendar from 1900
            int mMonth = 0;
            int mDay = 1;
            now.set(mYear, mMonth, mDay);

            setDateOnDatePickerDialog(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        } else {  // Set already filled date
            String[] s = tv_edit_dob.getText().toString().split("-");
            int mDay = Integer.parseInt(s[1]);
            int mMonth = Integer.parseInt(s[0]);
            int mYear = Integer.parseInt(s[2]);
            setDateOnDatePickerDialog(mYear, mMonth, mDay);
        }
    }

    // Set date on Date picker
    public void setDateOnDatePickerDialog(int year, int month, int day) {
        date_picker = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                String day, month;
                day = (dayOfMonth < 10) ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
                monthOfYear += 1;
                month = (monthOfYear < 10) ? "0" + monthOfYear : String.valueOf(monthOfYear);

                date = month + "-" + day + "-" + year;
                tv_edit_dob.setText(date);
            }
        }, year, month - 1, day);

        // Setting Max Date
        Calendar cal = Calendar.getInstance();   // Set Max Date
        int mYear = cal.get(Calendar.YEAR) - 18;
        int mMonth = cal.get(Calendar.MONTH);
        int mDay = cal.get(Calendar.DAY_OF_MONTH);
        cal.set(mYear, mMonth, mDay);
        date_picker.setMaxDate(cal);

        // Setting Min Date
        Calendar c = Calendar.getInstance();    // Set Min Date
        int mYr = 1900;
        int mMon = 0;
        int mDy = 1;
        c.set(mYr, mMon, mDy);
        date_picker.setMinDate(c);

        date_picker.show(((EditProfileActivity) mContext).getFragmentManager(), "");
        date_picker.setAccentColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        date_picker.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                date_picker.dismiss();
            }
        });
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

        if (select_type.equals("you_are_a")) {
            tv_intent_heading.setText(getString(R.string.you_are_a));
            tv_intent_title.setVisibility(View.GONE);
        } else if (select_type.equals("looking_for")) {
            tv_intent_heading.setText(getString(R.string.looking_for));
            tv_intent_title.setText("Select all the apply");
        }

        // Setting inactive all gender
        setInactiveGender();

        // If gender already selected, show already selected
        genderAlreadySelected();

        dialog_decline_button.setOnClickListener(this);

        btn_gender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                if (select_type.equals("you_are_a")) {
                    switch (type_you_are) {
                        case "man":
                            ed_edit_gender.setText(getString(R.string.man));
                            genderDialog.dismiss();
                            you_are_selectedId = 1;
                            break;

                        case "woman":
                            ed_edit_gender.setText(getString(R.string.woman));
                            genderDialog.dismiss();
                            you_are_selectedId = 2;
                            break;

                        case "couple":
                            ed_edit_gender.setText(getString(R.string.couple));
                            genderDialog.dismiss();
                            you_are_selectedId = 3;
                            break;

                        case "transgender male":
                            ed_edit_gender.setText(getString(R.string.trans_male));
                            genderDialog.dismiss();
                            you_are_selectedId = 4;
                            break;

                        case "transgender female":
                            ed_edit_gender.setText(getString(R.string.trans_female));
                            genderDialog.dismiss();
                            you_are_selectedId = 5;
                            break;

                        case "neutral":
                            ed_edit_gender.setText(getString(R.string.neutral));
                            genderDialog.dismiss();
                            you_are_selectedId = 6;
                            break;


                        default:
                            genderDialog.dismiss();
                            break;
                    }
                } else if (select_type.equals("looking_for")) {

                    if (selected_looking_for.trim().length() == 0) {
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.lookin_for_null));
                    } else {
                        tv_intent.setText(selected_looking_for);
                        genderDialog.dismiss();
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
        dialog_decline_button = genderDialog.findViewById(R.id.dialog_decline_button);
        fl_man = genderDialog.findViewById(R.id.fl_man);
        fl_woman = genderDialog.findViewById(R.id.fl_woman);
        fl_couple = genderDialog.findViewById(R.id.fl_couple);
        fl_trans_male = genderDialog.findViewById(R.id.fl_trans_male);
        fl_trans_female = genderDialog.findViewById(R.id.fl_trans_female);
        fl_neutral = genderDialog.findViewById(R.id.fl_neutral);

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
        // Setting gender
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
                    iv_neutral.setVisibility(View.VISIBLE);
                    tv_neutral.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                    break;

            }
        }

        else {
            String looking_gender = tv_intent.getText().toString().trim();
            setInactiveGender();
            if (looking_gender.contains("Man")) {
                iv_man.setVisibility(View.GONE);
                gif_man.setVisibility(View.VISIBLE);
                iv_man_border.setVisibility(View.VISIBLE);
                tv_man.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));

                getManIntentString();
            }

            if (looking_gender.contains("Woman")) {
                iv_woman.setVisibility(View.GONE);
                gif_woman.setVisibility(View.VISIBLE);
                iv_woman_border.setVisibility(View.VISIBLE);
                tv_woman.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));

                getWomanIntentString();
            }

            if (looking_gender.contains("Couple")) {
                iv_couple.setVisibility(View.GONE);
                gif_couple.setVisibility(View.VISIBLE);
                iv_couple_border.setVisibility(View.VISIBLE);
                tv_couple.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));

                getCoupleIntentString();
            }

            if (looking_gender.contains("Transgender Male")) {
                iv_trans_male.setVisibility(View.GONE);
                gif_trans_male.setVisibility(View.VISIBLE);
                iv_trans_male_border.setVisibility(View.VISIBLE);
                tv_trans_male.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));

                getTransMaleIntentString();
            }

            if (looking_gender.contains("Transgender Female")) {
                iv_trans_female.setVisibility(View.GONE);
                gif_trans_female.setVisibility(View.VISIBLE);
                iv_trans_female_border.setVisibility(View.VISIBLE);
                tv_trans_female.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));

                getTransFemaleIntentString();
            }

            if (looking_gender.contains("Neutral")) {
                iv_neutral.setVisibility(View.GONE);
                gif_neutral.setVisibility(View.VISIBLE);
                iv_couple_neutral_border.setVisibility(View.VISIBLE);
                tv_neutral.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));

                getNeutralIntent();
            }
        }


    }

    // Setting gender
    private void setGender() {
        if (select_type.equals("you_are_a")) {
            String you_are = ed_edit_gender.getText().toString().trim().toLowerCase();
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
        if(genderDialog != null){
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

            tv_man.setTextColor(ContextCompat.getColor(mContext,R.color.inactive_text_color));
            tv_woman.setTextColor(ContextCompat.getColor(mContext,R.color.inactive_text_color));
            tv_couple.setTextColor(ContextCompat.getColor(mContext,R.color.inactive_text_color));
            tv_trans_male.setTextColor(ContextCompat.getColor(mContext,R.color.inactive_text_color));
            tv_trans_female.setTextColor(ContextCompat.getColor(mContext,R.color.inactive_text_color));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

         setBasicInfoData();
    }

    // Write data to user table in firebase
    private void addUserFirebaseDatabase() {
        RegistrationInfo registrationInfo = session.getRegistration();
        String device_token = FirebaseInstanceId.getInstance().getToken();
        String name = registrationInfo.userDetail.full_name.toLowerCase();
        String uId = registrationInfo.userDetail.userId;

        String profilePic;
        if (registrationInfo.userDetail.images.size() > 0) {
            profilePic = registrationInfo.userDetail.images.get(0).image;
        } else {
            profilePic = registrationInfo.userDetail.defaultImg;
        }

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        FirebaseUserModel userModel = new FirebaseUserModel();
        userModel.firebaseToken = device_token;
        userModel.name = name;
        userModel.profilePic = profilePic;
        userModel.timeStamp = ServerValue.TIMESTAMP;
        userModel.uid = uId;
        userModel.isAdmin="0";
        userModel.isUserDeleted = 0;

        database.child(Constant.USER_TABLE).child(uId).setValue(userModel).
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });

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

    // Get Selected Man Intent in String for display in textview and get id for storing in database
    private void getManIntentString() {
        if (selected_looking_for.length() == 0) {
            selected_looking_for = "Man" + selected_looking_for;
            selected_looking_id = "man" + selected_looking_id;
        } else if (!selected_looking_for.contains("Man")) {
            selected_looking_for = "Man" + ", " + selected_looking_for;
            selected_looking_id = "man" + "," + selected_looking_id;
        }
    }

    // Get Selected Woman Intent in String for display in textview and get id for storing in database
    private void getWomanIntentString() {
        if (selected_looking_for.length() == 0) {
            selected_looking_for = "Woman" + selected_looking_for;
            selected_looking_id = "woman" + selected_looking_id;
        } else if (!selected_looking_for.contains("Woman")) {
            selected_looking_for = selected_looking_for + ", " + "Woman";
            selected_looking_id = selected_looking_id + "," + "woman";
        }
    }

    // Get Selected Couple Intent in String for display in textview and get id for storing in database
    private void getCoupleIntentString() {
        if (selected_looking_for.length() == 0) {
            selected_looking_for = "Couple" + selected_looking_for;
            selected_looking_id = "couple" + selected_looking_id;
        } else if (!selected_looking_for.contains("Couple")) {
            selected_looking_for = selected_looking_for + ", " + "Couple";
            selected_looking_id = selected_looking_id + "," + "couple";
        }
    }

    // Get Selected Trans Male Intent in String for display in textview and get id for storing in database
    private void getTransMaleIntentString() {
        if (selected_looking_for.length() == 0) {
            selected_looking_for = "Transgender Male" + selected_looking_for;
            selected_looking_id = "transgender_male" + selected_looking_id;
        } else if (!selected_looking_for.contains("Transgender Male")) {
            selected_looking_for = selected_looking_for + ", " + "Transgender Male";
            selected_looking_id = selected_looking_id + "," + "transgender_male";
        }
    }

    // Get Selected Trans Female Intent in String for display in textview and get id for storing in database
    private void getTransFemaleIntentString() {
        if (selected_looking_for.length() == 0) {
            selected_looking_for = "Transgender Female" + selected_looking_for;
            selected_looking_id = "transgender_female" + selected_looking_id;
        } else if (!selected_looking_for.contains("Transgender Female")) {
            selected_looking_for = selected_looking_for + ", " + "Transgender Female";
            selected_looking_id = selected_looking_id + "," + "transgender_female";
        }
    }

    // Get Man Intent deselected in dialog
    private void getManIntentDeselected() {
        if (selected_looking_for.length() == 1) {
            selected_looking_for = selected_looking_for.replace("Man", "");
            selected_looking_id = selected_looking_id.replace("man", "");
        } else {
            if (selected_looking_for.contains("Man,")) {
                selected_looking_for = selected_looking_for.replace("Man, ", "");
                selected_looking_id = selected_looking_id.replace("man,", "");
            }

            if (selected_looking_for.contains(", Man")) {
                selected_looking_for = selected_looking_for.replace(", Man", "");
                selected_looking_id = selected_looking_id.replace(",man", "");
            }

            if (selected_looking_for.contains("Man")) {
                selected_looking_for = selected_looking_for.replace("Man", "");
                selected_looking_id = selected_looking_id.replace("man", "");
            }
        }
    }

    // Get Woman Intent deselected in dialog
    private void getWomanIntentDeselected() {
        if (selected_looking_for.length() == 1) {
            selected_looking_for = selected_looking_for.replace("Woman", "");
            selected_looking_id = selected_looking_id.replace("woman", "");
        } else {
            if (selected_looking_for.contains("Woman,")) {
                selected_looking_for = selected_looking_for.replace("Woman, ", "");
                selected_looking_id = selected_looking_id.replace("woman,", "");
            } else if (selected_looking_for.contains(", Woman")) {
                selected_looking_for = selected_looking_for.replace(", Woman", "");
                selected_looking_id = selected_looking_id.replace(",woman", "");
            } else if (selected_looking_for.contains("Woman")) {
                selected_looking_for = selected_looking_for.replace("Woman", "");
                selected_looking_id = selected_looking_id.replace("woman", "");
            }
        }
    }

    // Get Couple Intent deselected in dialog
    private void getCoupleIntentDeselected() {
        if (selected_looking_for.length() == 1) {
            selected_looking_for = selected_looking_for.replace("Couple", "");
            selected_looking_id = selected_looking_id.replace("couple", "");
        } else {
            if (selected_looking_for.contains("Couple,")) {
                selected_looking_for = selected_looking_for.replace("Couple, ", "");
                selected_looking_id = selected_looking_id.replace("couple,", "");
            } else if (selected_looking_for.contains(", Couple")) {
                selected_looking_for = selected_looking_for.replace(", Couple", "");
                selected_looking_id = selected_looking_id.replace(",couple", "");
            } else if (selected_looking_for.contains("Couple")) {
                selected_looking_for = selected_looking_for.replace("Couple", "");
                selected_looking_id = selected_looking_id.replace("couple", "");
            }
        }
    }

    // Get Trans Male Intent deselected in dialog
    private void getTransMaleDeselected() {
        if (selected_looking_for.length() == 1) {
            selected_looking_for = selected_looking_for.replace("Transgender Male", "");
            selected_looking_id = selected_looking_id.replace("transgender_male", "");
        } else {
            if (selected_looking_for.contains("Transgender Male,")) {
                selected_looking_for = selected_looking_for.replace("Transgender Male, ", "");
                selected_looking_id = selected_looking_id.replace("transgender_male,", "");
            } else if (selected_looking_for.contains(", Transgender Male")) {
                selected_looking_for = selected_looking_for.replace(", Transgender Male", "");
                selected_looking_id = selected_looking_id.replace(",transgender_male", "");
            } else if (selected_looking_for.contains("Transgender Male")) {
                selected_looking_for = selected_looking_for.replace("Transgender Male", "");
                selected_looking_id = selected_looking_id.replace("transgender_male", "");
            }
        }
    }

    // Get Trans Female Intent deselected in dialog
    private void getTransFemaleDeselected() {
        if (selected_looking_for.length() == 1) {
            selected_looking_for = selected_looking_for.replace("Transgender Female", "");
            selected_looking_id = selected_looking_id.replace("transgender_female", "");
        } else {
            if (selected_looking_for.contains("Transgender Female, ")) {
                selected_looking_for = selected_looking_for.replace("Transgender Female, ", "");
                selected_looking_id = selected_looking_id.replace("transgender_female,", "");

            } else if (selected_looking_for.contains(", Transgender Female")) {
                selected_looking_for = selected_looking_for.replace(", Transgender Female", "");
                selected_looking_id = selected_looking_id.replace(",transgender_female", "");

            } else if (selected_looking_for.contains("Transgender Female")) {
                selected_looking_for = selected_looking_for.replace("Transgender Female", "");
                selected_looking_id = selected_looking_id.replace("transgender_female", "");
            }
        }
    }


    // Get Couple Intent deselected in dialog
    private void getNeutralDeselected() {
        if (selected_looking_for.length() == 1) {
            selected_looking_for = selected_looking_for.replace("Neutral", "");
            selected_looking_id = selected_looking_id.replace("neutral", "");
        } else {
            if (selected_looking_for.contains("Neutral,")) {
                selected_looking_for = selected_looking_for.replace("Neutral, ", "");
                selected_looking_id = selected_looking_id.replace("neutral,", "");
            } else if (selected_looking_for.contains(", Neutral")) {
                selected_looking_for = selected_looking_for.replace(", Neutral", "");
                selected_looking_id = selected_looking_id.replace(",neutral", "");
            } else if (selected_looking_for.contains("Neutral")) {
                selected_looking_for = selected_looking_for.replace("Neutral", "");
                selected_looking_id = selected_looking_id.replace("neutral", "");
            }
        }
    }



}
