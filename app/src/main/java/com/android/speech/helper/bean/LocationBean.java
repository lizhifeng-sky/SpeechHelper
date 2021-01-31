package com.android.speech.helper.bean;

/**
 * @author lizhifeng
 * @date 2021/1/3 16:36
 */
public class LocationBean {
    private String lng;
    private String lat;

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    @Override
    public String toString() {
        return "LocationBean{" +
                "lng='" + lng + '\'' +
                ", lat='" + lat + '\'' +
                '}';
    }
}
