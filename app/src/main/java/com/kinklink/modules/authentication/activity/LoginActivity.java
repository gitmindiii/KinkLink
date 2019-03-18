package com.kinklink.modules.authentication.activity;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
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
import com.kinklink.modules.authentication.model.Address;
import com.kinklink.modules.authentication.model.FirebaseUserModel;
import com.kinklink.modules.authentication.model.RegistrationInfo;
import com.kinklink.modules.chat.model.Chat;
import com.kinklink.modules.matches.activity.MainActivity;
import com.kinklink.server_task.WebService;
import com.kinklink.session.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    // Get Current Location
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    // Facebook Login
    CallbackManager callbackManager;
    LoginManager loginManager;
    LoginButton btn_facebook;
    private LinearLayout ly_dont_have_acc, ly_no_network;
    private TextView btn_login, btn_try_again, tv_forgot_pass;
    private EditText ed_email_id, ed_password;
    private Session session;
    private String addressFull = "";
    private ImageView cb_rem_me;
    private boolean cb_rem_isChecked;
    private RelativeLayout rl_remember_me;
    private LocationManager locationManager;
    private CardView cv_login;
    // variable to track event time
    private long mLastClickTime = 0;
    private Location mLastLocation;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    private String mLatitude, mLongitude, full_address, city;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;
    private Progress progress;
    private String myUId, otherUId, chatNode, otherName, otherProfileImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progress = new Progress(LoginActivity.this);
        session = new Session(LoginActivity.this);
        init();

        //Get Firebase auth instance
        firebaseDatabase = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        // Set Remember me check box active or inactive
        setRememberMeData();

        FacebookSdk.sdkInitialize(LoginActivity.this);

        // Facebook Integration
        loginManager = LoginManager.getInstance();

        callbackManager = CallbackManager.Factory.create();
        loginManager.logOut();

        // Click Listeners
        ly_dont_have_acc.setOnClickListener(this);
        btn_login.setOnClickListener(this);
        rl_remember_me.setOnClickListener(this);
        tv_forgot_pass.setOnClickListener(this);
        btn_facebook.setOnClickListener(this);

        // Get Latitude and Longitude
        callToGetCurrentLocation();

    }

    private void init() {
        ly_dont_have_acc = findViewById(R.id.ly_dont_have_acc);
        btn_login = findViewById(R.id.btn_login);
        ed_email_id = findViewById(R.id.ed_email_id);
        ed_password = findViewById(R.id.ed_password);
        cb_rem_me = findViewById(R.id.cb_rem_me);
        rl_remember_me = findViewById(R.id.rl_remember_me);
        btn_facebook = findViewById(R.id.btn_facebook);

        ly_no_network = findViewById(R.id.ly_no_network);
        cv_login = findViewById(R.id.cv_login);
        btn_try_again = findViewById(R.id.btn_try_again);
        tv_forgot_pass = findViewById(R.id.tv_forgot_pass);
    }

    // Get Latitude and Longitude
    private void callToGetCurrentLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
            createLocationRequest();
        }
        displayCurrentLocation();  // Method to get current lat long
    }

    private void setRememberMeData() {
        // If Remember me option selected, Set Email and Password from session
        if (session.getRememberEmail() != null && !session.getRememberEmail().equals("")) {
            cb_rem_me.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.check_box));
            cb_rem_isChecked = true;
            ed_email_id.setText(session.getRememberEmail());
        }

        if (session.getRememberPassword() != null && !session.getRememberPassword().equals("")) {
            cb_rem_me.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.check_box));
            cb_rem_isChecked = true;
            ed_password.setText(session.getRememberPassword());
        }
    }

    @Override
    public void onClick(View view) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (view.getId()) {
            case R.id.ly_dont_have_acc:   // Don't have account, Register
                Intent intent = new Intent(LoginActivity.this, RegisterGenderActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_login:  // Login Button
                View v = this.getCurrentFocus();
                if (v != null) {
                    AppHelper.hideKeyboard(v, LoginActivity.this);
                }

                if (isValid()) {
                    btn_login.setEnabled(false);

                    String email = ed_email_id.getText().toString().trim();
                    String password = ed_password.getText().toString().trim();
                    String device_token = FirebaseInstanceId.getInstance().getToken();

                    // Login Api
                    if (device_token != null) {
                        callLoginApi(email, password, device_token);
                    }
                }
                break;

            case R.id.rl_remember_me:    // Remember me option
                if (cb_rem_isChecked) {
                    cb_rem_isChecked = false;
                    cb_rem_me.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.uncheck_box));
                } else {
                    cb_rem_isChecked = true;
                    cb_rem_me.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.check_box));
                }
                break;

            case R.id.tv_forgot_pass:
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
                break;

            case R.id.btn_facebook:
                if (AppHelper.isConnectingToInternet(LoginActivity.this)) {
                    progress.show();
                    facebookLogin();
                } else {
                    CustomToast.getInstance(LoginActivity.this).showToast(LoginActivity.this, getString(R.string.alert_no_network));
                }
                break;
        }
    }

    // Validations for login
    private boolean isValid() {
        Validation v = new Validation();

        if (!v.isEditNull(ed_email_id)) {
            CustomToast.getInstance(this).showToast(this, getString(R.string.login_email_null));
            return false;
        } else if (!v.isEmailValid(ed_email_id)) {
            CustomToast.getInstance(this).showToast(this, getString(R.string.login_email_invalid));
            return false;
        } else if (!v.isEditNull(ed_password)) {
            CustomToast.getInstance(this).showToast(this, getString(R.string.login_password_null));
            return false;
        } else if (!v.isPasswordValid(ed_password)) {
            CustomToast.getInstance(this).showToast(this, getString(R.string.login_password_min_length));
            return false;
        } else if (!v.isPassExceedMax(ed_password)) {
            CustomToast.getInstance(this).showToast(this, getString(R.string.login_password_max_length));
            return false;
        }
        return true;
    }

    // Login Api
    private void callLoginApi(final String email, final String password, final String device_token) {
        if (AppHelper.isConnectingToInternet(this)) {
            progress.show();

            Map<String, String> map = new HashMap<>();
            map.put("email", email);
            map.put("password", password);
            map.put("device_token", device_token);
            map.put("device_type", "2");

            WebService api = new WebService(this, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            /* progress.dismiss();*/
                            if (cb_rem_isChecked) {
                                session.createRememberSession(email, password);
                            } else {
                                session.createRememberSession("", "");
                            }

                            Gson gson = new Gson();
                            RegistrationInfo registrationInfo = gson.fromJson(String.valueOf(js), RegistrationInfo.class);
                            session.createRegistration(registrationInfo);
                            session.setUserLoggedIn();
                            session.showPopup(false);
                            session.setPrivacyPolicy(registrationInfo.policy);

                            Utils.goToOnlineStatus(LoginActivity.this, Constant.ONLINE_STATUS);
                            session.setLoginPassword(password);

                            NotificationManager nManager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
                            assert nManager != null;
                            nManager.cancelAll();

                           /* String login_email = registrationInfo.userDetail.userId + "@kinklink.com";
                            callLoginFirebase(login_email, "123456");*/

                            addUserFirebaseDatabase();

                            // Call Update LatLong Api to update current location
                            if (session.getAuthToken() != null && !session.getAuthToken().equals("")) {
                                if (mLatitude != null && mLongitude != null && full_address != null && city != null && addressFull != null) {
                                    callUpdateLatLongApi(mLatitude, mLongitude, full_address, city, addressFull);
                                }
                            }
                        } else {
                            progress.dismiss();
                            if (message.equals("You are currently inactive by admin")) {
                                String inactive_status = js.getString("user_status");
                                if (inactive_status.equals("0")) {
                                    CustomToast.getInstance(LoginActivity.this).showToast(LoginActivity.this, getResources().getString(R.string.inactive_user));
                                } else {
                                    switch (message) {
                                        case "Please enter valid email address":
                                            CustomToast.getInstance(LoginActivity.this).showToast(LoginActivity.this, getResources().getString(R.string.login_password_wrong));
                                            break;
                                        case "Your password is invalid":
                                            CustomToast.getInstance(LoginActivity.this).showToast(LoginActivity.this, getResources().getString(R.string.login_password_wrong));
                                            break;
                                        default:
                                            CustomToast.getInstance(LoginActivity.this).showToast(LoginActivity.this, message);
                                            break;
                                    }
                                }
                            } else {
                                switch (message) {
                                    case "Please enter valid email address":
                                        CustomToast.getInstance(LoginActivity.this).showToast(LoginActivity.this, getResources().getString(R.string.login_password_wrong));
                                        break;
                                    case "Your password is invalid":
                                        CustomToast.getInstance(LoginActivity.this).showToast(LoginActivity.this, getResources().getString(R.string.login_password_wrong));
                                        break;
                                    default:
                                        CustomToast.getInstance(LoginActivity.this).showToast(LoginActivity.this, message);
                                        break;
                                }
                            }
                            btn_login.setEnabled(true);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    btn_login.setEnabled(true);
                }

            });
            api.callApi("login", Request.Method.POST, map);
        } else {
            View view = this.getCurrentFocus();
            if (view != null) {
                AppHelper.hideKeyboard(view, LoginActivity.this);
            }

            cv_login.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Preventing multiple clicks, using threshold of 1/2 second
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    // Login Api
                    callLoginApi(email, password, device_token);
                }
            });
        }
    }

    /*private void callRegisterFirebase(final RegistrationInfo registrationInfo) {
        if (registrationInfo.userDetail != null) {
            final String email = registrationInfo.userDetail.userId + "@kinklink.com";
            final String password = "123456";

            //create user
            auth.createUserWithEmailAndPassword(email, password).
                    addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Navigation after Login
                                goToScreen(registrationInfo);
                            } else {
                                callLoginFirebase(email, password);
                            }
                        }
                    });
        }
    }

    private void callLoginFirebase(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).
                addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            addUserFirebaseDatabase();
                        } else {
                            callRegisterFirebase(session.getRegistration());
                        }
                    }
                });
    }*/

    // Navigation after Login
    private void goToScreen(RegistrationInfo registrationInfo) {
        progress.dismiss();
        btn_login.setEnabled(true);
        if (registrationInfo != null && registrationInfo.userDetail != null && registrationInfo.userDetail.profile_step != null) {

            if (registrationInfo.userDetail.profile_step.equals("0")) {
                if (registrationInfo.userDetail.isEmailVerified.equals("1")) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finishAffinity();
                } else {
                    session.setScreen("EmailVarification");
                    startActivity(new Intent(LoginActivity.this, ActivityEmailVarification.class));
                    finishAffinity();
                }
            } else {


                switch (registrationInfo.userDetail.profile_step) {
                    case "1": {
                        session.setScreen("UploadPhotosFragment");
                        session.setPolicyDisplay(true);
                        startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
                        finish();
                        break;
                    }

                    case "2": {
                        session.setScreen("SignUpVerifyPhotoFragment");
                        session.setPolicyDisplay(true);
                        startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
                        finish();
                        break;
                    }

                    case "3": {
                        session.setScreen("SelectInterestFragment");
                        session.setPolicyDisplay(true);
                        startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
                        finish();
                        break;
                    }


                    default: {
                        if (session.getRegistration().userDetail.is_profile_complete.equals("0")) {
                            if (session.getRegistration().userDetail.profile_step.equals("0")) {
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {    // If user is not logged in, Make user login
                                session.logout();
                            }
                        } else if (session.getRegistration().userDetail.is_profile_complete.equals("1")) {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }
                    }
                }
            }
        } else {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finishAffinity();
        }
    }

    // Update Lat Long Api
    private void callUpdateLatLongApi(String latitude, String longitude, String full_address, String city, String addressFull) {
        if (AppHelper.isConnectingToInternet(this)) {
            final Map<String, String> map = new HashMap<>();
            map.put("latitude", latitude);
            map.put("longitude", longitude);
            map.put("full_address", addressFull);
            map.put("address", full_address);
            map.put("city", city);

            WebService api = new WebService(LoginActivity.this, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {

                }

                @Override
                public void ErrorListener(VolleyError error) {

                }

            });
            api.callApi("user/latLongUpdate", Request.Method.POST, map);
        } else {
            CustomToast.getInstance(LoginActivity.this).showToast(LoginActivity.this, getString(R.string.alert_no_network));
        }
    }

    private void facebookLogin() {
        if (AppHelper.isConnectingToInternet(LoginActivity.this)) {
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {

                    final String sSocialId = loginResult.getAccessToken().getUserId();

                    GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {

                            if (response.getError() != null) {
                                progress.dismiss();
                                CustomToast.getInstance(LoginActivity.this).showToast(LoginActivity.this, getResources().getString(R.string.alert_api_fail));
                            } else {
                                try {
                                    String email = "";
                                    if (object.has("email")) {
                                        email = object.getString("email");
                                    }

                                    final String socialId = object.getString("id");
                                    final String firstname = object.getString("first_name");
                                    final String lastname = object.getString("last_name");
                                    final String fullname = firstname + " " + lastname;
                                    final String profileImage = "https://graph.facebook.com/" + sSocialId + "/picture?width=1024&height=786";
                                    final String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                    RegistrationInfo reg_info = new RegistrationInfo();
                                    if (reg_info.userDetail != null) {
                                        if (object.has("email")) {
                                            email = object.getString("email");
                                            reg_info.userDetail.email = email;
                                        } else {
                                            reg_info.userDetail.email = "";
                                        }
                                        reg_info.userDetail.social_id = socialId;
                                        reg_info.userDetail.social_type = "facebook";
                                        reg_info.userDetail.full_name = fullname;

                                        RegistrationInfo.UserDetailBean.ImagesBean imagesBean = new RegistrationInfo.UserDetailBean.ImagesBean();
                                        imagesBean.image = profileImage;
                                        reg_info.userDetail.images.add(imagesBean);


                                        session.createRegistration(reg_info);
                                    }

                                    progress.dismiss();

                                    checkSocialLogin(socialId);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    progress.dismiss();

                                }

                            }

                        }

                    });

                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id, first_name, last_name, email, picture");
                    request.setParameters(parameters);
                    request.executeAsync();
                }

                @Override
                public void onCancel() {
                }

                @Override
                public void onError(FacebookException error) {
                    error.toString();
                }
            });

        }
    }

    private void checkSocialLogin(String socialId) {
        if (AppHelper.isConnectingToInternet(this)) {
            String device_token = FirebaseInstanceId.getInstance().getToken();

            Map<String, String> map = new HashMap<>();
            map.put("social_id", socialId);
            map.put("social_type", "facebook");
            map.put("device_token", device_token);
            map.put("device_type", "2");

            WebService api = new WebService(LoginActivity.this, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);
                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            Gson gson = new Gson();
                            RegistrationInfo registrationInfo = gson.fromJson(String.valueOf(js), RegistrationInfo.class);
                            session.showPopup(false);
                            session.createRegistration(registrationInfo);
                            session.setUserLoggedIn();

                            /*String login_email = registrationInfo.userDetail.userId + "@kinklink.com";
                            callLoginFirebase(login_email, "123456");*/

                            addUserFirebaseDatabase();

                            //   goToScreen(registrationInfo);
                        } else {
                            if (message.equalsIgnoreCase("No social user registered ")) {
                                Intent intent = new Intent(LoginActivity.this, RegisterGenderActivity.class);
                                startActivity(intent);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        CustomToast.getInstance(LoginActivity.this).showToast(LoginActivity.this, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {

                }

            });
            api.callApi("checkSocialRegistor", Request.Method.POST, map);
        } else {
            CustomToast.getInstance(LoginActivity.this).showToast(LoginActivity.this, getString(R.string.alert_no_network));
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Constant.MY_PERMISSIONS_REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    displayCurrentLocation();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        // Displaying the new location on UI
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
        //  displayCurrentLocation();

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
        if (LocationRuntimePermission.checkLocationPermission(this)) {
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (isGPSEnabled) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if (mLastLocation != null) {
                    mLatitude = String.valueOf(mLastLocation.getLatitude());
                    mLongitude = String.valueOf(mLastLocation.getLongitude());

                    LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                    session.setFilterCity(Double.toString(mLastLocation.getLatitude()), Double.toString(mLastLocation.getLongitude()));
                    new GioAddressTask(LoginActivity.this, latLng, new GioAddressTask.LocationListner() {
                        @Override
                        public void onSuccess(Address address) {
                            full_address = address.getFullAddress();
                            if (address.getCity() != null) {
                                addressFull = address.getCity();
                            }
                            if (address.getState() != null) {
                                if (addressFull.length() > 0)
                                    addressFull = addressFull + "," + address.getState();
                                else addressFull = address.getState();

                            }
                            if (address.getCountry() != null) {
                                if (addressFull.length() > 0)
                                    addressFull = addressFull + "," + address.getCountry();
                                else addressFull = address.getCountry();
                            }
                            session.setAddress(addressFull);
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
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) LoginActivity.this);
    }


    private void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) LoginActivity.this);
    }

    // Write data to user table in firebase
    private void addUserFirebaseDatabase() {
        RegistrationInfo registrationInfo = session.getRegistration();
        String device_token = FirebaseInstanceId.getInstance().getToken();

        if (registrationInfo != null && registrationInfo.userDetail != null && registrationInfo.userDetail.full_name != null) {
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

            String value = registrationInfo.userDetail.chat_update;
            if(value.equals("1"))
                userModel.firebaseToken = device_token;
            else
                userModel.firebaseToken = "";


            userModel.name = name;
            userModel.profilePic = profilePic;
            userModel.timeStamp = ServerValue.TIMESTAMP;
            userModel.uid = uId;
            userModel.isAdmin = "0";
            userModel.isUserDeleted = 0;

            database.child(Constant.USER_TABLE).child(uId).setValue(userModel).
                    addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            RegistrationInfo registrationInfo = session.getRegistration();
                            myUId = registrationInfo.userDetail.userId;
                            otherUId = "1";
                            gettingDataFromUserTable(otherUId);

                            //create note for chatroom
                            chatNode = gettingNotes();

                            firebaseDatabase.getReference().child(Constant.CHAT_ROOMS_TABLE).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.hasChild(chatNode)) {
                                        if (otherUId.equals("1")) {
                                            sendMessage();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            // Navigation after Login

                            goToScreen(session.getRegistration());
                        }
                    });
        }
    }

    // Get other user data from User table from firebase with help of UID
    private void gettingDataFromUserTable(String otherUId) {
        if (otherUId != null) {
            firebaseDatabase.getReference().child(Constant.USER_TABLE).child(otherUId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    FirebaseUserModel otherUserInfo;
                    otherUserInfo = dataSnapshot.getValue(FirebaseUserModel.class);

                    assert otherUserInfo != null;
                    otherName = otherUserInfo.name;

                    String[] nameArray = otherName.split(" ");
                    StringBuilder builder = new StringBuilder();
                    for (String s : nameArray) {
                        String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                        builder.append(cap).append(" ");
                    }

                    if (!otherUserInfo.profilePic.equals("")) {
                        otherProfileImage = otherUserInfo.profilePic;
                    } else {
                        otherProfileImage = "";
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    //create note for chatroom
    private String gettingNotes() {
        int myUid_ = Integer.parseInt(myUId);
        int otherUID_ = Integer.parseInt(otherUId);

        if (myUid_ < otherUID_) {
            chatNode = myUId + "_" + otherUId;
        } else {
            chatNode = otherUId + "_" + myUId;
        }

        return chatNode;
    }

    private void sendMessage() {
        String pushkey = firebaseDatabase.getReference().child(Constant.CHAT_ROOMS_TABLE).child(chatNode).push().getKey();
        String msg = session.getRegistration().policy;

        Chat otherChat = new Chat();
        otherChat.deleteby = "1";
        otherChat.firebaseToken = "";

        otherChat.imageUrl = "";
        otherChat.message = msg;
        otherChat.image = 0;

        if (otherName != null) {
            otherChat.name = otherName.toLowerCase();
        } else {
            otherChat.name = "admin";
        }

        if (otherProfileImage != null) {
            otherChat.profilePic = otherProfileImage;
        } else {
            otherChat.profilePic = "";
        }

        otherChat.timeStamp = ServerValue.TIMESTAMP;
        otherChat.uid = otherUId;
        otherChat.lastMsg = otherUId;

        firebaseDatabase.getReference().child(Constant.CHAT_ROOMS_TABLE).child(chatNode).child(pushkey).setValue(otherChat);
        firebaseDatabase.getReference().child(Constant.CHAT_HISTORY_TABLE).child(myUId).child(otherUId).setValue(otherChat);
        //   firebaseDatabase.getReference().child(Constant.CHAT_HISTORY_TABLE).child(otherUId).child(myUId).setValue(otherChat);

        progress.dismiss();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        if (progress.isShowing()) {
            progress.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progress.isShowing()) {
            progress.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (progress.isShowing()) {
            progress.dismiss();
        }

    }
}
