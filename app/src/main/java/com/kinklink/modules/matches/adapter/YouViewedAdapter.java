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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.kinklink.R;
import com.kinklink.modules.matches.activity.MatchProfileActivity;
import com.kinklink.modules.matches.model.ViewedMeModel;

import java.util.ArrayList;

public class YouViewedAdapter extends RecyclerView.Adapter<YouViewedAdapter.ViewHolder> {
    private ArrayList<ViewedMeModel> viewedMeList;
    private Context mContext;
    // variable to track event time
    private long mLastClickTime = 0;

    public YouViewedAdapter(Context mContext, ArrayList<ViewedMeModel> viewedMeList) {
        this.viewedMeList = viewedMeList;
        this.mContext = mContext;
    }

    @Override
    public YouViewedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.views_list, parent, false);
        return new YouViewedAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(YouViewedAdapter.ViewHolder holder, int position) {
        ViewedMeModel listBean = viewedMeList.get(position);

        String fullName = listBean.full_name;
        String name[] = fullName.split("\\s+");

        if (listBean.isOnline != null) {
            if (listBean.isOnline.equals("online")) {
                holder.tv_on_offline.setText(mContext.getResources().getString(R.string.online));
                holder.tv_on_offline.setTextColor(ContextCompat.getColor(mContext, R.color.accept_color));
            } else if (listBean.isOnline.equals("offline")) {
                holder.tv_on_offline.setText(mContext.getResources().getString(R.string.offline));
                holder.tv_on_offline.setTextColor(ContextCompat.getColor(mContext, R.color.yellow));
            }
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

    }

    @Override
    public int getItemCount() {
        return viewedMeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView iv_user_image, iv_like_icon, iv_match_check;
        private TextView tv_match_name, tv_match_age, tv_match_gender, tv_on_offline;
        private CardView card_view;

        ViewHolder(View itemView) {
            super(itemView);
            card_view = itemView.findViewById(R.id.card_view);
            iv_user_image = itemView.findViewById(R.id.iv_user_image);
            tv_match_name = itemView.findViewById(R.id.tv_match_name);
            tv_match_age = itemView.findViewById(R.id.tv_match_age);
            tv_match_gender = itemView.findViewById(R.id.tv_match_gender);
            iv_like_icon = itemView.findViewById(R.id.iv_like_icon);
            tv_on_offline = itemView.findViewById(R.id.tv_on_offline);
            iv_match_check = itemView.findViewById(R.id.iv_match_check);

            iv_like_icon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.active_view_ico));
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
                    //  ((MainActivity) mContext).addFragment(MatchProfileFragment.newInstance(viewedMeList.get(getAdapterPosition()).userId), true, R.id.fragment_place);
                    Intent intent = new Intent(mContext, MatchProfileActivity.class);
                    intent.putExtra("match_user_id", viewedMeList.get(getAdapterPosition()).userId);
                    mContext.startActivity(intent);
                    break;
            }
        }
    }
}