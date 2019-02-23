package com.kinklink.modules.chat.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.BaseKeyListener;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kinklink.R;
import com.kinklink.application.KinkLink;
import com.kinklink.fcm.FcmNotificationBuilder;
import com.kinklink.helper.AppHelper;
import com.kinklink.helper.Constant;
import com.kinklink.helper.CustomToast;
import com.kinklink.helper.Progress;
import com.kinklink.helper.TimeSpan;
import com.kinklink.image.picker.ImagePicker;
import com.kinklink.modules.authentication.listener.AdapterPositionListener;
import com.kinklink.modules.authentication.model.FirebaseUserModel;
import com.kinklink.modules.authentication.model.RegistrationInfo;
import com.kinklink.modules.chat.adapter.ChattingAdapter;
import com.kinklink.modules.chat.model.Chat;
import com.kinklink.modules.matches.activity.AdminProfileActivity;
import com.kinklink.modules.matches.activity.MatchProfileActivity;
import com.kinklink.server_task.WebService;
import com.kinklink.session.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView iv_back, iv_pick_image, iv_send_msg, iv_set_emoji, iv_menu,img_with_chat;
    private TextView tv_user_name, tv_chat_delete, tv_user_block, tv_chat_date;
    public RecyclerView chat_recycler_view;
    private EditText ed_message;
    private LinearLayout ly_no_network, ly_block_user;
    private RelativeLayout ly_user_chat, rl_chat;

    private Progress progress;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;
    private Map<String, Chat> map;

    private String myUId, myName, myProfileImage, otherUId, otherName, otherProfileImage;
    private String chatNode, blockedId = "";
    private FirebaseUserModel otherUserInfo;
    private ArrayList<Chat> chatList;
    private ChattingAdapter chattingAdapter;
    private Uri image_FirebaseURL;
    private CardView cv_chat_menu;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout rl_chat_tool_bar;

    // variable to track event time
    private long mLastClickTime = 0;

    private String lastIndexmessagekey;
    private long deleteTime;
    private boolean ischeck = true;
    private int listIndex = 0, increment = 0, totalCount = 0, tempCount = 0;
    public Boolean isCompleteChatLoad = false, isLoadFirst = true;
    private LinearLayoutManager linearLayoutManager;
    int unread_count=0;

    View view_tease_expire;
    RelativeLayout rel_view_chat;

    RelativeLayout rl_send_message,rl_user_deleted_view;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        init();

        progress = new Progress(ChatActivity.this);
        Session session = new Session(ChatActivity.this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        map = new HashMap<>();
        chatList = new ArrayList<>();
        RegistrationInfo registrationInfo = session.getRegistration();
        myUId = registrationInfo.userDetail.userId;
        myName = registrationInfo.userDetail.full_name;
        if (registrationInfo.userDetail.images.size() > 0) {
            myProfileImage = registrationInfo.userDetail.images.get(0).image;
        } else {
            myProfileImage = "";
        }

        if (getIntent().getExtras() != null) {
            otherUId = getIntent().getStringExtra("otherUID");
            Constant.ChatOpponentId = otherUId;

            if (otherUId != null) {
                if (AppHelper.isConnectingToInternet(this)) {
                    gettingDataFromUserTable(otherUId);

                    if (otherUId.equals("1")) {
                        iv_menu.setVisibility(View.GONE);
                    } else {
                        iv_menu.setVisibility(View.VISIBLE);
                    }
                } else {
                    ly_user_chat.setVisibility(View.GONE);
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

                            gettingDataFromUserTable(otherUId);
                        }
                    });
                }
            }
        }

        if (otherUId != null) {
            //create note for chatroom
            chatNode = gettingNotes();
        }
        reference = firebaseDatabase.getReference().child(Constant.CHAT_ROOMS_TABLE).child(chatNode);

        // Setting Chatting Adapter
        setChatAdapter();
        getTime();

        //Click Listeners
        iv_back.setOnClickListener(this);
        iv_send_msg.setOnClickListener(this);
        iv_pick_image.setOnClickListener(this);
        iv_set_emoji.setOnClickListener(this);
        iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                if (cv_chat_menu.getVisibility() == View.VISIBLE) {
                    cv_chat_menu.setVisibility(View.GONE);
                } else if (cv_chat_menu.getVisibility() == View.GONE) {
                    getBlockUserData();
                    cv_chat_menu.setVisibility(View.VISIBLE);
                }
            }
        });

        rl_chat.setOnClickListener(this);

        ly_block_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                cv_chat_menu.setVisibility(View.GONE);

                String[] nameArray = otherUserInfo.name.split(" ");
                StringBuilder builder = new StringBuilder();
                for (String s : nameArray) {
                    String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                    builder.append(cap).append(" ");
                }

                String otherUserName = builder.toString();

                if (blockedId.equals("")) {
                    blockChatDialog(getString(R.string.block), "Block " + otherUserName + "? Blocked user will no longer be able to send you messages.");
                } else if (blockedId.equals(myUId)) {
                    blockChatDialog(getString(R.string.unblock), "Are you sure, you want to Unblock " + otherUserName + "?");
                } else if (blockedId.equals(otherUId)) {
                    blockChatDialog(getString(R.string.block), "Block " + otherUserName + "? Blocked user will no longer be able to send you messages.");
                } else if (blockedId.equals("Both")) {
                    blockChatDialog(getString(R.string.unblock), "Are you sure, you want to Unblock " + otherUserName + "?");
                }
            }
        });

        tv_chat_delete.setOnClickListener(this);

        //  chat_recycler_view.scrollToPosition(chatList.size() - 1);

    }

    private void init() {
        img_with_chat=findViewById(R.id.img_with_chat);
        rl_send_message=findViewById(R.id.rl_send_message);
        rl_user_deleted_view=findViewById(R.id.rl_user_deleted_view);

         view_tease_expire=findViewById(R.id.view_tease_expire);
         rel_view_chat=findViewById(R.id.rel_view_chat);


        iv_back = findViewById(R.id.iv_back);
        rl_chat_tool_bar = findViewById(R.id.rl_chat_tool_bar);
        iv_pick_image = findViewById(R.id.iv_pick_image);
        iv_menu = findViewById(R.id.iv_menu);
        iv_set_emoji = findViewById(R.id.iv_set_emoji);
        iv_send_msg = findViewById(R.id.iv_send_msg);
        tv_user_name = findViewById(R.id.tv_user_name);
        ly_user_chat = findViewById(R.id.ly_user_chat);
        cv_chat_menu = findViewById(R.id.cv_chat_menu);
        chat_recycler_view = findViewById(R.id.chat_recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        ed_message = findViewById(R.id.ed_message);

        tv_chat_date = findViewById(R.id.tv_chat_date);
        rl_chat = findViewById(R.id.rl_chat);
        ly_block_user = findViewById(R.id.ly_block_user);
        tv_user_block = findViewById(R.id.tv_user_block);
        tv_chat_delete = findViewById(R.id.tv_chat_delete);
        rl_chat_tool_bar.setOnClickListener(this);

        ly_no_network = findViewById(R.id.ly_no_network);

    }

    private void setChatAdapter() {

        chattingAdapter = new ChattingAdapter(this, chatList, myUId, new AdapterPositionListener() {
            @Override
            public void getPosition(final int position) {
               /* chat_recycler_view.setOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                            if (chatList.get(position).image == 2) {
                                tv_chat_date.setVisibility(View.VISIBLE);
                                tv_chat_date.setText(chatList.get(linearLayoutManager.findFirstVisibleItemPosition()).banner_date);

                            } else {
                                tv_chat_date.setVisibility(View.GONE);
                                tv_chat_date.setText(chatList.get(linearLayoutManager.findFirstVisibleItemPosition()).banner_date);

                            }


                        }
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        if (isLoadFirst) {
                            if (chatList.size() > 0) {
                                if (chatList.get(position).image == 2) {
                                    tv_chat_date.setVisibility(View.VISIBLE);
                                    tv_chat_date.setText(chatList.get(linearLayoutManager.findFirstVisibleItemPosition()).banner_date);

                                } else {
                                    tv_chat_date.setVisibility(View.GONE);
                                    tv_chat_date.setText(chatList.get(linearLayoutManager.findFirstVisibleItemPosition()).banner_date);

                                }
                            }


                            isLoadFirst = false;
                        } else {
                            if (linearLayoutManager.findFirstVisibleItemPosition() != -1) {
                                tv_chat_date.setText(chatList.get(linearLayoutManager.findFirstVisibleItemPosition()).banner_date);
                                tv_chat_date.setVisibility(View.VISIBLE);
                            }


                        }
                    }
                });*/

                chat_recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                            if (chatList.get(position).image == 2) {
                                tv_chat_date.setVisibility(View.VISIBLE);
                                tv_chat_date.setText(chatList.get(linearLayoutManager.findFirstVisibleItemPosition()).banner_date);

                            } else {
                                tv_chat_date.setVisibility(View.GONE);
                                tv_chat_date.setText(chatList.get(linearLayoutManager.findFirstVisibleItemPosition()).banner_date);

                            }


                        }


                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        if (isLoadFirst) {
                            if (chatList.size() > 0) {
                                if (chatList.get(position).image == 2) {
                                    tv_chat_date.setVisibility(View.VISIBLE);
                                    tv_chat_date.setText(chatList.get(linearLayoutManager.findFirstVisibleItemPosition()).banner_date);

                                } else {
                                    tv_chat_date.setVisibility(View.GONE);
                                    tv_chat_date.setText(chatList.get(linearLayoutManager.findFirstVisibleItemPosition()).banner_date);

                                }
                            }


                            isLoadFirst = false;
                        } else {
                            if (linearLayoutManager.findFirstVisibleItemPosition() != -1) {
                                tv_chat_date.setText(chatList.get(linearLayoutManager.findFirstVisibleItemPosition()).banner_date);
                                tv_chat_date.setVisibility(View.VISIBLE);
                            }


                        }

                    }
                });
            }
        });

        linearLayoutManager = new LinearLayoutManager(this);
        //linearLayoutManager.setStackFromEnd(true);
        chat_recycler_view.setLayoutManager(linearLayoutManager);
        chat_recycler_view.setAdapter(chattingAdapter);
        chat_recycler_view.scrollToPosition(map.size() - 1);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ischeck = true;
                if (lastIndexmessagekey != null) {
                    if (totalCount >= tempCount) {
                        increment += 10;
                        getChatLoadMore(lastIndexmessagekey);
                    } else {
                        swipeRefreshLayout.setEnabled(false);
                    }
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }

            }
        });
        manageScrollOnKeyBoard();
        getBlockUserData();
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
                    tv_user_name.setText(builder.toString());

                    if (!otherUserInfo.profilePic.equals("")) {
                        otherProfileImage = otherUserInfo.profilePic;
                    } else {
                        otherProfileImage = "";
                    }

                    if(otherUserInfo.isUserDeleted==1){
                        rl_send_message.setVisibility(View.GONE);
                        rl_user_deleted_view.setVisibility(View.VISIBLE);
                    }
                    else{
                        rl_user_deleted_view.setVisibility(View.GONE);
                        rl_send_message.setVisibility(View.VISIBLE);

                    }


                    chattingAdapter.getImage(otherProfileImage);
                    Glide.with(getApplicationContext()).load(otherProfileImage).apply(new RequestOptions().placeholder(R.drawable.placeholder_user)).into(img_with_chat);


                    ly_user_chat.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void getBlockUserData() {
        if (chatNode != null) {
            firebaseDatabase.getReference().child(Constant.CHAT_BLOCK_TABLE).child(chatNode).child(Constant.CHAT_BLOCKED_BY).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue(String.class) != null) {
                        blockedId = dataSnapshot.getValue(String.class);

                        assert blockedId != null;
                        if (blockedId.equals("Both")) {
                            tv_user_block.setText(getString(R.string.unblock));
                        } else if (blockedId.equals("")) {
                            tv_user_block.setText(getString(R.string.block));
                        } else if (blockedId.equals(otherUId)) {
                            tv_user_block.setText(getString(R.string.block));
                        } else if (blockedId.equals(myUId)) {
                            tv_user_block.setText(getString(R.string.unblock));
                        }

                    } else {
                        blockedId = "";
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    //create note for chatroom
    private String gettingNotes() {
        int myUid_ = Integer.parseInt(myUId);
        int otherUID_ = Integer.parseInt(otherUId);

        if (myUid_ < otherUID_) {
            chatNode = myUId + "_" + otherUId;
        } else {
            chatNode = otherUId + "_" + myUId;
        }
        return chatNode;
    }

    // Get chats
    private void getChat() {

     /*  firebaseDatabase.getReference().child(Constant.CHAT_ROOMS_TABLE).child(chatNode).limitToLast(1).orderByKey()
      .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null){

                    firebaseDatabase.getReference().child(Constant.CHAT_ROOMS_TABLE).child(chatNode).child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {


                            //getChatLoadMore(dataSnapshot.getKey());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    getChatLoadMore(dataSnapshot.getKey());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
*/

       firebaseDatabase.getReference().child(Constant.CHAT_ROOMS_TABLE).child(chatNode).limitToLast(1).orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                getTotalDayBtwTimeStamp(dataSnapshot.getKey());
                getChatLoadMore(dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                getChatLoadMore(dataSnapshot.getKey());
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    private void getChatTotalCount() {
        firebaseDatabase.getReference().child(Constant.CHAT_ROOMS_TABLE).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getKey().equals(chatNode)) {
                    totalCount = (int) dataSnapshot.getChildrenCount() + 10;
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getChatLoadMore(String dataKey) {
        Query query = reference.orderByKey().endAt(dataKey).limitToLast(50);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                tempCount += 1;

                if (totalCount < tempCount) isCompleteChatLoad = true;

                assert chat != null;
                if ((Long) chat.timeStamp > deleteTime) {
                    if (ischeck) {
                        lastIndexmessagekey = dataSnapshot.getKey();
                        ischeck = false;
                        listIndex = increment;
                    }
                    getChatDataInmap(dataSnapshot.getKey(), chat);
                } else {
                    map.remove(dataSnapshot.getKey());
                    chatList.clear();
                    chattingAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                tempCount += 1;

                if (totalCount < tempCount) isCompleteChatLoad = true;
                assert chat != null;

                if ((Long) chat.timeStamp > deleteTime) {
                    if (ischeck) {
                        lastIndexmessagekey = dataSnapshot.getKey();
                        ischeck = false;
                        listIndex = increment;
                    }
                    getChatDataInmap(dataSnapshot.getKey(), chat);
                } else {
                    map.remove(dataSnapshot.getKey());
                    chatList.clear();
                    chattingAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getChatDataInmap(String key, Chat chat) {
        swipeRefreshLayout.setRefreshing(false);
        if (chat != null) {
            if (chat.deleteby.equals(myUId)) {
                return;
            } else {
                chat.banner_date = getDateBanner(chat.timeStamp);
                map.put(key, chat);
                chatList.clear();
                Collection<Chat> values = map.values();
                chatList.addAll(values);
            }
        }
        shortList();

        if (listIndex == 0) {
            chat_recycler_view.scrollToPosition(chatList.size() - 1);
        } else if (chatList.size() != (totalCount - 10)) {
            chat_recycler_view.scrollToPosition(19);
        } else if (chatList.size() == totalCount - 10) {
            swipeRefreshLayout.setEnabled(false);
            isCompleteChatLoad = true;
        }



        if (totalCount <= 30) {
            isCompleteChatLoad = true;
            isLoadFirst = true;
            swipeRefreshLayout.setEnabled(false);
        }

    }

    private String getDateBanner(Object timeStamp) {
        String banner_date = "";
        SimpleDateFormat sim = new SimpleDateFormat("EEE, d MMM", Locale.US);
        try {
            String date_str = sim.format(new Date((Long) timeStamp)).trim();
            String currentDate = sim.format(Calendar.getInstance().getTime()).trim();
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -1);
            String yesterdayDate = sim.format(calendar.getTime()).trim();

            if (date_str.equals(currentDate)) {
                banner_date = getString(R.string.dummy_time).trim();
            } else if (date_str.equals(yesterdayDate)) {
                banner_date = getString(R.string.yesterday).trim();
            } else {
                banner_date = date_str.trim();
            }

            return banner_date;
        } catch (Exception e) {
            e.printStackTrace();
            return banner_date;
        }
    }

    private void shortList() {
        Collections.sort(chatList, new Comparator<Chat>() {

            @Override
            public int compare(Chat a1, Chat a2) {
                if (a1.timeStamp == null || a2.timeStamp == null)
                    return -1;
                else {
                    Long long1 = Long.valueOf(String.valueOf(a1.timeStamp));
                    Long long2 = Long.valueOf(String.valueOf(a2.timeStamp));
                    return long1.compareTo(long2);
                }
            }
        });

        chattingAdapter.notifyDataSetChanged();
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

            case R.id.rl_chat:
                cv_chat_menu.setVisibility(View.GONE);
                break;

            case R.id.iv_send_msg:
                if (AppHelper.isConnectingToInternet(this)) {
                    getBlockUserData();
                    if (otherUserInfo != null) {
                        String[] nameArray = otherUserInfo.name.split(" ");
                        StringBuilder builder = new StringBuilder();
                        for (String s : nameArray) {
                            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                            builder.append(cap).append(" ");
                        }

                        if (blockedId.equals(myUId)) {
                            progress.dismiss();
                            sendMsgAlertDialog("You blocked " + builder.toString().trim() + ". Can't send any message.");
                        } else if (blockedId.equals(otherUId)) {
                            progress.dismiss();
                            sendMsgAlertDialog("You are blocked by " + builder.toString().trim() + ". Can't send any message.");
                        } else if (blockedId.equals("Both")) {
                            progress.dismiss();
                            sendMsgAlertDialog("You blocked " + builder.toString().trim() + ". Can't send any message.");
                        } else {
                            sendMessage();
                        }
                    }


                } else {
                    progress.dismiss();
                    CustomToast.getInstance(this).showToast(this, getString(R.string.toast_no_network));
                }

                break;

            case R.id.iv_pick_image:
                if (Build.VERSION.SDK_INT >= 23) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                                new String[]{Manifest.permission.CAMERA,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                Constant.MY_PERMISSIONS_REQUEST_CAMERA);
                    } else {
                        ImagePicker.pickImage(ChatActivity.this);
                    }
                } else {
                    ImagePicker.pickImage(ChatActivity.this);
                }
                break;

            case R.id.iv_set_emoji:
                CustomToast.getInstance(ChatActivity.this).showToast(ChatActivity.this, "Under Development");
                break;

            case R.id.tv_chat_delete:
                cv_chat_menu.setVisibility(View.GONE);
                blockChatDialog(getString(R.string.delete), "Do you want to delete chat?");
                break;


            case R.id.rl_chat_tool_bar:

                // if user deleted
                if(rl_user_deleted_view.getVisibility()==View.VISIBLE)
                    return;

                if (!otherUId.equals("1")) {
                    setUnread_count();
                    Intent intent = new Intent(ChatActivity.this, MatchProfileActivity.class);
                    intent.putExtra("match_user_id", otherUId);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(ChatActivity.this, AdminProfileActivity.class);
                    intent.putExtra("adminId", otherUId);
                    startActivity(intent);
                }
        }
    }

    private void getTime() {
        firebaseDatabase.getReference().child("chat_delete").child(chatNode).child(myUId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    deleteTime = Long.parseLong(String.valueOf(dataSnapshot.getValue()));
                }
                getOtherUserUnReadCount();
                getChatTotalCount();
                // Get chats
                getChat();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        String pushkey = firebaseDatabase.getReference().child(Constant.CHAT_ROOMS_TABLE).child(chatNode).push().getKey();
        String msg = ed_message.getText().toString().trim();
        String firebaseToken = FirebaseInstanceId.getInstance().getToken();

      /*  if (msg.equals("")) {
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
            otherChat.message = msg;
            otherChat.image = 0;
        }

        otherChat.name = otherName.toLowerCase();
        otherChat.profilePic = otherProfileImage;
        otherChat.timeStamp = ServerValue.TIMESTAMP;
        otherChat.uid = otherUId;
        otherChat.lastMsg = myUId;
        otherChat.unreadCount=0;

        Chat myChat = new Chat();
        myChat.deleteby = "";
        myChat.firebaseToken = firebaseToken;

        if (image_FirebaseURL != null) {
            myChat.imageUrl = image_FirebaseURL.toString();
            myChat.message = "";
            myChat.image = 1;

        } else {
            myChat.imageUrl = "";
            myChat.message = msg;
            myChat.image = 0;

            ed_message.setText("");
        }

        myChat.name = myName.toLowerCase();
        myChat.profilePic = myProfileImage;
        myChat.timeStamp = ServerValue.TIMESTAMP;
        myChat.uid = myUId;
        myChat.lastMsg = myUId;
        myChat.unreadCount=unread_count+1;

        firebaseDatabase.getReference().child(Constant.CHAT_ROOMS_TABLE).child(chatNode).child(pushkey).setValue(myChat);
        firebaseDatabase.getReference().child(Constant.CHAT_HISTORY_TABLE).child(myUId).child(otherUId).setValue(otherChat);
        firebaseDatabase.getReference().child(Constant.CHAT_HISTORY_TABLE).child(otherUId).child(myUId).setValue(myChat);

        if (image_FirebaseURL != null) {
            sendPushNotificationToReceiver(myName, "Image", myName, myUId, firebaseToken, chatNode);
        } else {
            sendPushNotificationToReceiver(myName, msg, myName, myUId, firebaseToken, chatNode);
        }

        image_FirebaseURL = null;
        progress.dismiss();
        isKeyBoadShow=true;
        chat_recycler_view.scrollToPosition(chatList.size() - 1);

    }

    private void deleteChat() {
        firebaseDatabase.getReference().child("chat_delete").child(chatNode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    if (dataSnapshot.hasChild(myUId)) {
                        firebaseDatabase.getReference().child("chat_delete").child(chatNode).child(myUId).setValue(ServerValue.TIMESTAMP);
                        Chat myChat = new Chat();
                        myChat.deleteby = "";
                        myChat.firebaseToken = FirebaseInstanceId.getInstance().getToken();
                        myChat.imageUrl = "";
                        myChat.image = 0;
                        myChat.message = "";
                        myChat.name = myName.toLowerCase();
                        myChat.profilePic = myProfileImage;
                        myChat.timeStamp = ServerValue.TIMESTAMP;
                        myChat.uid = myUId;
                        myChat.lastMsg = myUId;

                        firebaseDatabase.getReference().child(Constant.CHAT_HISTORY_TABLE).child(myUId).child(otherUId).setValue(myChat);
                        getTime();
                        totalCount = 0;
                        tempCount = 0;
                        listIndex = 0;
                        increment = 0;
                        map.clear();
                        chatList.clear();
                        chattingAdapter.notifyDataSetChanged();
                    }
                } else {
                    firebaseDatabase.getReference().child("chat_delete").child(chatNode).child(myUId).setValue(ServerValue.TIMESTAMP);
                    Chat myChat = new Chat();
                    myChat.deleteby = "";
                    myChat.firebaseToken = FirebaseInstanceId.getInstance().getToken();
                    myChat.imageUrl = "";
                    myChat.image = 0;
                    myChat.message = "";
                    myChat.name = myName.toLowerCase();
                    myChat.profilePic = myProfileImage;
                    myChat.timeStamp = ServerValue.TIMESTAMP;
                    myChat.uid = myUId;
                    myChat.lastMsg = myUId;

                    firebaseDatabase.getReference().child(Constant.CHAT_HISTORY_TABLE).child(myUId).child(otherUId).setValue(myChat);
                    getTime();
                    totalCount = 0;
                    tempCount = 0;
                    listIndex = 0;
                    increment = 0;
                    map.clear();
                    chatList.clear();
                    chattingAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        chattingAdapter.notifyDataSetChanged();
    }

    private void blockChatDialog(final String heading, String msg) {
        final Dialog blockDialog = new Dialog(ChatActivity.this);
        blockDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        blockDialog.setContentView(R.layout.dialog_block_user);
        blockDialog.setCancelable(false);
        blockDialog.setCanceledOnTouchOutside(false);

        TextView dialog_heading = blockDialog.findViewById(R.id.tv_dialog_heading);
        dialog_heading.setText(heading);

        TextView alert_message = blockDialog.findViewById(R.id.alert_message);
        alert_message.setText(msg);

        TextView btn_cancel = blockDialog.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                blockDialog.dismiss();
            }
        });

        ImageView dialog_decline_button = blockDialog.findViewById(R.id.dialog_decline_button);
        dialog_decline_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                blockDialog.dismiss();
            }
        });

        TextView btn_alert = blockDialog.findViewById(R.id.btn_alert);
        btn_alert.setText(heading);
        btn_alert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                if (heading.equals(getString(R.string.block)) || heading.equals(getString(R.string.unblock))) {
                    // Api call to block unblock user
                    callBlockUnblockUserApi(otherUId);

                } else if (heading.equals(getString(R.string.delete))) {
                    deleteChat();
                }
                blockDialog.dismiss();
            }
        });

        blockDialog.show();
    }

    private void blockUserChat() {
        if (blockedId.equals("Both")) {
            firebaseDatabase.getReference().child(Constant.CHAT_BLOCK_TABLE).child(chatNode).child(Constant.CHAT_BLOCKED_BY).setValue(otherUId);

        } else if (blockedId.equals("")) {
            firebaseDatabase.getReference().child(Constant.CHAT_BLOCK_TABLE).child(chatNode).child(Constant.CHAT_BLOCKED_BY).setValue(myUId);

        } else if (blockedId.equals(otherUId)) {
            firebaseDatabase.getReference().child(Constant.CHAT_BLOCK_TABLE).child(chatNode).child(Constant.CHAT_BLOCKED_BY).setValue("Both");

        } else if (blockedId.equals(myUId)) {
            firebaseDatabase.getReference().child(Constant.CHAT_BLOCK_TABLE).child(chatNode).child(Constant.CHAT_BLOCKED_BY).setValue(null);
        }

        getBlockUserData();
    }

    private void sendMsgAlertDialog(String message) {
        final Dialog dialog = new Dialog(ChatActivity.this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_send_msg_alert);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        TextView alert_message = dialog.findViewById(R.id.alert_message);
        alert_message.setText(message);

        ImageView dialog_decline_button = dialog.findViewById(R.id.dialog_decline_button);
        dialog_decline_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                dialog.dismiss();
            }
        });

        TextView btn_alert = dialog.findViewById(R.id.btn_alert);
        btn_alert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {

            case Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImage(ChatActivity.this);
                }
            }
            break;

            case Constant.MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImage(ChatActivity.this);
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if (requestCode == 234) {    // Image Picker
                Uri imageUri = ImagePicker.getImageURIFromResult(ChatActivity.this, requestCode, resultCode, data);
                progress.show();
                creatFirebaseProfilePicUrl(imageUri);
            }
        }
    }

    private void creatFirebaseProfilePicUrl(Uri selectedImageUri) {

        StorageReference storageRef;
        FirebaseStorage storage;
        FirebaseApp app;

        app = FirebaseApp.getInstance();
        assert app != null;
        storage = FirebaseStorage.getInstance(app);

        storageRef = storage.getReference("images/");
        StorageReference photoRef = storageRef.child(selectedImageUri.getLastPathSegment());
        photoRef.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        image_FirebaseURL = uri;
                        iv_send_msg.callOnClick();
                    }
                });
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cv_chat_menu.setVisibility(View.GONE);
        Constant.ChatOpponentId = "";
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setUnread_count();
        cv_chat_menu.setVisibility(View.GONE);
        Constant.ChatOpponentId = "";
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

    // Api call to block unblock user
    private void callBlockUnblockUserApi(final String userId) {
        if (AppHelper.isConnectingToInternet(ChatActivity.this)) {
            progress.show();

            Map<String, String> map = new HashMap<>();
            map.put("userId", userId);

            WebService api = new WebService(ChatActivity.this, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        ly_user_chat.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            if (message.equals("User blocked successfully")) {
                                tv_user_block.setText(getString(R.string.unblock));
                            } else if (message.equals("User unBlocked successfully")) {
                                tv_user_block.setText(getString(R.string.block));
                            }

                            blockUserChat();
                        } else {
                            progress.dismiss();
                            ly_user_chat.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);
                            CustomToast.getInstance(ChatActivity.this).showToast(ChatActivity.this, message);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        ly_user_chat.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    ly_user_chat.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                }

            });
            api.callApi("user/userBlockUnblock", Request.Method.POST, map);
        } else {
            ly_user_chat.setVisibility(View.GONE);
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

                    // Api call to block unblock user
                    callBlockUnblockUserApi(userId);

                }
            });
        }
    }


    public void getOtherUserUnReadCount(){
        firebaseDatabase.getReference().child(Constant.CHAT_HISTORY_TABLE).child(otherUId).child(myUId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(Chat.class) != null) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    unread_count = chat.unreadCount;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void getToatalDaybtwLastChat(){
        String currentTimeStamp=ServerValue.TIMESTAMP.get(".sv");



        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = formatter.format(new Date(Long.parseLong(currentTimeStamp)));
        Date currentDate=new Date(dateString);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUnread_count();
    }

    public void setUnread_count(){
        firebaseDatabase.getReference().child(Constant.CHAT_HISTORY_TABLE).child(myUId).child(otherUId).child(Constant.UNREAD_COUNT).setValue(0);
    }

    public void getTotalDayBtwTimeStamp(String last_node_key){


        firebaseDatabase.getReference().child(Constant.CHAT_ROOMS_TABLE).child(chatNode).child(last_node_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                Date d1 = new Date();
                Date d2=new Timestamp((Long) chat.timeStamp);
                TimeSpan ts = TimeSpan.subtract(d1, d2);
                long days=ts.getDays();


                if(days>Constant.TEASE_EXPIRE_DAYS){
                    rel_view_chat.setVisibility(View.GONE);
                    view_tease_expire.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }



    boolean isKeyBoadShow=true;
    void manageScrollOnKeyBoard(){

        chattingAdapter.setOnBottomReachedListener(new ChattingAdapter.OnBottomReachedListener() {
            @Override
            public void onBottomReached(int position) {
                isKeyBoadShow=true;
                Log.i("546001bottom","hide");
            }

            @Override
            public void onBottomNotReached(int position) {
                //isKeyBoadShow=false;
                Log.i("546001bottomNot","hide");
            }
        });

       /* chat_recycler_view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top,
                                       int right, int bottom, int oldLeft, int oldTop,
                                       int oldRight, int oldBottom) {

                if ((bottom < oldBottom) && isScrolled) {
                    chat_recycler_view.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            isScrolled = false;
                            chat_recycler_view.scrollToPosition(chatList.size() - 1);
                        }
                    }, 100);
                }

            }
        });*/


        final View activityRootView = findViewById(R.id.activityRoot);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();

                activityRootView.getWindowVisibleDisplayFrame(r);

                int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
                if (heightDiff > 100 ) {
                    if(isKeyBoadShow){
                        isKeyBoadShow=false;
                        chat_recycler_view.scrollToPosition(chatList.size() - 1);
                    }

                }else{


                }
            }
        });

    }



}
