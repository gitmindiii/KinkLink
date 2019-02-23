package com.kinklink.modules.authentication.model;

/**
 * Created by mindiii on 26/5/18.
 */

public class Address {

    private String city;
    private String state;
    private String country;
    private String stAddress1;
    private String stAddress2;
    private String placeName;
    private String fullAddress;
    private String postalCode;
    private String latitude;
    private String longitude;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getStAddress1() {
        return stAddress1;
    }

    public void setStAddress1(String stAddress1) {
        this.stAddress1 = stAddress1;
    }

    public String getStAddress2() {
        return stAddress2;
    }

    public void setStAddress2(String stAddress2) {
        this.stAddress2 = stAddress2;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

}