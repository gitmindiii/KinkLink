package com.kinklink.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.kinklink.R;
import com.kinklink.helper.Constant;
import com.kinklink.modules.matches.activity.MainActivity;
import com.kinklink.session.Session;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    String list_type, type, title, message, uid, notify_for, notification_id, notification_by;
    String body, click_action, sound;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);


        Log.i("notification4254", "" + remoteMessage.getData().toString());

        list_type = remoteMessage.getData().get("list_type");
        type = remoteMessage.getData().get("type");
        title = remoteMessage.getData().get("title");
        message = remoteMessage.getData().get("message");
        notify_for = remoteMessage.getData().get("notify_for");
        uid = remoteMessage.getData().get("notify_for");//remoteMessage.getData().get("uid");
        notification_id = remoteMessage.getData().get("notification_id");
        notification_by = remoteMessage.getData().get("uid");

        body = remoteMessage.getData().get("body");
        click_action = remoteMessage.getData().get("click_action");
        sound = remoteMessage.getData().get("sound");

        Session session = new Session(getApplicationContext());
        String isEmailVerified = session.getRegistration().userDetail.isEmailVerified;


        if (type.equals("chat") && isEmailVerified.equals("1")) {
            if (!Constant.ChatOpponentId.equals(uid)) {
                uid = remoteMessage.getData().get("uid");
                sendChatNotification(title, body, uid, type);
            }
        } else if (type.equals("view_updates") && isEmailVerified.equals("1")) {
            Intent intent = new Intent("NOTIFICATIONCOUNT");
            intent.putExtra("from", "viewcount");
            MainActivity.profileViewCount = MainActivity.profileViewCount + 1;
            sendBroadcast(intent);
            sendNotification(list_type, title, body, type, click_action, notify_for, notification_id);
        } else {

            if (isEmailVerified.equals("0")) {
                sendAdminNotification(list_type, title, body, type, click_action, notify_for, notification_id);
            } else {
                Intent intent = new Intent("NOTIFICATIONCOUNT");
                intent.putExtra("from", "notification");
                MainActivity.notificationCount = MainActivity.notificationCount + 1;
                sendBroadcast(intent);
                sendNotification(list_type, title, body, type, click_action, notify_for, notification_id);
            }


        }
    }

    private void sendNotification(String list_type, String title, String body, String type, String click_action, String notify_for, String notification_id) {
        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra("list_type", list_type);
        intent.putExtra("type", type);
        intent.putExtra("notify_for", notify_for);
        intent.putExtra("title", title);
        intent.putExtra("body", body);
        intent.putExtra("click_action", click_action);
        intent.putExtra("notification_id", notification_id);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int iUniqueId = (int) (System.currentTimeMillis() & 0xfffffff);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), iUniqueId, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String CHANNEL_ID = "my_channel_01";// The id of the channel.
        CharSequence name = "Abc";// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = null;

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(this.title)
                .setContentText(this.body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setSmallIcon(R.drawable.noti_app_icon);
            notificationBuilder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
        } else {
            notificationBuilder.setSmallIcon(R.drawable.noti_app_icon);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(mChannel);

        }
        assert notificationManager != null;
        notificationManager.notify(iUniqueId, notificationBuilder.build());
    }

    private void sendChatNotification(String title, String body, String uid, String type) {
        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra("type", type);
        intent.putExtra("title", title);
        intent.putExtra("body", body);
        intent.putExtra("uid", uid);


        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int iUniqueId = (int) (System.currentTimeMillis() & 0xfffffff);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), iUniqueId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String CHANNEL_ID = "my_channel_01";// The id of the channel.
        CharSequence name = "Abc";// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = null;

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(this.title)
                .setContentText(this.body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setSmallIcon(R.drawable.noti_app_icon);
            notificationBuilder.setColor(getResources().getColor(R.color.gray));
        } else {
            notificationBuilder.setSmallIcon(R.drawable.noti_app_icon);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(mChannel);

        }
        assert notificationManager != null;
        notificationManager.notify(iUniqueId, notificationBuilder.build());
    }

    private void sendAdminNotification(String list_type, String title, String body, String type, String click_action, String notify_for, String notification_id) {


        int iUniqueId = (int) (System.currentTimeMillis() & 0xfffffff);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String CHANNEL_ID = "my_channel_01";// The id of the channel.
        CharSequence name = "Abc";// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = null;

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(this.title)
                .setContentText(this.body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri);


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setSmallIcon(R.drawable.noti_app_icon);
            notificationBuilder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
        } else {
            notificationBuilder.setSmallIcon(R.drawable.noti_app_icon);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(mChannel);

        }
        assert notificationManager != null;
        notificationManager.notify(iUniqueId, notificationBuilder.build());
    }

}
