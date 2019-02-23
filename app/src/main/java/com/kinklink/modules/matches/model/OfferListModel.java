package com.kinklink.modules.matches.model;

import java.util.List;

public class OfferListModel {
    public String status;
    public String message;
    public String timeOut;
    public List<OfferListBean> offerList;

    public static class OfferListBean {
        public String offerId;
        public String offer_by;
        public String offer_for;
        public String offer_type;
        public String pay_by;
        public String offer_status;
        public String offer_amount;
        public String counter_apply;
        public String counter_status;
        public String counter_amount;
        public String offer_message;
        public String created_on;
        public String updated_on;
        public String cur_time;
        public String userId;
        public String full_name;
        public String gender;
        public String is_verify;
        public String age;
        public String image;
        public String city;
        public long timerTime;

    }
   /* public String status;
    public String message;
    public List<OfferListBean> offerList;

    public static class OfferListBean {
        public String offerId;
        public String offer_by;
        public String offer_for;
        public String offer_type;
        public String pay_by;
        public String offer_status;
        public String offer_amount;
        public String counter_apply;
        public String counter_status;
        public String counter_amount;
        public String offer_message;
        public String created_on;
        public String updated_on;
        public String cur_time;
        public String userId;
        public String full_name;
        public String gender;
        public String age;
        public String image;
        public String city;
    }*/


}
