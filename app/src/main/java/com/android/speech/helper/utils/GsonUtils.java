package com.android.speech.helper.utils;

import com.google.gson.Gson;

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
