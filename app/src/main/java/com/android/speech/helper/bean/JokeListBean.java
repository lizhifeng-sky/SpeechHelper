package com.android.speech.helper.bean;

import java.util.List;

/**
 * @author lizhifeng
 * @date 2021/1/11 21:37
 */
public class JokeListBean {
    private List<JokeBean> data;
    public void setData(List<JokeBean> data) {
        this.data = data;
    }
    public List<JokeBean> getData() {
        return data;
    }

}
