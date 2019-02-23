package com.kinklink.modules.matches.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.kinklink.R;
import com.kinklink.helper.Utils;
import com.kinklink.modules.authentication.listener.AdapterPositionListener;
import com.kinklink.modules.chat.activity.ChatActivity;
import com.kinklink.modules.matches.activity.MainActivity;
import com.kinklink.modules.matches.listener.MyVisitPositionListener;
import com.kinklink.modules.matches.model.MyVisitsModel;
import com.kinklink.modules.matches.model.NotificationModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by mindiii on 20/7/18.
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private ArrayList<NotificationModel> notificationList;
    private AdapterPositionListener listener;
    private Context mContext;

    // variable to track event time
    private long mLastClickTime = 0;

    public NotificationAdapter(Context mContext, ArrayList<NotificationModel> notificationList, AdapterPositionListener listener) {
        this.mContext = mContext;
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_notification_list_item, parent, false);
        return new NotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NotificationAdapter.ViewHolder holder, int position) {
        NotificationModel listBean = notificationList.get(position);

        if (listBean.full_name != null && !listBean.full_name.equals("") && !listBean.full_name.equals("null")) {
            String sourceString = "";
            String message[] = listBean.body.split(listBean.full_name);
            if (message.length > 1) {
                sourceString = "<b>" + listBean.full_name + "</b> " + message[1];
            } else {
                sourceString = "<b>" + listBean.full_name + "</b> " + message[0];
            }
            holder.tv_message.setText(Html.fromHtml(sourceString));
        } else {
            String message = listBean.body.substring(0, 1).toUpperCase() + listBean.body.substring(1);
            holder.tv_message.setText(message);
        }


        Glide.with(mContext).load(listBean.image).apply(new RequestOptions().placeholder(R.drawable.placeholder_user)).into(holder.iv_user_image);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(mContext.getResources().getString(R.string.date_time_format), Locale.US);

        try {
            Date date1 = simpleDateFormat.parse(listBean.created_on);
            Date date2 = simpleDateFormat.parse(listBean.cur_time);

            printDifference(mContext, date1, date2, holder.tv_time);

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView iv_user_image;
        private TextView tv_message, tv_time;
        private CardView cv_notification;

        ViewHolder(View itemView) {
            super(itemView);
            iv_user_image = itemView.findViewById(R.id.iv_user_image);
            tv_message = itemView.findViewById(R.id.tv_message);
            tv_time = itemView.findViewById(R.id.tv_time);
            cv_notification = itemView.findViewById(R.id.cv_notification);

            cv_notification.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Preventing multiple clicks, using threshold of 1/2 second
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            switch (v.getId()) {
                case R.id.cv_notification:
                    listener.getPosition(getAdapterPosition());
                    break;
            }

        }
    }

    private void printDifference(Context mContext, Date startDate, Date endDate, TextView textView) {
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

            //   textView.setText(String.format("%s %s", String.valueOf(elapsedHours), mContext.getResources().getString(R.string.hours_ago)));

        } else if (elapsedMinutes != 0) {
            if (elapsedMinutes == 1) {
                textView.setText(String.format("%s %s", String.valueOf(elapsedMinutes), mContext.getResources().getString(R.string.minute_ago)));
            } else {
                textView.setText(String.format("%s %s", String.valueOf(elapsedMinutes), mContext.getResources().getString(R.string.minutes_ago)));
            }

            //  textView.setText(String.format("%s %s", String.valueOf(elapsedMinutes), mContext.getResources().getString(R.string.minutes_ago)));
        } else if (elapsedSeconds != 0) {
            if (elapsedSeconds == 1) {
                textView.setText(String.format("%s %s", String.valueOf(elapsedSeconds), mContext.getResources().getString(R.string.second_ago)));
            } else {
                textView.setText(String.format("%s %s", String.valueOf(elapsedSeconds), mContext.getResources().getString(R.string.seconds_ago)));
            }

            //  textView.setText(String.format("%s %s", String.valueOf(elapsedSeconds), mContext.getResources().getString(R.string.seconds_ago)));
        }
    }
}