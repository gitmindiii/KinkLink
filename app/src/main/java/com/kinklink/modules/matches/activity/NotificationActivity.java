package com.kinklink.modules.matches.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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
import com.kinklink.modules.authentication.activity.KinkLinkParentActivity;
import com.kinklink.modules.authentication.listener.AdapterPositionListener;
import com.kinklink.modules.matches.adapter.NotificationAdapter;
import com.kinklink.modules.matches.listener.EndlessRecyclerViewScrollListener;
import com.kinklink.modules.matches.model.NotificationModel;
import com.kinklink.server_task.WebService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NotificationActivity extends KinkLinkParentActivity implements View.OnClickListener {
    private RelativeLayout rl_notifications;
    private LinearLayout ly_no_network, ly_no_notifications;
    private ImageView iv_back;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView notifications_recycler_view;
    private Progress progress;
    private int offsetLimit = 0;
    private ArrayList<NotificationModel> notificationsList;
    private NotificationAdapter notificationAdapter;

    // variable to track event time
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list);
        init();
        progress = new Progress(NotificationActivity.this);
        notificationsList = new ArrayList<>();

        getNotificationsList();

        setNotificationsListAdapter();

        iv_back.setOnClickListener(this);
    }

    private void init() {
        rl_notifications = findViewById(R.id.rl_notifications);
        ly_no_network = findViewById(R.id.ly_no_network);
        iv_back = findViewById(R.id.iv_back);
        ly_no_notifications = findViewById(R.id.ly_no_notifications);
        ImageView iv_no_record = findViewById(R.id.iv_no_record);
        TextView tv_no_record_txt = findViewById(R.id.tv_no_record_txt);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        notifications_recycler_view = findViewById(R.id.notifications_recycler_view);

        iv_no_record.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.no_notifications_icon));
        tv_no_record_txt.setText(getString(R.string.no_unread_notifications));
    }

    //Setting adapter to get Likes List
    private void setNotificationsListAdapter() {
        // Notifications List recycler adapter
        notificationAdapter = new NotificationAdapter(NotificationActivity.this, notificationsList, new AdapterPositionListener() {
            @Override
            public void getPosition(int position) {
                //callReadNotificationApi(notificationsList.get(position).id);

                Intent intent = new Intent(NotificationActivity.this, MainActivity.class);
                intent.putExtra("type", notificationsList.get(position).notification_type);
                intent.putExtra("uid",notificationsList.get(position).notification_by);
                intent.putExtra("notify_for",notificationsList.get(position).notification_by);
                intent.putExtra("list_type", notificationsList.get(position).list_type);
                intent.putExtra("notification_id",notificationsList.get(position).id);
                startActivity(intent);
                finishAffinity();
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(NotificationActivity.this);
        notifications_recycler_view.setLayoutManager(layoutManager);
        notifications_recycler_view.setAdapter(notificationAdapter);

        // Pull To refresh listener
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // cancel the Visual indication of a refresh
                swipeRefreshLayout.setRefreshing(false);
                offsetLimit = 0;
                notificationsList.clear();
                notificationAdapter.notifyDataSetChanged();
                KinkLink.getInstance().cancelAllRequests(KinkLink.TAG);
                getNotificationsList();
            }
        });

        // Endless Recycler Scroll Listener Pagination
        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                getNotificationsList();
            }
        };

        // Adds the scroll listener to RecyclerView
        notifications_recycler_view.addOnScrollListener(scrollListener);
    }

    private void callReadNotificationApi(final String id) {
        if (AppHelper.isConnectingToInternet(NotificationActivity.this)) {
            progress.show();

            final Map<String, String> map = new HashMap<>();
            map.put("notifyId", id);

            WebService api = new WebService(NotificationActivity.this, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            rl_notifications.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);
                            ly_no_notifications.setVisibility(View.GONE);
                        } else {
                            progress.dismiss();
                            CustomToast.getInstance(NotificationActivity.this).showToast(NotificationActivity.this, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        CustomToast.getInstance(NotificationActivity.this).showToast(NotificationActivity.this, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                }

            });
            api.callApi("user/readNotification", Request.Method.POST, map);
        } else {
            rl_notifications.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            TextView btn_try_again = findViewById(R.id.btn_try_again);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Preventing multiple clicks, using threshold of 1/2 second
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    callReadNotificationApi(id);
                }
            });
        }
    }


    // Get Notifications List Api
    private void getNotificationsList() {
        if (AppHelper.isConnectingToInternet(NotificationActivity.this)) {
            progress.show();
            WebService api = new WebService(NotificationActivity.this, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");

                        if (status.equals("success")) {
                            progress.dismiss();

                            JSONArray jsonArray = js.getJSONArray("notificationList");
                            NotificationModel model;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                model = new NotificationModel();
                                JSONObject object = jsonArray.getJSONObject(i);

                                model.userId = object.getString("userId");
                                model.full_name = object.getString("full_name");
                                model.id = object.getString("id");
                                model.notification_by = object.getString("notification_by");
                                model.notification_for = object.getString("notification_for");
                                model.reference_id = object.getString("reference_id");
                                model.notification_type = object.getString("notification_type");
                                model.notification_message = object.getString("notification_message");

                                JSONObject jsonObject = new JSONObject(object.getString("notification_message"));

                                model.title = jsonObject.getString("title");
                                model.body = jsonObject.getString("body");
                                model.list_type = jsonObject.getString("list_type");

                                model.is_read = object.getString("is_read");
                                model.web_notify = object.getString("web_notify");
                                model.status = object.getString("status");
                                model.created_on = object.getString("created_on");
                                model.cur_time = object.getString("cur_time");
                                model.image = object.getString("image");

                                notificationsList.add(model);
                            }

                            rl_notifications.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);
                            ly_no_notifications.setVisibility(View.GONE);
                            offsetLimit += 10;
                            notificationAdapter.notifyDataSetChanged();
                        } else {
                            progress.dismiss();
                            if (notificationsList.size() == 0) {
                                notifications_recycler_view.setVisibility(View.GONE);
                                ly_no_notifications.setVisibility(View.VISIBLE);
                                rl_notifications.setVisibility(View.VISIBLE);
                                ly_no_network.setVisibility(View.GONE);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        CustomToast.getInstance(NotificationActivity.this).showToast(NotificationActivity.this, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                }

            });
            api.callApi("user/getNotificationList?offset=" + offsetLimit + "&limit=10", Request.Method.GET, null);
        } else {
            rl_notifications.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            TextView btn_try_again = findViewById(R.id.btn_try_again);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Preventing multiple clicks, using threshold of 1/2 second
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    getNotificationsList();
                }
            });
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
            case R.id.iv_back:
                onBackPressed();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
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
