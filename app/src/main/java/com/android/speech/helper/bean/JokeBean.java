package com.android.speech.helper.bean;

/**
 * @author lizhifeng
 * @date 2021/1/11 21:36
 */
public class JokeBean {

    private String content;



    private String hashId;
    private long unixtime;
    private String updatetime;

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setHashId(String hashId) {
        this.hashId = hashId;
    }

    public String getHashId() {
        return hashId;
    }

    public void setUnixtime(long unixtime) {
        this.unixtime = unixtime;
    }

    public long getUnixtime() {
        return unixtime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getUpdatetime() {
        return updatetime;
    }

}
