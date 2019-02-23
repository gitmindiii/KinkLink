package com.kinklink.modules.authentication.activity;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.kinklink.R;
import com.kinklink.application.KinkLink;
import com.kinklink.helper.AppHelper;
import com.kinklink.helper.Constant;
import com.kinklink.helper.CustomToast;

import com.kinklink.helper.Progress;
import com.kinklink.helper.Utils;
import com.kinklink.helper.Validation;
import com.kinklink.server_task.WebService;
import com.kinklink.session.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText ed_email_id;
    private TextView btn_forgot_pass, btn_try_again;
    private CardView cv_forgot_password;
    private LinearLayout ly_no_network;
    private ImageView iv_back;
    private Session session;
    private Progress progress;

    // variable to track event time
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        init();
        progress = new Progress(ForgotPasswordActivity.this);
        session = new Session(ForgotPasswordActivity.this);

        iv_back.setOnClickListener(this);
        btn_forgot_pass.setOnClickListener(this);
    }

    private void init() {
        ed_email_id = findViewById(R.id.ed_email_id);
        btn_forgot_pass = findViewById(R.id.btn_forgot_pass);
        cv_forgot_password = findViewById(R.id.cv_forgot_password);
        ly_no_network = findViewById(R.id.ly_no_network);
        btn_try_again = findViewById(R.id.btn_try_again);
        iv_back = findViewById(R.id.iv_back);
    }

    // Validations for forgot password
    private boolean isValid() {
        Validation v = new Validation();

        if (!v.isEditNull(ed_email_id)) {
            CustomToast.getInstance(this).showToast(this, getString(R.string.login_email_null));
            return false;
        } else if (!v.isEmailValid(ed_email_id)) {
            CustomToast.getInstance(this).showToast(this, getString(R.string.login_email_invalid));
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (v.getId()) {
            case R.id.btn_forgot_pass:
                // Validations for forgot password
                if (isValid()) {
                    String email = ed_email_id.getText().toString().trim();

                    // Call Forgot Password Api
                    callForgotPasswordApi(email);
                }
                break;

            case R.id.iv_back:
                finish();
                break;
        }
    }

    // Call Forgot Password Api
    private void callForgotPasswordApi(final String email) {
        if (AppHelper.isConnectingToInternet(this)) {
            progress.show();

            Map<String, String> map = new HashMap<>();
            map.put("email", email);

            WebService api = new WebService(this, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            CustomToast.getInstance(ForgotPasswordActivity.this).showToast(ForgotPasswordActivity.this, message);

                            if (session.getUserLoggedIn()) {
                                callLogOutApi();    // Call Logout Api when user is logged in
                            } else {
                                finish();
                            }
                        } else {
                            progress.dismiss();
                            CustomToast.getInstance(ForgotPasswordActivity.this).showToast(ForgotPasswordActivity.this, message);
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
            api.callApi("forgotPassword", Request.Method.POST, map);
        } else {
            View view = this.getCurrentFocus();
            if (view != null) {
                AppHelper.hideKeyboard(view, ForgotPasswordActivity.this);
            }

            cv_forgot_password.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Preventing multiple clicks, using threshold of 1/2 second
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    // Forgot Password Api
                    callForgotPasswordApi(email);
                }
            });
        }
    }

    // Call Logout Api
    private void callLogOutApi() {
        if (AppHelper.isConnectingToInternet(this)) {
            progress.show();

            WebService api = new WebService(this, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            cv_forgot_password.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            Utils.goToOnlineStatus(ForgotPasswordActivity.this, Constant.OFFLINE_STATUS);
                            session.logout();

                        } else {
                            progress.dismiss();
                            CustomToast.getInstance(ForgotPasswordActivity.this).showToast(ForgotPasswordActivity.this, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        CustomToast.getInstance(ForgotPasswordActivity.this).showToast(ForgotPasswordActivity.this, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                }

            });
            api.callApi("user/logout", Request.Method.GET, null);
        } else {

            TextView btn_try_again = findViewById(R.id.btn_try_again);
            cv_forgot_password.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Preventing multiple clicks, using threshold of 1/2 second
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    callLogOutApi();
                }
            });
        }
    }
}
