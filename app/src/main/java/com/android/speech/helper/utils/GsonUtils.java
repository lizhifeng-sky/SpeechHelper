package com.android.speech.helper.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GsonUtils {
    private static Gson gson = null;

    public static Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    /**
     * Json -> Bean
     */
    public static <T> T fromJson(String json, Class<T> cls) {
        return getGson().fromJson(json, cls);
    }
}
