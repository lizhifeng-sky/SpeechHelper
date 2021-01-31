package com.iflytek;


import com.iflytek.cloud.SpeechError;

/**
 * @author lizhifeng
 * @date 2020/4/23 20:55
 */
public interface OnSpeakListener {
    void onSpeakBegin();
    void onCompleted(SpeechError speechError);
}
