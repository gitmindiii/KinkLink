package com.kinklink.modules.authentication.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.kinklink.R;
import com.kinklink.application.KinkLink;
import com.kinklink.helper.AppHelper;
import com.kinklink.helper.Constant;
import com.kinklink.helper.CustomToast;
import com.kinklink.helper.GioAddressTask;
import com.kinklink.helper.LocationRuntimePermission;
import com.kinklink.helper.Progress;
import com.kinklink.helper.Utils;
import com.kinklink.helper.Validation;
import com.kinklink.modules.authentication.activity.RegistrationActivity;
import com.kinklink.modules.authentication.model.RegistrationInfo;
import com.kinklink.server_task.WebService;
import com.kinklink.session.Session;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.LOCATION_SERVICE;

public class CreateAccountFragment extends Fragment implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private Session session;
    private Context mContext;
    private EditText ed_acc_name, ed_acc_email, ed_acc_password, ed_acc_confirm_pass;
    private TextView tv_acc_dob, btn_create_acc;
    private LinearLayout ly_get_login, ly_date_picker;
    private DatePickerDialog date_picker;
    private String date, acc_gender, acc_looking_for;

    private static final String ACC_GENDER = "acc_gender";
    private static final String ACC_LOOKING_FOR = "acc_looking_for";

    private ImageView iv_back;
    private TextView tv_registration_heading;
    private TextView btn_try_again;

    private ImageView iv_step_one, iv_step_one_bullet, iv_step_two, iv_step_two_bullet, iv_step_three, iv_step_three_bullet, iv_step_four_bullet;
    private TextView tv_step_two, tv_step_three, tv_step_four;
    private View status_view_1, status_view_2, status_view_3;

    private LocationManager locationManager;

    // Get Current Location
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    private String mLatitude, mLongitude, full_address, city;

    private CardView cv_registration;
    private LinearLayout ly_no_network, ly_acc_con_password, ly_acc_password;
    private FirebaseAuth auth;
    private Progress progress;

    // variable to track event time
    private long mLastClickTime = 0;

    public CreateAccountFragment() {
    }

    // Getting Gender from RegisterGenderActivity to RegistrationActivity
    public static CreateAccountFragment newInstance(String gender, String looking_for) {
        Bundle args = new Bundle();
        CreateAccountFragment fragment = new CreateAccountFragment();
        fragment.setArguments(args);
        args.putString(ACC_GENDER, gender);
        args.putString(ACC_LOOKING_FOR, looking_for);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Getting Gender
        if (getArguments() != null) {
            if ((getArguments().getString(ACC_GENDER) != null && getArguments().getString(ACC_LOOKING_FOR) != null)) {
                String you_are = getArguments().getString(ACC_GENDER);
                acc_looking_for = getArguments().getString(ACC_LOOKING_FOR);

                assert you_are != null;
                switch (you_are) {
                    case "Man":
                        acc_gender = "man";
                        break;

                    case "Woman":
                        acc_gender = "woman";
                        break;

                    case "Couple":
                        acc_gender = "couple";
                        break;

                    case "Transgender Male":
                        acc_gender = "transgender_male";
                        break;

                    case "Transgender Female":
                        acc_gender = "transgender_female";
                        break;
                    case "Non Binary":
                        acc_gender = "neutral";
                        break;


                }
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_account, container, false);
        init(view);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        progress = new Progress(mContext);

        session = new Session(mContext);
        session.setScreen("CreateAccountFragment");

        RegistrationInfo reg_info = session.getRegistration();

        // Set data if registered with facebook
        if (reg_info != null && reg_info.userDetail.social_type.equals("facebook")) {
            if (reg_info.userDetail.full_name != null) {
                ed_acc_name.setText(reg_info.userDetail.full_name);
            }

            if (reg_info.userDetail.email != null && !reg_info.userDetail.email.equals("")) {
                ed_acc_email.setText(reg_info.userDetail.email);
                ed_acc_email.setEnabled(false);
            } else {
                ed_acc_email.setEnabled(true);
            }

            ly_acc_password.setVisibility(View.GONE);
            ly_acc_con_password.setVisibility(View.GONE);
        }

        // Set Create Account Fragment Active
        setCreateAccountFragmentActive();

        ly_date_picker.setOnClickListener(this);
        btn_create_acc.setOnClickListener(this);
        ly_get_login.setOnClickListener(this);

        // Get Latitude and Longitude
        callToGetCurrentLocation();

        return view;
    }

    private void init(View view) {
        ed_acc_name = view.findViewById(R.id.ed_acc_name);
        tv_acc_dob = view.findViewById(R.id.tv_acc_dob);
        ed_acc_email = view.findViewById(R.id.ed_acc_email);
        ed_acc_password = view.findViewById(R.id.ed_acc_password);
        ed_acc_confirm_pass = view.findViewById(R.id.ed_acc_confirm_pass);

        ly_acc_password = view.findViewById(R.id.ly_acc_password);
        ly_acc_con_password = view.findViewById(R.id.ly_acc_con_password);

        ly_date_picker = view.findViewById(R.id.ly_date_picker);
        btn_create_acc = view.findViewById(R.id.btn_create_acc);
        ly_get_login = view.findViewById(R.id.ly_get_login);

        iv_back = ((RegistrationActivity) mContext).findViewById(R.id.iv_back);
        tv_registration_heading = ((RegistrationActivity) mContext).findViewById(R.id.tv_registration_heading);

        cv_registration = ((RegistrationActivity) mContext).findViewById(R.id.cv_registration);
        ly_no_network = ((RegistrationActivity) mContext).findViewById(R.id.ly_no_network);
        btn_try_again = ((RegistrationActivity) mContext).findViewById(R.id.btn_try_again);

        // Step 1
        iv_step_one = ((RegistrationActivity) mContext).findViewById(R.id.iv_step_one);
        iv_step_one_bullet = ((RegistrationActivity) mContext).findViewById(R.id.iv_step_one_bullet);

        // Step 2
        iv_step_two = ((RegistrationActivity) mContext).findViewById(R.id.iv_step_two);
        iv_step_two_bullet = ((RegistrationActivity) mContext).findViewById(R.id.iv_step_two_bullet);
        tv_step_two = ((RegistrationActivity) mContext).findViewById(R.id.tv_step_two);

        // Step 3
        iv_step_three = ((RegistrationActivity) mContext).findViewById(R.id.iv_step_three);
        iv_step_three_bullet = ((RegistrationActivity) mContext).findViewById(R.id.iv_step_three_bullet);
        tv_step_three = ((RegistrationActivity) mContext).findViewById(R.id.tv_step_three);

        // Step 4
        iv_step_four_bullet = ((RegistrationActivity) mContext).findViewById(R.id.iv_step_four_bullet);
        tv_step_four = ((RegistrationActivity) mContext).findViewById(R.id.tv_step_four);

        status_view_1 = ((RegistrationActivity) mContext).findViewById(R.id.status_view_1);
        status_view_2 = ((RegistrationActivity) mContext).findViewById(R.id.status_view_2);
        status_view_3 = ((RegistrationActivity) mContext).findViewById(R.id.status_view_3);
    }

    // Set Create Account Fragment Active
    private void setCreateAccountFragmentActive() {
        iv_back.setVisibility(View.VISIBLE);
        tv_registration_heading.setText(getString(R.string.create_your_account));
        // Utils.setTypeface(tv_basic_info, mContext, R.font.lato_bold);

        // Step 1
        iv_step_one.setVisibility(View.GONE);
        iv_step_one_bullet.setVisibility(View.VISIBLE);

        // Step 2
        iv_step_two.setVisibility(View.GONE);
        iv_step_two_bullet.setVisibility(View.GONE);
        tv_step_two.setVisibility(View.VISIBLE);

        // Step 3
        iv_step_three.setVisibility(View.GONE);
        iv_step_three_bullet.setVisibility(View.GONE);
        tv_step_three.setVisibility(View.VISIBLE);

        // Step 4
        iv_step_four_bullet.setVisibility(View.GONE);
        tv_step_four.setVisibility(View.VISIBLE);

        status_view_1.setBackgroundColor(getResources().getColor(R.color.gray));
        status_view_2.setBackgroundColor(getResources().getColor(R.color.gray));
        status_view_3.setBackgroundColor(getResources().getColor(R.color.gray));
    }

    // Get current lat long
    private void callToGetCurrentLocation() {
        // Get Latitude and Longitude
        locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
            createLocationRequest();
        }

        // Display current location using Fused Location Api
        displayCurrentLocation();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    // Get Latitude and Longitude


    @Override
    public void onClick(View view) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (view.getId()) {
            case R.id.ly_date_picker:  // Date picker for select birth date
                openDatePicker();
                break;

            case R.id.btn_create_acc:   // Create account button
                RegistrationInfo reg_info = session.getRegistration();
                if (reg_info != null && reg_info.userDetail != null && reg_info.userDetail.social_type != null) {
                    if (reg_info.userDetail.social_type.equals("facebook")) {
                        if (isFacebookValid()) {
                            String full_name = ed_acc_name.getText().toString().trim();
                            String email = ed_acc_email.getText().toString().trim();
                            String dob = tv_acc_dob.getText().toString().trim();

                            // Changing date format from MM-DD-YYYY to  YYYY-MM-DD
                            String dateOfBirth = Utils.dateInYMDFormat(dob);

                            // Create Account Button
                            callCreateAccountApi(full_name, email, "", dateOfBirth, acc_looking_for, acc_gender, reg_info.userDetail.social_id, "facebook", reg_info.userDetail.images.get(0).image, mLatitude, mLongitude);
                        }

                    }
                } else {
                    if (isValid()) {
                        String full_name = ed_acc_name.getText().toString().trim();
                        String email = ed_acc_email.getText().toString().trim();
                        String password = ed_acc_password.getText().toString().trim();
                        String dob = tv_acc_dob.getText().toString().trim();

                        // Changing date format from MM-DD-YYYY to  YYYY-MM-DD
                        String dateOfBirth = Utils.dateInYMDFormat(dob);

                        // Create Account Button
                        callCreateAccountApi(full_name, email, password, dateOfBirth, acc_looking_for, acc_gender, "", "", "", mLatitude, mLongitude);
                    }
                }

                break;

            case R.id.ly_get_login:   // Already have an account, Login
                session.logout();
                break;

            case R.id.iv_back:
                ((RegistrationActivity) mContext).finish();
                break;
        }
    }

    // Validations for creating account with facebook
    private boolean isFacebookValid() {
        String name = ed_acc_name.getText().toString().trim();
        Validation v = new Validation();

        if (!v.isEditNull(ed_acc_name)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_name_null));
            return false;
        } else if (!v.isEditNull(ed_acc_email)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_email_null));
            return false;
        } else if (!v.isEmailValid(ed_acc_email)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_email_invalid));
            return false;
        } else if (name.length() == 1) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_name_len_1));
            return false;
        } else if (!v.isNameValid(ed_acc_name)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_name_invalid));
            return false;
        } else if (!v.isTextNull(tv_acc_dob)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_dob_null));
            return false;
        }

        return true;
    }

    // Validations for Create Account
    private boolean isValid() {
        String name = ed_acc_name.getText().toString().trim();
        String password = ed_acc_password.getText().toString().trim();
        String con_password = ed_acc_confirm_pass.getText().toString().trim();
        Validation v = new Validation();

        if (!v.isEditNull(ed_acc_name)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_name_null));
            return false;
        } else if (name.length() == 1) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_name_len_1));
            return false;
        } else if (!v.isNameValid(ed_acc_name)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_name_invalid));
            return false;
        } else if (!v.isTextNull(tv_acc_dob)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_dob_null));
            return false;
        } else if (!v.isEditNull(ed_acc_email)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_email_null));
            return false;
        } else if (!v.isEmailValid(ed_acc_email)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_email_invalid));
            return false;
        } else if (!v.isEditNull(ed_acc_password)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_pass_null));
            return false;
        } else if (!v.isPasswordValid(ed_acc_password)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_pass_invalid));
            return false;
        } else if (!v.isPassExceedMax(ed_acc_password)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_pass_exceed_max_len));
            return false;
        } else if (!v.isEditNull(ed_acc_confirm_pass)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_con_pass_null));
            return false;
        } else if (!password.equals(con_password)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_con_pass_not_match));
            return false;
        }

        return true;
    }

    // Date picker to select birth date
    private void openDatePicker() {
        if (tv_acc_dob.getText().toString().trim().equals("")) {
            Calendar now = Calendar.getInstance();
            int mYear = 2000;   // Open Calendar from 1900
            int mMonth = 1;
            int mDay = 1;
            now.set(mYear, mMonth, mDay);

            setDateOnDatePickerDialog(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

        } else {
            String[] s = tv_acc_dob.getText().toString().split("-");
            int mDay = Integer.parseInt(s[1]);
            int mMonth = Integer.parseInt(s[0]);
            int mYear = Integer.parseInt(s[2]);

            setDateOnDatePickerDialog(mYear, mMonth, mDay);
        }
    }

    // Set Date in date picker dialog
    public void setDateOnDatePickerDialog(int year, int month, int day) {
        date_picker = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                String day, month;
                day = (dayOfMonth < 10) ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
                monthOfYear += 1;
                month = (monthOfYear < 10) ? "0" + monthOfYear : String.valueOf(monthOfYear);

                //  date = year + "-" + month + "-" + day;
                date = month + "-" + day + "-" + year;
                tv_acc_dob.setText(date);
            }
        }, year, month - 1, day);

        // Setting Max Date
        Calendar cal = Calendar.getInstance();
        int mYear = cal.get(Calendar.YEAR) - 18;
        int mMonth = cal.get(Calendar.MONTH);
        int mDay = cal.get(Calendar.DAY_OF_MONTH);
        cal.set(mYear, mMonth, mDay);
        date_picker.setMaxDate(cal);

        // Setting Min Date
        Calendar c = Calendar.getInstance();
        int mYr = 1900;
        int mMon = 0;
        int mDy = 1;
        c.set(mYr, mMon, mDy);
        date_picker.setMinDate(c);

        date_picker.show(((RegistrationActivity) mContext).getFragmentManager(), "");
        date_picker.setAccentColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        date_picker.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                date_picker.dismiss();
            }
        });
    }

    // Create Account Api
    private void callCreateAccountApi(final String full_name, final String email, final String password, final String dob, String looking_for, String gender, String socialId, String socialType, String socialImage, String latitude, final String longitude) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            String device_token = FirebaseInstanceId.getInstance().getToken();

            final Map<String, String> map = new HashMap<>();
            map.put("full_name", full_name);
            map.put("email", email);
            map.put("password", password);
            if (latitude == null) map.put("latitude", "");
            else map.put("latitude", latitude);

            if (latitude == null) map.put("longitude", "");
            else map.put("longitude", longitude);


            map.put("dob", dob);
            map.put("looking_for", looking_for);
            map.put("gender", gender);
            map.put("social_id", socialId);
            map.put("social_type", socialType);
            map.put("image", socialImage);
            map.put("device_token", device_token);
            map.put("device_type", "2");
            map.put("signupFrom", "2");
            Log.i("map873", "" + map.toString());

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            session.setUserLoggedIn();  // Setting user logged in
                            session.setUserGetRegistered(true);
                            //   session.setPolicyDisplay(true);
                            Utils.goToOnlineStatus(mContext, Constant.ONLINE_STATUS);

                            Gson gson = new Gson();
                            RegistrationInfo registrationInfo = gson.fromJson(String.valueOf(js), RegistrationInfo.class);
                            // registrationInfo.userDetail.is_profile_complete = "0";
                            session.createRegistration(registrationInfo);

                            session.setPrivacyPolicy(registrationInfo.policy);

                            session.setLoginPassword(password);

                            //  callRegisterFirebase(registrationInfo);

                            // registrationInfo.userDetail.profile_step = "1";
                            session.createRegistration(registrationInfo);
                            // session.setScreen("SignUpVerifyPhotoFragment");
                            // ((RegistrationActivity) mContext).replaceFragment(SignUpVerifyPhotoFragment.newInstance(), false, R.id.reg_fragment_place);

                            session.setScreen("UploadPhotosFragment");

                            cv_registration.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            // Call Update LatLong Api to update current location
                            if (session.getAuthToken() != null && !session.getAuthToken().equals("")) {
                                if (mLatitude != null && mLongitude != null && full_address != null && city != null) {
                                    callUpdateLatLongApi(mLatitude, mLongitude, full_address, city);
                                }
                            }

                            RegistrationInfo reg_info = session.getRegistration();
                            if (reg_info != null && reg_info.userDetail.social_id.equals("")) {
                                openEmailSentMagDialog();
                            } else {
                                ((RegistrationActivity) mContext).replaceFragment(UploadPhotosFragment.newInstance(), false, R.id.reg_fragment_place);

                            }

                        } else {
                            progress.dismiss();
                            cv_registration.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);
                            switch (message) {
                                case "Email id already exist":
                                    CustomToast.getInstance(mContext).showToast(mContext, getResources().getString(R.string.acc_email_already_exist));
                                    break;

                                default:
                                    CustomToast.getInstance(mContext).showToast(mContext, message);
                                    break;
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        cv_registration.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                }

            });
            api.callApi("registration", Request.Method.POST, map);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            cv_registration.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create Account Button
                    callCreateAccountApi(full_name, email, password, dob, acc_looking_for, acc_gender, "", "", "", mLatitude, mLongitude);
                }
            });
        }
    }

   /* private void callRegisterFirebase(final RegistrationInfo registrationInfo) {
        final String email = registrationInfo.userDetail.userId + "@kinklink.com";
        final String password = "123456";

        //create user
        auth.createUserWithEmailAndPassword(email, password).
                addOnCompleteListener(((RegistrationActivity) mContext), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            RegistrationInfo registrationInfo = session.getRegistration();
                            registrationInfo.userDetail.profile_step = "1";
                            session.createRegistration(registrationInfo);
                            // session.setScreen("SignUpVerifyPhotoFragment");
                            //  ((RegistrationActivity) mContext).replaceFragment(SignUpVerifyPhotoFragment.newInstance(), false, R.id.reg_fragment_place);

                            session.setScreen("UploadPhotosFragment");
                            ((RegistrationActivity) mContext).replaceFragment(UploadPhotosFragment.newInstance(), false, R.id.reg_fragment_place);

                        } else {
                            callLoginFirebase(email, password);
                        }

                    }
                });
    }

    private void callLoginFirebase(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).
                addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            RegistrationInfo registrationInfo = session.getRegistration();
                            registrationInfo.userDetail.profile_step = "1";
                            session.createRegistration(registrationInfo);
                           // session.setScreen("SignUpVerifyPhotoFragment");
                            // ((RegistrationActivity) mContext).replaceFragment(SignUpVerifyPhotoFragment.newInstance(), false, R.id.reg_fragment_place);

                            session.setScreen("UploadPhotosFragment");
                            ((RegistrationActivity) mContext).replaceFragment(UploadPhotosFragment.newInstance(), false, R.id.reg_fragment_place);

                        } else {
                            callRegisterFirebase(session.getRegistration());
                        }
                    }
                });
    }*/

    // Update Lat Long Api
    private void callUpdateLatLongApi(String latitude, String longitude, String full_address, String city) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            final Map<String, String> map = new HashMap<>();
            map.put("latitude", latitude);
            map.put("longitude", longitude);
            map.put("address", full_address);
            map.put("city", city);

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {

                }

                @Override
                public void ErrorListener(VolleyError error) {

                }

            });
            api.callApi("user/latLongUpdate", Request.Method.POST, map);
        } else {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.alert_no_network));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Constant.MY_PERMISSIONS_REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    displayCurrentLocation();
                } else {
                    displayCurrentLocation();
                }
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        // Display current location using Fused Location Api
        displayCurrentLocation();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayCurrentLocation();        // Display current location using Fused Location Api

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    // Display current location using Fused Location Api
    synchronized private void displayCurrentLocation() {
        // Runtime Location Permission
        if (LocationRuntimePermission.checkLocationPermission(mContext)) {
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (isGPSEnabled) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if (mLastLocation != null) {
                    mLatitude = String.valueOf(mLastLocation.getLatitude());
                    mLongitude = String.valueOf(mLastLocation.getLongitude());

                    LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                    new GioAddressTask(mContext, latLng, new GioAddressTask.LocationListner() {
                        @Override
                        public void onSuccess(com.kinklink.modules.authentication.model.Address address) {
                            full_address = address.getFullAddress();
                            city = address.getCity();
                        }
                    }).execute();
                }
            }
        }
    }

    /**
     * Method to verify google play services on the device
     */

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(mContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {

            }
            return false;
        }
        return true;
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        int UPDATE_INTERVAL = 10000;
        int FASTEST_INTERVAL = 5000;
        int DISPLACEMENT = 10;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPlayServices();

        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }

    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) getActivity());
    }


    private void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

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

    @Override
    public void onPause() {
        super.onPause();
        if (progress != null) {
            progress.dismiss();
        }
    }

    // Dialog to show email sent message
    private void openEmailSentMagDialog() {
        final Dialog legalDialog = new Dialog(mContext);
        legalDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        legalDialog.setCancelable(false);
        legalDialog.setContentView(R.layout.emailsent_msg_dialog);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(legalDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        legalDialog.getWindow().setAttributes(lWindowParams);

        TextView btn_ok = legalDialog.findViewById(R.id.btn_ok);


        TextView alert_message = legalDialog.findViewById(R.id.alert_message);

        alert_message.setMovementMethod(new ScrollingMovementMethod());


        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                legalDialog.dismiss();

            }
        });

        legalDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                ((RegistrationActivity) mContext).replaceFragment(UploadPhotosFragment.newInstance(), false, R.id.reg_fragment_place);

            }
        });


        legalDialog.getWindow().setGravity(Gravity.CENTER);
        legalDialog.show();
    }

}
