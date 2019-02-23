package com.kinklink.modules.matches.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kinklink.R;
import com.kinklink.modules.authentication.model.InterestsModel;

import java.util.ArrayList;

public class MatchInterestAdapter extends RecyclerView.Adapter<MatchInterestAdapter.ViewHolder> {
    private ArrayList<InterestsModel> interestsList;

    public MatchInterestAdapter(ArrayList<InterestsModel> interestsList) {
        this.interestsList = interestsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_match_interest_recycler, parent, false);
        return new MatchInterestAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String interest = interestsList.get(position).interest.substring(0, 1).toUpperCase() + interestsList.get(position).interest.substring(1);
        holder.item_interest.setText(interest);
    }

    @Override
    public int getItemCount() {
        return interestsList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView item_interest;

        ViewHolder(View itemView) {
            super(itemView);
            item_interest = itemView.findViewById(R.id.item_interest);
        }
    }
}
