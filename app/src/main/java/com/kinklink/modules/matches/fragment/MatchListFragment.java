package com.kinklink.modules.matches.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
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
import com.kinklink.helper.GioAddressTask;
import com.kinklink.helper.LocationRuntimePermission;
import com.kinklink.helper.Progress;
import com.kinklink.modules.authentication.activity.EditProfileActivity;
import com.kinklink.modules.authentication.activity.LoginActivity;
import com.kinklink.modules.authentication.fragment.EditOtherInfoFragment;
import com.kinklink.modules.authentication.listener.AdapterPositionListener;
import com.kinklink.modules.authentication.model.Address;
import com.kinklink.modules.authentication.model.OnlineInfo;
import com.kinklink.modules.authentication.model.RegistrationInfo;
import com.kinklink.modules.chat.activity.ChatActivity;
import com.kinklink.modules.matches.activity.MainActivity;
import com.kinklink.modules.matches.activity.MatchProfileActivity;
import com.kinklink.modules.matches.activity.MyProfileActivity;
import com.kinklink.modules.matches.activity.NotificationActivity;
import com.kinklink.modules.matches.adapter.MatchListAdapter;
import com.kinklink.modules.matches.listener.EndlessRecyclerViewScrollListener;
import com.kinklink.modules.matches.model.MatchListModel;
import com.kinklink.server_task.WebService;
import com.kinklink.session.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MatchListFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {
    private Context mContext;
    private RecyclerView match_list_recycler_view;
    private ArrayList<MatchListModel> matchList;
    private MatchListAdapter matchListAdapter;
    private int offsetLimit = 0;
    private Session session;
    private Dialog privacyDialog;

    // Get Current Location
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    private LocationManager locationManager;
    private String mLatitude="", mLongitude="", full_address="", city="",getFull_address="";

    private LinearLayout ly_no_network, ly_no_match;
    private RelativeLayout rl_main_activity;
    private TextView btn_try_again;

    // variable to track event time
    private long mLastClickTime = 0;
    private SwipeRefreshLayout swipeRefreshLayout;

    private TextView action_bar_heading, tv_notification_count;
    private RelativeLayout bottomMenu;
    private ImageView iv_travel_kinks, iv_viewed, iv_offers, iv_chat_history, iv_back, iv_menu;
    private Map<String, OnlineInfo> onlineList;
    private Progress progress;

    private LinearLayout ly_distance, ly_random, ly_percentage, ly_favorite, ly_filter;
    private ImageView iv_distance_border, iv_percentage_border, iv_random_border, iv_distance, iv_percentage, iv_random;

    public MatchListFragment() {
    }

    public static MatchListFragment newInstance() {
        Bundle args = new Bundle();
        MatchListFragment fragment = new MatchListFragment();
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
        View view = inflater.inflate(R.layout.fragment_match_list, container, false);
        init(view);

        progress = new Progress(mContext);
        progress.show();
        session = new Session(mContext);
        /*if (session.getRegistration().userDetail.is_profile_complete.equals("0")){
            openProfileIncompleteDialog();

        }*/
        //   String policyStatus = session.getRegistration().userDetail.policy_flag;

        matchList = new ArrayList<>();
        onlineList = new HashMap<>();

        getNotificationCount();

        // Get Current Latitude and Longitude
        callToGetCurrentLocation();

        // Call Update LatLong Api to update current location
        if (session.getAuthToken() != null && !session.getAuthToken().equals("")) {
            if (mLatitude != null && mLongitude != null && full_address != null && city != null &&
                    !mLatitude.equals("") && !mLongitude.equals("") && !full_address.equals("") && !city.equals("")&&!getFull_address.isEmpty()) {
                callUpdateLatLongApi(mLatitude, mLongitude, full_address, city,getFull_address);

                /*// Get Match List Api
                if (matchList.size() == 0) {
                    getAndSetMatchList();
                }*/
            } else {
                displayCurrentLocation();
            }
        }

        // Method to get random index for filtering data randomly
        //   setRandomIndexForFilter();

        /*if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();
        }*/

        // Get Match List Api
        if (matchList.size() == 0) {
            getAndSetMatchList();
        }

        /*if (policyStatus != null && policyStatus.equals("0")) {
            openPrivacyPolicyDialog();
        }*/

        // Adapter for Displaying match list
        setMatchListAdapter();

        // Get Online Table data from firebase
        getOnlineStatusFromFirebase();

        // Set Match Fragment Active
        setMatchListFragmentActive();

        ly_distance.setOnClickListener(this);
        ly_random.setOnClickListener(this);
        ly_percentage.setOnClickListener(this);
        ly_favorite.setOnClickListener(this);
        ly_filter.setOnClickListener(this);
        return view;
    }

    private void getAndSetMatchList() {
        if (session.getFilterSortBy() != null && !session.getFilterSortBy().equals("")) {

            switch (session.getFilterSortBy()) {
                case "distance":
                    setDistanceActive();
                    break;
                case "random":
                    setRandomActive();
                    break;
                case "percentage":
                    setPercentageActive();
                    break;
            }

            // Get Match List Api
            getMatchList();
        } else {
            /*session.setFilterSortBy("random");
            setRandomActive();
            // Method to get random index for filtering data randomly
            setRandomIndexForFilter();*/

            // Get Match List Api


            setPercentageActive();
            session.setFilterSortBy("percentage");
            session.setFilterRandomIndex("");
            offsetLimit = 0;
            getMatchList();
        }

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

            for (MatchListModel model : matchList) {
                if (dataSnapshot.getKey().equals(model.userId)) {
                    assert info != null;
                    model.isOnline = info.lastOnline;
                }
            }
            matchListAdapter.notifyDataSetChanged();
        }
    }

    private void init(View view) {
        match_list_recycler_view = view.findViewById(R.id.match_list_recycler_view);
        match_list_recycler_view.setVisibility(View.GONE);

        ly_no_match = view.findViewById(R.id.ly_no_match);

        rl_main_activity = ((MainActivity) mContext).findViewById(R.id.rl_main_activity);
        ly_no_network = ((MainActivity) mContext).findViewById(R.id.ly_no_network);
        btn_try_again = ((MainActivity) mContext).findViewById(R.id.btn_try_again);
        iv_travel_kinks = ((MainActivity) mContext).findViewById(R.id.iv_travel_kinks);
        iv_viewed = ((MainActivity) mContext).findViewById(R.id.iv_viewed);
        iv_offers = ((MainActivity) mContext).findViewById(R.id.iv_offers);
        iv_chat_history = ((MainActivity) mContext).findViewById(R.id.iv_chat_history);
        action_bar_heading = ((MainActivity) mContext).findViewById(R.id.action_bar_heading);
        iv_back = ((MainActivity) mContext).findViewById(R.id.iv_back);
        iv_menu = ((MainActivity) mContext).findViewById(R.id.iv_menu);
        bottomMenu = ((MainActivity) mContext).findViewById(R.id.bottomMenu);

        swipeRefreshLayout = view.findViewById(R.id.simpleSwipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        ImageView iv_profile = ((MainActivity) mContext).findViewById(R.id.iv_profile);
        iv_profile.setVisibility(View.VISIBLE);
        iv_profile.setOnClickListener(this);

        ImageView iv_teases = ((MainActivity) mContext).findViewById(R.id.iv_teases);
        iv_teases.setVisibility(View.GONE);
        iv_teases.setOnClickListener(this);

        LinearLayout ly_tease=((MainActivity)mContext).findViewById(R.id.ly_tease);
        ly_tease.setOnClickListener(this);

        RelativeLayout rl_notifications = ((MainActivity) mContext).findViewById(R.id.rl_notifications);
        tv_notification_count = ((MainActivity) mContext).findViewById(R.id.tv_notification_count);
        tv_notification_count.setVisibility(View.GONE);
        rl_notifications.setVisibility(View.VISIBLE);
        rl_notifications.setOnClickListener(this);

        ImageView iv_settings = ((MainActivity) mContext).findViewById(R.id.iv_settings);
        iv_settings.setVisibility(View.GONE);

        ly_distance = view.findViewById(R.id.ly_distance);
        ly_random = view.findViewById(R.id.ly_random);
        ly_percentage = view.findViewById(R.id.ly_percentage);
        ly_favorite = view.findViewById(R.id.ly_favorite);
        ly_filter = view.findViewById(R.id.ly_filter);

        iv_distance_border = view.findViewById(R.id.iv_distance_border);
        iv_percentage_border = view.findViewById(R.id.iv_percentage_border);
        iv_random_border = view.findViewById(R.id.iv_random_border);
        iv_distance = view.findViewById(R.id.iv_distance);
        iv_percentage = view.findViewById(R.id.iv_percentage);
        iv_random = view.findViewById(R.id.iv_random);

    }

    // Set Match Fragment Active
    private void setMatchListFragmentActive() {
        iv_travel_kinks.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.inactive_airplane_ico));
        iv_viewed.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.inactive_view_ico));
        iv_offers.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.inactive_gift_ico));
        iv_chat_history.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.inactive_chat_ico));

        action_bar_heading.setText(getString(R.string.matches));
        iv_back.setVisibility(View.GONE);
        iv_menu.setVisibility(View.GONE);
        bottomMenu.setVisibility(View.VISIBLE);
    }

    // Adapter for Displaying match list
    private void setMatchListAdapter() {
        // Match List recycler adapter
        matchListAdapter = new MatchListAdapter(matchList, mContext, new AdapterPositionListener() {
            @Override
            public void getPosition(int position) {
                if (matchList.size() != 0) {
                    // Listener of click on match list item
                    //  ((MainActivity) mContext).addFragment(MatchProfileFragment.newInstance(matchList.get(position).userId), true, R.id.fragment_place);
                    Intent intent = new Intent(mContext, MatchProfileActivity.class);
                    intent.putExtra("match_user_id", matchList.get(position).userId);
                    startActivity(intent);
                }
            }
        });
        GridLayoutManager layoutManager = new GridLayoutManager(mContext, 2);
        match_list_recycler_view.setLayoutManager(layoutManager);
        match_list_recycler_view.setAdapter(matchListAdapter);

        // Pull To refresh listener
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // cancel the Visual indication of a refresh
                swipeRefreshLayout.setRefreshing(false);
                KinkLink.getInstance().cancelAllRequests(KinkLink.TAG);

                ly_distance.setEnabled(false);
                ly_percentage.setEnabled(false);
                ly_random.setEnabled(false);
                ly_favorite.setEnabled(false);
                ly_filter.setEnabled(false);

                matchList.clear();
                offsetLimit = 0;

                // Method to get random index for filtering data randomly
                setRandomIndexForFilter();
                matchListAdapter.notifyDataSetChanged();

                // Get Match List Api
                getMatchList();

                getNotificationCount();
            }
        });

        // Endless Recycler Scroll Listener Pagination
        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (page != 1) {
                    progress.show();
                }
                // Get Match List Api
                getMatchList();
            }
        };

        // Adds the scroll listener to RecyclerView
        match_list_recycler_view.addOnScrollListener(scrollListener);

    }

    // Method to get random index for filtering data randomly
    private void setRandomIndexForFilter() {
        if (session.getFilterSortBy().equals("random")) {
            String[] randomArray = {"0", "1", "2", "3", "4", "5"};
            String index = randomArray[new Random().nextInt(randomArray.length)];
           /* Log.e("Random Index", index);*/
            session.setFilterRandomIndex(index);
        }
    }

    // Get Current Latitude and Longitude
    private void callToGetCurrentLocation() {
        // Get Latitude and Longitude
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
            createLocationRequest();
        }
        displayCurrentLocation();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    // Get Match List Api
    private void getMatchList() {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();
            final Map<String, String> map = new HashMap<>();
            map.put("offset", String.valueOf(offsetLimit));
            map.put("limit", "10");
            map.put("user", session.getFilterUserName());
            map.put("interest", session.getFilterInterests());


            if(session.getFilterLatitude()==null)map.put("latitude", "");
            else map.put("latitude", session.getFilterLatitude());

            if(session.getFilterLongitude()==null)map.put("longitude", "");
            else map.put("longitude", session.getFilterLongitude());

            map.put("longitude", session.getFilterLongitude());
            map.put("gender", session.getFilterGender());
            map.put("intent", "");
            map.put("bodyType", session.getFilterBodyType());
            map.put("ethnicity", session.getFilterEthnicity());

            if (session.getFilterFromAge().equals("")) {
                map.put("fromAge", "18");
            } else {
                map.put("fromAge", session.getFilterFromAge());
            }

            if (session.getFilterToAge().equals("")) {
                map.put("toAge", "100");
            } else {
                map.put("toAge", session.getFilterToAge());
            }

            if (session.getFilterMinDistance().equals("")) {
                map.put("rangMin", "0");
            } else {
                map.put("rangMin", session.getFilterMinDistance());
            }

            if (session.getFilterMaxDistance().equals("")) {
                map.put("rangMax", "300");
            } else {
                map.put("rangMax", session.getFilterMaxDistance());
            }
            map.put("sorting", session.getFilterSortBy());
            map.put("randomkey", session.getFilterRandomIndex());

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

                            match_list_recycler_view.setVisibility(View.VISIBLE);
                            ly_no_match.setVisibility(View.GONE);

                            JSONArray jsonArray = js.getJSONArray("matchList");
                            MatchListModel model;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                model = new MatchListModel();
                                JSONObject object = jsonArray.getJSONObject(i);
                                model.userId = object.getString("userId");
                                model.full_name = object.getString("full_name");
                                model.gender = object.getString("gender");
                                model.is_verify = object.getString("is_verify");
                                model.age = object.getString("age");
                                model.match_count = object.getString("match_count");
                                model.percentage = object.getString("percentage");
                                model.image = object.getString("image");
                                model.userInterestIds = object.getString("userInterestIds");
                                model.distance_in_mi = object.getString("distance_in_mi");
                                model.match = object.getString("match");
                                model.heart = object.getString("heart");

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
                                matchList.add(model);
                            }

                            offsetLimit += 10;
                            matchListAdapter.notifyDataSetChanged();

                            ly_distance.setEnabled(true);
                            ly_percentage.setEnabled(true);
                            ly_random.setEnabled(true);
                            ly_favorite.setEnabled(true);
                            ly_filter.setEnabled(true);

                        } else {
                            progress.dismiss();
                            if (matchList.size() == 0) {
                                match_list_recycler_view.setVisibility(View.GONE);
                                ly_no_match.setVisibility(View.VISIBLE);
                            }

                            ly_distance.setEnabled(true);
                            ly_percentage.setEnabled(true);
                            ly_random.setEnabled(true);
                            ly_favorite.setEnabled(true);
                            ly_filter.setEnabled(true);
                        }

                    } catch (JSONException e) {

                        if (LocationRuntimePermission.checkLocationPermission(mContext)){

                        }

                        e.printStackTrace();
                        progress.dismiss();
                        //CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));

                        ly_distance.setEnabled(true);
                        ly_percentage.setEnabled(true);
                        ly_random.setEnabled(true);
                        ly_favorite.setEnabled(true);
                        ly_filter.setEnabled(true);
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    ly_distance.setEnabled(true);
                    ly_percentage.setEnabled(true);
                    ly_random.setEnabled(true);
                    ly_favorite.setEnabled(true);
                    ly_filter.setEnabled(true);
                }

            });
            api.callApi("user/getMatchList", Request.Method.POST, map);
        } else {
            progress.dismiss();
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            ly_distance.setEnabled(true);
            ly_percentage.setEnabled(true);
            ly_random.setEnabled(true);
            ly_favorite.setEnabled(true);
            ly_filter.setEnabled(true);

            rl_main_activity.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(this);
        }
    }


    // Update Lat Long Api
    private void callUpdateLatLongApi(String latitude, String longitude, String full_address, String city,String addressFull) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            final Map<String, String> map = new HashMap<>();
            map.put("latitude", latitude);
            map.put("longitude", longitude);
            map.put("full_address", addressFull);
            map.put("address", full_address);
            map.put("city", city);

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    if (matchList.size() == 0) {
                        // Get Match List Api
                        getAndSetMatchList();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {

                }

            });
            api.callApi("user/latLongUpdate", Request.Method.POST, map);
        } else {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.alert_no_network));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Constant.MY_PERMISSIONS_REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    displayCurrentLocation();
                } else {
                    displayCurrentLocation();
                }
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        // Displaying the new location on UI
        displayCurrentLocation();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayCurrentLocation();

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
        if (LocationRuntimePermission.checkLocationPermission(mContext)) {
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (isGPSEnabled) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if (mLastLocation != null) {
                    mLatitude = String.valueOf(mLastLocation.getLatitude());
                    mLongitude = String.valueOf(mLastLocation.getLongitude());

                    LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    session.setFilterCity(Double.toString(mLastLocation.getLatitude()),Double.toString(mLastLocation.getLongitude()));

                    new GioAddressTask(mContext, latLng, new GioAddressTask.LocationListner() {
                        @Override
                        public void onSuccess(com.kinklink.modules.authentication.model.Address address) {
                            full_address = address.getStAddress1();
                            city = address.getCity();
                            getFull_address= address.getCity() + "," + address.getState() + "," + address.getCountry();

                            callUpdateLatLongApi(mLatitude, mLongitude, full_address, city,getFull_address);
                        }
                    }).execute();

                }
            } else {
                if (isAdded() && session.getUserGetRegistered()) {
                    session.setUserGetRegistered(false);
                    CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.enable_gps));
                }
            }
        } /*else {
            if (session.getUserGetRegistered()) {
                session.setUserGetRegistered(false);
                startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
            }
        }*/
    }

    /**
     * Method to verify google play services on the device
     */

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(mContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
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
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
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

        getNotificationCount();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) getActivity());
    }

    private void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) getActivity());
    }

    @Override
    public void onClick(View v) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (v.getId()) {
            case R.id.ly_filter:       // Open Filter Fragment
                ((MainActivity) mContext).addFragment(MatchFilterFragment.newInstance(), true, R.id.fragment_place);
                break;

            case R.id.ly_distance:    // Sort By - Distance click
                KinkLink.getInstance().cancelAllRequests(KinkLink.TAG);

                ly_distance.setEnabled(false);
                ly_percentage.setEnabled(true);
                ly_random.setEnabled(true);
                ly_favorite.setEnabled(true);
                ly_filter.setEnabled(true);

                setDistanceActive();
                session.setFilterSortBy("distance");
                session.setFilterRandomIndex("");
                offsetLimit = 0;
                matchList.clear();
                matchListAdapter.notifyDataSetChanged();
                getMatchList();
                break;

            case R.id.ly_percentage:    // Sort By - Percentage click
                KinkLink.getInstance().cancelAllRequests(KinkLink.TAG);

                ly_distance.setEnabled(true);
                ly_percentage.setEnabled(false);
                ly_random.setEnabled(true);
                ly_favorite.setEnabled(true);
                ly_filter.setEnabled(true);

                setPercentageActive();
                session.setFilterSortBy("percentage");
                session.setFilterRandomIndex("");
                offsetLimit = 0;
                matchList.clear();
                matchListAdapter.notifyDataSetChanged();
                getMatchList();
                break;

            case R.id.ly_random:        // Sort By - Random click
                KinkLink.getInstance().cancelAllRequests(KinkLink.TAG);

                ly_distance.setEnabled(true);
                ly_percentage.setEnabled(true);
                ly_random.setEnabled(false);
                ly_favorite.setEnabled(true);
                ly_filter.setEnabled(true);

                setRandomActive();
                setRandomIndexForFilter();
                session.setFilterSortBy("random");
                offsetLimit = 0;
                matchList.clear();
                matchListAdapter.notifyDataSetChanged();
                getMatchList();
                break;

            case R.id.ly_favorite:
                ((MainActivity) mContext).addFragment(FavoritesFragment.newInstance(), true, R.id.fragment_place);
                break;

            case R.id.iv_profile:
                // ((MainActivity) mContext).addFragment(MyProfileFragment.newInstance(), true, R.id.fragment_place);
                startActivity(new Intent(mContext, MyProfileActivity.class));
                break;

            case R.id.iv_teases:
                matchList.clear();
                ((MainActivity) mContext).addFragment(TeasesFragment.newInstance(), true, R.id.fragment_place);
                break;

            case R.id.rl_notifications:
                if (isAdded()) {
                    startActivity(new Intent(mContext, NotificationActivity.class));
                }
                break;

            case R.id.ly_tease:
                matchList.clear();
                ((MainActivity)mContext).clickTease();
                break;

            case R.id.btn_try_again:
                progress.dismiss();
                getMatchList();
                getNotificationCount();
                break;

        }
    }

    private void setDistanceActive() {
        iv_distance_border.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.active_match_filter_icon_border));
        iv_distance.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.active_distance));

        iv_percentage_border.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.inactive_match_filter_icon_border));
        iv_percentage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.commission));

        iv_random_border.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.inactive_match_filter_icon_border));
        iv_random.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.shuffle));
    }

    private void setRandomActive() {
        iv_distance_border.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.inactive_match_filter_icon_border));
        iv_distance.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.distance));

        iv_percentage_border.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.inactive_match_filter_icon_border));
        iv_percentage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.commission));

        iv_random_border.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.active_match_filter_icon_border));
        iv_random.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.active_shuffle));
    }

    private void setPercentageActive() {
        iv_distance_border.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.inactive_match_filter_icon_border));
        iv_distance.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.distance));

        iv_percentage_border.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.active_match_filter_icon_border));
        iv_percentage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.active_commission));

        iv_random_border.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.inactive_match_filter_icon_border));
        iv_random.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.shuffle));
    }

    // Open Privacy Policy Dialog
    private void openPrivacyPolicyDialog() {
        privacyDialog = new Dialog(mContext);
        privacyDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        privacyDialog.setCancelable(false);
        privacyDialog.setContentView(R.layout.dialog_privacy_policy);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(privacyDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        privacyDialog.getWindow().setAttributes(lWindowParams);

        callUpdatePolicyFlagApi();

        TextView btn_reply = privacyDialog.findViewById(R.id.btn_reply);

        TextView privacy_message = privacyDialog.findViewById(R.id.privacy_message);
        privacy_message.setText(session.getPrivacyPolicy());

       /* String htmlText = " %s ";
        WebView webView = privacyDialog.findViewById(R.id.webView1);
        webView.loadData(String.format(htmlText, session.getPrivacyPolicy()), "text/html", "utf-8");*/

        ImageView dialog_decline_button = privacyDialog.findViewById(R.id.dialog_decline_button);

        //  session.setPolicyDisplay(false);

        dialog_decline_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                privacyDialog.dismiss();
            }
        });

        btn_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                if (isAdded()) {
                    Intent chatIntent = new Intent(mContext, ChatActivity.class);
                    chatIntent.putExtra("otherUID", "1");
                    startActivity(chatIntent);
                    privacyDialog.dismiss();
                }
            }
        });

        privacyDialog.getWindow().setGravity(Gravity.CENTER);
        privacyDialog.show();
    }

    private void callUpdatePolicyFlagApi() {
        if (AppHelper.isConnectingToInternet(mContext)) {
            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    RegistrationInfo registrationInfo = session.getRegistration();
                    registrationInfo.userDetail.policy_flag = "1";
                    session.createRegistration(registrationInfo);
                }

                @Override
                public void ErrorListener(VolleyError error) {

                }

            });
            api.callApi("user/updatePolicyFlag", Request.Method.GET, null);
        } else {
            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.alert_no_network));
        }
    }

    private void getNotificationCount() {
        if (AppHelper.isConnectingToInternet(mContext)) {

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");
                        String Count = js.getString("Count");

                        if (status.equals("success")) {
                            progress.dismiss();

                            int count = Integer.parseInt(Count);
                            MainActivity.notificationCount = count;

                            if (count != 0) {
                                if (count < 100) {
                                    tv_notification_count.setText(Count);
                                } else {
                                    tv_notification_count.setText(new StringBuilder().append(Count).append("+").toString());
                                }
                                tv_notification_count.setVisibility(View.VISIBLE);
                            } else {
                                tv_notification_count.setVisibility(View.GONE);
                            }

                        } else {
                            progress.dismiss();
                            CustomToast.getInstance(mContext).showToast(mContext, message);
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
            api.callApi("user/getNotificationCount", Request.Method.GET, null);
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
    public void onPause() {
        super.onPause();
        if (progress != null) {
            progress.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        if (progress.isShowing()) {
            progress.dismiss();
        }
    }


    // Alert dialog to ask discard the changes
    private void openProfileIncompleteDialog() {
        final Dialog discardDialog = new Dialog(mContext);
        discardDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        discardDialog.setContentView(R.layout.dialog_profile_incomplete);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(discardDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        discardDialog.getWindow().setAttributes(lWindowParams);

        ///ImageView dialog_decline_button = discardDialog.findViewById(R.id.dialog_decline_button);
        //TextView btn_cancel = discardDialog.findViewById(R.id.btn_cancel);
        TextView btn_alert = discardDialog.findViewById(R.id.btn_alert);
        TextView alert_message = discardDialog.findViewById(R.id.alert_message);

        alert_message.setText(R.string.profile_incomplete_message);

        /*dialog_decline_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                discardDialog.dismiss();
            }
        });*/

        //btn_cancel.setVisibility(View.GONE);

       /* btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

              *//*  selected_looking_for = "";
                selected_looking_id = "";*//*
                discardDialog.dismiss();
            }
        });*/


        btn_alert.setGravity(Gravity.CENTER);


        btn_alert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

//                selected_looking_for = "";
//                selected_looking_id = "";
//                setBasicInfoData();
               // ((EditProfileActivity) mContext).replaceFragment(EditOtherInfoFragment.newInstance(), true, R.id.edit_fragment_place);
                discardDialog.dismiss();
            }
        });

        discardDialog.getWindow().setGravity(Gravity.CENTER);
        discardDialog.show();
    }





}
