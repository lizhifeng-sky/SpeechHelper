package com.android.speech.helper.bean.aiui;

import org.json.JSONObject;

import java.util.List;

/**
 * Auto-generated: 2021-04-02 13:48:9
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class JsonRootBean {

    private Answer answer;
    private String category;
    private Data data;
    private String dialog_stat;
    private int rc;
    private boolean save_history;
    public List<Semantic> semantic;
    private String service;
    private String sid;
    private String text;
    private String uuid;
    private String version;

    public List<Semantic> getSemantic() {
        return semantic;
    }

    public void setSemantic(List<Semantic> semantic) {
        this.semantic = semantic;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Data getData() {
        return data;
    }

    public void setDialog_stat(String dialog_stat) {
        this.dialog_stat = dialog_stat;
    }

    public String getDialog_stat() {
        return dialog_stat;
    }

    public void setRc(int rc) {
        this.rc = rc;
    }

    public int getRc() {
        return rc;
    }

    public void setSave_history(boolean save_history) {
        this.save_history = save_history;
    }

    public boolean getSave_history() {
        return save_history;
    }


    public void setService(String service) {
        this.service = service;
    }

    public String getService() {
        return service;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getSid() {
        return sid;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

}