package com.kinklink.modules.authentication.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mindiii on 18/4/18.
 */

public class RegistrationInfo implements Serializable {
    public String status;
    public String message;
    public UserDetailBean userDetail = new UserDetailBean();
    public String policy;

    public static class UserDetailBean {
        public String userId;
        public String full_name;
        public String email;
        public String preference;
        public String date_of_birth;
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
        public String defaultImg;
        public String isEmailVerified;
        public String verify_update;
        public String tease_update;
        public String favorite_update;
        public String view_update;
        public String chat_update;



        public List<ImagesBean> images = new ArrayList<>();
        public List<InterestsBean> interests = new ArrayList<>();

        public static class ImagesBean {
            public String userImageId;
            public String image;
        }

        public static class InterestsBean {
            public String userInterestId;
            public String interest;
            public String interestId;
        }
    }
}
