package com.android.speech.helper.bean.aiui;

import java.util.List;

/**
 * @author lizhifeng
 * @date 2021/4/2 14:20
 */
public class Semantic {
    private String intent;
    private List<Slots> slots;

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public List<Slots> getSlots() {
        return slots;
    }

    public void setSlots(List<Slots> slots) {
        this.slots = slots;
    }
}
