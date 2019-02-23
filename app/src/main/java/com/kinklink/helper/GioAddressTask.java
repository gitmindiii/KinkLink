package com.kinklink.helper;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

public class GioAddressTask extends AsyncTask<Void, Void, Void> {
    private WeakReference<Context> mContext;
    private LatLng latLng;
    private Address address;

    private LocationListner listner;

    public interface LocationListner{
        void onSuccess(com.kinklink.modules.authentication.model.Address address);
    }

    public GioAddressTask(Context mContext, LatLng latLng, LocationListner listner){
        this.mContext = new WeakReference<>(mContext);
        this.latLng = latLng;
        this.listner = listner;
    }


    @Override
    protected Void doInBackground(Void... voids) {

        Geocoder geocoder = new Geocoder(mContext.get(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            //addresses.toString();
            if(addresses.size()>0){
                address = addresses.get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(address!=null){
            com.kinklink.modules.authentication.model.Address adr = new com.kinklink.modules.authentication.model.Address ();
            adr.setCity(address.getLocality());
            adr.setState(address.getAdminArea());
            adr.setCountry(address.getCountryName());
            adr.setPostalCode(address.getPostalCode());
            adr.setStAddress1(address.getAddressLine(0));
            adr.setStAddress2(address.getAddressLine(1));
            adr.setLatitude(String.valueOf(latLng.latitude));
            adr.setLongitude(String.valueOf(latLng.longitude));
            adr.setPlaceName(adr.getCity() + ", " + adr.getCountry());
            if(listner!=null) listner.onSuccess(adr);
            //onSuccess(adr);
        }
    }

   //public abstract void onSuccess(com.kinklink.modules.authentication.model.Address address);
}
