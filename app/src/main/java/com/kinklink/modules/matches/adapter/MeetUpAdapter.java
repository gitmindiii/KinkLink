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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.kinklink.R;
import com.kinklink.modules.matches.activity.MatchProfileActivity;
import com.kinklink.modules.matches.model.MeetUpModel;

import java.util.ArrayList;

public class MeetUpAdapter extends RecyclerView.Adapter<MeetUpAdapter.ViewHolder> {
    private ArrayList<MeetUpModel> meetUpList;
    private Context mContext;
    // variable to track event time
    private long mLastClickTime = 0;

    public MeetUpAdapter(ArrayList<MeetUpModel> meetUpList, Context mContext) {
        this.meetUpList = meetUpList;
        this.mContext = mContext;
    }

    @Override
    public MeetUpAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_meet_up_list, parent, false);
        return new MeetUpAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MeetUpAdapter.ViewHolder holder, int position) {
        MeetUpModel listBean = meetUpList.get(position);

        String fullName = listBean.full_name;
        String name[] = fullName.split("\\s+");

        if (listBean.isOnline != null) {
            if (listBean.isOnline.equals("online")) {
                holder.tv_on_offline.setText(mContext.getResources().getString(R.string.online));
                holder.tv_on_offline.setTextColor(ContextCompat.getColor(mContext,R.color.accept_color));
            } else if (listBean.isOnline.equals("offline")) {
                holder.tv_on_offline.setText(mContext.getResources().getString(R.string.offline));
                holder.tv_on_offline.setTextColor(ContextCompat.getColor(mContext,R.color.yellow));
            }
        }

        if (listBean.is_verify.equals("0")) {
            holder.iv_match_check.setVisibility(View.GONE);
        } else if (listBean.is_verify.equals("3")) {
            holder.iv_match_check.setVisibility(View.VISIBLE);
        }else {
            holder.iv_match_check.setVisibility(View.GONE);
        }

        if (listBean.distance_in_mi != null) {
            if (!listBean.distance_in_mi.equals("")) {
                holder.tv_miles.setText(String.format("%s %s", listBean.distance_in_mi, mContext.getResources().getString(R.string.miles)));
            } else {
                holder.rl_dis_miles.setVisibility(View.GONE);
            }
        }

        holder.tv_match_name.setText(name[0]);
        holder.tv_match_age.setText(String.format("%s %s,", listBean.age, mContext.getResources().getString(R.string.yr)));

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

        Glide.with(mContext).load(listBean.image).apply(new RequestOptions().placeholder(ContextCompat.getDrawable(mContext,R.drawable.app_icon))).into(holder.iv_user_image);
    }

    @Override
    public int getItemCount() {
        return meetUpList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView iv_user_image, iv_match_check;
        private TextView tv_match_name, tv_match_age, tv_match_gender, tv_miles, tv_on_offline;
        private CardView card_view;
        private RelativeLayout rl_dis_miles;

        ViewHolder(View itemView) {
            super(itemView);
            card_view = itemView.findViewById(R.id.card_view);
            iv_user_image = itemView.findViewById(R.id.iv_user_image);
            tv_match_name = itemView.findViewById(R.id.tv_match_name);
            tv_match_age = itemView.findViewById(R.id.tv_match_age);
            tv_match_gender = itemView.findViewById(R.id.tv_match_gender);
            tv_miles = itemView.findViewById(R.id.tv_miles);
            rl_dis_miles = itemView.findViewById(R.id.rl_dis_miles);
            tv_on_offline = itemView.findViewById(R.id.tv_on_offline);
            iv_match_check = itemView.findViewById(R.id.iv_match_check);

            card_view.setOnClickListener(this);
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
                  //  ((MainActivity) mContext).addFragment(MatchProfileFragment.newInstance(meetUpList.get(getAdapterPosition()).userId), true, R.id.fragment_place);
                    Intent intent = new Intent(mContext, MatchProfileActivity.class);
                    intent.putExtra("match_user_id", meetUpList.get(getAdapterPosition()).userId);
                    mContext.startActivity(intent);
                    break;
            }
        }
    }
}