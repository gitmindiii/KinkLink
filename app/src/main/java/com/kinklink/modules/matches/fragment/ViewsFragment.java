package com.kinklink.modules.matches.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kinklink.R;
import com.kinklink.application.KinkLink;
import com.kinklink.helper.AppHelper;
import com.kinklink.helper.Constant;
import com.kinklink.helper.CustomToast;
import com.kinklink.helper.Progress;
import com.kinklink.modules.authentication.model.OnlineInfo;
import com.kinklink.modules.matches.activity.MainActivity;
import com.kinklink.modules.matches.activity.MyProfileActivity;
import com.kinklink.modules.matches.adapter.ViewedMeAdapter;
import com.kinklink.modules.matches.adapter.YouViewedAdapter;
import com.kinklink.modules.matches.listener.EndlessRecyclerViewScrollListener;
import com.kinklink.modules.matches.model.ViewedMeModel;
import com.kinklink.server_task.WebService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewsFragment extends Fragment implements View.OnClickListener {
    private Context mContext;
    private TextView action_bar_heading, btn_try_again, tv_who_i_viewed, tv_viewed_me;
    private RecyclerView viewed_recycler_view;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout ly_no_network, ly_no_viewed;
    private YouViewedAdapter youViewedAdapter;
    private ViewedMeAdapter viewedMeAdapter;
    private int offsetLimit = 0;
    private int tab_type = 1;
    private LinearLayout ly_view_list;
    private ArrayList<ViewedMeModel> viewedMeList, youViewedList;
    private Map<String, OnlineInfo> onlineList;
    private Progress progress;
    private ImageView iv_profile, iv_teases;
    private int activeFragId;
    private EndlessRecyclerViewScrollListener scrollListener;
    FrameLayout lay_viewd_count;

    // variable to track event time
    private long mLastClickTime = 0;

    public ViewsFragment() {
    }

    public static ViewsFragment newInstance() {
        Bundle args = new Bundle();
        ViewsFragment fragment = new ViewsFragment();
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
        View view = inflater.inflate(R.layout.fragment_views, container, false);
        init(view);

        progress = new Progress(mContext);
        viewedMeList = new ArrayList<>();
        youViewedList = new ArrayList<>();
        onlineList = new HashMap<>();

        action_bar_heading.setText(getString(R.string.views));

        // Setting Who You View tab active
        activeFragId = R.id.tv_viewed_me;
        tab_type = 1;

        // call view count read API
        taskReadViewCount();
        // Set Tab Panel
        setUpTabPanel();

        // Click Listeners
        iv_profile.setOnClickListener(this);
        iv_teases.setOnClickListener(this);
        // rl_notifications.setOnClickListener(this);
        tv_who_i_viewed.setOnClickListener(this);
        tv_viewed_me.setOnClickListener(this);

        return view;
    }

    // Set Tab Panel
    private void setUpTabPanel() {
        if (tab_type == 0) {
            // Get You View List
            callGetYouViewListApi();

            // Adapter for displaying Who You Viewed list
            setViewsAdapter();

            // Get Online Table data from firebase
            getOnlineStatusFromFirebase();

        } else if (tab_type == 1) {
            // Get Viewed Me List
            callGetViewedMeListApi();

            // Adapter for displaying Who Viewed Me list
            setViewsAdapter();

            // Get Online Table data from firebase
            getOnlineStatusFromFirebase();
        }
    }

    // Setting adapter to get Viewed Me list
    private void setViewsAdapter() {
        if (tab_type == 0) {
            // Who You Viewed List recycler adapter
            youViewedAdapter = new YouViewedAdapter(mContext, youViewedList);
            GridLayoutManager layoutManager = new GridLayoutManager(mContext, 2);
            viewed_recycler_view.setLayoutManager(layoutManager);
            viewed_recycler_view.setAdapter(youViewedAdapter);

            // Endless Recycler Scroll Listener Pagination
            scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    callGetYouViewListApi();
                }
            };

            // Adds the scroll listener to RecyclerView
            viewed_recycler_view.addOnScrollListener(scrollListener);

            // Pull To refresh listener
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    swipeRefreshLayout.setRefreshing(false);
                    offsetLimit = 0;
                    youViewedList.clear();
                    youViewedAdapter.notifyDataSetChanged();
                    scrollListener.resetState();
                    KinkLink.getInstance().cancelAllRequests(KinkLink.TAG);
                    callGetYouViewListApi();
                }
            });

        } else if (tab_type == 1) {
            // Who Viewed Me recycler adapter
            viewedMeAdapter = new ViewedMeAdapter(mContext, viewedMeList);
            GridLayoutManager layoutManager = new GridLayoutManager(mContext, 2);
            viewed_recycler_view.setLayoutManager(layoutManager);
            viewed_recycler_view.setAdapter(viewedMeAdapter);

            // Endless Recycler Scroll Listener Pagination
            final EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    callGetViewedMeListApi();
                }
            };

            // Adds the scroll listener to RecyclerView
            viewed_recycler_view.addOnScrollListener(scrollListener);

            // Pull To refresh listener
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    swipeRefreshLayout.setRefreshing(false);
                    offsetLimit = 0;
                    viewedMeList.clear();
                    viewedMeAdapter.notifyDataSetChanged();
                    KinkLink.getInstance().cancelAllRequests(KinkLink.TAG);
                    callGetViewedMeListApi();
                }
            });
        }
    }

    // Get Online Table data from firebase
    private void getOnlineStatusFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(Constant.ONLINE_TABLE);
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Set Online Offline status in Online Info type list
                if (youViewedAdapter != null || viewedMeAdapter != null) {
                    setOnlineData(dataSnapshot);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // Set Online Offline status in Online Info type list
                if (youViewedAdapter != null || viewedMeAdapter != null) {
                    setOnlineData(dataSnapshot);
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // Set Online Offline status in Online Info type list
                if (youViewedAdapter != null || viewedMeAdapter != null) {
                    setOnlineData(dataSnapshot);
                }

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void init(View view) {

        lay_viewd_count=((MainActivity) mContext).findViewById(R.id.lay_viewd_count);
        action_bar_heading = ((MainActivity) mContext).findViewById(R.id.action_bar_heading);

        ImageView iv_back = ((MainActivity) mContext).findViewById(R.id.iv_back);
        iv_back.setVisibility(View.GONE);

        ImageView iv_settings = ((MainActivity) mContext).findViewById(R.id.iv_settings);
        iv_settings.setVisibility(View.GONE);

        iv_profile = ((MainActivity) mContext).findViewById(R.id.iv_profile);
        iv_profile.setVisibility(View.VISIBLE);

        iv_teases = ((MainActivity) mContext).findViewById(R.id.iv_teases);
        iv_teases.setVisibility(View.GONE);

        RelativeLayout rl_notifications = ((MainActivity) mContext).findViewById(R.id.rl_notifications);
        rl_notifications.setVisibility(View.GONE);

        RelativeLayout bottom_menu = ((MainActivity) mContext).findViewById(R.id.bottomMenu);
        bottom_menu.setVisibility(View.VISIBLE);

        swipeRefreshLayout = view.findViewById(R.id.simpleSwipeRefreshLayout);
        viewed_recycler_view = view.findViewById(R.id.views_recycler_view);
        ly_no_network = ((MainActivity) mContext).findViewById(R.id.ly_no_network);
        btn_try_again = ((MainActivity) mContext).findViewById(R.id.btn_try_again);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        ly_view_list = view.findViewById(R.id.ly_view_list);
        ly_no_viewed = view.findViewById(R.id.ly_no_views);

        ImageView iv_no_record = view.findViewById(R.id.iv_no_record);
        TextView tv_no_record_txt = view.findViewById(R.id.tv_no_record_txt);
        iv_no_record.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_no_browser));
        tv_no_record_txt.setText(getResources().getString(R.string.no_record_found));

        tv_who_i_viewed = view.findViewById(R.id.tv_who_i_viewed);
        tv_viewed_me = view.findViewById(R.id.tv_viewed_me);

        ((MainActivity) mContext).setViewedFragmentActive();
    }

    // Set Online Offline status in Online Info type list
    private void setOnlineData(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue(OnlineInfo.class) != null) {
            OnlineInfo info = dataSnapshot.getValue(OnlineInfo.class);
            onlineList.put(dataSnapshot.getKey(), info);

            for (ViewedMeModel model : viewedMeList) {
                if (dataSnapshot.getKey().equals(model.userId)) {
                    assert info != null;
                    model.isOnline = info.lastOnline;
                }
            }

            if (tab_type == 0 && youViewedAdapter != null) {
                youViewedAdapter.notifyDataSetChanged();
            } else if (tab_type == 1 && viewedMeAdapter != null) {
                viewedMeAdapter.notifyDataSetChanged();
            }
        }
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
            case R.id.iv_profile:
                // ((MainActivity) mContext).addFragment(MyProfileFragment.newInstance(), true, R.id.fragment_place);
                startActivity(new Intent(mContext, MyProfileActivity.class));
                break;

            case R.id.iv_teases:
                ((MainActivity) mContext).addFragment(TeasesFragment.newInstance(), true, R.id.fragment_place);
                break;

           /* case R.id.rl_notifications:
                startActivity(new Intent(mContext, NotificationActivity.class));
                break;*/

            case R.id.tv_who_i_viewed:    // Who You View Tab Click
                if (activeFragId != R.id.tv_who_i_viewed) {
                    activeFragId = R.id.tv_who_i_viewed;
                    tab_type = 0;
                    offsetLimit = 0;
                    tv_who_i_viewed.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                    tv_viewed_me.setTextColor(ContextCompat.getColor(mContext, R.color.field_text_color));

                    // Set Tab Panel
                    setUpTabPanel();

                    youViewedList.clear();
                    youViewedAdapter.notifyDataSetChanged();
                }
                break;

            case R.id.tv_viewed_me:      // Who Viewed Me Tab Click
                if (activeFragId != R.id.tv_viewed_me) {
                    activeFragId = R.id.tv_viewed_me;
                    tab_type = 1;
                    offsetLimit = 0;
                    tv_who_i_viewed.setTextColor(ContextCompat.getColor(mContext, R.color.field_text_color));
                    tv_viewed_me.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));

                    // Set Tab Panel
                    setUpTabPanel();

                    viewedMeList.clear();
                    viewedMeAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    // Api call to get Who You View List
    private void callGetYouViewListApi() {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");

                        if (status.equals("success")) {
                           /* Gson gson = new Gson();
                            ViewedMeModel model = gson.fromJson(response, ViewedMeModel.class);
                            viewedMeList.addAll(model.viewMeList);*/

                            progress.dismiss();

                            JSONArray jsonArray = js.getJSONArray("viewByMeList");
                            ViewedMeModel model;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                model = new ViewedMeModel();
                                JSONObject object = jsonArray.getJSONObject(i);
                                model.userId = object.getString("userId");
                                model.full_name = object.getString("full_name");
                                model.gender = object.getString("gender");
                                model.is_verify = object.getString("is_verify");
                                model.age = object.getString("age");
                                model.image = object.getString("image");

                                //Create iterator on Set

                                for (Object o : onlineList.entrySet()) {
                                    Map.Entry mapEntry = (Map.Entry) o;
                                    // Get Key
                                    String key = (String) mapEntry.getKey();
                                    //Get Value
                                    OnlineInfo onlineInfo = (OnlineInfo) mapEntry.getValue();
                                    String value = onlineInfo.lastOnline;

                                    if (model.userId.equals(key)) {
                                        model.isOnline = value;
                                    }
                                }
                                youViewedList.add(model);
                            }

                            ly_view_list.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            viewed_recycler_view.setVisibility(View.VISIBLE);
                            ly_no_viewed.setVisibility(View.GONE);
                            offsetLimit += 10;
                            youViewedAdapter.notifyDataSetChanged();
                        } else {
                            progress.dismiss();
                            if (youViewedList.size() == 0) {
                                viewed_recycler_view.setVisibility(View.GONE);
                                ly_no_viewed.setVisibility(View.VISIBLE);
                                ly_view_list.setVisibility(View.VISIBLE);
                                ly_no_network.setVisibility(View.GONE);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                }

            });
            api.callApi("user/profileViewByMeList?offset=" + offsetLimit + "&limit=10", Request.Method.GET, null);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            ly_view_list.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Preventing multiple clicks, using threshold of 1/2 second
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    callGetYouViewListApi();
                }
            });
        }
    }

    // Api call to get Who Viewed Me List
    private void callGetViewedMeListApi() {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");

                        if (status.equals("success")) {
                           /* Gson gson = new Gson();
                            ViewedMeModel model = gson.fromJson(response, ViewedMeModel.class);
                            viewedMeList.addAll(model.viewMeList);*/

                            progress.dismiss();

                            JSONArray jsonArray = js.getJSONArray("viewMeList");
                            ViewedMeModel model;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                model = new ViewedMeModel();
                                JSONObject object = jsonArray.getJSONObject(i);
                                model.userId = object.getString("userId");
                                model.full_name = object.getString("full_name");
                                model.gender = object.getString("gender");
                                model.is_verify = object.getString("is_verify");
                                model.age = object.getString("age");
                                model.image = object.getString("image");

                                //Create iterator on Set

                                for (Object o : onlineList.entrySet()) {
                                    Map.Entry mapEntry = (Map.Entry) o;
                                    // Get Key
                                    String key = (String) mapEntry.getKey();
                                    //Get Value
                                    OnlineInfo onlineInfo = (OnlineInfo) mapEntry.getValue();
                                    String value = onlineInfo.lastOnline;

                                    if (model.userId.equals(key)) {
                                        model.isOnline = value;
                                    }
                                }
                                viewedMeList.add(model);
                            }

                            ly_view_list.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            viewed_recycler_view.setVisibility(View.VISIBLE);
                            ly_no_viewed.setVisibility(View.GONE);
                            offsetLimit += 10;
                            viewedMeAdapter.notifyDataSetChanged();
                        } else {
                            progress.dismiss();
                            if (viewedMeList.size() == 0) {
                                viewed_recycler_view.setVisibility(View.GONE);
                                ly_no_viewed.setVisibility(View.VISIBLE);
                                ly_view_list.setVisibility(View.VISIBLE);
                                ly_no_network.setVisibility(View.GONE);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                }

            });
            api.callApi("user/viewMyProfileList?offset=" + offsetLimit + "&limit=10", Request.Method.GET, null);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            ly_view_list.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Preventing multiple clicks, using threshold of 1/2 second
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    callGetViewedMeListApi();
                }
            });
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

    private void taskReadViewCount() {
        if (AppHelper.isConnectingToInternet(mContext)) {
            Map param_map=new HashMap();
            param_map.put("type","view_updates");
            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");
                        if (status.equals("success")) {

                            MainActivity.profileViewCount = 0;
                            lay_viewd_count.setVisibility(View.GONE);

                        } else {
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {

                }

            });
            api.callApi("user/updateStatusToViewed", Request.Method.POST, param_map);
        }
    }
}

