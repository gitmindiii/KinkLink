package com.kinklink.modules.authentication.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mindiii on 18/4/18.
 */

public class GetUserDetailModel implements Serializable {
    public String status;
    public String message;
    public UserDetailBean userDetail;

    public class UserDetailBean implements Serializable{
        public String userId;
        public String full_name;
        public String email;
        public String preference;
        public String date_of_birth;
        public String is_verify;
        public String defaultImg;
        public String show_kink;
        public int age;
        public String gender;
        public String looking_for;
        public String is_profile_complete;
        public String profile_step;
        public String policy_flag;
        public String social_id;
        public String social_type;
        public String auth_token;
        public String created_on;
        public String city;
        public String full_address;
        public String current_address;
        public String latitude;
        public String longitude;
        public String body_type;
        public String body_type_name;
        public String ethnicity;
        public String ethnicity_name;
        public String work;
        public String education;
        public String education_name;
        public String about;
        public String match;
        public String heart;
        public String distance_in_mi;
        public String offer_status;
        public String chat_status;
        public String like_status="";
        public String favorite_status;
        public String block_status;
        public String isOnline;
        public List<ImagesBean> images;
        public List<InterestsBean> interests;
        public List<NotInterestsBean> diffin;
        public List<String> differentInterest;
        public List<String> commonInterest;

        public class ImagesBean implements Serializable {
            public String userImageId;
            public String image;
            public String imageOriginal;
        }

        public class InterestsBean implements Serializable {
            public String userInterestId;
            public String interest;
            public String interestId;

        }

        public  class NotInterestsBean implements Serializable {
            public String userInterestId;
            public String interest;
            public String interestId;
        }
    }
}
