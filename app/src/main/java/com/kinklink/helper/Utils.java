package com.kinklink.helper;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kinklink.modules.authentication.model.OnlineInfo;
import com.kinklink.session.Session;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    public static Date date;
    public static SimpleDateFormat format;

    public static void setTypeface(TextView tv, Context context, int fontres) {
        tv.setTypeface(ResourcesCompat.getFont(context, fontres));
    }

    // Changing date format from YYYY-MM-DD to MM-DD-YYYY
    public static String dateInMDYFormat(String string) {
        try {
            date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(string);
            format = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return format.format(date);
    }

    // Changing date format from MM-DD-YYYY to  YYYY-MM-DD
    public static String dateInYMDFormat(String string) {
        try {
            date = new SimpleDateFormat("MM-dd-yyyy", Locale.US).parse(string);
            format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        return format.format(date);
    }

    public static void goToOnlineStatus(Context context, String status) {
        Session session = new Session(context);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        if (session.getRegistration() != null && session.getRegistration().userDetail != null) {
            if (session.getRegistration().userDetail.userId != null && !session.getRegistration().userDetail.userId.equals("")) {
                OnlineInfo onlineInfo = new OnlineInfo();
                onlineInfo.lastOnline = status;
                onlineInfo.email = session.getRegistration().userDetail.email;
                onlineInfo.uid = session.getRegistration().userDetail.userId;

                database.child(Constant.ONLINE_TABLE)
                        .child(session.getRegistration().userDetail.userId)
                        .setValue(onlineInfo);
            }
        }
    }

}
