package com.android.speech.helper.bean;

/**
 * @author lizhifeng
 * @date 2021/1/3 16:37
 */
/**
 * Copyright 2021 bejson.com
 */

import android.location.Location;
/**
 * Auto-generated: 2021-01-03 16:37:31
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class ResultBean {

    private LocationBean location;
    private int precise;
    private int confidence;
    private int comprehension;
    private String level;
    public void setLocation(LocationBean location) {
        this.location = location;
    }
    public LocationBean getLocation() {
        return location;
    }

    public void setPrecise(int precise) {
        this.precise = precise;
    }
    public int getPrecise() {
        return precise;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }
    public int getConfidence() {
        return confidence;
    }

    public void setComprehension(int comprehension) {
        this.comprehension = comprehension;
    }
    public int getComprehension() {
        return comprehension;
    }

    public void setLevel(String level) {
        this.level = level;
    }
    public String getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return "ResultBean{" +
                "location=" + location +
                ", precise=" + precise +
                ", confidence=" + confidence +
                ", comprehension=" + comprehension +
                ", level='" + level + '\'' +
                '}';
    }
}
