package com.kinklink.fcm;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FcmNotificationBuilder {
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String AUTHORIZATION = "Authorization";

    // Dev New Key
    private static final String AUTH_KEY = "key=" + "AAAADikjNhA:APA91bFTVvSEnY1err__oUebykSNjZW6zGsRlsz5DT8_HaWrghKDOrmBz-d_oDVwRrk-cecn1O_4rLihMks3I-PSTIFPztYdXXf7gQUl7vWwi-cDl532QW7r56XhvF7diNVrFVknevdJ";

    // Dev Old Key
    //   private static final String AUTH_KEY = "key=" + "AAAA3Kt-T0g:APA91bGVO0F-ChZ80evjjcJY7jsJPaTVIckPoQVdy4QPLt-4aM2j7TkJ6jSW02pXA5jTpcoVO_e23i_Dsa2ckoSx9spCSnZzJkqFO4wWSGZ-NVxQTJPstlODiFjXjAiOB4L9ZVYpTk6HcBZl5UYZ_wZLUfjQILo_Pw";

    // Stage New Key
    //private static final String AUTH_KEY = "key=" + "AAAAX9iiStQ:APA91bEYLyzEvhCgdRsbl_5xxogGIBNlcDPwqj6x6R70oJI27tKql_1_zUmAOnpSkWRH342MmFJgZ8xwknwPzrsbILribwZb5gAu1PaR1EyAiuhYnAF8FAGyFq71271usAESNFsf24O9";

    // Live New Key
    //private static final String AUTH_KEY = "key=" + "AAAAtpnPG-s:APA91bF5MdsVyLZb-Et2xAqU1SMt7EBHkKlfJ3sFWfEWEjgYNFAjEFl-y2rwbX7pM3P4wNiFUIHuMxHQhEqKzLPX8vWXS83m95BapIqPyKYK4e6h1yfLpYwu7RMMjoQH2XQN6l3UQgxV";

    // Stage Old Key
    // private static final String AUTH_KEY = "key=" + "AAAAK1Idt2I:APA91bEV5B_PDdFwZfdGpOdabz3B1mqULjkV5h4reYYA78Z5pQS8ti0c4QQih_YvK-PBM0NliIzlU6KZQD9pAUD3lVU9TnXFMid6FetlvrxTNxz4P9JfzJC1w-2hvB3lJxDZcgOF3EVR";


    private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";
    // json related keys

    private static final String KEY_TITLE = "title";
    private static final String KEY_TEXT = "message";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_UID = "uid";
    private static final String KEY_FCM_TOKEN = "fcm_token";

    private String mTitle;
    private String mMessage;
    private String mUsername;
    private String mUid;
    private String mFirebaseToken;
    private String mReceiverFirebaseToken;
    private String chatNode;

    private FcmNotificationBuilder() {

    }

    public static FcmNotificationBuilder initialize() {
        return new FcmNotificationBuilder();
    }

    public FcmNotificationBuilder title(String title) {
        mTitle = title;
        return this;
    }

    public FcmNotificationBuilder message(String message) {
        mMessage = message;
        return this;
    }

    public FcmNotificationBuilder username(String username) {
        mUsername = username;
        return this;
    }

    public FcmNotificationBuilder uid(String uid) {
        mUid = uid;
        return this;
    }

    public FcmNotificationBuilder firebaseToken(String firebaseToken) {
        mFirebaseToken = firebaseToken;
        return this;
    }

    public FcmNotificationBuilder chatNode(String chatNode) {
        chatNode = chatNode;
        return this;
    }

    public FcmNotificationBuilder receiverFirebaseToken(String receiverFirebaseToken) {
        mReceiverFirebaseToken = receiverFirebaseToken;
        return this;
    }

    public void send() {
        RequestBody requestBody = null;
        try {
            requestBody = RequestBody.create(MEDIA_TYPE_JSON, getValidJsonBody().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        assert requestBody != null;
        Request request = new Request.Builder()
                .addHeader(CONTENT_TYPE, APPLICATION_JSON)
                .addHeader(AUTHORIZATION, AUTH_KEY)
                .url(FCM_URL)
                .post(requestBody)
                .build();

        Call call = new OkHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            }
        });
    }

    private JSONObject getValidJsonBody() throws JSONException {
        JSONObject data = new JSONObject();

        data.put(KEY_TITLE, mUsername);
        data.put(KEY_TEXT, mMessage);
        data.put(KEY_USERNAME, mUsername);
        data.put(KEY_UID, mUid);
        data.put(KEY_FCM_TOKEN, mFirebaseToken);
        data.put("type", "chat");
        data.put("ChatTitle", mTitle);

        data.put("uid", mUid);

        data.put("sound", "default");

        data.put("body", mMessage);
        data.put("icon", "icon");
        data.put("title", mTitle);
        data.put("click_action", "ChatActivity");
        data.put("other_key", true);
        data.put("badge", "1");
        data.put("content_available", true);
        data.put("reference_id", chatNode);
        data.put("sound", "default");

        Map<String, Object> params = new HashMap<>();
        params.put("to", mReceiverFirebaseToken);
        params.put("title", mTitle);
        params.put("sound", "default");
        params.put("data", data);
        params.put("notification", data);

        return new JSONObject(params);
    }
}
