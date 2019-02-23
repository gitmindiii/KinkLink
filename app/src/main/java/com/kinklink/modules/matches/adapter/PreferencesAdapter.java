package com.kinklink.modules.matches.adapter;

import android.content.Context;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kinklink.R;
import com.kinklink.modules.authentication.listener.InterestIdListener;
import com.kinklink.modules.authentication.model.BasicListInfoModel;

import java.util.ArrayList;

public class PreferencesAdapter extends BaseAdapter {
    Context mContext;
    ArrayList<BasicListInfoModel> prefList;
    String selectItem;
    InterestIdListener listener;

    // variable to track event time
    private long mLastClickTime = 0;

    public PreferencesAdapter(String selectItem, Context mContext, ArrayList<BasicListInfoModel> prefList, InterestIdListener interestIdListener) {
        this.mContext = mContext;
        this.prefList = prefList;
        this.listener = interestIdListener;
        this.selectItem = selectItem;
    }

    @Override
    public int getCount() {
        return prefList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View view = inflater.inflate(R.layout.view_interest_list_item, parent, false);

        RelativeLayout rl_adapter_interest = view.findViewById(R.id.rl_adapter_interest);
        TextView tv_interests = view.findViewById(R.id.tv_interests);
        ImageView interests_check_box = view.findViewById(R.id.interests_check_box);


        if (prefList.get(position).isChecked) {
            interests_check_box.setVisibility(View.VISIBLE);
            rl_adapter_interest.setBackgroundColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
            tv_interests.setTextColor(ContextCompat.getColor(mContext,R.color.white));
        } else {
            interests_check_box.setVisibility(View.GONE);
            rl_adapter_interest.setBackgroundColor(ContextCompat.getColor(mContext,R.color.white));
            tv_interests.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));
        }

        String interest = prefList.get(position).selected_item.substring(0, 1).toUpperCase() + prefList.get(position).selected_item.substring(1);
        tv_interests.setText(interest);

        rl_adapter_interest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                String interest = prefList.get(position).item_key;

                if (prefList.get(position).isChecked) {
                    prefList.get(position).isChecked = false;

                    if (selectItem.contains(interest + ",")) {
                        selectItem = selectItem.replace(interest + ",", "");
                    } else if (selectItem.contains("," + interest)) {
                        selectItem = selectItem.replace("," + interest, "");

                    } else if (selectItem.contains(interest)) {
                        selectItem = selectItem.replace(interest, "");
                    }
                } else {
                    prefList.get(position).isChecked = true;

                    if (selectItem.length() == 0) {
                        selectItem = interest + selectItem;
                    } else {
                        selectItem = interest + "," + selectItem;
                    }
                }
                listener.getInterest(selectItem);

                notifyDataSetChanged();
            }
        });

        return view;
    }
}
