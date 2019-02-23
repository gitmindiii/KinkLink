package com.kinklink.modules.chat.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.kinklink.R;
import com.kinklink.modules.chat.activity.ChatActivity;
import com.kinklink.modules.chat.model.Chat;
import com.kinklink.session.Session;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<Chat> historyList;

    // variable to track event time
    private long mLastClickTime = 0;

    public ChatHistoryAdapter(Context mContext, ArrayList<Chat> historyList) {
        this.mContext = mContext;
        this.historyList = historyList;
    }

    @Override
    public ChatHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_chat_history_item, parent, false);
        return new ChatHistoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChatHistoryAdapter.ViewHolder holder, int position) {
        Chat chat = historyList.get(position);
        Session session = new Session(mContext);
        String privacyPolicy = session.getPrivacyPolicy();

        String[] nameArray = chat.name.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String s : nameArray) {
            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
            builder.append(cap + " ");
        }

        holder.tv_user_name.setText(builder.toString());

        if (chat.image == 1) {
            holder.tv_message.setText("Image");
        } else {
            holder.tv_message.setText(chat.message);
        }

        if(chat.unreadCount==0){
           holder.tv_unread_count.setVisibility(View.GONE);
          //  holder.tv_time.setTextColor(mContext.getResources().getColor(R.color.field_text_color));
        }

        else{
           // holder.tv_time.setTextColor(mContext.getResources().getColor(R.color.green));
            holder.tv_unread_count.setVisibility(View.VISIBLE);
            holder.tv_unread_count.setText(""+chat.unreadCount);
        }

        /*if (chat.imageUrl != null && chat.imageUrl.startsWith("https://firebasestorage.googleapis.com/")) {
            holder.tv_message.setText("Image");
        } else if (chat.uid.equals("1") && privacyPolicy.equals(chat.message)) {
            String htmlText = " %s ";
            String policyString = String.format(htmlText, chat.message.trim());

            holder.wv_message.loadData(policyString, "text/html", "utf-8");
            holder.wv_message.setVisibility(View.VISIBLE);
            holder.tv_message.setVisibility(View.GONE);
        } else {
            holder.wv_message.setVisibility(View.GONE);
            holder.tv_message.setText(chat.message);
            holder.tv_message.setVisibility(View.VISIBLE);

        }*/


        Glide.with(mContext).load(chat.profilePic).apply(new RequestOptions().placeholder(R.drawable.placeholder_user)).into(holder.iv_user_image);

        try {
            Date date = new Date((Long) chat.timeStamp);
            Date cur_date = Calendar.getInstance().getTime();

            SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            String fDate = dateformat.format(date);
            String tDate = dateformat.format(cur_date);

            if (!fDate.equals(tDate)) {
                SimpleDateFormat sd = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                holder.tv_time.setText(sd.format(chat.timeStamp));
            } else {
                SimpleDateFormat sd = new SimpleDateFormat("hh:mm a", Locale.US);
                String time_str = sd.format(chat.timeStamp);
                String time = time_str.replace("am", "AM").replace("pm", "PM");
                holder.tv_time.setText(time);
            }


            // printDifference(mContext, date, cur_date, holder.tv_time);
        } catch (Exception e) {
            e.printStackTrace();
        }

       /* if (!chat.uid.equals("1")) {
            SimpleDateFormat sd = new SimpleDateFormat("hh:mm a");
            try {
                Date date = new Date((Long) chat.timeStamp);
                Date cur_date = Calendar.getInstance().getTime();
                printDifference(mContext, date, cur_date, holder.tv_time);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            holder.tv_time.setVisibility(View.GONE);
        }*/

    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView iv_user_image;
        private TextView tv_user_name, tv_message, tv_time,tv_unread_count;
        private CardView cv_chat_history;
        private WebView wv_message;

        public ViewHolder(View itemView) {
            super(itemView);
            iv_user_image = itemView.findViewById(R.id.iv_user_image);
            tv_user_name = itemView.findViewById(R.id.tv_user_name);
            tv_message = itemView.findViewById(R.id.tv_message);
            tv_time = itemView.findViewById(R.id.tv_time);
            cv_chat_history = itemView.findViewById(R.id.cv_chat_history);
            tv_unread_count= itemView.findViewById(R.id.tv_unread_count);
            //  wv_message = itemView.findViewById(R.id.wv_message);

            cv_chat_history.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Preventing multiple clicks, using threshold of 1/2 second
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            switch (v.getId()) {
                case R.id.cv_chat_history:
                    Intent chatIntent = new Intent(mContext, ChatActivity.class);
                    chatIntent.putExtra("otherUID", historyList.get(getAdapterPosition()).uid);
                    mContext.startActivity(chatIntent);
                    break;
            }

        }
    }

    /*private void printDifference(Context mContext, Date startDate, Date endDate, TextView textView) {
        long different;

        //milliseconds
        if (endDate.getTime() > startDate.getTime()) {
            different = endDate.getTime() - startDate.getTime();
        } else {
            different = startDate.getTime() - endDate.getTime();
        }
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

        SimpleDateFormat sd = new SimpleDateFormat("hh:mm a");
        String time_str = sd.format(different);
        String time = time_str.replace("am", "AM").replace("pm", "PM");

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
            *//*if (elapsedHours == 1) {
                textView.setText(String.format("%s %s", String.valueOf(elapsedHours), mContext.getResources().getString(R.string.hour_ago)));
            } else {
                textView.setText(String.format("%s %s", String.valueOf(elapsedHours), mContext.getResources().getString(R.string.hours_ago)));
            }*//*

            textView.setText(time);

            //   textView.setText(String.format("%s %s", String.valueOf(elapsedHours), mContext.getResources().getString(R.string.hours_ago)));

        } else if (elapsedMinutes != 0) {
            *//*if (elapsedMinutes == 1) {
                textView.setText(String.format("%s %s", String.valueOf(elapsedMinutes), mContext.getResources().getString(R.string.minute_ago)));
            } else {
                textView.setText(String.format("%s %s", String.valueOf(elapsedMinutes), mContext.getResources().getString(R.string.minutes_ago)));
            }*//*

            textView.setText(time);

            //  textView.setText(String.format("%s %s", String.valueOf(elapsedMinutes), mContext.getResources().getString(R.string.minutes_ago)));
        } else if (elapsedSeconds != 0) {
           *//* if (elapsedSeconds == 1) {
                textView.setText(String.format("%s %s", String.valueOf(elapsedSeconds), mContext.getResources().getString(R.string.second_ago)));
            } else {
                textView.setText(String.format("%s %s", String.valueOf(elapsedSeconds), mContext.getResources().getString(R.string.seconds_ago)));
            }*//*

            textView.setText(time);

            //  textView.setText(String.format("%s %s", String.valueOf(elapsedSeconds), mContext.getResources().getString(R.string.seconds_ago)));
        }
    }*/
}
