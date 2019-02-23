package com.kinklink.modules.chat.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kinklink.R;
import com.kinklink.application.KinkLink;
import com.kinklink.helper.AppHelper;
import com.kinklink.helper.Constant;
import com.kinklink.helper.Progress;
import com.kinklink.modules.authentication.model.FirebaseUserModel;
import com.kinklink.modules.chat.adapter.ChatHistoryAdapter;
import com.kinklink.modules.chat.model.Chat;
import com.kinklink.modules.matches.activity.MainActivity;
import com.kinklink.modules.matches.activity.MyProfileActivity;
import com.kinklink.modules.matches.fragment.TeasesFragment;
import com.kinklink.session.Session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ChatHistoryFragment extends Fragment implements View.OnClickListener {
    private ChatHistoryAdapter adapter;
    private ArrayList<Chat> historyList, tempList;
    private ArrayList<FirebaseUserModel> userList;
    private Map<String, Chat> mapList;
    private Context mContext;
    private String myUid = "";
    private LinearLayout ly_chat_history, ly_no_network, ly_no_chat;
    private RecyclerView history_recycler_view;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseDatabase firebaseDatabase;

    private TextView btn_try_again;

    private TextView action_bar_heading;
    private Progress progress;

    // variable to track event time
    private long mLastClickTime = 0;

    public static ChatHistoryFragment newInstance() {
        Bundle args = new Bundle();
        ChatHistoryFragment fragment = new ChatHistoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_history, container, false);
        historyList = new ArrayList<>();
        tempList = new ArrayList<>();
        userList = new ArrayList<>();
        mapList = new HashMap<>();

        progress = new Progress(mContext);
        initView(view);
        action_bar_heading.setText(getString(R.string.messages));

        firebaseDatabase = FirebaseDatabase.getInstance();
        Session session = new Session(mContext);
        myUid = session.getRegistration().userDetail.userId;

        //set chat history adapter
        setChatHistoryAdapter();

        getHistoryList();

        return view;
    }

    private void setChatHistoryAdapter() {
        adapter = new ChatHistoryAdapter(mContext, historyList);
        history_recycler_view.setAdapter(adapter);

        // Pull To refresh listener
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // cancel the Visual indication of a refresh
                swipeRefreshLayout.setRefreshing(false);
                KinkLink.getInstance().cancelAllRequests(KinkLink.TAG);

                tempList.clear();
                historyList.clear();
                adapter.notifyDataSetChanged();

                getHistoryList();
            }
        });
    }

    private void initView(View view) {
        action_bar_heading = ((MainActivity) mContext).findViewById(R.id.action_bar_heading);

        ImageView iv_back = ((MainActivity) mContext).findViewById(R.id.iv_back);
        iv_back.setVisibility(View.GONE);

        ImageView iv_profile = ((MainActivity) mContext).findViewById(R.id.iv_profile);
        iv_profile.setVisibility(View.VISIBLE);
        iv_profile.setOnClickListener(this);

        ImageView iv_teases = ((MainActivity) mContext).findViewById(R.id.iv_teases);
        iv_teases.setVisibility(View.GONE);
        iv_teases.setOnClickListener(this);

        RelativeLayout rl_notifications = ((MainActivity) mContext).findViewById(R.id.rl_notifications);
        rl_notifications.setVisibility(View.GONE);

        ImageView iv_settings = ((MainActivity) mContext).findViewById(R.id.iv_settings);
        iv_settings.setVisibility(View.GONE);

        RelativeLayout bottom_menu = ((MainActivity) mContext).findViewById(R.id.bottomMenu);
        bottom_menu.setVisibility(View.VISIBLE);

        ly_no_network = view.findViewById(R.id.ly_no_network);
        ly_chat_history = view.findViewById(R.id.ly_chat_history);
        btn_try_again = view.findViewById(R.id.btn_try_again);
        ly_no_chat = view.findViewById(R.id.ly_no_chat);
        history_recycler_view = view.findViewById(R.id.history_recycler_view);
        TextView tv_no_record_txt = view.findViewById(R.id.tv_no_record_txt);
        tv_no_record_txt.setText(getString(R.string.no_chat_found));

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        iv_back.setOnClickListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    private void getHistoryList() {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            ly_no_network.setVisibility(View.GONE);
            firebaseDatabase.getReference().child(Constant.CHAT_HISTORY_TABLE).child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {
                        ly_no_chat.setVisibility(View.VISIBLE);
                        progress.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            firebaseDatabase.getReference().child(Constant.CHAT_HISTORY_TABLE).child(myUid).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    if (dataSnapshot.getValue(Chat.class) != null) {
                        Chat chat = dataSnapshot.getValue(Chat.class);
                        gettingDataFromUserTable(dataSnapshot.getKey(), chat);

                        ly_no_chat.setVisibility(View.GONE);
                        if (isAdded()) {
                            progress.dismiss();
                        }
                    }


                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    if (dataSnapshot.getValue(Chat.class) != null) {
                        Chat chat = dataSnapshot.getValue(Chat.class);
                        gettingDataFromUserTable(dataSnapshot.getKey(), chat);

                        ly_no_chat.setVisibility(View.GONE);
                        progress.dismiss();
                    }


                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    for (int i = 0; i < historyList.size(); i++) {
                        if (historyList.get(i).uid.equals(dataSnapshot.getKey())) {
                            historyList.remove(i);
                        }
                    }


                    if (historyList.size() == 0) {
                        ly_no_chat.setVisibility(View.VISIBLE);
                    } else {
                        ly_no_chat.setVisibility(View.GONE);
                    }

                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            ly_chat_history.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);

            btn_try_again.setOnClickListener(this);
        }
    }

    private void gettingDataFromUserTable(final String key, final Chat chat) {
        FirebaseDatabase.getInstance().getReference().child(Constant.USER_TABLE).child(key).
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue(FirebaseUserModel.class) != null) {
                            FirebaseUserModel infoFCM = dataSnapshot.getValue(FirebaseUserModel.class);
                            userList.add(infoFCM);

                            for (FirebaseUserModel userInfoFCM : userList) {
                                if (userInfoFCM.uid.equals(key)) {
                                    chat.profilePic = userInfoFCM.profilePic;
                                    chat.name = userInfoFCM.name;
                                    chat.firebaseToken = userInfoFCM.firebaseToken;
                                    chat.uid = key;
                                }
                            }

                            mapList.put(chat.uid, chat);
                            tempList.clear();
                            historyList.clear();
                            Collection<Chat> values = mapList.values();

                            tempList.addAll(values);

                            shortList();

                            for (int i = 0; i < tempList.size(); i++) {
                                if (tempList.get(i).uid.equals("1")) {
                                    historyList.add(0, tempList.get(i));
                                } else {
                                    historyList.add(tempList.get(i));
                                }
                            }

                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void shortList() {
        Collections.sort(tempList, new Comparator<Chat>() {
            @Override
            public int compare(Chat a1, Chat a2) {
                if (a1.timeStamp == null || a2.timeStamp == null)
                    return -1;
                else {
                    Long long1 = Long.valueOf(String.valueOf(a1.timeStamp));
                    Long long2 = Long.valueOf(String.valueOf(a2.timeStamp));
                    return long2.compareTo(long1);
                }
            }
        });

        if (tempList.size() == 0) {
            ly_no_chat.setVisibility(View.VISIBLE);
        } else {
            ly_no_chat.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
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

            case R.id.btn_try_again:
                getHistoryList();
                break;

            case R.id.iv_profile:
                startActivity(new Intent(mContext, MyProfileActivity.class));
                break;

            case R.id.iv_teases:
                ((MainActivity) mContext).replaceFragment(TeasesFragment.newInstance(), true, R.id.fragment_place);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}