package com.kinklink.modules.matches.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.crystal.crystalrangeseekbar.interfaces.OnSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnSeekbarFinalValueListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.kinklink.R;
import com.kinklink.application.KinkLink;
import com.kinklink.helper.AddressLocationTask;
import com.kinklink.helper.AppHelper;
import com.kinklink.helper.Constant;
import com.kinklink.helper.CustomToast;
import com.kinklink.helper.Progress;
import com.kinklink.helper.Utils;
import com.kinklink.modules.authentication.model.OnlineInfo;
import com.kinklink.modules.matches.activity.MainActivity;
import com.kinklink.modules.matches.activity.MyProfileActivity;
import com.kinklink.modules.matches.adapter.MeetUpAdapter;
import com.kinklink.modules.matches.adapter.MyVisitsAdapter;
import com.kinklink.modules.matches.listener.EndlessRecyclerViewScrollListener;
import com.kinklink.modules.matches.listener.MyVisitPositionListener;
import com.kinklink.modules.matches.model.MeetUpModel;
import com.kinklink.modules.matches.model.MyVisitsModel;
import com.kinklink.server_task.WebService;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TravelKinksFragment extends Fragment implements View.OnClickListener {
    private Context mContext;
    private TextView btn_try_again, tv_city_town, tv_from_date, tv_to_date, tv_meet_up, tv_my_visits, tv_no_record;
    private LinearLayout ly_no_network, ly_no_travel;
    private RelativeLayout rl_main_activity, rl_city_town;
    private RecyclerView travel_recycler_view;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<MeetUpModel> meetUpList;
    private MeetUpAdapter meetUpAdapter;

    private int offsetLimit = 0;
    public static int tab_type = 0;
    private Dialog travel_dialog, deleteVisitsDialog;
    private String fromDate = "", toDate = "", fromDistance = "", toDistance = "";
    private String country, state, mLatitude = "", mLongitude = "", city = "", travelLocation = "", date_type = "", date, visitAddress;
    private DatePickerDialog date_picker;
    private EndlessRecyclerViewScrollListener scrollListener;
    private Map<String, OnlineInfo> onlineList;
    private ImageView iv_meet_up_filter, iv_add_visits, iv_no_record_place_holder;
    private int activeFragId;

    // variable to track event time
    private long mLastClickTime = 0;

    // My Visits declarations
    private ArrayList<MyVisitsModel.VisitListBean> myVisitsList;
    private MyVisitsAdapter visitsAdapter;
    private int dialogType = 0; // 0 for add visit & 1 for edit visit
    private int editVisitPosition;
    private TextView btn_delete_visit;
    private Progress progress;

    public TravelKinksFragment() {
    }

    public static TravelKinksFragment newInstance() {
        Bundle args = new Bundle();
        TravelKinksFragment fragment = new TravelKinksFragment();
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
        View view = inflater.inflate(R.layout.fragment_travel_kinks, container, false);
        init(view);
        progress = new Progress(mContext);
        meetUpList = new ArrayList<>();
        onlineList = new HashMap<>();
        myVisitsList = new ArrayList<>();

        // Setting Meet Up tab active
        activeFragId = R.id.tv_meet_up;

        // Set Tab Panel
        setUpTabPanel();

        tv_meet_up.setOnClickListener(this);
        tv_my_visits.setOnClickListener(this);
        iv_meet_up_filter.setOnClickListener(this);
        iv_add_visits.setOnClickListener(this);

        return view;
    }

    // Set Tab Panel
    private void setUpTabPanel() {
        if (tab_type == 0) {
            fromDistance = "0";
            toDistance = "100";

            if (AppHelper.isConnectingToInternet(mContext)) {
                progress.show();
            }
            // Get Meet Up List
            getMyMeetUpList("", "", "", "", "", "", fromDistance, toDistance);

            // Adapter for displaying meet up list
            setTravelAdapter();

            // Get Online Table data from firebase
            getOnlineStatusFromFirebase();
        } else if (tab_type == 1) {
            // Get My Visits List
            getMyVisitsList();

            // Adapter for displaying my visits list
            setTravelAdapter();

        }
    }

    private void setTravelAdapter() {
        if (tab_type == 0) {
            activeFragId = R.id.tv_meet_up;
            tv_meet_up.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
            tv_my_visits.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));

            // Meet Up List recycler adapter
            meetUpAdapter = new MeetUpAdapter(meetUpList, mContext);
            GridLayoutManager layoutManager = new GridLayoutManager(mContext, 2);
            travel_recycler_view.setLayoutManager(layoutManager);
            travel_recycler_view.setAdapter(meetUpAdapter);

            // Endless Recycler Scroll Listener Pagination
            scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    if (page != 1) {
                        progress.show();
                    }
                    getMyMeetUpList(fromDate, toDate, city, travelLocation, mLatitude, mLongitude, fromDistance, toDistance);
                    // getMyMeetUpList();
                }
            };

            // Adds the scroll listener to RecyclerView
            travel_recycler_view.addOnScrollListener(scrollListener);

            // Pull To refresh listener
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // cancel the Visual indication of a refresh
                    swipeRefreshLayout.setRefreshing(false);

                    offsetLimit = 0;
                    meetUpList.clear();
                    meetUpAdapter.notifyDataSetChanged();
                    scrollListener.resetState();
                    KinkLink.getInstance().cancelAllRequests(KinkLink.TAG);
                    getMyMeetUpList(fromDate, toDate, city, travelLocation, mLatitude, mLongitude, fromDistance, toDistance);
                }
            });

            iv_meet_up_filter.setVisibility(View.VISIBLE);
            iv_add_visits.setVisibility(View.GONE);

            ly_no_travel.setVisibility(View.GONE);
            tv_no_record.setText(mContext.getResources().getString(R.string.no_meet_up_found));
            iv_no_record_place_holder.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.ic_no_meetups));
        } else if (tab_type == 1) {
            activeFragId = R.id.tv_my_visits;
            tv_meet_up.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));
            tv_my_visits.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));

            // Visit List recycler adapter
            visitsAdapter = new MyVisitsAdapter(myVisitsList, new MyVisitPositionListener() {
                @Override
                public void getEditClick(int position) {
                    if (travel_dialog != null && travel_dialog.isShowing()) {
                        travel_dialog.dismiss();
                    }
                    editVisitPosition = position;
                    dialogType = 1;
                    openMeetUpFilterDialog();

                }

                @Override
                public void getDeleteClick(int position) {
                    if (travel_dialog != null && travel_dialog.isShowing()) {
                        travel_dialog.dismiss();
                    }
                    openDeleteVisitDialog(position);
                }
            });
            LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
            travel_recycler_view.setLayoutManager(layoutManager);
            travel_recycler_view.setAdapter(visitsAdapter);

            // Endless Recycler Scroll Listener Pagination
            final EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    progress.show();
                    getMyVisitsList();
                }
            };

            // Adds the scroll listener to RecyclerView
            travel_recycler_view.addOnScrollListener(scrollListener);

            // Pull To refresh listener
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // cancel the Visual indication of a refresh
                    swipeRefreshLayout.setRefreshing(false);
                    offsetLimit = 0;
                    myVisitsList.clear();
                    visitsAdapter.notifyDataSetChanged();
                    KinkLink.getInstance().cancelAllRequests(KinkLink.TAG);
                    getMyVisitsList();
                }
            });

            iv_meet_up_filter.setVisibility(View.GONE);
            iv_add_visits.setVisibility(View.VISIBLE);

            ly_no_travel.setVisibility(View.GONE);
            tv_no_record.setText(mContext.getResources().getString(R.string.no_visits_found));
            iv_no_record_place_holder.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.ic_no_visit));
        }

    }

    private void init(View view) {
        TextView action_bar_heading = ((MainActivity) mContext).findViewById(R.id.action_bar_heading);
        action_bar_heading.setText(getString(R.string.travel_kink));

        ImageView iv_back = ((MainActivity) mContext).findViewById(R.id.iv_back);
        ImageView iv_settings = ((MainActivity) mContext).findViewById(R.id.iv_settings);

        ImageView iv_teases = ((MainActivity) mContext).findViewById(R.id.iv_teases);
        iv_teases.setVisibility(View.GONE);
        iv_teases.setOnClickListener(this);

        RelativeLayout rl_notifications = ((MainActivity) mContext).findViewById(R.id.rl_notifications);
        rl_notifications.setVisibility(View.GONE);

        ImageView iv_profile = ((MainActivity) mContext).findViewById(R.id.iv_profile);
        iv_profile.setVisibility(View.VISIBLE);
        iv_profile.setOnClickListener(this);

        RelativeLayout bottom_menu = ((MainActivity) mContext).findViewById(R.id.bottomMenu);
        rl_main_activity = ((MainActivity) mContext).findViewById(R.id.rl_main_activity);
        ly_no_network = ((MainActivity) mContext).findViewById(R.id.ly_no_network);
        btn_try_again = ((MainActivity) mContext).findViewById(R.id.btn_try_again);

        iv_meet_up_filter = view.findViewById(R.id.iv_meet_up_filter);
        iv_add_visits = view.findViewById(R.id.iv_add_visits);

        iv_back.setVisibility(View.GONE);
        iv_settings.setVisibility(View.GONE);
        iv_meet_up_filter.setVisibility(View.VISIBLE);
        iv_add_visits.setVisibility(View.GONE);
        bottom_menu.setVisibility(View.VISIBLE);

        travel_recycler_view = view.findViewById(R.id.travel_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        ly_no_travel = view.findViewById(R.id.ly_no_travel);
        iv_no_record_place_holder = view.findViewById(R.id.iv_no_record_place_holder);
        tv_no_record = view.findViewById(R.id.tv_no_record);

        tv_meet_up = view.findViewById(R.id.tv_meet_up);
        tv_my_visits = view.findViewById(R.id.tv_my_visits);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    // Get Online Table data from firebase
    private void getOnlineStatusFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(Constant.ONLINE_TABLE);
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Set Online Offline status in Online Info type list
                setOnlineData(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // Set Online Offline status in Online Info type list
                setOnlineData(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // Set Online Offline status in Online Info type list
                setOnlineData(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Set Online Offline status in Online Info type list
    private void setOnlineData(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue(OnlineInfo.class) != null) {
            OnlineInfo info = dataSnapshot.getValue(OnlineInfo.class);
            onlineList.put(dataSnapshot.getKey(), info);

            for (MeetUpModel model : meetUpList) {
                if (dataSnapshot.getKey().equals(model.userId)) {
                    assert info != null;
                    model.isOnline = info.lastOnline;
                }
            }

            meetUpAdapter.notifyDataSetChanged();
        }
    }

    // Get Meet Up List Api
    private void getMyMeetUpList(final String fromDate, final String toDate, final String meetUpCity, final String location, final String latitude, final String longitude, final String fromDistance, final String toDistance) {
        if (AppHelper.isConnectingToInternet(mContext)) {

            final Map<String, String> map = new HashMap<>();
            map.put("offset", String.valueOf(offsetLimit));
            map.put("limit", "10");
            map.put("fromDate", fromDate);
            map.put("toDate", toDate);
            map.put("city", meetUpCity);
            map.put("location", location);
            map.put("latitude", latitude);
            map.put("longitude", longitude);
            map.put("fromDistance", fromDistance);
            map.put("toDistance", toDistance);

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");

                        if (status.equals("success")) {
                            progress.dismiss();
                            rl_main_activity.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            travel_recycler_view.setVisibility(View.VISIBLE);
                            ly_no_travel.setVisibility(View.GONE);

                            JSONArray jsonArray = js.getJSONArray("meetUpList");
                            MeetUpModel model;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                model = new MeetUpModel();
                                JSONObject object = jsonArray.getJSONObject(i);
                                model.visitId = object.getString("visitId");
                                model.from_date = object.getString("from_date");
                                model.to_date = object.getString("to_date");
                                model.city = object.getString("city");
                                model.address = object.getString("address");
                                model.userId = object.getString("userId");
                                model.full_name = object.getString("full_name");
                                model.gender = object.getString("gender");
                                model.is_verify = object.getString("is_verify");
                                model.age = object.getString("age");
                                model.image = object.getString("image");
                                model.distance_in_mi = object.getString("distance_in_mi");

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
                                meetUpList.add(model);
                            }

                            offsetLimit += 10;
                            meetUpAdapter.notifyDataSetChanged();
                        } else {
                            progress.dismiss();
                            if (meetUpList.size() == 0) {
                                travel_recycler_view.setVisibility(View.GONE);
                                ly_no_travel.setVisibility(View.VISIBLE);
                                rl_main_activity.setVisibility(View.VISIBLE);
                                ly_no_network.setVisibility(View.GONE);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        rl_main_activity.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    rl_main_activity.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                }

            });
            api.callApi("meetup/getMeetUpList", Request.Method.POST, map);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            rl_main_activity.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Preventing multiple clicks, using threshold of 1/2 second
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    getMyMeetUpList(fromDate, toDate, meetUpCity, location, latitude, longitude, fromDistance, toDistance);
                }
            });
        }
    }

    // Get Visit List Api
    private void getMyVisitsList() {
        if (AppHelper.isConnectingToInternet(mContext)) {

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");

                        if (status.equals("success")) {
                            progress.dismiss();
                            rl_main_activity.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            travel_recycler_view.setVisibility(View.VISIBLE);
                            ly_no_travel.setVisibility(View.GONE);

                            Gson gson = new Gson();
                            MyVisitsModel model = gson.fromJson(String.valueOf(js), MyVisitsModel.class);
                            myVisitsList.addAll(model.visitList);

                            offsetLimit += 10;
                            visitsAdapter.notifyDataSetChanged();
                        } else {
                            progress.dismiss();
                            if (myVisitsList.size() == 0) {
                                travel_recycler_view.setVisibility(View.GONE);
                                ly_no_travel.setVisibility(View.VISIBLE);
                                rl_main_activity.setVisibility(View.VISIBLE);
                                ly_no_network.setVisibility(View.GONE);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        rl_main_activity.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    rl_main_activity.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                }

            });
            api.callApi("meetup/getMyVisitList?offset=" + offsetLimit + "&limit=10", Request.Method.GET, null);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            rl_main_activity.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Preventing multiple clicks, using threshold of 1/2 second
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    getMyVisitsList();
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
            case R.id.iv_profile:
                startActivity(new Intent(mContext, MyProfileActivity.class));
                break;

            case R.id.iv_teases:
                ((MainActivity) mContext).addFragment(TeasesFragment.newInstance(), true, R.id.fragment_place);
                break;

            case R.id.tv_meet_up:
                if (activeFragId != R.id.tv_meet_up) {
                    activeFragId = R.id.tv_meet_up;
                    tab_type = 0;
                    offsetLimit = 0;
                    tv_meet_up.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                    tv_my_visits.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));

                    // Set Tab Panel
                    setUpTabPanel();

                    meetUpList.clear();
                    meetUpAdapter.notifyDataSetChanged();
                }
                break;

            case R.id.tv_my_visits:
                if (activeFragId != R.id.tv_my_visits) {
                    activeFragId = R.id.tv_my_visits;
                    tab_type = 1;
                    offsetLimit = 0;
                    tv_meet_up.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));
                    tv_my_visits.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));

                    // Set Tab Panel
                    setUpTabPanel();

                    myVisitsList.clear();
                    visitsAdapter.notifyDataSetChanged();
                }
                break;

            case R.id.iv_meet_up_filter:
                fromDate = "";
                toDate = "";
                city = "";
                travelLocation = "";
                mLatitude = "";
                mLongitude = "";
                fromDistance = "0";
                toDistance = "100";

                openMeetUpFilterDialog();
                break;

            case R.id.iv_add_visits:
                country = "";
                state = "";
                city = "";
                travelLocation = "";
                visitAddress = "";

                dialogType = 0;

                if (travel_dialog != null && travel_dialog.isShowing()) {
                    travel_dialog.dismiss();
                }
                openMeetUpFilterDialog();
                break;

            case R.id.rl_city_town:
                // Open place picker to enter city/town
                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(((MainActivity) mContext));
                    startActivityForResult(intent, Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE);
                    rl_city_town.setEnabled(false);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.btn_search:
                Fragment fragment = getFragmentManager().findFragmentById(R.id.fragment_place);
                if (fragment instanceof MatchProfileFragment) {
                    ((MainActivity) mContext).onBackPressed();
                }


                if (tab_type == 0) {
                    {
                        travel_dialog.dismiss();
                        /*if (tv_from_date.getText().toString().equals(mContext.getResources().getString(R.string.from_dt))) {
                            fromDate = "";
                        } else {
                            fromDate = Utils.dateInYMDFormat(tv_from_date.getText().toString().trim());
                        }

                        if (tv_to_date.getText().toString().equals(mContext.getResources().getString(R.string.to_dt))) {
                            toDate = "";
                        } else {
                            toDate = Utils.dateInYMDFormat(tv_to_date.getText().toString().trim());
                        }*/

                        if (city == null) {
                            city = "";
                        }
                        if (travelLocation == null) {
                            travelLocation = "";
                        }
                        if (mLatitude == null) {
                            mLatitude = "";
                        }
                        if (mLongitude == null) {
                            mLongitude = "";
                        }

                        offsetLimit = 0;
                        meetUpList.clear();
                        scrollListener.resetState();
                        getMyMeetUpList(fromDate, toDate, city, travelLocation, mLatitude, mLongitude, fromDistance, toDistance);
                    }
                } else if (tab_type == 1) {
                    if (dialogType == 0) {
                        if (isAddVisitValid()) {
                            String from_date = "";//Utils.dateInYMDFormat(tv_from_date.getText().toString().trim());
                            String to_date = "";//Utils.dateInYMDFormat(tv_to_date.getText().toString().trim());

                            // Api to add visits
                            callAddVisit(from_date, to_date, visitAddress, city, mLatitude, mLongitude, travelLocation);
                        }
                    } else if (dialogType == 1) {
                        String from_date = Utils.dateInYMDFormat(tv_from_date.getText().toString().trim());
                        String to_date = Utils.dateInYMDFormat(tv_to_date.getText().toString().trim());
                        String visitLoc = tv_city_town.getText().toString().trim();
                        String visit_id = myVisitsList.get(editVisitPosition).visitId;

                        if (from_date.equals(myVisitsList.get(editVisitPosition).from_date) &&
                                to_date.equals(myVisitsList.get(editVisitPosition).to_date) && (visitLoc.equals(myVisitsList.get(editVisitPosition).location))) {
                            travel_dialog.dismiss();
                        } else {
                            // Call to edit visit
                            if (visitLoc.equals(myVisitsList.get(editVisitPosition).location)) {
                                city = myVisitsList.get(editVisitPosition).city;
                                visitAddress = myVisitsList.get(editVisitPosition).address;
                                mLatitude = myVisitsList.get(editVisitPosition).latitude;
                                mLongitude = myVisitsList.get(editVisitPosition).longitude;
                                travelLocation = myVisitsList.get(editVisitPosition).location;

                                callEditVisit(visit_id, from_date, to_date, visitAddress, city, mLatitude, mLongitude, travelLocation);

                            } else {
                                callEditVisit(visit_id, from_date, to_date, visitAddress, city, mLatitude, mLongitude, travelLocation);

                            }
                        }
                    }
                }
                break;

            case R.id.rl_from_date:
                date_type = "from";
                openFromDateDialog(tv_from_date);
                break;

            case R.id.rl_to_date:
                date_type = "to";
                openFromDateDialog(tv_to_date);
                break;

        }
    }

    private void openMeetUpFilterDialog() {
        travel_dialog = new Dialog(mContext);
        travel_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        travel_dialog.setContentView(R.layout.dialog_meetup_filter);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(travel_dialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        travel_dialog.getWindow().setAttributes(lWindowParams);

        initMeetUpFilterDialog();

        ImageView dialog_decline_button = travel_dialog.findViewById(R.id.dialog_decline_button);
        dialog_decline_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                travel_dialog.dismiss();
            }
        });


        travel_dialog.getWindow().setGravity(Gravity.CENTER);
        travel_dialog.show();
    }

    private void initMeetUpFilterDialog() {
        TextView btn_search = travel_dialog.findViewById(R.id.btn_search);
        LinearLayout ly_seekbar = travel_dialog.findViewById(R.id.ly_seekbar);
        TextView tv_add_visit_heading = travel_dialog.findViewById(R.id.tv_add_visit_heading);

        rl_city_town = travel_dialog.findViewById(R.id.rl_city_town);
        tv_city_town = travel_dialog.findViewById(R.id.tv_city_town);
        RelativeLayout rl_from_date = travel_dialog.findViewById(R.id.rl_from_date);
        RelativeLayout rl_to_date = travel_dialog.findViewById(R.id.rl_to_date);
        tv_from_date = travel_dialog.findViewById(R.id.tv_from_date);
        tv_to_date = travel_dialog.findViewById(R.id.tv_to_date);

        if (tab_type == 0) {
            btn_search.setText(getString(R.string.btn_search));
            tv_add_visit_heading.setText(getString(R.string.filter));
            ly_seekbar.setVisibility(View.VISIBLE);

            // get seekbar from view
            final CrystalSeekbar rangeSeekbar = travel_dialog.findViewById(R.id.rangeSeekbar);
            rangeSeekbar.setPosition(CrystalSeekbar.Position.LEFT).apply();
            rangeSeekbar.setMinStartValue(100).apply();
            fromDistance = "0";
            toDistance = "100";

            // get min and max text view
            final TextView tvMin = travel_dialog.findViewById(R.id.tv_miles_min_limit);
            final TextView tvMax = travel_dialog.findViewById(R.id.tv_miles_max_limit);

            rangeSeekbar.setOnSeekbarChangeListener(new OnSeekbarChangeListener() {
                @Override
                public void valueChanged(Number value) {
                    tvMin.setText(String.valueOf(0));
                    tvMax.setText(String.valueOf(value));
                }
            });

            rangeSeekbar.setOnSeekbarFinalValueListener(new OnSeekbarFinalValueListener() {
                @Override
                public void finalValue(Number value) {
                    fromDistance = String.valueOf(0);
                    toDistance = String.valueOf(value);
                }
            });


        } else if (tab_type == 1) {
            ly_seekbar.setVisibility(View.GONE);

            if (dialogType == 0) {
                tv_add_visit_heading.setText(getString(R.string.add_my_availability));
                btn_search.setText(getString(R.string.add_btn));
            } else if (dialogType == 1) {
                tv_from_date.setText(Utils.dateInMDYFormat(myVisitsList.get(editVisitPosition).from_date));
                tv_from_date.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));

                tv_to_date.setText(Utils.dateInMDYFormat(myVisitsList.get(editVisitPosition).to_date));
                tv_to_date.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));

                tv_city_town.setText(myVisitsList.get(editVisitPosition).location);
                tv_city_town.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));

                tv_add_visit_heading.setText(getString(R.string.edit));
                btn_search.setText(getString(R.string.done));
            }
        }

        btn_search.setOnClickListener(this);
        rl_city_town.setOnClickListener(this);
        rl_from_date.setOnClickListener(this);
        rl_to_date.setOnClickListener(this);


    }

    private void openFromDateDialog(TextView tv_date) {
        String date = tv_date.getText().toString().trim();
        String from_date;
        from_date = tv_from_date.getText().toString().trim();

        if (date.equals(mContext.getResources().getString(R.string.from_dt)) || (date.equals(mContext.getResources().getString(R.string.to_dt)) && from_date.equals(mContext.getResources().getString(R.string.from_dt)))) {  //Set date for first time
            Calendar now = Calendar.getInstance();
            int mYear = now.get(Calendar.YEAR);
            int mMonth = now.get(Calendar.MONTH);
            int mDay = now.get(Calendar.DAY_OF_MONTH);
            now.set(mYear, mMonth, mDay);

            setDateOnDatePickerDialog(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), tv_date);
        } else if (date.equals(mContext.getResources().getString(R.string.to_dt)) && !from_date.equals(mContext.getResources().getString(R.string.from_dt))) {  //Set date for first time
            String[] s = from_date.split("-");
            int mDay = Integer.parseInt(s[1]);
            int mMonth = Integer.parseInt(s[0]);
            int mYear = Integer.parseInt(s[2]);
            setDateOnDatePickerDialog(mYear, mMonth, mDay, tv_date);

            // Setting Current date as Min Date
            Calendar c = Calendar.getInstance();    // Set Min Date
            c.set(mYear, mMonth - 1, mDay);
            date_picker.setMinDate(c);

        } else {  // Set already filled date
            String[] s = date.split("-");
            int mDay = Integer.parseInt(s[1]);
            int mMonth = Integer.parseInt(s[0]);
            int mYear = Integer.parseInt(s[2]);

            setDateOnDatePickerDialog(mYear, mMonth, mDay, tv_date);

            if (date_type.equals("to") && !tv_from_date.getText().toString().trim().equals(getResources().getString(R.string.from_dt))) {
                String[] sDate;
                String fromDate = tv_from_date.getText().toString().trim();
                sDate = fromDate.split("-");
                int day = Integer.parseInt(sDate[1]);
                int month = Integer.parseInt(sDate[0]);
                int year = Integer.parseInt(sDate[2]);

                Calendar c = Calendar.getInstance();
                c.set(year, month - 1, day);
                date_picker.setMinDate(c);
            }
        }
    }

    // Set date on Date picker
    public void setDateOnDatePickerDialog(int year, int month, int day, final TextView tv_date) {
        date_picker = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                String day, month;
                day = (dayOfMonth < 10) ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
                monthOfYear += 1;
                month = (monthOfYear < 10) ? "0" + monthOfYear : String.valueOf(monthOfYear);

                date = month + "-" + day + "-" + year;
                tv_date.setText(date);
                tv_date.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));


                String fromDate = tv_from_date.getText().toString().trim();
                String toDate = tv_to_date.getText().toString().trim();
                if (date_type.equals("from") && !tv_to_date.getText().toString().equals(getResources().getString(R.string.to_dt))) {
                    if (!verifyDate(toDate, fromDate)) {  // When date is big
                        tv_to_date.setText(fromDate);
                    }
                }

            }
        }, year, month - 1, day);

        // Setting Current date as Min Date
        Calendar c = Calendar.getInstance();    // Set Min Date
        int mYr = c.get(Calendar.YEAR);
        int mMon = c.get(Calendar.MONTH);
        int mDy = c.get(Calendar.DAY_OF_MONTH);
        c.set(mYr, mMon, mDy);
        date_picker.setMinDate(c);

        date_picker.show(((MainActivity) mContext).getFragmentManager(), "");
        date_picker.setAccentColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        date_picker.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                date_picker.dismiss();
            }
        });
    }

    private boolean verifyDate(String toDate, String fromDate) {
        try {
            Date fDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).parse(fromDate);
            Date tDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).parse(toDate);

            return fDate.before(tDate);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Validations for Visit
    private boolean isAddVisitValid() {
        /*if (tv_from_date.getText().toString().equals(mContext.getResources().getString(R.string.from_dt))) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.add_visit_from_date_null));
            return false;
        }*//* else if (tv_to_date.getText().toString().equals(mContext.getResources().getString(R.string.to_dt))) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.add_visit_to_date_null));
            return false;*/
          if (travelLocation == null || travelLocation.equals("")) {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.add_visit_city_null));
            return false;
        }

        return true;
    }

    // Api call to add visit
    private void callAddVisit(final String fromDate, final String toDate, final String address, final String city, final String latitude, final String longitude, final String location) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            final Map<String, String> map = new HashMap<>();
            map.put("fromDate", fromDate);
            map.put("toDate", toDate);
            map.put("address", address);
            map.put("city", city);
            map.put("latitude", latitude);
            map.put("longitude", longitude);
            map.put("location", location);

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            rl_main_activity.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            travel_dialog.dismiss();
                            offsetLimit = 0;
                            myVisitsList.clear();
                            getMyVisitsList();
                        } else {
                            progress.dismiss();
                            travel_dialog.dismiss();
                            rl_main_activity.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        travel_dialog.dismiss();
                        rl_main_activity.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    travel_dialog.dismiss();
                    rl_main_activity.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                }

            });
            api.callApi("meetup/makeMyVisit", Request.Method.POST, map);
        } else {
            travel_dialog.dismiss();
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.alert_no_network));
        }
    }

    // Api call for Edit Visit
    private void callEditVisit(final String visit_id, final String from_date, final String to_date, final String address, final String city, final String mLatitude, final String mLongitude, final String visitLocation) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            final Map<String, String> map = new HashMap<>();
            map.put("visitId", visit_id);
            map.put("fromDate", from_date);
            map.put("toDate", to_date);
            map.put("address", address);
            map.put("city", city);
            map.put("latitude", mLatitude);
            map.put("longitude", mLongitude);
            map.put("location", visitLocation);

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            rl_main_activity.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            travel_dialog.dismiss();
                            offsetLimit = 0;
                            myVisitsList.clear();
                            getMyVisitsList();
                        } else {
                            progress.dismiss();
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                            travel_dialog.dismiss();
                            rl_main_activity.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        travel_dialog.dismiss();
                        rl_main_activity.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    travel_dialog.dismiss();
                    rl_main_activity.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                }

            });
            api.callApi("meetup/editMyVisit", Request.Method.POST, map);
        } else {
            travel_dialog.dismiss();
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.alert_no_network));
        }
    }

    // Open Delete Visit Dialog
    private void openDeleteVisitDialog(final int position) {
        deleteVisitsDialog = new Dialog(mContext);
        deleteVisitsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        deleteVisitsDialog.setContentView(R.layout.dialog_delete_visits);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(deleteVisitsDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        deleteVisitsDialog.getWindow().setAttributes(lWindowParams);

        ImageView dialog_decline_button = deleteVisitsDialog.findViewById(R.id.dialog_decline_button);
        TextView btn_cancel_visit = deleteVisitsDialog.findViewById(R.id.btn_cancel_visit);
        btn_delete_visit = deleteVisitsDialog.findViewById(R.id.btn_delete_visit);

        dialog_decline_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                deleteVisitsDialog.dismiss();
            }
        });

        btn_cancel_visit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                deleteVisitsDialog.dismiss();
            }
        });


        btn_delete_visit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                if (myVisitsList.size() != 0) {
                    btn_delete_visit.setEnabled(false);
                    String visit_id = myVisitsList.get(position).visitId;
                    // Call to delete visit
                    callDeleteVisit(visit_id);
                }
            }
        });

        deleteVisitsDialog.getWindow().setGravity(Gravity.CENTER);
        deleteVisitsDialog.show();
    }

    // Api call to delete visit
    private void callDeleteVisit(final String visit_id) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            final Map<String, String> map = new HashMap<>();
            map.put("visitId", visit_id);

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            rl_main_activity.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            deleteVisitsDialog.dismiss();
                            offsetLimit = 0;
                            myVisitsList.clear();
                            getMyVisitsList();

                            btn_delete_visit.setEnabled(true);
                        } else {
                            progress.dismiss();
                            btn_delete_visit.setEnabled(true);
                            deleteVisitsDialog.dismiss();
                            rl_main_activity.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        deleteVisitsDialog.dismiss();
                        btn_delete_visit.setEnabled(true);
                        rl_main_activity.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    deleteVisitsDialog.dismiss();
                    rl_main_activity.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                }

            });
            api.callApi("meetup/deleteMyVisit", Request.Method.POST, map);
        } else {
            deleteVisitsDialog.dismiss();
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.alert_no_network));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        rl_city_town.setEnabled(true);

        // Autocomplete Place Api
        if (requestCode == Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == -1) {
                Place place = PlaceAutocomplete.getPlace(mContext, data);

                // Parse Country, State and City from entered address
                getAddress(place);
            }
        }

    }

    // Parse Country, State and City from entered address
    private void getAddress(final Place place) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            new AddressLocationTask(mContext, place, new AddressLocationTask.AddressLocationListner() {
                @Override
                public void getLocation(String cty, String st, String cntry, String locAddress) {
                    city = cty;
                    state = st;
                    country = cntry;

                    if (!country.equals("") && !state.equals("") && !city.equals("")) {
                        travelLocation = city + ", " + state + ", " + country;
                    } else if (!country.equals("") && !state.equals("")) {
                        travelLocation = state + ", " + country;
                    } else if (!country.equals("")) {
                        travelLocation = country;
                    }

                    visitAddress = locAddress;

                    tv_city_town.setText(travelLocation);
                    tv_city_town.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));

                    if (place.getLatLng() != null) {
                        mLatitude = "" + place.getLatLng().latitude;
                        mLongitude = "" + place.getLatLng().longitude;
                    }
                }
            }).execute();
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
}
