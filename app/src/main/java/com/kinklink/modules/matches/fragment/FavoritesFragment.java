package com.kinklink.modules.matches.fragment;

import android.content.Context;
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
import com.kinklink.modules.matches.adapter.FavoritesAdapter;
import com.kinklink.modules.matches.listener.EndlessRecyclerViewScrollListener;
import com.kinklink.modules.matches.model.FavoriteModel;
import com.kinklink.server_task.WebService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FavoritesFragment extends Fragment implements View.OnClickListener {
    private Context mContext;
    private TextView action_bar_heading, btn_try_again;
    private RecyclerView favorite_recycler_view;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout ly_no_network, ly_no_favorites;
    private TextView tv_your_favorite, tv_who_favorite_me;
    private FavoritesAdapter favoritesAdapter;

    private int requestType = 0, offsetLimit = 0;
    private LinearLayout ly_favorite_list;
    private ArrayList<FavoriteModel> favoriteList;
    private int activeFragId;
    private Map<String, OnlineInfo> onlineList;
    private Progress progress;

    // variable to track event time
    private long mLastClickTime = 0;


    public FavoritesFragment() {
    }

    public static FavoritesFragment newInstance() {
        Bundle args = new Bundle();
        FavoritesFragment fragment = new FavoritesFragment();
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
        View view = inflater.inflate(R.layout.fragment_teases, container, false);
        init(view);

        progress = new Progress(mContext);
        favoriteList = new ArrayList<>();
        onlineList = new HashMap<>();

        action_bar_heading.setText(getString(R.string.favorites));

        // Setting Your favorites tab active
        activeFragId = R.id.tv_who_liked_me;
        requestType = 1;           // Request Type is set to 0 to get Your Favorites List

        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();
        }

        // Api call to get Favorites List
        callGetFavoritesListApi();

        // Setting adapter to get favorites list
        setGetFavoriteListAdapter();

        // Get Online Table data from firebase
        getOnlineStatusFromFirebase();

        return view;
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

    // Setting adapter to get favorites list
    private void setGetFavoriteListAdapter() {
        // Favorite List recycler adapter
        favoritesAdapter = new FavoritesAdapter(mContext, favoriteList);
        GridLayoutManager layoutManager = new GridLayoutManager(mContext, 2);
        favorite_recycler_view.setLayoutManager(layoutManager);
        favorite_recycler_view.setAdapter(favoritesAdapter);


        // Pull To refresh listener
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tv_your_favorite.setEnabled(false);
                tv_who_favorite_me.setEnabled(false);

                // cancel the Visual indication of a refresh
                swipeRefreshLayout.setRefreshing(false);
                offsetLimit = 0;
                favoriteList.clear();
                favoritesAdapter.notifyDataSetChanged();
                KinkLink.getInstance().cancelAllRequests(KinkLink.TAG);
                callGetFavoritesListApi();
            }
        });

        // Endless Recycler Scroll Listener Pagination
        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (page != 1) {
                    progress.show();
                }
                callGetFavoritesListApi();
            }
        };

        // Adds the scroll listener to RecyclerView
        favorite_recycler_view.addOnScrollListener(scrollListener);
    }

    private void init(View view) {
        action_bar_heading = ((MainActivity) mContext).findViewById(R.id.action_bar_heading);

        ImageView iv_back = ((MainActivity) mContext).findViewById(R.id.iv_back);
        ImageView iv_profile = ((MainActivity) mContext).findViewById(R.id.iv_profile);
        iv_profile.setVisibility(View.GONE);

        ImageView iv_teases = ((MainActivity) mContext).findViewById(R.id.iv_teases);
        iv_teases.setVisibility(View.GONE);

        RelativeLayout rl_notifications = ((MainActivity) mContext).findViewById(R.id.rl_notifications);
        rl_notifications.setVisibility(View.GONE);

        ImageView iv_settings = ((MainActivity) mContext).findViewById(R.id.iv_settings);
        iv_settings.setVisibility(View.GONE);

        RelativeLayout bottom_menu = ((MainActivity) mContext).findViewById(R.id.bottomMenu);
        swipeRefreshLayout = view.findViewById(R.id.simpleSwipeRefreshLayout);
        favorite_recycler_view = view.findViewById(R.id.likes_recycler_view);
        ly_no_network = ((MainActivity) mContext).findViewById(R.id.ly_no_network);
        btn_try_again = ((MainActivity) mContext).findViewById(R.id.btn_try_again);

        iv_back.setVisibility(View.VISIBLE);
        bottom_menu.setVisibility(View.GONE);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        tv_your_favorite = view.findViewById(R.id.tv_who_you_like);
        tv_who_favorite_me = view.findViewById(R.id.tv_who_liked_me);
        ly_favorite_list = view.findViewById(R.id.ly_likes_list);

        tv_your_favorite.setText(getResources().getString(R.string.your_favorite));
        tv_who_favorite_me.setText(getResources().getString(R.string.who_favorite_me));

        ly_no_favorites = view.findViewById(R.id.ly_no_likes);
        ImageView iv_no_record = view.findViewById(R.id.iv_no_record);
        TextView tv_no_record_txt = view.findViewById(R.id.tv_no_record_txt);
        iv_no_record.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.ic_no_stars));
        tv_no_record_txt.setText(getResources().getString(R.string.no_record_found));

        iv_back.setOnClickListener(this);
        tv_your_favorite.setOnClickListener(this);
        tv_who_favorite_me.setOnClickListener(this);
    }

    // Set Online Offline status in Online Info type list
    private void setOnlineData(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue(OnlineInfo.class) != null) {
            OnlineInfo info = dataSnapshot.getValue(OnlineInfo.class);
            onlineList.put(dataSnapshot.getKey(), info);

            for (FavoriteModel model : favoriteList) {
                if (dataSnapshot.getKey().equals(model.userId)) {
                    assert info != null;
                    model.isOnline = info.lastOnline;
                }
            }

            favoritesAdapter.notifyDataSetChanged();
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
            case R.id.iv_back:
                ((MainActivity) mContext).onBackPressed();
                break;

            case R.id.tv_who_you_like:       // Your Favorites Click
                if (activeFragId != R.id.tv_who_you_like) {
                    activeFragId = R.id.tv_who_you_like;
                    tv_your_favorite.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tv_who_favorite_me.setTextColor(getResources().getColor(R.color.field_text_color));
                    requestType = 0;    // Request Type is set to 0 to get Your Favorites List
                    favoriteList.clear();
                    offsetLimit = 0;
                    favorite_recycler_view.setAdapter(favoritesAdapter);
                    callGetFavoritesListApi();
                }
                break;

            case R.id.tv_who_liked_me:         // Who favorites me Click
                if (activeFragId != R.id.tv_who_liked_me) {
                    activeFragId = R.id.tv_who_liked_me;
                    tv_your_favorite.setTextColor(getResources().getColor(R.color.field_text_color));
                    tv_who_favorite_me.setTextColor(getResources().getColor(R.color.colorPrimary));
                    requestType = 1;     // Request Type is set to 1 to get Who favorites me List
                    favoriteList.clear();
                    offsetLimit = 0;
                    favorite_recycler_view.setAdapter(favoritesAdapter);
                    callGetFavoritesListApi();
                }
                break;

            case R.id.btn_try_again:
                callGetFavoritesListApi();
                break;
        }
    }

    // Api call to get Likes List
    private void callGetFavoritesListApi() {
        if (AppHelper.isConnectingToInternet(mContext)) {

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");

                        tv_your_favorite.setEnabled(true);
                        tv_who_favorite_me.setEnabled(true);

                        if (status.equals("success")) {
                            /*Gson gson = new Gson();
                            FavoriteModel model = gson.fromJson(String.valueOf(js), FavoriteModel.class);
                            favoriteList.addAll(model.favoriteList);*/

                            progress.dismiss();
                            JSONArray jsonArray = js.getJSONArray("favoriteList");
                            FavoriteModel model;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                model = new FavoriteModel();
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
                                favoriteList.add(model);
                            }

                            ly_favorite_list.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            favorite_recycler_view.setVisibility(View.VISIBLE);
                            ly_no_favorites.setVisibility(View.GONE);
                            offsetLimit += 10;
                            favoritesAdapter.notifyDataSetChanged();
                        } else {
                            progress.dismiss();
                            if (favoriteList.size() == 0) {
                                favorite_recycler_view.setVisibility(View.GONE);
                                ly_no_favorites.setVisibility(View.VISIBLE);
                                ly_favorite_list.setVisibility(View.VISIBLE);
                                ly_no_network.setVisibility(View.GONE);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();ly_favorite_list.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    ly_favorite_list.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                }

            });
            api.callApi("user/getFavoriteAndFavoriteMeList?offset=" + offsetLimit + "&limit=10&requestType=" + requestType, Request.Method.GET, null);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            ly_favorite_list.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(this);
        }
    }

    public void getFavoritesBackCall() {
        offsetLimit = 0;
        requestType = 1;
        favoriteList.clear();
        favoritesAdapter.notifyDataSetChanged();
        KinkLink.getInstance().cancelAllRequests(KinkLink.TAG);
        callGetFavoritesListApi();
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
