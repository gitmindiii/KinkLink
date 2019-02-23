package com.kinklink.server_task;

import android.content.Context;
import android.graphics.Bitmap;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.kinklink.R;
import com.kinklink.application.KinkLink;
import com.kinklink.helper.AppHelper;
import com.kinklink.helper.CustomToast;
import com.kinklink.session.Session;
import com.kinklink.volley_request.VolleyMultipartRequest;
import com.kinklink.volley_request.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class WebService {

    private Context mContext;
    private String TAG;
    private Session session;
    private WebResponseListner mListener;

    public WebService(Context context, String TAG, WebResponseListner listener) {
        super();
        mListener = listener;
        this.mContext = context;
        this.TAG = TAG;
        session = new Session(mContext);
    }


    public void callMultiPartApi(final String url, final Map<String, String> params) {
        callMultiPartApi(url, params, null);
    }

    // for image
    public void callMultiPartApi(final String url, final Map<String, String> params, final Map<String, Bitmap> bitmapList) {
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST,
                API.BASE_URL + url, new Response.Listener<NetworkResponse>() {

            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                System.out.println(resultResponse);
                mListener.onResponse(resultResponse, url);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyErrorHandle(error);
                mListener.ErrorListener(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("authToken", session.getAuthToken());
                return headers;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                for (Map.Entry<String, Bitmap> entry : bitmapList.entrySet()) {
                    String key = entry.getKey();
                    Bitmap bitmap = entry.getValue();
                    params.put(key, new DataPart(key.concat(".jpg"), AppHelper.getFileDataFromDrawable(bitmap), "image/png"));
                }

                return params;
            }
        };

        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1f));
        VolleySingleton.getInstance(mContext.getApplicationContext()).addToRequestQueue(multipartRequest);
    }

    public void callApi(final String url, int Method, final Map<String, String> params) {
        StringRequest stringRequest = new StringRequest(Method, API.BASE_URL + url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("#" + response);
                        mListener.onResponse(response, url);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleError(error);
                        mListener.ErrorListener(error);

                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                if (params == null)
                    return super.getParams();
                else return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                if (session.getAuthToken() != null) {
                    header.put("authToken", session.getAuthToken());
                }/* else {
                    header.put("authToken", "");
                }*/
                return header;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 0, 1f));

        KinkLink.getInstance().addToRequestQueue(stringRequest, TAG);
    }

    public interface WebResponseListner {
        void onResponse(String responce, String url);

        void ErrorListener(VolleyError error);
    }

    private void handleError(VolleyError error) {
        volleyErrorHandle(error);
    }


    public void volleyErrorHandle(VolleyError error) {
        NetworkResponse networkResponse = error.networkResponse;
        String errorMessage;
        if (networkResponse == null) {
            if (error.getClass().equals(TimeoutError.class)) {
                errorMessage = "Request timeout";
                CustomToast.getInstance(mContext).showToast(mContext, errorMessage);
            } else if (error.getClass().equals(NoConnectionError.class)) {
                errorMessage = "Failed to connect server, please try again";
                CustomToast.getInstance(mContext).showToast(mContext, errorMessage);
            }
        } else {
            String result = new String(networkResponse.data);
            try {
                JSONObject response = new JSONObject(result);
                String status = response.getString("responseCode");
                String message = response.getString("message");

                if (status.equals("300")) {
                    if (message.equalsIgnoreCase("You are currently inactive by admin")) {
                        CustomToast.getInstance(mContext).showToast(mContext, mContext.getResources().getString(R.string.inactive_user));
                    } else {
                        CustomToast.getInstance(mContext).showToast(mContext, mContext.getResources().getString(R.string.session_expire));
                    }
                    session.logout();

                } else if (networkResponse.statusCode == 404) {
                    errorMessage = "Resource not found";
                    CustomToast.getInstance(mContext).showToast(mContext, errorMessage);
                } else if (networkResponse.statusCode == 500) {
                    errorMessage = message + "Oops! Something went wrong";
                    CustomToast.getInstance(mContext).showToast(mContext, errorMessage);
                } else {
                    errorMessage = ServerResponseCode.getmeesageCode(networkResponse.statusCode);
                    CustomToast.getInstance(mContext).showToast(mContext, errorMessage);
                }

            } catch (JSONException e) {
                CustomToast.getInstance(mContext).showToast(mContext, mContext.getResources().getString(R.string.went_wrong));
                e.printStackTrace();
            }
        }

    }

}
