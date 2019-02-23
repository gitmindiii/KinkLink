package com.kinklink.modules.matches.model;

import java.util.List;

public class MyVisitsModel {
    public String status;
    public String message;
    public List<VisitListBean> visitList;

    public static class VisitListBean {
        public String visitId;
        public String user_id;
        public String from_date;
        public String to_date;
        public String city;
        public String address;
        public String location;
        public String latitude;
        public String longitude;
    }
}
