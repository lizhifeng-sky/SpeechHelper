package com.android.speech.helper.bean;

import java.util.List;

/**
 * @author lizhifeng
 * @date 2021/1/11 21:37
 */
public class JokeResultBean {
    private String reason;
    private List<JokeBean> result;
    private int error_code;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<JokeBean> getResult() {
        return result;
    }

    public void setResult(List<JokeBean> result) {
        this.result = result;
    }

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }
}
