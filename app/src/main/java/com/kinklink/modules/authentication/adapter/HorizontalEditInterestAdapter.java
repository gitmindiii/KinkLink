package com.kinklink.modules.authentication.adapter;

import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinklink.R;
import com.kinklink.modules.authentication.listener.AdapterPositionListener;
import com.kinklink.modules.authentication.model.InterestsModel;

import java.util.ArrayList;

public class HorizontalEditInterestAdapter extends RecyclerView.Adapter<HorizontalEditInterestAdapter.ViewHolder> {
    private ArrayList<InterestsModel> interestsList;
    private AdapterPositionListener listener;
    // variable to track event time
    private long mLastClickTime = 0;

    public HorizontalEditInterestAdapter(ArrayList<InterestsModel> interestsList, AdapterPositionListener listener) {
        this.interestsList = interestsList;
        this.listener = listener;
    }

    @Override
    public HorizontalEditInterestAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_horizontal_interests, parent, false);
        return new HorizontalEditInterestAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(HorizontalEditInterestAdapter.ViewHolder holder, int position) {
        String interest = interestsList.get(position).interest.substring(0, 1).toUpperCase() + interestsList.get(position).interest.substring(1);
        holder.item_interest.setText(interest);
    }

    @Override
    public int getItemCount() {
        return interestsList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView item_interest;
        private ImageView cancel_icon;

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            item_interest = itemView.findViewById(R.id.item_interest);
            cancel_icon = itemView.findViewById(R.id.cancel_icon);
            cancel_icon.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Preventing multiple clicks, using threshold of 1/2 second
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            switch (v.getId()) {
                case R.id.cancel_icon:
                    listener.getPosition(getAdapterPosition());
                    break;
            }
        }
    }
}
