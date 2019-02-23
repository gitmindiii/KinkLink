package com.kinklink.modules.matches.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.kinklink.R;
import com.kinklink.application.KinkLink;
import com.kinklink.modules.matches.activity.MatchProfileActivity;
import com.kinklink.modules.matches.listener.AdapterViewPositionListener;
import com.kinklink.modules.matches.model.LikesModel;
import com.kinklink.modules.matches.model.OfferListModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import cn.iwgang.countdownview.CountdownView;

public class LikesAdapter extends RecyclerView.Adapter<LikesAdapter.ViewHolder> {
    private ArrayList<LikesModel> likesList;
    private Context mContext;
    // variable to track event time
    private long mLastClickTime = 0;
    private AdapterViewPositionListener adapterViewPositionListener;


    public LikesAdapter(Context mContext, ArrayList<LikesModel> likesList,AdapterViewPositionListener adapterViewPositionListener) {
        this.likesList = likesList;
        this.mContext = mContext;
        this.adapterViewPositionListener=adapterViewPositionListener;
    }

    @Override
    public LikesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_likes_list, parent, false);
        return new LikesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LikesAdapter.ViewHolder holder, int position) {
        LikesModel listBean = likesList.get(position);

        String fullName = listBean.full_name;
        String name[] = fullName.split("\\s+");

       /* if (listBean.isOnline != null) {
            if (listBean.isOnline.equals("online")) {
                holder.tv_on_offline.setText(mContext.getResources().getString(R.string.online));
                holder.tv_on_offline.setTextColor(ContextCompat.getColor(mContext, R.color.accept_color));
            } else if (listBean.isOnline.equals("offline")) {
                holder.tv_on_offline.setText(mContext.getResources().getString(R.string.offline));
                holder.tv_on_offline.setTextColor(ContextCompat.getColor(mContext, R.color.yellow));
            }
        }*/

       if (position==likesList.size()-1){
           holder.txt_gap.setVisibility(View.VISIBLE);
       }else {
           holder.txt_gap.setVisibility(View.GONE);
       }

       if (listBean.city.equals("")){
           holder.txtCity.setText("NA");
       }else {
           holder.txtCity.setText(listBean.city);
       }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(mContext.getResources().getString(R.string.date_time_format), Locale.US);

        try {
            Date date1 = simpleDateFormat.parse(listBean.created_on);
            Date date2 = simpleDateFormat.parse(listBean.current_time);

            printDifference(mContext,date1, date2, holder.tv_on_offline);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (listBean.is_verify.equals("0")) {
            holder.iv_match_check.setVisibility(View.GONE);
        } else if (listBean.is_verify.equals("3")) {
            holder.iv_match_check.setVisibility(View.VISIBLE);
        } else {
            holder.iv_match_check.setVisibility(View.GONE);
        }

        holder.tv_match_name.setText(name[0]);
        holder.tv_match_age.setText(String.format("%s %s,", listBean.age, mContext.getResources().getString(R.string.yr)));


        switch (listBean.like_status) {
            case "1":
                holder.refreshTime(listBean.timerTime - System.currentTimeMillis());
                if (listBean.requestType.equals("1")&&listBean.like_status.equals("1")){
                    holder.ly_accept_reject.setVisibility(View.VISIBLE);
                    holder.txtStatus.setVisibility(View.GONE);
                }else {
                    holder.ly_accept_reject.setVisibility(View.GONE);
                    holder.txtStatus.setText(mContext.getResources().getString(R.string.pending));
                    holder.txtStatus.setTextColor(ContextCompat.getColor(mContext,R.color.yellow));
                    holder.txtStatus.setVisibility(View.VISIBLE);
                }
                break;

            case "2":
                holder.stopCounterTimer();
                if (listBean.requestType.equals("1")){
                    holder.ly_accept_reject.setVisibility(View.VISIBLE);
                }else holder.ly_accept_reject.setVisibility(View.GONE);
                holder.ly_counter_price.setVisibility(View.GONE);
                holder.txtStatus.setVisibility(View.GONE);
                holder.txtStatus.setText(mContext.getResources().getString(R.string.request_accepted));
                holder.txtStatus.setTextColor(ContextCompat.getColor(mContext,R.color.accept_color));
                holder.txtStatus.setVisibility(View.VISIBLE);
                break;

            case "3":
                holder.stopCounterTimer();
                if (listBean.requestType.equals("1")){
                    holder.ly_accept_reject.setVisibility(View.VISIBLE);
                }else holder.ly_accept_reject.setVisibility(View.GONE);

                holder.txtStatus.setVisibility(View.GONE);
                holder.ly_counter_price.setVisibility(View.VISIBLE);
               // holder.tv_counter_price.setText(offerModel.counter_amount);

                /*switch (offerModel.counter_status) {
                    case "0":
                        holder.ly_accept_reject.setVisibility(View.VISIBLE);
                        holder.tv_offer_status.setText(mContext.getResources().getString(R.string.counter_applied));
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
                }*/

                break;
        }

        String gender = "";
        switch (listBean.gender) {
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
        holder.tv_match_gender.setText(gender);

        //  final String userImageUrl = "https://graph.facebook.com/" + sSocialId + "/picture?type=large";
        //http:graph.facebook.com/1924247060919450/picture?type=large

        Glide.with(mContext).load(listBean.image).apply(new RequestOptions().placeholder(ContextCompat.getDrawable(mContext, R.drawable.app_icon))).into(holder.iv_user_image);

        //String teases_time[] = listBean.time_to_end.split("in");
        holder.tv_teases_text.setVisibility(View.GONE);
        holder.tv_teases_time.setVisibility(View.GONE);
       // holder.tv_teases_text.setText(teases_time[0] + "in");
      //  holder.tv_teases_time.setText(teases_time[1]);




    }


    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getAdapterPosition();
        LikesModel listBean = likesList.get(position);

        if (listBean.like_status.equals("1")) {
            holder.refreshTime(listBean.timerTime - System.currentTimeMillis());
        }

      /*  if (holder instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) holder;

            if (offerModel.offer_status.equals("0")) {
                holder.refreshTime(offerModel.timerTime - System.currentTimeMillis());
            }
        } else if (holder instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) h;

            if (offerModel.offer_status.equals("0")) {
                holder.refreshTime(offerModel.timerTime - System.currentTimeMillis());
            }
        }*/

    }


    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
       // holder.getCvCountdownView().stop();
    }

    @Override
    public int getItemCount() {
        return likesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView iv_user_image, iv_match_check;
        private TextView tv_match_name, tv_match_age,txtCity, tv_match_gender, tv_on_offline, tv_teases_text, tv_teases_time,btn_accept_offer,btn_reject_offer,txtStatus,txt_gap;
        private CardView card_view;
        private LinearLayout ly_teases_status,ly_accept_reject,ly_counter_price;
        CountdownView countdown_view;


        ViewHolder(View itemView) {
            super(itemView);
            card_view = itemView.findViewById(R.id.card_view);
            txt_gap = itemView.findViewById(R.id.txt_gap);
            iv_user_image = itemView.findViewById(R.id.iv_user_image);
            tv_match_name = itemView.findViewById(R.id.tv_match_name);
            btn_accept_offer=itemView.findViewById(R.id.btn_accept_offer);
            btn_reject_offer=itemView.findViewById(R.id.btn_reject_offer);
            txtCity = itemView.findViewById(R.id.txtCity);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            tv_match_age = itemView.findViewById(R.id.tv_match_age);
            tv_match_gender = itemView.findViewById(R.id.tv_match_gender);
            ly_accept_reject = itemView.findViewById(R.id.ly_accept_reject);
            tv_on_offline = itemView.findViewById(R.id.tv_on_offline);
            iv_match_check = itemView.findViewById(R.id.iv_match_check);
            ly_teases_status = itemView.findViewById(R.id.ly_teases_status);
            countdown_view = itemView.findViewById(R.id.countdown_view);
            tv_teases_text = itemView.findViewById(R.id.tv_teases_text);
            tv_teases_time = itemView.findViewById(R.id.tv_teases_time);
            ly_counter_price = itemView.findViewById(R.id.ly_counter_price);

            ly_teases_status.setVisibility(View.VISIBLE);

            card_view.setOnClickListener(this);
            btn_reject_offer.setOnClickListener(this);
            btn_accept_offer.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Preventing multiple clicks, using threshold of 1/2 second
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            switch (v.getId()) {
                case R.id.card_view:
                    KinkLink.getInstance().cancelAllRequests(KinkLink.TAG);
                    // ((MainActivity) mContext).addFragment(MatchProfileFragment.newInstance(likesList.get(getAdapterPosition()).userId), true, R.id.fragment_place);
                    Intent intent = new Intent(mContext, MatchProfileActivity.class);
                    intent.putExtra("match_user_id", likesList.get(getAdapterPosition()).userId);
                    mContext.startActivity(intent);
                    break;

                case R.id.btn_accept_offer:
                    adapterViewPositionListener.getAcceptClick(getAdapterPosition());
                    stopCounterTimer();
                    //ly_accept_reject.setVisibility(View.GONE);
                    txtStatus.setText(mContext.getResources().getString(R.string.request_accepted));
                    txtStatus.setTextColor(ContextCompat.getColor(mContext,R.color.accept_color));
                   // btn_accept_offer.setVisibility(View.GONE);
                   // tv_pending_status.setVisibility(View.GONE);

                    break;

                case R.id.btn_reject_offer:
                    adapterViewPositionListener.getRejectClick(getAdapterPosition());
                    stopCounterTimer();
                    //ly_accept_reject.setVisibility(View.GONE);
                    txtStatus.setText(mContext.getResources().getString(R.string.request_rejected));
                    txtStatus.setTextColor(ContextCompat.getColor(mContext,R.color.active_text_color));
                   // btn_accept_offer.setVisibility(View.VISIBLE);
                    //tv_pending_status.setVisibility(View.GONE);

                    break;
            }
        }

        void refreshTime(long leftTime) {
            if (leftTime > 0) {
                countdown_view.start(leftTime);
                countdown_view.setVisibility(View.VISIBLE);

              /*  ly_accept_reject.setVisibility(View.GONE);
                tv_pending_status.setText(mContext.getResources().getString(R.string.request_pending));
                tv_pending_status.setVisibility(View.VISIBLE);
                tv_offer_status.setVisibility(View.VISIBLE);
                tv_offer_status.setText(mContext.getResources().getString(R.string.time_left_for_offer));
                tv_offer_status.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));*/
            } else {
                stopCounterTimer();

              /*  ly_accept_reject.setVisibility(View.GONE);

                tv_offer_status.setText(mContext.getResources().getString(R.string.request_timeout));
                tv_pending_status.setVisibility(View.GONE);
                tv_offer_status.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));
                tv_offer_status.setVisibility(View.VISIBLE);*/
            }

            countdown_view.setOnCountdownEndListener(new CountdownView.OnCountdownEndListener() {
                @Override
                public void onEnd(CountdownView cv) {
                    countdown_view.setVisibility(View.GONE);

                  /*  ly_accept_reject.setVisibility(View.GONE);

                    tv_offer_status.setText(mContext.getResources().getString(R.string.request_timeout));
                    tv_pending_status.setVisibility(View.GONE);
                    tv_offer_status.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));
                    tv_offer_status.setVisibility(View.VISIBLE);*/
                }
            });
        }

        void stopCounterTimer() {
            countdown_view.stop();
            countdown_view.allShowZero();
            countdown_view.setVisibility(View.GONE);
        }

        public CountdownView getCvCountdownView() {
            return countdown_view;
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