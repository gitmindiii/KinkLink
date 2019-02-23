package com.kinklink.modules.matches.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import android.util.Log;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kinklink.R;
import com.kinklink.application.KinkLink;
import com.kinklink.fcm.FcmNotificationBuilder;
import com.kinklink.helper.AppHelper;
import com.kinklink.helper.Constant;
import com.kinklink.helper.CustomToast;
import com.kinklink.helper.Progress;
import com.kinklink.modules.authentication.model.FirebaseUserModel;
import com.kinklink.modules.authentication.model.OnlineInfo;
import com.kinklink.modules.authentication.model.RegistrationInfo;
import com.kinklink.modules.chat.model.Chat;
import com.kinklink.modules.matches.activity.MainActivity;
import com.kinklink.modules.matches.activity.MyProfileActivity;
import com.kinklink.modules.matches.adapter.LikesAdapter;
import com.kinklink.modules.matches.listener.AdapterViewPositionListener;
import com.kinklink.modules.matches.listener.EndlessRecyclerViewScrollListener;
import com.kinklink.modules.matches.model.LikesModel;
import com.kinklink.server_task.WebService;
import com.kinklink.session.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeasesFragment extends Fragment implements View.OnClickListener {
    private Context mContext;
    private TextView action_bar_heading, btn_try_again;
    private RecyclerView likes_recycler_view;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout ly_no_network, ly_no_likes;
    private TextView tv_who_you_like, tv_who_liked_me;
    private LikesAdapter likesAdapter;

    private int requestType = 0, offsetLimit = 0;
    private LinearLayout ly_likes_list;
    public static int tab_type = 0;
    private String myUId, myName="", myProfileImage="", otherUId, otherName="", otherProfileImage="";
    private ArrayList<LikesModel> likesList;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;
    private FirebaseUserModel otherUserInfo;
    private Uri image_FirebaseURL;
    private Session session;
    private String chatNode, blockedId = "";
    private int activeFragId;
    private Map<String, OnlineInfo> onlineList;
    private Progress progress;

    // variable to track event time
    private long mLastClickTime = 0;

    public TeasesFragment() {
    }

    public static TeasesFragment newInstance() {
        Bundle args = new Bundle();
        TeasesFragment fragment = new TeasesFragment();
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
        likesList = new ArrayList<>();
        onlineList = new HashMap<>();

        session = new Session(mContext);
        firebaseDatabase = FirebaseDatabase.getInstance();
        action_bar_heading.setText(getString(R.string.likes));



        // Setting Who You Like tab active
        activeFragId = R.id.tv_who_liked_me;
        requestType = 1;     // Request Type is set to 0 to get Who You Like List

        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();
        }

        // Api call to get Likes List
        callGetLikesListApi();


        // Setting adapter to get likes list
        setGetLikesListAdapter();

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

    //Setting adapter to get Likes List
    private void setGetLikesListAdapter() {
        // Likes List recycler adapter
        likesAdapter = new LikesAdapter(mContext, likesList, new AdapterViewPositionListener() {
            @Override
            public void getAcceptClick(int position) {
                String requestBy=likesList.get(position).userId;
                otherUId=likesList.get(position).userId;
                callAcceptRejectOfferApi(requestBy,"1",position);
            }

            @Override
            public void getRejectClick(int position) {
                String requestBy=likesList.get(position).userId;
                otherUId=likesList.get(position).userId;
                callAcceptRejectOfferApi(requestBy,"2",position);
            }

            @Override
            public void getCounterClick(int position) {

            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        likes_recycler_view.setLayoutManager(layoutManager);
        likes_recycler_view.setAdapter(likesAdapter);

        // Pull To refresh listener
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tv_who_you_like.setEnabled(false);
                tv_who_liked_me.setEnabled(false);

                // cancel the Visual indication of a refresh
                swipeRefreshLayout.setRefreshing(false);
                offsetLimit = 0;
                likesList.clear();
                likesAdapter.notifyDataSetChanged();
                KinkLink.getInstance().cancelAllRequests(KinkLink.TAG);
                callGetLikesListApi();
            }
        });

        // Endless Recycler Scroll Listener Pagination
        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                progress.show();
                callGetLikesListApi();
            }
        };

        // Adds the scroll listener to RecyclerView
        likes_recycler_view.addOnScrollListener(scrollListener);
    }

    // Set Online Offline status in Online Info type list
    private void setOnlineData(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue(OnlineInfo.class) != null) {
            OnlineInfo info = dataSnapshot.getValue(OnlineInfo.class);
            onlineList.put(dataSnapshot.getKey(), info);

            for (LikesModel model : likesList) {
                if (dataSnapshot.getKey().equals(model.userId)) {
                    assert info != null;
                    model.isOnline = info.lastOnline;
                }
            }

            likesAdapter.notifyDataSetChanged();
        }
    }

    private void sendMessage() {
        String pushkey = firebaseDatabase.getReference().child(Constant.CHAT_ROOMS_TABLE).child(chatNode).push().getKey();
        String msg = "";
        String firebaseToken = FirebaseInstanceId.getInstance().getToken();

       /* if (msg.equals("")) {
            return;
        }*/

        Chat otherChat = new Chat();
        otherChat.deleteby = "";
        otherChat.firebaseToken = firebaseToken;

        if (image_FirebaseURL != null) {
            otherChat.imageUrl = image_FirebaseURL.toString();
            otherChat.message = "";
            otherChat.image = 1;
        } else {
            otherChat.imageUrl = "";
            otherChat.message = "Get your Kink on! Messaging UNLOCKED";
            otherChat.image = 2;
        }

        otherChat.name = otherName.toLowerCase();
        otherChat.profilePic = otherProfileImage;
        otherChat.timeStamp = ServerValue.TIMESTAMP;
        otherChat.uid = otherUId;
        otherChat.lastMsg = myUId;

        Chat myChat = new Chat();
        myChat.deleteby = "";
        myChat.firebaseToken = firebaseToken;

        if (image_FirebaseURL != null) {
            myChat.imageUrl = image_FirebaseURL.toString();
            myChat.message = "";
            myChat.image = 1;

        } else {
            myChat.imageUrl = "";
            myChat.message = "Get your Kink on! Messaging UNLOCKED";
            myChat.image = 2;

            //ed_message.setText("");
        }


        myChat.name = myName;
        myChat.profilePic = myProfileImage;
        myChat.timeStamp = ServerValue.TIMESTAMP;
        myChat.uid = myUId;
        myChat.lastMsg = myUId;

        firebaseDatabase.getReference().child(Constant.CHAT_ROOMS_TABLE).child(chatNode).child(pushkey).setValue(myChat);
        firebaseDatabase.getReference().child(Constant.CHAT_HISTORY_TABLE).child(myUId).child(otherUId).setValue(otherChat);
        firebaseDatabase.getReference().child(Constant.CHAT_HISTORY_TABLE).child(otherUId).child(myUId).setValue(myChat);

        if (image_FirebaseURL != null) {
           // sendPushNotificationToReceiver(myName, "Image", myName, myUId, firebaseToken, chatNode);
          //  sendPushNotificationToSender(otherName, "Image", otherName, otherUId, firebaseToken, chatNode);
        } else {

            if (firebaseToken!=null){
                //sendPushNotificationToReceiver(myName, msg, myName, myUId, firebaseToken, chatNode);
               // sendPushNotificationToSender(otherName, msg, otherName, otherUId, firebaseToken, chatNode);

            }

        }

        image_FirebaseURL = null;
        progress.dismiss();
        //chat_recycler_view.scrollToPosition(chatList.size() - 1);

    }


    private void sendPushNotificationToReceiver(String title, String message, String username, String uid, String firebaseToken, String chatNode) {
        FcmNotificationBuilder.initialize()
                .title(title)
                .message(message)
                .username(username)
                .uid(uid)
                .firebaseToken(firebaseToken)
                .chatNode(chatNode)
                .receiverFirebaseToken(otherUserInfo.firebaseToken).send();
    }
    private void sendPushNotificationToSender(String title, String message, String username, String uid, String firebaseToken, String chatNode) {
        FcmNotificationBuilder.initialize()
                .title(title)
                .message(message)
                .username(username)
                .uid(uid)
                .firebaseToken(firebaseToken)
                .chatNode(chatNode)
                .receiverFirebaseToken(otherUserInfo.firebaseToken).send();
    }





    private void init(View view) {
        action_bar_heading = ((MainActivity) mContext).findViewById(R.id.action_bar_heading);

        ImageView iv_back = ((MainActivity) mContext).findViewById(R.id.iv_back);
        ImageView iv_profile = ((MainActivity) mContext).findViewById(R.id.iv_profile);
        iv_profile.setVisibility(View.VISIBLE);
        iv_profile.setOnClickListener(this);

        ImageView iv_teases = ((MainActivity) mContext).findViewById(R.id.iv_teases);
        iv_teases.setVisibility(View.GONE);

        RelativeLayout rl_notifications = ((MainActivity) mContext).findViewById(R.id.rl_notifications);
        rl_notifications.setVisibility(View.GONE);

        ImageView iv_settings = ((MainActivity) mContext).findViewById(R.id.iv_settings);
        iv_settings.setVisibility(View.GONE);

        RelativeLayout bottom_menu = ((MainActivity) mContext).findViewById(R.id.bottomMenu);
        swipeRefreshLayout = view.findViewById(R.id.simpleSwipeRefreshLayout);
        likes_recycler_view = view.findViewById(R.id.likes_recycler_view);
        ly_no_network = ((MainActivity) mContext).findViewById(R.id.ly_no_network);
        btn_try_again = ((MainActivity) mContext).findViewById(R.id.btn_try_again);

        iv_back.setVisibility(View.GONE);
        bottom_menu.setVisibility(View.VISIBLE);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        tv_who_you_like = view.findViewById(R.id.tv_who_you_like);
        tv_who_liked_me = view.findViewById(R.id.tv_who_liked_me);
        ly_likes_list = view.findViewById(R.id.ly_likes_list);

        tv_who_you_like.setText(getResources().getString(R.string.who_you_like));
        tv_who_liked_me.setText(getResources().getString(R.string.who_liked_me));

        ly_no_likes = view.findViewById(R.id.ly_no_likes);
        ImageView iv_no_record = view.findViewById(R.id.iv_no_record);
        TextView tv_no_record_txt = view.findViewById(R.id.tv_no_record_txt);
        iv_no_record.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_no_like));
        tv_no_record_txt.setText(getResources().getString(R.string.no_record_found));

        iv_back.setOnClickListener(this);
        tv_who_you_like.setOnClickListener(this);
        tv_who_liked_me.setOnClickListener(this);
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
            case R.id.iv_back:    // Back Icon Click
                ((MainActivity) mContext).onBackPressed();
                break;

            case R.id.tv_who_you_like:    // Who You Like Click
                if (activeFragId != R.id.tv_who_you_like) {
                    activeFragId = R.id.tv_who_you_like;
                    tv_who_you_like.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                    tv_who_liked_me.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));
                    requestType = 0;    // Request Type is set to 0 to get Who You Like List
                    likesList.clear();
                    offsetLimit = 0;
                    likes_recycler_view.setAdapter(likesAdapter);
                    callGetLikesListApi();
                }
                break;

            case R.id.tv_who_liked_me:      // Who Liked you Click
                if (activeFragId != R.id.tv_who_liked_me) {
                    activeFragId = R.id.tv_who_liked_me;
                    tv_who_you_like.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));
                    tv_who_liked_me.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                    requestType = 1;    // Request Type is set to 1 to get Who Liked you List
                    likesList.clear();
                    offsetLimit = 0;
                    likes_recycler_view.setAdapter(likesAdapter);
                    callGetLikesListApi();
                }
                break;

            case R.id.btn_try_again:
                callGetLikesListApi();
                break;

            case R.id.iv_profile:
                startActivity(new Intent(mContext,MyProfileActivity.class));
        }
    }

    // Api call to get Likes List
    public void callGetLikesListApi() {
        if (AppHelper.isConnectingToInternet(mContext)) {

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        tv_who_you_like.setEnabled(true);
                        tv_who_liked_me.setEnabled(true);

                        if (status.equals("success")) {
                            progress.dismiss();


                            JSONArray jsonArray = js.getJSONArray("likeList");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                LikesModel model;   model = new LikesModel();
                                model.timeOut = js.getString("timeOut");
                                JSONObject object = jsonArray.getJSONObject(i);
                                model.userId = object.getString("userId");
                                model.full_name = object.getString("full_name");
                                model.gender = object.getString("gender");
                                model.is_verify = object.getString("is_verify");
                                model.age = object.getString("age");
                                model.image = object.getString("image");
                                model.hrdiff = object.getString("hrdiff");
                                model.current_time = object.getString("current_time");
                                model.created_on = object.getString("created_on");
                                model.city = object.getString("city");
                                model.like_status = object.getString("like_status");
                                model.user_image_id = object.getString("user_image_id");
                                model.timeOut=js.getString("timeOut");
                                model.requestType=String.valueOf(requestType);
                                Pattern p = Pattern.compile("(\\d+):(\\d+):(\\d+):(\\d+)");
                                String time=js.getString("timeOut");
                                String pattern=time+":"+"00"+":"+"00"+":"+"00";
                                Matcher m = p.matcher(pattern);
                                if (m.matches()) {
                                  //  int day=Integer.parseInt(m.group(0));
                                    int day = Integer.parseInt(m.group(1));
                                    int hour = Integer.parseInt(m.group(2));
                                    int min = Integer.parseInt(m.group(3));
                                    int sec = Integer.parseInt(m.group(4));

                                    //   long ms = (long) hrs * 60 * 60 * 1000 + min * 60 * 1000;

                                    long ms = TimeUnit.DAYS.toMillis(day) + TimeUnit.HOURS.toMillis(hour) +TimeUnit.MINUTES.toMillis(min) + TimeUnit.SECONDS.toMillis(sec);

                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(mContext.getResources().getString(R.string.date_time_format), Locale.US);

                                    try {
                                        Date date1 = simpleDateFormat.parse(model.created_on);
                                        Date date2 = simpleDateFormat.parse(model.current_time);
                                        long endDate = date1.getTime() + ms;

                                        if (date2.getTime() < endDate) {
                                            long timer = endDate - date2.getTime();
                                            model.timerTime = timer + System.currentTimeMillis();
                                        } else {
                                            model.timerTime = 0;
                                        }

                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                }

                                likesList.add(model);




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

                            }


                            ly_likes_list.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            likes_recycler_view.setVisibility(View.VISIBLE);
                            ly_no_likes.setVisibility(View.GONE);
                            offsetLimit += 10;
                            likesAdapter.notifyDataSetChanged();
                        } else if (status.equals("fail")){
                            progress.dismiss();
                            if (likesList.size() == 0) {
                                likes_recycler_view.setVisibility(View.GONE);
                                ly_no_likes.setVisibility(View.VISIBLE);
                                ly_likes_list.setVisibility(View.VISIBLE);
                                ly_no_network.setVisibility(View.GONE);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        ly_likes_list.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    ly_likes_list.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                }

            });
            api.callApi("user/getLikeAndLikeMeList?offset=" + offsetLimit + "&limit=10&requestType=" + requestType, Request.Method.GET, null);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            ly_likes_list.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(this);
        }
    }


    // Api to Accept or reject Offer
    private void callAcceptRejectOfferApi(final String requestBy, final String statusRequest, final int position) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            Map<String, String> map = new HashMap<>();
            map.put("request_by", requestBy);
            map.put("status_request", statusRequest);

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        ly_likes_list.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);

                        if (status.equals("success")) {
                            progress.dismiss();

                            if (statusRequest.equals("2")) {
                                if (message.equals("Tease request accepted.")) {
                                    likesList.get(position).like_status = "1";
                                    createChatNode(requestBy);
                                    sendMessage();
                                    likesList.remove(position);
                                    if (likesList.size()==0){
                                        likes_recycler_view.setVisibility(View.GONE);
                                        ly_no_likes.setVisibility(View.VISIBLE);
                                        ly_likes_list.setVisibility(View.VISIBLE);
                                        ly_no_network.setVisibility(View.GONE);
                                    }
                                   // callGetLikesListApi();
                                } else if (message.equals("Tease request declined.")) {
                                    likesList.get(position).like_status = "2";

                                    likesList.remove(position);
                                    if (likesList.size()==0){
                                        likes_recycler_view.setVisibility(View.GONE);
                                        ly_no_likes.setVisibility(View.VISIBLE);
                                        ly_likes_list.setVisibility(View.VISIBLE);
                                        ly_no_network.setVisibility(View.GONE);
                                    }
                                   // callGetLikesListApi();
                                }

                                likesAdapter.notifyDataSetChanged();

                                } else if (statusRequest.equals("1")) {
                                if (message.equals("Tease request accepted.")) {
                                    likesList.get(position).like_status = "1";
                                    createChatNode(requestBy);
                                    sendMessage();
                                    likesList.remove(position);
                                    if (likesList.size()==0){
                                        likes_recycler_view.setVisibility(View.GONE);
                                        ly_no_likes.setVisibility(View.VISIBLE);
                                        ly_likes_list.setVisibility(View.VISIBLE);
                                        ly_no_network.setVisibility(View.GONE);
                                    }
                                    //callGetLikesListApi();
                                } else if (message.equals("Tease request declined.")) {
                                    likesList.get(position).like_status = "2";

                                    likesList.remove(position);
                                    if (likesList.size()==0){
                                        likes_recycler_view.setVisibility(View.GONE);
                                        ly_no_likes.setVisibility(View.VISIBLE);
                                        ly_likes_list.setVisibility(View.VISIBLE);
                                        ly_no_network.setVisibility(View.GONE);
                                    }
                                    //callGetLikesListApi();
                                }


                                likesAdapter.notifyDataSetChanged();



                                //likesAdapter.notifyDataSetChanged();

                              /*  offsetLimit = 0;
                                KinkLink.getInstance().cancelPendingRequests();
                                offerList.clear();
                                myOfferListAdapter.notifyDataSetChanged();
                                callGetOfferListApi();*/
                            }
                        } else {
                            progress.dismiss();
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        ly_likes_list.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);
                        progress.dismiss();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    ly_likes_list.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                    progress.dismiss();
                }

            });
            api.callApi("user/acceptIgnoreTease", Request.Method.POST, map);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            ly_likes_list.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callAcceptRejectOfferApi(statusRequest, requestBy, position);
                }
            });
        }
    }

    //create note for chatroom
    private String gettingNotes(String otherUId) {
        int myUid_ = Integer.parseInt(myUId);
        int otherUID_ = Integer.parseInt(otherUId);

        if (myUid_ < otherUID_) {
            chatNode = myUId + "_" + otherUId;
        } else {
            chatNode = otherUId + "_" + myUId;
        }
        return chatNode;
    }

    public void getLikesBackCall() {
        /*offsetLimit = 0;
        likesList.clear();
        likesAdapter.notifyDataSetChanged();
        callGetLikesListApi();*/

        offsetLimit = 0;
        requestType = 1;
        likesList.clear();
        likesAdapter.notifyDataSetChanged();
        KinkLink.getInstance().cancelAllRequests(KinkLink.TAG);
        callGetLikesListApi();
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

    // Get other user data from User table from firebase with help of UID
    private void gettingDataFromUserTable(String otherUId) {
        if (otherUId != null) {
            firebaseDatabase.getReference().child(Constant.USER_TABLE).child(otherUId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    otherUserInfo = dataSnapshot.getValue(FirebaseUserModel.class);

                    assert otherUserInfo != null;
                    otherName = otherUserInfo.name;

                    String[] nameArray = otherName.split(" ");
                    StringBuilder builder = new StringBuilder();
                    for (String s : nameArray) {
                        String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                        builder.append(cap).append(" ");
                    }
                    //tv_user_name.setText(builder.toString());

                    if (!otherUserInfo.profilePic.equals("")) {
                        otherProfileImage = otherUserInfo.profilePic;
                    } else {
                        otherProfileImage = "";
                    }

                   // chattingAdapter.getImage(otherProfileImage);

                   // ly_user_chat.setVisibility(View.VISIBLE);
                   // ly_no_network.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


    public void createChatNode(String otherUserId){
        RegistrationInfo registrationInfo = session.getRegistration();
        myUId = registrationInfo.userDetail.userId;
        myName = registrationInfo.userDetail.full_name;
        if (registrationInfo.userDetail.images.size() > 0) {
            myProfileImage = registrationInfo.userDetail.images.get(0).image;
        } else {
            myProfileImage = "";
        }
        gettingDataFromUserTable(otherUserId);
        if (otherUId != null) {
            //create note for chatroom
            chatNode = gettingNotes(otherUserId);
        }
        reference = firebaseDatabase.getReference().child(Constant.CHAT_ROOMS_TABLE).child(chatNode);




    }


}
