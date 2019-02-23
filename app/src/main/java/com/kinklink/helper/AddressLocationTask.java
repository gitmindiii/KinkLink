package com.kinklink.helper;

import android.content.Context;
import android.os.AsyncTask;


import com.google.android.gms.location.places.Place;
import com.kinklink.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AddressLocationTask extends AsyncTask<Void, Void, String> {
    private Context mContext;
    private Place place;
    private AddressLocationListner listner;
    private String city = "";
    private String state = "";
    private String country = "";
    private String result;

    public interface AddressLocationListner {
        void getLocation(String city, String state, String country, String locAddress);
    }

    public AddressLocationTask(Context mContext, Place place, AddressLocationListner listner) {
        this.mContext = mContext;
        this.place = place;
        this.listner = listner;
    }


    @Override
    protected String doInBackground(Void... voids) {
        String placeId = place.getId();

        String url = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeId + "&key=" + mContext.getResources().getString(R.string.google_maps_key);

        try {
            result = getAddressByURL(url);

            return result;
        } catch (Exception e) {
            return "error";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        if (!result.equals("")) {
            // With place id
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject object = jsonObject.getJSONObject("result");
                JSONArray add_compnents = object.getJSONArray("address_components");
                for (int i = 0; i < add_compnents.length(); i++) {
                    JSONObject obj = add_compnents.getJSONObject(i);
                    JSONArray type_array = obj.getJSONArray("types");

                    for (int j = 0; j < type_array.length(); j++) {
                        String value = type_array.getString(0);

                        if (value.equals("locality")) {
                            city = obj.getString("long_name");
                        }

                        if (value.equals("administrative_area_level_1")) {
                            state = obj.getString("long_name");
                        }

                        if (value.equals("country")) {
                            country = obj.getString("long_name");
                        }
                    }
                }

                String locAddress = object.getString("formatted_address");

                if (listner != null) listner.getLocation(city, state, country, locAddress);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private String getAddressByURL(String requestURL) {
        URL url;
        StringBuilder response = new StringBuilder();
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.setDoOutput(true);
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = br.readLine()) != null) {
                response.append(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.toString();
    }
}