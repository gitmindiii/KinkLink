package com.kinklink.modules.matches.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
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
import com.kinklink.modules.authentication.activity.EditProfileActivity;
import com.kinklink.modules.authentication.activity.KinkLinkParentActivity;
import com.kinklink.modules.authentication.model.RegistrationInfo;
import com.kinklink.modules.chat.activity.ChatActivity;
import com.kinklink.modules.chat.fragment.ChatHistoryFragment;
import com.kinklink.modules.chat.model.Chat;
import com.kinklink.modules.matches.fragment.FavoritesFragment;
import com.kinklink.modules.matches.fragment.MatchListFragment;
import com.kinklink.modules.matches.fragment.MatchProfileFragment;
import com.kinklink.modules.matches.fragment.MyProfileFragment;
import com.kinklink.modules.matches.fragment.OffersFragment;
import com.kinklink.modules.matches.fragment.TeasesFragment;
import com.kinklink.modules.matches.fragment.TravelKinksFragment;
import com.kinklink.modules.matches.fragment.ViewsFragment;
import com.kinklink.server_task.WebService;
import com.kinklink.session.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends KinkLinkParentActivity implements View.OnClickListener {
    private TextView action_bar_heading, tv_notification_count;
    private boolean doubleBackToExitPressedOnce;
    private LinearLayout ly_travel_kinks, ly_viewed, ly_matches, ly_offers, ly_chat_history, ly_tease;
    private ImageView iv_travel_kinks, iv_viewed, iv_offers, iv_chat_history, iv_back, iv_settings, iv_profile, iv_tease, iv_menu, iv_teases;
    private RelativeLayout bottomMenu, rl_notifications, rl_main_activity;
    // variable to track event time
    private long mLastClickTime = 0;
    private int activeFragId;
    private Session session;
    public static int notificationCount;
    public static int profileViewCount;
    private LinearLayout ly_no_network;

    private FrameLayout lay_viewd_count;
    private ImageView img_viewed_count, img_chat_unread_ic;
    private TextView txt_viewed_count;
    DatabaseReference chatHistory_db_ref = null;
    ChildEventListener chatHistory_unreadCount_listener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(R.layout.activity_main_new);


        session = new Session(MainActivity.this);
        RegistrationInfo registrationInfo = session.getRegistration();
        String myUId = registrationInfo.userDetail.userId;
        chatHistory_db_ref = FirebaseDatabase.getInstance().getReference().child(Constant.CHAT_HISTORY_TABLE).child(myUId);


        Progress progress = new Progress(MainActivity.this);
        init();
        checkUreadMsg();
        action_bar_heading.setText(getString(R.string.matches));
        activeFragId = R.id.ly_matches;

        // Adding Match List Fragment to display match lists
        addFragment(MatchListFragment.newInstance(), false, R.id.fragment_place);

        if (getIntent().getStringExtra("type") != null) {
            String type = getIntent().getStringExtra("type");
            String uid = getIntent().getStringExtra("uid");
            //String list_type = getIntent().getStringExtra("list_type");
            if(!type.equals("chat")){
                String notification_id=getIntent().getStringExtra("notification_id");
                if(notification_id!=null && !notification_id.isEmpty())
                    callReadNotificationApi(notification_id,type);
            }


            switch (type) {
                case "chat":
                    Intent chatIntent = new Intent(MainActivity.this, ChatActivity.class);
                    chatIntent.putExtra("otherUID", getIntent().getStringExtra("uid"));
                    startActivity(chatIntent);
                    break;

                case "offer_updates":
                    progress.dismiss();
                    activeFragId = R.id.ly_offers;
                    String list_type = getIntent().getStringExtra("list_type");
                    replaceFragment(OffersFragment.newInstance(list_type), false, R.id.fragment_place);
                    setOffersFragmentActive();
                    break;

                case "teases_updates":
                    list_type = getIntent().getStringExtra("list_type");
                    if (list_type.equals("my")) {
                        addFragment(TeasesFragment.newInstance(), true, R.id.fragment_place);
                        setTeaseFragmentactive();
                    } else {
                        Intent intent = new Intent(MainActivity.this, MatchProfileActivity.class);
                        intent.putExtra("match_user_id", uid);
                        startActivity(intent);
                        // setMatchesFragmentActive();
                    }


                    break;

                case "favorite_updates":
                    //addFragment(FavoritesFragment.newInstance(), true, R.id.fragment_place);

                    Intent favorite_intent = new Intent(MainActivity.this, MatchProfileActivity.class);
                    favorite_intent.putExtra("match_user_id", uid);
                    startActivity(favorite_intent);
                    break;

                case "photo_updates":
                    Intent intent = new Intent(MainActivity.this, MyProfileActivity.class);
                    intent.putExtra("verify_photo_notification", "verify");
                    startActivity(intent);
                    break;

                case "view_updates":
                   /* activeFragId = R.id.ly_viewed;
                    replaceFragment(ViewsFragment.newInstance(), false, R.id.fragment_place);
                    setViewedFragmentActive();*/

                    Intent view_intent = new Intent(MainActivity.this, MatchProfileActivity.class);
                    view_intent.putExtra("match_user_id", uid);
                    startActivity(view_intent);
                    break;

                case "chat_updates":
                    if (session.getRegistration().userDetail.is_profile_complete.equals("0")) {
                        if (session.getRegistration().userDetail.profile_step.equals("0")) {
                            startActivity(new Intent(MainActivity.this, EditProfileActivity.class));
                            finish();
                        }
                    } else if (session.getRegistration().userDetail.is_profile_complete.equals("1")) {
                        Intent chatUpdateIntent = new Intent(MainActivity.this, ChatActivity.class);
                        chatUpdateIntent.putExtra("otherUID", "1");
                        startActivity(chatUpdateIntent);
                    }
                    break;
            }
        }

        ly_travel_kinks.setOnClickListener(this);
        ly_viewed.setOnClickListener(this);
        ly_matches.setOnClickListener(this);
        ly_offers.setOnClickListener(this);
        ly_chat_history.setOnClickListener(this);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String type = intent.getStringExtra("from");

            if (type.equals("notification")) {

                if (notificationCount != 0) {
                    tv_notification_count.setText(String.valueOf(notificationCount));
                    tv_notification_count.setVisibility(View.VISIBLE);
                } else {
                    tv_notification_count.setVisibility(View.GONE);
                }

            } else if (type.equals("viewcount")) {
                if (profileViewCount != 0) {
                    txt_viewed_count.setText(String.valueOf(profileViewCount));
                    lay_viewd_count.setVisibility(View.VISIBLE);
                } else {
                    lay_viewd_count.setVisibility(View.GONE);
                }
            }


        }
    };

    private void init() {

        lay_viewd_count = findViewById(R.id.lay_viewd_count);
        img_viewed_count = findViewById(R.id.img_viewed_count);
        img_chat_unread_ic = findViewById(R.id.img_chat_unread_ic);
        txt_viewed_count = findViewById(R.id.txt_viewed_count);
        getProfileViewCount();

        action_bar_heading = findViewById(R.id.action_bar_heading);
        iv_back = findViewById(R.id.iv_back);
        iv_menu = findViewById(R.id.iv_menu);
        iv_teases = findViewById(R.id.iv_teases);
        bottomMenu = findViewById(R.id.bottomMenu);
        iv_profile = findViewById(R.id.iv_profile);
        iv_tease = findViewById(R.id.iv_tease);
        rl_notifications = findViewById(R.id.rl_notifications);
        iv_settings = findViewById(R.id.iv_settings);

        rl_main_activity = findViewById(R.id.rl_main_activity);

        ly_travel_kinks = findViewById(R.id.ly_travel_kinks);
        ly_tease = findViewById(R.id.ly_tease);
        ly_viewed = findViewById(R.id.ly_viewed);
        ly_matches = findViewById(R.id.ly_matches);
        ly_offers = findViewById(R.id.ly_offers);
        ly_chat_history = findViewById(R.id.ly_chat_history);
        ly_no_network = findViewById(R.id.ly_no_network);

        iv_travel_kinks = findViewById(R.id.iv_travel_kinks);
        iv_viewed = findViewById(R.id.iv_viewed);
        iv_offers = findViewById(R.id.iv_offers);
        iv_chat_history = findViewById(R.id.iv_chat_history);

        tv_notification_count = findViewById(R.id.tv_notification_count);
        tv_notification_count.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (v.getId()) {
            case R.id.ly_travel_kinks:   // Travel Kinks Bottom menu click
                clickTravelKink();
                break;

            case R.id.ly_viewed:     // Viewed  Bottom menu click
                clickViews();
                break;

            case R.id.ly_matches:       // Matches Bottom menu click
                clickMatches();
                break;


            case R.id.ly_tease:
                clickTease();

            case R.id.ly_offers:      // Offers Bottom menu click
                clickOffers();
                break;

            case R.id.ly_chat_history:       // Chat History Bottom menu click
                clickChatHistory();
                break;
        }
    }


    /*Click for tease*/

    public void clickTease() {

        if (AppHelper.isConnectingToInternet(MainActivity.this)) {
            if (activeFragId != R.id.ly_tease) {
                activeFragId = R.id.ly_tease;
                TeasesFragment.tab_type = 3;
                replaceFragment(TeasesFragment.newInstance(), false, R.id.fragment_place);
                setTeaseFragmentactive();
                rl_main_activity.setVisibility(View.VISIBLE);
                ly_no_network.setVisibility(View.GONE);
            }
        } else {
            rl_main_activity.setVisibility(View.GONE);
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

                    clickTease();
                }
            });
        }
    }


    private void setTeaseFragmentactive() {
        iv_travel_kinks.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_airplane_ico));
        iv_viewed.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_view_ico));
        iv_tease.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tease_active_icon));
        iv_offers.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_gift_ico));
        iv_chat_history.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_chat_ico));

        action_bar_heading.setText(getString(R.string.views));
        iv_back.setVisibility(View.GONE);
        iv_menu.setVisibility(View.GONE);
        bottomMenu.setVisibility(View.VISIBLE);
        iv_profile.setVisibility(View.VISIBLE);
        rl_notifications.setVisibility(View.GONE);
        iv_settings.setVisibility(View.GONE);
    }

    private void clickTravelKink() {
        if (AppHelper.isConnectingToInternet(MainActivity.this)) {
            if (activeFragId != R.id.ly_travel_kinks) {
                activeFragId = R.id.ly_travel_kinks;
                TravelKinksFragment.tab_type = 0;
                replaceFragment(TravelKinksFragment.newInstance(), false, R.id.fragment_place);
                setTravelKinksFragmentActive();
                rl_main_activity.setVisibility(View.VISIBLE);
                ly_no_network.setVisibility(View.GONE);
            }
        } else {
            rl_main_activity.setVisibility(View.GONE);
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

                    clickTravelKink();
                }
            });
        }
    }

    private void clickViews() {
        if (AppHelper.isConnectingToInternet(MainActivity.this)) {
            if (activeFragId != R.id.ly_viewed) {
                activeFragId = R.id.ly_viewed;

                // hide viewcount batch
                txt_viewed_count.setText("0");
                lay_viewd_count.setVisibility(View.GONE);

                replaceFragment(ViewsFragment.newInstance(), false, R.id.fragment_place);
                setViewedFragmentActive();
                rl_main_activity.setVisibility(View.VISIBLE);
                ly_no_network.setVisibility(View.GONE);
            }
        } else {
            rl_main_activity.setVisibility(View.GONE);
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

                    clickViews();
                }
            });
        }
    }

    /*Click for matches*/

    public void clickMatches() {
        if (AppHelper.isConnectingToInternet(MainActivity.this)) {
            if (activeFragId != R.id.ly_matches) {
                activeFragId = R.id.ly_matches;
                replaceFragment(MatchListFragment.newInstance(), false, R.id.fragment_place);
                setMatchesFragmentActive();
                rl_main_activity.setVisibility(View.VISIBLE);
                ly_no_network.setVisibility(View.GONE);
            }
        } else {
            rl_main_activity.setVisibility(View.GONE);
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

                    clickMatches();
                }
            });
        }
    }

    public void clickOffers() {
        if (AppHelper.isConnectingToInternet(MainActivity.this)) {
            if (activeFragId != R.id.ly_offers) {
                activeFragId = R.id.ly_offers;
                replaceFragment(OffersFragment.newInstance("other"), false, R.id.fragment_place);
                setOffersFragmentActive();
                rl_main_activity.setVisibility(View.VISIBLE);
                ly_no_network.setVisibility(View.GONE);
            }
        } else {
            rl_main_activity.setVisibility(View.GONE);
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

                    clickOffers();
                }
            });
        }
    }

    private void clickChatHistory() {
        if (AppHelper.isConnectingToInternet(MainActivity.this)) {
            if (activeFragId != R.id.ly_chat_history) {
                activeFragId = R.id.ly_chat_history;
                // CustomToast.getInstance(MainActivity.this).showToast(MainActivity.this, "Under Development");

                replaceFragment(ChatHistoryFragment.newInstance(), false, R.id.fragment_place);
                setChatHistoryFragmentActive();
                rl_main_activity.setVisibility(View.VISIBLE);
                ly_no_network.setVisibility(View.GONE);

                    /*Intent chatIntent = new Intent(MainActivity.this, ChatActivity.class);
                    chatIntent.putExtra("otherUID", "1");
                    startActivity(chatIntent);*/
            }
        } else {
            rl_main_activity.setVisibility(View.GONE);
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

                    clickChatHistory();
                }
            });
        }
    }

    // Setting Travel Kinks Fragment Active
    public void setTravelKinksFragmentActive() {
        iv_travel_kinks.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.active_airplane_ico));
        iv_tease.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tease_inactive_icon));
        iv_viewed.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_view_ico));
        iv_offers.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_gift_ico));
        iv_chat_history.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_chat_ico));

        action_bar_heading.setText(getString(R.string.travel_kink));
        iv_back.setVisibility(View.GONE);
        iv_menu.setVisibility(View.GONE);
        bottomMenu.setVisibility(View.VISIBLE);
        iv_profile.setVisibility(View.VISIBLE);
        iv_teases.setVisibility(View.GONE);
        rl_notifications.setVisibility(View.GONE);
        iv_settings.setVisibility(View.GONE);
    }

    // Setting Views Fragment Active
    public void setViewedFragmentActive() {
        iv_travel_kinks.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_airplane_ico));
        iv_tease.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tease_inactive_icon));
        iv_viewed.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.active_view_ico));
        iv_offers.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_gift_ico));
        iv_chat_history.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_chat_ico));

        action_bar_heading.setText(getString(R.string.views));
        iv_back.setVisibility(View.GONE);
        iv_menu.setVisibility(View.GONE);
        bottomMenu.setVisibility(View.VISIBLE);
        iv_profile.setVisibility(View.VISIBLE);
        iv_teases.setVisibility(View.GONE);
        rl_notifications.setVisibility(View.GONE);
        iv_settings.setVisibility(View.GONE);
    }

    // Setting Matches Fragment Active
    public void setMatchesFragmentActive() {
        iv_travel_kinks.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_airplane_ico));
        iv_tease.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tease_inactive_icon));
        iv_viewed.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_view_ico));
        iv_offers.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_gift_ico));
        iv_chat_history.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_chat_ico));

        action_bar_heading.setText(getString(R.string.matches));
        iv_back.setVisibility(View.GONE);
        iv_menu.setVisibility(View.GONE);
        bottomMenu.setVisibility(View.VISIBLE);
        iv_profile.setVisibility(View.VISIBLE);
        iv_teases.setVisibility(View.GONE);
        rl_notifications.setVisibility(View.VISIBLE);
        iv_settings.setVisibility(View.GONE);
    }

    // Setting Offers Fragment Active
    public void setOffersFragmentActive() {
        iv_travel_kinks.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_airplane_ico));
        iv_tease.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tease_inactive_icon));
        iv_viewed.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_view_ico));
        iv_offers.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.acive_gift_ico));
        iv_chat_history.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_chat_ico));

        action_bar_heading.setText(getString(R.string.offers));
        iv_back.setVisibility(View.GONE);
        iv_menu.setVisibility(View.GONE);
        bottomMenu.setVisibility(View.VISIBLE);
        iv_profile.setVisibility(View.VISIBLE);
        iv_tease.setVisibility(View.GONE);
        rl_notifications.setVisibility(View.GONE);
        iv_settings.setVisibility(View.GONE);
    }

    // Setting Settings Fragment Active
    public void setChatHistoryFragmentActive() {
        iv_travel_kinks.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_airplane_ico));
        iv_tease.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tease_inactive_icon));
        iv_viewed.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_view_ico));
        iv_offers.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_gift_ico));
        iv_chat_history.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.active_chat_ico));

        action_bar_heading.setText(getString(R.string.messages));
        iv_back.setVisibility(View.GONE);
        iv_menu.setVisibility(View.GONE);
        bottomMenu.setVisibility(View.VISIBLE);
        iv_profile.setVisibility(View.VISIBLE);
        iv_teases.setVisibility(View.GONE);
        rl_notifications.setVisibility(View.GONE);
        iv_settings.setVisibility(View.GONE);

    }


    @Override
    public void onBackPressed() {
        Handler handler = new Handler();
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();

            final FragmentManager.OnBackStackChangedListener listener = new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_place);

                    if (fragment instanceof MatchListFragment) {    // Setting MatchListFragment Fragment Active on back press
                        iv_back.setVisibility(View.GONE);
                        iv_menu.setVisibility(View.GONE);
                        bottomMenu.setVisibility(View.VISIBLE);
                        action_bar_heading.setText(getString(R.string.matches));
                        iv_profile.setVisibility(View.VISIBLE);
                        iv_teases.setVisibility(View.GONE);
                        iv_settings.setVisibility(View.GONE);
                        rl_notifications.setVisibility(View.VISIBLE);

                        //   ((MatchListFragment) fragment).getMatchBackCall();

                    } else if (fragment instanceof TravelKinksFragment) {    // Setting Meet Up Fragment Active on back press
                        iv_back.setVisibility(View.GONE);
                        iv_menu.setVisibility(View.GONE);
                        bottomMenu.setVisibility(View.VISIBLE);
                        action_bar_heading.setText(getString(R.string.travel_kink));
                        iv_profile.setVisibility(View.VISIBLE);
                        // iv_teases.setVisibility(View.GONE);
                        rl_notifications.setVisibility(View.GONE);
                        iv_settings.setVisibility(View.GONE);

                        //  ((TravelKinksFragment) fragment).getTravelBackCall();
                    } else if (fragment instanceof OffersFragment) {      // Setting Offers Fragment Active on back press
                        iv_back.setVisibility(View.GONE);
                        iv_menu.setVisibility(View.GONE);
                        bottomMenu.setVisibility(View.VISIBLE);
                        action_bar_heading.setText(getString(R.string.offers));
                        iv_profile.setVisibility(View.VISIBLE);
                        iv_teases.setVisibility(View.GONE);
                        iv_settings.setVisibility(View.GONE);
                        rl_notifications.setVisibility(View.GONE);

                        //   ((OffersFragment) fragment).getOffersBackCall();

                    } else if (fragment instanceof TeasesFragment) {      // Setting Likes Fragment Active on back press
                        iv_back.setVisibility(View.VISIBLE);
                        iv_menu.setVisibility(View.GONE);
                        bottomMenu.setVisibility(View.GONE);
                        action_bar_heading.setText(getString(R.string.likes));
                        iv_profile.setVisibility(View.GONE);
                        iv_teases.setVisibility(View.GONE);
                        iv_settings.setVisibility(View.GONE);
                        rl_notifications.setVisibility(View.GONE);

                        ((TeasesFragment) fragment).getLikesBackCall();

                    } else if (fragment instanceof FavoritesFragment) {   // Setting Favorites Fragment Active on back press
                        iv_back.setVisibility(View.VISIBLE);
                        iv_menu.setVisibility(View.GONE);
                        bottomMenu.setVisibility(View.GONE);
                        action_bar_heading.setText(getString(R.string.favorites));
                        iv_profile.setVisibility(View.GONE);
                        iv_teases.setVisibility(View.GONE);
                        iv_settings.setVisibility(View.GONE);
                        rl_notifications.setVisibility(View.GONE);

                        ((FavoritesFragment) fragment).getFavoritesBackCall();

                    } else if (fragment instanceof ViewsFragment) {     // Setting Viewed Me Fragment Active on back press
                        iv_back.setVisibility(View.GONE);
                        iv_menu.setVisibility(View.GONE);
                        bottomMenu.setVisibility(View.VISIBLE);
                        action_bar_heading.setText(getString(R.string.views));
                        iv_profile.setVisibility(View.VISIBLE);
                        iv_teases.setVisibility(View.GONE);
                        iv_settings.setVisibility(View.GONE);
                        rl_notifications.setVisibility(View.GONE);

                        //  ((ViewsFragment) fragment).getViewsBackCall();

                    } else if (fragment instanceof MatchProfileFragment) {    // Setting Match Profile Fragment Active on back press
                        iv_back.setVisibility(View.VISIBLE);
                        iv_menu.setVisibility(View.VISIBLE);
                        bottomMenu.setVisibility(View.GONE);
                        action_bar_heading.setText(getString(R.string.matches));
                        iv_profile.setVisibility(View.GONE);
                        iv_teases.setVisibility(View.GONE);
                        iv_settings.setVisibility(View.GONE);
                        rl_notifications.setVisibility(View.GONE);

                    } else if (fragment instanceof MyProfileFragment) {    // Setting My Profile Fragment Active on back press
                        iv_back.setVisibility(View.VISIBLE);
                        iv_menu.setVisibility(View.GONE);
                        bottomMenu.setVisibility(View.GONE);
                        action_bar_heading.setText(getString(R.string.my_profile));
                        iv_profile.setVisibility(View.GONE);
                        iv_teases.setVisibility(View.GONE);
                        iv_settings.setVisibility(View.VISIBLE);
                        rl_notifications.setVisibility(View.GONE);
                    }
                }
            };
            getSupportFragmentManager().addOnBackStackChangedListener(listener);

        } else if (!doubleBackToExitPressedOnce) {
            this.doubleBackToExitPressedOnce = true;
            CustomToast.getInstance(this).showToast(this, getString(R.string.click_again_to_exit));

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (discardDialog != null) {
            discardDialog.dismiss();
        }
        unregisterWifiReceiver();
    }

    private void unregisterWifiReceiver() {
        MainActivity.this.unregisterReceiver(receiver);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (session.getRegistration().userDetail.is_profile_complete.equals("0")) {
            openProfileIncompleteDialog();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivity.this.registerReceiver(receiver, new IntentFilter("NOTIFICATIONCOUNT"));


    }


    Dialog discardDialog = null;

    private void openProfileIncompleteDialog() {
        discardDialog = new Dialog(MainActivity.this);
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


    private void checkUreadMsg() {

        chatHistory_unreadCount_listener =
                chatHistory_db_ref.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        if (dataSnapshot.getValue(Chat.class) != null) {
                            int unreadCount = dataSnapshot.getValue(Chat.class).unreadCount;
                            if (unreadCount > 0) {
                                img_chat_unread_ic.setVisibility(View.VISIBLE);
                            } else img_chat_unread_ic.setVisibility(View.GONE);
                        }

                    }


                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (dataSnapshot.getValue(Chat.class) != null) {
                            int unreadCount = dataSnapshot.getValue(Chat.class).unreadCount;
                            if (unreadCount > 0) {
                                img_chat_unread_ic.setVisibility(View.VISIBLE);
                            } else img_chat_unread_ic.setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue(Chat.class) != null) {
                            int unreadCount = dataSnapshot.getValue(Chat.class).unreadCount;
                            if (unreadCount > 0) {
                                img_chat_unread_ic.setVisibility(View.VISIBLE);
                            } else img_chat_unread_ic.setVisibility(View.GONE);

                        }
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatHistory_db_ref != null && chatHistory_unreadCount_listener != null)
            chatHistory_db_ref.removeEventListener(chatHistory_unreadCount_listener);
    }

    private void getProfileViewCount() {
        if (AppHelper.isConnectingToInternet(MainActivity.this)) {

            WebService api = new WebService(MainActivity.this, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");
                        String Count = js.getString("count");

                        if (status.equals("success")) {

                            int count = Integer.parseInt(Count);
                            MainActivity.profileViewCount = count;

                            if (count != 0) {
                                if (count < 100) {
                                    txt_viewed_count.setText(Count);
                                } else {
                                    txt_viewed_count.setText(new StringBuilder().append(Count).append("+").toString());
                                }
                                lay_viewd_count.setVisibility(View.VISIBLE);
                            } else {
                                lay_viewd_count.setVisibility(View.GONE);
                            }

                        } else {
                            CustomToast.getInstance(MainActivity.this).showToast(MainActivity.this, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        CustomToast.getInstance(MainActivity.this).showToast(MainActivity.this, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {

                }

            });
            api.callApi("user/getViewedprofileCount", Request.Method.GET, null);
        }
    }


    private void callReadNotificationApi(final String id, final String notification_type) {
        if (AppHelper.isConnectingToInternet(MainActivity.this)) {

            final Map<String, String> map = new HashMap<>();
            map.put("notifyId", id);

            WebService api = new WebService(MainActivity.this, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {

                            int notify_count=MainActivity.notificationCount-1;
                            if (notify_count >0) {
                                MainActivity.notificationCount = notify_count;
                                if (notify_count < 100) {
                                    tv_notification_count.setText(""+notify_count);
                                } else {
                                    tv_notification_count.setText(new StringBuilder().append(""+notify_count).append("+").toString());
                                }
                                tv_notification_count.setVisibility(View.VISIBLE);
                            } else {
                                tv_notification_count.setVisibility(View.GONE);
                            }


                            if(notification_type.equals("view_updates")){
                                int count=MainActivity.profileViewCount-1;
                                if (count > 0) {
                                    MainActivity.profileViewCount=count;
                                    if (count < 100) {
                                        txt_viewed_count.setText(""+count);
                                    } else {
                                        txt_viewed_count.setText(new StringBuilder().append(""+count).append("+").toString());
                                    }
                                    lay_viewd_count.setVisibility(View.VISIBLE);
                                } else {
                                    lay_viewd_count.setVisibility(View.GONE);
                                }
                            }


                        } else {

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        CustomToast.getInstance(MainActivity.this).showToast(MainActivity.this, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {

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

                    callReadNotificationApi(id,notification_type);
                }
            });
        }
    }

}
