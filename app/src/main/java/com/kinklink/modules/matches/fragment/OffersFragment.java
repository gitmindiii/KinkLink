package com.kinklink.modules.matches.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
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
import com.kinklink.modules.matches.activity.MainActivity;
import com.kinklink.modules.matches.activity.MyProfileActivity;
import com.kinklink.modules.matches.adapter.MyOfferListAdapter;
import com.kinklink.modules.matches.adapter.ReceiveOfferListAdapter;
import com.kinklink.modules.matches.listener.AdapterViewPositionListener;
import com.kinklink.modules.matches.listener.EndlessRecyclerViewScrollListener;
import com.kinklink.modules.matches.model.OfferListModel;
import com.kinklink.server_task.WebService;

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

public class OffersFragment extends Fragment implements View.OnClickListener {
    private static final String LIST_TYPE = "list_type";
    private Context mContext;
    private TextView action_bar_heading, btn_try_again;
    private RecyclerView offers_recycler_view;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout ly_no_network, ly_no_offer;
    private TextView tv_receive_offer, tv_my_offer;
    private ReceiveOfferListAdapter receiveOfferListAdapter;
    private MyOfferListAdapter myOfferListAdapter;

    private static int requestType = 2;
    private int offsetLimit = 0;
    private LinearLayout ly_offer_list;
    private ArrayList<OfferListModel.OfferListBean> offerList;
    private int activeFragId;
    private Dialog counterDialog;
    private Progress progress;

    // variable to track event time
    private long mLastClickTime = 0;

    public OffersFragment() {
    }

    public static OffersFragment newInstance(String list_type) {
        Bundle args = new Bundle();
        OffersFragment fragment = new OffersFragment();
        fragment.setArguments(args);
        args.putString(LIST_TYPE, list_type);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            if (getArguments().getString(LIST_TYPE) != null) {
                String list_type = getArguments().getString(LIST_TYPE);
                assert list_type != null;
                if (list_type.equals("other")) {
                    // Setting Receive Offers tab active
                    activeFragId = R.id.tv_receive_offer;
                    requestType = 2;        // Request Type is set to 2 to get Receive Offers List

                } else if (list_type.equals("my")) {
                    // Setting My Offers tab active
                    activeFragId = R.id.tv_my_offer;
                    requestType = 1;        // Request Type is set to 1 to get My Offers List
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_offers, container, false);
        init(view);

        progress = new Progress(mContext);
        offerList = new ArrayList<>();

        action_bar_heading.setText(getString(R.string.offers));

        /*// Setting Receive Offers tab active
        activeFragId = R.id.tv_receive_offer;
        requestType = 2;        // Request Type is set to 2 to get Receive Offers List*/

        // Get Offers List Api
        callGetOfferListApi();

        // Setting adapter to get Offers list
        setGetOfferListAdapter();

        return view;
    }

    // Setting adapter to get Offers list
    private void setGetOfferListAdapter() {
        // Receive Offer List recycler adapter
        receiveOfferListAdapter = new ReceiveOfferListAdapter(mContext, offerList, new AdapterViewPositionListener() {
            /*@Override
            public void getProfileClick(int position) {
                ((MainActivity) mContext).addFragment(MatchProfileFragment.newInstance(offerList.get(position).userId), true, R.id.fragment_place);
            }*/

            @Override
            public void getAcceptClick(int position) {
                String offerId = offerList.get(position).offerId;
                callAcceptRejectOfferApi(offerId, "1", position);
            }

            @Override
            public void getRejectClick(int position) {
                String offerId = offerList.get(position).offerId;
                callAcceptRejectOfferApi(offerId, "2", position);
            }

            @Override
            public void getCounterClick(int position) {
                openEnterCounterOfferDialog(position);

            }

        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        offers_recycler_view.setLayoutManager(layoutManager);

        // My Offer List recycler adapter
        myOfferListAdapter = new MyOfferListAdapter(mContext, offerList, new AdapterViewPositionListener() {
           /* @Override
            public void getProfileClick(int position) {
                ((MainActivity) mContext).addFragment(MatchProfileFragment.newInstance(offerList.get(position).userId), true, R.id.fragment_place);
            }*/

            @Override
            public void getAcceptClick(int position) {
                String offerId = offerList.get(position).offerId;
                callAcceptRejectOfferApi(offerId, "1", position);
            }

            @Override
            public void getRejectClick(int position) {
                String offerId = offerList.get(position).offerId;
                callAcceptRejectOfferApi(offerId, "2", position);
            }

            @Override
            public void getCounterClick(int position) {
            }

        });

        if (requestType == 2) {
            tv_receive_offer.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
            tv_my_offer.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));

            offers_recycler_view.setAdapter(receiveOfferListAdapter);
        } else if (requestType == 1) {
            tv_receive_offer.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));
            tv_my_offer.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));

            offers_recycler_view.setAdapter(myOfferListAdapter);
        }


        // Pull To refresh listener
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // cancel the Visual indication of a refresh
                swipeRefreshLayout.setRefreshing(false);
                offsetLimit = 0;
                KinkLink.getInstance().cancelAllRequests(KinkLink.TAG);

                if (requestType == 2) {
                    offerList.clear();
                    offers_recycler_view.setVisibility(View.GONE);
                    receiveOfferListAdapter.notifyDataSetChanged();
                } else if (requestType == 1) {
                    offerList.clear();
                    offers_recycler_view.setVisibility(View.GONE);
                    myOfferListAdapter.notifyDataSetChanged();
                }

                callGetOfferListApi();
            }
        });

        // Endless Recycler Scroll Listener Pagination
        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                callGetOfferListApi();
            }
        };

        // Adds the scroll listener to RecyclerView
        offers_recycler_view.addOnScrollListener(scrollListener);
    }

    private void init(View view) {
        action_bar_heading = ((MainActivity) mContext).findViewById(R.id.action_bar_heading);

        ImageView iv_back = ((MainActivity) mContext).findViewById(R.id.iv_back);
        iv_back.setVisibility(View.GONE);

        ImageView iv_profile = ((MainActivity) mContext).findViewById(R.id.iv_profile);
        iv_profile.setVisibility(View.VISIBLE);
        iv_profile.setOnClickListener(this);

        ImageView iv_teases = ((MainActivity) mContext).findViewById(R.id.iv_teases);
        iv_teases.setVisibility(View.VISIBLE);
        iv_teases.setOnClickListener(this);

        RelativeLayout rl_notifications = ((MainActivity) mContext).findViewById(R.id.rl_notifications);
        rl_notifications.setVisibility(View.GONE);
        //   rl_notifications.setOnClickListener(this);

        ImageView iv_settings = ((MainActivity) mContext).findViewById(R.id.iv_settings);
        iv_settings.setVisibility(View.GONE);

        RelativeLayout bottom_menu = ((MainActivity) mContext).findViewById(R.id.bottomMenu);
        swipeRefreshLayout = view.findViewById(R.id.simpleSwipeRefreshLayout);
        offers_recycler_view = view.findViewById(R.id.offers_recycler_view);
        ly_no_network = ((MainActivity) mContext).findViewById(R.id.ly_no_network);
        btn_try_again = ((MainActivity) mContext).findViewById(R.id.btn_try_again);

        bottom_menu.setVisibility(View.VISIBLE);
        ((MainActivity) mContext).setOffersFragmentActive();

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        tv_receive_offer = view.findViewById(R.id.tv_receive_offer);
        tv_my_offer = view.findViewById(R.id.tv_my_offer);
        ly_offer_list = view.findViewById(R.id.ly_offer_list);

        ly_no_offer = view.findViewById(R.id.ly_no_offer);

        tv_receive_offer.setOnClickListener(this);
        tv_my_offer.setOnClickListener(this);
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
             //   ((MainActivity) mContext).addFragment(MyProfileFragment.newInstance(), true, R.id.fragment_place);
                startActivity(new Intent(mContext, MyProfileActivity.class));
                break;

            case R.id.iv_teases:
                ((MainActivity) mContext).addFragment(TeasesFragment.newInstance(), true, R.id.fragment_place);
                break;

            /*case R.id.rl_notifications:
                startActivity(new Intent(mContext, NotificationActivity.class));
                break;*/

            case R.id.tv_receive_offer:        // Receive Offer Click
                if (activeFragId != R.id.tv_receive_offer) {
                    activeFragId = R.id.tv_receive_offer;

                    offers_recycler_view.setVisibility(View.GONE);
                    tv_receive_offer.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                    tv_my_offer.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));
                    requestType = 2;     // Request Type is set to 2 to get Receive Offers List
                    offerList.clear();
                    offsetLimit = 0;
                    offers_recycler_view.setAdapter(receiveOfferListAdapter);
                    callGetOfferListApi();
                }
                break;

            case R.id.tv_my_offer:         // My Offer Click
                if (activeFragId != R.id.tv_my_offer) {
                    activeFragId = R.id.tv_my_offer;

                    offers_recycler_view.setVisibility(View.GONE);
                    tv_receive_offer.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));
                    tv_my_offer.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                    requestType = 1;      // Request Type is set to 1 to get My Offers List
                    offerList.clear();
                    offsetLimit = 0;
                    offers_recycler_view.setAdapter(myOfferListAdapter);
                    callGetOfferListApi();
                }
                break;
        }
    }

    // Api call to get Offer List
    private void callGetOfferListApi() {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");

                        if (status.equals("success")) {
                            progress.dismiss();

                            JSONArray jsonArray = js.getJSONArray("offerList");
                            OfferListModel.OfferListBean model;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                model = new OfferListModel.OfferListBean();
                                JSONObject object = jsonArray.getJSONObject(i);
                                model.offerId = object.getString("offerId");
                                model.offer_by = object.getString("offer_by");
                                model.offer_for = object.getString("offer_for");
                                model.offer_type = object.getString("offer_type");
                                model.pay_by = object.getString("pay_by");
                                model.offer_status = object.getString("offer_status");
                                model.offer_amount = object.getString("offer_amount");
                                model.counter_apply = object.getString("counter_apply");
                                model.counter_status = object.getString("counter_status");
                                model.counter_amount = object.getString("counter_amount");
                                model.offer_message = object.getString("offer_message");
                                model.created_on = object.getString("created_on");
                                model.updated_on = object.getString("updated_on");
                                model.cur_time = object.getString("cur_time");
                                model.userId = object.getString("userId");
                                model.full_name = object.getString("full_name");
                                model.gender = object.getString("gender");
                                model.is_verify = object.getString("is_verify");
                                model.age = object.getString("age");
                                model.image = object.getString("image");
                                model.city = object.getString("city");

                                Pattern p = Pattern.compile("(\\d+):(\\d+):(\\d+)");
                                Matcher m = p.matcher(js.getString("timeOut"));
                                if (m.matches()) {
                                    int hrs = Integer.parseInt(m.group(1));
                                    int min = Integer.parseInt(m.group(2));
                                    //   long ms = (long) hrs * 60 * 60 * 1000 + min * 60 * 1000;

                                    long ms = TimeUnit.HOURS.toMillis(hrs) + TimeUnit.MINUTES.toMillis(min);

                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(mContext.getResources().getString(R.string.date_time_format), Locale.US);

                                    try {
                                        Date date1 = simpleDateFormat.parse(model.created_on);
                                        Date date2 = simpleDateFormat.parse(model.cur_time);
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
                                offerList.add(model);
                            }

                            ly_offer_list.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            offers_recycler_view.setVisibility(View.VISIBLE);
                            ly_no_offer.setVisibility(View.GONE);

                            offsetLimit += 10;

                            if (requestType == 2) {
                                receiveOfferListAdapter.notifyDataSetChanged();
                            } else if (requestType == 1) {
                                myOfferListAdapter.notifyDataSetChanged();
                            }
                        } else {
                            progress.dismiss();
                            if (offerList.size() == 0) {
                                offers_recycler_view.setVisibility(View.GONE);
                                ly_no_offer.setVisibility(View.VISIBLE);
                                ly_offer_list.setVisibility(View.VISIBLE);
                                ly_no_network.setVisibility(View.GONE);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        ly_offer_list.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    ly_offer_list.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                }

            });
            api.callApi("offer/getOfferList?offset=" + offsetLimit + "&limit=10&requestType=" + requestType, Request.Method.GET, null);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            ly_offer_list.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callGetOfferListApi();
                }
            });
        }
    }

    // Api to Accept or reject Offer
    private void callAcceptRejectOfferApi(final String offerId, final String requestType, final int position) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            Map<String, String> map = new HashMap<>();
            map.put("offerId", offerId);
            map.put("requestType", requestType);

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        ly_offer_list.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);

                        if (status.equals("success")) {
                            progress.dismiss();

                            if (requestType.equals("2")) {
                                if (message.equals("Offer accepted successfully")) {
                                    offerList.get(position).offer_status = "1";
                                } else if (message.equals("Offer ignore successfully")) {
                                    offerList.get(position).offer_status = "2";
                                }
                                receiveOfferListAdapter.notifyDataSetChanged();

                               /* offsetLimit = 0;
                                KinkLink.getInstance().cancelPendingRequests();
                                offerList.clear();
                                receiveOfferListAdapter.notifyDataSetChanged();
                                callGetOfferListApi();*/


                            } else if (requestType.equals("1")) {
                                if (message.equals("Offer accepted successfully")) {
                                    offerList.get(position).counter_status = "1";
                                } else if (message.equals("Offer ignore successfully")) {
                                    offerList.get(position).counter_status = "2";
                                }
                                myOfferListAdapter.notifyDataSetChanged();

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
                        ly_offer_list.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);
                        progress.dismiss();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    ly_offer_list.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                    progress.dismiss();
                }

            });
            api.callApi("offer/acceptIgnoreOffer", Request.Method.POST, map);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            ly_offer_list.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callAcceptRejectOfferApi(offerId, requestType, position);
                }
            });
        }
    }

    // Dialog to enter offer counter price
    private void openEnterCounterOfferDialog(final int position) {
        counterDialog = new Dialog(mContext);
        counterDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        counterDialog.setContentView(R.layout.dialog_counter_offer);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(counterDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        counterDialog.getWindow().setAttributes(lWindowParams);

        ImageView dialog_decline_button = counterDialog.findViewById(R.id.dialog_decline_button);
        final EditText ed_counter_price = counterDialog.findViewById(R.id.ed_counter_price);
        TextView btn_counter = counterDialog.findViewById(R.id.btn_counter);

        dialog_decline_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counterDialog.dismiss();
            }
        });

        btn_counter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String counter_price = ed_counter_price.getText().toString().trim();
                String offerId = offerList.get(position).offerId;
                if (counter_price.equals("")) {
                    CustomToast.getInstance(mContext).showToast(mContext, mContext.getString(R.string.offer_amount_null));
                } else {
                    double counter_amount = Double.parseDouble(counter_price);
                    if (counter_amount > 0) {
                        if (!counter_price.equals(offerList.get(position).offer_amount)) {
                            counterDialog.dismiss();
                            callOfferCounterApi(offerId, counter_price, position);
                        } else {
                            CustomToast.getInstance(mContext).showToast(mContext, mContext.getString(R.string.counter_amount_invalid));
                        }
                    } else
                        CustomToast.getInstance(mContext).showToast(mContext, mContext.getString(R.string.offer_amount_null));
                }
            }
        });

        counterDialog.getWindow().setGravity(Gravity.CENTER);
        counterDialog.show();
    }

    // Api to offer counter
    private void callOfferCounterApi(final String offerId, final String counter_price, final int position) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            Map<String, String> map = new HashMap<>();
            map.put("offerId", offerId);
            map.put("counterAmount", counter_price);

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        ly_offer_list.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);

                        if (status.equals("success")) {
                            progress.dismiss();

                            if (message.equals("Counter apply successfully")) {
                               /* offerList.get(position).offer_status = "3";
                                offerList.get(position).counter_amount = counter_price;
                                offerList.get(position).counter_status = "0";*/

                                OfferListModel.OfferListBean listBean = offerList.get(position);
                                listBean.offer_status = "3";
                                listBean.counter_status = "0";
                                listBean.counter_amount = counter_price;
                                offerList.set(position, listBean);

                                if (requestType == 2) {
                                    receiveOfferListAdapter.notifyDataSetChanged();
                                } else if (requestType == 1) {
                                    myOfferListAdapter.notifyDataSetChanged();
                                }
                            }

                        } else {
                            progress.dismiss();
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        ly_offer_list.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    ly_offer_list.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                }

            });
            api.callApi("offer/counterOffer", Request.Method.POST, map);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            ly_offer_list.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callOfferCounterApi(offerId, counter_price, position);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        progress.dismiss();
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
