package com.kinklink.modules.authentication.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kinklink.R;
import com.kinklink.application.KinkLink;
import com.kinklink.helper.AppHelper;
import com.kinklink.helper.CustomToast;
import com.kinklink.helper.Progress;
import com.kinklink.modules.authentication.model.RegistrationInfo;
import com.kinklink.server_task.WebService;
import com.kinklink.session.Session;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityEmailVarification extends AppCompatActivity implements View.OnClickListener {

    Context mContext;
    private Progress progress;
    private LinearLayout ly_no_network;
    private TextView alert_message, btn_varified;
    private TextView btn_try_again;
    private Session session;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        progress = new Progress(mContext);
        setContentView(R.layout.activity_emailvarification);
        initViews();
    }


    void initViews() {
        btn_try_again = findViewById(R.id.btn_try_again);
        ly_no_network = findViewById(R.id.ly_no_network);
        alert_message = findViewById(R.id.alert_message);
        btn_varified = findViewById(R.id.btn_varified);
        String alert_msg = getResources().getString(R.string.email_sent_msg);
        String alert_append_msg = alert_msg + " <font color='#cf1f2a'>" + getResources().getString(R.string.resend_str) + "</font>";
        alert_message.setText(Html.fromHtml(alert_append_msg));

        alert_message.setOnClickListener(this);
        btn_varified.setOnClickListener(this);
        session = new Session(mContext);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.alert_message:
                task_ReSendEmailForVarification();
                break;

            case R.id.btn_varified:
                task_CheckIsEmailVerified();
                break;
        }
    }


    private void task_CheckIsEmailVerified() {

        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            String device_token = FirebaseInstanceId.getInstance().getToken();

            final Map<String, String> map = new HashMap<>();
            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {

                @Override
                public void onResponse(String responce, String url) {
                    progress.dismiss();
                    try {
                        JSONObject js = new JSONObject(responce);
                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {

                            String isEmailVerified = js.getString("isEmailVerified");
                            if(isEmailVerified.equals("1")){
                                RegistrationInfo registrationInfo =session.getRegistration();
                                registrationInfo.userDetail.isEmailVerified="1";
                                session.createRegistration(registrationInfo);
                                Intent intent = new Intent(mContext, EditProfileActivity.class);
                                startActivity(intent);
                                finish();
                            }

                            else{
                                CustomToast.getInstance(mContext).showToast(mContext, message);
                            }


                        } else {
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                }
            });

            api.callApi("user/getUserVerifiedStatus", Request.Method.GET, map);

        } else {
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (AppHelper.isConnectingToInternet(mContext)) {
                        ly_no_network.setVisibility(View.GONE);
                        task_CheckIsEmailVerified();
                    }
                }
            });
        }

    }

    private void task_ReSendEmailForVarification() {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            final Map<String, String> map = new HashMap<>();
            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {

                @Override
                public void onResponse(String responce, String url) {
                    progress.dismiss();
                    try {
                        JSONObject js = new JSONObject(responce);
                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                        } else {
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                }
            });

            api.callApi("user/resendEmailVerifyLink", Request.Method.GET, map);

        } else {
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (AppHelper.isConnectingToInternet(mContext)) {
                        ly_no_network.setVisibility(View.GONE);
                        task_ReSendEmailForVarification();
                    }
                }
            });
        }
    }
}
