package com.kinklink.modules.authentication.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AvtarModel implements Serializable {


    public String status;
    public String message;


    public class AvtarData implements Serializable{
        public String avatarId;
        public String avatarName;
        public String status;
        public String avatarUrl;
    }


    public List<AvtarData> data=new ArrayList<>();



}
