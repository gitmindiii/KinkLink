package com.kinklink.modules.matches.adapter;

import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinklink.R;
import com.kinklink.helper.Utils;
import com.kinklink.modules.matches.listener.MyVisitPositionListener;
import com.kinklink.modules.matches.model.MyVisitsModel;

import java.util.ArrayList;

public class MyVisitsAdapter extends RecyclerView.Adapter<MyVisitsAdapter.ViewHolder> {
    private ArrayList<MyVisitsModel.VisitListBean> visitList;
    private MyVisitPositionListener listener;
    // variable to track event time
    private long mLastClickTime = 0;

    public MyVisitsAdapter(ArrayList<MyVisitsModel.VisitListBean> visitList, MyVisitPositionListener listener) {
        this.visitList = visitList;
        this.listener = listener;
    }

    @Override
    public MyVisitsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_my_visits_item, parent, false);
        return new MyVisitsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyVisitsAdapter.ViewHolder holder, int position) {
        MyVisitsModel.VisitListBean listBean = visitList.get(position);

        holder.tv_from_date.setText(Utils.dateInMDYFormat(listBean.from_date));
        holder.tv_to_date.setText(Utils.dateInMDYFormat(listBean.to_date));
        holder.tv_visit_city.setText(listBean.location);

       /* if(position == visitList.size()-1){
            holder.bottom_view.setVisibility(View.VISIBLE);
        }else {
            holder.bottom_view.setVisibility(View.GONE);
        }*/
    }

    @Override
    public int getItemCount() {
        return visitList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_from_date, tv_to_date, tv_visit_city;
        ImageView iv_edit_visit, iv_delete_visit;

        ViewHolder(View itemView) {
            super(itemView);
            tv_from_date = itemView.findViewById(R.id.tv_from_date);
            tv_to_date = itemView.findViewById(R.id.tv_to_date);
            tv_visit_city = itemView.findViewById(R.id.tv_visit_city);
            iv_edit_visit = itemView.findViewById(R.id.iv_edit_visit);
            iv_delete_visit = itemView.findViewById(R.id.iv_delete_visit);

            iv_edit_visit.setOnClickListener(this);
            iv_delete_visit.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Preventing multiple clicks, using threshold of 1/2 second
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            switch (v.getId()) {
                case R.id.iv_edit_visit:
                    listener.getEditClick(getAdapterPosition());
                    break;

                case R.id.iv_delete_visit:
                    listener.getDeleteClick(getAdapterPosition());
                    break;
            }
        }
    }
}