package com.kinklink.modules.matches.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.daimajia.swipe.SwipeLayout;
import com.kinklink.R;
import com.kinklink.modules.matches.activity.MatchProfileActivity;
import com.kinklink.modules.matches.listener.AdapterViewPositionListener;
import com.kinklink.modules.matches.model.OfferListModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import cn.iwgang.countdownview.CountdownView;

public class ReceiveOfferListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<OfferListModel.OfferListBean> offerList;
    private AdapterViewPositionListener listener;
    long stop_time, restart_time;

    // variable to track event time
    private long mLastClickTime = 0;

    public ReceiveOfferListAdapter(Context mContext, ArrayList<OfferListModel.OfferListBean> offerList, AdapterViewPositionListener listener) {
        this.mContext = mContext;
        this.offerList = offerList;
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;
        View view;
        switch (viewType) {
            case 0:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_offer_list1, parent, false);
                holder = new ReceiveOfferListAdapter.SimpleViewHolder(view);
                return holder;

            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_offer_list2, parent, false);
                holder = new ReceiveOfferListAdapter.ViewHolder(view);
                return holder;


            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_offer_list1, parent, false);
                holder = new ReceiveOfferListAdapter.SimpleViewHolder(view);
                return holder;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return TextUtils.isEmpty(offerList.get(position).offer_message) ? 0 : 1;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder h, int position) {
        OfferListModel.OfferListBean offerModel = offerList.get(position);

        if (h instanceof SimpleViewHolder) {
            SimpleViewHolder holder = (SimpleViewHolder) h;

            Glide.with(mContext).load(offerModel.image).apply(new RequestOptions().placeholder(ContextCompat.getDrawable(mContext, R.drawable.app_icon))).into(holder.iv_user_image);

            String fullName = offerModel.full_name;
            String name[] = fullName.split("\\s+");
            holder.tv_offer_name.setText(name[0]);
            holder.tv_offer_age.setText(String.format("%s %s,", offerModel.age, mContext.getResources().getString(R.string.yr)));

            String gender = "";
            switch (offerModel.gender) {
                case "man":
                    gender = "Man";
                    break;

                case "woman":
                    gender = "Woman";
                    break;

                case "couple":
                    gender = "Couple";
                    break;

                case "transgender_male":
                    gender = "TG Male";
                    break;

                case "transgender_female":
                    gender = "TG Female";
                    break;

                case "neutral":
                    gender = "Non Binary";
                    break;
            }

            holder.tv_offer_gender.setText(gender);

            switch (offerModel.offer_status) {
                case "0":
                    holder.ly_accept_reject.setVisibility(View.VISIBLE);
                    // holder.tv_offer_status.setVisibility(View.GONE);
                    holder.ly_counter_price.setVisibility(View.GONE);

                    holder.refreshTime(offerModel.timerTime - System.currentTimeMillis());
                    holder.tv_offer_status.setText(mContext.getResources().getString(R.string.time_left_for_offer));
                    holder.tv_offer_status.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));
                    holder.tv_offer_status.setVisibility(View.VISIBLE);
                    break;

                case "1":
                    holder.stopCounterTimer();

                    holder.ly_accept_reject.setVisibility(View.GONE);
                    holder.ly_counter_price.setVisibility(View.GONE);
                    holder.tv_offer_status.setText(mContext.getResources().getString(R.string.request_accepted));
                    holder.tv_offer_status.setTextColor(ContextCompat.getColor(mContext,R.color.accept_color));
                    holder.tv_offer_status.setVisibility(View.VISIBLE);
                    break;

                case "2":
                    holder.stopCounterTimer();

                    holder.ly_accept_reject.setVisibility(View.GONE);
                    holder.ly_counter_price.setVisibility(View.GONE);
                    holder.tv_offer_status.setText(mContext.getResources().getString(R.string.request_rejected));
                    holder.tv_offer_status.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                    holder.tv_offer_status.setVisibility(View.VISIBLE);
                    break;

                case "3":
                    holder.stopCounterTimer();

                    holder.ly_counter_price.setVisibility(View.VISIBLE);
                    holder.tv_counter_price.setText(offerModel.counter_amount);

                    switch (offerModel.counter_status) {
                        case "0":
                            holder.ly_accept_reject.setVisibility(View.GONE);
                            holder.tv_offer_status.setText(mContext.getResources().getString(R.string.counter_pending));
                            holder.tv_offer_status.setTextColor(ContextCompat.getColor(mContext,R.color.pending_text_color));
                            holder.tv_offer_status.setVisibility(View.VISIBLE);
                            break;
                        case "1":
                            holder.ly_accept_reject.setVisibility(View.GONE);
                            holder.tv_offer_status.setText(mContext.getResources().getString(R.string.counter_accepted));
                            holder.tv_offer_status.setTextColor(ContextCompat.getColor(mContext,R.color.accept_color));
                            holder.tv_offer_status.setVisibility(View.VISIBLE);
                            break;
                        case "2":
                            holder.ly_accept_reject.setVisibility(View.GONE);
                            holder.tv_offer_status.setText(mContext.getResources().getString(R.string.counter_rejected));
                            holder.tv_offer_status.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                            holder.tv_offer_status.setVisibility(View.VISIBLE);
                            break;
                    }
                    break;
            }


            if (offerModel.offer_type.equals("0")) {
                holder.tv_payment_type.setText(mContext.getResources().getString(R.string.free_love_txt));
                holder.ly_payment_amount.setVisibility(View.GONE);
                holder.btn_counter_offer.setVisibility(View.GONE);
                holder.ly_counter_price.setVisibility(View.GONE);
            } else if (offerModel.offer_type.equals("1")) {
                if (offerModel.pay_by.equals(offerModel.offer_by)) {
                    holder.tv_payment_type.setText(mContext.getResources().getString(R.string.i_will_pay));
                    holder.ly_payment_amount.setVisibility(View.VISIBLE);
                    holder.tv_you_pay_price.setVisibility(View.VISIBLE);
                    holder.tv_you_pay_price.setText(offerModel.offer_amount);
                    holder.btn_counter_offer.setVisibility(View.VISIBLE);
                } else {
                    holder.tv_payment_type.setText(mContext.getResources().getString(R.string.you_pay_me));
                    holder.ly_payment_amount.setVisibility(View.VISIBLE);
                    holder.tv_you_pay_price.setVisibility(View.VISIBLE);
                    holder.tv_you_pay_price.setText(offerModel.offer_amount);
                    holder.btn_counter_offer.setVisibility(View.VISIBLE);
                }
            }

            if (!offerModel.city.equals("")) {
                holder.tv_offer_city.setText(offerModel.city);
            } else {
                holder.tv_offer_city.setVisibility(View.GONE);
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(mContext.getResources().getString(R.string.date_time_format), Locale.US);

            try {
                Date date1 = simpleDateFormat.parse(offerModel.created_on);
                Date date2 = simpleDateFormat.parse(offerModel.cur_time);

                printDifference(date1, date2, holder.tv_offer_time);

            } catch (ParseException e) {
                e.printStackTrace();
            }


        } else if (h instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) h;

            Glide.with(mContext).load(offerModel.image).apply(new RequestOptions().placeholder(ContextCompat.getDrawable(mContext, R.drawable.app_icon))).into(holder.iv_user_image);

            String fullName = offerModel.full_name;
            String name[] = fullName.split("\\s+");
            holder.tv_offer_name.setText(name[0]);
            holder.tv_offer_age.setText(String.format("%s %s,", offerModel.age, mContext.getResources().getString(R.string.yr)));

            String gender = "";
            switch (offerModel.gender) {
                case "man":
                    gender = "Man";
                    break;
                case "woman":
                    gender = "Woman";
                    break;
                case "transgender":
                    gender = "Transgender";
                    break;
                case "couple":
                    gender = "Couple";
                    break;
            }

            holder.tv_offer_gender.setText(gender);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(mContext.getResources().getString(R.string.date_time_format), Locale.US);

            try {
                Date date1 = simpleDateFormat.parse(offerModel.created_on);
                Date date2 = simpleDateFormat.parse(offerModel.cur_time);

                printDifference(date1, date2, holder.tv_offer_time);

            } catch (ParseException e) {
                e.printStackTrace();
            }

            switch (offerModel.offer_status) {
                case "0":
                    holder.ly_accept_reject.setVisibility(View.VISIBLE);
                    //  holder.tv_offer_status.setVisibility(View.GONE);
                    holder.ly_counter_price.setVisibility(View.GONE);

                    holder.refreshTime(offerModel.timerTime - System.currentTimeMillis());
                    holder.tv_offer_status.setText(mContext.getResources().getString(R.string.time_left_for_offer));
                    holder.tv_offer_status.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));
                    holder.tv_offer_status.setVisibility(View.VISIBLE);
                    break;

                case "1":
                    holder.stopCounterTimer();
                    holder.ly_accept_reject.setVisibility(View.GONE);
                    holder.ly_counter_price.setVisibility(View.GONE);
                    holder.tv_offer_status.setText(mContext.getResources().getString(R.string.request_accepted));
                    holder.tv_offer_status.setTextColor(ContextCompat.getColor(mContext,R.color.accept_color));
                    holder.tv_offer_status.setVisibility(View.VISIBLE);
                    break;

                case "2":
                    holder.stopCounterTimer();

                    holder.ly_accept_reject.setVisibility(View.GONE);
                    holder.ly_counter_price.setVisibility(View.GONE);
                    holder.tv_offer_status.setText(mContext.getResources().getString(R.string.request_rejected));
                    holder.tv_offer_status.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                    holder.tv_offer_status.setVisibility(View.VISIBLE);
                    break;

                case "3":
                    holder.stopCounterTimer();

                    holder.ly_counter_price.setVisibility(View.VISIBLE);
                    holder.tv_counter_price.setText(offerModel.counter_amount);

                    switch (offerModel.counter_status) {
                        case "0":
                            holder.ly_accept_reject.setVisibility(View.GONE);
                            holder.tv_offer_status.setText(mContext.getResources().getString(R.string.counter_pending));
                            holder.tv_offer_status.setTextColor(ContextCompat.getColor(mContext,R.color.pending_text_color));
                            holder.tv_offer_status.setVisibility(View.VISIBLE);
                            break;
                        case "1":
                            holder.ly_accept_reject.setVisibility(View.GONE);
                            holder.tv_offer_status.setText(mContext.getResources().getString(R.string.counter_accepted));
                            holder.tv_offer_status.setTextColor(ContextCompat.getColor(mContext,R.color.accept_color));
                            holder.tv_offer_status.setVisibility(View.VISIBLE);
                            break;
                        case "2":
                            holder.ly_accept_reject.setVisibility(View.GONE);
                            holder.tv_offer_status.setText(mContext.getResources().getString(R.string.counter_rejected));
                            holder.tv_offer_status.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                            holder.tv_offer_status.setVisibility(View.VISIBLE);
                            break;
                    }
                    break;
            }


            if (offerModel.offer_type.equals("0")) {
                holder.tv_payment_type.setText(mContext.getResources().getString(R.string.free_love_txt));
                holder.ly_payment_amount.setVisibility(View.GONE);
                holder.btn_counter_offer.setVisibility(View.GONE);
                holder.ly_counter_price.setVisibility(View.GONE);
            } else if (offerModel.offer_type.equals("1")) {
                if (offerModel.pay_by.equals(offerModel.offer_by)) {
                    holder.tv_payment_type.setText(mContext.getResources().getString(R.string.i_will_pay));
                    holder.ly_payment_amount.setVisibility(View.VISIBLE);
                    holder.tv_you_pay_price.setVisibility(View.VISIBLE);
                    holder.tv_you_pay_price.setText(offerModel.offer_amount);
                    holder.btn_counter_offer.setVisibility(View.VISIBLE);
                } else {
                    holder.tv_payment_type.setText(mContext.getResources().getString(R.string.you_pay_me));
                    holder.ly_payment_amount.setVisibility(View.VISIBLE);
                    holder.tv_you_pay_price.setVisibility(View.VISIBLE);
                    holder.tv_you_pay_price.setText(offerModel.offer_amount);
                    holder.btn_counter_offer.setVisibility(View.VISIBLE);
                }
            }

            if (!offerModel.city.equals("")) {
                holder.tv_offer_city.setText(offerModel.city);
            } else {
                holder.tv_offer_city.setVisibility(View.GONE);
            }

            holder.tv_offer_message.setText(offerModel.offer_message);

            /*if (position == offerList.size() - 1) {
                holder.bottom_view.setVisibility(View.VISIBLE);
            } else {
                holder.bottom_view.setVisibility(View.GONE);
            }*/
        }

    }


    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder h) {
        super.onViewAttachedToWindow(h);

        int position = h.getAdapterPosition();
        OfferListModel.OfferListBean offerModel = offerList.get(position);

        if (h instanceof SimpleViewHolder) {
            SimpleViewHolder holder = (SimpleViewHolder) h;

            if (offerModel.offer_status.equals("0")) {
                holder.refreshTime(offerModel.timerTime - System.currentTimeMillis());
            }
        } else if (h instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) h;

            if (offerModel.offer_status.equals("0")) {
                holder.refreshTime(offerModel.timerTime - System.currentTimeMillis());
            }
        }
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder h) {
        super.onViewDetachedFromWindow(h);

        if (h instanceof SimpleViewHolder) {
            SimpleViewHolder holder = (SimpleViewHolder) h;

            holder.getCvCountdownView().stop();
        } else if (h instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) h;

            holder.getCvCountdownView().stop();
        }
    }

    private void printDifference(Date startDate, Date endDate, TextView textView) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;
        long weeksInMilli = daysInMilli * 7;
        long monthInMilli = weeksInMilli * 30;
        long yearInMilli = monthInMilli * 12;

        long elapsedYears = different / yearInMilli;
        different = different % yearInMilli;

        long elapsedMonths = different / monthInMilli;
        different = different % monthInMilli;

        long elapsedWeeks = different / weeksInMilli;
        different = different % weeksInMilli;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        if (elapsedYears != 0) {
            if (elapsedYears == 1) {
                textView.setText(String.format("%s %s", String.valueOf(elapsedYears), mContext.getResources().getString(R.string.year_ago)));
            } else {
                textView.setText(String.format("%s %s", String.valueOf(elapsedYears), mContext.getResources().getString(R.string.years_ago)));
            }
        } else if (elapsedMonths != 0) {
            if (elapsedMonths == 1) {
                textView.setText(String.format("%s %s", String.valueOf(elapsedMonths), mContext.getResources().getString(R.string.month_ago)));
            } else {
                textView.setText(String.format("%s %s", String.valueOf(elapsedMonths), mContext.getResources().getString(R.string.months_ago)));
            }

        } else if (elapsedWeeks != 0) {
            if (elapsedWeeks == 1) {
                textView.setText(String.format("%s %s", String.valueOf(elapsedWeeks), mContext.getResources().getString(R.string.week_ago)));
            } else {
                textView.setText(String.format("%s %s", String.valueOf(elapsedWeeks), mContext.getResources().getString(R.string.weeks_ago)));
            }

        } else if (elapsedDays != 0) {
            if (elapsedDays == 1) {
                textView.setText(String.format("%s %s", String.valueOf(elapsedDays), mContext.getResources().getString(R.string.day_ago)));
            } else {
                textView.setText(String.format("%s %s", String.valueOf(elapsedDays), mContext.getResources().getString(R.string.days_ago)));
            }

        } else if (elapsedHours != 0) {
            if (elapsedHours == 1) {
                textView.setText(String.format("%s %s", String.valueOf(elapsedHours), mContext.getResources().getString(R.string.hour_ago)));
            } else {
                textView.setText(String.format("%s %s", String.valueOf(elapsedHours), mContext.getResources().getString(R.string.hours_ago)));
            }

        } else if (elapsedMinutes != 0) {
            if (elapsedMinutes == 1) {
                textView.setText(String.format("%s %s", String.valueOf(elapsedMinutes), mContext.getResources().getString(R.string.minute_ago)));
            } else {
                textView.setText(String.format("%s %s", String.valueOf(elapsedMinutes), mContext.getResources().getString(R.string.minutes_ago)));
            }

        } else if (elapsedSeconds != 0) {
            if (elapsedSeconds == 1) {
                textView.setText(String.format("%s %s", String.valueOf(elapsedSeconds), mContext.getResources().getString(R.string.second_ago)));
            } else {
                textView.setText(String.format("%s %s", String.valueOf(elapsedSeconds), mContext.getResources().getString(R.string.seconds_ago)));
            }
        }
    }

    @Override
    public int getItemCount() {
        return offerList.size();
    }

    public class SimpleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        LinearLayout ly_offer_price, ly_you_pay, ly_counter_price, ly_payment_amount, ly_accept_reject;
        TextView tv_offer_price, tv_you_pay_price, tv_counter_price, tv_offer_name, tv_offer_age, tv_offer_gender, tv_offer_time, tv_offer_city,
                btn_accept_offer, btn_counter_offer, btn_decline_offer, tv_payment_type, tv_offer_status;
        ImageView iv_user_image;
        RelativeLayout rl_user_info;
        CountdownView countdown_view;

        SimpleViewHolder(View itemView) {
            super(itemView);
            ly_you_pay = itemView.findViewById(R.id.ly_you_pay);
            ly_counter_price = itemView.findViewById(R.id.ly_counter_price);
            tv_you_pay_price = itemView.findViewById(R.id.tv_you_pay_price);
            tv_counter_price = itemView.findViewById(R.id.tv_counter_price);
            tv_offer_name = itemView.findViewById(R.id.tv_offer_name);
            tv_offer_age = itemView.findViewById(R.id.tv_offer_age);
            tv_offer_gender = itemView.findViewById(R.id.tv_offer_gender);
            tv_offer_time = itemView.findViewById(R.id.tv_offer_time);
            btn_accept_offer = itemView.findViewById(R.id.btn_accept_offer);
            btn_counter_offer = itemView.findViewById(R.id.btn_counter_offer);
            btn_decline_offer = itemView.findViewById(R.id.btn_decline_offer);
            iv_user_image = itemView.findViewById(R.id.iv_user_image);
            ly_payment_amount = itemView.findViewById(R.id.ly_payment_amount);
            tv_payment_type = itemView.findViewById(R.id.tv_payment_type);
            tv_offer_city = itemView.findViewById(R.id.tv_offer_city);
            rl_user_info = itemView.findViewById(R.id.rl_user_info);
            ly_accept_reject = itemView.findViewById(R.id.ly_accept_reject);
            tv_offer_status = itemView.findViewById(R.id.tv_offer_status);

            countdown_view = itemView.findViewById(R.id.countdown_view);

            ly_counter_price.setVisibility(View.GONE);
            btn_counter_offer.setVisibility(View.GONE);
            rl_user_info.setOnClickListener(this);
            btn_accept_offer.setOnClickListener(this);
            btn_decline_offer.setOnClickListener(this);
            btn_counter_offer.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            switch (v.getId()) {
                case R.id.rl_user_info:
                    //  listener.getProfileClick(getAdapterPosition());
                    //  ((MainActivity) mContext).addFragment(MatchProfileFragment.newInstance(offerList.get(getAdapterPosition()).userId), true, R.id.fragment_place);
                    Intent intent = new Intent(mContext, MatchProfileActivity.class);
                    intent.putExtra("match_user_id", offerList.get(getAdapterPosition()).userId);
                    mContext.startActivity(intent);

                    break;

                case R.id.btn_accept_offer:
                    listener.getAcceptClick(getAdapterPosition());
                    stopCounterTimer();
                    ly_accept_reject.setVisibility(View.GONE);
                    tv_offer_status.setText(mContext.getResources().getString(R.string.request_accepted));
                    tv_offer_status.setTextColor(ContextCompat.getColor(mContext,R.color.accept_color));
                    tv_offer_status.setVisibility(View.VISIBLE);

                    break;

                case R.id.btn_decline_offer:
                    listener.getRejectClick(getAdapterPosition());
                    stopCounterTimer();
                    ly_accept_reject.setVisibility(View.GONE);
                    tv_offer_status.setText(mContext.getResources().getString(R.string.request_rejected));
                    tv_offer_status.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                    tv_offer_status.setVisibility(View.VISIBLE);
                    break;

                case R.id.btn_counter_offer:
                    stopCounterTimer();
                    listener.getCounterClick(getAdapterPosition());
                    break;
            }
        }

        void refreshTime(long leftTime) {
            if (leftTime > 0) {
                countdown_view.start(leftTime);
                countdown_view.setVisibility(View.VISIBLE);
                ly_accept_reject.setVisibility(View.VISIBLE);
                tv_offer_status.setVisibility(View.VISIBLE);

            } else {
                stopCounterTimer();

                ly_accept_reject.setVisibility(View.GONE);

                tv_offer_status.setText(mContext.getResources().getString(R.string.request_timeout));
                tv_offer_status.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));
                tv_offer_status.setVisibility(View.VISIBLE);
            }

            countdown_view.setOnCountdownEndListener(new CountdownView.OnCountdownEndListener() {
                @Override
                public void onEnd(CountdownView cv) {
                    countdown_view.setVisibility(View.GONE);

                    ly_accept_reject.setVisibility(View.GONE);

                    tv_offer_status.setText(mContext.getResources().getString(R.string.request_timeout));
                    tv_offer_status.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));
                    tv_offer_status.setVisibility(View.VISIBLE);
                }
            });
        }

        void stopCounterTimer() {
            countdown_view.stop();
            countdown_view.allShowZero();
            countdown_view.setVisibility(View.GONE);
        }

        CountdownView getCvCountdownView() {
            return countdown_view;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayout ly_offer_price, ly_you_pay, ly_counter_price, ly_payment_amount, ly_accept_reject;
        TextView tv_offer_price, tv_you_pay_price, tv_counter_price, tv_offer_name, tv_offer_age, tv_offer_gender, tv_offer_time, tv_offer_city,
                btn_accept_offer, btn_counter_offer, btn_decline_offer, tv_payment_type, tv_offer_message, tv_offer_status;
        ImageView iv_user_image, iv_left_arrow, iv_right_arrow;
        RelativeLayout rl_message_view, rl_user_info;
        CountdownView countdown_view;
        SwipeLayout swipeLayout;

        ViewHolder(View itemView) {
            super(itemView);
            ly_you_pay = itemView.findViewById(R.id.ly_you_pay);
            ly_counter_price = itemView.findViewById(R.id.ly_counter_price);
            tv_you_pay_price = itemView.findViewById(R.id.tv_you_pay_price);
            tv_counter_price = itemView.findViewById(R.id.tv_counter_price);
            tv_offer_name = itemView.findViewById(R.id.tv_offer_name);
            tv_offer_age = itemView.findViewById(R.id.tv_offer_age);
            tv_offer_gender = itemView.findViewById(R.id.tv_offer_gender);
            tv_offer_time = itemView.findViewById(R.id.tv_offer_time);
            btn_accept_offer = itemView.findViewById(R.id.btn_accept_offer);
            btn_counter_offer = itemView.findViewById(R.id.btn_counter_offer);
            btn_decline_offer = itemView.findViewById(R.id.btn_decline_offer);
            iv_user_image = itemView.findViewById(R.id.iv_user_image);
            iv_left_arrow = itemView.findViewById(R.id.iv_left_arrow);
            iv_right_arrow = itemView.findViewById(R.id.iv_right_arrow);
            ly_payment_amount = itemView.findViewById(R.id.ly_payment_amount);
            tv_payment_type = itemView.findViewById(R.id.tv_payment_type);
            tv_offer_city = itemView.findViewById(R.id.tv_offer_city);
            tv_offer_message = itemView.findViewById(R.id.tv_offer_message);
            rl_message_view = itemView.findViewById(R.id.rl_message_view);
            rl_user_info = itemView.findViewById(R.id.rl_user_info);

            ly_accept_reject = itemView.findViewById(R.id.ly_accept_reject);
            tv_offer_status = itemView.findViewById(R.id.tv_offer_status);

            countdown_view = itemView.findViewById(R.id.countdown_view);
            swipeLayout = itemView.findViewById(R.id.swipe_layout);

            ly_counter_price.setVisibility(View.GONE);

            btn_counter_offer.setVisibility(View.GONE);
            rl_user_info.setOnClickListener(this);
            btn_accept_offer.setOnClickListener(this);
            btn_decline_offer.setOnClickListener(this);
            btn_counter_offer.setOnClickListener(this);
            iv_left_arrow.setOnClickListener(this);
            iv_right_arrow.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            // Preventing multiple clicks, using threshold of 1/2 second
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            switch (v.getId()) {
                case R.id.rl_user_info:
                    // listener.getProfileClick(getAdapterPosition());
                    //  ((MainActivity) mContext).addFragment(MatchProfileFragment.newInstance(offerList.get(getAdapterPosition()).userId), true, R.id.fragment_place);
                    Intent intent = new Intent(mContext, MatchProfileActivity.class);
                    intent.putExtra("match_user_id", offerList.get(getAdapterPosition()).userId);
                    mContext.startActivity(intent);

                    break;

                case R.id.btn_accept_offer:
                    listener.getAcceptClick(getAdapterPosition());
                    stopCounterTimer();
                    ly_accept_reject.setVisibility(View.GONE);
                    tv_offer_status.setText(mContext.getResources().getString(R.string.request_accepted));
                    tv_offer_status.setTextColor(ContextCompat.getColor(mContext,R.color.accept_color));
                    tv_offer_status.setVisibility(View.VISIBLE);
                    break;

                case R.id.btn_decline_offer:
                    listener.getRejectClick(getAdapterPosition());
                    stopCounterTimer();
                    ly_accept_reject.setVisibility(View.GONE);
                    tv_offer_status.setText(mContext.getResources().getString(R.string.request_rejected));
                    tv_offer_status.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                    tv_offer_status.setVisibility(View.VISIBLE);
                    break;

                case R.id.btn_counter_offer:
                    stopCounterTimer();
                    listener.getCounterClick(getAdapterPosition());
                    break;

                case R.id.iv_left_arrow:
                    swipeLayout.open();
                    break;

                case R.id.iv_right_arrow:
                    swipeLayout.close();
                    break;
            }
        }

        void refreshTime(long leftTime) {
            if (leftTime > 0) {
                countdown_view.start(leftTime);
                countdown_view.setVisibility(View.VISIBLE);
                ly_accept_reject.setVisibility(View.VISIBLE);
                tv_offer_status.setVisibility(View.VISIBLE);

            } else {
                stopCounterTimer();

                ly_accept_reject.setVisibility(View.GONE);

                tv_offer_status.setText(mContext.getResources().getString(R.string.request_timeout));
                tv_offer_status.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));
                tv_offer_status.setVisibility(View.VISIBLE);
            }

            countdown_view.setOnCountdownEndListener(new CountdownView.OnCountdownEndListener() {
                @Override
                public void onEnd(CountdownView cv) {
                    countdown_view.setVisibility(View.GONE);

                    ly_accept_reject.setVisibility(View.GONE);

                    tv_offer_status.setText(mContext.getResources().getString(R.string.request_timeout));
                    tv_offer_status.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));
                    tv_offer_status.setVisibility(View.VISIBLE);
                }
            });
        }

        void stopCounterTimer() {
            countdown_view.stop();
            countdown_view.allShowZero();
            countdown_view.setVisibility(View.GONE);
        }

        CountdownView getCvCountdownView() {
            return countdown_view;
        }
    }

    public void getTimeOut(long timeOut) {
        long timeOut1 = timeOut;
        notifyDataSetChanged();
    }
}
