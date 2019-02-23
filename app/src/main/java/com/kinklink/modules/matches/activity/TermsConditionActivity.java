package com.kinklink.modules.matches.activity;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.kinklink.R;
import com.kinklink.application.KinkLink;
import com.kinklink.helper.AppHelper;
import com.kinklink.helper.CustomToast;
import com.kinklink.helper.Progress;
import com.kinklink.server_task.WebService;

import org.json.JSONException;
import org.json.JSONObject;

public class TermsConditionActivity extends AppCompatActivity implements View.OnClickListener {
    private RelativeLayout rl_terms_conditions;
    private LinearLayout ly_no_network;
    private Progress progress;
    private TextView btn_try_again;
    private WebView wv_terms_conditions;
    private ImageView iv_back;

    // variable to track event time
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_condition);
        progress = new Progress(TermsConditionActivity.this);

        init();
        getTermsAndConditionUrl();
        iv_back.setOnClickListener(this);
    }

    private void init() {
        rl_terms_conditions = findViewById(R.id.rl_terms_conditions);
        ly_no_network = findViewById(R.id.ly_no_network);
        btn_try_again = findViewById(R.id.btn_try_again);
        wv_terms_conditions = findViewById(R.id.wv_terms_conditions);
        iv_back = findViewById(R.id.iv_back);
    }

    private void getTermsAndConditionUrl() {
        if (AppHelper.isConnectingToInternet(TermsConditionActivity.this)) {
            progress.show();
            WebService api = new WebService(TermsConditionActivity.this, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            JSONObject contentObject = js.getJSONObject("Content");
                            String termsPolicyUrl = contentObject.getString("term_and_condition");

                            wv_terms_conditions.getSettings().setBuiltInZoomControls(true);
                            wv_terms_conditions.getSettings().setSupportZoom(true);
                            wv_terms_conditions.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                            wv_terms_conditions.getSettings().setJavaScriptEnabled(true);
                            wv_terms_conditions.getSettings().setAllowFileAccess(true);
                            wv_terms_conditions.getSettings().setDomStorageEnabled(true);
                            wv_terms_conditions.getSettings().setDefaultZoom(WebSettings.ZoomDensity.CLOSE);
                            wv_terms_conditions.getSettings().setLoadWithOverviewMode(true);
                            wv_terms_conditions.getSettings().setUseWideViewPort(true);
                            wv_terms_conditions.getSettings().setPluginState(WebSettings.PluginState.ON);
                            wv_terms_conditions.getSettings().setAllowContentAccess(true);
                            wv_terms_conditions.loadUrl("https://docs.google.com/gview?embedded=true&url=" + termsPolicyUrl);

                            rl_terms_conditions.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            progress.dismiss();

                        } else {
                            progress.dismiss();
                            CustomToast.getInstance(TermsConditionActivity.this).showToast(TermsConditionActivity.this, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        rl_terms_conditions.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);
                        CustomToast.getInstance(TermsConditionActivity.this).showToast(TermsConditionActivity.this, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    rl_terms_conditions.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                }

            });
            api.callApi("getContent", Request.Method.GET, null);
        } else {

            rl_terms_conditions.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(this);
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
            case R.id.btn_try_again:
                getTermsAndConditionUrl();
                break;

            case R.id.iv_back:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
