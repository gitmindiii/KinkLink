package com.kinklink.session;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.facebook.login.LoginManager;
import com.google.gson.Gson;
import com.kinklink.modules.authentication.activity.LoginActivity;
import com.kinklink.modules.authentication.model.RegistrationInfo;

public class Session {
    private Context context;
    private SharedPreferences mypref, mypref2;
    private SharedPreferences.Editor editor, editor2;
    private static final String PREF_NAME = "KINKLINK";
    private static final String PREF_NAME2 = "AppSession";

    private static final String ScreenName = "screenName";
    private static final String isShow = "isShowPopup";
    private static final String RegisterInfo = "preRegistrationInfo";
    private static final String AuthToken = "authToken";
    private static final String IsUserLoggedIn = "userLoggedIn";
    private static final String UserGetRegistered = "userGetRegistered";
    private static final String UserPolicyDisplay = "userPolicyDisplay";
    private static final String UserPrivacyPolicy = "userPrivacyPolicy";

    private static final String RememberEmail = "remEmail";
    private static final String RememberPassword = "remPassword";

    private static final String LoginPassword = "loginPassword";

    private static final String FilterUserName = "filterUserName";
    private static final String Address = "address";
    private static final String FilterInterests = "filterInterests";
    private static final String FilterLatitude = "filterLatitude";
    private static final String FilterLongitude = "filterLongitude";
    private static final String FilterPlace = "filterPlace";
    private static final String FilterInterestString = "filterInterestString";
    private static final String FilterFromAge = "filterFromAge";
    private static final String FilterToAge = "filterToAge";
    private static final String FilterMinDistance = "filterMinDistance";
    private static final String FilterMaxDistance = "filterMaxDistance";
    private static final String FilterGender = "filterGender";
    private static final String FilterBodyType = "filterBodyType";
    private static final String FilterEthnicity = "filterEthnicity";
    private static final String FilterSortBy = "sortByType";
    private static final String FilterRandomIndex = "randomIndex";

    private static final String MakeOfferStatus = "makeOfferStatus";

    public Session(Context context) {
        this.context = context;
        mypref = this.context.getSharedPreferences(PREF_NAME, Context.MODE_MULTI_PROCESS);
        mypref2 = this.context.getSharedPreferences(PREF_NAME2, Context.MODE_MULTI_PROCESS);
        editor = mypref.edit();
        editor2 = mypref2.edit();
        editor.apply();
        editor2.apply();
    }

    public void createRegistration(RegistrationInfo registrationInfo) {
        Gson gson = new Gson();
        String json = gson.toJson(registrationInfo);
        editor.putString(RegisterInfo, json);
        editor.putString(AuthToken, registrationInfo.userDetail.auth_token);
        editor.commit();
    }

    public RegistrationInfo getRegistration() {
        Gson gson = new Gson();
        String string = mypref.getString(RegisterInfo, "");
        if (!string.isEmpty())
            return gson.fromJson(string, RegistrationInfo.class);
        else return null;
    }

    public String getAuthToken() {
        return mypref.getString(AuthToken, "");
    }

    public void setScreen(String screenName) {
        editor.putString(ScreenName, screenName);
        editor.apply();
    }

    public void showPopup(boolean isShowPopup) {
        editor.putBoolean(isShow, isShowPopup);
        editor.apply();
    }

    public boolean getIsShowPopup(){
        return mypref.getBoolean(ScreenName, false);
    }

    public String getScreen() {
        return mypref.getString(ScreenName, "");
    }

    public void setUserLoggedIn() {
        editor.putBoolean(IsUserLoggedIn, true);
        editor.apply();
    }

    public boolean getUserLoggedIn() {
        return mypref.getBoolean(IsUserLoggedIn, false);
    }

    public void createRememberSession(String email, String password) {
        editor2.putString(RememberEmail, email);
        editor2.putString(RememberPassword, password);
        editor2.apply();
    }

    public String getRememberEmail() {
        return mypref2.getString(RememberEmail, "");
    }

    public String getRememberPassword() {
        return mypref2.getString(RememberPassword, "");
    }

    public void logout() {
        final LoginManager loginManager = LoginManager.getInstance();
        loginManager.logOut();

        Intent showLogin = new Intent(context, LoginActivity.class);
        showLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        showLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(showLogin);
        editor.putBoolean(IsUserLoggedIn, false);
        editor.clear();
        editor.apply();
    }


    public void setFilterUserName(String filterUserName) {
        editor.putString(FilterUserName, filterUserName);
        editor.apply();
    }

    public void setAddress(String address) {
        editor.putString(Address, address);
        editor.apply();
    }

    public String getAddress() {
        return mypref.getString(Address, "");
    }

    public String getFilterUserName() {
        return mypref.getString(FilterUserName, "");
    }


    public void setFilterInterests(String selected_interests) {
        editor.putString(FilterInterests, selected_interests);
        editor.apply();
    }

    public void setFilterCity(String latitude, String longitude) {
        editor.putString(FilterLatitude, latitude);
        editor.putString(FilterLongitude, longitude);
        editor.apply();
    }

    public String getFilterInterests() {
        return mypref.getString(FilterInterests, "");
    }

    public String getFilterLatitude() {
        return mypref.getString(FilterLatitude, "");
    }

    public String getFilterLongitude() {
        return mypref.getString(FilterLongitude, "");
    }

    public void setFilterPlaceInterest(String place, String interest) {
        editor.putString(FilterPlace, place);
        editor.putString(FilterInterestString, interest);
        editor.apply();
    }

    public String getFilterPlace() {
        return mypref.getString(FilterPlace, "");
    }

    public String getFilterInterestString() {
        return mypref.getString(FilterInterestString, "");
    }

    public void setOfferStatus(int status) {
        editor.putInt(MakeOfferStatus, status);
        editor.apply();
    }

    public int getOfferStatus() {
        return mypref.getInt(MakeOfferStatus, 4);
    }

    public void setFilterAgeDistance(String fromAge, String toAge, String rangMin, String rangMax) {
        editor.putString(FilterFromAge, fromAge);
        editor.putString(FilterToAge, toAge);
        editor.putString(FilterMinDistance, rangMin);
        editor.putString(FilterMaxDistance, rangMax);
        editor.apply();
    }

    public String getFilterFromAge() {
        return mypref.getString(FilterFromAge, "");
    }

    public String getFilterToAge() {
        return mypref.getString(FilterToAge, "");
    }

    public String getFilterMinDistance() {
        return mypref.getString(FilterMinDistance, "");
    }

    public String getFilterMaxDistance() {
        return mypref.getString(FilterMaxDistance, "");
    }

    public void setGenderIntentBodyEthnicity(String gender, String body_type, String ethnicity) {
        editor.putString(FilterGender, gender);
        editor.putString(FilterBodyType, body_type);
        editor.putString(FilterEthnicity, ethnicity);
        editor.apply();
    }

    public String getFilterGender() {
        return mypref.getString(FilterGender, "");
    }

    public String getFilterBodyType() {
        return mypref.getString(FilterBodyType, "");
    }

    public String getFilterEthnicity() {
        return mypref.getString(FilterEthnicity, "");
    }

    public void setFilterSortBy(String sortByType) {
        editor.putString(FilterSortBy, sortByType);
        editor.apply();
    }

    public String getFilterSortBy() {
        return mypref.getString(FilterSortBy, "");
    }

    public void setFilterRandomIndex(String index) {
        editor.putString(FilterRandomIndex, index);
        editor.apply();
    }

    public String getFilterRandomIndex() {
        return mypref.getString(FilterRandomIndex, "");
    }

    public void setLoginPassword(String password) {
        editor.putString(LoginPassword, password);
        editor.apply();
    }

    public String getLoginPassword() {
        return mypref.getString(LoginPassword, "");
    }

    public void setUserGetRegistered(boolean status) {
        editor.putBoolean(UserGetRegistered, status);
        editor.apply();
    }

    public boolean getUserGetRegistered() {
        return mypref.getBoolean(UserGetRegistered, false);
    }

    public void setPolicyDisplay(boolean policyDisplay) {
        editor.putBoolean(UserPolicyDisplay, policyDisplay);
        editor.apply();
    }

    public boolean getPolicyDisplay() {
        return mypref.getBoolean(UserPolicyDisplay, false);
    }

    public void setPrivacyPolicy(String privacyPolicy) {
        editor.putString(UserPrivacyPolicy, privacyPolicy);
        editor.apply();
    }

    public String getPrivacyPolicy() {
        return mypref.getString(UserPrivacyPolicy, "");
    }
}
