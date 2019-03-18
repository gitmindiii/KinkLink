package com.kinklink.modules.matches.fragment;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.kinklink.R;
import com.kinklink.application.KinkLink;
import com.kinklink.helper.AppHelper;
import com.kinklink.helper.Constant;
import com.kinklink.helper.CustomToast;
import com.kinklink.helper.Progress;
import com.kinklink.helper.Utils;
import com.kinklink.helper.Validation;
import com.kinklink.modules.authentication.activity.ForgotPasswordActivity;
import com.kinklink.modules.authentication.model.RegistrationInfo;
import com.kinklink.modules.matches.activity.ActivityNotificationSetting;
import com.kinklink.modules.matches.activity.MyProfileActivity;
import com.kinklink.modules.matches.activity.TermsConditionActivity;
import com.kinklink.server_task.WebService;
import com.kinklink.session.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends Fragment implements View.OnClickListener {
    private Context mContext;
    private TextView action_bar_heading, btn_change_password;
    private RelativeLayout rl_change_password, rl_terms_conditions, rl_logout, rl_delete_ac,rl_notifications_setting;
    private Session session;
    private Dialog changePassword, logoutPassword;
    private EditText ed_old_password, ed_new_password, ed_confirm_password;
    private LinearLayout ly_forgot_password, ly_no_network;
    private CardView cv_settings;
    private String userId;
    private TextView btn_cancel;
    private TextView btn_alert;
    private Progress progress;
    private LinearLayout ly_change_password;

    // variable to track event time
    private long mLastClickTime = 0;


    public SettingsFragment() {
    }

    public static SettingsFragment newInstance() {
        Bundle args = new Bundle();
        SettingsFragment fragment = new SettingsFragment();
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
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        session = new Session(mContext);
        init(view);
        action_bar_heading.setText(getString(R.string.settings));
        progress = new Progress(mContext);

        userId = session.getRegistration().userDetail.userId;

        if (!session.getRegistration().userDetail.social_id.equals("")) {
            ly_change_password.setVisibility(View.GONE);
        } else {
            ly_change_password.setVisibility(View.VISIBLE);
        }

        // Click listeners
        rl_logout.setOnClickListener(this);
        rl_delete_ac.setOnClickListener(this);
        rl_notifications_setting.setOnClickListener(this);
        rl_change_password.setOnClickListener(this);
        rl_terms_conditions.setOnClickListener(this);
        //  rl_subscription.setOnClickListener(this);
        //  rl_verify_photo.setOnClickListener(this);
        return view;
    }

    private void init(View view) {

        action_bar_heading = ((MyProfileActivity) mContext).findViewById(R.id.action_bar_heading);

        ImageView iv_settings = ((MyProfileActivity) mContext).findViewById(R.id.iv_settings);
        iv_settings.setVisibility(View.GONE);

        ImageView iv_back = ((MyProfileActivity) mContext).findViewById(R.id.iv_back);
        iv_back.setOnClickListener(this);

        ly_change_password = view.findViewById(R.id.ly_change_password);
        rl_change_password = view.findViewById(R.id.rl_change_password);
        rl_terms_conditions = view.findViewById(R.id.rl_terms_conditions);
        //   rl_subscription = view.findViewById(R.id.rl_subscription);
        //  rl_verify_photo = view.findViewById(R.id.rl_verify_photo);
        rl_logout = view.findViewById(R.id.rl_logout);
        rl_delete_ac = view.findViewById(R.id.rl_delete_ac);
        rl_notifications_setting=view.findViewById(R.id.rl_notifications_setting);

        cv_settings = view.findViewById(R.id.cv_settings);
        ly_no_network = view.findViewById(R.id.ly_no_network);

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
            /*case R.id.rl_subscription:
                CustomToast.getInstance(mContext).showToast(mContext, "Under Development");
                break;*/

            case R.id.iv_back:
                ((MyProfileActivity) mContext).onBackPressed();
                break;

            case R.id.rl_logout:   // Logout Option Click
                rl_logout.setEnabled(false);
                callLogOutApi();   // Call Logout Api
                break;

            case R.id.rl_delete_ac:   // Delete account

                if (session.getRegistration().userDetail.social_id.equals("")) {
                    deleteConfirmationDialog();

                } else {
                    deleteSocialAccountConfirmationDialog();
                }

                //
                break;


            case R.id.rl_notifications_setting:   // Delete account

                startActivity(new Intent(mContext,ActivityNotificationSetting.class));
                //
                break;

            /*case R.id.rl_verify_photo:
                progress.dismiss();
                ((MyProfileActivity) mContext).addFragment(VerifyPhotoFragment.newInstance(), true, R.id.fragment_place);
                break;*/

            case R.id.rl_terms_conditions:
                startActivity(new Intent(mContext, TermsConditionActivity.class));
                break;

            case R.id.rl_change_password:
                openChangePasswordDialog();   // Open Dialog for Change Password
                break;

            case R.id.btn_change_password:
                if (AppHelper.isConnectingToInternet(mContext)) {
                    if (isPasswordValid()) {
                        String old_pass = ed_old_password.getText().toString().trim();
                        String new_pass = ed_new_password.getText().toString().trim();

                        // Call Change Password Api
                        callChangePasswordApi(old_pass, new_pass);
                    }
                } else {
                    changePassword.dismiss();
                    CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.alert_no_network));
                }
                break;

            case R.id.btn_cancel:  // allow to logout on change password Yes button click
                logoutPassword.dismiss();
                // Call Logout Api when click on yes button
                callLogOutApi();
                break;

            case R.id.btn_alert:  // Keep login on change password
                logoutPassword.dismiss();
                break;

            case R.id.ly_forgot_password:  // Start Forgot Password Api
                changePassword.dismiss();
                startActivity(new Intent(mContext, ForgotPasswordActivity.class));
                break;
        }
    }

    // Call Logout Api
    private void callLogOutApi() {
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
                            cv_settings.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);
                            FirebaseDatabase.getInstance().getReference().child(Constant.USER_TABLE).child(userId).child("firebaseToken").setValue("");
                            FirebaseAuth.getInstance().signOut();

                            NotificationManager nManager = ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE));
                            assert nManager != null;
                            nManager.cancelAll();


                            Utils.goToOnlineStatus(mContext, Constant.OFFLINE_STATUS);
                            session.logout();

                        } else {
                            progress.dismiss();
                            cv_settings.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                            rl_logout.setEnabled(true);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        rl_logout.setEnabled(true);
                        cv_settings.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    rl_logout.setEnabled(true);
                    cv_settings.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                }

            });
            api.callApi("user/logout", Request.Method.GET, null);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            assert view != null;
            TextView btn_try_again = view.findViewById(R.id.btn_try_again);
            cv_settings.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callLogOutApi();
                }
            });
        }
    }

    // Delete Account Api

    private void callDeleteAccountApi() {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();
            String pass = "";
            if (session.getRegistration().userDetail.social_id.equals(""))
                pass = session.getLoginPassword();

            final Map<String, String> map = new HashMap<>();
            map.put("password", pass);

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            cv_settings.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);
                            FirebaseDatabase.getInstance().getReference().child(Constant.USER_TABLE).child(userId).child("firebaseToken").setValue("");
                            FirebaseDatabase.getInstance().getReference().child(Constant.USER_TABLE).child(userId).child("isUserDeleted").setValue(1);

                            FirebaseAuth.getInstance().signOut();

                            NotificationManager nManager = ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE));
                            assert nManager != null;
                            nManager.cancelAll();

                            CustomToast.getInstance(mContext).showToast(mContext, message);
                            Utils.goToOnlineStatus(mContext, Constant.OFFLINE_STATUS);
                            session.logout();

                        } else {
                            progress.dismiss();
                            cv_settings.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                            rl_logout.setEnabled(true);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        rl_logout.setEnabled(true);
                        cv_settings.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    rl_logout.setEnabled(true);
                    cv_settings.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                }

            });
            api.callApi("user/deleteAccount", Request.Method.POST, map);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            assert view != null;
            TextView btn_try_again = view.findViewById(R.id.btn_try_again);
            cv_settings.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callDeleteAccountApi();
                }
            });
        }
    }


    // Call Change Password Api
    private void callChangePasswordApi(final String old_pass, final String new_pass) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            final Map<String, String> map = new HashMap<>();
            map.put("oldPassword", old_pass);
            map.put("newPassword", new_pass);

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            cv_settings.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            session.setLoginPassword(new_pass);
                            changePassword.dismiss();

                            openLogoutPasswordDialog();
                        } else {
                            progress.dismiss();
                            cv_settings.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        cv_settings.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    cv_settings.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                }

            });
            api.callApi("user/changePassword", Request.Method.POST, map);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            assert view != null;
            TextView btn_try_again = view.findViewById(R.id.btn_try_again);
            cv_settings.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Change Password Api
                    callChangePasswordApi(old_pass, new_pass);
                }
            });
        }
    }

    // Open Dialog to confirm logout on change password
    private void openLogoutPasswordDialog() {
        logoutPassword = new Dialog(mContext);
        logoutPassword.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        logoutPassword.setContentView(R.layout.dialog_custom_alert);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(logoutPassword.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        logoutPassword.getWindow().setAttributes(lWindowParams);

        initLogoutPasswordDialog();

        ImageView dialog_decline_button = logoutPassword.findViewById(R.id.dialog_decline_button);
        dialog_decline_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutPassword.dismiss();
            }
        });

        btn_cancel.setOnClickListener(this);
        btn_alert.setOnClickListener(this);

        logoutPassword.getWindow().setGravity(Gravity.CENTER);
        logoutPassword.show();
    }

    private void initLogoutPasswordDialog() {
        TextView alert_message = logoutPassword.findViewById(R.id.alert_message);
        btn_cancel = logoutPassword.findViewById(R.id.btn_cancel);
        btn_alert = logoutPassword.findViewById(R.id.btn_alert);

        alert_message.setText(getString(R.string.logout_dialog_message));
        btn_cancel.setText(getString(R.string.dialog_yes_btn));
        btn_alert.setText(getString(R.string.dialog_no_btn));
    }

    // Validations to check password fields valid in change password
    private boolean isPasswordValid() {
        String old_password = ed_old_password.getText().toString().trim();
        String new_password = ed_new_password.getText().toString().trim();
        String con_password = ed_confirm_password.getText().toString().trim();
        Validation v = new Validation();

        if (!v.isEditNull(ed_old_password)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.old_password_null));
            return false;
        } else if (!v.isPasswordValid(ed_old_password)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_pass_invalid));
            return false;
        } else if (!v.isPassExceedMax(ed_old_password)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_pass_exceed_max_len));
            return false;
        } else if (!old_password.equals(session.getLoginPassword())) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.old_login_pass_not_match));
            return false;
        } else if (!v.isEditNull(ed_new_password)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.new_password_null));
            return false;
        } else if (!v.isPasswordValid(ed_new_password)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_pass_invalid));
            return false;
        } else if (!v.isPassExceedMax(ed_new_password)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_pass_exceed_max_len));
            return false;
        } else if (old_password.equals(new_password)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.old_new_pass_cannot_match));
            return false;
        } else if (!v.isEditNull(ed_confirm_password)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_con_pass_null));
            return false;
        } else if (!new_password.equals(con_password)) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_con_pass_not_match));
            return false;
        }

        return true;
    }

    // Open Change Password Dialog
    private void openChangePasswordDialog() {
        changePassword = new Dialog(mContext);
        changePassword.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        changePassword.setContentView(R.layout.dialog_change_password);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(changePassword.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        changePassword.getWindow().setAttributes(lWindowParams);

        initChangePasswordDialog();

        ImageView dialog_decline_button = changePassword.findViewById(R.id.dialog_decline_button);
        dialog_decline_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword.dismiss();
            }
        });

        btn_change_password.setOnClickListener(this);
        ly_forgot_password.setOnClickListener(this);

        changePassword.getWindow().setGravity(Gravity.CENTER);
        changePassword.show();
    }

    private void initChangePasswordDialog() {
        ed_old_password = changePassword.findViewById(R.id.ed_old_password);
        ed_new_password = changePassword.findViewById(R.id.ed_new_password);
        ed_confirm_password = changePassword.findViewById(R.id.ed_confirm_password);

        btn_change_password = changePassword.findViewById(R.id.btn_change_password);
        ly_forgot_password = changePassword.findViewById(R.id.ly_forgot_password);

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


    // Dialog to confirm delete account
    private void deleteConfirmationDialog() {
        final Dialog legalDialog = new Dialog(mContext);
        legalDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        legalDialog.setCancelable(false);
        legalDialog.setContentView(R.layout.delete_account_alert_dialog);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(legalDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        legalDialog.getWindow().setAttributes(lWindowParams);

        TextView btn_i_agree = legalDialog.findViewById(R.id.btn_i_agree);
        final EditText edit_password = legalDialog.findViewById(R.id.edit_password);


        TextView alert_message = legalDialog.findViewById(R.id.alert_message);
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

                String txt_password = edit_password.getText().toString().trim();
                if (txt_password.length() < 8) {
                    CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.login_password_min_length));
                } else {
                    String password = session.getLoginPassword();
                    if (password.equals(txt_password)) {
                        legalDialog.dismiss();
                        callDeleteAccountApi();
                    } else {
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.pass_not_matched));
                    }

                }


            }
        });

        legalDialog.getWindow().setGravity(Gravity.CENTER);
        legalDialog.show();
    }

    // Dialog to confirm delete social account
    private void deleteSocialAccountConfirmationDialog() {
        final Dialog legalDialog = new Dialog(mContext);
        legalDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        legalDialog.setCancelable(false);
        legalDialog.setContentView(R.layout.delete_social_account_alert_dialog);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(legalDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        legalDialog.getWindow().setAttributes(lWindowParams);

        TextView btn_i_agree = legalDialog.findViewById(R.id.btn_i_agree);
        TextView btn_i_cancel = legalDialog.findViewById(R.id.btn_i_cancel);


        TextView alert_message = legalDialog.findViewById(R.id.alert_message);
        alert_message.setMovementMethod(new ScrollingMovementMethod());


        btn_i_cancel.setOnClickListener(new View.OnClickListener() {
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

        btn_i_agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                callDeleteAccountApi();


            }
        });

        legalDialog.getWindow().setGravity(Gravity.CENTER);
        legalDialog.show();
    }






}
