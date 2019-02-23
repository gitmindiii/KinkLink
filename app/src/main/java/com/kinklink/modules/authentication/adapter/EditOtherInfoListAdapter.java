package com.kinklink.modules.authentication.adapter;

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
import com.kinklink.modules.authentication.model.EditOtherInfoModel;

import java.util.ArrayList;

public class EditOtherInfoListAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<EditOtherInfoModel.DropDownListBean.OtherInfoBean> list;
    private InterestIdListener listener;

    // variable to track event time
    private long mLastClickTime = 0;

    public EditOtherInfoListAdapter(Context mContext, ArrayList<EditOtherInfoModel.DropDownListBean.OtherInfoBean> list, InterestIdListener listener) {
        this.mContext = mContext;
        this.list = list;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return list.size();
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
        View view = inflater.inflate(R.layout.view_interest_list_item, parent, false);

        RelativeLayout rl_adapter_interest = view.findViewById(R.id.rl_adapter_interest);
        TextView tv_list_items = view.findViewById(R.id.tv_interests);
        ImageView item_check_box = view.findViewById(R.id.interests_check_box);

        if (list.get(position).isChecked) {
            item_check_box.setVisibility(View.VISIBLE);
            rl_adapter_interest.setBackgroundColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
            tv_list_items.setTextColor(ContextCompat.getColor(mContext,R.color.white));
        } else {
            item_check_box.setVisibility(View.GONE);
            rl_adapter_interest.setBackgroundColor(ContextCompat.getColor(mContext,R.color.white));
            tv_list_items.setTextColor(ContextCompat.getColor(mContext,R.color.field_text_color));
        }

        tv_list_items.setText(list.get(position).value);

        rl_adapter_interest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                String interest = list.get(position).value;

                for (int i = 0; i < list.size(); i++) {
                    list.get(i).isChecked = false;
                }
                list.get(position).isChecked = true;

                listener.getInterest(interest);

                notifyDataSetChanged();
            }
        });
        return view;
    }
}
