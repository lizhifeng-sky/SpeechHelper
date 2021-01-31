package com.android.speech.helper;

import android.app.Application;
import android.util.Log;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

/**
 * @author lizhifeng
 * @date 2021/1/4 22:46
 */
public class SpeechApplication extends Application {
    @Override
    public void onCreate() {
        String param = "appid=5ff31405," +
                // 设置使用v5+
                SpeechConstant.ENGINE_MODE + "=" + SpeechConstant.MODE_MSC;
        SpeechUtility.createUtility(SpeechApplication.this, param);
        super.onCreate();
        Log.e("SpeechApplication","onCreate");
    }
}
