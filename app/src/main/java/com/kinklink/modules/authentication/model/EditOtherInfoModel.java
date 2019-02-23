package com.kinklink.modules.authentication.model;

import java.util.List;

public class EditOtherInfoModel {
    public String status;
    public String message;
    public DropDownListBean dropDownList;

    public static class DropDownListBean {
        public List<OtherInfoBean> education;
        public List<OtherInfoBean> ethnicity;
        public List<OtherInfoBean> bodyType;

        public static class OtherInfoBean {
            public String id;
            public String value;
            public boolean isChecked = false;
        }
    }
}
