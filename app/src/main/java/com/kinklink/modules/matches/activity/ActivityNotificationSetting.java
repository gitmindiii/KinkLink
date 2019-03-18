package com.kinklink.modules.matches.activity;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kinklink.R;
import com.kinklink.application.KinkLink;
import com.kinklink.helper.AppHelper;
import com.kinklink.helper.Constant;
import com.kinklink.modules.authentication.model.RegistrationInfo;
import com.kinklink.server_task.WebService;
import com.kinklink.session.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityNotificationSetting extends AppCompatActivity {

    Context mContext;
    private Session session;
    TextView action_bar_heading;
    ImageView iv_back;
    SwitchCompat switch_favorite, switch_tease, switch_profile, switch_chat, switch_photo_verify;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_setting);
        mContext=this;
        session = new Session(mContext);
        initViews();
    }

    public void initViews(){
        iv_back=findViewById(R.id.iv_back);
        iv_back.setVisibility(View.VISIBLE);
        action_bar_heading=findViewById(R.id.action_bar_heading);
        action_bar_heading.setText("Notification Setting");
        switch_favorite = findViewById(R.id.switch_favorite);
        switch_tease = findViewById(R.id.switch_tease);
        switch_profile = findViewById(R.id.switch_profile);
        switch_chat = findViewById(R.id.switch_chat);
        switch_photo_verify = findViewById(R.id.switch_photo_verify);
        setNotificationSwitch();

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // notification on/off switch
    public void setNotificationSwitch() {
        String favorite_update = session.getRegistration().userDetail.favorite_update;
        String tease_update = session.getRegistration().userDetail.tease_update;
        String view_update = session.getRegistration().userDetail.view_update;
        String chat_update = session.getRegistration().userDetail.chat_update;
        String verify_update = session.getRegistration().userDetail.verify_update;

        switch_favorite.setChecked((favorite_update!=null&&favorite_update.equals("1")) ? true : false);
        switch_tease.setChecked((tease_update!=null&&tease_update.equals("1")) ? true : false);
        switch_profile.setChecked((view_update!=null&&view_update.equals("1")) ? true : false);
        switch_chat.setChecked((chat_update!=null&&chat_update.equals("1")) ? true : false);
        switch_photo_verify.setChecked((verify_update!=null&&verify_update.equals("1")) ? true : false);

        switch_favorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    callNotificationOnOffApi("favorite_update", "1");
                else callNotificationOnOffApi("favorite_update", "0");
            }
        });
        switch_tease.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    callNotificationOnOffApi("tease_update", "1");
                else callNotificationOnOffApi("tease_update", "0");
            }
        });
        switch_profile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    callNotificationOnOffApi("view_update", "1");
                else callNotificationOnOffApi("view_update", "0");
            }
        });

        switch_chat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    callNotificationOnOffApi("chat_update", "1");
                else callNotificationOnOffApi("chat_update", "0");
            }
        });

        switch_photo_verify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    callNotificationOnOffApi("verify_update", "1");
                else callNotificationOnOffApi("verify_update", "0");
            }
        });
    }

    private void callNotificationOnOffApi(final String key, final String value) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            final Map<String, String> map = new HashMap<>();
            map.put("notification_type", key);
            map.put("value", value);

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            RegistrationInfo registrationInfo = session.getRegistration();
                            if (key.equals("favorite_update"))
                                registrationInfo.userDetail.favorite_update = value;
                            else if (key.equals("tease_update"))
                                registrationInfo.userDetail.tease_update = value;
                            else if (key.equals("view_update"))
                                registrationInfo.userDetail.view_update = value;
                            else if (key.equals("chat_update")){
                                String userId=session.getRegistration().userDetail.userId;
                                String device_token = FirebaseInstanceId.getInstance().getToken();
                                if(value.equals("1"))
                                    FirebaseDatabase.getInstance().getReference().child(Constant.USER_TABLE).child(userId).child("firebaseToken").setValue(device_token);
                                else
                                   FirebaseDatabase.getInstance().getReference().child(Constant.USER_TABLE).child(userId).child("firebaseToken").setValue("");
                                registrationInfo.userDetail.chat_update = value;
                            }

                            else if (key.equals("verify_update"))
                                registrationInfo.userDetail.verify_update = value;
                            session.createRegistration(registrationInfo);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();

                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {


                }

            });
            api.callApi("user/setNotificationPrefrence", Request.Method.POST, map);
        }
    }
}
