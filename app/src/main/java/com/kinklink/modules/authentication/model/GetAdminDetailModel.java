package com.kinklink.modules.authentication.model;

import java.io.Serializable;
import java.util.List;

public class GetAdminDetailModel implements Serializable {


    /**
     * status : success
     * message : Admin detail
     * admin : {"id":"1","name":"KinkLink Admin$#%&","email":"admin@kinklink.com","password":"$2y$10$kE7SBojn3y.QPk1F0qfUJeKh3FNjHpWhm9OjilGYBZwThN6umuHq6","profile_image":"http://dev.kinklink.com/uploads/user_avatar/medium/37FU4YWwMjNLyv8i.jpeg","date_of_birth":"2019-01-18","gender":"man","ethnicity":"1","work":"CEO of KinkLink","education":"6","about":"KinkLink.com is strictly an online matching service for people who are looking for a dates. This is not an escort site, nor will we permit any type of escorting on this site.","educationName":"Bachelors Degree","ethnicityName":"Caucasian","kink":[{"interestId":"1","interest":"chess","type":"0","status":"1","created_on":"2018-04-03 04:24:18"},{"interestId":"2","interest":"kabaddy","type":"0","status":"1","created_on":"2018-04-03 04:24:18"},{"interestId":"3","interest":"kho kho","type":"0","status":"1","created_on":"2018-04-03 04:24:18"},{"interestId":"4","interest":"listening music","type":"0","status":"1","created_on":"2018-04-04 22:32:29"},{"interestId":"5","interest":"painting","type":"0","status":"1","created_on":"2018-04-04 22:39:07"},{"interestId":"6","interest":"singing","type":"0","status":"1","created_on":"2018-04-05 04:09:27"},{"interestId":"7","interest":"travelling","type":"0","status":"1","created_on":"2018-04-18 02:38:20"},{"interestId":"8","interest":"watching movie","type":"0","status":"1","created_on":"2018-04-05 05:31:53"},{"interestId":"9","interest":"shopping","type":"0","status":"1","created_on":"2018-04-05 05:37:54"},{"interestId":"10","interest":"dancing","type":"0","status":"1","created_on":"2018-04-05 05:44:13"},{"interestId":"21","interest":"games","type":"0","status":"1","created_on":"2018-04-27 07:29:53"},{"interestId":"22","interest":"net surfing","type":"0","status":"1","created_on":"2018-04-27 07:33:12"},{"interestId":"23","interest":"sport","type":"0","status":"1","created_on":"2018-04-27 07:58:48"},{"interestId":"24","interest":"searching","type":"0","status":"1","created_on":"2018-04-27 08:00:12"}]}
     */

    private String status;
    private String message;
    private AdminBean admin;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AdminBean getAdmin() {
        return admin;
    }

    public void setAdmin(AdminBean admin) {
        this.admin = admin;
    }

    public static class AdminBean implements Serializable{
        /**
         * id : 1
         * name : KinkLink Admin$#%&
         * email : admin@kinklink.com
         * password : $2y$10$kE7SBojn3y.QPk1F0qfUJeKh3FNjHpWhm9OjilGYBZwThN6umuHq6
         * profile_image : http://dev.kinklink.com/uploads/user_avatar/medium/37FU4YWwMjNLyv8i.jpeg
         * date_of_birth : 2019-01-18
         * gender : man
         * ethnicity : 1
         * work : CEO of KinkLink
         * education : 6
         * about : KinkLink.com is strictly an online matching service for people who are looking for a dates. This is not an escort site, nor will we permit any type of escorting on this site.
         * educationName : Bachelors Degree
         * ethnicityName : Caucasian
         * kink : [{"interestId":"1","interest":"chess","type":"0","status":"1","created_on":"2018-04-03 04:24:18"},{"interestId":"2","interest":"kabaddy","type":"0","status":"1","created_on":"2018-04-03 04:24:18"},{"interestId":"3","interest":"kho kho","type":"0","status":"1","created_on":"2018-04-03 04:24:18"},{"interestId":"4","interest":"listening music","type":"0","status":"1","created_on":"2018-04-04 22:32:29"},{"interestId":"5","interest":"painting","type":"0","status":"1","created_on":"2018-04-04 22:39:07"},{"interestId":"6","interest":"singing","type":"0","status":"1","created_on":"2018-04-05 04:09:27"},{"interestId":"7","interest":"travelling","type":"0","status":"1","created_on":"2018-04-18 02:38:20"},{"interestId":"8","interest":"watching movie","type":"0","status":"1","created_on":"2018-04-05 05:31:53"},{"interestId":"9","interest":"shopping","type":"0","status":"1","created_on":"2018-04-05 05:37:54"},{"interestId":"10","interest":"dancing","type":"0","status":"1","created_on":"2018-04-05 05:44:13"},{"interestId":"21","interest":"games","type":"0","status":"1","created_on":"2018-04-27 07:29:53"},{"interestId":"22","interest":"net surfing","type":"0","status":"1","created_on":"2018-04-27 07:33:12"},{"interestId":"23","interest":"sport","type":"0","status":"1","created_on":"2018-04-27 07:58:48"},{"interestId":"24","interest":"searching","type":"0","status":"1","created_on":"2018-04-27 08:00:12"}]
         */

        private String id;
        private String name;
        private String email;
        private String password;
        private String profile_image;
        private String date_of_birth;
        private String gender;
        private String ethnicity;
        private String work;
        private String education;
        private String about;
        private String educationName;
        private String ethnicityName;
        private List<KinkBean> kink;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getProfile_image() {
            return profile_image;
        }

        public void setProfile_image(String profile_image) {
            this.profile_image = profile_image;
        }

        public String getDate_of_birth() {
            return date_of_birth;
        }

        public void setDate_of_birth(String date_of_birth) {
            this.date_of_birth = date_of_birth;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getEthnicity() {
            return ethnicity;
        }

        public void setEthnicity(String ethnicity) {
            this.ethnicity = ethnicity;
        }

        public String getWork() {
            return work;
        }

        public void setWork(String work) {
            this.work = work;
        }

        public String getEducation() {
            return education;
        }

        public void setEducation(String education) {
            this.education = education;
        }

        public String getAbout() {
            return about;
        }

        public void setAbout(String about) {
            this.about = about;
        }

        public String getEducationName() {
            return educationName;
        }

        public void setEducationName(String educationName) {
            this.educationName = educationName;
        }

        public String getEthnicityName() {
            return ethnicityName;
        }

        public void setEthnicityName(String ethnicityName) {
            this.ethnicityName = ethnicityName;
        }

        public List<KinkBean> getKink() {
            return kink;
        }

        public void setKink(List<KinkBean> kink) {
            this.kink = kink;
        }

        public static class KinkBean implements Serializable{
            /**
             * interestId : 1
             * interest : chess
             * type : 0
             * status : 1
             * created_on : 2018-04-03 04:24:18
             */

            private String interestId;
            private String interest;
            private String type;
            private String status;
            private String created_on;

            public String getInterestId() {
                return interestId;
            }

            public void setInterestId(String interestId) {
                this.interestId = interestId;
            }

            public String getInterest() {
                return interest;
            }

            public void setInterest(String interest) {
                this.interest = interest;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }

            public String getCreated_on() {
                return created_on;
            }

            public void setCreated_on(String created_on) {
                this.created_on = created_on;
            }
        }
    }
}
