package com.kinklink.modules.authentication.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kinklink.R;
import com.kinklink.helper.CustomToast;
import com.kinklink.modules.matches.activity.TermsConditionActivity;
import com.kinklink.session.Session;

import pl.droidsonroids.gif.GifTextView;

public class RegisterGenderActivity extends AppCompatActivity implements View.OnClickListener {
    private RelativeLayout rl_you_are_a, rl_looking_for;
    private Dialog genderDialog;
    private ImageView dialog_decline_button, iv_back;
    private TextView btn_reg_gender, btn_gender;
    private FrameLayout fl_man, fl_woman, fl_couple, fl_trans_male, fl_trans_female,fl_neutral;
    private GifTextView gif_man, gif_woman, gif_couple, gif_trans_male, gif_trans_female,gif_neutral;
    private ImageView iv_man, iv_woman, iv_couple, iv_trans_male, iv_trans_female,iv_neutral,
            iv_man_border, iv_woman_border, iv_couple_border, iv_trans_male_border, iv_trans_female_border,iv_couple_neutral_border;
    private TextView tv_you_are_a, tv_looking_for, tv_man, tv_woman, tv_couple, tv_trans_male, tv_trans_female, tv_intent_heading,tv_intent_title,tv_neutral,txtTermsCondition;
    private String type_you_are = "";
    private String select_type = "";

    private int you_are_selectedId = 0;
    private LinearLayout ly_register_gender;
    private Session session;

    // variable to track event time
    private long mLastClickTime = 0;
    private Dialog legalDialog;

    private String selected_looking_for = "", selected_looking_id = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register_gender);
        session = new Session(RegisterGenderActivity.this);
        init();

        // Dialog to agree Legal Agreement
        openLegalAgreementDialog();

        // Click Listeners
        rl_you_are_a.setOnClickListener(this);
        rl_looking_for.setOnClickListener(this);
        btn_reg_gender.setOnClickListener(this);
        iv_back.setOnClickListener(this);
    }

    private void init() {
        rl_you_are_a = findViewById(R.id.rl_you_are_a);
        rl_looking_for = findViewById(R.id.rl_looking_for);


        tv_you_are_a = findViewById(R.id.tv_you_are_a);
        tv_looking_for = findViewById(R.id.tv_looking_for);
        btn_reg_gender = findViewById(R.id.btn_reg_gender);

        iv_back = findViewById(R.id.iv_back);
        ly_register_gender = findViewById(R.id.ly_register_gender);
    }

    @Override
    public void onClick(View view) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (view.getId()) {
            case R.id.rl_you_are_a:   // Select you _are
                select_type = "you_are_a";
                openGenderDialog();
                break;

            case R.id.rl_looking_for:   // Select looking_for
                select_type = "looking_for";
                openGenderDialog();
                break;

            case R.id.dialog_decline_button:   // Gender selection dialog decline button
                type_you_are = "";
                selected_looking_for = "";
                selected_looking_id = "";

                String looking_gender = tv_looking_for.getText().toString().trim();

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

                } if (looking_gender.contains("Neutral")) {
                getNeutralIntent();
            }
                genderDialog.dismiss();
                break;

            case R.id.fl_man: {  // Select Man

                if (select_type.equals("you_are_a")) {
                    type_you_are = "man";
                    setInactiveGender();
                    iv_man.setVisibility(View.GONE);
                    gif_man.setVisibility(View.VISIBLE);
                    iv_man_border.setVisibility(View.VISIBLE);
                    tv_man.setTextColor(getResources().getColor(R.color.colorPrimary));
                }

                if (select_type.equals("looking_for")) {
                    if (iv_man.getVisibility() == View.VISIBLE) {
                        iv_man.setVisibility(View.GONE);
                        gif_man.setVisibility(View.VISIBLE);
                        iv_man_border.setVisibility(View.VISIBLE);
                        tv_man.setTextColor(getResources().getColor(R.color.colorPrimary));

                        getManIntentString();

                    } else {
                        iv_man.setVisibility(View.VISIBLE);
                        gif_man.setVisibility(View.GONE);
                        iv_man_border.setVisibility(View.GONE);
                        tv_man.setTextColor(getResources().getColor(R.color.inactive_text_color));

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
                    tv_woman.setTextColor(getResources().getColor(R.color.colorPrimary));
                }

                if (select_type.equals("looking_for")) {
                    if (iv_woman.getVisibility() == View.VISIBLE) {
                        iv_woman.setVisibility(View.GONE);
                        gif_woman.setVisibility(View.VISIBLE);
                        iv_woman_border.setVisibility(View.VISIBLE);
                        tv_woman.setTextColor(getResources().getColor(R.color.colorPrimary));

                        getWomanIntentString();

                    } else {
                        iv_woman.setVisibility(View.VISIBLE);
                        gif_woman.setVisibility(View.GONE);
                        iv_woman_border.setVisibility(View.GONE);
                        tv_woman.setTextColor(getResources().getColor(R.color.inactive_text_color));

                        getWomanIntentDeselected();
                    }
                }
            }
            break;

            case R.id.fl_trans_male: {   // Select Transgender Male
                if (select_type.equals("you_are_a")) {
                    type_you_are = "transgender male";
                    setInactiveGender();
                    iv_trans_male.setVisibility(View.GONE);
                    gif_trans_male.setVisibility(View.VISIBLE);
                    iv_trans_male_border.setVisibility(View.VISIBLE);
                    tv_trans_male.setTextColor(getResources().getColor(R.color.colorPrimary));
                }

                if (select_type.equals("looking_for")) {
                    if (iv_trans_male.getVisibility() == View.VISIBLE) {
                        iv_trans_male.setVisibility(View.GONE);
                        gif_trans_male.setVisibility(View.VISIBLE);
                        iv_trans_male_border.setVisibility(View.VISIBLE);
                        tv_trans_male.setTextColor(getResources().getColor(R.color.colorPrimary));

                        getTransMaleIntentString();

                    } else {
                        iv_trans_male.setVisibility(View.VISIBLE);
                        gif_trans_male.setVisibility(View.GONE);
                        iv_trans_male_border.setVisibility(View.GONE);
                        tv_trans_male.setTextColor(getResources().getColor(R.color.inactive_text_color));

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
                    tv_trans_female.setTextColor(getResources().getColor(R.color.colorPrimary));
                }

                if (select_type.equals("looking_for")) {
                    if (iv_trans_female.getVisibility() == View.VISIBLE) {
                        iv_trans_female.setVisibility(View.GONE);
                        gif_trans_female.setVisibility(View.VISIBLE);
                        iv_trans_female_border.setVisibility(View.VISIBLE);
                        tv_trans_female.setTextColor(getResources().getColor(R.color.colorPrimary));

                        getTransFemaleIntentString();

                    } else {
                        iv_trans_female.setVisibility(View.VISIBLE);
                        gif_trans_female.setVisibility(View.GONE);
                        iv_trans_female_border.setVisibility(View.GONE);
                        tv_trans_female.setTextColor(getResources().getColor(R.color.field_text_color));

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
                    tv_couple.setTextColor(getResources().getColor(R.color.colorPrimary));
                }

                if (select_type.equals("looking_for")) {
                    if (iv_couple.getVisibility() == View.VISIBLE) {
                        iv_couple.setVisibility(View.GONE);
                        gif_couple.setVisibility(View.VISIBLE);
                        iv_couple_border.setVisibility(View.VISIBLE);
                        tv_couple.setTextColor(getResources().getColor(R.color.colorPrimary));

                        getCoupleIntentString();

                    } else {
                        iv_couple.setVisibility(View.VISIBLE);
                        gif_couple.setVisibility(View.GONE);
                        iv_couple_border.setVisibility(View.GONE);
                        tv_couple.setTextColor(getResources().getColor(R.color.field_text_color));

                        getCoupleIntentDeselected();
                    }
                }
            }
            break;

            case R.id.fl_neutral:{
                if (select_type.equals("you_are_a")){
                    type_you_are="neutral";
                    setInactiveGender();
                    iv_neutral.setVisibility(View.GONE);
                    gif_neutral.setVisibility(View.VISIBLE);
                    iv_couple_neutral_border.setVisibility(View.VISIBLE);
                    tv_neutral.setTextColor(getResources().getColor(R.color.colorPrimary));
                } if (select_type.equals("looking_for")) {
                    if (iv_neutral.getVisibility() == View.VISIBLE) {
                        iv_neutral.setVisibility(View.GONE);
                        gif_neutral.setVisibility(View.VISIBLE);
                        iv_couple_neutral_border.setVisibility(View.VISIBLE);
                        tv_neutral.setTextColor(getResources().getColor(R.color.colorPrimary));

                        getNeutralIntent();

                    } else {
                        iv_neutral.setVisibility(View.VISIBLE);
                        gif_neutral.setVisibility(View.GONE);
                        iv_couple_neutral_border.setVisibility(View.GONE);
                        tv_neutral.setTextColor(getResources().getColor(R.color.field_text_color));

                        getNeutralDeselected();
                    }
                }
            }

            break;

            case R.id.btn_reg_gender: {    // Register gender button
                if (isValid()) {
                    Intent intent = new Intent(RegisterGenderActivity.this, RegistrationActivity.class);
                    intent.putExtra("Gender", tv_you_are_a.getText().toString().trim());
                    String looking_for = getLookingForString();
                    if(looking_for.contains("Neutral") || looking_for.contains("Non Binary")){
                        looking_for.replace("Neutral","neutral");
                        looking_for.replace("Non Binary","neutral");
                    }
                    intent.putExtra("Looking_for", looking_for);
                    startActivity(intent);
                }
                break;
            }

            case R.id.iv_back:    // Back button
                onBackPressed();
                break;
        }
    }

    // Validations for select gender
    private boolean isValid() {
        if (tv_you_are_a.getText().toString().trim().equals(getString(R.string.you_are_a))) {
            CustomToast.getInstance(this).showToast(this, getString(R.string.you_are_a_null));
            return false;
        }

        if (tv_looking_for.getText().toString().trim().equals(getString(R.string.looking_for))) {
            CustomToast.getInstance(this).showToast(this, getString(R.string.lookin_for_null));
            return false;
        }

        return true;
    }

    // Dialog to agree Legal Agreement
    private void openLegalAgreementDialog() {
        legalDialog = new Dialog(this);
        legalDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        legalDialog.setCancelable(false);
        legalDialog.setContentView(R.layout.dialog_legal_account);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(legalDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        legalDialog.getWindow().setAttributes(lWindowParams);

        TextView btn_i_agree = legalDialog.findViewById(R.id.btn_i_agree);
        TextView txtTermsCondition = legalDialog.findViewById(R.id.txtTermsCondition);
        SpannableString content = new SpannableString(getString(R.string.terms_and_conditions));
        content.setSpan(new UnderlineSpan(), 0, getString(R.string.terms_and_conditions).length(), 0);
        txtTermsCondition.setText(content);
        txtTermsCondition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(RegisterGenderActivity.this,TermsConditionActivity.class);
                startActivity(intent);
            }
        });

        TextView alert_message = legalDialog.findViewById(R.id.alert_message);
        alert_message.setText(getString(R.string.legal_message));
        alert_message.setMovementMethod(new ScrollingMovementMethod());
        ImageView dialog_decline_button = legalDialog.findViewById(R.id.dialog_decline_button);

        dialog_decline_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                startActivity(new Intent(RegisterGenderActivity.this, LoginActivity.class));
                finishAffinity();

                session.logout();
                legalDialog.dismiss();
            }
        });

        btn_i_agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                ly_register_gender.setEnabled(true);
                legalDialog.dismiss();
            }
        });

        legalDialog.getWindow().setGravity(Gravity.CENTER);
        legalDialog.show();
    }

    public void setUnderLineText(TextView tv, String textToUnderLine) {
        String tvt = tv.getText().toString();
        int ofe = tvt.indexOf(textToUnderLine, 0);

        UnderlineSpan underlineSpan = new UnderlineSpan();
        SpannableString wordToSpan = new SpannableString(tv.getText());
        for (int ofs = 0; ofs < tvt.length() && ofe != -1; ofs = ofe + 1) {
            ofe = tvt.indexOf(textToUnderLine, ofs);
            if (ofe == -1)
                break;
            else {
                wordToSpan.setSpan(underlineSpan, ofe, ofe + textToUnderLine.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv.setText(wordToSpan, TextView.BufferType.SPANNABLE);
            }
        }
    }


    // Dialog to select gender
    private void openGenderDialog() {
        genderDialog = new Dialog(this);
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
            tv_intent_heading.setText(R.string.looking_for);
            tv_intent_title.setText(R.string.select_all_apply);
        }

        // Setting inactive all gender
        setInactiveGender();

        // If gender already selected, show already selected
        genderAlreadySelected();

        dialog_decline_button.setOnClickListener(this);

        btn_gender.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
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
                            tv_you_are_a.setText(getString(R.string.man));
                            genderDialog.dismiss();
                            you_are_selectedId = 1;
                            break;

                        case "woman":
                            tv_you_are_a.setText(getString(R.string.woman));
                            genderDialog.dismiss();
                            you_are_selectedId = 2;
                            break;

                        case "couple":
                            tv_you_are_a.setText(getString(R.string.couple));
                            genderDialog.dismiss();
                            you_are_selectedId = 3;
                            break;

                        case "transgender male":
                            tv_you_are_a.setText(getString(R.string.trans_male));
                            genderDialog.dismiss();
                            you_are_selectedId = 4;
                            break;

                        case "transgender female":
                            tv_you_are_a.setText(getString(R.string.trans_female));
                            genderDialog.dismiss();
                            you_are_selectedId = 5;
                            break;

                        case "neutral":
                            tv_you_are_a.setText(R.string.neutral);
                            genderDialog.dismiss();
                            you_are_selectedId = 6;
                            break;

                        default:
                            you_are_selectedId = 0;
                            CustomToast.getInstance(RegisterGenderActivity.this).showToast(RegisterGenderActivity.this, getString(R.string.you_are_a_null));
                            break;
                    }
                } else if (select_type.equals("looking_for")) {
                    if (selected_looking_for.trim().length() == 0) {
                        CustomToast.getInstance(RegisterGenderActivity.this).showToast(RegisterGenderActivity.this, getString(R.string.lookin_for_null));
                    } else {
                        tv_looking_for.setText(selected_looking_for);
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
        iv_neutral = genderDialog.findViewById(R.id.iv_neutral);

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
                    tv_man.setTextColor(getResources().getColor(R.color.colorPrimary));
                    break;

                case 2:
                    setInactiveGender();
                    iv_woman.setVisibility(View.GONE);
                    gif_woman.setVisibility(View.VISIBLE);
                    iv_woman_border.setVisibility(View.VISIBLE);
                    tv_woman.setTextColor(getResources().getColor(R.color.colorPrimary));
                    break;

                case 3:
                    setInactiveGender();
                    iv_couple.setVisibility(View.GONE);
                    gif_couple.setVisibility(View.VISIBLE);
                    iv_couple_border.setVisibility(View.VISIBLE);
                    tv_couple.setTextColor(getResources().getColor(R.color.colorPrimary));
                    break;

                case 4:
                    setInactiveGender();
                    iv_trans_male.setVisibility(View.GONE);
                    gif_trans_male.setVisibility(View.VISIBLE);
                    iv_trans_male_border.setVisibility(View.VISIBLE);
                    tv_trans_male.setTextColor(getResources().getColor(R.color.colorPrimary));
                    break;

                case 5:
                    setInactiveGender();
                    iv_trans_female.setVisibility(View.GONE);
                    gif_trans_female.setVisibility(View.VISIBLE);
                    iv_trans_female_border.setVisibility(View.VISIBLE);
                    tv_trans_female.setTextColor(getResources().getColor(R.color.colorPrimary));
                    break;

                case 6:
                    setInactiveGender();
                    iv_neutral.setVisibility(View.GONE);
                    gif_neutral.setVisibility(View.VISIBLE);
                    iv_couple_neutral_border.setVisibility(View.VISIBLE);
                    tv_neutral.setTextColor(getResources().getColor(R.color.colorPrimary));
                    break;

            }
        }

        if (select_type.equals("looking_for")) {
            String looking_gender = tv_looking_for.getText().toString().trim();
            setInactiveGender();
            if (looking_gender.contains("Man")) {
                iv_man.setVisibility(View.GONE);
                gif_man.setVisibility(View.VISIBLE);
                iv_man_border.setVisibility(View.VISIBLE);
                tv_man.setTextColor(getResources().getColor(R.color.colorPrimary));
            }

            if (looking_gender.contains("Woman")) {
                iv_woman.setVisibility(View.GONE);
                gif_woman.setVisibility(View.VISIBLE);
                iv_woman_border.setVisibility(View.VISIBLE);
                tv_woman.setTextColor(getResources().getColor(R.color.colorPrimary));
            }

            if (looking_gender.contains("Couple")) {
                iv_couple.setVisibility(View.GONE);
                gif_couple.setVisibility(View.VISIBLE);
                iv_couple_border.setVisibility(View.VISIBLE);
                tv_couple.setTextColor(getResources().getColor(R.color.colorPrimary));
            }

            if (looking_gender.contains("Transgender Male")) {
                iv_trans_male.setVisibility(View.GONE);
                gif_trans_male.setVisibility(View.VISIBLE);
                iv_trans_male_border.setVisibility(View.VISIBLE);
                tv_trans_male.setTextColor(getResources().getColor(R.color.colorPrimary));
            }

            if (looking_gender.contains("Transgender Female")) {
                iv_trans_female.setVisibility(View.GONE);
                gif_trans_female.setVisibility(View.VISIBLE);
                iv_trans_female_border.setVisibility(View.VISIBLE);
                tv_trans_female.setTextColor(getResources().getColor(R.color.colorPrimary));
            }

            if (looking_gender.contains("Non Binary")) {
                iv_neutral.setVisibility(View.GONE);
                gif_neutral.setVisibility(View.VISIBLE);
                iv_couple_neutral_border.setVisibility(View.VISIBLE);
                tv_neutral.setTextColor(getResources().getColor(R.color.colorPrimary));

            }
        }
    }

    // Setting gender
    private void setGender() {
        if (select_type.equals("you_are_a")) {
            String you_are = tv_you_are_a.getText().toString().trim().toLowerCase();
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
                    you_are_selectedId = 6;
                    break;

            }
        }
    }

    // Setting all genders as inactive
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

        tv_man.setTextColor(getResources().getColor(R.color.inactive_text_color));
        tv_woman.setTextColor(getResources().getColor(R.color.inactive_text_color));
        tv_couple.setTextColor(getResources().getColor(R.color.inactive_text_color));
        tv_trans_male.setTextColor(getResources().getColor(R.color.inactive_text_color));
        tv_trans_female.setTextColor(getResources().getColor(R.color.inactive_text_color));
        tv_neutral.setTextColor(getResources().getColor(R.color.inactive_text_color));
    }

    @Override
    public void onBackPressed() {
        session.logout();
    }

    private String getLookingForString() {
        String looking_gender = tv_looking_for.getText().toString().trim();
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


        }if (looking_gender.contains("Non Binary")) {
            getNeutralIntent();
        }

        return selected_looking_id;
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

    // Get Selected Couple Intent in String for display in textview and get id for storing in database
    private void getNeutralIntent() {
        if (selected_looking_for.length() == 0) {
            selected_looking_for = "Non Binary" + selected_looking_for;
            selected_looking_id = "neutral" + selected_looking_id;
        } else if (!selected_looking_for.contains("Non Binary")) {
            selected_looking_for = selected_looking_for + ", " + "Non Binary";
            selected_looking_id = selected_looking_id + "," + "neutral";
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
    private void getNeutralDeselected() {
        if (selected_looking_for.length() == 1) {
            selected_looking_for = selected_looking_for.replace("Non Binary", "");
            selected_looking_id = selected_looking_id.replace("neutral", "");
        } else {
            if (selected_looking_for.contains("Non Binary,")) {
                selected_looking_for = selected_looking_for.replace("Non Binary, ", "");
                selected_looking_id = selected_looking_id.replace("neutral,", "");
            } else if (selected_looking_for.contains(", Non Binary")) {
                selected_looking_for = selected_looking_for.replace(", Non Binary", "");
                selected_looking_id = selected_looking_id.replace(",neutral", "");
            } else if (selected_looking_for.contains("Non Binary")) {
                selected_looking_for = selected_looking_for.replace("Non Binary", "");
                selected_looking_id = selected_looking_id.replace("neutral", "");
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
}
