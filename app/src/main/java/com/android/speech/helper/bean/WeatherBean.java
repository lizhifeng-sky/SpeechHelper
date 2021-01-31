package com.android.speech.helper.bean;

/**
 * @author lizhifeng
 * @date 2021/1/4 14:05
 */
/**
 * Copyright 2021 bejson.com
 */
import java.util.List;

/**
 * Auto-generated: 2021-01-04 14:2:10
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class WeatherBean {

    private String cityid;
    private String city;
    private String cityEn;
    private String country;
    private String countryEn;
    private String update_time;
    private List<WeatherDateBean> data;
    private Aqi aqi;
    public void setCityid(String cityid) {
        this.cityid = cityid;
    }
    public String getCityid() {
        return cityid;
    }

    public void setCity(String city) {
        this.city = city;
    }
    public String getCity() {
        return city;
    }

    public void setCityEn(String cityEn) {
        this.cityEn = cityEn;
    }
    public String getCityEn() {
        return cityEn;
    }

    public void setCountry(String country) {
        this.country = country;
    }
    public String getCountry() {
        return country;
    }

    public void setCountryEn(String countryEn) {
        this.countryEn = countryEn;
    }
    public String getCountryEn() {
        return countryEn;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }
    public String getUpdate_time() {
        return update_time;
    }

    public void setData(List<WeatherDateBean> data) {
        this.data = data;
    }
    public List<WeatherDateBean> getData() {
        return data;
    }

    public void setAqi(Aqi aqi) {
        this.aqi = aqi;
    }
    public Aqi getAqi() {
        return aqi;
    }

}
