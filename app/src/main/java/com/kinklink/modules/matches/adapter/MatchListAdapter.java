package com.kinklink.modules.matches.adapter;

import android.content.Context;
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
import com.kinklink.modules.authentication.listener.AdapterPositionListener;
import com.kinklink.modules.matches.model.MatchListModel;

import java.util.ArrayList;

public class MatchListAdapter extends RecyclerView.Adapter<MatchListAdapter.ViewHolder> {
    private ArrayList<MatchListModel> matchList;
    private Context mContext;
    private AdapterPositionListener listener;
    // variable to track event time
    private long mLastClickTime = 0;

    public MatchListAdapter(ArrayList<MatchListModel> matchList, Context mContext, AdapterPositionListener listener) {
        this.matchList = matchList;
        this.mContext = mContext;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_match_list, parent, false);
        return new MatchListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MatchListModel itemModel = matchList.get(position);

        if (itemModel.isOnline != null) {
            if (itemModel.isOnline.equals("online")) {
                holder.tv_on_offline.setText(mContext.getResources().getString(R.string.online));
                holder.tv_on_offline.setTextColor(mContext.getResources().getColor(R.color.accept_color));
            } else if (itemModel.isOnline.equals("offline")) {
                holder.tv_on_offline.setText(mContext.getResources().getString(R.string.offline));
                holder.tv_on_offline.setTextColor(mContext.getResources().getColor(R.color.yellow));
            }
        }

        String fullName = itemModel.full_name;
        String name[] = fullName.split("\\s+");

        if (itemModel.distance_in_mi != null) {
            if (!itemModel.distance_in_mi.equals("")) {
                holder.tv_miles.setText(String.format("%s %s", itemModel.distance_in_mi, mContext.getResources().getString(R.string.miles)));
            } else {
                holder.rl_dis_miles.setVisibility(View.GONE);
            }
        }

        if (itemModel.is_verify.equals("0")) {
            holder.iv_match_check.setVisibility(View.GONE);
        } else if (itemModel.is_verify.equals("3")) {
            holder.iv_match_check.setVisibility(View.VISIBLE);
        } else {
            holder.iv_match_check.setVisibility(View.GONE);
        }

        holder.tv_match_name.setText(name[0]);
        holder.tv_match_age.setText(String.format("%s %s,", itemModel.age, mContext.getResources().getString(R.string.yr)));

        String gender = "";
        switch (itemModel.gender) {
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
        holder.tv_match_percentage.setText(String.format("%s%%", itemModel.percentage));

        //  final String userImageUrl = "https://graph.facebook.com/" + sSocialId + "/picture?type=large";
        //http:graph.facebook.com/1924247060919450/picture?type=large

        Glide.with(mContext).load(itemModel.image).apply(new RequestOptions().placeholder(ContextCompat.getDrawable(mContext, R.drawable.app_icon))).into(holder.iv_user_image);
        // Picasso.with(mContext).load( matchList.get(position).image).fit().into(holder.iv_user_image);

    }

    @Override
    public int getItemCount() {
        return matchList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView iv_user_image, iv_match_check;
        private TextView tv_match_name, tv_match_age, tv_match_gender, tv_match_percentage, tv_miles, tv_on_offline;
        private CardView card_view;
        private RelativeLayout rl_dis_miles;


        ViewHolder(View itemView) {
            super(itemView);
            card_view = itemView.findViewById(R.id.card_view);
            iv_user_image = itemView.findViewById(R.id.iv_user_image);
            iv_match_check = itemView.findViewById(R.id.iv_match_check);
            tv_match_name = itemView.findViewById(R.id.tv_match_name);
            tv_match_age = itemView.findViewById(R.id.tv_match_age);
            tv_match_gender = itemView.findViewById(R.id.tv_match_gender);
            tv_match_percentage = itemView.findViewById(R.id.tv_match_percentage);
            tv_miles = itemView.findViewById(R.id.tv_miles);
            rl_dis_miles = itemView.findViewById(R.id.rl_dis_miles);
            tv_on_offline = itemView.findViewById(R.id.tv_on_offline);

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
                    listener.getPosition(getAdapterPosition());
                    break;
            }
        }
    }
}
