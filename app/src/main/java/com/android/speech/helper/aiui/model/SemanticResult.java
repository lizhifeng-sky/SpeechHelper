package com.android.speech.helper.aiui.model;

import org.json.JSONObject;

/**
 * AIUI语义结果
 */

public class SemanticResult {
    public int rc;
    public String service;
    public String answer;
    public JSONObject data;
    public JSONObject semantic;
}
