package com.android.speech.helper.aiui.handler;

import com.android.speech.helper.aiui.model.Answer;
import com.android.speech.helper.aiui.model.SemanticResult;

import org.json.JSONArray;
/**
 * 语义结果处理抽象类
 */

public abstract class IntentHandler {
    public static final String NEWLINE = "<br/>";
    public static final String NEWLINE_NO_HTML = "\n";

    public IntentHandler() {
    }

    public abstract Answer getFormatContent(SemanticResult result);

    protected String optSlotValue(SemanticResult result, String slotKey) {
        JSONArray slots = result.semantic.optJSONArray("slots");
        if (slots != null) {
            for (int index = 0; index < slots.length(); index++) {
                String key = slots.optJSONObject(index).optString("name");

                if (key.equalsIgnoreCase(slotKey)) {
                    return slots.optJSONObject(index).optString("value");
                }
            }
        }
        return null;
    }
}
